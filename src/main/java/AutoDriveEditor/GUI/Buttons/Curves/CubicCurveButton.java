package AutoDriveEditor.GUI.Buttons.Curves;

import AutoDriveEditor.GUI.Buttons.CurveBaseButton;
import AutoDriveEditor.GUI.GUIBuilder;
import AutoDriveEditor.MapPanel.CubicCurve;
import AutoDriveEditor.RoadNetwork.MapNode;

import javax.swing.*;
import java.awt.*;
import java.awt.event.MouseEvent;
import java.awt.geom.Point2D;
import java.util.concurrent.locks.Lock;

import static AutoDriveEditor.GUI.Buttons.Curves.QuadCurveButton.quadCurve;
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

public final class CubicCurveButton extends CurveBaseButton {

    public static CubicCurve cubicCurve;
    public static boolean isCubicCurveCreated = false;
    private boolean showConnectingLine = false;
    private MapNode selectedControlPoint;

    public CubicCurveButton(JPanel panel) {
        button = makeImageToggleButton("buttons/cubiccurve","buttons/cubiccurve_selected", null, "panel_curves_cubicbezier_tooltip", "panel_curves_cubicbezier_alt", panel, false, false, null,  false, this);
    }

    @Override
    public String getButtonID() { return "CubicCurveButton"; }

    @Override
    public String getButtonAction() { return "ActionButton"; }

    @Override
    public String getButtonPanel() { return "Curves"; }

    @Override
    public String getInfoText() { return getLocaleString("panel_curves_desc");
    }

    @Override
    protected void setCurveStartNode(MapNode startNode) {
        curveStartNode = startNode;
        showInTextArea(getLocaleString("infopanel_curve_select_end"), true, false);
        showConnectingLine = true;
    }

    @Override
    protected void setCurveEndAndCreate(MapNode endNode) {
        if (!isCubicCurveCreated) {
            showInTextArea(getLocaleString("infopanel_curve_created"), true, false);
            cubicCurve = new CubicCurve(curveStartNode, endNode);
            cubicCurve.setNumInterpolationPoints(GUIBuilder.numIterationsSlider.getValue() + 1);
            isCubicCurveCreated = true;
            showConnectingLine = false;
        }
    }

    @Override
    protected boolean isCurveCreated() {
        return isCubicCurveCreated;
    }

    @Override
    public void setNodeType(int nodeType) { cubicCurve.setNodeType(nodeType); }

    @Override
    public void setDualPath(boolean isDualPath) { cubicCurve.setDualPath(isDualPath); }

    @Override
    public void setReversePath(boolean isReversePath) { cubicCurve.setReversePath(isReversePath); }

    @Override
    public void setNumInterpolationPoints(int numPoints) { cubicCurve.setNumInterpolationPoints(numPoints); }

    @Override
    protected void storeCurvePanelSettings() {
        if (cubicCurve != null) {
            curvePanelNodeTypeStore = cubicCurve.getNodeType();
            curvePanelReverseStore = cubicCurve.isReversePath();
            curvePanelDualStore = cubicCurve.isDualPath();
            curvePanelIntPointsStore = cubicCurve.getNumInterpolationPoints();
        }
    }

    @Override
    protected void restoreCurvePanelSettings() {
        if (cubicCurve != null) {
            cubicCurve.setNodeType(curvePanelNodeTypeStore);
            if (curvePanelNodeTypeStore == 0) {
                curvePathRegular.setSelected(true);
            } else {
                curvePathSubPrio.setSelected(true);
            }
            cubicCurve.setReversePath(curvePanelReverseStore);
            curvePathReverse.setSelected(curvePanelReverseStore);
            cubicCurve.setDualPath(curvePanelDualStore);
            curvePathDual.setSelected(curvePanelDualStore);
            cubicCurve.setNumInterpolationPoints(curvePanelIntPointsStore);
            numIterationsSlider.setValue(curvePanelIntPointsStore);
        }
    }

    @Override
    public void mousePressed(MouseEvent e) {
        if (e.getButton() == MouseEvent.BUTTON3 && !isCubicCurveCreated) {
            if (curveStartNode != null) {
                LOG.info("Cancelling Quad Curve");
                showConnectingLine = false;
                cancelCurve();
            }
        }
        if (e.getButton() == MouseEvent.BUTTON1) {
            MapNode node = getNodeAtScreenPosition(e.getX(), e.getY());
            if (cubicCurve != null && node != null && node.isControlNode() && cubicCurve.isControlPoint(node)) {
                selectedControlPoint = node;
                controlNodeSelected = true;
            }
        }
    }

    @Override
    public void mouseMoved(MouseEvent e) {
        if (cubicCurve == null && showConnectingLine) {
            getMapPanel().repaint();
        }
    }

    @Override
    public void mouseDragged(MouseEvent e) {
        if (cubicCurve != null && isCubicCurveCreated && !isDraggingMap && controlNodeSelected) {
            if (selectedControlPoint == cubicCurve.getControlPoint1()) {
                cubicCurve.moveControlPoint1(e.getX() - prevMousePosX, e.getY() - prevMousePosY);
            }
            if (selectedControlPoint == cubicCurve.getControlPoint2()) {
                cubicCurve.moveControlPoint2(e.getX() - prevMousePosX, e.getY() - prevMousePosY);
            }
            cubicCurve.updateCurve();
            getMapPanel().repaint();
        }
    }

    @Override
    public void cancelCurve() {
        if (cubicCurve != null) cubicCurve.clear();
        isCubicCurveCreated = false;
        showConnectingLine = false;
        cubicCurve = null;
        curveStartNode = null;
        if (quadCurve == null) GUIBuilder.curveOptionsPanel.setVisible(false);
        getMapPanel().repaint();
    }

    @Override
    public void commitCurve() {
        if (cubicCurve != null) cubicCurve.commitCurve();
        isCubicCurveCreated = false;
        showConnectingLine = false;
        cubicCurve = null;
        curveStartNode = null;
        if (quadCurve == null) GUIBuilder.curveOptionsPanel.setVisible(false);
        getMapPanel().repaint();
    }

    @Override
    public void drawToScreen(Graphics2D g, Lock drawLock, double scaledSizeQuarter, double scaledSizeHalf) {

        //
        // draw the initial connection arrow
        //

        if (cubicCurve == null && showConnectingLine) {
            Point2D startNodePos = worldPosToScreenPos(curveStartNode.x, curveStartNode.z);
            Point2D mousePos = new Point2D.Double(currentMouseX,currentMouseY);

            backBufferGraphics.setColor(Color.WHITE);
            drawArrowBetween(backBufferGraphics, startNodePos, mousePos, false);
        }

        if (cubicCurve!= null && isCubicCurveCreated) {

            //
            //draw interpolation points for curve
            //

            Color colour;
            for (int j = 0; j < cubicCurve.curveNodesList.size() - 1; j++) {

                MapNode currentNode = cubicCurve.curveNodesList.get(j);
                MapNode nextNode = cubicCurve.curveNodesList.get(j + 1);

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

                if (cubicCurve.isReversePath()) {
                    if (cubicCurve.getNodeType() == NODE_FLAG_STANDARD) {
                        colour = colourConnectReverse;
                    } else {
                        colour = colourConnectReverseSubprio;
                    }
                } else if (cubicCurve.isDualPath()) {
                    if (cubicCurve.getNodeType() == NODE_FLAG_STANDARD) {
                        colour = colourConnectDual;
                    } else {
                        colour = colourConnectDualSubprio;
                    }
                } else if (currentNode.flag == 1) {
                    colour = colourConnectSubprio;
                } else {
                    colour = colourConnectRegular;
                }

                //
                // draw the connection arrows between the interpolation points
                //

                backBufferGraphics.setColor(colour);
                drawArrowBetween(backBufferGraphics, currentNodePos, nextNodePos, cubicCurve.isDualPath()) ;
            }

            //
            // draw the control nodes, this is done last to make them visible at all times
            //

            Polygon p = new Polygon();

            Point2D nodePos = worldPosToScreenPos(cubicCurve.getControlPoint1().x, cubicCurve.getControlPoint1().z);
            p.addPoint((int) (nodePos.getX() - scaledSizeQuarter), (int) (nodePos.getY() - scaledSizeQuarter));
            p.addPoint((int) (nodePos.getX() + scaledSizeQuarter), (int) (nodePos.getY() - scaledSizeQuarter));
            p.addPoint((int) nodePos.getX(), (int) (nodePos.getY() + scaledSizeQuarter));
            g.setColor(colourNodeControl);
            g.fillPolygon(p);

            if (cubicCurve.getControlPoint1().isSelected || hoveredNode == cubicCurve.getControlPoint1()) {
                Graphics2D g2 = (Graphics2D) backBufferGraphics.create();
                BasicStroke bs = new BasicStroke((float) (scaledSizeHalf / 5));
                g2.setStroke(bs);
                g2.setColor(colourNodeSelected);
                g2.drawPolygon(p);
                g2.dispose();
            }

            p.reset();

            nodePos = worldPosToScreenPos(cubicCurve.getControlPoint2().x, cubicCurve.getControlPoint2().z);
            p.addPoint((int) (nodePos.getX() - scaledSizeQuarter), (int) (nodePos.getY() - scaledSizeQuarter));
            p.addPoint((int) (nodePos.getX() + scaledSizeQuarter), (int) (nodePos.getY() - scaledSizeQuarter));
            p.addPoint((int) nodePos.getX(), (int) (nodePos.getY() + scaledSizeQuarter));
            g.setColor(colourNodeControl);
            g.fillPolygon(p);

            if (cubicCurve.getControlPoint2().isSelected || hoveredNode == cubicCurve.getControlPoint2()) {
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
