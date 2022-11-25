package AutoDriveEditor.GUI.Buttons.CopyPaste;

import AutoDriveEditor.GUI.Buttons.CopyPasteBaseButton;

import javax.swing.*;
import java.awt.event.MouseEvent;

import static AutoDriveEditor.Locale.LocaleManager.getLocaleString;
import static AutoDriveEditor.Managers.MultiSelectManager.*;
import static AutoDriveEditor.MapPanel.MapPanel.getMapPanel;
import static AutoDriveEditor.Utils.GUIUtils.makeImageToggleButton;

public class AreaSelectButton extends CopyPasteBaseButton {

    public AreaSelectButton(JPanel panel) {
        button = makeImageToggleButton("buttons/select","buttons/select_selected", null, "copypaste_select_tooltip","copypaste_select_alt", panel, false, false, null, false, this);
    }

    @Override
    public String getButtonID() { return "AreaSelectButton"; }

    @Override
    public String getButtonAction() { return "ActionButton"; }

    @Override
    public String getButtonPanel() { return "Edit"; }

    @Override
    public String getInfoText() { return getLocaleString("copypaste_select_tooltip"); }

    @Override
    public void mousePressed(MouseEvent e) {
        if (e.getButton() == MouseEvent.BUTTON3) {
            startMultiSelect(e.getX(), e.getY());
        }
    }

    @Override
    public void mouseReleased(MouseEvent e) {
        if (e.getButton() == MouseEvent.BUTTON3) {
            stopMultiSelect(e.getX(), e.getY());
            getAllNodesInSelectedArea(rectangleStart, rectangleEnd);
        }
    }

    @Override
    public void mouseDragged(MouseEvent e) {
        if (rectangleStart != null && isMultiSelectDragging) {
            getMapPanel().repaint();
        }
    }

    @Override
    public void mouseClicked(MouseEvent e) {
        if (e.getButton() == MouseEvent.BUTTON3) {
            clearMultiSelection();
        }
    }
}
