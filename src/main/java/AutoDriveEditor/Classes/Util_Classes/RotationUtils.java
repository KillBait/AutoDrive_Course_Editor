package AutoDriveEditor.Classes.Util_Classes;

import AutoDriveEditor.RoadNetwork.MapNode;

import java.awt.*;
import java.awt.geom.AffineTransform;
import java.awt.geom.Point2D;
import java.util.ArrayList;

import static AutoDriveEditor.AutoDriveEditor.curveManager;
import static AutoDriveEditor.AutoDriveEditor.getMapPanel;
import static AutoDriveEditor.Classes.Util_Classes.MathUtils.roundUpDoubleToDecimalPlaces;
import static AutoDriveEditor.GUI.MapPanel.*;
import static AutoDriveEditor.Managers.MultiSelectManager.*;
import static AutoDriveEditor.RoadNetwork.MapNode.createControlNode;
import static AutoDriveEditor.XMLConfig.AutoSave.resumeAutoSaving;
import static AutoDriveEditor.XMLConfig.AutoSave.suspendAutoSaving;
import static AutoDriveEditor.XMLConfig.EditorXML.nodeSize;
import static AutoDriveEditor.XMLConfig.EditorXML.rotationStep;
import static java.lang.Math.PI;

public class RotationUtils {

    private Point2D centrePointWorld;
    private static MapNode controlNode;
//    private int snapDegrees;
    private double lastAngle = 0;

    public RotationUtils() {
        // the control node coordinates specified here is just a placeholder
        // we have to specify something to createSetting the control node.
        controlNode = createControlNode(0, 0);
    }

    public void rotateSelected(ArrayList<MapNode> list, double angle) {
        suspendAutoSaving();
        Point2D selectionCentre = centrePointWorld;
        for (MapNode node : list) {
            rotatePoint(node, selectionCentre, angle);
        }
        if (curveManager.isCurvePreviewCreated()) {
            curveManager.updateAllCurves();
        }
//        if (isQuadCurveCreated) quadCurve.updateCurve();
//        if (isCubicCurveCreated) cubicCurve.updateCurve();
        getMapPanel().repaint();
        resumeAutoSaving();
    }

    public void rotateChanger(ArrayList<MapNode> list, Point2D centrePointWorld, int degrees) {
        setCentrePointWorld(centrePointWorld);
        rotatePoint(controlNode, centrePointWorld, degrees);
        rotateSelected(list, degrees);
        lastAngle = 0;
    }

    public static void rotatePoint(MapNode node, Point2D centre, double angle) {
        Point2D result = new Point2D.Double();
        AffineTransform rotation = new AffineTransform();
        double angleInRadians = Math.toRadians(angle);
        rotation.rotate(angleInRadians, centre.getX(), centre.getY());
        rotation.transform(new Point2D.Double(node.x, node.z), result);
        node.x = roundUpDoubleToDecimalPlaces(result.getX(), 3);
        node.z = roundUpDoubleToDecimalPlaces(result.getY(), 3);
    }

    @SuppressWarnings("unused")
    public double rotateControlNode(int mouseX, int mouseY, int degreesOffset) {
        Point2D mouseWorld = screenPosToWorldPos(mouseX, mouseY);

        double theta = Math.atan2(centrePointWorld.getY() - mouseWorld.getY(), centrePointWorld.getX() - mouseWorld.getX()) - PI / 2;
        if (theta < 0) theta += 2*PI;

        double thetaRounded = Math.round(Math.toDegrees(theta) / rotationStep) * rotationStep;
        double step = thetaRounded - lastAngle;
        rotatePoint(controlNode, centrePointWorld, step);
        rotateSelected(multiSelectList, step);
        lastAngle = thetaRounded;
        return step;
    }

    //
    // setters
    //

    public void setCentrePoint(ArrayList<MapNode> list) {
        if (list != null && !list.isEmpty()) {
            SelectionAreaInfo selectionInfo = getSelectionBounds(list);
            centrePointWorld = selectionInfo.getSelectionCentre(WORLD_COORDINATES);
            lastAngle = 0;
        }
    }

    public void setCentrePointWorld(Point2D centrePoint) {
        centrePointWorld = centrePoint;
    }

    public void setInitialControlNodePosition(ArrayList<MapNode> list) {
        if (list != null && !list.isEmpty()) {
            SelectionAreaInfo selectionInfo = getSelectionBounds(list);
            controlNode.x = selectionInfo.getSelectionCentre(WORLD_COORDINATES).getX();
            Point2D screenY = selectionInfo.getSelectionCentre(SCREEN_COORDINATES);
            controlNode.z = screenPosToWorldPos((int) screenY.getX(), (int) screenY.getY() - getSelectionRadius(list)).getY();
        }
    }

//    public void setRotationSnapDegree(int numDegrees) {
//        snapDegrees = numDegrees;
//    }

    //
    // Getters
    //

    public MapNode getControlNode() {
        return controlNode;
    }

    public Point getCentrePointScreen() {
        return worldPosToScreenPos(centrePointWorld);
    }

    public Point2D getCentrePointWorld() {
        return centrePointWorld;
    }

    public int getSelectionRadius(ArrayList<MapNode> list) {
        if (list != null && !list.isEmpty()) {
            MapNode maxNode = getFurthestNode(list);
            SelectionAreaInfo selectionInfo = getSelectionBounds(list);
            Point2D maxPosition = worldPosToScreenPos(maxNode.x, maxNode.z);
            return (int) (selectionInfo.getSelectionCentre(SCREEN_COORDINATES).distance(maxPosition.getX(), maxPosition.getY()) + ((nodeSize * 0.5) * zoomLevel));
        } else {
            return 0;
        }
    }

    @SuppressWarnings("unused")
    public double getTheta(double worldX, double worldY) {
        double theta = Math.atan2(centrePointWorld.getY() - worldY, centrePointWorld.getX() - worldX) - PI / 2;
        if (theta < 0) theta += 2*PI;
        return theta;
    }

    private MapNode getFurthestNode(ArrayList<MapNode> list) {
        double maxDistance = 0;
        MapNode maxNode = null;

        Point2D nodePointWorld = new Point2D.Double();

        SelectionAreaInfo selectionInfo = getSelectionBounds(list);
        Point2D centrePointWorld = selectionInfo.getSelectionCentre(WORLD_COORDINATES);
        for (MapNode node : list) {
            nodePointWorld.setLocation(node.x, node.z);
            if (centrePointWorld.distance(nodePointWorld) >= maxDistance) {
                maxNode = node;
                maxDistance = centrePointWorld.distance(nodePointWorld);
            }
        }
        return maxNode;
    }


}
