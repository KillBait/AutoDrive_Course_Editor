package AutoDriveEditor.GUI.Menus.ScanMenu;

import AutoDriveEditor.GUI.Menus.JMenuItemBase;

import javax.swing.*;
import java.awt.event.ActionEvent;

import static AutoDriveEditor.Managers.ScanManager.mergeOverlappingNodes;

public class MergeNodesMenu extends JMenuItemBase {

    public static JMenuItem menu_MergeNodes;

    public MergeNodesMenu() {
        menu_MergeNodes = makeMenuItem("menu_scan_merge", false);
    }

    @Override
    public void actionPerformed(ActionEvent e) {
        super.actionPerformed(e);
        mergeOverlappingNodes();
    }
}
