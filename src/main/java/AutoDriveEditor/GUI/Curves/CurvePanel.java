package AutoDriveEditor.GUI.Curves;

import AutoDriveEditor.Classes.Interfaces.CurveInterface;
import AutoDriveEditor.Classes.UI_Components.PopoutJPanel;
import AutoDriveEditor.Classes.UI_Components.ScaleAnimIcon;
import AutoDriveEditor.Managers.CurveManager;
import AutoDriveEditor.Managers.PopupManager;
import AutoDriveEditor.RoadNetwork.Connection;
import com.formdev.flatlaf.extras.FlatSVGIcon;
import net.miginfocom.swing.MigLayout;

import javax.swing.*;
import javax.swing.border.EmptyBorder;
import java.awt.*;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.awt.event.ItemEvent;
import java.awt.event.ItemListener;

import static AutoDriveEditor.AutoDriveEditor.curveManager;
import static AutoDriveEditor.AutoDriveEditor.getMapPanel;
import static AutoDriveEditor.Classes.Util_Classes.GUIUtils.makeRadioButton;
import static AutoDriveEditor.Classes.Util_Classes.LoggerUtils.LOG;
import static AutoDriveEditor.GUI.Menus.DebugMenu.Logging.LogCurveInfoMenu.bDebugLogCurveInfo;
import static AutoDriveEditor.GUI.Menus.DebugMenu.Logging.LogGUIInfoMenu.bDebugLogGUIInfo;
import static AutoDriveEditor.GUI.TextPanel.showInTextArea;
import static AutoDriveEditor.Locale.LocaleManager.getLocaleString;
import static AutoDriveEditor.Managers.IconManager.*;
import static AutoDriveEditor.RoadNetwork.MapNode.NODE_FLAG_REGULAR;
import static AutoDriveEditor.RoadNetwork.MapNode.NODE_FLAG_SUBPRIO;
import static AutoDriveEditor.XMLConfig.EditorXML.*;
import static javax.swing.JLayeredPane.POPUP_LAYER;

public class CurvePanel extends JPanel implements ActionListener, ItemListener {

    // CurvePanel ActionCommands

    public static final String BUTTON_COMMIT_CURVE = "Confirm Curve";
    public static final String BUTTON_CANCEL_CURVE = "Cancel Curve";
    public static final String RADIOBUTTON_PATHTYPE_REGULAR = "Regular";
    public static final String RADIOBUTTON_PATHTYPE_SUBPRIO = "SubPrio";

    public static final String RADIOBUTTON_CONNECTION_NORMAL = "Normal";
    public static final String RADIOBUTTON_CONNECTION_REVERSE = "Reverse";
    public static final String RADIOBUTTON_CONNECTION_DUAL = "Dual";

    // CurvePanel GUI elements
    public static PopoutJPanel curveOptionsPanel;

    public static JRadioButton nodePriorityRegular;
    public static JRadioButton nodePrioritySubPrio;

    public static JRadioButton curvePathNormal;
    public static JRadioButton curvePathReverse;
    public static JRadioButton curvePathDual;

    public static JSlider numIterationsSlider;
    private final JTextField numIterationsTextField;

    private final JSlider numControlPointsSlider;
    private final JTextField numControlPointsTextField;

    public CurvePanel() {

        curveOptionsPanel = PopupManager.makePopupPanel(null, "", "insets 20 0 0 0, gap 5", PopoutJPanel.Justification.CENTER, false);
        curveOptionsPanel.setSticky(true);
        curveOptionsPanel.setAnimationSpeed(8);
        curveOptionsPanel.setBorderWidth(2);
        curveOptionsPanel.setBorderRadius(12);
        curveOptionsPanel.setAnchorComponent(getMapPanel());
        curveOptionsPanel.setAnchorPositionY(-25);
        getMapPanel().add(curveOptionsPanel, POPUP_LAYER);

        // createSetting a panel for path radio buttons

        JPanel optionsPanel = new JPanel(new MigLayout("gap 3, inset 0"));
        optionsPanel.setBorder(new EmptyBorder(0,0,0,0));

        FlatSVGIcon swapIcon = getSVGIcon(CURVEPANEL_SWAP_DIRECTION_ICON);
        addDarkColourFilter(true, swapIcon, new Color(10,45,140), Color.CYAN);

        ScaleAnimIcon animFlipDirectionIcon = createScaleAnimIcon(swapIcon, false);
        JButton flipDirectionButton = createAnimButton(animFlipDirectionIcon, optionsPanel, "panel_curve_swap", null, bAutoSaveEnabled, true, this);
        flipDirectionButton.addActionListener(e -> curveManager.swapCurveDirection());
        optionsPanel.add(flipDirectionButton, "span 1 4");

        optionsPanel.add(new JLabel(getLocaleString("panel_curve_label_node_priority")), "center, wrap");

        JPanel curvePriorityPanel = new JPanel(new MigLayout("inset 2"));
        curvePriorityPanel.putClientProperty("FlatLaf.style", "border: 0,0,0,0,@disabledForeground,1,16; background: darken($Panel.background,5%)");
        ButtonGroup nodePriorityGroup = new ButtonGroup();
        nodePriorityRegular = makeRadioButton("panel_curve_radio_regular", RADIOBUTTON_PATHTYPE_REGULAR, "panel_curve_radio_regular_tooltip", null,true, false, curvePriorityPanel, nodePriorityGroup, false, this);
        nodePrioritySubPrio = makeRadioButton("panel_curve_radio_subprio", RADIOBUTTON_PATHTYPE_SUBPRIO, "panel_curve_radio_subprio_tooltip", null,false, false,curvePriorityPanel, nodePriorityGroup, false, this);
        optionsPanel.add(curvePriorityPanel, "center, gapbottom 0, wrap");


        optionsPanel.add(new JLabel(getLocaleString("panel_curve_label_connection_type")), "center, span, wrap");

        
        JPanel curveTypePanel = new JPanel(new MigLayout("inset 2"));
        curveTypePanel.putClientProperty("FlatLaf.style", "border: 0,0,0,0,@disabledForeground,1,16; background: darken($Panel.background,5%)");
        ButtonGroup connectionTypeGroup = new ButtonGroup();
        curvePathNormal = makeRadioButton("panel_curve_radio_normal", RADIOBUTTON_CONNECTION_NORMAL, "panel_curve_radio_normal_tooltip", null,false, false,curveTypePanel, connectionTypeGroup, false, this);
        curvePathReverse = makeRadioButton("panel_curve_radio_reverse", RADIOBUTTON_CONNECTION_REVERSE, "panel_curve_radio_reverse_tooltip", null,false, false,curveTypePanel, connectionTypeGroup, false, this);
        curvePathDual = makeRadioButton("panel_curve_radio_dual", RADIOBUTTON_CONNECTION_DUAL, "panel_curve_radio_dual_tooltip", null,false, false,curveTypePanel, connectionTypeGroup, false, this);
        optionsPanel.add(curveTypePanel, "center");

        curveOptionsPanel.add(optionsPanel);



        // createSetting panel for slider using vertical layout

        JPanel interpolationSliderPanel = new JPanel(new MigLayout("gap 0"));
        interpolationSliderPanel.putClientProperty("FlatLaf.style", "border: 0,0,0,0,@disabledForeground,1,16; background: darken($Panel.background,5%)");

        interpolationSliderPanel.add(new JLabel(getLocaleString("panel_curve_label_interpolation")), "span 2,center, wrap");

        // createSetting the iterations slider text field and updateVisibility it with the default value
        numIterationsTextField = new JTextField();
        numIterationsTextField.setEditable(false);
        updateNumIterationTextField(curveIterationsDefault);

        // createSetting the iterations slider and set its properties
        numIterationsSlider = new JSlider(JSlider.HORIZONTAL);
        numIterationsSlider.setBorder(BorderFactory.createEmptyBorder(0,0,0,0));
        numIterationsSlider.setMinimum(1);
        numIterationsSlider.setMaximum(curveSliderMax);
        numIterationsSlider.setValue(curveIterationsDefault);
        numIterationsSlider.setSnapToTicks(true);
        numIterationsSlider.addChangeListener(e -> {
            JSlider source = (JSlider) e.getSource();
            if (source.getValueIsAdjusting()) {
                updateNumIterationTextField(numIterationsSlider.getValue());
                curveManager.setNumInterpolationPoints(numIterationsSlider.getValue());
                getMapPanel().repaint();
            }
        });
        interpolationSliderPanel.add(numIterationsSlider);
        interpolationSliderPanel.add(numIterationsTextField, "wrap");

        interpolationSliderPanel.add(new JLabel(getLocaleString("panel_curve_label_control_points")), "center, span 2, wrap");

        numControlPointsTextField = new JTextField();
        numControlPointsTextField.setEditable(false);
        updateNumControlPointsTextField(curveControlPointDefault);


        numControlPointsSlider = new JSlider(JSlider.HORIZONTAL,1, 1, curveControlPointDefault);
        numControlPointsSlider.setBorder(BorderFactory.createEmptyBorder(0,0,0,0));
        numControlPointsSlider.setSnapToTicks(true);
        numControlPointsSlider.setMajorTickSpacing(10);
        numControlPointsSlider.setMinorTickSpacing(1);
        numControlPointsSlider.addChangeListener(e -> {
            JSlider source = (JSlider) e.getSource();
            if (source.getValueIsAdjusting()) {
                updateNumControlPointsTextField(numControlPointsSlider.getValue());
                curveManager.setNumControlPoints(numControlPointsSlider.getValue());
                getMapPanel().repaint();
            }
        });
        interpolationSliderPanel.add(numControlPointsSlider);
        interpolationSliderPanel.add(numControlPointsTextField);
        curveOptionsPanel.add(interpolationSliderPanel, "gap 0 5 0 0");

        JPanel buttonPanel = new JPanel(new MigLayout("wrap 1", "[]", "10[30]"));

        ScaleAnimIcon animAcceptIcon = createScaleAnimIcon(CONFIRM_ICON, false);
        JButton commitCurve = createAnimButton(animAcceptIcon, buttonPanel, "panel_curve_confirm", null, bAutoSaveEnabled, true, this);
        commitCurve.setActionCommand(BUTTON_COMMIT_CURVE);

        ScaleAnimIcon animCancelIcon = createScaleAnimIcon(CANCEL_ICON, false);
        JButton cancelCurve = createAnimButton(animCancelIcon, buttonPanel, "panel_curve_cancel", null, bAutoSaveEnabled, true, this);
        cancelCurve.setActionCommand(BUTTON_CANCEL_CURVE);
        curveOptionsPanel.add(buttonPanel, "center");
    }

    @Override
    public void actionPerformed(ActionEvent e) {
        if (bDebugLogCurveInfo) LOG.info("CurvePanelListener ActionCommand: {}", e.getActionCommand());
        try {
            switch (e.getActionCommand()) {
                case BUTTON_COMMIT_CURVE:
                    curveManager.commitActiveCurve();
                    showInTextArea(getLocaleString("toolbar_curves_complete"), true, false);
                    break;
                case BUTTON_CANCEL_CURVE:
                    curveManager.cancelActiveCurve();
                    showInTextArea(getLocaleString("toolbar_curves_canceled"), true, false);
                    break;
            }
        } catch (Exception ignored) {}

    }

    @Override
    public void itemStateChanged(ItemEvent e) {
        AbstractButton actionButton = (AbstractButton) e.getItem();
        if (bDebugLogCurveInfo) LOG.info("CurvePanel ItemStateChange: {}", actionButton.getActionCommand());
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
        getMapPanel().repaint();
    }

    private void updateNumIterationTextField(int numIterations) {
        String labelText = numIterations + " " + getLocaleString("panel_curve_label_text");
        numIterationsTextField.setText(labelText);
        //if (numControlPointsSlider != null) numIterationsSlider.setValue(numIterations);
    }

    private void updateNumControlPointsTextField(int numIterations) {
        String labelText = numIterations + " " + getLocaleString("panel_curve_label_text");
        numControlPointsTextField.setText(labelText);
        //if (numControlPointsSlider != null) numControlPointsSlider.setValue(numIterations);
    }

    public void setMinControlNodes(int i) {
        if (bDebugLogCurveInfo) LOG.info("Setting min control nodes to {}", i);
        numControlPointsSlider.setMinimum(i);
//        numControlPointsSlider.setValue(Math.min(numControlPointsSlider.getValue(), i));
        //updateNumControlPointsTextField(numControlPointsSlider.getValue());
    }

    public void setMaxControlNodes(int i) {
        if (bDebugLogCurveInfo) LOG.info("Setting max control nodes to {}", i);
        numControlPointsSlider.setMaximum(i);
        //numControlPointsSlider.setValue(Math.min(numControlPointsSlider.getValue(), i));
        //updateNumControlPointsTextField(numControlPointsSlider.getValue());
    }

    public void showCurvePanel() {
        if (bDebugLogGUIInfo) LOG.info("Showing CurvePanel");
        curveOptionsPanel.setVisible(true);
        curveOptionsPanel.playOpeningAnimation();
        getMapPanel().repaint();
    }

    public void hideCurvePanel() {
        if (bDebugLogGUIInfo) LOG.info("Hiding CurvePanel");
        curveOptionsPanel.setVisible(false);
        curveOptionsPanel.playClosingAnimation();
        getMapPanel().repaint();
    }

    public void updateCurvePanel(CurveManager.CurveInfo curveInfo) {
        CurveInterface curve = curveInfo.getCurve();
        if (bDebugLogGUIInfo) LOG.info("Updating CurvePanel with CurveInfo: {}", curveInfo);
        updateCurvePanel(curve.getNumInterpolations(), curve.getNumCurrentControlPoints(), curve.getMinControlPoints(), curve.getMaxControlPoints(), curve.getCurveType(), curve.getCurvePriority());
        getMapPanel().repaint();

    }

    public void updateCurvePanel(CurveInterface curve) {
        if (bDebugLogGUIInfo) {
            LOG.info("Updating CurvePanel for Curve: {}", curve);
        }
        updateCurvePanel(curve.getNumInterpolations(), curve.getNumCurrentControlPoints(), curve.getMinControlPoints(), curve.getMaxControlPoints(), curve.getCurveType(), curve.getCurvePriority());
        getMapPanel().repaint();
    }

    public void updateCurvePanel(int numIterations, int numControlPoints, int numMinControlPoints, int numMaxControlPoints, Connection.ConnectionType connectionType, int nodePriority) {
        if (bDebugLogGUIInfo) LOG.info("Interpolations: {}, ControlPoints: {} ( Min {} , Max {} ), MICurveType: {}, NodePriority: {}", numIterations, numControlPoints, numMinControlPoints, numMaxControlPoints, connectionType, nodePriority);
        // Set the iterations slider and text field
        numIterationsSlider.setValue(numIterations);
        updateNumIterationTextField(numIterations);
        // Check if min and max control points are the same, if so disable the slider
        numControlPointsSlider.setEnabled(numMinControlPoints != numMaxControlPoints);
        // Set the control points slider min and max values
        setMinControlNodes(numMinControlPoints);
        setMaxControlNodes(numMaxControlPoints);
        // Set the control points slider and text field
        numControlPointsSlider.setValue(numControlPoints);
        updateNumControlPointsTextField(numControlPoints);

        switch (connectionType) {
            case REGULAR:
                curvePathNormal.setSelected(true);
                break;
            case REVERSE:
                curvePathReverse.setSelected(true);
                break;
            case DUAL:
                curvePathDual.setSelected(true);
                break;
        }
        switch (nodePriority) {
            case NODE_FLAG_REGULAR:
                nodePriorityRegular.setSelected(true);
                break;
            case NODE_FLAG_SUBPRIO:
                nodePrioritySubPrio.setSelected(true);
                break;
        }
        getMapPanel().repaint();
    }

    public void resetCurvePanel() {
        if (bDebugLogGUIInfo) LOG.info("Resetting CurvePanel");
        nodePriorityRegular.setSelected(true);
        curvePathNormal.setSelected(true);
        numIterationsSlider.setValue(curveIterationsDefault);
        updateNumIterationTextField(curveIterationsDefault);
        numControlPointsSlider.setValue(curveControlPointDefault);
        updateNumControlPointsTextField(curveControlPointDefault);
        getMapPanel().repaint();
    }

    public boolean isCurvePanelVisible() { return curveOptionsPanel.isVisible(); }
}
