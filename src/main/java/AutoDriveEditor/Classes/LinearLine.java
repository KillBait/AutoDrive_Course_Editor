package AutoDriveEditor.Classes;

import AutoDriveEditor.GUI.Buttons.LinerLineBaseButton;
import AutoDriveEditor.GUI.MapPanel;
import AutoDriveEditor.RoadNetwork.MapNode;

import java.awt.geom.Point2D;
import java.util.LinkedList;

import static AutoDriveEditor.AutoDriveEditor.changeManager;
import static AutoDriveEditor.AutoDriveEditor.getMapPanel;
import static AutoDriveEditor.GUI.MapPanel.createConnectionBetween;
import static AutoDriveEditor.GUI.MapPanel.getYValueFromHeightMap;
import static AutoDriveEditor.GUI.Menus.DebugMenu.Logging.LogLinearLineInfoMenu.bDebugLogLinearlineInfo;
import static AutoDriveEditor.RoadNetwork.MapNode.NODE_FLAG_REGULAR;
import static AutoDriveEditor.RoadNetwork.MapNode.NODE_FLAG_SUBPRIO;
import static AutoDriveEditor.RoadNetwork.RoadMap.createMapNode;
import static AutoDriveEditor.RoadNetwork.RoadMap.createNewNetworkNode;
import static AutoDriveEditor.Utils.LoggerUtils.LOG;
import static AutoDriveEditor.XMLConfig.AutoSave.resumeAutoSaving;
import static AutoDriveEditor.XMLConfig.AutoSave.suspendAutoSaving;
import static AutoDriveEditor.XMLConfig.EditorXML.*;

public class LinearLine {

    private final LinkedList<MapNode> lineNodeList;
    private MapNode lineStartNode;
    private final Point2D lineEndWorldLocation;
    private final int nodeType;

    public LinearLine(MapNode startNode, int mouseX, int mouseY, int nodeType) {
        this.lineNodeList = new LinkedList<>();
        this.lineStartNode = startNode;
        Point2D pointerWorldPos = MapPanel.screenPosToWorldPos(mouseX, mouseY);
        this.lineEndWorldLocation = new Point2D.Double(pointerWorldPos.getX(), pointerWorldPos.getY());
        this.nodeType = nodeType;
        getInterpolationPointsForLinearLine();
    }

    private void getInterpolationPointsForLinearLine() {

        Point2D.Double point = new Point2D.Double();

        this.lineNodeList.clear();
        double diffX = this.lineEndWorldLocation.getX() - this.lineStartNode.x;
        double diffY = this.lineEndWorldLocation.getY() - this.lineStartNode.z;
        double powX = Math.pow(diffX, 2);
        double powY = Math.pow(diffY, 2);
        double lineLength = Math.sqrt( powX + powY);

        int multiplier = (int)lineLength/(linearLineNodeDistance);
        int id = 0;

        if (multiplier > 0) {
            for(int i=0 ; i <= multiplier ; i++) {
                point.x = this.lineStartNode.x * (1 - ((double)i / multiplier)) + this.lineEndWorldLocation.getX() * ((double)i / multiplier);
                point.y = this.lineStartNode.z * (1 - ((double)i / multiplier)) + this.lineEndWorldLocation.getY() * ((double)i / multiplier);
                this.lineNodeList.add(createMapNode(id, point.getX(), 0, point.getY(), this.nodeType, false, false));
                id++;
            }
        } else {
            this.lineNodeList.add(createMapNode(0, this.lineStartNode.x, 0, this.lineStartNode.z, this.nodeType, false, false));
            this.lineNodeList.add(createMapNode(1, this.lineEndWorldLocation.getX(), 0, this.lineEndWorldLocation.getY(), this.nodeType, false, false));
        }
    }

    public LinkedList<MapNode> getLinearLineNodeList() { return lineNodeList; }

    public void updateLineEndLocation(double worldX, double worldY) {
        if ((this.lineStartNode != null)) {
            if (bCreateLinearLineEndNode && bGridSnap) {
                double newX, newY;
                if (bGridSnapSubs) {
                    newX = Math.round(worldX / (gridSpacingX / (gridSubDivisions + 1))) * (gridSpacingX / (gridSubDivisions + 1));
                    newY = Math.round(worldY / (gridSpacingY / (gridSubDivisions + 1))) * (gridSpacingY / (gridSubDivisions + 1));
                } else {
                    newX = Math.round(worldX / gridSpacingX) * gridSpacingX;
                    newY = Math.round(worldY / gridSpacingY) * gridSpacingY;
                }
                this.lineEndWorldLocation.setLocation(newX, newY);
            } else {
                this.lineEndWorldLocation.setLocation(worldX, worldY);
            }
            getInterpolationPointsForLinearLine();
        }
    }

    public void updateLineStartLocation(double worldX, double worldY) {
        if ((this.lineStartNode != null && linearLineNodeDistance >0)) {
            this.lineStartNode.x = worldX;
            this.lineStartNode.z = worldY;
            getInterpolationPointsForLinearLine();
        }
    }

    public void clear() {
        this.lineNodeList.clear();
        this.lineStartNode = null;
    }

    public MapNode commitLinearLineEndingAt(MapNode endNode, int connectionType, int nodeType) {

        float yInterpolation;
        boolean endNodeCreated = false;
        double heightMapY;

        suspendAutoSaving();

        LinkedList<MapNode> mergeNodesList  = new LinkedList<>();

        if (bDebugLogLinearlineInfo) LOG.info("## LinearLine.commit Debug ## LineNodeList size = {}",this.lineNodeList.size()-1);

        mergeNodesList.add(lineStartNode);

        // fix for linear lines with no intermediate nodes not setting the start node flag properly

        if (lineNodeList.size() == 2) {
            if (nodeType == NODE_FLAG_REGULAR) lineStartNode.flag = 0;
            if (nodeType == NODE_FLAG_SUBPRIO) lineStartNode.flag = 1;
        }

        if (bDebugLogLinearlineInfo) LOG.info("## LinearLine.commit Debug ## Calculating Y interpolation for all points");

        yInterpolation = calcYInterpolation(this.lineStartNode, lineEndWorldLocation);

        for (MapNode tempNode : this.lineNodeList) {
            if (tempNode != this.lineNodeList.getFirst()) {
                heightMapY = lineStartNode.y + ( yInterpolation * tempNode.id);
                if (tempNode != this.lineNodeList.getLast()) {
                    MapNode newNode = createNewNetworkNode(tempNode.x, heightMapY, tempNode.z, nodeType, false, false);
                    mergeNodesList.add(newNode);
                    if (bDebugLogLinearlineInfo) LOG.info("## LinearLine.commit Debug ## created interpolation node:- ID {} at x {}, y {}, z {}", newNode.id, newNode.x, newNode.y, newNode.z);
                } else {
                    if (endNode == null) {
                        MapNode newNode = createNewNetworkNode(tempNode.x, heightMapY, tempNode.z, nodeType, false, false);
                        mergeNodesList.add(newNode);
                        endNodeCreated = true;
                        if (bDebugLogLinearlineInfo) LOG.info("## LinearLine.commit Debug ## Created end node at world co-ordinates {},{},{}",lineEndWorldLocation.getX(), heightMapY, lineEndWorldLocation.getY());
                    } else {
                        mergeNodesList.add(endNode);
                    }
                }
            }

        }

        if (bDebugLogLinearlineInfo) LOG.info("## LinearLine.commit Debug ## Start ID = {} : End ID = {} : EndNodeCreated = {} : mergeNodesList size = {} : connectionType = {}", this.lineStartNode.id, mergeNodesList.getLast().id, endNodeCreated, mergeNodesList.size()-1, connectionType);
        changeManager.addChangeable( new LinerLineBaseButton.LinearLineChanger(mergeNodesList, endNodeCreated, connectionType));
        connectNodes(mergeNodesList, connectionType);
        getMapPanel().repaint();

        resumeAutoSaving();
        return mergeNodesList.getLast();
    }

    /**
     * Given the start + end Y locations, will calculate a linear
     * value that can be added to each node along the linear line
     * to smooth out any height difference.
     * <p></p>
     * Both locations must be world coordinates
     *<p></p>
     * @param startNode MapNode that starts the line
     * @param endPointWorld End coordinates as a Point2D
     *
     * @return Value to add to each node's Y coordinate
     **/

    private float calcYInterpolation(MapNode startNode, Point2D endPointWorld) {

        double lineEndY;
        float returnVal;

        MapNode lineEndNode = MapPanel.getNodeAtWorldPosition(endPointWorld.getX(), endPointWorld.getY());

        if (lineEndNode == null) {
            lineEndY = getYValueFromHeightMap(endPointWorld.getX(), endPointWorld.getY());
            if (bDebugLogLinearlineInfo) LOG.info("## LinearLine.calcY Debug ## NO MapNode found at line end co-ordinates ( {},{} )", endPointWorld.getX(), endPointWorld.getY());
        } else {
            if (bDebugLogLinearlineInfo) LOG.info("## LinearLine.calcY Debug ## MapNode ID {} found at line end co-ordinates", lineEndNode.id);
            lineEndY = lineEndNode.y;
        }

        if (lineEndY == -1 && startNode.y != -1) lineEndY = startNode.y;
        if (lineEndY != -1 && startNode.y == -1) startNode.y = lineEndY;

        if (this.lineNodeList.size() <= 1) {
            returnVal = (float) (lineEndY - startNode.y);
            if (bDebugLogLinearlineInfo) LOG.info("## LinearLine.calcY Debug ## No interpolation points were created, Calculated Y is from {} to {} (length = {})", startNode.y, lineEndY, lineEndY - startNode.y);
        } else {
            returnVal = (float) ((lineEndY - startNode.y) / (this.lineNodeList.size() - 1));
            if (bDebugLogLinearlineInfo) {
                LOG.info("## LinearLine.calcY Debug ## Calculated Y interpolation from {} to {} (length = {})", startNode.y, lineEndY, lineEndY - startNode.y);
                LOG.info("## LinearLine.calcY Debug ## adding {} per node ( length {} / num Nodes {} = {} )", returnVal, lineEndY - startNode.y, this.lineNodeList.size() - 1, returnVal);
            }
        }
        return returnVal;
    }

    public static void connectNodes(LinkedList<MapNode> mergeNodesList, int connectionType)  {
        suspendAutoSaving();
        if ( mergeNodesList.size() <= 1) {
            if (bDebugLogLinearlineInfo) LOG.info("## LinearLine.connectNodes Debug ## mergeNodesList size <= 1 , connecting start and end nodes");
            createConnectionBetween(mergeNodesList.getFirst(),mergeNodesList.getLast(),connectionType);
        } else {
            for (int j = 0; j < mergeNodesList.size() - 1; j++) {
                MapNode connectionStartNode = mergeNodesList.get(j);
                MapNode connectionEndNode = mergeNodesList.get(j + 1);
                if (bDebugLogLinearlineInfo) {
                    LOG.info("## LinearLine.connectNodes Debug ## Creating connection type {} between ID {} ({},{},{}) and ID {} ({},{},{})", connectionType, connectionStartNode.id, connectionStartNode.x, connectionStartNode.y, connectionStartNode.z, connectionEndNode.id, connectionEndNode.x, connectionEndNode.y, connectionEndNode.z);
                }
                createConnectionBetween(connectionStartNode, connectionEndNode, connectionType);
            }
        }
        resumeAutoSaving();
        if (bDebugLogLinearlineInfo) LOG.info("## LinearLine.connectNodes Debug ## Finished Creating LinearLine");
    }

    @SuppressWarnings("unused")
    public boolean isLineCreated() {
        return this.lineNodeList.size() >0;
    }

    // getters
    public MapNode getLineStartNode() { return this.lineStartNode; }

    public MapNode getLineEndNode() { return this.lineNodeList.getLast(); }
}
