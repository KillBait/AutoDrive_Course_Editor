package AutoDriveEditor.GUI.Menus.DebugMenu;

import AutoDriveEditor.GUI.Menus.JCheckBoxMenuItemBase;
import AutoDriveEditor.Managers.DebugDisplayManager;

import javax.swing.*;
import java.awt.event.ItemEvent;

import static AutoDriveEditor.AutoDriveEditor.debugDisplayManager;
import static AutoDriveEditor.AutoDriveEditor.getMapPanel;

public class ShowRenderProfileInfo extends JCheckBoxMenuItemBase {

    public static boolean bDebugShowRenderProfileInfo;
    public static DebugDisplayManager.DebugGroup renderProfileGroup;

    public ShowRenderProfileInfo() {
        makeCheckBoxMenuItem("menu_debug_profile", false, true);
        renderProfileGroup = debugDisplayManager.addDebugGroup("Render Thread Profile");
    }

    @Override
    public void itemStateChanged(ItemEvent e) {
        super.itemStateChanged(e);
        AbstractButton menuItem = (AbstractButton) e.getItem();
        renderProfileGroup.setVisible(menuItem.isSelected());
        bDebugShowRenderProfileInfo = menuItem.isSelected();
        getMapPanel().repaint();
    }
}
