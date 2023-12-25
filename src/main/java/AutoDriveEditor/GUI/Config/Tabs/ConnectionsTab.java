package AutoDriveEditor.GUI.Config.Tabs;

import javax.swing.*;
import java.awt.*;
import java.awt.event.ItemEvent;
import java.awt.event.KeyEvent;
import java.awt.event.KeyListener;

import static AutoDriveEditor.AutoDriveEditor.getMapPanel;
import static AutoDriveEditor.GUI.Menus.DebugMenu.Logging.LogConfigGUIInfoMenu.bDebugLogConfigGUIInfo;
import static AutoDriveEditor.Locale.LocaleManager.getLocaleString;
import static AutoDriveEditor.Utils.GUIUtils.makeCheckBox;
import static AutoDriveEditor.Utils.LoggerUtils.LOG;
import static AutoDriveEditor.XMLConfig.EditorXML.*;

public class ConnectionsTab extends JPanel {

    public ConnectionsTab() {

        setLayout(new GridLayout(4,2,0,0));

        // Linear line end node creation

        JLabel createLinearLineEndNodeLabel = new JLabel(getLocaleString("panel_config_tab_connections_create_end_node") + "  ", JLabel.TRAILING);
        JCheckBox cbCreateLinearLineEndNode = makeCheckBox(createLinearLineEndNodeLabel, "CreateEndNode", null, true, bCreateLinearLineEndNode);
        cbCreateLinearLineEndNode.addItemListener(e -> bCreateLinearLineEndNode = e.getStateChange() == ItemEvent.SELECTED);
        createLinearLineEndNodeLabel.setLabelFor(cbCreateLinearLineEndNode);
        add(createLinearLineEndNodeLabel);
        add(cbCreateLinearLineEndNode);

        // linear line arrows

        JLabel enableFilledArrowsLabel = new JLabel(getLocaleString("panel_config_tab_connections_filled_arrows") + "  ", JLabel.TRAILING);
        JCheckBox cbEnableFilledArrows = makeCheckBox(enableFilledArrowsLabel, "FilledArrows", null, true, bFilledArrows);
        //cbEnableFilledArrows.addItemListener(e -> bFilledArrows = e.getStateChange() == ItemEvent.SELECTED);
        cbEnableFilledArrows.addItemListener(e -> {
            bFilledArrows = e.getStateChange() == ItemEvent.SELECTED;
            getMapPanel().repaint();
        });
        enableFilledArrowsLabel.setLabelFor(cbEnableFilledArrows);
        add(enableFilledArrowsLabel);
        add(cbEnableFilledArrows);

        // Linear line spacing slider

        String lineLabelText = getLocaleString("panel_config_tab_connections_line_spacing_label") + " ( " + linearLineNodeDistance + "m )  ";

        JLabel linearLineLengthLabel = new JLabel(lineLabelText, JLabel.TRAILING);
        JSlider linearLineLengthSlider = new JSlider(SwingConstants.HORIZONTAL);

        linearLineLengthSlider.addChangeListener(e -> {
            JSlider source = (JSlider) e.getSource();
            if (source.getValueIsAdjusting()) {
                if (linearLineLengthSlider.getValue() > 5 ) {
                    linearLineNodeDistance = linearLineLengthSlider.getValue();
                } else {
                    linearLineNodeDistance = 5;
                    linearLineLengthSlider.setValue(5);
                }
                String text = getLocaleString("panel_config_tab_connections_line_spacing_label") + " ( " + linearLineNodeDistance + "m )  ";
                linearLineLengthLabel.setText(text);
            }
        });

        // a keyboard listener for JSliders to stop the arrow keys
        // movement, we just consume the event and do nothing

        linearLineLengthSlider.addKeyListener(new KeyListener() {
            @Override
            public void keyTyped(KeyEvent e) {e.consume();}

            @Override
            public void keyPressed(KeyEvent e) {e.consume();}

            @Override
            public void keyReleased(KeyEvent e) {e.consume();}
        });

        linearLineLengthSlider.setPreferredSize(new Dimension(185,40));
        linearLineLengthSlider.setBorder(BorderFactory.createEmptyBorder(0,0,0,0));
        linearLineLengthSlider.setPaintTicks(true);
        linearLineLengthSlider.setSnapToTicks(false);
        linearLineLengthSlider.setMajorTickSpacing(10);
        linearLineLengthSlider.setMinorTickSpacing(5);
        linearLineLengthSlider.setPaintLabels(true);
        linearLineLengthSlider.setMinimum(0);
        linearLineLengthSlider.setMaximum(50);
        linearLineLengthSlider.setValue(linearLineNodeDistance);

        add(linearLineLengthLabel);
        add(linearLineLengthSlider);

        // Hidden Connections Transparency slider

        JLabel lblHiddenNodesTransparencyLevel = new JLabel(getLocaleString("panel_config_tab_connections_transparency_level") + "  ", JLabel.TRAILING);
        JSlider slHiddenNodesTransparencyLevel = new JSlider(SwingConstants.HORIZONTAL);
        slHiddenNodesTransparencyLevel.addChangeListener(e -> {
            JSlider source = (JSlider) e.getSource();
            if (source.getValueIsAdjusting()) {
                hiddenNodesTransparencyLevel = (float) source.getValue() / 100;
                if (bDebugLogConfigGUIInfo) LOG.info("Hidden Nodes Transparency Level = {}", hiddenNodesTransparencyLevel);
                getMapPanel().repaint();
            }
        });
        slHiddenNodesTransparencyLevel.setBorder(BorderFactory.createEmptyBorder(0,0,0,0));
        slHiddenNodesTransparencyLevel.setPaintTicks(true);
        slHiddenNodesTransparencyLevel.setSnapToTicks(true);
        slHiddenNodesTransparencyLevel.setMajorTickSpacing(10);
        slHiddenNodesTransparencyLevel.setPaintLabels(true);
        slHiddenNodesTransparencyLevel.setMinimum(0);
        slHiddenNodesTransparencyLevel.setMaximum(100);
        slHiddenNodesTransparencyLevel.setValue((int)(hiddenNodesTransparencyLevel*100));

        add(lblHiddenNodesTransparencyLevel);
        add(slHiddenNodesTransparencyLevel);

    }
}
