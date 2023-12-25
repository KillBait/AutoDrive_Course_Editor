package AutoDriveEditor.GUI.Menus.DebugMenu.Logging;

import AutoDriveEditor.GUI.Menus.JCheckBoxMenuItemBase;

import javax.swing.*;
import java.awt.event.ItemEvent;

public class LogMultiSelectInfoMenu extends JCheckBoxMenuItemBase {

    public static boolean bDebugLogMultiSelectInfo;

    public LogMultiSelectInfoMenu() {
        makeCheckBoxMenuItem("menu_debug_log_multiselect", "menu_debug_log_multiselect_accstring", false, true);
    }

    @Override
    public void itemStateChanged(ItemEvent e) {
        super.itemStateChanged(e);
        AbstractButton menuItem = (AbstractButton) e.getItem();
        bDebugLogMultiSelectInfo = menuItem.isSelected();
    }
}
