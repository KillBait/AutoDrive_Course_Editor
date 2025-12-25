package AutoDriveEditor.GUI.Menus.EditMenu;

import AutoDriveEditor.Classes.Util_Classes.ExceptionUtils;
import AutoDriveEditor.GUI.Menus.JMenuItemBase;
import AutoDriveEditor.Managers.CopyPasteManager;

import javax.swing.*;
import java.awt.event.ActionEvent;
import java.awt.event.InputEvent;
import java.awt.event.KeyEvent;

public class PasteOriginalLocationMenu extends JMenuItemBase {

    public static JMenuItem menu_PasteOriginalLocation;

    public PasteOriginalLocationMenu() {
        menu_PasteOriginalLocation = makeMenuItem("menu_edit_paste_original_location", KeyEvent.VK_V, InputEvent.SHIFT_DOWN_MASK, false);
    }

    @Override
    public void actionPerformed(ActionEvent e) {
        super.actionPerformed(e);
        try {
            CopyPasteManager.pasteSelection(true);
        } catch (ExceptionUtils.MismatchedIdException ex) {
            throw new RuntimeException(ex);
        }
    }
}
