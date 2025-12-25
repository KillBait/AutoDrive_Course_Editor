package AutoDriveEditor.GUI.Buttons.ActionBar.ChangePanel;

import AutoDriveEditor.Classes.KeyBinds.Shortcut;
import AutoDriveEditor.Classes.UI_Components.ScaleAnimIcon;
import AutoDriveEditor.GUI.Buttons.BaseButton;
import AutoDriveEditor.Managers.CopyPasteManager;

import javax.swing.*;
import java.awt.event.ActionEvent;

import static AutoDriveEditor.AutoDriveEditor.getMapPanel;
import static AutoDriveEditor.Classes.Util_Classes.LoggerUtils.LOG;
import static AutoDriveEditor.GUI.Menus.EditMenu.CopyMenu.menu_Copy;
import static AutoDriveEditor.Locale.LocaleManager.getLocaleString;
import static AutoDriveEditor.Managers.IconManager.*;
import static AutoDriveEditor.Managers.MultiSelectManager.isMultipleSelected;
import static AutoDriveEditor.Managers.MultiSelectManager.multiSelectList;
import static AutoDriveEditor.Managers.ShortcutManager.ShortcutID.COPY_SHORTCUT;
import static AutoDriveEditor.Managers.ShortcutManager.getUserShortcutByID;

public class CopyButton extends BaseButton {

    public CopyButton(JPanel panel) {
        ScaleAnimIcon animCopyIcon = createScaleAnimIcon(COPY_ICON, false);
        button = createAnimButton(animCopyIcon, panel, null, null, false, false, this);

        Shortcut copyMenuShortcut = getUserShortcutByID(COPY_SHORTCUT);
        if (copyMenuShortcut != null) copyMenuShortcut.setCallbackObject(this);
    }

    @Override
    public String getButtonID() { return "CopyButton"; }

    @Override
    public String getButtonAction() { return "ActionBarButton"; }

    @Override
    public String getButtonPanel() { return "ActionBar"; }

    @Override
    public void onButtonCreation() {
        Shortcut moveShortcut = getUserShortcutByID(COPY_SHORTCUT);
        if (moveShortcut != null) menu_Copy.setAccelerator(moveShortcut.getKeyStroke());
    }

    @Override
    public void actionPerformed(ActionEvent e) {
        super.actionPerformed(e);
        copySelectedNodes();
    }

    private void copySelectedNodes() {
        if (isMultipleSelected && !multiSelectList.isEmpty()) {
            CopyPasteManager.copySelection();
        } else {
            LOG.info("Nothing to Copy");
        }
        getMapPanel().repaint();
    }

    @Override
    public String buildToolTip() {
        String description = getLocaleString("actionbar_copypaste_tooltip").replace("{current}", getLocaleString("actionbar_copypaste_copy"));
        return getShortcutText(COPY_SHORTCUT, description);
    }
}
