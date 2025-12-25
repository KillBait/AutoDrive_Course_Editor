package AutoDriveEditor.GUI.Buttons.Toolbar.Experimental;

import AutoDriveEditor.Classes.UI_Components.ScaleAnimIcon;
import AutoDriveEditor.GUI.Buttons.BaseButton;

import javax.swing.*;
import java.awt.*;
import java.awt.event.MouseEvent;
import java.awt.geom.Point2D;

import static AutoDriveEditor.AutoDriveEditor.getMapPanel;
import static AutoDriveEditor.GUI.MapPanel.nodeSizeScaledHalf;
import static AutoDriveEditor.Managers.IconManager.*;
import static AutoDriveEditor.Managers.MultiSelectManager.*;

public final class ScaleButton extends BaseButton {

    //
    // Early testing:- May not make into release...
    //

    private final Rectangle selectRect = new Rectangle();
    private Rectangle hoverRectangle;
    private final Rectangle topLeftSelectRect = new Rectangle();
    private final Rectangle topRightSelectRect = new Rectangle();
    private final Rectangle bottomLeftSelectRect = new Rectangle();
    private final Rectangle bottomRightSelectRect = new Rectangle();


    private boolean fill;

    public ScaleButton(JPanel panel) {
        //button = makeImageToggleButton("buttons/scale","buttons/scale_selected", null,null,null, panel, false, false,  null, false, this);
        ScaleAnimIcon animExperimental1Icon = createScaleAnimIcon(SCALE_ICON, false);
        button = createAnimToggleButton(animExperimental1Icon, panel, null, null,  false, false, this);
    }

    @Override
    public String getButtonID() { return "ScaleButton"; }

    @Override
    public String getButtonAction() { return "ActionButton"; }

    @Override
    public String getButtonPanel() { return "Edit"; }

    @Override
    public Boolean useMultiSelection() { return true; }

    @Override
    public void mouseReleased(MouseEvent e) {
        super.mouseReleased(e);

        if (e.getButton() == MouseEvent.BUTTON3) {
            if (!multiSelectList.isEmpty()) {
                updateSelectionRectangles();
                getMapPanel().repaint();
            }
        }
    }

    @Override
    public void mouseMoved(MouseEvent e) {
        if (!multiSelectList.isEmpty() && this.button.isSelected()) {
            if (topLeftSelectRect.contains(e.getX(), e.getY())) {
                hoverRectangle = topLeftSelectRect;
            } else if (topRightSelectRect.contains(e.getX(), e.getY())) {
                hoverRectangle = topRightSelectRect;
            } else if (bottomLeftSelectRect.contains(e.getX(), e.getY())) {
                hoverRectangle = bottomLeftSelectRect;
            } else if (bottomRightSelectRect.contains(e.getX(), e.getY())) {
                hoverRectangle = bottomRightSelectRect;
            } else {
                hoverRectangle = null;
            }
            getMapPanel().repaint();
        }
    }

    private void updateSelectionRectangles() {

        if (!multiSelectList.isEmpty() && this.button.isSelected()) {
            // Get all nodes in the selection area
            SelectionAreaInfo selectionInfo = getSelectionBounds(multiSelectList);
            // Get the top left screen co-ordinates
            Point2D topLeft = selectionInfo.getSelectionStart(SCREEN_COORDINATES);
            // Adjust the top left location to account for the node size
            topLeft.setLocation(topLeft.getX() - nodeSizeScaledHalf, topLeft.getY() - nodeSizeScaledHalf);
            // Get the top bottom right screen co-ordinates
            Point2D bottomRight = selectionInfo.getSelectionEnd(SCREEN_COORDINATES);
            // Adjust the bottom right location to account for the node size
            bottomRight.setLocation(bottomRight.getX() + nodeSizeScaledHalf, bottomRight.getY() + nodeSizeScaledHalf);
            // Get the width of the adjusted selection area
            double selectionWidth = bottomRight.getX() - topLeft.getX();
            // Get the height of the adjusted selection area
            double selectionHeight = bottomRight.getY() - topLeft.getY();
            // calculate which is the small side of the selection area, make the selection box a proportion of the smallest side
            int minSelectionSize = (int) (Math.min(selectionWidth, selectionHeight) / 10);

            selectRect.setRect(topLeft.getX(), topLeft.getY(), selectionWidth, selectionHeight);
            topLeftSelectRect.setRect(selectRect.x, selectRect.y, minSelectionSize, minSelectionSize);
            topRightSelectRect.setRect(selectRect.x + (selectRect.width - minSelectionSize), selectRect.y, minSelectionSize, minSelectionSize);
            bottomLeftSelectRect.setRect(selectRect.x, selectRect.y + (selectRect.height - minSelectionSize), minSelectionSize, minSelectionSize);
            bottomRightSelectRect.setRect(selectRect.getX() + (selectRect.width - minSelectionSize), selectRect.y + (selectRect.height - minSelectionSize), minSelectionSize, minSelectionSize);
        }
    }



    @Override
    public void drawToScreen(Graphics g) {
        if (!multiSelectList.isEmpty() && this.button.isSelected()) {
            g.setColor(Color.WHITE);
            g.drawRect(selectRect.x, selectRect.y, selectRect.width, selectRect.height);
            updateSelectionRectangles();
            drawCornerRectangle(g, topLeftSelectRect);
            drawCornerRectangle(g, topRightSelectRect);
            drawCornerRectangle(g, bottomLeftSelectRect);
            drawCornerRectangle(g, bottomRightSelectRect);
        }
    }

    private void drawCornerRectangle(Graphics g, Rectangle rectangle) {
        g.setColor(Color.WHITE);
        if (hoverRectangle == rectangle) {
            g.fillRect(rectangle.x, rectangle.y, rectangle.width, rectangle.height);
        } else {
            g.drawRect(rectangle.x, rectangle.y, rectangle.width, rectangle.height);
        }
    }

    @Override
    public String buildToolTip() {
        return "";
    }
}
