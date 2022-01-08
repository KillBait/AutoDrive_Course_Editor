package AutoDriveEditor.MapPanel;

import java.awt.geom.Point2D;
import java.util.LinkedList;

import AutoDriveEditor.Managers.ChangeManager;
import AutoDriveEditor.RoadNetwork.MapNode;
import AutoDriveEditor.RoadNetwork.RoadMap;

import static AutoDriveEditor.AutoDriveEditor.*;
import static AutoDriveEditor.MapPanel.MapPanel.*;
import static AutoDriveEditor.RoadNetwork.MapNode.*;
import static AutoDriveEditor.Utils.LoggerUtils.*;
import static AutoDriveEditor.XMLConfig.EditorXML.*;

public class LinearLine {

    public LinkedList<MapNode> lineNodeList;
    private MapNode lineStartNode;
    private int interpolationPointDistance;

    public LinearLine(MapNode startNode, double mousex, double mousey, int nodeDistance) {
        this.lineNodeList = new LinkedList<>();
        this.lineStartNode = startNode;
        this.interpolationPointDistance = nodeDistance;
        getLinearInterpolationPointsForLine(this.lineStartNode, mousex, mousey);
    }

    public LinearLine(MapNode startNode, double mouseX, double mouseY) {this(startNode, mouseX, mouseY, linearLineNodeDistance);}

    // to fix - zoom in/out updates the drawn line position but not the nodes coordinates along the line

    private void getLinearInterpolationPointsForLine(MapNode startNode, double endX, double endY) {

        lineNodeList.clear();

        double diffX = endX - startNode.x;
        double diffY = endY - startNode.z;

        double powX = Math.pow(diffX, 2);
        double powY = Math.pow(diffY, 2);

        double lineLength = Math.sqrt( powX + powY);

        int multiplier = (int)lineLength/(this.interpolationPointDistance / mapZoomFactor);
        int id = 1;

        for(int i=0;i<=multiplier;i++) {
            Point2D.Double point = new Point2D.Double();
            point.x = startNode.x * ((double)1 - ((double)i/(double)multiplier)) + endX * ((double)i / (double)multiplier);
            point.y = startNode.z * (1 - (i/(double)multiplier)) + endY * (i / (double)multiplier);
            lineNodeList.add(new MapNode(id,point.getX(),0,point.getY(), NODE_FLAG_STANDARD, false, false));
            id++;
        }
    }

    public void updateLine(double mouseX, double mouseY) {
        if ((this.lineStartNode != null && this.interpolationPointDistance >0)) {
            getLinearInterpolationPointsForLine(this.lineStartNode, mouseX, mouseY);
        }
    }

    public void clear() {
        this.lineNodeList.clear();
        this.lineStartNode = null;
    }

    public void commit(MapNode lineEndNode, int connectionType, int nodeType) {
        canAutoSave = false;

        LinkedList<MapNode> mergeNodesList  = new LinkedList<>();

        if (DEBUG) LOG.info("LinearLine size = {}",this.lineNodeList.size());
        mergeNodesList.add(lineStartNode);

        if (lineEndNode.y == -1 && lineStartNode.y != -1) lineEndNode.y = lineStartNode.y;
        if (lineEndNode.y != -1 && lineStartNode.y == -1) lineStartNode.y = lineEndNode.y;



        float yInterpolation = (float) ((lineEndNode.y - lineStartNode.y) / (this.lineNodeList.size() - 1));

        LOG.info(" start = {} , end = {}, interp = {}", lineStartNode.y, lineEndNode.y, yInterpolation );

        for (int j = 1; j < this.lineNodeList.size() - 1; j++) {
            MapNode tempNode = this.lineNodeList.get(j);
            double heightMapY = getYValueFromHeightMap(tempNode.x, tempNode.z);
            if (heightMapY == -1) {
                heightMapY = lineStartNode.y + ( yInterpolation * j);
            }
            MapNode newNode = new MapNode(RoadMap.mapNodes.size() + 1, tempNode.x, heightMapY, tempNode.z, nodeType, false, false);
            RoadMap.mapNodes.add(newNode);
            mergeNodesList.add(newNode);
        }

        mergeNodesList.add(lineEndNode);
        if (DEBUG) LOG.info("mergeNodesList size = {}",mergeNodesList.size());
        changeManager.addChangeable( new ChangeManager.LinearLineChanger(this.lineStartNode, lineEndNode, mergeNodesList, connectionType));
        connectNodes(mergeNodesList, connectionType);

        canAutoSave = true;

    }

    public static void connectNodes(LinkedList<MapNode> mergeNodesList, int connectionType)  {
        canAutoSave = false;
        for (int j = 0; j < mergeNodesList.size() - 1; j++) {
            MapNode startNode = mergeNodesList.get(j);
            MapNode endNode = mergeNodesList.get(j+1);
            MapPanel.createConnectionBetween(startNode,endNode,connectionType);
        }
        canAutoSave = true;
    }

    public boolean isLineCreated() {
        return this.lineNodeList.size() >0;
    }

    // getters
    public MapNode getLineStartNode() { return this.lineStartNode; }

    public int getInterpolationPointDistance() { return this.interpolationPointDistance; }

    //
    // setters
    //

    public void setInterpolationDistance(int distance) {
        this.interpolationPointDistance = distance;
    }
}
