package AutoDriveEditor.GUI.Menus.DebugMenu;

import AutoDriveEditor.GUI.Menus.JCheckBoxMenuItemBase;
import AutoDriveEditor.Managers.DebugDisplayManager;

import javax.swing.*;
import java.awt.event.ItemEvent;

import static AutoDriveEditor.AutoDriveEditor.debugDisplayManager;
import static AutoDriveEditor.AutoDriveEditor.getMapPanel;

public class ShowFrameInfo extends JCheckBoxMenuItemBase {

    public static boolean bDebugShowFPSInfo;
    public static DebugDisplayManager.DebugGroup fpsProfileGroup;

    public ShowFrameInfo() {
        makeCheckBoxMenuItem("menu_debug_show_frame_info", false, true);
        fpsProfileGroup = debugDisplayManager.addDebugGroup("Frame Info");
    }

    @Override
    public void itemStateChanged(ItemEvent e) {
        super.itemStateChanged(e);
        AbstractButton menuItem = (AbstractButton) e.getItem();
        fpsProfileGroup.setVisible(menuItem.isSelected());
        bDebugShowFPSInfo = menuItem.isSelected();
        getMapPanel().repaint();
    }
}
