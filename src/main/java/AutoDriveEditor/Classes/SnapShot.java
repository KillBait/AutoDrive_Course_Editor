package AutoDriveEditor.Classes;

import AutoDriveEditor.Classes.Util_Classes.ExceptionUtils;
import AutoDriveEditor.Managers.MultiSelectManager;
import AutoDriveEditor.RoadNetwork.Connection;
import AutoDriveEditor.RoadNetwork.MapNode;
import AutoDriveEditor.RoadNetwork.RoadMap;

import java.awt.geom.Point2D;
import java.util.*;

import static AutoDriveEditor.AutoDriveEditor.getMapPanel;
import static AutoDriveEditor.Classes.Util_Classes.LoggerUtils.LOG;
import static AutoDriveEditor.Classes.Util_Classes.MathUtils.roundUpDoubleToDecimalPlaces;
import static AutoDriveEditor.GUI.MapPanel.getYValueFromHeightMap;
import static AutoDriveEditor.GUI.MapPanel.screenPosToWorldPos;
import static AutoDriveEditor.GUI.Menus.DebugMenu.Logging.LogSnapShotInfoMenu.bDebugLogSnapShotInfo;
import static AutoDriveEditor.GUI.Menus.DebugMenu.Logging.LogUndoRedoMenu.bDebugLogUndoRedoInfo;
import static AutoDriveEditor.RoadNetwork.RoadMap.getIndexPositionOfNode;
import static AutoDriveEditor.RoadNetwork.RoadMap.networkNodesList;

/**
 * This class is responsible for creating and managing a snapshots of selected nodes
 * in the road network. It stores the state of nodes and connections at a specific
 * time and can be used to createSetting new copies of the nodes and connections or restore
 * their original state
 */

@SuppressWarnings({"LoggingSimilarMessage", "unused"})
public class SnapShot {

    public enum SnapShotType {FULL, CONNECTION_ONLY, MANUAL}
    private final SnapShotType snapShotType;
    private final ArrayList<Connection> connectionList;
    private final ArrayList<Connection> otherIncoming;
    private final ArrayList<Connection> otherOutgoing;
    private final TreeMap<NodeBackup, Integer> originalNodeMap;
    private final Map<Integer, MapNode> newNodeMap;
    private final ArrayList<Connection> newNodeConnections;
    MultiSelectManager.SelectionAreaInfo nodeSelectionAreaInfo;

    /**
     * Construct a new SnapShot.
     * Initializes the lists for the nodes/connections and other required information.
     * <p><b>WARNING: The is will remove/re-add all connections to/from
     * the specified nodes</b></p>
     *
     * @param nodeList A LinkedList() of MapNode to be included in the snapshot.
     */
    public SnapShot(LinkedList<MapNode> nodeList) {
        this(new ArrayList<>(nodeList));
    }

    /**
     * Construct a new SnapShot.
     * Initializes the lists for the nodes/connections and other required information.
     * <p><b>WARNING: The is will remove/re-add all connections to/from
     * the specified nodes</b></p>
     *
     * @param nodeList A ArrayList() of MapNode to be included in the snapshot.
     */
    public SnapShot(ArrayList<MapNode> nodeList) {
        this.snapShotType = SnapShotType.FULL;
        this.connectionList = new ArrayList<>();
        this.otherIncoming = new ArrayList<>();
        this.otherOutgoing = new ArrayList<>();
        this.originalNodeMap = new TreeMap<>();
        this.newNodeMap = new HashMap<>();
        this.newNodeConnections = new ArrayList<>();
        if (bDebugLogSnapShotInfo || bDebugLogUndoRedoInfo) {
            LOG.info("## ************************************************");
            LOG.info("## SnapShot(List) ## Creating {} Snapshot", snapShotType);
            LOG.info("## ***********************************************");
        }
        // check if any of the selected nodes are un-deletable
        ArrayList<MapNode> validList = new ArrayList<>();
        for (MapNode mapNode : nodeList) {
            if (mapNode.canDelete()) {
                if (bDebugLogSnapShotInfo || bDebugLogUndoRedoInfo) LOG.info("## SnapShot ## Adding node ID: {} to SnapShot", mapNode.id);
                validList.add(mapNode);
            } else {
                if (bDebugLogSnapShotInfo || bDebugLogUndoRedoInfo) LOG.info("## SnapShot ## Skipping node ID: {} canDelete() is false", mapNode.id);
            }
        }
        this.nodeSelectionAreaInfo = MultiSelectManager.getSelectionBounds(validList);
        createSnapShot(validList);
    }

    /**
     * Construct a Connection only SnapShot.
     * Initializes the lists for the connections and other required information.
     * <p><b>WARNING: The is will remove/re-add <u>ONLY</u> the connections
     * between the start and end nodes specified in the constructor</b></p>
     *
     * @param startNode the starting noe of the connection.
     * @param endNode the ending node of the connection.
     * @param snapShotType the type of snapshot to be created.
     */
    public SnapShot(MapNode startNode, MapNode endNode, SnapShotType snapShotType) {
        this.snapShotType = snapShotType;
        this.connectionList = new ArrayList<>();
        this.otherIncoming = new ArrayList<>();
        this.otherOutgoing = new ArrayList<>();
        this.originalNodeMap = new TreeMap<>();
        this.newNodeMap = new HashMap<>();
        this.newNodeConnections = new ArrayList<>();
        if (bDebugLogSnapShotInfo || bDebugLogUndoRedoInfo) {
            LOG.info("## ****************************************************************");
            LOG.info("## SnapShot(startNode, endNode, SnapShotType) ## Creating {} Snapshot", snapShotType.toString());
            LOG.info("## ****************************************************************");
        }
        ArrayList<MapNode> nodeList = new ArrayList<>();
        nodeList.add(startNode);
        nodeList.add(endNode);
        createConnectionOnlySnapShot(nodeList);

    }

    /**
     * Construct a manual SnapShot.
     * Initializes the lists for the nodes/connections and other required information.
     * <p><b>WARNING: The is will remove/re-add <u>ONLY</u> the MapNodes/Connections
     * specified in the constructor arguments</b></p>
     *
     * @param generatedNodes A LinkedList() of MapNode to be included in the snapshot.
     * @param generatedConnections A ArrayList() of Connection to be included in the snapshot.
     * @param snapShotType The type of snapshot to be created.
     */
    public SnapShot(LinkedList<MapNode> generatedNodes, ArrayList<Connection> generatedConnections, SnapShotType snapShotType) {
        this(new ArrayList<>(generatedNodes), generatedConnections, SnapShotType.MANUAL);
    }

    /**
     * Construct a manual SnapShot.
     * Initializes the lists for the nodes/connections and other required information.
     * <p><b>WARNING: The is will remove/re-add <u>ONLY</u> the MapNodes/Connections
     * specified in the constructor arguments</b></p>
     *
     * @param generatedNodes A ArrayList() of MapNode to be included in the snapshot.
     * @param generatedConnections A ArrayList() of Connection to be included in the snapshot.
     * @param snapShotType The type of snapshot to be created.
     */
    public SnapShot(ArrayList<MapNode> generatedNodes, ArrayList<Connection> generatedConnections, SnapShotType snapShotType) {
        if (snapShotType != SnapShotType.MANUAL) {
            throw new IllegalArgumentException("SnapShot: this constructor only accepts SnapShotType.MANUAL");
        }
        this.snapShotType = SnapShotType.MANUAL;
        this.connectionList = new ArrayList<>();
        this.otherIncoming = new ArrayList<>();
        this.otherOutgoing = new ArrayList<>();
        this.originalNodeMap = new TreeMap<>();
        this.newNodeMap = new HashMap<>();
        this.newNodeConnections = new ArrayList<>();
        if (bDebugLogSnapShotInfo || bDebugLogUndoRedoInfo) {
            LOG.info("## ******************************************************************************************");
            LOG.info("## SnapShot(generatedNodes, generatedConnections, SnapShotType) ## Creating {} Snapshot", snapShotType.toString());
            LOG.info("## ******************************************************************************************");
        }
        createManualSnapShot(generatedNodes, generatedConnections);

    }

    /**
     * Construct a single MapNode SnapShot.
     * Initializes the lists for the nodes/connections and other required information.
     * <p><b>WARNING: The is will <u>ONLY</u> remove/re-add all connections to/from
     * the specified MapNode</b></p>
     *
     * @param node The mapNode for the snapshot.
     */
    public SnapShot(MapNode node) {
        this.snapShotType = SnapShotType.FULL;
        this.connectionList = new ArrayList<>();
        this.otherIncoming = new ArrayList<>();
        this.otherOutgoing = new ArrayList<>();
        this.originalNodeMap = new TreeMap<>();
        this.newNodeMap = new HashMap<>();
        this.newNodeConnections = new ArrayList<>();
        if (bDebugLogSnapShotInfo || bDebugLogUndoRedoInfo) LOG.info("## SnapShot(MapNode) ## Creating {} Snapshot", snapShotType.toString());
        Point2D.Double nodeLocation = new Point2D.Double(node.getX(), node.getZ());
        this.nodeSelectionAreaInfo = new MultiSelectManager.SelectionAreaInfo(nodeLocation, nodeLocation,
                new Point2D.Double(0, 0), nodeLocation);
        ArrayList<MapNode> nodeList = new ArrayList<>();
        nodeList.add(node);
        if (bDebugLogSnapShotInfo || bDebugLogUndoRedoInfo) {
            LOG.info("## *********************************************");
            LOG.info("## SnapShot(mapNode) ## Creating {} Snapshot", snapShotType.toString());
            LOG.info("## *********************************************");
        }
        createSnapShot(nodeList);
    }

    /**
     * Creates the actual snapshot of the given nodes.
     *
     * @param nodeList The list of MapNode to be included in the snapshot.
     */
    private void createSnapShot(ArrayList<MapNode> nodeList) {

        int snapShotID = 0;
        for (MapNode mapNode : nodeList) {
            NodeBackup backup = new NodeBackup(snapShotID++, mapNode);
            originalNodeMap.put(backup, mapNode.id);
            if (bDebugLogSnapShotInfo || bDebugLogUndoRedoInfo) LOG.info("## SnapShot.createSnapShot() ## Backing up node ID: {} ( SnapShotID {} ) [X: {}, Y: {}, Z: {}] , Flag: {} , Marker: {} , Group: {}", backup.getMapNodeID(), backup.getSnapShotID(), backup.getMapNodeX(), backup.getMapNodeY(), backup.getMapNodeZ(), backup.getFlag(), backup.getMarkerName(), backup.getMarkerGroup());
        }

        //Store all the outbound connections to the nodes in list
        for (MapNode mapNode : nodeList) {
            for (MapNode outNode : mapNode.outgoing) {
                Connection.ConnectionType type;
                type = RoadMap.getConnection(mapNode, outNode);
                if (type == Connection.ConnectionType.DUAL && findConnection(connectionList, outNode, mapNode, type)) {
                    if (bDebugLogSnapShotInfo || bDebugLogUndoRedoInfo) LOG.info("## SnapShot.createSnapShot() ## Skipping existing {} connection: {} -> {}", type, mapNode.id, outNode.id);
                    continue;
                }
                if (type != Connection.ConnectionType.UNKNOWN) {
                    Connection con = new Connection(mapNode, outNode, type);
                    if (nodeList.contains(outNode)) {
                        addConnection(con);
                        if (bDebugLogSnapShotInfo || bDebugLogUndoRedoInfo) LOG.info("## SnapShot.createSnapShot() ## Storing Connection: {} -> {} : Type: {}", con.getStartNodeID(), con.getEndNodeID(), con.getConnectionType());
                    } else {
                        addOtherOutgoing(con);
                        if (bDebugLogSnapShotInfo || bDebugLogUndoRedoInfo) LOG.info("## SnapShot.createSnapShot() ## Storing Other Out Connection: {} -> {} : Type: {}", con.getStartNodeID(), con.getEndNodeID(), con.getConnectionType());
                    }
                }
            }
        }

        //Store all the inbound connections to the nodes in list
        for (MapNode mapNode : networkNodesList) {
            for (MapNode out : mapNode.outgoing) {
                if (nodeList.contains(out) && !nodeList.contains(mapNode)) {
                    Connection.ConnectionType type;
                    type = RoadMap.getConnection(mapNode, out);
                    if (type == Connection.ConnectionType.DUAL) {
                        if (nodeList.contains(out)) {
                            if (bDebugLogSnapShotInfo || bDebugLogUndoRedoInfo) LOG.info("## SnapShot.createSnapShot() ## Skipping existing dual connection: {} -> {}", mapNode.id, out.id);
                            continue;
                        }
                    }
                    Connection con = new Connection(mapNode, out, type);
                    addOtherIncoming(con);
                    if (bDebugLogSnapShotInfo || bDebugLogUndoRedoInfo) LOG.info("## SnapShot.createSnapShot() ## Storing Other In Connection: {} -> {} : Type: {}", con.getStartNodeID(), con.getEndNodeID(), con.getConnectionType());
                }
            }
        }
    }

    private void createConnectionOnlySnapShot(ArrayList<MapNode> nodeList) {
        //Store all the outbound connections for the nodes in list
        for (MapNode mapNode : nodeList) {
            for (MapNode outNode : mapNode.outgoing) {
                Connection.ConnectionType type;
                type = RoadMap.getConnection(mapNode, outNode);
                if (type == Connection.ConnectionType.DUAL && findConnection(connectionList, outNode, mapNode, type)) {
                    if (bDebugLogSnapShotInfo || bDebugLogUndoRedoInfo) LOG.info("## SnapShot.createConnectionOnlySnapShot() ## Skipping existing {} connection in list: {} -> {}", type, mapNode.id, outNode.id);
                    continue;
                }
                if (type != Connection.ConnectionType.UNKNOWN) {
                    Connection con = new Connection(mapNode, outNode, type);
                    if (nodeList.contains(outNode)) {
                        addConnection(con);
                        if (bDebugLogSnapShotInfo || bDebugLogUndoRedoInfo) LOG.info("## SnapShot.createConnectionOnlySnapShot() ## Adding Connection: to list: {} -> {} : Type: {}", con.getStartNodeID(), con.getEndNodeID(), con.getConnectionType());
                    }
                }
            }
        }
    }

    private void createManualSnapShot(ArrayList<MapNode> nodeList, ArrayList<Connection> connectionList) {
        int snapShotID = 0;
        for (MapNode mapNode : nodeList) {
            NodeBackup backup = new NodeBackup(snapShotID++, mapNode);
            originalNodeMap.put(backup, mapNode.id);
            if (bDebugLogSnapShotInfo || bDebugLogUndoRedoInfo) LOG.info("## SnapShot.createManualSnapShot() ## Backing up node ID: {} ( SnapShotID {} ) [X: {}, Y: {}, Z: {}] , Flag: {} , Marker: {} , Group: {}", backup.getMapNodeID(), backup.getSnapShotID(), backup.getMapNodeX(), backup.getMapNodeY(), backup.getMapNodeZ(), backup.getFlag(), backup.getMarkerName(), backup.getMarkerGroup());
        }

        //Store all the connections in the specified list
        for (Connection con : connectionList) {
            if (con.getConnectionType() != Connection.ConnectionType.UNKNOWN) {
                addConnection(con);
                if (bDebugLogSnapShotInfo || bDebugLogUndoRedoInfo) LOG.info("## SnapShot.createManualSnapShot() ## Storing Connection: {} -> {} : Type: {}", con.getStartNodeID(), con.getEndNodeID(), con.getConnectionType());
            }
        }
    }

    /**
     * Removes the original connections from the road network.
     */
    public void removeOriginalConnections() {
        for (Connection connection : connectionList) {
            MapNode.removeConnection(connection);
        }
    }

    /**
     * Restores the original connections back to the road network.
     */
    public void restoreOriginalConnections() {
        for (Connection connection : connectionList) {
            MapNode.addConnection(connection);
        }
    }

    /**
     * Removes the original nodes and connections from the road network.
     */
    public void removeOriginalNodes() {
        for (Connection connection : otherOutgoing) {
            if (bDebugLogSnapShotInfo || bDebugLogUndoRedoInfo) LOG.info("## SnapShot.removeOtherOutgoing ## Removing connection: {} -> {} : Type: {}", connection.getStartNodeID(), connection.getEndNodeID(),connection.getConnectionType());
            MapNode.removeConnection(connection);
        }
        for (Connection connection : otherIncoming) {
            if (bDebugLogSnapShotInfo || bDebugLogUndoRedoInfo) LOG.info("## SnapShot.removeOtherIncoming ## Removing connection: {} -> {} : Type: {}", connection.getStartNodeID(), connection.getEndNodeID(),connection.getConnectionType());
            MapNode.removeConnection(connection);
        }
        for (Connection connection : connectionList) {
            if (bDebugLogSnapShotInfo || bDebugLogUndoRedoInfo) LOG.info("## SnapShot.removeConnections ## Removing connection: {} -> {} : Type: {}", connection.getStartNodeID(), connection.getEndNodeID(),connection.getConnectionType());
            MapNode.removeConnection(connection);
        }
        for (Map.Entry<NodeBackup, Integer> entry : originalNodeMap.entrySet()) {
            if (bDebugLogSnapShotInfo || bDebugLogUndoRedoInfo) {
                NodeBackup node = entry.getKey();
                LOG.info("## SnapShot.removeOriginalNodes() ## Removing node: {} ( SnapShotID {} ) {} --- Out {} , In {} ---", node.getMapNodeID(), node.getSnapShotID(), node.getMapNode(), node.getMapNode().outgoing, node.getMapNode().incoming);
            }
            RoadMap.removeMapNode(entry.getKey().getMapNode());
        }
    }

    /**
     * Restores the original nodes and connections back to the road network.
     *
     * @throws ExceptionUtils.MismatchedIdException if there is a mismatch in node IDs.
     */
    public void restoreOriginalNodes() throws ExceptionUtils.MismatchedIdException {
        for (Map.Entry<NodeBackup, Integer> entry : originalNodeMap.entrySet()) {
            NodeBackup nodeBackup = entry.getKey();
            MapNode mapNode = nodeBackup.getMapNode();
            mapNode.id = nodeBackup.mapNodeID;
            if (bDebugLogSnapShotInfo || bDebugLogUndoRedoInfo) LOG.info("## SnapShot.restoreOriginalNodes() ## inserting node: {} ( {} ) {} --- Out {} , In {} ---", nodeBackup.getMapNodeID(), nodeBackup.getSnapShotID(), mapNode, mapNode.outgoing, mapNode.incoming);
            insertMapNode(mapNode);
            mapNode.setSelected(false);
        }


        for (Connection connection : connectionList) {
            if (bDebugLogSnapShotInfo || bDebugLogUndoRedoInfo) LOG.info("## SnapShot.restoreOriginalNodes() ## Restoring connection: {} -> {} : Type: {}", connection.getStartNodeID(), connection.getEndNodeID(),connection.getConnectionType());
            restoreOriginalConnection(connection);
        }
        for (Connection connection : otherOutgoing) {
            if (bDebugLogSnapShotInfo || bDebugLogUndoRedoInfo) LOG.info("## SnapShot.restoreOriginalNodes() ## Restoring other out connection: {} -> {} : Type: {}", connection.getStartNodeID(),connection.getEndNodeID(),connection.getConnectionType());
            restoreOriginalConnection(connection);
        }
        for (Connection connection : otherIncoming) {
            if (bDebugLogSnapShotInfo || bDebugLogUndoRedoInfo) LOG.info("## SnapShot.restoreOriginalNodes() ## Restoring other in connection: {} -> {} : Type: {}", connection.getStartNodeID(), connection.getEndNodeID(), connection.getConnectionType());
            restoreOriginalConnection(connection);
        }
    }

    /**
     * Remove the original connection from the nodes in the road network.
     *
     * @param connection The Connection instance to be restored.
     */
    private void removeOriginalConnection(Connection connection) {
        if (connection.getStartNode() != null && connection.getEndNode() != null) {
            MapNode.removeConnection(connection);
        }
    }

    /**
     * Restores the connection for the original stored nodes to the road network.
     *
     * @param connection The Connection instance to be restored.
     */
    private void restoreOriginalConnection(Connection connection) {
        if (connection.getStartNode() != null && connection.getEndNode() != null) {
            MapNode.addConnection(connection);
        }
    }

    /**
     * Removes the new copies of nodes from the road network.
     */
    public void removeNewCopyOfNodes() {
        for (Map.Entry<Integer, MapNode> entry : newNodeMap.entrySet()) {
            MapNode node = entry.getValue();
            if (bDebugLogSnapShotInfo || bDebugLogUndoRedoInfo) LOG.info("## SnapShot.removeNewCopyOfNodes() ## Removing new copy of node: {} ( {} ) {}", node.id, node.getID(), node);
            RoadMap.removeMapNode(node);
        }
        for (Connection connection : newNodeConnections) {
            if (bDebugLogSnapShotInfo || bDebugLogUndoRedoInfo) LOG.info("## SnapShot.removeNewCopyOfNodes() ## Removing new copy of connection: {} -> {} : Type: {}", connection.getStartNodeID(), connection.getEndNodeID(),connection.getConnectionType());
            MapNode.removeConnection(connection);
        }
    }

    /**
     * Creates new copies of the original nodes and adds them to the road network.
     *
     * @param inOriginalLocation If true, the nodes are created in their original location
     *                           otherwise, they are moved to the center of the map panel.
     */
    public void createNewCopyOfNodes(boolean inOriginalLocation) {
        newNodeMap.clear();
        newNodeConnections.clear();
        Point2D mapPanelCentreWorld;
        double diffX = 0;
        double diffY = 0;

        if (!inOriginalLocation) {
            mapPanelCentreWorld = screenPosToWorldPos(getMapPanel().getWidth() / 2, getMapPanel().getHeight() / 2);
            diffX = nodeSelectionAreaInfo.selectionCentre.getX() - mapPanelCentreWorld.getX();
            diffY = nodeSelectionAreaInfo.selectionCentre.getY() - mapPanelCentreWorld.getY();
            if (bDebugLogSnapShotInfo || bDebugLogUndoRedoInfo) {
                LOG.info("## SnapShot.createNewCopyOfNodes() ## World coordinates of viewport Centre = {}", mapPanelCentreWorld);
                LOG.info("## SnapShot.createNewCopyOfNodes() ## Move distance = {},{}", diffX, diffY);
            }
        }

        int lastID = RoadMap.networkNodesList.size() + 1;
        if (bDebugLogSnapShotInfo || bDebugLogUndoRedoInfo) LOG.info("## SnapShot.createNewCopyOfNodes() ## lastID = {}", RoadMap.networkNodesList.getLast().id);
        for (Map.Entry<NodeBackup, Integer> entry : originalNodeMap.entrySet()) {
            NodeBackup nodeBackup = entry.getKey();
            int newNodeID = lastID++;
            if (bDebugLogSnapShotInfo || bDebugLogUndoRedoInfo) LOG.info("## SnapShot.createNewCopyOfNodes() ## OLD coord X = {}, old Y = {}, old Z = {}", nodeBackup.getMapNodeX(), nodeBackup.getMapNodeY(), nodeBackup.getMapNodeZ());
            double nodeX = roundUpDoubleToDecimalPlaces(nodeBackup.getMapNodeX() - diffX, 3);
            double nodeZ = roundUpDoubleToDecimalPlaces(nodeBackup.getMapNodeZ() - diffY, 3);
            double nodeY = getYValueFromHeightMap(nodeX, nodeZ);
            if (bDebugLogSnapShotInfo || bDebugLogUndoRedoInfo) LOG.info("## SnapShot.createNewCopyOfNodes() ## NEW coord X = {}, new Y = {}, new Z = {}", nodeX, nodeY, nodeZ);
            MapNode newNode = new MapNode(newNodeID, nodeX, nodeY, nodeZ, nodeBackup.getFlag(), false);
            newNodeMap.put(nodeBackup.mapNodeID, newNode);
            if (nodeBackup.hasMapMarker()) newNode.createMapMarker(nodeBackup.getMarkerName(), nodeBackup.getMarkerGroup(), nodeBackup.getParkingID(), nodeBackup.getParkedVehiclesList());
            if (bDebugLogSnapShotInfo || bDebugLogUndoRedoInfo) LOG.info("## SnapShot.createNewCopyOfNodes() ## Restoring node: {} ( {} ) {}", newNode.getID(), nodeBackup.getSnapShotID(), newNode);
            RoadMap.addNodeToNetwork(newNode);
        }


        for (Connection connection : connectionList) {
            if (bDebugLogSnapShotInfo || bDebugLogUndoRedoInfo) LOG.info("## SnapShot.createNewCopyOfNodes() ## Restoring connection: {} -> {} : Type: {}", connection.getStartNodeID(), connection.getEndNodeID(),connection.getConnectionType());
            createNewConnection(connection);
        }
    }

    /**
     * Recreates all the original connections between the newly created nodes.
     *
     * @param connection The Connection instance to be created.
     */
    private void createNewConnection(Connection connection) {
        MapNode sourceNode = newNodeMap.get(connection.getStartNodeID());
        MapNode targetNode = newNodeMap.get(connection.getEndNodeID());
        if (bDebugLogSnapShotInfo || bDebugLogUndoRedoInfo) LOG.info("createNewConnection: sourceID {} -> targetID {} : Type: {}", sourceNode.id, targetNode.id, connection.getConnectionType());
        Connection newConnection = new Connection(sourceNode, targetNode, connection.getConnectionType());
        MapNode.addConnection(newConnection);
        newNodeConnections.add(newConnection);
    }

    /**
     * Adds a connection between the selected nodes the snapshot.
     *
     * @param connection The Connection instance to be added to the snapshot.
     */
    public void addConnection(Connection connection) { connectionList.add(connection); }

    /**
     * Adds an outgoing connection from any of the selected nodes to any
     * MapNode that does not exist in the snapshot node list
     *
     * @param connection The Connection instance to be added to the snapshot.
     */
    public void addOtherOutgoing(Connection connection) {
        otherOutgoing.add(connection);
    }

    /**
     * Adds an incoming connection from any MapNode that does not exist in the snapshot
     * node list, to any node that does exist in the snapshot node list.
     *
     * @param connection The Connection instance to be added to the snapshot.
     */
    public void addOtherIncoming(Connection connection) {
        otherIncoming.add(connection);
    }

    /**
     * Inserts a MapNode into the road network at the correct position.
     *
     * @param toAdd The MapNode to be inserted.
     * @throws ExceptionUtils.MismatchedIdException if there is a mismatch in node IDs.
     */
    private void insertMapNode(MapNode toAdd) throws ExceptionUtils.MismatchedIdException {

        // starting at the index of where we need to insert the node
        // increment the ID's of all nodes to the right of the mapNodes by +1
        // so when we insert the node, all the id's match their index

        int insertIndex = toAdd.id - 1;

        // createSetting an iterator starting at the insertion point
        ListIterator<MapNode> roadMapIterator = networkNodesList.listIterator(insertIndex);

        if (roadMapIterator.hasPrevious()) {
            MapNode leftNode = roadMapIterator.previous();
            if (leftNode.id != toAdd.id - 1 || getIndexPositionOfNode(leftNode) + 1 != toAdd.id -1) {
                LOG.info("SnapShot.insertMapNode() Exception: leftNode Id {} (index {})", leftNode.id, getIndexPositionOfNode(leftNode));
                // throw an exception to halt the insertion before anything is actually committed and causing corruption
                throw new ExceptionUtils.MismatchedIdException("SnapShot.insertMapNode() Exception", toAdd.id, insertIndex - 1, toAdd.id - 1);
            }
            // Move the iterator back to the original position
            roadMapIterator.next();
        }

        while (roadMapIterator.hasNext()) {
            MapNode node = roadMapIterator.next();
            node.id++;
        }

        // insert the MapNode into the list

        if (bDebugLogSnapShotInfo || bDebugLogUndoRedoInfo) LOG.info("## SnapShot.insertMapNode() ## inserting MapNode ID {} into index {}", toAdd.id, insertIndex );
        networkNodesList.add(insertIndex, toAdd);
    }

    /**
     * Finds a connection in the given list.
     *
     * @param list The list of Connection objects to search.
     * @param sourceNode The source MapNode of the connection.
     * @param targetNode The target MapNode of the connection.
     * @param type The type of the connection.
     * @return true if the connection is found, false otherwise.
     */
    public boolean findConnection(ArrayList<Connection> list, MapNode sourceNode, MapNode targetNode, Connection.ConnectionType type) {
        for (Connection node : list) {
            if (node.getConnectionType() == type && node.getStartNodeID() == sourceNode.id && node.getEndNodeID() == targetNode.id) {
                return true;
            }
        }
        return false;
    }

    //
    // Getters
    //

    /**
     * Gets the list of the new nodes created by the snapshot.
     *
     * @return An ArrayList of the newly created MapNodes.
     */
    public ArrayList<MapNode>getNewNodeList() {
        ArrayList<MapNode> nodeList = new ArrayList<>();
        for (Map.Entry<Integer, MapNode> entry : newNodeMap.entrySet()) {
            MapNode node = entry.getValue();
            nodeList.add(node);
        }
        return nodeList;
    }

    /**
     * Gets the list of the original nodes added to the snapshot.
     *
     * @return An ArrayList of the original MapNodes.
     */
    public ArrayList<MapNode>getOriginalNodeList() {
        ArrayList<MapNode> nodeList = new ArrayList<>();
        for (Map.Entry<NodeBackup, Integer> entry : originalNodeMap.entrySet()) {
            NodeBackup nb = entry.getKey();
            nodeList.add(nb.getMapNode());
        }
        return nodeList;
    }

    /**
     * The NodeBackup class is the main way the snapshot stores all the node/connection information
     * so it can be used to remove/restore the original nodes and connections at a later time
     */
    public static class NodeBackup implements Comparable<NodeBackup> {

        private final int snapshotID;
        private final MapNode mapNode;
        private final int mapNodeID;
        private final double mapNodeX;
        private final double mapNodeY;
        private final double mapNodeZ;
        private final int flag;
        private final boolean hasMapMarker;
        private final String markerName;
        private final String markerGroup;
        private final int parkingID;
        private final List<String> parkedVehiclesList;

        /**
         * Construct a new NodeBackup instance.
         *
         * @param snapID The snapshot ID.
         * @param node The MapNode to be backed up.
         */
        public NodeBackup(int snapID, MapNode node) {
            this.snapshotID = snapID;
            this.mapNode = node;
            this.mapNodeID = node.id;
            this.mapNodeX = node.x;
            this.mapNodeY = node.y;
            this.mapNodeZ = node.z;
            this.flag = node.flag;
            if (node.hasMapMarker()) {
                this.hasMapMarker = node.hasMapMarker();
                this.markerName = node.getMarkerName();
                this.markerGroup = node.getMarkerGroup();
                this.parkingID = node.getParkingID();
                this.parkedVehiclesList = node.getParkedVehiclesList();
            } else {
                this.hasMapMarker = false;
                this.markerName = null;
                this.markerGroup = null;
                this.parkingID = -1;
                this.parkedVehiclesList = new ArrayList<>();
            }
        }

        /**
         * Compares this NodeBackup instance with another for ordering
         * and is used to help sort the TreeMap by the mapNodeID
         *
         * @param other The other NodeBackup instance to compare with.
         * @return A negative integer, zero, or a positive integer as
         *         this instance is less than, equal to, or greater
         *         than the specified object.
         */
        @Override
        public int compareTo(NodeBackup other) {
            return Integer.compare(this.mapNodeID, other.mapNodeID);
        }

        public int getSnapShotID() { return snapshotID; }
        public MapNode getMapNode() { return mapNode; }
        public int getMapNodeID() { return mapNodeID; }
        public double getMapNodeX() { return mapNodeX; }
        public double getMapNodeY() { return mapNodeY; }
        public double getMapNodeZ() { return mapNodeZ; }
        public int getFlag() { return flag; }
        public boolean hasMapMarker() { return hasMapMarker; }
        public String getMarkerName() { return markerName; }
        public String getMarkerGroup() { return markerGroup; }
        public int getParkingID() { return parkingID; }
        public List<String> getParkedVehiclesList() { return parkedVehiclesList; }
    }
}
