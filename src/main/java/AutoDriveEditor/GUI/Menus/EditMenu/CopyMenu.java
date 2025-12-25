package AutoDriveEditor.GUI.Menus.EditMenu;

import AutoDriveEditor.Classes.KeyBinds.Shortcut;
import AutoDriveEditor.GUI.Menus.JMenuItemBase;
import AutoDriveEditor.Managers.CopyPasteManager;

import javax.swing.*;
import java.awt.event.ActionEvent;

import static AutoDriveEditor.Managers.ShortcutManager.ShortcutID.COPY_SHORTCUT;
import static AutoDriveEditor.Managers.ShortcutManager.getUserShortcutByID;
import static AutoDriveEditor.Managers.ShortcutManager.registerMenuShortcut;

public class CopyMenu extends JMenuItemBase {

    public static JMenuItem menu_Copy;

    public CopyMenu() {
        menu_Copy = makeMenuItem("menu_edit_copy", 0, 0, false);
        Shortcut moveShortcut = getUserShortcutByID(COPY_SHORTCUT);
        if (moveShortcut != null) {
            registerMenuShortcut(this, moveShortcut, moveShortcut.getKeyCode(), moveShortcut.getModifier());
        }
    }

    @Override
    public void actionPerformed(ActionEvent e) {
        super.actionPerformed(e);
        CopyPasteManager.copySelection();
    }
}
