package AutoDriveEditor.GUI;

import javax.swing.*;
import javax.swing.border.EmptyBorder;
import java.awt.*;
import java.awt.event.*;

import static AutoDriveEditor.AutoDriveEditor.EXPERIMENTAL;
import static AutoDriveEditor.GUI.GUIBuilder.*;
import static AutoDriveEditor.GUI.GUIImages.tractorImage;
import static AutoDriveEditor.Locale.LocaleManager.localeString;
import static AutoDriveEditor.MapPanel.MapPanel.*;
import static AutoDriveEditor.Utils.GUIUtils.makeBasicButton;
import static AutoDriveEditor.Utils.GUIUtils.makeCheckBox;
import static AutoDriveEditor.Utils.LoggerUtils.LOG;
import static AutoDriveEditor.XMLConfig.EditorXML.*;

public class ConfigGUI extends JFrame {
    public static ConfigGUI configGUI;
    public static boolean isConfigWindowOpen = false;
    private static final int WIN_WIDTH = 450;
    private static final int WIN_HEIGHT = 300;

    Boolean bTempAutoSaveEnabled = bAutoSaveEnabled;
    Integer tempAutoSaveInterval = autoSaveInterval;
    Integer tempMaxAutoSaveSlots = maxAutoSaveSlots;

    JButton applyNewAutoSaveSettingsNow;
    JButton applyNewAutoSaveSettingsLater;


    public static void  createConfigGUI(Component comp) {
        SwingUtilities.invokeLater(() -> showConfigGUI(comp));
        isConfigWindowOpen = true;
        openConfig.setEnabled(false);
    }

    public static void showConfigGUI(Component comp) {
        if (configGUI != null) configGUI.dispatchEvent(new WindowEvent(configGUI, WindowEvent.WINDOW_CLOSING));
        //configListener = new ConfigListener();
        configGUI = new ConfigGUI();
        configGUI.addWindowListener(new WindowAdapter() {
            @Override
            public void windowClosed(WindowEvent e) {
                isConfigWindowOpen = false;
                openConfig.setSelected(false);
                openConfig.setEnabled(true);
                super.windowClosed(e);
            }
        });
        configGUI.setDefaultCloseOperation(JFrame.DISPOSE_ON_CLOSE);
        configGUI.setTitle(localeString.getString("panel_config_gui_title"));
        configGUI.setIconImage(tractorImage);
        configGUI.setResizable(false);
        configGUI.pack();
        configGUI.setLocationRelativeTo(comp);
        configGUI.setVisible(true);
    }

    @Override
    public Dimension getPreferredSize() {
        return new Dimension(WIN_WIDTH, WIN_HEIGHT);
    }


    public ConfigGUI() {
        super();

        JTabbedPane configTabPane = new JTabbedPane(SwingConstants.TOP);

        JPanel gui = new JPanel(new BorderLayout(5, 5));

        //
        // MapPanel Tab
        //

        JPanel mapPanelTab = new JPanel();
        configTabPane.addTab(localeString.getString("panel_config_tab_mappanel"), null, mapPanelTab, localeString.getString("panel_config_tab_mappanel_tooltip"));
        JPanel mapPanelOptions = new JPanel(new GridLayout(4,2,0,5));

        // Use online images checkbox

        JLabel enableOnlineImagesLabel = new JLabel(localeString.getString("panel_config_tab_mappanel_online_images") + "  ", JLabel.TRAILING);
        JCheckBox cbEnableOnlineImages = makeCheckBox(enableOnlineImagesLabel, "OnlineImages", null, true, bUseOnlineMapImages);
        cbEnableOnlineImages.addItemListener(e -> bUseOnlineMapImages = e.getStateChange() == ItemEvent.SELECTED);
        enableOnlineImagesLabel.setLabelFor(cbEnableOnlineImages);
        mapPanelOptions.add(enableOnlineImagesLabel);
        mapPanelOptions.add(cbEnableOnlineImages);

        // Middle mouse button move checkbox

        JLabel middleMouseMoveLabel = new JLabel(localeString.getString("panel_config_tab_mappanel_middle_mouse_move") + "  ", JLabel.TRAILING);
        JCheckBox cbMiddleMouseMove = makeCheckBox(middleMouseMoveLabel, "MiddleMouseMove", null, true, bMiddleMouseMove);
        cbMiddleMouseMove.addItemListener(e -> bMiddleMouseMove = e.getStateChange() == ItemEvent.SELECTED);
        enableOnlineImagesLabel.setLabelFor(cbEnableOnlineImages);
        mapPanelOptions.add(middleMouseMoveLabel);
        mapPanelOptions.add(cbMiddleMouseMove);

        // Maximum zoom level text field and buttons

        JPanel zoomPanel = new JPanel();
        zoomPanel.setBorder(new EmptyBorder(new Insets(7,0,0,0)));
        zoomPanel.setLayout(new FlowLayout(FlowLayout.LEADING));

        JLabel maxZoomLabel = new JLabel(localeString.getString("panel_config_tab_mappanel_max_zoom") + "  ", JLabel.TRAILING);

        JTextField maxZoomTextField = new JTextField(String.valueOf(maxZoomLevel));
        maxZoomTextField.setEditable(false);
        maxZoomTextField.setColumns(6);
        maxZoomTextField.setPreferredSize(new Dimension(80, 20));
        maxZoomTextField.setHorizontalAlignment(JTextField.LEADING);
        zoomPanel.add(maxZoomTextField);

        JButton plusButton = makeBasicButton(null,"panel_config_tab_mappanel_max_zoom_plus_tooltip","panel_config_tab_mappanel_max_zoom_plus", zoomPanel, true, true);
        plusButton.setMargin(new Insets(0,5,0,5));
        Timer plusButtonTimer = new Timer(50, e -> {
            maxZoomLevel++;
            maxZoomTextField.setText(String.valueOf(maxZoomLevel));
        });
        plusButtonTimer.setCoalesce(true);
        plusButtonTimer.setRepeats(true);

        plusButton.getModel().addChangeListener(e -> {
            if (plusButton.getModel().isPressed()) {
                plusButtonTimer.start();
            } else {
                plusButtonTimer.stop();
            }
        });

        JButton minusButton = makeBasicButton(null,"panel_config_tab_mappanel_max_zoom_minus_tooltip","panel_config_tab_mappanel_max_zoom_minus", zoomPanel, true, true);
        minusButton.setMargin(new Insets(0,7,0,7));
        Timer minusButtonTimer = new Timer(50, e -> {
            maxZoomLevel--;
            maxZoomTextField.setText(String.valueOf(maxZoomLevel));
        });
        minusButtonTimer.setCoalesce(true);
        minusButtonTimer.setRepeats(true);

        minusButton.getModel().addChangeListener(e -> {
            if (minusButton.getModel().isPressed()) {
                minusButtonTimer.start();
            } else {
                minusButtonTimer.stop();
            }
        });

        maxZoomLabel.setLabelFor(maxZoomTextField);
        mapPanelOptions.add(maxZoomLabel);

        mapPanelOptions.add(zoomPanel);

        // Linear line spacing slider

        String lineLabelText = localeString.getString("panel_config_tab_mappanel_linearline_label") + " ( " + linearLineNodeDistance + "m )  ";

        JLabel linearLineLengthLabel = new JLabel(lineLabelText, JLabel.TRAILING);
        JSlider linearLineLengthSlider = new JSlider(SwingConstants.HORIZONTAL);

        linearLineLengthSlider.addChangeListener(e -> {
            JSlider source = (JSlider) e.getSource();
            if (source.getValueIsAdjusting()) {
                if (linearLineLengthSlider.getValue() > 5 ) {
                    linearLineNodeDistance = linearLineLengthSlider.getValue();
                } else {
                    linearLineNodeDistance = 5;
                    linearLineLengthSlider.setValue(5);
                }
                String text = localeString.getString("panel_config_tab_mappanel_linearline_label") + " ( " + linearLineNodeDistance + "m )  ";
                linearLineLengthLabel.setText(text);
            }
        });

        // a keyboard listener for JSliders to stop the arrow keys
        // movement, we just consume the event and do nothing

        linearLineLengthSlider.addKeyListener(new KeyListener() {
            @Override
            public void keyTyped(KeyEvent e) {e.consume();}

            @Override
            public void keyPressed(KeyEvent e) {e.consume();}

            @Override
            public void keyReleased(KeyEvent e) {e.consume();}
        });

        linearLineLengthSlider.setPreferredSize(new Dimension(185,20));
        linearLineLengthSlider.setBorder(BorderFactory.createEmptyBorder(0,0,0,0));
        linearLineLengthSlider.setPaintTicks(true);
        linearLineLengthSlider.setSnapToTicks(false);
        linearLineLengthSlider.setMajorTickSpacing(10);
        linearLineLengthSlider.setMinorTickSpacing(5);
        linearLineLengthSlider.setPaintLabels(true);
        linearLineLengthSlider.setMinimum(0);
        linearLineLengthSlider.setMaximum(50);
        linearLineLengthSlider.setValue(linearLineNodeDistance);

        mapPanelOptions.add(linearLineLengthLabel);
        mapPanelOptions.add(linearLineLengthSlider);

        mapPanelTab.add(mapPanelOptions);

        //
        // autoSaveTab Tab
        //

        JPanel autoSaveTab = new JPanel();
        autoSaveTab.setLayout(new BoxLayout(autoSaveTab,BoxLayout.Y_AXIS));
        configTabPane.addTab(localeString.getString("panel_config_tab_autosave"), null, autoSaveTab, localeString.getString("panel_config_tab_autosave_tooltip"));
        JPanel autoSaveOptions = new JPanel(new GridLayout(3,2,0,0));

        // AutoSave enable checkbox

        JLabel enabledAutoSaveLabel = new JLabel(localeString.getString("panel_config_tab_autosave_enabled") + "  ", JLabel.TRAILING);
        JCheckBox cbEnableAutoSave = makeCheckBox(enabledAutoSaveLabel, "AutoSaveEnabled", null, true, bAutoSaveEnabled);
        cbEnableAutoSave.addItemListener(e -> {
            bTempAutoSaveEnabled = e.getStateChange() == ItemEvent.SELECTED;
            LOG.info("AutoSave = {}", bTempAutoSaveEnabled);
            checkIfThreadRestartButtonNeedsEnabling();
        });
        enabledAutoSaveLabel.setLabelFor(cbEnableAutoSave);
        autoSaveOptions.add(enabledAutoSaveLabel);
        autoSaveOptions.add(cbEnableAutoSave);

        // Autosave interval slider

        JLabel autoSaveIntervalLabel = new JLabel(localeString.getString("panel_config_tab_autosave_interval") + "  ", JLabel.TRAILING);
        JSlider autoSaveIntervalSlider = new JSlider(SwingConstants.HORIZONTAL);
        autoSaveIntervalSlider.addMouseListener(new MouseListener() {
            @Override
            public void mouseClicked(MouseEvent e) {}

            @Override
            public void mousePressed(MouseEvent e) {}

            @Override
            public void mouseReleased(MouseEvent e) {
                if (autoSaveIntervalSlider.getValue() > 5 ) {
                    tempAutoSaveInterval = autoSaveIntervalSlider.getValue();
                } else {
                    tempAutoSaveInterval = 5;
                    autoSaveIntervalSlider.setValue(5);
                }
                LOG.info("AutoSave Interval set to {}", tempAutoSaveInterval);
                checkIfThreadRestartButtonNeedsEnabling();
            }

            @Override
            public void mouseEntered(MouseEvent e) {}

            @Override
            public void mouseExited(MouseEvent e) {}
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

        JLabel maxAutoSaveSlotLabel = new JLabel(localeString.getString("panel_config_tab_autosave_maxslot") + "  ", JLabel.TRAILING);
        JSlider slMaxAutoSaveSlot = new JSlider(SwingConstants.HORIZONTAL);
        slMaxAutoSaveSlot.addChangeListener(e -> {
            JSlider source = (JSlider) e.getSource();
            if (source.getValueIsAdjusting()) {
                tempMaxAutoSaveSlots = source.getValue();
                LOG.info("Max AutoSave Slots = {}", tempMaxAutoSaveSlots);
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
        autoSaveTab.add(autoSaveOptions);

        JPanel applyPanel = new JPanel();
        applyPanel.setLayout(new BoxLayout(applyPanel, BoxLayout.X_AXIS));
        applyPanel.setBorder(BorderFactory.createEmptyBorder(10,10,10,10));
        applyNewAutoSaveSettingsNow = makeBasicButton(null, "panel_config_tab_autosave_apply_now_tooltip", "panel_config_tab_autosave_apply_now", applyPanel, false, true);
        applyPanel.add(Box.createHorizontalGlue());
        applyNewAutoSaveSettingsLater = makeBasicButton(null, "panel_config_tab_autosave_apply_later_tooltip", "panel_config_tab_autosave_apply_later", applyPanel, false, true);
        applyNewAutoSaveSettingsNow.addActionListener(e -> {
            applyNewAutoSaveSettingsNow.setEnabled(false);
            applyNewAutoSaveSettingsLater.setEnabled(false);
            bAutoSaveEnabled = bTempAutoSaveEnabled;
            autoSaveInterval = tempAutoSaveInterval;
            maxAutoSaveSlots = tempMaxAutoSaveSlots;
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
        autoSaveTab.add(applyPanel);

        //
        // Curve Options Tab
        //

        JPanel curvesTab = new JPanel();
        curvesTab.setLayout(new BoxLayout(curvesTab,BoxLayout.Y_AXIS));
        configTabPane.addTab(localeString.getString("panel_config_tab_curves"), null, curvesTab, localeString.getString("panel_config_tab_curves_tooltip"));
        JPanel curveOptions = new JPanel(new GridLayout(3,2,0,0));

        // Curve nodes maximum

        JLabel maxCurveNodesLabel = new JLabel(localeString.getString("panel_config_tab_curves_maxnodes") + " ( " + curveSliderMax + " )  ", JLabel.TRAILING);
        JSlider maxCurveNodesSlider = new JSlider(SwingConstants.HORIZONTAL);



        JLabel defaultCurveNodesLabel = new JLabel(localeString.getString("panel_config_tab_curves_defaultnodes") + " ( " + curveSliderDefault + " )  ", JLabel.TRAILING);
        JSlider defaultCurveNodesSlider = new JSlider(SwingConstants.HORIZONTAL);

        maxCurveNodesSlider.addChangeListener(e -> {
            JSlider source = (JSlider) e.getSource();
            if (source.getValueIsAdjusting()) {
                int maxNodeValue = maxCurveNodesSlider.getValue();
                if (maxNodeValue < 1 ) maxNodeValue = 1;
                curveSliderMax = maxNodeValue;
                if (curveSliderDefault > curveSliderMax) {
                    curveSliderDefault = curveSliderMax;
                    defaultCurveNodesSlider.setValue(curveSliderDefault);
                    String text = localeString.getString("panel_config_tab_curves_defaultnodes") + " ( " + curveSliderDefault + " )  ";
                    defaultCurveNodesLabel.setText(text);
                }
                numIterationsSlider.setMaximum(curveSliderMax);
                String text = localeString.getString("panel_config_tab_curves_maxnodes") + " ( " + curveSliderMax + " )  ";
                maxCurveNodesLabel.setText(text);
            }
        });

        maxCurveNodesSlider.setBorder(BorderFactory.createEmptyBorder(0,0,0,0));
        maxCurveNodesSlider.setPaintTicks(true);
        maxCurveNodesSlider.setSnapToTicks(false);
        maxCurveNodesSlider.setMajorTickSpacing(5);
        maxCurveNodesSlider.setPaintLabels(true);
        maxCurveNodesSlider.setMinimum(0);
        maxCurveNodesSlider.setMaximum(50);
        maxCurveNodesSlider.setValue(curveSliderMax);

        curveOptions.add(maxCurveNodesLabel);
        curveOptions.add(maxCurveNodesSlider);
        curvesTab.add(curveOptions);

        // Curve nodes default

        defaultCurveNodesSlider.addChangeListener(e -> {
            JSlider source = (JSlider) e.getSource();
            if (source.getValueIsAdjusting()) {
                int defaultValue = defaultCurveNodesSlider.getValue();
                if (defaultValue > curveSliderMax) defaultValue = curveSliderMax;
                if (defaultValue < 1) defaultValue = 1;
                curveSliderDefault = defaultValue;
                defaultCurveNodesSlider.setValue(curveSliderDefault);
                String text = localeString.getString("panel_config_tab_curves_defaultnodes") + " ( " + curveSliderDefault + " )  ";
                defaultCurveNodesLabel.setText(text);
            }
        });

        defaultCurveNodesSlider.setBorder(BorderFactory.createEmptyBorder(0,0,0,0));
        defaultCurveNodesSlider.setPaintTicks(true);
        defaultCurveNodesSlider.setSnapToTicks(false);
        defaultCurveNodesSlider.setMajorTickSpacing(5);
        defaultCurveNodesSlider.setPaintLabels(true);
        defaultCurveNodesSlider.setMinimum(0);
        defaultCurveNodesSlider.setMaximum(50);
        defaultCurveNodesSlider.setValue(curveSliderDefault);

        curveOptions.add(defaultCurveNodesLabel);
        curveOptions.add(defaultCurveNodesSlider);
        curvesTab.add(curveOptions);

        // control point movement scaler

        JLabel curveControlPointScalerLabel = new JLabel(localeString.getString("panel_config_tab_curves_controlpointscaler") + "  ", JLabel.TRAILING);
        JSlider curveControlPointScalerSlider = new JSlider(SwingConstants.HORIZONTAL);

        curveControlPointScalerSlider.addChangeListener(e -> {
            JSlider source = (JSlider) e.getSource();
            if (source.getValueIsAdjusting()) {
                int defaultValue = curveControlPointScalerSlider.getValue();
                if (defaultValue < 1) defaultValue = 1;
                controlPointMoveScaler = defaultValue;
                curveControlPointScalerSlider.setValue(controlPointMoveScaler);
            }
        });

        curveControlPointScalerSlider.setBorder(BorderFactory.createEmptyBorder(0,0,0,0));
        curveControlPointScalerSlider.setPaintTicks(true);
        curveControlPointScalerSlider.setSnapToTicks(true);
        curveControlPointScalerSlider.setMajorTickSpacing(1);
        curveControlPointScalerSlider.setPaintLabels(true);
        curveControlPointScalerSlider.setMinimum(0);
        curveControlPointScalerSlider.setMaximum(10);
        curveControlPointScalerSlider.setValue(controlPointMoveScaler);

        curveOptions.add(curveControlPointScalerLabel);
        curveOptions.add(curveControlPointScalerSlider);
        curvesTab.add(curveOptions);


        //
        // Experimental tab
        //

        if (EXPERIMENTAL) {
            JComponent experimentalTab = new JPanel(new GridLayout(3,2,0,0));
            configTabPane.addTab("Experimental", null, experimentalTab, "Experimental");

            gui.add(configTabPane);
            configTabPane.setTabLayoutPolicy(JTabbedPane.SCROLL_TAB_LAYOUT);
        }

        gui.add(configTabPane);
        add(gui);
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
