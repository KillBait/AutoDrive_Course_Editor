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
import java.awt.event.ActionListener;

import static AutoDriveEditor.AutoDriveEditor.getMapPanel;
import static AutoDriveEditor.Classes.Util_Classes.LoggerUtils.LOG;
import static AutoDriveEditor.Locale.LocaleManager.getLocaleString;
import static AutoDriveEditor.Managers.IconManager.*;
import static AutoDriveEditor.XMLConfig.EditorXML.bRotationSnapEnabled;
import static AutoDriveEditor.XMLConfig.EditorXML.rotationStep;
import static javax.swing.JLayeredPane.POPUP_LAYER;

public class RotationSnapButton extends OptionsBaseButton {

    private int tempRotationStep;
    private JButton acceptStepButton;
    private PopoutJPanel snapPopoutJPanel;

    @Override
    public String getButtonID() { return "RotationSnapButton"; }

    @Override
    public String getButtonAction() { return "ActionBarButton"; }

    @Override
    public String getButtonPanel() { return "ActionBar"; }

    @Override
    public void actionPerformed(ActionEvent e) {
        super.actionPerformed(e);
        if (e.getSource() == button) {
            bRotationSnapEnabled = !bRotationSnapEnabled;
            acceptStepButton.setEnabled(false);
        } else if (e.getSource() == acceptStepButton) {
            rotationStep = tempRotationStep;
            acceptStepButton.setEnabled(false);
        }

        acceptStepButton.addActionListener(new ActionListener() {
            @Override
            public void actionPerformed(ActionEvent e) {
                rotationStep = tempRotationStep;
                acceptStepButton.setEnabled(false);
            }
        });


        updateTooltip();
        forceShowToolTip(button);
    }

    public RotationSnapButton(JPanel panel) {
        ScaleAnimIcon animAutoSaveIcon = createToggleScalingAnimatedIcon(ROTATE_SNAP_OFF_ICON, ROTATE_SNAP_ON_ICON, bRotationSnapEnabled, 20, 20, 1.0f, .25f, 100);
        button = createAnimDropdownToggleButton(animAutoSaveIcon, panel, null, null, bRotationSnapEnabled, false, this);
        createPopupPanel();

        ((DropdownToggleButton) button).addDropdownButtonListener(event -> {
            if (snapPopoutJPanel.isVisible()) {
                PopupManager.hidePopupPanel(button);
            } else {
                PopupManager.showPopupPanel(button);
            }

        });
    }

    private void createPopupPanel() {
        // createSetting the autosave popup Panel
        snapPopoutJPanel = PopupManager.makePopupPanel(button, "actionbar_options_rotation_popup_title");

        snapPopoutJPanel.add(new JLabel(getLocaleString("actionbar_options_rotation_panel_text_tooltip")), "center, span, gap 5, wrap");

        // createSetting a JSpinner for th modifying the autosave time interval
        JSpinner degreeSpinner = new FlatSpinner();
        SpinnerModel degreeValue = new SpinnerNumberModel(rotationStep, 5, 90, 1);
        degreeSpinner.setModel(degreeValue);
        degreeSpinner.addChangeListener(e -> {
            JSpinner spinner1 = (JSpinner) e.getSource();
            tempRotationStep = (int) spinner1.getValue();
            acceptStepButton.setEnabled(tempRotationStep != rotationStep);
        });
        snapPopoutJPanel.add(degreeSpinner, "center");

        // createSetting accept + apply button
        FlatSVGIcon acceptStep = getSVGIcon(ROTATE_CONFIRM_ICON, 15, 15);
        ScaleAnimIcon animAcceptStepIcon = createScaleAnimIcon(acceptStep, false, 15 ,15);

//        ScaleAnimIcon animAcceptTimeIcon = createScaleAnimIcon(CONFIRM_ICON, false, 15 ,15);
        acceptStepButton = createAnimButton(animAcceptStepIcon, snapPopoutJPanel, "actionbar_options_autosave_panel_confirm_tooltip", null, false, false, this);
        snapPopoutJPanel.add(acceptStepButton, "center");

        getMapPanel().add(snapPopoutJPanel, POPUP_LAYER);
    }

    @Override
    public String buildToolTip() {
        String rotationSnapCurrent = bRotationSnapEnabled ? getLocaleString("actionbar_tooltip_common_enabled") : getLocaleString("actionbar_tooltip_common_disabled");
        String statusLine = getLocaleString("actionbar_options_rotation_tooltip").replace("{current}", rotationSnapCurrent);

        return String.format("<html>%s</html>", statusLine);
    }
}