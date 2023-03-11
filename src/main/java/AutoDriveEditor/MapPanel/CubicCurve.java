package AutoDriveEditor.MapPanel;

import AutoDriveEditor.GUI.Buttons.CurveBaseButton;
import AutoDriveEditor.GUI.GUIBuilder;
import AutoDriveEditor.RoadNetwork.MapNode;
import AutoDriveEditor.RoadNetwork.RoadMap;

import java.awt.geom.Point2D;
import java.util.LinkedList;

import static AutoDriveEditor.AutoDriveEditor.buttonManager;
import static AutoDriveEditor.AutoDriveEditor.changeManager;
import static AutoDriveEditor.GUI.MenuBuilder.bDebugLogCurveInfo;
import static AutoDriveEditor.Listeners.MouseListener.prevMousePosX;
import static AutoDriveEditor.Listeners.MouseListener.prevMousePosY;
import static AutoDriveEditor.MapPanel.MapPanel.*;
import static AutoDriveEditor.RoadNetwork.MapNode.*;
import static AutoDriveEditor.Utils.LoggerUtils.LOG;
import static AutoDriveEditor.Utils.MathUtils.roundUpDoubleToDecimalPlaces;
import static AutoDriveEditor.XMLConfig.EditorXML.*;

public class CubicCurve {

    public LinkedList<MapNode> curveNodesList;
    private MapNode curveStartNode;
    private MapNode curveEndNode;
    private MapNode controlPoint1;
    private MapNode controlPoint2;
    private final Point2D.Double virtualControlPoint1;
    private final Point2D.Double virtualControlPoint2;

    private int numInterpolationPoints;
    private int nodeType;
    private boolean isReversePath;
    private boolean isDualPath;

    public CubicCurve(MapNode startNode, MapNode endNode) {
        this.curveNodesList = new LinkedList<>();
        this.curveStartNode = startNode;
        this.curveEndNode = endNode;
        this.numInterpolationPoints = GUIBuilder.numIterationsSlider.getValue();
        if (this.numInterpolationPoints < 2 ) this.numInterpolationPoints = 2 ;
        this.controlPoint1 = new MapNode(0, startNode.x,0, endNode.z, NODE_FLAG_CONTROL_POINT, false, true);
        this.controlPoint2 = new MapNode(1, endNode.x,0, startNode.z, NODE_FLAG_CONTROL_POINT, false, true);
        this.virtualControlPoint1 = new Point2D.Double(controlPoint1.x,controlPoint1.z);
        this.virtualControlPoint2 = new Point2D.Double(controlPoint2.x,controlPoint2.z);
        this.isReversePath = GUIBuilder.curvePathReverse.isSelected();
        this.isDualPath = GUIBuilder.curvePathDual.isSelected();
        this.nodeType = GUIBuilder.curvePathRegular.isSelected() ? NODE_FLAG_STANDARD : NODE_FLAG_SUBPRIO;
        this.updateCurve();
        GUIBuilder.curveOptionsPanel.setVisible(true);
    }

    public void getInterpolationPointsForCurve (MapNode startNode, MapNode endNode) {

        if ((startNode == null || endNode == null || this.numInterpolationPoints < 1 )) return;

        double step = 1/(double)this.numInterpolationPoints;
        curveNodesList.clear();

        // first we add the starting node

        curveNodesList.add(curveStartNode);

        // now we calculate all the points in-between the start and end nodes
        // i=step makes sure we skip the first node to calculate as it's the curveStartNode
        //
        // i+step<1.0001 means we compare one node ahead, we don't calculate the end node (as it's curveEndNode)
        // rounding errors mean we can't compare i+step<1 as the last node would make i = 1.0000000000004 - 1.00000000000010
        // we would be one node missing due to the comparison being fulfilled.

        int id = 0;
        for(double i=step;i+step<1.0001;i += step) {
            Point2D.Double point = calcPointsForCurve(startNode, endNode, this.virtualControlPoint1.x, this.virtualControlPoint1.y, this.virtualControlPoint2.x, this.virtualControlPoint2.y, i);
            curveNodesList.add(new MapNode(id,point.getX(),-1,point.getY(), this.nodeType, false, false));
            if (i+step >=1.0001 ) LOG.info("WARNING -- last node was not calculated, this should not happen!! -- step = {} ,  ", i+step);
            id++;
        }

        //add the end node to complete the curve

        curveNodesList.add(curveEndNode);
    }

    public Point2D.Double calcPointsForCurve(MapNode startNode, MapNode endNode, double pointer1x, double pointer1y, double pointer2x, double pointer2y, double precision) {
        Point2D.Double point = new Point2D.Double();
        double abs = Math.abs(Math.pow((1 - precision), 3));
        point.x = abs * startNode.x + 3 * Math.pow((1 - precision), 2) * precision * pointer1x + 3 * Math.abs((1 - precision)) * Math.pow(precision, 2) * pointer2x + Math.abs(Math.pow(precision, 3)) * endNode.x;
        point.y = abs * startNode.z + 3 * Math.pow((1 - precision), 2) * precision * pointer1y + 3 * Math.abs((1 - precision)) * Math.pow(precision, 2) * pointer2y + Math.abs(Math.pow(precision, 3)) * endNode.z;
        return point;
    }

    public void updateCurve() {
        if ((this.curveStartNode != null && this.curveEndNode !=null && this.numInterpolationPoints >= 1)) {
            getInterpolationPointsForCurve(this.curveStartNode,this.curveEndNode);
        }
    }

    public void commitCurve() {
        canAutoSave = false;
        LinkedList<MapNode> mergeNodesList  = new LinkedList<>();

        mergeNodesList.add(curveStartNode);

        if (this.curveStartNode.y != -1 && this.curveEndNode.y == -1) {
            this.curveEndNode.y = this.curveStartNode.y;
        }
        if (this.curveEndNode.y != -1 && this.curveStartNode.y == -1) {
            this.curveStartNode.y = this.curveEndNode.y;
        }

        float yInterpolation = (float) ((curveEndNode.y - curveStartNode.y) / (this.curveNodesList.size() - 1));

        for (int j = 1; j < curveNodesList.size() - 1; j++) {
            MapNode tempNode = curveNodesList.get(j);
            double heightMapY = getYValueFromHeightMap(tempNode.x, tempNode.z);
            if (heightMapY == -1) {
                heightMapY = curveStartNode.y + ( yInterpolation * j);
            }
            MapNode newNode = new MapNode(RoadMap.networkNodesList.size() + 1, tempNode.x, heightMapY, tempNode.z, this.nodeType, false, false);
            RoadMap.networkNodesList.add(newNode);
            mergeNodesList.add(newNode);
        }

        mergeNodesList.add(curveEndNode);
        changeManager.addChangeable( new CurveBaseButton.CurveChanger(mergeNodesList, isReversePath, isDualPath));
        connectNodes(mergeNodesList, isReversePath, isDualPath);

        canAutoSave = true;

        if (bDebugLogCurveInfo) LOG.info("CubicCurve created {} nodes", mergeNodesList.size() - 2 );
    }

    public static void connectNodes(LinkedList<MapNode> mergeNodesList, boolean reversePath, boolean dualPath)  {
        for (int j = 0; j < mergeNodesList.size() - 1; j++) {
            MapNode startNode = mergeNodesList.get(j);
            MapNode endNode = mergeNodesList.get(j+1);
            if (reversePath) {
                MapPanel.createConnectionBetween(startNode,endNode,CONNECTION_REVERSE);
            } else if (dualPath) {
                MapPanel.createConnectionBetween(startNode,endNode,CONNECTION_DUAL);
            } else {
                MapPanel.createConnectionBetween(startNode,endNode,CONNECTION_STANDARD);
            }
        }
    }

    public void clear() {
        this.curveNodesList.clear();
        this.controlPoint1 = null;
        this.controlPoint2 = null;
        this.curveStartNode = null;
        this.curveEndNode = null;
    }

    public void updateVirtualControlPoint1(double diffX, double diffY) {
        if (buttonManager.getCurrentButtonID().equals("Cubic")) {
            this.virtualControlPoint1.x += diffX * controlPointMoveScaler;
            this.virtualControlPoint1.y += diffY * controlPointMoveScaler;
        } else {
            this.virtualControlPoint1.x += diffX;
            this.virtualControlPoint1.y += diffY;
        }

        this.updateCurve();
    }

    public void updateVirtualControlPoint2(double diffX, double diffY) {
        if (buttonManager.getCurrentButtonID().equals("CubicCurveButton")) {
            this.virtualControlPoint2.x += diffX * controlPointMoveScaler;
            this.virtualControlPoint2.y += diffY * controlPointMoveScaler;
        } else {
            this.virtualControlPoint2.x += diffX;
            this.virtualControlPoint2.y += diffY;
        }
        this.updateCurve();
    }

    public void updateControlPoint1(double diffX, double diffY) {
        this.controlPoint1.x = roundUpDoubleToDecimalPlaces(this.controlPoint1.x + diffX, 3);
        this.controlPoint1.z = roundUpDoubleToDecimalPlaces(this.controlPoint1.z + diffY, 3);
        updateVirtualControlPoint1(diffX, diffY);
        updateCurve();
    }

    public void updateControlPoint2(double diffX, double diffY) {
        this.controlPoint2.x = roundUpDoubleToDecimalPlaces(this.controlPoint2.x + diffX, 3);
        this.controlPoint2.z = roundUpDoubleToDecimalPlaces(this.controlPoint2.z + diffY, 3);
        updateVirtualControlPoint2(diffX, diffY);
        updateCurve();
    }

    public void moveControlPoint1(double diffX, double diffY) {
        Point2D point = calcScaledDifference(this.controlPoint1, diffX, diffY);
        this.controlPoint1.x = roundUpDoubleToDecimalPlaces(this.controlPoint1.x + point.getX(), 3);
        this.controlPoint1.z = roundUpDoubleToDecimalPlaces(this.controlPoint1.z + point.getY(), 3);
        updateVirtualControlPoint1(point.getX(), point.getY());
        this.updateCurve();
    }

    public void moveControlPoint2(double diffX, double diffY) {
        Point2D point = calcScaledDifference(this.controlPoint2, diffX, diffY);
        this.controlPoint2.x = roundUpDoubleToDecimalPlaces(this.controlPoint2.x + point.getX(), 3);
        this.controlPoint2.z = roundUpDoubleToDecimalPlaces(this.controlPoint2.z + point.getY(), 3);
        updateVirtualControlPoint2(point.getX(), point.getY());
        this.updateCurve();
    }


    private Point2D calcScaledDifference(MapNode node, double diffX, double diffY) {
        double scaledDiffX;
        double scaledDiffY;
        if (bGridSnap) {
            Point2D p = screenPosToWorldPos((int) (prevMousePosX + diffX), (int) (prevMousePosY + diffY));
            double newX, newY;
            if (bGridSnapSubs) {
                newX = Math.round(p.getX() / (gridSpacingX / (gridSubDivisions + 1))) * (gridSpacingX / (gridSubDivisions + 1));
                newY = Math.round(p.getY() / (gridSpacingY / (gridSubDivisions + 1))) * (gridSpacingY / (gridSubDivisions + 1));
            } else {
                newX = Math.round(p.getX() / gridSpacingX) * gridSpacingX;
                newY = Math.round(p.getY() / gridSpacingY) * gridSpacingY;
            }
            scaledDiffX = newX - node.x;
            scaledDiffY = newY - node.z;

        } else {
            scaledDiffX = roundUpDoubleToDecimalPlaces((diffX * mapZoomFactor) / zoomLevel, 3);
            scaledDiffY = roundUpDoubleToDecimalPlaces((diffY * mapZoomFactor) / zoomLevel, 3);
        }
        return new Point2D.Double(scaledDiffX, scaledDiffY);
    }

    public boolean isReversePath() { return isReversePath; }

    public boolean isDualPath() { return isDualPath; }

    public boolean isCurveAnchorPoint(MapNode node) { return node == this.curveStartNode || node == this.curveEndNode; }

    @SuppressWarnings("unused")
    public boolean isCurveValid() {
        return this.curveNodesList != null && this.controlPoint1 !=null && this.controlPoint2 != null && this.curveNodesList.size() > 2;
    }

    public boolean isControlPoint(MapNode node) { return node == this.controlPoint1 || node == this.controlPoint2; }

    //
    // getters
    //

    public int getNodeType() { return this.nodeType; }

    @SuppressWarnings("unused")
    public int getNumInterpolationPoints() { return this.numInterpolationPoints; }

    @SuppressWarnings("unused")
    public LinkedList<MapNode> getCurveNodes() { return this.curveNodesList; }

    public MapNode getCurveStartNode() { return this.curveStartNode; }

    public MapNode getCurveEndNode() { return this.curveEndNode; }

    public MapNode getControlPoint1() { return this.controlPoint1; }

    public MapNode getControlPoint2() { return this.controlPoint2; }

    //
    // setters
    //

    public void setReversePath(boolean isSelected) {
        this.isReversePath = isSelected;
    }

    public void setDualPath(boolean isSelected) {
        this.isDualPath = isSelected;
    }

    public void setNodeType(int nodeType) {
        this.nodeType = nodeType;
        if (nodeType == NODE_FLAG_SUBPRIO) {
            for (int j = 1; j < curveNodesList.size() - 1; j++) {
                MapNode tempNode = curveNodesList.get(j);
                tempNode.flag = 1;
            }
        } else {
            for (int j = 1; j < curveNodesList.size() - 1; j++) {
                MapNode tempNode = curveNodesList.get(j);
                tempNode.flag = 0;
            }
        }
    }

    public void setNumInterpolationPoints(int points) {
        this.numInterpolationPoints = points;
        if (this.curveStartNode != null && this.curveEndNode !=null) {
            getInterpolationPointsForCurve(this.curveStartNode,this.curveEndNode);
        }
    }

    public void setCurveStartNode(MapNode curveStartNode) {
        this.curveStartNode = curveStartNode;
        this.updateCurve();
    }

    public void setCurveEndNode(MapNode curveEndNode) {
        this.curveEndNode = curveEndNode;
        this.updateCurve();
    }
}


