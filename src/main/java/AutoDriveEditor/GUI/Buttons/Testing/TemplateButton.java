package AutoDriveEditor.GUI.Buttons.Testing;

import AutoDriveEditor.GUI.Buttons.BaseButton;

import javax.swing.*;
import java.awt.*;
import java.awt.event.ActionEvent;
import java.awt.event.MouseEvent;
import java.awt.event.MouseWheelEvent;

import static AutoDriveEditor.Locale.LocaleManager.getLocaleString;
import static AutoDriveEditor.Utils.GUIUtils.makeImageToggleButton;

@SuppressWarnings("unused")
public class TemplateButton extends BaseButton {

    //
    //
    //

    public TemplateButton(JPanel panel) {

        // create your button, the pointer to it must be assigned to the button variable
        //
        // e.g.
        // button = new JButton()
        // button = new JToggleButton()

        button = makeImageToggleButton( "buttons/unknown", "buttons/unknown_selected", null, "nodes_test_tooltip", "options_con_connect_alt", panel, true, true, null, false, this);
    }

    @Override
    public String getButtonID() { return "TemplateButton"; }

    @Override
    public String getButtonAction() { return "TemplateAction"; }

    @Override
    public String getButtonPanel() { return "Template"; }

    @Override
    public String getInfoText() { return getLocaleString("nodes_template_tooltip"); }

    @Override
    public Boolean ignoreButtonDeselect() { return false; }

    @Override
    public Boolean useMultiSelection() { return true; }

    @Override
    public Boolean previewNodeSelectionChange() { return true; }

    @Override
    public void setEnabled(boolean selected) {}

    @Override
    public void setSelected(boolean selected) {}

    @Override
    public void actionPerformed(ActionEvent e) {}

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
    public void drawToScreen(Graphics g) {}




}
