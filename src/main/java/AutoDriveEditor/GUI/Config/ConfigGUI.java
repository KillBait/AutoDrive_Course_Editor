package AutoDriveEditor.GUI.Config;

import AutoDriveEditor.Utils.Classes.EventTriggerNumberFilter;
import com.bric.colorpicker.ColorPicker;

import javax.swing.*;
import javax.swing.border.EmptyBorder;
import javax.swing.border.TitledBorder;
import javax.swing.event.ChangeListener;
import javax.swing.text.PlainDocument;
import java.awt.*;
import java.awt.event.*;
import java.util.Enumeration;

import static AutoDriveEditor.GUI.Buttons.Options.OpenConfigButton.configListener;
import static AutoDriveEditor.GUI.Config.ColourPreviewPanel.*;
import static AutoDriveEditor.GUI.GUIBuilder.numIterationsSlider;
import static AutoDriveEditor.GUI.GUIImages.getTractorImage;
import static AutoDriveEditor.GUI.MenuBuilder.bDebugLogGUIInfo;
import static AutoDriveEditor.Locale.LocaleManager.getLocaleString;
import static AutoDriveEditor.MapPanel.MapPanel.*;
import static AutoDriveEditor.Utils.GUIUtils.*;
import static AutoDriveEditor.Utils.LoggerUtils.LOG;
import static AutoDriveEditor.XMLConfig.EditorXML.*;
import static javax.swing.BoxLayout.X_AXIS;

public class ConfigGUI extends JFrame {
    public static ConfigGUI configGUI;
    public static boolean isConfigWindowOpen = false;
    private static final int WIN_WIDTH = 450;
    private static final int WIN_HEIGHT = 300;

    private boolean bTempAutoSaveEnabled = bAutoSaveEnabled;
    private int tempAutoSaveInterval = autoSaveInterval;
    private int tempMaxAutoSaveSlots = maxAutoSaveSlots;

    private final JButton applyNewAutoSaveSettingsNow;
    private final JButton applyNewAutoSaveSettingsLater;

    private JTextField redTextField;
    private JTextField greenTextField;
    private JTextField blueTextField;

    public static ColorPicker colorPicker;
    public static ButtonGroup pathGroup;
    public static JPanel previewPanel;
    public static JPanel connectionPanel;
    public static JPanel nodePanel;

    public static JRadioButton regularNode;
    public static JRadioButton subprioNode;
    public static JRadioButton regularConnection;
    public static JRadioButton subprioConnection;
    public static JRadioButton dualConnection;
    public static JRadioButton reverseConnection;

    public static void  createConfigGUI(Component comp) {
        SwingUtilities.invokeLater(() -> showConfigGUI(comp));
        isConfigWindowOpen = true;
    }

    public static void showConfigGUI(Component comp) {
        if (configGUI != null) configGUI.dispatchEvent(new WindowEvent(configGUI, WindowEvent.WINDOW_CLOSING));
        configGUI = new ConfigGUI();
        configGUI.addWindowListener(new WindowAdapter() {
            @Override
            public void windowClosed(WindowEvent e) {
                isConfigWindowOpen = false;
                super.windowClosed(e);
                getMapPanel().repaint();
            }
        });
        configGUI.setDefaultCloseOperation(JFrame.DISPOSE_ON_CLOSE);
        configGUI.setTitle(getLocaleString("panel_config_gui_title"));
        configGUI.setIconImage(getTractorImage());
        configGUI.setResizable(false);
        configGUI.pack();
        configGUI.setLocationRelativeTo(comp);
        configGUI.setVisible(true);
        configGUI.setAlwaysOnTop(false);
    }

    @Override
    public Dimension getPreferredSize() {
        return new Dimension(WIN_WIDTH, WIN_HEIGHT);
    }


    public ConfigGUI() {
        super();

        JTabbedPane configTabPane = new JTabbedPane(SwingConstants.TOP);
        configTabPane.setTabLayoutPolicy(JTabbedPane.SCROLL_TAB_LAYOUT);

        JPanel gui = new JPanel(new BorderLayout(5, 5));

        //
        // MapPanel Tab
        //

        JPanel mapPanelTab = new JPanel();
        configTabPane.addTab(getLocaleString("panel_config_tab_mappanel"), null, mapPanelTab, getLocaleString("panel_config_tab_mappanel_tooltip"));
        JPanel mapPanelOptions = new JPanel(new GridLayout(4,2,0,5));

        // Use online images checkbox

        JLabel enableOnlineImagesLabel = new JLabel(getLocaleString("panel_config_tab_mappanel_online_images") + "  ", JLabel.TRAILING);
        JCheckBox cbEnableOnlineImages = makeCheckBox(enableOnlineImagesLabel, "OnlineImages", null, true, bUseOnlineMapImages);
        cbEnableOnlineImages.addItemListener(e -> bUseOnlineMapImages = e.getStateChange() == ItemEvent.SELECTED);
        enableOnlineImagesLabel.setLabelFor(cbEnableOnlineImages);
        mapPanelOptions.add(enableOnlineImagesLabel);
        mapPanelOptions.add(cbEnableOnlineImages);

        // Middle mouse button move checkbox

        JLabel middleMouseMoveLabel = new JLabel(getLocaleString("panel_config_tab_mappanel_middle_mouse_move") + "  ", JLabel.TRAILING);
        JCheckBox cbMiddleMouseMove = makeCheckBox(middleMouseMoveLabel, "MiddleMouseMove", null, true, bMiddleMouseMove);
        cbMiddleMouseMove.addItemListener(e -> bMiddleMouseMove = e.getStateChange() == ItemEvent.SELECTED);
        enableOnlineImagesLabel.setLabelFor(cbEnableOnlineImages);
        mapPanelOptions.add(middleMouseMoveLabel);
        mapPanelOptions.add(cbMiddleMouseMove);

        // Maximum zoom level text field and buttons

        JPanel zoomPanel = new JPanel();
        zoomPanel.setBorder(new EmptyBorder(new Insets(7,0,0,0)));
        zoomPanel.setLayout(new FlowLayout(FlowLayout.LEADING));

        JLabel maxZoomLabel = new JLabel(getLocaleString("panel_config_tab_mappanel_max_zoom") + "  ", JLabel.TRAILING);

        /*SpinnerModel spinnerValue = new SpinnerNumberModel(20, 1, 50, 1);
        JSpinner spinner = new JSpinner(spinnerValue);
        spinner.setSize(80,20);
        zoomPanel.add(spinner);*/

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
        mapPanelTab.add(mapPanelOptions);

        //
        // autoSaveTab Tab
        //

        JPanel autoSaveTab = new JPanel();
        autoSaveTab.setLayout(new BoxLayout(autoSaveTab,BoxLayout.Y_AXIS));
        configTabPane.addTab(getLocaleString("panel_config_tab_autosave"), null, autoSaveTab, getLocaleString("panel_config_tab_autosave_tooltip"));
        JPanel autoSaveOptions = new JPanel(new GridLayout(3,2,0,0));

        // AutoSave setEnabled checkbox

        JLabel enabledAutoSaveLabel = new JLabel(getLocaleString("panel_config_tab_autosave_enabled") + "  ", JLabel.TRAILING);
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

        JLabel autoSaveIntervalLabel = new JLabel(getLocaleString("panel_config_tab_autosave_interval") + "  ", JLabel.TRAILING);
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

        JLabel maxAutoSaveSlotLabel = new JLabel(getLocaleString("panel_config_tab_autosave_maxslot") + "  ", JLabel.TRAILING);
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
        applyPanel.setLayout(new BoxLayout(applyPanel, X_AXIS));
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
        configTabPane.addTab(getLocaleString("panel_config_tab_curves"), null, curvesTab, getLocaleString("panel_config_tab_curves_tooltip"));
        JPanel curveOptions = new JPanel(new GridLayout(3,2,0,0));

        // Curve nodes maximum

        JLabel maxCurveNodesLabel = new JLabel(getLocaleString("panel_config_tab_curves_maxnodes") + " ( " + curveSliderMax + " )  ", JLabel.TRAILING);
        JSlider maxCurveNodesSlider = new JSlider(SwingConstants.HORIZONTAL);

        JLabel defaultCurveNodesLabel = new JLabel(getLocaleString("panel_config_tab_curves_defaultnodes") + " ( " + curveSliderDefault + " )  ", JLabel.TRAILING);
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
                    String text = getLocaleString("panel_config_tab_curves_defaultnodes") + " ( " + curveSliderDefault + " )  ";
                    defaultCurveNodesLabel.setText(text);
                }
                numIterationsSlider.setMaximum(curveSliderMax);
                String text = getLocaleString("panel_config_tab_curves_maxnodes") + " ( " + curveSliderMax + " )  ";
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
                String text = getLocaleString("panel_config_tab_curves_defaultnodes") + " ( " + curveSliderDefault + " )  ";
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

        JLabel curveControlPointScalerLabel = new JLabel(getLocaleString("panel_config_tab_curves_controlpointscaler") + "  ", JLabel.TRAILING);
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
        // Linear Line Tab
        //

        JPanel linearLineTab = new JPanel();
        //linearLineTab.setLayout(new BoxLayout(linearLineTab,BoxLayout.Y_AXIS));
        configTabPane.addTab(getLocaleString("panel_config_tab_linearline"), null, linearLineTab, getLocaleString("panel_config_tab_linearline_tooltip"));
        JPanel linearLineOptions = new JPanel(new GridLayout(3,2,0,5));

        // linear line arrows

        JLabel enableFilledArrowsLabel = new JLabel(getLocaleString("panel_config_tab_linearline_filledarrows") + "  ", JLabel.TRAILING);
        JCheckBox cbEnableFilledArrows = makeCheckBox(enableFilledArrowsLabel, "FilledArrows", null, true, bFilledArrows);
        cbEnableFilledArrows.addItemListener(e -> bFilledArrows = e.getStateChange() == ItemEvent.SELECTED);
        enableFilledArrowsLabel.setLabelFor(cbEnableFilledArrows);
        linearLineOptions.add(enableFilledArrowsLabel);
        linearLineOptions.add(cbEnableFilledArrows);

        // Linear line end node creation

        JLabel createLinearLineEndNodeLabel = new JLabel(getLocaleString("panel_config_tab_linearline_create_endnode") + "  ", JLabel.TRAILING);
        JCheckBox cbCreateLinearLineEndNode = makeCheckBox(createLinearLineEndNodeLabel, "CreateEndNode", null, true, bCreateLinearLineEndNode);
        cbCreateLinearLineEndNode.addItemListener(e -> bCreateLinearLineEndNode = e.getStateChange() == ItemEvent.SELECTED);
        createLinearLineEndNodeLabel.setLabelFor(cbCreateLinearLineEndNode);
        linearLineOptions.add(createLinearLineEndNodeLabel);
        linearLineOptions.add(cbCreateLinearLineEndNode);

        // Linear line spacing slider

        String lineLabelText = getLocaleString("panel_config_tab_linearline_linespacing_label") + " ( " + linearLineNodeDistance + "m )  ";

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
                String text = getLocaleString("panel_config_tab_mappanel_linearline_label") + " ( " + linearLineNodeDistance + "m )  ";
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

        linearLineLengthSlider.setPreferredSize(new Dimension(185,40));
        linearLineLengthSlider.setBorder(BorderFactory.createEmptyBorder(0,0,0,0));
        linearLineLengthSlider.setPaintTicks(true);
        linearLineLengthSlider.setSnapToTicks(false);
        linearLineLengthSlider.setMajorTickSpacing(10);
        linearLineLengthSlider.setMinorTickSpacing(5);
        linearLineLengthSlider.setPaintLabels(true);
        linearLineLengthSlider.setMinimum(0);
        linearLineLengthSlider.setMaximum(50);
        linearLineLengthSlider.setValue(linearLineNodeDistance);

        linearLineOptions.add(linearLineLengthLabel);
        linearLineOptions.add(linearLineLengthSlider);
        linearLineTab.add(linearLineOptions);


        //
        // Colours tab
        //

        JPanel experimentalPanelTab = new JPanel(new GridBagLayout());
        experimentalPanelTab.setComponentOrientation(ComponentOrientation.LEFT_TO_RIGHT);
        configTabPane.addTab(getLocaleString("panel_config_tab_colours"), null, experimentalPanelTab, getLocaleString("panel_config_tab_colours_tooltip"));
        GridBagConstraints gbc = new GridBagConstraints();

        // Colour picker

        JPanel picker = new JPanel();

        gbc.fill = GridBagConstraints.BOTH;
        gbc.anchor = GridBagConstraints.FIRST_LINE_START;
        gbc.weightx = 1;
        gbc.weighty = 1;
        gbc.ipady = 120;
        gbc.gridx = 0;
        gbc.gridy = 0;
        gbc.gridwidth = 2;
        gbc.gridheight = 2;

        experimentalPanelTab.add(picker, gbc);

        // R G B text input

        JPanel rgbSelectPanel = new JPanel();
        rgbSelectPanel.setLayout(new BoxLayout(rgbSelectPanel, BoxLayout.Y_AXIS));

        gbc.fill = GridBagConstraints.BOTH;
        gbc.anchor = GridBagConstraints.LAST_LINE_START;
        gbc.weightx = 0;
        gbc.weighty = 0;
        gbc.ipady = 0;
        gbc.gridx = 0;
        gbc.gridy = 2;
        gbc.gridwidth = 2;
        gbc.gridheight = 1;

        experimentalPanelTab.add(rgbSelectPanel, gbc);

        // path and node type selection

        JPanel selectionPanel = new JPanel();
        selectionPanel.setLayout(new BoxLayout(selectionPanel, BoxLayout.Y_AXIS));

        gbc.insets = new Insets(0,-20,0,0);
        gbc.fill = GridBagConstraints.BOTH;
        gbc.anchor = GridBagConstraints.FIRST_LINE_END;
        gbc.weightx = 0;
        gbc.weighty = 0;
        gbc.ipady = 0;
        gbc.gridx = 2;
        gbc.gridy = 0;
        gbc.gridwidth = 1;
        gbc.gridheight = 2;
        experimentalPanelTab.add(selectionPanel, gbc);

        // preview panel

        previewPanel = new ColourPreviewPanel();
        TitledBorder previewBorder = BorderFactory.createTitledBorder(BorderFactory.createSoftBevelBorder(3)/*, getLocaleString("panel_config_tab_colours_preview")*/);
        previewBorder.setTitleJustification(TitledBorder.CENTER);
        previewPanel.setBorder(previewBorder);

        gbc.insets = new Insets(0,5,0,25);
        gbc.fill = GridBagConstraints.BOTH;
        gbc.anchor = GridBagConstraints.LAST_LINE_END;
        gbc.weightx = 0.5;
        gbc.weighty = 1;
        gbc.ipady = 0;
        gbc.gridx = 2;
        gbc.gridy = 2;
        gbc.gridwidth = 1;
        gbc.gridheight = 1;

        experimentalPanelTab.add(previewPanel, gbc);

        // Colour picker

        colorPicker = new ColorPicker(false, false);
        colorPicker.setPreferredSize(new Dimension(200,180));
        colorPicker.setHSBControlsVisible(false);
        colorPicker.setPreviewSwatchVisible(false);
        colorPicker.setColor(Color.BLUE);
        colorPicker.addColorListener(colorModel -> {
            // update the preview panel
            Color newColor = new Color(colorModel.getColor().getRGB());

            //
            // update all the text boxes with the new values
            // NOTE: the .setText will trigger the changeListener for each textfield box
            //

            redTextField.setText(String.valueOf(colorModel.getRed()));
            greenTextField.setText(String.valueOf(colorModel.getGreen()));
            blueTextField.setText(String.valueOf(colorModel.getBlue()));

            String actionCommand = getButtonActionCommand(pathGroup);
            if (bDebugLogGUIInfo) LOG.info(" Action = {}", actionCommand);
            switch (actionCommand) {
                case "NODE_REGULAR":
                    colourNodeRegular = newColor;
                    setPreviewNodeColour(newColor);
                    break;
                case "NODE_SUBPRIO":
                    colourNodeSubprio = newColor;
                    setPreviewNodeColour(newColor);
                    break;
                case "NODE_SELECTED":
                    colourNodeSelected = newColor;
                    break;
                case "NODE_CONTROL":
                    colourNodeControl = newColor;
                    setPreviewControlNodeColour(newColor);
                    break;
                case "CONNECT_REGULAR":
                    colourConnectRegular = newColor;
                    setPreviewConnectionColour(newColor, false);
                    break;
                case "CONNECT_SUBPRIO":
                    colourConnectSubprio = newColor;
                    setPreviewConnectionColour(newColor, false);
                    break;
                case "CONNECT_DUAL":
                    colourConnectDual = newColor;
                    setPreviewConnectionColour(newColor, true);
                    break;
                case "CONNECT_DUAL_SUBPRIO":
                    colourConnectDualSubprio = newColor;
                    setPreviewConnectionColour(newColor, true);
                    break;
                case "CONNECT_REVERSE":
                    colourConnectReverse = newColor;
                    setPreviewConnectionColour(newColor, false);
                    break;
                case "CONNECT_REVERSE_SUBPRIO":
                    colourConnectReverseSubprio = newColor;
                    setPreviewConnectionColour(newColor, false);
                    break;
            }
            previewPanel.repaint();
        });

        picker.add(colorPicker);

        //
        // RGB input text boxes
        //

        JPanel inputPanel = new JPanel();

        JLabel redLabel = new JLabel( "R ", JLabel.TRAILING);
        redTextField = new JTextField(String.valueOf(255));
        redTextField.setPreferredSize(new Dimension(30, 20));
        redTextField.setMaximumSize(new Dimension(30, 20));
        redTextField.setHorizontalAlignment(JTextField.LEADING);

        //  WARNING .. this changeListener approach causes un-intended effects
        //
        // If you manually adjust a value in a text field, this will cause
        // a ChangeEvent being triggered for that text box, resulting in
        // it's ChangeListener triggering.
        //
        // Intended :- The ChangeListener will update the color wheel to the new value
        //
        // Un-Intended:- As we just updated the color wheel, the color wheel
        // ColorChanged event will trigger and update all the text fields, causing
        // the text boxes ChangeEvent being trigger again.
        //
        // example...
        //
        // 1) change a value of the red text box.
        // 2) red text boxes causes a ChangeEvent, triggering its ChangeListener, updating color wheel
        // 3) color wheels ColorChange updates all the textboxes, causing an ChangeEvent for each one
        //
        //
        // This is not a horrible bug, I can live with it since all we get is 1 unwanted triggering
        // that has no real effect and the user won't notice it.


        ChangeListener redColourChangeListener = e -> {
            Color redAdjust = new Color((int) e.getSource(), Integer.parseInt(greenTextField.getText()), Integer.parseInt(blueTextField.getText()));
            updateColorWheel(redAdjust);
        };

        PlainDocument docRed = (PlainDocument) redTextField.getDocument();
        docRed.setDocumentFilter(new EventTriggerNumberFilter(0, 255, false, false, redColourChangeListener));

        inputPanel.add(redLabel);
        inputPanel.add(redTextField);

        JLabel greenLabel = new JLabel( " G ", JLabel.TRAILING);
        greenTextField = new JTextField(String.valueOf(255));
        greenTextField.setPreferredSize(new Dimension(30, 20));
        greenTextField.setMaximumSize(new Dimension(30, 20));
        greenTextField.setHorizontalAlignment(JTextField.LEADING);

        ChangeListener greenColourChangeListener = e -> {
            Color greenAdjust = new Color(Integer.parseInt(redTextField.getText()), (int)e.getSource(), Integer.parseInt(blueTextField.getText()));
            updateColorWheel(greenAdjust);
        };

        PlainDocument docGreen = (PlainDocument) greenTextField.getDocument();
        docGreen.setDocumentFilter(new EventTriggerNumberFilter(0, 255, false, false, greenColourChangeListener));

        inputPanel.add(greenLabel);
        inputPanel.add(greenTextField);

        JLabel blueLabel = new JLabel( " B ", JLabel.TRAILING);
        blueTextField = new JTextField(String.valueOf(255));
        blueTextField.setPreferredSize(new Dimension(30, 20));
        blueTextField.setMaximumSize(new Dimension(30, 20));
        blueTextField.setHorizontalAlignment(JTextField.LEADING);

        ChangeListener blueColourChangeListener = e -> {
            Color blueAdjust = new Color(Integer.parseInt(redTextField.getText()), Integer.parseInt(greenTextField.getText()), (int) e.getSource());
            updateColorWheel(blueAdjust);
        };

        PlainDocument docBlue = (PlainDocument) blueTextField.getDocument();
        docBlue.setDocumentFilter(new EventTriggerNumberFilter(0, 255, false, false, blueColourChangeListener));

        inputPanel.add(blueLabel);
        inputPanel.add(blueTextField);

        rgbSelectPanel.add(Box.createRigidArea(new Dimension(25, 10)));
        rgbSelectPanel.add(inputPanel);

        //
        // node and connection settings
        //

        selectionPanel.add(Box.createRigidArea(new Dimension(1, 5)));

        nodePanel = new JPanel(new SpringLayout());
        TitledBorder nodeBorder = BorderFactory.createTitledBorder(getLocaleString("panel_config_tab_colours_selection_node"));
        nodeBorder.setTitleJustification(TitledBorder.CENTER);
        nodePanel.setBorder(nodeBorder);


        pathGroup = new ButtonGroup();
        regularNode = makeRadioButton("panel_config_tab_colours_node_normal", "NODE_REGULAR", "panel_config_tab_colours_node_normal_tooltip", Color.BLACK, true,false, nodePanel, pathGroup, true, configListener);
        nodePanel.add(Box.createRigidArea(new Dimension(25, 1)));
        subprioNode = makeRadioButton("panel_config_tab_colours_node_subprio", "NODE_SUBPRIO", "panel_config_tab_colours_node_subprio_tooltip", Color.BLACK, false,false, nodePanel, pathGroup, false, configListener);

        subprioNode = makeRadioButton("panel_config_tab_colours_node_selected", "NODE_SELECTED", "panel_config_tab_colours_node_selected_tooltip", Color.BLACK, false,false, nodePanel, pathGroup, false, configListener);
        nodePanel.add(Box.createRigidArea(new Dimension(25, 1)));
        subprioNode = makeRadioButton("panel_config_tab_colours_node_control", "NODE_CONTROL", "panel_config_tab_colours_node_control_tooltip", Color.BLACK, false,false, nodePanel, pathGroup, false, configListener);

        makeCompactGrid(nodePanel, 2, 5, 5, 5, 0, 0);

        selectionPanel.add(nodePanel);

        connectionPanel = new JPanel(new SpringLayout());
        TitledBorder connectionBorder = BorderFactory.createTitledBorder(getLocaleString("panel_config_tab_colours_selection_connections"));
        connectionBorder.setTitleJustification(TitledBorder.CENTER);
        connectionPanel.setBorder(connectionBorder);

        regularConnection = makeRadioButton("panel_config_tab_colours_connection_normal", "CONNECT_REGULAR", "panel_config_tab_colours_connection_normal_tooltip", Color.BLACK, true,false, connectionPanel, pathGroup, true, configListener);
        connectionPanel.add(Box.createRigidArea(new Dimension(2, 1)));
        subprioConnection = makeRadioButton("panel_config_tab_colours_connection_subprio", "CONNECT_SUBPRIO", "panel_config_tab_colours_connection_subprio_tooltip", Color.BLACK, true,false, connectionPanel, pathGroup, false, configListener);

        dualConnection = makeRadioButton("panel_config_tab_colours_connection_dual", "CONNECT_DUAL", "panel_config_tab_colours_connection_dual_tooltip", Color.BLACK, true,false, connectionPanel, pathGroup, false, configListener);
        connectionPanel.add(Box.createRigidArea(new Dimension(2, 1)));
        reverseConnection = makeRadioButton("panel_config_tab_colours_connection_reverse", "CONNECT_REVERSE", "panel_config_tab_colours_connection_reverse_tooltip", Color.BLACK, true,false, connectionPanel, pathGroup, false, configListener);

        makeRadioButton("panel_config_tab_colours_connection_dual_subprio", "CONNECT_DUAL_SUBPRIO", "panel_config_tab_colours_connection_dual_subprio_tooltip", Color.BLACK, true,false, connectionPanel, pathGroup, false, configListener);
        connectionPanel.add(Box.createRigidArea(new Dimension(2, 1)));
        makeRadioButton("panel_config_tab_colours_connection_reverse_subprio", "CONNECT_REVERSE_SUBPRIO", "panel_config_tab_colours_connection_reverse_subprio_tooltip", Color.BLACK, true,false, connectionPanel, pathGroup, false, configListener);

        makeCompactGrid(connectionPanel, 3, 5, 0, 5, 0, 0);

        selectionPanel.add(connectionPanel);

        JPanel confirmPanel = new JPanel(new FlowLayout(FlowLayout.CENTER,0,0));

        makeButton("RESET COLOURS", "panel_config_tab_colours_button_reset_tooltip", "panel_config_tab_colours_button_reset", confirmPanel, null, false, configListener, true);

        selectionPanel.add(confirmPanel);
        gui.add(configTabPane);
        add(gui);
    }

    public static String getButtonActionCommand(ButtonGroup buttonGroup) {
        for (Enumeration<AbstractButton> buttons = buttonGroup.getElements(); buttons.hasMoreElements();) {
            JRadioButton button = (JRadioButton) buttons.nextElement();
            if (button.isSelected()) {
                return button.getActionCommand();

            }
        }
        return "";
    }
    public static void updateColorWheel(Color color) { colorPicker.setRGB(color.getRed(), color.getGreen(), color.getBlue());}

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
