package AutoDriveEditor.Classes.UI_Components.ShortcutTable;

import javax.swing.*;
import javax.swing.table.TableCellRenderer;
import java.awt.*;

import static AutoDriveEditor.Classes.Util_Classes.GUIUtils.makeSVGImageButton;
import static AutoDriveEditor.GUI.Config.Tabs.KeyBindsTab.getCellBGColour;
import static AutoDriveEditor.Managers.IconManager.*;

// TODO: Implement the tooltip display for the buttons

public class ButtonCellRenderer extends JPanel implements TableCellRenderer {

    private String currentTooltip;

    public ButtonCellRenderer(JTable table) {
        setLayout(new GridLayout(1,2));
        setOpaque(true);

        //
        // Can't use SVG images here, as the icons dont render for some themes
        // temporary fix is to draw the SVG into a BufferImage and use that instead
        //
        // TODO: Investigate further, no obvious reason for the rendering issue..
        //

        JButton removeButton = makeSVGImageButton(getIconPath(CANCEL_ICON), 18, 18, null, "panel_config_tab_keybinds_button_remove",  this, true, null);

        JButton resetButton = makeSVGImageButton(getIconPath(RESET_ICON), 16, 16, null, "panel_config_tab_keybinds_button_reset",  this, true, null);

        add(removeButton);
        add(resetButton);
    }

    // Set the background colour of the cell based on the row index
    @Override
    public Component getTableCellRendererComponent(JTable table, Object value, boolean isSelected, boolean hasFocus, int row, int column) {
        if (!isSelected) {
            setBackground(getCellBGColour(row));
        }
        return this;
    }

    // Set the tooltip text for the buttons
    // TODO: Fix tooltip only displaying when the cell containing the buttons is selected.
    //
    @Override
    public String getToolTipText() {
        return currentTooltip;
    }


}
