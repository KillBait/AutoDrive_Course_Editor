package AutoDriveEditor.Listeners;

import AutoDriveEditor.GUI.MapPanel;
import AutoDriveEditor.Managers.MultiSelectManager;

import java.awt.event.MouseEvent;
import java.awt.event.MouseMotionListener;
import java.awt.event.MouseWheelEvent;
import java.awt.event.MouseWheelListener;
import java.awt.geom.Point2D;

import static AutoDriveEditor.AutoDriveEditor.*;
import static AutoDriveEditor.GUI.MapImage.pdaImage;
import static AutoDriveEditor.GUI.MapPanel.bIsShiftPressed;
import static AutoDriveEditor.GUI.MapPanel.screenPosToWorldPos;

public class MouseListener implements MouseMotionListener, MouseWheelListener, java.awt.event.MouseListener {

    private final MultiSelectManager multiSelectManager;
    public static int currentMouseX;
    public static int currentMouseY;
    public static int prevMousePosX;
    public static int prevMousePosY;

    public MouseListener(MapPanel mapPanel) {
        this.multiSelectManager = new MultiSelectManager();
    }

    public MouseListener() {
        this.multiSelectManager = new MultiSelectManager();
    }

    @Override
    public void mouseClicked(MouseEvent e) {
        storeCurrentMousePos(e.getX(), e.getY());
        multiSelectManager.mouseClicked(e);
        widgetManager.mouseClicked(e);
        buttonManager.mouseClicked(e);
        curveManager.mouseClicked(e);
        getMapPanel().mouseButtonClicked(e);
        storePreviousMousePos(e.getX(), e.getY());

    }

    @Override
    public void mousePressed(MouseEvent e) {
        storeCurrentMousePos(e.getX(), e.getY());
        multiSelectManager.mousePressed(e);
        widgetManager.mousePressed(e);
        buttonManager.mousePressed(e);
        curveManager.mousePressed(e);
        getMapPanel().mouseButtonPressed(e);
        storePreviousMousePos(e.getX(), e.getY());
    }

    @Override
    public void mouseReleased(MouseEvent e) {
        storeCurrentMousePos(e.getX(), e.getY());
        multiSelectManager.mouseReleased(e);
        widgetManager.mouseReleased(e);
        buttonManager.mouseReleased(e);
        curveManager.mouseReleased(e);
        getMapPanel().mouseButtonReleased(e);
        storePreviousMousePos(e.getX(), e.getY());
    }

    @Override
    public void mouseEntered(MouseEvent e) {
        buttonManager.mouseEntered(e);
    }

    @Override
    public void mouseExited(MouseEvent e) {
        buttonManager.mouseExited(e);
    }

    @Override
    public void mouseDragged(MouseEvent e) {
        storeCurrentMousePos(e.getX(), e.getY());
        multiSelectManager.mouseDragged(e);
        widgetManager.mouseDragged(e);
        buttonManager.mouseDragged(e);
        curveManager.mouseDragged(e);
        getMapPanel().mouseDragged(e.getX(), e.getY());
        storePreviousMousePos(e.getX(), e.getY());
    }

    @Override
    public void mouseMoved(MouseEvent e) {
        storeCurrentMousePos(e.getX(), e.getY());
        multiSelectManager.mouseMoved(e);
        widgetManager.mouseMoved(e);
        buttonManager.mouseMoved(e);
        curveManager.mouseMoved(e);
        getMapPanel().mouseMoved(e.getX(), e.getY());
        storePreviousMousePos(e.getX(), e.getY());
    }

    @Override
    public void mouseWheelMoved(MouseWheelEvent e) {
        int num = Math.min(Math.max(e.getWheelRotation(), -1), 1);
        if (!bIsShiftPressed && pdaImage != null)  getMapPanel().setNewZoomLevel(num);
        widgetManager.mouseWheelMoved(e);
        buttonManager.mouseWheelMoved(e);
        curveManager.mouseWheelMoved(e);
    }


    private void storeCurrentMousePos(int x, int y) {
        currentMouseX = x;
        currentMouseY = y;
    }
    private void storePreviousMousePos(int x, int y) {
        prevMousePosX = x;
        prevMousePosY = y;
    }


    public static Point2D getCurrentMouseWorldPos() {
        return screenPosToWorldPos(currentMouseX, currentMouseY);
    }


    public static double getCurrentMouseWorldY() {
        Point2D p = screenPosToWorldPos(currentMouseX, currentMouseY);
        return p.getY();
    }

    public static double getCurrentMouseWorldX() {
        Point2D p = screenPosToWorldPos(currentMouseX, currentMouseY);
        return p.getX();
    }

    public static int getCurrentMouseY() {
        return currentMouseY;
    }

    public static int getPrevMouseX() {
        return prevMousePosX;
    }

    public static int getPrevMouseY() {
        return prevMousePosY;
    }
}
