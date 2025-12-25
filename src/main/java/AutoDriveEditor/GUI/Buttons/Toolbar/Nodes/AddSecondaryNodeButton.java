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
import static AutoDriveEditor.RoadNetwork.MapNode.NODE_FLAG_SUBPRIO;

public class AddSecondaryNodeButton extends AddNodeBaseButton {

    @Override
    public String getButtonID() { return "AddSecondaryNodeButton"; }

    @Override
    public String getButtonAction() { return "ActionButton"; }

    @Override
    public String getButtonPanel() { return "Nodes"; }

    @Override
    public String getInfoText() {
        String current = getLocaleString("toolbar_nodes_connection_type_subprio");
        return getLocaleString("toolbar_nodes_create_node_infotext").replace("{current}", current);
    }

    @Override
    public void onButtonCreation() {
        super.onButtonCreation();
        addNodeShortcutGroup.addButton(1, (BaseButton) this);
    }

    public AddSecondaryNodeButton(JPanel panel) {
        ScaleAnimIcon animAddSubprioNodeIcon = createScaleAnimIcon(BUTTON_ADD_NODE_SUBPRIO_ICON, false);
        button = createAnimToggleButton(animAddSubprioNodeIcon, panel, null, null,  false, false, this);
        nodeType = NODE_FLAG_SUBPRIO;
    }

    @Override
    public String buildToolTip() {
//        String current = getLocaleString("toolbar_nodes_connection_type_subprio");
//        return getLocaleString("toolbar_nodes_create_node_tooltip").replace("{current}", current);

        String current = getLocaleString("toolbar_nodes_connection_type_subprio");
        String tooltip = getLocaleString("toolbar_nodes_create_node_tooltip").replace("{current}", current);
        Shortcut s = ShortcutManager.getUserShortcutByID(NEW_NODE_SHORTCUT);
        if (s != null) {
            return tooltip + "\n\n( Shortcut: " + s.getShortcutString() + " )";
        } else {
            return tooltip;
        }

    }
}
