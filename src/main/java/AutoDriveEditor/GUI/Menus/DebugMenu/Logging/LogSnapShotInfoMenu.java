package AutoDriveEditor.GUI.Menus.DebugMenu.Logging;

import AutoDriveEditor.GUI.Menus.JCheckBoxMenuItemBase;

import javax.swing.*;
import java.awt.event.ItemEvent;

public class LogSnapShotInfoMenu extends JCheckBoxMenuItemBase {

    public static boolean bDebugLogSnapShotInfo;

    public LogSnapShotInfoMenu() {
        makeCheckBoxMenuItem("menu_debug_log_snapshot", false, true);
    }

    @Override
    public void itemStateChanged(ItemEvent e) {
        super.itemStateChanged(e);
        AbstractButton menuItem = (AbstractButton) e.getItem();
        bDebugLogSnapShotInfo = menuItem.isSelected();
    }
}
