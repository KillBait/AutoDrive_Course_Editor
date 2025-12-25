package AutoDriveEditor.GUI;

import AutoDriveEditor.Classes.KeyBinds.Shortcut;
import AutoDriveEditor.Classes.Util_Classes.ColourUtils;
import AutoDriveEditor.Classes.Util_Classes.ProfileUtil;
import AutoDriveEditor.GUI.RenderThreads.ConnectionDrawThread;
import AutoDriveEditor.GUI.RenderThreads.NodeDrawThread;
import AutoDriveEditor.Listeners.MouseListener;
import AutoDriveEditor.Managers.ButtonManager;
import AutoDriveEditor.Managers.RenderManager;
import AutoDriveEditor.RoadNetwork.Connection;
import AutoDriveEditor.RoadNetwork.MapNode;
import AutoDriveEditor.RoadNetwork.RoadMap;
import com.formdev.flatlaf.extras.FlatSVGIcon;

import javax.swing.*;
import java.awt.*;
import java.awt.event.*;
import java.awt.geom.AffineTransform;
import java.awt.geom.Point2D;
import java.awt.image.BufferedImage;
import java.awt.image.RasterFormatException;
import java.math.BigDecimal;
import java.math.RoundingMode;
import java.time.LocalDate;
import java.time.Month;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.Map;
import java.util.Objects;
import java.util.concurrent.CountDownLatch;
import java.util.concurrent.locks.Lock;
import java.util.concurrent.locks.ReentrantLock;

import static AutoDriveEditor.AutoDriveEditor.*;
import static AutoDriveEditor.Classes.Util_Classes.GUIUtils.InputEvent_NONE;
import static AutoDriveEditor.Classes.Util_Classes.ImageUtils.getNewBufferImage;
import static AutoDriveEditor.Classes.Util_Classes.LoggerUtils.LOG;
import static AutoDriveEditor.Classes.Util_Classes.MathUtils.limitDoubleToDecimalPlaces;
import static AutoDriveEditor.Classes.Util_Classes.MathUtils.limitFloatToDecimalPlaces;
import static AutoDriveEditor.GUI.Buttons.Toolbar.Nodes.RotationButton.rotation;
import static AutoDriveEditor.GUI.MapImage.*;
import static AutoDriveEditor.GUI.MapPanel.NodeHoverTextList.TYPE_FOOTER;
import static AutoDriveEditor.GUI.MapPanel.NodeHoverTextList.TYPE_HEADER;
import static AutoDriveEditor.GUI.Menus.DebugMenu.Logging.LogHeightmapInfoMenu.bDebugLogHeightMapInfo;
import static AutoDriveEditor.GUI.Menus.DebugMenu.Logging.LogLinearLineInfoMenu.bDebugLogLinearlineInfo;
import static AutoDriveEditor.GUI.Menus.DebugMenu.Logging.LogZoomScaleMenu.bDebugLogZoomScale;
import static AutoDriveEditor.GUI.Menus.DebugMenu.ShowFrameInfo.bDebugShowFPSInfo;
import static AutoDriveEditor.GUI.Menus.DebugMenu.ShowFrameInfo.fpsProfileGroup;
import static AutoDriveEditor.GUI.Menus.DebugMenu.ShowHeightMapInfo.bDebugShowHeightMapInfo;
import static AutoDriveEditor.GUI.Menus.DebugMenu.ShowHeightMapInfo.heightMapProfileGroup;
import static AutoDriveEditor.GUI.Menus.DebugMenu.ShowNodeLocationInfo.bDebugShowNodeLocationInfo;
import static AutoDriveEditor.GUI.Menus.DebugMenu.ShowRenderProfileInfo.bDebugShowRenderProfileInfo;
import static AutoDriveEditor.GUI.Menus.DebugMenu.ShowRenderProfileInfo.renderProfileGroup;
import static AutoDriveEditor.GUI.Menus.DebugMenu.ShowZoomLevelInfo.bDebugShowZoomLevelInfo;
import static AutoDriveEditor.GUI.Menus.DebugMenu.ShowZoomLevelInfo.zoomProfileGroup;
import static AutoDriveEditor.GUI.Menus.FileMenu.SaveConfigMenu.menu_SaveConfig;
import static AutoDriveEditor.GUI.Menus.RoutesMenu.SaveRoutesXML.menu_SaveRoutesXML;
import static AutoDriveEditor.GUI.RenderThreads.ConnectionDrawThread.*;
import static AutoDriveEditor.GUI.RenderThreads.NodeDrawThread.*;
import static AutoDriveEditor.Listeners.MouseListener.prevMousePosX;
import static AutoDriveEditor.Listeners.MouseListener.prevMousePosY;
import static AutoDriveEditor.Locale.LocaleManager.getLocaleString;
import static AutoDriveEditor.Managers.IconManager.*;
import static AutoDriveEditor.Managers.ShortcutManager.ShortcutID.FOCUS_NODE_SHORTCUT;
import static AutoDriveEditor.Managers.ShortcutManager.getUserShortcutByID;
import static AutoDriveEditor.Managers.ShortcutManager.registerShortcut;
import static AutoDriveEditor.RoadNetwork.RoadMap.getConnection;
import static AutoDriveEditor.RoadNetwork.RoadMap.networkNodesList;
import static AutoDriveEditor.XMLConfig.AutoSave.startAutoSaveThread;
import static AutoDriveEditor.XMLConfig.EditorXML.*;
import static AutoDriveEditor.XMLConfig.GameXML.saveGameConfig;
import static AutoDriveEditor.XMLConfig.RoutesXML.saveRouteManagerXML;

@SuppressWarnings("LoggingSimilarMessage")
public class MapPanel extends JLayeredPane {

    public static final int CONFIG_SAVEGAME = 1;
    public static final int CONFIG_ROUTEMANAGER = 2;

    public static int MIN_VISIBLE_NODE_SIZE = 2;
    public static int configType;

    public final Thread nodeDrawThread;
    public final Thread connectionDrawThread;

    public static final Lock drawLock = new ReentrantLock();
    public static CountDownLatch threadCountLatch;
    public static CountDownLatch drawOrderLatch;
    private byte frameCounter = 0;
    private int averageFPS = 0;
    private float averageFrame = 0;
    private float totalTime;

    public static BufferedImage regularNodeImage;
    public static BufferedImage rotationNodeImage;
    public static BufferedImage rotationNodeSelectedImage;
    public static BufferedImage subprioNodeImage;
    public static BufferedImage selectedNodeOverlayImage;
    public static BufferedImage controlNodeImage;
    public static BufferedImage controlNodeSelectedImage;
    public static BufferedImage radiusNodeImage;
    private static BufferedImage overlapWarningImage;
    private static BufferedImage negativeHeightWarningImage;
    private static BufferedImage parkingImage;
    private static BufferedImage markerImage;


    public static float nodeSizeScaled;
    public static float nodeSizeScaledHalf;
    public static float nodeSizeScaledQuarter;
    public static float nodeSizeWorld;

    public static float gridSpacingX;
    public static float gridSpacingY;
    public static int gridSubDivisions;

    public static double relativeCentreX = 0.5;
    public static double relativeCentreY = 0.5;
    public static double zoomLevel = 1F;
    public static int mapScale = 1;
    public Timer zoomTimer;

    public static boolean stale = false;
    public static RoadMap roadMap;

    public MapNode hoveredNode = null;
    public static boolean isDraggingMap = false;
    public static boolean bIsShiftPressed;
    public static Graphics renderGraphics;

    public MapPanel() {

        setLayer(this, 0);

        // Add the Listeners needed to handle user input
        MouseListener mouseListener = new MouseListener(this);
        addMouseListener(mouseListener);
        addMouseMotionListener(mouseListener);
        addMouseWheelListener(mouseListener);

        // Add all keybindings

        InputMap iMap = this.getInputMap(JComponent.WHEN_IN_FOCUSED_WINDOW);
        ActionMap aMap = this.getActionMap();
        iMap.put(KeyStroke.getKeyStroke(KeyEvent.VK_SHIFT, InputEvent.SHIFT_DOWN_MASK),"ShiftPressed");
        iMap.put(KeyStroke.getKeyStroke(KeyEvent.VK_SHIFT, InputEvent_NONE, true),"ShiftReleased");
        aMap.put("ShiftPressed", new AbstractAction() {
            @Override
            public void actionPerformed(ActionEvent e) {
                bIsShiftPressed = true;
            }
        });
        aMap.put("ShiftReleased", new AbstractAction() {
            @Override
            public void actionPerformed(ActionEvent e) {
                bIsShiftPressed = false;
            }
        });


        Shortcut focusShortcut = getUserShortcutByID(FOCUS_NODE_SHORTCUT);
        if (focusShortcut != null) {
            Action focusButtonAction = new AbstractAction() {
                @Override
                public void actionPerformed(ActionEvent e) {
                    if (hoveredNode != null) centreNodeInMapPanel(hoveredNode);
                }
            };
            registerShortcut(this, focusShortcut, focusButtonAction, this);
        }

        // Add a ComponentListener to handle window resizing and refreshing
        addComponentListener(new ComponentAdapter(){
            @Override
            public void componentResized(ComponentEvent e) {
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

        // Start the autosave thread ( if enabled in Editor.xml )

        if (bAutoSaveEnabled) {
            startAutoSaveThread();
        } else {
            LOG.info("AutoSave is disabled");
        }

        // Create the map panel icons
        markerImage = getSVGBufferImage(MARKER_ICON, 25, 25);
        parkingImage = getSVGBufferImage(PARKING_ICON, 20, 20);
        overlapWarningImage = getSVGBufferImage(WARNING_ICON, 20, 20);
        negativeHeightWarningImage = getSVGBufferImage(WARNING_Y_ICON, 20, 20);


        // Not sure if this helps, but does not appear to have a negative effect either
        setDoubleBuffered(true);
    }

    // Draw the snap grid

    public synchronized void drawGrid(Graphics g) {

        if (zoomLevel > 2) {

            Point2D panelWorldTopLeft = screenPosToWorldPos(0, 0);
            Point2D panelWorldBottomRight = screenPosToWorldPos(this.getWidth(), this.getHeight());

            g.setColor(colourGridLines);

            double startX = Math.floor(panelWorldTopLeft.getX() / gridSpacingX) * gridSpacingX;
            for (double worldX = startX; worldX < panelWorldBottomRight.getX(); worldX += gridSpacingX) {
                Point2D worldStart = worldPosToScreenPos(worldX, panelWorldTopLeft.getY());
                g.drawLine((int) worldStart.getX(), (int) worldStart.getY(), (int) worldStart.getX(), getMapPanel().getHeight());
            }

            // Draw horizontal lines
            double startY = Math.floor(panelWorldTopLeft.getY() / gridSpacingY) * gridSpacingY;
            for (double worldY = startY; worldY < panelWorldBottomRight.getY(); worldY += gridSpacingY) {
                Point2D worldStart = worldPosToScreenPos(panelWorldTopLeft.getX(), worldY);
                g.drawLine((int) worldStart.getX(), (int) worldStart.getY(), getMapPanel().getWidth(), (int) worldStart.getY());
            }
        }
    }

    @Override
    protected void paintComponent(Graphics g) {
        super.paintComponent(g);

        ProfileUtil totalDrawTimer = new ProfileUtil();
        ProfileUtil scaledDrawTimer = new ProfileUtil();
        ProfileUtil gridDrawTimer = new ProfileUtil();
        ProfileUtil renderTimer = new ProfileUtil();
        ProfileUtil buttonDrawTimer = new ProfileUtil();

        Graphics2D g2 = (Graphics2D) g.create();
        g2.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);
        g2.setRenderingHint(RenderingHints.KEY_ALPHA_INTERPOLATION, RenderingHints.VALUE_ALPHA_INTERPOLATION_SPEED);
        g2.setRenderingHint(RenderingHints.KEY_COLOR_RENDERING, RenderingHints.VALUE_COLOR_RENDER_SPEED);
        g2.setRenderingHint(RenderingHints.KEY_DITHERING, RenderingHints.VALUE_DITHER_DISABLE);
        g2.setRenderingHint(RenderingHints.KEY_FRACTIONALMETRICS, RenderingHints.VALUE_FRACTIONALMETRICS_OFF);
        g2.setRenderingHint(RenderingHints.KEY_INTERPOLATION, RenderingHints.VALUE_INTERPOLATION_NEAREST_NEIGHBOR);
        g2.setRenderingHint(RenderingHints.KEY_RENDERING, RenderingHints.VALUE_RENDER_SPEED);
        g2.setRenderingHint(RenderingHints.KEY_STROKE_CONTROL, RenderingHints.VALUE_STROKE_DEFAULT);
        g2.setRenderingHint(RenderingHints.KEY_TEXT_ANTIALIASING, RenderingHints.VALUE_TEXT_ANTIALIAS_OFF);

        if (roadMap != null) {

            if (bDebugShowRenderProfileInfo || bDebugShowFPSInfo) totalDrawTimer.startTimer();

            if (bDebugShowRenderProfileInfo) scaledDrawTimer.startTimer();
            getResizedMap(g2);
            if (bDebugShowRenderProfileInfo) scaledDrawTimer.stopTimer();

            if (bShowGrid) {
                gridDrawTimer.startTimer();
                drawGrid(g2);
                gridDrawTimer.stopTimer();
            }

            ArrayList<MapNode> visibleNodes = new ArrayList<>();
            Point2D topLeft = screenPosToWorldPos(0, 0);
            Point2D bottomRight = screenPosToWorldPos(getWidth(), getHeight());
            double offScreenDistance = 24;

            // Calculate visible nodes
            for (MapNode mapNode : RoadMap.networkNodesList) {
                if (topLeft.getX() - offScreenDistance < mapNode.x &&
                        bottomRight.getX() + offScreenDistance > mapNode.x &&
                        topLeft.getY() - offScreenDistance < mapNode.z &&
                        bottomRight.getY() + offScreenDistance > mapNode.z) {
                    visibleNodes.add(mapNode);
                }
            }

            // Pass visible nodes to threads
            NodeDrawThread.setVisibleNodes(visibleNodes);
            ConnectionDrawThread.setVisibleNodes(visibleNodes);

            if (bDebugShowRenderProfileInfo) renderTimer.startTimer();
            if (roadMap != null) {

                renderGraphics = g2;
                threadCountLatch = new CountDownLatch(2);
                drawOrderLatch = new CountDownLatch(1);

                connectionDrawThread.interrupt();
                nodeDrawThread.interrupt();

                try {
                    threadCountLatch.await();
                } catch (InterruptedException e) {
                    e.printStackTrace();
                }
                drawNodeInfo(g2);
            }
            if (bDebugShowRenderProfileInfo) buttonDrawTimer.startTimer();
            RenderManager.drawAll(g2);
            if (bDebugShowRenderProfileInfo) {
                buttonDrawTimer.stopTimer();
                renderTimer.stopTimer();
            }
            if (bDebugShowRenderProfileInfo || bDebugShowFPSInfo) {
                totalDrawTimer.stopTimer();
            }

            if (bDebugShowFPSInfo) {
                float frameTime = (float) totalDrawTimer.getTime(3);
                byte maxFrameCount = 50;
                if (frameCounter < maxFrameCount) {
                    totalTime += frameTime;
                    frameCounter++;
                } else {
                    averageFrame = totalTime / maxFrameCount;
                    averageFPS = (int) (1000 / averageFrame);

                    totalTime = 0;
                    frameCounter = 0;
                }
            }
        } else {

            FlatSVGIcon logo = getSVGIcon(LOGO);
            if (logo != null) {
                int panelWidth = getWidth();
                int panelHeight = getHeight();
                int imageWidth = logo.getWidth();
                int imageHeight = logo.getHeight();

                // Calculate the scaling factor to maintain aspect ratio
                double scaleFactor = Math.min((double) panelWidth / imageWidth, (double) panelHeight / imageHeight);

                // Calculate the new dimensions
                int newWidth = (int) (imageWidth * scaleFactor);
                int newHeight = (int) (imageHeight * scaleFactor);

                BufferedImage buff = getSVGBufferImage(logo, newWidth, newHeight, null);

                // Calculate the top-left coordinates to center the image
                int x = (panelWidth - newWidth) / 2;
                int y = (panelHeight - newHeight) / 2;

                g.drawImage(buff, x, y, newWidth, newHeight, this);
            }
        }

        if (bDebugShowRenderProfileInfo) {
            renderProfileGroup.reset();
            renderProfileGroup.addText("Connection Compute", connectionComputeTimer.getTime(3) + " ms");
            renderProfileGroup.addText("Connection Render", connectionDrawTimer.getTime(3) + " ms");
            renderProfileGroup.addText("Total Connections Rendered", String.valueOf(connectionDrawTotal));
            renderProfileGroup.addText("Node Compute", nodeComputeTimer.getTime(3) + " ms");
            renderProfileGroup.addText("Node Render", nodeDrawTimer.getTime(3) + " ms");
            renderProfileGroup.addText("Total Nodes Rendered", String.valueOf(nodeComputeTotal));
            renderProfileGroup.addText("Image Render", imageDrawTimer.getTime(3) + " ms");
            renderProfileGroup.addText("Text Render", textDrawTimer.getTime(3) + " ms");
            renderProfileGroup.addText("Button Render", buttonDrawTimer.getTime(3) + " ms");
            renderProfileGroup.addText("Selection Render", rectangleDrawTimer.getTime(3) + " ms");
            renderProfileGroup.addEmptyLine();
            renderProfileGroup.addCenteredText("Render Times", true);
            renderProfileGroup.addText("Render Thread", renderTimer.getTime(3) + " ms");
            renderProfileGroup.addText("Map Image Render", scaledDrawTimer.getTime(3) + " ms");
            renderProfileGroup.addText("Grid Render", gridDrawTimer.getTime(3) + " ms");
            renderProfileGroup.addLine();
            renderProfileGroup.addText("Total Render Time", totalDrawTimer.getTime(3) + " ms");
        }

        if (bDebugShowZoomLevelInfo) {
            zoomProfileGroup.reset();
            zoomProfileGroup.addText("Current", String.valueOf(limitDoubleToDecimalPlaces(zoomLevel, 1, RoundingMode.HALF_UP)));
            zoomProfileGroup.addText("Maximum", String.valueOf(maxZoomLevel));
        }

        if (bDebugShowFPSInfo) {
            fpsProfileGroup.reset();
            fpsProfileGroup.addText("Average FPS", String.valueOf(averageFPS));
            fpsProfileGroup.addText("Average Frame", limitFloatToDecimalPlaces(averageFrame, 3, RoundingMode.HALF_UP) + " ms");
        }

        debugDisplayManager.drawDebug(g2);

        g2.dispose();
    }

    private void drawNodeInfo(Graphics2D g2) {
        ArrayList<NodeHoverTextList> infoList = new ArrayList<>();

        if (hoveredNode != null && nodeSizeScaled >= MIN_VISIBLE_NODE_SIZE) {

            if (bDebugShowNodeLocationInfo || bShowSelectedNodeID) {
                if (!hoveredNode.isControlNode()) infoList.add(new NodeHoverTextList("Node ID " + hoveredNode.id, TYPE_HEADER, Color.BLACK, Color.WHITE, true, 1.0f));
            }

            if (hoveredNode.isParkDestination()) {
                infoList.add(new NodeHoverTextList("Park Destination " + hoveredNode.getParkingID(), TYPE_HEADER, Color.WHITE, new Color(16, 106, 238
                ), true, 1.0f));
                for (String vehicleId : hoveredNode.getParkedVehiclesList()) {
                    infoList.add(new NodeHoverTextList("Vehicle ID : " + vehicleId, TYPE_FOOTER, Color.BLACK, Color.CYAN, true, 1.0f));
                }
            }

            if (hoveredNode.y == -1) {
                infoList.add(new NodeHoverTextList("Node Y is invalid ( -1 )", TYPE_HEADER, Color.BLACK, Color.RED, true, 1.0f));
            }

            if (hoveredNode.hasWarning()) {
                infoList.add(new NodeHoverTextList((hoveredNode.getWarningNodes().size()) + " Overlapping Nodes", TYPE_HEADER, Color.BLACK, Color.YELLOW, true, 1.0f));
                int numWarnings = 0;
                for (MapNode overlapNode: hoveredNode.getWarningNodes()) {
                    infoList.add(new NodeHoverTextList("ID: " + overlapNode.id, TYPE_FOOTER, Color.BLACK, new Color(255,255,170), false, 0.75f));
                    numWarnings++;
                    if (numWarnings >= 9) {
                        infoList.add(new NodeHoverTextList(" + " + ((hoveredNode.getWarningNodes().size() + 1) - numWarnings) + " Others", TYPE_FOOTER, Color.BLACK, new Color(255,255,170), true, 0.75f));
                        break;
                    }
                }
            }

            if (bDebugShowNodeLocationInfo) {
                Color nodeInfoBGColour = new Color(200,255,200);
                Color nodeInfoFGColour = Color.BLACK;
                Color connectionBGColour = new Color(255, 200, 0);
                Color connectionFGColour = Color.BLACK;
                infoList.add(new NodeHoverTextList("Node Information", TYPE_HEADER, nodeInfoFGColour, Color.GREEN, true, 1.0f));
                infoList.add(new NodeHoverTextList("X: " + hoveredNode.x, TYPE_FOOTER, nodeInfoFGColour, nodeInfoBGColour, false, 0.75f));
                if (!hoveredNode.isControlNode()) infoList.add(new NodeHoverTextList("Y: " + hoveredNode.y, TYPE_FOOTER, nodeInfoFGColour, nodeInfoBGColour, false, 0.75f));
                infoList.add(new NodeHoverTextList("Z: " + hoveredNode.z, TYPE_FOOTER, nodeInfoFGColour, nodeInfoBGColour, false, 0.75f));
                if (!hoveredNode.isControlNode()) {
                    String flagType = (hoveredNode.flag == 0)?" (Regular)" : " (Subprio)";
                    infoList.add(new NodeHoverTextList("Flag: " + hoveredNode.flag + flagType, TYPE_FOOTER, nodeInfoFGColour, nodeInfoBGColour, false, 0.75f));
                    if (hoveredNode.isNodeHidden()) infoList.add(new NodeHoverTextList("Hidden: " + hoveredNode.isNodeHidden(), TYPE_FOOTER, nodeInfoFGColour, nodeInfoBGColour, false, 0.75f));
                    infoList.add(new NodeHoverTextList("Connections", TYPE_HEADER, connectionFGColour, Color.ORANGE, true, 1.0f));
                    if (!hoveredNode.incoming.isEmpty()) {
                        int numIncoming = 0;
                        infoList.add(new NodeHoverTextList(hoveredNode.incoming.size() + " In ID:", TYPE_FOOTER, connectionFGColour, connectionBGColour, false, 0.75f));
                        for (MapNode inNode: hoveredNode.incoming) {
                            String type = getConnection(inNode, hoveredNode).getDescription();
                            infoList.add(new NodeHoverTextList("  " + inNode.id + " (" + type + ")", TYPE_FOOTER, connectionFGColour, connectionBGColour, false, 0.75f));
                            numIncoming++;
                            if (numIncoming > 5) {
                                infoList.add(new NodeHoverTextList(" + " + (hoveredNode.incoming.size() - 5) + " Others", TYPE_FOOTER, connectionFGColour, connectionBGColour, true, 0.75f));
                                break;
                            }
                        }
                    } else {
                        infoList.add(new NodeHoverTextList("   No Incoming", TYPE_FOOTER, connectionFGColour, connectionBGColour, false, 0.75f));
                    }
                    infoList.add(new NodeHoverTextList("----------", TYPE_FOOTER, connectionFGColour, connectionBGColour, false, 0.75f));
                    if (!hoveredNode.outgoing.isEmpty()) {
                        infoList.add(new NodeHoverTextList(hoveredNode.outgoing.size() + " Out ID:", TYPE_FOOTER, connectionFGColour, connectionBGColour, false, 0.75f));
                        int numOutgoing = 0;
                        for (MapNode outNode: hoveredNode.outgoing) {
                            String type = getConnection(hoveredNode, outNode).getDescription();
                            infoList.add(new NodeHoverTextList("  " + outNode.id + " (" + type + ")", TYPE_FOOTER, connectionFGColour, connectionBGColour, false, 0.75f));
                            numOutgoing++;
                            if (numOutgoing > 5) {
                                infoList.add(new NodeHoverTextList(" + " + (hoveredNode.outgoing.size() - 5) + " Others", TYPE_FOOTER, connectionFGColour, connectionBGColour, true, 0.75f));
                                break;
                            }
                        }

                    } else {
                        infoList.add(new NodeHoverTextList("   No Outgoing:", TYPE_FOOTER, connectionFGColour, connectionBGColour, false, 0.75f));

                    }
                    if (!hoveredNode.getHiddenConnectionsList().isEmpty()) {
                        Color hiddenBGColour = Color.CYAN;
                        Color hiddenFGColour = Color.BLACK;
                        infoList.add(new NodeHoverTextList("Hidden Connections", TYPE_HEADER, hiddenFGColour, hiddenBGColour, true, 1.0f));
                        for (MapNode hiddenNode : hoveredNode.getHiddenConnectionsList()) {
                            infoList.add(new NodeHoverTextList("  " + hiddenNode.id, TYPE_FOOTER, hiddenFGColour, hiddenBGColour, false, 0.75f));
                        }
                    }

                } else {
                    infoList.add(new NodeHoverTextList("Flag: Control Node", TYPE_FOOTER, nodeInfoFGColour, nodeInfoBGColour, false, 0.75f));
                }
            }

            // get the default font related information
            FontMetrics fm = g2.getFontMetrics();
            int lineHeight = fm.getHeight();

            if (!infoList.isEmpty()) {
                // loop through all the strings to display, compare and updateVisibility maxStringLength to use later
                int maxStringLength = 0;
                for (NodeHoverTextList infoEntry : infoList) {
                    if (fm.stringWidth(infoEntry.text) >= maxStringLength) maxStringLength = fm.stringWidth(infoEntry.text);
                }

                int maxWidth = maxStringLength + (fm.getAscent() * 2);
                int numFooters = 0;
                int totalHeight = 0;
                int totalEntries = 0;

                Point2D screenPos = worldPosToScreenPos(hoveredNode.x, hoveredNode.z);
                int bgStartX = Math.max(0, (int) (screenPos.getX() + nodeSizeScaledHalf));
                int bgStartY = Math.max(0, (int) screenPos.getY());

                int infoBoxBottomPos = bgStartY + (infoList.size() * lineHeight);
                int infoBoxRightPos = bgStartX + maxWidth;

                if (infoBoxBottomPos > getMapPanel().getHeight()) {
                    bgStartY -= (infoBoxBottomPos - getMapPanel().getHeight());
                }

                if (infoBoxRightPos > getMapPanel().getWidth()) {
                    bgStartX -= (infoBoxRightPos - getMapPanel().getWidth());
                }

                for (NodeHoverTextList infoEntry : infoList) {

                    // draw the transparent background colour
                    g2.setComposite(AlphaComposite.SrcOver.derive(infoEntry.transparency));
                    g2.setColor(infoEntry.bgColour);
                    g2.fillRect(bgStartX, bgStartY + (lineHeight * totalEntries), maxWidth, lineHeight);

                    // draw the text
                    int textCentreOffset;
                    g2.setComposite(AlphaComposite.SrcOver.derive(1.0f));
                    g2.setColor(infoEntry.fgColour);
                    if (infoEntry.centreText) {
                        textCentreOffset = ( maxWidth - fm.stringWidth(infoEntry.text)) / 2;
                    } else {
                        textCentreOffset = fm.getAscent() / 2;
                    }
                    g2.drawString(infoEntry.text, bgStartX + textCentreOffset, (bgStartY + fm.getAscent()) + (lineHeight * totalEntries));

                    // draw a border if the entry type is TYPE_HEADER
                    if (infoEntry.type == TYPE_HEADER) {
                        g2.setColor(Color.BLACK);
                        // Draw HEADER border
                        g2.drawRect(bgStartX, bgStartY + (lineHeight * totalEntries), maxWidth, lineHeight);
                        // Draw the border around all the footers
                        if (numFooters > 0) {
                            g2.drawRect(bgStartX, bgStartY + (totalHeight - (lineHeight * numFooters)), maxWidth, (lineHeight * numFooters));
                        }
                        numFooters = 0;
                    } else {
                        numFooters++;
                    }

                    // if we have reached the end of the list, draw the rectangular outline around the last group of footers
                    if (totalEntries == infoList.size() - 1) {
                        if (numFooters > 0) {
                            g2.setColor(Color.BLACK);
                            g2.drawRect(bgStartX, bgStartY + (totalHeight - (lineHeight * (numFooters - 1))), maxWidth, (lineHeight * numFooters));
                        }
                    }
                    totalHeight += fm.getHeight();
                    totalEntries++;
                }
            }
        }
    }

    private void getResizedMap(Graphics2D g2) throws RasterFormatException {
        if (pdaImage != null) {

            // Clamp the relativeCentre values to stay within the map bounds

            float halfWidthRatio = (float) ((getMapPanel().getWidth() * 0.5) / (zoomLevel * pdaImage.getWidth()));
            float halfHeightRatio = (float) ((getMapPanel().getHeight() * 0.5) / (zoomLevel * pdaImage.getHeight()));

            relativeCentreX = Math.max(halfWidthRatio, Math.min(relativeCentreX, 1 - halfWidthRatio));
            relativeCentreY = Math.max(halfHeightRatio, Math.min(relativeCentreY, 1 - halfHeightRatio));

            // Calculate the new draw offset
            double offsetX = relativeCentreX * pdaImage.getWidth() - (this.getWidth() / zoomLevel) / 2;
            double offsetY = relativeCentreY * pdaImage.getHeight() - (this.getHeight() / zoomLevel) / 2;

            // Apply transformations and draw the image
            AffineTransform transform = new AffineTransform();
            transform.scale(zoomLevel, zoomLevel);
            transform.translate(-offsetX, -offsetY);
            g2.drawImage(mapPanelImage, transform, null);
        }
    }

    public static void moveMapBy(int diffX, int diffY) {
        if (roadMap != null && pdaImage != null) {
            // Move the relative centre values by the amount specified
            relativeCentreX -= diffX / (zoomLevel * pdaImage.getWidth());
            relativeCentreY -= diffY / (zoomLevel * pdaImage.getHeight());
            widgetManager.updateAllWidgets();
            getMapPanel().repaint();
        }
    }

    public void setNewZoomLevel(int direction) {
        // Quick exponential scaling calculation
        double newZoomLevel = getNewZoomLevel(direction);
        if (bDebugLogZoomScale) LOG.info("## setNewZoomLevel() ## Applying new zoomLevel - Old = {} , New = {}",zoomLevel, newZoomLevel);
        if (bInterpolateZoom) {
            if (zoomTimer == null) {
                if (bDebugLogZoomScale) LOG.info("Creating Zoom Interpolation Timer");
                try {
                    zoomTimer = new Timer((int) (2 / newZoomLevel), e -> {
                        interpolateZoom(newZoomLevel);
                    });
                    zoomTimer.start();
                } catch (Exception e) {
                    LOG.info("Exception creating interpolateZoom() timer, setting new zoom level immediately.");
                }
            }
        }
        zoomLevel = newZoomLevel;
        updateNodeScaling();
    }

    private double getNewZoomLevel(int direction) {
        double newZoomLevel = zoomLevel;
        float scaleFactor = (float) Math.pow(1.6, Math.abs(direction));
        if (direction < 0) {
            // Zoom in
            newZoomLevel *= scaleFactor;
        } else if (direction > 0) {
            // Zoom out
            newZoomLevel /= scaleFactor;
        }

        // Calculate the minimum zoom level required to fit the entire image
        // within the mapPanel and make sure we always pick the highest
        float minZoomToFitWidth = (float)this.getWidth() / pdaImage.getWidth();
        float minZoomToFitHeight = (float) this.getHeight() / pdaImage.getHeight();
        float minZoomToFit = Math.max(minZoomToFitWidth, minZoomToFitHeight);

        // Enforce minimum and maximum zoom levels
        newZoomLevel = Math.min(Math.max(newZoomLevel, minZoomToFit), maxZoomLevel);
        return newZoomLevel;
    }

    private void interpolateZoom(double targetZoomLevel) {
        zoomLevel = zoomLevel + 0.5F * (targetZoomLevel - zoomLevel);
        if (Math.abs(targetZoomLevel - zoomLevel) < 0.01) {
            zoomLevel = targetZoomLevel;
            zoomTimer.stop();
            zoomTimer = null;
        }
        updateNodeScaling();
        widgetManager.updateAllWidgets();
        repaint();
    }

    public static MapNode getNodeAtWorldPosition(double worldPosX, double worldPosZ) {
        MapNode selected = null;

        if (roadMap != null) {
            // make sure we prioritize returning control nodes over regular nodes
            for (MapNode mapNode : networkNodesList) {
                if (worldPosX < mapNode.x + nodeSizeWorld && worldPosX > mapNode.x - nodeSizeWorld && worldPosZ < mapNode.z + nodeSizeWorld && worldPosZ > mapNode.z - nodeSizeWorld) {
                    selected = mapNode;
                    break;
                }
            }

            if (rotation != null && Objects.equals(buttonManager.getCurrentButtonID(),"RotateButton")) {
                MapNode rotateControlNode = rotation.getControlNode();
                if (worldPosX < rotateControlNode.x + nodeSizeWorld && worldPosX > rotateControlNode.x - nodeSizeWorld && worldPosZ < rotateControlNode.z + nodeSizeWorld && worldPosZ > rotateControlNode.z - nodeSizeWorld) {
                    return rotation.getControlNode();
                }
            }
        }
        return selected;
    }

    public static MapNode getNodeAtScreenPosition(int mousePosX, int mousePosY) {
        MapNode selected = null;
        Point2D outPos;

        if (roadMap != null) {
            // make sure we prioritize returning control nodes over regular nodes
            for (MapNode mapNode : networkNodesList) {
                outPos = worldPosToScreenPos(mapNode.x, mapNode.z);
                if (mousePosX < outPos.getX() + nodeSizeScaledHalf && mousePosX > outPos.getX() - nodeSizeScaledHalf && mousePosY < outPos.getY() + nodeSizeScaledHalf && mousePosY > outPos.getY() - nodeSizeScaledHalf) {
                    // TODO Fix node visibility check
                    /*if (mapNode.isNodeVisible())*/
                    selected = mapNode;
                    break;
                }
            }

            if (curveManager.isCurvePreviewCreated()) {
                MapNode node = curveManager.getControlNodeAt(mousePosX, mousePosY);
                if (node != null) {
                    return node;
                }
            }
        }
        return selected;
    }

    public static double getYValueFromHeightMap(double worldX, double worldZ) {
        if (heightMapImage != null) {
            double x, y;

            double scaleX = (double) pdaImage.getWidth() / (double)heightMapImage.getWidth();
            double scaleY = (double) pdaImage.getHeight() / (double)heightMapImage.getHeight();
            if (bDebugLogHeightMapInfo) LOG.info("heightmap scale = {} , {}", scaleX, scaleY);

            x = ((double)heightMapImage.getWidth() / 2) + (int) Math.floor((worldX / mapScale) / scaleX );
            y = ((double)heightMapImage.getHeight() / 2) + (int) Math.floor((worldZ / mapScale) / scaleY );

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
        double worldPosX = 0;
        double worldPosY = 0;
        if (pdaImage != null) {
             double topLeftX = (relativeCentreX * pdaImage.getWidth()) - ((getMapPanel().getWidth() / zoomLevel)/2);
             double topLeftY = (relativeCentreY * pdaImage.getHeight()) - ((getMapPanel().getHeight() / zoomLevel)/2);

             double diffScaledX = (double)screenX / zoomLevel;
             double diffScaledY = (double)screenY / zoomLevel;

             int centerPointOffsetX = (pdaImage.getWidth() / 2) * mapScale;
             int centerPointOffsetY = (pdaImage.getHeight() / 2) * mapScale;

             worldPosX = limitDoubleToDecimalPlaces(((topLeftX + diffScaledX) * mapScale) - centerPointOffsetX, 3, RoundingMode.HALF_UP);
             worldPosY = limitDoubleToDecimalPlaces(((topLeftY + diffScaledY) * mapScale) - centerPointOffsetY, 3, RoundingMode.HALF_UP);
         }
         return new Point2D.Double(worldPosX, worldPosY);
    }

    public static Point worldPosToScreenPos(Point2D worldPos) {
        return worldPosToScreenPos(worldPos.getX(), worldPos.getY());
    }

    public static Point worldPosToScreenPos(double worldX, double worldZ) {

        int centerPointOffset = 1024 * mapScale;

        worldX += centerPointOffset;
        worldZ += centerPointOffset;

        int scaledX = (int) ((worldX/ mapScale) * zoomLevel);
        int scaledY = (int) ((worldZ/ mapScale) * zoomLevel);

        double centerXScaled = (relativeCentreX * (pdaImage.getWidth()*zoomLevel));
        double centerYScaled = (relativeCentreY * (pdaImage.getHeight()*zoomLevel));

        int topLeftX = (int)centerXScaled - (getMapPanel().getWidth() / 2);
        int topLeftY = (int)centerYScaled - (getMapPanel().getHeight() / 2);

        return new Point(scaledX - topLeftX, scaledY - topLeftY);
    }

    // TODO: Re-do this function completely
    //       Changes:-
    //       (1) Change the behaviour to examine the current connection and adjust
    //           the incoming/outgoing to accomplish the desired connection type
    //       WARNING:-
    //       EXTREME care needs to be taken, one missed mistake or edge case can cause
    //       this function to corrupt a config beyond repair!!.

    public static boolean createConnectionBetween(MapNode start, MapNode target, Connection.ConnectionType type) {

        if (start == target) {
            if (bDebugLogLinearlineInfo) {
                LOG.info("createConnectionBetween() - start and target cannot be the same");
                new Exception("createConnectionBetween() - start and target cannot be the same").printStackTrace();
            }
            return false;
        }
        if (!networkNodesList.contains(start) || !networkNodesList.contains(target)) {
            LOG.info("createConnectionBetween() - One or more nodes are not part of the networkNodesList");
            LOG.info("createConnectionBetween() - start: {} , target: {}", start, target);
            return false;
        }

        if (!start.outgoing.contains(target)) {
            start.outgoing.add(target);

            if (type == Connection.ConnectionType.REGULAR || type == Connection.ConnectionType.CROSSED_REGULAR || type == Connection.ConnectionType.SUBPRIO) {
                if (!target.incoming.contains(start)) {
                    target.incoming.add(start);
                }
            } else if (type == Connection.ConnectionType.REVERSE || type == Connection.ConnectionType.CROSSED_REVERSE) {
                start.incoming.remove(target);
                target.incoming.remove(start);
                target.outgoing.remove(start);
            } else if (type == Connection.ConnectionType.DUAL) {
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
            if (type == Connection.ConnectionType.REGULAR || type == Connection.ConnectionType.CROSSED_REGULAR || type == Connection.ConnectionType.SUBPRIO) {
                start.outgoing.remove(target);
                target.incoming.remove(start);
            } else if (type == Connection.ConnectionType.REVERSE || type == Connection.ConnectionType.CROSSED_REVERSE) {
                start.outgoing.remove(target);
                start.incoming.remove(target);
                target.outgoing.remove(start);
                target.incoming.remove(start);

            } else if (type == Connection.ConnectionType.DUAL) {
                start.outgoing.remove(target);
                start.incoming.remove(target);
                target.incoming.remove(start);
                target.outgoing.remove(start);
            }
        }
        return true;
    }


    //
    // Mouse movement and drag detection
    //

    public void mouseMoved(int mousePosX, int mousePosY) {
        if (pdaImage != null) {
            if (bDebugShowHeightMapInfo) {
                if (heightMapImage != null) {
                    Point2D point = screenPosToWorldPos(mousePosX, mousePosY);

                    double scaleX = (double) heightMapImage.getWidth() / pdaImage.getWidth();
                    double scaleY = (double) heightMapImage.getHeight() / pdaImage.getHeight();

                    double x = (int) (((point.getX() / mapScale) + ((double) pdaImage.getWidth() / 2)) * scaleX);
                    double y = (int) (((point.getY() / mapScale) + ((double) pdaImage.getHeight() / 2)) * scaleY);

                    float hValue;

                    if (heightmapType == HeightmapFormat.USHORT_GREY) {
                        hValue = heightMapPixelData[(int) (y * heightMapImage16bit.getWidth() + x)] & 0xFFFF; // Convert to unsigned 16-bit value;
                        heightMapProfileGroup.reset();
                        heightMapProfileGroup.addText("Image Type", "16bit Grey");
                        heightMapProfileGroup.addText("Image Size", String.valueOf((heightMapImage16bit.getWidth() - 1)));
                        heightMapProfileGroup.addText("Value Red", "N/A");
                        heightMapProfileGroup.addText("Value Green", "N/A");

                    } else {
                        Color color = new Color(heightMapImage.getRGB((int)x, (int)y));
                        hValue = (float)((color.getRed()<<8) + color.getGreen());
                        heightMapProfileGroup.addText("Image Type", "8bit RGB");
                        heightMapProfileGroup.addText("Image Size", String.valueOf((heightMapImage.getWidth() - 1)));
                        heightMapProfileGroup.addText("Value Red", String.valueOf(color.getRed()));
                        heightMapProfileGroup.addText("Value Green", String.valueOf(color.getGreen()));
                    }
                    heightMapProfileGroup.addText("Position X", String.valueOf((int)x));
                    heightMapProfileGroup.addText("Position Y", String.valueOf((int)y));
                    heightMapProfileGroup.addText("Raw Value", String.valueOf((int)hValue));
                    heightMapProfileGroup.addText("Calculated Y", String.valueOf(limitFloatToDecimalPlaces(hValue/256, 3, RoundingMode.HALF_UP)));
                    getMapPanel().repaint();
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
        if (isDraggingMap) {
            moveMapBy(mousePosX - prevMousePosX, mousePosY - prevMousePosY);
        } else {
            if (buttonManager != null) {
                ButtonManager.ButtonInterface button = buttonManager.getCurrentButton();
                if (button != null && button.usePanelEdgeScrolling()) {
                    if (mousePosX >= this.getWidth() && mousePosX > prevMousePosX) moveMapBy(- 10, 0);
                    if (mousePosX <= 0 && mousePosX < prevMousePosX) moveMapBy(10, 0);
                    if (mousePosY >= this.getHeight() && mousePosY > prevMousePosY) moveMapBy(0, -10);
                    if (mousePosY <= 0 && mousePosY < prevMousePosY) moveMapBy( 0, 10);
                }
            }
        }
    }

    //
    // Left mouse button click/pressed/released states
    //
    public void mouseButtonPressed(MouseEvent e) {
        if (e.getButton() == MouseEvent.BUTTON2 && bMiddleMouseMove ||
                e.getButton() == MouseEvent.BUTTON1 && !bMiddleMouseMove) {
            isDraggingMap = true;
        }
    }

    public void mouseButtonReleased(MouseEvent ignoredE) {
        isDraggingMap = false;
    }

    public void mouseButtonClicked(MouseEvent ignoredE) {}

    public static void updateNodeScaling() {
        if (pdaImage != null) {
            nodeSizeScaled = (int) ((nodeSize * zoomLevel) *0.5f);
            // Ensure nodeSizeScaled is always an odd number
            if ((int) nodeSizeScaled % 2 == 0) {
                nodeSizeScaled += 1;
            }
            nodeSizeScaledHalf = nodeSizeScaled * 0.5f;
            nodeSizeScaledQuarter = nodeSizeScaled * 0.25f;

            Point2D nodeLeftEdge = screenPosToWorldPos((int) (100 - nodeSizeScaledQuarter), 0);
            Point2D nodeRightEdge = screenPosToWorldPos((int) (100 + nodeSizeScaledQuarter), 0);
            nodeSizeWorld = (float) (nodeRightEdge.getX() - nodeLeftEdge.getX());

            // updateWidget the cached node image for the render thread
            updateCachedNodeImages();
        }
    }

    public static void updateCachedNodeImages() {
        Graphics2D g;

        if (nodeSizeScaled > 0) {
            if (getSVGIcon((NODE_ICON)) != null) {

                // Create the regular node image

                FlatSVGIcon nodeNormal = getSVGIcon(NODE_ICON);
                addColourFilter(nodeNormal, new Color(128, 128, 128), colourNodeRegular);

                regularNodeImage = getSVGBufferImage(nodeNormal, (int) nodeSizeScaled, (int) nodeSizeScaled, null);

                // Create the subprio node image
                FlatSVGIcon nodeSubprio = getSVGIcon(NODE_ICON);
                addColourFilter(nodeSubprio, new Color(128, 128, 128), colourNodeSubprio);
                subprioNodeImage = getSVGBufferImage(nodeSubprio, (int) nodeSizeScaled, (int) nodeSizeScaled, null);

                FlatSVGIcon nodeSelectRing = getSVGIcon(NODE_SELECTION_ICON);
                addColourFilter(nodeSelectRing, new Color(175, 175, 175), colourNodeSelected);
                selectedNodeOverlayImage = getSVGBufferImage(nodeSelectRing, (int) nodeSizeScaled, (int) nodeSizeScaled, null);

                //
                // create the control node color filters
                //
                FlatSVGIcon controlNode = getSVGIcon(CONTROL_NODE_ICON);
                Map<Color, Color> filters = new HashMap<>();
                filters.put(new Color(128,128,128), colourNodeControl);
                filters.put(new Color(64,64,64), ColourUtils.darken(colourNodeControl, 50));
                FlatSVGIcon.ColorFilter controlNodeFilter = new FlatSVGIcon.ColorFilter().addAll(filters);
                controlNode.setColorFilter(controlNodeFilter);

                controlNodeImage = getSVGBufferImage(controlNode, (int) nodeSizeScaled, (int) nodeSizeScaled, null);
                FlatSVGIcon controlNodeSelectRing = getSVGIcon(CONTROL_NODE_SELECTED_ICON);
                addColourFilter(controlNodeSelectRing, new Color(128, 128, 128), colourNodeSelected);
                controlNodeSelectedImage = getSVGBufferImage(controlNodeSelectRing, (int) nodeSizeScaled, (int) nodeSizeScaled, null);

                FlatSVGIcon rotateNode = getSVGIcon(ROTATE_NODE_ICON);
                rotateNode.setColorFilter(controlNodeFilter);
                //addColourFilter(controlNodeSelectRing, new Color(255, 0, 214), colourNodeSelected);
                rotationNodeImage = getSVGBufferImage(rotateNode, (int) nodeSizeScaled, (int) nodeSizeScaled, null);

                FlatSVGIcon rotateSelectedNode = getSVGIcon(ROTATE_NODE_SELECTED_ICON);
                addColourFilter(rotateSelectedNode, new Color(128, 128, 128), colourNodeSelected);
                rotationNodeSelectedImage = getSVGBufferImage(rotateSelectedNode, (int) nodeSizeScaled, (int) nodeSizeScaled, null);

                FlatSVGIcon radiusNode = getSVGIcon(RADIUS_NODE_ICON);
                radiusNode.setColorFilter(controlNodeFilter);
                radiusNodeImage = getSVGBufferImage(radiusNode, (int) nodeSizeScaled, (int) nodeSizeScaled, null);


            } else {
                // The node SVG file didn't load for some reason, fall back to using the previous version way of creating the node images
                regularNodeImage = getNewBufferImage((int) nodeSizeScaled, (int) nodeSizeScaled, Transparency.BITMASK);
                g = (Graphics2D) regularNodeImage.getGraphics();
                g.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);
                g.setRenderingHint(RenderingHints.KEY_RENDERING, RenderingHints.VALUE_RENDER_QUALITY);
                g.setRenderingHint(RenderingHints.KEY_INTERPOLATION, RenderingHints.VALUE_INTERPOLATION_BICUBIC);
                g.setColor(colourNodeRegular);
                //g.drawImage(image, 0, 0, (int) nodeSizeScaled, (int) nodeSizeScaled, null);
                g.fillArc(0, 0, (int) nodeSizeScaled, (int) nodeSizeScaled, 0, 360);
                g.dispose();

                subprioNodeImage = getNewBufferImage((int) nodeSizeScaled, (int) nodeSizeScaled, Transparency.BITMASK);
                g = (Graphics2D) subprioNodeImage.getGraphics();
                g.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);
                g.setRenderingHint(RenderingHints.KEY_RENDERING, RenderingHints.VALUE_RENDER_QUALITY);
                g.setRenderingHint(RenderingHints.KEY_INTERPOLATION, RenderingHints.VALUE_INTERPOLATION_BICUBIC);
                g.setColor(colourNodeSubprio);
                //g.drawImage(image, 0, 0, (int) nodeSizeScaled, (int) nodeSizeScaled, null);
                g.fillArc(0, 0, (int) nodeSizeScaled, (int) nodeSizeScaled, 0, 360);
                g.dispose();

                selectedNodeOverlayImage = getNewBufferImage((int) nodeSizeScaled, (int) nodeSizeScaled, Transparency.TRANSLUCENT);
                g = (Graphics2D) selectedNodeOverlayImage.getGraphics();
                g.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);
                g.setRenderingHint(RenderingHints.KEY_RENDERING, RenderingHints.VALUE_RENDER_QUALITY);
                g.setRenderingHint(RenderingHints.KEY_INTERPOLATION, RenderingHints.VALUE_INTERPOLATION_BICUBIC);
                float borderThickness = nodeSizeScaled * .1f;
                g.setStroke(new BasicStroke(borderThickness));
                g.setColor(colourNodeSelected);
                g.drawOval((int) (borderThickness / 2), (int) (borderThickness / 2),
                        (int) (nodeSizeScaled - borderThickness), (int) (nodeSizeScaled - borderThickness));
                g.dispose();
            }
        }
    }

    public void centreNodeInMapPanel(MapNode node) {
        Point2D target = worldPosToScreenPos(node.x, node.z);
        double x = (this.getWidth() >> 1) - target.getX();
        double y = (this.getHeight() >> 1) - target.getY();
        moveMapBy((int) x, (int) y);
        widgetManager.updateAllWidgets();
        getMapPanel().repaint();
    }

    public static void forceMapImageRedraw() {
        if (pdaImage != null) {
            int widthScaled = (int) (getMapPanel().getWidth() / zoomLevel);
            int heightScaled = (int) (getMapPanel().getHeight() / zoomLevel);
            if (widthScaled < 0 || widthScaled> pdaImage.getWidth() || heightScaled < 0 || heightScaled > pdaImage.getHeight()) {
                zoomLevel = Math.max((float) getMapPanel().getWidth()/pdaImage.getWidth(),(float) getMapPanel().getHeight()/pdaImage.getHeight());
            }
            updateNodeScaling();
            widgetManager.updateAllWidgets();
            getMapPanel().repaint();
        }
    }

    public static void checkIfConfigIsStaleAndConfirmSave() {
        if (stale) {
            int response = JOptionPane.showConfirmDialog(editor, getLocaleString("dialog_exit_unsaved"), getLocaleString("AutoDrive"), JOptionPane.YES_NO_OPTION, JOptionPane.QUESTION_MESSAGE);
            if (response == JOptionPane.YES_OPTION) {
                if (configType == CONFIG_SAVEGAME) {
                    saveGameConfig(null, false, false);
                } else if (configType == CONFIG_ROUTEMANAGER) {
                    saveRouteManagerXML(null, false, false);
                }
                setStale(false);
            }
        }
    }

    //
    // getters
    //

    public static boolean isStale() { return stale; }
    public MapNode getHoveredNode() { return hoveredNode; }
    // Getters for the MapPanel Icons
    public static BufferedImage getOverlapWarningImage() { return overlapWarningImage; }
    public static BufferedImage getNegativeHeightWarningImage() { return negativeHeightWarningImage; }
    public static BufferedImage getParkingImage() { return parkingImage; }
    public static BufferedImage getMarkerImage() { return markerImage; }

    //
    // setters
    //

   public void setRoadMap(RoadMap roadMap) { MapPanel.roadMap = roadMap; }

   public void setMapScale(int newMapScale) { MapPanel.mapScale = newMapScale; }

   public static void setStale(boolean newStaleState) {
        if (stale != newStaleState) {
            stale = newStaleState;
            editor.setTitle(createWindowTitleString());
        }
        if (configType == CONFIG_SAVEGAME) {
            menu_SaveConfig.setEnabled(stale);
        } else if (configType == CONFIG_ROUTEMANAGER) {
            menu_SaveRoutesXML.setEnabled(stale);
        }
   }
    public void setHoveredNode(MapNode hoveredNode) { this.hoveredNode = hoveredNode; }


    /**
     * Hold all the information required to display the hover text for a node.
     */
    static class NodeHoverTextList {

        public static final int TYPE_HEADER = 0;
        public static final int TYPE_FOOTER = 1;
        final String text;
        final int type;
        final Color fgColour;
        final Color bgColour;
        final boolean centreText;
        final float transparency;

        public NodeHoverTextList(String text, int type, Color fgColour, Color bgColour, boolean centreText, float transparency) {
            this.text = text;
            this.type = type;
            this.fgColour = fgColour;
            this.bgColour = bgColour;
            this.centreText = centreText;
            this.transparency = transparency;
        }
    }
}
