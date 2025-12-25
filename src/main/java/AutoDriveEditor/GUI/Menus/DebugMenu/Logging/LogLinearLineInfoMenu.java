package AutoDriveEditor.GUI.Menus.DebugMenu.Logging;

import AutoDriveEditor.GUI.Menus.JCheckBoxMenuItemBase;

import javax.swing.*;
import java.awt.event.ItemEvent;

public class LogLinearLineInfoMenu extends JCheckBoxMenuItemBase {

    public static boolean bDebugLogLinearlineInfo;

    public LogLinearLineInfoMenu() {
        makeCheckBoxMenuItem("menu_debug_log_linearline", false, true);
    }

    @Override
    public void itemStateChanged(ItemEvent e) {
        super.itemStateChanged(e);
        AbstractButton menuItem = (AbstractButton) e.getItem();
        bDebugLogLinearlineInfo = menuItem.isSelected();
    }
}
