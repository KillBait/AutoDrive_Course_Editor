package AutoDriveEditor.GUI.Buttons.Nodes;

import AutoDriveEditor.GUI.Buttons.BaseButton;
import AutoDriveEditor.Managers.ChangeManager;
import AutoDriveEditor.RoadNetwork.MapNode;
import AutoDriveEditor.RoadNetwork.RoadMap;
import AutoDriveEditor.Utils.ExceptionUtils;

import javax.swing.*;
import java.awt.event.MouseEvent;
import java.util.Comparator;
import java.util.LinkedList;
import java.util.UUID;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import static AutoDriveEditor.AutoDriveEditor.*;
import static AutoDriveEditor.GUI.Buttons.Curves.CubicCurveButton.cubicCurve;
import static AutoDriveEditor.GUI.Buttons.Curves.CubicCurveButton.isCubicCurveCreated;
import static AutoDriveEditor.GUI.Buttons.Curves.QuadCurveButton.isQuadCurveCreated;
import static AutoDriveEditor.GUI.Buttons.Curves.QuadCurveButton.quadCurve;
import static AutoDriveEditor.GUI.MapPanel.*;
import static AutoDriveEditor.GUI.Menus.DebugMenu.Logging.LogCurveInfoMenu.bDebugLogCurveInfo;
import static AutoDriveEditor.GUI.Menus.DebugMenu.Logging.LogUndoRedoMenu.bDebugLogUndoRedo;
import static AutoDriveEditor.GUI.TextPanel.showInTextArea;
import static AutoDriveEditor.Locale.LocaleManager.getLocaleString;
import static AutoDriveEditor.Managers.MultiSelectManager.clearMultiSelection;
import static AutoDriveEditor.Managers.MultiSelectManager.multiSelectList;
import static AutoDriveEditor.RoadNetwork.RoadMap.networkNodesList;
import static AutoDriveEditor.RoadNetwork.RoadMap.showMismatchedIDError;
import static AutoDriveEditor.Utils.ExceptionUtils.createExceptionDialog;
import static AutoDriveEditor.Utils.GUIUtils.makeImageToggleButton;
import static AutoDriveEditor.Utils.LoggerUtils.LOG;
import static AutoDriveEditor.XMLConfig.AutoSave.resumeAutoSaving;
import static AutoDriveEditor.XMLConfig.AutoSave.suspendAutoSaving;

public class DeleteNodeButton extends BaseButton {

    public static final LinkedList<NodeLinks> deleteNodeList = new LinkedList<>();

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
    public Boolean useMultiSelection() { return true; }

    @Override
    public void mousePressed(MouseEvent e) {
        super.mousePressed(e);
        if (e.getButton() == MouseEvent.BUTTON1) {
            MapNode toDeleteNode = getNodeAtScreenPosition(e.getX(), e.getY());

            if (toDeleteNode != null) {
                LOG.info("Deleted {} from {}", toDeleteNode.id, networkNodesList.indexOf(toDeleteNode));
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
                    LOG.info("Added {} to delete list", toDeleteNode.id);
                    changeManager.addChangeable( new DeleteNodeChanger(deleteNodeList));
                    removeDeleteListNodes();
                    /*if (toDeleteNode.hasWarning()) {
                        LOG.info("Checking {} warning nodes", toDeleteNode.getWarningNodes().size());
                        for(MapNode overNode : toDeleteNode.getWarningNodes()) {
                            LOG.info("ID {}", overNode.id);
                        }
                        for(MapNode overNode : toDeleteNode.getWarningNodes()) {
                            LOG.info("Checking node ID {}", overNode.id);
                            checkNodeOverlap(overNode);
                        }
                    }*/
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
            suspendAutoSaving();
            LOG.info("{}", getLocaleString("console_node_area_remove"));
            if (quadCurve != null && isQuadCurveCreated) {
                if (multiSelectList.contains(quadCurve.getCurveStartNode())) {
                    if (bDebugLogCurveInfo) LOG.info("Cannot delete start node of quad curve until it is confirmed or cancelled");
                    multiSelectList.remove(quadCurve.getCurveStartNode());
                    quadCurve.getCurveStartNode().setSelected(false);
                }
                if (multiSelectList.contains(quadCurve.getCurveEndNode())) {
                    if (bDebugLogCurveInfo) LOG.info("Cannot delete end nodes of quad curve until it is confirmed or cancelled");
                    multiSelectList.remove(quadCurve.getCurveEndNode());
                    quadCurve.getCurveEndNode().setSelected(false);
                }
                if (multiSelectList.contains(quadCurve.getControlPoint())) {
                    if (bDebugLogCurveInfo) LOG.info("Cannot delete quad curve control point");
                    multiSelectList.remove(quadCurve.getControlPoint());
                    quadCurve.getControlPoint().setSelected(false);
                }
            }
            if (cubicCurve != null && isCubicCurveCreated) {
                if (multiSelectList.contains(cubicCurve.getCurveStartNode())) {
                    if (bDebugLogCurveInfo) LOG.info("Cannot delete start node of cubic curve until it is confirmed or cancelled");
                    multiSelectList.remove(cubicCurve.getCurveStartNode());
                    cubicCurve.getCurveStartNode().setSelected(false);
                }
                if (multiSelectList.contains(cubicCurve.getCurveEndNode())) {
                    if (bDebugLogCurveInfo) LOG.info("Cannot delete end node of cubic curve until it is confirmed or cancelled");
                    multiSelectList.remove(cubicCurve.getCurveEndNode());
                    cubicCurve.getCurveEndNode().setSelected(false);
                }
                if (multiSelectList.contains(cubicCurve.getControlPoint1())) {
                    if (bDebugLogCurveInfo) LOG.info("Cannot delete cubic curve control point 1");
                    multiSelectList.remove(cubicCurve.getControlPoint1());
                    cubicCurve.getControlPoint1().setSelected(false);
                }
                if (multiSelectList.contains(cubicCurve.getControlPoint2())) {
                    if (bDebugLogCurveInfo) LOG.info("Cannot delete cubic curve control point 2");
                    multiSelectList.remove(cubicCurve.getControlPoint2());
                    cubicCurve.getControlPoint2().setSelected(false);
                }
            }
            for (MapNode node : multiSelectList) {
                addToDeleteList(node);
                if (bDebugLogUndoRedo) LOG.info("Added ID {} to delete list", node.id);
            }
            changeManager.addChangeable( new DeleteNodeChanger(deleteNodeList));
            removeDeleteListNodes();
            deleteNodeList.clear();
            clearMultiSelection();
            resumeAutoSaving();
        }
    }

    public static void addToDeleteList(MapNode node) {
        LinkedList<MapNode> otherNodesInLinks = new LinkedList<>();
        LinkedList<MapNode> otherNodesOutLinks = new LinkedList<>();

        for (MapNode mapNode : RoadMap.networkNodesList) {
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
        suspendAutoSaving();
        for (NodeLinks nodeLinks : deleteNodeList) {
            for (MapNode overlapNode : nodeLinks.node.getWarningNodes()) {
                overlapNode.getWarningNodes().remove(nodeLinks.node);
                if (overlapNode.getWarningNodes().size() == 0) overlapNode.clearWarningNodes();
            }
            RoadMap.removeMapNode(nodeLinks.node);
        }
        setStale(true);
        hoveredNode = null;
        getMapPanel().repaint();
        resumeAutoSaving();
    }

    public static class NodeLinks {

        public final MapNode node;
        public final int nodeIDBackup;
        public final LinkedList<MapNode> otherIncoming;
        public final LinkedList<MapNode> otherOutgoing;

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
        private final UUID UUID;

        public DeleteNodeChanger(LinkedList<NodeLinks> nodeLinks){
            super();
            this.nodeListToDelete = new LinkedList<> (nodeLinks);

            // v1.07 Fix for OutOfBoundsException
            //
            // Bug was caused when the nodes were stored for the undo function,
            // the order of the nodes in the list was the same as they were selected.
            //
            // Since the node ID's were out of order, it was possible to get into the
            // situation where the undo function was trying to insert a node back into
            // the roadmap at a position beyond the last index,
            //
            // Fix applied is to sort the list numerically by ID order, when the nodes
            // are restored it always starts with the smallest ID and works its way up
            //

            nodeListToDelete.sort(Comparator.comparingInt(nl -> nl.node.id));

            this.isStale = isStale();
            this.UUID = RoadMap.uuid;
        }

        public void undo(){
            showInTextArea("Restoring " + this.nodeListToDelete.size() + " Nodes.", true, false);
            try {
                for (NodeLinks insertNode : this.nodeListToDelete) {
                    if (bDebugLogUndoRedo) LOG.info("## DeleteNodeChanger undo() ## Insert ID {} Backup ID ({})",insertNode.node.id,insertNode.nodeIDBackup);
                    if (insertNode.node.id != insertNode.nodeIDBackup) {
                        if (bDebugLogUndoRedo) LOG.info("## DeleteNodeChanger undo() ## MapNode ID mismatch.. correcting ID {} -> ID {}", insertNode.node.id, insertNode.nodeIDBackup);
                        insertNode.node.id = insertNode.nodeIDBackup;
                    }
                    roadMap.insertMapNode(insertNode.node, insertNode.otherIncoming, insertNode.otherOutgoing);
                }
            } catch (IndexOutOfBoundsException outOfBoundsException) {
                if (!this.UUID.equals(RoadMap.uuid)) {
                    JOptionPane.showMessageDialog(editor, getLocaleString("dialog_undo_uuid_mismatch"), getLocaleString("dialog_undo_error_title"), JOptionPane.ERROR_MESSAGE);
                    showInTextArea(getLocaleString("dialog_undo_uuid_mismatch"), true, true);
                } else {
                    showOutOfBoundsError(outOfBoundsException, "deleteNodeChanger() undo()");
                    showInTextArea(getLocaleString("dialog_undo_outofbounds"), true, false);
                }
            } catch (ExceptionUtils.MismatchedIdException e) {
                showMismatchedIDError("DeleteNodeChanger undo()", e);
            }
            hoveredNode = null;
            getMapPanel().repaint();
            setStale(this.isStale);
        }

        public void redo(){
            for (NodeLinks nodeLinks : this.nodeListToDelete) {
                MapNode toDelete = nodeLinks.node;
                RoadMap.removeMapNode(toDelete);
            }
            hoveredNode = null;
            getMapPanel().repaint();
            setStale(true);
        }
    }

    public static void showOutOfBoundsError(IndexOutOfBoundsException e, String functionName) {

        String value1 = "";
        String value2 = "";
        Pattern pattern = Pattern.compile("\\d+");
        Matcher matcher = pattern.matcher(e.getMessage());

        // Find and extract the numbers
        int count = 0;
        while (matcher.find() && count < 2) {
            String number = matcher.group();
            if (count == 0) value1 = number;
            if (count == 1) value2 = number;
            count++;
        }


        String errorText1 = getLocaleString("dialog_id_mismatch1") + " " + value1 + " " +
                getLocaleString("dialog_id_mismatch2") + " " + value2 + " ( ";

        String errorText2 ="<html><center>" + getLocaleString("dialog_id_mismatch4") +
                "<br><center>" + getLocaleString("dialog_id_mismatch5");

        createExceptionDialog(e, getLocaleString("dialog_id_mismatch_title"), "IndexOutOfBoundsException", functionName, errorText1, errorText2);
        LOG.warn("## {} ## {} aborted to prevent roadmap corruption: Error inserting mapNode ID {} into index {}", e.getMessage(), functionName, value1, value2);
    }


}
