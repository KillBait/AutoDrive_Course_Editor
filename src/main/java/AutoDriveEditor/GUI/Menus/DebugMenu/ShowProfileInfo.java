package AutoDriveEditor.GUI.Menus.DebugMenu;

import AutoDriveEditor.GUI.Menus.JCheckBoxMenuItemBase;

import javax.swing.*;
import java.awt.event.ItemEvent;

import static AutoDriveEditor.AutoDriveEditor.getMapPanel;

public class ShowProfileInfo extends JCheckBoxMenuItemBase {

    public static boolean bDebugShowProfileInfo;
    public ShowProfileInfo() {
        makeCheckBoxMenuItem("menu_debug_profile", "menu_debug_profile_accstring", false, true);
    }

    @Override
    public void itemStateChanged(ItemEvent e) {
        super.itemStateChanged(e);
        AbstractButton menuItem = (AbstractButton) e.getItem();
        bDebugShowProfileInfo = menuItem.isSelected();
        getMapPanel().repaint();
    }
}
