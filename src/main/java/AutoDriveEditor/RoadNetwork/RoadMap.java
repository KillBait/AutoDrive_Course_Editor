package AutoDriveEditor.RoadNetwork;

import java.util.LinkedList;
import java.util.ListIterator;
import java.util.UUID;

import static AutoDriveEditor.GUI.MenuBuilder.bDebugLogUndoRedo;
import static AutoDriveEditor.Utils.LoggerUtils.LOG;

public class RoadMap {

    public static String mapName;
    public static LinkedList<MapNode> mapNodes;
    public static UUID uuid;
    //public static LinkedList<MapMarker> mapMarkers;

    public RoadMap() {
        //mapMarkers = new LinkedList<>();
        mapNodes = new LinkedList<>();
        mapName = null;

        // generate a unique random UUID, we can use this to compare and detect when
        // a different config has been loaded.

        // The UUID is used by the undo/redo system to detect if the current function
        // it is trying to complete an operation on a config that is no longer loaded
        // into the editor. A Dialog box will display explaining why the error occurred.

        uuid = UUID.randomUUID();
    }

    public void insertMapNode(MapNode toAdd, LinkedList<MapNode> otherNodesInList, LinkedList<MapNode> otherNodesOutList) {

        // starting at the index of where we need to insert the node
        // increment the ID's of all nodes to the right of the mapNodes by +1
        // so when we insert the node, all the id's match their index

        /*LinkedList<MapNode> nodes = mapNodes;
        if (bDebugLogUndoRedo) LOG.info("## insertMapNode() ## bumping all ID's of mapNodes index {} -> {} by +1", toAdd.id - 1, nodes.size() - 1);
        for (int i = toAdd.id - 1; i <= nodes.size() - 1; i++) {
            MapNode mapNode = nodes.get(i);
            mapNode.id++;
        }*/

        ListIterator<MapNode> roadMap = mapNodes.listIterator(toAdd.id - 1);
        while (roadMap.hasNext()) {
            MapNode node = roadMap.next();
            node.id++;
        }

        // insert the MapNode into the list

        if (bDebugLogUndoRedo) LOG.info("## insertMapNode() ## inserting index {} ( ID {} ) into mapNodes", toAdd.id - 1, toAdd.id );
        mapNodes.add(toAdd.id -1 , toAdd);

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
        for (MapNode mapNode : mapNodes) {
            mapNode.outgoing.remove(toDelete);
            mapNode.incoming.remove(toDelete);
            if (mapNode.id > toDelete.id) {
                mapNode.id--;
            }
        }

        mapNodes.remove(toDelete);
    }

    public static boolean isDual(MapNode start, MapNode target) {
        return start.outgoing.contains(target) && target.outgoing.contains(start);
    }

    public static boolean isReverse(MapNode start, MapNode target) {
        return start.outgoing.contains(target) && !target.incoming.contains(start);
    }
}
