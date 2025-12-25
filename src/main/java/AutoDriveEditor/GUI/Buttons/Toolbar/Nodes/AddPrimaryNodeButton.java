package AutoDriveEditor.GUI.Buttons.Toolbar.Nodes;

import AutoDriveEditor.Classes.KeyBinds.Shortcut;
import AutoDriveEditor.Classes.UI_Components.ScaleAnimIcon;
import AutoDriveEditor.GUI.Buttons.AddNodeBaseButton;
import AutoDriveEditor.GUI.Buttons.BaseButton;
import AutoDriveEditor.Managers.ShortcutManager;

import javax.swing.*;

import static AutoDriveEditor.Locale.LocaleManager.getLocaleString;
import static AutoDriveEditor.Managers.IconManager.*;
import static AutoDriveEditor.Managers.ShortcutManager.ShortcutID.NEW_NODE_SHORTCUT;
import static AutoDriveEditor.RoadNetwork.MapNode.NODE_FLAG_REGULAR;

public class AddPrimaryNodeButton extends AddNodeBaseButton {

    @Override
    public String getButtonID() { return "AddPrimaryNodeButton"; }

    @Override
    public String getButtonAction() { return "ActionButton"; }

    @Override
    public String getButtonPanel() { return "Nodes"; }

    @Override
    public String getInfoText() {
        String current = getLocaleString("toolbar_nodes_connection_type_regular");
        return getLocaleString("toolbar_nodes_create_node_infotext").replace("{current}", current);
    }

    @Override
    public void onButtonCreation() {
        super.onButtonCreation();
        addNodeShortcutGroup.addButton(0, (BaseButton) this);
    }

    public AddPrimaryNodeButton(JPanel panel) {
        ScaleAnimIcon animAddRegularNodeIcon = createScaleAnimIcon(BUTTON_ADD_NODE_REGULAR_ICON, false);
        button = createAnimToggleButton(animAddRegularNodeIcon, panel, null, null,  false, false, this);
        nodeType = NODE_FLAG_REGULAR;
    }

    @Override
    public String buildToolTip() {
        String current = getLocaleString("toolbar_nodes_connection_type_regular");
        String tooltip = getLocaleString("toolbar_nodes_create_node_tooltip").replace("{current}", current);
        Shortcut s = ShortcutManager.getUserShortcutByID(NEW_NODE_SHORTCUT);
        if (s != null) {
            return tooltip + "\n\n( Shortcut: " + s.getShortcutString() + " )";
        } else {
            return tooltip;
        }
    }
}
