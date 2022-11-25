package AutoDriveEditor.GUI.Buttons.Testing;

import AutoDriveEditor.GUI.Buttons.BaseButton;

import javax.swing.*;
import java.awt.event.ActionEvent;

import static AutoDriveEditor.Locale.LocaleManager.getLocaleString;
import static AutoDriveEditor.Utils.GUIUtils.makeButton;
import static AutoDriveEditor.Utils.LoggerUtils.LOG;

public class TestButton extends BaseButton {

    // test overrides

    public TestButton(JPanel panel) {
        button = makeButton(null, "nodes_test_tooltip", "nodes_test_alt", panel, null, false, this, true);
        //button = makeImageButton("buttons/copy", "buttons/copy_selected", null, "copypaste_copy_tooltip", "copypaste_copy_alt", panel,true, this);
    }

    @Override
    public String getButtonID() { return "TestButton"; }

    @Override
    public String getButtonAction() { return "ActionButton"; }

    @Override
    public String getButtonPanel() { return "Test"; }

    @Override
    public String getInfoText() { return getLocaleString("nodes_test_tooltip"); }

    @Override
    public void actionPerformed(ActionEvent e) {
        super.actionPerformed(e);
        LOG.info("Super called");
    }
}
