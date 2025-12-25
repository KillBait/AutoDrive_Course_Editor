package AutoDriveEditor.Managers;

import AutoDriveEditor.Classes.SnapShot;
import AutoDriveEditor.Classes.Util_Classes.ExceptionUtils;
import AutoDriveEditor.RoadNetwork.MapNode;

import static AutoDriveEditor.AutoDriveEditor.changeManager;
import static AutoDriveEditor.AutoDriveEditor.getMapPanel;
import static AutoDriveEditor.Classes.Util_Classes.LoggerUtils.LOG;
import static AutoDriveEditor.GUI.MapPanel.isStale;
import static AutoDriveEditor.GUI.MapPanel.setStale;
import static AutoDriveEditor.GUI.Menus.DebugMenu.Logging.LogCopyPasteManagerMenu.bDebugLogCopyPasteManagerInfo;
import static AutoDriveEditor.GUI.Menus.EditorMenu.updateEditMenu;
import static AutoDriveEditor.Managers.MultiSelectManager.clearMultiSelection;
import static AutoDriveEditor.Managers.MultiSelectManager.multiSelectList;
import static AutoDriveEditor.Managers.ScanManager.checkAreaForNodeOverlap;
import static AutoDriveEditor.XMLConfig.AutoSave.resumeAutoSaving;
import static AutoDriveEditor.XMLConfig.AutoSave.suspendAutoSaving;

public class CopyPasteManager {

    public static SnapShot snapShot;

    public static void cutSelection() {
        snapShot = new SnapShot(multiSelectList);
        if (bDebugLogCopyPasteManagerInfo) LOG.info("## CopyPasteManager.cutSelection() ## Cutting {} nodes",multiSelectList.size());
        changeManager.addChangeable(new CutNodesChanger(snapShot));
        suspendAutoSaving();
        snapShot.removeOriginalNodes();
        clearMultiSelection();
        updateEditMenu();
        setStale(true);
        resumeAutoSaving();
        getMapPanel().repaint();
    }

    public static void copySelection() {
        if (!multiSelectList.isEmpty()) {
            if (bDebugLogCopyPasteManagerInfo) LOG.info("## CopyPasteManager.copySelection() ## Copying {} nodes",multiSelectList.size());
            snapShot = new SnapShot(multiSelectList);
            clearMultiSelection();
            updateEditMenu();
        }
    }

    public static void pasteSelection(boolean inOriginalLocation) throws ExceptionUtils.MismatchedIdException {
        if (snapShot != null) {
            if (bDebugLogCopyPasteManagerInfo) LOG.info("## CopyPasteManager.pasteSelection() ## Pasting {} nodes",snapShot.getOriginalNodeList().size());
            suspendAutoSaving();
            snapShot.createNewCopyOfNodes(inOriginalLocation);
            changeManager.addChangeable(new PasteSelectionChanger(new SnapShot(snapShot.getNewNodeList())));
            setStale(true);
            resumeAutoSaving();
            getMapPanel().repaint();
        }
    }

    //
    //  Getter
    //

    public static boolean getSnapShot() { return snapShot != null; }

    //
    // CutNodesChanger
    //

    static class CutNodesChanger implements ChangeManager.Changeable {
        private final SnapShot snapshot;
        private final boolean isStale;

        public CutNodesChanger(SnapShot snapShot){
            super();
            this.snapshot = snapShot;
            this.isStale = isStale();
        }

        public void undo() {
            suspendAutoSaving();
            try {
                this.snapshot.restoreOriginalNodes();
            } catch (ExceptionUtils.MismatchedIdException e) {
                throw new RuntimeException(e);
            }
            for (MapNode node : this.snapshot.getNewNodeList()) {
                checkAreaForNodeOverlap(node);
            }
            getMapPanel().repaint();
            setStale(this.isStale);
            resumeAutoSaving();
        }

        public void redo(){
            suspendAutoSaving();
            this.snapshot.removeOriginalNodes();
            getMapPanel().repaint();
            setStale(true);
            resumeAutoSaving();
        }
    }

    //
    // PasteSelectionChanger
    //

    public static class PasteSelectionChanger implements ChangeManager.Changeable {
        private final SnapShot snapShot;
        private final boolean isStale;

        public PasteSelectionChanger(SnapShot snapShot) {
            super();
            this.snapShot = snapShot;
            this.isStale = isStale();
        }

        public void undo() {
            suspendAutoSaving();
            clearMultiSelection();
            this.snapShot.removeOriginalNodes();
            getMapPanel().repaint();
            setStale(this.isStale);
            resumeAutoSaving();
        }

        public void redo() {
            suspendAutoSaving();
            try {
                this.snapShot.restoreOriginalNodes();
            } catch (ExceptionUtils.MismatchedIdException e) {
                throw new RuntimeException(e);
            }
            getMapPanel().repaint();
            setStale(true);
            resumeAutoSaving();
        }
    }

}
