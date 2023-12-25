package AutoDriveEditor.GUI.Menus.DebugMenu.Logging;

import AutoDriveEditor.GUI.Menus.JCheckBoxMenuItemBase;

import javax.swing.*;
import java.awt.event.ItemEvent;

public class LogCopyPasteMenu extends JCheckBoxMenuItemBase {

    public static boolean bDebugLogCopyPasteInfo;

    public LogCopyPasteMenu() {
        makeCheckBoxMenuItem("menu_debug_log_copypaste_info", "menu_debug_log_copypaste_info_accstring", false, true);
    }

    @Override
    public void itemStateChanged(ItemEvent e) {
        super.itemStateChanged(e);
        AbstractButton menuItem = (AbstractButton) e.getItem();
        bDebugLogCopyPasteInfo = menuItem.isSelected();
    }
}
