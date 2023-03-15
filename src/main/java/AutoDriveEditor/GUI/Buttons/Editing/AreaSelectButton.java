package AutoDriveEditor.GUI.Buttons.Editing;

import AutoDriveEditor.GUI.Buttons.CopyPasteBaseButton;

import javax.swing.*;

import static AutoDriveEditor.Locale.LocaleManager.getLocaleString;
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
    public Boolean ignoreMultiSelect() { return false; }
}
