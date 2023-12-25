package AutoDriveEditor.GUI.Menus.EditMenu;

import AutoDriveEditor.GUI.Menus.JMenuItemBase;

import javax.swing.*;
import java.awt.event.ActionEvent;
import java.awt.event.InputEvent;
import java.awt.event.KeyEvent;

import static AutoDriveEditor.GUI.Buttons.CopyPasteBaseButton.pasteSelected;

public class PasteMenu extends JMenuItemBase {

    public static JMenuItem menu_Paste;

    public PasteMenu() {
        menu_Paste = makeMenuItem("menu_edit_paste",  "menu_edit_paste_accstring", KeyEvent.VK_V, InputEvent.CTRL_DOWN_MASK, false);
    }

    @Override
    public void actionPerformed(ActionEvent e) {
        super.actionPerformed(e);
        pasteSelected();
    }
}
