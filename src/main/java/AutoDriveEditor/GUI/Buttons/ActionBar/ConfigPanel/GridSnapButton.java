package AutoDriveEditor.GUI.Buttons.ActionBar.ConfigPanel;

import AutoDriveEditor.Classes.KeyBinds.Shortcut;
import AutoDriveEditor.Classes.UI_Components.ScaleAnimIcon;
import AutoDriveEditor.Classes.UI_Components.StateButton;
import AutoDriveEditor.GUI.Buttons.OptionsBaseButton;

import javax.swing.*;
import java.awt.event.ActionEvent;
import java.awt.event.MouseAdapter;
import java.awt.event.MouseEvent;

import static AutoDriveEditor.AutoDriveEditor.getMapPanel;
import static AutoDriveEditor.Locale.LocaleManager.getLocaleString;
import static AutoDriveEditor.Managers.IconManager.*;
import static AutoDriveEditor.Managers.ShortcutManager.ShortcutID.TOGGLE_GRID_SNAPPING_SHORTCUT;
import static AutoDriveEditor.Managers.ShortcutManager.ShortcutID.TOGGLE_SUBDIVISION_SNAPPING_SHORTCUT;
import static AutoDriveEditor.Managers.ShortcutManager.getUserShortcutByID;
import static AutoDriveEditor.Managers.ShortcutManager.registerShortcut;
import static AutoDriveEditor.XMLConfig.EditorXML.bGridSnapEnabled;
import static AutoDriveEditor.XMLConfig.EditorXML.bGridSnapSubs;

public class GridSnapButton extends OptionsBaseButton {

    @Override
    public String getButtonID() { return "GridSnapSelectButton"; }

    @Override
    public String getButtonAction() { return "ActionBarButton"; }

    @Override
    public String getButtonPanel() { return "ActionBar"; }

    public GridSnapButton(JPanel panel) {

        ScaleAnimIcon newGridSnap = createToggleScalingAnimatedIcon(GRID_SNAP_OFF_ICON, GRID_SNAP_ON_ICON, bGridSnapEnabled, 20, 20, 1.0f, 0.25f, 100);
        ScaleAnimIcon newGridSubSnap = createToggleScalingAnimatedIcon(SUB_SNAP_OFF_ICON, SUB_SNAP_ON_ICON, !bGridSnapSubs, 20, 20, 1.0f, 0.25f, 100);

        StateButton gridSnapButton = new StateButton((bGridSnapSubs) ? newGridSubSnap : newGridSnap, panel, null, null, bGridSnapEnabled, false, this);
        gridSnapButton.addState("ALT_1", (bGridSnapSubs) ? newGridSnap : newGridSubSnap, null, null);

        button = gridSnapButton;

        button.addMouseListener(new MouseAdapter() {
            @Override
            public void mouseReleased(MouseEvent e) {
                if (button.isEnabled()) {
                    if (SwingUtilities.isLeftMouseButton(e)) {
                        bGridSnapEnabled = button.isSelected();
                    }
                    if (SwingUtilities.isRightMouseButton(e)) {
                        bGridSnapSubs = !bGridSnapSubs;
                        gridSnapButton.swapIcon();
                    }
                }
                forceShowToolTip(button);
                updateTooltip();
            }
        });

        Shortcut gridSnapShortcut = getUserShortcutByID(TOGGLE_GRID_SNAPPING_SHORTCUT);
        if (gridSnapShortcut != null) {
            Action gridSnapAction = new AbstractAction() {
                @Override
                public void actionPerformed(ActionEvent e) {
                    if (button.isEnabled()) {
                        bGridSnapEnabled = !button.isSelected();
                        button.setSelected(bGridSnapEnabled);
                        if (button.getIcon() instanceof ScaleAnimIcon) {
                            ((ScaleAnimIcon)button.getIcon()).setSelected(bGridSnapEnabled);
                            ((ScaleAnimIcon) button.getIcon()).startAnimation(button);
                        }
                        updateTooltip();
                    }
                }
            };
            registerShortcut(this, gridSnapShortcut, gridSnapAction, getMapPanel());
        }

        Shortcut subSnapShortcut = getUserShortcutByID(TOGGLE_SUBDIVISION_SNAPPING_SHORTCUT);
        if (subSnapShortcut != null) {
            Action subSnapAction = new AbstractAction() {
                @Override
                public void actionPerformed(ActionEvent e) {
                    if (button.isEnabled()) {
                        bGridSnapSubs = !bGridSnapSubs;
                        gridSnapButton.swapIcon();
                        updateTooltip();
                    }
                }
            };
            registerShortcut(this, subSnapShortcut, subSnapAction, getMapPanel());
        }

    }

    @Override
    public String buildToolTip() {
        String snapCurrent = bGridSnapEnabled ? getLocaleString("actionbar_tooltip_common_on") : getLocaleString("actionbar_tooltip_common_off");
        String snapNext = bGridSnapEnabled ? getLocaleString("actionbar_tooltip_common_off").toLowerCase() : getLocaleString("actionbar_tooltip_common_on").toLowerCase();
        String modeCurrent = bGridSnapSubs ? getLocaleString("snapping_tooltip_status_snap_sub") : getLocaleString("snapping_tooltip_status_snap_grid");
        String nodeNext = bGridSnapSubs ? getLocaleString("snapping_tooltip_status_snap_grid") : getLocaleString("snapping_tooltip_status_snap_sub");

        String statusLine = getLocaleString("actionbar_options_grid_snap_status_tooltip").replace("{current}", snapCurrent).replace("{next}", snapNext);
        String changeLine = getLocaleString("actionbar_options_sub_snap_status_tooltip").replace("{current}", modeCurrent).replace("{next}", nodeNext);

        return String.format("<html>%s<hr>%s</html>", statusLine, changeLine);
    }
}
