package AutoDriveEditor.GUI.Menus.DebugMenu;

import AutoDriveEditor.GUI.Menus.JCheckBoxMenuItemBase;
import AutoDriveEditor.Managers.DebugDisplayManager;

import javax.swing.*;
import java.awt.event.ItemEvent;

import static AutoDriveEditor.AutoDriveEditor.debugDisplayManager;
import static AutoDriveEditor.AutoDriveEditor.getMapPanel;

public class ShowWidgetManagerInfo extends JCheckBoxMenuItemBase {

    public static boolean bDebugShowWidgetManagerInfo;
    public static DebugDisplayManager.DebugGroup widgetManagerGroup;

    public ShowWidgetManagerInfo() {
        makeCheckBoxMenuItem("menu_debug_widget", false, true);
        widgetManagerGroup = debugDisplayManager.addDebugGroup("WidgetManager");
    }

    @Override
    public void itemStateChanged(ItemEvent e) {
        super.itemStateChanged(e);
        AbstractButton menuItem = (AbstractButton) e.getItem();
        widgetManagerGroup.setVisible(menuItem.isSelected());
        bDebugShowWidgetManagerInfo = menuItem.isSelected();
        getMapPanel().repaint();
    }
}
