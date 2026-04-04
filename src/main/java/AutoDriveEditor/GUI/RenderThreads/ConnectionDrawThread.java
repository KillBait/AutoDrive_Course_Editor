package AutoDriveEditor.GUI.RenderThreads;

import AutoDriveEditor.Classes.Util_Classes.ProfileUtil;
import AutoDriveEditor.RoadNetwork.MapNode;
import AutoDriveEditor.RoadNetwork.RoadMap;

import java.awt.*;
import java.awt.geom.Point2D;
import java.util.ArrayList;

import static AutoDriveEditor.Classes.Util_Classes.LoggerUtils.LOG;
import static AutoDriveEditor.Classes.Util_Classes.MathUtils.normalizeAngle;
import static AutoDriveEditor.GUI.MapImage.pdaImage;
import static AutoDriveEditor.GUI.MapPanel.*;
import static AutoDriveEditor.GUI.Menus.DebugMenu.ShowRenderProfileInfo.bDebugShowRenderProfileInfo;
import static AutoDriveEditor.RoadNetwork.MapNode.NODE_FLAG_REGULAR;
import static AutoDriveEditor.RoadNetwork.MapNode.NODE_FLAG_SUBPRIO;
import static AutoDriveEditor.XMLConfig.EditorXML.*;

public class ConnectionDrawThread implements Runnable{

    // The connection drawing thread finishes last in almost all cases, so we keep this as small as possible
    // we only draw the connections in the visible area (plus some extra padding) so we don't see the
    // connections clipping.

    private static ArrayList<MapNode> visibleNodes;

    private static volatile boolean isStopped = false;
    private final ArrayList<ConnectionDrawList> drawList = new ArrayList<>();

    public static final ProfileUtil connectionComputeTimer = new ProfileUtil();
    public static final ProfileUtil connectionDrawTimer = new ProfileUtil();

    public static int connectionDrawTotal;
    public static int skippedConnections;

    private static class ConnectionDrawList {
        final Point2D startPos;
        final Point2D endPos;
        final Color colour;
        final boolean isDual;
        final boolean isHidden;

        public ConnectionDrawList(Point2D start, Point2D end, Color colour, boolean dual, boolean hidden) {
            this.startPos = start;
            this.endPos = end;
            this.colour = colour;
            this.isDual = dual;
            this.isHidden = hidden;
        }
    }

    public static void setVisibleNodes(ArrayList<MapNode> nodeList) {
        visibleNodes = nodeList;
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
                drawList.clear();
                this.wait();
            } catch (InterruptedException e) {
                if (isStopped) {
                    LOG.info("ConnectionDraw Thread exiting");
                    return;
                }

                if (bDebugShowRenderProfileInfo) {
                    connectionDrawTimer.resetTimer();
                    connectionComputeTimer.resetTimer();
                    connectionComputeTimer.startTimer();
                }

                if (renderGraphics != null && pdaImage != null) {
                    Color colour;
                    for (MapNode mapNode : visibleNodes) {
                        if (!mapNode.outgoing.isEmpty()) {
                            Point2D nodePos = worldPosToScreenPos(mapNode.x, mapNode.z);
                            for (MapNode outgoing : mapNode.outgoing) {
                                Point2D outPos = worldPosToScreenPos(outgoing.x, outgoing.z);
                                int mapNodeFlag = (mapNode.getPreviewNodeFlagChange())? 1 - mapNode.flag : mapNode.flag;
                                int outFlag = (outgoing.getPreviewNodeFlagChange())? 1 - outgoing.flag : outgoing.flag;
                                boolean hidden = mapNode.getPreviewConnectionHiddenList().contains(outgoing) != mapNode.isConnectionHidden(outgoing);
                                if (RoadMap.isRegular(mapNode, outgoing)) {
                                    if (!mapNode.getIgnoreDrawingConnectionsList().contains(outgoing)) {
                                        if (mapNodeFlag == NODE_FLAG_SUBPRIO) {
                                            drawList.add(new ConnectionDrawList(nodePos, outPos, colourConnectSubprio, false, hidden));
                                        } else {
                                            drawList.add(new ConnectionDrawList(nodePos, outPos, colourConnectRegular, false, hidden));
                                        }
                                    }
                                } else if (RoadMap.isDual(mapNode, outgoing)) {
                                    if (!mapNode.getIgnoreDrawingConnectionsList().contains(outgoing)) {
                                        // for node type preview, if either node is subprio draw both arrows the correct colour
                                        if (mapNodeFlag == NODE_FLAG_REGULAR && outFlag == NODE_FLAG_REGULAR) {
                                            colour = colourConnectDual;
                                        } else {
                                            colour = colourConnectDualSubprio;
                                        }
                                        drawList.add(new ConnectionDrawList(nodePos, outPos, colour, true, hidden));
                                    }
                                } else if (RoadMap.isReverse(mapNode, outgoing) || RoadMap.isCrossedReverse(mapNode, outgoing)) {
                                    if (!mapNode.getIgnoreDrawingConnectionsList().contains(outgoing)) {
                                        if (mapNodeFlag == NODE_FLAG_REGULAR) {
                                            colour = colourConnectReverse;
                                        } else {
                                            colour = colourConnectReverseSubprio;
                                        }
                                        drawList.add(new ConnectionDrawList(nodePos, outPos, colour, false, hidden));
                                    }
                                }
                            }
                        }
//                        if (!mapNode.connections.isEmpty()) {
//                            Point2D nodePos = mapNode.getScreenPosition2D();
//                            for (Connection connection : mapNode.connections) {
//                                Point2D outPos = connection.getEndNodeScreenPos();
//                                drawList.add(new ConnectionDrawList(nodePos, outPos, connection.getColor(), false, false));
//                            }
//                        }

                    }



                    if (bDebugShowRenderProfileInfo) connectionComputeTimer.stopTimer();
                    drawLock.lock();
                    try {
                        if (bDebugShowRenderProfileInfo) connectionDrawTimer.startTimer();
                        skippedConnections = drawArrowList(renderGraphics, drawList);
                        if (bDebugShowRenderProfileInfo) connectionDrawTimer.stopTimer();
                    } finally {
                        drawLock.unlock();
                    }
                }
                connectionDrawTotal = drawList.size();
                threadCountLatch.countDown();
                drawOrderLatch.countDown();
            }
        }
    }

    private int drawArrowList(Graphics g, ArrayList<ConnectionDrawThread.ConnectionDrawList> nodeList) {
        if (nodeList.isEmpty()) return 0;

        Graphics2D gTrans = (Graphics2D) g.create();
        Polygon p = new Polygon();

        double arrowLeftAngle = Math.toRadians(-20);
        double arrowRightAngle = Math.toRadians(20);

        int sx, sy, ex, ey;

        double angleRad, offsetX, offsetY, maxDistance;

        int skipped = 0;


        for (ConnectionDrawThread.ConnectionDrawList mapNode : nodeList) {

            sx = (int) mapNode.startPos.getX();
            sy = (int) mapNode.startPos.getY();
            ex = (int) mapNode.endPos.getX();
            ey = (int) mapNode.endPos.getY();

            maxDistance = Math.hypot(ex - sx, ey - sy);

            if (maxDistance < 1) {
                skipped++;
                continue;
            }

            angleRad = Math.atan2(sy - ey, sx - ex);

            offsetX = nodeSizeScaledHalf * Math.cos(angleRad);
            offsetY = nodeSizeScaledHalf * Math.sin(angleRad);

            gTrans.setComposite(AlphaComposite.SrcOver.derive(mapNode.isHidden ? hiddenNodesTransparencyLevel : 1f));
            gTrans.setColor(mapNode.colour);

            if (nodeSizeScaled >= 2.0) {
                double lineLength = maxDistance - nodeSizeScaled;
                int diff = 0;

                if (mapNode.isDual) {
                    if (lineLength <= nodeSizeScaled * 2) {
                        diff = (int) ((nodeSizeScaled * 2) - lineLength) / 2;
                    }
                } else {
                    if (lineLength <= nodeSizeScaled) {
                        diff = (int) (nodeSizeScaled - lineLength);
                    }
                }

                if (maxDistance >= nodeSizeScaled) {

                    double adjustedArrowLength = (nodeSize * zoomLevel * 0.7) - (diff / 1.15);

                    double normalizedAngleRad = normalizeAngle(angleRad + arrowLeftAngle);
                    int arrowLeftX = (int) (ex + Math.cos(normalizedAngleRad) * adjustedArrowLength);
                    int arrowLeftY = (int) (ey + Math.sin(normalizedAngleRad) * adjustedArrowLength);

                    double normalizedAngleRad2 = normalizeAngle(angleRad + arrowRightAngle);
                    int arrowRightX = (int) (ex + Math.cos(normalizedAngleRad2) * adjustedArrowLength);
                    int arrowRightY = (int) (ey + Math.sin(normalizedAngleRad2) * adjustedArrowLength);

                    if (bFilledArrows) {
                        p.addPoint((int) (ex + offsetX), (int) (ey + offsetY));
                        p.addPoint(arrowLeftX, arrowLeftY);
                        p.addPoint(arrowRightX, arrowRightY);
                        gTrans.fillPolygon(p);
                        p.reset();
                    } else {

                        gTrans.drawLine((int) (ex + offsetX), (int) (ey + offsetY), arrowLeftX, arrowLeftY);
                        gTrans.drawLine((int) (ex + offsetX), (int) (ey + offsetY), arrowRightX, arrowRightY);
                    }
                }
            }
            gTrans.drawLine((int) (sx - offsetX), (int) (sy - offsetY), (int) (ex + offsetX), (int) (ey + offsetY));
        }
        return skipped;
    }
}
