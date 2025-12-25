package AutoDriveEditor.GUI.Menus.DebugMenu.Logging;

import AutoDriveEditor.GUI.Menus.JCheckBoxMenuItemBase;

import javax.swing.*;
import java.awt.event.ItemEvent;

public class LogCurveInfoMenu extends JCheckBoxMenuItemBase {

    public static boolean bDebugLogCurveInfo;

    public LogCurveInfoMenu() {
        makeCheckBoxMenuItem("menu_debug_log_curve", false, true);
    }

    @Override
    public void itemStateChanged(ItemEvent e) {
        super.itemStateChanged(e);
        AbstractButton menuItem = (AbstractButton) e.getItem();
        bDebugLogCurveInfo = menuItem.isSelected();
    }
}
