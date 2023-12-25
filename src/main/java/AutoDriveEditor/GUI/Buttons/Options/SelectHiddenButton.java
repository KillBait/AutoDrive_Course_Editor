package AutoDriveEditor.GUI.Buttons.Options;

import AutoDriveEditor.GUI.Buttons.OptionsBaseButton;

import javax.swing.*;
import java.awt.event.ActionEvent;

import static AutoDriveEditor.GUI.TextPanel.showInTextArea;
import static AutoDriveEditor.Locale.LocaleManager.getLocaleString;
import static AutoDriveEditor.Utils.GUIUtils.makeImageToggleButton;
import static AutoDriveEditor.XMLConfig.EditorXML.bSelectHidden;

public class SelectHiddenButton extends OptionsBaseButton {

    public SelectHiddenButton(JPanel panel) {
        String tooltip;
        boolean isSelected;

        if (bSelectHidden) {
            tooltip = "display_select_hidden_enabled_tooltip";
            isSelected = false;
        } else {
            tooltip = "display_select_hidden_disabled_tooltip";
            isSelected = true;
        }

        button = makeImageToggleButton("buttons/hidden","buttons/hidden_selected", null, tooltip, tooltip, panel, false, false, null, false, this);
        button.setSelected(isSelected);
    }

    @Override
    public String getButtonID() { return "SelectHiddenButton"; }

    @Override
    public String getButtonAction() { return "ActionButton"; }

    @Override
    public String getButtonPanel() { return "Options"; }

    @Override
    public String getInfoText() { return getLocaleString("nodes_create_primary_node_tooltip"); }

    @Override
    public void actionPerformed(ActionEvent e) {
        super.actionPerformed(e);
        bSelectHidden = !bSelectHidden;
        if (bSelectHidden) {
            button.setToolTipText(getLocaleString("display_select_hidden_enabled_tooltip"));
            showInTextArea(getLocaleString("display_select_hidden_enabled_tooltip"), true, false);
        } else {
            button.setToolTipText(getLocaleString("display_select_hidden_disabled_tooltip"));
            showInTextArea(getLocaleString("display_select_hidden_disabled_tooltip"), true, false);
        }
    }
}
