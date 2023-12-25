package AutoDriveEditor.GUI.Menus.EditMenu;

import AutoDriveEditor.GUI.Menus.JMenuItemBase;

import javax.swing.*;
import java.awt.event.ActionEvent;
import java.awt.event.InputEvent;
import java.awt.event.KeyEvent;

import static AutoDriveEditor.GUI.Buttons.CopyPasteBaseButton.copySelected;

public class CopyMenu extends JMenuItemBase {

    public static JMenuItem menu_Copy;

    public CopyMenu() {
        menu_Copy = makeMenuItem("menu_edit_copy",  "menu_edit_copy_accstring", KeyEvent.VK_C, InputEvent.CTRL_DOWN_MASK, false);
    }

    @Override
    public void actionPerformed(ActionEvent e) {
        super.actionPerformed(e);
        copySelected();
    }
}
