package AutoDriveEditor.Utils.Classes;

import AutoDriveEditor.RoadNetwork.MapNode;

public class KDTree2DNode {

    public final MapNode mapNode;
    KDTree2DNode left, right;

    public KDTree2DNode(MapNode mapNode) {
        this.mapNode = mapNode;
        left = right = null;
    }
}
