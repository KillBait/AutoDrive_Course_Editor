package AutoDriveEditor.GUI.Menus.HelpMenu;

import AutoDriveEditor.GUI.Menus.JMenuItemBase;

import java.awt.event.ActionEvent;

import static AutoDriveEditor.Managers.VersionManager.showVersionHistory;

public class ShowHistoryMenu extends JMenuItemBase {
    public ShowHistoryMenu() {
        makeMenuItem("menu_help_history", "menu_help_history_accstring");
    }

    @Override
    public void actionPerformed(ActionEvent e) {
        super.actionPerformed(e);
        showVersionHistory();
    }
}
