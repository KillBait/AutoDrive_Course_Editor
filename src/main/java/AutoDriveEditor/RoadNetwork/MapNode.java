package AutoDriveEditor.RoadNetwork;

import AutoDriveEditor.Classes.Interfaces.SelectorInterface;
import AutoDriveEditor.Classes.Widgets.Selectors.MoveSelector;
import AutoDriveEditor.Classes.Widgets.Selectors.RadiusSelector;
import AutoDriveEditor.Classes.Widgets.Selectors.RotationSelector;

import java.awt.*;
import java.awt.geom.Point2D;
import java.math.RoundingMode;
import java.util.ArrayList;
import java.util.List;

import static AutoDriveEditor.AutoDriveEditor.curveManager;
import static AutoDriveEditor.Classes.Util_Classes.LoggerUtils.LOG;
import static AutoDriveEditor.Classes.Util_Classes.MathUtils.limitDoubleToDecimalPlaces;
import static AutoDriveEditor.Classes.Util_Classes.MathUtils.roundUpDoubleToDecimalPlaces;
import static AutoDriveEditor.GUI.MapPanel.worldPosToScreenPos;
import static AutoDriveEditor.GUI.Menus.DebugMenu.Logging.LogCurveInfoMenu.bDebugLogCurveInfo;
import static AutoDriveEditor.GUI.Menus.DebugMenu.Logging.LogMarkerInfoMenu.bDebugLogMarkerInfo;
import static AutoDriveEditor.Managers.ScanManager.checkNodeOverlap;
import static AutoDriveEditor.XMLConfig.EditorXML.bSelectHidden;

public class MapNode implements Comparable<MapNode> {

//    public enum NodeFlag {
//        REGULAR(0),
//        SUBPRIO(1),
//        AUTODRIVE_GENERATED(2, 4),
//
//        ROTATE_NODE(98),
//        CONTROL_NODE(99);
//
//        private final int[] values;
//
//        NodeFlag(int... values) {
//            if (this.name().equals("AUTODRIVE_GENERATED")) {
//                if (values.length != 2) {
//                    throw new IllegalArgumentException("NodeType AUTODRIVE_GENERATED must have exactly 2 values.");
//                }
//            } else {
//                if (values.length != 1) {
//                    throw new IllegalArgumentException("NodeType " + this.name() + " must have exactly 1 value.");
//                }
//            }
//            this.values = values;
//        }
//        public NodeFlag getFlag() { return this; }
//
//        public boolean isRegular() { return this == REGULAR; }
//        public boolean isSubPrio() { return this == SUBPRIO; }
//        public boolean isAutoDriveGenerated() {
//            for (int value : values) {
//                if (this == AUTODRIVE_GENERATED) {
//                    return true;
//                }
//            }
//            return false;
//        }
//        public boolean isRotateNode() { return this == ROTATE_NODE; }
//        public boolean isControlNode() { return this == CONTROL_NODE; }
//
//    }


    // These flags that match the network flags created by the AutoDrive Mod
    public static final int NODE_FLAG_REGULAR = 0;
    public static final int NODE_FLAG_SUBPRIO = 1;

    // Special node types used by the editor only..
    public static final int NODE_FLAG_SPECIAL_NODE = 97;
    public static final int NODE_FLAG_ROTATION_NODE = 98;
    public static final int NODE_FLAG_CONTROL_NODE = 99;

    public enum NodeWarning {
        NODE_WARNING_NONE("None"),
        NODE_WARNING_OVERLAP("Overlap"),
        NODE_WARNING_NEGATIVE_Y("Negative Y"),
        NODE_WARNING_OVERLAP_Y("Overlap Y");

        private final String description;

        NodeWarning(String description) { this.description = description; }

        public String getDescription() { return description; }

    }

    // Shared static instances of selectors
    private static final SelectorInterface MOVE_SELECTOR = new MoveSelector();
    private static final SelectorInterface ROTATION_SELECTOR = new RotationSelector();
    private static final SelectorInterface RADIUS_SELECTOR = new RadiusSelector();

//    public enum ConnectionResult {
//        SUCCESS,
//        FAILED,
//        ALREADY_EXISTS,
//        ADDED,
//        REMOVED,
//        MODIFIED;
//    }
//
//    public enum ConnectionAction {
//        ADD,
//        REMOVE;
//    }

//    // Pointers used for as nodes warning types
//    public static final int NODE_WARNING_NONE = 0;
//    public static final int NODE_WARNING_OVERLAP = 1;
//    public static final int NODE_WARNING_NEGATIVE_Y = 2;
//    public static final int NODE_WARNING_OVERLAP_Y = 3;

    // All the nodes pointers, these match the ones created by AutoDrive Mod
    public double x;
    public double y;
    public double z;
    public int id;
    public ArrayList<MapNode> incoming;
    public ArrayList<MapNode> outgoing;
    public int flag;
    private MapMarker mapMarker;

    // used by editor only!
    private final ArrayList<Connection> connectionList;
    private SelectorInterface nodeSelector;
    private boolean isSelected;
    private boolean hasWarning;
    private final ArrayList<MapNode> warningNodes;
    private NodeWarning warningType;
    private boolean showNodeSelectionPreview;
    private boolean showNodeHiddenPreview;
    private boolean showNodeFlagChangePreview;
    private boolean isNodeHidden;
    private final ArrayList<MapNode> ignoreDrawingConnectionsList;
    private final ArrayList<MapNode> hiddenConnectionsList;
    private final ArrayList<MapNode> previewHiddenConnectionsList;
    public boolean scheduledToBeDeleted;

    public MapNode(int id, double x, double y, double z, int flag, boolean isSelected) {

        // Variables match the ones generated by AutoDrive Mod
        this.id = id;
        this.x = roundUpDoubleToDecimalPlaces(x, 3);
        this.y = roundUpDoubleToDecimalPlaces(y, 3);
        this.z = roundUpDoubleToDecimalPlaces(z, 3);
        this.incoming = new ArrayList<>();
        this.outgoing = new ArrayList<>();
        this.flag = flag;

        // set the editor only variables
        this.connectionList = new ArrayList<>();
        this.nodeSelector = null; // Default to no selector, is assigned in creation functions
        this.mapMarker = null;
        this.isSelected = isSelected;
        this.hasWarning = false;
        this.warningNodes = new ArrayList<>();
        this.warningType = NodeWarning.NODE_WARNING_NONE;
        this.scheduledToBeDeleted = false;
        this.showNodeSelectionPreview = false;  // used by SelectionManager
        this.showNodeHiddenPreview = false;
        this.previewHiddenConnectionsList = new ArrayList<>();
        this.showNodeFlagChangePreview = false;
        this.isNodeHidden = false;
        this.ignoreDrawingConnectionsList = new ArrayList<>();
        this.hiddenConnectionsList = new ArrayList<>();


    }

    public static MapNode createMapNode(int id, double x, double y, double z, int flag) {
        double xPos = limitDoubleToDecimalPlaces(x,3, RoundingMode.HALF_UP);
        double yPos = limitDoubleToDecimalPlaces(y,3, RoundingMode.HALF_UP);
        double zPos = limitDoubleToDecimalPlaces(z,3, RoundingMode.HALF_UP);
        MapNode newNode = new MapNode(id, xPos, yPos, zPos, flag, false);
        newNode.nodeSelector = MOVE_SELECTOR;
        return newNode;
    }

    public static MapNode createMapNode(int id, double x, double y, double z, int flag, boolean isSelected) {
        double xPos = limitDoubleToDecimalPlaces(x,3, RoundingMode.HALF_UP);
        double yPos = limitDoubleToDecimalPlaces(y,3, RoundingMode.HALF_UP);
        double zPos = limitDoubleToDecimalPlaces(z,3, RoundingMode.HALF_UP);
        MapNode newNode = new MapNode(id, xPos, yPos, zPos, flag, false);
        newNode.nodeSelector = MOVE_SELECTOR;
        return newNode;
    }

    public static MapNode createNewNetworkNode(double x, double y, double z, int flag, boolean isSelected) {
        MapNode createdNode = createMapNode(RoadMap.networkNodesList.size() + 1, x, y, z, flag, isSelected);
        createdNode.nodeSelector = MOVE_SELECTOR;
        RoadMap.networkNodesList.add(createdNode);
        checkNodeOverlap(createdNode);
        return createdNode;
    }

    public static MapNode createControlNode(double x, double z) {
        MapNode controlNode = createMapNode(-99, x, 0, z, NODE_FLAG_CONTROL_NODE);
        controlNode.nodeSelector = MOVE_SELECTOR;
        return controlNode;
    }

    public static MapNode createRotationNode(double x, double z) {
        MapNode rotationNode = new MapNode(-98, x, 0, z, NODE_FLAG_ROTATION_NODE, false);
        rotationNode.nodeSelector = ROTATION_SELECTOR;
        return rotationNode;
    }

    public static MapNode createSpecialNode(double x, double z) {
        MapNode specialNode = new MapNode(-98, x, 0, z, NODE_FLAG_SPECIAL_NODE, false);
        specialNode.nodeSelector = RADIUS_SELECTOR;
        return specialNode;
    }


    public void createMapMarker(String newName, String newGroup) {
        if (bDebugLogMarkerInfo) LOG.info("Creating Map Marker for Node ID {} ( Name = {}, Group = {} )", this.id, newName, newGroup);
        this.mapMarker = new MapMarker(newName, newGroup, null, null);
    }

    public void createMapMarker(String newName, String newGroup, int id, List<String> newParkedVehiclesList) {
        if (bDebugLogMarkerInfo) LOG.info("Creating Map Marker for Node ID {} ( Name = {}, Group = {}, ParkingDestinationVehicleID = {} )", this.id, newName, newGroup, newParkedVehiclesList);
        this.mapMarker = new MapMarker(newName, newGroup, id, newParkedVehiclesList);
    }

    public void removeMapMarker() {
        if (bDebugLogMarkerInfo) LOG.info("Removing Map Marker from Node ID {}", this.id);
        this.mapMarker = null;
    }



    public void clearWarningNodes() {
        this.warningNodes.clear();
        this.hasWarning = false;
        this.warningType = NodeWarning.NODE_WARNING_NONE;
    }

    //
    // removeConnection: Currently only used by the SnapShot class
    //
    public static void removeConnection(Connection connection) {
        // Can these function variables be converted to inline, absolutely,
        // but they make it easier to read and debug while the new connection
        // system is still be implemented
        // TODO: Inline these variables once the new connection system is complete and stable
        ArrayList<MapNode> sourceIncoming = connection.getStartNode().incoming;
        ArrayList<MapNode> sourceOutgoing = connection.getStartNode().outgoing;
        ArrayList<MapNode> targetIncoming = connection.getEndNode().incoming;
        ArrayList<MapNode> targetOutgoing = connection.getEndNode().outgoing;
        switch (connection.getConnectionType()) {
            case CROSSED_REGULAR:
                sourceOutgoing.remove(connection.getEndNode());
                targetIncoming.remove(connection.getStartNode());
                targetOutgoing.remove(connection.getStartNode());
                break;
            case CROSSED_REVERSE:
                sourceOutgoing.remove(connection.getEndNode());
                targetOutgoing.remove(connection.getStartNode());
                sourceIncoming.remove(connection.getEndNode());
                break;
            case DUAL:
                sourceOutgoing.remove(connection.getEndNode());
                targetIncoming.remove(connection.getStartNode());
                sourceIncoming.remove(connection.getEndNode());
                targetOutgoing.remove(connection.getStartNode());
                break;
            case REVERSE:
                sourceOutgoing.remove(connection.getEndNode());
                break;
            case SUBPRIO:
            case REGULAR:
                sourceOutgoing.remove(connection.getEndNode());
                targetIncoming.remove(connection.getStartNode());
                break;
            default:
                LOG.info("## MapNode.removeConnection() ## WARNING!!!! unexpected connection type: {}", connection.getConnectionType());
        }
    }

    //
    // addConnection: Currently only used by the SnapShot class
    //
    public static void addConnection(Connection connection) {
        // Can these function variables be converted to inline, absolutely,
        // but they make it easier to read and debug while the new connection
        // system is still be implemented
        // TODO: Inline these variables once the new connection system is complete and stable
        ArrayList<MapNode> sourceIncoming = connection.getStartNode().incoming;
        ArrayList<MapNode> sourceOutgoing = connection.getStartNode().outgoing;
        ArrayList<MapNode> targetIncoming = connection.getEndNode().incoming;
        ArrayList<MapNode> targetOutgoing = connection.getEndNode().outgoing;
        switch (connection.getConnectionType()) {
            case DUAL:
                sourceOutgoing.add(connection.getEndNode());
                targetIncoming.add(connection.getStartNode());
                sourceIncoming.add(connection.getEndNode());
                targetOutgoing.add(connection.getStartNode());
                break;
            case REVERSE:
            case CROSSED_REVERSE:
                sourceOutgoing.add(connection.getEndNode());
                break;
            case SUBPRIO:
            case REGULAR:
            case CROSSED_REGULAR:
                sourceOutgoing.add(connection.getEndNode());
                targetIncoming.add(connection.getStartNode());
                break;
            default:
                LOG.info("## MapNode.addConnection() ## WARNING!!!! unexpected connection type: {}", connection.getConnectionType());
        }
    }

//    public ConnectionResult addConnectionTo(MapNode mapNode, Connection.ConnectionType connectionType) {
//        switch (connectionType) {
//            case DUAL:
//                if (isDual(this, mapNode)) {
//                    this.outgoing.remove(mapNode);
//                    mapNode.incoming.remove(this);
//                    mapNode.outgoing.remove(this);
//                    this.incoming.remove(mapNode);
//
//                    return ConnectionResult.REMOVED;
//                } else if (isReverse(this, mapNode)) {
//                    mapNode.outgoing.add(this);
//                    mapNode.incoming.add(this);
//                    this.incoming.add(mapNode);
//                    return ConnectionResult.MODIFIED;
//                } else if (isRegular(this, mapNode)) {
//                    mapNode.outgoing.add(this);
//                    this.incoming.add(mapNode);
//                    return ConnectionResult.MODIFIED;
//                }
//                break;
//            case REVERSE:
//                if (isDual(this, mapNode)) {
//                    mapNode.incoming.remove(this);
//                    mapNode.outgoing.remove(this);
//                    this.incoming.remove(mapNode);
//                    return ConnectionResult.MODIFIED;
//                } else if (isReverse(this, mapNode)) {
//                    this.outgoing.remove(mapNode);
//                    return ConnectionResult.REMOVED;
//                } else if (isCrossedRegular(this, mapNode)) {
//
//                } else if (isRegular(this,  mapNode)) {
//                    mapNode.incoming.remove(this);
//                    return ConnectionResult.MODIFIED;
//                }
//                break;
//            case SUBPRIO:
//            case REGULAR:
//            case CROSSED_REGULAR:
//            case CROSSED_REVERSE:
//                if (this.outgoing.contains(mapNode)) {
//                    return false;
//                }
//                this.outgoing.add(mapNode);
//                mapNode.incoming.add(this);
//                break;
//        }
//        if (this.outgoing.contains(mapNode)) {
//            return false;
//        }
//        this.outgoing.add(mapNode);
//        mapNode.incoming.add(this);
//        return true;
//    }

    //
    // Getters
    //

    public boolean canDelete() {
        if (this.isControlNode()) {
            if (bDebugLogCurveInfo) LOG.info("Cannot delete control node");
            return false;
        } else if (this.isRotationNode()) {
            if (bDebugLogCurveInfo) LOG.info("Cannot delete rotation node");
            return false;
        } else if (this.isNodeHidden && !bSelectHidden) {
            if (bDebugLogCurveInfo) LOG.info("Cannot delete hidden node");
            return false;
        }else if (curveManager.isCurvePreviewCreated()) {
            if (curveManager.isAnchorNode(this)) {
                if (bDebugLogCurveInfo) LOG.info("Cannot delete control point of curve until it is confirmed or cancelled");
                return false;
            }
        }
        return true;
    }

    public boolean hasMapMarker() {
        return this.mapMarker != null;
    }
    public boolean hasWarning() { return this.hasWarning; }
    public boolean hasVehicleParking() { return !this.getParkedVehiclesList().isEmpty(); }
    public void clearWarning() {
        this.hasWarning = false;
        this.warningType = NodeWarning.NODE_WARNING_NONE;
    }
    public boolean isConnectionHidden(MapNode outNode) { return this.hiddenConnectionsList.contains(outNode); }
    public boolean isMapNode() { return !this.isControlNode() && !this.isRotationNode() && !this.isSpecialNode(); }
    public boolean isControlNode() { return this.flag == NODE_FLAG_CONTROL_NODE; }
    public boolean isRotationNode() { return this.flag == NODE_FLAG_ROTATION_NODE; }
    public boolean isSpecialNode() { return this.flag == NODE_FLAG_SPECIAL_NODE; }

    public boolean isSelected() { return this.isSelected; }
    public boolean isNodeHidden() { return this.isNodeHidden; }
    public boolean isSelectable() { return !this.isNodeHidden || bSelectHidden; }
    @SuppressWarnings("BooleanMethodIsAlwaysInverted")
    public boolean isScheduledToBeDeleted() { return this.scheduledToBeDeleted; }
    public boolean isParkDestination() {return ( this.mapMarker != null && this.mapMarker.parkedVehiclesList !=null ); }
    public double getX() { return x; }
    public double getY() { return y; }
    public double getZ() { return z; }
    public int getID() { return id; }
    public int getFlag() { return flag; }
    /**
     * Returns a Point2D.Double() that contains the MapNodes x and z position.
     */
    public Point2D.Double getWorldPosition2D() { return new Point2D.Double(this.x, this.z); }
    public Point getScreenPosition2D() { return worldPosToScreenPos(this.x, this.z); }
    public ArrayList<Connection> getConnectionList() { return this.connectionList; }
    public ArrayList<MapNode> getWarningNodes() { return this.warningNodes; }
    public List<String> getParkedVehiclesList() { return this.mapMarker.parkedVehiclesList; }
    public ArrayList<MapNode> getIgnoreDrawingConnectionsList() { return this.ignoreDrawingConnectionsList; }
    public ArrayList<MapNode> getHiddenConnectionsList() { return this.hiddenConnectionsList; }
    public String getMarkerName() { return (this.hasMapMarker()) ? this.mapMarker.name : ""; }
    public String getMarkerGroup() { return (this.hasMapMarker()) ? this.mapMarker.group : ""; }
    public NodeWarning getWarningType() { return this.warningType; }
    public boolean getPreviewNodeSelectionChange() { return this.showNodeSelectionPreview; }
    public boolean getPreviewNodeHiddenChange() { return this.showNodeHiddenPreview; }
    public boolean getPreviewNodeFlagChange() { return this.showNodeFlagChangePreview; }
    public ArrayList<MapNode> getPreviewConnectionHiddenList() { return this.previewHiddenConnectionsList; }
    public int getParkingID() { return this.mapMarker.mapMarkerID; }

    public SelectorInterface getNodeSelector() {
        return (this.nodeSelector != null) ? this.nodeSelector : MOVE_SELECTOR;
    }



    //
    // Setters
    //


    public void setX(double x) { this.x = x; }
    public void setY(double y) { this.y = y; }
    public void setZ(double z) { this.z = z; }

    public void setMarkerName(String markerName) {
        this.mapMarker.name = markerName;
    }
    public void setMarkerGroup(String markerGroup) {
        this.mapMarker.group = markerGroup;
    }
    public void setParkedVehiclesList(List<String> parkedVehiclesList) {
        this.mapMarker.parkedVehiclesList = parkedVehiclesList;
    }
    public void setHasWarning(boolean hasWarning, NodeWarning warningType) {
        this.hasWarning = hasWarning;
        this.warningType = warningType;
    }
    public void setWarningType(NodeWarning warningType) { this.warningType = warningType; }
    public void setSelected(boolean selected) {
        this.isSelected = selected;
    }
    public void setPreviewNodeSelectionChange(boolean showNodeSelectionPreview) { this.showNodeSelectionPreview = showNodeSelectionPreview; }
    public void setPreviewNodeHiddenChange(boolean showNodeHiddenPreview) { this.showNodeHiddenPreview = showNodeHiddenPreview; }
    public void setPreviewNodeFlagChange(boolean showFlagChangePreview) { this.showNodeFlagChangePreview = showFlagChangePreview; }
    //public void setPreviewConnectionHidden(boolean showConnectionHiddenPreview) { this.showConnectionHiddenPreview = showConnectionHiddenPreview; }
    public void setNodeHidden(boolean isVisible) { this.isNodeHidden = isVisible; }
    public void setScheduledToBeDeleted(boolean scheduledToBeDeleted) {
        this.scheduledToBeDeleted = scheduledToBeDeleted;
    }

//    public void addConnection(Connection connection) {
//        this.connectionList.add(connection);
//    }

    @SuppressWarnings("UnusedReturnValue")
    public boolean addHiddenConnection(MapNode mapNode) {
        if (this.outgoing.contains(mapNode)) {
            this.hiddenConnectionsList.add(mapNode);
            return true;
        }
        return false;
    }
    @SuppressWarnings("UnusedReturnValue")
    public boolean removeHiddenConnection(MapNode mapNode) {
        if (this.hiddenConnectionsList.contains(mapNode)) {
            return this.hiddenConnectionsList.remove(mapNode);
        }
        return false;
    }

    public static class MapNodeStore {
        private final MapNode mapNode;
        private final int mapNodeIDBackup;
        private final ArrayList<MapNode> incomingBackup;
        private final ArrayList<MapNode> outgoingBackup;

        public MapNodeStore(MapNode node) {
            this.mapNode = node;
            this.mapNodeIDBackup = node.id;
            this.incomingBackup = new ArrayList<>();
            this.outgoingBackup = new ArrayList<>();
            backupConnections();
        }

        public MapNode getMapNode() {
            if (this.hasChangedID()) this.resetID();
            return this.mapNode;
        }

        public void resetID() {
            this.mapNode.id = this.mapNodeIDBackup;
        }

        public boolean hasChangedID() {
            return this.mapNode.id != this.mapNodeIDBackup;
        }

        public void clearConnections() {
            clearIncoming();
            clearOutgoing();
        }

        public void clearIncoming() { this.mapNode.incoming.clear(); }

        public void clearOutgoing() { this.mapNode.outgoing.clear(); }

        public void backupConnections() {
            copyList(this.mapNode.incoming, this.incomingBackup);
            copyList(this.mapNode.outgoing, this.outgoingBackup);
        }

        public void restoreConnections() {
            copyList(this.incomingBackup, this.mapNode.incoming);
            copyList(this.outgoingBackup, this.mapNode.outgoing);
        }

        @SuppressWarnings("unused")
        public void backupIncoming() { copyList(this.mapNode.incoming, this.incomingBackup); }

        @SuppressWarnings("unused")
        public void restoreIncoming() { copyList(this.incomingBackup, this.mapNode.incoming); }

        @SuppressWarnings("unused")
        public void backupOutgoing() { copyList(this.mapNode.outgoing, this.outgoingBackup); }

        @SuppressWarnings("unused")
        public void restoreOutgoing() { copyList(this.outgoingBackup, this.mapNode.outgoing); }

        private void copyList(ArrayList<MapNode> from, ArrayList<MapNode> to) {
            to.clear();
            for (int i = 0; i <= from.size() - 1 ; i++) {
                MapNode mapNode = from.get(i);
                if (!to.contains(mapNode)) to.add(mapNode);
            }
        }
    }

    @Override
    public int compareTo(MapNode other) {
        // Implements comparison logic for MapNode objects
        int xComparison = Double.compare(this.x, other.x);
        if (xComparison != 0) return xComparison;
        return Double.compare(this.z, other.z);
    }

    @Override
    public String toString() {
        return String.format("MapNode@%s {id=%d}", Integer.toHexString(hashCode()), id);
    }

    private static class MapMarker {
        public String name;
        public String group;
        // EXPERIMENTAL CODE
        public Integer mapMarkerID;
        public List<String> parkedVehiclesList;
        // END EXPERIMENTAL CODE

        public MapMarker (String name, String group, Integer mapMarkerID, List<String> parkedVehiclesList) {
            this.name = name;
            this.group = group;
            // EXPERIMENTAL CODE
            this.mapMarkerID = mapMarkerID;
            this.parkedVehiclesList = parkedVehiclesList;
            // END EXPERIMENTAL CODE
        }
    }
}
