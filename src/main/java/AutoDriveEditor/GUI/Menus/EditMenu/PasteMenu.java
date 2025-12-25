package AutoDriveEditor.GUI.Menus.EditMenu;

import AutoDriveEditor.Classes.Util_Classes.ExceptionUtils;
import AutoDriveEditor.GUI.Menus.JMenuItemBase;
import AutoDriveEditor.Managers.CopyPasteManager;

import javax.swing.*;
import java.awt.event.ActionEvent;
import java.awt.event.InputEvent;
import java.awt.event.KeyEvent;

public class PasteMenu extends JMenuItemBase {

    public static JMenuItem menu_Paste;

    public PasteMenu() {
        menu_Paste = makeMenuItem("menu_edit_paste", KeyEvent.VK_V, InputEvent.CTRL_DOWN_MASK, false);
    }

    @Override
    public void actionPerformed(ActionEvent e) {
        super.actionPerformed(e);
        try {
            CopyPasteManager.pasteSelection(false);
        } catch (ExceptionUtils.MismatchedIdException ex) {
            throw new RuntimeException(ex);
        }
    }
}
