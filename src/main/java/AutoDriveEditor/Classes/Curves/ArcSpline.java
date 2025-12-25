package AutoDriveEditor.Classes.Curves;

import AutoDriveEditor.Classes.Interfaces.ArcSplineInterface;
import AutoDriveEditor.Classes.SnapShot;
import AutoDriveEditor.RoadNetwork.Connection;
import AutoDriveEditor.RoadNetwork.MapNode;

import java.awt.*;
import java.awt.event.MouseEvent;
import java.awt.geom.AffineTransform;
import java.awt.geom.Point2D;
import java.util.ArrayList;
import java.util.LinkedList;
import java.util.Map;

import static AutoDriveEditor.AutoDriveEditor.*;
import static AutoDriveEditor.Classes.Util_Classes.ColourUtils.lighten;
import static AutoDriveEditor.Classes.Util_Classes.LoggerUtils.LOG;
import static AutoDriveEditor.Classes.Util_Classes.MathUtils.roundUpDoubleToDecimalPlaces;
import static AutoDriveEditor.GUI.Buttons.BaseButton.drawArrowBetween;
import static AutoDriveEditor.GUI.MapPanel.*;
import static AutoDriveEditor.GUI.Menus.DebugMenu.Logging.LogCurveInfoMenu.bDebugLogCurveInfo;
import static AutoDriveEditor.RoadNetwork.MapNode.*;
import static AutoDriveEditor.XMLConfig.AutoSave.resumeAutoSaving;
import static AutoDriveEditor.XMLConfig.AutoSave.suspendAutoSaving;
import static AutoDriveEditor.XMLConfig.EditorXML.*;
import static java.lang.Math.PI;

public class ArcSpline extends CurveBase implements ArcSplineInterface {

    /**
     * Creates a circular curve with definable start and end in degrees.
     * NOTE: This is currently a Work In Progress
     */

    // NOTE: used when registering curve types in CurveManager
    public static final String CURVE_TYPE_ARCSPLINE = "ArcSpline (Work In Progress)";

    // Minimum radius for the arc
    private static final double MIN_RADIUS = 2f;

    // The centre point of the ArcSpline
    private final Point2D centrePoint;

    // Control points for the ArcSpline
    private MapNode startAngleNode;
    private MapNode endAngleNode;
    private MapNode centreNode;
    private MapNode adjustRadiusNode;

    // Rotation angles for the start and end nodes
    private double startNodeAngle = -90;
    private double endNodeAngle = 90;

    // Initial radius of the arc
    private double initialRadius = 8;

    // Flag to indicate if the arc is active and it's direction
    private boolean arcActive;
    private boolean bClockwiseConnections = true;

    /**
     * Constructor for the ArcSpline class, initializes the
     * curve and generates the initial interpolation points
     * based on the selected starting position.
     *
     * @param startPoint centre point of the ArcSpline in world coordinates
     */
    public ArcSpline(Point2D startPoint) {
        // Call super to let the BaseCurve constructor set up the base code needed for the curve.
        super();
        if (bDebugLogCurveInfo) LOG.info("## ArcSpline.constructor ## Creating Point ArcSpline at {} , {}", startPoint.getX(), startPoint.getY());
        this.centrePoint = startPoint;
        initControlPoints();
        //radiusAdjustImage = getSVGBufferImage(UNDO_ICON, (int) nodeSizeScaled, (int) nodeSizeScaled);
    }

    /**
     * Returns the minimum number of control nodes this curve supports.
     */
    @Override
    public int getMinControlPoints() {
        return 4;
    }

    /**
     * Returns the maximum number of control nodes this curve supports.
     */
    @Override
    public int getMaxControlPoints() {
        return 4;
    }

    /**
     * Returns the current number of control points in use.
     * For ArcSpline, it is always 4 (centre, start, end, radius).
     *
     * @return Current number of control points, in Integer format
     */
    @Override
    public int getNumCurrentControlPoints() {
        return getMaxControlPoints();
    }

    /**
     * Initializes the control points for the ArcSpline.
     * Creates the centre node and the start and end nodes based on the initial radius.
     */
    @Override
    protected void initControlPoints() {
        centreNode = createControlNode(centrePoint.getX(), centrePoint.getY());
        controlPointsMap.put(0, centreNode);

        startAngleNode = createRotationNode(centrePoint.getX() - initialRadius, centrePoint.getY());
        controlPointsMap.put(1, startAngleNode);

        endAngleNode = createRotationNode(centrePoint.getX() + initialRadius, centrePoint.getY());
        controlPointsMap.put(2, endAngleNode);

        adjustRadiusNode = createSpecialNode(centrePoint.getX() - (initialRadius / 2.0), centrePoint.getY());
        controlPointsMap.put(3, adjustRadiusNode);
    }

    /**
     * Returns ta list of all control points.
     *
     * @return (ArrayList) of all control nodes.
     */
    @Override
    public ArrayList<MapNode> getNodes() {
        return new ArrayList<>(controlPointsMap.values());
    }

    /**
     * swap the direction of the curve connections
     */
    @Override
    public void swapCurveDirection() {
        bClockwiseConnections = !bClockwiseConnections;
        updateCurve();

    }

    @Override
    public void mouseReleased(MouseEvent e) {
        // Handle mouse release event for ArcSpline
        if (e.getButton() == MouseEvent.BUTTON1) {
            if (!arcActive) {
                if (bDebugLogCurveInfo)
                    LOG.info("## {}.mouseReleased() ## ArcSpline: Setting initial control point position: {}, {}", instanceName, e.getX(), e.getY());
                // Create the centre node at the mouse position
                arcActive = true;
                generateInterpolationPoints();
            }
        }
    }

    @Override
    public void mouseMoved(MouseEvent e) {
        super.mouseMoved(e);
    }

    /**
     * Moves the specified node by the specified difference in x and y coordinates.
     *
     * @param controlPoint The control point to move.
     * @param diffX        The difference in x coordinate.
     * @param diffY        The difference in y coordinate.
     * @param applyScaling Whether to apply scaling to the movement.
     */
    @Override
    public void moveControlNodeBy(MapNode controlPoint, double diffX, double diffY, boolean applyScaling) {
        if (controlPoint == centreNode) {
            for (Map.Entry<Integer, MapNode> entry : controlPointsMap.entrySet()) {
                MapNode entryNode = entry.getValue();
                entryNode.x = roundUpDoubleToDecimalPlaces(entryNode.x + diffX, 3);
                entryNode.z = roundUpDoubleToDecimalPlaces(entryNode.z + diffY, 3);
            }
        } else if (controlPoint == adjustRadiusNode) {
            double intendedX = controlPoint.x + diffX;
            double intendedZ = controlPoint.z + diffY;
            clampNodeToLine(controlPoint, centreNode, startAngleNode, intendedX, intendedZ);
            updateArcRadiusFromAdjustNode();
        }
    }

    /**
     * Rotates the start node of the Arc based on the angle from the centre point
     * of the arc to the mouse pointer position.
     *
     * @param mouseX The x-coordinate of the mouse position.
     * @param mouseY The z-coordinate of the mouse position.
     * @return The movement difference calculated from previous position to the new position
     */
    @Override
    public Point2D rotateStartNode(MapNode rotationNode, int mouseX, int mouseY) {
        suspendAutoSaving();

        Point2D mouseWorld = screenPosToWorldPos(mouseX, mouseY);
        Point2D beforePos = new Point2D.Double(rotationNode.getX(), rotationNode.getZ());

        double theta = Math.atan2(centreNode.getZ() - mouseWorld.getY(), centreNode.getX() - mouseWorld.getX()) - PI / 2;
        if (theta < 0) theta += 2 * PI;

        int degree = (bRotationSnapEnabled) ? rotationStep : 1;
        double thetaRounded = Math.round(Math.toDegrees(theta) / degree) * degree;

        double step = thetaRounded - startNodeAngle;
        rotatePoint(rotationNode, centreNode.getWorldPosition2D(), step);

        //rotatePoint(rotationNode, centreNode.getWorldPosition2D(), step);
        updateRadiusAdjustNode();

        curveManager.updateAllCurves();
        widgetManager.updateAllWidgets();
        getMapPanel().repaint();
        resumeAutoSaving();
        startNodeAngle = thetaRounded;

        // Return the calculated difference from the start point to the new point; if result is 0.0,0.0 i.e. no movement, return null
        return new Point2D.Double(rotationNode.getX() - beforePos.getX(), rotationNode.getZ() - beforePos.getY());
    }

    /**
     * Rotates the end node of the Arc based on the angle from the centre point
     * of the arc to the mouse pointer position.
     *
     * @param mouseX The x-coordinate of the mouse position.
     * @param mouseY The z-coordinate of the mouse position.
     * @return Difference calculated from previous position to the new position, null if no movement
     *
     */
    @Override
    public Point2D rotateEndNode(MapNode rotationNode, int mouseX, int mouseY) {
        suspendAutoSaving();

        Point2D mouseWorld = screenPosToWorldPos(mouseX, mouseY);
        Point2D beforePos = new Point2D.Double(rotationNode.getX(), rotationNode.getZ());

        double theta = Math.atan2(centreNode.getZ() - mouseWorld.getY(), centreNode.getX() - mouseWorld.getX()) - PI / 2;
        if (theta < 0) theta += 2 * PI;

        int degree = (bRotationSnapEnabled) ? rotationStep : 1;
        double thetaRounded = Math.round(Math.toDegrees(theta) / degree) * degree;

        double step = thetaRounded - endNodeAngle;
        rotatePoint(rotationNode, centreNode.getWorldPosition2D(), step);

        curveManager.updateAllCurves();
        widgetManager.updateAllWidgets();
        getMapPanel().repaint();
        resumeAutoSaving();
        endNodeAngle = thetaRounded;

        Point2D.Double result = new Point2D.Double(rotationNode.getX() - beforePos.getX(), rotationNode.getZ() - beforePos.getY());
        // Return the calculated difference from the start point to the new point; if result is 0.0,0.0 i.e. no movement, return null
        return (result.getX() != 0.0 || result.getY() != 0.0) ? result : null;
    }

    /**
     * Rotates a point around a centre by a given angle.
     *
     * @param node   the MapNode to rotate
     * @param centre the centre point to rotate around
     * @param angle  the angle in degrees to rotate
     */
    public static void rotatePoint(MapNode node, Point2D.Double centre, double angle) {
        Point2D result = new Point2D.Double();
        AffineTransform rotation = new AffineTransform();
        double angleInRadians = Math.toRadians(angle);
        rotation.rotate(angleInRadians, centre.getX(), centre.getY());
        rotation.transform(new Point2D.Double(node.x, node.z), result);
        node.x = roundUpDoubleToDecimalPlaces(result.getX(), 3);
        node.z = roundUpDoubleToDecimalPlaces(result.getY(), 3);
    }

    /**
     * Update the position of the radius adjust node to be midway between the centre and start angle nodes.
     */
    private void updateRadiusAdjustNode() {
        double midX = (centreNode.getX() + startAngleNode.getX()) / 2.0;
        double midZ = (centreNode.getZ() + startAngleNode.getZ()) / 2.0;
        adjustRadiusNode.x = roundUpDoubleToDecimalPlaces(midX, 3);
        adjustRadiusNode.z = roundUpDoubleToDecimalPlaces(midZ, 3);
    }

    /**
     * Update the arc radius based on the position of the adjust radius node.
     * Recalculates the positions of the start and end nodes to maintain the arc shape.
     */
    private void updateArcRadiusFromAdjustNode() {
        double dx = adjustRadiusNode.getX() - centreNode.getX();
        double dz = adjustRadiusNode.getZ() - centreNode.getZ();
        double halfRadius = Math.sqrt(dx * dx + dz * dz);
        double newRadius = Math.max(halfRadius * 2, MIN_RADIUS); // Clamp to minimum

        // Update start node position along its angle
        double startAngleRad = Math.toRadians(startNodeAngle - 90);
        startAngleNode.x = roundUpDoubleToDecimalPlaces(centreNode.getX() + newRadius * Math.cos(startAngleRad), 3);
        startAngleNode.z = roundUpDoubleToDecimalPlaces(centreNode.getZ() + newRadius * Math.sin(startAngleRad), 3);

        // Update end node position along its angle
        double endAngleRad = Math.toRadians(endNodeAngle - 90);
        endAngleNode.x = roundUpDoubleToDecimalPlaces(centreNode.getX() + newRadius * Math.cos(endAngleRad), 3);
        endAngleNode.z = roundUpDoubleToDecimalPlaces(centreNode.getZ() + newRadius * Math.sin(endAngleRad), 3);

        // Update the stored radius
        setRadius(newRadius);
    }

    /**
     * Limit the node movement to a point that only exists along a straight line from the
     * start node to the end node
     *
     * @param node the node to clamp
     * @param lineStart The start node of the line
     * @param lineEnd The end node of the line
     * @param intendedX the intended x position of the node
     * @param intendedZ the intended z position of the node
     */
    private void clampNodeToLine(MapNode node, MapNode lineStart, MapNode lineEnd, double intendedX, double intendedZ) {
        double cx = lineStart.getX();
        double cz = lineStart.getZ();
        double sx = lineEnd.getX();
        double sz = lineEnd.getZ();

        double dx = sx - cx;
        double dz = sz - cz;
        double px = intendedX - cx;
        double pz = intendedZ - cz;

        double lenSq = dx * dx + dz * dz;
        double dot = (px * dx + pz * dz) / lenSq;

        // Clamp dot to [0,1] to keep node between endpoints
        dot = Math.max(0, Math.min(1, dot));

        // Calculate the projected point
        double projX = cx + dot * dx;
        double projZ = cz + dot * dz;

        // Enforce minimum radius: distance from centre to adjust node >= MIN_RADIUS / 2
        double minDist = MIN_RADIUS / 2.0;
        double dist = Math.hypot(projX - cx, projZ - cz);
        if (dist < minDist) {
            // Place the node at minDist along the line direction
            double lineLen = Math.sqrt(dx * dx + dz * dz);
            if (lineLen > 0) {
                double normDx = dx / lineLen;
                double normDz = dz / lineLen;
                projX = cx + normDx * minDist;
                projZ = cz + normDz * minDist;
            } else {
                projX = cx;
                projZ = cz;
            }
        }

        node.x = roundUpDoubleToDecimalPlaces(projX, 3);
        node.z = roundUpDoubleToDecimalPlaces(projZ, 3);
    }

    /**
     * Check if the specified node is a curve anchor.
     *
     * @param node The MapNode to check.
     * @return True if the node is a curve anchor, false otherwise.
     */
    @Override
    public boolean isCurveAnchorNode(MapNode node) {
        return false;
    }

    /**
     * Generates interpolation points along the arc defined by the start and end nodes.
     */
    @Override
    protected void generateInterpolationPoints() {
        getCurvePreviewNodesList().clear();
        generateArcPoints(centreNode.getX(), centreNode.getZ(), initialRadius, getNumInterpolations() + 2);
        generateConnections();
    }

    /**
     * Generates points along a circular arc.
     *
     * @param centerX   The x-coordinate of the arc's center.
     * @param centerY   The y-coordinate of the arc's center.
     * @param radius    The radius of the arc.
     * @param numPoints The number of points to generate along the arc.
     */
    private void generateArcPoints(double centerX, double centerY, double radius, int numPoints) {
        getCurvePreviewNodesList().clear();
        int id = 0;

        // Normalize angles to [0, 360)
        startNodeAngle = (startNodeAngle % 360 + 360) % 360;
        endNodeAngle = (endNodeAngle % 360 + 360) % 360;

        // Handle cases where the end angle is less than the start angle
        if (endNodeAngle < startNodeAngle) {
            endNodeAngle += 360;
        }

        double startRad = Math.toRadians(startNodeAngle - 90);
        double endRad = Math.toRadians(endNodeAngle - 90);
        double angleStep = (endRad - startRad) / (numPoints - 1);

        for (int i = 0; i < numPoints; i++) {
            double angle = startRad + i * angleStep;
            double x = centerX + radius * Math.cos(angle);
            double z = centerY + radius * Math.sin(angle);
            MapNode newNode = createMapNode(id, x, -1, z, this.getCurvePriority(), false);

            getCurvePreviewNodesList().add(newNode);
            id++;
        }
    }

    /**
     * Generates connections between all the nodes generated by the preview functions.
     */
    @Override
    protected void generateConnections() {
        LinkedList<MapNode> curveNodesList = getCurvePreviewNodesList();
        ArrayList<Connection> curveConnectionList = getCurvePreviewConnectionList();
        curveConnectionList.clear();
        if (bClockwiseConnections) {
            for (int j = 0; j < curveNodesList.size() - 1; j++) {
                MapNode startNode = curveNodesList.get(j);
                MapNode endNode = curveNodesList.get(j + 1);
                curveConnectionList.add(new Connection(startNode, endNode, getCurveType()));
            }
        } else {
            for (int j = curveNodesList.size(); j > 1; j--) {
                MapNode startNode = curveNodesList.get(j - 1);
                MapNode endNode = curveNodesList.get(j - 2);
                curveConnectionList.add(new Connection(startNode, endNode, getCurveType()));
            }
        }
    }

    /**
     * This method is used to commit the curve to the current map.
     * It uses the positions of the preview nodes to createSetting
     * the actual nodes added to the map.
     */
    @Override
    public void commitCurve() {

        suspendAutoSaving();
        LinkedList<MapNode> networkNodes = new LinkedList<>();

        if (this.startAngleNode.y != -1 && this.endAngleNode.y == -1) {
            this.endAngleNode.y = this.startAngleNode.y;
        }
        if (this.endAngleNode.y != -1 && this.startAngleNode.y == -1) {
            this.startAngleNode.y = this.endAngleNode.y;
        }

        float yInterpolation = (float) ((endAngleNode.y - startAngleNode.y) / (this.getCurvePreviewNodesList().size() - 1));

        for (int j = 0; j < getCurvePreviewNodesList().size(); j++) {
            MapNode tempNode = getCurvePreviewNodesList().get(j);
            double heightMapY = startAngleNode.y + (yInterpolation * j);
            MapNode newNode = createNewNetworkNode(tempNode.x, heightMapY, tempNode.z, this.getCurvePriority(), false);
            networkNodes.add(newNode);
        }

        connectNodes(networkNodes);
        changeManager.addChangeable(new CurveChanger(new SnapShot(networkNodes)));
        resumeAutoSaving();
        curveManager.getCurrentCurveWidget().setCurrentAnchor(null);

        if (bDebugLogCurveInfo)
            LOG.info("## {}.commitCurve() ## Created {} nodes", instanceName, getCurvePreviewNodesList().size());
    }

    /**
     * This method is used to connect all the nodes in the curve.
     *
     * @param nodesList List of nodes added to map
     */
    private void connectNodes(LinkedList<MapNode> nodesList) {
        if (bClockwiseConnections) {
            for (int j = 0; j < nodesList.size() - 1; j++) {
                MapNode startNode = nodesList.get(j);
                MapNode endNode = nodesList.get(j + 1);
                createConnectionBetween(startNode, endNode, getCurveType());
            }
        } else {
            for (int j = nodesList.size(); j > 1; j--) {
                MapNode startNode = nodesList.get(j - 1);
                MapNode endNode = nodesList.get(j - 2);
                createConnectionBetween(startNode, endNode, getCurveType());
            }
        }
    }

    /**
     * Returns the rotation angle for the given node.
     *
     * @param node The MapNode for which to get the rotation angle.
     * @return The rotation angle in degrees or 0 if the node is not a rotation node.
     */
    @Override
    public int getRotationAngle(MapNode node) {
        if (node == startAngleNode) {
            return (int) startNodeAngle;
        } else if (node == endAngleNode) {
            return (int) endNodeAngle;
        }
        return 0;
    }

    @Override
    public double getRadiusAngle() {
        Point2D centreNodeScreenPos = worldPosToScreenPos(centreNode.getX(), centreNode.getZ());
        Point2D startAngleNodeScreenPos = worldPosToScreenPos(startAngleNode.getX(), startAngleNode.getZ());

        double dx = startAngleNodeScreenPos.getX() - centreNodeScreenPos.getX();
        double dy = startAngleNodeScreenPos.getY() - centreNodeScreenPos.getY();
        return Math.atan2(dy, dx);

    }

    /**
     * Sets the radius of the arc and regenerates the interpolation points.
     *
     * @param radius The new radius for the arc.
     */
    public void setRadius(double radius) {
        this.initialRadius = radius;
        generateInterpolationPoints();
    }

    //
    // Draw the ArcSpline
    //

    @Override
    public void drawToScreen(Graphics g) {
        if (!getCurvePreviewNodesList().isEmpty()) {
            Color colour;
            Graphics2D g2d = (Graphics2D) g.create();
            g2d.setComposite(AlphaComposite.SrcOver.derive(0.55f));
            // draw the preview nodes
            for (int i = 1; i < getCurvePreviewNodesList().size() - 1; i++) {
                MapNode node = getCurvePreviewNodesList().get(i);
                Point2D nodeScreenPos = worldPosToScreenPos(node.getX(), node.getZ());
                colour = (node.getFlag() == NODE_FLAG_REGULAR) ? colourNodeRegular : colourNodeSubprio;
                g2d.setColor(lighten(colour, 50));
                g2d.fillArc((int) (nodeScreenPos.getX() - nodeSizeScaledHalf), (int) (nodeScreenPos.getY() - nodeSizeScaledHalf), (int) nodeSizeScaled, (int) nodeSizeScaled, 0, 360);
            }
            // draw the preview connections
            for (Connection c : getCurvePreviewConnectionList()) {
                drawArrowBetween(g2d, c.getStartNode().getScreenPosition2D(), c.getEndNode().getScreenPosition2D(), c.isDual(), c.getColor());
            }

            if (curveManager.getCurrentCurve() == this) g2d.setComposite(AlphaComposite.SrcOver.derive(1f));
            // Iterate over the current in use number of control nodes
            controlPointsMap.entrySet().stream().limit(getNumCurrentControlPoints()).forEach(entry -> {
                MapNode node = entry.getValue();
                Point nodePos = worldPosToScreenPos(node.x, node.z);
                if (curveManager.getCurrentCurve() == this) {
                    g2d.setComposite(AlphaComposite.SrcOver.derive(1f));
                } else {
                    g2d.setComposite(AlphaComposite.SrcOver.derive(0.55f));
                }
                if (node.isControlNode()) {
                    g2d.drawImage(controlNodeImage, (int) (nodePos.x - (double) (controlNodeImage.getWidth() / 2)), (int) (nodePos.y - (double) (controlNodeImage.getHeight() / 2)), (int) nodeSizeScaled, (int) nodeSizeScaled, null);
                    if (selectedControlPoint == node || node.isSelected() || node.getPreviewNodeSelectionChange()) {
                        g2d.drawImage(controlNodeSelectedImage, (int) (nodePos.x - (double) (controlNodeSelectedImage.getWidth() / 2)), (int) (nodePos.y - (double) (controlNodeSelectedImage.getHeight() / 2)), (int) nodeSizeScaled, (int) nodeSizeScaled, null);
                    }
                } else if (node.isRotationNode()) {
                    g2d.drawImage(rotationNodeImage, (int) (nodePos.x - (double) (rotationNodeImage.getWidth() / 2)), (int) (nodePos.y - (double) (rotationNodeImage.getHeight() / 2)), (int) nodeSizeScaled, (int) nodeSizeScaled, null);
                    if (selectedControlPoint == node || node.isSelected()|| node.getPreviewNodeSelectionChange()) {
                        g2d.drawImage(rotationNodeSelectedImage, (int) (nodePos.x - (double) (rotationNodeImage.getWidth() / 2)), (int) (nodePos.y - (double) (rotationNodeImage.getHeight() / 2)), (int) nodeSizeScaled, (int) nodeSizeScaled, null);
                    }
                } else if (node.isSpecialNode()) {
                    // draw a dashed line between the centre node, the radius adjust node and the start angle node
                    Point2D centreNodeScreenPos = worldPosToScreenPos(centreNode.getX(), centreNode.getZ());
                    Point2D startAngleNodeScreenPos = worldPosToScreenPos(startAngleNode.getX(), startAngleNode.getZ());
                    Point2D adjustRadiusNodeScreenPos = worldPosToScreenPos(adjustRadiusNode.getX(), adjustRadiusNode.getZ());

                    double angleRad = Math.atan2(centreNodeScreenPos.getY() - adjustRadiusNodeScreenPos.getY(),
                                centreNodeScreenPos.getX() - adjustRadiusNodeScreenPos.getX());

                    double offsetX = nodeSizeScaledHalf * Math.cos(angleRad);
                    double offsetY = nodeSizeScaledHalf * Math.sin(angleRad);

                    g2d.setColor(Color.GRAY);
                    g2d.drawLine((int) (centreNodeScreenPos.getX() - offsetX), (int) (centreNodeScreenPos.getY() - offsetY),
                                (int) (adjustRadiusNodeScreenPos.getX() + offsetX), (int) (adjustRadiusNodeScreenPos.getY() + offsetY));
                    g2d.drawLine((int) (adjustRadiusNodeScreenPos.getX() - offsetX), (int) (adjustRadiusNodeScreenPos.getY() - offsetY),
                                (int) (startAngleNodeScreenPos.getX() + offsetX), (int) (startAngleNodeScreenPos.getY() + offsetY));


                    double dx = startAngleNodeScreenPos.getX() - centreNodeScreenPos.getX();
                    double dy = startAngleNodeScreenPos.getY() - centreNodeScreenPos.getY();
                    double angle = Math.atan2(dy, dx);

                    // Save the original transform
                    AffineTransform old = g2d.getTransform();

                    // Move to node position and rotate
                    g2d.translate(nodePos.x, nodePos.y);
                    g2d.rotate(angle);

                    // Draw the image centered at (0,0)
                    g2d.setComposite(AlphaComposite.SrcOver.derive(0.5f));
                    g2d.drawImage(radiusNodeImage, -(radiusNodeImage.getWidth() / 2), -(radiusNodeImage.getHeight() / 2), (int) nodeSizeScaled, (int) nodeSizeScaled, null);

                        // Restore the original transform
                    g2d.setTransform(old);
                }

            });







            //g2d.setStroke(oldStroke);
            g2d.dispose();
        }
    }
}

