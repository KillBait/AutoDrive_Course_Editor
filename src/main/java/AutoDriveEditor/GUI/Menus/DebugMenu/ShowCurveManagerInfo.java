package AutoDriveEditor.GUI.Menus.DebugMenu;

import AutoDriveEditor.GUI.Menus.JCheckBoxMenuItemBase;
import AutoDriveEditor.Managers.DebugDisplayManager;

import javax.swing.*;
import java.awt.event.ItemEvent;

import static AutoDriveEditor.AutoDriveEditor.debugDisplayManager;
import static AutoDriveEditor.AutoDriveEditor.getMapPanel;

public class ShowCurveManagerInfo extends JCheckBoxMenuItemBase {

    public static DebugDisplayManager.DebugGroup curveManagerGroup;

    public ShowCurveManagerInfo() {
        makeCheckBoxMenuItem("menu_debug_curves", false, true);
        curveManagerGroup = debugDisplayManager.addDebugGroup("CurveManager");
    }

    @Override
    public void itemStateChanged(ItemEvent e) {
        super.itemStateChanged(e);
        AbstractButton menuItem = (AbstractButton) e.getItem();
        curveManagerGroup.setVisible(menuItem.isSelected());
        getMapPanel().repaint();
    }
}
