package AutoDriveEditor.GUI.Buttons.ActionBar.ConfigPanel;

import AutoDriveEditor.Classes.KeyBinds.Shortcut;
import AutoDriveEditor.Classes.UI_Components.ScaleAnimIcon;
import AutoDriveEditor.GUI.Buttons.OptionsBaseButton;

import javax.swing.*;
import java.awt.event.ActionEvent;

import static AutoDriveEditor.AutoDriveEditor.getMapPanel;
import static AutoDriveEditor.GUI.TextPanel.showInTextArea;
import static AutoDriveEditor.Locale.LocaleManager.getLocaleString;
import static AutoDriveEditor.Managers.IconManager.*;
import static AutoDriveEditor.Managers.ShortcutManager.ShortcutID.TOGGLE_SELECT_HIDDEN_SHORTCUT;
import static AutoDriveEditor.Managers.ShortcutManager.getUserShortcutByID;
import static AutoDriveEditor.Managers.ShortcutManager.registerShortcut;
import static AutoDriveEditor.XMLConfig.EditorXML.bSelectHidden;

public class SelectHiddenButton extends OptionsBaseButton {

    @Override
    public String getButtonID() { return "SelectHiddenButton"; }

    @Override
    public String getButtonAction() { return "ActionBarButton"; }

    @Override
    public String getButtonPanel() { return "ActionBar"; }

    @Override
    public void actionPerformed(ActionEvent e) {
        super.actionPerformed(e);
        bSelectHidden = !bSelectHidden;
        setSelected(bSelectHidden);

        updateTooltip();
        forceShowToolTip(button);
        showInTextArea(getToolTip(),true,false);
    }

    public SelectHiddenButton(JPanel panel) {

        ScaleAnimIcon animSelectHiddenIcon = createToggleScalingAnimatedIcon(SELECT_HIDDEN_OFF_ICON, SELECT_HIDDEN_ON_ICON, bSelectHidden, 20, 20, 1.0f, .25f, 100);
        button = createAnimToggleButton(animSelectHiddenIcon, panel, null, null, bSelectHidden, false, this);
//        button.addMouseListener(new MouseAdapter() {
//            @Override
//            public void mouseClicked(MouseEvent e) {
//                if (button.isEnabled()) {
//                    bSelectHidden = !bSelectHidden;
//                }
//                forceShowToolTip(button);
//                updateTooltip();
//            }
//        });

        Shortcut showHiddenShortcut = getUserShortcutByID(TOGGLE_SELECT_HIDDEN_SHORTCUT);
        if (showHiddenShortcut != null) {
            Action showHiddenAction = new AbstractAction() {
                @Override
                public void actionPerformed(ActionEvent e) {
                    if (button.isEnabled()) {
                        bSelectHidden = !bSelectHidden;
                        button.setSelected(bSelectHidden);
                        if (button.getIcon() instanceof ScaleAnimIcon) {
                            ((ScaleAnimIcon)button.getIcon()).setSelected(bSelectHidden);
                            ((ScaleAnimIcon) button.getIcon()).startAnimation(button);
                        }
                        getMapPanel().repaint();
                        updateTooltip();
                    }
                }
            };
            registerShortcut(this, showHiddenShortcut, showHiddenAction, getMapPanel());
        }
    }

    @Override
    public String buildToolTip() {
        String selectCurrent = bSelectHidden ? getLocaleString("actionbar_tooltip_common_on") : getLocaleString("actionbar_tooltip_common_off");
        String statusLine = getLocaleString("actionbar_options_display_hidden_tooltip").replace("{status}", selectCurrent);
        return String.format("<html>%s</html>", statusLine);
    }
}
