package AutoDriveEditor.GUI.Menus.DebugMenu.Logging;

import AutoDriveEditor.GUI.Menus.JCheckBoxMenuItemBase;

import javax.swing.*;
import java.awt.event.ItemEvent;

public class LogScanManagerInfoMenu extends JCheckBoxMenuItemBase {

    public static boolean bDebugLogScanManagerInfo;

    public LogScanManagerInfoMenu() {
        makeCheckBoxMenuItem("menu_debug_log_scanmanager_info", "menu_debug_log_scanmanager_info_accstring", false, true);
    }

    @Override
    public void itemStateChanged(ItemEvent e) {
        super.itemStateChanged(e);
        AbstractButton menuItem = (AbstractButton) e.getItem();
        bDebugLogScanManagerInfo = menuItem.isSelected();
    }
}
