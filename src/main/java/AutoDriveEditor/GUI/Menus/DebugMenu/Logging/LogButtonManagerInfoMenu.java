package AutoDriveEditor.GUI.Menus.DebugMenu.Logging;

import AutoDriveEditor.GUI.Menus.JCheckBoxMenuItemBase;

import javax.swing.*;
import java.awt.event.ItemEvent;

public class LogButtonManagerInfoMenu extends JCheckBoxMenuItemBase {

    public static boolean bDebugLogButtonManagerInfo;

    public LogButtonManagerInfoMenu() {
        makeCheckBoxMenuItem("menu_debug_log_button_state", false, true);
    }

    @Override
    public void itemStateChanged(ItemEvent e) {
        super.itemStateChanged(e);
        AbstractButton menuItem = (AbstractButton) e.getItem();
        bDebugLogButtonManagerInfo = menuItem.isSelected();
    }
}
