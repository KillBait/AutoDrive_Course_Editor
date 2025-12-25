package AutoDriveEditor.GUI.Config;

import AutoDriveEditor.Classes.UI_Components.TopRoundedLabel;
import AutoDriveEditor.GUI.Config.Tabs.*;
import net.miginfocom.swing.MigLayout;

import javax.swing.*;
import java.awt.*;
import java.awt.event.WindowAdapter;
import java.awt.event.WindowEvent;
import java.util.HashMap;
import java.util.Map;

import static AutoDriveEditor.AutoDriveEditor.EXPERIMENTAL;
import static AutoDriveEditor.AutoDriveEditor.getMapPanel;
import static AutoDriveEditor.Classes.Util_Classes.LoggerUtils.LOG;
import static AutoDriveEditor.GUI.Menus.DebugMenu.Logging.LogGUIInfoMenu.bDebugLogGUIInfo;
import static AutoDriveEditor.Locale.LocaleManager.getLocaleString;
import static AutoDriveEditor.Managers.IconManager.getTractorImage;

public class ConfigGUI extends JFrame {

    private static final int WIN_WIDTH = 620;
    private static final int WIN_HEIGHT = 460;

    public static ConfigGUI configGUI;
    private DefaultListModel<String> listModel;
    private JList<String> itemList;
    private final Map<String, String> tooltips = new HashMap<>(); // Store tooltips for each list item// Store the JList reference
    public static boolean isConfigWindowOpen = false;

    public static TopRoundedLabel contentTabLabel;

    public static void createConfigGUI(Component comp) {
        SwingUtilities.invokeLater(() -> {
            configGUI = new ConfigGUI();
            configGUI.dispatchEvent(new WindowEvent(configGUI, WindowEvent.WINDOW_CLOSING));
            configGUI.addWindowListener(new WindowAdapter() {
                @Override
                public void windowClosed(WindowEvent e) {
                    isConfigWindowOpen = false;
                    super.windowClosed(e);
                }
            });
            configGUI.setDefaultCloseOperation(JFrame.DISPOSE_ON_CLOSE);
            configGUI.setTitle(getLocaleString("panel_config_window_title"));
            configGUI.setIconImage(getTractorImage());
            configGUI.setPreferredSize(new Dimension(WIN_WIDTH, WIN_HEIGHT));
            configGUI.setResizable(true);
            configGUI.pack();
            configGUI.setLocationRelativeTo(getMapPanel());
            configGUI.setVisible(true);
            configGUI.setAlwaysOnTop(false);
        });
        isConfigWindowOpen = true;
    }

    private ConfigGUI() {

        // Create the main panel
        JPanel guiPanel = new JPanel(new MigLayout("insets 10 5 10 10", "[][]", "[]0[grow]"));

        // Create the JList
        JList<String> itemList = createStringJList();

        // Create a JScrollPane for the JList
        JScrollPane scrollPane = new JScrollPane(itemList);

        // Create a JLabel for the content panel
        contentTabLabel = new TopRoundedLabel("", 10, null);

        // Add the label to the card panel
        guiPanel.add(contentTabLabel, "wrap");

        // Create a CardLayout JPanel to show all the config pages
        JPanel contentPanel = new JPanel(new CardLayout());
        contentPanel.setBorder(BorderFactory.createLineBorder(Color.LIGHT_GRAY));
        guiPanel.add(contentPanel, "push, grow");

        addTab(contentPanel, new EditorTab(), "panel_config_list_editor", "panel_config_list_editor_tooltip");
        addTab(contentPanel, new AutoSaveTab(), "panel_config_list_autosave", "panel_config_list_autosave_tooltip");
        addTab(contentPanel, new CurvesTab(), "panel_config_list_curves", "panel_config_list_curves_tooltip");
        addTab(contentPanel, new ConnectionsTab(), "panel_config_list_connections", "panel_config_list_connections_tooltip");
        addTab(contentPanel, new ColoursTab(), "panel_config_list_node_colour", "panel_config_list_node_colour_tooltip");
        addTab(contentPanel, new MoveWidgetTab(), "panel_config_list_widget_move", "panel_config_list_widget_move_tooltip");
        addTab(contentPanel, new ThemeTab(), "panel_config_list_theme", "panel_config_list_theme_tooltip");
        addTab(contentPanel, new KeyBindsTab(), "panel_config_list_keybinds", "panel_config_list_keybinds_tooltip");

        // Add experimental tab if enabled
        if (EXPERIMENTAL) {
            //
            // Early testing, may not make it into release..
            //
            addTab(contentPanel, new ExperimentalTab(), "panel_config_list_experimental", "panel_config_list_experimental_tooltip");

        }
        guiPanel.add(scrollPane, "dock west, gap 10 0 10 10, w 125!");

        // Add a listener to the JList to swap the content panel to the selected item
        // and updateVisibility the content tab label
        itemList.addListSelectionListener(e -> {
            if (!e.getValueIsAdjusting()) {
                CardLayout cl = (CardLayout) (contentPanel.getLayout());
                if (bDebugLogGUIInfo) LOG.info("Selected Tab: {}", itemList.getSelectedValue());
                contentTabLabel.setText(itemList.getSelectedValue() + " Options");
                cl.show(contentPanel, itemList.getSelectedValue());
            }
        });

        // Add the main panel to the frame
        add(guiPanel);
    }

    // Utility function to get the ConfigGUI instance
    public static ConfigGUI getConfigGUI_NEW() { return configGUI; }

    // Create a JList with a DefaultListModel
    private JList<String> createStringJList() {
        listModel = new DefaultListModel<>();
        itemList = new JList<>(listModel);
        itemList.setSelectionMode(ListSelectionModel.SINGLE_SELECTION);
        SwingUtilities.invokeLater(() -> { itemList.setSelectedIndex(0); });
        return itemList;
    }

    // Add a tab to the content panel and JList
    private void addTab(JPanel mainPanel, JPanel optionsPanel, String panelName, String toolTipText) {
        String localizedPanelName = getLocaleString(panelName);
        mainPanel.add(optionsPanel, localizedPanelName);
        listModel.addElement(localizedPanelName);
        tooltips.put(localizedPanelName, getLocaleString(toolTipText)); // Store the tooltip text

        // Set tooltip for the newly added element
        itemList.setCellRenderer(new CustomListCellRenderer(tooltips));
    }

    private static class CustomListCellRenderer extends DefaultListCellRenderer {
        private final Map<String, String> tooltips;

        public CustomListCellRenderer(Map<String, String> tooltips) {
            this.tooltips = tooltips;
        }

        @SuppressWarnings("SuspiciousMethodCalls")
        @Override
        public Component getListCellRendererComponent(JList<?> list, Object value, int index, boolean isSelected, boolean cellHasFocus) {
            JLabel label = (JLabel) super.getListCellRendererComponent(list, value, index, isSelected, cellHasFocus);
            label.setBorder(BorderFactory.createEmptyBorder(2, 5, 2, 5)); // Top, Left, Bottom, Right padding
            label.setToolTipText(tooltips.get(value)); // Set tooltip text from the map
            return label;
        }
    }
}
