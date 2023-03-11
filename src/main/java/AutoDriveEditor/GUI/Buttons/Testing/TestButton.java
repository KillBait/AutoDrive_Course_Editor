package AutoDriveEditor.GUI.Buttons.Testing;

import AutoDriveEditor.GUI.Buttons.BaseButton;

import javax.swing.*;
import java.awt.event.ActionEvent;

import static AutoDriveEditor.Locale.LocaleManager.getLocaleString;
import static AutoDriveEditor.Utils.GUIUtils.makeButton;

public class TestButton extends BaseButton {

    // test overrides

    public TestButton(JPanel panel) {
        button = makeButton(null, "nodes_test_tooltip", "nodes_test_alt", panel, null, false, this, true);
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
        // do stuff here on click
    }
}
