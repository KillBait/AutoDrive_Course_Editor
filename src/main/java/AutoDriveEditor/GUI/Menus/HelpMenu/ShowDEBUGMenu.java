package AutoDriveEditor.GUI.Menus.HelpMenu;

import AutoDriveEditor.GUI.Menus.JCheckBoxMenuItemBase;

import javax.swing.*;
import java.awt.event.ItemEvent;

import static AutoDriveEditor.AutoDriveEditor.bIsDebugEnabled;
import static AutoDriveEditor.GUI.Menus.EditorMenu.menu_DEBUG;

public class ShowDEBUGMenu extends JCheckBoxMenuItemBase {

    public ShowDEBUGMenu() {
        makeCheckBoxMenuItem("menu_help_debug", false, true);
    }

    @Override
    public void itemStateChanged(ItemEvent e) {
        super.itemStateChanged(e);
        AbstractButton menuItem = (AbstractButton) e.getItem();
        menu_DEBUG.setVisible(menuItem.isSelected());
        bIsDebugEnabled = menuItem.isSelected();
    }
}
