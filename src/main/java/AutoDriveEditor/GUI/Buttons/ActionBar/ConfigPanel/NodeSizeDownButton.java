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
import static AutoDriveEditor.Managers.ShortcutManager.ShortcutID.NODE_SIZE_DOWN_SHORTCUT;
import static AutoDriveEditor.Managers.ShortcutManager.getUserShortcutByID;
import static AutoDriveEditor.Managers.ShortcutManager.registerShortcut;
import static AutoDriveEditor.XMLConfig.EditorXML.nodeSize;
import static AutoDriveEditor.XMLConfig.EditorXML.setNewNodeSize;

public class NodeSizeDownButton extends OptionsBaseButton {

    @Override
    public String getButtonID() { return "NodeSizeDownButton"; }

    @Override
    public String getButtonAction() { return "ActionBarButton"; }

    @Override
    public String getButtonPanel() { return "ActionBar"; }

    @Override
    public void actionPerformed(ActionEvent e) { super.actionPerformed(e); }

    public NodeSizeDownButton(JPanel panel) {
        ScaleAnimIcon animNodeDownIcon = createScaleAnimIcon(NODE_DOWN_ICON, false);
        button = createAnimButton(animNodeDownIcon, panel, null, null, false, false, this);

        Timer nodeDownTimer = new Timer(25, e -> {
            if (nodeSize > 0.1) setNewNodeSize(nodeSize - 0.10f);
            widgetManager.updateAllWidgets();
        });
        nodeDownTimer.setCoalesce(true);
        nodeDownTimer.setRepeats(true);

        button.getModel().addChangeListener(e -> {
            if (button.getModel().isPressed()) {
                nodeDownTimer.setRepeats(true);
                nodeDownTimer.start();
            } else {
                nodeDownTimer.stop();
            }
        });

        Shortcut nodeDownShortcut = getUserShortcutByID(NODE_SIZE_DOWN_SHORTCUT);
        if (nodeDownShortcut != null) {
            Action nodeDownAction = new AbstractAction() {
                @Override
                public void actionPerformed(ActionEvent e) {
                    if (button.isEnabled()) {
                        nodeDownTimer.setRepeats(false);
                        nodeDownTimer.start();
                        widgetManager.updateAllWidgets();
                    }
                }
            };
            registerShortcut(this, nodeDownShortcut, nodeDownAction, getMapPanel());
        }
    }

    @Override
    public String buildToolTip() {
        return getLocaleString("actionbar_options_node_size_tooltip").replace("{current}", getLocaleString("actionbar_tooltip_common_Decrease"));
    }
}
