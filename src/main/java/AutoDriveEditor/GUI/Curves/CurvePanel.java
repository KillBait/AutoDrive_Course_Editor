package AutoDriveEditor.GUI.Curves;

import AutoDriveEditor.GUI.Buttons.CurveBaseButton;

import javax.swing.*;
import javax.swing.border.BevelBorder;
import javax.swing.event.ChangeEvent;
import javax.swing.event.ChangeListener;
import java.awt.*;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.awt.event.ItemEvent;
import java.awt.event.ItemListener;

import static AutoDriveEditor.AutoDriveEditor.getMapPanel;
import static AutoDriveEditor.GUI.Menus.DebugMenu.Logging.LogCurveInfoMenu.bDebugLogCurveInfo;
import static AutoDriveEditor.GUI.Menus.DebugMenu.Logging.LogGUIInfoMenu.bDebugLogGUIInfo;
import static AutoDriveEditor.Locale.LocaleManager.getLocaleString;
import static AutoDriveEditor.RoadNetwork.MapNode.NODE_FLAG_REGULAR;
import static AutoDriveEditor.RoadNetwork.MapNode.NODE_FLAG_SUBPRIO;
import static AutoDriveEditor.Utils.GUIUtils.*;
import static AutoDriveEditor.Utils.LoggerUtils.LOG;
import static AutoDriveEditor.XMLConfig.EditorXML.curveSliderDefault;
import static AutoDriveEditor.XMLConfig.EditorXML.curveSliderMax;
import static javax.swing.BoxLayout.X_AXIS;
import static javax.swing.BoxLayout.Y_AXIS;

public class CurvePanel extends JPanel implements ActionListener, ItemListener, ChangeListener {

    // CurvePanel ActionCommands

    public static final String BUTTON_COMMIT_CURVE = "Confirm Curve";
    public static final String BUTTON_CANCEL_CURVE = "Cancel Curve";
    public static final String RADIOBUTTON_PATHTYPE_REGULAR = "Regular";
    public static final String RADIOBUTTON_PATHTYPE_SUBPRIO = "SubPrio";
    public static final String RADIOBUTTON_PATHTYPE_REVERSE = "Reverse";
    public static final String RADIOBUTTON_PATHTYPE_DUAL = "Dual";

    // CurvePanel GUI elements

    public static JButton commitCurve;
    public static JButton cancelCurve;
    public static JSlider numIterationsSlider;
    public static JPanel curveOptionsPanel;
    public static JRadioButton curvePathRegular;
    public static JRadioButton curvePathSubPrio;
    public static JRadioButton curvePathReverse;
    public static JRadioButton curvePathDual;
    public static CurveBaseButton currentCurveButton;


    public CurvePanel() {

        setLayout(new BoxLayout(this, X_AXIS)); //create container ( left to right layout)
        setBorder(BorderFactory.createCompoundBorder(BorderFactory.createRaisedBevelBorder(), BorderFactory.createLoweredBevelBorder()));
        setVisible(false);
        setOpaque(true);
        setBackground(new Color(25,25,25,128));

        // create a panel for path radio buttons

        GridBagConstraints gbc = new GridBagConstraints();
        JPanel curveRadioPanel = new JPanel(new SpringLayout());
        curveRadioPanel.setBorder(BorderFactory.createBevelBorder(BevelBorder.RAISED, new Color(64,64,64), new Color(32,32,32)));
        curveRadioPanel.setOpaque(false);

        gbc.fill = GridBagConstraints.BOTH;
        gbc.anchor = GridBagConstraints.FIRST_LINE_END;
        gbc.weightx = 0;
        gbc.weighty = 0;
        gbc.ipady = 0;
        gbc.gridx = 2;
        gbc.gridy = 0;
        gbc.gridwidth = 1;
        gbc.gridheight = 2;
        add(curveRadioPanel, gbc);

        ButtonGroup pathNodeGroup = new ButtonGroup();

        curvePathRegular = makeRadioButton("panel_slider_radio_regular", RADIOBUTTON_PATHTYPE_REGULAR,"panel_slider_radio_regular_tooltip", Color.ORANGE,true, false, curveRadioPanel, pathNodeGroup, false, this);
        curvePathSubPrio = makeRadioButton("panel_slider_radio_subprio", RADIOBUTTON_PATHTYPE_SUBPRIO,"panel_slider_radio_subprio_tooltip", Color.ORANGE,false, false,curveRadioPanel, pathNodeGroup, false, this);
        curvePathReverse = makeRadioButton("panel_slider_radio_reverse", RADIOBUTTON_PATHTYPE_REVERSE,"panel_slider_radio_reverse_tooltip", Color.ORANGE,false, false,curveRadioPanel, null, false, this);
        curvePathDual = makeRadioButton("panel_slider_radio_dual", RADIOBUTTON_PATHTYPE_DUAL,"panel_slider_radio_dual_tooltip", Color.ORANGE,false, false,curveRadioPanel, null, false, this);

        add(curveRadioPanel);
        makeCompactGrid(curveRadioPanel, 2, 4, 0, 5, 5, 5);

        // create panel for slider using vertical layout

        JPanel interpolationSliderPanel = new JPanel();
        interpolationSliderPanel.setLayout(new BoxLayout(interpolationSliderPanel, Y_AXIS));
        interpolationSliderPanel.setBorder(BorderFactory.createEmptyBorder());
        interpolationSliderPanel.setOpaque(false);

        // add padding before the label to centre it

        interpolationSliderPanel.add(Box.createRigidArea(new Dimension(72, 5)));
        JLabel textLabel = new JLabel(getLocaleString("panel_slider_label"));
        textLabel.setForeground(Color.ORANGE);
        interpolationSliderPanel.add(textLabel);

        numIterationsSlider = new JSlider(JSlider.HORIZONTAL,0, curveSliderMax, curveSliderDefault);
        numIterationsSlider.setVisible(true);
        numIterationsSlider.setOpaque(false);
        numIterationsSlider.setForeground(Color.ORANGE);
        numIterationsSlider.setMajorTickSpacing(10);
        numIterationsSlider.setPaintTicks(true);
        numIterationsSlider.setPaintLabels(true);
        numIterationsSlider.addChangeListener(this);
        interpolationSliderPanel.add(numIterationsSlider);
        add(interpolationSliderPanel);

        add(Box.createRigidArea(new Dimension(8, 0)));
        commitCurve = makeImageButton("curvepanel/confirm","curvepanel/confirm_select", BUTTON_COMMIT_CURVE,"panel_slider_confirm_curve","panel_slider_confirm_curve_alt", this, true, this);
        add(Box.createRigidArea(new Dimension(8, 0)));
        cancelCurve = makeImageButton("curvepanel/cancel","curvepanel/cancel_select", BUTTON_CANCEL_CURVE,"panel_slider_cancel_curve","panel_slider_cancel_curve_alt", this, true, this);
        add(Box.createRigidArea(new Dimension(8, 0)));

    }

    public static void setCurvePanelCurrentButton(CurveBaseButton currentButton) {
        if (bDebugLogGUIInfo) LOG.info("Setting Current CurveButton to {}", currentButton.getButtonID());
        currentCurveButton = currentButton;
    }

    @Override
    public void actionPerformed(ActionEvent e) {
        if (bDebugLogCurveInfo) LOG.info("CurvePanelListener ActionCommand: {}", e.getActionCommand());
        try {
            switch (e.getActionCommand()) {
                case BUTTON_COMMIT_CURVE:
                    currentCurveButton.commitCurve();
                    break;
                case BUTTON_CANCEL_CURVE:
                    currentCurveButton.cancelCurve();
                    break;
            }
        } catch (Exception ignored) {}

    }

    @Override
    public void itemStateChanged(ItemEvent e) {
        AbstractButton actionButton = (AbstractButton) e.getItem();
        if (bDebugLogCurveInfo) LOG.info("CurvePanel ItemStateChange: {}", actionButton.getActionCommand());
        if (bDebugLogCurveInfo) LOG.info("selectedButton = {}" , currentCurveButton);
        switch (actionButton.getActionCommand()) {
            case RADIOBUTTON_PATHTYPE_REGULAR:
                currentCurveButton.setNodeType(NODE_FLAG_REGULAR);
                break;
            case RADIOBUTTON_PATHTYPE_SUBPRIO:
                currentCurveButton.setNodeType(NODE_FLAG_SUBPRIO);
                break;
            case RADIOBUTTON_PATHTYPE_REVERSE:
                if (actionButton.isSelected()) {
                    curvePathDual.setSelected(false);
                }
                currentCurveButton.setReversePath(actionButton.isSelected());
                currentCurveButton.setDualPath(false);
                break;
            case RADIOBUTTON_PATHTYPE_DUAL:
                if (actionButton.isSelected()) {
                    curvePathReverse.setSelected(false);

                }
                currentCurveButton.setDualPath(actionButton.isSelected());
                currentCurveButton.setReversePath(false);
                break;
        }
        getMapPanel().repaint();
    }

    @Override
    public void stateChanged(ChangeEvent e) {
        JSlider source = (JSlider) e.getSource();
        if (source.getValueIsAdjusting()) {
            int value = source.getValue();
            currentCurveButton.setNumInterpolationPoints(value + 1);
            getMapPanel().repaint();
        }
    }
}
