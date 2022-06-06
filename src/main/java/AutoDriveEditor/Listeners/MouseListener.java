package AutoDriveEditor.Listeners;

import AutoDriveEditor.MapPanel.MapPanel;

import java.awt.event.MouseEvent;
import java.awt.event.MouseMotionListener;
import java.awt.event.MouseWheelEvent;
import java.awt.event.MouseWheelListener;

public class MouseListener implements java.awt.event.MouseListener, MouseMotionListener, MouseWheelListener {

    private final MapPanel mapPanel;
    public static int currentMouseX;
    public static int currentMouseY;
    public static int prevMousePosX;
    public static int prevMousePosY;

    public MouseListener(MapPanel mapPanel) {
        this.mapPanel = mapPanel;
    }

    @Override
    public void mouseClicked(MouseEvent e) {
        storeCurrentMousePos(e.getX(), e.getY());
        if (e.getButton() == MouseEvent.BUTTON1) {
            mapPanel.mouseButton1Clicked(e.getX(), e.getY());
        }
        if (e.getButton() == MouseEvent.BUTTON2) {
            mapPanel.mouseButton2Clicked(e.getX(), e.getY());
        }
        if (e.getButton() == MouseEvent.BUTTON3) {
            mapPanel.mouseButton3Clicked(e.getX(), e.getY());
        }
        storePreviousMousePos(e.getX(), e.getY());

    }

    @Override
    public void mousePressed(MouseEvent e) {
        storeCurrentMousePos(e.getX(), e.getY());
        if (e.getButton() == MouseEvent.BUTTON1) {
            mapPanel.mouseButton1Pressed(e.getX(), e.getY());
        }
        if (e.getButton() == MouseEvent.BUTTON2) {
            mapPanel.mouseButton2Pressed(e.getX(), e.getY());
        }
        if (e.getButton() == MouseEvent.BUTTON3) {
            mapPanel.mouseButton3Pressed(e.getX(), e.getY());
        }
        storePreviousMousePos(e.getX(), e.getY());
    }

    @Override
    public void mouseReleased(MouseEvent e) {
        storeCurrentMousePos(e.getX(), e.getY());
        if (e.getButton() == MouseEvent.BUTTON1) {
            mapPanel.mouseButton1Released(e.getX(), e.getY());
        }
        if (e.getButton() == MouseEvent.BUTTON2) {
            mapPanel.mouseButton2Released();
        }
        if (e.getButton() == MouseEvent.BUTTON3) {
            mapPanel.mouseButton3Released(e.getX(), e.getY());
        }
        storePreviousMousePos(e.getX(), e.getY());
    }

    @Override
    public void mouseEntered(MouseEvent e) {
    }

    @Override
    public void mouseExited(MouseEvent e) {
    }

    @Override
    public void mouseDragged(MouseEvent e) {
        storeCurrentMousePos(e.getX(), e.getY());
        mapPanel.mouseDragged(e.getX(), e.getY());
        storePreviousMousePos(e.getX(), e.getY());
    }

    @Override
    public void mouseMoved(MouseEvent e) {
        storeCurrentMousePos(e.getX(), e.getY());
        mapPanel.mouseMoved(e.getX(), e.getY());
        storePreviousMousePos(e.getX(), e.getY());
    }

    @Override
    public void mouseWheelMoved(MouseWheelEvent e) {
        mapPanel.increaseZoomLevelBy(e.getWheelRotation());
    }


    private void storeCurrentMousePos(int x, int y) {
        currentMouseX = x;
        currentMouseY = y;
    }
    private void storePreviousMousePos(int x, int y) {
        prevMousePosX = x;
        prevMousePosY = y;
    }
}
