package AutoDriveEditor.MapPanel;

import javax.swing.*;
import javax.swing.text.AttributeSet;
import javax.swing.text.DocumentFilter;
import javax.swing.text.PlainDocument;
import java.awt.*;
import java.awt.event.ComponentAdapter;
import java.awt.event.ComponentEvent;
import java.awt.geom.Point2D;
import java.awt.geom.Rectangle2D;
import java.awt.image.RasterFormatException;
import java.text.Collator;
import java.util.ArrayList;
import java.util.Collections;
import java.util.LinkedList;
import java.util.Objects;
import java.util.concurrent.*;
import java.util.concurrent.locks.Lock;
import java.util.concurrent.locks.ReentrantLock;

import AutoDriveEditor.GUI.GUIBuilder;
import AutoDriveEditor.Listeners.KeyboardListener;
import AutoDriveEditor.Listeners.MouseListener;
import AutoDriveEditor.Managers.CopyPasteManager;
import AutoDriveEditor.RoadNetwork.MapMarker;
import AutoDriveEditor.RoadNetwork.MapNode;
import AutoDriveEditor.RoadNetwork.MarkerGroup;
import AutoDriveEditor.RoadNetwork.RoadMap;

import static AutoDriveEditor.AutoDriveEditor.*;
import static AutoDriveEditor.GUI.GUIBuilder.*;
import static AutoDriveEditor.GUI.GUIImages.*;
import static AutoDriveEditor.Utils.GUIUtils.*;
import static AutoDriveEditor.GUI.MenuBuilder.*;
import static AutoDriveEditor.Listeners.MouseListener.*;
import static AutoDriveEditor.Locale.LocaleManager.*;
import static AutoDriveEditor.Managers.ChangeManager.*;
import static AutoDriveEditor.Managers.ScanManager.*;
import static AutoDriveEditor.MapPanel.MapImage.*;
import static AutoDriveEditor.RoadNetwork.MapNode.*;
import static AutoDriveEditor.Utils.LoggerUtils.*;
import static AutoDriveEditor.Utils.MathUtils.*;
import static AutoDriveEditor.XMLConfig.EditorXML.*;
import static AutoDriveEditor.XMLConfig.GameXML.*;
import static AutoDriveEditor.XMLConfig.RouteManagerXML.*;

public class MapPanel extends JPanel{

    public static final int CONNECTION_STANDARD = 0;
    public static final int CONNECTION_SUBPRIO = 1; // never used as subprio routes are based on a nodes .flag value
    public static final int CONNECTION_DUAL = 2;
    public static final int CONNECTION_REVERSE = 3;

    public static final int CONFIG_SAVEGAME = 1;
    public static final int CONFIG_ROUTEMANAGER = 2;

    public static int configType;

    public Thread nodeDrawThread;
    public Thread connectionDrawThread;
    public static ScheduledExecutorService scheduledExecutorService;
    public static ScheduledFuture scheduledFuture;
    private static final Lock drawLock = new ReentrantLock();
    private static CountDownLatch latch;
    public static volatile boolean canAutoSave= true;


    public int offsetX, oldOffsetX;
    public int offsetY, oldOffsetY;
    public int widthScaled, oldWidthScaled;
    public int heightScaled, oldHeightScaled;
    public static boolean isUsingConvertedImage = false;
    private static double x = 0.5;
    private static double y = 0.5;
    public static double zoomLevel = 1.0;
    public static int mapZoomFactor = 1;
    public static double nodeSize = 1.0;

    public static boolean stale = false;

    public static RoadMap roadMap;
    public static MapNode hoveredNode = null;
    private MapNode movingNode = null;
    private static MapNode selected = null;

    private boolean isDragging = false;
    public static  boolean isDraggingNode = false;

    private static Point2D rectangleStart;
    private static Point2D rectangleEnd;
    public boolean isMultiSelectAllowed = false;
    public static boolean isMultipleSelected = false;
    public static LinkedList<MapNode> multiSelectList  = new LinkedList<>();

    public boolean isDraggingRoute = false;
    private static boolean isControlNodeSelected = false;
    public static boolean isQuadCurveCreated = false;
    public static boolean isCubicCurveCreated = false;
    public static QuadCurve quadCurve;
    public static CubicCurve cubicCurve;
    public static LinearLine linearLine;
    public static int connectionType = 0; // 0 = regular , 1 = dual, 2 = reverse

    public static int createRegularConnectionState = 0;
    public static int createDualConnectionState = 0;
    public static int createReverseConnectionState = 0;

    public static LinkedList<NodeLinks> deleteNodeList = new LinkedList<>();
    public static int moveDiffX, moveDiffY;
    public static double preSnapX, preSnapY;
    public static CopyPasteManager cnpManager;

    private static final Color BROWN = new Color(152, 104, 50 );


    public MapPanel() {

        MouseListener mouseListener = new MouseListener(this);
        addMouseListener(mouseListener);
        addMouseMotionListener(mouseListener);
        addMouseWheelListener(mouseListener);

        addComponentListener(new ComponentAdapter(){
            @Override
            public void componentResized(ComponentEvent e) {
                getNewBackBufferImage(mapPanel.getWidth(), mapPanel.getHeight());
                // Part 2 of work around for map resize bug, force a refresh of all the values
                // used to redraw the map.
                forceMapImageRedraw();
            }
        });

        KeyboardListener keyListener = new KeyboardListener();
        setFocusable(true);
        addKeyListener(keyListener);

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

        LOG.info("Starting AutoSave Thread");
        startAutoSaveThread();

    }

    public static void startAutoSaveThread() {
        scheduledExecutorService = Executors.newScheduledThreadPool(1);

        scheduledFuture = scheduledExecutorService.scheduleAtFixedRate(() -> {
            //LOG.info("Executed!");
            if (roadMap != null && image != null) {
                if (configType == CONFIG_SAVEGAME) {
                    autoSaveGameConfigFile();
                } else if (configType == CONFIG_ROUTEMANAGER) {
                    autoSaveRouteManagerXML();
                }
            }
        }, autoSaveInterval, autoSaveInterval,TimeUnit.MINUTES);
    }

    public static void restartAutoSaveThread() {
        scheduledExecutorService.shutdownNow();
        LOG.info("restarting AutoSave Thread");
        startAutoSaveThread();
    }



    //
    // The NodeDraw thread is at least 2 or 3 times quicker to execute that connectionDraw(), so we
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


            while ( !isStopped ) {

                try {
                    this.wait();
                } catch (InterruptedException e) {
                    if (isStopped) {
                        LOG.info("NodeDraw Thread exiting");
                        return;
                    }

                    long startTime = 0;

                    if (bDebugProfile) {
                        startTime = System.currentTimeMillis();
                    }

                    int width = getMapPanel().getWidth();
                    int height = getMapPanel().getHeight();

                    int sizeScaled = (int) (nodeSize * zoomLevel);
                    int sizeScaledHalf = (int) (sizeScaled * 0.5);

                    if (backBufferGraphics != null) {

                        LinkedList<MapNode> mapNodes = RoadMap.mapNodes;

                        //
                        // Draw all nodes in visible area of map
                        // The original code would draw all the nodes even if they were not visible
                        //

                        drawLock.lock();
                        try {
                            for (MapNode mapNode : mapNodes) {
                                Point2D nodePos = worldPosToScreenPos(mapNode.x, mapNode.z);
                                if (0 < nodePos.getX() && width > nodePos.getX() && 0 < nodePos.getY() && height > nodePos.getY()) {
                                    if (mapNode.hasWarning && mapNode.isSelected) {
                                        backBufferGraphics.drawImage(nodeImageSelected, (int) (nodePos.getX() - sizeScaledHalf), (int) (nodePos.getY() - sizeScaledHalf), sizeScaled, sizeScaled, null);
                                    } else if (mapNode.isSelected && mapNode.flag == 0) {
                                        backBufferGraphics.drawImage(nodeImageSelected, (int) (nodePos.getX() - sizeScaledHalf), (int) (nodePos.getY() - sizeScaledHalf), sizeScaled, sizeScaled, null);
                                    } else if (mapNode.isSelected && mapNode.flag == 1) {
                                        backBufferGraphics.drawImage(subPrioNodeImageSelected, (int) (nodePos.getX() - sizeScaledHalf), (int) (nodePos.getY() - sizeScaledHalf), sizeScaled, sizeScaled, null);
                                    } else if (mapNode.flag == 1) {
                                        backBufferGraphics.drawImage(subPrioNodeImage, (int) (nodePos.getX() - sizeScaledHalf), (int) (nodePos.getY() - sizeScaledHalf), sizeScaled, sizeScaled, null);
                                    } else {
                                        backBufferGraphics.drawImage(nodeImage, (int) (nodePos.getX() - sizeScaledHalf), (int) (nodePos.getY() - sizeScaledHalf), sizeScaled, sizeScaled, null);
                                    }

                                    if (mapNode.hasWarning) {
                                        if (mapNode.warningType == NODE_WARNING_OVERLAP) {
                                            backBufferGraphics.drawImage(warningImage, (int) (nodePos.getX() - sizeScaledHalf), (int) (nodePos.getY() - sizeScaledHalf), warningImage.getWidth(), warningImage.getHeight(), null);
                                        }
                                    } else {
                                        if (mapNode.y == -1) {
                                            backBufferGraphics.drawImage(warningYImage, (int) (nodePos.getX() - sizeScaledHalf), (int) (nodePos.getY() - sizeScaledHalf), warningYImage.getWidth(), warningYImage.getHeight(), null);
                                        }
                                    }
                                }

                                // show the node ID if we in debug mode, the higher the node count, the more text spam there is :-P
                                // It will affect editor speed, the more nodes the worse it will get, you have been warned :)

                                if (bDebugShowID) {
                                    Point2D newPoint =  new Point2D.Double(nodePos.getX() - 12 , nodePos.getY() + 30);
                                    textList.add(new TextDisplayStore(String.valueOf(mapNode.id), newPoint, Color.WHITE, false));
                                }
                            }
                        } finally {
                            drawLock.unlock();
                        }

                        // do we draw the node hover-over image and add the marker name/group to the draw list

                        if (hoveredNode != null) {
                            Point2D hoverNodePos = worldPosToScreenPos(hoveredNode.x, hoveredNode.z);
                            if (hoveredNode.flag == NODE_FLAG_STANDARD) {
                                backBufferGraphics.drawImage(nodeImageSelected, (int) (hoverNodePos.getX() - sizeScaledHalf), (int) (hoverNodePos.getY() - sizeScaledHalf), sizeScaled, sizeScaled, null);
                            } else if (hoveredNode.flag == NODE_FLAG_SUBPRIO) {
                                backBufferGraphics.drawImage(subPrioNodeImageSelected, (int) (hoverNodePos.getX() - sizeScaledHalf), (int) (hoverNodePos.getY() - sizeScaledHalf), sizeScaled, sizeScaled, null);
                            }
                            for (MapMarker mapMarker : RoadMap.mapMarkers) {
                                if (hoveredNode.id == mapMarker.mapNode.id) {
                                    String text = mapMarker.name + " ( " + mapMarker.group + " )";
                                    Point2D nodePosMarker = worldPosToScreenPos(mapMarker.mapNode.x - 1, mapMarker.mapNode.z - 1);
                                    textList.add( new TextDisplayStore( text, nodePosMarker, Color.WHITE, false));
                                }
                            }
                            if (bDebugShowSelectedLocation) {
                                String text = "X = " + hoveredNode.x + " , Y = " + hoveredNode.y + " , Z = " + hoveredNode.z + " , Flags = " + hoveredNode.flag + " , In = " + hoveredNode.incoming.size() + " , Out = " + hoveredNode.outgoing.size();
                                if (hoveredNode.hasWarning) text +=" , " + (hoveredNode.warningNodes.size() + 1) + " Overlapping Nodes";
                                Point2D nodePosMarker = worldPosToScreenPos(hoveredNode.x + 1, hoveredNode.z);
                                textList.add( new TextDisplayStore( text, nodePosMarker, Color.WHITE, false));
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

                        // iterate over all the markers and add the names to the draw list

                        LinkedList<MapMarker> mapMarkers = RoadMap.mapMarkers;
                        for (MapMarker mapMarker : mapMarkers) {
                            if (RoadMap.mapNodes.contains(mapMarker.mapNode)) {
                                Point2D nodePos = worldPosToScreenPos(mapMarker.mapNode.x - 1, mapMarker.mapNode.z - 1);
                                textList.add(new TextDisplayStore(mapMarker.name, nodePos, Color.WHITE, false));
                            }
                        }

                        // display all the text we need to render

                        drawLock.lock();
                        try {
                            for (TextDisplayStore list : textList) {
                                backBufferGraphics.setColor(list.colour);
                                if (list.useBackground) {
                                    FontMetrics fm = backBufferGraphics.getFontMetrics();
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

                    // Draw any liner lines

                    if (selected != null) {
                        if (editorState == EDITORSTATE_CONNECTING) {

                            Color colour = Color.GREEN;

                            if (linearLine.lineNodeList.size() > 1) {
                                for (int j = 0; j < linearLine.lineNodeList.size() -1; j++) { // skip the starting node of the array
                                    MapNode firstPos = linearLine.lineNodeList.get(j);
                                    MapNode secondPos = linearLine.lineNodeList.get(j+1);

                                    Point2D startNodePos = worldPosToScreenPos(firstPos.x, firstPos.z);
                                    Point2D endNodePos = worldPosToScreenPos(secondPos.x, secondPos.z);

                                    // don't draw the circle for the last node in the array
                                    if (j < linearLine.lineNodeList.size() - 1 ) {
                                        backBufferGraphics.drawImage(curveNodeImage, (int) (startNodePos.getX() - sizeScaledHalf), (int) (startNodePos.getY() - sizeScaledHalf), sizeScaled, sizeScaled, null);
                                    }
                                    if ( connectionType == CONNECTION_DUAL ) {
                                        colour = Color.BLUE;
                                    } else if ( connectionType == CONNECTION_REVERSE ) {
                                        colour = Color.CYAN;
                                    }

                                    drawLock.lock();
                                    try {
                                        backBufferGraphics.setColor(colour);
                                        drawArrowBetween(backBufferGraphics, startNodePos, endNodePos, connectionType == CONNECTION_DUAL);
                                    } finally {
                                        drawLock.unlock();
                                    }
                                }
                            }  else {
                                if (linearLine.lineNodeList.size() == 1) {
                                    if ( connectionType == CONNECTION_DUAL ) {
                                        colour = Color.BLUE;
                                    } else if ( connectionType == CONNECTION_REVERSE ) {
                                        colour = Color.CYAN;
                                    }
                                    Point2D startNodePos = worldPosToScreenPos(linearLine.getLineStartNode().x, linearLine.getLineStartNode().z);
                                    Point2D mousePos = new Point2D.Double(currentMouseX,currentMouseY);
                                    drawLock.lock();
                                    try {
                                        backBufferGraphics.setColor(colour);
                                        drawArrowBetween(backBufferGraphics, startNodePos, mousePos, connectionType == CONNECTION_DUAL);
                                    } finally {
                                        drawLock.unlock();
                                    }
                                }
                            }
                        } else {

                            Point2D startNodePos = worldPosToScreenPos(selected.x, selected.z);
                            Point2D mousePos = new Point2D.Double(currentMouseX,currentMouseY);

                            drawLock.lock();
                            try {
                                backBufferGraphics.setColor(Color.WHITE);
                                drawArrowBetween(backBufferGraphics, startNodePos, mousePos, false);
                            } finally {
                                drawLock.unlock();
                            }
                        }
                    }

                    // Draw the quad curve connection preview

                    if (quadCurve != null) {
                        if (isQuadCurveCreated) {
                            // draw control point
                            Point2D nodePos = worldPosToScreenPos(quadCurve.getControlPoint().x, quadCurve.getControlPoint().z);
                            if (quadCurve.getControlPoint().isSelected || hoveredNode == quadCurve.getControlPoint()) {
                                backBufferGraphics.drawImage(controlPointImageSelected, (int) (nodePos.getX() - sizeScaledHalf), (int) (nodePos.getY() - sizeScaledHalf), sizeScaled, sizeScaled, null);
                            } else {
                                backBufferGraphics.drawImage(controlPointImage, (int) (nodePos.getX() - sizeScaledHalf), (int) (nodePos.getY() - sizeScaledHalf), sizeScaled, sizeScaled, null);
                            }

                            //draw interpolation points for curve
                            Color colour = Color.GREEN;
                            for (int j = 0; j < quadCurve.curveNodesList.size() - 1; j++) {

                                MapNode currentNode = quadCurve.curveNodesList.get(j);
                                MapNode nextNode = quadCurve.curveNodesList.get(j + 1);

                                Point2D currentNodePos = worldPosToScreenPos(currentNode.x, currentNode.z);
                                Point2D nextNodePos = worldPosToScreenPos(nextNode.x, nextNode.z);

                                //don't draw the first node as it already been drawn
                                if (j != 0) {
                                    if (quadCurve.getNodeType() == NODE_FLAG_STANDARD) {
                                        backBufferGraphics.drawImage(curveNodeImage,(int) (currentNodePos.getX() - sizeScaledHalf), (int) (currentNodePos.getY() - sizeScaledHalf), sizeScaled, sizeScaled, null);
                                    } else {
                                        backBufferGraphics.drawImage(subPrioNodeImage,(int) (currentNodePos.getX() - (sizeScaledHalf / 2 )), (int) (currentNodePos.getY() - (sizeScaledHalf / 2 )), sizeScaledHalf, sizeScaledHalf, null);
                                    }
                                }

                                if (quadCurve.isReversePath()) {
                                    colour = Color.CYAN;
                                } else if (quadCurve.isDualPath() && quadCurve.getNodeType() == NODE_FLAG_STANDARD) {
                                    colour = Color.BLUE;
                                } else if (quadCurve.isDualPath() && quadCurve.getNodeType() == NODE_FLAG_SUBPRIO) {
                                    colour = BROWN;
                                } else if (currentNode.flag == 1) {
                                    colour = Color.ORANGE;
                                }

                                drawLock.lock();
                                try {
                                    backBufferGraphics.setColor(colour);
                                    drawArrowBetween(backBufferGraphics, currentNodePos, nextNodePos, quadCurve.isDualPath()) ;
                                } finally {
                                    drawLock.unlock();
                                }
                            }
                        }
                    }

                    // Draw the cubic curve connection preview

                    if (cubicCurve != null) {
                        if (isCubicCurveCreated) {
                            // draw control point
                            Point2D nodePos = worldPosToScreenPos(cubicCurve.getControlPoint1().x, cubicCurve.getControlPoint1().z);
                            if (cubicCurve.getControlPoint1().isSelected || hoveredNode == cubicCurve.getControlPoint1()) {
                                backBufferGraphics.drawImage(controlPointImageSelected, (int) (nodePos.getX() - sizeScaledHalf), (int) (nodePos.getY() - sizeScaledHalf), sizeScaled, sizeScaled, null);
                            } else {
                                backBufferGraphics.drawImage(controlPointImage, (int) (nodePos.getX() - sizeScaledHalf), (int) (nodePos.getY() - sizeScaledHalf), sizeScaled, sizeScaled, null);
                            }

                            nodePos = worldPosToScreenPos(cubicCurve.getControlPoint2().x, cubicCurve.getControlPoint2().z);
                            if (cubicCurve.getControlPoint2().isSelected || hoveredNode == cubicCurve.getControlPoint2()) {
                                backBufferGraphics.drawImage(controlPointImageSelected, (int) (nodePos.getX() - sizeScaledHalf), (int) (nodePos.getY() - sizeScaledHalf), sizeScaled, sizeScaled, null);
                            } else {
                                backBufferGraphics.drawImage(controlPointImage, (int) (nodePos.getX() - sizeScaledHalf), (int) (nodePos.getY() - sizeScaledHalf), sizeScaled, sizeScaled, null);
                            }

                            //draw interpolation points for curve
                            Color colour = Color.GREEN;
                            for (int j = 0; j < cubicCurve.curveNodesList.size() - 1; j++) {

                                MapNode current = cubicCurve.curveNodesList.get(j);
                                MapNode next = cubicCurve.curveNodesList.get(j + 1);

                                Point2D currentNodePos = worldPosToScreenPos(current.x, current.z);
                                Point2D nextNodePos = worldPosToScreenPos(next.x, next.z);

                                //don't draw the first node as it already been drawn
                                if (j != 0) {
                                    if (cubicCurve.getNodeType() == NODE_FLAG_STANDARD) {
                                        backBufferGraphics.drawImage(curveNodeImage,(int) (currentNodePos.getX() - sizeScaledHalf), (int) (currentNodePos.getY() - sizeScaledHalf), sizeScaled, sizeScaled, null);
                                    } else {
                                        backBufferGraphics.drawImage(subPrioNodeImage,(int) (currentNodePos.getX() - (sizeScaledHalf / 2 )), (int) (currentNodePos.getY() - (sizeScaledHalf / 2 )), sizeScaledHalf, sizeScaledHalf, null);
                                    }
                                }

                                if (cubicCurve.isReversePath()) {
                                    colour = Color.CYAN;
                                } else if (cubicCurve.isDualPath() && cubicCurve.getNodeType() == NODE_FLAG_STANDARD) {
                                    colour = Color.BLUE;
                                } else if (cubicCurve.isDualPath() && cubicCurve.getNodeType() == NODE_FLAG_SUBPRIO) {
                                    colour = BROWN;
                                } else if (current.flag == 1) {
                                    colour = Color.ORANGE;
                                }

                                drawLock.lock();
                                try {
                                    backBufferGraphics.setColor(colour);
                                    drawArrowBetween(backBufferGraphics, currentNodePos, nextNodePos, cubicCurve.isDualPath()) ;
                                } finally {
                                    drawLock.unlock();
                                }
                            }
                        }
                    }

                    // draw the right button selection rectangle

                    if (rectangleStart != null) {

                        Point2D mousePos = new Point2D.Double(prevMousePosX,prevMousePosY);
                        int diffX = (int) (mousePos.getX() - rectangleStart.getX());
                        int diffY = (int) (mousePos.getY() - rectangleStart.getY());
                        int rectangleX = (int) rectangleStart.getX();
                        int rectangleY = (int) rectangleStart.getY();
                        if (diffX < 0) {
                            rectangleX += diffX;
                            diffX = -diffX;
                        }
                        if (diffY < 0) {
                            rectangleY += diffY;
                            diffY = -diffY;
                        }

                        drawLock.lock();
                        try {
                            backBufferGraphics.setColor(Color.WHITE);
                            backBufferGraphics.drawRect(rectangleX, rectangleY, diffX, diffY);
                        } finally {
                            drawLock.unlock();
                        }
                    }

                    if (bDebugProfile) {
                        String text = "Finished Node Rendering in " + (System.currentTimeMillis() - startTime) + " ms";
                        showInTextArea(text,false, false);
                    }

                    textList.clear();
                    latch.countDown();
                }
            }


        }
    }

    //
    // The connection drawing thread finishes last in almost all cases, so we keep this as small as possible
    // we only draw the connections in the visible area (plus some extra padding) so we don't see the
    // connections clipping.
    //

    public static class ConnectionDrawThread implements Runnable {
        private static volatile boolean isStopped = false;
        private final LinkedList<DrawList> brownDrawList = new LinkedList<>();
        private final LinkedList<DrawList> blueDrawList = new LinkedList<>();
        private final LinkedList<DrawList> cyanDrawList = new LinkedList<>();
        private final LinkedList<DrawList> orangeDrawList = new LinkedList<>();
        private final LinkedList<DrawList> greenDrawList = new LinkedList<>();

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

            while ( !isStopped ) {

                try {
                    brownDrawList.clear();
                    blueDrawList.clear();
                    cyanDrawList.clear();
                    orangeDrawList.clear();
                    greenDrawList.clear();
                    this.wait();
                } catch (InterruptedException e) {
                    if (isStopped) {
                        LOG.info("ConnectionDraw Thread exiting");
                        return;
                    }

                    long startTime = 0;

                    if (bDebugProfile) {
                        startTime = System.currentTimeMillis();
                    }

                    //double currentNodeSize = nodeSize * zoomLevel * 0.5;

                    if (backBufferGraphics != null) {

                        int width = getMapPanel().getWidth();
                        int height = getMapPanel().getHeight();
                        //Color drawColour;

                        LinkedList<MapNode> nodes = RoadMap.mapNodes;

                        for (MapNode mapNode : nodes) {
                            LinkedList<MapNode> mapNodes = mapNode.outgoing;
                            Point2D nodePos = worldPosToScreenPos(mapNode.x, mapNode.z);

                            if (0 - (40 * zoomLevel) < nodePos.getX() && width + (40 * zoomLevel) > nodePos.getX() && 0 - (40 * zoomLevel) < nodePos.getY() && height + (40 * zoomLevel) > nodePos.getY()) {
                                for (MapNode outgoing : mapNodes) {
                                    boolean dual = RoadMap.isDual(mapNode, outgoing);
                                    boolean reverse = RoadMap.isReverse(mapNode, outgoing);

                                    Point2D outPos = worldPosToScreenPos(outgoing.x, outgoing.z);

                                    if (dual && mapNode.flag == 1) {
                                        brownDrawList.add(new DrawList(nodePos, outPos, true));
                                    } else if (dual) {
                                        blueDrawList.add(new DrawList(nodePos, outPos, true));
                                    } else if (reverse) {
                                        cyanDrawList.add(new DrawList(nodePos, outPos, false));
                                    } else if (mapNode.flag == 1) {
                                        orangeDrawList.add(new DrawList(nodePos, outPos, false));
                                    } else {
                                        greenDrawList.add(new DrawList(nodePos, outPos, false));
                                    }
                                }
                            }
                        }



                        // draw all the dual subPriority connection arrows

                        if (brownDrawList.size() > 0) {
                            drawLock.lock();
                            try {
                                backBufferGraphics.setColor(BROWN);
                                for (DrawList drawList : brownDrawList) {
                                    drawArrowBetween(backBufferGraphics, drawList.startPos, drawList.endPos, drawList.isDual);
                                }
                            } finally {
                                drawLock.unlock();
                            }
                        }

                        // draw all the Dual connection arrows

                        if (blueDrawList.size() > 0) {
                            drawLock.lock();
                            try {
                                backBufferGraphics.setColor(Color.BLUE);
                                for (DrawList drawList : blueDrawList) {
                                    drawArrowBetween(backBufferGraphics, drawList.startPos, drawList.endPos, drawList.isDual);
                                }
                            } finally {
                                drawLock.unlock();
                            }
                        }

                        // draw all the reverse connection arrows

                        if (cyanDrawList.size() > 0) {
                            drawLock.lock();
                            try {
                                backBufferGraphics.setColor(Color.CYAN);
                                for (DrawList drawList : cyanDrawList) {
                                    drawArrowBetween(backBufferGraphics, drawList.startPos, drawList.endPos, drawList.isDual);
                                }
                            } finally {
                                drawLock.unlock();
                            }
                        }

                        // draw all the SubPriority connection arrows

                        if (orangeDrawList.size() > 0) {
                            drawLock.lock();
                            try {
                                backBufferGraphics.setColor(Color.ORANGE);
                                for (DrawList drawList : orangeDrawList) {
                                    drawArrowBetween(backBufferGraphics, drawList.startPos, drawList.endPos, drawList.isDual);
                                }
                            } finally {
                                drawLock.unlock();
                            }
                        }

                        // draw all the regular connection arrows

                        if (greenDrawList.size() > 0) {
                            drawLock.lock();
                            try {
                                backBufferGraphics.setColor(Color.GREEN);
                                for (DrawList drawList : greenDrawList) {
                                    drawArrowBetween(backBufferGraphics, drawList.startPos, drawList.endPos, drawList.isDual);
                                }
                            } finally {
                                drawLock.unlock();
                            }
                        }
                    }

                    if (bDebugProfile) {
                        String text = "Finished Connection Rendering in " + (System.currentTimeMillis() - startTime) + " ms (" + zoomLevel + ")";
                        showInTextArea(text, false, false);
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

        if (image != null) {
            backBufferGraphics.clipRect(0, 0, this.getWidth(), this.getHeight());
            backBufferGraphics.drawImage(croppedImage, 0, 0, this.getWidth(), this.getHeight(), null);

            if (bShowGrid) drawGrid();

            if (roadMap != null) {
                latch = new CountDownLatch(2);

                nodeDrawThread.interrupt();
                connectionDrawThread.interrupt();
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
        if (image != null) {
            widthScaled = (int) (this.getWidth() / zoomLevel);
            heightScaled = (int) (this.getHeight() / zoomLevel);

            // Part 1 of work around for map resize bug, increase the zoomLevel
            // if widthScaled and heightScaled are bigger than the map image dimensions
            //
            // This will get us close, but the zoomLevel is still off by a small
            // amount and just moving the map in any direction will force MoveMapBy()
            // to run again and recalculate all the values so when run again ResizeMap()
            // will calculate it correctly.

            if ( (int) x + widthScaled > image.getWidth() ) {
                while ( widthScaled > image.getWidth() ) {
                    double step = -1 * (zoomLevel * 0.1);
                    if (bDebugZoomScale) LOG.info("widthScaled is out of bounds ( {} ) .. increasing zoomLevel by {}", widthScaled, step);
                    zoomLevel -= step;
                    widthScaled = (int) (this.getWidth() / zoomLevel);
                }
                if (bDebugZoomScale) LOG.info("widthScaled is {}", widthScaled);
            }

            if ( (int) y + heightScaled > image.getHeight() ) {
                while ( heightScaled > image.getHeight() ) {
                    double step = -1 * (zoomLevel * 0.1);
                    if (bDebugZoomScale) LOG.info("heightScaled is out of bounds ( {} ) .. increasing zoomLevel by {}", heightScaled, step);
                    zoomLevel -= step;
                    heightScaled = (int) (this.getHeight() / zoomLevel);
                }
                if (bDebugZoomScale) LOG.info("heightScaled is {}", heightScaled);
            }

            double calcX = ((this.getWidth() * 0.5) / zoomLevel) / image.getWidth();
            double calcY = ((this.getHeight() * 0.5) / zoomLevel) / image.getHeight();

            x = Math.min(x, 1 - calcX);
            x = Math.max(x, calcX);
            y = Math.min(y, 1 - calcY);
            y = Math.max(y, calcY);


            int centerX = (int) (x * image.getWidth());
            int centerY = (int) (y * image.getHeight());

            offsetX = (centerX - (widthScaled / 2) );
            offsetY = (centerY - (heightScaled / 2));

            if (offsetX != oldOffsetX || offsetY != oldOffsetY || widthScaled != oldWidthScaled || heightScaled != oldHeightScaled) {
                try {
                    croppedImage = image.getSubimage(offsetX, offsetY, widthScaled, heightScaled);
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
        if ((roadMap == null) || (image == null)) {
            return;
        }
        x -= diffX / (zoomLevel * image.getWidth());
        y -= diffY / (zoomLevel * image.getHeight());

        getResizedMap();
        this.repaint();
    }

    public void increaseZoomLevelBy(int rotations) {

        if ((roadMap == null) || (image == null)) {
            return;
        }
        double step = rotations * (zoomLevel * 0.1);
        if (((this.getWidth()/(zoomLevel - step)) > image.getWidth()) || ((this.getHeight()/(zoomLevel - step)) > image.getHeight())){
            return;
        }
        if ((zoomLevel - step) >=0 && (zoomLevel - step) < 30) {
            zoomLevel -= step;
            getResizedMap();
            this.repaint();
        }
    }

    public void snapMoveNodeBy(LinkedList<MapNode> nodeList, int diffX, int diffY) {
        double scaledDiffX;
        double scaledDiffY;

        canAutoSave = false;

        Point2D p = screenPosToWorldPos( prevMousePosX + diffX, prevMousePosY + diffY);
        double newX, newY;
        if (bGridSnapSubs) {
            newX = Math.round(p.getX() / (gridSpacingX / (gridSubDivisions + 1))) * (gridSpacingX / (gridSubDivisions + 1));
            newY = Math.round(p.getY() / (gridSpacingY / (gridSubDivisions + 1))) * (gridSpacingY / (gridSubDivisions + 1));
        } else {
            newX = Math.round(p.getX() / gridSpacingX) * gridSpacingX;
            newY = Math.round(p.getY() / gridSpacingY) * gridSpacingY;
        }
        scaledDiffX = newX - movingNode.x;
        scaledDiffY = newY - movingNode.z;

        for (MapNode node : nodeList) {
            if (!node.isControlNode) {
                if (node.x + scaledDiffX > -1024 * mapZoomFactor && node.x + scaledDiffX < 1024 * mapZoomFactor) {
                    node.x += scaledDiffX;
                }
                if (node.z + scaledDiffY > -1024 * mapZoomFactor && node.z + scaledDiffY < 1024 * mapZoomFactor) {
                    node.z += scaledDiffY;
                }
            }
            if (isQuadCurveCreated) {
                if (node == quadCurve.getCurveStartNode()) {
                    quadCurve.setCurveStartNode(node);
                } else if (node == quadCurve.getCurveEndNode()) {
                    quadCurve.setCurveEndNode(node);
                }
                    if (node == quadCurve.getControlPoint()) {
                        quadCurve.updateControlPoint(scaledDiffX, scaledDiffY);
                    }
            }
            if (isCubicCurveCreated) {
                if (node == cubicCurve.getCurveStartNode()) {
                    cubicCurve.setCurveStartNode(node);
                } else if (node == cubicCurve.getCurveEndNode()) {
                    cubicCurve.setCurveEndNode(node);
                }
                if (node == cubicCurve.getControlPoint1()) {
                    cubicCurve.updateControlPoint1(scaledDiffX, scaledDiffY);
                }
                if (node == cubicCurve.getControlPoint2()) {
                    cubicCurve.updateControlPoint2(scaledDiffX, scaledDiffY);
                }
            }
        }

        canAutoSave = true;

        this.repaint();
    }

    public void moveNodeBy(LinkedList<MapNode> nodeList, int diffX, int diffY, boolean snapOverride) {
        double scaledDiffX;
        double scaledDiffY;

        canAutoSave = false;

        for (MapNode node : nodeList) {
            if (bGridSnap && !snapOverride) {
                Point2D p = screenPosToWorldPos( prevMousePosX + diffX, prevMousePosY + diffY);
                double newX, newY;
                if (bGridSnapSubs) {
                    newX = Math.round(p.getX() / (gridSpacingX / (gridSubDivisions + 1))) * (gridSpacingX / (gridSubDivisions + 1));
                    newY = Math.round(p.getY() / (gridSpacingY / (gridSubDivisions + 1))) * (gridSpacingY / (gridSubDivisions + 1));
                } else {
                    newX = Math.round(p.getX() / gridSpacingX) * gridSpacingX;
                    newY = Math.round(p.getY() / gridSpacingY) * gridSpacingY;
                }
                scaledDiffX = newX - node.x;
                scaledDiffY = newY - node.z;

            } else {
                scaledDiffX = (diffX * mapZoomFactor) / zoomLevel;
                scaledDiffY = (diffY * mapZoomFactor) / zoomLevel;
            }

            if (!node.isControlNode) {
                if (node.x + scaledDiffX > -1024 * mapZoomFactor && node.x + scaledDiffX < 1024 * mapZoomFactor) {
                    if (bGridSnap && !snapOverride) {
                        node.x = (double) Math.round((node.x + scaledDiffX) * 50) / 50;
                    } else {
                        node.x += scaledDiffX;
                    }
                }
                if (node.z + scaledDiffY > -1024 * mapZoomFactor && node.z + scaledDiffY < 1024 * mapZoomFactor) {
                    if (bGridSnap && !snapOverride) {
                        node.z = (double) Math.round((node.z + scaledDiffY) * 50) / 50;
                    } else {
                        node.z += scaledDiffY;
                    }
                }
            }

            if (isQuadCurveCreated) {
                if (node == quadCurve.getCurveStartNode()) {
                    quadCurve.setCurveStartNode(node);
                } else if (node == quadCurve.getCurveEndNode()) {
                    quadCurve.setCurveEndNode(node);
                }
                if (node == quadCurve.getControlPoint()) {
                    quadCurve.updateControlPoint(scaledDiffX, scaledDiffY);
                }
            }
            if (isCubicCurveCreated) {
                if (node == cubicCurve.getCurveStartNode()) {
                    cubicCurve.setCurveStartNode(node);
                } else if (node == cubicCurve.getCurveEndNode()) {
                    cubicCurve.setCurveEndNode(node);
                }
                if (node == cubicCurve.getControlPoint1()) {
                    cubicCurve.updateControlPoint1(scaledDiffX, scaledDiffY);
                }
                if (node == cubicCurve.getControlPoint2()) {
                    cubicCurve.updateControlPoint2(scaledDiffX, scaledDiffY);
                }
            }
        }

        canAutoSave = true;

        this.repaint();
    }

    public MapNode getNodeAt(double posX, double posY) {

        MapNode selected = null;

        if ((roadMap != null) && (image != null)) {

            Point2D outPos;
            double currentNodeSize = nodeSize * zoomLevel * 0.5;

            // make sure we prioritize returning control nodes over regular nodes

            for (MapNode mapNode : RoadMap.mapNodes) {
                outPos = worldPosToScreenPos(mapNode.x, mapNode.z);
                if (posX < outPos.getX() + currentNodeSize && posX > outPos.getX() - currentNodeSize && posY < outPos.getY() + currentNodeSize && posY > outPos.getY() - currentNodeSize) {
                    selected = mapNode;
                    break;
                }
            }

            if (isQuadCurveCreated) {
                outPos = worldPosToScreenPos(quadCurve.getControlPoint().x, quadCurve.getControlPoint().z);
                if (posX < outPos.getX() + currentNodeSize && posX > outPos.getX() - currentNodeSize && posY < outPos.getY() + currentNodeSize && posY > outPos.getY() - currentNodeSize) {
                    return quadCurve.getControlPoint();
                }
            }
            if (isCubicCurveCreated) {
                outPos = worldPosToScreenPos(cubicCurve.getControlPoint1().x, cubicCurve.getControlPoint1().z);
                if (posX < outPos.getX() + currentNodeSize && posX > outPos.getX() - currentNodeSize && posY < outPos.getY() + currentNodeSize && posY > outPos.getY() - currentNodeSize) {
                    return cubicCurve.getControlPoint1();
                }
                outPos = worldPosToScreenPos(cubicCurve.getControlPoint2().x, cubicCurve.getControlPoint2().z);
                if (posX < outPos.getX() + currentNodeSize && posX > outPos.getX() - currentNodeSize && posY < outPos.getY() + currentNodeSize && posY > outPos.getY() - currentNodeSize) {
                    return cubicCurve.getControlPoint2();
                }
            }
        }
        return selected;
    }



    public static double getYValueFromHeightMap(double worldX, double worldZ) {
        if (heightMapImage != null) {
            double x, y;
            x = ((512 * mapZoomFactor)) + (int) Math.ceil(worldX / 2);
            y = ((512 * mapZoomFactor)) + (int) Math.ceil(worldZ / 2);
            if (x <0) x = 0;
            if (y <0) y = 0;
            if (x > 1024) x = 1024;
            if (y > 1024) y = 1024;
            Color color = new Color(heightMapImage.getRGB((int)x, (int)y));
            return color.getRed() + 0.5;
        }
        return -1;
    }

    public void removeDeleteListNodes() {
        canAutoSave = false;

        for (NodeLinks nodeLinks : deleteNodeList) {
            MapNode inList = nodeLinks.node;
            RoadMap.removeMapNode(inList);
        }

        canAutoSave = true;

        setStale(true);
        hoveredNode = null;
        this.repaint();
    }

    public void removeDestination(MapNode toDelete) {
        canAutoSave = false;

        MapMarker destinationToDelete = null;
        LinkedList<MapMarker> mapMarkers = RoadMap.mapMarkers;
        for (MapMarker mapMarker : mapMarkers) {
            if (mapMarker.mapNode.id == toDelete.id) {
                destinationToDelete = mapMarker;
            }
        }
        if (destinationToDelete != null) {
            changeManager.addChangeable( new MarkerRemoveChanger(destinationToDelete));
            RoadMap.removeMapMarker(destinationToDelete);
            setStale(true);
            this.repaint();
        }

        canAutoSave = true;

    }

    public MapNode createNode(double worldX, double worldZ, int flag) {
        canAutoSave = false;
        if ((roadMap == null) || (image == null)) {
            return null;
        }
        double heightMapY = getYValueFromHeightMap(worldX, worldZ);
        MapNode mapNode = new MapNode(RoadMap.mapNodes.size()+1, worldX, heightMapY, worldZ, flag, false, false); //flag = 0 causes created node to be regular by default
        RoadMap.mapNodes.add(mapNode);
        this.repaint();
        changeManager.addChangeable( new AddNodeChanger(mapNode) );
        MapPanel.getMapPanel().setStale(true);
        canAutoSave = true;
        return mapNode;
    }

    public static Point2D screenPosToWorldPos(int screenX, int screenY) {
        double centerX = (x * (image.getWidth()));
        double centerY = (y * (image.getHeight()));

        double widthScaled = (getMapPanel().getWidth() / zoomLevel);
        double heightScaled = (getMapPanel().getHeight() / zoomLevel);

        double topLeftX = centerX - (widthScaled/2);
        double topLeftY = centerY - (heightScaled/2);

        double diffScaledX = screenX / zoomLevel;
        double diffScaledY = screenY / zoomLevel;

        int centerPointOffset = 1024 * mapZoomFactor;

        double worldPosX = ((topLeftX + diffScaledX) * mapZoomFactor) - centerPointOffset;
        double worldPosY = ((topLeftY + diffScaledY) * mapZoomFactor) - centerPointOffset;

        return new Point2D.Double(worldPosX, worldPosY);
    }

    public static Point2D worldPosToScreenPos(double worldX, double worldY) {

        int centerPointOffset = 1024 * mapZoomFactor;

        worldX += centerPointOffset;
        worldY += centerPointOffset;

        double scaledX = (worldX/mapZoomFactor) * zoomLevel;
        double scaledY = (worldY/mapZoomFactor) * zoomLevel;

        double centerXScaled = (x * (image.getWidth()*zoomLevel));
        double centerYScaled = (y * (image.getHeight()*zoomLevel));

        double topLeftX = centerXScaled - ((double) mapPanel.getWidth() /2);
        double topLeftY = centerYScaled - ((double) mapPanel.getHeight()/2);

        return new Point2D.Double(scaledX - topLeftX,scaledY - topLeftY);
    }

    public static void createConnectionBetween(MapNode start, MapNode target, int type) {
        if (start == target) {
            return;
        }

        //if (target.incoming.size() == 0 && target.outgoing.size() == 0) { target.y = start.y; }

        if (!start.outgoing.contains(target)) {
            start.outgoing.add(target);

            if (type == CONNECTION_STANDARD) {
                if (!target.incoming.contains(start))
                target.incoming.add(start);
            } else if (type == CONNECTION_REVERSE ) {
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
            } else if (type == CONNECTION_REVERSE ) {
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

    public void createDestinationAt(MapNode mapNode, String destinationName, String groupName) {
        if (mapNode != null && destinationName != null && destinationName.length() > 0) {
            if (groupName == null) groupName = "All";
            MapMarker mapMarker = new MapMarker(mapNode, destinationName, groupName);
            changeManager.addChangeable( new MarkerAddChanger(mapMarker));
            if (configType == CONFIG_ROUTEMANAGER) {
                boolean found = false;
                for (MarkerGroup marker : markerGroup) {
                    if (Objects.equals(marker.groupName, groupName)) found = true;
                }
                if (!found && !groupName.equals("All")) {
                    LOG.info("Adding new group {} to markerGroup", groupName);
                    markerGroup.add(new MarkerGroup(markerGroup.size() + 1, groupName));
                }
            }
            roadMap.addMapMarker(mapMarker);
            setStale(true);
        }
    }

    public void changeNodePriority(MapNode nodeToChange) {
        nodeToChange.flag = 1 - nodeToChange.flag;
        changeManager.addChangeable( new NodePriorityChanger(nodeToChange));
        setStale(true);
        this.repaint();
    }

    public void removeAllNodesInScreenArea(Point2D rectangleStartScreen, Point2D rectangleEndScreen) {

        getAllNodesInScreenArea(rectangleStartScreen, rectangleEndScreen);
        LOG.info("{}", localeString.getString("console_node_area_remove"));
        if (quadCurve != null && isQuadCurveCreated) {
            if (multiSelectList.contains(quadCurve.getCurveStartNode())) {
                if (DEBUG) LOG.info("Cannot delete start node of quad curve until it is confirmed or cancelled");
                multiSelectList.remove(quadCurve.getCurveStartNode());
                quadCurve.getCurveStartNode().isSelected = false;
            }
            if (multiSelectList.contains(quadCurve.getCurveEndNode())) {
                if (DEBUG) LOG.info("Cannot delete end nodes of quad curve until it is confirmed or cancelled");
                multiSelectList.remove(quadCurve.getCurveEndNode());
                quadCurve.getCurveEndNode().isSelected = false;
            }
            if (multiSelectList.contains(quadCurve.getControlPoint())) {
                if (DEBUG) LOG.info("Cannot delete quad curve control point");
                multiSelectList.remove(quadCurve.getControlPoint());
                quadCurve.getControlPoint().isSelected = false;

            }
        }
        if (cubicCurve != null && isCubicCurveCreated) {
            if (multiSelectList.contains(cubicCurve.getCurveStartNode())) {
                if (DEBUG) LOG.info("Cannot delete start node of cubic curve until it is confirmed or cancelled");
                multiSelectList.remove(cubicCurve.getCurveStartNode());
                cubicCurve.getCurveStartNode().isSelected = false;
            }
            if (multiSelectList.contains(cubicCurve.getCurveEndNode())) {
                if (DEBUG) LOG.info("Cannot delete end node of cubic curve until it is confirmed or cancelled");
                multiSelectList.remove(cubicCurve.getCurveEndNode());
                cubicCurve.getCurveEndNode().isSelected = false;
            }
            if (multiSelectList.contains(cubicCurve.getControlPoint1())) {
                if (DEBUG) LOG.info("Cannot delete cubic curve control point 1");
                multiSelectList.remove(cubicCurve.getControlPoint1());
                cubicCurve.getControlPoint1().isSelected = false;
            }
            if (multiSelectList.contains(cubicCurve.getControlPoint2())) {
                if (DEBUG) LOG.info("Cannot delete cubic curve control point 2");
                multiSelectList.remove(cubicCurve.getControlPoint2());
                cubicCurve.getControlPoint2().isSelected = false;
            }
        }
        for (MapNode node : multiSelectList) {

            addToDeleteList(node);
            if (bDebugUndoRedo) LOG.info("Added ID {} to delete list", node.id);
        }
        changeManager.addChangeable( new DeleteNodeChanger(deleteNodeList));
        canAutoSave = false;
        removeDeleteListNodes();
        canAutoSave = true;
        deleteNodeList.clear();
        clearMultiSelection();
    }

    public void changeAllNodesPriInScreenArea(Point2D rectangleStartScreen, Point2D rectangleEndScreen) {
        canAutoSave = false;

        getAllNodesInScreenArea(rectangleStartScreen, rectangleEndScreen);
        if (!multiSelectList.isEmpty()) {
            for (MapNode node : multiSelectList) {
                node.flag = 1 - node.flag;
            }
        }
        changeManager.addChangeable( new NodePriorityChanger(multiSelectList));
        setStale(true);
        clearMultiSelection();
        canAutoSave = true;
    }

   public void getAllNodesInScreenArea(Point2D rectangleStartScreen, Point2D rectangleEndScreen) {
       if ((roadMap == null) || (image == null)) {
           return;
       }
       int screenStartX = (int) rectangleStartScreen.getX();
       int screenStartY = (int) rectangleStartScreen.getY();
       int width = (int) (rectangleEndScreen.getX() - rectangleStartScreen.getX());
       int height = (int) (rectangleEndScreen.getY() - rectangleStartScreen.getY());

       Rectangle2D rectangle = getNormalizedRectangleFor(screenStartX, screenStartY, width, height);
       screenStartX = (int) rectangle.getX();
       screenStartY = (int) rectangle.getY();
       width = (int) rectangle.getWidth();
       height = (int) rectangle.getHeight();
       double currentNodeSize = nodeSize * zoomLevel * 0.5;

       for (MapNode mapNode : RoadMap.mapNodes) {

           Point2D nodePos = worldPosToScreenPos(mapNode.x, mapNode.z);

           if (screenStartX < nodePos.getX() + currentNodeSize && (screenStartX + width) > nodePos.getX() - currentNodeSize && screenStartY < nodePos.getY() + currentNodeSize && (screenStartY + height) > nodePos.getY() - currentNodeSize) {

               if (multiSelectList.contains(mapNode)) {
                   multiSelectList.remove(mapNode);
                   mapNode.isSelected = false;
               } else {
                   multiSelectList.add(mapNode);
                   mapNode.isSelected = true;
               }
           }
       }

       if (isQuadCurveCreated) {
           MapNode controlPoint = quadCurve.getControlPoint();
           Point2D nodePos = worldPosToScreenPos(controlPoint.x, controlPoint.z);
           if (screenStartX < nodePos.getX() + currentNodeSize && (screenStartX + width) > nodePos.getX() - currentNodeSize && screenStartY < nodePos.getY() + currentNodeSize && (screenStartY + height) > nodePos.getY() - currentNodeSize) {
               if (multiSelectList.contains(controlPoint)) {
                   multiSelectList.remove(controlPoint);
                   controlPoint.isSelected = false;
               } else {
                   multiSelectList.add(controlPoint);
                   controlPoint.isSelected = true;
               }
           }
       }

       if (isCubicCurveCreated) {
           MapNode controlPoint1 = cubicCurve.getControlPoint1();
           MapNode controlPoint2 = cubicCurve.getControlPoint2();

           Point2D nodePos1 = worldPosToScreenPos(controlPoint1.x, controlPoint1.z);
           if (screenStartX < nodePos1.getX() + currentNodeSize && (screenStartX + width) > nodePos1.getX() - currentNodeSize && screenStartY < nodePos1.getY() + currentNodeSize && (screenStartY + height) > nodePos1.getY() - currentNodeSize) {
               if (multiSelectList.contains(controlPoint1)) {
                   multiSelectList.remove(controlPoint1);
                   controlPoint1.isSelected = false;
               } else {
                   multiSelectList.add(controlPoint1);
                   controlPoint1.isSelected = true;
               }
           }
           Point2D nodePos2 = worldPosToScreenPos(controlPoint2.x, controlPoint2.z);
           if (screenStartX < nodePos2.getX() + currentNodeSize && (screenStartX + width) > nodePos2.getX() - currentNodeSize && screenStartY < nodePos2.getY() + currentNodeSize && (screenStartY + height) > nodePos2.getY() - currentNodeSize) {
               if (multiSelectList.contains(controlPoint2)) {
                   multiSelectList.remove(controlPoint2);
                   controlPoint2.isSelected = false;
               } else {
                   multiSelectList.add(controlPoint2);
                   controlPoint2.isSelected = true;
               }
           }
       }

       if (multiSelectList.size() > 0) {
           LOG.info("Selected {} nodes", multiSelectList.size());
           isMultipleSelected = true;
       } else {
           LOG.info("No nodes selected");
           isMultipleSelected = false;
       }


   }

    public static void clearMultiSelection() {
        if (multiSelectList != null && multiSelectList.size() > 0 ) {
            for (MapNode node : multiSelectList) {
                node.isSelected = false;
            }
            multiSelectList.clear();
        }
        isMultipleSelected = false;
        rectangleStart = null;
        rectangleEnd = null;
        MapPanel.getMapPanel().repaint();
    }

    public static void drawArrowBetween(Graphics g, Point2D start, Point2D target, boolean dual) {

        double startX = start.getX();
        double startY = start.getY();
        double targetX = target.getX();
        double targetY = target.getY();


        double vecX = startX - targetX;
        double vecY = startY - targetY;

        double angleRad = Math.atan2(vecY, vecX);

        angleRad = normalizeAngle(angleRad);

        // calculate where to start the line based around the circumference of the node

        double distCos = ((nodeSize * zoomLevel) * 0.5) * Math.cos(angleRad);
        double distSin = ((nodeSize * zoomLevel) * 0.5) * Math.sin(angleRad);

        double lineStartX = startX - distCos;
        double lineStartY = startY - distSin;

        // calculate where to finish the line based around the circumference of the node
        double lineEndX = targetX + distCos;
        double lineEndY = targetY + distSin;

        g.drawLine((int) lineStartX, (int) lineStartY, (int) lineEndX, (int) lineEndY);

        if (zoomLevel > 2.5) {
            double arrowLength = 1.3 * zoomLevel;

            double arrowLeft = normalizeAngle(angleRad + Math.toRadians(-20));
            double arrowLeftX = targetX + Math.cos(arrowLeft) * arrowLength;
            double arrowLeftY = targetY + Math.sin(arrowLeft) * arrowLength;

            double arrowRight = normalizeAngle(angleRad + Math.toRadians(20));
            double arrowRightX = targetX + Math.cos(arrowRight) * arrowLength;
            double arrowRightY = targetY + Math.sin(arrowRight) * arrowLength;
            g.drawLine((int) lineEndX, (int) lineEndY, (int) arrowLeftX, (int) arrowLeftY);
            g.drawLine((int) lineEndX, (int) lineEndY, (int) arrowRightX, (int) arrowRightY);

            if (dual) {
                angleRad = normalizeAngle(angleRad+Math.PI);

                arrowLeft = normalizeAngle(angleRad + Math.toRadians(-20));
                arrowRight = normalizeAngle(angleRad + Math.toRadians(20));

                arrowLeftX = start.getX() + Math.cos(arrowLeft) * arrowLength;
                arrowLeftY = start.getY() + Math.sin(arrowLeft) * arrowLength;
                arrowRightX = start.getX() + Math.cos(arrowRight) * arrowLength;
                arrowRightY = start.getY() + Math.sin(arrowRight) * arrowLength;

                g.drawLine((int) lineStartX, (int) lineStartY, (int) arrowLeftX, (int) arrowLeftY);
                g.drawLine((int) lineStartX, (int) lineStartY, (int) arrowRightX, (int) arrowRightY);
            }
        }
    }

    public void confirmCurve() {
        int returnVal = 2; // default to confirm both
        if (quadCurve != null && cubicCurve != null) {
            String[] options = {"QuadCurve", "CubicCurve", "Both", "Cancel"};
            returnVal = JOptionPane.showOptionDialog(this, localeString.getString("dialog_curve_multiple_confirm"), "" + localeString.getString("dialog_curve_multiple_confirm_title"), JOptionPane.YES_NO_CANCEL_OPTION, JOptionPane.WARNING_MESSAGE, null, options, options[3]);
            if (DEBUG) LOG.info("option {}", returnVal);
        }
        if (returnVal == 0 || returnVal == 2) {
            if (quadCurve != null) {
                quadCurve.commitCurve();
                stopQuadCurve();
                setStale(true);
            }
        }

        if (returnVal == 1 || returnVal == 2) {
            if (cubicCurve != null) {
                cubicCurve.commitCurve();
                stopCubicCurve();
                setStale(true);
            }
        }
    }

    public void cancelCurve() {
        int returnVal = 2; // default to confirm both
        if (quadCurve != null && cubicCurve != null) {
            String[] options = {"QuadCurve", "CubicCurve", "Both", "Cancel"};
            returnVal = JOptionPane.showOptionDialog(this, localeString.getString("dialog_curve_multiple_cancel"), "" + localeString.getString("dialog_curve_multiple_cancel_title"), JOptionPane.YES_NO_CANCEL_OPTION, JOptionPane.WARNING_MESSAGE, null, options, options[3]);
            if (DEBUG) LOG.info("option {}", returnVal);
        }

        if (returnVal == 0 || returnVal == 2) {
            if (quadCurve != null) {
                stopQuadCurve();
                LOG.info(localeString.getString("curve_cancel"));
            }
        }

        if (returnVal == 1 || returnVal == 2) {
            if (cubicCurve != null) {
                stopCubicCurve();
                LOG.info(localeString.getString("curve_cancel"));
            }
        }
    }

    private void stopQuadCurve() {
        if (quadCurve != null) quadCurve.clear();
        isQuadCurveCreated = false;
        isControlNodeSelected = false;
        quadCurve = null;
        selected = null;
        //if (cubicCurve == null)  GUIBuilder.curvePanel.setVisible(false);
        this.repaint();
    }

    private void stopCubicCurve() {
        if (cubicCurve != null) cubicCurve.clear();
        isCubicCurveCreated = false;
        isControlNodeSelected = false;
        cubicCurve = null;
        selected = null;
        //if (quadCurve == null) GUIBuilder.curvePanel.setVisible(false);
        this.repaint();
    }

    //
    // Mouse movement and drag detection
    //

    public void mouseMoved(int mousePosX, int mousePosY) {
        if (image != null) {

            if (bDebugHeightMap) {
                if (heightMapImage != null) {
                    double x, y;
                    Point2D point = MapPanel.screenPosToWorldPos(mousePosX, mousePosY);
                    x = ((512 * mapZoomFactor)) + (int) Math.ceil(point.getX() / 2);
                    y = ((512 * mapZoomFactor)) + (int) Math.ceil(point.getY() / 2);
                    if (x <0) x = 0;
                    if (y <0) y = 0;
                    //int color = heightMapImage.getRGB((int)x,(int)y);
                    Color color = new Color(heightMapImage.getRGB((int)x, (int)y));
                    String colourText="Heightmap R = " + color.getRed() + " , G = " + color.getGreen() + " , B = " + color.getBlue() + " , (" + (float)((color.getRed()<<8) + color.getGreen()) / 256 + ")";
                    showInTextArea(colourText, true, false);
                    String pointerText = "Mouse X = " + x + ", Y =" + y;
                    showInTextArea(pointerText, false, false);
                }
            }

            if (editorState == EDITORSTATE_CONNECTING && selected != null) {
                if (isDraggingRoute) {
                    Point2D pointerPos = screenPosToWorldPos(mousePosX, mousePosY);
                    linearLine.updateLine(pointerPos.getX(), pointerPos.getY());
                    this.repaint();
                }
            }
            if (editorState == EDITORSTATE_QUADRATICBEZIER && selected != null) {
                this.repaint();
            }
            if (editorState == EDITORSTATE_CUBICBEZIER && selected != null) {
                this.repaint();
            }
            movingNode = getNodeAt(mousePosX, mousePosY);
            if (movingNode != hoveredNode) {
                hoveredNode = movingNode;
                this.repaint();
            }
        }
    }

    public void mouseDragged(int mousePosX, int mousePosY) {
        int diffX = mousePosX - prevMousePosX;
        int diffY = mousePosY - prevMousePosY;

        if (isDragging) {
            moveMapBy(diffX, diffY);
        }
        if (isDraggingNode) {
            moveDiffX += diffX;
            moveDiffY += diffY;
            //if (editorState== EDITORSTATE_MOVING) {
                if (bGridSnap) {
                    snapMoveNodeBy(multiSelectList, diffX, diffY);
                } else {
                    moveNodeBy(multiSelectList, diffX, diffY, false);
                }
        }

        if (isControlNodeSelected) {
            if (quadCurve != null) {
                if (movingNode == quadCurve.getControlPoint()) {
                    quadCurve.moveControlPoint(diffX, diffY);
                }
            }
            if (cubicCurve != null) {
                if ( movingNode == cubicCurve.getControlPoint1()) {
                    cubicCurve.moveControlPoint1(diffX, diffY);
                }
                if ( movingNode == cubicCurve.getControlPoint2()) {
                    cubicCurve.moveControlPoint2(diffX, diffY);
                }
            }
        }

        if (editorState == EDITORSTATE_QUADRATICBEZIER) {
            if (movingNode !=null && isQuadCurveCreated) {
                if (quadCurve != null) {
                    quadCurve.updateCurve();
                }
                this.repaint();
            }
        }

        if (editorState == EDITORSTATE_CUBICBEZIER) {
            if (movingNode !=null && isCubicCurveCreated) {
                if (cubicCurve != null) {
                    cubicCurve.updateCurve();
                }
                this.repaint();
            }
        }

        if (rectangleStart != null && isMultiSelectAllowed) {
                this.repaint();
        }
    }

    //
    // Left mouse button click/pressed/released states
    //

    public void mouseButton1Clicked(int mousePosX, int mousePosY) {

        movingNode = getNodeAt(mousePosX, mousePosY);

        if (editorState == EDITORSTATE_CREATE_PRIMARY_NODE) {
            Point2D worldPos = screenPosToWorldPos(mousePosX, mousePosY);
            MapNode newNode = createNode(worldPos.getX(), worldPos.getY(), NODE_FLAG_STANDARD);
             checkAreaForNodeOverlap(newNode);
        }

        if (editorState == EDITORSTATE_CHANGE_NODE_PRIORITY) {
            MapNode changingNode = getNodeAt(mousePosX, mousePosY);
            if (changingNode != null) {
                if (changingNode.flag != NODE_FLAG_CONTROL_POINT) {
                    changeNodePriority(changingNode);
                }
            }
        }

        if (editorState == EDITORSTATE_CREATE_SUBPRIO_NODE) {
            Point2D worldPos = screenPosToWorldPos(mousePosX, mousePosY);
            MapNode newNode = createNode(worldPos.getX(), worldPos.getY(), NODE_FLAG_SUBPRIO);
            checkAreaForNodeOverlap(newNode);
        }

        if (editorState == EDITORSTATE_CREATING_DESTINATION) {
            if (movingNode != null) {
                for (int i = 0; i < RoadMap.mapMarkers.size(); i++) {
                    MapMarker mapMarker = RoadMap.mapMarkers.get(i);
                    if (mapMarker.mapNode == movingNode) {
                        LOG.info("{}", localeString.getString("console_marker_add_exists"));
                        return;
                    }
                }
                destInfo info = showNewMarkerDialog(movingNode.id);
                if (info != null && info.getName() != null) {
                    LOG.info("{} {} - Name = {} , Group = {}", localeString.getString("console_marker_add"), movingNode.id, info.getName(), info.getGroup());
                    createDestinationAt(movingNode, info.getName(), info.getGroup());
                    this.repaint();
                }
            }
        }

        if (editorState == EDITORSTATE_EDITING_DESTINATION) {
            if (movingNode != null) {
                for (int i = 0; i < RoadMap.mapMarkers.size(); i++) {
                    MapMarker mapMarker = RoadMap.mapMarkers.get(i);
                    if (mapMarker.mapNode == movingNode) {
                        destInfo info = showEditMarkerDialog(mapMarker.mapNode.id, mapMarker.name, mapMarker.group);
                        if (info != null && info.getName() != null) {
                            LOG.info("{} {} - old name = {} , old group = {}", localeString.getString("console_marker_modify"), movingNode.id, info.getName(), info.getGroup());
                            changeManager.addChangeable( new MarkerEditChanger(mapMarker.mapNode, movingNode.id, mapMarker.name, info.getName(), mapMarker.group, info.getGroup()));
                            if (configType == CONFIG_ROUTEMANAGER) {
                                boolean found = false;
                                for (MarkerGroup marker : markerGroup) {
                                    if (Objects.equals(marker.groupName, info.getGroup())) {
                                        found = true;
                                        break;
                                    }
                                }
                                if (!found && !info.getGroup().equals("All")) {
                                    LOG.info("Adding new group {} to markerGroup", info.getGroup());
                                    markerGroup.add(new MarkerGroup(markerGroup.size() + 1, info.getGroup()));
                                }
                            }
                            mapMarker.name = info.getName();
                            mapMarker.group = info.getGroup();
                            setStale(true);
                        }
                    }
                }
            }
        }
        if (editorState == EDITORSTATE_ALIGN_HORIZONTAL) {
            if (DEBUG) LOG.info("{} , {} , {}", isMultipleSelected, multiSelectList.size(), movingNode);
            if (isMultipleSelected && multiSelectList != null && movingNode != null) {
                LOG.info("Horizontal Align {} nodes at {}",multiSelectList.size(), movingNode.y);
                changeManager.addChangeable( new AlignmentChanger(multiSelectList, 0, 0, movingNode.z));
                canAutoSave = false;
                for (MapNode node : multiSelectList) {
                    node.z = movingNode.z;
                }
                if (isQuadCurveCreated) {
                    quadCurve.updateCurve();
                }
                canAutoSave = true;
                setStale(true);
                clearMultiSelection();
                this.repaint();
            }
        }

        if (editorState == EDITORSTATE_ALIGN_VERTICAL) {
            if (isMultipleSelected && multiSelectList != null && movingNode != null) {
                LOG.info("Vertical Align {} nodes at {}",multiSelectList.size(), movingNode.x);
                changeManager.addChangeable( new AlignmentChanger(multiSelectList, movingNode.x, 0,0));
                canAutoSave = false;
                for (MapNode node : multiSelectList) {
                    node.x = movingNode.x;
                }
                if (isQuadCurveCreated) {
                    quadCurve.updateCurve();
                }
                canAutoSave = true;
                setStale(true);
                clearMultiSelection();
                this.repaint();

            }
        }

        if (editorState == EDITORSTATE_ALIGN_DEPTH) {
            if (DEBUG) LOG.info("{} , {} , {}", isMultipleSelected, multiSelectList.size(), movingNode);
            if (isMultipleSelected && multiSelectList != null && movingNode != null) {
                LOG.info("Depth Aligning {} nodes at {}",multiSelectList.size(), movingNode.y);
                changeManager.addChangeable( new AlignmentChanger(multiSelectList, 0, movingNode.y, 0));
                canAutoSave = false;
                for (MapNode node : multiSelectList) {
                    node.y = movingNode.y;
                }
                if (isQuadCurveCreated) {
                    quadCurve.updateCurve();
                }
                canAutoSave = true;
                setStale(true);
                clearMultiSelection();
                this.repaint();
            }
        }

        if (editorState == EDITORSTATE_ALIGN_EDIT_NODE) {
            if (movingNode != null) {
                showEditNodeLocationDialog(movingNode);
            }
        }



        if (editorState == EDITORSTATE_QUADRATICBEZIER) {
            if (movingNode != null) {
                if (selected == null && !isQuadCurveCreated) {
                    selected = movingNode;
                    showInTextArea(localeString.getString("curve_start"), true, false);
                } else if (selected == hoveredNode) {
                    selected = null;
                    showInTextArea(localeString.getString("curve_cancel"), true, false);
                    cancelCurve();
                } else {
                    if (!isQuadCurveCreated) {
                        showInTextArea(localeString.getString("curve_complete"), true, false);
                        quadCurve = new QuadCurve(selected, movingNode);
                        quadCurve.setNumInterpolationPoints(GUIBuilder.numIterationsSlider.getValue());
                        isQuadCurveCreated = true;
                        selected = null;
                    }
                }
                this.repaint();
            }
        }

        if (editorState == EDITORSTATE_CUBICBEZIER) {
            if (movingNode != null) {
                if (selected == null && !isCubicCurveCreated) {
                    selected = movingNode;
                    showInTextArea(localeString.getString("curve_start"), true, false);
                } else if (selected == hoveredNode) {
                    selected = null;
                    showInTextArea(localeString.getString("curve_cancel"), true, false);
                    cancelCurve();
                } else {
                    if (!isCubicCurveCreated) {
                        showInTextArea(localeString.getString("curve_complete"), true, false);
                        cubicCurve = new CubicCurve(selected, movingNode);
                        cubicCurve.setNumInterpolationPoints(GUIBuilder.numIterationsSlider.getValue());
                        isCubicCurveCreated = true;
                        selected = null;
                    }
                }
                this.repaint();
            }
        }
    }

    public void mouseButton1Pressed(int mousePosX, int mousePosY) {
        if (!bMiddleMouseMove) isDragging = true;
        movingNode = getNodeAt(mousePosX, mousePosY);

        if (editorState == EDITORSTATE_CONNECTING) {
            if (movingNode != null) {
                if (selected == null) {
                    if (!movingNode.isControlNode) {
                        selected = movingNode;
                        Point2D pointerPos = screenPosToWorldPos(mousePosX, mousePosY);
                        linearLine = new LinearLine(selected, pointerPos.getX(), pointerPos.getY());
                        isDraggingRoute = true;
                        showInTextArea(localeString.getString("linearline_start"), true, false);
                    }
                } else if (selected == hoveredNode) {
                    selected = null;
                    showInTextArea(localeString.getString("linearline_cancel"), true, false);
                } else {
                    if (!movingNode.isControlNode) {
                        int nodeType = 0;
                        if (connectionType == CONNECTION_STANDARD) {
                            nodeType = createRegularConnectionState;
                        } else if (connectionType == CONNECTION_DUAL) {
                            nodeType = createDualConnectionState;
                        } else if (connectionType == CONNECTION_REVERSE) {
                            nodeType = createReverseConnectionState;
                        }

                        linearLine.commit(movingNode, connectionType, nodeType);
                        showInTextArea(localeString.getString("linearline_complete"), true, false);
                        linearLine.clear();
                        MapPanel.getMapPanel().setStale(true);

                        if (bContinuousConnections) {
                            selected = movingNode;
                            Point2D pointerPos = screenPosToWorldPos(mousePosX, mousePosY);
                            linearLine = new LinearLine(movingNode, pointerPos.getX(), pointerPos.getY());
                        } else {
                            isDraggingRoute = false;
                            selected = null;
                        }
                    }
                }
                this.repaint();
            }
        }

        if (editorState == EDITORSTATE_QUADRATICBEZIER) {
            if (isQuadCurveCreated && movingNode == quadCurve.getControlPoint()) {
                    isControlNodeSelected = true;
            }
        }

        if (editorState == EDITORSTATE_CUBICBEZIER) {
            if (isCubicCurveCreated) {
                if (movingNode == cubicCurve.getControlPoint1() || movingNode == cubicCurve.getControlPoint2()) {
                    isControlNodeSelected = true;
                }
            }
        }

        if (movingNode != null) {
            isDragging = false;
            boolean delete = true;
            if (editorState == EDITORSTATE_MOVING) {
                if (bGridSnap || bGridSnapSubs) {
                    Point2D p = worldPosToScreenPos(movingNode.x, movingNode.z);
                    preSnapX = p.getX();
                    preSnapY = p.getY();
                }
                moveDiffX = 0;
                moveDiffY = 0;
                isDraggingNode = true;
                if (!multiSelectList.contains(movingNode)) {
                    multiSelectList.add(movingNode);
                }
            }
            if (editorState == EDITORSTATE_DELETE_NODES) {
                if (quadCurve != null && isQuadCurveCreated) {
                    if (movingNode == quadCurve.getCurveStartNode() || movingNode == quadCurve.getCurveEndNode()){
                        if (DEBUG) LOG.info("Cannot delete start node of quad curve until it is confirmed or cancelled");
                        delete = false;
                    }
                    if (movingNode == quadCurve.getControlPoint()) {
                        if (DEBUG) LOG.info("Cannot delete control point of quad curve");
                        delete = false;
                    }
                }
                if (cubicCurve != null && isCubicCurveCreated) {
                    if (movingNode == cubicCurve.getCurveStartNode() || movingNode == cubicCurve.getCurveEndNode()) {
                        if (DEBUG) LOG.info("Cannot delete start/end node of cubic curve until it is confirmed or cancelled");
                        delete = false;
                    }
                    if (movingNode == cubicCurve.getControlPoint1() || movingNode == cubicCurve.getControlPoint2()) {
                        if (DEBUG) LOG.info("Cannot delete control point of cubic curve");
                        delete = false;
                    }
                }
                if (delete) {
                    addToDeleteList(movingNode);
                    changeManager.addChangeable( new DeleteNodeChanger(deleteNodeList));
                    removeDeleteListNodes();
                    deleteNodeList.clear();
                    clearMultiSelection();
                }
            }
            if (editorState == EDITORSTATE_DELETING_DESTINATION) {
                removeDestination(movingNode);
            }
        }
    }

    public void mouseButton1Released(int mousePosX, int mousePosY) {
        if (!bMiddleMouseMove) isDragging = false;
        if (isDraggingNode) {
            if (bGridSnap || bGridSnapSubs) {
                Point2D p = worldPosToScreenPos(movingNode.x, movingNode.z);
                moveDiffX = (int) (p.getX() - preSnapX);
                moveDiffY = (int) (p.getY() - preSnapY);
                changeManager.addChangeable( new MoveNodeChanger(multiSelectList, moveDiffX, moveDiffY, true));
            } else {
                changeManager.addChangeable( new MoveNodeChanger(multiSelectList, moveDiffX, moveDiffY, false));
            }
            setStale(true);
            if (!isMultipleSelected) clearMultiSelection();
        }
        isDraggingNode = false;
        isControlNodeSelected=false;
        if (movingNode == null) {
            //LOG.info("movingNode is null");
            MapNode node = getNodeAt(mousePosX, mousePosY);
            if (node != null) checkAreaForNodeOverlap(node);
        } else {
            checkNodeOverlap(movingNode);
        }
    }

    //
    // Middle mouse button click/pressed/released states
    //

    public void mouseButton2Clicked(int mousePosX, int mousePosY) {}

    public void mouseButton2Pressed(int mousePosX, int mousePosY) {
        if (bMiddleMouseMove) {
            isDragging = true;
        }
    }

    public void mouseButton2Released() {
        if (bMiddleMouseMove) isDragging = false;
    }

    //
    // Right mouse button click/pressed/released states
    //

    public void mouseButton3Clicked(int mousePosX, int mousePosY) {
        rectangleStart = null;
        rectangleEnd = null;
        clearMultiSelection();
        LOG.info("clearing Node Selection");
        this.repaint();
    }

    public void mouseButton3Pressed(int mousePosX, int mousePosY) {

        if (editorState == EDITORSTATE_CONNECTING) {
            selected = null;
            if (linearLine != null ) linearLine.clear();
            showInTextArea("",true, false);
            this.repaint();
            return;
        }

        if (editorState == EDITORSTATE_QUADRATICBEZIER) {
            if (selected != null && !isQuadCurveCreated) {
                LOG.info("Cancelling Curve");
                stopQuadCurve();
            }
        }

        if (editorState == EDITORSTATE_CUBICBEZIER) {
            if (selected != null && !isCubicCurveCreated) {
                LOG.info("Cancelling Curve");
                stopCubicCurve();
            }
        }

        if (isMultiSelectAllowed) {
            rectangleStart = new Point2D.Double(mousePosX, mousePosY);
        }
    }

    public void mouseButton3Released(int mousePosX, int mousePosY) {
        if (rectangleStart != null) {


            rectangleEnd = new Point2D.Double(mousePosX, mousePosY);
            if (rectangleStart.getX() != rectangleEnd.getX() && rectangleStart.getY() != rectangleEnd.getY()) {
                LOG.info("{} {},{} -- {} {}/{}", localeString.getString("console_rect_start"), rectangleStart.getX(), rectangleStart.getY(), localeString.getString("console_rect_end"), rectangleEnd.getX(), rectangleEnd.getY());
            } else {
                rectangleStart = null;
                rectangleEnd = null;
            }

            if (rectangleStart != null && rectangleEnd != null) {
                switch (editorState) {
                    case EDITORSTATE_DELETE_NODES:
                        removeAllNodesInScreenArea(rectangleStart, rectangleEnd);
                        this.repaint();
                        break;
                    case EDITORSTATE_CHANGE_NODE_PRIORITY:
                        //LOG.info("{}", localeString.getString("console_node_priority_toggle"));
                        changeAllNodesPriInScreenArea(rectangleStart, rectangleEnd);
                        this.repaint();
                        break;
                    case EDITORSTATE_MOVING:
                    case EDITORSTATE_ALIGN_HORIZONTAL:
                    case EDITORSTATE_ALIGN_VERTICAL:
                    case EDITORSTATE_ALIGN_DEPTH:
                        //LOG.info("{}", localeString.getString("console_node_align_vertical"));
                        //LOG.info("{}", localeString.getString("console_node_align_horizontal"));
                        //LOG.info("{}", localeString.getString("console_node_select_move"));
                        getAllNodesInScreenArea(rectangleStart, rectangleEnd);
                        this.repaint();
                        break;
                    case EDITORSTATE_CNP_SELECT:
                        getAllNodesInScreenArea(rectangleStart, rectangleEnd);
                        if (multiSelectList.size() > 0) {
                            rotationMenuEnabled(true);
                        }
                        this.repaint();
                        break;
                }
            }
        }
        rectangleStart = null;
        rectangleEnd = null;
    }

    public static void addToDeleteList(MapNode node) {
        LinkedList<MapNode> otherNodesInLinks = new LinkedList<>();
        LinkedList<MapNode> otherNodesOutLinks = new LinkedList<>();

        LinkedList<MapNode> roadmapNodes = RoadMap.mapNodes;
        for (MapNode mapNode : roadmapNodes) {
            if (mapNode != node) {
                if (mapNode.outgoing.contains(node)) {
                    otherNodesOutLinks.add(mapNode);
                }
                if (mapNode.incoming.contains(node)) {
                    otherNodesInLinks.add(mapNode);
                }
            }

        }
        MapMarker linkedMarker = null;
        LinkedList<MapMarker> mapMarkers = RoadMap.mapMarkers;
        for (MapMarker mapMarker : mapMarkers) {
            if (mapMarker.mapNode == node) {
                if (DEBUG) LOG.info("## MapNode ID {} has a linked MapMarker ## storing in case undo system needs it", node.id);
                linkedMarker = mapMarker;
            }
        }
        // NOTE -- linkedMarker is safe to be passed as null, just means no marker is linked to that node
        deleteNodeList.add(new NodeLinks(node, otherNodesInLinks, otherNodesOutLinks, linkedMarker));
    }

    public static void updateMapZoomFactor(int zoomFactor) {
        if (roadMap != null) {
            getMapPanel().setMapZoomFactor(zoomFactor);
            getMapPanel().repaint();
            updateMapZoomStore(roadMap.roadMapName, zoomFactor);
        }

    }

    public static void cutSelected() {
        if (isMultipleSelected && multiSelectList.size() > 0 ) {
            cnpManager.CutSelection(multiSelectList);
            MapPanel.getMapPanel().repaint();
        } else {
            LOG.info("Nothing to Cut");
        }
        editorState = EDITORSTATE_NOOP;
        updateButtons();
    }

    public static void copySelected() {
        if (isMultipleSelected && multiSelectList.size() > 0 ) {
            cnpManager.CopySelection(multiSelectList);
        } else {
            LOG.info("Nothing to Copy");
        }
        editorState = EDITORSTATE_NOOP;
        updateButtons();
    }

    public static void pasteSelected(boolean pasteInOriginalLocation) {
        cnpManager.PasteSelection(pasteInOriginalLocation);
        editorState = EDITORSTATE_NOOP;
        updateButtons();

    }

    public static void pasteSelectedInOriginalLocation() {
        cnpManager.PasteSelection(true);
        editorState = EDITORSTATE_NOOP;
        updateButtons();

    }

    public static void centreNode() {
        if (roadMap != null && mapImage != null ) {
            int result = mapPanel.showCentreNodeDialog();
            if (result != -1) {
                MapNode node = RoadMap.mapNodes.get(result);
                Point2D target = worldPosToScreenPos(node.x, node.z);
                double x = (getMapPanel().getWidth() >> 1) - target.getX();
                double y = (getMapPanel().getHeight() >> 1) - target.getY();
                getMapPanel().moveMapBy((int)x,(int)y);
            }
        }

    }

    public static void fixNodeHeight() {
        if (roadMap != null) {
            int result = JOptionPane.showConfirmDialog(editor, localeString.getString("dialog_fix_node_height"), "AutoDrive Editor", JOptionPane.OK_CANCEL_OPTION, JOptionPane.QUESTION_MESSAGE);
            if (result == JOptionPane.OK_OPTION) {
                for (MapNode node : RoadMap.mapNodes) {
                    double heightMapY = getYValueFromHeightMap(node.x, node.z);
                    if (node.y == -1) {
                        //LOG.info("ID {} ({}) adjusting Y to {}", node.id, node.y, heightMapY);
                        node.y = heightMapY;
                    }
                }
            } else {
                LOG.info("Cancelled node fix");
            }
        }


    }

    //
    // Dialog for Rotation Angle
    //

    public void showAutoSaveIntervalDialog() {
        JTextField autoSaveText = new JTextField(String.valueOf(autoSaveInterval));
        JLabel autoSaveLabel = new JLabel(" ");
        PlainDocument docAutoSaveInterval = (PlainDocument) autoSaveText.getDocument();
        docAutoSaveInterval.setDocumentFilter(new NumberFilter(autoSaveLabel, 1, 60, false));

        JTextField maxAutoSavesText = new JTextField(String.valueOf(maxAutoSaveSlots));
        JLabel maxAutoSavesLabel = new JLabel(" ");
        PlainDocument docMaxAutoSaves = (PlainDocument) autoSaveText.getDocument();
        docMaxAutoSaves.setDocumentFilter(new NumberFilter(maxAutoSavesLabel, 1, 20, false));

        Object[] inputFields = {localeString.getString("dialog_autosave_interval_set"), autoSaveText, autoSaveLabel,
                localeString.getString("dialog_autosave_max_saves"), maxAutoSavesText, maxAutoSavesLabel};

        int option = JOptionPane.showConfirmDialog(this, inputFields, ""+ localeString.getString("dialog_autosave_title"), JOptionPane.OK_CANCEL_OPTION, JOptionPane.QUESTION_MESSAGE);

        if (option == JOptionPane.OK_OPTION) {
            autoSaveInterval = Integer.parseInt(autoSaveText.getText());
            maxAutoSaveSlots = Integer.parseInt(maxAutoSavesText.getText());
            restartAutoSaveThread();
            this.repaint();
        }
    }

    //
    // Dialog for Rotation Angle
    //


    public void showRotationSettingDialog() {

        JTextField rotText = new JTextField(String.valueOf(rotationAngle));
        JLabel rotLabel = new JLabel(" ");
        PlainDocument docX = (PlainDocument) rotText.getDocument();
        docX.setDocumentFilter(new NumberFilter(rotLabel, 0, 360, false));

        Object[] inputFields = {localeString.getString("dialog_rotation_set"), rotText, rotLabel,};

        int option = JOptionPane.showConfirmDialog(this, inputFields, ""+ localeString.getString("dialog_rotation_set"), JOptionPane.OK_CANCEL_OPTION, JOptionPane.QUESTION_MESSAGE);

        if (option == JOptionPane.OK_OPTION) {
            rotationAngle = (int) Double.parseDouble(rotText.getText());
            this.repaint();
        }
    }

    //
    // Dialog for Centre Node
    //

    public Integer showCentreNodeDialog() {

        JTextField centreNode = new JTextField(String.valueOf(1));
        JLabel labelNode = new JLabel(" ");
        PlainDocument docX = (PlainDocument) centreNode.getDocument();
        docX.setDocumentFilter(new NumberFilter(labelNode, 0, RoadMap.mapNodes.size(), false));

        Object[] inputFields = {localeString.getString("dialog_centre_node"), centreNode, labelNode};

        int option = JOptionPane.showConfirmDialog(this, inputFields, ""+ localeString.getString("dialog_centre_node_title"), JOptionPane.OK_CANCEL_OPTION, JOptionPane.QUESTION_MESSAGE);

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
        docX.setDocumentFilter(new NumberFilter(labelDistance, 0, 2048 * mapZoomFactor, true));

        Object[] inputFields = {localeString.getString("dialog_scan_area"), mergeDistance, labelDistance};

        int option = JOptionPane.showConfirmDialog(this, inputFields, ""+ localeString.getString("dialog_scan_title"), JOptionPane.OK_CANCEL_OPTION, JOptionPane.QUESTION_MESSAGE);

        if (option == JOptionPane.OK_OPTION) {
            int result = scanNetworkForOverlapNodes(Double.parseDouble(mergeDistance.getText()), true);
        }
    }

    //
    // Dialog for Edit Node position
    //

    public void showEditNodeLocationDialog(MapNode node) {

        JTextField posX = new JTextField(String.valueOf((float)node.x));
        JLabel labelPosX = new JLabel(" ");
        PlainDocument docX = (PlainDocument) posX.getDocument();
        docX.setDocumentFilter(new NumberFilter(labelPosX, -1024 * mapZoomFactor, 1024 * mapZoomFactor, true));



        JTextField posZ = new JTextField(String.valueOf((float)node.z));
        JLabel labelPosZ = new JLabel(" ");
        PlainDocument docZ = (PlainDocument) posZ.getDocument();
        docZ.setDocumentFilter(new NumberFilter(labelPosZ, -1024 * mapZoomFactor, 1024 * mapZoomFactor, true));

        JTextField posY = new JTextField(String.valueOf((float)node.y));
        JLabel labelPosY = new JLabel(" ");
        if (node.y < 0 ) {
            labelPosY.setForeground(Color.RED);
            labelPosY.setText("* Invalid Y location");
        }
        PlainDocument docY = (PlainDocument) posY.getDocument();
        docY.setDocumentFilter(new NumberFilter(labelPosY, -1024 * mapZoomFactor, 1024 * mapZoomFactor, true));


        Object[] inputFields = {localeString.getString("dialog_node_position_x"), posX, labelPosX,
                localeString.getString("dialog_node_position_y"), posY, labelPosY,
                localeString.getString("dialog_node_position_z"), posZ, labelPosZ};

        int option = JOptionPane.showConfirmDialog(this, inputFields, ""+ localeString.getString("dialog_node_title"), JOptionPane.OK_CANCEL_OPTION, JOptionPane.QUESTION_MESSAGE);

        if (option == JOptionPane.OK_OPTION) {
            node.x = Double.parseDouble(posX.getText());
            node.y = Double.parseDouble(posY.getText());
            node.z = Double.parseDouble(posZ.getText());
            this.repaint();
        }
    }

    //
    // Dialog for Grid Spacing
    //

    public void showGridSettingDialog() {

        JTextField cordX = new JTextField(String.valueOf(gridSpacingX));
        JLabel labelX = new JLabel(" ");
        PlainDocument docX = (PlainDocument) cordX.getDocument();
        docX.setDocumentFilter(new NumberFilter(labelX, 1, 2048 * mapZoomFactor, true));

        JTextField cordY = new JTextField(String.valueOf(gridSpacingY));
        JLabel labelY = new JLabel(" ");
        PlainDocument docY = (PlainDocument) cordY.getDocument();
        docY.setDocumentFilter(new NumberFilter(labelY, 1, 2048 * mapZoomFactor, true));

        JTextField subDivisions = new JTextField(String.valueOf(gridSubDivisions));
        JLabel subLabel = new JLabel(" ");
        PlainDocument docSub = (PlainDocument) subDivisions.getDocument();
        docSub.setDocumentFilter(new NumberFilter(subLabel, 1, 50, false));

        Object[] inputFields = {localeString.getString("dialog_grid_set_x"), cordX, labelX,
                localeString.getString("dialog_grid_set_y"), cordY, labelY,
                localeString.getString("dialog_grid_set_subdivisions"), subDivisions, subLabel};

        int option = JOptionPane.showConfirmDialog(this, inputFields, ""+ localeString.getString("dialog_grid_title"), JOptionPane.OK_CANCEL_OPTION, JOptionPane.QUESTION_MESSAGE);

        if (option == JOptionPane.OK_OPTION) {
            gridSpacingX = Double.parseDouble(cordX.getText());
            gridSpacingY = Double.parseDouble(cordY.getText());
            gridSubDivisions = (int) Double.parseDouble(subDivisions.getText());
            this.repaint();
        }
    }

    static class NumberFilter extends DocumentFilter {

        private final JLabel messageLabel;
        private final double maxValue;
        private final double minValue;
        private final boolean allowedDecimalPlaces;

        public NumberFilter(JLabel label, double minNum, double maxNum, boolean allowDecimalPlaces) {
            super();
            this.messageLabel = label;
            this.minValue = minNum;
            this.maxValue = maxNum;
            this.allowedDecimalPlaces = allowDecimalPlaces;
        }

        @Override
        public void insertString(FilterBypass fb, int offset,String string, AttributeSet attr) {
            try {
                if (this.allowedDecimalPlaces) {
                   if (string.equals(".") && !fb.getDocument().getText(0, fb.getDocument().getLength()).contains(".")) {
                       super.insertString(fb, offset, string, attr);
                       return;
                   }
                }
                Double.parseDouble(string);
                super.insertString(fb, offset, string, attr);

            } catch (Exception e) {
                this.messageLabel.setForeground(Color.RED);
                if (allowedDecimalPlaces) {
                    this.messageLabel.setText("* Only valid numeric digits ( 0-9 ) and ( . )");
                } else {
                    this.messageLabel.setText("* Only valid numeric digits ( 0-9 )");
                }

            }
        }

        @Override
        public void replace(FilterBypass fb, int offset, int length, String text, AttributeSet attrs) {
            try {
                if (this.allowedDecimalPlaces) {
                    if (text.equals(".") && !fb.getDocument().getText(0, fb.getDocument().getLength()).contains(".")) {
                        super.insertString(fb, offset, text, attrs);
                        return;
                    }
                }
                Double.parseDouble(text);
                this.messageLabel.setText(" ");
                super.replace(fb, offset, length, text, attrs);
                double value = Double.parseDouble(fb.getDocument().getText(0, fb.getDocument().getLength()));
                if ( value > this.maxValue) {
                    this.messageLabel.setForeground(Color.RED);
                    this.messageLabel.setText("* Value Cannot be bigger than " + maxValue);
                    //LOG.info("bigger = {} -- offset = {} , length = {} , text = {}", fb.getDocument().getText(0, fb.getDocument().getLength()), offset, length, text);
                    super.replace(fb, 0, fb.getDocument().getLength(), String.valueOf((int)this.maxValue), attrs);
                }
                if ( value < this.minValue) {
                    this.messageLabel.setForeground(Color.RED);
                    this.messageLabel.setText("* Value Cannot be lower than " + minValue);
                    //LOG.info("bigger = {} -- offset = {} , length = {} , text = {}", fb.getDocument().getText(0, fb.getDocument().getLength()), offset, length, text);
                    super.replace(fb, 0, fb.getDocument().getLength(), String.valueOf((int)this.maxValue), attrs);
                }
            } catch (Exception e) {
                this.messageLabel.setForeground(Color.RED);
                if (allowedDecimalPlaces) {
                    this.messageLabel.setText("* Only valid numeric digits ( 0-9 ) and ( . )");
                } else {
                    this.messageLabel.setText("* Only valid numeric digits ( 0-9 )");
                }
            }
        }
    }


    //
    // Dialogs for marker add/edit
    //

    private destInfo showNewMarkerDialog(int id) {

        JTextField destName = new JTextField();
        String[] group = new String[1];

        ArrayList<String> groupArray = new ArrayList<>();
        if (configType == CONFIG_SAVEGAME) {
            LinkedList<MapMarker> mapMarkers = RoadMap.mapMarkers;
            for (MapMarker mapMarker : mapMarkers) {
                if (!mapMarker.group.equals("All")) {
                    if (!groupArray.contains(mapMarker.group)) {
                        groupArray.add(mapMarker.group);
                    }
                }
            }
        } else if (configType == CONFIG_ROUTEMANAGER) {
            for (MarkerGroup marker : markerGroup) {
                if (!marker.groupName.equals("All")) {
                    if (!groupArray.contains(marker.groupName)) {
                        groupArray.add(marker.groupName);
                    }
                }
            }
        }

        Collator coll = Collator.getInstance(locale);
        coll.setStrength(Collator.PRIMARY);
        Collections.sort(groupArray, coll);

        String[] groupString = new String[groupArray.size() + 1];
        groupString[0] = "None";
        for (int i = 0; i < groupArray.size(); i++) {
            groupString[i+1] = groupArray.get(i);
        }

        JComboBox comboBox = new JComboBox(groupString);
        comboBox.setEditable(true);
        comboBox.setSelectedIndex(0);
        comboBox.addActionListener(e -> {
            JComboBox cb = (JComboBox)e.getSource();
            group[0] = (String)cb.getSelectedItem();
        });

        Object[] inputFields = {localeString.getString("dialog_marker_select_name"), destName,
                localeString.getString("dialog_marker_add_select_group"), comboBox};

        int option = JOptionPane.showConfirmDialog(this, inputFields, ""+ localeString.getString("dialog_marker_add_title") + " ( Node ID " + id +" )", JOptionPane.OK_CANCEL_OPTION, JOptionPane.INFORMATION_MESSAGE, getMarkerIcon());

        if (option == JOptionPane.OK_OPTION) {

            if (group[0] == null || group[0].equals("None")) group[0] = "All";
            if (destName.getText() != null && destName.getText().length() > 0) {
                // since we can't return more than 1 string, we have to package them up
                return new destInfo(destName.getText(), group[0]);
            } else {
                LOG.info("{}", localeString.getString("console_marker_add_cancel_noname"));
                // null's are bad mmmmmkay.....
                return null;
            }
        }
        LOG.info("{}" , localeString.getString("console_marker_add_cancel"));
        return null;
    }

    private destInfo showEditMarkerDialog(int id, String markerName, String markerGroupName) {

        String[] group = new String[1];
        int groupIndex = 0;


        JSeparator separator = new JSeparator(SwingConstants.HORIZONTAL);
        JTextField destName = new JTextField(markerName);

        ArrayList<String> groupArray = new ArrayList<>();
        if (configType == CONFIG_SAVEGAME) {
            LinkedList<MapMarker> mapMarkers = RoadMap.mapMarkers;
            for (MapMarker mapMarker : mapMarkers) {
                if (!mapMarker.group.equals("All")) {
                    if (!groupArray.contains(mapMarker.group)) {
                        groupArray.add(mapMarker.group);
                    }
                }
            }
        } else if (configType == CONFIG_ROUTEMANAGER) {
            for (MarkerGroup marker : markerGroup) {
                if (!marker.groupName.equals("All")) {
                    if (!groupArray.contains(marker.groupName)) {
                        groupArray.add(marker.groupName);
                    }
                }
            }
        }

        Collator coll = Collator.getInstance(locale);
        coll.setStrength(Collator.PRIMARY);
        Collections.sort(groupArray, coll);


        String[] groupString = new String[groupArray.size() + 1];
        groupString[0] = "None";

        for (int i = 0; i < groupArray.size(); i++) {
            groupString[i+1] = groupArray.get(i);
            if (groupString[i+1].equals(markerGroupName)) {
                groupIndex = i + 1;
            }

        }

        // edge case - set the output group to the selected one, this only
        // applies if the group isn't changed, otherwise it would return null
        group[0] = groupString[groupIndex];

        JComboBox comboBox = new JComboBox(groupString);
        comboBox.setEditable(true);
        comboBox.setSelectedIndex(groupIndex);
        comboBox.addActionListener(e -> {
            JComboBox cb = (JComboBox)e.getSource();
            group[0] = (String)cb.getSelectedItem();
        });
        
        int option = 0;

        if (configType == CONFIG_SAVEGAME) {
            Object[] inputFields = {localeString.getString("dialog_marker_select_name"), destName," ",
                    localeString.getString("dialog_marker_group_change"), comboBox," ",
                    separator, "<html><center><b><u>NOTE</b></u>:</center>" + localeString.getString("dialog_marker_group_empty_warning") + " ",
                    " "};
            option = JOptionPane.showConfirmDialog(this, inputFields, "" + localeString.getString("dialog_marker_edit_title") + " ( Node ID " + id +" )", JOptionPane.OK_CANCEL_OPTION, JOptionPane.INFORMATION_MESSAGE, getMarkerIcon());
        } else if (configType == CONFIG_ROUTEMANAGER) {
            Object[] inputFields = {localeString.getString("dialog_marker_select_name"), destName," ",
                    localeString.getString("dialog_marker_group_change"), comboBox};
            option = JOptionPane.showConfirmDialog(this, inputFields, "" + localeString.getString("dialog_marker_edit_title") + " ( Node ID " + id +" )", JOptionPane.OK_CANCEL_OPTION, JOptionPane.INFORMATION_MESSAGE, getMarkerIcon());
        }
        
        //int option = JOptionPane.showConfirmDialog(this, inputFields, "" + localeString.getString("dialog_marker_edit_title") + " ( Node ID " + id +" )", JOptionPane.OK_CANCEL_OPTION, JOptionPane.INFORMATION_MESSAGE, getMarkerIcon());

        if (option == JOptionPane.OK_OPTION) {

            if (group[0] == null || group[0].equals("None")) group[0] = "All";
            if (destName.getText() != null && destName.getText().length() > 0) {
                if (markerName.equals(destName.getText()) && markerGroupName.equals(group[0])) {
                    LOG.info("{}", localeString.getString("console_marker_edit_cancel_nochange"));
                    return null;
                } else {
                    // since we can't return more than 1 string, we have to package them up
                    return new destInfo(destName.getText(), group[0]);
                }
            }
        }
        LOG.info("{}", localeString.getString("console_marker_edit_cancel"));
        return null;
    }

    public static class destInfo{
        private final String name;
        private final String group;
        public destInfo(String destName, String groupName){
            name = destName;
            group = groupName;
        }
        // getter setters
        public String getName() {
            return name;
        }

        public String getGroup() {
            return group;
        }

    }

   //
   // getter and setters
   //

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

    public boolean isStale() {
        return stale;
    }

    public void setStale(boolean newStale) {
        if (isStale() != newStale) {
            stale = newStale;
            editor.setTitle(createTitle());
        }
    }

    //
    //
    //

    public static class NodeLinks {

        public MapNode node;
        public int nodeIDBackup;
        public LinkedList<MapNode> otherIncoming;
        public LinkedList<MapNode> otherOutgoing;
        public MapMarker linkedMarker;

        public NodeLinks(MapNode mapNode, LinkedList<MapNode> in, LinkedList<MapNode> out, MapMarker marker) {
            this.node = mapNode;
            this.nodeIDBackup = mapNode.id;
            this.otherIncoming = new LinkedList<>();
            this.otherOutgoing = new LinkedList<>();
            this.linkedMarker = marker;

            for (int i = 0; i <= in.size() - 1 ; i++) {
                MapNode inNode = in.get(i);
                if (!this.otherIncoming.contains(inNode)) this.otherIncoming.add(inNode);
            }
            for (int i = 0; i <= out.size() - 1 ; i++) {
                MapNode outNode = out.get(i);
                if (!this.otherOutgoing.contains(outNode)) this.otherOutgoing.add(outNode);
            }
        }
    }
}
