package AutoDriveEditor.GUI.RenderThreads;

import AutoDriveEditor.RoadNetwork.MapNode;
import AutoDriveEditor.RoadNetwork.RoadMap;
import AutoDriveEditor.Utils.ProfileUtil;

import java.awt.*;
import java.awt.geom.Point2D;
import java.util.ArrayList;

import static AutoDriveEditor.AutoDriveEditor.getMapPanel;
import static AutoDriveEditor.Classes.MapImage.pdaImage;
import static AutoDriveEditor.GUI.Buttons.ConnectionSelectBaseButton.Connection;
import static AutoDriveEditor.GUI.Buttons.ConnectionSelectBaseButton.connectionsList;
import static AutoDriveEditor.GUI.MapPanel.*;
import static AutoDriveEditor.GUI.Menus.DebugMenu.ShowProfileInfo.bDebugShowProfileInfo;
import static AutoDriveEditor.RoadNetwork.MapNode.NODE_FLAG_REGULAR;
import static AutoDriveEditor.RoadNetwork.MapNode.NODE_FLAG_SUBPRIO;
import static AutoDriveEditor.Utils.LoggerUtils.LOG;
import static AutoDriveEditor.Utils.MathUtils.normalizeAngle;
import static AutoDriveEditor.XMLConfig.EditorXML.*;

public class ConnectionDrawThread implements Runnable{

    // The connection drawing thread finishes last in almost all cases, so we keep this as small as possible
    // we only draw the connections in the visible area (plus some extra padding) so we don't see the
    // connections clipping.

    private static volatile boolean isStopped = false;
    private final ArrayList<ConnectionDrawThread.ConnectionDrawList> drawList = new ArrayList<>();

    public static final ProfileUtil connectionComputeTimer = new ProfileUtil();
    public static final ProfileUtil connectionDrawTimer = new ProfileUtil();

    public static int connectionDrawTotal;

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

                if (bDebugShowProfileInfo) {
                    connectionDrawTimer.resetTimer();
                    connectionComputeTimer.resetTimer();
                    connectionComputeTimer.startTimer();
                }

                if (renderGraphics != null && pdaImage != null) {
                    Point2D topLeft = screenPosToWorldPos(0,0);
                    Point2D bottomRight = screenPosToWorldPos(getMapPanel().getWidth(),getMapPanel().getHeight());
                    double offScreenDistance = 24;
                    Color colour;

                    for (MapNode mapNode : RoadMap.networkNodesList) {
                        if (topLeft.getX() - offScreenDistance < mapNode.x && bottomRight.getX() + offScreenDistance > mapNode.x && topLeft.getY() - offScreenDistance < mapNode.z && bottomRight.getY() + offScreenDistance > mapNode.z) {
                            if (mapNode.outgoing.size() > 0) {
                                Point2D nodePos = worldPosToScreenPos(mapNode.x, mapNode.z);
                                for (MapNode outgoing : mapNode.outgoing) {
                                    Point2D outPos = worldPosToScreenPos(outgoing.x, outgoing.z);
                                    int mapNodeFlag = (mapNode.getPreviewNodeFlagChange())? 1 - mapNode.flag : mapNode.flag;
                                    int outFlag = (outgoing.getPreviewNodeFlagChange())? 1 - outgoing.flag : outgoing.flag;
                                    boolean hidden = (Connection.contains(connectionsList, mapNode, outgoing) != mapNode.isConnectionHidden(outgoing));
                                    if (RoadMap.isDual(mapNode, outgoing)) {
                                        if (!mapNode.getIgnoreDrawingConnectionsList().contains(outgoing)) {
                                            // for node type preview, if either node is subprio draw both arrows the correct colour
                                            if (mapNodeFlag == NODE_FLAG_REGULAR && outFlag == NODE_FLAG_REGULAR) {
                                                colour = colourConnectDual;
                                            } else {
                                                colour = colourConnectDualSubprio;
                                            }
                                            drawList.add(new ConnectionDrawList(nodePos, outPos, colour, true, hidden));
                                        }
                                    } else if (RoadMap.isReverse(mapNode, outgoing)) {
                                        if (!mapNode.getIgnoreDrawingConnectionsList().contains(outgoing)) {
                                            if (mapNodeFlag == NODE_FLAG_REGULAR) {
                                                colour = colourConnectReverse;
                                            } else {
                                                colour = colourConnectReverseSubprio;
                                            }
                                            drawList.add(new ConnectionDrawList(nodePos, outPos, colour, false, hidden));
                                        }
                                    } else {
                                        if (!mapNode.getIgnoreDrawingConnectionsList().contains(outgoing)) {
                                            if (mapNodeFlag == NODE_FLAG_SUBPRIO) {
                                                drawList.add(new ConnectionDrawList(nodePos, outPos, colourConnectSubprio, false, hidden));
                                            } else {
                                                drawList.add(new ConnectionDrawList(nodePos, outPos, colourConnectRegular, false, hidden));
                                            }
                                        }
                                    }
                                }
                            }
                        }
                    }

                    if (bDebugShowProfileInfo) connectionComputeTimer.stopTimer();
                    drawLock.lock();
                    try {
                        if (bDebugShowProfileInfo) connectionDrawTimer.startTimer();
                        drawArrowList(renderGraphics, drawList);
                        if (bDebugShowProfileInfo) connectionDrawTimer.stopTimer();
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

    private void drawArrowList(Graphics g, ArrayList<ConnectionDrawThread.ConnectionDrawList> nodeList) {

        if (nodeList.size() > 0) {

            double startX;
            double startY;
            double targetX;
            double targetY;

            Polygon p = new Polygon();
            Graphics2D gTrans = (Graphics2D) g.create();

            for (ConnectionDrawThread.ConnectionDrawList mapNode : nodeList) {

                startX = mapNode.startPos.getX();
                startY = mapNode.startPos.getY();
                targetX = mapNode.endPos.getX();
                targetY = mapNode.endPos.getY();

                double angleRad = Math.atan2(startY - targetY, startX - targetX);

                double distCos = (nodeSizeScaledHalf) * Math.cos(angleRad);
                double distSin = (nodeSizeScaledHalf) * Math.sin(angleRad);

                // calculate where the line starts based around the circumference of the start node

                double lineStartX = startX - distCos;
                double lineStartY = startY - distSin;

                // calculate where to finish the line based around the circumference of the node

                double lineEndX = targetX + distCos;
                double lineEndY = targetY + distSin;

                // Calculate the distance between the two points

                double maxDistance = Math.sqrt(Math.pow((targetX - startX), 2) + Math.pow((targetY - startY), 2));

                float tr = 1f;
                if (mapNode.isHidden) tr = hiddenNodesTransparencyLevel;
                gTrans.setComposite(AlphaComposite.SrcOver.derive(tr));
                gTrans.setColor(mapNode.colour);

                if (nodeSizeScaled >= 2.0) {
                    double lineLength = maxDistance - nodeSizeScaled;
                    int diff = 0;

                    if (mapNode.isDual) {
                        if (lineLength <= (nodeSizeScaled * 2)) {
                            diff =(int) ((nodeSizeScaled * 2) - lineLength) / 2;
                        }
                    } else {
                        if (lineLength <= nodeSizeScaled) {
                            diff = (int) (nodeSizeScaled - lineLength);
                        }
                    }
                    double adjustedArrowLength = ((nodeSize * zoomLevel) * 0.7) - (diff / 1.15);

                    // Calculate where the center of the edge closest to the start point is
                    double targetPolygonCenterX = targetX + (Math.cos(angleRad) * (adjustedArrowLength));
                    double targetPolygonCenterY = targetY + (Math.sin(angleRad) * (adjustedArrowLength));

                    double arrowLeft = normalizeAngle(angleRad + Math.toRadians(-20));
                    double arrowLeftX = targetX + (Math.cos(arrowLeft) * adjustedArrowLength);
                    double arrowLeftY = targetY + (Math.sin(arrowLeft) * adjustedArrowLength);

                    double arrowRight = normalizeAngle(angleRad + Math.toRadians(20));
                    double arrowRightX = targetX + (Math.cos(arrowRight) * adjustedArrowLength);
                    double arrowRightY = targetY + (Math.sin(arrowRight) * adjustedArrowLength);

                    if (maxDistance >= nodeSizeScaled) {
                        if (bFilledArrows) {
                            // filled arrows look better, but have a performance impact on the draw times
                            p.addPoint((int) lineEndX, (int) lineEndY);
                            p.addPoint((int) arrowLeftX, (int) arrowLeftY);
                            //p.addPoint((int) targetPolygonCenterX, (int) targetPolygonCenterY);
                            p.addPoint((int) arrowRightX, (int) arrowRightY);
                            gTrans.fillPolygon(p);
                            p.reset();
                        } else {
                            gTrans.drawLine((int) lineStartX, (int) lineStartY, (int) lineEndX, (int) (lineEndY));
                            gTrans.drawLine((int) lineEndX, (int) lineEndY, (int) arrowLeftX, (int) arrowLeftY);
                            gTrans.drawLine((int) lineEndX, (int) lineEndY, (int) arrowRightX, (int) arrowRightY);
                        }
                    }
                    if (mapNode.isDual) {
                        angleRad = normalizeAngle(angleRad+Math.PI);
                        double startPolygonCenterX = startX + (Math.cos(angleRad) * adjustedArrowLength);
                        double startPolygonCenterY = startY + (Math.sin(angleRad) * adjustedArrowLength);
                        gTrans.drawLine((int) startPolygonCenterX, (int) startPolygonCenterY, (int) targetPolygonCenterX, (int) targetPolygonCenterY);
                    } else {
                        gTrans.drawLine((int) lineStartX, (int) lineStartY, (int) targetPolygonCenterX, (int) targetPolygonCenterY);

                    }
                } else {
                    // small zoomLevel's don't draw the actual Nodes, draw from the start to the end of
                    // the node position, no visible gaps are seen between the node points.
                    gTrans.drawLine((int) lineStartX, (int) lineStartY, (int) lineEndX, (int) lineEndY);
                }
            }
        }
    }
}
