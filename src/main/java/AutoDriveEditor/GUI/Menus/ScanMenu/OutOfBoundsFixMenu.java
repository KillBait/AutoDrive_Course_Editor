package AutoDriveEditor.GUI.Menus.ScanMenu;

import AutoDriveEditor.GUI.Menus.JMenuItemBase;
import AutoDriveEditor.Managers.ChangeManager;
import AutoDriveEditor.RoadNetwork.MapNode;
import AutoDriveEditor.RoadNetwork.RoadMap;

import javax.swing.*;
import java.awt.event.ActionEvent;
import java.awt.geom.Point2D;
import java.util.LinkedList;

import static AutoDriveEditor.AutoDriveEditor.*;
import static AutoDriveEditor.Classes.MapImage.mapPanelImage;
import static AutoDriveEditor.GUI.MapPanel.*;
import static AutoDriveEditor.Locale.LocaleManager.getLocaleString;
import static AutoDriveEditor.Utils.LoggerUtils.LOG;

//
// Feature Commit by @rheational ( https://github.com/rhaetional )
// Fixes issue #57 ( https://github.com/KillBait/AutoDrive_Course_Editor/issues/57 )
//

public class OutOfBoundsFixMenu extends JMenuItemBase {

    public static JMenuItem menu_OutOfBoundsFix;

    public OutOfBoundsFixMenu() {
        menu_OutOfBoundsFix = makeMenuItem("menu_scan_fix_oob_nodes", "menu_scan_fix_oob_nodes_accstring", false);
    }

    @Override
    public void actionPerformed(ActionEvent e) {
        super.actionPerformed(e);
        if (roadMap != null) {

            //determine bounds
            int centerPointOffsetX = (mapPanelImage.getWidth() / 2) * mapScale;
            int centerPointOffsetY = (mapPanelImage.getHeight() / 2) * mapScale;

            String bounds = String.format("\n    X: %d to %d\n    Z: %d to %d", -centerPointOffsetX, centerPointOffsetX, -centerPointOffsetY, centerPointOffsetY);
            int result = JOptionPane.showConfirmDialog(editor, getLocaleString("dialog_fix_out-of-bound_nodes") + bounds, "AutoDrive Editor", JOptionPane.OK_CANCEL_OPTION, JOptionPane.QUESTION_MESSAGE);
            if (result == JOptionPane.OK_OPTION) {

                CoordinateChanger coordChanger = new CoordinateChanger();
                MapNode firstMapNode = null;
                for (MapNode node : RoadMap.networkNodesList) {
                    if ((node.x > centerPointOffsetX) || (node.x < -centerPointOffsetX) || (node.z > centerPointOffsetY) || (node.z < -centerPointOffsetY)) {
                        if (EXPERIMENTAL)
                            LOG.info("## fixOutOfBoundsNodes() ## found out-of-bounds node: ID={}, X={}, Y={}, Z={}", node.id, node.x, node.z, node.z);
                        coordChanger.addCoordinateChange(node, 0, node.y, 0);
                        node.x = 0.0;
                        node.z = 0.0;
                        //store first node found
                        if (firstMapNode == null)
                            firstMapNode = node;
                    }
                }
                // Centre screen on first node found and add changes to change manager
                if (firstMapNode != null) {
                    Point2D target = worldPosToScreenPos(firstMapNode.x, firstMapNode.z);
                    double x = (mapPanel.getWidth() >> 1) - target.getX();
                    double y = (mapPanel.getHeight() >> 1) - target.getY();
                    moveMapBy((int)x,(int)y);
                    changeManager.addChangeable(coordChanger);
                }
            } else {
                LOG.info("Cancelled out-of-bound node fix.");
            }
        }
    }

    /**
     * Coordinate changer is a generic class to incrementally store arbitrary changes to the coordinates of
     * multiple nodes.
     */
    private static class CoordinateChanger  implements ChangeManager.Changeable {
        private final Boolean isStale;
        private final LinkedList<Coordinates> nodeList;

        public CoordinateChanger(){
            super();
            this.isStale = isStale();
            this.nodeList = new LinkedList<>();
        }

        public void addCoordinateChange(MapNode node, double newX, double newY, double newZ) {
            this.nodeList.add(new Coordinates(node, newX, newY, newZ));
        }
        public void undo() {
            for (Coordinates storedNode : nodeList) {
                storedNode.mapNode.x -= storedNode.diffX;
                storedNode.mapNode.y -= storedNode.diffY;
                storedNode.mapNode.z -= storedNode.diffZ;
            }
            getMapPanel().repaint();
            setStale(this.isStale);
        }

        public void redo() {
            for (Coordinates storedNode : nodeList) {
                storedNode.mapNode.x += storedNode.diffX;
                storedNode.mapNode.y += storedNode.diffY;
                storedNode.mapNode.z += storedNode.diffZ;
            }
            getMapPanel().repaint();
            setStale(true);
        }

        private static class Coordinates {
            private final MapNode mapNode;
            private final double diffX;
            private final double diffY;
            private final double diffZ;

            public Coordinates(MapNode node, double newX, double newY, double newZ) {
                // Reference to node
                this.mapNode = node;
                // store difference in coordinates
                this.diffX = newX - node.x;
                this.diffY = newY - node.y;
                this.diffZ = newZ - node.z;
            }
        }
    }
}
