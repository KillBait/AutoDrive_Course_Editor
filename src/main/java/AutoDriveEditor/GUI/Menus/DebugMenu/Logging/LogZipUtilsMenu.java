package AutoDriveEditor.GUI.Menus.DebugMenu.Logging;

import AutoDriveEditor.GUI.Menus.JCheckBoxMenuItemBase;

import javax.swing.*;
import java.awt.event.ItemEvent;

public class LogZipUtilsMenu extends JCheckBoxMenuItemBase {

    public static boolean bDebugLogZipUtils;

    public LogZipUtilsMenu() {
        makeCheckBoxMenuItem("menu_debug_log_zip_utils", false, true);
    }

    @Override
    public void itemStateChanged(ItemEvent e) {
        super.itemStateChanged(e);
        AbstractButton menuItem = (AbstractButton) e.getItem();
        bDebugLogZipUtils = menuItem.isSelected();
    }
}
