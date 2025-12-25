package AutoDriveEditor.Classes.UI_Components.ShortcutTable;

import AutoDriveEditor.Classes.KeyBinds.Shortcut;
import AutoDriveEditor.Managers.ShortcutManager;

import javax.swing.*;
import javax.swing.table.TableCellEditor;
import java.awt.*;

import static AutoDriveEditor.Classes.Util_Classes.GUIUtils.makeSVGImageButton;
import static AutoDriveEditor.Classes.Util_Classes.LoggerUtils.LOG;
import static AutoDriveEditor.GUI.Config.Tabs.KeyBindsTab.getCellBGColour;
import static AutoDriveEditor.GUI.Menus.DebugMenu.Logging.LogShortcutInfoMenu.bDebugLogShortcutInfo;
import static AutoDriveEditor.Managers.IconManager.*;
import static AutoDriveEditor.Managers.ShortcutManager.updateShortcut;

/**
 * A custom cell editor for a JTable that provides buttons to removeOriginalNodes or reset shortcuts.
 */
public class ButtonCellEditor extends AbstractCellEditor implements TableCellEditor {
    private final JPanel panel;
    private int currentRow;
    private final JTable table;

    /**
     * Constructs a ButtonCellEditor for the specified JTable.
     *
     * @param cellTable the JTable that uses this editor
     */
    @SuppressWarnings("DataFlowIssue")
    public ButtonCellEditor(JTable cellTable) {

        this.table = cellTable;
        panel = new JPanel(new GridLayout(1, 2));
        panel.setOpaque(true);

        // createSetting a removeOriginalNodes button
        JButton removeButton = makeSVGImageButton(getIconPath(CANCEL_ICON), 18, 18, null, "panel_config_tab_keybinds_button_remove",  panel, true, null);

        // createSetting a reset button
        JButton resetButton = makeSVGImageButton(getIconPath(RESET_ICON), 18, 16, null, "panel_config_tab_keybinds_button_reset",  panel, true, null);

        // add an action listener to clear the shortcut in the selected row and updateVisibility the list of shortcuts
        removeButton.addActionListener(e -> {
            int row = table.getSelectedRow();
            if (row != -1) {
                if (bDebugLogShortcutInfo) LOG.info("Clearing shortcut in row {}", row);
                String rowName = (String) table.getModel().getValueAt(row, 0);
                Shortcut shortcut = ShortcutManager.getUserShortcutByName(rowName);
                updateShortcut(shortcut.getId(), 0, 0);
                table.getModel().setValueAt(shortcut.getShortcutString(), row, 1);
            }
        });

        // add an action listener to reset the shortcut in the selected row and updateVisibility the list of shortcuts
        resetButton.addActionListener(e -> {
            if (bDebugLogShortcutInfo) LOG.info("ActionListener: Resetting keybinding in row {}", currentRow);
            int row = table.getSelectedRow();
            if (row != -1) {
                String actionName = (String) table.getModel().getValueAt(row, 0);
                LOG.info("Resetting keybinding for action: {}", actionName);
                Shortcut shortcut = ShortcutManager.getDefaultShortcutByName(actionName);
                LOG.info("Resetting keybinding for action: {} to default: {Key: {} Modifier: {}}", actionName, shortcut.getKeyCodeString(), shortcut.getModifierString());
                updateShortcut(shortcut.getId(), shortcut.getKeyCode(), shortcut.getModifier());
                if (bDebugLogShortcutInfo) LOG.info("ActionListener: Reset keybinding {} to default: {Key: {} Modifier: {}}", actionName, shortcut.getKeyCodeString(), shortcut.getModifierString());
                table.getModel().setValueAt(shortcut.getShortcutString(), row, 1);
            }
        });

        panel.add(removeButton);
        panel.add(resetButton);
    }

    /**
     * Returns the value contained in the cell.
     *
     * @return null as there is no value to return
     */
    @Override
    public Object getCellEditorValue() { return null; }

    /**
     * Sets the current selected row and returns the panel that is being edited.
     *
     * @param table the JTable that is asking the editor to edit; can be null
     * @param value the value of the cell to be edited; ignored
     * @param isSelected true if the cell is to be rendered with highlighting; ignored
     * @param row the row of the cell being edited
     * @param column the column of the cell being edited; ignored
     * @return the component that is editing
     */
    @Override
    public Component getTableCellEditorComponent(JTable table, Object value, boolean isSelected, int row, int column) {
        currentRow = row;
        if (!isSelected) {
            panel.setBackground(getCellBGColour(row));
        }
        return panel;
    }
}