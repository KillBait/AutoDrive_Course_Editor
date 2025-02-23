package AutoDriveEditor.Classes;

import AutoDriveEditor.RoadNetwork.MapNode;

import java.awt.*;
import java.awt.geom.AffineTransform;
import java.awt.geom.Point2D;
import java.util.LinkedList;

import static AutoDriveEditor.AutoDriveEditor.getMapPanel;
import static AutoDriveEditor.GUI.Buttons.Curves.CubicCurveButton.cubicCurve;
import static AutoDriveEditor.GUI.Buttons.Curves.CubicCurveButton.isCubicCurveCreated;
import static AutoDriveEditor.GUI.Buttons.Curves.QuadCurveButton.isQuadCurveCreated;
import static AutoDriveEditor.GUI.Buttons.Curves.QuadCurveButton.quadCurve;
//quarticbezier
import static AutoDriveEditor.GUI.Buttons.Curves.QuarticCurveButton.isQuarticCurveCreated;
import static AutoDriveEditor.GUI.Buttons.Curves.QuarticCurveButton.quarticCurve;
//
//quinticbezier
import static AutoDriveEditor.GUI.Buttons.Curves.QuinticCurveButton.isQuinticCurveCreated;
import static AutoDriveEditor.GUI.Buttons.Curves.QuinticCurveButton.quinticCurve;
//
import static AutoDriveEditor.GUI.MapPanel.*;
import static AutoDriveEditor.Managers.CopyPasteManager.*;
import static AutoDriveEditor.Managers.MultiSelectManager.multiSelectList;
import static AutoDriveEditor.RoadNetwork.RoadMap.createControlNode;
import static AutoDriveEditor.Utils.MathUtils.roundUpDoubleToDecimalPlaces;
import static AutoDriveEditor.XMLConfig.AutoSave.resumeAutoSaving;
import static AutoDriveEditor.XMLConfig.AutoSave.suspendAutoSaving;
import static AutoDriveEditor.XMLConfig.EditorXML.nodeSize;
import static java.lang.Math.PI;

public class Rotation {

    private Point2D centrePointWorld;
    private static MapNode controlNode;
    private int snapDegrees;
    private double lastAngle = 0;

    public Rotation() {
        // the control node coordinates specified here is just a placeholder
        // we have to specify something to create the control node.
        controlNode = createControlNode(0, 0);
    }

    public void rotateSelected(LinkedList<MapNode> list, double angle) {
        suspendAutoSaving();
        Point2D selectionCentre = centrePointWorld;
        for (MapNode node : list) {
            rotatePoint(node, selectionCentre, angle);
        }
        if (isQuadCurveCreated) quadCurve.updateCurve();
        if (isCubicCurveCreated) cubicCurve.updateCurve();
        if (isQuarticCurveCreated) quarticCurve.updateCurve();//quarticbezier
        if (isQuinticCurveCreated) quinticCurve.updateCurve();//quinticbezier
        getMapPanel().repaint();
        resumeAutoSaving();
    }

    public void rotateChanger(LinkedList<MapNode> list, Point2D centrePointWorld, int degrees) {
        setCentrePointWorld(centrePointWorld);
        rotatePoint(controlNode, centrePointWorld, degrees);
        rotateSelected(list, degrees);
        lastAngle = 0;
    }

    public void rotatePoint(MapNode node, Point2D centre, double angle) {
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

        double thetaRounded = Math.round(Math.toDegrees(theta) / snapDegrees) * snapDegrees;
        double step = thetaRounded - lastAngle;
        rotatePoint(controlNode, centrePointWorld, step);
        rotateSelected(multiSelectList, step);
        lastAngle = thetaRounded;
        return step;
    }

    //
    // setters
    //

    public void setCentrePoint(LinkedList<MapNode> list) {
        if (list != null && list.size() > 0) {
            selectionAreaInfo selectionInfo = getSelectionBounds(list);
            centrePointWorld = selectionInfo.getSelectionCentre(WORLD_COORDINATES);
            lastAngle = 0;
        }
    }

    public void setCentrePointWorld(Point2D centrePoint) {
        centrePointWorld = centrePoint;
    }

    public void setInitialControlNodePosition(LinkedList<MapNode> list) {
        if (list != null && list.size() > 0) {
            selectionAreaInfo selectionInfo = getSelectionBounds(list);
            controlNode.x = selectionInfo.getSelectionCentre(WORLD_COORDINATES).getX();
            Point2D screenY = selectionInfo.getSelectionCentre(SCREEN_COORDINATES);
            controlNode.z = screenPosToWorldPos((int) screenY.getX(), (int) screenY.getY() - getSelectionRadius(list)).getY();
        }
    }

    public void setRotationSnapDegree(int numDegrees) {
        snapDegrees = numDegrees;
    }

    //
    // Getters
    //

    public MapNode getControlNode() {
        return controlNode;
    }

    public Point getCentrePointScreen() {
        return worldPosToScreenPos(centrePointWorld.getX(), centrePointWorld.getY());
    }

    public Point2D getCentrePointWorld() {
        return centrePointWorld;
    }

    public int getSelectionRadius(LinkedList<MapNode> list) {
        if (list != null && list.size() > 0) {
            MapNode maxNode = getFurthestNode(list);
            selectionAreaInfo selectionInfo = getSelectionBounds(list);
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

    private MapNode getFurthestNode(LinkedList<MapNode> list) {
        double maxDistance = 0;
        MapNode maxNode = null;

        Point2D nodePointWorld = new Point2D.Double();

        selectionAreaInfo selectionInfo = getSelectionBounds(list);
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
