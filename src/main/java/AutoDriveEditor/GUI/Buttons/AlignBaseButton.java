package AutoDriveEditor.GUI.Buttons;

import AutoDriveEditor.Managers.ButtonManager;
import AutoDriveEditor.Managers.ChangeManager;
import AutoDriveEditor.RoadNetwork.MapNode;

import java.awt.*;
import java.awt.event.MouseEvent;
import java.util.ArrayList;

import static AutoDriveEditor.AutoDriveEditor.curveManager;
import static AutoDriveEditor.AutoDriveEditor.getMapPanel;
import static AutoDriveEditor.GUI.MapPanel.*;
import static AutoDriveEditor.Managers.MultiSelectManager.*;
import static AutoDriveEditor.Managers.ScanManager.checkNodeOverlap;
import static AutoDriveEditor.XMLConfig.AutoSave.resumeAutoSaving;
import static AutoDriveEditor.XMLConfig.AutoSave.suspendAutoSaving;

public abstract class AlignBaseButton extends BaseButton implements ButtonManager.ToolTipBuilder {

    protected abstract void adjustNodesTo(MapNode toNode);

    protected final int ALIGN_NONE = 0;
    protected final int ALIGN_HORIZONTAL = 1;
    protected final int ALIGN_VERTICAL = 2;

    protected int direction = 0; // 0 = horizontal, 1 = vertical, 2 = depth

    private MapNode alignNode;
    private boolean drawLine;

    @Override
    public Boolean useMultiSelection() { return true; }

    @Override
    public void mouseClicked(MouseEvent e) {
        super.mouseClicked(e);
        if (e.getButton() == MouseEvent.BUTTON1) {
            MapNode clickedNode = getNodeAtScreenPosition(e.getX(), e.getY());
            if (!multiSelectList.isEmpty() && isMultipleSelected &&  clickedNode != null) {
                suspendAutoSaving();
                adjustNodesTo(clickedNode);
                if (curveManager.isCurvePreviewCreated()) curveManager.updateAllCurves();
                setStale(true);
                clearMultiSelection();
                getMapPanel().repaint();
                resumeAutoSaving();
                drawLine = false;
            }
        }
    }

    @Override
    public void mouseMoved(MouseEvent e) {
        if (!multiSelectList.isEmpty() && isMultipleSelected) {
            alignNode = getNodeAtScreenPosition(e.getX(), e.getY());
            drawLine = alignNode != null;
        }
    }

    @Override
    public void drawToScreen(Graphics g) {
        if (drawLine && !multiSelectList.isEmpty() && isMultipleSelected) {
            if (alignNode != null) {
                Graphics2D gTemp = (Graphics2D) g.create();
                Point nodeScreenPos = worldPosToScreenPos(alignNode.x, alignNode.z);
                BasicStroke bsDash = new BasicStroke(3, BasicStroke.CAP_BUTT, BasicStroke.JOIN_ROUND, 1.0f, new float[]{8f, 0f, 4f}, 3f);
                gTemp.setComposite(AlphaComposite.SrcOver.derive(0.5f));
                gTemp.setStroke(bsDash);
                gTemp.setColor(Color.WHITE);
                if (direction == ALIGN_HORIZONTAL) {
                    gTemp.drawLine(0, nodeScreenPos.y, getMapPanel().getWidth(), nodeScreenPos.y);
                } else if (direction == ALIGN_VERTICAL) {
                    gTemp.drawLine(nodeScreenPos.x, 0, nodeScreenPos.x, getMapPanel().getHeight());
                }
                gTemp.dispose();
            }
        }
    }

    public static class AlignmentChanger implements ChangeManager.Changeable {
        private final Boolean isStale;
        private final ArrayList<ZStore> nodeList;

        public AlignmentChanger(ArrayList<MapNode> nodeList, double x, double y, double z){
            super();
            this.isStale = isStale();
            this.nodeList = new ArrayList<>();

            for (MapNode node : nodeList) {
                this.nodeList.add(new ZStore(node, x, y, z));
            }
        }

        public void undo() {
            suspendAutoSaving();
            for (ZStore storedNode : nodeList) {
                storedNode.mapNode.x += storedNode.diffX;
                storedNode.mapNode.y += storedNode.diffY;
                storedNode.mapNode.z += storedNode.diffZ;
                checkNodeOverlap(storedNode.mapNode);
            }
            setStale(this.isStale);
            resumeAutoSaving();
            getMapPanel().repaint();
        }

        public void redo() {
            suspendAutoSaving();
            for (ZStore storedNode : nodeList) {
                storedNode.mapNode.x -= storedNode.diffX;
                storedNode.mapNode.y -= storedNode.diffY;
                storedNode.mapNode.z -= storedNode.diffZ;
                checkNodeOverlap(storedNode.mapNode);
            }
            setStale(true);
            resumeAutoSaving();
            getMapPanel().repaint();
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
