package AutoDriveEditor.GUI.Menus.DebugMenu.Logging;

import AutoDriveEditor.GUI.Menus.JCheckBoxMenuItemBase;

import javax.swing.*;
import java.awt.event.ItemEvent;

public class LogFlipConnectionMenu extends JCheckBoxMenuItemBase {

    public static boolean bDebugLogFlipConnection;

    public LogFlipConnectionMenu() {
        makeCheckBoxMenuItem("menu_debug_log_flip_connection", false, true);
    }

    @Override
    public void itemStateChanged(ItemEvent e) {
        super.itemStateChanged(e);
        AbstractButton menuItem = (AbstractButton) e.getItem();
        bDebugLogFlipConnection = menuItem.isSelected();
    }
}
