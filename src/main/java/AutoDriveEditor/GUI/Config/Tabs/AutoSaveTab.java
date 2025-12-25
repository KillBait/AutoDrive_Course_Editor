package AutoDriveEditor.GUI.Config.Tabs;

import com.formdev.flatlaf.extras.FlatSVGIcon;
import net.miginfocom.swing.MigLayout;

import javax.swing.*;
import java.awt.*;
import java.awt.event.ItemEvent;

import static AutoDriveEditor.Classes.Util_Classes.GUIUtils.makeBasicButton;
import static AutoDriveEditor.Classes.Util_Classes.GUIUtils.makeCheckBox;
import static AutoDriveEditor.Classes.Util_Classes.LoggerUtils.LOG;
import static AutoDriveEditor.GUI.Menus.DebugMenu.Logging.LogConfigGUIInfoMenu.bDebugLogConfigGUIInfo;
import static AutoDriveEditor.Locale.LocaleManager.getLocaleString;
import static AutoDriveEditor.Managers.IconManager.*;
import static AutoDriveEditor.XMLConfig.AutoSave.*;
import static AutoDriveEditor.XMLConfig.EditorXML.*;

public class AutoSaveTab extends JPanel {

    private boolean bTempAutoSaveEnabled = bAutoSaveEnabled;
    private int tempAutoSaveInterval = autoSaveInterval;
    private int tempMaxAutoSaveSlots = maxAutoSaveSlots;

    private final JButton applyNewAutoSaveSettingsNow;
    private final JButton applyNewAutoSaveSettingsLater;

    public AutoSaveTab() {

        // Set the layout of the main panel
        setLayout(new MigLayout("center, insets 10 30 0 30", "[]", "[]10[]15[]15[]30[]"));

        // Autosave Label

        JLabel autosaveLabel = new JLabel(getLocaleString("panel_config_tab_autosave_autosave"));
        add(autosaveLabel, "center, wrap");

        // Toggle Autosave Panel

        JPanel toggleAutosavePanel = new JPanel(new MigLayout("center"));
        toggleAutosavePanel.putClientProperty("FlatLaf.style", "border: 0,0,0,0,@disabledForeground,1,16; background: darken($Panel.background,5%)");
        FlatSVGIcon autosaveIcon = getSVGIcon(AUTOSAVE_ICON);
        JLabel autosaveIconLabel = new JLabel(autosaveIcon);


        // AutoSave Enabled checkbox

        JLabel enabledAutoSaveLabel = new JLabel(getLocaleString("panel_config_tab_autosave_enabled"), JLabel.TRAILING);
        JCheckBox cbEnableAutoSave = makeCheckBox(enabledAutoSaveLabel, "AutoSaveEnabled", null, true, bAutoSaveEnabled);
        cbEnableAutoSave.addItemListener(e -> {
            bTempAutoSaveEnabled = e.getStateChange() == ItemEvent.SELECTED;
            if (bDebugLogConfigGUIInfo) LOG.info("AutoSave = {}", bTempAutoSaveEnabled);
            checkIfThreadRestartButtonNeedsEnabling();
        });
        //enabledAutoSaveLabel.setLabelFor(cbEnableAutoSave);
        toggleAutosavePanel.add(autosaveIconLabel, "gap 0 20 0 0");
        toggleAutosavePanel.add(enabledAutoSaveLabel);
        toggleAutosavePanel.add(cbEnableAutoSave);

        add(toggleAutosavePanel, "center, grow, wrap");

        // Slider Panel

        JLabel sliderLabel = new JLabel(getLocaleString("panel_config_tab_autosave_schedule"));
        add(sliderLabel, "center, wrap");

        JPanel mapPanel = new JPanel(new MigLayout("center, gap 0 0 0 0"));
        mapPanel.putClientProperty("FlatLaf.style", "border: 0,0,10,0,@disabledForeground,1,16; background: darken($Panel.background,5%)");

        FlatSVGIcon timeIcon = getSVGIcon(TIME_ICON);
        timeIcon.setColorFilter(FlatSVGIcon.ColorFilter.getInstance()
                .add(new Color(66, 139, 193), null, new Color(193, 148, 0))
                .add(new Color(245, 245, 245,255), null, new Color(245, 245, 245,40))
                .add(new Color(229, 229, 229,255), null, new Color(229, 229, 229, 30)));
        JLabel timeIconLabel = new JLabel(timeIcon);
        // Autosave interval slider

        JLabel autoSaveIntervalLabel = new JLabel(getLocaleString("panel_config_tab_autosave_interval"));
        JSlider autoSaveIntervalSlider = new JSlider(SwingConstants.HORIZONTAL);
        JTextField autoSaveIntervalTextField = new JTextField();
        autoSaveIntervalTextField.setText(autoSaveInterval + " Mins");
        autoSaveIntervalTextField.setEditable(false);


        autoSaveIntervalSlider.addChangeListener(e -> {
            JSlider source = (JSlider) e.getSource();
            if (source.getValueIsAdjusting()) {
                int intervalValue = autoSaveIntervalSlider.getValue();
                tempAutoSaveInterval = Math.max(intervalValue, 5);
                //autoSaveIntervalSlider.setValue(tempAutoSaveInterval);
                String labelText = tempAutoSaveInterval + " Mins";
                autoSaveIntervalTextField.setText(labelText);
                checkIfThreadRestartButtonNeedsEnabling();
            }
        });

        autoSaveIntervalSlider.setBorder(BorderFactory.createEmptyBorder(0,0,0,0));
        //autoSaveIntervalSlider.setPaintTicks(true);
        autoSaveIntervalSlider.setSnapToTicks(true);
        autoSaveIntervalSlider.setMajorTickSpacing(10);
        autoSaveIntervalSlider.setMinorTickSpacing(5);
        //autoSaveIntervalSlider.setPaintLabels(true);
        autoSaveIntervalSlider.setMinimum(0);
        autoSaveIntervalSlider.setMaximum(60);
        autoSaveIntervalSlider.setValue(autoSaveInterval);



        mapPanel.add(timeIconLabel, "span 1 4, gap 20 20 0 0");
        mapPanel.add(autoSaveIntervalLabel, "center, wrap");
        mapPanel.add(autoSaveIntervalSlider);
        mapPanel.add(autoSaveIntervalTextField, "center, wrap");
        //optionsPanel.add(mapPanel, "wrap");


        // Maximum autosave slots slider

        JLabel maxAutoSaveSlotLabel = new JLabel(getLocaleString("panel_config_tab_autosave_maxslot"));
        JSlider slMaxAutoSaveSlot = new JSlider(SwingConstants.HORIZONTAL);
        JTextField autoSaveSlotsTextField = new JTextField();
        autoSaveSlotsTextField.setText(maxAutoSaveSlots + " Slots");
        autoSaveSlotsTextField.setEditable(false);

        slMaxAutoSaveSlot.addChangeListener(e -> {
            JSlider source = (JSlider) e.getSource();
            if (source.getValueIsAdjusting()) {
                tempMaxAutoSaveSlots = source.getValue();
                if (bDebugLogConfigGUIInfo) LOG.info("Max AutoSave Slots = {}", tempMaxAutoSaveSlots);
                autoSaveSlotsTextField.setText(tempMaxAutoSaveSlots + " Slots");
                checkIfThreadRestartButtonNeedsEnabling();
            }
        });
        slMaxAutoSaveSlot.setBorder(BorderFactory.createEmptyBorder(0,0,0,0));
        //slMaxAutoSaveSlot.setPaintTicks(true);
        slMaxAutoSaveSlot.setSnapToTicks(true);
        slMaxAutoSaveSlot.setMajorTickSpacing(1);
        //slMaxAutoSaveSlot.setPaintLabels(true);
        slMaxAutoSaveSlot.setMinimum(1);
        slMaxAutoSaveSlot.setMaximum(10);
        slMaxAutoSaveSlot.setValue(maxAutoSaveSlots);

        mapPanel.add(maxAutoSaveSlotLabel, "center, wrap");
        mapPanel.add(slMaxAutoSaveSlot);
        mapPanel.add(autoSaveSlotsTextField, "center, wrap");


        JPanel applyPanel = new JPanel(new MigLayout("center"));
        applyPanel.putClientProperty("FlatLaf.style", "border: 10,10,10,10,@disabledForeground,1,16; background: darken($Panel.background,5%)");
        applyNewAutoSaveSettingsNow = makeBasicButton(null, "panel_config_tab_autosave_apply_now_tooltip", "panel_config_tab_autosave_apply_now", applyPanel, false, true);
        applyNewAutoSaveSettingsLater = makeBasicButton(null, "panel_config_tab_autosave_apply_later_tooltip", "panel_config_tab_autosave_apply_later", applyPanel, false, true);

        applyNewAutoSaveSettingsNow.addActionListener(e -> {
            applyNewAutoSaveSettingsNow.setEnabled(false);
            applyNewAutoSaveSettingsLater.setEnabled(false);
            bAutoSaveEnabled = bTempAutoSaveEnabled;
            autoSaveInterval = tempAutoSaveInterval;
            maxAutoSaveSlots = tempMaxAutoSaveSlots;
            //updateAutosaveStatusLabel(bAutoSaveEnabled);
            if (bAutoSaveEnabled) {
                if (scheduledFuture != null) {
                    restartAutoSaveThread();
                } else {
                    startAutoSaveThread();
                }
            } else {
                stopAutoSaveThread();
            }

        });

        applyNewAutoSaveSettingsLater.addActionListener(e -> {
            LOG.info("Apply new settings on restart");
            applyNewAutoSaveSettingsNow.setEnabled(false);
            applyNewAutoSaveSettingsLater.setEnabled(false);
            bAutoSaveEnabled = bTempAutoSaveEnabled;
            autoSaveInterval = tempAutoSaveInterval;
            maxAutoSaveSlots = tempMaxAutoSaveSlots;
        });

        add(mapPanel, "center, grow, wrap");

        JPanel buttonPanel = new JPanel(new MigLayout());
        buttonPanel.putClientProperty("FlatLaf.style", "border: 10,10,10,10");

        buttonPanel.add(new JLabel(""), "dock center, grow");
        buttonPanel.add(applyNewAutoSaveSettingsNow, "dock west");
        buttonPanel.add(applyNewAutoSaveSettingsLater, "dock east");

        add(buttonPanel, "grow");


    }

    private void checkIfThreadRestartButtonNeedsEnabling() {
        if ((bTempAutoSaveEnabled != bAutoSaveEnabled) || (tempAutoSaveInterval != autoSaveInterval) || (tempMaxAutoSaveSlots != maxAutoSaveSlots)) {
            applyNewAutoSaveSettingsNow.setEnabled(true);
            applyNewAutoSaveSettingsLater.setEnabled(true);
        } else {
            applyNewAutoSaveSettingsNow.setEnabled(false);
            applyNewAutoSaveSettingsLater.setEnabled(false);
        }
    }
}
