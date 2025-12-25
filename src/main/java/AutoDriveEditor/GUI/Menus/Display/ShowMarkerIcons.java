package AutoDriveEditor.GUI.Menus.Display;

import AutoDriveEditor.Classes.KeyBinds.Shortcut;
import AutoDriveEditor.GUI.Menus.JCheckBoxMenuItemBase;
import AutoDriveEditor.Managers.ShortcutManager;

import javax.swing.*;
import java.awt.event.ItemEvent;

import static AutoDriveEditor.AutoDriveEditor.getMapPanel;
import static AutoDriveEditor.Managers.ShortcutManager.ShortcutID.TOGGLE_MARKER_ICONS_SHORTCUT;
import static AutoDriveEditor.XMLConfig.EditorXML.bShowMarkerIcons;

public class ShowMarkerIcons extends JCheckBoxMenuItemBase {

    public ShowMarkerIcons() {
        Shortcut showMarkerIconShortcut = ShortcutManager.getUserShortcutByID(TOGGLE_MARKER_ICONS_SHORTCUT);
        if (showMarkerIconShortcut != null) {
            ShortcutManager.registerMenuShortcut(this, "menu_display_show_marker_icons", bShowMarkerIcons, showMarkerIconShortcut);
        }
    }

    @Override
    public void itemStateChanged(ItemEvent e) {
        super.itemStateChanged(e);
        AbstractButton menuItem = (AbstractButton) e.getItem();
        bShowMarkerIcons = menuItem.isSelected();
        getMapPanel().repaint();
    }
}
