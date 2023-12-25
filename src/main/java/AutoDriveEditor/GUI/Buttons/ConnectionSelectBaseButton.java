package AutoDriveEditor.GUI.Buttons;

import AutoDriveEditor.RoadNetwork.MapNode;
import AutoDriveEditor.RoadNetwork.RoadMap;

import java.awt.event.MouseEvent;
import java.awt.geom.Path2D;
import java.awt.geom.Point2D;
import java.awt.geom.Rectangle2D;
import java.util.ArrayList;

import static AutoDriveEditor.AutoDriveEditor.getMapPanel;
import static AutoDriveEditor.GUI.Buttons.LinerLineBaseButton.*;
import static AutoDriveEditor.GUI.MapPanel.screenPosToWorldPos;
import static AutoDriveEditor.GUI.MapPanel.worldPosToScreenPos;
import static AutoDriveEditor.GUI.Menus.DebugMenu.Logging.LogConnectSelectionMenu.bDebugConnectSelection;
import static AutoDriveEditor.Listeners.MouseListener.currentMouseX;
import static AutoDriveEditor.Listeners.MouseListener.currentMouseY;
import static AutoDriveEditor.Managers.ButtonManager.getCurrentButton;
import static AutoDriveEditor.Managers.MultiSelectManager.*;
import static AutoDriveEditor.RoadNetwork.MapNode.NODE_FLAG_REGULAR;
import static AutoDriveEditor.RoadNetwork.MapNode.NODE_FLAG_SUBPRIO;
import static AutoDriveEditor.Utils.LoggerUtils.LOG;
import static AutoDriveEditor.XMLConfig.EditorXML.linearLineNodeDistance;

public abstract class ConnectionSelectBaseButton extends BaseButton {

    public static final ArrayList<Connection> connectionsList = new ArrayList<>();
    public final int padding = linearLineNodeDistance * 2;
    public MapNode lastDetected;

    public static boolean ignore = true;
    public static boolean getIgnore() { return ignore;}


    protected boolean detectRegularConnections() { return true; }
    protected boolean detectSubprioConnections() { return true; }
    protected boolean detectReverseConnections() { return true; }
    protected boolean detectDualConnections() { return true; }


    @Override
    public Boolean useMultiSelection() { return true; }
    @Override
    public Boolean addSelectedToMultiSelectList() { return false; }
    @Override
    public Boolean ignoreDeselect() { return true; }
    @Override
    public Boolean previewNodeSelectionChange() { return false; }

    @Override
    public void mouseMoved(MouseEvent e) {
        if (getCurrentButton() == this) {
            resetConnectionList();
            Point2D mousePosWorld = screenPosToWorldPos(e.getX(), e.getY());
            getConnectionsAroundPoint(mousePosWorld);
            getMapPanel().repaint();
        }
    }

    @Override
    public void mouseDragged(MouseEvent e) {
        if (getCurrentButton() == this && getIsMultiSelectDragging()) {
            resetConnectionList();
            if (useRectangularSelection) {
                getConnectionsInSelectionRectangle(multiSelectRect);
            } else {
                getConnectionsInSelectionPath(getFreeformSelectionPath());
            }
        }
    }

    @Override
    public void mouseReleased(MouseEvent e) {
        for (Connection segment : connectionsList) {
            if (!segment.getStartNode().getHiddenConnectionsList().contains(segment.getEndNode())) {
                segment.getStartNode().getHiddenConnectionsList().add(segment.getEndNode());
            } else {
                segment.getStartNode().getHiddenConnectionsList().remove(segment.getEndNode());
            }
        }
        resetConnectionList();
        getMapPanel().repaint();
    }

    public void getConnectionsAroundPoint(Point2D point) {
        double startX = point.getX() - padding;
        double startY = point.getY() - padding;
        double endX = point.getX() + padding;
        double endY = point.getY() + padding;

        for (MapNode mapNode : RoadMap.networkNodesList) {
            if (mapNode.x > startX && mapNode.x < endX && mapNode.z > startY && mapNode.z < endY) {
                if (mapNode.outgoing.size() > 0) {
                    for (MapNode outgoing : mapNode.outgoing) {
                        Point2D mouseWorldPos = screenPosToWorldPos(currentMouseX, currentMouseY);
                        if (pointToLineDistance(mapNode, outgoing, mouseWorldPos.getX(), mouseWorldPos.getY()) < .5) {
                            int connectionType = getConnectionTypeFor(mapNode, outgoing);
                            if (connectionType != CONNECTION_UNKNOWN) {
                                connectionsList.add(new Connection(mapNode, outgoing, connectionType));
                            }
                            lastDetected = mapNode;
                        }
                    }
                }
            }
        }
    }

    public double pointToLineDistance(MapNode startNode, MapNode endNode, double worldMouseX, double worldMouseY) {

        // calculate the direction vector of the line
        double dx = endNode.x - startNode.x;
        double dy = endNode.z - startNode.z;
        double lengthSquared = dx * dx + dy * dy;

        // handle the special case where the line segment is a point
        if (lengthSquared == 0.0) {
            return Math.sqrt((worldMouseX - startNode.x) * (worldMouseX - startNode.x) + (worldMouseY - startNode.z) * (worldMouseY - startNode.z));
        }

        double t = ((worldMouseX - startNode.x) * dx + (worldMouseY - startNode.z) * dy) / lengthSquared;
        t = Math.max(0, Math.min(1, t));

        double closestX = startNode.x + t * dx;
        double closestY = startNode.z + t * dy;

        return Math.sqrt((worldMouseX - closestX) * (worldMouseX - closestX) +
                (worldMouseY - closestY) * (worldMouseY - closestY));
    }

    private void getConnectionsInSelectionRectangle(Rectangle2D rect) {
        double startX = rect.getX() - padding;
        double startY = rect.getY() - padding;
        double endX = rect.getX() + (rect.getWidth() + padding);
        double endY = rect.getY() + (rect.getHeight() + padding);

        for (MapNode mapNode : RoadMap.networkNodesList) {
            if (mapNode.x > startX && mapNode.x < endX && mapNode.z > startY && mapNode.z < endY) {
                if (mapNode.outgoing.size() > 0) {
                    for (MapNode outgoing : mapNode.outgoing) {
                        if (rect.intersectsLine(mapNode.x, mapNode.z, outgoing.x, outgoing.z)) {
                            int connectionType = getConnectionTypeFor(mapNode, outgoing);
                            if (connectionType != CONNECTION_UNKNOWN) {
                                connectionsList.add(new Connection(mapNode, outgoing, connectionType));
                            }
                            lastDetected = mapNode;
                        }
                    }
                }
            }
        }
    }

    private void getConnectionsInSelectionPath(Path2D path) {
        if (path != null) {
            Rectangle2D rec = path.getBounds2D();
            Point2D startWorld = screenPosToWorldPos((int) rec.getX(), (int) rec.getY());
            Point2D endWorld = screenPosToWorldPos((int) (rec.getX() + rec.getWidth()), (int) (rec.getY() + rec.getHeight()));
            double startX = startWorld.getX() - padding;
            double startY = startWorld.getY() - padding;
            double endX = endWorld.getX() + padding;
            double endY = endWorld.getY() + padding;

            for (MapNode mapNode : RoadMap.networkNodesList) {
                // check if the mapNode is within the bounding box of the selection path
                if (mapNode.x > startX && mapNode.x < endX && mapNode.z > startY && mapNode.z < endY) {
                    for (MapNode outgoing : mapNode.outgoing) {
                        Point2D pointStart = worldPosToScreenPos(mapNode.x, mapNode.z);
                        Point2D pointEnd = worldPosToScreenPos(outgoing.x, outgoing.z);
                        int interval = (getCurrentButton() != null)? getLineDetectionInterval() : 10;
                        if (isLineIntersectingPath(pointStart, pointEnd, interval, getFreeformSelectionPath())) {
                            int connectionType = getConnectionTypeFor(mapNode, outgoing);
                            if (connectionType != CONNECTION_UNKNOWN) {
                                connectionsList.add(new Connection(mapNode, outgoing, connectionType));
                            }
                            lastDetected = mapNode;
                        }
                    }
                }
            }
        }
    }

    private boolean isLineIntersectingPath(Point2D point1, Point2D point2, double interval, Path2D path) {
        double length = point1.distance(point2);
        for (double t = 0; t <= 1.0; t += interval / length) {
            double x = point1.getX() + t * (point2.getX() - point1.getX());
            double y = point1.getY() + t * (point2.getY() - point1.getY());
            if (path.contains(x, y)) {
                // Found a point inside the path
                return true;
            }
        }
        // No points inside the path
        return false;
    }

    protected int getConnectionTypeFor(MapNode mapNode, MapNode outgoing) {

        int foundConnection = CONNECTION_UNKNOWN;

        if (RoadMap.isDual(mapNode, outgoing)) {
            if (detectDualConnections()) {
                // check if we have already detected on way of a dual connection and ignore if it is
                if (!Connection.contains(connectionsList, mapNode, outgoing)) {
                    if (mapNode.flag == NODE_FLAG_REGULAR && outgoing.flag == NODE_FLAG_REGULAR) {
                        if (bDebugConnectSelection && lastDetected != mapNode) {
                            LOG.info("Point Add # Dual {} --> {}", mapNode.id, outgoing.id);
                        }
                    } else if (mapNode.flag == NODE_FLAG_SUBPRIO || outgoing.flag == NODE_FLAG_SUBPRIO) {
                        if (bDebugConnectSelection && lastDetected != mapNode) {
                            LOG.info("Point Add # Subprio Dual {} --> {}", mapNode.id, outgoing.id);
                        }
                    }
                    foundConnection = CONNECTION_DUAL;
                }
            }
        } else if (RoadMap.isReverse(mapNode, outgoing)) {
            if (detectReverseConnections()) {
                if (mapNode.flag == NODE_FLAG_REGULAR) {
                    if (bDebugConnectSelection && lastDetected != mapNode) {
                        LOG.info("Point Add # Reverse {} --> {}", mapNode.id, outgoing.id);
                    }
                } else if (mapNode.flag == NODE_FLAG_SUBPRIO) {
                    if (bDebugConnectSelection && lastDetected != mapNode) {
                        LOG.info("Point Add # Subprio Reverse {} --> {}", mapNode.id, outgoing.id);
                    }
                }
                foundConnection = CONNECTION_REVERSE;
            }
        } else if (RoadMap.isRegular(mapNode, outgoing)) {
            if (detectRegularConnections() || detectSubprioConnections()) {
                if (mapNode.flag == NODE_FLAG_REGULAR && detectRegularConnections()) {
                    if (bDebugConnectSelection && lastDetected != mapNode) {
                        LOG.info("Point Add # Regular {} --> {}", mapNode.id, outgoing.id);
                    }
                    foundConnection = CONNECTION_STANDARD;
                } else if (mapNode.flag == NODE_FLAG_SUBPRIO && detectSubprioConnections()) {
                    if (bDebugConnectSelection && lastDetected != mapNode) {
                        LOG.info("Point Add # Subprio Regular {} --> {}", mapNode.id, outgoing.id);
                    }
                    foundConnection = CONNECTION_STANDARD_SUBPRIO;
                }
            }
        }
        lastDetected = mapNode;
        return foundConnection;
    }

    public void resetConnectionList() {
        for (Connection connection: connectionsList) {
            connection.startNode.getIgnoreDrawingConnectionsList().clear();
            connection.endNode.getIgnoreDrawingConnectionsList().clear();
        }
        connectionsList.clear();
    }

    public void resetHiddenStatusForAll(int connectionType) {
        for (MapNode node: RoadMap.networkNodesList) {
            if (node.getHiddenConnectionsList().size() > 0) {
                node.getHiddenConnectionsList().removeIf(hiddenNode -> getConnectionTypeFor(node, hiddenNode) == connectionType);
            }
        }
    }

    //
    // Getters
    //

    public static ArrayList<Connection> getConnectionList() { return connectionsList; }


    public static class Connection {

        private final MapNode startNode;
        private final MapNode endNode;
        private final int type; // connection type i.e. normal/reverse/dual
        private final boolean isHidden;

        public Connection(MapNode fromNode, MapNode toNode, int connectionType, boolean isHidden) {
            this.startNode = fromNode;
            this.endNode = toNode;
            this.type = connectionType;
            this.isHidden = isHidden;
        }

        public Connection(MapNode fromNode, MapNode toNode, int connectionType) {
            this.startNode = fromNode;
            this.endNode = toNode;
            this.type = connectionType;
            this.isHidden = false;
        }

        public MapNode getStartNode() { return startNode; }
        public MapNode getEndNode() { return endNode; }
        public int getConnectionType() { return type; }
        public double getStartX() { return this.startNode.x; }
        public double getStartZ() { return this.startNode.z; }
        public double getEndX() { return this.endNode.x; }
        public double getEndZ() { return this.endNode.z; }
        public boolean isHidden() { return this.isHidden; }

        public static boolean contains(ArrayList<Connection> segList, MapNode startNode, MapNode endNode) {
            for (Connection segment : segList) {
                if (segment.startNode.equals(startNode) && segment.endNode.equals(endNode)) {
                    return true;
                }
            }
            return false;
        }

        @Override
        public boolean equals(Object obj) {
            if (this == obj) {
                return true;
            }
            if (obj == null || getClass() != obj.getClass()) {
                return false;
            }
            Connection other = (Connection) obj;
            return this.startNode == other.startNode && this.endNode == other.endNode && this.type == other.type;
        }
    }
}
