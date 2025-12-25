package AutoDriveEditor.GUI.Menus.DebugMenu;

import AutoDriveEditor.GUI.Menus.JCheckBoxMenuItemBase;

import javax.swing.*;
import java.awt.event.InputEvent;
import java.awt.event.ItemEvent;
import java.awt.event.KeyEvent;

import static AutoDriveEditor.AutoDriveEditor.getMapPanel;

public class ShowNodeLocationInfo extends JCheckBoxMenuItemBase {

    public static boolean bDebugShowNodeLocationInfo;

    public ShowNodeLocationInfo() {
        makeCheckBoxMenuItem("menu_debug_shownodelocationinfo", KeyEvent.VK_7, InputEvent.ALT_DOWN_MASK, false, true);
    }

    @Override
    public void itemStateChanged(ItemEvent e) {
        super.itemStateChanged(e);
        AbstractButton menuItem = (AbstractButton) e.getItem();
        bDebugShowNodeLocationInfo = menuItem.isSelected();
        getMapPanel().repaint();
    }
}
