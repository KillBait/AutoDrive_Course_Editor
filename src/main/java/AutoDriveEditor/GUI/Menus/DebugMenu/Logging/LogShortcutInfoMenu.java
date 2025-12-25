package AutoDriveEditor.GUI.Menus.DebugMenu.Logging;

import AutoDriveEditor.GUI.Menus.JCheckBoxMenuItemBase;

import javax.swing.*;
import java.awt.event.ItemEvent;

public class LogShortcutInfoMenu extends JCheckBoxMenuItemBase {

    public static boolean bDebugLogShortcutInfo;

    public LogShortcutInfoMenu() {
        makeCheckBoxMenuItem("menu_debug_log_shortcut", false, true);
    }

    @Override
    public void itemStateChanged(ItemEvent e) {
        super.itemStateChanged(e);
        AbstractButton menuItem = (AbstractButton) e.getItem();
        bDebugLogShortcutInfo = menuItem.isSelected();
    }
}
