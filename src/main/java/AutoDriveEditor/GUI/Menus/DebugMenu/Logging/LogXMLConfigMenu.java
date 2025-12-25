package AutoDriveEditor.GUI.Menus.DebugMenu.Logging;

import AutoDriveEditor.GUI.Menus.JCheckBoxMenuItemBase;

import javax.swing.*;
import java.awt.event.ItemEvent;

public class LogXMLConfigMenu extends JCheckBoxMenuItemBase {

    public static boolean bDebugLogXMLInfo;

    public LogXMLConfigMenu() {
        makeCheckBoxMenuItem("menu_debug_log_config", false, true);
    }

    @Override
    public void itemStateChanged(ItemEvent e) {
        super.itemStateChanged(e);
        AbstractButton menuItem = (AbstractButton) e.getItem();
        bDebugLogXMLInfo = menuItem.isSelected();
    }
}
