package AutoDriveEditor.GUI.Config.Tabs;

import AutoDriveEditor.RoadNetwork.Connection;
import com.formdev.flatlaf.extras.FlatSVGIcon;
import net.miginfocom.swing.MigLayout;

import javax.swing.*;
import java.awt.*;
import java.awt.event.ItemEvent;
import java.awt.event.ItemListener;

import static AutoDriveEditor.AutoDriveEditor.curveManager;
import static AutoDriveEditor.Classes.Util_Classes.GUIUtils.makeRadioButton;
import static AutoDriveEditor.Classes.Util_Classes.LoggerUtils.LOG;
import static AutoDriveEditor.GUI.Curves.CurvePanel.*;
import static AutoDriveEditor.GUI.Menus.DebugMenu.Logging.LogCurveInfoMenu.bDebugLogCurveInfo;
import static AutoDriveEditor.Locale.LocaleManager.getLocaleString;
import static AutoDriveEditor.Managers.IconManager.*;
import static AutoDriveEditor.RoadNetwork.MapNode.NODE_FLAG_REGULAR;
import static AutoDriveEditor.RoadNetwork.MapNode.NODE_FLAG_SUBPRIO;
import static AutoDriveEditor.XMLConfig.EditorXML.*;

public class CurvesTab extends JPanel implements ItemListener {

    public static JRadioButton priorityRegular;
    public static JRadioButton prioritySubPrio;

    public static JRadioButton typeNormal;
    public static JRadioButton typeReverse;
    public static JRadioButton typeDual;

    public CurvesTab() {

        setLayout(new MigLayout("center, insets 10 30 30 30", "[]", "[]15[]15[]15[]"));

        // Curves Label
        JLabel curvesLabel = new JLabel(getLocaleString("panel_config_tab_curves"));
        add(curvesLabel, "center, wrap");

        // Curve Panel
        JPanel curvesPanel = new JPanel(new MigLayout("center" , "[]", "[]0[]5[]0[]5[]5[]5[]5[]0[]"));
        curvesPanel.putClientProperty("FlatLaf.style", "border: 0,0,0,0,@disabledForeground,1,16; background: darken($Panel.background,5%)");
        FlatSVGIcon curveIcon = getSVGIcon(CURVE_ICON);
        addDarkColourFilter(true, curveIcon, new Color(0, 54,152), new Color(0, 180, 30));
        JLabel curveIconLabel = new JLabel(curveIcon);

        //
        // Curve nodes maximum
        //

        JLabel maxCurveNodesLabel = new JLabel(getLocaleString("panel_config_tab_curves_maxnodes"), JLabel.TRAILING);
        JSlider maxCurveNodesSlider = new JSlider(SwingConstants.HORIZONTAL);
        JTextField maxCurveNodesTextField = new JTextField();
        maxCurveNodesTextField.setText(curveSliderMax + " Nodes");
        maxCurveNodesTextField.setEditable(false);



        maxCurveNodesSlider.setBorder(BorderFactory.createEmptyBorder(0,0,0,0));
        maxCurveNodesSlider.setSnapToTicks(false);
        maxCurveNodesSlider.setMajorTickSpacing(5);
        maxCurveNodesSlider.setMinimum(0);
        maxCurveNodesSlider.setMaximum(50);
        maxCurveNodesSlider.setValue(curveSliderMax);

        curvesPanel.add(curveIconLabel, "span 1 4");

        curvesPanel.add(maxCurveNodesLabel, "center, span 3 1, gap 0 " + curveIcon.getWidth() + " 0 0, wrap");
        curvesPanel.add(maxCurveNodesSlider, "gap 0");
        curvesPanel.add(maxCurveNodesTextField, "wrap");


        //
        // Curve nodes default
        //

        JLabel defaultCurveNodesLabel = new JLabel(getLocaleString("panel_config_tab_curves_defaultnodes"), JLabel.TRAILING);
        JSlider defaultCurveNodesSlider = new JSlider(SwingConstants.HORIZONTAL);
        JTextField defaultCurveNodesTextField = new JTextField();
        defaultCurveNodesTextField.setText(curveIterationsDefault + " Nodes");
        defaultCurveNodesTextField.setEditable(false);


        maxCurveNodesSlider.addChangeListener(e -> {
            JSlider source = (JSlider) e.getSource();
            if (source.getValueIsAdjusting()) {
                int maxNodeValue = maxCurveNodesSlider.getValue();
                curveSliderMax = Math.max(maxNodeValue, 1);
                if (curveSliderMax < curveIterationsDefault) {
                    curveIterationsDefault = curveSliderMax;
                    defaultCurveNodesSlider.setValue(curveIterationsDefault);
                    defaultCurveNodesTextField.setText(curveIterationsDefault + " Nodes");
                }
                // set the maximum number of iterations in the curve panel
                numIterationsSlider.setMaximum(curveSliderMax);
                maxCurveNodesTextField.setText(curveSliderMax + " Nodes");
            }
        });


        defaultCurveNodesSlider.addChangeListener(e -> {
            JSlider source = (JSlider) e.getSource();
            if (source.getValueIsAdjusting()) {
                int defaultValue = defaultCurveNodesSlider.getValue();
                curveIterationsDefault = Math.max(defaultValue, 1);
                if (curveIterationsDefault > curveSliderMax) {
                    curveSliderMax = curveIterationsDefault;
                    maxCurveNodesSlider.setValue(curveSliderMax);
                    maxCurveNodesTextField.setText(curveSliderMax + " Nodes");
                }
                defaultCurveNodesTextField.setText(curveIterationsDefault + " Nodes");
            }
        });

        defaultCurveNodesSlider.setBorder(BorderFactory.createEmptyBorder(0,0,0,0));
        defaultCurveNodesSlider.setSnapToTicks(false);
        defaultCurveNodesSlider.setMajorTickSpacing(5);
        defaultCurveNodesSlider.setMinimum(0);
        defaultCurveNodesSlider.setMaximum(50);
        defaultCurveNodesSlider.setValue(curveIterationsDefault);


        curvesPanel.add(defaultCurveNodesLabel,"center, span 3 1, gap 0 " + curveIcon.getWidth() + " 0 0, wrap");
        curvesPanel.add(defaultCurveNodesSlider, "gap 0");
        curvesPanel.add(defaultCurveNodesTextField, "gap 0, wrap");

        JLabel priorityLabel = new JLabel(getLocaleString("panel_config_tab_curves_connection_priority"), JLabel.TRAILING);

        JPanel priorityPanel = new JPanel(new MigLayout("center, insets 3"));
        priorityPanel.putClientProperty("FlatLaf.style", "border: 0,0,0,0,@disabledForeground,1,16; background: darken($Panel.background,10%)");
        ButtonGroup priorityGroup = new ButtonGroup();
        priorityRegular = makeRadioButton("panel_curve_radio_regular", RADIOBUTTON_PATHTYPE_REGULAR, "panel_curve_radio_regular_tooltip", null,curveNodeDefaultPriority == NODE_FLAG_REGULAR, false, priorityPanel, priorityGroup, false, null);
        priorityRegular.addItemListener(e -> curveNodeDefaultPriority = NODE_FLAG_REGULAR);
        prioritySubPrio = makeRadioButton("panel_curve_radio_subprio", RADIOBUTTON_PATHTYPE_SUBPRIO, "panel_curve_radio_subprio_tooltip", null,curveNodeDefaultPriority == NODE_FLAG_SUBPRIO, false,priorityPanel, priorityGroup, false, null);
        prioritySubPrio.addItemListener(e -> curveNodeDefaultPriority = NODE_FLAG_SUBPRIO);


        JPanel typePanel = new JPanel(new MigLayout("center, insets 3"));
        typePanel.putClientProperty("FlatLaf.style", "border: 0,0,0,0,@disabledForeground,1,16; background: darken($Panel.background,10%)");
        ButtonGroup typeGroup = new ButtonGroup();
        JLabel typeLabel = new JLabel(getLocaleString("panel_config_tab_curves_connection_type"), JLabel.TRAILING);
        typeNormal = makeRadioButton("panel_curve_radio_normal", RADIOBUTTON_CONNECTION_NORMAL, "panel_curve_radio_normal_tooltip", null,curveDefaultConnection == Connection.ConnectionType.REGULAR, false,typePanel, typeGroup, false, null);
        typeNormal.addItemListener(e -> curveDefaultConnection = Connection.ConnectionType.REGULAR);
        typeReverse = makeRadioButton("panel_curve_radio_reverse", RADIOBUTTON_CONNECTION_REVERSE, "panel_curve_radio_reverse_tooltip", null,curveDefaultConnection == Connection.ConnectionType.REVERSE, false,typePanel, typeGroup, false, null);
        typeReverse.addItemListener(e -> curveDefaultConnection = Connection.ConnectionType.REVERSE);
        typeDual = makeRadioButton("panel_curve_radio_dual", RADIOBUTTON_CONNECTION_DUAL, "panel_curve_radio_dual_tooltip", null,curveDefaultConnection == Connection.ConnectionType.DUAL, false, typePanel, typeGroup, false, null);
        typeDual.addItemListener(e -> curveDefaultConnection = Connection.ConnectionType.DUAL);

        curvesPanel.add(priorityLabel, "center, span 3 1, gap 0, wrap");
        curvesPanel.add(priorityPanel, "center, span 3 1, gap 0, wrap");
        curvesPanel.add(typeLabel, "center, span 3 1, gap 0, wrap");
        curvesPanel.add(typePanel, "center, span 3 1, gap 0, wrap");
        add(curvesPanel, "center, grow, wrap");

        // control point movement scaler

        JLabel scalerLabel = new JLabel(getLocaleString("panel_config_tab_curves_movement"));
        add(scalerLabel, "center, wrap");

        JPanel scalerPanel = new JPanel(new MigLayout("center, gap 0 0 0 0"));
        scalerPanel.putClientProperty("FlatLaf.style", "border: 0,0,0,0,@disabledForeground,1,16; background: darken($Panel.background,5%)");
        FlatSVGIcon scalerIcon = getSVGIcon(MOVEMENT_ICON);
        JLabel scalerIconLabel = new JLabel(scalerIcon);

        JLabel curveControlPointScalerLabel = new JLabel(getLocaleString("panel_config_tab_curves_controlpointscaler"), JLabel.TRAILING);
        JSlider curveControlPointScalerSlider = new JSlider(SwingConstants.HORIZONTAL);
        curveControlPointScalerSlider.setBorder(BorderFactory.createEmptyBorder(0,0,0,0));
        curveControlPointScalerSlider.setSnapToTicks(true);
        curveControlPointScalerSlider.setMajorTickSpacing(1);
        curveControlPointScalerSlider.setMinimum(1);
        curveControlPointScalerSlider.setMaximum(10);
        curveControlPointScalerSlider.setValue(controlPointMoveScaler);

        JTextField curveControlPointScalerTextField = new JTextField();
        curveControlPointScalerTextField.setText("x " + controlPointMoveScaler);
        curveControlPointScalerTextField.setEditable(false);
        curveControlPointScalerTextField.setHorizontalAlignment(SwingConstants.CENTER);

        curveControlPointScalerSlider.addChangeListener(e -> {
            JSlider source = (JSlider) e.getSource();
            if (source.getValueIsAdjusting()) {
                int newValue = curveControlPointScalerSlider.getValue();
                controlPointMoveScaler = Math.max(newValue, 1);
                curveControlPointScalerTextField.setText("x " + controlPointMoveScaler);
            }
        });



        scalerPanel.add(scalerIconLabel, "gap 0 10 0 0, span 1 2");
        scalerPanel.add(curveControlPointScalerLabel, "span 2 1, wrap, gap bottom 5, gap left 30");
        scalerPanel.add(curveControlPointScalerSlider);
        scalerPanel.add(curveControlPointScalerTextField, "center, wrap");

        add(scalerPanel, "center, grow");
    }

    @Override
    public void itemStateChanged(ItemEvent e) {
        AbstractButton actionButton = (AbstractButton) e.getItem();
        if (bDebugLogCurveInfo) LOG.info("CurveTab ItemStateChange: {}", actionButton.getActionCommand());
        switch (actionButton.getActionCommand()) {
            case RADIOBUTTON_PATHTYPE_REGULAR:
                curveManager.setNodePriority(NODE_FLAG_REGULAR);
                break;
            case RADIOBUTTON_PATHTYPE_SUBPRIO:
                curveManager.setNodePriority(NODE_FLAG_SUBPRIO);
                break;
            case RADIOBUTTON_CONNECTION_NORMAL:
                curveManager.setCurveType(Connection.ConnectionType.REGULAR);
                break;
            case RADIOBUTTON_CONNECTION_REVERSE:
                curveManager.setCurveType(Connection.ConnectionType.REVERSE);
                break;
            case RADIOBUTTON_CONNECTION_DUAL:
                curveManager.setCurveType(Connection.ConnectionType.DUAL);
                break;
        }
    }
}
