package AutoDriveEditor.Utils.Classes;

import AutoDriveEditor.RoadNetwork.MapNode;

public class KDTree3DNode {
    MapNode mapNode;
    KDTree3DNode left;
    KDTree3DNode right;

    KDTree3DNode(MapNode mapNode) {
        this.mapNode = mapNode;
        this.left = null;
        this.right = null;
    }
}
