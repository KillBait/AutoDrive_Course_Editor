package AutoDriveEditor.GUI.Menus.DebugMenu.Logging;

import AutoDriveEditor.GUI.Menus.JCheckBoxMenuItemBase;

import javax.swing.*;
import java.awt.event.ItemEvent;

public class LogFileIOMenu extends JCheckBoxMenuItemBase {

    public static boolean bDebugLogFileIO;

    public LogFileIOMenu() {
        makeCheckBoxMenuItem("menu_debug_log_fileio", "menu_debug_log_fileio_accstring", false, true);
    }

    @Override
    public void itemStateChanged(ItemEvent e) {
        super.itemStateChanged(e);
        AbstractButton menuItem = (AbstractButton) e.getItem();
        bDebugLogFileIO = menuItem.isSelected();
    }
}
