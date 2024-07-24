package AutoDriveEditor.GUI;

import javax.swing.*;
import java.awt.*;

import static AutoDriveEditor.Utils.ConversionUtils.ColorToHex;
import static AutoDriveEditor.Utils.LoggerUtils.LOG;
import static AutoDriveEditor.XMLConfig.EditorXML.autoSaveInterval;
import static AutoDriveEditor.XMLConfig.EditorXML.bAutoSaveEnabled;

public class TextPanel extends JPanel {

    // Text Area

    public static JTextArea textArea;

    // AutoSave panel labels

    private static JLabel autosaveStatusLabel;
    private static JLabel autosaveTimeLabel;

    // InfoPanel labels

    private static JLabel imageLoadedLabel;
    private static JLabel heightMapLoadedLabel;
    private static JLabel currentMapScaleLabel;

    public TextPanel() {
        setLayout(new BoxLayout(this, BoxLayout.X_AXIS));
        setBorder(BorderFactory.createCompoundBorder(BorderFactory.createLoweredBevelBorder(), BorderFactory.createRaisedBevelBorder()));

        //
        // Set up the text area
        //

        textArea = new JTextArea("Welcome to the AutoDrive Editor... Load a config to start editing..\n", 3, 20);
        JScrollPane scrollPane = new JScrollPane(textArea);
        scrollPane.setVerticalScrollBarPolicy(JScrollPane.VERTICAL_SCROLLBAR_NEVER);
        textArea.setEditable(false);
        textArea.setFont(new Font("Monospaced", Font.PLAIN, 12));
        add(scrollPane);

        //
        // Set up the autosave info panel
        //

        JPanel autosaveInfoPanel = new JPanel(new GridBagLayout());
        autosaveInfoPanel.setBorder(BorderFactory.createEtchedBorder());
        Dimension autosaveInfoPanelSize = new Dimension(120, 70);
        autosaveInfoPanel.setPreferredSize(autosaveInfoPanelSize);
        autosaveInfoPanel.setMaximumSize(autosaveInfoPanelSize);
        autosaveInfoPanel.setMinimumSize(autosaveInfoPanelSize);
        autosaveInfoPanel.setBackground(Color.LIGHT_GRAY);

        GridBagConstraints autogbc = new GridBagConstraints();
        autogbc.fill = GridBagConstraints.HORIZONTAL;
        autogbc.anchor = GridBagConstraints.WEST;
        autogbc.insets = new Insets(0, 3, 5, 3);

        JLabel autosaveHeaderLabel = new JLabel("<html><u><b>AutoSave");
        autosaveHeaderLabel.setHorizontalAlignment(SwingConstants.CENTER);
        autogbc.gridx = 0;
        autogbc.gridy = 0;
        autogbc.gridwidth = 2;
        autosaveInfoPanel.add(autosaveHeaderLabel, autogbc);

        autosaveStatusLabel = new JLabel("AutoSave : ");
        autosaveStatusLabel.setHorizontalAlignment(SwingConstants.LEFT);
        autogbc.gridx = 0;
        autogbc.gridy = 1;
        updateAutosaveStatusLabel(bAutoSaveEnabled);
        autosaveInfoPanel.add(autosaveStatusLabel, autogbc);

        autosaveTimeLabel = new JLabel("Interval : ");
        autosaveTimeLabel.setHorizontalAlignment(SwingConstants.LEFT);
        autogbc.gridx = 0;
        autogbc.gridy = 2;
        setAutosaveIntervalLabel();
        autosaveInfoPanel.add(autosaveTimeLabel, autogbc);

        //
        // Add the autosaveInfoPanel
        //

        add(autosaveInfoPanel);

        //
        // Set up the map info panel
        //

        JPanel mapInfoPanel = new JPanel(new GridBagLayout());
        mapInfoPanel.setBorder(BorderFactory.createEtchedBorder());
        Dimension mapInfoPanelSize = new Dimension(150, 70);
        mapInfoPanel.setPreferredSize(mapInfoPanelSize);
        mapInfoPanel.setMaximumSize(mapInfoPanelSize);
        mapInfoPanel.setMinimumSize(mapInfoPanelSize);
        mapInfoPanel.setBackground(Color.LIGHT_GRAY);

        autogbc.insets = new Insets(1, 3, 1, 3);
        autogbc.anchor = GridBagConstraints.EAST;

        JLabel mapInfoHeaderLabel = new JLabel("<html><u><b>MapInfo");
        mapInfoHeaderLabel.setHorizontalAlignment(SwingConstants.CENTER);
        autogbc.gridx = 0;
        autogbc.gridy = 0;
        autogbc.gridwidth = 2;
        mapInfoPanel.add(mapInfoHeaderLabel, autogbc);

        imageLoadedLabel = new JLabel("Map Image : ");
        imageLoadedLabel.setHorizontalAlignment(SwingConstants.LEFT);
        autogbc.gridx = 0;
        autogbc.gridy = 1;
        mapInfoPanel.add(imageLoadedLabel, autogbc);

        heightMapLoadedLabel = new JLabel("HeightMap : ");
        heightMapLoadedLabel.setHorizontalAlignment(SwingConstants.LEFT);
        autogbc.gridx = 0;
        autogbc.gridy = 2;
        mapInfoPanel.add(heightMapLoadedLabel, autogbc);

        currentMapScaleLabel = new JLabel("Map Scale : ");
        currentMapScaleLabel.setHorizontalAlignment(SwingConstants.LEFT);
        autogbc.gridx = 0;
        autogbc.gridy = 3;
        mapInfoPanel.add(currentMapScaleLabel, autogbc);

        add(mapInfoPanel);
    }


    private static void setLabel(JLabel label, String text, Color colour) {
        if (label != null) {
            if (colour != null) label.setForeground(colour);
            label.setText(text);
        }
    }

    public static void setImageLoadedLabel(String text, Color colour) {
        String labelText = "<html>Map Image : <b><font color=" + ColorToHex(colour,true) + ">" + text;
        setLabel(imageLoadedLabel, labelText, null);
    }

    public static void setHeightMapLoadedLabel(String text, Color colour) {
        String labelText = "<html>HeightMap : <b><font color=" + ColorToHex(colour,true) + ">" + text;
        setLabel(heightMapLoadedLabel, labelText, null);
    }

    public static void setCurrentMapScaleLabel(String text) {
        String labelText = "<html>Map Scale : <b>" + text;
        setLabel(currentMapScaleLabel, labelText, null);
    }

    public static void updateAutosaveStatusLabel() { updateAutosaveStatusLabel(bAutoSaveEnabled); }
    public static void updateAutosaveStatusLabel(boolean enabled) {
        String text = "<html>Status : <b>" + ((enabled)? "<font color=#006400>Enabled" : "<font color=#C80000>Disabled");
        setLabel(autosaveStatusLabel, text, null);
    }

    public static void setAutosaveIntervalLabel() {
        String text = "<html>Interval : <b>" + autoSaveInterval + " </b>min";
        setLabel(autosaveTimeLabel, text, null);
    }

    public static void showInTextArea(String text, boolean clearAll, boolean outputToLogFile) {
        if (clearAll) {
            textArea.selectAll();
            textArea.replaceSelection(null);
        }
        if (outputToLogFile) LOG.info(text);
        textArea.append(text + "\n");
    }



}
