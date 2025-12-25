package AutoDriveEditor.GUI.Buttons;

import AutoDriveEditor.Managers.ButtonManager;
import AutoDriveEditor.RoadNetwork.Connection;
import AutoDriveEditor.RoadNetwork.MapNode;
import AutoDriveEditor.RoadNetwork.RoadMap;

import java.awt.event.MouseEvent;
import java.awt.geom.Path2D;
import java.awt.geom.Point2D;
import java.awt.geom.Rectangle2D;
import java.util.ArrayList;
import java.util.Iterator;

import static AutoDriveEditor.AutoDriveEditor.buttonManager;
import static AutoDriveEditor.AutoDriveEditor.getMapPanel;
import static AutoDriveEditor.GUI.MapPanel.screenPosToWorldPos;
import static AutoDriveEditor.GUI.MapPanel.worldPosToScreenPos;
import static AutoDriveEditor.Listeners.MouseListener.currentMouseX;
import static AutoDriveEditor.Listeners.MouseListener.currentMouseY;
import static AutoDriveEditor.Managers.MultiSelectManager.*;
import static AutoDriveEditor.XMLConfig.EditorXML.linearLineNodeDistance;

public abstract class ConnectionSelectBaseButton extends BaseButton implements ButtonManager.ToolTipBuilder {

    public static final ArrayList<Connection> connectionsList = new ArrayList<>();
    public final int padding = linearLineNodeDistance * 2;
    public MapNode lastDetected;
    protected Connection.ConnectionType connectionTypeFilter = Connection.ConnectionType.NONE;

    // any class that extends the base functionality and wants to modify the filter logic
    // must override the filterConnections() function

    @Override
    public Boolean useMultiSelection() { return true; }
    @Override
    public Boolean addSelectedToMultiSelectList() { return false; }
    @Override
    public Boolean ignoreDeselect() { return true; }
    @Override
    public Boolean previewNodeSelectionChange() { return false; }

    @Override
    public void onButtonDeselect() {
        if (!connectionsList.isEmpty()) resetConnectionList();
    }

    @Override
    public void mouseMoved(MouseEvent e) {
        if (buttonManager.getCurrentButton() == this) {
            resetConnectionList();
            Point2D mousePosWorld = screenPosToWorldPos(e.getX(), e.getY());
            getConnectionsAroundPoint(mousePosWorld);
            getMapPanel().repaint();
            filterConnections();
        }
    }

    @Override
    public void mouseDragged(MouseEvent e) {
        if (buttonManager.getCurrentButton() == this && getIsMultiSelectDragging()) {
            resetConnectionList();
            if (useRectangularSelection) {
                getConnectionsInSelectionRectangle(multiSelectRect);
            } else {
                getConnectionsInSelectionPath(getFreeformSelectionPath());
            }
            filterConnections();
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
                if (!mapNode.outgoing.isEmpty()) {
                    for (MapNode outgoing : mapNode.outgoing) {
                        Point2D mouseWorldPos = screenPosToWorldPos(currentMouseX, currentMouseY);
                        if (pointToLineDistance(mapNode, outgoing, mouseWorldPos.getX(), mouseWorldPos.getY()) < .5) {
                            Connection.ConnectionType connectionType = RoadMap.getConnection(mapNode, outgoing);
                            if (connectionType != Connection.ConnectionType.UNKNOWN) {
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
                if (!mapNode.outgoing.isEmpty()) {
                    for (MapNode outgoing : mapNode.outgoing) {
                        if (rect.intersectsLine(mapNode.x, mapNode.z, outgoing.x, outgoing.z)) {
                            Connection.ConnectionType connectionType = RoadMap.getConnection(mapNode, outgoing);
                            if (connectionType != Connection.ConnectionType.UNKNOWN) {
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
                        int interval = (buttonManager.getCurrentButton() != null)? getLineDetectionInterval() : 10;
                        if (isLineIntersectingPath(pointStart, pointEnd, interval, getFreeformSelectionPath())) {
                            Connection.ConnectionType connectionType = RoadMap.getConnection(mapNode, outgoing);
                            if (connectionType != Connection.ConnectionType.UNKNOWN) {
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

    protected void filterConnections() {
        Iterator<Connection> iterator = connectionsList.iterator();
        while (iterator.hasNext()) {
            Connection connection = iterator.next();
            if (connectionTypeFilter != Connection.ConnectionType.NONE) {
                if (connectionTypeFilter == Connection.ConnectionType.REGULAR) {
                    if (connection.getConnectionType() != Connection.ConnectionType.REGULAR && connection.getConnectionType() != Connection.ConnectionType.CROSSED_REGULAR) {
                        iterator.remove();
                        continue;
                    }
                } else if (connectionTypeFilter == Connection.ConnectionType.REVERSE) {
                    if (connection.getConnectionType() != Connection.ConnectionType.REVERSE && connection.getConnectionType() != Connection.ConnectionType.CROSSED_REVERSE) {
                        iterator.remove();
                        continue;
                    }
                } else {
                    if (connectionTypeFilter != connection.getConnectionType()) {
                        iterator.remove();
                        continue;
                    }
                }
                connection.getStartNode().getPreviewConnectionHiddenList().add(connection.getEndNode());
            }
        }
    }

    public void resetConnectionList() {
        for (Connection connection: connectionsList) {
            connection.getStartNode().getIgnoreDrawingConnectionsList().clear();
            connection.getEndNode().getIgnoreDrawingConnectionsList().clear();
            connection.getStartNode().getPreviewConnectionHiddenList().clear();
        }
        connectionsList.clear();
    }

    public void resetHiddenStatusForConnectionType(Connection.ConnectionType connectionType) {
        for (MapNode node : RoadMap.networkNodesList) {
            if (!node.getHiddenConnectionsList().isEmpty()) {
                Iterator<MapNode> iterator = node.getHiddenConnectionsList().iterator();
                while (iterator.hasNext()) {
                    MapNode hiddenNode = iterator.next();
                    Connection.ConnectionType hiddenConnection = RoadMap.getConnection(node, hiddenNode);
                    if (connectionType == Connection.ConnectionType.REGULAR) {
                        if (hiddenConnection == Connection.ConnectionType.REGULAR || hiddenConnection == Connection.ConnectionType.CROSSED_REGULAR) {
                            iterator.remove();
                        }
                    } else if (connectionType == Connection.ConnectionType.REVERSE) {
                        if (hiddenConnection == Connection.ConnectionType.REVERSE || hiddenConnection == Connection.ConnectionType.CROSSED_REVERSE) {
                            iterator.remove();
                        }
                    } else {
                        // hiddenConnection is neither regular nor reverse
                        if (hiddenConnection == connectionType) {
                            iterator.remove();
                        }
                    }
                }
            }
        }
    }

    //
    // Getters
    //

    public static ArrayList<Connection> getConnectionList() { return connectionsList; }
}
