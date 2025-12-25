package AutoDriveEditor.GUI.Buttons.ActionBar.ChangePanel;

import AutoDriveEditor.Classes.SnapShot;
import AutoDriveEditor.Classes.UI_Components.ScaleAnimIcon;
import AutoDriveEditor.GUI.Buttons.BaseButton;
import AutoDriveEditor.Managers.CopyPasteManager;

import javax.swing.*;
import java.awt.event.ActionEvent;

import static AutoDriveEditor.Classes.Util_Classes.LoggerUtils.LOG;
import static AutoDriveEditor.Locale.LocaleManager.getLocaleString;
import static AutoDriveEditor.Managers.IconManager.*;
import static AutoDriveEditor.Managers.MultiSelectManager.isMultipleSelected;
import static AutoDriveEditor.Managers.MultiSelectManager.multiSelectList;

public class CutButton extends BaseButton {

    private static SnapShot snapShot;

    public CutButton(JPanel panel) {
        ScaleAnimIcon animCutButton = createScaleAnimIcon(CUT_ICON, false);
        button = createAnimButton(animCutButton, panel, null, null, false, false, this);
    }

    @Override
    public String getButtonID() { return "CutButton"; }

    @Override
    public String getButtonAction() { return "ActionBarButton"; }

    @Override
    public String getButtonPanel() { return "ActionBar"; }

    @Override
    public void actionPerformed(ActionEvent e) {
        super.actionPerformed(e);
        if (isMultipleSelected && !multiSelectList.isEmpty()) {
            CopyPasteManager.cutSelection();
        } else {
            LOG.info("Nothing to Cut");
        }
    }
    @Override
    public String buildToolTip() {
        return getLocaleString("actionbar_copypaste_tooltip").replace("{current}", getLocaleString("actionbar_copypaste_cut"));
    }
}
