package AutoDriveEditor.GUI.Menus.DebugMenu.Logging;

import AutoDriveEditor.GUI.Menus.JCheckBoxMenuItemBase;

import javax.swing.*;
import java.awt.event.ItemEvent;

public class LogGUIInfoMenu extends JCheckBoxMenuItemBase {

    public static boolean bDebugLogGUIInfo;

    public LogGUIInfoMenu() {
        makeCheckBoxMenuItem("menu_debug_log_gui_info", false, true);
    }

    @Override
    public void itemStateChanged(ItemEvent e) {
        super.itemStateChanged(e);
        AbstractButton menuItem = (AbstractButton) e.getItem();
        bDebugLogGUIInfo = menuItem.isSelected();
    }
}
