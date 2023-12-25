package AutoDriveEditor.GUI.Config.Tabs;

import javax.swing.*;
import java.awt.*;

import static AutoDriveEditor.GUI.Curves.CurvePanel.numIterationsSlider;
import static AutoDriveEditor.Locale.LocaleManager.getLocaleString;
import static AutoDriveEditor.XMLConfig.EditorXML.*;

public class CurvesTab extends JPanel {

    public CurvesTab() {

        setLayout(new GridLayout(4,2,0,0));

        // Curve nodes maximum

        JLabel maxCurveNodesLabel = new JLabel(getLocaleString("panel_config_tab_curves_maxnodes") + " ( " + curveSliderMax + " )  ", JLabel.TRAILING);
        JSlider maxCurveNodesSlider = new JSlider(SwingConstants.HORIZONTAL);

        JLabel defaultCurveNodesLabel = new JLabel(getLocaleString("panel_config_tab_curves_defaultnodes") + " ( " + curveSliderDefault + " )  ", JLabel.TRAILING);
        JSlider defaultCurveNodesSlider = new JSlider(SwingConstants.HORIZONTAL);

        maxCurveNodesSlider.addChangeListener(e -> {
            JSlider source = (JSlider) e.getSource();
            if (source.getValueIsAdjusting()) {
                int maxNodeValue = maxCurveNodesSlider.getValue();
                if (maxNodeValue < 1 ) maxNodeValue = 1;
                curveSliderMax = maxNodeValue;
                if (curveSliderDefault > curveSliderMax) {
                    curveSliderDefault = curveSliderMax;
                    defaultCurveNodesSlider.setValue(curveSliderDefault);
                    String text = getLocaleString("panel_config_tab_curves_defaultnodes") + " ( " + curveSliderDefault + " )  ";
                    defaultCurveNodesLabel.setText(text);
                }
                numIterationsSlider.setMaximum(curveSliderMax);
                String text = getLocaleString("panel_config_tab_curves_maxnodes") + " ( " + curveSliderMax + " )  ";
                maxCurveNodesLabel.setText(text);
            }
        });

        maxCurveNodesSlider.setBorder(BorderFactory.createEmptyBorder(0,0,0,0));
        maxCurveNodesSlider.setPaintTicks(true);
        maxCurveNodesSlider.setSnapToTicks(false);
        maxCurveNodesSlider.setMajorTickSpacing(5);
        maxCurveNodesSlider.setPaintLabels(true);
        maxCurveNodesSlider.setMinimum(0);
        maxCurveNodesSlider.setMaximum(50);
        maxCurveNodesSlider.setValue(curveSliderMax);

        add(maxCurveNodesLabel);
        add(maxCurveNodesSlider);

        // Curve nodes default

        defaultCurveNodesSlider.addChangeListener(e -> {
            JSlider source = (JSlider) e.getSource();
            if (source.getValueIsAdjusting()) {
                int defaultValue = defaultCurveNodesSlider.getValue();
                if (defaultValue > curveSliderMax) defaultValue = curveSliderMax;
                if (defaultValue < 1) defaultValue = 1;
                curveSliderDefault = defaultValue;
                defaultCurveNodesSlider.setValue(curveSliderDefault);
                String text = getLocaleString("panel_config_tab_curves_defaultnodes") + " ( " + curveSliderDefault + " )  ";
                defaultCurveNodesLabel.setText(text);
            }
        });

        defaultCurveNodesSlider.setBorder(BorderFactory.createEmptyBorder(0,0,0,0));
        defaultCurveNodesSlider.setPaintTicks(true);
        defaultCurveNodesSlider.setSnapToTicks(false);
        defaultCurveNodesSlider.setMajorTickSpacing(5);
        defaultCurveNodesSlider.setPaintLabels(true);
        defaultCurveNodesSlider.setMinimum(0);
        defaultCurveNodesSlider.setMaximum(50);
        defaultCurveNodesSlider.setValue(curveSliderDefault);

        add(defaultCurveNodesLabel);
        add(defaultCurveNodesSlider);

        // control point movement scaler

        JLabel curveControlPointScalerLabel = new JLabel(getLocaleString("panel_config_tab_curves_controlpointscaler") + "  ", JLabel.TRAILING);
        JSlider curveControlPointScalerSlider = new JSlider(SwingConstants.HORIZONTAL);

        curveControlPointScalerSlider.addChangeListener(e -> {
            JSlider source = (JSlider) e.getSource();
            if (source.getValueIsAdjusting()) {
                int defaultValue = curveControlPointScalerSlider.getValue();
                if (defaultValue < 1) defaultValue = 1;
                controlPointMoveScaler = defaultValue;
                curveControlPointScalerSlider.setValue(controlPointMoveScaler);
            }
        });

        curveControlPointScalerSlider.setBorder(BorderFactory.createEmptyBorder(0,0,0,0));
        curveControlPointScalerSlider.setPaintTicks(true);
        curveControlPointScalerSlider.setSnapToTicks(true);
        curveControlPointScalerSlider.setMajorTickSpacing(1);
        curveControlPointScalerSlider.setPaintLabels(true);
        curveControlPointScalerSlider.setMinimum(0);
        curveControlPointScalerSlider.setMaximum(10);
        curveControlPointScalerSlider.setValue(controlPointMoveScaler);

        add(curveControlPointScalerLabel);
        add(curveControlPointScalerSlider);
    }
}
