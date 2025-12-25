package AutoDriveEditor.GUI.Buttons.ActionBar.ConfigPanel;

import AutoDriveEditor.Classes.UI_Components.DropdownToggleButton;
import AutoDriveEditor.Classes.UI_Components.PopoutJPanel;
import AutoDriveEditor.Classes.UI_Components.ScaleAnimIcon;
import AutoDriveEditor.GUI.Buttons.OptionsBaseButton;
import AutoDriveEditor.Managers.PopupManager;
import com.formdev.flatlaf.extras.FlatSVGIcon;
import com.formdev.flatlaf.extras.components.FlatSpinner;

import javax.swing.*;
import java.awt.event.ActionEvent;

import static AutoDriveEditor.AutoDriveEditor.getMapPanel;
import static AutoDriveEditor.Classes.Util_Classes.LoggerUtils.LOG;
import static AutoDriveEditor.Locale.LocaleManager.getLocaleString;
import static AutoDriveEditor.Managers.IconManager.*;
import static AutoDriveEditor.XMLConfig.AutoSave.*;
import static AutoDriveEditor.XMLConfig.EditorXML.autoSaveInterval;
import static AutoDriveEditor.XMLConfig.EditorXML.bAutoSaveEnabled;
import static javax.swing.JLayeredPane.POPUP_LAYER;

public class AutosaveButton extends OptionsBaseButton {

    private int tempAutoSaveInterval;
    private JButton acceptTimeButton;
    private PopoutJPanel popoutJPanel;

    @Override
    public String getButtonID() { return "AutosaveButton"; }

    @Override
    public String getButtonAction() { return "ActionBarButton"; }

    @Override
    public String getButtonPanel() { return "ActionBar"; }

    @Override
    public void actionPerformed(ActionEvent e) {
        super.actionPerformed(e);
        if (e.getSource() == button) {
            bAutoSaveEnabled = !bAutoSaveEnabled;
            if (bAutoSaveEnabled) {
                if (scheduledFuture != null) {
                    restartAutoSaveThread();
                } else {
                    startAutoSaveThread();
                }
            } else {
                stopAutoSaveThread();
            }
        }
        if (e.getSource() == acceptTimeButton) {
            autoSaveInterval = tempAutoSaveInterval;
            LOG.info("Setting new autosave interval to: {} minutes", autoSaveInterval);
            acceptTimeButton.setEnabled(false);
            popoutJPanel.playClosingAnimation();
            if (scheduledFuture != null) {
                restartAutoSaveThread();
            }
        }
        updateTooltip();
    }
    public AutosaveButton(JPanel panel) {
        ScaleAnimIcon animAutoSaveIcon = createToggleScalingAnimatedIcon(AUTOSAVE_OFF_ICON, AUTOSAVE_ON_ICON, bAutoSaveEnabled, 20, 20, 1.0f, .25f, 100);
        button = createAnimDropdownToggleButton(animAutoSaveIcon, panel, null, null, bAutoSaveEnabled, true, this);
        createPopupPanel();

        ((DropdownToggleButton) button).addDropdownButtonListener(event -> {
            if (popoutJPanel.isVisible()) {
                PopupManager.hidePopupPanel(button);
            } else {
                PopupManager.showPopupPanel(button);
            }

        });
    }

    private void createPopupPanel() {
        // createSetting the autosave popup Panel
        popoutJPanel = PopupManager.makePopupPanel(button, "actionbar_options_autosave_popup_tooltip");

        popoutJPanel.add(new JLabel(getLocaleString("actionbar_options_autosave_panel_interval_tooltip")), "center, span, gap 5, wrap");

        // createSetting a JSpinner for th modifying the autosave time interval
        JSpinner intervalSpinner = new FlatSpinner();
        SpinnerModel spinnerValue = new SpinnerNumberModel(autoSaveInterval, 5, 60, 1);
        intervalSpinner.setModel(spinnerValue);
        intervalSpinner.addChangeListener(e -> {
            JSpinner spinner1 = (JSpinner) e.getSource();
            tempAutoSaveInterval = (int) spinner1.getValue();
            acceptTimeButton.setEnabled(tempAutoSaveInterval != autoSaveInterval);
        });
        popoutJPanel.add(intervalSpinner, "center");

        // createSetting accept + apply button
        FlatSVGIcon acceptIcon = getSVGIcon(CONFIRM_ICON, 15, 15);
        ScaleAnimIcon animAcceptTimeIcon = createScaleAnimIcon(acceptIcon, false, 15 ,15);

//        ScaleAnimIcon animAcceptTimeIcon = createScaleAnimIcon(CONFIRM_ICON, false, 15 ,15);
        acceptTimeButton = createAnimButton(animAcceptTimeIcon, popoutJPanel, "actionbar_options_autosave_panel_confirm_tooltip", null, bAutoSaveEnabled, false, this);
        popoutJPanel.add(acceptTimeButton, "center");

        getMapPanel().add(popoutJPanel, POPUP_LAYER);
    }

    @Override
    public String buildToolTip() {
        String autoSaveCurrent = bAutoSaveEnabled ? getLocaleString("actionbar_tooltip_common_enabled") : getLocaleString("actionbar_tooltip_common_disabled");

        String statusLine = getLocaleString("actionbar_options_autosave_tooltip").replace("{current}", autoSaveCurrent);
        String intervalLine = getLocaleString("actionbar_options_autosave_interval_tooltip").replace("{current}", autoSaveInterval + "m");

        return String.format("<html>%s<hr>%s</html>", statusLine, intervalLine);
    }
}