package AutoDriveEditor.GUI.Menus.EditMenu;

import AutoDriveEditor.GUI.Menus.JMenuItemBase;

import javax.swing.*;
import java.awt.event.ActionEvent;
import java.awt.event.InputEvent;
import java.awt.event.KeyEvent;

import static AutoDriveEditor.AutoDriveEditor.changeManager;

public class UndoMenu extends JMenuItemBase {

    public static JMenuItem menu_Undo;

    public UndoMenu() {
        menu_Undo = makeMenuItem("menu_edit_undo",  "menu_edit_undo_accstring", KeyEvent.VK_Z, InputEvent.CTRL_DOWN_MASK, false);
    }

    @Override
    public void actionPerformed(ActionEvent e) {
        super.actionPerformed(e);
        changeManager.undo();
    }
}
