package AutoDriveEditor.GUI.Menus.DebugMenu.Logging;

import AutoDriveEditor.GUI.Menus.JCheckBoxMenuItemBase;

import javax.swing.*;
import java.awt.event.ItemEvent;

public class LogAutoSaveMenu extends JCheckBoxMenuItemBase {

    public static boolean bDebugLogAutoSave;

    public LogAutoSaveMenu() {
        makeCheckBoxMenuItem("menu_debug_log_autosave", false, true);
    }

    @Override
    public void itemStateChanged(ItemEvent e) {
        super.itemStateChanged(e);
        AbstractButton menuItem = (AbstractButton) e.getItem();
        bDebugLogAutoSave = menuItem.isSelected();
    }
}
