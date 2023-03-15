package AutoDriveEditor.GUI.Buttons.Nodes;

import AutoDriveEditor.GUI.Buttons.BaseButton;
import AutoDriveEditor.Managers.ChangeManager;
import AutoDriveEditor.RoadNetwork.MapNode;
import AutoDriveEditor.RoadNetwork.RoadMap;

import javax.swing.*;
import java.awt.event.MouseEvent;
import java.util.LinkedList;
import java.util.UUID;

import static AutoDriveEditor.AutoDriveEditor.changeManager;
import static AutoDriveEditor.AutoDriveEditor.editor;
import static AutoDriveEditor.GUI.Buttons.Curves.CubicCurveButton.cubicCurve;
import static AutoDriveEditor.GUI.Buttons.Curves.CubicCurveButton.isCubicCurveCreated;
import static AutoDriveEditor.GUI.Buttons.Curves.QuadCurveButton.isQuadCurveCreated;
import static AutoDriveEditor.GUI.Buttons.Curves.QuadCurveButton.quadCurve;
import static AutoDriveEditor.GUI.MenuBuilder.bDebugLogCurveInfo;
import static AutoDriveEditor.GUI.MenuBuilder.bDebugLogUndoRedo;
import static AutoDriveEditor.Locale.LocaleManager.getLocaleString;
import static AutoDriveEditor.Managers.MultiSelectManager.*;
import static AutoDriveEditor.Managers.ScanManager.checkNodeOverlap;
import static AutoDriveEditor.MapPanel.MapPanel.*;
import static AutoDriveEditor.Utils.GUIUtils.makeImageToggleButton;
import static AutoDriveEditor.Utils.GUIUtils.showInTextArea;
import static AutoDriveEditor.Utils.LoggerUtils.LOG;

public class DeleteNodeButton extends BaseButton {

    public static LinkedList<NodeLinks> deleteNodeList = new LinkedList<>();

    public DeleteNodeButton(JPanel panel) {
        button = makeImageToggleButton("buttons/deletenodes","buttons/deletenodes_selected", null,"nodes_remove_tooltip","nodes_remove_alt", panel, false, false, null, false, this);
    }

    @Override
    public String getButtonID() { return "DeleteNodeButton"; }

    @Override
    public String getButtonAction() { return "ActionButton"; }

    @Override
    public String getButtonPanel() { return "Nodes"; }

    @Override
    public String getInfoText() { return getLocaleString("nodes_remove_tooltip"); }

    @Override
    public Boolean ignoreMultiSelect() { return false; }

    @Override
    public void mousePressed(MouseEvent e) {
        super.mousePressed(e);
        if (e.getButton() == MouseEvent.BUTTON1) {
            MapNode toDeleteNode = getNodeAtScreenPosition(e.getX(), e.getY());
            if (toDeleteNode != null) {
                boolean canDelete = true;
                if (toDeleteNode.isControlNode()) {
                    if (bDebugLogCurveInfo) LOG.info("Cannot delete control point of curve");
                    canDelete = false;
                }
                if (quadCurve != null && isQuadCurveCreated) {
                    if (quadCurve.isCurveAnchorPoint(toDeleteNode)) {
                        if (bDebugLogCurveInfo) LOG.info("Cannot delete start node of quad curve until it is confirmed or cancelled");
                        canDelete = false;
                    }
                }
                if (cubicCurve != null && isCubicCurveCreated) {
                    if (cubicCurve.isCurveAnchorPoint(toDeleteNode)) {
                        if (bDebugLogCurveInfo) LOG.info("Cannot delete start/end node of cubic curve until it is confirmed or cancelled");
                        canDelete = false;
                    }
                }
                if (canDelete) {
                    addToDeleteList(toDeleteNode);
                    changeManager.addChangeable( new DeleteNodeChanger(deleteNodeList));
                    removeDeleteListNodes();
                    if (toDeleteNode.hasWarning) {
                        LOG.info("Checking {} warning nodes", toDeleteNode.warningNodes.size());
                        for(MapNode overNode : toDeleteNode.warningNodes) {
                            LOG.info("Checking node ID {}", overNode.id);
                            checkNodeOverlap(overNode);
                        }
                    }
                    deleteNodeList.clear();
                    clearMultiSelection();
                }
            }
        }
    }

    @Override
    public void mouseReleased(MouseEvent e) {
        super.mouseReleased(e);
        if (e.getButton() == MouseEvent.BUTTON3) {
            removeAllNodesInScreenArea();
        }
    }

    private void removeAllNodesInScreenArea() {
        if (multiSelectList.size() > 0) {
            LOG.info("{}", getLocaleString("console_node_area_remove"));
            if (quadCurve != null && isQuadCurveCreated) {
                if (multiSelectList.contains(quadCurve.getCurveStartNode())) {
                    if (bDebugLogCurveInfo) LOG.info("Cannot delete start node of quad curve until it is confirmed or cancelled");
                    multiSelectList.remove(quadCurve.getCurveStartNode());
                    quadCurve.getCurveStartNode().isSelected = false;
                }
                if (multiSelectList.contains(quadCurve.getCurveEndNode())) {
                    if (bDebugLogCurveInfo) LOG.info("Cannot delete end nodes of quad curve until it is confirmed or cancelled");
                    multiSelectList.remove(quadCurve.getCurveEndNode());
                    quadCurve.getCurveEndNode().isSelected = false;
                }
                if (multiSelectList.contains(quadCurve.getControlPoint())) {
                    if (bDebugLogCurveInfo) LOG.info("Cannot delete quad curve control point");
                    multiSelectList.remove(quadCurve.getControlPoint());
                    quadCurve.getControlPoint().isSelected = false;
                }
            }
            if (cubicCurve != null && isCubicCurveCreated) {
                if (multiSelectList.contains(cubicCurve.getCurveStartNode())) {
                    if (bDebugLogCurveInfo) LOG.info("Cannot delete start node of cubic curve until it is confirmed or cancelled");
                    multiSelectList.remove(cubicCurve.getCurveStartNode());
                    cubicCurve.getCurveStartNode().isSelected = false;
                }
                if (multiSelectList.contains(cubicCurve.getCurveEndNode())) {
                    if (bDebugLogCurveInfo) LOG.info("Cannot delete end node of cubic curve until it is confirmed or cancelled");
                    multiSelectList.remove(cubicCurve.getCurveEndNode());
                    cubicCurve.getCurveEndNode().isSelected = false;
                }
                if (multiSelectList.contains(cubicCurve.getControlPoint1())) {
                    if (bDebugLogCurveInfo) LOG.info("Cannot delete cubic curve control point 1");
                    multiSelectList.remove(cubicCurve.getControlPoint1());
                    cubicCurve.getControlPoint1().isSelected = false;
                }
                if (multiSelectList.contains(cubicCurve.getControlPoint2())) {
                    if (bDebugLogCurveInfo) LOG.info("Cannot delete cubic curve control point 2");
                    multiSelectList.remove(cubicCurve.getControlPoint2());
                    cubicCurve.getControlPoint2().isSelected = false;
                }
            }
            for (MapNode node : multiSelectList) {

                addToDeleteList(node);
                if (bDebugLogUndoRedo) LOG.info("Added ID {} to delete list", node.id);
            }
            changeManager.addChangeable( new DeleteNodeChanger(deleteNodeList));
            canAutoSave = false;
            removeDeleteListNodes();
            canAutoSave = true;
            deleteNodeList.clear();
            clearMultiSelection();
        }
    }

    public static void addToDeleteList(MapNode node) {
        LinkedList<MapNode> otherNodesInLinks = new LinkedList<>();
        LinkedList<MapNode> otherNodesOutLinks = new LinkedList<>();

        LinkedList<MapNode> roadmapNodes = RoadMap.networkNodesList;
        for (MapNode mapNode : roadmapNodes) {
            if (mapNode != node) {
                if (mapNode.outgoing.contains(node)) {
                    otherNodesOutLinks.add(mapNode);
                }
                if (mapNode.incoming.contains(node)) {
                    otherNodesInLinks.add(mapNode);
                }
            }

        }
        deleteNodeList.add(new NodeLinks(node, otherNodesInLinks, otherNodesOutLinks));
    }

    public static void removeDeleteListNodes() {
        canAutoSave = false;

        for (NodeLinks nodeLinks : deleteNodeList) {
            MapNode inList = nodeLinks.node;
            RoadMap.removeMapNode(inList);
        }

        canAutoSave = true;
        setStale(true);
        hoveredNode = null;
        getMapPanel().repaint();
    }

    public static class NodeLinks {

        public MapNode node;
        public int nodeIDBackup;
        public LinkedList<MapNode> otherIncoming;
        public LinkedList<MapNode> otherOutgoing;

        public NodeLinks(MapNode mapNode, LinkedList<MapNode> in, LinkedList<MapNode> out) {
            this.node = mapNode;
            this.nodeIDBackup = mapNode.id;
            this.otherIncoming = new LinkedList<>();
            this.otherOutgoing = new LinkedList<>();

            for (int i = 0; i <= in.size() - 1 ; i++) {
                MapNode inNode = in.get(i);
                if (!this.otherIncoming.contains(inNode)) this.otherIncoming.add(inNode);
            }
            for (int i = 0; i <= out.size() - 1 ; i++) {
                MapNode outNode = out.get(i);
                if (!this.otherOutgoing.contains(outNode)) this.otherOutgoing.add(outNode);
            }
        }
    }

    //
    // Delete Node
    //

    public static class DeleteNodeChanger implements ChangeManager.Changeable {

        private final LinkedList<NodeLinks> nodeListToDelete;
        private final boolean isStale;
        private final UUID opUUID;

        @SuppressWarnings("unchecked")
        public DeleteNodeChanger(LinkedList<NodeLinks> nodeLinks){
            super();
            this.nodeListToDelete =  (LinkedList<NodeLinks>) nodeLinks.clone();
            this.isStale = isStale();
            this.opUUID = RoadMap.uuid;
        }

        public void undo(){
            showInTextArea("Restoring " + this.nodeListToDelete.size() + " Nodes.", true, false);
            try {
                for (NodeLinks insertNode : this.nodeListToDelete) {
                    if (bDebugLogUndoRedo) LOG.info("Insert {} ({})",insertNode.node.id,insertNode.nodeIDBackup);
                    if (insertNode.node.id != insertNode.nodeIDBackup) {
                        if (bDebugLogUndoRedo) LOG.info("## RemoveNode Undo ## ID mismatch.. correcting ID {} -> ID {}", insertNode.node.id, insertNode.nodeIDBackup);
                        insertNode.node.id = insertNode.nodeIDBackup;
                    }
                    roadMap.insertMapNode(insertNode.node, insertNode.otherIncoming, insertNode.otherOutgoing);
                }
                String text = this.nodeListToDelete.size() + " nodes restored";
                showInTextArea(text, true, true);
            } catch (IndexOutOfBoundsException outOfBoundsException) {
                if (!this.opUUID.equals(RoadMap.uuid)) {
                    JOptionPane.showMessageDialog(editor, getLocaleString("dialog_undo_uuid_mismatch"), getLocaleString("dialog_undo_error_title"), JOptionPane.ERROR_MESSAGE);
                    showInTextArea(getLocaleString("dialog_undo_uuid_mismatch"), true, true);
                } else {
                    JOptionPane.showMessageDialog(editor, getLocaleString("dialog_undo_outofbounds"), getLocaleString("dialog_undo_error_title"), JOptionPane.ERROR_MESSAGE);
                    showInTextArea(getLocaleString("dialog_undo_outofbounds"), true, true);
                }
            }
            getMapPanel().repaint();
            setStale(this.isStale);
        }

        public void redo(){
            for (NodeLinks nodeLinks : this.nodeListToDelete) {
                MapNode toDelete = nodeLinks.node;
                RoadMap.removeMapNode(toDelete);
            }
            getMapPanel().repaint();
            setStale(true);
        }
    }
}
