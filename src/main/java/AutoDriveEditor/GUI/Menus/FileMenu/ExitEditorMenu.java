package AutoDriveEditor.GUI.Menus.FileMenu;

import AutoDriveEditor.GUI.Menus.JMenuItemBase;

import java.awt.event.ActionEvent;
import java.awt.event.InputEvent;
import java.awt.event.KeyEvent;
import java.awt.event.WindowEvent;

import static AutoDriveEditor.AutoDriveEditor.editor;

public class ExitEditorMenu extends JMenuItemBase {

    public ExitEditorMenu() {
        makeMenuItem("menu_file_exit",  "menu_file_exit_accstring", KeyEvent.VK_Q, InputEvent.ALT_DOWN_MASK, true );
    }

    @Override
    public void actionPerformed(ActionEvent e) {
        super.actionPerformed(e);
        editor.dispatchEvent(new WindowEvent(editor, WindowEvent.WINDOW_CLOSING));
    }
}
