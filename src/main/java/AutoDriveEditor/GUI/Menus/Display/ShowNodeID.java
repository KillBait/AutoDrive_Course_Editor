package AutoDriveEditor.GUI.Menus.Display;

import AutoDriveEditor.Classes.KeyBinds.Shortcut;
import AutoDriveEditor.GUI.Menus.JCheckBoxMenuItemBase;
import AutoDriveEditor.Managers.ShortcutManager;

import javax.swing.*;
import java.awt.event.ItemEvent;

import static AutoDriveEditor.AutoDriveEditor.getMapPanel;
import static AutoDriveEditor.Classes.Util_Classes.GUIUtils.InputEvent_NONE;
import static AutoDriveEditor.Classes.Util_Classes.GUIUtils.KeyEvent_NONE;
import static AutoDriveEditor.Managers.ShortcutManager.ShortcutID.SHOW_NODE_ID_ON_HOVER_SHORTCUT;
import static AutoDriveEditor.Managers.ShortcutManager.ShortcutID.TOGGLE_PARKING_ICONS_SHORTCUT;
import static AutoDriveEditor.Managers.ShortcutManager.getUserShortcutByID;
import static AutoDriveEditor.XMLConfig.EditorXML.bShowParkingIcons;
import static AutoDriveEditor.XMLConfig.EditorXML.bShowSelectedNodeID;

public class ShowNodeID extends JCheckBoxMenuItemBase {

    public ShowNodeID() {

        Shortcut showNodeIDShortcut = getUserShortcutByID(SHOW_NODE_ID_ON_HOVER_SHORTCUT);
        if (showNodeIDShortcut != null) {
            ShortcutManager.registerMenuShortcut(this, "menu_display_showID", bShowSelectedNodeID, showNodeIDShortcut);
        }

        //makeCheckBoxMenuItem("menu_display_showID", KeyEvent_NONE, InputEvent_NONE, bShowSelectedNodeID,true);
    }

    public void itemStateChanged(ItemEvent e) {
        super.itemStateChanged(e);
        AbstractButton menuItem = (AbstractButton) e.getItem();
        bShowSelectedNodeID = menuItem.isSelected();
        getMapPanel().repaint();
    }
}
