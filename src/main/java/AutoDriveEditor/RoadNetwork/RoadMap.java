package AutoDriveEditor.RoadNetwork;

import AutoDriveEditor.Classes.Util_Classes.ExceptionUtils;

import java.util.LinkedList;
import java.util.UUID;

import static AutoDriveEditor.Classes.Util_Classes.LoggerUtils.LOG;
import static AutoDriveEditor.GUI.MapPanel.roadMap;
import static AutoDriveEditor.Locale.LocaleManager.getLocaleString;
import static AutoDriveEditor.Managers.ScanManager.checkNodeOverlap;
import static AutoDriveEditor.RoadNetwork.MapNode.NODE_FLAG_REGULAR;
import static AutoDriveEditor.RoadNetwork.MapNode.NODE_FLAG_SUBPRIO;

public class RoadMap {

    public static String mapName;
    public static LinkedList<MapNode> networkNodesList;
    public static UUID uuid;


    public RoadMap() {
        networkNodesList = new LinkedList<>();
        mapName = null;
        uuid = UUID.randomUUID();

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

    public static void removeMapNode(MapNode toDelete) throws IndexOutOfBoundsException {
        if (networkNodesList.contains(toDelete)) {
            networkNodesList.forEach(mapNode -> {
                if (mapNode.outgoing.remove(toDelete)) LOG.info("## RoadMap.removeMapNode() ## Removing MapNode ID {} from outgoing connections of MapNode ID {}", toDelete.id, mapNode.id);
                if (mapNode.incoming.remove(toDelete)) LOG.info("## RoadMap.removeMapNode() ## Removing MapNode ID {} from incoming connections of MapNode ID {}", toDelete.id, mapNode.id);
                if (mapNode.id > toDelete.id) mapNode.id--;
                mapNode.getWarningNodes().remove(toDelete);
                if (mapNode.getWarningNodes().isEmpty()) mapNode.clearWarning();
            });
            networkNodesList.remove(toDelete);
        } else {
            LOG.warn("## RoadMap.removeMapNode() ## MapNode ID {} not found in networkNodesList", toDelete.id);
        }
    }

    public static boolean isDual(MapNode start, MapNode target) {
        return start.outgoing.contains(target) && target.incoming.contains(start) &&
               target.outgoing.contains(start) && start.incoming.contains(target);
    }

    public static boolean isReverse(MapNode start, MapNode target) {
        return start.outgoing.contains(target) && !target.incoming.contains(start) &&
               !target.outgoing.contains(start) && !start.incoming.contains(target);
    }

    public static boolean isRegular(MapNode start, MapNode target) {
        return start.outgoing.contains(target) && target.incoming.contains(start) &&
                !target.outgoing.contains(start) && !start.incoming.contains(target);
    }

    public static boolean isCrossedRegular(MapNode start, MapNode target) {
        return start.outgoing.contains(target) && target.incoming.contains(start) &&
               target.outgoing.contains(start) && !start.incoming.contains(target) ;
    }

    public static boolean isCrossedReverse(MapNode start, MapNode target) {
        return start.outgoing.contains(target) && !target.incoming.contains(start) &&
               target.outgoing.contains(start) && start.incoming.contains(target);
    }

    public static Connection.ConnectionType getConnection(MapNode startNode, MapNode endNode) {
        if (isCrossedRegular(startNode, endNode)) {
            return Connection.ConnectionType.CROSSED_REGULAR;
        } else if (isCrossedReverse(startNode, endNode)) {
            return Connection.ConnectionType.CROSSED_REVERSE;
        } else if (isDual(startNode, endNode)) {
            return Connection.ConnectionType.DUAL;
        } else if (isReverse(startNode, endNode)) {
            return Connection.ConnectionType.REVERSE;
        } else if (isRegular(startNode, endNode) && startNode.getFlag() == NODE_FLAG_SUBPRIO) {
            return Connection.ConnectionType.SUBPRIO;
        } else if (isRegular(startNode, endNode) && startNode.getFlag() == NODE_FLAG_REGULAR) {
            return Connection.ConnectionType.REGULAR;
        } else {
            return Connection.ConnectionType.UNKNOWN;
        }
    }

    public static int getIndexPositionOfNode(MapNode node) { return networkNodesList.indexOf(node); }

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

    public static boolean isMapLoaded() {
        return roadMap != null;
    }
}
