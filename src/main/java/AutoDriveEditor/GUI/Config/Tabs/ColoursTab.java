package AutoDriveEditor.GUI.Config.Tabs;

import AutoDriveEditor.Classes.UI_Components.ColourWheel.ColorWheel;
import AutoDriveEditor.Classes.UI_Components.HeaderList.HeaderListEntry;
import AutoDriveEditor.Classes.UI_Components.HeaderList.HeaderListPanel;
import AutoDriveEditor.GUI.Config.ConnectionPreviewPanel;
import com.formdev.flatlaf.extras.components.FlatButton;
import net.miginfocom.swing.MigLayout;

import javax.swing.*;
import java.awt.*;

import static AutoDriveEditor.AutoDriveEditor.getMapPanel;
import static AutoDriveEditor.Classes.Util_Classes.LoggerUtils.LOG;
import static AutoDriveEditor.GUI.Config.ConnectionPreviewPanel.*;
import static AutoDriveEditor.GUI.Config.ConfigGUI.getConfigGUI_NEW;
import static AutoDriveEditor.GUI.MapPanel.updateCachedNodeImages;
import static AutoDriveEditor.Locale.LocaleManager.getLocaleString;
import static AutoDriveEditor.XMLConfig.EditorXML.*;

public class ColoursTab extends JPanel {

    private final ColorWheel colourWheel;
    private final HeaderListPanel listPanel;
    private HeaderListEntry selectedEntry;
    private final ConnectionPreviewPanel colourPreviewPanel;


    public ColoursTab() {
        setLayout(new MigLayout("", "[]", "[grow]"));

        //
        // Colour list
        //

        JPanel colourPanel = new JPanel(new MigLayout("insets 0 0 0 0, wrap 1"));

        // createSetting a new HeaderListPanel
        listPanel = new HeaderListPanel();

        // Initialize the list with default colors
        buildList(listPanel);
        colourPanel.add(listPanel, "growy, pushy");

        //
        // Reset to default button
        //

        FlatButton resetColourButton = new FlatButton();
        resetColourButton.setToolTipText(getLocaleString("panel_config_tab_colours_button_reset"));
        resetColourButton.setText(getLocaleString("panel_config_tab_colours_button_reset"));
        resetColourButton.setFocusable(false);
        resetColourButton.addActionListener(e -> {
            int response = JOptionPane.showConfirmDialog(getConfigGUI_NEW(), getLocaleString("dialog_reset_colours"), getLocaleString("dialog_reset_colours_title"), JOptionPane.YES_NO_OPTION, JOptionPane.QUESTION_MESSAGE);
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
                colourGridLines = new Color(25, 25, 25);
                setPreviewNodeColour(colourNodeRegular);
                setPreviewConnectionColour(colourConnectRegular, false);
                ((DefaultListModel<?>) listPanel.getEntryList().getModel()).removeAllElements();
                buildList(listPanel);
                listPanel.repaint();
                refreshPreviewPanel();
                updateCachedNodeImages();
                getMapPanel().repaint();
                LOG.info("Colours reset");
            }
        });
        colourPanel.add(resetColourButton, "bottom, center, gapbottom 10, gaptop 10");
        add(colourPanel, "gapleft 5, gaptop 15, grow");

        //
        // Colour picker
        //

        JPanel pickerPanel = new JPanel(new MigLayout("insets 0, align center"));
        colourWheel = new ColorWheel();
        colourWheel.setAlphaVisible(false);
        colourWheel.setPreferredSize(new Dimension(250,250));
        colourWheel.addColorPropertyChangeListener(evt -> {
            Color newColor = evt.getColor();
            if (selectedEntry != null) {
                selectedEntry.setIconColor(newColor);
                updateColours(selectedEntry, newColor);
            }
        });
        pickerPanel.add(colourWheel, "wrap");

        //
        // Colour preview panel
        //

        colourPreviewPanel = new ConnectionPreviewPanel(new Dimension(175, 40));
        pickerPanel.add(colourPreviewPanel, "center, gaptop 10");
        add(pickerPanel);

        // Add a ListSelectionListener to handle selection events
        listPanel.getEntryList().addListSelectionListener(e -> {
            if (e.getValueIsAdjusting()) {
                HeaderListEntry entry = listPanel.getEntryList().getSelectedValue();
                if (entry.isValidEntry()) {
                    colourWheel.setRGB(entry.getIconColor());
                    selectedEntry = entry;
                    updateColours(entry, entry.getIconColor());
                }
            }
        });
    }

    private void updateColours(HeaderListEntry entry, Color newColor) {
        if (entry == null) return;
        setPreviewToControlNode(false);
        setNodeSelected(false);
        entry.setIconColor(newColor);
        switch (entry.getValue()) {
            case "NODE_REGULAR":
                colourNodeRegular = newColor;
                setPreviewNodeColour(newColor);
                break;
            case "NODE_SUBPRIO":
                colourNodeSubprio = newColor;
                setPreviewNodeColour(newColor);
                break;
            case "NODE_SELECTED":
                setNodeSelected(true);
                colourNodeSelected = newColor;
                break;
            case "NODE_CONTROL":
                colourNodeControl = newColor;
                setPreviewToControlNode(true);
                break;
            case "CONNECT_REGULAR":
                colourConnectRegular = newColor;
                setPreviewConnectionColour(newColor, false);
                break;
            case "CONNECT_SUBPRIO":
                setPreviewConnectionColour(newColor, false);
                colourConnectSubprio = newColor;
                break;
            case "CONNECT_DUAL":
                setPreviewConnectionColour(newColor, false);
                colourConnectDual = newColor;
                break;
            case "CONNECT_DUAL_SUBPRIO":
                setPreviewConnectionColour(newColor, false);
                colourConnectDualSubprio = newColor;
                break;
            case "CONNECT_REVERSE":
                setPreviewConnectionColour(newColor, false);
                colourConnectReverse = newColor;
                break;
            case "CONNECT_REVERSE_SUBPRIO":
                setPreviewConnectionColour(newColor, false);
                colourConnectReverseSubprio = newColor;
                break;
            case "GRID_LINES":
                colourGridLines = newColor;
                break;
        }
        listPanel.repaint();
        refreshPreviewPanel();
        updateCachedNodeImages();
        getMapPanel().repaint();
    }

    private void buildList(HeaderListPanel list) {
        // Initialize the color map with default colors
        list.addSeparator(getLocaleString("panel_config_tab_colours_header_node"), null, true, true, Color.LIGHT_GRAY);
        list.addColorEntry(colourNodeRegular, getLocaleString("panel_config_tab_colours_node_regular"), "NODE_REGULAR");
        list.addColorEntry(colourNodeSubprio, getLocaleString("panel_config_tab_colours_node_subprio"), "NODE_SUBPRIO");
        list.addColorEntry(colourNodeSelected, getLocaleString("panel_config_tab_colours_node_selected"), "NODE_SELECTED");
        list.addColorEntry(colourNodeControl, getLocaleString("panel_config_tab_colours_node_control"), "NODE_CONTROL");
        list.addSeparator(getLocaleString("panel_config_tab_colours_header_connections"), null, true, true, Color.LIGHT_GRAY);
        list.addColorEntry(colourConnectRegular, getLocaleString("panel_config_tab_colours_connection_regular"), "CONNECT_REGULAR");
        list.addColorEntry(colourConnectSubprio, getLocaleString("panel_config_tab_colours_connection_subprio"), "CONNECT_SUBPRIO");
        list.addColorEntry(colourConnectDual, getLocaleString("panel_config_tab_colours_connection_dual"), "CONNECT_DUAL");
        list.addColorEntry(colourConnectDualSubprio, getLocaleString("panel_config_tab_colours_connection_dual_subprio"), "CONNECT_DUAL_SUBPRIO");
        list.addColorEntry(colourConnectReverse, getLocaleString("panel_config_tab_colours_connection_reverse"), "CONNECT_REVERSE");
        list.addColorEntry(colourConnectReverseSubprio, getLocaleString("panel_config_tab_colours_connection_reverse_subprio"), "CONNECT_REVERSE_SUBPRIO");
        list.addSeparator(getLocaleString("panel_config_tab_colours_header_grid"), null, true, true, Color.LIGHT_GRAY);
        list.addColorEntry(colourGridLines, getLocaleString("panel_config_tab_colours_grid_lines"), "GRID_LINES");
    }

    private void refreshPreviewPanel() { if (colourPreviewPanel != null) colourPreviewPanel.repaint(); }
}
