package AutoDriveEditor.GUI.Buttons.Editing;

import AutoDriveEditor.GUI.Buttons.CopyPasteBaseButton;

import javax.swing.*;
import java.awt.event.ActionEvent;

import static AutoDriveEditor.GUI.MenuBuilder.bDebugLogCopyPasteInfo;
import static AutoDriveEditor.Utils.GUIUtils.makeImageButton;
import static AutoDriveEditor.Utils.LoggerUtils.LOG;

public class CopySelectionButton extends CopyPasteBaseButton {

    public CopySelectionButton(JPanel panel) {
        button = makeImageButton("buttons/copy", "buttons/copy_selected", null, "copypaste_copy_tooltip","copypaste_copy_alt", panel, false, this);
    }

    @Override
    public String getButtonID() { return "CopySelectionButton"; }

    @Override
    public String getButtonAction() { return "ActionButton"; }

    @Override
    public String getButtonPanel() { return "Edit"; }

    @Override
    public Boolean ignoreDeselect() { return true; }

    @Override
    public void actionPerformed(ActionEvent e) {
        super.actionPerformed(e);
        if (bDebugLogCopyPasteInfo) LOG.info("CopySelectionButton > Button Pressed");
        copySelected();
    }
}
