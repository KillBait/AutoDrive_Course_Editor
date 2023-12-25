package AutoDriveEditor.GUI.Menus.EditMenu;

import AutoDriveEditor.GUI.Menus.JMenuItemBase;

import javax.swing.*;
import java.awt.event.ActionEvent;
import java.awt.event.InputEvent;
import java.awt.event.KeyEvent;

import static AutoDriveEditor.GUI.Buttons.CopyPasteBaseButton.pasteSelectedInOriginalLocation;

public class PasteOriginalLocationMenu extends JMenuItemBase {

    public static JMenuItem menu_PasteOriginalLocation;

    public PasteOriginalLocationMenu() {
        menu_PasteOriginalLocation = makeMenuItem("menu_edit_paste_original_location",  "menu_edit_paste_original_location_accstring", KeyEvent.VK_V, InputEvent.SHIFT_DOWN_MASK, false);
    }

    @Override
    public void actionPerformed(ActionEvent e) {
        super.actionPerformed(e);
        pasteSelectedInOriginalLocation();
    }
}
