package AutoDriveEditor.GUI.Buttons;

import static AutoDriveEditor.AutoDriveEditor.getMapPanel;
import static AutoDriveEditor.GUI.MapPanel.cnpManager;
import static AutoDriveEditor.Managers.MultiSelectManager.isMultipleSelected;
import static AutoDriveEditor.Managers.MultiSelectManager.multiSelectList;
import static AutoDriveEditor.Utils.LoggerUtils.LOG;
import static AutoDriveEditor.XMLConfig.AutoSave.resumeAutoSaving;
import static AutoDriveEditor.XMLConfig.AutoSave.suspendAutoSaving;

public abstract class CopyPasteBaseButton extends BaseButton {

    public static void cutSelected() {
        suspendAutoSaving();
        if (isMultipleSelected && multiSelectList.size() > 0 ) {
            cnpManager.CutSelection(multiSelectList);
            getMapPanel().repaint();

        } else {
            LOG.info("Nothing to Cut");
        }
        resumeAutoSaving();
    }

    public static void copySelected() {
        suspendAutoSaving();
        if (isMultipleSelected && multiSelectList.size() > 0 ) {
            cnpManager.CopySelection(multiSelectList);

        } else {
            LOG.info("Nothing to Copy");
        }
        getMapPanel().repaint();
        resumeAutoSaving();
    }

    public static void pasteSelected() {
        suspendAutoSaving();
        cnpManager.PasteSelection(false);
        getMapPanel().repaint();
        resumeAutoSaving();
    }

    public static void pasteSelectedInOriginalLocation() {
        cnpManager.PasteSelection(true);
    }
}
