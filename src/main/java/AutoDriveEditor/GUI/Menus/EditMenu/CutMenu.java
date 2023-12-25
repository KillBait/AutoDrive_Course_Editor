package AutoDriveEditor.GUI.Menus.EditMenu;

import AutoDriveEditor.GUI.Menus.JMenuItemBase;

import javax.swing.*;
import java.awt.event.ActionEvent;
import java.awt.event.InputEvent;
import java.awt.event.KeyEvent;

import static AutoDriveEditor.GUI.Buttons.CopyPasteBaseButton.cutSelected;

public class CutMenu extends JMenuItemBase {

    public static JMenuItem menu_Cut;

    public CutMenu() {
        menu_Cut = makeMenuItem("menu_edit_cut",  "menu_edit_cut_accstring", KeyEvent.VK_X, InputEvent.CTRL_DOWN_MASK, false);
    }

    @Override
    public void actionPerformed(ActionEvent e) {
        super.actionPerformed(e);
        cutSelected();
    }
}
