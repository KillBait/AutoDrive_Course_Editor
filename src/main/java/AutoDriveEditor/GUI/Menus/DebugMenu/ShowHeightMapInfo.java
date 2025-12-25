package AutoDriveEditor.GUI.Menus.DebugMenu;

import AutoDriveEditor.GUI.Menus.JCheckBoxMenuItemBase;
import AutoDriveEditor.Managers.DebugDisplayManager;

import javax.swing.*;
import java.awt.event.ItemEvent;

import static AutoDriveEditor.AutoDriveEditor.debugDisplayManager;
import static AutoDriveEditor.AutoDriveEditor.getMapPanel;
import static AutoDriveEditor.GUI.TextPanel.showInTextArea;

public class ShowHeightMapInfo extends JCheckBoxMenuItemBase {

    public static boolean bDebugShowHeightMapInfo;
    public static DebugDisplayManager.DebugGroup heightMapProfileGroup;

    public ShowHeightMapInfo() {
        makeCheckBoxMenuItem("menu_debug_heightmap", false, true);
        heightMapProfileGroup = debugDisplayManager.addDebugGroup("Height Map");
    }

    @Override
    public void itemStateChanged(ItemEvent e) {
        super.itemStateChanged(e);
        AbstractButton menuItem = (AbstractButton) e.getItem();
        heightMapProfileGroup.setVisible(menuItem.isSelected());
        bDebugShowHeightMapInfo = menuItem.isSelected();
        if (!menuItem.isSelected()) showInTextArea("", true, false);
        getMapPanel().repaint();
    }
}
