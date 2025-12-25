package AutoDriveEditor.Classes.Curves;

import AutoDriveEditor.Classes.SnapShot;
import AutoDriveEditor.RoadNetwork.Connection;
import AutoDriveEditor.RoadNetwork.MapNode;

import java.awt.geom.Point2D;
import java.util.ArrayList;
import java.util.LinkedList;
import java.util.Map;

import static AutoDriveEditor.AutoDriveEditor.*;
import static AutoDriveEditor.Classes.Util_Classes.LoggerUtils.LOG;
import static AutoDriveEditor.Classes.Util_Classes.MathUtils.roundUpDoubleToDecimalPlaces;
import static AutoDriveEditor.GUI.MapPanel.createConnectionBetween;
import static AutoDriveEditor.GUI.Menus.DebugMenu.Logging.LogCurveInfoMenu.bDebugLogCurveInfo;
import static AutoDriveEditor.RoadNetwork.MapNode.*;
import static AutoDriveEditor.XMLConfig.AutoSave.resumeAutoSaving;
import static AutoDriveEditor.XMLConfig.AutoSave.suspendAutoSaving;
import static AutoDriveEditor.XMLConfig.EditorXML.controlPointMoveScaler;
import static AutoDriveEditor.XMLConfig.EditorXML.curveControlPointDefault;

public class BezierCurve extends CurveBase {

    public static final String CURVE_TYPE_BEZIER = "Bezier";

    private MapNode startNode;
    private MapNode endNode;

    /**
     * Constructor for the BezierCurve class, initializes the
     * curve and generates the initial interpolation points.
     *
     * @param startNode Node to start the curve from
     * @param endNode   Node to end the curve at
     */
    public BezierCurve(MapNode startNode, MapNode endNode) {
        // We have to call super here to let the BaseCurve class constructor initialize
        // the base code and all the common data structures needed for the curve.
        super();
        // initialize the curve start and end points
        this.startNode = startNode;
        this.endNode = endNode;
        // initialize the control points
        initControlPoints();
        // set the initial number of control points ( will also trigger initial curve generation )
        setNumCurrentControlPoints(curveControlPointDefault);
    }

    /**
     * Get the minimum number of nodes for this curve
     * This is the minimum number of control points that can be used to create the curve.
     */
    @Override
    public int getMinControlPoints() { return 1; }

    /**
     * The maximum number of nodes for this curve
     * This is the maximum number of control points that can be used to create the curve.
     */
    @Override
    public int getMaxControlPoints() { return 4;}

    /**
     * Move the specified MapNode by the specified distance.
     * @param controlNode The MapNode to move
     * @param diffX The distance to move in the X direction
     * @param diffY The distance to move in the Z direction
     * @param applyScaling If true, apply the controlPointMoveScaler to the movement
     */
    @Override
    public void moveControlNodeBy(MapNode controlNode, double diffX, double diffY, boolean applyScaling) {
        for (Map.Entry<Integer, MapNode> entry : controlPointsMap.entrySet()) {
            if (entry.getValue().equals(controlNode)) {
                controlNode.x = roundUpDoubleToDecimalPlaces(controlNode.x + diffX, 3);
                controlNode.z = roundUpDoubleToDecimalPlaces(controlNode.z + diffY, 3);

                Point2D oldLocation = virtualControlPointMap.get(controlNode);
                double adjustX = (applyScaling) ? (diffX * controlPointMoveScaler) : diffX;
                double adjustY = (applyScaling) ? (diffY * controlPointMoveScaler) : diffY;
                double newX = oldLocation.getX() + adjustX;
                double newY = oldLocation.getY() + adjustY;
                virtualControlPointMap.get(controlNode).setLocation(newX, newY);
                return;
            }
        }
    }

    /**
     * Initialize all the control points set using the Min and Max settings
     * @see #getMinControlPoints()
     * @see #getMaxControlPoints()
     */
    @Override
    protected void initControlPoints() {
        double controlX = this.startNode.x * .5 + this.endNode.x * .5;
        double controlZ = this.startNode.z * .5 + this.endNode.z * .5;
        for (int i=0; i < getMaxControlPoints(); i++) {
            MapNode controlPoint = createControlNode(controlX, controlZ);
            controlPointsMap.put(i, controlPoint);
            virtualControlPointMap.put(controlPoint, new Point2D.Double(controlPoint.x, controlPoint.z));
        }
    }

    /**
     * This method generates the interpolation points for the curve.
     * It calculates the points in-between the start and end nodes
     * based on the number of interpolations set.
     *
     */
    @SuppressWarnings("DataFlowIssue")
    @Override
    protected void generateInterpolationPoints() {
        // clear the previous node preview list
        getCurvePreviewNodesList().clear();
        // calculate the length of each iteration
        float iterationLength = (float) (1.0 / (this.getNumInterpolations() +1));
        // calculate all the points in-between the start and end nodes
        int id = 0;
        for (int i = 1; i < getNumInterpolations() + 1; i++) {
            double step = iterationLength * i;
            Point2D.Double point = calcPointsForCurve(this.startNode, this.endNode, step);
            // createSetting the preview node, we don't specify a Y value as it's just for display purposes only.
            MapNode newNode = createMapNode(id, point.getX(), -1, point.getY(), this.getCurvePriority(), false);
            getCurvePreviewNodesList().add(newNode);
            id++;
        }
    }

    /**
     * Generate the connections between all nodes in the list.
     */
    @Override
    protected void generateConnections() {
        LinkedList<MapNode> curveNodesList = getCurvePreviewNodesList();
        ArrayList<Connection> curveConnectionList = getCurvePreviewConnectionList();

        curveConnectionList.clear();
        curveConnectionList.add(new Connection(this.startNode, curveNodesList.getFirst(), getCurveType()));
        for (int j = 0; j < curveNodesList.size() - 1; j++) {
            MapNode startNode = curveNodesList.get(j);
            MapNode endNode = curveNodesList.get(j + 1);
            curveConnectionList.add(new Connection(startNode, endNode, getCurveType()));
        }
        curveConnectionList.add(new Connection(curveNodesList.getLast(), this.endNode, getCurveType()));
    }

    // Based on the number of control points, calculate the curve points
    /**
     * Calculate the points for the curve based on the number of control points.
     * It calls the required method for the number of control points set.
     *
     * @param startNode The starting MapNode of the curve
     * @param endNode   The ending MapNode of the curve
     * @param t         The parameter t, which varies from 0 to 1 along the curve
     * @return A Point2D.Double representing the calculated point on the curve
     */
    private Point2D.Double calcPointsForCurve(MapNode startNode, MapNode endNode, double t) {
        switch (getNumCurrentControlPoints()) {
            case 1:
                return calcPointsForQuadraticCurve(startNode, endNode, t);
            case 2:
                return calcPointsForCubicCurve(startNode, endNode, t);
            case 3:
                return calcPointsForQuarticCurve(startNode, endNode, t);
            case 4:
                return calcPointsForQuinticCurve(startNode, endNode, t);
            default:
                return null;

        }
    }

    /**
     * Calculates a point on the quadratic Bézier curve using 1 control point.
     * @param startNode starting MapNode of the curve
     * @param endNode ending MapNode of the curve
     * @param t parameter between 0 and 1 representing the position on the curve
     * @return Point2D.Double representing the calculated point on the curve
     */
    public Point2D.Double calcPointsForQuadraticCurve(MapNode startNode, MapNode endNode, double t) {
        Point2D cp1 = this.virtualControlPointMap.get(controlPointsMap.get(0));
        double oneMinusT = 1 - t;
        double powOneMinusT = Math.pow(oneMinusT, 2);
        double t2 = Math.pow(t, 2);
        Point2D.Double point = new Point2D.Double();
        point.x = powOneMinusT * startNode.x + 2 *
                  oneMinusT * t * cp1.getX() + t2 *
                  endNode.x;
        point.y = powOneMinusT * startNode.z + 2 *
                  oneMinusT * t * cp1.getY() + t2 *
                  endNode.z;
        return point;
    }

    /**
     * Calculates a point on the cubic Bézier curve using 2 control points.
     * @param startNode starting MapNode of the curve
     * @param endNode ending MapNode of the curve
     * @param t parameter between 0 and 1 representing the position on the curve
     * @return Point2D.Double representing the calculated point on the curve
     */
    public Point2D.Double calcPointsForCubicCurve(MapNode startNode, MapNode endNode, double t) {
        Point2D cp1 = this.virtualControlPointMap.get(controlPointsMap.get(0));
        Point2D cp2 = this.virtualControlPointMap.get(controlPointsMap.get(1));
        double oneMinusT = 1 - t;
        double oneMinusT2 = Math.pow(oneMinusT, 2);
        double t2 = Math.pow(t, 2);
        Point2D.Double point = new Point2D.Double();
        point.x = oneMinusT2 * oneMinusT * startNode.x + 3 *
                  oneMinusT2 * t * cp1.getX() + 3 *
                  oneMinusT * t2 * cp2.getX() + t2 *
                  t * endNode.x;
        point.y = oneMinusT2 * oneMinusT * startNode.z + 3 *
                  oneMinusT2 * t * cp1.getY() + 3 *
                  oneMinusT * t2 * cp2.getY() + t2 *
                  t * endNode.z;
        return point;
    }

    //
    // GitHub submission for Quartic/Quintic curve by whitevamp.. Many Thanks
    //

    /**
     * Calculates a point on the quartic Bézier curve using 3 control points.
     * @param startNode starting MapNode of the curve
     * @param endNode ending MapNode of the curve
     * @param t parameter between 0 and 1 representing the position on the curve
     * @return Point2D.Double representing the calculated point on the curve
     */
    public Point2D.Double calcPointsForQuarticCurve(MapNode startNode, MapNode endNode, double t) {
        Point2D cp1 = this.virtualControlPointMap.get(controlPointsMap.get(0));
        Point2D cp2 = this.virtualControlPointMap.get(controlPointsMap.get(1));
        Point2D cp3 = this.virtualControlPointMap.get(controlPointsMap.get(2));
        double oneMinusT = 1 - t;
        double oneMinusT2 = Math.pow(oneMinusT, 2);
        double oneMinusT3 = Math.pow(oneMinusT, 3);
        double oneMinusT4 = Math.pow(oneMinusT, 4);
        double t2 = Math.pow(t, 2);
        double t3 = Math.pow(t, 3);
        double t4 = Math.pow(t, 4);

        Point2D.Double point = new Point2D.Double();
        point.x = oneMinusT4 * startNode.x +
                4 * oneMinusT3 * t * cp1.getX() +
                6 * oneMinusT2 * t2 * cp2.getX() +
                4 * oneMinusT * t3 * cp3.getX() +
                t4 * endNode.x;
        point.y = oneMinusT4 * startNode.z +
                4 * oneMinusT3 * t * cp1.getY() +
                6 * oneMinusT2 * t2 * cp2.getY() +
                4 * oneMinusT * t3 * cp3.getY() +
                t4 * endNode.z;
        return point;
    }

    /**
     * Calculates a point on the quintic Bézier curve using 4 control points.
     * @param startNode starting MapNode of the curve
     * @param endNode ending MapNode of the curve
     * @param t parameter between 0 and 1 representing the position on the curve
     * @return Point2D.Double representing the calculated point on the curve
     */
    public Point2D.Double calcPointsForQuinticCurve(MapNode startNode, MapNode endNode, double t) {
        Point2D cp1 = this.virtualControlPointMap.get(controlPointsMap.get(0));
        Point2D cp2 = this.virtualControlPointMap.get(controlPointsMap.get(1));
        Point2D cp3 = this.virtualControlPointMap.get(controlPointsMap.get(2));
        Point2D cp4 = this.virtualControlPointMap.get(controlPointsMap.get(3));
        double oneMinusT = 1 - t;
        double oneMinusT2 = Math.pow(oneMinusT, 2);
        double oneMinusT3 = Math.pow(oneMinusT, 3);
        double oneMinusT4 = Math.pow(oneMinusT, 4);
        double oneMinusT5 = Math.pow(oneMinusT, 5);
        double t2 = Math.pow(t, 2);
        double t3 = Math.pow(t, 3);
        double t4 = Math.pow(t, 4);
        double t5 = Math.pow(t, 5);

        Point2D.Double point = new Point2D.Double();
        point.x = oneMinusT5 * startNode.x +
                5 * oneMinusT4 * t * cp1.getX() +
                10 * oneMinusT3 * t2 * cp2.getX() +
                10 * oneMinusT2 * t3 * cp3.getX() +
                5 * oneMinusT * t4 * cp4.getX() +
                t5 * endNode.x;
        point.y = oneMinusT5 * startNode.z +
                5 * oneMinusT4 * t * cp1.getY() +
                10 * oneMinusT3 * t2 * cp2.getY() +
                10 * oneMinusT2 * t3 * cp3.getY() +
                5 * oneMinusT * t4 * cp4.getY() +
                t5 * endNode.z;
        return point;
    }

    /**
     * This method is used to commit the curve to the current map.
     * It uses the positions of the preview nodes to createSetting
     * the actual nodes added to the map.
     */
    @Override
    public void commitCurve() {

        suspendAutoSaving();
        LinkedList<MapNode> networkNodes  = new LinkedList<>();

        if (this.startNode.y != -1 && this.endNode.y == -1) {
            this.endNode.y = this.startNode.y;
        }
        if (this.endNode.y != -1 && this.startNode.y == -1) {
            this.startNode.y = this.endNode.y;
        }

        float yInterpolation = (float) ((endNode.y - startNode.y) / (this.getCurvePreviewNodesList().size() - 1));

        for (int j = 0; j < getCurvePreviewNodesList().size(); j++) {
            MapNode tempNode = getCurvePreviewNodesList().get(j);
            double heightMapY = startNode.y + ( yInterpolation * j);
            MapNode newNode = createNewNetworkNode(tempNode.x, heightMapY, tempNode.z, this.getCurvePriority(), false);
            networkNodes.add(newNode);
        }

        connectNodes(networkNodes);
        changeManager.addChangeable( new CurveChanger(new SnapShot(networkNodes)));
        resumeAutoSaving();
        curveManager.getCurrentCurveWidget().setCurrentAnchor(null);

        if (bDebugLogCurveInfo) LOG.info("## {}.commitCurve() ## Created {} nodes", instanceName, getCurvePreviewNodesList().size());
    }

    /**
     * This method is used to connect all the nodes in the curve.
     * @param nodesList List of nodes added to map
     */
    private void connectNodes(LinkedList<MapNode> nodesList) {
        createConnectionBetween(startNode, nodesList.getFirst(), getCurveType());
        for (int j = 0; j < nodesList.size() - 1; j++) {
            MapNode startNode = nodesList.get(j);
            MapNode endNode = nodesList.get(j+1);
            createConnectionBetween(startNode,endNode, getCurveType());
        }
        createConnectionBetween(nodesList.getLast(), endNode, getCurveType());
    }

    /**
     * This method will attempt to swap the anchor points and all control
     * node positions to reverse the curve direction and maintain the curve shape.
     * <br>
     * NOTE: not fully tested, have not found a failure so far.
     */
    @Override
    public void swapCurveDirection() {
        // call the base class method to swap the start and end nodes
        MapNode tempNode = startNode;
        startNode = endNode;
        endNode = tempNode;
        // swap the control points
        for (int i = 0; i < getNumCurrentControlPoints() / 2; i++) {
            // Get the control nodes to swap
            MapNode nodeA = controlPointsMap.get(i);
            Point2D nodeAVirtual = virtualControlPointMap.get(nodeA);
            MapNode nodeB = controlPointsMap.get(getNumCurrentControlPoints() - i - 1);
            Point2D nodeBVirtual = virtualControlPointMap.get(nodeB);

            // Swap their positions
            double tempX = nodeA.x;
            double tempZ = nodeA.z;
            Point2D.Double tempPoint = new Point2D.Double(nodeAVirtual.getX(), nodeAVirtual.getY());
            nodeA.x = nodeB.x;
            nodeA.z = nodeB.z;
            nodeAVirtual.setLocation(nodeBVirtual.getX(), nodeBVirtual.getY());
            nodeB.x = tempX;
            nodeB.z = tempZ;
            nodeBVirtual.setLocation(tempPoint.getX(), tempPoint.getY());
        }
        updateCurve();
        widgetManager.updateAllWidgets();
    }

    /**
     * Check if the given node is a curve anchor node.
     * Anchor nodes are either the start or end node of the curve.
     *
     * @param node The MapNode to check
     * @return true if the node is a curve anchor node, false otherwise
     */
    @Override
    public boolean isCurveAnchorNode(MapNode node) {
        return node.equals(startNode) || node.equals(endNode);
    }



    /**
     * Return the start node of the curve
     * @return Curve start node
     */
    public MapNode getStartNode() { return startNode; }

    /**
     * Return the end node of the curve
     * @return Curve end node
     */
    public MapNode getEndNode() { return endNode; }

    public int getRotationAngle(MapNode startNode) { return 0; }

}


