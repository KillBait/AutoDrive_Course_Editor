package AutoDriveEditor.GUI.Menus.MapImagesMenu;

import AutoDriveEditor.GUI.Menus.JMenuItemBase;

import javax.swing.*;
import java.awt.event.ActionEvent;
import java.awt.event.InputEvent;
import java.awt.event.KeyEvent;

import static AutoDriveEditor.XMLConfig.GameXML.lastUsedLocation;

public class SaveMapImageMenu extends JMenuItemBase {

    public static JMenuItem menu_SaveMapImage;
    public SaveMapImageMenu() {
        menu_SaveMapImage = makeMenuItem("menu_map_saveimage",  "menu_map_saveimage_accstring", KeyEvent.VK_B, InputEvent.ALT_DOWN_MASK, false);
    }
    @Override
    public void actionPerformed(ActionEvent e) {
        super.actionPerformed(e);
        JFileChooser fc = new JFileChooser(lastUsedLocation);
    }
}
