package AutoDriveEditor.GUI.Menus.RoutesMenu;

import AutoDriveEditor.GUI.Menus.JMenuItemBase;

import javax.swing.*;
import java.awt.event.ActionEvent;

import static AutoDriveEditor.XMLConfig.RoutesXML.saveRouteManagerXML;

public class SaveRoutesXML extends JMenuItemBase {

    public static JMenuItem menu_SaveRoutesXML;

    public SaveRoutesXML() {
        menu_SaveRoutesXML = makeMenuItem("menu_routes_save_xml",  "menu_routes_save_xml_accstring", false);
    }

    @Override
    public void actionPerformed(ActionEvent e) {
        super.actionPerformed(e);
        saveRouteManagerXML(null, false, false);
    }
}
