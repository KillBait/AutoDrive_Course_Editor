package AutoDriveEditor.GUI.Menus.DebugMenu.Logging;

import AutoDriveEditor.GUI.Menus.JCheckBoxMenuItemBase;

import javax.swing.*;
import java.awt.event.ItemEvent;

public class LogConnectSelectionMenu extends JCheckBoxMenuItemBase {

    public static boolean bDebugConnectSelection;

    public LogConnectSelectionMenu() {
        makeCheckBoxMenuItem("menu_debug_log_connection_selection_info", "menu_debug_log_connection_selection_info_accstring", false, true);
    }

    @Override
    public void itemStateChanged(ItemEvent e) {
        super.itemStateChanged(e);
        AbstractButton menuItem = (AbstractButton) e.getItem();
        bDebugConnectSelection = menuItem.isSelected();
    }
}
