package AutoDriveEditor.GUI.Menus.EditMenu;

import AutoDriveEditor.GUI.Menus.JMenuItemBase;

import javax.swing.*;
import java.awt.event.ActionEvent;
import java.awt.event.InputEvent;
import java.awt.event.KeyEvent;

import static AutoDriveEditor.AutoDriveEditor.changeManager;

public class RedoMenu extends JMenuItemBase {

    public static JMenuItem menu_Redo;

    public RedoMenu() {
        menu_Redo = makeMenuItem("menu_edit_redo", KeyEvent.VK_Z, InputEvent.SHIFT_DOWN_MASK, false);
    }

    @Override
    public void actionPerformed(ActionEvent e) {
        super.actionPerformed(e);
        changeManager.redo();
    }
}
