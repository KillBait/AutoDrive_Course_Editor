package AutoDriveEditor.Classes.Interfaces;

import AutoDriveEditor.Classes.Widgets.Selectors.SelectorBase;

import java.awt.*;
import java.awt.event.MouseEvent;
import java.awt.event.MouseWheelEvent;
import java.awt.geom.Point2D;

public interface SelectorInterface {
    // every selector class must implement this interface
    void mouseClicked(MouseEvent e);
    void mousePressed(MouseEvent e);
    void mouseReleased(MouseEvent e);
    void mouseDragged(MouseEvent e);
    void mouseMoved(MouseEvent e);
    void mouseWheelMoved(MouseWheelEvent e);
    SelectorBase.SELECTED_AXIS checkSelectedAxis(Point mousePos);
    Point2D getSelectOffsetWorld();
    Point2D getWidgetPosWorld();
    boolean isSelected();
    void setOwnerWidget(WidgetInterface owner);
    void setSelectorEnabled(boolean enabled);
    void updateSelector();
    void drawToScreen(Graphics g);
}
