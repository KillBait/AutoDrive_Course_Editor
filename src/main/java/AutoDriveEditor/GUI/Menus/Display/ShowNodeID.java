package AutoDriveEditor.GUI.Menus.Display;

import AutoDriveEditor.GUI.Menus.JCheckBoxMenuItemBase;

import javax.swing.*;
import java.awt.event.ItemEvent;

import static AutoDriveEditor.AutoDriveEditor.getMapPanel;
import static AutoDriveEditor.Utils.GUIUtils.InputEvent_NONE;
import static AutoDriveEditor.Utils.GUIUtils.KeyEvent_NONE;
import static AutoDriveEditor.XMLConfig.EditorXML.bShowSelectedNodeID;

public class ShowNodeID extends JCheckBoxMenuItemBase {

    public ShowNodeID() {
        makeCheckBoxMenuItem("menu_display_showID", "menu_display_showID_accstring", KeyEvent_NONE, InputEvent_NONE, bShowSelectedNodeID,true);
    }

    public void itemStateChanged(ItemEvent e) {
        super.itemStateChanged(e);
        AbstractButton menuItem = (AbstractButton) e.getItem();
        bShowSelectedNodeID = menuItem.isSelected();
        getMapPanel().repaint();
    }
}
