package AutoDriveEditor.Classes.Interfaces;

import AutoDriveEditor.RoadNetwork.MapNode;

import java.awt.*;
import java.awt.event.MouseEvent;
import java.awt.geom.Point2D;
import java.util.ArrayList;

public interface WidgetInterface {

    /**
     * All the functions that a widget must implement, Override any of the
     * functions as needed in the specific widget class.
     */

    void initSelectors();
    void mouseClicked(MouseEvent e);
    void mousePressed(MouseEvent e);
    void mouseReleased(MouseEvent e);
    void mouseDragged(MouseEvent e);
    void mouseMoved(MouseEvent e);
    void mouseWheelMoved(MouseEvent e);
    void setWidgetEnabled(boolean enabled);
    void setCurrentAnchor(MapNode mapNode);
    void setCurrentSelector(SelectorInterface selector);
    MapNode getCurrentAnchor();
    Point2D getWidgetPosWorld();
    Point getWidgetPosScreen();
    boolean isEnabled();
    void updateWidget();
    SelectorInterface getCurrentSelector();
    void drawToScreen(Graphics g);
    void onMultiSelectStart();
    void onMultiSelectStop();
    void onMultiSelectChange(ArrayList<MapNode> nodeList);
    void onMultiSelectAdd(ArrayList<MapNode> addedNodes);
    void onMultiSelectRemove(ArrayList<MapNode> removedNodes);
    void onMultiSelectOneTime(ArrayList<MapNode> oneTimeList);
    void onMultiSelectCleared();
}
