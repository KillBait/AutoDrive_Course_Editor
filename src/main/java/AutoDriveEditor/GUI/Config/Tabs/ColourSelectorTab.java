package AutoDriveEditor.GUI.Config.Tabs;

import AutoDriveEditor.GUI.Config.ColourPreviewPanel;
import AutoDriveEditor.Utils.Classes.EventTriggerNumberFilter;
import com.bric.colorpicker.ColorPicker;

import javax.swing.*;
import javax.swing.border.TitledBorder;
import javax.swing.event.ChangeListener;
import javax.swing.text.PlainDocument;
import java.awt.*;
import java.util.Enumeration;

import static AutoDriveEditor.GUI.Buttons.Options.OpenConfigButton.configListener;
import static AutoDriveEditor.GUI.Config.ColourPreviewPanel.*;
import static AutoDriveEditor.GUI.Menus.DebugMenu.Logging.LogConfigGUIInfoMenu.bDebugLogConfigGUIInfo;
import static AutoDriveEditor.Locale.LocaleManager.getLocaleString;
import static AutoDriveEditor.Utils.GUIUtils.*;
import static AutoDriveEditor.Utils.LoggerUtils.LOG;
import static AutoDriveEditor.XMLConfig.EditorXML.*;

public class ColourSelectorTab extends JPanel {

    private static ColorPicker colorPicker;
    private ButtonGroup pathGroup;
    public static JPanel previewPanel;

    private static JRadioButton regularNode;

    private JTextField redTextField;
    private JTextField greenTextField;
    private JTextField blueTextField;

    public ColourSelectorTab() {

        setLayout(new GridBagLayout());
        setComponentOrientation(ComponentOrientation.LEFT_TO_RIGHT);
        GridBagConstraints gbc = new GridBagConstraints();

        //
        // Colour picker
        //

        JPanel colourPickerPanel = new JPanel();

        gbc.fill = GridBagConstraints.BOTH;
        gbc.anchor = GridBagConstraints.FIRST_LINE_START;
        gbc.weightx = 1;
        gbc.weighty = 1;
        gbc.ipady = 120;
        gbc.gridx = 0;
        gbc.gridy = 0;
        gbc.gridwidth = 2;
        gbc.gridheight = 2;

        add(colourPickerPanel, gbc);

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
            if (bDebugLogConfigGUIInfo) LOG.info(" Action = {}", actionCommand);
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
            refreshPreviewPanel();
        });

        colourPickerPanel.add(colorPicker);

        //
        // RGB input text boxes
        //

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

        add(rgbSelectPanel, gbc);

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

        JPanel nodeSelectionPanel = new JPanel();
        nodeSelectionPanel.setLayout(new BoxLayout(nodeSelectionPanel, BoxLayout.Y_AXIS));

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
        add(nodeSelectionPanel, gbc);

        nodeSelectionPanel.add(Box.createRigidArea(new Dimension(1, 5)));

        JPanel nodePanel = new JPanel(new SpringLayout());
        TitledBorder nodeBorder = BorderFactory.createTitledBorder(getLocaleString("panel_config_tab_colours_selection_node"));
        nodeBorder.setTitleJustification(TitledBorder.CENTER);
        nodePanel.setBorder(nodeBorder);


        pathGroup = new ButtonGroup();
        regularNode = makeRadioButton("panel_config_tab_colours_node_normal", "NODE_REGULAR", "panel_config_tab_colours_node_normal_tooltip", Color.BLACK, true,false, nodePanel, pathGroup, true, configListener);
        nodePanel.add(Box.createRigidArea(new Dimension(25, 1)));
        makeRadioButton("panel_config_tab_colours_node_subprio", "NODE_SUBPRIO", "panel_config_tab_colours_node_subprio_tooltip", Color.BLACK, false, false, nodePanel, pathGroup, false, configListener);

        makeRadioButton("panel_config_tab_colours_node_selected", "NODE_SELECTED", "panel_config_tab_colours_node_selected_tooltip", Color.BLACK, false,false, nodePanel, pathGroup, false, configListener);
        nodePanel.add(Box.createRigidArea(new Dimension(25, 1)));
        makeRadioButton("panel_config_tab_colours_node_control", "NODE_CONTROL", "panel_config_tab_colours_node_control_tooltip", Color.BLACK, false,false, nodePanel, pathGroup, false, configListener);

        makeCompactGrid(nodePanel, 2, 5, 5, 5, 0, 0);

        nodeSelectionPanel.add(nodePanel);

        JPanel connectionPanel = new JPanel(new SpringLayout());
        TitledBorder connectionBorder = BorderFactory.createTitledBorder(getLocaleString("panel_config_tab_colours_selection_connections"));
        connectionBorder.setTitleJustification(TitledBorder.CENTER);
        connectionPanel.setBorder(connectionBorder);

        makeRadioButton("panel_config_tab_colours_connection_normal", "CONNECT_REGULAR", "panel_config_tab_colours_connection_normal_tooltip", Color.BLACK, true, false, connectionPanel, pathGroup, true, configListener);
        connectionPanel.add(Box.createRigidArea(new Dimension(2, 1)));
        makeRadioButton("panel_config_tab_colours_connection_subprio", "CONNECT_SUBPRIO", "panel_config_tab_colours_connection_subprio_tooltip", Color.BLACK, true, false, connectionPanel, pathGroup, false, configListener);

        makeRadioButton("panel_config_tab_colours_connection_dual", "CONNECT_DUAL", "panel_config_tab_colours_connection_dual_tooltip", Color.BLACK, true, false, connectionPanel, pathGroup, false, configListener);
        connectionPanel.add(Box.createRigidArea(new Dimension(2, 1)));
        makeRadioButton("panel_config_tab_colours_connection_reverse", "CONNECT_REVERSE", "panel_config_tab_colours_connection_reverse_tooltip", Color.BLACK, true, false, connectionPanel, pathGroup, false, configListener);

        makeRadioButton("panel_config_tab_colours_connection_dual_subprio", "CONNECT_DUAL_SUBPRIO", "panel_config_tab_colours_connection_dual_subprio_tooltip", Color.BLACK, true,false, connectionPanel, pathGroup, false, configListener);
        connectionPanel.add(Box.createRigidArea(new Dimension(2, 1)));
        makeRadioButton("panel_config_tab_colours_connection_reverse_subprio", "CONNECT_REVERSE_SUBPRIO", "panel_config_tab_colours_connection_reverse_subprio_tooltip", Color.BLACK, true,false, connectionPanel, pathGroup, false, configListener);

        makeCompactGrid(connectionPanel, 3, 5, 0, 5, 0, 0);

        nodeSelectionPanel.add(connectionPanel);

        JPanel confirmPanel = new JPanel(new FlowLayout(FlowLayout.CENTER,0,0));

        makeButton("RESET COLOURS", "panel_config_tab_colours_button_reset_tooltip", "panel_config_tab_colours_button_reset", confirmPanel, null, false, configListener, true);

        nodeSelectionPanel.add(confirmPanel);

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

        add(previewPanel, gbc);
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

    public static void refreshPreviewPanel() { if (previewPanel != null) previewPanel.repaint(); }

    public static void resetNodePanelSelection() { regularNode.setSelected(true); }

}
