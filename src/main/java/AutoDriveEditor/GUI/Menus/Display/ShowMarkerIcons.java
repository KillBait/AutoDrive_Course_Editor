package AutoDriveEditor.GUI.Menus.Display;

import AutoDriveEditor.GUI.Menus.JCheckBoxMenuItemBase;

import javax.swing.*;
import java.awt.event.ItemEvent;

import static AutoDriveEditor.AutoDriveEditor.getMapPanel;
import static AutoDriveEditor.XMLConfig.EditorXML.bShowMarkerIcons;
import static AutoDriveEditor.XMLConfig.EditorXML.bShowMarkerNames;

public class ShowMarkerIcons extends JCheckBoxMenuItemBase {

    public ShowMarkerIcons() {
        makeCheckBoxMenuItem("menu_display_show_marker_icons", "menu_display_show_marker_icons_accstring", bShowMarkerNames, true);
    }

    @Override
    public void itemStateChanged(ItemEvent e) {
        super.itemStateChanged(e);
        AbstractButton menuItem = (AbstractButton) e.getItem();
        bShowMarkerIcons = menuItem.isSelected();
        getMapPanel().repaint();
    }
}
