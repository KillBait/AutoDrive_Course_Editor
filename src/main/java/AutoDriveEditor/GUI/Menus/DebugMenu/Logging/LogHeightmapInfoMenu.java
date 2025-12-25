package AutoDriveEditor.GUI.Menus.DebugMenu.Logging;

import AutoDriveEditor.GUI.Menus.JCheckBoxMenuItemBase;

import javax.swing.*;
import java.awt.event.ItemEvent;

public class LogHeightmapInfoMenu extends JCheckBoxMenuItemBase {

    public static boolean bDebugLogHeightMapInfo;

    public LogHeightmapInfoMenu() {
        makeCheckBoxMenuItem("menu_debug_log_heightmap", false, true);
    }

    @Override
    public void itemStateChanged(ItemEvent e) {
        super.itemStateChanged(e);
        AbstractButton menuItem = (AbstractButton) e.getItem();
        bDebugLogHeightMapInfo = menuItem.isSelected();
    }
}
