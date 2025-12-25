package AutoDriveEditor.GUI.Menus.DebugMenu.Logging;

import AutoDriveEditor.GUI.Menus.JCheckBoxMenuItemBase;

import javax.swing.*;
import java.awt.event.ItemEvent;

public class LogThemeManagerInfoMenu extends JCheckBoxMenuItemBase {

    public static boolean bDebugLogThemeManagerInfo;

    public LogThemeManagerInfoMenu() {
        makeCheckBoxMenuItem("menu_debug_log_theme_manager", false, true);
    }

    @Override
    public void itemStateChanged(ItemEvent e) {
        super.itemStateChanged(e);
        AbstractButton menuItem = (AbstractButton) e.getItem();
        bDebugLogThemeManagerInfo = menuItem.isSelected();
    }
}
