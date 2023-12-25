package AutoDriveEditor.GUI.Config;

import AutoDriveEditor.GUI.Config.Tabs.*;

import javax.swing.*;
import java.awt.*;
import java.awt.event.*;

import static AutoDriveEditor.AutoDriveEditor.EXPERIMENTAL;
import static AutoDriveEditor.AutoDriveEditor.getMapPanel;
import static AutoDriveEditor.GUI.Config.ColourPreviewPanel.*;
import static AutoDriveEditor.GUI.Config.Tabs.ColourSelectorTab.*;
import static AutoDriveEditor.GUI.EditorImages.getTractorImage;
import static AutoDriveEditor.GUI.Menus.DebugMenu.Logging.LogGUIInfoMenu.bDebugLogGUIInfo;
import static AutoDriveEditor.Locale.LocaleManager.getLocaleString;
import static AutoDriveEditor.Utils.LoggerUtils.LOG;
import static AutoDriveEditor.XMLConfig.EditorXML.*;

public class ConfigGUI extends JFrame implements ItemListener, ActionListener {
    public static ConfigGUI configGUI;
    public static boolean isConfigWindowOpen = false;
    private static final int WIN_WIDTH = 450;
    private static final int WIN_HEIGHT = 300;

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

    public static ConfigGUI getConfigGUI() { return configGUI; }

    public ConfigGUI() {
        super();

        JTabbedPane configTabPane = new JTabbedPane(SwingConstants.TOP);
        configTabPane.setTabLayoutPolicy(JTabbedPane.SCROLL_TAB_LAYOUT);

        JPanel configWindowGUI = new JPanel(new BorderLayout(5, 5));

        // Add mapPanel Tab

        configTabPane.addTab(getLocaleString("panel_config_tab_mappanel"), null, new MapPanelTab(), getLocaleString("panel_config_tab_mappanel_tooltip"));

        // Add autoSaveTab Tab

        configTabPane.addTab(getLocaleString("panel_config_tab_autosave"), null, new AutoSaveTab(), getLocaleString("panel_config_tab_autosave_tooltip"));

        // Add CurveOptions Tab

        configTabPane.addTab(getLocaleString("panel_config_tab_curves"), null, new CurvesTab(), getLocaleString("panel_config_tab_curves_tooltip"));

        // Add LinearLine Tab

        configTabPane.addTab(getLocaleString("panel_config_tab_connections"), null, new ConnectionsTab(), getLocaleString("panel_config_tab_connections_tooltip"));

        // Add colourSelector tab

        configTabPane.addTab(getLocaleString("panel_config_tab_colours"), null, new ColourSelectorTab(), getLocaleString("panel_config_tab_colours_tooltip"));

        //  Add shortcuts tab

        if (EXPERIMENTAL) {

            //
            // Early testing, may not make it into release..
            //

            configTabPane.addTab(getLocaleString("panel_config_tab_shortcuts"), null, new KeybindsTab(), getLocaleString("panel_config_tab_shortcuts_tooltip"));
        }

        // Add experimental tab ( if enabled )

        if (EXPERIMENTAL) {
            configTabPane.addTab(getLocaleString("panel_config_tab_experimental"), null, new ExperimentalTab(), getLocaleString("panel_config_tab_experimental_tooltip"));
        }

        configWindowGUI.add(configTabPane);
        add(configWindowGUI);
    }

    //
    // Begin Config Listeners
    //

    @Override
    public void actionPerformed(ActionEvent e) {
        AbstractButton selectionPanelButton = (AbstractButton) e.getSource();
        if (bDebugLogGUIInfo) LOG.info("Config ActionPerformed: {}", selectionPanelButton.getActionCommand());
        if ("RESET COLOURS".equals(selectionPanelButton.getActionCommand())) {
            int response = JOptionPane.showConfirmDialog(configGUI, getLocaleString("dialog_reset_colours"), getLocaleString("dialog_reset_colours_title"), JOptionPane.YES_NO_OPTION, JOptionPane.QUESTION_MESSAGE);
            if (response == JOptionPane.YES_OPTION) {
                colourNodeRegular = Color.RED;
                colourNodeSubprio = Color.ORANGE;
                colourNodeSelected = Color.WHITE;
                colourNodeControl = Color.MAGENTA;
                colourConnectRegular = Color.GREEN;
                colourConnectSubprio =  Color.ORANGE;
                colourConnectDual = Color.BLUE;
                colourConnectDualSubprio = new Color(150,100,50); // Color.BROWN
                colourConnectReverse = Color.CYAN;
                colourConnectReverseSubprio = Color.CYAN;
                setPreviewNodeColour(colourNodeRegular);
                setPreviewConnectionColour(colourConnectRegular, false);
                resetNodePanelSelection();
                LOG.info("Colours reset");
            }
        }
    }

    @Override
    public void itemStateChanged(ItemEvent e) {
        AbstractButton selectionPanelButton = (AbstractButton) e.getItem();
        if (bDebugLogGUIInfo) LOG.info("Config ItemStateChange: {}", selectionPanelButton.getActionCommand());
        switch (selectionPanelButton.getActionCommand()) {

            // TODO:- Fix colour updates not updating the cached images that render thread use

            case "NODE_REGULAR":
                if (selectionPanelButton.isSelected()) {
                    setPreviewToControlNode(false);
                    setPreviewNodeColour(colourNodeRegular);
                    setPreviewConnectionColour(colourConnectRegular, false);
                    updateColorWheel(colourNodeRegular);
                    if (bDebugLogGUIInfo) LOG.info(" -> Set Preview NODE_REGULAR");
                }
                break;
            case "NODE_SUBPRIO":
                if (selectionPanelButton.isSelected()) {
                    setPreviewToControlNode(false);
                    setPreviewNodeColour(colourNodeSubprio);
                    setPreviewConnectionColour(colourConnectSubprio, false);
                    updateColorWheel(colourNodeSubprio);
                    if (bDebugLogGUIInfo) LOG.info(" -> Set Preview CONNECT_SUBPRIO");
                }
                break;
            case "NODE_SELECTED":
                if (selectionPanelButton.isSelected()) {
                    setNodeSelected();
                    updateColorWheel(colourNodeSelected);
                    if (bDebugLogGUIInfo) LOG.info(" -> Set Preview CONNECT_SELECTED");
                }
                break;
            case "NODE_CONTROL":
                if (selectionPanelButton.isSelected()) {
                    setPreviewToControlNode(true);
                    updateColorWheel(colourNodeControl);
                    if (bDebugLogGUIInfo) LOG.info(" -> Set Preview CONNECT_CONTROL");
                }
                break;
            case "CONNECT_REGULAR":
                if (selectionPanelButton.isSelected()) {
                    setPreviewToControlNode(false);
                    setPreviewNodeColour(colourNodeRegular);
                    setPreviewConnectionColour(colourConnectRegular, false);
                    updateColorWheel(colourConnectRegular);
                    if (bDebugLogGUIInfo) LOG.info(" -> Set Preview NODE_REGULAR / CONNECT_REGULAR");
                }
                break;
            case "CONNECT_SUBPRIO":
                if (selectionPanelButton.isSelected()) {
                    setPreviewToControlNode(false);
                    setPreviewNodeColour(colourNodeSubprio);
                    setPreviewConnectionColour(colourConnectSubprio, false);
                    updateColorWheel(colourConnectSubprio);
                    if (bDebugLogGUIInfo) LOG.info(" -> Set Preview CONNECT_SUBPRIO");
                }
                break;
            case "CONNECT_DUAL":
                if (selectionPanelButton.isSelected()) {
                    setPreviewToControlNode(false);
                    setPreviewNodeColour(colourNodeRegular);
                    setPreviewConnectionColour(colourConnectDual, true);
                    updateColorWheel(colourConnectDual);
                    if (bDebugLogGUIInfo) LOG.info(" -> Set Preview CONNECT_DUAL");
                }
                break;
            case "CONNECT_DUAL_SUBPRIO":
                if (selectionPanelButton.isSelected()) {
                    setPreviewToControlNode(false);
                    setPreviewNodeColour(colourNodeSubprio);
                    setPreviewConnectionColour(colourConnectDualSubprio, true);
                    updateColorWheel(colourConnectDualSubprio);
                    if (bDebugLogGUIInfo) LOG.info(" -> Set Preview CONNECT_DUAL");
                }
                break;
            case "CONNECT_REVERSE":
                if (selectionPanelButton.isSelected()) {
                    setPreviewToControlNode(false);
                    setPreviewNodeColour(colourNodeRegular);
                    setPreviewConnectionColour(colourConnectReverse, false);
                    updateColorWheel(colourConnectReverse);
                    if (bDebugLogGUIInfo) LOG.info(" -> Set Preview CONNECT_REVERSE");
                }
                break;
            case "CONNECT_REVERSE_SUBPRIO":
                if (selectionPanelButton.isSelected()) {
                    setPreviewToControlNode(false);
                    setPreviewNodeColour(colourNodeSubprio);
                    setPreviewConnectionColour(colourConnectReverseSubprio, false);
                    updateColorWheel(colourConnectReverseSubprio);
                    if (bDebugLogGUIInfo) LOG.info(" -> Set Preview CONNECT_DUAL");
                }
                break;
        }
        refreshPreviewPanel();
    }

}
