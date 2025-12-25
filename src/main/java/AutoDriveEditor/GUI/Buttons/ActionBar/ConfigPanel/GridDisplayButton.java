package AutoDriveEditor.GUI.Buttons.ActionBar.ConfigPanel;

import AutoDriveEditor.Classes.KeyBinds.Shortcut;
import AutoDriveEditor.Classes.UI_Components.DropdownToggleButton;
import AutoDriveEditor.Classes.UI_Components.PopoutJPanel;
import AutoDriveEditor.Classes.UI_Components.PropertyChangeNumberFilter;
import AutoDriveEditor.Classes.UI_Components.ScaleAnimIcon;
import AutoDriveEditor.GUI.Buttons.OptionsBaseButton;
import AutoDriveEditor.Managers.PopupManager;
import AutoDriveEditor.RoadNetwork.RoadMap;
import AutoDriveEditor.XMLConfig.EditorXML;
import com.formdev.flatlaf.extras.components.FlatRadioButton;
import com.formdev.flatlaf.ui.FlatLineBorder;
import net.miginfocom.swing.MigLayout;

import javax.swing.*;
import javax.swing.border.TitledBorder;
import javax.swing.text.PlainDocument;
import java.awt.*;
import java.awt.event.ActionEvent;
import java.util.Collections;

import static AutoDriveEditor.AutoDriveEditor.getMapPanel;
import static AutoDriveEditor.Classes.Util_Classes.LoggerUtils.LOG;
import static AutoDriveEditor.GUI.MapPanel.*;
import static AutoDriveEditor.Locale.LocaleManager.getLocaleString;
import static AutoDriveEditor.Managers.IconManager.*;
import static AutoDriveEditor.Managers.ShortcutManager.ShortcutID.TOGGLE_SHOW_GRID_SHORTCUT;
import static AutoDriveEditor.Managers.ShortcutManager.getUserShortcutByID;
import static AutoDriveEditor.Managers.ShortcutManager.registerShortcut;
import static AutoDriveEditor.XMLConfig.EditorXML.*;
import static AutoDriveEditor.XMLConfig.EditorXML.GRID_TYPE.*;

public class GridDisplayButton extends OptionsBaseButton {

    private PopoutJPanel popoutJPanel;
    private static ButtonGroup gridDefaultGroup;
    private static JTextField cordX;
    private static JTextField cordY;
    private static JTextField cordS;
    private static JButton applyCustomButton;

    @Override
    public String getButtonID() { return "DisplayGridButton"; }

    @Override
    public String getButtonAction() { return "ActionBarButton"; }

    @Override
    public String getButtonPanel() { return "ActionBar"; }

    @Override
    public void actionPerformed(ActionEvent e) {
        super.actionPerformed(e);
        bShowGrid = !bShowGrid;
        setCurrentTooltip();
        getMapPanel().repaint();
        forceShowToolTip(button);
    }

    public GridDisplayButton(JPanel panel) {
        ScaleAnimIcon animConConnectIcon = createToggleScalingAnimatedIcon(GRID_OFF_ICON, GRID_ON_ICON, bShowGrid, 20, 20, 1.0f, .25f, 200);
        button = createAnimDropdownToggleButton(animConConnectIcon, panel, null, null, bShowGrid, false, this);
        setCurrentTooltip();
        createPopoutPanel();

        Shortcut showGridShortcut = getUserShortcutByID(TOGGLE_SHOW_GRID_SHORTCUT);
        if (showGridShortcut != null) {
            Action showGridAction = new AbstractAction() {
                @Override
                public void actionPerformed(ActionEvent e) {
                    if (button.isEnabled()) {
                        bShowGrid = !bShowGrid;
                        button.setSelected(bShowGrid);
                        if (button.getIcon() instanceof ScaleAnimIcon) {
                            ((ScaleAnimIcon)button.getIcon()).setSelected(bShowGrid);
                            ((ScaleAnimIcon) button.getIcon()).startAnimation(button);
                        }
                        getMapPanel().repaint();
                        updateTooltip();
                    }
                }
            };
            registerShortcut(this, showGridShortcut, showGridAction, getMapPanel());
        }

        ((DropdownToggleButton) button).addDropdownButtonListener(event -> {
                if (popoutJPanel.isVisible()) {
                    PopupManager.hidePopupPanel(button);
                } else {
                    PopupManager.showPopupPanel(button);
                }

        });
    }

    private void createPopoutPanel() {
        popoutJPanel = PopupManager.makePopupPanel(button, "actionbar_options_grid_popup_tooltip");

        JPanel defaultPanel = new JPanel(new MigLayout("insets 5, gap 0"));
        popoutJPanel.add(defaultPanel, "span, center");
        gridDefaultGroup = new ButtonGroup();

        addRadioButton(defaultPanel, gridDefaultGroup, "actionbar_options_grid_default_1x1_button_tooltip", GRID_1x1, 1, 1);
        addRadioButton(defaultPanel, gridDefaultGroup, "actionbar_options_grid_default_2x2_button_tooltip", GRID_2x2, 2, 2);
        addRadioButton(defaultPanel, gridDefaultGroup, "actionbar_options_grid_default_4x4_button_tooltip", GRID_4x4, 4, 4);
        addRadioButton(defaultPanel, gridDefaultGroup, "actionbar_options_grid_default_custom_button_tooltip", GRID_CUSTOM, -1, -1);

        JPanel customPanel = new JPanel(new MigLayout("insets 5, gap 5"));
        customPanel.setBorder(new TitledBorder(new FlatLineBorder(new Insets(0,0,0,0), UIManager.getColor("Component.borderColor"), 2, 12), "Custom", TitledBorder.CENTER, TitledBorder.TOP));
        customPanel.setBackground(popoutJPanel.getBackgroundColor());

        cordX = createTextField(String.valueOf(gridSpacingX), TEXT_X_ICON, 1, 2048 * mapScale, "actionbar_options_grid_panel_text_x_tooltip");
        customPanel.add(cordX, "wrap");

        cordY = createTextField(String.valueOf(gridSpacingY), TEXT_Y_ICON, 1, 2048 * mapScale, "actionbar_options_grid_panel_text_y_tooltip");
        customPanel.add(cordY, "wrap");

        cordS = createTextField(String.valueOf(gridSubDivisions), TEXT_S_ICON, 1, 32, "actionbar_options_grid_panel_text_s_tooltip");
        customPanel.add(cordS, "wrap");

        applyCustomButton = new JButton(getLocaleString("actionbar_options_grid_default_custom_apply"));
        applyCustomButton.addActionListener(e -> {
            LOG.info("Applying Custom Grid Values ( X: {} , Y: {} , S: {} )", cordX.getText(), cordY.getText(), cordS.getText());
            gridType = GRID_CUSTOM;
            gridSpacingX = (cordX == null || cordX.getText().isEmpty()) ? 1 : Float.parseFloat(cordX.getText());
            gridSpacingY = (cordY == null || cordY.getText().isEmpty()) ? 1 : Float.parseFloat(cordY.getText());
            gridSubDivisions = (cordS == null || cordS.getText().isEmpty()) ? 1 : Integer.parseInt(cordS.getText());
            LOG.info("ApplyButton --> Updating stored map Info for {}", RoadMap.mapName);
            for (MapInfoStore store : knownMapList) {
                if (store.getMapName().equals(RoadMap.mapName)) {
                    store.setGridSettings(gridType, gridSpacingX, gridSpacingY, gridSubDivisions);
                    //gridDefault = GRID.GRID_CUSTOM;
                    break;
                }
            }
            //setNewGridValues(gridType, gridSpacingX, gridSpacingY, gridSubDivisions);
            getMapPanel().repaint();
        });
        customPanel.add(applyCustomButton, "center");

        popoutJPanel.add(customPanel, "center");

        getMapPanel().add(popoutJPanel, POPUP_LAYER);
    }

    private void addRadioButton(JPanel panel, ButtonGroup group, String tooltipKey, GRID_TYPE gridType, int spacingX, int spacingY) {
        FlatRadioButton radioButton = new FlatRadioButton();
        radioButton.setText(getLocaleString(tooltipKey));
        radioButton.setToolTipText(getLocaleString(tooltipKey));
        radioButton.addItemListener(e -> {
            if (radioButton.isSelected()) {
                if (gridType != GRID_CUSTOM) {
                    gridSpacingX = spacingX;
                    gridSpacingY = spacingY;
                    gridSubDivisions = 1;
                    cordX.setEnabled(false);
                    cordY.setEnabled(false);
                    cordS.setEnabled(false);
                    applyCustomButton.setEnabled(false);
                    for (MapInfoStore store : knownMapList) {
                        if (store.getMapName().equals(RoadMap.mapName)) {
                            store.setGridSettings(gridType, spacingX, spacingY, 1);
                            break;
                        }
                    }
                } else {
                    cordX.setEnabled(true);
                    cordY.setEnabled(true);
                    cordS.setEnabled(true);
                    applyCustomButton.setEnabled(true);
                }
                getMapPanel().repaint();
            }
        });
        group.add(radioButton);
        panel.add(radioButton, "wrap");
    }

    @SuppressWarnings("SameParameterValue")
    private JTextField createTextField(String text, String iconKey, int min, int max, String tooltip) {
        JTextField textField = new JTextField(text);
        if (EditorXML.gridType != GRID_CUSTOM) {
            textField.setEnabled(false);
        }
        textField.putClientProperty("JTextField.leadingIcon", getSVGIcon(iconKey));
        PropertyChangeNumberFilter filter = new PropertyChangeNumberFilter(min, max, true, 2, false);
        filter.addPropertyChangeListener(evt -> handleTextFieldError(evt, textField));
        PlainDocument doc = (PlainDocument) textField.getDocument();
        doc.setDocumentFilter(filter);
        textField.setToolTipText(getLocaleString(tooltip));
        return textField;
    }

    private void handleTextFieldError(java.beans.PropertyChangeEvent evt, JTextField textField) {
        String errorText = (String) evt.getNewValue();
        if (!errorText.isEmpty()) {

            textField.putClientProperty("JComponent.outline", "error");
            Timer timer = new Timer(1000, e -> {
                textField.putClientProperty("JComponent.outline", "none");
                ((Timer) e.getSource()).stop();
            });
            timer.setRepeats(false);
            timer.start();
        } else {
            textField.putClientProperty("JComponent.outline", "none");
        }
    }

    public static void updateGridPanelSettings(GRID_TYPE type, float x, float y, int s) {
        if (cordX != null && cordY != null && cordS != null) {
            // Find the radio button associated with the GRID_TYPE and select it
            int num = 0;
            for (AbstractButton button : Collections.list(gridDefaultGroup.getElements())) {
                if (num == type.ordinal()) {
                    gridDefaultGroup.setSelected(button.getModel(), true);
                    break;
                }
                num++;
            }
            cordX.setText(String.valueOf(x));
            cordY.setText(String.valueOf(y));
            cordS.setText(String.valueOf(s));
            boolean enabled = (type == GRID_CUSTOM);
            cordX.setEnabled(enabled);
            cordY.setEnabled(enabled);
            cordS.setEnabled(enabled);
            applyCustomButton.setEnabled(enabled);
        }
    }

    private void setCurrentTooltip() {
        String initTooltip = buildToolTip();
        button.setToolTipText(initTooltip);
        button.getAccessibleContext().setAccessibleDescription(initTooltip);
    }

    @Override
    public String buildToolTip() {
        String connectCurrent = bShowGrid ? getLocaleString("actionbar_tooltip_common_on") : getLocaleString("actionbar_tooltip_common_off");
        return getLocaleString("actionbar_options_grid_status_tooltip").replace("{current}", connectCurrent);
    }
}