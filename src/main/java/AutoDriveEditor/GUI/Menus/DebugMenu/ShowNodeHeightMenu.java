package AutoDriveEditor.GUI.Menus.DebugMenu;

import AutoDriveEditor.GUI.Menus.JCheckBoxMenuItemBase;

import javax.swing.*;
import java.awt.event.ItemEvent;

import static AutoDriveEditor.AutoDriveEditor.getMapPanel;

public class ShowNodeHeightMenu extends JCheckBoxMenuItemBase {

    public static boolean bDebugShowHeight;
    public ShowNodeHeightMenu() {
        makeCheckBoxMenuItem("menu_debug_showheight", "menu_debug_showheight_accstring", false, true);
    }

    @Override
    public void itemStateChanged(ItemEvent e) {
        super.itemStateChanged(e);
        AbstractButton menuItem = (AbstractButton) e.getItem();
        bDebugShowHeight = menuItem.isSelected();
        getMapPanel().repaint();
    }
}
