package AutoDriveEditor.GUI.Buttons.Toolbar.Nodes;

import AutoDriveEditor.Classes.KeyBinds.Shortcut;
import AutoDriveEditor.Classes.UI_Components.JToggleStateButton;
import AutoDriveEditor.Classes.UI_Components.ScaleAnimIcon;
import AutoDriveEditor.GUI.Buttons.BaseButton;
import AutoDriveEditor.GUI.Buttons.LinerLineBaseButton;
import AutoDriveEditor.Managers.ShortcutManager;
import AutoDriveEditor.RoadNetwork.Connection;

import javax.swing.*;
import java.awt.event.MouseAdapter;
import java.awt.event.MouseEvent;
import java.awt.event.MouseListener;

import static AutoDriveEditor.GUI.TextPanel.showInTextArea;
import static AutoDriveEditor.Locale.LocaleManager.getLocaleString;
import static AutoDriveEditor.Managers.IconManager.*;
import static AutoDriveEditor.Managers.RenderManager.PRIORITY_HIGH;
import static AutoDriveEditor.Managers.ShortcutManager.ShortcutID.NEW_LINEAR_LINE_SHORTCUT;
import static AutoDriveEditor.RoadNetwork.MapNode.NODE_FLAG_REGULAR;
import static AutoDriveEditor.RoadNetwork.MapNode.NODE_FLAG_SUBPRIO;

public class DualConnectionButton extends LinerLineBaseButton implements MouseListener {

    @Override
    public String getButtonID() { return "DualConnectionButton"; }

    @Override
    public String getButtonAction() { return "ActionButton"; }

    @Override
    public String getButtonPanel() { return "Nodes"; }

    @Override
    public String getInfoText() { return getLocaleString("toolbar_nodes_connection_create_infotext"); }

    @Override
    public void onButtonCreation() {
        super.onButtonCreation();
        linerLineShortcutGroup.addButton(1, (BaseButton) this);
    }

    @Override
    public void onButtonSelect() {
        if (linearLine != null) {
            linearLine.setConnectionType(Connection.ConnectionType.DUAL);
            linearLine.setNodeFlagType(dualConnectionNodePriority);
        }
    }

    private int dualConnectionNodePriority = NODE_FLAG_REGULAR;

    public DualConnectionButton(JPanel panel) {

        ScaleAnimIcon animAddDualConnectionIcon = createScaleAnimIcon(BUTTON_ADD_DUAL_CONNECTION_ICON, false);
        JToggleStateButton dualConnectionButton = createAnimToggleStateButton(animAddDualConnectionIcon, panel, null, null,  false, false, this);
        dualConnectionButton.addState(BUTTON_ADD_SUBPRIO_DUAL_CONNECTION_ICON, SUBPRIO_PRIORITY_STRING, "");
        dualConnectionButton.addState(BUTTON_ADD_DUAL_CONNECTION_ICON, NORMAL_PRIORITY_STRING, "");
        button = dualConnectionButton;
        button.addMouseListener(new MouseAdapter() {
            @Override
            public void mouseClicked(MouseEvent e) {
                if (button.isEnabled() && SwingUtilities.isRightMouseButton(e)) {
                    JToggleStateButton currentButton = (JToggleStateButton) button;
                    if (currentButton.getCurrentStateName().equals(NORMAL_PRIORITY_STRING)) {
                        dualConnectionNodePriority = NODE_FLAG_REGULAR;
                        linearLine.setNodeFlagType(dualConnectionNodePriority);
                    } else {
                        dualConnectionNodePriority = NODE_FLAG_SUBPRIO;
                        linearLine.setNodeFlagType(dualConnectionNodePriority);
                    }
                    showInTextArea(currentButton.getCurrentStateTooltip(), true, false);
                    currentButton.nextState();
                    updateTooltip();
                }
            }
        });
        setRenderPriority(PRIORITY_HIGH);
    }

    @Override
    public String buildToolTip() {
        String current = getLocaleString("toolbar_nodes_connection_type_dual");
        String tooltip = getLocaleString("toolbar_nodes_connection_create_tooltip").replace("{current}", current);
        Shortcut s = ShortcutManager.getUserShortcutByID(NEW_LINEAR_LINE_SHORTCUT);
        if (s != null) {
            return tooltip + "\n\n( Shortcut: " + s.getShortcutString() + " )";
        } else {
            return tooltip;
        }
    }
}