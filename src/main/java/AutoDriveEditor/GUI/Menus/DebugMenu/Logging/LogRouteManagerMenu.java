package AutoDriveEditor.GUI.Menus.DebugMenu.Logging;

import AutoDriveEditor.GUI.Menus.JCheckBoxMenuItemBase;

import javax.swing.*;
import java.awt.event.ItemEvent;

public class LogRouteManagerMenu extends JCheckBoxMenuItemBase {

    public static boolean bDebugLogRouteManager;

    public LogRouteManagerMenu() {
        makeCheckBoxMenuItem("menu_debug_log_routemanager", "menu_debug_log_routemanager_accstring", false, true);
    }

    @Override
    public void itemStateChanged(ItemEvent e) {
        super.itemStateChanged(e);
        AbstractButton menuItem = (AbstractButton) e.getItem();
        bDebugLogRouteManager = menuItem.isSelected();
    }
}
