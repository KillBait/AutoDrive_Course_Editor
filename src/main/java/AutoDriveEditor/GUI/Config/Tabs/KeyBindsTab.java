package AutoDriveEditor.GUI.Config.Tabs;

import AutoDriveEditor.Classes.KeyBinds.Shortcut;
import AutoDriveEditor.Classes.UI_Components.ShortcutTable.ButtonCellEditor;
import AutoDriveEditor.Classes.UI_Components.ShortcutTable.ButtonCellRenderer;
import AutoDriveEditor.Managers.ShortcutManager;
import com.formdev.flatlaf.FlatLaf;
import com.formdev.flatlaf.extras.components.FlatTable;
import com.formdev.flatlaf.util.ColorFunctions;
import net.miginfocom.swing.MigLayout;

import javax.swing.*;
import javax.swing.table.DefaultTableModel;
import javax.swing.table.TableColumn;
import java.awt.*;
import java.awt.event.KeyAdapter;
import java.awt.event.KeyEvent;
import java.awt.event.MouseAdapter;
import java.awt.event.MouseEvent;

import static AutoDriveEditor.Classes.Util_Classes.GUIUtils.makeBasicButton;
import static AutoDriveEditor.Classes.Util_Classes.LoggerUtils.LOG;
import static AutoDriveEditor.GUI.Config.ConfigGUI.configGUI;
import static AutoDriveEditor.GUI.Menus.DebugMenu.Logging.LogShortcutInfoMenu.bDebugLogShortcutInfo;
import static AutoDriveEditor.Locale.LocaleManager.getLocaleString;
import static AutoDriveEditor.Managers.ShortcutManager.checkIfShortcutInUse;
import static AutoDriveEditor.Managers.ShortcutManager.updateShortcut;

public class KeyBindsTab extends JPanel {

    private static FlatTable table;
    private static JPanel keyBindsPanel;
    private static JLabel keyPressLabel;
    private static Color dialogFGColor;
    private static Timer timer;
    private static Color defCellColour;
    private static Color altCellColour;

    public KeyBindsTab() {

        // Store a reference to this panel for use in the popup window
        keyBindsPanel = this;

        // Set the layout of the main panel
        setLayout(new MigLayout("center, insets 30 30 10 30", "[]", "[]15[]"));

        // Column names
        String[] columnNames = {
                getLocaleString("panel_config_tab_keybinds_table_action"),
                getLocaleString("panel_config_tab_keybinds_table_shortcut"),
                ""};

        // Create table model with non-editable columns
        DefaultTableModel tableModel = new DefaultTableModel(columnNames, 0) {
            @Override
            public boolean isCellEditable(int row, int column) {
                // Make all but the button cell non-editable
                return (column == 2);
            }
        };

        // Retrieve shortcuts from ShortcutManager
        for (Shortcut shortcut : ShortcutManager.getAllShortcuts()) {
            String functionName = shortcut.getLocalizedString();
            String keyCodeString = shortcut.getKeyCodeString();
            String modifierString = shortcut.getModifierString();
            tableModel.addRow(new Object[]{functionName, (modifierString.isEmpty()) ? keyCodeString : modifierString + " + " + keyCodeString});
        }

        // Create JTable
        table = new FlatTable();

        // set the table row colours
        updateTableColours();

        // add the button cell editor and renderer to the third column
        table.setModel(tableModel);
        table.getColumnModel().getColumn(2).setCellEditor(new ButtonCellEditor(table));
        table.getColumnModel().getColumn(2).setCellRenderer(new ButtonCellRenderer(table));

        // Set fixed width for the third column
        TableColumn thirdColumn = table.getColumnModel().getColumn(2);
        int fixedWidth = 50;
        thirdColumn.setPreferredWidth(fixedWidth);
        thirdColumn.setMinWidth(fixedWidth);
        thirdColumn.setMaxWidth(fixedWidth);
        thirdColumn.setResizable(false);

        // Add mouse listener to detect clicks on column 2
        table.addMouseListener(new MouseAdapter() {
            @Override
            public void mouseClicked(MouseEvent e) {
                int row = table.rowAtPoint(e.getPoint());
                int column = table.columnAtPoint(e.getPoint());
                if (bDebugLogShortcutInfo) {
                    LOG.info("Mouse clicked on row: {} ( {} ) , column: {} ( {} )", row, table.getValueAt(row, 0) , column, table.getColumnName(column));
                }
                // Only respond if the clicked column is column 2
                if (table.getColumnName(column).equals("Shortcut")) {  // Column index 1 is "Column 2"
                    // Open the popup window when the cell is clicked
                    openPopupWindow(tableModel, row);
                }
            }
        });

        // Add a property change listener to updateVisibility the table colours when the UI changes
        table.addPropertyChangeListener(e -> {
            if (e.getPropertyName().equals("UI") || e.getPropertyName().equals("foreground") || e.getPropertyName().equals("gridcolor")) {
                UIManager.put("Table.alternateRowColor", FlatLaf.isLafDark() ? ColorFunctions.lighten(this.getBackground(), 0.15F) : ColorFunctions.darken(this.getBackground(), 0.15F));
            }
        });

        // Add the scrollPane to the main panel
        add(new JScrollPane(table), "wrap");

        // Add a button panel to the bottom of the panel
        JPanel buttonPanel = new JPanel(new MigLayout("center"));
        buttonPanel.putClientProperty("FlatLaf.style", "border: 10,75,10,75");

        // Create and add the 'Clear All' buttons
        JButton clearAllButton = makeBasicButton(null, "panel_config_tab_keybinds_button_clear_all", "panel_config_tab_keybinds_button_clear_all", buttonPanel, true, true);
        clearAllButton.addActionListener(e -> {
            LOG.info("Clearing all shortcuts");
            ShortcutManager.clearAllShortcuts();
            // Clear the shortcuts in the table model
            for (int row = 0; row < table.getRowCount(); row++) {
                String rowName = (String) table.getModel().getValueAt(row, 0);
                for(Shortcut shortcut : ShortcutManager.getAllShortcuts()) {
                    if (rowName.equals(shortcut.getLocalizedString())) {
                        table.getModel().setValueAt(shortcut.getShortcutString(), row, 1);
                    }
                }
            }
        });

        // Create and add the 'Reset All' button
        JButton resetAllButton = makeBasicButton(null, "panel_config_tab_keybinds_button_reset_all", "panel_config_tab_keybinds_button_reset_all", buttonPanel, true, true);
        resetAllButton.addActionListener(e -> {
            LOG.info("Resetting all shortcuts");
            ShortcutManager.resetAllToDefault();
            for (int row = 0; row < table.getRowCount(); row++) {
                String rowName = (String) table.getModel().getValueAt(row, 0);
                for(Shortcut shortcut : ShortcutManager.getAllShortcuts()) {
                    if (rowName.equals(shortcut.getLocalizedString())) {
                        table.getModel().setValueAt(shortcut.getShortcutString(), row, 1);
                    }
                }
            }
        });

        // Add the buttons to the button panel
        buttonPanel.add(new JLabel(""), "dock center, push");// filler to center the buttons
        buttonPanel.add(clearAllButton, "dock west");
        buttonPanel.add(resetAllButton, "dock east");

        // Add the button panel to the bottom of the main panel
        add(buttonPanel, "grow");


    }

    // get the background colour for a table cell
    public static Color getCellBGColour(int row) {
        Color background = table.getBackground();
        Color alternateRowColor = FlatLaf.isLafDark() ? ColorFunctions.lighten(background, 0.10F) : ColorFunctions.darken(background, 0.10F);
        UIManager.put("Table.alternateRowColor", alternateRowColor);
        altCellColour = alternateRowColor;
        defCellColour = table.getBackground();
        return (row % 2 == 0) ? defCellColour : altCellColour;
    }

    // updateVisibility the table colours
    private void updateTableColours() {
        Color background = this.getBackground();
        Color alternateRowColor = FlatLaf.isLafDark() ? ColorFunctions.lighten(background, 0.10F) : ColorFunctions.darken(background, 0.10F);
        UIManager.put("Table.alternateRowColor", alternateRowColor);
        altCellColour = alternateRowColor;
        defCellColour = this.getBackground();

    }



    // open a popup window for key detection
    private static void openPopupWindow(DefaultTableModel tableModel, int row) {

        // Create a JDialog as a popup window
        JDialog popup = new JDialog(configGUI, getLocaleString("panel_config_tab_keybinds_popup_title"), true);
        popup.setSize(300, 100);
        //popup.setSize(300, 100);
        popup.setLayout(new BorderLayout());
        keyPressLabel = new JLabel(getLocaleString("panel_config_tab_keybinds_popup_keypress"), JLabel.CENTER);
        popup.add(keyPressLabel, BorderLayout.CENTER);

        // Center the dialog relative to the table
        popup.setLocationRelativeTo(keyBindsPanel);
        popup.setMinimumSize(popup.getPreferredSize());
        // Add a KeyListener to detect key presses
        popup.addKeyListener(new KeyAdapter() {
            @Override
            public void keyPressed(KeyEvent e) {
                // Ignore modifier keys (Shift, Ctrl, Alt, etc.)
                if (!isModifierKey(e)) {
                    // Capture key and modifier information
                    String keyText = KeyEvent.getKeyText(e.getKeyCode());
                    String modifiersText = KeyEvent.getModifiersExText(e.getModifiersEx());

                    if (checkIfShortcutInUse(e.getKeyCode(), e.getModifiersEx())) {
                        dialogFGColor = keyPressLabel.getForeground();
                        keyPressLabel.setForeground(Color.RED);
                        keyPressLabel.setText(getLocaleString("panel_config_tab_keybinds_popup_key_in_use"));
                        startResetTimer();
                        if (bDebugLogShortcutInfo) LOG.info("Key '{}' is already in use", keyText);
                        return;
                    }

                    if (e.getKeyCode() == KeyEvent.VK_ESCAPE) {
                        popup.dispose();
                        return;
                    }

                    // Display key and modifier information in the clicked cell
                    String result = modifiersText.isEmpty() ? keyText : modifiersText + " + " + keyText;
                    tableModel.setValueAt(result, row, 1);
                    String actionName = (String) tableModel.getValueAt(row, 0);
                    if (bDebugLogShortcutInfo) LOG.info("Detected new shortcut for '{}' : keyCode '{}' , modifier '{}'",actionName, e.getKeyCode(), e.getModifiersEx());
                    for (Shortcut shortcut : ShortcutManager.getAllShortcuts()) {
                        if (actionName.equals(shortcut.getLocalizedString())) {
                            if (bDebugLogShortcutInfo) LOG.info("Updating shortcut '{}' to '{}'", actionName, result);
                            updateShortcut(shortcut.getId(), e.getKeyCode(), e.getModifiersEx());

                        }
                    }
                    // Close the dialog
                    popup.dispose();
                }
            }
        });

        // Make sure the dialog gets focus for key detection
        popup.setFocusable(true);
        popup.setFocusableWindowState(true);
        popup.setVisible(true);
    }

    private static void startResetTimer() {
        if (timer != null) {
            timer.stop();
        }
        timer = new Timer(2000, e -> {
            keyPressLabel.setForeground(dialogFGColor);
            keyPressLabel.setText(getLocaleString("panel_config_tab_keybinds_popup_keypress"));
        });
        timer.setRepeats(false);
        timer.start();
    }

    // Utility method to check if the pressed key is a modifier key
    private static boolean isModifierKey(KeyEvent e) {
        int keyCode = e.getKeyCode();
        return keyCode == KeyEvent.VK_SHIFT || keyCode == KeyEvent.VK_CONTROL ||
                keyCode == KeyEvent.VK_ALT || keyCode == KeyEvent.VK_META ||
                keyCode == KeyEvent.VK_ALT_GRAPH || keyCode == KeyEvent.VK_CAPS_LOCK;
    }
}
