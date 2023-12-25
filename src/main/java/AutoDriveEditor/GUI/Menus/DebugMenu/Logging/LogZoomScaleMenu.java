package AutoDriveEditor.GUI.Menus.DebugMenu.Logging;

import AutoDriveEditor.GUI.Menus.JCheckBoxMenuItemBase;

import javax.swing.*;
import java.awt.event.ItemEvent;

public class LogZoomScaleMenu extends JCheckBoxMenuItemBase {

    public static boolean bDebugLogZoomScale;

    public LogZoomScaleMenu() {
        makeCheckBoxMenuItem("menu_debug_log_zoom", "menu_debug_log_zoom_accstring", false, true);
    }

    @Override
    public void itemStateChanged(ItemEvent e) {
        super.itemStateChanged(e);
        AbstractButton menuItem = (AbstractButton) e.getItem();
        bDebugLogZoomScale = menuItem.isSelected();
    }
}
