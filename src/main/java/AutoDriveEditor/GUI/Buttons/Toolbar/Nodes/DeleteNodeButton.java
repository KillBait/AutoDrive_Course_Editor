package AutoDriveEditor.GUI.Buttons.Toolbar.Nodes;

import AutoDriveEditor.Classes.KeyBinds.Shortcut;
import AutoDriveEditor.Classes.SnapShot;
import AutoDriveEditor.Classes.UI_Components.ScaleAnimIcon;
import AutoDriveEditor.Classes.Util_Classes.ExceptionUtils;
import AutoDriveEditor.GUI.Buttons.BaseButton;
import AutoDriveEditor.Managers.ChangeManager;
import AutoDriveEditor.Managers.ShortcutManager;
import AutoDriveEditor.RoadNetwork.MapNode;

import javax.swing.*;
import java.awt.event.ActionEvent;
import java.awt.event.MouseEvent;
import java.util.ArrayList;

import static AutoDriveEditor.AutoDriveEditor.changeManager;
import static AutoDriveEditor.AutoDriveEditor.getMapPanel;
import static AutoDriveEditor.Classes.Util_Classes.LoggerUtils.LOG;
import static AutoDriveEditor.GUI.MapPanel.*;
import static AutoDriveEditor.GUI.Menus.DebugMenu.Logging.LogUndoRedoMenu.bDebugLogUndoRedoInfo;
import static AutoDriveEditor.GUI.TextPanel.showInTextArea;
import static AutoDriveEditor.Locale.LocaleManager.getLocaleString;
import static AutoDriveEditor.Managers.IconManager.*;
import static AutoDriveEditor.Managers.MultiSelectManager.clearMultiSelection;
import static AutoDriveEditor.Managers.MultiSelectManager.multiSelectList;
import static AutoDriveEditor.Managers.ScanManager.checkAreaForNodeOverlap;
import static AutoDriveEditor.Managers.ShortcutManager.ShortcutID.DELETE_SHORTCUT;
import static AutoDriveEditor.Managers.ShortcutManager.getUserShortcutByID;
import static AutoDriveEditor.Managers.ShortcutManager.registerShortcut;
import static AutoDriveEditor.RoadNetwork.RoadMap.showMismatchedIDError;
import static AutoDriveEditor.XMLConfig.AutoSave.resumeAutoSaving;
import static AutoDriveEditor.XMLConfig.AutoSave.suspendAutoSaving;

public class DeleteNodeButton extends BaseButton {

    @Override
    public String getButtonID() { return "DeleteNodeButton"; }

    @Override
    public String getButtonAction() { return "ActionButton"; }

    @Override
    public String getButtonPanel() { return "Nodes"; }

    @Override
    public String getInfoText() { return getLocaleString("toolbar_nodes_remove_infotext"); }

    @Override
    public Boolean useMultiSelection() { return true; }

    public DeleteNodeButton(JPanel panel) {
        ScaleAnimIcon animDeleteIcon = createScaleAnimIcon(BUTTON_DELETE_ICON, false);
        button = createAnimToggleButton(animDeleteIcon, panel, null, null,  false, false, this);

        Shortcut deleteShortcut = getUserShortcutByID(DELETE_SHORTCUT);
        if (deleteShortcut != null) {
            Action deleteButtonAction = new AbstractAction() {
                @Override
                public void actionPerformed(ActionEvent e) {
                    if (!multiSelectList.isEmpty()) {
                        removeSelectedListNodes();
                    } else {
                        if (getMapPanel().hoveredNode != null) {
                            removeNode(getMapPanel().hoveredNode);
                        }
                    }
                    getMapPanel().repaint();
                }
            };
            registerShortcut(this, deleteShortcut, deleteButtonAction, getMapPanel());
        }
    }

    @Override
    public void mouseReleased(MouseEvent e) {
        super.mouseReleased(e);
        if (e.getButton() == MouseEvent.BUTTON1) {
            MapNode toDeleteNode = getNodeAtScreenPosition(e.getX(), e.getY());
            if (toDeleteNode != null && toDeleteNode.canDelete()) {
                removeNode(toDeleteNode);
                getMapPanel().repaint();
            }
        }
    }

    @Override
    public void onMultiSelectChange(ArrayList<MapNode> nodeList) {
        removeSelectedListNodes();
    }

    private void removeNode(MapNode toDeleteNode) {
        if (toDeleteNode != null && toDeleteNode.canDelete()) {
            if (bDebugLogUndoRedoInfo) LOG.info("Removing Selected Node ID {}", toDeleteNode.id);
            SnapShot deleteNodeSnapShot = new SnapShot(toDeleteNode);
            changeManager.addChangeable( new DeleteNodeChanger(deleteNodeSnapShot));

            suspendAutoSaving();
            deleteNodeSnapShot.removeOriginalNodes();
            if (toDeleteNode == getMapPanel().hoveredNode) getMapPanel().hoveredNode = null;
            setStale(true);
            resumeAutoSaving();
        }

    }

    private void removeSelectedListNodes() {
        ArrayList<MapNode> deleteList = new ArrayList<>();
        for (MapNode mapNode : multiSelectList) {
            if (mapNode.canDelete()) {
                deleteList.add(mapNode);
                if (bDebugLogUndoRedoInfo) LOG.info("Added MapNode ID {} to delete list", mapNode.id);
            }
        }

        if (!deleteList.isEmpty()) {
            SnapShot deleteSnapShot = new SnapShot(deleteList);
            changeManager.addChangeable( new DeleteNodeChanger(deleteSnapShot));

            suspendAutoSaving();
            if (bDebugLogUndoRedoInfo) LOG.info("Removing {} Nodes", deleteSnapShot.getOriginalNodeList().size());
            deleteSnapShot.removeOriginalNodes();
            setStale(true);
            clearMultiSelection();
            resumeAutoSaving();
        }
    }

    @Override
    public String buildToolTip() {
        Shortcut s = ShortcutManager.getUserShortcutByID(DELETE_SHORTCUT);
        if (s != null) {
            return getLocaleString("toolbar_nodes_remove_tooltip") + "\n\n( Shortcut: " + s.getShortcutString() + " )";
        } else {
            return getLocaleString("toolbar_nodes_remove_tooltip");
        }
    }

    public static class DeleteNodeChanger implements ChangeManager.Changeable {

        private final SnapShot snapShot;
        private final boolean isStale;

        public DeleteNodeChanger(SnapShot snapShot){
            this.snapShot = snapShot;
            this.isStale = isStale();
        }

        public void undo(){
            suspendAutoSaving();
            showInTextArea("Restoring " + this.snapShot.getOriginalNodeList().size() + " Nodes.", true, false);
            try {
                this.snapShot.restoreOriginalNodes();
            } catch (ExceptionUtils.MismatchedIdException e) {
                showMismatchedIDError("DeleteNodeChanger.undo()", e);
            }
            for (MapNode node : this.snapShot.getOriginalNodeList()) {
                checkAreaForNodeOverlap(node);
            }
            setStale(this.isStale);
            getMapPanel().repaint();
            resumeAutoSaving();
        }

        public void redo(){
            suspendAutoSaving();
            try {
                this.snapShot.removeOriginalNodes();
            } catch (Exception e) {
                throw new RuntimeException(e);
            }

            setStale(true);
            resumeAutoSaving();
            getMapPanel().repaint();
        }
    }
}
