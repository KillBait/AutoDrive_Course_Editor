package AutoDriveEditor.GUI.Menus.Display;

import AutoDriveEditor.GUI.Menus.JCheckBoxMenuItemBase;

import javax.swing.*;
import java.awt.event.ItemEvent;

import static AutoDriveEditor.AutoDriveEditor.getMapPanel;
import static AutoDriveEditor.XMLConfig.EditorXML.bShowMarkerNames;

public class ShowMarkerNames extends JCheckBoxMenuItemBase {

    public ShowMarkerNames() {
        makeCheckBoxMenuItem("menu_display_show_marker_names", "menu_display_show_marker_names_accstring", bShowMarkerNames, true);
    }

    @Override
    public void itemStateChanged(ItemEvent e) {
        super.itemStateChanged(e);
        AbstractButton menuItem = (AbstractButton) e.getItem();
        bShowMarkerNames = menuItem.isSelected();
        getMapPanel().repaint();
    }
}
