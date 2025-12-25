package AutoDriveEditor.GUI.Menus.EditMenu;

import AutoDriveEditor.GUI.Menus.JMenuItemBase;
import AutoDriveEditor.Managers.CopyPasteManager;

import javax.swing.*;
import java.awt.event.ActionEvent;
import java.awt.event.InputEvent;
import java.awt.event.KeyEvent;


public class CutMenu extends JMenuItemBase {

    public static JMenuItem menu_Cut;

    public CutMenu() {
        menu_Cut = makeMenuItem("menu_edit_cut", KeyEvent.VK_X, InputEvent.CTRL_DOWN_MASK, false);
    }

    @Override
    public void actionPerformed(ActionEvent e) {
        super.actionPerformed(e);
        CopyPasteManager.cutSelection();
    }
}
