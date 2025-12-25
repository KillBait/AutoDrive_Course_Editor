package AutoDriveEditor.GUI.Menus.DebugMenu;

import AutoDriveEditor.GUI.Menus.JCheckBoxMenuItemBase;
import AutoDriveEditor.Managers.DebugDisplayManager;

import javax.swing.*;
import java.awt.event.ItemEvent;

import static AutoDriveEditor.AutoDriveEditor.debugDisplayManager;
import static AutoDriveEditor.AutoDriveEditor.getMapPanel;

public class ShowZoomLevelInfo extends JCheckBoxMenuItemBase {

    public static boolean bDebugShowZoomLevelInfo;
    public static DebugDisplayManager.DebugGroup zoomProfileGroup;

    public ShowZoomLevelInfo() {
        makeCheckBoxMenuItem("menu_debug_zoomlevel", false, true);
        zoomProfileGroup = debugDisplayManager.addDebugGroup("Zoom Level");
    }

    @Override
    public void itemStateChanged(ItemEvent e) {
        super.itemStateChanged(e);
        AbstractButton menuItem = (AbstractButton) e.getItem();
        zoomProfileGroup.setVisible(menuItem.isSelected());
        bDebugShowZoomLevelInfo = menuItem.isSelected();
        getMapPanel().repaint();
    }
}
