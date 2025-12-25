package AutoDriveEditor.GUI.Menus.Display;

import AutoDriveEditor.Classes.KeyBinds.Shortcut;
import AutoDriveEditor.GUI.Menus.JCheckBoxMenuItemBase;
import AutoDriveEditor.Managers.ShortcutManager;

import javax.swing.*;
import java.awt.event.ItemEvent;

import static AutoDriveEditor.AutoDriveEditor.*;
import static AutoDriveEditor.Managers.ShortcutManager.ShortcutID.*;
import static AutoDriveEditor.XMLConfig.EditorXML.bShowMarkerNames;

public class ShowMarkerNames extends JCheckBoxMenuItemBase {

    public ShowMarkerNames() {
        Shortcut showMarkerNamesShortcut = ShortcutManager.getUserShortcutByID(TOGGLE_MARKER_NAMES_SHORTCUT);
        if (showMarkerNamesShortcut != null) {
            ShortcutManager.registerMenuShortcut(this, "menu_display_show_marker_names", bShowMarkerNames, showMarkerNamesShortcut);
        }
    }

    @Override
    public void itemStateChanged(ItemEvent e) {
        super.itemStateChanged(e);
        AbstractButton menuItem = (AbstractButton) e.getItem();
        bShowMarkerNames = menuItem.isSelected();
        getMapPanel().repaint();
    }
}
