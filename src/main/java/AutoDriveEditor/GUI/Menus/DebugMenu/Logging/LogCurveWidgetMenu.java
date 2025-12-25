package AutoDriveEditor.GUI.Menus.DebugMenu.Logging;

import AutoDriveEditor.GUI.Menus.JCheckBoxMenuItemBase;

import javax.swing.*;
import java.awt.event.ItemEvent;

public class LogCurveWidgetMenu extends JCheckBoxMenuItemBase {

    public static boolean bDebugLogCurveWidget;

    public LogCurveWidgetMenu() {
        makeCheckBoxMenuItem("menu_debug_log_curve_widget", false, true);
    }

    @Override
    public void itemStateChanged(ItemEvent e) {
        super.itemStateChanged(e);
        AbstractButton menuItem = (AbstractButton) e.getItem();
        bDebugLogCurveWidget = menuItem.isSelected();
    }
}
