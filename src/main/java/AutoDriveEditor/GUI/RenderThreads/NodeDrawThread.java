package AutoDriveEditor.GUI.RenderThreads;

import AutoDriveEditor.Managers.CopyPasteManager;
import AutoDriveEditor.RoadNetwork.MapNode;
import AutoDriveEditor.RoadNetwork.RoadMap;
import AutoDriveEditor.Utils.ProfileUtil;

import java.awt.*;
import java.awt.geom.Path2D;
import java.awt.geom.Point2D;
import java.awt.geom.Rectangle2D;
import java.awt.image.BufferedImage;
import java.util.ArrayList;

import static AutoDriveEditor.AutoDriveEditor.buttonManager;
import static AutoDriveEditor.AutoDriveEditor.getMapPanel;
import static AutoDriveEditor.Classes.MapImage.pdaImage;
import static AutoDriveEditor.GUI.EditorImages.*;
import static AutoDriveEditor.GUI.MapPanel.*;
import static AutoDriveEditor.GUI.Menus.DebugMenu.ShowAllNodeIDMenu.bDebugShowAllNodeID;
import static AutoDriveEditor.GUI.Menus.DebugMenu.ShowNodeHeightMenu.bDebugShowHeight;
import static AutoDriveEditor.GUI.Menus.DebugMenu.ShowProfileInfo.bDebugShowProfileInfo;
import static AutoDriveEditor.Managers.CopyPasteManager.SCREEN_COORDINATES;
import static AutoDriveEditor.Managers.CopyPasteManager.getSelectionBounds;
import static AutoDriveEditor.Managers.MultiSelectManager.*;
import static AutoDriveEditor.RoadNetwork.MapNode.NODE_FLAG_REGULAR;
import static AutoDriveEditor.RoadNetwork.MapNode.NODE_WARNING_OVERLAP;
import static AutoDriveEditor.Utils.LoggerUtils.LOG;
import static AutoDriveEditor.XMLConfig.EditorXML.*;

public class NodeDrawThread implements Runnable {

    // The NodeDraw thread is 2 or 3 times quicker to execute than the connectionDraw() thread,
    // so we try and spread the draw load around by doing the curve/line/rectangle drawing here

    public static final ProfileUtil nodeComputeTimer = new ProfileUtil();
    public static final ProfileUtil nodeDrawTimer = new ProfileUtil();
    public static final ProfileUtil imageDrawTimer = new ProfileUtil();
    public static final ProfileUtil textDrawTimer = new ProfileUtil();
    public static final ProfileUtil buttonDrawTimer = new ProfileUtil();
    public static final ProfileUtil rectangleDrawTimer = new ProfileUtil();

    public static int nodeComputeTotal;

    private static volatile boolean isStopped = false;

    public static void stop() {
        LOG.info("Stopping NodeDraw thread");
        isStopped = true;
    }

    @Override
    public synchronized void run() {

        LOG.info("Starting NodeDraw thread");
        ArrayList<NodeDisplayList> nodeDrawArray = new ArrayList<>();
        ArrayList<ImageDisplayList> imageDrawArray = new ArrayList<>();
        ArrayList<TextDisplayStore> textDrawArray = new ArrayList<>();
        ArrayList<RectangleDisplayList> rectangleDrawArray = new ArrayList<>();

        while ( !isStopped ) {

            try {
                nodeDrawArray.clear();
                imageDrawArray.clear();
                textDrawArray.clear();
                rectangleDrawArray.clear();

                this.wait();
            } catch (InterruptedException e) {

                if (isStopped) {
                    LOG.info("NodeDraw Thread exiting");
                    return;
                }

                if (bDebugShowProfileInfo) {
                    nodeComputeTimer.resetTimer();
                    nodeDrawTimer.resetTimer();
                    imageDrawTimer.resetTimer();
                    textDrawTimer.resetTimer();
                    buttonDrawTimer.resetTimer();
                    rectangleDrawTimer.resetTimer();
                    nodeComputeTimer.startTimer();
                }

                int width = getMapPanel().getWidth();
                int height = getMapPanel().getHeight();

                if (renderGraphics != null && pdaImage != null) {

                    //
                    // Draw all visible nodes in screen area
                    //

                    boolean select;
                    boolean vis;
                    int flag;

                    for (MapNode mapNode : RoadMap.networkNodesList) {
                        Point nodePos = worldPosToScreenPos(mapNode.x, mapNode.z);
                        if (0 < nodePos.getX() && width > nodePos.getX() && 0 < nodePos.getY() && height > nodePos.getY()) {
                            if (nodeSizeScaled >= 2.0) {
                                if (mapNode != hoveredNode) {
                                    vis = mapNode.isNodeHidden() == mapNode.getPreviewNodeHiddenChange();
                                    select = mapNode.isSelected() != mapNode.getPreviewNodeSelectionChange();
                                    flag = (mapNode.getPreviewNodeFlagChange())? 1 - mapNode.flag : mapNode.flag;
                                    nodeDrawArray.add(new NodeDisplayList((int) nodePos.getX(), (int) nodePos.getY(), flag, select, vis));
                                }
                            }

                            if (bDebugShowAllNodeID) {
                                textDrawArray.add(new TextDisplayStore(String.valueOf(mapNode.id), nodePos, Color.WHITE, false, null, false, 1.0f));
                            }

                            if (bDebugShowHeight) {
                                Point2D newPoint = new Point2D.Double(nodePos.getX(), nodePos.getY() + 25);
                                textDrawArray.add(new TextDisplayStore(String.valueOf(mapNode.y), newPoint, Color.WHITE, false, null, false, 1.0f));
                            }
                        }


                        if (mapNode.hasWarning()) {
                            if (mapNode.getWarningType() == NODE_WARNING_OVERLAP) {
                                imageDrawArray.add(new ImageDisplayList(getOverlapWarningImage(), (int) nodePos.getX(), (int) nodePos.getY()));
                            }
                        } else {
                            if (mapNode.y == -1) {
                                imageDrawArray.add(new ImageDisplayList(getNegativeHeightWarningImage(), (int) nodePos.getX(), (int) nodePos.getY()));
                            }
                        }

                        if (mapNode.isParkDestination() && bShowParkingIcons) {
                            imageDrawArray.add(new ImageDisplayList(getParkingImage(), (int) nodePos.getX(), (int) nodePos.getY()));
                        }

                        // show the node ID if we in debug mode, the higher the node count, the more text spam there is :-P
                        // It will affect editor speed, the more nodes the worse it will get, you have been warned :)

                        if (mapNode.hasMapMarker()) {
                            if (mapNode.getMarkerName() != null) {
                                Point2D nodeScreenPos = worldPosToScreenPos(mapNode.x, mapNode.z - 1 );
                                String markerText = mapNode.getMarkerName();
                                if (hoveredNode == mapNode || bShowMarkerNames) {
                                    if (mapNode == hoveredNode) markerText += " ( " + mapNode.getMarkerGroup() + " )";
                                    textDrawArray.add(new TextDisplayStore(markerText, nodeScreenPos, Color.WHITE, false, null, false , 1.0f));
                                }
                                if (bShowMarkerIcons) {
                                    imageDrawArray.add(new ImageDisplayList(getMarkerImage(), (int) nodeScreenPos.getX(), (int) nodeScreenPos.getY() - 20));
                                }
                            }
                        }
                    }

                    // do we draw the node hover-over image and add the marker name/group to the drawToScreen list

                    if (hoveredNode != null ) {
                        if (!hoveredNode.isControlNode()) {
                                Point2D hoverNodePos = worldPosToScreenPos(hoveredNode.x, hoveredNode.z);
                                nodeDrawArray.add(new NodeDisplayList((int) hoverNodePos.getX(), (int) hoverNodePos.getY(), hoveredNode.flag, hoveredNode.isSelectable(), !hoveredNode.isNodeHidden()));
                        }
                    }
                }

                if (isMultiSelectDragging) {
                    if (useRectangularSelection) {
                        Point2D rectScreenEnd = worldPosToScreenPos(multiSelectRect.getX() + multiSelectRect.getWidth(), multiSelectRect.getY() + multiSelectRect.getHeight());
                        Point2D rectScreenStart = worldPosToScreenPos(multiSelectRect.getX(), multiSelectRect.getY());
                        rectangleDrawArray.add(new RectangleDisplayList((int) rectScreenStart.getX(), (int) rectScreenStart.getY(), (int) (rectScreenEnd.getX() - rectScreenStart.getX()), (int) (rectScreenEnd.getY() - rectScreenStart.getY()), Color.WHITE, true));
                    } else {
                        Path2D freeformPath = getFreeformSelectionPath();
                        Graphics2D newG = (Graphics2D) renderGraphics;
                        newG.setColor(Color.WHITE);
                        newG.draw(freeformPath);
                        newG.setComposite(AlphaComposite.SrcOver.derive(0.25f));
                        newG.drawLine((int) freeformPath.getCurrentPoint().getX(), (int) freeformPath.getCurrentPoint().getY(), (int) freeformSelectionStart.getX(), (int) freeformSelectionStart.getY());
                        newG.setComposite(AlphaComposite.SrcOver.derive(1f));
                    }
                }

                if (isMultipleSelected && bShowSelectionBounds) {
                    CopyPasteManager.selectionAreaInfo selectionInfo = getSelectionBounds(multiSelectList);
                    Graphics2D gTemp = (Graphics2D) renderGraphics.create();
                    gTemp.setColor(Color.WHITE);
                    Point2D topLeft = selectionInfo.getSelectionStart(SCREEN_COORDINATES);
                    Point2D bottomRight = selectionInfo.getSelectionEnd(SCREEN_COORDINATES);
                    double rectSizeX = bottomRight.getX() - topLeft.getX();
                    double rectSizeY = bottomRight.getY() - topLeft.getY();
                    rectangleDrawArray.add(new RectangleDisplayList((int) (topLeft.getX() - nodeSizeScaledQuarter), (int) (topLeft.getY() - nodeSizeScaledQuarter), (int) (rectSizeX + (nodeSizeScaledQuarter * 2)), (int) (rectSizeY + (nodeSizeScaledQuarter * 2)), Color.WHITE, false));
                }

                if (bDebugShowProfileInfo) nodeComputeTimer.stopTimer();
                try {
                    drawOrderLatch.await();
                    drawLock.lock();
                    if (bDebugShowProfileInfo) nodeDrawTimer.startTimer();
                    if (nodeDrawArray.size() > 0) drawNodes(renderGraphics, nodeDrawArray);
                    if (bDebugShowProfileInfo) nodeDrawTimer.stopTimer();
                    if (bDebugShowProfileInfo) imageDrawTimer.startTimer();
                    if (imageDrawArray.size() > 0) drawImages(renderGraphics, imageDrawArray);
                    if (bDebugShowProfileInfo) imageDrawTimer.stopTimer();
                    if (bDebugShowProfileInfo) textDrawTimer.startTimer();
                    if (textDrawArray.size() > 0) drawText(renderGraphics, textDrawArray);
                    if (bDebugShowProfileInfo) textDrawTimer.stopTimer();
                    if (bDebugShowProfileInfo) buttonDrawTimer.startTimer();
                    buttonManager.drawToScreen(renderGraphics);
                    if (bDebugShowProfileInfo) buttonDrawTimer.stopTimer();
                    if (bDebugShowProfileInfo) rectangleDrawTimer.startTimer();
                    if (rectangleDrawArray.size() > 0) drawRectangles(renderGraphics, rectangleDrawArray);
                    if (bDebugShowProfileInfo) rectangleDrawTimer.stopTimer();
                } catch (InterruptedException ex) {
                    ex.printStackTrace();
                    throw new RuntimeException(ex);
                } finally {
                    drawLock.unlock();
                }

                if (bDebugShowProfileInfo) {
                    nodeComputeTotal = nodeDrawArray.size();
                }
                threadCountLatch.countDown();
            }
        }
    }

    private void drawNodes(Graphics g,  ArrayList<NodeDisplayList> nodeList) {
        if (nodeSizeScaled >= MIN_VISIBLE_NODE_SIZE) {
            Graphics2D gTrans = (Graphics2D) g.create();
            BasicStroke selectedStroke = new BasicStroke((float) (nodeSizeScaledQuarter * 0.8));

            Graphics2D gSelected = (Graphics2D) g.create();
            gSelected.setColor(colourNodeSelected);
            gSelected.setStroke(selectedStroke);

            Composite visible = AlphaComposite.SrcOver.derive(1f);
            Composite hidden = AlphaComposite.SrcOver.derive(hiddenNodesTransparencyLevel);

            for (NodeDisplayList node : nodeList) {
                if (node.isVisible) {
                    gTrans.setComposite(visible);
                } else {
                    gTrans.setComposite(hidden);
                }

                if (node.flag == NODE_FLAG_REGULAR) {
                    gTrans.drawImage(cachedRegularNodeImage, (int) (node.x - cachedRegularNodeImage.getWidth()/2), (int) (node.y - cachedRegularNodeImage.getHeight()/2), (int) nodeSizeScaled, (int) nodeSizeScaled, null);
                } else {
                    gTrans.drawImage(cachedSubprioNodeImage, (int) (node.x - cachedRegularNodeImage.getWidth()/2), (int) (node.y - cachedRegularNodeImage.getHeight()/2), (int) nodeSizeScaled, (int) nodeSizeScaled, null);

                }
                if (node.isSelected) {
                    if (node.isVisible) {
                        gSelected.setComposite(visible);
                    } else {
                        gSelected.setComposite(hidden);
                    }
                    gSelected.drawArc((int) (node.x - (nodeSizeScaledHalf * 0.8)), (int) (node.y - (nodeSizeScaledHalf * 0.8)), (int) (nodeSizeScaled - (nodeSizeScaledQuarter * 0.8)), (int) (nodeSizeScaled - (nodeSizeScaledQuarter * 0.8)), 0, 360);
                }
            }
            gTrans.dispose();
        }
    }

    private void drawImages(Graphics g, ArrayList<ImageDisplayList> imageList) {
        int imageWidth;
        int imageHeight;
        for (ImageDisplayList list : imageList)  {
            imageWidth = list.image.getWidth();
            imageHeight = list.image.getHeight();
            g.drawImage(list.image, (list.x - (imageWidth / 2)), (list.y - (imageHeight / 2)), imageWidth, imageHeight, null);
        }
    }

    private void drawText(Graphics g, ArrayList<TextDisplayStore> textList) {
        FontMetrics fm = g.getFontMetrics();
        for (TextDisplayStore list : textList) {
            Rectangle2D rect = fm.getStringBounds(list.text, renderGraphics);
            if (list.useBackground) {
                Graphics2D gText = (Graphics2D) g.create();
                gText.setComposite(AlphaComposite.SrcOver.derive(list.transparencyLevel));
                gText.setColor(list.bgColour);
                gText.fillRect((int)list.position.getX(),
                        (int) list.position.getY() - fm.getAscent(),
                        (int) rect.getWidth(),
                        (int) rect.getHeight() + 2);
                gText.setComposite(AlphaComposite.SrcOver.derive(1.0f));
                gText.dispose();
            }
            g.setColor(list.colour);
            g.drawString(list.text, (int) (list.position.getX() - (rect.getWidth() / 2)), (int) (list.position.getY() + (( rect.getHeight() / 2) - 3)));
        }
    }

    private void drawRectangles(Graphics g, ArrayList<RectangleDisplayList> rectangleList) {
        Graphics2D gRect = (Graphics2D) g.create();
        BasicStroke stDash = new BasicStroke(1, BasicStroke.CAP_BUTT,
                BasicStroke.JOIN_ROUND, 1.0f, new float[]{10f, 0f, 2f}, 2f);
        gRect.setStroke(stDash);

        for (RectangleDisplayList rec : rectangleList) {
            if (rec.hasStroke) {
                gRect.setColor(rec.colour);
                gRect.drawRect(rec.rectangle.x, rec.rectangle.y, rec.rectangle.width, rec.rectangle.height);
            } else {
                g.setColor(rec.colour);
                g.drawRect(rec.rectangle.x, rec.rectangle.y, rec.rectangle.width, rec.rectangle.height);
            }
        }
        gRect.dispose();
    }

    private static class NodeDisplayList {
        final double x;
        final double y;
        final int flag;
        final boolean isSelected;
        final boolean isVisible;

        public NodeDisplayList(int x, int y, int flag, boolean isSelected, boolean isVisible) {
            this.x = x;
            this.y = y;
            this.flag = flag;
            this.isSelected = isSelected;
            this.isVisible = isVisible;
        }
    }

    private static class ImageDisplayList {
        final BufferedImage image;
        final int x;
        final int y;

        public ImageDisplayList(BufferedImage image, int x, int y) {
            this.image = image;
            this.x = x;
            this.y = y;
        }
    }

    private static class TextDisplayStore {
        final String text;
        final Point2D position;
        final Color colour;
        final boolean useBackground;
        final Color bgColour;
        final boolean bgTransparency;
        final float transparencyLevel;

        public TextDisplayStore(String text, Point2D textPos, Color textColour, boolean background, Color bgColour, boolean bgTransparency, float transparencyLevel) {
            this.text = text;
            this.position = textPos;
            this.colour = textColour;
            this.useBackground = background;
            this.bgColour = bgColour;
            this.bgTransparency = bgTransparency;
            this.transparencyLevel = transparencyLevel;
        }
    }

    private static class RectangleDisplayList {

        final Rectangle rectangle;
        final Color colour;
        final Boolean hasStroke;

        public RectangleDisplayList(int x, int y, int width, int height, Color colour, boolean hasStroke) {
            this.rectangle = new Rectangle(x, y, width, height);
            this.colour = colour;
            this.hasStroke = hasStroke;
        }
    }

}
