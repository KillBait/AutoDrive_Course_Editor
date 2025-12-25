package AutoDriveEditor.Classes.Interfaces;

import AutoDriveEditor.RoadNetwork.Connection;
import AutoDriveEditor.RoadNetwork.MapNode;

import java.awt.*;
import java.awt.event.MouseEvent;
import java.awt.event.MouseWheelEvent;
import java.awt.geom.Point2D;
import java.util.ArrayList;

/**
 * Interface defining the methods required for curve manipulation and interaction.
 */

public interface CurveInterface {

    //
    // Mouse interaction methods
    //

    void mouseClicked(MouseEvent e);
    void mousePressed(MouseEvent e);
    void mouseReleased(MouseEvent e);
    void mouseDragged(MouseEvent e);
    void mouseMoved(MouseEvent e);
    void mouseWheelMoved(MouseWheelEvent e);

    //
    // Curve specific methods
    //

    void initCurve();

    void moveControlNodeBy(MapNode node, double diffX, double diffY, boolean applyScaling);
    void updateCurve();
    void cancelCurve();
    void commitCurve();
    void swapCurveDirection();

    //
    // Getters
    //

    boolean isControlNode(MapNode node);
    boolean isCurveAnchorNode(MapNode node);
    ArrayList<MapNode> getActiveControlPoints();
    int getNumCurrentControlPoints();
    int getNumInterpolations();
    Connection.ConnectionType getCurveType();
    int getRotationAngle(MapNode node);
    int getCurvePriority();
    int getMinControlPoints();
    int getMaxControlPoints();
    ArrayList<MapNode> getNodes();

    //
    // Setters
    //

    void setCurvePriority(int nodeType);
    void setCurveType(Connection.ConnectionType type);
    void setCurveInterpolations(int numPoints);
    void setNumCurrentControlPoints(int value);

    //
    // Rendering
    //

    void drawToScreen(Graphics g);

}
