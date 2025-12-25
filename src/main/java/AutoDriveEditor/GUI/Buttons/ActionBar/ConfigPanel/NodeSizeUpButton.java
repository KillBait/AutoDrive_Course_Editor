package AutoDriveEditor.GUI.Buttons.ActionBar.ConfigPanel;

import AutoDriveEditor.Classes.KeyBinds.Shortcut;
import AutoDriveEditor.Classes.UI_Components.ScaleAnimIcon;
import AutoDriveEditor.GUI.Buttons.OptionsBaseButton;

import javax.swing.*;
import java.awt.event.ActionEvent;

import static AutoDriveEditor.AutoDriveEditor.getMapPanel;
import static AutoDriveEditor.AutoDriveEditor.widgetManager;
import static AutoDriveEditor.Locale.LocaleManager.getLocaleString;
import static AutoDriveEditor.Managers.IconManager.*;
import static AutoDriveEditor.Managers.ShortcutManager.ShortcutID.NODE_SIZE_UP_SHORTCUT;
import static AutoDriveEditor.Managers.ShortcutManager.getUserShortcutByID;
import static AutoDriveEditor.Managers.ShortcutManager.registerShortcut;
import static AutoDriveEditor.XMLConfig.EditorXML.nodeSize;
import static AutoDriveEditor.XMLConfig.EditorXML.setNewNodeSize;

public class NodeSizeUpButton extends OptionsBaseButton {

    @Override
    public String getButtonID() { return "NodeSizeUpButton"; }

    @Override
    public String getButtonAction() { return "ActionBarButton"; }

    @Override
    public String getButtonPanel() { return "ActionBar"; }

    @Override
    public void actionPerformed(ActionEvent e) { super.actionPerformed(e); }

    public NodeSizeUpButton(JPanel panel) {
        ScaleAnimIcon animNodeUpIcon = createScaleAnimIcon(NODE_UP_ICON, false);
        button = createAnimButton(animNodeUpIcon, panel, null, null, false, false, this);

        Timer nodeUpTimer = new Timer(25, e -> {
            setNewNodeSize(nodeSize + 0.10f);
            widgetManager.updateAllWidgets();
        });
        nodeUpTimer.setCoalesce(true);
        nodeUpTimer.setRepeats(true);

        button.getModel().addChangeListener(e -> {
            if (button.getModel().isPressed()) {
                nodeUpTimer.setRepeats(true);
                nodeUpTimer.start();
            } else {
                nodeUpTimer.stop();
            }
        });

        Shortcut nodeUpShortcut = getUserShortcutByID(NODE_SIZE_UP_SHORTCUT);
        if (nodeUpShortcut != null) {
            Action nodeUpAction = new AbstractAction() {
                @Override
                public void actionPerformed(ActionEvent e) {
                    if (button.isEnabled()) {
                        nodeUpTimer.setRepeats(false);
                        nodeUpTimer.start();
                    }
                }
            };
            registerShortcut(this, nodeUpShortcut, nodeUpAction, getMapPanel());
        }
    }

    @Override
    public String buildToolTip() {
        return getLocaleString("actionbar_options_node_size_tooltip").replace("{current}", getLocaleString("actionbar_tooltip_common_Increase"));
    }
}
