package AutoDriveEditor.RoadNetwork;

import AutoDriveEditor.Utils.ExceptionUtils;

import java.math.RoundingMode;
import java.util.LinkedList;
import java.util.ListIterator;
import java.util.UUID;

import static AutoDriveEditor.GUI.MapPanel.getYValueFromHeightMap;
import static AutoDriveEditor.GUI.MapPanel.roadMap;
import static AutoDriveEditor.GUI.Menus.DebugMenu.Logging.LogUndoRedoMenu.bDebugLogUndoRedo;
import static AutoDriveEditor.Locale.LocaleManager.getLocaleString;
import static AutoDriveEditor.Managers.ScanManager.checkNodeOverlap;
import static AutoDriveEditor.RoadNetwork.MapNode.NODE_FLAG_REGULAR;
import static AutoDriveEditor.Utils.LoggerUtils.LOG;
import static AutoDriveEditor.Utils.MathUtils.limitDoubleToDecimalPlaces;

public class RoadMap {

    public static String mapName;
    public static LinkedList<MapNode> networkNodesList;
    public static UUID uuid;


    public RoadMap() {
        networkNodesList = new LinkedList<>();
        mapName = null;

        // generate a unique random UUID, we can use this to compare and detect when
        // a different config has been loaded.

        // The UUID is used by the undo/redo system to detect if the current function
        // it is trying to complete an operation on a config that is no longer loaded
        // into the editor. A Dialog box will display explaining why the error occurred.

        uuid = UUID.randomUUID();

    }

    public static MapNode createMapNode(int id, double x, double z, int nodeType, boolean isSelected, boolean isControlNode) {
        double xPos = limitDoubleToDecimalPlaces(x,3, RoundingMode.HALF_UP);
        double yPos = limitDoubleToDecimalPlaces(getYValueFromHeightMap(x, z),3, RoundingMode.HALF_UP);
        double zPos = limitDoubleToDecimalPlaces(z,3, RoundingMode.HALF_UP);
        return new MapNode(id, xPos, yPos, zPos, nodeType, isSelected, isControlNode);
    }

    public static MapNode createMapNode(int id, double x, double y, double z, int nodeType, boolean isSelected, boolean isControlNode) {
        double xPos = limitDoubleToDecimalPlaces(x,3, RoundingMode.HALF_UP);
        double yPos = limitDoubleToDecimalPlaces(y,3, RoundingMode.HALF_UP);
        double zPos = limitDoubleToDecimalPlaces(z,3, RoundingMode.HALF_UP);
        return new MapNode(id, xPos, yPos, zPos, nodeType, isSelected, isControlNode);
    }

    public static MapNode createNewNetworkNode(double x, double z, int nodeType, boolean isSelected, boolean isControlNode) {
        MapNode createdNode = createMapNode(RoadMap.networkNodesList.size() + 1, x, z, nodeType, isSelected, isControlNode);
        RoadMap.networkNodesList.add(createdNode);
        checkNodeOverlap(createdNode);
        return createdNode;
    }

    public static MapNode createNewNetworkNode(double x, double y, double z, int nodeType, boolean isSelected, boolean isControlNode) {
        MapNode createdNode = createMapNode(RoadMap.networkNodesList.size() + 1, x, y, z, nodeType, isSelected, isControlNode);
        RoadMap.networkNodesList.add(createdNode);
        checkNodeOverlap(createdNode);
        return createdNode;
    }

    @SuppressWarnings("UnusedReturnValue")
    public static boolean addNodeToNetwork(MapNode node) {
        if (node != null) {
            RoadMap.networkNodesList.add(node);
            checkNodeOverlap(node);
            return true;
        }
        return false;
    }

    public static MapNode createControlNode(double x, double z) {
        return new MapNode(-99, x, 0, z, NODE_FLAG_REGULAR, false, true);
    }

    public void insertMapNode(MapNode toAdd, LinkedList<MapNode> otherNodesInList, LinkedList<MapNode> otherNodesOutList) throws ExceptionUtils.MismatchedIdException {

        // starting at the index of where we need to insert the node
        // increment the ID's of all nodes to the right of the mapNodes by +1
        // so when we insert the node, all the id's match their index

        int insertIndex = toAdd.id - 1;

        // create an iterator starting at the insertion point
        ListIterator<MapNode> roadMapIterator = networkNodesList.listIterator(insertIndex);


        //LOG.info("insert MapNode.id {} into index {} :- RoadMap size = {}", toAdd.id, insertIndex, networkNodesList.size());
        if (roadMapIterator.hasPrevious()) {
            MapNode leftNode = roadMapIterator.previous();
            //LOG.info("leftNode Id {} (index {})", leftNode.id, getIndexPositionOfNode(leftNode));
            if (leftNode.id != toAdd.id - 1 || getIndexPositionOfNode(leftNode) + 1 != toAdd.id -1) {
                // throw an exception to halt the insertion before anything is actually committed and causing corruption
                throw new ExceptionUtils.MismatchedIdException("insertMapNode() Exception", toAdd.id, insertIndex - 1, toAdd.id - 1);
            }
            // Move the iterator back to the original position
            roadMapIterator.next();
        }

        while (roadMapIterator.hasNext()) {
            MapNode node = roadMapIterator.next();
            node.id++;
        }

        // insert the MapNode into the list

        if (bDebugLogUndoRedo) LOG.info("## insertMapNode() ## inserting MapNode ID {} into index {}", toAdd.id, toAdd.id -1 );
        networkNodesList.add(insertIndex, toAdd);

        //now we need to restore all the connections that went from/to it

        if (otherNodesInList != null) {
            for (MapNode otherInNode : otherNodesInList) {
                if (!otherInNode.incoming.contains(toAdd)) otherInNode.incoming.add(toAdd);
            }
        }

        if (otherNodesOutList != null) {
            for (MapNode otherOutNode : otherNodesOutList) {
                if (!otherOutNode.outgoing.contains(toAdd)) otherOutNode.outgoing.add(toAdd);
            }
        }
    }

    public static void removeMapNode(MapNode toDelete) {
        for (MapNode mapNode : networkNodesList) {
            mapNode.outgoing.remove(toDelete);
            mapNode.incoming.remove(toDelete);
            mapNode.getHiddenConnectionsList().clear();
            if (mapNode.id > toDelete.id) {
                mapNode.id--;
            }
        }

        networkNodesList.remove(toDelete);
    }

    public static boolean isDual(MapNode start, MapNode target) {
        return start.outgoing.contains(target) && target.incoming.contains(start) && target.outgoing.contains(start) && start.incoming.contains(target);
    }

    public static boolean isReverse(MapNode start, MapNode target) {
        return start.outgoing.contains(target) && !target.incoming.contains(start);
    }

    public static boolean isRegular(MapNode start, MapNode target) {
        boolean regular = start.outgoing.contains(target) && target.incoming.contains(start);
        return regular || !isReverse(target, start);
    }

    public int getIndexPositionOfNode(MapNode node) { return networkNodesList.indexOf(node); }

    //
    // getters
    //

    public static RoadMap getRoadMap() { return roadMap; }


    //
    // setters
    //


    @SuppressWarnings("AccessStaticViaInstance")
    public static void setRoadMapNodes(RoadMap roadMap, LinkedList<MapNode> mapNodes) {
        roadMap.networkNodesList = mapNodes;
    }

    public static void showMismatchedIDError(String functionName, ExceptionUtils.MismatchedIdException e) {
        String errorText1 = getLocaleString("dialog_id_mismatch1") + " " + e.getValue1() + " " +
                getLocaleString("dialog_id_mismatch2") + " " + e.getValue2() + " ( " +
                getLocaleString("dialog_id_mismatch3") + e.getValue3() + " )";

        String errorText2 ="<html><center>" + getLocaleString("dialog_id_mismatch4") +
                "<br><center>" + getLocaleString("dialog_id_mismatch5");

        e.showExceptionDialog(e, getLocaleString("dialog_id_mismatch_title"), functionName, errorText1, errorText2);
        LOG.warn("## {} ## {} aborted to prevent roadmap corruption: Error inserting mapNode ID {} into index {} (Expected {})", e.getMessage(), functionName, e.getValue1(), e.getValue2(), e.getValue3());
    }

    //
    // Testing only
    //

    @SuppressWarnings("unused")
    public static void checkRoadMapValidity() {
        int numOutRegular = 0;
        int numOutReverse = 0;
        int numOutDual = 0;
        int numOutUnknown = 0;
        int numInRegular = 0;
        int numInDual = 0;
        int numInReverse = 0;
        int numInUnknown = 0;

        for (MapNode mapnode : RoadMap.networkNodesList) {
            for (MapNode outNode: mapnode.outgoing) {
                if (isDual(mapnode, outNode)) {
                    numOutDual++;
                } else if (isReverse(mapnode, outNode)) {
                    numOutReverse++;
                } else if (isRegular(mapnode, outNode)) {
                    numOutRegular++;
                } else {
                    LOG.info("UNKNOWN connection between {} and {}", mapnode.id, outNode.id);
                    numOutUnknown++;
                }
            }

            for (MapNode outNode: mapnode.incoming) {
                if (isDual(outNode, mapnode)) {
                    numInRegular++;
                } else if (isRegular(outNode, mapnode)) {
                    numInRegular++;
                } else {
                    LOG.info("UNKNOWN connection between {} and {}", mapnode.id, outNode.id);
                    numInUnknown++;
                }
            }
        }
        LOG.info(" OUT:- Dual {} , Reverse {}, Regular {}", numOutDual, numOutReverse, numOutRegular);
        LOG.info(" IN:- Dual {} , Reverse {}, Regular {}", numInDual, numInReverse, numInRegular);
    }


}
