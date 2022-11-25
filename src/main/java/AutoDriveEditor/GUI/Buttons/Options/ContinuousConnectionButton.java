package AutoDriveEditor.GUI.Buttons.Options;

import AutoDriveEditor.GUI.Buttons.OptionsBaseButton;

import javax.swing.*;
import java.awt.event.ActionEvent;

import static AutoDriveEditor.Locale.LocaleManager.getLocaleString;
import static AutoDriveEditor.Utils.GUIUtils.makeImageToggleButton;
import static AutoDriveEditor.Utils.GUIUtils.showInTextArea;
import static AutoDriveEditor.XMLConfig.EditorXML.bContinuousConnections;

public class ContinuousConnectionButton extends OptionsBaseButton {

    public ContinuousConnectionButton(JPanel panel) {
        String tooltip;
        boolean isSelected;

        if (bContinuousConnections) {
            tooltip = "options_con_connect_enabled_tooltip";
            isSelected = false;
        } else {
            tooltip = "options_con_connect_disabled_tooltip";
            isSelected = true;
        }
        button = makeImageToggleButton("buttons/conconnect", "buttons/conconnect_selected", null, tooltip, "options_con_connect_alt", panel, !bContinuousConnections, false, null, false, this);
        button.setSelected(isSelected);
    }

    @Override
    public String getButtonID() { return "ContinuousConnectionButton"; }

    @Override
    public String getButtonAction() { return "OptionToggle"; }

    @Override
    public String getButtonPanel() { return "Options"; }

    @Override
    public String getInfoText() { return null; }

    @Override
    public void actionPerformed(ActionEvent e) {
        super.actionPerformed(e);
        bContinuousConnections = !bContinuousConnections;
        if (bContinuousConnections) {
            button.setToolTipText(getLocaleString("options_con_connect_enabled_tooltip"));
            showInTextArea(getLocaleString("options_con_connect_enabled_tooltip"), true, false);
        } else {
            button.setToolTipText(getLocaleString("options_con_connect_disabled_tooltip"));
            showInTextArea(getLocaleString("options_con_connect_disabled_tooltip"), true, false);
        }
    }
}
