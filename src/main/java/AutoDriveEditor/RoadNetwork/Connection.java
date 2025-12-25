package AutoDriveEditor.RoadNetwork;

import java.awt.*;
import java.util.Objects;

import static AutoDriveEditor.RoadNetwork.MapNode.NODE_FLAG_REGULAR;
import static AutoDriveEditor.RoadNetwork.MapNode.NODE_FLAG_SUBPRIO;
import static AutoDriveEditor.XMLConfig.EditorXML.*;

@SuppressWarnings("unused")
public class Connection {

    public enum ConnectionType {
        NONE("None"),
        UNKNOWN("Unknown"),
        MULTIPLE( "Multiple"),
        REGULAR("Regular"),
        SUBPRIO("Subprio"),
        DUAL("Dual"),
        REVERSE("Reverse"),
        CROSSED("Crossed"),
        CROSSED_REGULAR("Crossed Regular"),
        CROSSED_REVERSE("Crossed Reverse");

        private final String description;

        ConnectionType(String description){
            this.description = description;
        }

        public String getDescription() { return description; }

        public Color getColor(MapNode startNode) {
            Color colour;
            switch (this) {
                case DUAL:
                    if (startNode.getFlag() == NODE_FLAG_SUBPRIO) {
                        colour = colourConnectDualSubprio;
                    } else {
                        colour = colourConnectDual;
                    }
                    break;
                case REVERSE:
                case CROSSED_REVERSE:
                    if (startNode.getFlag() == NODE_FLAG_REGULAR) {
                        colour = colourConnectReverse;
                    } else {
                        colour = colourConnectReverseSubprio;
                    }
                    break;
                case REGULAR:
                case CROSSED_REGULAR:
                case SUBPRIO:
                    if (startNode.getFlag() == NODE_FLAG_REGULAR) {
                        colour = colourConnectRegular;
                    } else {
                        colour = colourConnectSubprio;
                    }
                    break;
                default:
                    colour = Color.WHITE;
                    break;
            }
            return colour;
        }
    }

    private final MapNode startNode;
    private final int startNodeID;
    private final MapNode endNode;
    private final int endNodeID;
    private final ConnectionType connectionType;
    private final boolean isHighlighted;

    public Connection(MapNode startNode, MapNode endNode, ConnectionType connectionType, boolean isHighlighted) {
        this.startNode = startNode;
        this.startNodeID = startNode.id;
        this.endNode = endNode;
        this.endNodeID = endNode.id;
        this.connectionType = connectionType;
        this.isHighlighted = isHighlighted;
    }

    public Connection(MapNode startNode, MapNode endNode, ConnectionType connectionType) {
        this.startNode = startNode;
        this.startNodeID = startNode.id;
        this.endNode = endNode;
        this.endNodeID = endNode.id;
        this.connectionType = connectionType;
        this.isHighlighted = false;
    }

    public Color getColor() {
        Color c;
        switch (connectionType) {
            case MULTIPLE:
                c = Color.RED;
                break;
            case CROSSED_REGULAR:
            case REGULAR:
                c = colourConnectRegular;
                break;
            case SUBPRIO:
                c = colourConnectSubprio;
                break;
            case DUAL:
                if (startNode.getFlag() == NODE_FLAG_SUBPRIO || endNode.getFlag() == NODE_FLAG_SUBPRIO) {
                    c = colourConnectDualSubprio;
                } else {
                    c = colourConnectDual;
                }
                break;
            case CROSSED_REVERSE:
            case REVERSE:
                if (startNode.getFlag() == NODE_FLAG_REGULAR) {
                    c = colourConnectReverse;
                } else {
                    c = colourConnectReverseSubprio;
                }
                break;
            default:
                c = Color.WHITE;
        }
        return c;
    }



    public ConnectionType getConnectionType() {
        return connectionType;
    }

    //
    // Getters
    //

    public MapNode getStartNode() { return startNode; }
    public int getStartNodeID() { return startNodeID; }
    public MapNode getEndNode() { return endNode; }
    public int getEndNodeID() { return endNodeID; }
    public String getConnectionTypeString() { return connectionType.getDescription(); }

    public boolean isRegular() { return connectionType == ConnectionType.REGULAR || connectionType == ConnectionType.CROSSED_REGULAR || connectionType == ConnectionType.SUBPRIO; }
    public boolean isSubprio() { return connectionType == ConnectionType.REGULAR && startNode.getFlag() == NODE_FLAG_SUBPRIO; }
    public boolean isReverse() { return connectionType == ConnectionType.REVERSE || connectionType == ConnectionType.CROSSED_REVERSE; }
    public boolean isCrossed() { return connectionType == ConnectionType.CROSSED || connectionType == ConnectionType.CROSSED_REGULAR || connectionType == ConnectionType.CROSSED_REVERSE; }
    public boolean isCrossedRegular() { return connectionType == ConnectionType.CROSSED_REGULAR; }
    public boolean isCrossedReverse() { return connectionType == ConnectionType.CROSSED_REVERSE; }
    public boolean isDual() { return connectionType == ConnectionType.DUAL; }

    public boolean isHighlighted() { return isHighlighted; }
    public boolean isReversible() { return connectionType != ConnectionType.DUAL; }
    public boolean isHidden() { return this.startNode.isConnectionHidden(this.endNode); }
    public boolean isSelectable() { return !isHidden() || bSelectHidden; }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        Connection that = (Connection) o;
        return Objects.equals(this.startNode, that.startNode) &&
                Objects.equals(this.endNode, that.endNode) &&
                this.connectionType == that.connectionType;
    }

    @Override
    public int hashCode() {
        return Objects.hash(startNode) + Objects.hash(endNode);
    }

    @Override
    public String toString() {
        return "Connection{" +
                "startNode=" + startNode + " ( ID " + startNodeID + " )" +
                ", endNode=" + endNode + " ( ID " + endNodeID + " )" +
                ", connectionType=" + connectionType +
                ", isHighlighted=" + isHighlighted +
                '}';
    }
}
