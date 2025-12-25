package AutoDriveEditor.Classes.Interfaces;

import AutoDriveEditor.RoadNetwork.MapNode;

import java.awt.geom.Point2D;

/**
 * Interface defining the methods required for arc spline interaction.
 */

public interface ArcSplineInterface extends CurveInterface {

    //
    // ArcSpline specific methods
    //

    int getRotationAngle(MapNode node);
    double getRadiusAngle();
    Point2D rotateStartNode(MapNode rotationNode, int mouseX, int mouseY);
    Point2D rotateEndNode(MapNode rotationNode, int mouseX, int mouseY);
    Point2D rotateControlNode(MapNode rotationNode, int mouseX, int mouseY);
}
