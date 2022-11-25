package AutoDriveEditor.MapPanel;

import AutoDriveEditor.GUI.Buttons.LinerLineBaseButton;
import AutoDriveEditor.RoadNetwork.MapNode;
import AutoDriveEditor.RoadNetwork.RoadMap;

import java.awt.geom.Point2D;
import java.util.LinkedList;

import static AutoDriveEditor.AutoDriveEditor.changeManager;
import static AutoDriveEditor.GUI.MenuBuilder.bDebugLogLinearlineInfo;
import static AutoDriveEditor.MapPanel.MapPanel.*;
import static AutoDriveEditor.Utils.LoggerUtils.LOG;
import static AutoDriveEditor.XMLConfig.EditorXML.linearLineNodeDistance;

public class LinearLine {

    private final LinkedList<MapNode> lineNodeList;
    private MapNode lineStartNode;
    private final int nodeType;

    public LinearLine(MapNode startNode, double mouseX, double mouseY, int nodeType) {
        this.lineNodeList = new LinkedList<>();
        this.lineStartNode = startNode;
        this.nodeType = nodeType;
        getLinearInterpolationPointsForLine(this.lineStartNode, mouseX, mouseY);
    }

    private void getLinearInterpolationPointsForLine(MapNode startNode, double endX, double endY) {

        this.lineNodeList.clear();

        double diffX = endX - startNode.x;
        double diffY = endY - startNode.z;

        double powX = Math.pow(diffX, 2);
        double powY = Math.pow(diffY, 2);

        double lineLength = Math.sqrt( powX + powY);

        int multiplier = (int)lineLength/(linearLineNodeDistance);
        int id = 1;

        for(int i=0;i<=multiplier;i++) {
            Point2D.Double point = new Point2D.Double();
            point.x = startNode.x * (1 - ((double)i / multiplier)) + endX * ((double)i / multiplier);
            point.y = startNode.z * (1 - ((double)i / multiplier)) + endY * ((double)i / multiplier);
            this.lineNodeList.add(new MapNode(id, point.getX(), 0, point.getY(), this.nodeType, false, false));
            id++;
        }
    }

    public LinkedList<MapNode> getLinearLineNodeList() { return lineNodeList; }

    public void updateLine(double mouseX, double mouseY) {
        if ((this.lineStartNode != null && linearLineNodeDistance >0)) {
            getLinearInterpolationPointsForLine(this.lineStartNode, mouseX, mouseY);
        }
    }

    public void clear() {
        this.lineNodeList.clear();
        this.lineStartNode = null;
    }

    public void commit(Point2D endConnectionScreenPos, int connectionType, int nodeType) {

        float yInterpolation;
        MapNode endNode;
        boolean endNodeCreated = false;
        double heightMapY;

        canAutoSave = false;

        LinkedList<MapNode> mergeNodesList  = new LinkedList<>();

        if (bDebugLogLinearlineInfo) LOG.info("## LinearLine.commit Debug ## LineNodeList size = {}",this.lineNodeList.size()-1);

        mergeNodesList.add(lineStartNode);

        endNode = getNodeAt(endConnectionScreenPos.getX(), endConnectionScreenPos.getY());

        if (this.lineNodeList.size() <= 1) {
            if (bDebugLogLinearlineInfo) LOG.info("## LinearLine.commit Debug ## Line length is less than interpolation Distance");
            if (endNode == null) {
                if (bDebugLogLinearlineInfo) LOG.info("## LinearLine.commit Debug ## End node does not exists...Creating node");
                Point2D newNodeLoc = screenPosToWorldPos((int)endConnectionScreenPos.getX(), (int)endConnectionScreenPos.getY());
                MapNode newNode = new MapNode(RoadMap.mapNodes.size() + 1, newNodeLoc.getX(), getYValueFromHeightMap(newNodeLoc.getX(), newNodeLoc.getY()), newNodeLoc.getY(), nodeType, false, false);
                RoadMap.mapNodes.add(newNode);
                endNode = newNode;
                endNodeCreated = true;
            } else {
                if (bDebugLogLinearlineInfo) LOG.info("## LinearLine.commit Debug ## End node already exists");
            }
        } else {
            if (bDebugLogLinearlineInfo) LOG.info("## LinearLine.commit Debug ## Calculating Y interpolation for all points");

            yInterpolation = calcYInterpolation(this.lineStartNode, endConnectionScreenPos);

            if (bDebugLogLinearlineInfo) LOG.info("## LinearLine.commit Debug ## Ignoring last interpolation point as end node already exists");

            for (int j = 1; j < this.lineNodeList.size()-1; j++) {
                MapNode tempNode = this.lineNodeList.get(j);
                heightMapY = lineStartNode.y + ( yInterpolation * j);
                MapNode newNode = new MapNode(RoadMap.mapNodes.size() + 1, tempNode.x, heightMapY, tempNode.z, nodeType, false, false);
                RoadMap.mapNodes.add(newNode);
                mergeNodesList.add(newNode);
                if (bDebugLogLinearlineInfo) LOG.info("## LinearLine.commit Debug ## creating node {} : ID {} at x {}, y {}, z {}", j, newNode.id, newNode.x, newNode.y, newNode.z);
            }

            if (endNode == null) {
                if (bDebugLogLinearlineInfo) LOG.info("## LinearLine.commit Debug ## End node does not exists...Creating node");
                Point2D endNodeWorldLoc = screenPosToWorldPos((int)endConnectionScreenPos.getX(), (int)endConnectionScreenPos.getY());
                MapNode newEndNode = new MapNode(RoadMap.mapNodes.size() + 1, endNodeWorldLoc.getX(), getYValueFromHeightMap(endNodeWorldLoc.getX(), endNodeWorldLoc.getY()), endNodeWorldLoc.getY(), nodeType, false, false);
                if (bDebugLogLinearlineInfo) LOG.info("## LinearLine.commit Debug ## Created end node at world co-ordinates {},{},{}",endNodeWorldLoc.getX(), getYValueFromHeightMap(endNodeWorldLoc.getX(), endNodeWorldLoc.getY()), endNodeWorldLoc.getY());
                RoadMap.mapNodes.add(newEndNode);
                endNode = newEndNode;
                endNodeCreated = true;
            }
        }

        if (bDebugLogLinearlineInfo) LOG.info("## LinearLine.commit Debug ## Start ID = {} : End ID = {} : EndNodeCreated = {} : mergeNodesList size = {} : connectionType = {}", this.lineStartNode.id, endNode.id, endNodeCreated, mergeNodesList.size()-1, connectionType);
        changeManager.addChangeable( new LinerLineBaseButton.LinearLineChanger(this.lineStartNode, endNode, endNodeCreated, mergeNodesList, connectionType));
        connectNodes(this.lineStartNode, endNode, mergeNodesList, connectionType);

        canAutoSave = true;

    }

    private float calcYInterpolation(MapNode startNode, Point2D endConnectionScreenPos) {

        double lineEndY;
        float returnVal;

        MapNode lineEndNode = getNodeAt(endConnectionScreenPos.getX(), endConnectionScreenPos.getY());

        if (lineEndNode == null) {
            Point2D endConnectionWorldPos = screenPosToWorldPos((int) endConnectionScreenPos.getX(), (int) endConnectionScreenPos.getY());
            lineEndY = getYValueFromHeightMap(endConnectionWorldPos.getX(), endConnectionWorldPos.getY());
            if (bDebugLogLinearlineInfo) LOG.info("## LinearLine.calcY Debug ## NO MapNode found at line end co-ordinates ( {},{} )", endConnectionWorldPos.getX(), endConnectionWorldPos.getY());
        } else {
            if (bDebugLogLinearlineInfo) LOG.info("## LinearLine.calcY Debug ## MapNode found at line end co-ordinates");
            lineEndY = lineEndNode.y;
        }

        if (lineEndY == -1 && startNode.y != -1) lineEndY = startNode.y;
        if (lineEndY != -1 && startNode.y == -1) startNode.y = lineEndY;

        if (this.lineNodeList.size() <= 1) {
            returnVal = (float) (lineEndY - startNode.y);
        } else {
            returnVal = (float) ((lineEndY - startNode.y) / (this.lineNodeList.size() - 1));
        }

        if (bDebugLogLinearlineInfo) {
            LOG.info("## LinearLine.calcY Debug ## Y interpolation -- start Y = {} , end Y = {}, difference = {} / {} ( {} )", startNode.y, lineEndY, lineEndY - startNode.y, this.lineNodeList.size() - 1, returnVal );
        }

        return returnVal;
    }

    public static void connectNodes(MapNode startNode, MapNode endNode, LinkedList<MapNode> mergeNodesList, int connectionType)  {
        canAutoSave = false;
        if ( mergeNodesList.size() <= 1) {
            MapPanel.createConnectionBetween(startNode,endNode,connectionType);
        } else {
            for (int j = 0; j < mergeNodesList.size() -1; j++) {
                MapNode connectionStartNode = mergeNodesList.get(j);
                MapNode connectionEndNode = mergeNodesList.get(j+1);
                if (bDebugLogLinearlineInfo) LOG.info("## LinearLine.connectNodes Debug ## Creating connection between ID {} ({},{},{}) and ID {} ({},{},{})", connectionStartNode.id, connectionStartNode.x, connectionStartNode.y, connectionStartNode.z, connectionEndNode.id, connectionEndNode.x, connectionEndNode.y, connectionEndNode.z);
                MapPanel.createConnectionBetween(connectionStartNode,connectionEndNode,connectionType);
            }
            if (bDebugLogLinearlineInfo) LOG.info("## LinearLine.connectNodes Debug ## Creating connection between last interpolation node : ID {} ({},{},{}) and end node : ID {} ({},{},{})", mergeNodesList.getLast().id, mergeNodesList.getLast().x, mergeNodesList.getLast().y, mergeNodesList.getLast().z, endNode.id, endNode.x, endNode.y, endNode.z);
            MapPanel.createConnectionBetween(mergeNodesList.getLast(),endNode,connectionType);
        }
        canAutoSave = true;
        if (bDebugLogLinearlineInfo) LOG.info("## LinearLine.connectNodes Debug ## Finished Creating LinearLine");
    }

    @SuppressWarnings("unused")
    public boolean isLineCreated() {
        return this.lineNodeList.size() >0;
    }

    // getters
    public MapNode getLineStartNode() { return this.lineStartNode; }
}
