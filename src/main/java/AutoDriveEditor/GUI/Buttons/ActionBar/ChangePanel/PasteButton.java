package AutoDriveEditor.GUI.Buttons.ActionBar.ChangePanel;

import AutoDriveEditor.Classes.UI_Components.ScaleAnimIcon;
import AutoDriveEditor.Classes.Util_Classes.ExceptionUtils;
import AutoDriveEditor.GUI.Buttons.BaseButton;
import AutoDriveEditor.Managers.CopyPasteManager;

import javax.swing.*;
import java.awt.event.ActionEvent;

import static AutoDriveEditor.Locale.LocaleManager.getLocaleString;
import static AutoDriveEditor.Managers.IconManager.*;

public class PasteButton extends BaseButton {

    public PasteButton(JPanel panel) {
        ScaleAnimIcon animPasteIcon = createScaleAnimIcon(PASTE_ICON, false);
        button = createAnimButton(animPasteIcon, panel, null, null, false, false, this);
    }

    @Override
    public String getButtonID() {
        return "PasteButton";
    }

    @Override
    public String getButtonAction() {
        return "ActionBarButton";
    }

    @Override
    public String getButtonPanel() {
        return "ActionBar";
    }

    @Override
    public void actionPerformed(ActionEvent e) {
        super.actionPerformed(e);
        try {
            CopyPasteManager.pasteSelection(false);
        } catch (ExceptionUtils.MismatchedIdException ex) {
            throw new RuntimeException(ex);
        }
    }



    @Override
    public String buildToolTip() {
        return getLocaleString("actionbar_copypaste_paste_tooltip");
    }
}
