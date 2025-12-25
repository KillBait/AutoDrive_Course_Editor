package AutoDriveEditor.GUI.Buttons.ActionBar.ConfigPanel;

import AutoDriveEditor.Classes.UI_Components.ScaleAnimIcon;
import AutoDriveEditor.GUI.Buttons.OptionsBaseButton;
import AutoDriveEditor.GUI.Config.ConfigGUI;

import javax.swing.*;
import java.awt.event.ActionEvent;

import static AutoDriveEditor.AutoDriveEditor.editor;
import static AutoDriveEditor.GUI.Config.ConfigGUI.configGUI;
import static AutoDriveEditor.Locale.LocaleManager.getLocaleString;
import static AutoDriveEditor.Managers.IconManager.*;

public class ConfigButton extends OptionsBaseButton {

    @Override
    public String getButtonID() { return "OpenConfigButton"; }

    @Override
    public String getButtonAction() { return "ActionBarButton"; }

    @Override
    public String getButtonPanel() { return "ActionBar"; }

    @Override
    public void setEnabled(boolean enabled) {}

    @Override
    public void actionPerformed(ActionEvent e) {
        super.actionPerformed(e);
        if (!ConfigGUI.isConfigWindowOpen) {
            ConfigGUI.createConfigGUI(editor);
        } else {
            configGUI.toFront();
        }
    }

    //public static ConfigListener configListener;

    public ConfigButton(JPanel panel) {
        ScaleAnimIcon animConfigIcon = createScaleAnimIcon(OPEN_CONFIG_ICON, false);
        button = createAnimButton(animConfigIcon, panel, null, null, false, true, this);
        //setTooltips(getLocaleString("actionbar_options_config_open_tooltip"));
        //configListener = new ConfigListener();
    }


    @Override
    public String buildToolTip() {
        return getLocaleString("actionbar_options_config_open_tooltip");
    }
}
