package AutoDriveEditor.GUI.Buttons;

import AutoDriveEditor.Managers.ChangeManager;
import AutoDriveEditor.RoadNetwork.MapNode;

import java.awt.event.MouseEvent;
import java.util.LinkedList;

import static AutoDriveEditor.GUI.Buttons.Curves.CubicCurveButton.cubicCurve;
import static AutoDriveEditor.GUI.Buttons.Curves.CubicCurveButton.isCubicCurveCreated;
import static AutoDriveEditor.GUI.Buttons.Curves.QuadCurveButton.isQuadCurveCreated;
import static AutoDriveEditor.GUI.Buttons.Curves.QuadCurveButton.quadCurve;
import static AutoDriveEditor.Managers.MultiSelectManager.*;
import static AutoDriveEditor.MapPanel.MapPanel.*;

public abstract class AlignBaseButton extends BaseButton {

    protected abstract void adjustNodesTo(MapNode toNode);

    @Override
    public Boolean ignoreMultiSelect() { return false; }

    @Override
    public void mouseClicked(MouseEvent e) {
        super.mouseClicked(e);
        if (e.getButton() == MouseEvent.BUTTON1) {
            MapNode clickedNode = getNodeAtScreenPosition(e.getX(), e.getY());
            if (multiSelectList != null && isMultipleSelected &&  clickedNode != null) {
                canAutoSave = false;
                adjustNodesTo(clickedNode);
                if (quadCurve != null && isQuadCurveCreated) quadCurve.updateCurve();
                if (cubicCurve != null && isCubicCurveCreated) cubicCurve.updateCurve();
                canAutoSave = true;
                setStale(true);
                clearMultiSelection();
                getMapPanel().repaint();
            }
        }
    }

    public static class AlignmentChanger implements ChangeManager.Changeable {
        private final Boolean isStale;
        private final LinkedList<ZStore> nodeList;

        public AlignmentChanger(LinkedList<MapNode> multiSelectList, double x, double y, double z){
            super();
            this.isStale = isStale();
            this.nodeList = new LinkedList<>();

            for (MapNode node : multiSelectList) {
                nodeList.add(new ZStore(node, x, y, z));
            }
        }

        public void undo() {
            for (ZStore storedNode : nodeList) {
                storedNode.mapNode.x += storedNode.diffX;
                storedNode.mapNode.y += storedNode.diffY;
                storedNode.mapNode.z += storedNode.diffZ;
            }
            getMapPanel().repaint();
            setStale(this.isStale);
        }

        public void redo() {
            for (ZStore storedNode : nodeList) {
                storedNode.mapNode.x -= storedNode.diffX;
                storedNode.mapNode.y -= storedNode.diffY;
                storedNode.mapNode.z -= storedNode.diffZ;
            }
            getMapPanel().repaint();
            setStale(true);
        }

        private static class ZStore {
            private final MapNode mapNode;
            private final double diffX;
            private final double diffY;
            private final double diffZ;

            public ZStore(MapNode node, double dX, double dY, double dZ) {
                this.mapNode = node;
                if (dX == 0) {
                    this.diffX = 0;
                } else {
                    this.diffX = node.x - dX;
                }
                if (dY == 0) {
                    this.diffY = 0;
                } else {
                    this.diffY = node.y - dY;
                }
                if (dZ == 0) {
                    this.diffZ = 0;
                } else {
                    this.diffZ = node.z - dZ;
                }
            }
        }
    }
}
