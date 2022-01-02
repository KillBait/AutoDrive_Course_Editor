package AutoDriveEditor.RoadNetwork;

import java.util.LinkedList;

public class MapNode {

    public static final int NODE_STANDARD = 0;
    public static final int NODE_SUBPRIO = 1;
    public static final int NODE_CONTROLPOINT = 99;

    public LinkedList<MapNode> incoming;
    public LinkedList<MapNode> outgoing;
    public double x, y, z;
    public int id, flag;
    public boolean isControlNode;
    public boolean isSelected;
    public boolean hasWarning;
    public LinkedList<MapNode> warningNodes;
    public boolean scheduleDelete;

    public MapNode(int id, double x, double y, double z, int flag, boolean isSelected, boolean isControlNode) {
        this.id = id;
        this.x = x;
        this.y = y;
        this.z = z;
        this.incoming = new LinkedList<>();
        this.outgoing = new LinkedList<>();
        this.flag = flag;

        // editor use only!

        this.isSelected = isSelected;
        this.isControlNode = isControlNode;
        this.hasWarning = false;
        this.warningNodes = new LinkedList<>();
        this.scheduleDelete = false;
    }
}
