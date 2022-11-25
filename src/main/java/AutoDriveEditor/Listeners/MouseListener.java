package AutoDriveEditor.Listeners;

import AutoDriveEditor.MapPanel.MapPanel;

import java.awt.event.MouseEvent;
import java.awt.event.MouseMotionListener;
import java.awt.event.MouseWheelEvent;
import java.awt.event.MouseWheelListener;

import static AutoDriveEditor.AutoDriveEditor.buttonManager;

public class MouseListener implements java.awt.event.MouseListener, MouseMotionListener, MouseWheelListener {

    private final MapPanel previewPanel;
    public static int currentMouseX;
    public static int currentMouseY;
    public static int prevMousePosX;
    public static int prevMousePosY;

    public MouseListener(MapPanel mapPanel) {
        this.previewPanel = mapPanel;
    }

    @Override
    public void mouseClicked(MouseEvent e) {
        storeCurrentMousePos(e.getX(), e.getY());
        buttonManager.mouseClicked(e);
        if (e.getButton() == MouseEvent.BUTTON1) {
            previewPanel.mouseButton1Clicked(e.getX(), e.getY());
        }
        if (e.getButton() == MouseEvent.BUTTON2) {
            previewPanel.mouseButton2Clicked(e.getX(), e.getY());
        }
        if (e.getButton() == MouseEvent.BUTTON3) {
            previewPanel.mouseButton3Clicked(e.getX(), e.getY());
        }
        storePreviousMousePos(e.getX(), e.getY());

    }

    @Override
    public void mousePressed(MouseEvent e) {
        storeCurrentMousePos(e.getX(), e.getY());
        buttonManager.mousePressed(e);
        if (e.getButton() == MouseEvent.BUTTON1) {
            previewPanel.mouseButton1Pressed(e.getX(), e.getY());
        }
        if (e.getButton() == MouseEvent.BUTTON2) {
            previewPanel.mouseButton2Pressed(e.getX(), e.getY());
        }
        if (e.getButton() == MouseEvent.BUTTON3) {
            previewPanel.mouseButton3Pressed(e.getX(), e.getY());
        }
        storePreviousMousePos(e.getX(), e.getY());
    }

    @Override
    public void mouseReleased(MouseEvent e) {
        storeCurrentMousePos(e.getX(), e.getY());
        buttonManager.mouseReleased(e);
        if (e.getButton() == MouseEvent.BUTTON1) {
            previewPanel.mouseButton1Released(e.getX(), e.getY());
        }
        if (e.getButton() == MouseEvent.BUTTON2) {
            previewPanel.mouseButton2Released();
        }
        if (e.getButton() == MouseEvent.BUTTON3) {
            previewPanel.mouseButton3Released(e.getX(), e.getY());
        }
        storePreviousMousePos(e.getX(), e.getY());
    }

    @Override
    public void mouseEntered(MouseEvent e) {}

    @Override
    public void mouseExited(MouseEvent e) {}

    @Override
    public void mouseDragged(MouseEvent e) {
        storeCurrentMousePos(e.getX(), e.getY());
        buttonManager.mouseDragged(e);
        previewPanel.mouseDragged(e.getX(), e.getY());
        storePreviousMousePos(e.getX(), e.getY());
    }

    @Override
    public void mouseMoved(MouseEvent e) {
        storeCurrentMousePos(e.getX(), e.getY());
        buttonManager.mouseMoved(e);
        previewPanel.mouseMoved(e.getX(), e.getY());
        storePreviousMousePos(e.getX(), e.getY());
    }

    @Override
    public void mouseWheelMoved(MouseWheelEvent e) {
        previewPanel.increaseZoomLevelBy(e.getWheelRotation());
        buttonManager.mouseWheelMoved(e);
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
