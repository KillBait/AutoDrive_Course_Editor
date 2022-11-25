package AutoDriveEditor.GUI.Buttons;

import AutoDriveEditor.MapPanel.MapPanel;

import static AutoDriveEditor.Managers.MultiSelectManager.isMultipleSelected;
import static AutoDriveEditor.Managers.MultiSelectManager.multiSelectList;
import static AutoDriveEditor.MapPanel.MapPanel.cnpManager;
import static AutoDriveEditor.Utils.LoggerUtils.LOG;

public abstract class CopyPasteBaseButton extends BaseButton {

    public static void cutSelected() {
        if (isMultipleSelected && multiSelectList.size() > 0 ) {
            cnpManager.CutSelection(multiSelectList);
            MapPanel.getMapPanel().repaint();
        } else {
            LOG.info("Nothing to Cut");
        }
    }

    public static void copySelected() {
        if (isMultipleSelected && multiSelectList.size() > 0 ) {
            cnpManager.CopySelection(multiSelectList);
        } else {
            LOG.info("Nothing to Copy");
        }
    }

    public static void pasteSelected() {
        cnpManager.PasteSelection(false);
    }

    public static void pasteSelectedInOriginalLocation() {
        cnpManager.PasteSelection(true);
    }
}
