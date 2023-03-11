package AutoDriveEditor.GUI.Buttons.Options;

import AutoDriveEditor.GUI.Buttons.OptionsBaseButton;
import AutoDriveEditor.GUI.Config.ConfigGUI;
import AutoDriveEditor.Listeners.ConfigListener;

import javax.swing.*;
import java.awt.event.ActionEvent;

import static AutoDriveEditor.AutoDriveEditor.editor;
import static AutoDriveEditor.GUI.Config.ConfigGUI.configGUI;
import static AutoDriveEditor.GUI.Config.ConfigGUI.createConfigGUI;
import static AutoDriveEditor.Utils.GUIUtils.makeImageButton;

public class OpenConfigButton extends OptionsBaseButton {

    public static ConfigListener configListener;

    public OpenConfigButton(JPanel panel) {
        button = makeImageButton("buttons/config", "buttons/config_selected", null, "options_config_open_tooltip","options_config_open_alt", panel, true, this);
        configListener = new ConfigListener();
    }

    @Override
    public String getButtonID() { return "OpenConfigButton"; }

    @Override
    public void actionPerformed(ActionEvent e) {
        super.actionPerformed(e);
        if (!ConfigGUI.isConfigWindowOpen) {
            createConfigGUI(editor);
        } else {
            configGUI.toFront();
        }
    }

    // Override base function - ignore any enabled/disabled messages as we want it available at all times
    @Override
    public void setEnabled(boolean enabled) {}

}
