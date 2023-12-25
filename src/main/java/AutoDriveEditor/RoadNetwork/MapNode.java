package AutoDriveEditor.RoadNetwork;

import java.util.ArrayList;
import java.util.List;

import static AutoDriveEditor.GUI.Buttons.ConnectionSelectBaseButton.getIgnore;
import static AutoDriveEditor.GUI.Menus.DebugMenu.Logging.LogMarkerInfoMenu.bDebugLogMarkerInfo;
import static AutoDriveEditor.RoadNetwork.RoadMap.createMapNode;
import static AutoDriveEditor.Utils.LoggerUtils.LOG;
import static AutoDriveEditor.Utils.MathUtils.roundUpDoubleToDecimalPlaces;
import static AutoDriveEditor.XMLConfig.EditorXML.bSelectHidden;

@SuppressWarnings("unused")
public class MapNode  implements Comparable<MapNode> {

    public static final int NODE_FLAG_REGULAR = 0;
    public static final int NODE_FLAG_SUBPRIO = 1;
    public static final int NODE_FLAG_TEMPORARY = 98;
    public static final int NODE_FLAG_CONTROL_POINT = 99;
    public static final int NODE_WARNING_NONE = 0;
    public static final int NODE_WARNING_OVERLAP = 1;
    public static final int NODE_WARNING_NEGATIVE_Y = 2;
    public static final int NODE_WARNING_OVERLAP_Y = 3;

    public int id;
    public double x, y, z;
    public ArrayList<MapNode> incoming;
    public ArrayList<MapNode> outgoing;
    public int flag;
    public MapMarker mapMarker;

    private boolean isSelected;
    private boolean isControlNode;
    private boolean hasWarning;
    private final ArrayList<MapNode> warningNodes;
    private int warningType;
    private boolean scheduledToBeDeleted;
    private boolean showNodeSelectionPreview;
    private boolean showNodeHiddenPreview;
    private boolean showNodeFlagChangePreview;
    private boolean isNodeHidden;
    private final ArrayList<MapNode> ignoreDrawingConnectionsList;
    private final ArrayList<MapNode> hiddenConnectionsList;


    public MapNode(int id, double x, double y, double z, int flag, boolean isSelected, boolean isControlNode) {

        // Autodrive mod created

        this.id = id;
        this.x = roundUpDoubleToDecimalPlaces(x, 3);
        this.y = roundUpDoubleToDecimalPlaces(y, 3);
        this.z = roundUpDoubleToDecimalPlaces(z, 3);
        this.incoming = new ArrayList<>();
        this.outgoing = new ArrayList<>();
        this.flag = flag;

        // editor use only!

        this.mapMarker = null;
        this.isSelected = isSelected;
        this.isControlNode = isControlNode;
        this.hasWarning = false;
        this.warningNodes = new ArrayList<>();
        this.warningType = NODE_WARNING_NONE;
        this.scheduledToBeDeleted = false;
        this.showNodeSelectionPreview = false;  // used by SelectionManager
        this.showNodeHiddenPreview = false;  // used by SelectionManager
        this.showNodeFlagChangePreview = false;
        this.isNodeHidden = false;
        this.ignoreDrawingConnectionsList = new ArrayList<>();
        this.hiddenConnectionsList = new ArrayList<>();
    }

    public void createMapMarker(String newName, String newGroup) {
        if (bDebugLogMarkerInfo) LOG.info("Creating Map Marker for Node ID {} ( Name = {}, Group = {} )", this.id, newName, newGroup);
        this.mapMarker = new MapMarker(newName, newGroup, null, null);
    }

    // EXPERIMENTAL CODE
    public void createMapMarker(String newName, String newGroup, int id, List<Integer> newParkedVehiclesList) {
        if (bDebugLogMarkerInfo) LOG.info("Creating Map Marker for Node ID {} ( Name = {}, Group = {}, ParkingDestinationVehicleID = {} )", this.id, newName, newGroup, newParkedVehiclesList);
        this.mapMarker = new MapMarker(newName, newGroup, id, newParkedVehiclesList);
    }
    // END EXPERIMENTAL CODE

    public void removeMapMarker() {
        if (bDebugLogMarkerInfo) LOG.info("Removing Map Marker from Node ID {}", this.id);
        this.mapMarker = null;
    }

    // EXPERIMENTAL CODE
    public boolean isParkDestination() {return ( this.mapMarker != null && this.mapMarker.parkedVehiclesList !=null ); }
    public int getParkingID() { return this.mapMarker.mapMarkerID; }
    // END EXPERIMENTAL CODE

    public void clearWarningNodes() {
        this.warningNodes.clear();
        this.hasWarning = false;
        this.warningType = NODE_WARNING_NONE;
    }

    //
    //
    //

    public boolean hasMapMarker() {
        return this.mapMarker != null;
    }
    public boolean hasWarning() { return this.hasWarning; }

    public boolean hasVehicleParking() { return this.getParkedVehiclesList().size() > 0; }
    @SuppressWarnings("BooleanMethodIsAlwaysInverted")
    public boolean isScheduledToBeDeleted() { return this.scheduledToBeDeleted; }
    public boolean isControlNode() { return this.isControlNode; }
    public boolean isSelected() { return this.isSelected; }
    public boolean isNodeHidden() { return this.isNodeHidden; }
    public boolean isSelectable() { return bSelectHidden || !this.isNodeHidden; }

    public boolean isGH() {
        if (getIgnore()) {
            return true;
        } else {
            return isSelectable();
        }
    }

    //
    // Getters
    //

    public ArrayList<MapNode> getWarningNodes() { return this.warningNodes; }
    public List<Integer> getParkedVehiclesList() { return this.mapMarker.parkedVehiclesList; }
    public ArrayList<MapNode> getIgnoreDrawingConnectionsList() { return this.ignoreDrawingConnectionsList; }
    public ArrayList<MapNode> getHiddenConnectionsList() { return this.hiddenConnectionsList; }

    public String getMarkerName() {
        return this.mapMarker.name;
    }
    public String getMarkerGroup() {
        return this.mapMarker.group;
    }
    public int getWarningType() { return this.warningType; }
    public boolean getPreviewNodeSelectionChange() { return this.showNodeSelectionPreview; }
    public boolean getPreviewNodeHiddenChange() { return this.showNodeHiddenPreview; }
    public boolean getPreviewNodeFlagChange() { return this.showNodeFlagChangePreview; }


    //
    // Setters
    //

    public void setMarkerName(String markerName) {
        this.mapMarker.name = markerName;
    }
    public void setMarkerGroup(String markerGroup) {
        this.mapMarker.group = markerGroup;
    }
    public void setParkedVehiclesList(List<Integer> parkedVehiclesList) {
        this.mapMarker.parkedVehiclesList = parkedVehiclesList;
    }
    public void setHasWarning(boolean hasWarning, int warningType) {
        this.hasWarning = hasWarning;
        this.warningType = warningType;
    }
    //public void setWarningType(int warningType) { this.warningType = warningType; }
    public void setScheduledToBeDeleted(boolean toDelete) { this.scheduledToBeDeleted = toDelete; }
    public void setIsControlNode(boolean isControlNode) {
        this.isControlNode = isControlNode;
    }
    public void setSelected(boolean selected) {
        this.isSelected = selected;
    }
    public void setPreviewNodeSelectionChange(boolean showNodeSelectionPreview) { this.showNodeSelectionPreview = showNodeSelectionPreview; }
    public void setPreviewNodeHiddenChange(boolean showNodeHiddenPreview) { this.showNodeHiddenPreview = showNodeHiddenPreview; }
    public void setPreviewNodeFlagChange(boolean showFlagChangePreview) { this.showNodeFlagChangePreview = showFlagChangePreview; }
    public void setNodeHidden(boolean isVisible) { this.isNodeHidden = isVisible; }
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

    public boolean isConnectionHidden(MapNode outNode) { return this.hiddenConnectionsList.contains(outNode); }



    //
    // Misc Functions - for debug purposes only!!
    //

    public static int getConnectionsFor(MapNode mapNode) {
        int result = 0;
        for (MapNode outgoing : mapNode.outgoing) {
            //Point2D outPos = worldPosToScreenPos(outgoing.x, outgoing.z);
            if (RoadMap.isDual(mapNode, outgoing)) {
                result = 3;
            } else if (RoadMap.isReverse(mapNode, outgoing)) {
                result = 4;
            } else {
                if (mapNode.flag == 1) {
                    result = 2;
                } else {
                    result = 1;

                }
            }
        }

        if (result == 0) {
            if (mapNode.incoming.size() > 0) {
                for (MapNode incoming : mapNode.incoming) {
                    //Point2D outPos = worldPosToScreenPos(outgoing.x, outgoing.z);
                    if (RoadMap.isDual(mapNode, incoming)) {
                        result = 3;
                    } else if (mapNode.flag == 1) {
                        result = 2;
                    } else {
                        result = 1;
                    }
                }
            } else {
                for (MapNode nodes : RoadMap.networkNodesList) {
                    if (nodes.outgoing.contains(mapNode)) {
                        result = 4;
                        break;
                    }
                }
            }
        }
        return result;
    }

    @Override
    public int compareTo(MapNode other) {
        // Implements comparison logic for MapNode objects
        int xComparison = Double.compare(this.x, other.x);
        if (xComparison != 0) return xComparison;
        return Double.compare(this.z, other.z);
    }

    public MapNode getCopyOfNode(MapNode oldNode) {
        MapNode newNode = createMapNode(oldNode.id, oldNode.x, oldNode.y, oldNode.z, oldNode.flag, oldNode.isSelected, oldNode.isControlNode);
        newNode.incoming = new ArrayList<>();
        newNode.incoming.addAll(oldNode.incoming);
        newNode.outgoing = new ArrayList<>();
        newNode.outgoing.addAll(oldNode.outgoing);
        if (oldNode.hasMapMarker()) {
            newNode.createMapMarker(oldNode.getMarkerName(), oldNode.getMarkerGroup());
        }
        newNode.hasWarning = oldNode.hasWarning;
        newNode.warningNodes.addAll(oldNode.warningNodes);
        newNode.warningType = oldNode.warningType;
        newNode.scheduledToBeDeleted = oldNode.scheduledToBeDeleted;
        return newNode;
    }



    private static class MapMarker {
        public String name;
        public String group;
        // EXPERIMENTAL CODE
        public Integer mapMarkerID;
        public List<Integer> parkedVehiclesList;
        // END EXPERIMENTAL CODE

        public MapMarker (String name, String group, Integer mapMarkerID, List<Integer> parkedVehiclesList) {
            this.name = name;
            this.group = group;
            // EXPERIMENTAL CODE
            this.mapMarkerID = mapMarkerID;
            this.parkedVehiclesList = parkedVehiclesList;
            // END EXPERIMENTAL CODE
        }
    }
}
