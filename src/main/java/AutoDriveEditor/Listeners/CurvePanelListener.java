package AutoDriveEditor.Listeners;

import AutoDriveEditor.GUI.Buttons.CurveBaseButton;
import AutoDriveEditor.GUI.GUIBuilder;

import javax.swing.*;
import javax.swing.event.ChangeEvent;
import javax.swing.event.ChangeListener;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.awt.event.ItemEvent;
import java.awt.event.ItemListener;

import static AutoDriveEditor.AutoDriveEditor.DEBUG;
import static AutoDriveEditor.AutoDriveEditor.buttonManager;
import static AutoDriveEditor.GUI.Buttons.Curves.CubicCurveButton.cubicCurve;
import static AutoDriveEditor.GUI.Buttons.Curves.CubicCurveButton.isCubicCurveCreated;
import static AutoDriveEditor.GUI.Buttons.Curves.QuadCurveButton.isQuadCurveCreated;
import static AutoDriveEditor.GUI.Buttons.Curves.QuadCurveButton.quadCurve;
import static AutoDriveEditor.GUI.GUIBuilder.*;
import static AutoDriveEditor.MapPanel.MapPanel.getMapPanel;
import static AutoDriveEditor.RoadNetwork.MapNode.NODE_FLAG_STANDARD;
import static AutoDriveEditor.RoadNetwork.MapNode.NODE_FLAG_SUBPRIO;
import static AutoDriveEditor.Utils.LoggerUtils.LOG;

public class CurvePanelListener implements ActionListener, ItemListener, ChangeListener {

    @Override
    public void actionPerformed(ActionEvent e) {
        if (DEBUG) LOG.info("CurvePanelListener ActionCommand: {}", e.getActionCommand());
        try {
            CurveBaseButton button = (CurveBaseButton) buttonManager.getCurrentButton();
            switch (e.getActionCommand()) {
                case BUTTON_COMMIT_CURVE:
                    button.commitCurve();
                    break;
                case BUTTON_CANCEL_CURVE:
                    button.cancelCurve();
                    break;
            }
        } catch (Exception ignored) {}

    }

    @Override
    public void itemStateChanged(ItemEvent e) {
        AbstractButton button = (AbstractButton) e.getItem();
        if (DEBUG) LOG.info("CurvePanel ItemStateChange: {}", button.getActionCommand());
        switch (button.getActionCommand()) {
            case RADIOBUTTON_PATHTYPE_REGULAR:
                if (quadCurve != null && isQuadCurveCreated) {
                    quadCurve.setNodeType(NODE_FLAG_STANDARD);
                } else if (cubicCurve != null && isCubicCurveCreated) {
                    cubicCurve.setNodeType(NODE_FLAG_STANDARD);
                }
                mapPanel.repaint();
                break;
            case RADIOBUTTON_PATHTYPE_SUBPRIO:
                if (quadCurve != null) {
                    quadCurve.setNodeType(NODE_FLAG_SUBPRIO);
                } else if (cubicCurve != null) {
                    cubicCurve.setNodeType(NODE_FLAG_SUBPRIO);
                }
                mapPanel.repaint();
                break;
            case RADIOBUTTON_PATHTYPE_REVERSE:
                if (button.isSelected()) {
                    GUIBuilder.curvePathDual.setSelected(false);
                }
                if (quadCurve != null) {
                    quadCurve.setReversePath(button.isSelected());
                    quadCurve.setDualPath(false);
                } else if (cubicCurve != null) {
                    cubicCurve.setReversePath(button.isSelected());
                    cubicCurve.setDualPath(false);
                }
                mapPanel.repaint();
                break;
            case RADIOBUTTON_PATHTYPE_DUAL:
                if (button.isSelected()) {
                    GUIBuilder.curvePathReverse.setSelected(false);

                }
                if (quadCurve != null) {
                    quadCurve.setDualPath(button.isSelected());
                    quadCurve.setReversePath(false);
                } else if (cubicCurve != null) {
                    cubicCurve.setDualPath(button.isSelected());
                    cubicCurve.setReversePath(false);
                }

                mapPanel.repaint();
                break;
        }
    }

    @Override
    public void stateChanged(ChangeEvent e) {
        JSlider source = (JSlider) e.getSource();
        if (source.getValueIsAdjusting()) {
            int value = source.getValue();
            if (quadCurve != null) {
                quadCurve.setNumInterpolationPoints(value + 1);
                getMapPanel().repaint();
            } else if (cubicCurve != null) {
                cubicCurve.setNumInterpolationPoints(value + 1);
                getMapPanel().repaint();
            }
        }
    }
}
