package AutoDriveEditor.GUI.Buttons.Editing;

import AutoDriveEditor.GUI.Buttons.CopyPasteBaseButton;

import javax.swing.*;
import java.awt.event.ActionEvent;

import static AutoDriveEditor.GUI.MenuBuilder.bDebugLogCopyPasteInfo;
import static AutoDriveEditor.Utils.GUIUtils.makeImageButton;
import static AutoDriveEditor.Utils.LoggerUtils.LOG;

public class CutSelectionButton extends CopyPasteBaseButton {

    public CutSelectionButton(JPanel panel) {
        button = makeImageButton("buttons/cut", "buttons/cut_selected", null, "copypaste_cut_tooltip","copypaste_cut_alt", panel, false, this);
    }

    @Override
    public String getButtonID() { return "CutSelectionButton"; }

    @Override
    public String getButtonAction() { return "ActionButton"; }

    @Override
    public String getButtonPanel() { return "Edit"; }

    @Override
    public Boolean ignoreDeselect() { return true; }

    @Override
    public void actionPerformed(ActionEvent e) {
        super.actionPerformed(e);
        if (bDebugLogCopyPasteInfo) LOG.info("CutSelectionButton > Button Pressed");
        cutSelected();
    }
}
