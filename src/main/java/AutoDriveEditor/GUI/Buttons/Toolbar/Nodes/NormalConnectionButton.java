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

public class NormalConnectionButton extends LinerLineBaseButton implements MouseListener{

    @Override
    public String getButtonID() { return "NormalConnectionButton"; }

    @Override
    public String getButtonAction() { return "ActionButton"; }

    @Override
    public String getButtonPanel() { return "Nodes"; }

    @Override
    public String getInfoText() { return getLocaleString("toolbar_nodes_connection_create_infotext"); }

    @Override
    public void onButtonCreation() {
        super.onButtonCreation();
        linerLineShortcutGroup.addButton(0, (BaseButton) this);
    }

    @Override
    public void onButtonSelect() {
        if (linearLine != null) {
            linearLine.setConnectionType(Connection.ConnectionType.REGULAR);
            linearLine.setNodeFlagType(normalConnectionNodePriority);
        }
    }

    private int normalConnectionNodePriority = NODE_FLAG_REGULAR;

    public NormalConnectionButton(JPanel panel) {

        ScaleAnimIcon animAddRegularConnectionIcon = createScaleAnimIcon(BUTTON_ADD_NORMAL_CONNECTION_ICON, false);
        JToggleStateButton normalConnectionButton = createAnimToggleStateButton(animAddRegularConnectionIcon, panel, null, null,  false, false, this);
        normalConnectionButton.addState(BUTTON_ADD_SUBPRIO_NORMAL_CONNECTION_ICON, SUBPRIO_PRIORITY_STRING, "");
        normalConnectionButton.addState(BUTTON_ADD_NORMAL_CONNECTION_ICON, NORMAL_PRIORITY_STRING, "");
        button = normalConnectionButton;
        button.addMouseListener(new MouseAdapter() {
            @Override
            public void mouseClicked(MouseEvent e) {
                if (button.isEnabled() && SwingUtilities.isRightMouseButton(e)) {
                    JToggleStateButton currentButton = (JToggleStateButton) button;
                    if (currentButton.getCurrentStateName().equals(NORMAL_PRIORITY_STRING)) {
                        normalConnectionNodePriority = NODE_FLAG_REGULAR;
                        linearLine.setNodeFlagType(normalConnectionNodePriority);
                    } else {
                        normalConnectionNodePriority = NODE_FLAG_SUBPRIO;
                        linearLine.setNodeFlagType(normalConnectionNodePriority);
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
        String current = getLocaleString("toolbar_nodes_connection_type_regular");
        String tooltip = getLocaleString("toolbar_nodes_connection_create_tooltip").replace("{current}", current);
        Shortcut s = ShortcutManager.getUserShortcutByID(NEW_LINEAR_LINE_SHORTCUT);
        if (s != null) {
            return tooltip + "\n\n( Shortcut: " + s.getShortcutString() + " )";
        } else {
            return tooltip;
        }
    }
}