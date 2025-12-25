package AutoDriveEditor.GUI.Menus.DebugMenu.Logging;

import AutoDriveEditor.GUI.Menus.JCheckBoxMenuItemBase;

import javax.swing.*;
import java.awt.event.ItemEvent;

public class LogConfigGUIInfoMenu extends JCheckBoxMenuItemBase {

    public static boolean bDebugLogConfigGUIInfo;

    public LogConfigGUIInfoMenu() {
        makeCheckBoxMenuItem("menu_debug_log_config_gui", false, true);
    }

    @Override
    public void itemStateChanged(ItemEvent e) {
        super.itemStateChanged(e);
        AbstractButton menuItem = (AbstractButton) e.getItem();
        bDebugLogConfigGUIInfo = menuItem.isSelected();
    }
}
