package AutoDriveEditor.GUI.Buttons.Toolbar.Nodes;

import AutoDriveEditor.Classes.Interfaces.WidgetInterface;
import AutoDriveEditor.Classes.KeyBinds.Shortcut;
import AutoDriveEditor.Classes.UI_Components.ScaleAnimIcon;
import AutoDriveEditor.Classes.Widgets.MoveWidget;
import AutoDriveEditor.GUI.Buttons.BaseButton;
import AutoDriveEditor.Managers.ShortcutManager;

import javax.swing.*;
import java.awt.event.ActionEvent;

import static AutoDriveEditor.AutoDriveEditor.*;
import static AutoDriveEditor.Locale.LocaleManager.getLocaleString;
import static AutoDriveEditor.Managers.IconManager.*;
import static AutoDriveEditor.Managers.ShortcutManager.ShortcutID.MOVE_NODE_SHORTCUT;
import static AutoDriveEditor.Managers.ShortcutManager.getUserShortcutByID;
import static AutoDriveEditor.Managers.ShortcutManager.registerShortcut;

public class MoveNodeButton extends BaseButton {

    private WidgetInterface moveWidget;

    @Override
    public String getButtonID() { return "MoveNodeButton"; }

    @Override
    public String getButtonAction() { return "ActionButton"; }

    @Override
    public String getButtonPanel() { return "Nodes"; }

    @Override
    public String getInfoText() { return getLocaleString("toolbar_nodes_move_infotext"); }

    @Override
    public Boolean useMultiSelection() { return true; }

    @Override
    public void onButtonSelect() {
        if (moveWidget == null) moveWidget = widgetManager.addWidgetClass(MoveWidget.class, this.getButtonID());
        moveWidget.setWidgetEnabled(true);
    }

    @Override
    public void onButtonDeselect() {
        if (moveWidget != null) moveWidget.setWidgetEnabled(false);
    }

    public MoveNodeButton(JPanel panel) {

        ScaleAnimIcon animMoveNodeIcon = createScaleAnimIcon(BUTTON_MOVE_NODE_ICON, false);
        button = createAnimToggleButton(animMoveNodeIcon, panel, null, null,  false, false, this);

        // Setup Keyboard Shortcut
        Shortcut moveShortcut = getUserShortcutByID(MOVE_NODE_SHORTCUT);
        if (moveShortcut != null) {
            Action moveButtonAction = new AbstractAction() {
                @Override
                public void actionPerformed(ActionEvent e) {
                    if (button.isEnabled() && !button.isSelected()) {
                        buttonManager.makeCurrent(buttonNode);
                    } else {
                        buttonManager.deSelectAll();
                    }
                }
            };
            registerShortcut(this, moveShortcut, moveButtonAction, getMapPanel());
        }
    }

    @Override
    public String buildToolTip() {
        Shortcut s = ShortcutManager.getUserShortcutByID(MOVE_NODE_SHORTCUT);
        if (s != null) {
            return "<html>" + getLocaleString("toolbar_nodes_move_tooltip") + "<br>( Shortcut: <b> " + s.getShortcutString() + " )</b></html>";
        } else {
            return getLocaleString("toolbar_nodes_move_tooltip");
        }
    }
}