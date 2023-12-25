package AutoDriveEditor.GUI.Menus.DebugMenu.Logging;

import AutoDriveEditor.GUI.Menus.JCheckBoxMenuItemBase;

import javax.swing.*;
import java.awt.event.ItemEvent;

public class LogMarkerInfoMenu extends JCheckBoxMenuItemBase {

    public static boolean bDebugLogMarkerInfo;

    public LogMarkerInfoMenu() {
        makeCheckBoxMenuItem("menu_debug_log_marker_info", "menu_debug_log_marker_info_accstring", false, true);
    }

    @Override
    public void itemStateChanged(ItemEvent e) {
        super.itemStateChanged(e);
        AbstractButton menuItem = (AbstractButton) e.getItem();
        bDebugLogMarkerInfo = menuItem.isSelected();
    }
}
