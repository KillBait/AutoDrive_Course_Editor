package AutoDriveEditor.Listeners;

import java.awt.*;
import java.awt.event.MouseEvent;
import java.awt.event.MouseMotionListener;
import java.awt.event.MouseWheelEvent;
import java.awt.event.MouseWheelListener;
import java.awt.geom.Point2D;

import AutoDriveEditor.MapPanel.MapPanel;

import static AutoDriveEditor.GUI.GUIUtils.*;
import static AutoDriveEditor.GUI.MenuBuilder.*;
import static AutoDriveEditor.MapPanel.MapImage.*;
import static AutoDriveEditor.MapPanel.MapPanel.*;

public class MouseListener implements java.awt.event.MouseListener, MouseMotionListener, MouseWheelListener {

    private final MapPanel mapPanel;
    public static int prevMousePosX;
    public static int prevMousePosY;

    public MouseListener(MapPanel mapPanel) {
        this.mapPanel = mapPanel;
    }

    @Override
    public void mouseClicked(MouseEvent e) {
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
        mapPanel.mouseDragged(e.getX(), e.getY());
        storePreviousMousePos(e.getX(), e.getY());
    }

    @Override
    public void mouseMoved(MouseEvent e) {
        mapPanel.mouseMoved(e.getX(), e.getY());
        if (bDebugHeightMap) {
            if (image !=null && heightMapImage != null) {
                double x, y;
                Point2D point = MapPanel.screenPosToWorldPos(e.getX(), e.getY());
                x = ((512 * mapZoomFactor)) + (int) Math.ceil(point.getX() / 2);
                y = ((512 * mapZoomFactor)) + (int) Math.ceil(point.getY() / 2);
                if (x <0) x = 0;
                if (y <0) y = 0;
                Color color = new Color(heightMapImage.getRGB((int)x, (int)y));
                String colourText="Heightmap R = " + color.getRed() + " , G = " + color.getGreen() + " , B = " + color.getBlue() + " , (" + ((color.getRed()>>8) + color.getGreen()) + ")";
                showInTextArea(colourText, true);
                String pointerText = "Mouse X = " + x + ", Y =" + y;
                showInTextArea(pointerText, false);
            }
        }
        storePreviousMousePos(e.getX(), e.getY());
    }

    @Override
    public void mouseWheelMoved(MouseWheelEvent e) {
        mapPanel.increaseZoomLevelBy(e.getWheelRotation());
        storePreviousMousePos(e.getX(), e.getY());
    }

    private void storePreviousMousePos(int x, int y) {
        prevMousePosX = x;
        prevMousePosY = y;
    }
}
