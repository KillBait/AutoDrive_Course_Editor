package AutoDriveEditor.GUI.Menus.DebugMenu.Logging;

import AutoDriveEditor.GUI.Menus.JCheckBoxMenuItemBase;

import javax.swing.*;
import java.awt.event.ItemEvent;

import static AutoDriveEditor.AutoDriveEditor.getMapPanel;

public class ShowZoomLevelInfo extends JCheckBoxMenuItemBase {

    public static boolean bDebugShowZoomLevelInfo;
    public ShowZoomLevelInfo() {
        makeCheckBoxMenuItem("menu_debug_zoomlevel", "menu_debug_zoomlevel_accstring", false, true);
    }

    @Override
    public void itemStateChanged(ItemEvent e) {
        super.itemStateChanged(e);
        AbstractButton menuItem = (AbstractButton) e.getItem();
        bDebugShowZoomLevelInfo = menuItem.isSelected();
        getMapPanel().repaint();
    }
}
