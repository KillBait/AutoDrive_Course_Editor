package AutoDriveEditor.GUI;

import AutoDriveEditor.GUI.Curves.CurvePanel;
import AutoDriveEditor.GUI.RenderThreads.ConnectionDrawThread;
import AutoDriveEditor.GUI.RenderThreads.NodeDrawThread;
import AutoDriveEditor.Listeners.MouseListener;
import AutoDriveEditor.Managers.CopyPasteManager;
import AutoDriveEditor.RoadNetwork.MapNode;
import AutoDriveEditor.RoadNetwork.RoadMap;
import AutoDriveEditor.Utils.ExceptionUtils;
import AutoDriveEditor.Utils.GUIUtils;
import AutoDriveEditor.Utils.ProfileUtil;

import javax.swing.*;
import java.awt.*;
import java.awt.event.*;
import java.awt.geom.Point2D;
import java.awt.image.BufferedImage;
import java.awt.image.RasterFormatException;
import java.math.BigDecimal;
import java.math.RoundingMode;
import java.util.ArrayList;
import java.util.Objects;
import java.util.concurrent.CountDownLatch;
import java.util.concurrent.locks.Lock;
import java.util.concurrent.locks.ReentrantLock;

import static AutoDriveEditor.AutoDriveEditor.*;
import static AutoDriveEditor.Classes.MapImage.*;
import static AutoDriveEditor.GUI.Buttons.Curves.CubicCurveButton.cubicCurve;
import static AutoDriveEditor.GUI.Buttons.Curves.CubicCurveButton.isCubicCurveCreated;
import static AutoDriveEditor.GUI.Buttons.Curves.QuadCurveButton.isQuadCurveCreated;
import static AutoDriveEditor.GUI.Buttons.Curves.QuadCurveButton.quadCurve;
//quarticbezier
import static AutoDriveEditor.GUI.Buttons.Curves.QuarticCurveButton.isQuarticCurveCreated;
import static AutoDriveEditor.GUI.Buttons.Curves.QuarticCurveButton.quarticCurve;
//
import static AutoDriveEditor.GUI.Buttons.LinerLineBaseButton.*;
import static AutoDriveEditor.GUI.Buttons.Nodes.RotationButton.rotation;
import static AutoDriveEditor.GUI.Curves.CurvePanel.curveOptionsPanel;
import static AutoDriveEditor.GUI.MapPanel.NodeHoverTextList.TYPE_FOOTER;
import static AutoDriveEditor.GUI.MapPanel.NodeHoverTextList.TYPE_HEADER;
import static AutoDriveEditor.GUI.Menus.DebugMenu.Logging.LogHeightmapInfoMenu.bDebugLogHeightMapInfo;
import static AutoDriveEditor.GUI.Menus.DebugMenu.Logging.LogZoomScaleMenu.bDebugLogZoomScale;
import static AutoDriveEditor.GUI.Menus.DebugMenu.Logging.ShowZoomLevelInfo.bDebugShowZoomLevelInfo;
import static AutoDriveEditor.GUI.Menus.DebugMenu.ShowHeightMapInfo.bDebugShowHeightMapInfo;
import static AutoDriveEditor.GUI.Menus.DebugMenu.ShowNodeLocationInfo.bDebugShowNodeLocationInfo;
import static AutoDriveEditor.GUI.Menus.DebugMenu.ShowProfileInfo.bDebugShowProfileInfo;
import static AutoDriveEditor.GUI.Menus.FileMenu.SaveConfigMenu.menu_SaveConfig;
import static AutoDriveEditor.GUI.Menus.RoutesMenu.SaveRoutesXML.menu_SaveRoutesXML;
import static AutoDriveEditor.GUI.RenderThreads.ConnectionDrawThread.*;
import static AutoDriveEditor.GUI.RenderThreads.NodeDrawThread.*;
import static AutoDriveEditor.GUI.TextPanel.showInTextArea;
import static AutoDriveEditor.Listeners.MouseListener.prevMousePosX;
import static AutoDriveEditor.Listeners.MouseListener.prevMousePosY;
import static AutoDriveEditor.Locale.LocaleManager.getLocaleString;
import static AutoDriveEditor.Managers.MultiSelectManager.isMultiSelectDragging;
import static AutoDriveEditor.Managers.MultiSelectManager.useRectangularSelection;
import static AutoDriveEditor.RoadNetwork.MapNode.NODE_FLAG_REGULAR;
import static AutoDriveEditor.RoadNetwork.MapNode.NODE_FLAG_SUBPRIO;
import static AutoDriveEditor.RoadNetwork.RoadMap.*;
import static AutoDriveEditor.Utils.GUIUtils.InputEvent_NONE;
import static AutoDriveEditor.Utils.ImageUtils.getNewBufferImage;
import static AutoDriveEditor.Utils.LoggerUtils.LOG;
import static AutoDriveEditor.Utils.MathUtils.limitDoubleToDecimalPlaces;
import static AutoDriveEditor.XMLConfig.AutoSave.startAutoSaveThread;
import static AutoDriveEditor.XMLConfig.EditorXML.*;
import static AutoDriveEditor.XMLConfig.GameXML.saveGameConfig;
import static AutoDriveEditor.XMLConfig.RoutesXML.saveRouteManagerXML;

public class MapPanel extends JPanel {

    public static final int CONFIG_SAVEGAME = 1;
    public static final int CONFIG_ROUTEMANAGER = 2;

    public static double MIN_VISIBLE_NODE_SIZE = 2;
    public static int configType;

    public final Thread nodeDrawThread;
    public final Thread connectionDrawThread;

    public static final Lock drawLock = new ReentrantLock();
    public static CountDownLatch threadCountLatch;
    public static CountDownLatch drawOrderLatch;

    public static BufferedImage croppedImage;
    public static BufferedImage cachedRegularNodeImage;
    public static BufferedImage cachedSubprioNodeImage;

    public double offsetX, oldOffsetX;
    public double offsetY, oldOffsetY;

    public double widthScaled;
    public static double oldWidthScaled;
    public double heightScaled;
    public static double oldHeightScaled;

    public static double nodeSizeScaled;
    public static double nodeSizeScaledHalf;
    public static double nodeSizeScaledQuarter;
    public static double nodeSizeWorld;


    private static double x = 0.5;
    private static double y = 0.5;
    public static double zoomLevel = 1.0;
    public static int mapScale = 1;
    public Timer zoomTimer;

    public static boolean stale = false;
    public static RoadMap roadMap;
    public static MapNode hoveredNode = null;
    public static boolean isDraggingMap = false;
    public static CopyPasteManager cnpManager;
    public static boolean bIsShiftPressed;
    public static Graphics renderGraphics;

    public MapPanel() {

        // Create the curves panel and attach it to the MapPanel (initially hidden until needed)

        curveOptionsPanel = new CurvePanel();
        add( new GUIUtils.AlphaContainer(curveOptionsPanel));

        // Add the Listeners needed to handle user input

        MouseListener mouseListener = new MouseListener(this);
        addMouseListener(mouseListener);
        addMouseMotionListener(mouseListener);
        addMouseWheelListener(mouseListener);

        // Add all keybindings

        InputMap iMap = this.getInputMap(JComponent.WHEN_IN_FOCUSED_WINDOW);
        ActionMap aMap = this.getActionMap();

        iMap.put(KeyStroke.getKeyStroke("F"), "Focus");
        iMap.put(KeyStroke.getKeyStroke(KeyEvent.VK_SHIFT, InputEvent.SHIFT_DOWN_MASK),"ShiftPressed");
        iMap.put(KeyStroke.getKeyStroke(KeyEvent.VK_SHIFT, InputEvent_NONE, true),"ShiftReleased");
        aMap.put("Focus", new AbstractAction() {
            @Override
            public void actionPerformed(ActionEvent e) {
                if (hoveredNode != null) {
                    centreNodeInMapPanel(hoveredNode);
                }
            }
        });
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

        // initialize the copy/paste manager

        if (cnpManager == null) {
            LOG.info("Initializing CopyPaste Manager");
            cnpManager = new CopyPasteManager();
        }

        // Start the autosave thread ( if enabled in Editor.xml )

        if (bAutoSaveEnabled) {
            startAutoSaveThread();
        } else {
            LOG.info("AutoSave is disabled");
        }

        // initialize the cropped buffer

        croppedImage = getNewBufferImage(2024, 2048, Transparency.OPAQUE);

        // Not sure if this helps, but does not appear to have a negative effect either

        setDoubleBuffered(true);
    }

    // Draw the snap grid

    public synchronized void drawGrid(Graphics g) {

        if (zoomLevel > 2) {

            int worldMax = -1024 * mapScale;

            Point2D panelWorldTopLeft = screenPosToWorldPos(0, 0);
            Point2D panelWorldBottomRight = screenPosToWorldPos(this.getWidth(), this.getHeight());

            g.setColor(new Color(25, 25, 25));
            for (int worldX = worldMax; worldX < panelWorldBottomRight.getX(); worldX += gridSpacingX) {
                if (worldX > panelWorldTopLeft.getX()) {
                    Point2D worldStart = worldPosToScreenPos(worldX, panelWorldTopLeft.getY());
                    g.drawLine((int) worldStart.getX(), (int) worldStart.getY(), (int) worldStart.getX(), getMapPanel().getHeight());
                }
            }
            for (int worldY = worldMax; worldY < panelWorldBottomRight.getY(); worldY += gridSpacingY) {
                if (worldY > panelWorldTopLeft.getY()) {
                    Point2D worldStart = worldPosToScreenPos(panelWorldTopLeft.getX(), worldY);
                    g.drawLine((int) worldStart.getX(), (int) worldStart.getY(), getMapPanel().getWidth(), (int) worldStart.getY());
                }
            }
        }
    }

    @Override
    protected void paintComponent(Graphics g) {
        super.paintComponent(g);

        ProfileUtil totalDrawTimer = new ProfileUtil();
        ProfileUtil calcCroppedTimer = new ProfileUtil();
        ProfileUtil croppedDrawTimer = new ProfileUtil();
        ProfileUtil gridDrawTimer = new ProfileUtil();
        ProfileUtil renderTimer = new ProfileUtil();

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

        if (roadMap != null && croppedImage != null) {

            if (bDebugShowProfileInfo) totalDrawTimer.startTimer();

            if (bDebugShowProfileInfo) calcCroppedTimer.startTimer();
            getResizedMap();
            if (bDebugShowProfileInfo) calcCroppedTimer.stopTimer();
            if (bDebugShowProfileInfo) croppedDrawTimer.startTimer();
            g.drawImage(croppedImage, 0, 0, this.getWidth(), this.getHeight(), null);
            if (bDebugShowProfileInfo) croppedDrawTimer.stopTimer();

            if (bShowGrid) {
                gridDrawTimer.startTimer();
                drawGrid(g2);
                gridDrawTimer.stopTimer();
            }

            if (bDebugShowProfileInfo) renderTimer.startTimer();
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
                if (bDebugShowProfileInfo) renderTimer.stopTimer();
                if (bDebugShowProfileInfo) totalDrawTimer.stopTimer();
                drawNodeInfo(g2);
            }
        }

        if (bDebugShowProfileInfo) {

            // draw the background
            Graphics2D bg = (Graphics2D) g2.create();
            bg.setComposite(AlphaComposite.SrcOver.derive(0.75f));
            bg.setColor(Color.BLACK);
            bg.fillRect(15, 15, 190, 345);
            bg.dispose();

            g.setColor(Color.WHITE);

            if (bDebugShowProfileInfo) {
                String computeText = "Connection Compute " + connectionComputeTimer.getTime(3) + " ms";
                String renderText = "Connection Render " + connectionDrawTimer.getTime(3) + " ms";
                String renderTotal = "Connections Rendered " + connectionDrawTotal;
                String nodeComputeString = "Node Compute " + nodeComputeTimer.getTime(3) + " ms";
                String nodeRenderString = "Node Render " + nodeDrawTimer.getTime(3) + " ms";
                String nodeTotalString = "Nodes Rendered " + nodeComputeTotal;
                String imageRenderString = "Icon Render " + imageDrawTimer.getTime(3) + " ms";
                String textRenderString = "Text Render " + textDrawTimer.getTime(3) + " ms";
                String buttonRenderString = "Button Render " + buttonDrawTimer.getTime(3) + " ms";
                String rectangleRenderString = "Rectangle Render " + rectangleDrawTimer.getTime(3) + " ms";

                g.drawString("Render Threads Profile", 20, 30);
                g.drawString("----------------------------------------", 20, 40);
                g.drawString(computeText, 20, 60);
                g.drawString(renderText, 20, 75);
                g.drawString(renderTotal, 20, 90);
                g.drawString(nodeComputeString, 20, 105);
                g.drawString(nodeRenderString, 20, 120);
                g.drawString(nodeTotalString, 20, 135);
                g.drawString(imageRenderString, 20, 150);
                g.drawString(textRenderString, 20, 165);
                g.drawString(buttonRenderString, 20, 180);
                g.drawString(rectangleRenderString, 20, 195);

                String calcCroppedString = "Calc Cropped Image " + calcCroppedTimer.getTime(3) + " ms";
                String drawCroppedString = "Cropped -> MapPanel " + croppedDrawTimer.getTime(3) + " ms";
                String gridRenderString = "Grid Render " + gridDrawTimer.getTime(3) + " ms";
                String renderString = "Render Threads " + renderTimer.getTime(3) + " ms";
                String totalRenderString = "Total Render Time " + totalDrawTimer.getTime(3) + " ms";

                g.drawString("Totals", 20, 240);
                g.drawString("-------", 20, 255);
                g.drawString(calcCroppedString, 20, 270);
                g.drawString(drawCroppedString, 20, 285);
                g.drawString(gridRenderString, 20, 300);
                g.drawString(renderString, 20, 315);
                g.drawString("----------------------------------------", 20, 330);
                g.drawString(totalRenderString, 20, 345);
                totalDrawTimer.resetTimer();
            }
        }
        if (bDebugShowZoomLevelInfo) {
            g2.setColor(Color.WHITE);
            g2.drawString(""+limitDoubleToDecimalPlaces(zoomLevel, 1, RoundingMode.HALF_UP), 10, getMapPanel().getHeight()-10);
        }
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
                for (Integer vehicleId : hoveredNode.getParkedVehiclesList()) {
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
                            String type = "";
                            if (isDual(inNode, hoveredNode)) {
                                if (inNode.flag == NODE_FLAG_REGULAR && hoveredNode.flag == NODE_FLAG_REGULAR) {
                                    type = "Dual";
                                } else {
                                    type = "Subprio Dual";
                                }
                            } else if (isRegular(inNode, hoveredNode)) {
                                if (inNode.flag == NODE_FLAG_REGULAR) {
                                    type = "Regular";
                                } else if (inNode.flag == NODE_FLAG_SUBPRIO) {
                                    type = "Subprio Regular";
                                }
                            }
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
                            String type = "";
                            if (isDual(hoveredNode, outNode)) {
                                if (hoveredNode.flag == NODE_FLAG_REGULAR && outNode.flag == NODE_FLAG_REGULAR) {
                                    type = "Dual";
                                } else {
                                    type = "Subprio Dual";
                                }
                            } else if (isReverse(hoveredNode, outNode)) {
                                if (hoveredNode.flag == NODE_FLAG_REGULAR) {
                                    type = "Reverse";
                                } else {
                                    type = "Subprio Reverse";
                                }
                            } else if (isRegular(outNode, hoveredNode)) {
                                if (outNode.flag == NODE_FLAG_REGULAR) {
                                    type = "Regular";
                                } else if (outNode.flag == NODE_FLAG_SUBPRIO) {
                                    type = "Subprio Regular";
                                }
                            }
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
                        Color hiddenBGColour = new Color(128, 128, 128);
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
                // loop through all the strings to display, compare and update maxStringLength to use later
                int maxStringLength = 0;
                for (NodeHoverTextList infoEntry : infoList) {
                    if (fm.stringWidth(infoEntry.text) >= maxStringLength) maxStringLength = fm.stringWidth(infoEntry.text);
                }

                int maxWidth = maxStringLength + (fm.getAscent() * 2);
                int numFooters = 0;
                int totalHeight = 0;
                int totalEntries = 0;

                Point2D screenPos = worldPosToScreenPos(hoveredNode.x, hoveredNode.z);
                int bgStartX = (int) (screenPos.getX() + nodeSizeScaledHalf);
                int bgStartY = (int) screenPos.getY();

                int infoBoxBottomPos = bgStartY + (infoList.size() * lineHeight);
                int infoBoxRightPos = bgStartX + maxWidth;

                if (infoBoxBottomPos > getMapPanel().getHeight()) {
                    int diff = infoBoxBottomPos - getMapPanel().getHeight();
                    bgStartY = bgStartY - diff;
                }

                if (infoBoxRightPos > getMapPanel().getWidth()) {
                    int diff = infoBoxRightPos - getMapPanel().getWidth();
                    bgStartX = bgStartX - diff;
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

    private void getResizedMap() throws RasterFormatException {
        if (pdaImage != null) {

            widthScaled = (int) (this.getWidth() / zoomLevel);
            heightScaled = (int) (this.getHeight() / zoomLevel);

            double calcX = (((this.getWidth() * 0.5) / zoomLevel) / mapPanelImage.getWidth());
            double calcY = (((this.getHeight() * 0.5) / zoomLevel) / mapPanelImage.getHeight());

            x = Math.min(x, 1 - calcX);
            x = Math.max(x, calcX);
            y = Math.min(y, 1 - calcY);
            y = Math.max(y, calcY);

            int centerX = (int) (x * mapPanelImage.getWidth());
            int centerY = (int) (y * mapPanelImage.getHeight());

            double halfWidthScaled = widthScaled / 2;
            double halfHeightScaled = heightScaled / 2;

            offsetX = centerX - halfWidthScaled;
            offsetX = Math.max(offsetX, 0);
            offsetY = centerY - halfHeightScaled;
            offsetY = Math.max(offsetY, 0);

            if (offsetY + heightScaled > pdaImage.getHeight()) {
                heightScaled = mapPanelImage.getHeight() - offsetY;
            }
            if (offsetX + widthScaled > pdaImage.getWidth()) {
                widthScaled = mapPanelImage.getWidth() - offsetX;
            }

            //int centerX = (int) (x * pdaImage.getWidth());
            //int centerY = (int) (y * pdaImage.getHeight());

            //double offsetX = centerX - widthScaled / 2;
            //double offsetY = centerY - heightScaled / 2;

            /*LOG.info("zoom = {}", zoomLevel);

            AffineTransform transform = new AffineTransform();
            transform.translate(-offsetX, -offsetY);
            transform.scale(zoomLevel, zoomLevel);
            g.drawImage(pdaImage, transform, null);*/

            if (offsetX != oldOffsetX || offsetY != oldOffsetY || widthScaled != oldWidthScaled || heightScaled != oldHeightScaled) {
                try {
                    croppedImage = mapPanelImage.getSubimage((int)offsetX, (int)offsetY, (int)widthScaled, (int)heightScaled);

                    if (bDebugLogZoomScale) LOG.info("## MapPanel.ResizeMap() ## ZoomLevel = {} ## SubImage start at {},{} - size {},{}", zoomLevel, offsetX, offsetY, widthScaled, heightScaled);
                    oldOffsetX = offsetX;
                    oldOffsetY = offsetY;
                    oldWidthScaled = widthScaled;
                    oldHeightScaled = heightScaled;
                } catch (Exception e) {
                    LOG.info("## MapPanel.ResizeMap() ## Exception in getSubImage()");
                    LOG.info("## MapPanel.ResizeMap() ## x = {} , y = {} , offsetX = {} , offsetY = {}  -- PanelWidth = {} , PanelHeight = {} , zoomLevel = {} , widthScaled = {} , heightScaled = {}", x, y, offsetX, offsetY, this.getWidth(), this.getHeight(), zoomLevel, widthScaled, heightScaled);
                    e.printStackTrace();
                }
            }
        }
    }

    public static void moveMapBy(int diffX, int diffY) {
        if (roadMap != null && pdaImage != null) {
            x -= diffX / (zoomLevel * pdaImage.getWidth());
            y -= diffY / (zoomLevel * pdaImage.getHeight());
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
                    zoomTimer = new Timer((int) (5 / newZoomLevel), e -> interpolateZoom(newZoomLevel));
                    zoomTimer.start();
                } catch (Exception e) {
                    LOG.info("Exception creating interpolateZoom() timer, setting new zoom level immediately.");
                    zoomLevel = newZoomLevel;
                    updateNodeScaling();
                    repaint();
                }
            }
        } else {
            zoomLevel = newZoomLevel;
            updateNodeScaling();
            repaint();
        }

    }

    private double getNewZoomLevel(int direction) {
        double newZoomLevel = zoomLevel;
        double scaleFactor = Math.pow(1.6, Math.abs(direction));
        if (direction < 0) {
            // Zoom in
            newZoomLevel *= scaleFactor;
        } else if (direction > 0) {
            // Zoom out
            newZoomLevel /= scaleFactor;
        }

        // Calculate the minimum zoom level required to fit the entire image
        // within the mapPanel and make sure we always pick the highest
        double minZoomToFitWidth = (double)this.getWidth() / pdaImage.getWidth();
        double minZoomToFitHeight = (double)this.getHeight() / pdaImage.getHeight();
        double minZoomToFit = Math.max(minZoomToFitWidth, minZoomToFitHeight);

        // Enforce minimum and maximum zoom levels
        newZoomLevel = Math.min(Math.max(newZoomLevel, minZoomToFit), maxZoomLevel);
        return newZoomLevel;
    }

    private void interpolateZoom(double targetZoomLevel) {
        zoomLevel = zoomLevel + 0.5 * (targetZoomLevel - zoomLevel);
        if (Math.abs(targetZoomLevel - zoomLevel) < 0.01) {
            zoomLevel = targetZoomLevel;
            zoomTimer.stop();
            zoomTimer = null;
        }
        updateNodeScaling();
        repaint();
    }

    public static MapNode getNodeAtWorldPosition(double worldPosX, double worldPosZ) {
        MapNode selected = null;

        if (roadMap != null) {
            // make sure we prioritize returning control nodes over regular nodes
            for (MapNode mapNode : RoadMap.networkNodesList) {
                if (worldPosX < mapNode.x + nodeSizeWorld && worldPosX > mapNode.x - nodeSizeWorld && worldPosZ < mapNode.z + nodeSizeWorld && worldPosZ > mapNode.z - nodeSizeWorld) {
                    selected = mapNode;
                    break;
                }
            }

            if (quadCurve != null && isQuadCurveCreated) {
                MapNode cpNode = quadCurve.getControlPoint();
                if (worldPosX < cpNode.x + nodeSizeWorld && worldPosX > cpNode.x - nodeSizeWorld && worldPosZ < cpNode.z + nodeSizeWorld && worldPosZ > cpNode.z - nodeSizeWorld) {
                    return quadCurve.getControlPoint();
                }
            }
            if (cubicCurve != null && isCubicCurveCreated) {
                MapNode cp1Node = cubicCurve.getControlPoint1();
                if (worldPosX < cp1Node.x + nodeSizeWorld && worldPosX > cp1Node.x - nodeSizeWorld && worldPosZ < cp1Node.z + nodeSizeWorld && worldPosZ > cp1Node.z - nodeSizeWorld) {
                    return cubicCurve.getControlPoint1();
                }

                MapNode cp2Node = cubicCurve.getControlPoint2();
                if (worldPosX < cp2Node.x + nodeSizeWorld && worldPosX > cp2Node.x - nodeSizeWorld && worldPosZ < cp2Node.z + nodeSizeWorld && worldPosZ > cp2Node.z - nodeSizeWorld) {
                    return cubicCurve.getControlPoint2();
                }
            }
			//quarticbezier
			if (quarticCurve != null && isQuarticCurveCreated) {
				MapNode cp1Node = quarticCurve.getControlPoint1();
				if (worldPosX < cp1Node.x + nodeSizeWorld && worldPosX > cp1Node.x - nodeSizeWorld &&
					worldPosZ < cp1Node.z + nodeSizeWorld && worldPosZ > cp1Node.z - nodeSizeWorld) {
					return quarticCurve.getControlPoint1();
				}

				MapNode cp2Node = quarticCurve.getControlPoint2();
				if (worldPosX < cp2Node.x + nodeSizeWorld && worldPosX > cp2Node.x - nodeSizeWorld &&
					worldPosZ < cp2Node.z + nodeSizeWorld && worldPosZ > cp2Node.z - nodeSizeWorld) {
					return quarticCurve.getControlPoint2();
				}
				
				MapNode cp3Node = quarticCurve.getControlPoint3();
				if (worldPosX < cp3Node.x + nodeSizeWorld && worldPosX > cp3Node.x - nodeSizeWorld &&
					worldPosZ < cp3Node.z + nodeSizeWorld && worldPosZ > cp3Node.z - nodeSizeWorld) {
					return quarticCurve.getControlPoint3();
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

            for (MapNode mapNode : RoadMap.networkNodesList) {
                outPos = worldPosToScreenPos(mapNode.x, mapNode.z);
                if (mousePosX < outPos.getX() + nodeSizeScaledHalf && mousePosX > outPos.getX() - nodeSizeScaledHalf && mousePosY < outPos.getY() + nodeSizeScaledHalf && mousePosY > outPos.getY() - nodeSizeScaledHalf) {
                    // TODO Fix node visibility check
                    /*if (mapNode.isNodeVisible())*/
                    selected = mapNode;
                    break;
                }
            }

            if (quadCurve != null && isQuadCurveCreated) {
                Point2D cpPosition = worldPosToScreenPos(quadCurve.getControlPoint().x, quadCurve.getControlPoint().z);
                if (mousePosX < cpPosition.getX() + nodeSizeScaledHalf && mousePosX > cpPosition.getX() - nodeSizeScaledHalf && mousePosY < cpPosition.getY() + nodeSizeScaledHalf && mousePosY > cpPosition.getY() - nodeSizeScaledHalf) {
                    return quadCurve.getControlPoint();
                }
            }

            if (cubicCurve != null && isCubicCurveCreated) {
                Point2D cp1Position = worldPosToScreenPos(cubicCurve.getControlPoint1().x, cubicCurve.getControlPoint1().z);
                if (mousePosX < cp1Position.getX() + nodeSizeScaledHalf && mousePosX > cp1Position.getX() - nodeSizeScaledHalf && mousePosY < cp1Position.getY() + nodeSizeScaledHalf && mousePosY > cp1Position.getY() - nodeSizeScaledHalf) {
                    return cubicCurve.getControlPoint1();
                }

                Point2D cp2Position = worldPosToScreenPos(cubicCurve.getControlPoint2().x, cubicCurve.getControlPoint2().z);
                if (mousePosX < cp2Position.getX() + nodeSizeScaledHalf && mousePosX > cp2Position.getX() - nodeSizeScaledHalf && mousePosY < cp2Position.getY() + nodeSizeScaledHalf && mousePosY > cp2Position.getY() - nodeSizeScaledHalf) {
                    return cubicCurve.getControlPoint2();
                }
            }
			//quarticbezier
			if (quarticCurve != null && isQuarticCurveCreated) {
				Point2D cp1Position = worldPosToScreenPos(quarticCurve.getControlPoint1().x, quarticCurve.getControlPoint1().z);
				if (mousePosX < cp1Position.getX() + nodeSizeScaledHalf && mousePosX > cp1Position.getX() - nodeSizeScaledHalf &&
					mousePosY < cp1Position.getY() + nodeSizeScaledHalf && mousePosY > cp1Position.getY() - nodeSizeScaledHalf) {
					return quarticCurve.getControlPoint1();
				}

				Point2D cp2Position = worldPosToScreenPos(quarticCurve.getControlPoint2().x, quarticCurve.getControlPoint2().z);
				if (mousePosX < cp2Position.getX() + nodeSizeScaledHalf && mousePosX > cp2Position.getX() - nodeSizeScaledHalf &&
					mousePosY < cp2Position.getY() + nodeSizeScaledHalf && mousePosY > cp2Position.getY() - nodeSizeScaledHalf) {
					return quarticCurve.getControlPoint2();
				}
				
				Point2D cp3Position = worldPosToScreenPos(quarticCurve.getControlPoint3().x, quarticCurve.getControlPoint3().z);
				if (mousePosX < cp3Position.getX() + nodeSizeScaledHalf && mousePosX > cp3Position.getX() - nodeSizeScaledHalf &&
					mousePosY < cp3Position.getY() + nodeSizeScaledHalf && mousePosY > cp3Position.getY() - nodeSizeScaledHalf) {
					return quarticCurve.getControlPoint3();
				}
			}

            if (rotation != null && Objects.equals(buttonManager.getCurrentButtonID(),"RotateButton")) {
                Point2D rotatePosition = worldPosToScreenPos(rotation.getControlNode().x, rotation.getControlNode().z);
                if (mousePosX < rotatePosition.getX() + nodeSizeScaledQuarter && mousePosX > rotatePosition.getX() - nodeSizeScaledQuarter && mousePosY < rotatePosition.getY() + nodeSizeScaledQuarter && mousePosY > rotatePosition.getY() - nodeSizeScaledQuarter) {
                    return rotation.getControlNode();
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

        double topLeftX = (x * pdaImage.getWidth()) - ((getMapPanel().getWidth() / zoomLevel)/2);
        double topLeftY = (y * pdaImage.getHeight()) - ((getMapPanel().getHeight() / zoomLevel)/2);

        double diffScaledX = (double)screenX / zoomLevel;
        double diffScaledY = (double)screenY / zoomLevel;

        int centerPointOffsetX = (pdaImage.getWidth() / 2) * mapScale;
        int centerPointOffsetY = (pdaImage.getHeight() / 2) * mapScale;

        double worldPosX = limitDoubleToDecimalPlaces(((topLeftX + diffScaledX) * mapScale) - centerPointOffsetX, 3, RoundingMode.HALF_UP);
        double worldPosY = limitDoubleToDecimalPlaces(((topLeftY + diffScaledY) * mapScale) - centerPointOffsetY, 3, RoundingMode.HALF_UP);

        return new Point2D.Double(worldPosX, worldPosY);
    }

    public static Point worldPosToScreenPos(double worldX, double worldZ) {

        int centerPointOffset = 1024 * mapScale;

        worldX += centerPointOffset;
        worldZ += centerPointOffset;

        double scaledX = (worldX/ mapScale) * zoomLevel;
        double scaledY = (worldZ/ mapScale) * zoomLevel;

        double centerXScaled = (x * (pdaImage.getWidth()*zoomLevel));
        double centerYScaled = (y * (pdaImage.getHeight()*zoomLevel));

        double topLeftX = centerXScaled - (getMapPanel().getWidth() / 2F);
        double topLeftY = centerYScaled - (getMapPanel().getHeight()/ 2F);

        return new Point((int) (scaledX - topLeftX), (int) (scaledY - topLeftY));
    }

    // TODO: Re-do this function completely
    //       Changes:-
    //       (1) Change the behaviour to examine the current connection and adjust
    //           the incoming/outgoing to accomplish the desired connection type
    //       WARNING:-
    //       EXTREME care needs to be taken, one missed mistake or edge case can cause
    //       this to function to corrupt a config beyond repair!!.

    public static void createConnectionBetween(MapNode start, MapNode target, int type) {

        if (start == target) return;

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

    /*public static void createConnectionBetween(MapNode start, MapNode target, int type) {
        if (start == null || target == null || start == target) {
            // Invalid nodes or self-connection, do nothing
            return;
        }

        if (type == CONNECTION_UNKNOWN) {
            // Invalid connection type, do nothing
            return;
        }

        // Store the original connections
        ArrayList<MapNode> startOutgoingCopy = new ArrayList<>(start.outgoing);
        ArrayList<MapNode> startIncomingCopy = new ArrayList<>(start.incoming);
        ArrayList<MapNode> targetOutgoingCopy = new ArrayList<>(target.outgoing);
        ArrayList<MapNode> targetIncomingCopy = new ArrayList<>(target.incoming);

        // Make adjustments to the stored versions based on the connection type
        adjustConnections(startOutgoingCopy, startIncomingCopy, targetOutgoingCopy, targetIncomingCopy, start, target, type);

        // Check for validity before applying changes to the nodes
        if (isValidConnection(start, target, type)) {
            // Apply the changes to the actual nodes
            start.outgoing = new ArrayList<>(startOutgoingCopy);
            start.incoming = new ArrayList<>(startIncomingCopy);
            target.outgoing = new ArrayList<>(targetOutgoingCopy);
            target.incoming = new ArrayList<>(targetIncomingCopy);
        }
    }

    private static void adjustConnections(
            ArrayList<MapNode> startOutgoingCopy, ArrayList<MapNode> startIncomingCopy,
            ArrayList<MapNode> targetOutgoingCopy, ArrayList<MapNode> targetIncomingCopy,
            MapNode start, MapNode target, int type) {
        // Make adjustments to the stored versions based on the connection type
        switch (type) {
            case CONNECTION_STANDARD:
                startOutgoingCopy.add(target);
                targetIncomingCopy.add(start);
                break;
            case CONNECTION_REVERSE:
                startOutgoingCopy.remove(target);
                targetIncomingCopy.remove(start);
                break;
            case CONNECTION_DUAL:
                adjustConnections(startOutgoingCopy, startIncomingCopy, targetOutgoingCopy, targetIncomingCopy, start, target, CONNECTION_STANDARD);
                adjustConnections(startOutgoingCopy, startIncomingCopy, targetOutgoingCopy, targetIncomingCopy, target, start, CONNECTION_STANDARD);
                break;
            default:
                // Invalid connection type, do nothing
                break;
        }
    }

    private static boolean isValidConnection(MapNode start, MapNode target, int type) {
        switch (type) {
            case CONNECTION_STANDARD:
                return !start.outgoing.contains(target) && !target.incoming.contains(start);
            case CONNECTION_REVERSE:
                return !start.outgoing.contains(target) && !start.incoming.contains(target)
                        && !target.outgoing.contains(start) && !target.incoming.contains(start);
            case CONNECTION_DUAL:
                return isValidConnection(start, target, CONNECTION_STANDARD)
                        && isValidConnection(target, start, CONNECTION_STANDARD);
            default:
                return false;
        }
    }*/

    //
    // Mouse movement and drag detection
    //

    public void mouseMoved(int mousePosX, int mousePosY) {
        if (pdaImage != null) {
            if (bDebugShowHeightMapInfo) {
                if (heightMapImage != null) {
                    double x, y;
                    Point2D point = screenPosToWorldPos(mousePosX, mousePosY);

                    double scaleX = (double) heightMapImage.getWidth() / pdaImage.getWidth();
                    double scaleY = (double) heightMapImage.getHeight() / pdaImage.getHeight();
                    if (bDebugLogHeightMapInfo) LOG.info("heightmap scale = {} , {}", scaleX, scaleY);

                    x = (int) (((point.getX() / mapScale) + (pdaImage.getWidth() / 2)) * scaleX);
                    y = (int) (((point.getY() / mapScale) + (pdaImage.getHeight() / 2)) * scaleY);

                    if (bDebugLogHeightMapInfo) {
                        LOG.info("mapScale {}", mapScale);
                        LOG.info("HeightMap size {},{}", heightMapImage.getWidth(), heightMapImage.getHeight());
                    }

                    if (bDebugLogHeightMapInfo) LOG.info(" - mapZoomFactor {} - halfWidth {} , halfHeight {} :: halfPointX {} , halfPointY {}", mapScale, heightMapImage.getWidth() / 2, heightMapImage.getHeight() / 2, (point.getX() / mapScale), (point.getY() / mapScale));
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
        if (isDraggingMap) moveMapBy(mousePosX - prevMousePosX, mousePosY - prevMousePosY);
        if (isMultiSelectDragging) {
            if (useRectangularSelection) {
                if (mousePosX > this.getWidth()) moveMapBy( -10, 0);
                if (mousePosX < 0) moveMapBy( 10, 0);
                if (mousePosY > this.getHeight()) moveMapBy( 0, -10);
                if (mousePosY < 0) moveMapBy( 0, 10);
            }
        }
    }

    //
    // Left mouse button click/pressed/released states
    //

    @SuppressWarnings("EmptyMethod")
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

    @SuppressWarnings("EmptyMethod")
    public void mouseButton2Clicked(int ignoredMousePosX, int ignoredMousePosY) {}

    public void mouseButton2Pressed(int ignoredMousePosX, int ignoredMousePosY) {
        if (bMiddleMouseMove) isDraggingMap = true;
    }

    public void mouseButton2Released() {
        if (bMiddleMouseMove) isDraggingMap = false;
    }

    //
    // Right mouse button click/pressed/released states
    //

    @SuppressWarnings("EmptyMethod")
    public void mouseButton3Clicked(int ignoredMousePosX, int ignoredMousePosY) {}

    @SuppressWarnings("EmptyMethod")
    public void mouseButton3Pressed(int ignoredMousePosX, int ignoredMousePosY) {}

    @SuppressWarnings("EmptyMethod")
    public void mouseButton3Released(int ignoredMousePosX, int ignoredMousePosY) {}

    public static void updateNodeScaling() {
        if (pdaImage != null) {
            nodeSizeScaled = ((nodeSize * zoomLevel) *0.5);
            nodeSizeScaledHalf = nodeSizeScaled * 0.5;
            nodeSizeScaledQuarter = nodeSizeScaled * 0.25;
            Point2D nodeLeftEdge = screenPosToWorldPos((int) (100 - nodeSizeScaledQuarter), 0);
            Point2D nodeRightEdge = screenPosToWorldPos((int) (100 + nodeSizeScaledQuarter), 0);
            nodeSizeWorld = nodeRightEdge.getX() - nodeLeftEdge.getX();

            // update the cached Node image for the render thread

            // TODO (1) Create subprio bufferImage
            // TODO (2) Link to colour changing in preferences

            if (nodeSizeScaled > 1)  {
                updateCachedNodeImages();
            }
        }
    }

    public static void updateCachedNodeImages() {
        cachedRegularNodeImage = getNewBufferImage((int) nodeSizeScaled, (int) nodeSizeScaled, Transparency.BITMASK);
        Graphics2D g = (Graphics2D) cachedRegularNodeImage.getGraphics();
        g.setColor(colourNodeRegular);
        g.fillArc( 0, 0, (int) nodeSizeScaled, (int) nodeSizeScaled, 0, 360);
        g.dispose();

        cachedSubprioNodeImage = getNewBufferImage((int) nodeSizeScaled, (int) nodeSizeScaled, Transparency.BITMASK);
        Graphics2D g1 = (Graphics2D) cachedSubprioNodeImage.getGraphics();
        g1.setColor(colourNodeSubprio);
        g1.fillArc( 0, 0, (int) nodeSizeScaled, (int) nodeSizeScaled, 0, 360);
        g1.dispose();
    }

    public void centreNodeInMapPanel(MapNode node) {
        Point2D target = worldPosToScreenPos(node.x, node.z);
        double x = (this.getWidth() >> 1) - target.getX();
        double y = (this.getHeight() >> 1) - target.getY();
        moveMapBy((int) x, (int) y);
    }

    public static void forceMapImageRedraw() {
        if (pdaImage != null) {
            oldWidthScaled = 0;
            oldHeightScaled = 0;
            int widthScaled = (int) (getMapPanel().getWidth() / zoomLevel);
            int heightScaled = (int) (getMapPanel().getHeight() / zoomLevel);
            if (widthScaled < 0 || widthScaled> pdaImage.getWidth() || heightScaled < 0 || heightScaled > pdaImage.getHeight()) {
                zoomLevel = Math.max((double)getMapPanel().getWidth()/pdaImage.getWidth(),(double)getMapPanel().getHeight()/pdaImage.getHeight());
            }
            updateNodeScaling();
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

    public static boolean isStale() { return stale; }

    //
    // getters
    //

   public RoadMap getRoadMap() { return roadMap; }

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

    //
    // Testing only
    //

    @SuppressWarnings("unused")
    public static boolean checkConfigForSequenceErrors() {
        // check if each MapNode has the correct ID set
        int id = 1;
        try {
            for (MapNode networkNode : RoadMap.networkNodesList) {
                if (networkNode.id != id) {
                    id++;
                } else {
                    throw new ExceptionUtils.SequenceException("RoadMap index " + (id-1) + " does not match expected value ("+ id + ")");
                }
            }
            return true;
        } catch (ExceptionUtils.SequenceException e) {
            LOG.info("Failed {}", e.getMessage());
            e.printStackTrace();
            return false;
        }
    }
}
