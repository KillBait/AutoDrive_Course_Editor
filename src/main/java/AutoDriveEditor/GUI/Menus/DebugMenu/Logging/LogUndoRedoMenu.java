package AutoDriveEditor.GUI.Menus.DebugMenu.Logging;

import AutoDriveEditor.GUI.Menus.JCheckBoxMenuItemBase;

import javax.swing.*;
import java.awt.event.ItemEvent;

public class LogUndoRedoMenu extends JCheckBoxMenuItemBase {

    public static boolean bDebugLogUndoRedoInfo;

    public LogUndoRedoMenu() {
        makeCheckBoxMenuItem("menu_debug_log_undo_redo", false, true);
    }

    @Override
    public void itemStateChanged(ItemEvent e) {
        super.itemStateChanged(e);
        AbstractButton menuItem = (AbstractButton) e.getItem();
        bDebugLogUndoRedoInfo = menuItem.isSelected();
    }
}
