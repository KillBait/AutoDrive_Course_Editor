package AutoDriveEditor.GUI.Menus.DebugMenu.Logging;

import AutoDriveEditor.GUI.Menus.JCheckBoxMenuItemBase;

import javax.swing.*;
import java.awt.event.ItemEvent;

public class LogListenerStateMenu extends JCheckBoxMenuItemBase {

    public static boolean bDebugMenuState;

    public LogListenerStateMenu() {
        makeCheckBoxMenuItem("menu_debug_log_menu", "menu_debug_log_menu_accstring", false, true);
    }

    @Override
    public void itemStateChanged(ItemEvent e) {
        super.itemStateChanged(e);
        AbstractButton menuItem = (AbstractButton) e.getItem();
        bDebugMenuState = menuItem.isSelected();
    }
}
