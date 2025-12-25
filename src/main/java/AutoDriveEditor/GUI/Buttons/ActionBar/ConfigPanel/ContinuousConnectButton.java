package AutoDriveEditor.GUI.Buttons.ActionBar.ConfigPanel;

import AutoDriveEditor.Classes.KeyBinds.Shortcut;
import AutoDriveEditor.Classes.UI_Components.ScaleAnimIcon;
import AutoDriveEditor.GUI.Buttons.OptionsBaseButton;

import javax.swing.*;
import java.awt.event.ActionEvent;

import static AutoDriveEditor.AutoDriveEditor.getMapPanel;
import static AutoDriveEditor.GUI.TextPanel.showInTextArea;
import static AutoDriveEditor.Locale.LocaleManager.getLocaleString;
import static AutoDriveEditor.Managers.IconManager.*;
import static AutoDriveEditor.Managers.ShortcutManager.ShortcutID.TOGGLE_CONTINUOUS_CONNECTION_SHORTCUT;
import static AutoDriveEditor.Managers.ShortcutManager.getUserShortcutByID;
import static AutoDriveEditor.Managers.ShortcutManager.registerShortcut;
import static AutoDriveEditor.XMLConfig.EditorXML.bContinuousConnections;

public class ContinuousConnectButton extends OptionsBaseButton {

    @Override
    public String getButtonID() { return "ContinuousConnectButton"; }

    @Override
    public String getButtonAction() { return "ActionBarButton"; }

    @Override
    public String getButtonPanel() { return "ActionBar"; }


    @Override
    public void actionPerformed(ActionEvent e) {
        super.actionPerformed(e);
        bContinuousConnections = !bContinuousConnections;
        setSelected(bContinuousConnections);

        updateTooltip();
        forceShowToolTip(button);
        showInTextArea(getToolTip(),true,false);
    }

    public ContinuousConnectButton(JPanel panel) {
        ScaleAnimIcon animConConnectIcon = createToggleScalingAnimatedIcon(CONTINIOUS_CONNECT_OFF_ICON, CONTINIOUS_CONNECT_ON_ICON, bContinuousConnections, 20, 20, 1.0f, .25f, 100);
        button = createAnimToggleButton(animConConnectIcon, panel, null, null, bContinuousConnections, false, this);
        // Setup Keyboard Shortcut
        Shortcut continuousConnectionShortcut = getUserShortcutByID(TOGGLE_CONTINUOUS_CONNECTION_SHORTCUT);
        if (continuousConnectionShortcut != null) {
            Action continuousConnectionAction = new AbstractAction() {
                @Override
                public void actionPerformed(ActionEvent e) {
                    if (button.isEnabled()) {
                        bContinuousConnections = !bContinuousConnections;
                        button.setSelected(bContinuousConnections);
                        if (button.getIcon() instanceof ScaleAnimIcon) {
                            ((ScaleAnimIcon)button.getIcon()).setSelected(bContinuousConnections);
                            ((ScaleAnimIcon) button.getIcon()).startAnimation(button);
                        }
                        updateTooltip();
                    }
                }
            };
            registerShortcut(this, continuousConnectionShortcut, continuousConnectionAction, getMapPanel());
        }
    }

    @Override
    public String buildToolTip() {
        String connectCurrent = bContinuousConnections ? getLocaleString("actionbar_tooltip_common_enabled") : getLocaleString("actionbar_tooltip_common_disabled");
        return getLocaleString("actionbar_options_continuous_connection_tooltip").replace("{current}", connectCurrent);
    }
}
