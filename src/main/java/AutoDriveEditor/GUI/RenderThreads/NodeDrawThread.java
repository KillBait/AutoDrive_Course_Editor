package AutoDriveEditor.GUI.RenderThreads;

import AutoDriveEditor.Classes.Util_Classes.ProfileUtil;
import AutoDriveEditor.RoadNetwork.MapNode;

import java.awt.*;
import java.awt.geom.Point2D;
import java.awt.geom.Rectangle2D;
import java.awt.image.BufferedImage;
import java.util.ArrayList;

import static AutoDriveEditor.AutoDriveEditor.buttonManager;
import static AutoDriveEditor.AutoDriveEditor.getMapPanel;
import static AutoDriveEditor.Classes.Util_Classes.LoggerUtils.LOG;
import static AutoDriveEditor.GUI.MapImage.pdaImage;
import static AutoDriveEditor.GUI.MapPanel.*;
import static AutoDriveEditor.GUI.Menus.DebugMenu.ShowAllNodeIDMenu.bDebugShowAllNodeID;
import static AutoDriveEditor.GUI.Menus.DebugMenu.ShowNodeHeightMenu.bDebugShowHeight;
import static AutoDriveEditor.GUI.Menus.DebugMenu.ShowRenderProfileInfo.bDebugShowRenderProfileInfo;
import static AutoDriveEditor.RoadNetwork.MapNode.NODE_FLAG_REGULAR;
import static AutoDriveEditor.RoadNetwork.MapNode.NodeWarning.NODE_WARNING_OVERLAP;
import static AutoDriveEditor.XMLConfig.EditorXML.*;

public class NodeDrawThread implements Runnable {

    // The NodeDraw thread is 2 or 3 times quicker to execute than the connectionDraw() thread,
    // so we try and spread the draw load around by doing the curve/line/rectangle drawing here

    public static final ProfileUtil nodeComputeTimer = new ProfileUtil();
    public static final ProfileUtil nodeDrawTimer = new ProfileUtil();
    public static final ProfileUtil imageDrawTimer = new ProfileUtil();
    public static final ProfileUtil textDrawTimer = new ProfileUtil();

    public static int nodeComputeTotal;

    private static volatile boolean isStopped = false;

    private static ArrayList<MapNode> visibleNodes;

    public static void setVisibleNodes(ArrayList<MapNode> nodes) {
        visibleNodes = nodes;
    }


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

        while ( !isStopped ) {

            try {
                nodeDrawArray.clear();
                imageDrawArray.clear();
                textDrawArray.clear();
                this.wait();
            } catch (InterruptedException e) {

                if (isStopped) {
                    LOG.info("NodeDraw Thread exiting");
                    return;
                }

                if (bDebugShowRenderProfileInfo) {
                    nodeComputeTimer.resetTimer();
                    nodeDrawTimer.resetTimer();
                    imageDrawTimer.resetTimer();
                    textDrawTimer.resetTimer();
                    nodeComputeTimer.startTimer();
                }

                if (renderGraphics != null && pdaImage != null) {

                    //
                    // Draw all visible nodes in screen area
                    //

                    boolean select;
                    boolean vis;
                    int flag;

                    for (MapNode mapNode : visibleNodes) {
                        Point nodePos = worldPosToScreenPos(mapNode.x, mapNode.z);
                        if (nodeSizeScaled >= 2.0) {
                            if (mapNode != getMapPanel().hoveredNode || !mapNode.isSpecialNode()) {
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
                                if (getMapPanel().hoveredNode == mapNode || bShowMarkerNames) {
                                    if (mapNode == getMapPanel().hoveredNode) markerText += " ( " + mapNode.getMarkerGroup() + " )";
                                    textDrawArray.add(new TextDisplayStore(markerText, nodeScreenPos, Color.WHITE, false, null, false , 1.0f));
                                }
                                if (bShowMarkerIcons) {
                                    imageDrawArray.add(new ImageDisplayList(getMarkerImage(), (int) nodeScreenPos.getX(), (int) nodeScreenPos.getY() - 20));
                                }
                            }
                        }
                    }

                    // do we draw the node hover-over image and add the marker name/group to the drawToScreen list

                    MapNode hoverNode = getMapPanel().getHoveredNode();
                    if (hoverNode != null) {
                        if (hoverNode.isMapNode()) {
                            boolean selected = buttonManager.getCurrentButton() == null || buttonManager.getCurrentButton().showHoverNodeSelect() || hoverNode.isSelected();
                            Point2D hoverNodePos = worldPosToScreenPos(hoverNode.x, hoverNode.z);
                            nodeDrawArray.add(new NodeDisplayList((int) hoverNodePos.getX(), (int) hoverNodePos.getY(), hoverNode.flag, selected, !hoverNode.isNodeHidden()));
                        }
                    }
                }

                if (bDebugShowRenderProfileInfo) nodeComputeTimer.stopTimer();
                try {
                    drawOrderLatch.await();
                    drawLock.lock();
                    if (bDebugShowRenderProfileInfo) nodeDrawTimer.startTimer();
                    if (!nodeDrawArray.isEmpty()) drawNodes(renderGraphics, nodeDrawArray);
                    if (bDebugShowRenderProfileInfo) nodeDrawTimer.stopTimer();
                    if (bDebugShowRenderProfileInfo) imageDrawTimer.startTimer();
                    if (!imageDrawArray.isEmpty()) drawImages(renderGraphics, imageDrawArray);
                    if (bDebugShowRenderProfileInfo) imageDrawTimer.stopTimer();
                    if (bDebugShowRenderProfileInfo) textDrawTimer.startTimer();
                    if (!textDrawArray.isEmpty()) drawText(renderGraphics, textDrawArray);
                    if (bDebugShowRenderProfileInfo) textDrawTimer.stopTimer();
                } catch (InterruptedException ex) {
                    ex.printStackTrace();
                    throw new RuntimeException(ex);
                } finally {
                    drawLock.unlock();
                }

                if (bDebugShowRenderProfileInfo) {
                    nodeComputeTotal = nodeDrawArray.size();
                }
                threadCountLatch.countDown();
            }
        }
    }

    private void drawNodes(Graphics g,  ArrayList<NodeDisplayList> nodeList) {
        if (nodeSizeScaled >= MIN_VISIBLE_NODE_SIZE) {
            Graphics2D gTrans = (Graphics2D) g.create();
            Composite visible = AlphaComposite.SrcOver.derive(1f);
            Composite hidden = AlphaComposite.SrcOver.derive(hiddenNodesTransparencyLevel);

            for (NodeDisplayList node : nodeList) {
                gTrans.setComposite((node.isVisible) ? visible : hidden);
                gTrans.drawImage((node.flag == NODE_FLAG_REGULAR) ? regularNodeImage : subprioNodeImage, (int) (node.x - (double) (regularNodeImage.getWidth()-1) / 2), (int) (node.y - (double) (regularNodeImage.getHeight()-1) / 2), (int) nodeSizeScaled, (int) nodeSizeScaled, null);

                if (node.isSelected) {
                    gTrans.drawImage(selectedNodeOverlayImage, (int) (node.x - (double) (selectedNodeOverlayImage.getWidth()-1) /2), (int) (node.y - (double) (selectedNodeOverlayImage.getHeight()-1) / 2), (int) nodeSizeScaled, (int) nodeSizeScaled, null);
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
}
