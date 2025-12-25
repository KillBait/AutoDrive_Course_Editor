package AutoDriveEditor.GUI.Menus.DebugMenu.Logging;

import AutoDriveEditor.GUI.Menus.JCheckBoxMenuItemBase;

import javax.swing.*;
import java.awt.event.ItemEvent;

public class LogRouteManagerMenu extends JCheckBoxMenuItemBase {

    public static boolean bDebugLogRouteManagerInfo;

    public LogRouteManagerMenu() {
        makeCheckBoxMenuItem("menu_debug_log_routemanager", false, true);
    }

    @Override
    public void itemStateChanged(ItemEvent e) {
        super.itemStateChanged(e);
        AbstractButton menuItem = (AbstractButton) e.getItem();
        bDebugLogRouteManagerInfo = menuItem.isSelected();
    }
}
