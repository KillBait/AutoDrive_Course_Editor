package AutoDriveEditor.GUI.Menus.DebugMenu.Logging;

import AutoDriveEditor.GUI.Menus.JCheckBoxMenuItemBase;

import javax.swing.*;
import java.awt.event.ItemEvent;

public class LogCurveManagerInfoMenu extends JCheckBoxMenuItemBase {

    public static boolean bDebugLogCurveManagerInfo;

    public LogCurveManagerInfoMenu() {
        makeCheckBoxMenuItem("menu_debug_log_curvemanager", false, true);
    }

    @Override
    public void itemStateChanged(ItemEvent e) {
        super.itemStateChanged(e);
        AbstractButton menuItem = (AbstractButton) e.getItem();
        bDebugLogCurveManagerInfo = menuItem.isSelected();
    }
}
