package AutoDriveEditor.GUI.Menus.DebugMenu.Logging;

import AutoDriveEditor.GUI.Menus.JCheckBoxMenuItemBase;

import javax.swing.*;
import java.awt.event.ItemEvent;

public class LogMergeFunctionMenu  extends JCheckBoxMenuItemBase {

    public static boolean bDebugLogMerge;

    public LogMergeFunctionMenu() {
        makeCheckBoxMenuItem("menu_debug_log_merge", "menu_debug_log_merge_accstring", false, true);
    }

    @Override
    public void itemStateChanged(ItemEvent e) {
        super.itemStateChanged(e);
        AbstractButton menuItem = (AbstractButton) e.getItem();
        bDebugLogMerge = menuItem.isSelected();
    }
}
