package AutoDriveEditor.GUI.Buttons.ActionBar.ChangePanel;

import AutoDriveEditor.Classes.UI_Components.ScaleAnimIcon;
import AutoDriveEditor.GUI.Buttons.BaseButton;

import javax.swing.*;
import java.awt.event.ActionEvent;

import static AutoDriveEditor.AutoDriveEditor.changeManager;
import static AutoDriveEditor.Classes.Util_Classes.LoggerUtils.LOG;
import static AutoDriveEditor.GUI.Menus.DebugMenu.Logging.LogCopyPasteManagerMenu.bDebugLogCopyPasteManagerInfo;
import static AutoDriveEditor.Locale.LocaleManager.getLocaleString;
import static AutoDriveEditor.Managers.IconManager.*;

public class UndoButton extends BaseButton {

    public UndoButton(JPanel panel) {
        ScaleAnimIcon animUndoIcon = createScaleAnimIcon(UNDO_ICON, false);
        button = createAnimButton(animUndoIcon, panel, null, null, false, false, this);
    }

    @Override
    public String getButtonID() { return "UndoButton"; }

    @Override
    public String getButtonAction() { return "ActionBarButton"; }

    @Override
    public String getButtonPanel() { return "ActionBar"; }

    @Override
    public Boolean ignoreButtonDeselect() { return true; }

    @Override
    public void actionPerformed(ActionEvent e) {
        super.actionPerformed(e);
        if (bDebugLogCopyPasteManagerInfo) LOG.info("{} -> Button Pressed", this.getButtonID());
        changeManager.undo();
    }

    @Override
    public String buildToolTip() {
        return getLocaleString("actionbar_change_panel_tooltip").replace("{current}", getLocaleString("actionbar_change_panel_undo"));
    }
}
