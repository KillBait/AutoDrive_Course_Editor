package AutoDriveEditor.RoadNetwork;

import java.util.LinkedList;
import java.util.List;

import static AutoDriveEditor.GUI.MenuBuilder.bDebugLogMarkerInfo;
import static AutoDriveEditor.RoadNetwork.RoadMap.createMapNode;
import static AutoDriveEditor.Utils.LoggerUtils.LOG;
import static AutoDriveEditor.Utils.MathUtils.roundUpDoubleToDecimalPlaces;

@SuppressWarnings("unused")
public class MapNode {

    public static final int NODE_FLAG_STANDARD = 0;
    public static final int NODE_FLAG_SUBPRIO = 1;
    public static final int NODE_FLAG_TEMPORARY = 98;
    public static final int NODE_FLAG_CONTROL_POINT = 99;
    public static final int NODE_WARNING_NONE = 0;
    public static final int NODE_WARNING_OVERLAP = 1;
    public static final int NODE_WARNING_NEGATIVE_Y = 2;
    public static final int NODE_WARNING_OVERLAP_Y = 3;



    public int id;
    public double x, y, z;
    public LinkedList<MapNode> incoming;
    public LinkedList<MapNode> outgoing;
    public int flag;
    public MapMarker mapMarker;
    public boolean isSelected;
    public boolean isControlNode;
    public boolean hasWarning;
    public LinkedList<MapNode> warningNodes;
    public int warningType;
    public boolean scheduledToBeDeleted;
    public boolean isInSelectionArea;

    public MapNode(int id, double x, double y, double z, int flag, boolean isSelected, boolean isControlNode) {

        // Autodrive mod created

        this.id = id;
        this.x = roundUpDoubleToDecimalPlaces(x, 3);
        this.y = roundUpDoubleToDecimalPlaces(y, 3);
        this.z = roundUpDoubleToDecimalPlaces(z, 3);
        this.incoming = new LinkedList<>();
        this.outgoing = new LinkedList<>();
        this.flag = flag;

        // editor use only!

        this.mapMarker = null;
        this.isSelected = isSelected;
        this.isControlNode = isControlNode;
        this.hasWarning = false;
        this.warningNodes = new LinkedList<>();
        this.warningType = NODE_WARNING_NONE;
        this.scheduledToBeDeleted = false;
        this.isInSelectionArea = false;
    }

    public void createMapMarker(String newName, String newGroup) {
        if (bDebugLogMarkerInfo) LOG.info("Creating Map Marker for Node ID {} ( Name = {}, Group = {} )", this.id, newName, newGroup);
        this.mapMarker = new MapMarker(newName, newGroup, null);
    }
    public void createMapMarker(String newName, String newGroup, List<Integer> newParkedVehiclesList) {
        if (bDebugLogMarkerInfo) LOG.info("Creating Map Marker for Node ID {} ( Name = {}, Group = {}, ParkingDestinationVehicleID = {} )", this.id, newName, newGroup, newParkedVehiclesList);
        this.mapMarker = new MapMarker(newName, newGroup, newParkedVehiclesList);
    }

    public void removeMapMarker() {
        if (bDebugLogMarkerInfo) LOG.info("Removing Map Marker from Node ID {}", this.id);
        this.mapMarker = null;
    }

    public void clearWarning() {
        this.hasWarning = false;
        this.warningType = NODE_WARNING_NONE;
    }

    public boolean hasMapMarker() {
        return this.mapMarker != null;
    }
    public boolean isParkDestination() {return ( this.mapMarker != null && this.mapMarker.parkedVehiclesList !=null ); }

    public boolean isControlNode() {
        return this.isControlNode;
    }

    public boolean isSelected() {
        return this.isSelected;
    }

    public String getMarkerName() {
        return this.mapMarker.name;
    }

    public String getMarkerGroup() {
        return this.mapMarker.group;
    }
    public List<Integer> getParkedVehiclesList() {
        return this.mapMarker.parkedVehiclesList;
    }
    public void setMarkerName(String markerName) {
        this.mapMarker.name = markerName;
    }
    public void setMarkerGroup(String markerGroup) {
        this.mapMarker.group = markerGroup;
    }
    public void setParkedVehiclesList(List<Integer> parkedVehiclesList) {
        this.mapMarker.parkedVehiclesList = parkedVehiclesList;
    }
    public void setControlNode(boolean isControlNode) {
        this.isControlNode = isControlNode;
    }
    public void setSelected(boolean selected) {
        this.isSelected = selected;
    }

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
    public MapNode getCopyOfNode(MapNode oldNode) {
        MapNode newNode = createMapNode(oldNode.id, oldNode.x, oldNode.y, oldNode.z, oldNode.flag, oldNode.isSelected, oldNode.isControlNode);
        newNode.incoming = new LinkedList<>();
        newNode.incoming.addAll(oldNode.incoming);
        newNode.outgoing = new LinkedList<>();
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
        public List<Integer> parkedVehiclesList;

        public MapMarker (String name, String group, List<Integer> parkedVehiclesList) {
            this.name = name;
            this.group = group;
            this.parkedVehiclesList = parkedVehiclesList;
        }
    }
}
