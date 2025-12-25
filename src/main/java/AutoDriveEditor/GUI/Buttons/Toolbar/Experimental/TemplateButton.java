package AutoDriveEditor.GUI.Buttons.Toolbar.Experimental;

import AutoDriveEditor.Classes.UI_Components.ScaleAnimIcon;
import AutoDriveEditor.GUI.Buttons.BaseButton;

import javax.swing.*;
import java.awt.*;
import java.awt.event.ActionEvent;
import java.awt.event.MouseEvent;
import java.awt.event.MouseWheelEvent;

import static AutoDriveEditor.AutoDriveEditor.buttonManager;
import static AutoDriveEditor.Locale.LocaleManager.getLocaleString;
import static AutoDriveEditor.Managers.IconManager.*;

@SuppressWarnings({"unused", "RedundantMethodOverride"})
public class TemplateButton extends BaseButton {

    //
    //
    //

    public TemplateButton(JPanel panel) {

        // createSetting your button, the pointer to it must be assigned to the button variable
        //
        // e.g.
        // button = new JButton()
        // button = new JToggleButton()

        ScaleAnimIcon animExperimental1Icon = createScaleAnimIcon(TEMPLATE_ICON, false);
        button = createAnimToggleButton(animExperimental1Icon, panel, null, null,  false, false, this);
        buttonManager.getCurrentButton();
    }

    /* Unique ID for this button
     * @see ButtonManager.getCurrentButtonID() to get the ID of the currently selected button
     */
    @Override
    public String getButtonID() { return "TemplateButton"; }

    /* ID string for this button, can be the same as others to group multiple together
     * @see ButtonManager.getCurrentButtonAction() to get the action of the currently selected button
     */
    @Override
    public String getButtonAction() { return "TemplateAction"; }

    /* ID string for this buttons panel, can be the same as others to group multiple together
     * @see ButtonManager.getCurrentButtonPanel() to get the panel of the currently selected button
     */
    @Override
    public String getButtonPanel() { return "Template"; }

    /* Text string to be shown in the infotext panel on the main editor window
     * when this button is selected.
     */
    @Override
    public String getInfoText() { return getLocaleString(""); }

    //
    // These entries are for demonstration only and none are mandatory
    // only override if you want to replace or extend the default behavior
    // * @see BaseButton for default behavior *

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

    /*
     * If you need to draw anything on the map panel, use this function.
     * anything here is drawn after the main network nodes/connections are rendered.
     */
    @Override
    public void drawToScreen(Graphics g) {}


    /*
     * This function is used to createSetting a tooltip for the button.
     * e.g. return "This button does this and that"
     *      return "<html><center><b>This button does this and that</b><br>and this too</center></html>"
     *      return getLocaleString("this_button_does_this_that_localized_text")
     */
    @Override
    public String buildToolTip() {
        return "";
    }
}
