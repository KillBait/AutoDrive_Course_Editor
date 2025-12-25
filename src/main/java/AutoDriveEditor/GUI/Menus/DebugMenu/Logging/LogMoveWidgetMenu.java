package AutoDriveEditor.GUI.Menus.DebugMenu.Logging;

import AutoDriveEditor.GUI.Menus.JCheckBoxMenuItemBase;

import javax.swing.*;
import java.awt.event.ItemEvent;

public class LogMoveWidgetMenu extends JCheckBoxMenuItemBase {

    public static boolean bDebugLogMoveWidget;

    public LogMoveWidgetMenu() {
        makeCheckBoxMenuItem("menu_debug_log_move_widget", false, true);
    }

    @Override
    public void itemStateChanged(ItemEvent e) {
        super.itemStateChanged(e);
        AbstractButton menuItem = (AbstractButton) e.getItem();
        bDebugLogMoveWidget = menuItem.isSelected();
    }
}
