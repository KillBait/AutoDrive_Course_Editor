package AutoDriveEditor.GUI.Config.Tabs;

import javax.swing.*;
import java.awt.*;
import java.awt.event.ItemEvent;

import static AutoDriveEditor.GUI.Menus.DebugMenu.Logging.LogConfigGUIInfoMenu.bDebugLogConfigGUIInfo;
import static AutoDriveEditor.GUI.TextPanel.updateAutosaveStatusLabel;
import static AutoDriveEditor.Locale.LocaleManager.getLocaleString;
import static AutoDriveEditor.Utils.GUIUtils.makeBasicButton;
import static AutoDriveEditor.Utils.GUIUtils.makeCheckBox;
import static AutoDriveEditor.Utils.LoggerUtils.LOG;
import static AutoDriveEditor.XMLConfig.AutoSave.*;
import static AutoDriveEditor.XMLConfig.EditorXML.*;
import static javax.swing.BoxLayout.X_AXIS;

public class AutoSaveTab extends JPanel {

    private boolean bTempAutoSaveEnabled = bAutoSaveEnabled;
    private int tempAutoSaveInterval = autoSaveInterval;
    private int tempMaxAutoSaveSlots = maxAutoSaveSlots;

    private final JButton applyNewAutoSaveSettingsNow;
    private final JButton applyNewAutoSaveSettingsLater;

    public AutoSaveTab() {

        setLayout(new BoxLayout( this, BoxLayout.Y_AXIS));


        JPanel autoSaveOptions = new JPanel(new GridLayout(3,2,0,10));

        // AutoSave Enabled checkbox

        JLabel enabledAutoSaveLabel = new JLabel(getLocaleString("panel_config_tab_autosave_enabled") + "  ", JLabel.TRAILING);
        JCheckBox cbEnableAutoSave = makeCheckBox(enabledAutoSaveLabel, "AutoSaveEnabled", null, true, bAutoSaveEnabled);
        cbEnableAutoSave.addItemListener(e -> {
            bTempAutoSaveEnabled = e.getStateChange() == ItemEvent.SELECTED;
            if (bDebugLogConfigGUIInfo) LOG.info("AutoSave = {}", bTempAutoSaveEnabled);
            checkIfThreadRestartButtonNeedsEnabling();
        });
        enabledAutoSaveLabel.setLabelFor(cbEnableAutoSave);
        autoSaveOptions.add(enabledAutoSaveLabel);
        autoSaveOptions.add(cbEnableAutoSave);

        // Autosave interval slider

        JLabel autoSaveIntervalLabel = new JLabel(getLocaleString("panel_config_tab_autosave_interval") + " ("+ autoSaveInterval+")" + "  ", JLabel.TRAILING);
        JSlider autoSaveIntervalSlider = new JSlider(SwingConstants.HORIZONTAL);

        autoSaveIntervalSlider.addChangeListener(e -> {
            JSlider source = (JSlider) e.getSource();
            if (source.getValueIsAdjusting()) {
                int intervalValue = autoSaveIntervalSlider.getValue();
                tempAutoSaveInterval = Math.max(intervalValue, 5);
                autoSaveIntervalSlider.setValue(tempAutoSaveInterval);
                String text = getLocaleString("panel_config_tab_autosave_interval") + " (" + tempAutoSaveInterval + ")  ";
                autoSaveIntervalLabel.setText(text);
                checkIfThreadRestartButtonNeedsEnabling();
            }
        });

        autoSaveIntervalSlider.setBorder(BorderFactory.createEmptyBorder(0,0,0,0));
        autoSaveIntervalSlider.setPaintTicks(true);
        autoSaveIntervalSlider.setSnapToTicks(true);
        autoSaveIntervalSlider.setMajorTickSpacing(10);
        autoSaveIntervalSlider.setMinorTickSpacing(5);
        autoSaveIntervalSlider.setPaintLabels(true);
        autoSaveIntervalSlider.setMinimum(0);
        autoSaveIntervalSlider.setMaximum(60);
        autoSaveIntervalSlider.setValue(autoSaveInterval);

        autoSaveOptions.add(autoSaveIntervalLabel);
        autoSaveOptions.add(autoSaveIntervalSlider);


        // Maximum autosave slots slider

        JLabel maxAutoSaveSlotLabel = new JLabel(getLocaleString("panel_config_tab_autosave_maxslot") + "  ", JLabel.TRAILING);
        JSlider slMaxAutoSaveSlot = new JSlider(SwingConstants.HORIZONTAL);
        slMaxAutoSaveSlot.addChangeListener(e -> {
            JSlider source = (JSlider) e.getSource();
            if (source.getValueIsAdjusting()) {
                tempMaxAutoSaveSlots = source.getValue();
                if (bDebugLogConfigGUIInfo) LOG.info("Max AutoSave Slots = {}", tempMaxAutoSaveSlots);
                checkIfThreadRestartButtonNeedsEnabling();
            }
        });
        slMaxAutoSaveSlot.setBorder(BorderFactory.createEmptyBorder(0,0,0,0));
        slMaxAutoSaveSlot.setPaintTicks(true);
        slMaxAutoSaveSlot.setSnapToTicks(true);
        slMaxAutoSaveSlot.setMajorTickSpacing(1);
        slMaxAutoSaveSlot.setPaintLabels(true);
        slMaxAutoSaveSlot.setMinimum(1);
        slMaxAutoSaveSlot.setMaximum(10);
        slMaxAutoSaveSlot.setValue(maxAutoSaveSlots);

        autoSaveOptions.add(maxAutoSaveSlotLabel);
        autoSaveOptions.add(slMaxAutoSaveSlot);
        add(autoSaveOptions);

        JPanel applyPanel = new JPanel();
        applyPanel.setLayout(new BoxLayout(applyPanel, X_AXIS));
        applyPanel.setBorder(BorderFactory.createEmptyBorder(10,10,10,20));
        applyNewAutoSaveSettingsNow = makeBasicButton(null, "panel_config_tab_autosave_apply_now_tooltip", "panel_config_tab_autosave_apply_now", applyPanel, false, true);
        applyPanel.add(Box.createHorizontalGlue());
        applyNewAutoSaveSettingsLater = makeBasicButton(null, "panel_config_tab_autosave_apply_later_tooltip", "panel_config_tab_autosave_apply_later", applyPanel, false, true);

        applyNewAutoSaveSettingsNow.addActionListener(e -> {
            applyNewAutoSaveSettingsNow.setEnabled(false);
            applyNewAutoSaveSettingsLater.setEnabled(false);
            bAutoSaveEnabled = bTempAutoSaveEnabled;
            autoSaveInterval = tempAutoSaveInterval;
            maxAutoSaveSlots = tempMaxAutoSaveSlots;
            updateAutosaveStatusLabel(bAutoSaveEnabled);
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

        add(applyPanel);
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
