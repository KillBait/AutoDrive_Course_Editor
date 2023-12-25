package AutoDriveEditor.GUI.Menus.Display;

import AutoDriveEditor.GUI.Menus.JCheckBoxMenuItemBase;

import javax.swing.*;
import java.awt.event.ItemEvent;

import static AutoDriveEditor.AutoDriveEditor.getMapPanel;
import static AutoDriveEditor.XMLConfig.EditorXML.bShowMarkerNames;
import static AutoDriveEditor.XMLConfig.EditorXML.bShowParkingIcons;

public class ShowParkingIcons extends JCheckBoxMenuItemBase {

    public ShowParkingIcons() {
        makeCheckBoxMenuItem("menu_display_show_parking_icons", "menu_display_show_parking_icons_accstring", bShowMarkerNames, true);
    }

    @Override
    public void itemStateChanged(ItemEvent e) {
        super.itemStateChanged(e);
        AbstractButton menuItem = (AbstractButton) e.getItem();
        bShowParkingIcons = menuItem.isSelected();
        getMapPanel().repaint();
    }
}
