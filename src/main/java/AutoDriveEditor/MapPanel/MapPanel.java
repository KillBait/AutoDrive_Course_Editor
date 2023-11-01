package AutoDriveEditor.MapPanel;

import AutoDriveEditor.Listeners.MouseListener;
import AutoDriveEditor.Managers.CopyPasteManager;
import AutoDriveEditor.RoadNetwork.MapNode;
import AutoDriveEditor.RoadNetwork.RoadMap;
import AutoDriveEditor.Utils.Classes.CoordinateChanger;
import AutoDriveEditor.Utils.Classes.LabelNumberFilter;
import AutoDriveEditor.Utils.Classes.NameableThread;
import AutoDriveEditor.Utils.ProfileUtils;

import javax.swing.*;
import javax.swing.text.PlainDocument;
import java.awt.*;
import java.awt.event.*;
import java.awt.geom.Point2D;
import java.awt.geom.Rectangle2D;
import java.awt.image.BufferedImage;
import java.awt.image.RasterFormatException;
import java.math.BigDecimal;
import java.math.RoundingMode;
import java.util.ArrayList;
import java.util.Objects;
import java.util.concurrent.*;
import java.util.concurrent.locks.Lock;
import java.util.concurrent.locks.ReentrantLock;

import static AutoDriveEditor.AutoDriveEditor.*;
import static AutoDriveEditor.GUI.Buttons.Curves.CubicCurveButton.cubicCurve;
import static AutoDriveEditor.GUI.Buttons.Curves.CubicCurveButton.isCubicCurveCreated;
import static AutoDriveEditor.GUI.Buttons.Curves.QuadCurveButton.isQuadCurveCreated;
import static AutoDriveEditor.GUI.Buttons.Curves.QuadCurveButton.quadCurve;
import static AutoDriveEditor.GUI.Buttons.Editing.RotationButton.rotation;
import static AutoDriveEditor.GUI.GUIBuilder.mapPanel;
import static AutoDriveEditor.GUI.GUIImages.negativeHeightWarningImage;
import static AutoDriveEditor.GUI.GUIImages.overlapWarningImage;
import static AutoDriveEditor.GUI.MenuBuilder.*;
import static AutoDriveEditor.Listeners.MouseListener.prevMousePosX;
import static AutoDriveEditor.Listeners.MouseListener.prevMousePosY;
import static AutoDriveEditor.Locale.LocaleManager.getLocaleString;
import static AutoDriveEditor.Managers.CopyPasteManager.SCREEN_COORDINATES;
import static AutoDriveEditor.Managers.CopyPasteManager.getSelectionBounds;
import static AutoDriveEditor.Managers.MultiSelectManager.*;
import static AutoDriveEditor.Managers.ScanManager.scanNetworkForOverlapNodes;
import static AutoDriveEditor.Managers.ScanManager.searchDistance;
import static AutoDriveEditor.MapPanel.MapImage.*;
import static AutoDriveEditor.RoadNetwork.MapNode.NODE_FLAG_STANDARD;
import static AutoDriveEditor.RoadNetwork.MapNode.NODE_WARNING_OVERLAP;
import static AutoDriveEditor.Utils.GUIUtils.showInTextArea;
import static AutoDriveEditor.Utils.ImageUtils.*;
import static AutoDriveEditor.Utils.LoggerUtils.LOG;
import static AutoDriveEditor.Utils.MathUtils.*;
import static AutoDriveEditor.XMLConfig.EditorXML.*;
import static AutoDriveEditor.XMLConfig.GameXML.autoSaveGameConfigFile;
import static AutoDriveEditor.XMLConfig.RoutesXML.autoSaveRouteManagerXML;

public class MapPanel extends JPanel {

    public static final int CONNECTION_STANDARD = 0;
    @SuppressWarnings("unused")
    public static final int CONNECTION_SUBPRIO = 1; // never used as subprio routes are based on a nodes .flag value
    public static final int CONNECTION_DUAL = 2;
    public static final int CONNECTION_REVERSE = 3;

    public static final int CONFIG_SAVEGAME = 1;
    public static final int CONFIG_ROUTEMANAGER = 2;

    public static int configType;

    public Thread nodeDrawThread;
    public Thread connectionDrawThread;
    public static ScheduledExecutorService scheduledExecutorService;
    @SuppressWarnings("rawtypes")
    public static ScheduledFuture scheduledFuture;
    public static final Lock drawLock = new ReentrantLock();
    private static CountDownLatch latch;
    public static volatile boolean canAutoSave= true;

    public static BufferedImage croppedImage;

    public int offsetX, oldOffsetX;
    public int offsetY, oldOffsetY;
    public int widthScaled, oldWidthScaled;
    public int heightScaled, oldHeightScaled;
    public static boolean isUsingImportedImage = false;
    private static double x = 0.5;
    private static double y = 0.5;
    public static double zoomLevel = 1.0;
    public static int mapZoomFactor = 1;

    public static boolean stale = false;
    public static RoadMap roadMap;
    public static MapNode hoveredNode = null;
    public static boolean isDraggingMap = false;
    public static CopyPasteManager cnpManager;

    public static boolean bIsShiftPressed;

    public MapPanel() {

        MouseListener mouseListener = new MouseListener(this);
        addMouseListener(mouseListener);
        addMouseMotionListener(mouseListener);
        addMouseWheelListener(mouseListener);

        InputMap iMap = this.getInputMap(JComponent.WHEN_IN_FOCUSED_WINDOW);
        ActionMap aMap = this.getActionMap();

        iMap.put(KeyStroke.getKeyStroke("F"), "Focus");
        iMap.put(KeyStroke.getKeyStroke(KeyEvent.VK_SHIFT, InputEvent.SHIFT_DOWN_MASK),"ee");
        iMap.put(KeyStroke.getKeyStroke(KeyEvent.VK_SHIFT,0, true),"ef");
        aMap.put("Focus", new AbstractAction() {
            @Override
            public void actionPerformed(ActionEvent e) {
                if (hoveredNode != null) {
                    centreNodeInMapPanel(hoveredNode);
                }
            }
        });
        aMap.put("ee", new AbstractAction() {
            @Override
            public void actionPerformed(ActionEvent e) {
                bIsShiftPressed = true;
            }
        });

        aMap.put("ef", new AbstractAction() {
            @Override
            public void actionPerformed(ActionEvent e) {
                bIsShiftPressed = false;
            }
        });

        setDoubleBuffered(true);

        addComponentListener(new ComponentAdapter(){
            @Override
            public void componentResized(ComponentEvent e) {
                getNewBackBufferImage(mapPanel.getWidth(), mapPanel.getHeight());
                // Part 2 of work around for map resize bug, force a refresh of all the values
                // used to redraw the map.
                forceMapImageRedraw();
            }
        });

        setFocusable(true);

        // start the thread responsible for drawing node connections

        ConnectionDrawThread t1 = new ConnectionDrawThread();
        connectionDrawThread = new Thread(t1 ,"ConnectionDraw Thread");
        connectionDrawThread.start();

        // start the thread responsible for drawing nodes

        NodeDrawThread t2 = new NodeDrawThread();
        nodeDrawThread = new Thread(t2 ,"NodeDraw Thread");
        nodeDrawThread.start();

        // initialize the copy/paste manager

        if (cnpManager == null) {
            LOG.info("Initializing CopyPaste Manager");
            cnpManager = new CopyPasteManager();
        }

        if (bAutoSaveEnabled) {
            startAutoSaveThread();
        } else {
            LOG.info("AutoSave is disabled");
        }
    }

    public static void startAutoSaveThread() {
        LOG.info("Starting AutoSave Thread");

        scheduledExecutorService = Executors.newSingleThreadScheduledExecutor( new NameableThread(Executors.defaultThreadFactory(), "AutoSave"));
        scheduledFuture = scheduledExecutorService.scheduleAtFixedRate(() -> {
            if (roadMap != null && mapPanelImage != null) {
                if (configType == CONFIG_SAVEGAME) {
                    autoSaveGameConfigFile();
                } else if (configType == CONFIG_ROUTEMANAGER) {
                    autoSaveRouteManagerXML();
                }
            }
        }, autoSaveInterval, autoSaveInterval,TimeUnit.MINUTES);
        LOG.info("Started AutoSave Thread ( Interval in Minutes {} , Max Slots {} )", autoSaveInterval, maxAutoSaveSlots);
    }

    public static void stopAutoSaveThread() {
        if (scheduledExecutorService != null) {
            scheduledExecutorService.shutdown();
            try {
                if (!scheduledExecutorService.awaitTermination(2 , TimeUnit.SECONDS)) {
                    scheduledExecutorService.shutdownNow();
                    if (!scheduledExecutorService.awaitTermination(3 , TimeUnit.SECONDS)) {
                        LOG.info("AutoSave thread failed to shut down after 5 seconds");
                    }
                } else {
                    LOG.info("AutoSave thread stopped");
                }
            } catch (InterruptedException e) {
                e.printStackTrace();
                scheduledExecutorService.shutdownNow();
            }
        }
    }

    public static void restartAutoSaveThread() {
        LOG.info("Restarting AutoSave Thread");
        if (scheduledExecutorService != null) {
            stopAutoSaveThread();
            startAutoSaveThread();
        } else {
            LOG.info("Failed to restart AutoSave Thread ( Thread not active )");
        }
    }

    //
    // The NodeDraw thread is 2 or 3 times quicker to execute than connectionDraw(), so we
    // try and spread the draw load around by doing the curve/line/rectangle drawing here
    //

    public static class NodeDrawThread implements Runnable {

        private static volatile boolean isStopped = false;

        private static class TextDisplayStore {
            String text;
            Point2D position;
            Color colour;
            boolean useBackground;

            public TextDisplayStore(String text, Point2D textPos, Color textColour, boolean background) {
                this.text = text;
                this.position = textPos;
                this.colour = textColour;
                this.useBackground = background;
            }
        }

        public static void stop() {
            LOG.info("Stopping NodeDraw thread");
            isStopped = true;
        }

        @Override
        public synchronized void run() {

            ArrayList<TextDisplayStore> textList = new ArrayList<>();
            LOG.info("Starting NodeDraw thread");
            ProfileUtils nodeDrawTimer = new ProfileUtils();

            while ( !isStopped ) {

                try {
                    this.wait();
                } catch (InterruptedException e) {
                    if (isStopped) {
                        LOG.info("NodeDraw Thread exiting");
                        return;
                    }


                    if (bDebugProfile) nodeDrawTimer.startTimer();

                    int width = getMapPanel().getWidth();
                    int height = getMapPanel().getHeight();

                    double nodeSizeScaled = nodeSize * zoomLevel;
                    double nodeSizeScaledHalf = nodeSizeScaled * 0.5;
                    double nodeSizeScaledQuarter = nodeSizeScaled * 0.25;

                    FontMetrics fm = backBufferGraphics.getFontMetrics();

                    if (backBufferGraphics != null) {

                        //
                        // Draw all nodes in visible area of map
                        // The original code we would draw all the nodes even if they were not visible
                        //

                        for (MapNode mapNode : RoadMap.networkNodesList) {
                            Point2D nodePos = worldPosToScreenPos(mapNode.x, mapNode.z);
                            if (0 < nodePos.getX() && width > nodePos.getX() && 0 < nodePos.getY() && height > nodePos.getY()) {
                                if (bDebugProfile) nodeDrawTimer.pauseTimer();
                                drawLock.lock();

                                try {
                                    if (bDebugProfile) nodeDrawTimer.restartTimer();
                                    if (mapNode.flag == NODE_FLAG_STANDARD) {
                                        backBufferGraphics.setColor(colourNodeRegular);
                                    } else {
                                        backBufferGraphics.setColor(colourNodeSubprio);
                                    }
                                    backBufferGraphics.fillArc((int) (nodePos.getX() - nodeSizeScaledQuarter), (int) (nodePos.getY() - nodeSizeScaledQuarter), (int) nodeSizeScaledHalf, (int) nodeSizeScaledHalf, 0, 360);

                                    if (mapNode.isSelected) {
                                        if (!mapNode.isInSelectionArea) {
                                            backBufferGraphics.setColor(colourNodeSelected);
                                            Graphics2D g1 = (Graphics2D) backBufferGraphics.create();
                                            BasicStroke bs = new BasicStroke((float) (nodeSizeScaledQuarter / 2.5));
                                            g1.setStroke(bs);
                                            g1.drawArc((int) (nodePos.getX() - nodeSizeScaledQuarter), (int) (nodePos.getY() - nodeSizeScaledQuarter), (int) nodeSizeScaledHalf, (int) nodeSizeScaledHalf, 0, 360);
                                            g1.dispose();
                                        }
                                    } else {
                                        if (mapNode.isInSelectionArea) {
                                            backBufferGraphics.setColor(colourNodeSelected);
                                            Graphics2D g1 = (Graphics2D) backBufferGraphics.create();
                                            BasicStroke bs = new BasicStroke((float) (nodeSizeScaledQuarter / 2.5));
                                            g1.setStroke(bs);
                                            g1.drawArc((int) (nodePos.getX() - nodeSizeScaledQuarter), (int) (nodePos.getY() - nodeSizeScaledQuarter), (int) nodeSizeScaledHalf, (int) nodeSizeScaledHalf, 0, 360);
                                            g1.dispose();
                                        }
                                    }

                                    if (mapNode.hasWarning) {
                                        if (mapNode.warningType == NODE_WARNING_OVERLAP) {
                                            int overlapImageWidth = overlapWarningImage.getWidth();
                                            int overlapImageHeight = overlapWarningImage.getHeight();
                                            backBufferGraphics.drawImage(overlapWarningImage, (int) (nodePos.getX() - (overlapImageWidth / 2)), (int) (nodePos.getY() - (overlapImageHeight / 2)), overlapImageWidth, overlapImageHeight, null);
                                        }
                                    } else {
                                        if (mapNode.y == -1) {
                                            int negativeImageWidth = negativeHeightWarningImage.getWidth();
                                            int negativeImageHeight = negativeHeightWarningImage.getHeight();
                                            backBufferGraphics.drawImage(negativeHeightWarningImage, (int) (nodePos.getX() - (negativeImageWidth / 2)), (int) (nodePos.getY() - (negativeImageHeight / 2)), negativeImageWidth, negativeImageHeight, null);
                                        }
                                    }
                                } finally {
                                    drawLock.unlock();
                                }
                            }

                            // show the node ID if we in debug mode, the higher the node count, the more text spam there is :-P
                            // It will affect editor speed, the more nodes the worse it will get, you have been warned :)

                            if (bDebugShowID) {
                                String text = String.valueOf(mapNode.id);
                                Rectangle2D rect = fm.getStringBounds(text, backBufferGraphics);
                                Point2D newPoint =  new Point2D.Double(nodePos.getX() - (rect.getWidth() / 2) - 1, (nodePos.getY() + (rect.getHeight() / 2) - 3));
                                textList.add(new TextDisplayStore(String.valueOf(mapNode.id), newPoint, Color.WHITE, false));
                            }

                            if (mapNode.hasMapMarker()) {
                                if (mapNode.getMarkerName() != null) {
                                    Point2D nodeScreenPos = worldPosToScreenPos(mapNode.x - 1, mapNode.z - 1);
                                    String markerText = mapNode.getMarkerName();
                                    if ((hoveredNode != null) && (mapNode == hoveredNode)) {
                                        markerText +=" ( " + mapNode.getMarkerGroup() + " )";
                                    }
                                    textList.add(new TextDisplayStore(markerText, nodeScreenPos, Color.WHITE, false));
                                }
                            }
                        }

                        // do we drawToScreen the node hover-over image and add the marker name/group to the drawToScreen list

                        if (hoveredNode != null) {
                            Point2D hoverNodePos = worldPosToScreenPos(hoveredNode.x, hoveredNode.z);
                            if (bDebugProfile) nodeDrawTimer.pauseTimer();
                            drawLock.lock();
                            try {
                                if (bDebugProfile) nodeDrawTimer.restartTimer();
                                if (!hoveredNode.isControlNode()) {
                                    backBufferGraphics.setColor(colourNodeSelected);
                                    Graphics2D g2 = (Graphics2D) backBufferGraphics.create();
                                    BasicStroke bs = new BasicStroke((float) (nodeSizeScaledQuarter / 2.5));
                                    g2.setStroke(bs);
                                    g2.drawArc((int) (hoverNodePos.getX() - nodeSizeScaledQuarter), (int) (hoverNodePos.getY() - nodeSizeScaledQuarter), (int) nodeSizeScaledHalf, (int) nodeSizeScaledHalf, 0, 360);
                                    g2.dispose();
                                }
                            } finally {
                                drawLock.unlock();
                            }

                            if (bDebugShowSelectedLocation) {
                                String nodeInfo;
                                if (!bDebugShowID) {
                                    nodeInfo = "ID = " + hoveredNode.id + " >> ";
                                } else {
                                    nodeInfo = " ";
                                }
                                nodeInfo += "X = " + hoveredNode.x + ", Y = " + hoveredNode.y + ", Z = " + hoveredNode.z + ", Flag = " + hoveredNode.flag + ", In = " + hoveredNode.incoming.size() + ", Out = " + hoveredNode.outgoing.size();
                                if (hoveredNode.hasWarning) nodeInfo +=" , " + (hoveredNode.warningNodes.size() + 1) + " Overlapping Nodes";
                                Point2D nodePosMarker = worldPosToScreenPos(hoveredNode.x + 1, hoveredNode.z);
                                textList.add( new TextDisplayStore( nodeInfo, nodePosMarker, Color.WHITE, false));
                            }

                            if (hoveredNode.hasWarning && !bDebugShowSelectedLocation ) {
                                String text = (hoveredNode.warningNodes.size() + 1) + " Nodes Overlapping";
                                Point2D nodePosMarker = worldPosToScreenPos(hoveredNode.x + 1, hoveredNode.z);
                                textList.add( new TextDisplayStore( text, nodePosMarker, Color.WHITE, true));

                            }

                            if (hoveredNode.y == -1 ) {
                                String text = "Node Y is invalid ( -1 )";
                                Point2D nodePosMarker = worldPosToScreenPos(hoveredNode.x + 1, hoveredNode.z);
                                textList.add( new TextDisplayStore( text, nodePosMarker, Color.WHITE, true));
                            }
                        }

                        // display all the text we need to render

                        if (textList.size() > 0) {
                            if (bDebugProfile) nodeDrawTimer.pauseTimer();
                            drawLock.lock();
                            try {
                                if (bDebugProfile) nodeDrawTimer.restartTimer();
                                for (TextDisplayStore list : textList) {
                                    backBufferGraphics.setColor(list.colour);
                                    if (list.useBackground) {
                                        //FontMetrics fm = backBufferGraphics.getFontMetrics();
                                        Rectangle2D rect = fm.getStringBounds(list.text, backBufferGraphics);

                                        backBufferGraphics.setColor(Color.YELLOW);
                                        backBufferGraphics.fillRect((int)list.position.getX(),
                                                (int) list.position.getY() - fm.getAscent(),
                                                (int) rect.getWidth(),
                                                (int) rect.getHeight() + 2);
                                        backBufferGraphics.setColor(Color.BLACK);
                                    }
                                    backBufferGraphics.drawString(list.text, (int) list.position.getX(), (int) list.position.getY());
                                }
                            } finally {
                                drawLock.unlock();
                            }
                        }
                    }

                    // Draw anything the buttons need

                    if (bDebugProfile) nodeDrawTimer.pauseTimer();
                    drawLock.lock();
                    try {
                        if (bDebugProfile) nodeDrawTimer.restartTimer();
                        buttonManager.draw(backBufferGraphics, drawLock, nodeSizeScaledQuarter, nodeSizeScaledHalf);
                    } finally {
                        drawLock.unlock();
                    }

                    // draw the right button selection rectangle

                    if (isMultiSelectDragging) {
                        Point2D mousePos = new Point2D.Float(prevMousePosX,prevMousePosY);
                        Point2D rectWorldStart = worldPosToScreenPos(rectangleStart.getX(), rectangleStart.getY());
                        int diffX = (int) (mousePos.getX() - rectWorldStart.getX());
                        int diffY = (int) (mousePos.getY() - rectWorldStart.getY());
                        int rectangleX = (int) rectWorldStart.getX();
                        int rectangleY = (int) rectWorldStart.getY();
                        if (diffX < 0) {
                            rectangleX += diffX;
                            diffX = -diffX;
                        }
                        if (diffY < 0) {
                            rectangleY += diffY;
                            diffY = -diffY;
                        }

                        Graphics2D gTemp = (Graphics2D) backBufferGraphics.create();
                        BasicStroke bsDash = new BasicStroke(1, BasicStroke.CAP_BUTT,
                                BasicStroke.JOIN_ROUND, 1.0f, new float[]{10f, 0f, 2f}, 2f);
                        gTemp.setStroke(bsDash);

                        if (bDebugProfile) nodeDrawTimer.pauseTimer();
                        drawLock.lock();
                        try {
                            if (bDebugProfile) nodeDrawTimer.restartTimer();
                            gTemp.setColor(Color.WHITE);
                            gTemp.drawRect(rectangleX, rectangleY, diffX, diffY);
                        } finally {
                            drawLock.unlock();
                            gTemp.dispose();
                        }
                    }

                    if (bDebugProfile) {
                        nodeDrawTimer.stopTimer();
                        String text = "Finished Node Rendering in " + nodeDrawTimer.getTime() + " ms";
                        showInTextArea(text,false, false);
                        nodeDrawTimer.resetTimer();
                    }

                    if (isMultipleSelected && bShowSelectionBounds) {
                        if (bDebugProfile) nodeDrawTimer.pauseTimer();
                        drawLock.lock();
                        try {
                            if (bDebugProfile) nodeDrawTimer.restartTimer();
                            CopyPasteManager.selectionAreaInfo selectionInfo = getSelectionBounds(multiSelectList/*, WORLD_COORDINATES*/);
                            Graphics2D gTemp = (Graphics2D) backBufferGraphics.create();

                            gTemp.setColor(Color.WHITE);
                            Point2D topLeft = selectionInfo.getSelectionStart(SCREEN_COORDINATES);
                            Point2D bottomRight = selectionInfo.getSelectionEnd(SCREEN_COORDINATES);
                            double rectSizeX = bottomRight.getX() - topLeft.getX();
                            double rectSizeY = bottomRight.getY() - topLeft.getY();
                            gTemp.drawRect((int) (topLeft.getX() - nodeSizeScaledQuarter), (int) (topLeft.getY() - nodeSizeScaledQuarter), (int) (rectSizeX + (nodeSizeScaledQuarter * 2)), (int) (rectSizeY + (nodeSizeScaledQuarter * 2)));
                            gTemp.dispose();
                        } finally {
                            drawLock.unlock();
                        }
                    }

                    textList.clear();
                    latch.countDown();
                }
            }

        }
    }

    //
    // The connection drawing thread finishes last in almost all cases, so we keep this as small as possible
    // we only drawToScreen the connections in the visible area (plus some extra padding) so we don't see the
    // connections clipping.
    //

    public static class ConnectionDrawThread implements Runnable {
        private static volatile boolean isStopped = false;
        private final ArrayList<DrawList> dualSubprioArrowDrawList = new ArrayList<>();
        private final ArrayList<DrawList> dualArrowDrawList = new ArrayList<>();
        private final ArrayList<DrawList> reverseArrowDrawList = new ArrayList<>();
        private final ArrayList<DrawList> reverseSubprioArrowDrawList = new ArrayList<>();
        private final ArrayList<DrawList> subprioArrowDrawList = new ArrayList<>();
        private final ArrayList<DrawList> regularArrowDrawList = new ArrayList<>();

        private static class DrawList {
            Point2D startPos;
            Point2D endPos;
            boolean isDual;

            public DrawList(Point2D start, Point2D end, boolean dual) {
                this.startPos = start;
                this.endPos = end;
                this.isDual = dual;
            }
        }

        public static void stop() {
            LOG.info("Stopping ConnectionDraw Thread");
            isStopped = true;
        }

        @Override
        public synchronized void run() {

            LOG.info("Starting ConnectionDraw Thread");
            ProfileUtils connectionDrawTimer = new ProfileUtils();

            while ( !isStopped ) {

                try {
                    dualSubprioArrowDrawList.clear();
                    dualArrowDrawList.clear();
                    reverseArrowDrawList.clear();
                    reverseSubprioArrowDrawList.clear();
                    subprioArrowDrawList.clear();
                    regularArrowDrawList.clear();
                    this.wait();
                } catch (InterruptedException e) {
                    if (isStopped) {
                        LOG.info("ConnectionDraw Thread exiting");
                        return;
                    }

                    if (bDebugProfile) connectionDrawTimer.startTimer();

                    if (backBufferGraphics != null) {

                        int width = getMapPanel().getWidth();
                        int height = getMapPanel().getHeight();

                        for (MapNode mapNode : RoadMap.networkNodesList) {
                            Point2D nodePos = worldPosToScreenPos(mapNode.x, mapNode.z);
                            double offScreenDistance = 40 * zoomLevel;
                            if (0 - offScreenDistance < nodePos.getX() && width + offScreenDistance > nodePos.getX() && 0 - offScreenDistance < nodePos.getY() && height + offScreenDistance > nodePos.getY()) {
                                for (MapNode outgoing : mapNode.outgoing) {
                                    Point2D outPos = worldPosToScreenPos(outgoing.x, outgoing.z);
                                    if (RoadMap.isDual(mapNode, outgoing)) {
                                        if (!bHideDualConnection) {
                                            if ( mapNode.flag == 1) {
                                                dualSubprioArrowDrawList.add(new DrawList(nodePos, outPos, true));
                                            } else {
                                                dualArrowDrawList.add(new DrawList(nodePos, outPos, true));
                                            }
                                        }
                                    } else if (RoadMap.isReverse(mapNode, outgoing)) {
                                        if (!bHideReverseConnection) {
                                            if ( mapNode.flag == 1) {
                                                reverseSubprioArrowDrawList.add(new DrawList(nodePos, outPos, false));
                                            } else {
                                                reverseArrowDrawList.add(new DrawList(nodePos, outPos, false));
                                            }
                                        }
                                    } else {
                                        if (mapNode.flag == 1) {
                                            if (!bHideSubprioConnection) subprioArrowDrawList.add(new DrawList(nodePos, outPos, false));
                                        } else {
                                            if (!bHideRegularConnection) regularArrowDrawList.add(new DrawList(nodePos, outPos, false));
                                        }
                                    }
                                }
                            }
                        }

                        // draw all the connection arrows

                        connectionDrawTimer.pauseTimer();
                        drawLock.lock();
                        try {
                            connectionDrawTimer.restartTimer();
                            batchDrawArrowBetween(backBufferGraphics, colourConnectDualSubprio, dualSubprioArrowDrawList);
                            batchDrawArrowBetween(backBufferGraphics, colourConnectDual, dualArrowDrawList);
                            batchDrawArrowBetween(backBufferGraphics, colourConnectReverse, reverseArrowDrawList);
                            batchDrawArrowBetween(backBufferGraphics, colourConnectReverseSubprio, reverseSubprioArrowDrawList);
                            batchDrawArrowBetween(backBufferGraphics, colourConnectSubprio, subprioArrowDrawList);
                            batchDrawArrowBetween(backBufferGraphics, colourConnectRegular, regularArrowDrawList);
                        } finally {
                            drawLock.unlock();
                        }
                    }

                    if (bDebugProfile) {
                        connectionDrawTimer.stopTimer();
                        String text = "Finished Connection Rendering in " + connectionDrawTimer.getTime() + " ms";
                        showInTextArea(text, false, false);
                        connectionDrawTimer.resetTimer();
                    }
                    latch.countDown();
                }
            }
        }
    }

    // Draw the snap grid

    public synchronized void drawGrid() {

        int worldMax = 1024 * mapZoomFactor;
        Point2D panelWorldTopLeft = screenPosToWorldPos(0,0);
        Point2D panelWorldBottomRight = screenPosToWorldPos(MapPanel.getMapPanel().getWidth(),MapPanel.getMapPanel().getHeight());

        if (zoomLevel > 2 ) {
            Color colour = new Color(25,25,25);
            backBufferGraphics.setPaint(colour);
            for (double worldX = 0; worldX < worldMax; worldX += gridSpacingX) {
                if ( worldX < panelWorldBottomRight.getX()) {
                    Point2D worldStart = worldPosToScreenPos(worldX, panelWorldTopLeft.getY());
                    Point2D worldEnd = worldPosToScreenPos(worldX, panelWorldBottomRight.getY());
                    backBufferGraphics.drawLine((int) worldStart.getX(), (int) worldStart.getY(), (int) worldEnd.getX(), (int) worldEnd.getY());
                }
                if ( -worldX > panelWorldTopLeft.getX()) {
                    Point2D worldStart = worldPosToScreenPos(-worldX, panelWorldTopLeft.getY());
                    Point2D worldEnd = worldPosToScreenPos(-worldX, panelWorldBottomRight.getY());
                    backBufferGraphics.drawLine( (int) worldStart.getX(), (int) worldStart.getY(), (int) worldEnd.getX(), (int) worldEnd.getY());
                }
            }
            for (double worldY = 0; worldY < worldMax; worldY += gridSpacingY) {
                if ( worldY > panelWorldTopLeft.getY() && worldY < panelWorldBottomRight.getY() ) {
                    Point2D worldStart = worldPosToScreenPos(panelWorldTopLeft.getX(), worldY);
                    Point2D worldEnd = worldPosToScreenPos(panelWorldBottomRight.getX(), worldY);
                    backBufferGraphics.drawLine((int) worldStart.getX(), (int) worldStart.getY(), (int) worldEnd.getX(), (int) worldEnd.getY());
                }
                if (-worldY < panelWorldBottomRight.getY()) {
                    Point2D worldStart = worldPosToScreenPos(panelWorldTopLeft.getX(), -worldY);
                    Point2D worldEnd = worldPosToScreenPos(panelWorldBottomRight.getX(), -worldY);
                    backBufferGraphics.drawLine( (int) worldStart.getX(), (int) worldStart.getY(), (int) worldEnd.getX(), (int) worldEnd.getY());
                }
            }
        }
    }

    @Override
    protected void paintComponent(Graphics g) {
        super.paintComponent(g);

        if (bDebugProfile) {
            showInTextArea("", true, false);
        }

        if (mapPanelImage != null) {
            backBufferGraphics.drawImage(croppedImage, 0, 0, this.getWidth(), this.getHeight(), null);

            if (bShowGrid) drawGrid();

            if (roadMap != null) {
                latch = new CountDownLatch(2);

                connectionDrawThread.interrupt();
                nodeDrawThread.interrupt();

                try {
                    latch.await();
                } catch (InterruptedException e) {
                    e.printStackTrace();
                }

                g.drawImage(backBufferImage, 0, 0, null);
            }
        }
    }

    private void getResizedMap() throws RasterFormatException {
        if (mapPanelImage != null) {
            widthScaled = (int) (this.getWidth() / zoomLevel) + 1;
            heightScaled = (int) (this.getHeight() / zoomLevel) + 1;

            //LOG.info("topLeft = {} , bottomRight = {}", screenPosToWorldPos(0,0), screenPosToWorldPos(this.getWidth(), this.getHeight()));

            // Part 1 of work around for map resize bug, increase the zoomLevel
            // if widthScaled and heightScaled are bigger than the map image dimensions
            //
            // This will get us close, but the zoomLevel is still off by a small
            // amount and just moving the map in any direction will force MoveMapBy()
            // to run again and recalculate all the values so when run again ResizeMap()
            // will calculate it correctly.

            if ( x + widthScaled > mapPanelImage.getWidth() ) {
                while ( widthScaled > mapPanelImage.getWidth() ) {
                    double step = -1 * (zoomLevel * 0.1);
                    if (bDebugLogZoomScale) LOG.info("widthScaled is out of bounds ( {} ) .. increasing zoomLevel by {}", widthScaled, step);
                    zoomLevel -= step;
                    widthScaled = (int) (this.getWidth() / zoomLevel);
                }
                if (bDebugLogZoomScale) LOG.info("widthScaled is {}", widthScaled);
            }

            if ( (int) y + heightScaled > mapPanelImage.getHeight() ) {
                while ( heightScaled > mapPanelImage.getHeight() ) {
                    double step = -1 * (zoomLevel * 0.1);
                    if (bDebugLogZoomScale) LOG.info("heightScaled is out of bounds ( {} ) .. increasing zoomLevel by {}", heightScaled, step);
                    zoomLevel -= step;
                    heightScaled = (int) (this.getHeight() / zoomLevel);
                }
                if (bDebugLogZoomScale) LOG.info("heightScaled is {}", heightScaled);
            }

            double calcX = (((this.getWidth() * 0.5) / zoomLevel) / mapPanelImage.getWidth());
            double calcY = (((this.getHeight() * 0.5) / zoomLevel) / mapPanelImage.getHeight());

            x = Math.min(x, 1 - calcX);
            x = Math.max(x, calcX);
            y = Math.min(y, 1 - calcY);
            y = Math.max(y, calcY);

            int centerX = (int) (x * mapPanelImage.getWidth());
            int centerY = (int) (y * mapPanelImage.getHeight());

            offsetX = Math.max(1, (centerX - (widthScaled / 2)));
            offsetY = Math.max(1, (centerY - (heightScaled / 2)));
            if (offsetY +  heightScaled > pdaImage.getWidth()) heightScaled = pdaImage.getHeight() - offsetY;

            if (offsetX != oldOffsetX || offsetY != oldOffsetY || widthScaled != oldWidthScaled || heightScaled != oldHeightScaled) {
                try {
                    croppedImage = mapPanelImage.getSubimage(offsetX, offsetY, widthScaled, heightScaled);
                    oldOffsetX = offsetX;
                    oldOffsetY = offsetY;
                    oldWidthScaled = widthScaled;
                    oldHeightScaled = heightScaled;
                } catch (Exception e) {
                    LOG.info("## MapPanel.ResizeMap() ## Exception in getSubImage()");
                    LOG.info("## MapPanel.ResizeMap() ## x = {} , y = {} , offsetX = {} , offsetY = {}  -- width = {} , height = {} , zoomLevel = {} , widthScaled = {} , heightScaled = {}", x, y, offsetX, offsetY, this.getWidth(), this.getHeight(), zoomLevel, widthScaled, heightScaled);
                    e.printStackTrace();
                }
            }
        }
    }

    public void moveMapBy(int diffX, int diffY) {
        if ((roadMap == null) || (mapPanelImage == null)) {
            return;
        }
        x -= diffX / (zoomLevel * mapPanelImage.getWidth());
        y -= diffY / (zoomLevel * mapPanelImage.getHeight());

        getResizedMap();
        this.repaint();
    }

    public void increaseZoomLevelBy(int rotations) {

        if ((roadMap == null) || (mapPanelImage == null)) {
            return;
        }



        if (((this.getWidth()/(zoomLevel - rotations)) > mapPanelImage.getWidth()) || ((this.getHeight()/(zoomLevel - rotations)) > mapPanelImage.getHeight())){
            if (bDebugLogZoomScale) {
                LOG.info("## increaseZoomLevelBy() ##  Failed size check");
                if (((this.getWidth()/(zoomLevel - rotations)) > mapPanelImage.getWidth())) LOG.info("## new zoom level exceeds mapPanel width ({})", mapPanelImage.getWidth());
                if (((this.getHeight()/(zoomLevel - rotations)) > mapPanelImage.getHeight())) LOG.info("## new zoom level exceeds mapPanel height ({})", mapPanelImage.getWidth());
            }
            return;
        }

        if ((zoomLevel - rotations) >=0 && (zoomLevel - rotations) < maxZoomLevel) {
            zoomLevel = limitDoubleToDecimalPlaces(zoomLevel - rotations, 1, RoundingMode.UP);
            if (bDebugLogZoomScale) LOG.info("new zoomLevel = {}", zoomLevel);
            getResizedMap();
            this.repaint();
        }
    }

    // Work In Progress for v1.1.0 - not yet working/usable
    //
    // function :- getNodeAtWorldPosition(double worldPosX, double worldPosZ)
    //
    // TODO - convert the node size on screen to world size and use as search area around node co-ordinates

    @SuppressWarnings("unused")
    public static MapNode getNodeAtWorldPosition(double worldPosX, double worldPosZ) {
        MapNode selected = null;

        if ((roadMap != null) && (mapPanelImage != null)) {

            double currentNodeSize = nodeSize * zoomLevel * 0.5;
            double nodeSizeScaledHalf = (currentNodeSize * 0.5);
            double nodeSizeScaledQuarter = (currentNodeSize * 0.25);

            // make sure we prioritize returning control nodes over regular nodes

            for (MapNode mapNode : RoadMap.networkNodesList) {
                if (worldPosX < mapNode.x + nodeSizeScaledHalf && worldPosX > mapNode.x - nodeSizeScaledHalf && worldPosZ < mapNode.z + nodeSizeScaledHalf && worldPosZ > mapNode.z - nodeSizeScaledHalf) {
                    selected = mapNode;
                    break;
                }
            }

            if (quadCurve != null && isQuadCurveCreated) {
                MapNode cpNode = quadCurve.getControlPoint();
                if (worldPosX < cpNode.x + nodeSizeScaledHalf && worldPosX > cpNode.x - nodeSizeScaledHalf && worldPosZ < cpNode.z + nodeSizeScaledHalf && worldPosZ > cpNode.z - nodeSizeScaledHalf) {
                    return quadCurve.getControlPoint();
                }
            }
            if (cubicCurve != null && isCubicCurveCreated) {
                MapNode cp1Node = cubicCurve.getControlPoint1();
                if (worldPosX < cp1Node.x + nodeSizeScaledHalf && worldPosX > cp1Node.x - nodeSizeScaledHalf && worldPosZ < cp1Node.z + nodeSizeScaledHalf && worldPosZ > cp1Node.z - nodeSizeScaledHalf) {
                    return cubicCurve.getControlPoint1();
                }

                MapNode cp2Node = cubicCurve.getControlPoint2();
                if (worldPosX < cp2Node.x + nodeSizeScaledHalf && worldPosX > cp2Node.x - nodeSizeScaledHalf && worldPosZ < cp2Node.z + nodeSizeScaledHalf && worldPosZ > cp2Node.z - nodeSizeScaledHalf) {
                    return cubicCurve.getControlPoint2();
                }
            }

            if (rotation != null && Objects.equals(buttonManager.getCurrentButtonID(),"RotateButton")) {
                MapNode rotateControlNode = rotation.getControlNode();
                if (worldPosX < rotateControlNode.x + nodeSizeScaledQuarter && worldPosX > rotateControlNode.x - nodeSizeScaledQuarter && worldPosZ < rotateControlNode.z + nodeSizeScaledQuarter && worldPosZ > rotateControlNode.z - nodeSizeScaledQuarter) {
                    return rotation.getControlNode();
                }
            }
        }
        return selected;
    }

    public static MapNode getNodeAtScreenPosition(double worldPosX, double worldPosZ) {

        MapNode selected = null;

        if ((roadMap != null) && (mapPanelImage != null)) {

            Point2D outPos;
            double currentNodeSize = nodeSize * zoomLevel * 0.5;
            double nodeSizeScaledHalf = (currentNodeSize * 0.5);
            double nodeSizeScaledQuarter = (currentNodeSize * 0.25);

            // make sure we prioritize returning control nodes over regular nodes

            for (MapNode mapNode : RoadMap.networkNodesList) {
                outPos = worldPosToScreenPos(mapNode.x, mapNode.z);
                if (worldPosX < outPos.getX() + nodeSizeScaledHalf && worldPosX > outPos.getX() - nodeSizeScaledHalf && worldPosZ < outPos.getY() + nodeSizeScaledHalf && worldPosZ > outPos.getY() - nodeSizeScaledHalf) {
                    selected = mapNode;
                    break;
                }
            }

            if (quadCurve != null && isQuadCurveCreated) {
                Point2D cpPosition = worldPosToScreenPos(quadCurve.getControlPoint().x, quadCurve.getControlPoint().z);
                if (worldPosX < cpPosition.getX() + nodeSizeScaledHalf && worldPosX > cpPosition.getX() - nodeSizeScaledHalf && worldPosZ < cpPosition.getY() + nodeSizeScaledHalf && worldPosZ > cpPosition.getY() - nodeSizeScaledHalf) {
                    return quadCurve.getControlPoint();
                }
            }
            if (cubicCurve != null && isCubicCurveCreated) {
                Point2D cp1Position = worldPosToScreenPos(cubicCurve.getControlPoint1().x, cubicCurve.getControlPoint1().z);
                if (worldPosX < cp1Position.getX() + nodeSizeScaledHalf && worldPosX > cp1Position.getX() - nodeSizeScaledHalf && worldPosZ < cp1Position.getY() + nodeSizeScaledHalf && worldPosZ > cp1Position.getY() - nodeSizeScaledHalf) {
                    return cubicCurve.getControlPoint1();
                }

                Point2D cp2Position = worldPosToScreenPos(cubicCurve.getControlPoint2().x, cubicCurve.getControlPoint2().z);
                if (worldPosX < cp2Position.getX() + nodeSizeScaledHalf && worldPosX > cp2Position.getX() - nodeSizeScaledHalf && worldPosZ < cp2Position.getY() + nodeSizeScaledHalf && worldPosZ > cp2Position.getY() - nodeSizeScaledHalf) {
                    return cubicCurve.getControlPoint2();
                }
            }

            if (rotation != null && Objects.equals(buttonManager.getCurrentButtonID(),"RotateButton")) {
                Point2D rotatePosition = worldPosToScreenPos(rotation.getControlNode().x, rotation.getControlNode().z);
                if (worldPosX < rotatePosition.getX() + nodeSizeScaledQuarter && worldPosX > rotatePosition.getX() - nodeSizeScaledQuarter && worldPosZ < rotatePosition.getY() + nodeSizeScaledQuarter && worldPosZ > rotatePosition.getY() - nodeSizeScaledQuarter) {
                    return rotation.getControlNode();
                }
            }
        }
        return selected;
    }



    public static double getYValueFromHeightMap(double worldX, double worldZ) {
        if (heightMapImage != null) {
            double x, y;

            double scaleX = (double) mapPanelImage.getWidth() / (double)heightMapImage.getWidth();
            double scaleY = (double) mapPanelImage.getHeight() / (double)heightMapImage.getHeight();
            if (bDebugLogHeightMapInfo) LOG.info("heightmap scale = {} , {}", scaleX, scaleY);

            x = ((double)heightMapImage.getWidth() / 2) + (int) Math.floor((worldX / mapZoomFactor) / scaleX );
            y = ((double)heightMapImage.getHeight() / 2) + (int) Math.floor((worldZ / mapZoomFactor) / scaleY );

            if (x <0) x = 0;
            if (y <0) y = 0;
            if (x > heightMapImage.getWidth()) x = heightMapImage.getWidth() - 1;
            if (y > heightMapImage.getHeight()) y = heightMapImage.getHeight() - 1;

            Color color = new Color(heightMapImage.getRGB((int)x, (int)y));
            BigDecimal bd = new BigDecimal(Double.toString((float)((color.getRed()<<8) + color.getGreen()) / 256));
            bd = bd.setScale(3, RoundingMode.HALF_UP);
            return bd.doubleValue() / heightMapScale;
        }
        return -1;
    }

     public static Point2D screenPosToWorldPos(int screenX, int screenY) {

        double topLeftX = (x * mapPanelImage.getWidth()) - ((getMapPanel().getWidth() / zoomLevel)/2);
        double topLeftY = (y * mapPanelImage.getHeight()) - ((getMapPanel().getHeight() / zoomLevel)/2);

        double diffScaledX = (double)screenX / zoomLevel;
        double diffScaledY = (double)screenY / zoomLevel;

        int centerPointOffsetX = (mapPanelImage.getWidth() / 2) * mapZoomFactor;
        int centerPointOffsetY = (mapPanelImage.getHeight() / 2) * mapZoomFactor;

        double worldPosX = roundUpDoubleToDecimalPlaces(((topLeftX + diffScaledX) * mapZoomFactor) - centerPointOffsetX,3);
        double worldPosY = roundUpDoubleToDecimalPlaces(((topLeftY + diffScaledY) * mapZoomFactor) - centerPointOffsetY, 3);

        return new Point2D.Double(worldPosX, worldPosY);
    }

    public static Point worldPosToScreenPos(double worldX, double worldZ) {

        int centerPointOffset = 1024 * mapZoomFactor;

        worldX += centerPointOffset;
        worldZ += centerPointOffset;

        double scaledX = (worldX/mapZoomFactor) * zoomLevel;
        double scaledY = (worldZ/mapZoomFactor) * zoomLevel;

        double centerXScaled = (x * (mapPanelImage.getWidth()*zoomLevel));
        double centerYScaled = (y * (mapPanelImage.getHeight()*zoomLevel));

        double topLeftX = centerXScaled - (mapPanel.getWidth() / 2F);
        double topLeftY = centerYScaled - (mapPanel.getHeight()/ 2F);

        return new Point((int) (scaledX - topLeftX), (int) (scaledY - topLeftY));
    }

    public static void createConnectionBetween(MapNode start, MapNode target, int type) {
        if (start == target) {
            return;
        }

        if (!start.outgoing.contains(target)) {
            start.outgoing.add(target);

            if (type == CONNECTION_STANDARD) {
                if (!target.incoming.contains(start)) {
                    target.incoming.add(start);
                }
            } else if (type == CONNECTION_REVERSE) {
                start.incoming.remove(target);
                target.incoming.remove(start);
                target.outgoing.remove(start);
            } else if (type == CONNECTION_DUAL) {
                if (!target.incoming.contains(start)) {
                    target.incoming.add(start);
                }
                if (!target.outgoing.contains(start)) {
                    target.outgoing.add(start);
                }
                if (!start.incoming.contains(target)) {
                    start.incoming.add(target);
                }
            }
        } else {
            if (type == CONNECTION_STANDARD) {
                start.outgoing.remove(target);
                target.incoming.remove(start);
            } else if (type == CONNECTION_REVERSE) {
                start.outgoing.remove(target);
                start.incoming.remove(target);
                target.outgoing.remove(start);
                target.incoming.remove(start);

            } else if (type == CONNECTION_DUAL) {
                start.outgoing.remove(target);
                start.incoming.remove(target);
                target.incoming.remove(start);
                target.outgoing.remove(start);
            }
        }
    }

    public static void batchDrawArrowBetween(Graphics g, Color colour, ArrayList<ConnectionDrawThread.DrawList> nodeList) {
        if (nodeList.size() >0 ) {
            double startX;
            double startY;
            double targetX;
            double targetY;
            final double relativeNodeSize = nodeSize * zoomLevel;

            g.setColor(colour);

            for (ConnectionDrawThread.DrawList mapNode : nodeList) {
                startX = mapNode.startPos.getX();
                startY = mapNode.startPos.getY();
                targetX = mapNode.endPos.getX();
                targetY = mapNode.endPos.getY();

                double vecX = startX - targetX;
                double vecY = startY - targetY;

                double angleRad = Math.atan2(vecY, vecX);

                //angleRad = normalizeAngle(angleRad);

                // calculate where to start the line based around the circumference of the node

                double distCos = (relativeNodeSize * 0.25) * Math.cos(angleRad);
                double distSin = (relativeNodeSize * 0.25) * Math.sin(angleRad);

                double lineStartX = startX - distCos;
                double lineStartY = startY - distSin;

                // calculate where to finish the line based around the circumference of the node
                double lineEndX = targetX + distCos;
                double lineEndY = targetY + distSin;

                g.drawLine((int) lineStartX, (int) lineStartY, (int) lineEndX, (int) lineEndY);

                // only draw the arms of the arrow if the zoom level is high enough to be seen

                if (zoomLevel > 2.5) {
                    double arrowLength = relativeNodeSize * 0.70;

                    double arrowLeft = normalizeAngle(angleRad + Math.toRadians(-20));
                    double arrowLeftX = targetX + Math.cos(arrowLeft) * arrowLength;
                    double arrowLeftY = targetY + Math.sin(arrowLeft) * arrowLength;

                    double arrowRight = normalizeAngle(angleRad + Math.toRadians(20));
                    double arrowRightX = targetX + Math.cos(arrowRight) * arrowLength;
                    double arrowRightY = targetY + Math.sin(arrowRight) * arrowLength;


                    if (bFilledArrows) {
                        Polygon p = new Polygon();
                        p.addPoint((int) lineEndX, (int) lineEndY);
                        p.addPoint((int) arrowLeftX, (int) arrowLeftY);
                        p.addPoint((int) arrowRightX, (int) arrowRightY);
                        g.fillPolygon(p);
                    } else {
                        g.drawLine((int) lineEndX, (int) lineEndY, (int) arrowLeftX, (int) arrowLeftY);
                        g.drawLine((int) lineEndX, (int) lineEndY, (int) arrowRightX, (int) arrowRightY);
                    }

                    if (mapNode.isDual) {
                        angleRad = normalizeAngle(angleRad+Math.PI);

                        arrowLeft = normalizeAngle(angleRad + Math.toRadians(-20));
                        arrowRight = normalizeAngle(angleRad + Math.toRadians(20));

                        arrowLeftX = startX + Math.cos(arrowLeft) * arrowLength;
                        arrowLeftY = startY + Math.sin(arrowLeft) * arrowLength;
                        arrowRightX = startX + Math.cos(arrowRight) * arrowLength;
                        arrowRightY = startY + Math.sin(arrowRight) * arrowLength;

                        if (bFilledArrows) {
                            Polygon p = new Polygon();
                            p.addPoint((int) lineStartX, (int) lineStartY);
                            p.addPoint((int) arrowLeftX, (int) arrowLeftY);
                            p.addPoint((int) arrowRightX, (int) arrowRightY);
                            g.fillPolygon(p);
                        } else {
                            g.drawLine((int) lineStartX, (int) lineStartY, (int) arrowLeftX, (int) arrowLeftY);
                            g.drawLine((int) lineStartX, (int) lineStartY, (int) arrowRightX, (int) arrowRightY);
                        }
                    }
                }
            }
        }
    }

    /**
     * Draws an arrow between two points, all the specified locations must be
     * screen space co-ordinates
     *
     * @param g Graphics context the line will be drawn to
     * @param startNode Start point of the connection arrow
     * @param targetNode End point of the connection arrow
     * @param dual Should it be drawn as a dual connection
     */

    public static void drawArrowBetween(Graphics g, Point2D startNode, Point2D targetNode, boolean dual) {

        double startX = startNode.getX();
        double startY = startNode.getY();
        double targetX = targetNode.getX();
        double targetY = targetNode.getY();


        double vecX = startX - targetX;
        double vecY = startY - targetY;

        double angleRad = Math.atan2(vecY, vecX);

        angleRad = normalizeAngle(angleRad);

        // calculate where to start the line based around the circumference of the node

        double distCos = ((nodeSize * zoomLevel) * 0.25) * Math.cos(angleRad);
        double distSin = ((nodeSize * zoomLevel) * 0.25) * Math.sin(angleRad);

        double lineStartX = startX - distCos;
        double lineStartY = startY - distSin;

        // calculate where to finish the line based around the circumference of the node
        double lineEndX = targetX + distCos;
        double lineEndY = targetY + distSin;

        double lineLength = Point2D.distance(startX, startY, targetX, targetY);

        if (lineLength > (nodeSize * zoomLevel) / 2) {
            g.drawLine((int) lineStartX, (int) lineStartY, (int) lineEndX, (int) lineEndY);
        }

        if (zoomLevel > 2.5) {
            double arrowLength = (nodeSize * zoomLevel) * 0.70;


            if (lineLength < arrowLength) {
                if (bDebugLogLinearlineInfo) LOG.info("distance = {}, nodeSize = {}", lineLength, (nodeSize * zoomLevel) / 2);
                return;
            }

            double arrowLeft = normalizeAngle(angleRad + Math.toRadians(-20));
            double arrowLeftX = targetX + Math.cos(arrowLeft) * arrowLength;
            double arrowLeftY = targetY + Math.sin(arrowLeft) * arrowLength;

            double arrowRight = normalizeAngle(angleRad + Math.toRadians(20));
            double arrowRightX = targetX + Math.cos(arrowRight) * arrowLength;
            double arrowRightY = targetY + Math.sin(arrowRight) * arrowLength;

            if (bFilledArrows) {
                Polygon p = new Polygon();
                p.addPoint((int) lineEndX, (int) lineEndY);
                p.addPoint((int) arrowLeftX, (int) arrowLeftY);
                p.addPoint((int) arrowRightX, (int) arrowRightY);
                g.fillPolygon(p);
            } else {
                g.drawLine((int) lineEndX, (int) lineEndY, (int) arrowLeftX, (int) arrowLeftY);
                g.drawLine((int) lineEndX, (int) lineEndY, (int) arrowRightX, (int) arrowRightY);
            }

            if (dual) {
                angleRad = normalizeAngle(angleRad+Math.PI);

                arrowLeft = normalizeAngle(angleRad + Math.toRadians(-20));
                arrowRight = normalizeAngle(angleRad + Math.toRadians(20));

                arrowLeftX = startNode.getX() + Math.cos(arrowLeft) * arrowLength;
                arrowLeftY = startNode.getY() + Math.sin(arrowLeft) * arrowLength;
                arrowRightX = startNode.getX() + Math.cos(arrowRight) * arrowLength;
                arrowRightY = startNode.getY() + Math.sin(arrowRight) * arrowLength;

                if (bFilledArrows) {
                    Polygon p = new Polygon();
                    p.addPoint((int) lineStartX, (int) lineStartY);
                    p.addPoint((int) arrowLeftX, (int) arrowLeftY);
                    p.addPoint((int) arrowRightX, (int) arrowRightY);
                    g.fillPolygon(p);
                } else {
                    g.drawLine((int) lineStartX, (int) lineStartY, (int) arrowLeftX, (int) arrowLeftY);
                    g.drawLine((int) lineStartX, (int) lineStartY, (int) arrowRightX, (int) arrowRightY);
                }
            }
        }
    }

    //
    // Mouse movement and drag detection
    //

    public void mouseMoved(int mousePosX, int mousePosY) {
        if (mapPanelImage != null) {
            if (bDebugShowHeightMapInfo) {
                if (heightMapImage != null) {
                    double x, y;
                    Point2D point = screenPosToWorldPos(mousePosX, mousePosY);

                    double scaleX = (double) heightMapImage.getWidth() / mapPanelImage.getWidth();
                    double scaleY = (double) heightMapImage.getHeight() / mapPanelImage.getHeight();
                    if (bDebugLogHeightMapInfo) LOG.info("heightmap scale = {} , {}", scaleX, scaleY);

                    x = (int) ((point.getX() + (mapPanelImage.getWidth() / 2)) * scaleX);
                    y = (int) ((point.getY() + (mapPanelImage.getHeight() / 2)) * scaleY);
                    if (bDebugLogHeightMapInfo) LOG.info(" - mapZoomFactor {} - halfWidth {} , halfHeight {} :: halfPointX {} , halfPointY {}", mapZoomFactor, heightMapImage.getWidth() / 2, heightMapImage.getHeight() / 2, (point.getX() / mapZoomFactor), (point.getY() / mapZoomFactor));
                    if (bDebugLogHeightMapInfo) LOG.info(" - heightmap coordinates {} , {} - Point coordinates {} , {}", x, y, point.getX(), point.getY());

                    Color color = new Color(heightMapImage.getRGB((int)x, (int)y));
                    double heightValue = (double)((color.getRed()<<8) + color.getGreen()) / 256;
                    String colourText="Heightmap Red = " + color.getRed() + " , Green = " + color.getGreen() + " -- Calculated Y Value = " + heightValue / heightMapScale + " ( " + heightValue + " / " + heightMapScale + " ) --";
                    showInTextArea(colourText, true, false);
                    String pointerText = "HeightMap (Size = " + (heightMapImage.getWidth() - 1) + ") HeightMap X = " + x + ", Y =" + y;
                    showInTextArea(pointerText, false, false);
                }
            }

            MapNode cursorPosNode = getNodeAtScreenPosition(mousePosX, mousePosY);
            if (cursorPosNode != hoveredNode) {
                hoveredNode = cursorPosNode;
                this.repaint();
            }
        }
    }

    public void mouseDragged(int mousePosX, int mousePosY) {
        int diffX = mousePosX - prevMousePosX;
        int diffY = mousePosY - prevMousePosY;

        if (isDraggingMap) {
            moveMapBy(diffX, diffY);
        }

        if (isMultiSelectDragging) {
            if (mousePosX > mapPanel.getWidth()) getMapPanel().moveMapBy( -10, 0);
            if (mousePosX < 0) getMapPanel().moveMapBy( 10, 0);
            if (mousePosY > mapPanel.getHeight()) getMapPanel().moveMapBy( 0, -10);
            if (mousePosY < 0) getMapPanel().moveMapBy( 0, 10);
        }
    }

    //
    // Left mouse button click/pressed/released states
    //

    public void mouseButton1Clicked(int ignoredMousePosX, int ignoredMousePosY) {}

    public void mouseButton1Pressed(int mousePosX, int mousePosY) {
        if (!bMiddleMouseMove) isDraggingMap = true;

        MapNode pressedNode = getNodeAtScreenPosition(mousePosX, mousePosY);
        if (pressedNode != null) {
            isDraggingMap = false;
        }
    }

    public void mouseButton1Released(int ignoredMousePosX, int ignoredMousePosY) {
        if (!bMiddleMouseMove) isDraggingMap = false;
    }

    //
    // Middle mouse button click/pressed/released states
    //

    public void mouseButton2Clicked(int ignoredMousePosX, int ignoredMousePosY) {}

    public void mouseButton2Pressed(int ignoredMousePosX, int ignoredMousePosY) {
        if (bMiddleMouseMove) {
            isDraggingMap = true;
        }
    }

    public void mouseButton2Released() {
        if (bMiddleMouseMove) isDraggingMap = false;
    }

    public void mouseButton3Clicked(int ignoredMousePosX, int ignoredMousePosY) {}

    public void mouseButton3Pressed(int ignoredMousePosX, int ignoredMousePosY) {}

    public void mouseButton3Released(int ignoredMousePosX, int ignoredMousePosY) {}

    /**
     * Loops over all nodes in network and moves out-of-bounds nodes to the centre (x,z)=(0,0). The bounds are based
     * on the dimensions of MapPanel.MapPanelImage and the current mapZoomFactor.
     */
    public static void fixOutOfBoundsNodes() {
        final boolean DEBUG = false;

        if (roadMap != null) {

            //determine bounds
            int centerPointOffsetX = (mapPanelImage.getWidth() / 2) * mapZoomFactor;
            int centerPointOffsetY = (mapPanelImage.getHeight() / 2) * mapZoomFactor;

            String bounds = String.format("\n    %d <= X <= %d\n    %d <= Z <= %d",-centerPointOffsetX,centerPointOffsetX,-centerPointOffsetY,centerPointOffsetY);
            int result = JOptionPane.showConfirmDialog(editor, getLocaleString("dialog_fix_out-of-bound_nodes") + bounds, "AutoDrive Editor", JOptionPane.OK_CANCEL_OPTION, JOptionPane.QUESTION_MESSAGE);
            if (result == JOptionPane.OK_OPTION) {

                CoordinateChanger coordChanger = new CoordinateChanger();
                MapNode firstMapNode = null;
                for (MapNode node : RoadMap.networkNodesList) {
                    if ((node.x > centerPointOffsetX) || (node.x < -centerPointOffsetX) || (node.z > centerPointOffsetY) || (node.z < -centerPointOffsetY)) {
                        if (DEBUG) LOG.info("## fixOutOfBoundsNodes() ## found out-of-bounds node: ID={}, X={}, Y={}, Z={}", node.id, node.x, node.z, node.z );
                        coordChanger.addCoordinateChange(node, 0, node.y, 0);
                        node.x = 0.0;
                        node.z = 0.0;
                        //store first node found
                        if (firstMapNode==null)
                            firstMapNode = node;
                    }
                }
                // Centre screen on first node found and add changes to change manager
                if (firstMapNode != null) {
                    centreNodeInMapPanel(firstMapNode);
                    changeManager.addChangeable(coordChanger);
                }
            }
             else {
                LOG.info("Cancelled out-of-bound node fix.");
            }
        }
    }


    public static void fixNodeHeight() {
        if (roadMap != null) {
            int result = JOptionPane.showConfirmDialog(editor, getLocaleString("dialog_fix_node_height"), "AutoDrive Editor", JOptionPane.OK_CANCEL_OPTION, JOptionPane.QUESTION_MESSAGE);
            if (result == JOptionPane.OK_OPTION) {
                for (MapNode node : RoadMap.networkNodesList) {
                    double heightMapY = getYValueFromHeightMap(node.x, node.z);
                    if (node.y == -1) {
                        node.y = heightMapY;
                    }
                }
            } else {
                LOG.info("Cancelled node fix");
            }
        }
    }


    public static void centreNodeDialog() {
        if (roadMap != null && mapPanelImage != null ) {
            int result = mapPanel.showCentreNodeDialog();
            if (result != -1) {
                MapNode node = RoadMap.networkNodesList.get(result);
                Point2D target = worldPosToScreenPos(node.x, node.z);
                double x = (getMapPanel().getWidth() >> 1) - target.getX();
                double y = (getMapPanel().getHeight() >> 1) - target.getY();
                getMapPanel().moveMapBy((int)x,(int)y);
            }
        }
    }

    public static void centreNodeInMapPanel(MapNode node) {
        Point2D target = worldPosToScreenPos(node.x, node.z);
        double x = (getMapPanel().getWidth() >> 1) - target.getX();
        double y = (getMapPanel().getHeight() >> 1) - target.getY();
        getMapPanel().moveMapBy((int) x, (int) y);
    }

    //
    // Dialog for Centre Node
    //

    public Integer showCentreNodeDialog() {

        JTextField centreNode = new JTextField(String.valueOf(1));
        JLabel labelNode = new JLabel(" ");
        PlainDocument docX = (PlainDocument) centreNode.getDocument();
        docX.setDocumentFilter(new LabelNumberFilter(labelNode, 0, RoadMap.networkNodesList.size(), false, false));

        Object[] inputFields = {getLocaleString("dialog_centre_node"), centreNode, labelNode};

        int option = JOptionPane.showConfirmDialog(this, inputFields, ""+ getLocaleString("dialog_centre_node_title"), JOptionPane.OK_CANCEL_OPTION, JOptionPane.QUESTION_MESSAGE);

        if (option == JOptionPane.OK_OPTION) {
            return Integer.parseInt(centreNode.getText()) - 1 ;
        }
        return -1;
    }

    //
    // Dialog for Merge Nodes
    //

    public void showScanDialog() {

        JTextField mergeDistance = new JTextField(String.valueOf(searchDistance));
        JLabel labelDistance = new JLabel(" ");
        PlainDocument docX = (PlainDocument) mergeDistance.getDocument();
        docX.setDocumentFilter(new LabelNumberFilter(labelDistance, 0, 2048 * mapZoomFactor, true, false));

        Object[] inputFields = {getLocaleString("dialog_scan_area"), mergeDistance, labelDistance};

        int option = JOptionPane.showConfirmDialog(this, inputFields, ""+ getLocaleString("dialog_scan_title"), JOptionPane.OK_CANCEL_OPTION, JOptionPane.QUESTION_MESSAGE);

        if (option == JOptionPane.OK_OPTION) {
            scanNetworkForOverlapNodes(Double.parseDouble(mergeDistance.getText()), true);
        }
    }

   //
   // getters
   //

   public static boolean isStale() {
       return stale;
   }

   public static MapPanel getMapPanel() {
        return mapPanel;
    }
   public static void forceMapImageRedraw() {
        mapPanel.oldWidthScaled = 0;
        MapPanel.getMapPanel().getResizedMap();
        MapPanel.getMapPanel().moveMapBy(0,1); // hacky way to get map image to refresh
        mapPanel.repaint();
    }
   public RoadMap getRoadMap() {
        return roadMap;
    }

   public void setRoadMap(RoadMap roadMap) {
        MapPanel.roadMap = roadMap;
    }

   public void setMapZoomFactor(int newZoomFactor) {
        MapPanel.mapZoomFactor = newZoomFactor;
    }

    //
    // setters
    //

   public static void setStale(boolean newStaleState) {
        if (isStale() != newStaleState) {
            stale = newStaleState;
            editor.setTitle(createWindowTitleString());
        }
        if (configType == CONFIG_SAVEGAME) {
            saveConfigMenuItem.setEnabled(isStale());
        } else if (configType == CONFIG_ROUTEMANAGER) {
            saveRoutesXML.setEnabled(isStale());
        }
    }
}
