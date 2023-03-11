package AutoDriveEditor.GUI.Buttons.Curves;

import AutoDriveEditor.GUI.Buttons.CurveBaseButton;
import AutoDriveEditor.GUI.GUIBuilder;
import AutoDriveEditor.MapPanel.QuadCurve;
import AutoDriveEditor.RoadNetwork.MapNode;

import javax.swing.*;
import java.awt.*;
import java.awt.event.MouseEvent;
import java.awt.geom.Point2D;
import java.util.concurrent.locks.Lock;

import static AutoDriveEditor.GUI.Buttons.Curves.CubicCurveButton.cubicCurve;
import static AutoDriveEditor.GUI.GUIBuilder.*;
import static AutoDriveEditor.Listeners.MouseListener.*;
import static AutoDriveEditor.Locale.LocaleManager.getLocaleString;
import static AutoDriveEditor.MapPanel.MapPanel.*;
import static AutoDriveEditor.RoadNetwork.MapNode.NODE_FLAG_STANDARD;
import static AutoDriveEditor.Utils.GUIUtils.makeImageToggleButton;
import static AutoDriveEditor.Utils.GUIUtils.showInTextArea;
import static AutoDriveEditor.Utils.ImageUtils.backBufferGraphics;
import static AutoDriveEditor.Utils.LoggerUtils.LOG;
import static AutoDriveEditor.XMLConfig.EditorXML.*;

public final class QuadCurveButton extends CurveBaseButton {

    public static QuadCurve quadCurve;
    public static boolean isQuadCurveCreated = false;
    private boolean showConnectingLine = false;

    public QuadCurveButton(JPanel panel) {
        button = makeImageToggleButton("buttons/quadcurve","buttons/quadcurve_selected", null, "panel_curves_quadbezier_tooltip", "panel_curves_quadbezier_alt", panel, false, false, null, false, this);
    }

    @Override
    public String getButtonID() { return "QuadCurveButton"; }

    @Override
    public String getButtonAction() { return "ActionButton"; }

    @Override
    public String getButtonPanel() { return "Curves"; }

    @Override
    public String getInfoText() {
        return getLocaleString("panel_curves_desc");
    }

    @Override
    protected void setCurveStartNode(MapNode startNode) {
        curveStartNode = startNode;
        showInTextArea(getLocaleString("infopanel_curve_select_end"), true, false);
        showConnectingLine = true;
    }

    @Override
    protected void setCurveEndAndCreate(MapNode endNode) {
        if (!isQuadCurveCreated) {
            showInTextArea(getLocaleString("infopanel_curve_created"), true, false);
            quadCurve = new QuadCurve(curveStartNode, endNode);
            quadCurve.setNumInterpolationPoints(GUIBuilder.numIterationsSlider.getValue() + 1);
            isQuadCurveCreated = true;
            showConnectingLine = false;
            GUIBuilder.curveOptionsPanel.setVisible(true);
        }
    }

    @Override
    protected boolean isCurveCreated() {
        return isQuadCurveCreated;
    }

    @Override
    public void setNodeType(int nodeType) { quadCurve.setNodeType(nodeType); }

    @Override
    public void setDualPath(boolean isDualPath) { quadCurve.setDualPath(isDualPath); }

    @Override
    public void setReversePath(boolean isReversePath) { quadCurve.setReversePath(isReversePath); }

    @Override
    public void setNumInterpolationPoints(int numPoints) { quadCurve.setNumInterpolationPoints(numPoints); }

    @Override
    protected void storeCurvePanelSettings() {
        if (quadCurve != null) {
            curvePanelNodeTypeStore = quadCurve.getNodeType();
            curvePanelReverseStore = quadCurve.isReversePath();
            curvePanelDualStore = quadCurve.isDualPath();
            curvePanelIntPointsStore = quadCurve.getNumInterpolationPoints();
        }
    }

    @Override
    protected void restoreCurvePanelSettings() {
        if (quadCurve != null) {
            quadCurve.setNodeType(curvePanelNodeTypeStore);
            if (curvePanelNodeTypeStore == 0) {
                curvePathRegular.setSelected(true);
            } else {
                curvePathSubPrio.setSelected(true);
            }
            quadCurve.setReversePath(curvePanelReverseStore);
            curvePathReverse.setSelected(curvePanelReverseStore);
            quadCurve.setDualPath(curvePanelDualStore);
            curvePathDual.setSelected(curvePanelDualStore);
            quadCurve.setNumInterpolationPoints(curvePanelIntPointsStore);
            numIterationsSlider.setValue(curvePanelIntPointsStore);
        }
    }

    @Override
    public void mousePressed(MouseEvent e) {
        if (e.getButton() == MouseEvent.BUTTON3 && !isQuadCurveCreated) {
            if (curveStartNode != null) {
                LOG.info("Cancelling Quad Curve");
                showConnectingLine = false;
                cancelCurve();
            }
        }
        if (e.getButton() == MouseEvent.BUTTON1) {
            MapNode node = getNodeAtScreenPosition(e.getX(), e.getY());
            if (quadCurve != null && node != null && node.isControlNode() && quadCurve.isControlPoint(node)) controlNodeSelected = true;
        }
    }

    @Override
    public void mouseMoved(MouseEvent e) {
        if (quadCurve == null && showConnectingLine) {
            getMapPanel().repaint();
        }
    }

    @Override
    public void mouseDragged(MouseEvent e) {
        if (quadCurve != null && isQuadCurveCreated && !isDraggingMap && controlNodeSelected) {
            quadCurve.moveControlPoint(e.getX() - prevMousePosX, e.getY() - prevMousePosY);
            quadCurve.updateCurve();
            getMapPanel().repaint();
        }
   }

    @Override
    public void cancelCurve() {
        if (quadCurve != null) quadCurve.clear();
        isQuadCurveCreated = false;
        showConnectingLine = false;
        quadCurve = null;
        curveStartNode = null;
        if (cubicCurve == null) GUIBuilder.curveOptionsPanel.setVisible(false);
        getMapPanel().repaint();
    }

    @Override
    public void commitCurve() {
        quadCurve.commitCurve();
        isQuadCurveCreated = false;
        showConnectingLine = false;
        quadCurve = null;
        curveStartNode = null;
        if (cubicCurve == null) GUIBuilder.curveOptionsPanel.setVisible(false);
        getMapPanel().repaint();
    }

    @Override
    public void drawToScreen(Graphics2D g, Lock drawLock, double scaledSizeQuarter, double scaledSizeHalf) {

        //
        // draw connection line
        //

        if (quadCurve == null && showConnectingLine) {
            Point2D startNodePos = worldPosToScreenPos(curveStartNode.x, curveStartNode.z);
            Point2D mousePos = new Point2D.Double(currentMouseX,currentMouseY);

            backBufferGraphics.setColor(Color.WHITE);
            drawArrowBetween(backBufferGraphics, startNodePos, mousePos, false);
        }

        if (quadCurve!= null && isQuadCurveCreated) {

            //
            // draw interpolation points for curve
            //

            Color colour;
            for (int j = 0; j < quadCurve.curveNodesList.size() - 1; j++) {

                MapNode currentNode = quadCurve.curveNodesList.get(j);
                MapNode nextNode = quadCurve.curveNodesList.get(j + 1);

                Point2D currentNodePos = worldPosToScreenPos(currentNode.x, currentNode.z);
                Point2D nextNodePos = worldPosToScreenPos(nextNode.x, nextNode.z);

                //
                //don't draw the first node as it already been drawn
                //

                if (j != 0) {
                    Shape oldClip = backBufferGraphics.getClip();
                    Graphics2D g2d = (Graphics2D) backBufferGraphics.create();
                    g2d.setComposite(AlphaComposite.SrcOver.derive(0.5f));
                    if (currentNode.flag == NODE_FLAG_STANDARD) {
                        g2d.setColor(Color.WHITE);
                    } else {
                        g2d.setColor(colourNodeSubprio);
                    }
                    g2d.fillArc((int) (currentNodePos.getX() - scaledSizeQuarter), (int) (currentNodePos.getY() - scaledSizeQuarter), (int) scaledSizeHalf, (int) scaledSizeHalf, 0, 360);
                    g2d.setClip(oldClip);
                    g2d.dispose();
                }

                if (quadCurve.isReversePath()) {
                    if (quadCurve.getNodeType() == NODE_FLAG_STANDARD) {
                        colour = colourConnectReverse;
                    } else {
                        colour = colourConnectReverseSubprio;
                    }
                } else if (quadCurve.isDualPath()) {
                    if (quadCurve.getNodeType() == NODE_FLAG_STANDARD) {
                        colour = colourConnectDual;
                    } else {
                        colour = colourConnectDualSubprio;
                    }
                } else if (currentNode.flag == 1) {
                    colour = colourConnectSubprio;
                } else {
                    colour = colourConnectRegular;
                }
                backBufferGraphics.setColor(colour);
                drawArrowBetween(backBufferGraphics, currentNodePos, nextNodePos, quadCurve.isDualPath()) ;
            }

            //
            // draw the control nodes, this is done last to make them visible at all times
            //

            Point2D nodePos = worldPosToScreenPos(quadCurve.getControlPoint().x, quadCurve.getControlPoint().z);
            Polygon p = new Polygon();
            p.addPoint((int) (nodePos.getX() - scaledSizeQuarter), (int) (nodePos.getY() - scaledSizeQuarter));
            p.addPoint((int) (nodePos.getX() + scaledSizeQuarter), (int) (nodePos.getY() - scaledSizeQuarter));
            p.addPoint((int) nodePos.getX(), (int) (nodePos.getY() + scaledSizeQuarter));
            g.setColor(colourNodeControl);
            g.fillPolygon(p);

            if (quadCurve.getControlPoint().isSelected || hoveredNode == quadCurve.getControlPoint()) {
                Graphics2D g2 = (Graphics2D) backBufferGraphics.create();
                BasicStroke bs = new BasicStroke((float) (scaledSizeHalf / 5));
                g2.setStroke(bs);
                g2.setColor(colourNodeSelected);
                g2.drawPolygon(p);
                g2.dispose();
            }
        }
    }
}
