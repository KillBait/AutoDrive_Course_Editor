package AutoDriveEditor.GUI.Menus.DebugMenu.Logging;

import AutoDriveEditor.GUI.Menus.JCheckBoxMenuItemBase;

import javax.swing.*;
import java.awt.event.ItemEvent;

public class LogWidgetManagerInfoMenu extends JCheckBoxMenuItemBase {

    public static boolean bDebugLogWidgetManagerInfo;

    public LogWidgetManagerInfoMenu() {
        makeCheckBoxMenuItem("menu_debug_log_widgetmanager", false, true);
    }

    @Override
    public void itemStateChanged(ItemEvent e) {
        super.itemStateChanged(e);
        AbstractButton menuItem = (AbstractButton) e.getItem();
        bDebugLogWidgetManagerInfo = menuItem.isSelected();
    }
}
