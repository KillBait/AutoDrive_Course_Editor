package AutoDriveEditor.GUI.Menus.DebugMenu.Logging;

import AutoDriveEditor.GUI.Menus.JCheckBoxMenuItemBase;

import javax.swing.*;
import java.awt.event.ItemEvent;

public class LogCopyPasteManagerMenu extends JCheckBoxMenuItemBase {

    public static boolean bDebugLogCopyPasteManagerInfo;

    public LogCopyPasteManagerMenu() {
        makeCheckBoxMenuItem("menu_debug_log_copypaste", false, true);
    }

    @Override
    public void itemStateChanged(ItemEvent e) {
        super.itemStateChanged(e);
        AbstractButton menuItem = (AbstractButton) e.getItem();
        bDebugLogCopyPasteManagerInfo = menuItem.isSelected();
    }
}
