package AutoDriveEditor.RoadNetwork;

import java.util.LinkedList;

public class MapNode {

    public static final int NODE_FLAG_STANDARD = 0;
    public static final int NODE_FLAG_SUBPRIO = 1;
    public static final int NODE_FLAG_CONTROL_POINT = 99;

    public static final int NODE_WARNING_NONE = 0;
    public static final int NODE_WARNING_OVERLAP = 1;
    public static final int NODE_WARNING_NEGATIVE_Y = 2;
    public static final int NODE_WARNING_OVERLAP_Y = 3;



    public LinkedList<MapNode> incoming;
    public LinkedList<MapNode> outgoing;
    public double x, y, z;
    public int id, flag;
    public boolean isControlNode;
    public boolean isSelected;
    public boolean hasWarning;
    public int warningType;
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
        this.warningType = NODE_WARNING_NONE;
        this.scheduleDelete = false;
    }

    public void clearWarning() {
        this.hasWarning = false;
        this.warningType = NODE_WARNING_NONE;
    }
}
