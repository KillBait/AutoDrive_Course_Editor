package AutoDriveEditor.GUI.Buttons.Curves;

import AutoDriveEditor.Classes.QuinticCurve;
import AutoDriveEditor.GUI.Buttons.CurveBaseButton;
import AutoDriveEditor.RoadNetwork.MapNode;

import javax.swing.*;
import java.awt.*;
import java.awt.event.MouseEvent;
import java.awt.geom.Point2D;

import static AutoDriveEditor.AutoDriveEditor.getMapPanel;
import static AutoDriveEditor.GUI.Buttons.Curves.CubicCurveButton.cubicCurve;
import static AutoDriveEditor.GUI.Buttons.Curves.QuadCurveButton.quadCurve;
import static AutoDriveEditor.GUI.Buttons.Curves.QuarticCurveButton.quarticCurve;
import static AutoDriveEditor.GUI.Curves.CurvePanel.*;
import static AutoDriveEditor.GUI.MapPanel.*;
import static AutoDriveEditor.GUI.TextPanel.showInTextArea;
import static AutoDriveEditor.Listeners.MouseListener.*;
import static AutoDriveEditor.Locale.LocaleManager.getLocaleString;
import static AutoDriveEditor.RoadNetwork.MapNode.NODE_FLAG_REGULAR;
import static AutoDriveEditor.Utils.GUIUtils.makeImageToggleButton;
import static AutoDriveEditor.Utils.LoggerUtils.LOG;
import static AutoDriveEditor.XMLConfig.EditorXML.*;

public final class QuinticCurveButton extends CurveBaseButton {

    public static QuinticCurve quinticCurve;
    public static boolean isQuinticCurveCreated = false;
    private boolean showConnectingLine = false;
    private MapNode selectedControlPoint;

    public QuinticCurveButton(JPanel panel) {
        button = makeImageToggleButton("buttons/quinticcurve", "buttons/quinticcurve_selected", null, "panel_curves_quinticbezier_tooltip", "panel_curves_quinticbezier_alt", panel, false, false, null, false, this);
    }

    @Override
    public String getButtonID() {
        return "QuinticCurveButton";
    }

    @Override
    public String getButtonAction() {
        return "ActionButton";
    }

    @Override
    public String getButtonPanel() {
        return "Curves";
    }

    @Override
    public String getInfoText() {
        return getLocaleString("panel_curves_desc");
    }

    @Override
    protected void setCurvePreviewStartNode(MapNode startNode) {
        curveStartNode = startNode;
        showInTextArea(getLocaleString("infopanel_curve_select_end"), true, false);
        showConnectingLine = true;
    }

    @Override
    protected void setCurvePreviewEndAndDisplay(MapNode endNode) {
        if (!isQuinticCurveCreated) {
            showInTextArea(getLocaleString("infopanel_curve_created"), true, false);
            quinticCurve = new QuinticCurve(curveStartNode, endNode);
            quinticCurve.setNumInterpolationPoints(numIterationsSlider.getValue() + 1);
            isQuinticCurveCreated = true;
            showConnectingLine = false;
        }
    }

    @Override
    protected boolean isCurveCreated() {
        return isQuinticCurveCreated;
    }

    @Override
    public void setNodeType(int nodeType) {
        quinticCurve.setNodeType(nodeType);
    }

    @Override
    public void setDualPath(boolean isDualPath) {
        quinticCurve.setDualPath(isDualPath);
    }

    @Override
    public void setReversePath(boolean isReversePath) {
        quinticCurve.setReversePath(isReversePath);
    }

    @Override
    public void setNumInterpolationPoints(int numPoints) {
        quinticCurve.setNumInterpolationPoints(numPoints);
    }

    @Override
    protected void storeCurvePanelSettings() {
        if (quinticCurve != null) {
            curvePanelNodeTypeStore = quinticCurve.getNodeType();
            curvePanelReverseStore = quinticCurve.isReversePath();
            curvePanelDualStore = quinticCurve.isDualPath();
            curvePanelIntPointsStore = quinticCurve.getCurveNodes().size();
        }
    }

    @Override
    protected void restoreCurvePanelSettings() {
        if (quinticCurve != null) {
            quinticCurve.setNodeType(curvePanelNodeTypeStore);
            if (curvePanelNodeTypeStore == 0) {
                curvePathRegular.setSelected(true);
            } else {
                curvePathSubPrio.setSelected(true);
            }
            quinticCurve.setReversePath(curvePanelReverseStore);
            curvePathReverse.setSelected(curvePanelReverseStore);
            quinticCurve.setDualPath(curvePanelDualStore);
            curvePathDual.setSelected(curvePanelDualStore);
            quinticCurve.setNumInterpolationPoints(curvePanelIntPointsStore);
            numIterationsSlider.setValue(curvePanelIntPointsStore);
        }
    }

    @Override
    public void mousePressed(MouseEvent e) {
        if (e.getButton() == MouseEvent.BUTTON3 && !isQuinticCurveCreated) {
            if (curveStartNode != null) {
                LOG.info("Cancelling Quintic Curve");
                showConnectingLine = false;
                cancelCurve();
            }
        }
        if (e.getButton() == MouseEvent.BUTTON1) {
            MapNode node = getNodeAtScreenPosition(e.getX(), e.getY());
            if (quinticCurve != null && node != null && node.isControlNode() && quinticCurve.isControlPoint(node)) {
                selectedControlPoint = node;
                controlNodeSelected = true;
            }
        }
    }

    @Override
    public void mouseMoved(MouseEvent e) {
        if (quinticCurve == null && showConnectingLine) {
            getMapPanel().repaint();
        }
    }

    @Override
    public void mouseDragged(MouseEvent e) {
        if (quinticCurve != null && isQuinticCurveCreated && !isDraggingMap && controlNodeSelected) {
            if (selectedControlPoint == quinticCurve.getControlPoint1()) {
                quinticCurve.moveControlPoint1(e.getX() - prevMousePosX, e.getY() - prevMousePosY);
            }
            if (selectedControlPoint == quinticCurve.getControlPoint2()) {
                quinticCurve.moveControlPoint2(e.getX() - prevMousePosX, e.getY() - prevMousePosY);
            }
            if (selectedControlPoint == quinticCurve.getControlPoint3()) {
                quinticCurve.moveControlPoint3(e.getX() - prevMousePosX, e.getY() - prevMousePosY);
            }
            if (selectedControlPoint == quinticCurve.getControlPoint4()) {
                quinticCurve.moveControlPoint4(e.getX() - prevMousePosX, e.getY() - prevMousePosY);
            }
            quinticCurve.updateCurve();
            getMapPanel().repaint();
        }
    }

    @Override
    public void cancelCurve() {
        if (quinticCurve != null) quinticCurve.clear();
        isQuinticCurveCreated = false;
        showConnectingLine = false;
        quinticCurve = null;
        curveStartNode = null;
        if (cubicCurve == null && quadCurve == null && quarticCurve == null) curveOptionsPanel.setVisible(false);
        getMapPanel().repaint();
    }

    @Override
    public void commitCurve() {
        if (quinticCurve != null) quinticCurve.commitCurve();
        isQuinticCurveCreated = false;
        showConnectingLine = false;
        quinticCurve = null;
        curveStartNode = null;
        if (cubicCurve == null && quadCurve == null && quarticCurve == null) curveOptionsPanel.setVisible(false);
        getMapPanel().repaint();
    }

    @Override
    public void drawToScreen(Graphics g) {
        // Draw the initial connection arrow
        if (quinticCurve == null && showConnectingLine) {
            Point2D startNodePos = worldPosToScreenPos(curveStartNode.x, curveStartNode.z);
            Point2D mousePos = new Point2D.Double(currentMouseX, currentMouseY);
            drawArrowBetween(g, startNodePos, mousePos, false, Color.WHITE, false);
        }

        if (quinticCurve != null && isQuinticCurveCreated) {
            // Draw interpolation points and connection arrows for the curve
            Color colour;
            for (int j = 0; j < quinticCurve.getCurveNodes().size() - 1; j++) {
                MapNode currentNode = quinticCurve.getCurveNodes().get(j);
                MapNode nextNode = quinticCurve.getCurveNodes().get(j + 1);
                Point2D currentNodePos = worldPosToScreenPos(currentNode.x, currentNode.z);
                Point2D nextNodePos = worldPosToScreenPos(nextNode.x, nextNode.z);

                if (j != 0) {
                    Shape oldClip = g.getClip();
                    Graphics2D g2d = (Graphics2D) g.create();
                    g2d.setComposite(AlphaComposite.SrcOver.derive(0.5f));
                    if (currentNode.flag == NODE_FLAG_REGULAR) {
                        g2d.setColor(Color.WHITE);
                    } else {
                        g2d.setColor(colourNodeSubprio);
                    }
                    g2d.fillArc((int) (currentNodePos.getX() - nodeSizeScaledHalf),
                                (int) (currentNodePos.getY() - nodeSizeScaledHalf),
                                (int) nodeSizeScaled,
                                (int) nodeSizeScaled, 0, 360);
                    g2d.setClip(oldClip);
                    g2d.dispose();
                }

                if (quinticCurve.isReversePath()) {
                    if (quinticCurve.getNodeType() == NODE_FLAG_REGULAR) {
                        colour = colourConnectReverse;
                    } else {
                        colour = colourConnectReverseSubprio;
                    }
                } else if (quinticCurve.isDualPath()) {
                    if (quinticCurve.getNodeType() == NODE_FLAG_REGULAR) {
                        colour = colourConnectDual;
                    } else {
                        colour = colourConnectDualSubprio;
                    }
                } else if (currentNode.flag == 1) {
                    colour = colourConnectSubprio;
                } else {
                    colour = colourConnectRegular;
                }
                drawArrowBetween(g, currentNodePos, nextNodePos, quinticCurve.isDualPath(), colour, false);
            }

            // Draw control nodes (four in total)
            Polygon p = new Polygon();
            // Control Point 1
            Point2D nodePos = worldPosToScreenPos(quinticCurve.getControlPoint1().x, quinticCurve.getControlPoint1().z);
            p.addPoint((int) (nodePos.getX() - nodeSizeScaledHalf), (int) (nodePos.getY() - nodeSizeScaledHalf));
            p.addPoint((int) (nodePos.getX() + nodeSizeScaledHalf), (int) (nodePos.getY() - nodeSizeScaledHalf));
            p.addPoint((int) nodePos.getX(), (int) (nodePos.getY() + nodeSizeScaledHalf));
            g.setColor(colourNodeControl);
            g.fillPolygon(p);
            if (quinticCurve.getControlPoint1().isSelected() || hoveredNode == quinticCurve.getControlPoint1()) {
                Graphics2D g2 = (Graphics2D) g.create();
                BasicStroke bs = new BasicStroke((float) (nodeSizeScaled / 5));
                g2.setStroke(bs);
                g2.setColor(colourNodeSelected);
                g2.drawPolygon(p);
                g2.dispose();
            }
            p.reset();

            // Control Point 2
            nodePos = worldPosToScreenPos(quinticCurve.getControlPoint2().x, quinticCurve.getControlPoint2().z);
            p.addPoint((int) (nodePos.getX() - nodeSizeScaledHalf), (int) (nodePos.getY() - nodeSizeScaledHalf));
            p.addPoint((int) (nodePos.getX() + nodeSizeScaledHalf), (int) (nodePos.getY() - nodeSizeScaledHalf));
            p.addPoint((int) nodePos.getX(), (int) (nodePos.getY() + nodeSizeScaledHalf));
            g.setColor(colourNodeControl);
            g.fillPolygon(p);
            if (quinticCurve.getControlPoint2().isSelected() || hoveredNode == quinticCurve.getControlPoint2()) {
                Graphics2D g2 = (Graphics2D) g.create();
                BasicStroke bs = new BasicStroke((float) (nodeSizeScaled / 5));
                g2.setStroke(bs);
                g2.setColor(colourNodeSelected);
                g2.drawPolygon(p);
                g2.dispose();
            }
            p.reset();

            // Control Point 3
            nodePos = worldPosToScreenPos(quinticCurve.getControlPoint3().x, quinticCurve.getControlPoint3().z);
            p.addPoint((int) (nodePos.getX() - nodeSizeScaledHalf), (int) (nodePos.getY() - nodeSizeScaledHalf));
            p.addPoint((int) (nodePos.getX() + nodeSizeScaledHalf), (int) (nodePos.getY() - nodeSizeScaledHalf));
            p.addPoint((int) nodePos.getX(), (int) (nodePos.getY() + nodeSizeScaledHalf));
            g.setColor(colourNodeControl);
            g.fillPolygon(p);
            if (quinticCurve.getControlPoint3().isSelected() || hoveredNode == quinticCurve.getControlPoint3()) {
                Graphics2D g2 = (Graphics2D) g.create();
                BasicStroke bs = new BasicStroke((float) (nodeSizeScaled / 5));
                g2.setStroke(bs);
                g2.setColor(colourNodeSelected);
                g2.drawPolygon(p);
                g2.dispose();
            }
            p.reset();

            // Control Point 4
            nodePos = worldPosToScreenPos(quinticCurve.getControlPoint4().x, quinticCurve.getControlPoint4().z);
            p.addPoint((int) (nodePos.getX() - nodeSizeScaledHalf), (int) (nodePos.getY() - nodeSizeScaledHalf));
            p.addPoint((int) (nodePos.getX() + nodeSizeScaledHalf), (int) (nodePos.getY() - nodeSizeScaledHalf));
            p.addPoint((int) nodePos.getX(), (int) (nodePos.getY() + nodeSizeScaledHalf));
            g.setColor(colourNodeControl);
            g.fillPolygon(p);
            if (quinticCurve.getControlPoint4().isSelected() || hoveredNode == quinticCurve.getControlPoint4()) {
                Graphics2D g2 = (Graphics2D) g.create();
                BasicStroke bs = new BasicStroke((float) (nodeSizeScaled / 5));
                g2.setStroke(bs);
                g2.setColor(colourNodeSelected);
                g2.drawPolygon(p);
                g2.dispose();
            }
        }
    }
}
