package AutoDriveEditor.GUI.Menus.DebugMenu.Logging;

import AutoDriveEditor.GUI.Menus.JCheckBoxMenuItemBase;

import javax.swing.*;
import java.awt.event.ItemEvent;

public class LogXMLReaderMenu extends JCheckBoxMenuItemBase {

    public static boolean bDebugXMLReader;

    public LogXMLReaderMenu() {
        makeCheckBoxMenuItem("menu_debug_log_xml_reader", false, true);
    }

    @Override
    public void itemStateChanged(ItemEvent e) {
        super.itemStateChanged(e);
        AbstractButton menuItem = (AbstractButton) e.getItem();
        bDebugXMLReader = menuItem.isSelected();
    }
}
