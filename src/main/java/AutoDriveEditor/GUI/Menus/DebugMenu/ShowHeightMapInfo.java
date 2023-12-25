package AutoDriveEditor.GUI.Menus.DebugMenu;

import AutoDriveEditor.GUI.Menus.JCheckBoxMenuItemBase;

import javax.swing.*;
import java.awt.event.ItemEvent;

import static AutoDriveEditor.AutoDriveEditor.getMapPanel;
import static AutoDriveEditor.GUI.TextPanel.showInTextArea;

public class ShowHeightMapInfo extends JCheckBoxMenuItemBase {

    public static boolean bDebugShowHeightMapInfo;
    public ShowHeightMapInfo() {
        makeCheckBoxMenuItem("menu_debug_heightmap", "menu_debug_heightmap_accstring", false, true);
    }

    @Override
    public void itemStateChanged(ItemEvent e) {
        super.itemStateChanged(e);
        AbstractButton menuItem = (AbstractButton) e.getItem();
        bDebugShowHeightMapInfo = menuItem.isSelected();
        if (!menuItem.isSelected()) showInTextArea("", true, false);
        getMapPanel().repaint();
    }
}
