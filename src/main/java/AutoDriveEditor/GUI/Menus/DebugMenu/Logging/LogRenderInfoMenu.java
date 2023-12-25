package AutoDriveEditor.GUI.Menus.DebugMenu.Logging;

import AutoDriveEditor.GUI.Menus.JCheckBoxMenuItemBase;

import javax.swing.*;
import java.awt.event.ItemEvent;

public class LogRenderInfoMenu extends JCheckBoxMenuItemBase {

    public static boolean bDebugLogRenderInfo;

    public LogRenderInfoMenu() {
        makeCheckBoxMenuItem("menu_debug_log_render_info", "menu_debug_log_render_info_accstring", false, true);
    }

    @Override
    public void itemStateChanged(ItemEvent e) {
        super.itemStateChanged(e);
        AbstractButton menuItem = (AbstractButton) e.getItem();
        bDebugLogRenderInfo = menuItem.isSelected();
    }
}
