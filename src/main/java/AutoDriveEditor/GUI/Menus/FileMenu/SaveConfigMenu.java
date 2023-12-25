package AutoDriveEditor.GUI.Menus.FileMenu;

import AutoDriveEditor.GUI.Menus.JMenuItemBase;

import javax.swing.*;
import java.awt.event.ActionEvent;
import java.awt.event.InputEvent;
import java.awt.event.KeyEvent;

import static AutoDriveEditor.XMLConfig.GameXML.saveGameConfig;

public class SaveConfigMenu extends JMenuItemBase {

    public static JMenuItem menu_SaveConfig;

    public SaveConfigMenu() {
        menu_SaveConfig = makeMenuItem("menu_file_saveconfig",  "menu_file_saveconfig_accstring", KeyEvent.VK_S, InputEvent.ALT_DOWN_MASK, false);
    }

    @Override
    public void actionPerformed(ActionEvent e) {
        super.actionPerformed(e);
        saveGameConfig(null, false, false);
    }
}
