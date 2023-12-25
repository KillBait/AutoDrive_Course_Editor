package AutoDriveEditor.GUI.Menus.ScanMenu;

import AutoDriveEditor.GUI.Menus.JMenuItemBase;
import AutoDriveEditor.RoadNetwork.MapNode;
import AutoDriveEditor.RoadNetwork.RoadMap;

import javax.swing.*;
import java.awt.event.ActionEvent;

import static AutoDriveEditor.AutoDriveEditor.editor;
import static AutoDriveEditor.GUI.MapPanel.getYValueFromHeightMap;
import static AutoDriveEditor.GUI.MapPanel.roadMap;
import static AutoDriveEditor.Locale.LocaleManager.getLocaleString;
import static AutoDriveEditor.Utils.LoggerUtils.LOG;

public class FixNodesHeightMenu extends JMenuItemBase {

    public static JMenuItem menu_FixNodesHeight;

    public FixNodesHeightMenu() {
        menu_FixNodesHeight = makeMenuItem("menu_heightmap_fix_nodes", "menu_heightmap_fix_nodes_accstring", false);
    }

    @Override
    public void actionPerformed(ActionEvent e) {
        super.actionPerformed(e);
        if (roadMap != null) {
            int result = JOptionPane.showConfirmDialog(editor, getLocaleString("dialog_fix_node_height"), "AutoDrive Editor", JOptionPane.OK_CANCEL_OPTION, JOptionPane.QUESTION_MESSAGE);
            if (result == JOptionPane.OK_OPTION) {
                for (MapNode node : RoadMap.networkNodesList) {
                    double heightMapY = getYValueFromHeightMap(node.x, node.z);
                    if (node.y == -1) {
                        node.y = heightMapY;
                    }
                }
            } else {
                LOG.info("Cancelled node fix");
            }
        }
    }
}
