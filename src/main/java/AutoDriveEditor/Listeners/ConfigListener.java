package AutoDriveEditor.Listeners;

import javax.swing.*;
import java.awt.*;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.awt.event.ItemEvent;
import java.awt.event.ItemListener;

import static AutoDriveEditor.AutoDriveEditor.editor;
import static AutoDriveEditor.GUI.Config.ColourPreviewPanel.*;
import static AutoDriveEditor.GUI.Config.ConfigGUI.*;
import static AutoDriveEditor.GUI.MenuBuilder.bDebugLogGUIInfo;
import static AutoDriveEditor.Locale.LocaleManager.getLocaleString;
import static AutoDriveEditor.Utils.LoggerUtils.LOG;
import static AutoDriveEditor.XMLConfig.EditorXML.*;

public class ConfigListener implements ItemListener, ActionListener {


    @Override
    public void actionPerformed(ActionEvent e) {
        AbstractButton selectionPanelButton = (AbstractButton) e.getSource();
        if (bDebugLogGUIInfo) LOG.info("Config ActionPerformed: {}", selectionPanelButton.getActionCommand());
        if ("RESET COLOURS".equals(selectionPanelButton.getActionCommand())) {
            int response = JOptionPane.showConfirmDialog(editor, getLocaleString("dialog_reset_colours"), getLocaleString("dialog_reset_colours_title"), JOptionPane.YES_NO_OPTION, JOptionPane.QUESTION_MESSAGE);
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
                regularNode.setSelected(true);
                LOG.info("Colours reset");
            }
        }
    }

    @Override
    public void itemStateChanged(ItemEvent e) {
        AbstractButton selectionPanelButton = (AbstractButton) e.getItem();
        if (bDebugLogGUIInfo) LOG.info("Config ItemStateChange: {}", selectionPanelButton.getActionCommand());
        switch (selectionPanelButton.getActionCommand()) {
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
        previewPanel.repaint();
    }
}
