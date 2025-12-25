package AutoDriveEditor.GUI.Config.Tabs;

import AutoDriveEditor.Classes.UI_Components.ColourWheel.ColorWheel;
import AutoDriveEditor.Classes.UI_Components.SwatchIcon;
import AutoDriveEditor.Classes.UI_Components.ToggleComboBox;
import AutoDriveEditor.GUI.Config.MoveWidgetPreviewPanel;
import com.formdev.flatlaf.ui.FlatBorder;
import net.miginfocom.swing.MigLayout;

import javax.swing.*;
import java.awt.*;
import java.awt.event.ActionListener;
import java.util.Collections;

import static AutoDriveEditor.AutoDriveEditor.getMapPanel;
import static AutoDriveEditor.AutoDriveEditor.widgetManager;
import static AutoDriveEditor.Classes.Util_Classes.GUIUtils.makeBasicButton;
import static AutoDriveEditor.Classes.Util_Classes.LoggerUtils.LOG;
import static AutoDriveEditor.GUI.Menus.DebugMenu.Logging.LogGUIInfoMenu.bDebugLogGUIInfo;
import static AutoDriveEditor.Locale.LocaleManager.getLocaleString;
import static AutoDriveEditor.XMLConfig.EditorXML.*;
import static com.formdev.flatlaf.util.ColorFunctions.darken;

public class MoveWidgetTab extends JPanel {

    private DefaultListModel<String> listModel;
    private ColorWheel colourWheel;

    private JLabel manualPositionLabel;
    private ToggleComboBox<String> freeMoveSizeComboBox;

    @SuppressWarnings({"rawtypes", "ExtractMethodRecommender"})
    public MoveWidgetTab() {

        setLayout(new MigLayout("insets 10 10 10 10", "[][]", "[grow][]"));

        // Create a JPanel to hold the JList and the config pages
        JPanel guiPanel = new JPanel(new MigLayout("insets 5"));
        guiPanel.putClientProperty("FlatLaf.style", "border: 0,0,0,0,@disabledForeground,1,16; background: darken($Panel.background,5%)");

        // Create a JList with a DefaultListModel
        JList<String> itemList = createStringJList();
        itemList.setCellRenderer(new CustomListCellRenderer());
        itemList.setOpaque(true);
        guiPanel.add(itemList);
        SwingUtilities.invokeLater(() -> itemList.setSelectedIndex(0));


        // Create a CardLayout JPanel to show all the config pages
        JPanel contentPanel = new JPanel(new CardLayout());
        contentPanel.setBorder(new FlatBorder());
        //contentPanel.putClientProperty("FlatLaf.style", "border: 0,0,0,0");

        guiPanel.add(contentPanel, "push, grow");

        // Add a ListSelectionListener to the JList to switch between the config pages
        itemList.addListSelectionListener(e -> {
            if (!e.getValueIsAdjusting()) {
                CardLayout cl = (CardLayout) (contentPanel.getLayout());
                if (bDebugLogGUIInfo) LOG.info("Selected Tab: {}", itemList.getSelectedValue());
                cl.show(contentPanel, itemList.getSelectedValue());
            }
        });
        itemList.setSelectedIndex(0);

        //
        // Create the Axis Tab
        //

        JPanel axisTab = new JPanel(new MigLayout("center, insets 10 10 0 10", "[][shrink][]"));

        // Axis Length
        JLabel axisLengthLabel = new JLabel(getLocaleString("panel_config_tab_move_widget_tab_axis_length"), JLabel.TRAILING);
        JSlider axisLengthSlider = new JSlider(JSlider.HORIZONTAL, 10, 50, axisLength);
        JTextField axisLengthTextField = new JTextField();
        axisLengthTextField.setText(axisLength + " px");
        axisLengthTextField.setEditable(false);
        axisLengthSlider.addChangeListener(e -> {
            JSlider source = (JSlider) e.getSource();
            if (source.getValueIsAdjusting()) {
                int newValue = axisLengthSlider.getValue();
                axisLengthTextField.setText(newValue + "px");
                axisLength = (byte) newValue;
//                getMoveWidget().updateVisibility();
                widgetManager.updateAllWidgets();
                getMapPanel().repaint();
            }
        });
        axisTab.add(axisLengthLabel, "gaptop 10");
        axisTab.add(axisLengthSlider);
        axisTab.add(axisLengthTextField, "wrap");

        // Axis Width
        JLabel axisWidthLabel = new JLabel(getLocaleString("panel_config_tab_move_widget_tab_axis_width"), JLabel.TRAILING);
        JSlider axisWidthSlider = new JSlider(JSlider.HORIZONTAL, 5, 15, axisWidth);
        JTextField axisWidthTextField = new JTextField();
        axisWidthTextField.setText(axisWidth + " px");
        axisWidthTextField.setEditable(false);
        axisWidthSlider.addChangeListener(e -> {
            JSlider source = (JSlider) e.getSource();
            if (source.getValueIsAdjusting()) {
                int newValue = axisWidthSlider.getValue();
                axisWidthTextField.setText(newValue + " px");
                axisWidth = (byte) newValue;
                widgetManager.updateAllWidgets();
                getMapPanel().repaint();
            }
        });

        axisTab.add(axisWidthLabel, "gapbottom 0");
        axisTab.add(axisWidthSlider, "gapbottom 0");
        axisTab.add(axisWidthTextField, "wrap, gapbottom 0");

        //
        JPanel axisDirectionPanel = new JPanel(new MigLayout("center, insets 0 0 0 0"));

        // X Arrow Direction
        axisDirectionPanel.add(new JLabel(getLocaleString("panel_config_tab_move_widget_tab_axis_x")), "center");
        String[] xDirectionList = {getLocaleString("panel_config_tab_move_widget_tab_axis_position_left"),
                getLocaleString("panel_config_tab_move_widget_tab_axis_position_right")};
        JComboBox<String> xDirectionComboBox = new JComboBox<>(xDirectionList);
        xDirectionComboBox.setSelectedIndex((xAxisDirection == X_DIRECTION.LEFT) ? 0 : 1);
        xDirectionComboBox.addActionListener(e -> {
            JComboBox comboBox = (JComboBox)e.getSource();
            int selectedIndex = comboBox.getSelectedIndex();
            switch (selectedIndex) {
                case 0: // Left
                    xAxisDirection = X_DIRECTION.LEFT;
                    break;
                case 1: // Right
                    xAxisDirection = X_DIRECTION.RIGHT;
                    break;
            }
//            getMoveWidget().updateVisibility();
            widgetManager.updateAllWidgets();
            getMapPanel().repaint();
        });
        axisDirectionPanel.add(xDirectionComboBox, "gapright 10");

        axisDirectionPanel.add(new JLabel(getLocaleString("panel_config_tab_move_widget_tab_axis_y")), "center");
        String[] yDirectionList = {getLocaleString("panel_config_tab_move_widget_tab_axis_position_up"),
                getLocaleString("panel_config_tab_move_widget_tab_axis_position_down")};
        JComboBox<String> yDirectionComboBox = new JComboBox<>(yDirectionList);
        yDirectionComboBox.setSelectedIndex((yAxisDirection == Y_DIRECTION.UP) ? 0 : 1);
        yDirectionComboBox.addActionListener(e -> {
            JComboBox comboBox = (JComboBox)e.getSource();
            int selectedIndex = comboBox.getSelectedIndex();
            switch (selectedIndex) {
                case 0: // Up
                    yAxisDirection = Y_DIRECTION.UP;
                    break;
                case 1: // Down
                    yAxisDirection = Y_DIRECTION.DOWN;
                    break;
            }
//            getMoveWidget().updateVisibility();
            widgetManager.updateAllWidgets();
            getMapPanel().repaint();
        });
        axisDirectionPanel.add(yDirectionComboBox);
        axisTab.add(axisDirectionPanel, "spanx, center, gap 0");

        addTab(contentPanel, axisTab, "panel_config_tab_move_widget_axis");


        //
        // Create the Arrow Tab
        //

        JPanel arrowTab = new JPanel(new MigLayout("center, insets 20 10 0 10", "[][shrink][]", "[][][]"));

        // Arrow Length
        JLabel arrowLengthLabel = new JLabel(getLocaleString("panel_config_tab_move_widget_tab_arrow_length"), JLabel.TRAILING);
        JSlider arrowLengthSlider = new JSlider(JSlider.HORIZONTAL, 10, 40, arrowLength);
        JTextField arrowLengthTextField = new JTextField();
        arrowLengthTextField.setText(arrowLength + " px");
        arrowLengthTextField.setEditable(false);
        arrowLengthSlider.addChangeListener(e -> {
            JSlider source = (JSlider) e.getSource();
            if (source.getValueIsAdjusting()) {
                int newValue = arrowLengthSlider.getValue();
                arrowLengthTextField.setText(newValue + " px");
                arrowLength = (byte) newValue;
                widgetManager.updateAllWidgets();
                getMapPanel().repaint();
            }
        });
        arrowTab.add(arrowLengthLabel, "gaptop 10");
        arrowTab.add(arrowLengthSlider);
        arrowTab.add(arrowLengthTextField, "wrap");

        // Arrow Width
        JLabel arrowWidthLabel = new JLabel(getLocaleString("panel_config_tab_move_widget_tab_arrow_width"), JLabel.TRAILING);
        JSlider arrowWidthSlider = new JSlider(JSlider.HORIZONTAL, 5, 20, arrowWidth);
        JTextField arrowWidthTextField = new JTextField();
        arrowWidthTextField.setText(arrowWidth + " px");
        arrowWidthTextField.setEditable(false);
        arrowWidthSlider.addChangeListener(e -> {
            JSlider source = (JSlider) e.getSource();
            if (source.getValueIsAdjusting()) {
                int newValue = arrowWidthSlider.getValue();
                arrowWidthTextField.setText(newValue + " px");
                arrowWidth = (byte) newValue;
//                getMoveWidget().updateVisibility();
                widgetManager.updateAllWidgets();
                getMapPanel().repaint();
            }
        });

        arrowTab.add(arrowWidthLabel, "gaptop 10");
        arrowTab.add(arrowWidthSlider);
        arrowTab.add(arrowWidthTextField, "wrap");
        addTab(contentPanel, arrowTab, "panel_config_tab_move_widget_arrow");

        //
        // Create the FreeMove Tab
        //

        JPanel freeMoveTab = new JPanel(new MigLayout("center, insets 15 10 0 10"));

        freeMoveTab.add(new JLabel(getLocaleString("panel_config_tab_move_widget_tab_position")), "center");
        String[] freeMovePositionList = {getLocaleString("panel_config_tab_move_widget_tab_position_center"),
                getLocaleString("panel_config_tab_move_widget_tab_position_manual")};
        JComboBox<String> freeMovePositionComboBox = new JComboBox<>(freeMovePositionList);
        if (freeMovePosition == FREEMOVE_POSITION.CENTER) {
            // delay setting the combo box items disabled, avoids triggering a NullPointerException
            // at runtime as the ToggleComboBox is not yet created. Quick and dirty fix :(
            SwingUtilities.invokeLater(() -> {
                freeMoveSizeComboBox.disableIndex(1);
                freeMoveSizeComboBox.disableIndex(2);
            });
            freeMovePositionComboBox.setSelectedIndex(0);
        } else {
            freeMovePositionComboBox.setSelectedIndex(1);
        }
        //freeMovePositionComboBox.setSelectedIndex((freeMovePosition == FREEMOVE_POSITION.CENTER) ? 0 : 1);
        freeMovePositionComboBox.addActionListener(e -> {
            JComboBox comboBox = (JComboBox)e.getSource();
            int selectedIndex = comboBox.getSelectedIndex();
            switch (selectedIndex) {
                case 0: // Center
                    freeMovePosition = FREEMOVE_POSITION.CENTER;
                    manualPositionLabel.setVisible(false);
                    //freeMoveSizeComboBox.disableIndex(0);
                    freeMoveSizeComboBox.disableIndex(1);
                    freeMoveSizeComboBox.disableIndex(2);
                    break;
                case 1: // Manual
                    freeMovePosition = FREEMOVE_POSITION.MANUAL;
                    manualPositionLabel.setVisible(true);
                    //freeMoveSizeComboBox.enableIndex(0);
                    freeMoveSizeComboBox.enableIndex(1);
                    freeMoveSizeComboBox.enableIndex(2);
                    break;
            }
//            getMoveWidget().updateVisibility();
            widgetManager.updateAllWidgets();
            getMapPanel().repaint();
        });
        freeMoveTab.add(freeMovePositionComboBox);

        freeMoveTab.add(new JLabel(getLocaleString("panel_config_tab_move_widget_tab_type")), "center");
        String[] freeMoveTypeList = {getLocaleString("panel_config_tab_move_widget_tab_type_square"),
                getLocaleString("panel_config_tab_move_widget_tab_type_round")};
        JComboBox<String> freeMoveTypeComboBox = new JComboBox<>(freeMoveTypeList);
        freeMoveTypeComboBox.setSelectedIndex((freeMoveType == FREEMOVE_TYPE.SQUARE) ? 0 : 1);
        freeMoveTypeComboBox.addActionListener(e -> {
            JComboBox comboBox = (JComboBox)e.getSource();
            int selectedIndex = comboBox.getSelectedIndex();
            switch (selectedIndex) {
                case 0: // Square
                    freeMoveType = FREEMOVE_TYPE.SQUARE;
                    break;
                case 1: // Round
                    freeMoveType = FREEMOVE_TYPE.ROUND;
                    break;
            }
//            getMoveWidget().updateVisibility();
            widgetManager.updateAllWidgets();
            getMapPanel().repaint();
        });
        freeMoveTab.add(freeMoveTypeComboBox, "wrap");

        freeMoveTab.add(new JLabel(getLocaleString("panel_config_tab_move_widget_tab_style")), "center");
        String[] freeMoveStyleList = {getLocaleString("panel_config_tab_move_widget_tab_style_solid"),
                getLocaleString("panel_config_tab_move_widget_tab_style_outline"),
                getLocaleString("panel_config_tab_move_widget_tab_style_pattern")};
        JComboBox<String> freeMovePatternComboBox = new JComboBox<>(freeMoveStyleList);
        freeMovePatternComboBox.setSelectedIndex((freeMoveStyle == FREEMOVE_STYLE.SOLID) ? 0 : (freeMoveStyle == FREEMOVE_STYLE.OUTLINE) ? 1 : 2);
        freeMovePatternComboBox.addActionListener(e -> {
            JComboBox comboBox = (JComboBox)e.getSource();
            int selectedIndex = comboBox.getSelectedIndex();
            switch (selectedIndex) {
                case 0: //
                    freeMoveStyle = FREEMOVE_STYLE.SOLID;
                    break;
                case 1: //
                    freeMoveStyle = FREEMOVE_STYLE.OUTLINE;
                    break;
                case 2:  //
                    freeMoveStyle  = FREEMOVE_STYLE.PATTERN;
                    break;
            }
//            getMoveWidget().updateVisibility();
            widgetManager.updateAllWidgets();
            getMapPanel().repaint();
        });
        freeMoveTab.add(freeMovePatternComboBox);

        freeMoveTab.add(new JLabel(getLocaleString("panel_config_tab_move_widget_tab_size")), "center");
        String[] freeMoveSizeList = {getLocaleString("panel_config_tab_move_widget_tab_size_small"),
                getLocaleString("panel_config_tab_move_widget_tab_size_medium"),
                getLocaleString("panel_config_tab_move_widget_tab_size_large")};
        freeMoveSizeComboBox = new ToggleComboBox<>(freeMoveSizeList);
        freeMoveSizeComboBox.setSelectedIndex((freeMoveSize == FREEMOVE_SIZE.SMALL) ? 0 : (freeMoveSize == FREEMOVE_SIZE.MEDIUM) ? 1 : 2);

        freeMoveSizeComboBox.addActionListener(e -> {
            JComboBox comboBox = (JComboBox)e.getSource();
            int selectedIndex = comboBox.getSelectedIndex();
            switch (selectedIndex) {
                case 0:
                    freeMoveSize = FREEMOVE_SIZE.SMALL;
                    break;
                case 1:
                    freeMoveSize = FREEMOVE_SIZE.MEDIUM;
                    break;
                case 2:
                    freeMoveSize = FREEMOVE_SIZE.LARGE;
                    break;
            }
//            getMoveWidget().updateVisibility();
            widgetManager.updateAllWidgets();
            getMapPanel().repaint();
        });
        freeMoveTab.add(freeMoveSizeComboBox, "wrap");

        manualPositionLabel = new JLabel(getLocaleString("panel_config_tab_move_widget_tab_position_manual_infotext"));
        manualPositionLabel.putClientProperty("FlatLaf.style", "arc: 10; border: 2,10,4,10,darken($Panel.background,15%)");
        manualPositionLabel.setBackground(darken(UIManager.getColor("Panel.background"),0.075f));
        manualPositionLabel.setVisible(freeMovePosition != FREEMOVE_POSITION.CENTER);
        manualPositionLabel.addPropertyChangeListener(e -> {
            if (e.getPropertyName().equals("UI") || e.getPropertyName().equals("foreground") || e.getPropertyName().equals("gridcolor")) {
                manualPositionLabel.setBackground(darken(UIManager.getColor("Panel.background"),0.075f));
            }
        });
        freeMoveTab.add(manualPositionLabel, "gaptop 5, spanx, center");


        addTab(contentPanel, freeMoveTab, "panel_config_tab_move_widget_move");

        //
        // Create the Colours Tab
        //

        JPanel colourTab = new JPanel(new MigLayout("center, insets 10 10 0 10", "[][][]", "[][]"));

        ButtonGroup colourGroup = new ButtonGroup() {
            @Override
            public void setSelected(ButtonModel m, boolean b) {
                if (b && m != null && !m.isSelected()) {
                    super.setSelected(m, true);
                } else {
                    clearSelection();
                }
            }
        };

        ActionListener colourButtonListener = e -> {
            //colourWheel.swatchGroupClearSelection();
            JToggleButton button = (JToggleButton) e.getSource();
            SwatchIcon icon = (SwatchIcon) button.getIcon();
            Color c = icon.getColor();
            colourWheel.setRGB(c);
        };

        JLabel xColourLabel = new JLabel(getLocaleString("panel_config_tab_move_widget_tab_x_axis_colour"), JLabel.TRAILING);
        SwatchIcon xColourIcon = new SwatchIcon(xAxisColor);
        JToggleButton xColourButton = new JToggleButton(xColourIcon);
        xColourButton.setOpaque(false);
        xColourButton.setBorder(BorderFactory.createEmptyBorder());
        xColourButton.setPreferredSize(new Dimension(40,26));
        xColourButton.setActionCommand(getLocaleString("panel_config_tab_move_widget_tab_x_axis_colour"));
        xColourButton.addActionListener(colourButtonListener);

        colourGroup.add(xColourButton);

        JButton xColourResetButton = makeBasicButton(null, "panel_config_tab_move_widget_tab_colour_reset", "panel_config_tab_move_widget_tab_colour_reset", colourTab, true, true);
        xColourResetButton.addActionListener(e -> {
            colourGroup.clearSelection();
            xAxisColor = DEFAULT_X_AXIS_COLOR; // Reset to default color
            xColourIcon.setColor(xAxisColor);
            xColourButton.repaint();
            colourWheel.setRGB(xAxisColor);
            widgetManager.updateAllWidgets();
            getMapPanel().repaint();
        });

        colourTab.add(xColourLabel);
        colourTab.add(xColourButton);
        colourTab.add(xColourResetButton, "wrap");

        JLabel yColourLabel = new JLabel(getLocaleString("panel_config_tab_move_widget_tab_y_axis_colour"), JLabel.TRAILING);
        SwatchIcon yColourIcon = new SwatchIcon(yAxisColor);
        JToggleButton yColourButton = new JToggleButton(yColourIcon);
        yColourButton.setPreferredSize(new Dimension(40,26));
        yColourButton.setOpaque(false);
        yColourButton.setBorder(BorderFactory.createEmptyBorder());
        yColourButton.setActionCommand(getLocaleString("panel_config_tab_move_widget_tab_y_axis_colour"));
        yColourButton.addActionListener(colourButtonListener);

        colourGroup.add(yColourButton);

        JButton yColourResetButton = makeBasicButton(null, "panel_config_tab_move_widget_tab_colour_reset", "panel_config_tab_move_widget_tab_colour_reset", colourTab, true, true);
        yColourResetButton.addActionListener(e -> {
            colourGroup.clearSelection();
            yAxisColor = DEFAULT_Y_AXIS_COLOR; // Reset to default color
            yColourIcon.setColor(yAxisColor);
            yColourButton.repaint();
            colourWheel.setRGB(yAxisColor);
            widgetManager.updateAllWidgets();
            getMapPanel().repaint();
        });

        colourTab.add(yColourLabel);
        colourTab.add(yColourButton);
        colourTab.add(yColourResetButton, " wrap");

        JLabel freeColourLabel = new JLabel(getLocaleString("panel_config_tab_move_widget_tab_free_axis_colour"), JLabel.TRAILING);
        SwatchIcon freeColourIcon = new SwatchIcon(freeMoveColor);
        JToggleButton freeColourButton = new JToggleButton(freeColourIcon);
        freeColourButton.setPreferredSize(new Dimension(40,26));
        freeColourButton.setOpaque(false);
        freeColourButton.setBorder(BorderFactory.createEmptyBorder());
        freeColourButton.setActionCommand(getLocaleString("panel_config_tab_move_widget_tab_free_axis_colour"));
        freeColourButton.addActionListener(colourButtonListener);

        JButton freeColourResetButton = makeBasicButton(null, "panel_config_tab_move_widget_tab_colour_reset", "panel_config_tab_move_widget_tab_colour_reset", colourTab, true, true);
        freeColourResetButton.addActionListener(e -> {
            colourGroup.clearSelection();
            freeMoveColor = DEFAULT_FREE_AXIS_COLOR; // Reset to default color
            freeColourIcon.setColor(freeMoveColor);
            freeColourButton.repaint();
            colourWheel.setRGB(freeMoveColor);
            widgetManager.updateAllWidgets();
            getMapPanel().repaint();
        });

        colourGroup.add(freeColourButton);

        colourTab.add(freeColourLabel);
        colourTab.add(freeColourButton);
        colourTab.add(freeColourResetButton, "wrap");

        addTab(contentPanel, colourTab, "panel_config_tab_move_widget_colour");

        add(guiPanel, "span 2 1, grow, wrap");

        //
        // Create the Colour Wheel
        //

        colourWheel = new ColorWheel();
        colourWheel.setPreferredSize(new Dimension(200,200));
        colourWheel.setMaximumSize(new Dimension(200, 200));
        colourWheel.addColorPropertyChangeListener(evt -> {
            Color newColour = evt.getColor();
            for (AbstractButton button : Collections.list(colourGroup.getElements())) {
                if (button.isSelected() && button.getIcon() instanceof SwatchIcon) {
                    SwatchIcon icon = (SwatchIcon) button.getIcon();
                    icon.setColor(newColour);
                    button.repaint();
                    if (button.getActionCommand().equals(getLocaleString("panel_config_tab_move_widget_tab_x_axis_colour"))) {
                        xAxisColor = newColour;
                    } else if (button.getActionCommand().equals(getLocaleString("panel_config_tab_move_widget_tab_y_axis_colour"))) {
                        yAxisColor = newColour;
                    } else if (button.getActionCommand().equals(getLocaleString("panel_config_tab_move_widget_tab_free_axis_colour"))) {
                        freeMoveColor = newColour;
                    }
                    break;
                }
            }
//            getMoveWidget().updateVisibility();
            widgetManager.updateAllWidgets();
            getMapPanel().repaint();
        });
        add(colourWheel, "center");

        MoveWidgetPreviewPanel moveWidgetPreviewPanel = new MoveWidgetPreviewPanel();
        moveWidgetPreviewPanel.setBorder(BorderFactory.createLineBorder(Color.LIGHT_GRAY, 1));
        add(moveWidgetPreviewPanel, "center");
    }

    // Create a JList with a DefaultListModel
    private JList<String> createStringJList() {
        listModel = new DefaultListModel<>();
        JList<String> itemList = new JList<>(listModel);
        itemList.setSelectionMode(ListSelectionModel.SINGLE_SELECTION);
        return itemList;
    }

    // Add a tab to the content panel and JList
    private void addTab(JPanel mainPanel, JPanel optionsPanel, String panelName) {
        String localizedPanelName = getLocaleString(panelName);
        mainPanel.add(optionsPanel, localizedPanelName);
        listModel.addElement(localizedPanelName);
    }



    private static class CustomListCellRenderer extends DefaultListCellRenderer {

        @Override
        public Component getListCellRendererComponent(JList<?> list, Object value, int index, boolean isSelected, boolean cellHasFocus) {
            JLabel label = (JLabel) super.getListCellRendererComponent(list, value, index, isSelected, cellHasFocus);
            label.setBorder(BorderFactory.createEmptyBorder(2, 5, 2, 5)); // Top, Left, Bottom, Right padding
            //label.setToolTipText(tooltips.get(value)); // Set tooltip text from the map

            // Remove background color
            label.setOpaque(true);
            label.setBackground(list.getParent().getBackground());
            label.setForeground(list.getForeground());

            // Remove selection background and foreground
            if (isSelected) {
                label.setBackground(darken(list.getParent().getBackground(), 0.05f));
                label.setForeground(list.getForeground());
            }

            // Create a panel to hold the label and the colored line
            JPanel panel = new JPanel(new BorderLayout());
            panel.setOpaque(false);
            panel.add(label, BorderLayout.CENTER);

            // Add the colored line panel only if the entry is selected
            if (isSelected) {
                JPanel linePanel = getJPanel(label);
                panel.add(linePanel, BorderLayout.EAST);
            }

            // Adjust the preferred size of the panel to account for the colored line
            panel.setPreferredSize(new Dimension(label.getPreferredSize().width + 10, label.getPreferredSize().height));

            return panel;
        }

        private JPanel getJPanel(JLabel label) {
            JPanel linePanel = new JPanel() {
                @Override
                protected void paintComponent(Graphics g) {
                    super.paintComponent(g);
                    Graphics2D g2d = (Graphics2D) g;
                    g2d.setColor(UIManager.getColor("TabbedPane.underlineColor")); // Set the color of the line
                    g2d.fillRect(0, 0, 3, getHeight());
                }
            };
            linePanel.setPreferredSize(new Dimension(3, (int) label.getPreferredSize().getHeight())); // Set the width of the line
            return linePanel;
        }
    }
}


