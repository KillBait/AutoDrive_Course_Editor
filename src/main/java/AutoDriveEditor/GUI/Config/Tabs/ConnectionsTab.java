package AutoDriveEditor.GUI.Config.Tabs;

import com.formdev.flatlaf.extras.FlatSVGIcon;
import net.miginfocom.swing.MigLayout;

import javax.swing.*;
import java.awt.event.ItemEvent;
import java.awt.event.KeyEvent;
import java.awt.event.KeyListener;

import static AutoDriveEditor.AutoDriveEditor.getMapPanel;
import static AutoDriveEditor.Classes.Util_Classes.GUIUtils.makeCheckBox;
import static AutoDriveEditor.Classes.Util_Classes.LoggerUtils.LOG;
import static AutoDriveEditor.GUI.Menus.DebugMenu.Logging.LogConfigGUIInfoMenu.bDebugLogConfigGUIInfo;
import static AutoDriveEditor.Locale.LocaleManager.getLocaleString;
import static AutoDriveEditor.Managers.IconManager.*;
import static AutoDriveEditor.XMLConfig.EditorXML.*;

public class ConnectionsTab extends JPanel {

    public ConnectionsTab() {

        setLayout(new MigLayout("center, insets 10 30 0 30", "[]", "[]10[]20[]10[]10[]"));

        // Autosave Label

        JLabel connectionsLabel = new JLabel(getLocaleString("panel_config_list_connections"));
        add(connectionsLabel, "center, span, wrap");

        JPanel connectPanel = new JPanel(new MigLayout("center"));
        connectPanel.putClientProperty("FlatLaf.style", "border: 0,0,0,0,@disabledForeground,1,16; background: darken($Panel.background,5%)");
        FlatSVGIcon connectIcon = getSVGIcon(CONNECT_ICON);
        JLabel connectIconLabel = new JLabel(connectIcon);

        // Linear line end node creation

        JLabel createLinearLineEndNodeLabel = new JLabel(getLocaleString("panel_config_tab_connections_create_end_node") + "  ", JLabel.TRAILING);
        JCheckBox cbCreateLinearLineEndNode = makeCheckBox(createLinearLineEndNodeLabel, "CreateEndNode", null, true, bCreateLinearLineEndNode);
        cbCreateLinearLineEndNode.addItemListener(e -> bCreateLinearLineEndNode = e.getStateChange() == ItemEvent.SELECTED);
        //createLinearLineEndNodeLabel.setLabelFor(cbCreateLinearLineEndNode);
        connectPanel.add(connectIconLabel, "span 1 4, gap 10 10 0 0");
        connectPanel.add(createLinearLineEndNodeLabel, "gap 50 0 0 0");
        connectPanel.add(cbCreateLinearLineEndNode, "wrap");

        // linear line arrows

        JLabel enableFilledArrowsLabel = new JLabel(getLocaleString("panel_config_tab_connections_filled_arrows") + "  ", JLabel.TRAILING);
        JCheckBox cbEnableFilledArrows = makeCheckBox(enableFilledArrowsLabel, "FilledArrows", null, true, bFilledArrows);
        //cbEnableFilledArrows.addItemListener(e -> bFilledArrows = e.getStateChange() == ItemEvent.SELECTED);
        cbEnableFilledArrows.addItemListener(e -> {
            bFilledArrows = e.getStateChange() == ItemEvent.SELECTED;
            getMapPanel().repaint();
        });

        connectPanel.add(enableFilledArrowsLabel, "gap 50 0 0 0");
        connectPanel.add(cbEnableFilledArrows, "wrap");

        // Linear line spacing slider

        String linearLineLengthText = getLocaleString("panel_config_tab_connections_line_spacing_label");
        JLabel linearLineLengthLabel = new JLabel(linearLineLengthText, JLabel.TRAILING);

        JPanel linePanel = new JPanel(new MigLayout("center"));
        linePanel.setBorder(BorderFactory.createEmptyBorder(0,0,0,0));
        linePanel.setOpaque(false);
        //linePanel.putClientProperty("FlatLaf.style", "border: 10,10,10,10,@disabledForeground,1,16; background: darken($Panel.background,5%)");

        JSlider linearLineLengthSlider = new JSlider(SwingConstants.HORIZONTAL);
        JTextField linearLineLengthTextField = new JTextField();

        linearLineLengthTextField.setText(linearLineNodeDistance + "m");
        linearLineLengthTextField.setEditable(false);

        linearLineLengthSlider.setOpaque(false);
        linearLineLengthSlider.setSnapToTicks(false);
        linearLineLengthSlider.setMajorTickSpacing(10);
        linearLineLengthSlider.setMinorTickSpacing(5);
        linearLineLengthSlider.setMinimum(0);
        linearLineLengthSlider.setMaximum(50);
        linearLineLengthSlider.setValue(linearLineNodeDistance);


        linearLineLengthSlider.addChangeListener(e -> {
            JSlider source = (JSlider) e.getSource();
            if (source.getValueIsAdjusting()) {
                if (linearLineLengthSlider.getValue() > 5 ) {
                    linearLineNodeDistance = linearLineLengthSlider.getValue();
                } else {
                    linearLineNodeDistance = 5;
                    linearLineLengthSlider.setValue(5);
                }
                linearLineLengthTextField.setText(linearLineNodeDistance + "m");
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

        connectPanel.add(linearLineLengthLabel, "center, span, gap 0 55 10 0, wrap");
        linePanel.add(linearLineLengthSlider);
        linePanel.add(linearLineLengthTextField);
        connectPanel.add(linePanel, "span, center");

        add(connectPanel, "center, grow, wrap");

        // Hidden Connections Transparency slider

        JLabel hiddenConnectionsLabel = new JLabel(getLocaleString("panel_config_tab_connections_transparency"));
        add(hiddenConnectionsLabel, "center, span, wrap, gapbottom 10");

        JPanel transparencyPanel = new JPanel(new MigLayout());
        transparencyPanel.putClientProperty("FlatLaf.style", "border: 0,0,0,0,@disabledForeground,1,16; background: darken($Panel.background,5%)");
        FlatSVGIcon transparencyIcon = getSVGIcon(HIDDEN_ICON);
        JLabel transparencyIconLabel = new JLabel(transparencyIcon);

        JLabel hiddenNodesTransparencyLabel = new JLabel(getLocaleString("panel_config_tab_connections_transparency_level"), JLabel.TRAILING);
        JSlider hiddenNodesTransparencySlider = new JSlider(SwingConstants.HORIZONTAL);
        JTextField hiddenNodesTransparencyTextField = new JTextField();
        hiddenNodesTransparencyTextField.setEditable(false);
        hiddenNodesTransparencyTextField.setText((int)(hiddenNodesTransparencyLevel*100) + "%");

        hiddenNodesTransparencySlider.addChangeListener(e -> {
            JSlider source = (JSlider) e.getSource();
            if (source.getValueIsAdjusting()) {
                hiddenNodesTransparencyTextField.setText(source.getValue() + "%");
                hiddenNodesTransparencyLevel = (float) source.getValue() / 100;
                if (bDebugLogConfigGUIInfo) LOG.info("Hidden Nodes Transparency Level = {}", hiddenNodesTransparencyLevel);
                getMapPanel().repaint();
            }
        });
        hiddenNodesTransparencySlider.setBorder(BorderFactory.createEmptyBorder(0,0,0,0));
        hiddenNodesTransparencySlider.setSnapToTicks(true);
        hiddenNodesTransparencySlider.setMajorTickSpacing(10);
        hiddenNodesTransparencySlider.setMinimum(0);
        hiddenNodesTransparencySlider.setMaximum(100);
        hiddenNodesTransparencySlider.setValue((int)(hiddenNodesTransparencyLevel*100));
        hiddenNodesTransparencySlider.setOpaque(false);

        transparencyPanel.add(transparencyIconLabel, "span 1 2, gap 15 0 0 0");
        transparencyPanel.add(hiddenNodesTransparencyLabel, "center, span, wrap");
        transparencyPanel.add(hiddenNodesTransparencySlider, "center");
        transparencyPanel.add(hiddenNodesTransparencyTextField);

        add(transparencyPanel, "center, grow");

    }
}
