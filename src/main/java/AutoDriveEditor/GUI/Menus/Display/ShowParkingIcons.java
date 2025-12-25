package AutoDriveEditor.GUI.Menus.Display;

import AutoDriveEditor.Classes.KeyBinds.Shortcut;
import AutoDriveEditor.GUI.Menus.JCheckBoxMenuItemBase;
import AutoDriveEditor.Managers.ShortcutManager;

import javax.swing.*;
import java.awt.event.ItemEvent;

import static AutoDriveEditor.AutoDriveEditor.getMapPanel;
import static AutoDriveEditor.Managers.ShortcutManager.ShortcutID.TOGGLE_PARKING_ICONS_SHORTCUT;
import static AutoDriveEditor.XMLConfig.EditorXML.bShowParkingIcons;

public class ShowParkingIcons extends JCheckBoxMenuItemBase {

    public ShowParkingIcons() {

        Shortcut showParkingIconShortcut = ShortcutManager.getUserShortcutByID(TOGGLE_PARKING_ICONS_SHORTCUT);
        if (showParkingIconShortcut != null) {
            ShortcutManager.registerMenuShortcut(this, "menu_display_show_parking_icons", bShowParkingIcons, showParkingIconShortcut);
        }
    }

    @Override
    public void itemStateChanged(ItemEvent e) {
        super.itemStateChanged(e);
        AbstractButton menuItem = (AbstractButton) e.getItem();
        bShowParkingIcons = menuItem.isSelected();
        getMapPanel().repaint();
    }
}
