package AutoDriveEditor.Classes.Widgets.Selectors;

import AutoDriveEditor.Classes.Interfaces.SelectorInterface;
import AutoDriveEditor.Classes.Interfaces.WidgetInterface;
import AutoDriveEditor.Managers.RenderManager;

import java.awt.*;
import java.awt.event.MouseEvent;
import java.awt.event.MouseWheelEvent;
import java.awt.geom.Point2D;

import static AutoDriveEditor.Listeners.MouseListener.currentMouseX;
import static AutoDriveEditor.Listeners.MouseListener.currentMouseY;

public abstract class SelectorBase extends RenderManager.Drawable implements SelectorInterface {

    public enum SELECTED_AXIS {NONE, NODE, X, Y, BOTH}

    // Widget axis selectors
    protected SelectorBase.SELECTED_AXIS lastUsedDirectionAxis = SelectorBase.SELECTED_AXIS.NONE;
    protected SelectorBase.SELECTED_AXIS directionAxis = SelectorBase.SELECTED_AXIS.NONE;

    // Owner widget for this selector, used for callback to get information from the widget that called it
    WidgetInterface ownerWidget;


    boolean isEnabled = false;




    // Offset from the widget origin (in world coordinates) to the interaction position with the selectors indicator
    Point2D selectOffsetWorld = new Point2D.Double();

    // Default implementation of the SelectorInterface methods
    // You only need to @Override these methods if you need specific behavior

    @Override
    public void mouseClicked(MouseEvent e) {}

    @Override
    public void mousePressed(MouseEvent e) {}

    @Override
    public void mouseReleased(MouseEvent e) {}

    @Override
    public void mouseDragged(MouseEvent e) {}

    @Override
    public void mouseMoved(MouseEvent e) {}

    @Override
    public void mouseWheelMoved(MouseWheelEvent e) {}

    @Override
    public void setOwnerWidget(WidgetInterface widget) { this.ownerWidget = widget; }

    @Override
    public Point2D getSelectOffsetWorld() { return selectOffsetWorld; }

    @Override
    public Point2D getWidgetPosWorld() {
        return null;
    }

//    @Override
//    public SELECTED_AXIS checkSelectedAxis(MouseEvent e) {
//        return null;
//    }

    @Override
    public void setSelectorEnabled(boolean enabled) {
        this.isEnabled = enabled;
    }

    @Override
    public boolean isSelected() {
        //SELECTED_AXIS s = this.checkSelectedAxis(new Point(currentMouseX, currentMouseY));
        return this.checkSelectedAxis(new Point(currentMouseX, currentMouseY)) != SELECTED_AXIS.NONE;
    }

    @Override
    public void updateSelector() {
    }

//    public SELECTED_AXIS checkWidgetSelection(MouseEvent e) {
//        return SELECTED_AXIS.NONE;
//    };

    public SELECTED_AXIS checkSelectedAxis(Point point) {
        return SELECTED_AXIS.NONE;
    };

    @Override
    public void drawToScreen(Graphics g) {

    }

    @Override
    public String toString() {
        return String.format("%s@%s", this.getClass().getSimpleName(), Integer.toHexString(hashCode()));
    }
}
