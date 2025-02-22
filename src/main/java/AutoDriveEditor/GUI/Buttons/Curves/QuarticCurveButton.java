package AutoDriveEditor.GUI.Buttons.Curves;

import AutoDriveEditor.Classes.QuarticCurve;
import AutoDriveEditor.GUI.Buttons.CurveBaseButton;
import AutoDriveEditor.RoadNetwork.MapNode;

import javax.swing.*;
import java.awt.*;
import java.awt.event.MouseEvent;
import java.awt.geom.Point2D;

import static AutoDriveEditor.AutoDriveEditor.getMapPanel;
import static AutoDriveEditor.GUI.Buttons.Curves.CubicCurveButton.cubicCurve;
import static AutoDriveEditor.GUI.Buttons.Curves.QuadCurveButton.quadCurve;
import static AutoDriveEditor.GUI.Curves.CurvePanel.*;
import static AutoDriveEditor.GUI.MapPanel.*;
import static AutoDriveEditor.GUI.TextPanel.showInTextArea;
import static AutoDriveEditor.Listeners.MouseListener.*;
import static AutoDriveEditor.Locale.LocaleManager.getLocaleString;
import static AutoDriveEditor.RoadNetwork.MapNode.NODE_FLAG_REGULAR;
import static AutoDriveEditor.Utils.GUIUtils.makeImageToggleButton;
import static AutoDriveEditor.Utils.LoggerUtils.LOG;
import static AutoDriveEditor.XMLConfig.EditorXML.*;

public final class QuarticCurveButton extends CurveBaseButton {

    public static QuarticCurve quarticCurve;
    public static boolean isQuarticCurveCreated = false;
    private boolean showConnectingLine = false;
    private MapNode selectedControlPoint;

    public QuarticCurveButton(JPanel panel) {
        button = makeImageToggleButton("buttons/quarticcurve", "buttons/quarticcurve_selected", null, "panel_curves_quarticbezier_tooltip", "panel_curves_quarticbezier_alt", panel, false, false, null, false, this);
    }

    @Override
    public String getButtonID() {
        return "QuarticCurveButton";
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
        if (!isQuarticCurveCreated) {
            showInTextArea(getLocaleString("infopanel_curve_created"), true, false);
            quarticCurve = new QuarticCurve(curveStartNode, endNode);
            quarticCurve.setNumInterpolationPoints(numIterationsSlider.getValue() + 1);
            isQuarticCurveCreated = true;
            showConnectingLine = false;
        }
    }

    @Override
    protected boolean isCurveCreated() {
        return isQuarticCurveCreated;
    }

    @Override
    public void setNodeType(int nodeType) {
        quarticCurve.setNodeType(nodeType);
    }

    @Override
    public void setDualPath(boolean isDualPath) {
        quarticCurve.setDualPath(isDualPath);
    }

    @Override
    public void setReversePath(boolean isReversePath) {
        quarticCurve.setReversePath(isReversePath);
    }

    @Override
    public void setNumInterpolationPoints(int numPoints) {
        quarticCurve.setNumInterpolationPoints(numPoints);
    }

    @Override
    protected void storeCurvePanelSettings() {
        if (quarticCurve != null) {
            curvePanelNodeTypeStore = quarticCurve.getNodeType();
            curvePanelReverseStore = quarticCurve.isReversePath();
            curvePanelDualStore = quarticCurve.isDualPath();
            curvePanelIntPointsStore = quarticCurve.getCurveNodes().size();
        }
    }

    @Override
    protected void restoreCurvePanelSettings() {
        if (quarticCurve != null) {
            quarticCurve.setNodeType(curvePanelNodeTypeStore);
            if (curvePanelNodeTypeStore == 0) {
                curvePathRegular.setSelected(true);
            } else {
                curvePathSubPrio.setSelected(true);
            }
            quarticCurve.setReversePath(curvePanelReverseStore);
            curvePathReverse.setSelected(curvePanelReverseStore);
            quarticCurve.setDualPath(curvePanelDualStore);
            curvePathDual.setSelected(curvePanelDualStore);
            quarticCurve.setNumInterpolationPoints(curvePanelIntPointsStore);
            numIterationsSlider.setValue(curvePanelIntPointsStore);
        }
    }

    @Override
    public void mousePressed(MouseEvent e) {
        if (e.getButton() == MouseEvent.BUTTON3 && !isQuarticCurveCreated) {
            if (curveStartNode != null) {
                LOG.info("Cancelling Quartic Curve");
                showConnectingLine = false;
                cancelCurve();
            }
        }
        if (e.getButton() == MouseEvent.BUTTON1) {
            MapNode node = getNodeAtScreenPosition(e.getX(), e.getY());
            if (quarticCurve != null && node != null && node.isControlNode() && quarticCurve.isControlPoint(node)) {
                selectedControlPoint = node;
                controlNodeSelected = true;
            }
        }
    }

    @Override
    public void mouseMoved(MouseEvent e) {
        if (quarticCurve == null && showConnectingLine) {
            getMapPanel().repaint();
        }
    }

    @Override
    public void mouseDragged(MouseEvent e) {
        if (quarticCurve != null && isQuarticCurveCreated && !isDraggingMap && controlNodeSelected) {
            if (selectedControlPoint == quarticCurve.getControlPoint1()) {
                quarticCurve.moveControlPoint1(e.getX() - prevMousePosX, e.getY() - prevMousePosY);
            }
            if (selectedControlPoint == quarticCurve.getControlPoint2()) {
                quarticCurve.moveControlPoint2(e.getX() - prevMousePosX, e.getY() - prevMousePosY);
            }
            if (selectedControlPoint == quarticCurve.getControlPoint3()) {
                quarticCurve.moveControlPoint3(e.getX() - prevMousePosX, e.getY() - prevMousePosY);
            }
            quarticCurve.updateCurve();
            getMapPanel().repaint();
        }
    }

    @Override
    public void cancelCurve() {
        if (quarticCurve != null) quarticCurve.clear();
        isQuarticCurveCreated = false;
        showConnectingLine = false;
        quarticCurve = null;
        curveStartNode = null;
        if (cubicCurve == null && quadCurve == null) curveOptionsPanel.setVisible(false);
        getMapPanel().repaint();
    }

    @Override
    public void commitCurve() {
        if (quarticCurve != null) quarticCurve.commitCurve();
        isQuarticCurveCreated = false;
        showConnectingLine = false;
        quarticCurve = null;
        curveStartNode = null;
        if (cubicCurve == null && quadCurve == null) curveOptionsPanel.setVisible(false);
        getMapPanel().repaint();
    }

    @Override
    public void drawToScreen(Graphics g) {
        // Draw the initial connection arrow
        if (quarticCurve == null && showConnectingLine) {
            Point2D startNodePos = worldPosToScreenPos(curveStartNode.x, curveStartNode.z);
            Point2D mousePos = new Point2D.Double(currentMouseX, currentMouseY);
            drawArrowBetween(g, startNodePos, mousePos, false, Color.WHITE, false);
        }

        if (quarticCurve != null && isQuarticCurveCreated) {
            // Draw interpolation points and connection arrows for the curve
            Color colour;
            for (int j = 0; j < quarticCurve.getCurveNodes().size() - 1; j++) {
                MapNode currentNode = quarticCurve.getCurveNodes().get(j);
                MapNode nextNode = quarticCurve.getCurveNodes().get(j + 1);
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

                if (quarticCurve.isReversePath()) {
                    if (quarticCurve.getNodeType() == NODE_FLAG_REGULAR) {
                        colour = colourConnectReverse;
                    } else {
                        colour = colourConnectReverseSubprio;
                    }
                } else if (quarticCurve.isDualPath()) {
                    if (quarticCurve.getNodeType() == NODE_FLAG_REGULAR) {
                        colour = colourConnectDual;
                    } else {
                        colour = colourConnectDualSubprio;
                    }
                } else if (currentNode.flag == 1) {
                    colour = colourConnectSubprio;
                } else {
                    colour = colourConnectRegular;
                }
                drawArrowBetween(g, currentNodePos, nextNodePos, quarticCurve.isDualPath(), colour, false);
            }

            // Draw control nodes (three in total)
            Polygon p = new Polygon();
            // Control Point 1
            Point2D nodePos = worldPosToScreenPos(quarticCurve.getControlPoint1().x, quarticCurve.getControlPoint1().z);
            p.addPoint((int) (nodePos.getX() - nodeSizeScaledHalf), (int) (nodePos.getY() - nodeSizeScaledHalf));
            p.addPoint((int) (nodePos.getX() + nodeSizeScaledHalf), (int) (nodePos.getY() - nodeSizeScaledHalf));
            p.addPoint((int) nodePos.getX(), (int) (nodePos.getY() + nodeSizeScaledHalf));
            g.setColor(colourNodeControl);
            g.fillPolygon(p);
            if (quarticCurve.getControlPoint1().isSelected() || hoveredNode == quarticCurve.getControlPoint1()) {
                Graphics2D g2 = (Graphics2D) g.create();
                BasicStroke bs = new BasicStroke((float) (nodeSizeScaled / 5));
                g2.setStroke(bs);
                g2.setColor(colourNodeSelected);
                g2.drawPolygon(p);
                g2.dispose();
            }
            p.reset();

            // Control Point 2
            nodePos = worldPosToScreenPos(quarticCurve.getControlPoint2().x, quarticCurve.getControlPoint2().z);
            p.addPoint((int) (nodePos.getX() - nodeSizeScaledHalf), (int) (nodePos.getY() - nodeSizeScaledHalf));
            p.addPoint((int) (nodePos.getX() + nodeSizeScaledHalf), (int) (nodePos.getY() - nodeSizeScaledHalf));
            p.addPoint((int) nodePos.getX(), (int) (nodePos.getY() + nodeSizeScaledHalf));
            g.setColor(colourNodeControl);
            g.fillPolygon(p);
            if (quarticCurve.getControlPoint2().isSelected() || hoveredNode == quarticCurve.getControlPoint2()) {
                Graphics2D g2 = (Graphics2D) g.create();
                BasicStroke bs = new BasicStroke((float) (nodeSizeScaled / 5));
                g2.setStroke(bs);
                g2.setColor(colourNodeSelected);
                g2.drawPolygon(p);
                g2.dispose();
            }
            p.reset();

            // Control Point 3
            nodePos = worldPosToScreenPos(quarticCurve.getControlPoint3().x, quarticCurve.getControlPoint3().z);
            p.addPoint((int) (nodePos.getX() - nodeSizeScaledHalf), (int) (nodePos.getY() - nodeSizeScaledHalf));
            p.addPoint((int) (nodePos.getX() + nodeSizeScaledHalf), (int) (nodePos.getY() - nodeSizeScaledHalf));
            p.addPoint((int) nodePos.getX(), (int) (nodePos.getY() + nodeSizeScaledHalf));
            g.setColor(colourNodeControl);
            g.fillPolygon(p);
            if (quarticCurve.getControlPoint3().isSelected() || hoveredNode == quarticCurve.getControlPoint3()) {
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
