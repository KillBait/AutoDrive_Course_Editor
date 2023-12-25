package AutoDriveEditor.GUI.Menus.DebugMenu.Logging;

import AutoDriveEditor.GUI.Menus.JCheckBoxMenuItemBase;

import javax.swing.*;
import java.awt.event.ItemEvent;

public class LogButtonInfoMenu extends JCheckBoxMenuItemBase {

    public static boolean bDebugLogButton;

    public LogButtonInfoMenu() {
        makeCheckBoxMenuItem("menu_debug_log_button_state", "menu_debug_log_button_state_accstring", false, true);
    }

    @Override
    public void itemStateChanged(ItemEvent e) {
        super.itemStateChanged(e);
        AbstractButton menuItem = (AbstractButton) e.getItem();
        bDebugLogButton = menuItem.isSelected();
    }
}
