package AutoDriveEditor.GUI.Menus.DebugMenu;

import AutoDriveEditor.GUI.Menus.JCheckBoxMenuItemBase;

import javax.swing.*;
import java.awt.event.InputEvent;
import java.awt.event.ItemEvent;
import java.awt.event.KeyEvent;

import static AutoDriveEditor.AutoDriveEditor.getMapPanel;
public class ShowAllNodeIDMenu extends JCheckBoxMenuItemBase {

    public static boolean bDebugShowAllNodeID;

    public ShowAllNodeIDMenu() {
        makeCheckBoxMenuItem("menu_debug_show_all_node_ID", KeyEvent.VK_6, InputEvent.ALT_DOWN_MASK, false,true);
    }

    @Override
    public void itemStateChanged(ItemEvent e) {
        super.itemStateChanged(e);
        AbstractButton menuItem = (AbstractButton) e.getItem();
        bDebugShowAllNodeID = menuItem.isSelected();
        getMapPanel().repaint();
    }
}
