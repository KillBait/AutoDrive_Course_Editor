package AutoDriveEditor.GUI.Menus.DebugMenu.Logging;

import AutoDriveEditor.GUI.Menus.JCheckBoxMenuItemBase;

import javax.swing.*;
import java.awt.event.ItemEvent;

public class LogMultiSelectManagerInfoMenu extends JCheckBoxMenuItemBase {

    public static boolean bDebugLogMultiSelectManagerInfo;

    public LogMultiSelectManagerInfoMenu() {
        makeCheckBoxMenuItem("menu_debug_log_multiselect", false, true);
    }

    @Override
    public void itemStateChanged(ItemEvent e) {
        super.itemStateChanged(e);
        AbstractButton menuItem = (AbstractButton) e.getItem();
        bDebugLogMultiSelectManagerInfo = menuItem.isSelected();
    }
}
