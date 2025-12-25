package AutoDriveEditor.Classes.Widgets;

import AutoDriveEditor.Classes.Interfaces.ArcSplineInterface;
import AutoDriveEditor.Classes.Interfaces.SelectorInterface;
import AutoDriveEditor.Classes.Widgets.Selectors.MoveSelector;
import AutoDriveEditor.Classes.Widgets.Selectors.RadiusSelector;
import AutoDriveEditor.Classes.Widgets.Selectors.RotationSelector;
import AutoDriveEditor.Managers.CurveManager;
import AutoDriveEditor.RoadNetwork.MapNode;

import java.awt.*;
import java.awt.event.MouseEvent;
import java.awt.geom.Point2D;
import java.util.ArrayList;

import static AutoDriveEditor.AutoDriveEditor.*;
import static AutoDriveEditor.Classes.Curves.ArcSpline.CURVE_TYPE_ARCSPLINE;
import static AutoDriveEditor.Classes.Util_Classes.MathUtils.roundUpDoubleToDecimalPlaces;
import static AutoDriveEditor.GUI.MapPanel.getNodeAtScreenPosition;

public class ArcSplineWidget extends MoveWidget {


    public ArcSplineWidget() {
        super();
    }

    @Override
    public void initSelectors() {
        SelectorInterface moveSelector = new MoveSelector();
        selectorList.add(moveSelector);
        setCurrentSelector(moveSelector);
        selectorList.add(new RotationSelector());
        selectorList.add(new RadiusSelector());
    }

    @Override
    public void mouseReleased(MouseEvent e) {
        MapNode selectedNode = getNodeAtScreenPosition(e.getX(), e.getY());
        if (e.getButton() == MouseEvent.BUTTON1) {
            if (selectedNode != null) {
                CurveManager.CurveInfo c = curveManager.getCurveForNode(selectedNode);
                if (c != null && c.getCurveType().equals(CURVE_TYPE_ARCSPLINE)) {
                    curveManager.setActiveCurve(c);
                    setCurrentSelector(selectedNode.getNodeSelector());
                    c.getWidget().setCurrentAnchor(selectedNode);
                    c.setActiveNode(selectedNode);
                    curveManager.updateAllCurves();
                }
            }
        } else if (e.getButton() == MouseEvent.BUTTON3) {
            if (selectedNode == null) {
                setCurrentAnchor(null);
                getMapPanel().repaint();
            }
        }
        isDraggingWidget = false;
    }

    @Override
    public void mouseDragged(MouseEvent e) {
        if (isDraggingWidget) {
            // calculate the move difference from the last stored position
            Point2D diff = calcMoveOffset(e.getPoint());
            ArcSplineInterface curve = (ArcSplineInterface) curveManager.getCurrentCurve();
            MapNode curveNode1 = curve.getActiveControlPoints().get(1);
            MapNode curveNode2 = curve.getActiveControlPoints().get(2);

            if (currentAnchor.isControlNode()) {
                moveNodesBy(currentAnchor, diff.getX(), diff.getY(), false);
                moveWidgetWorldPosBy(diff.getX(), diff.getY());
            } else if (currentAnchor.isRotationNode()) {
                Point2D rotationDiff = new Point2D.Double(0,0);
                if (currentAnchor == curveNode1) {
                    rotationDiff = curve.rotateStartNode(curveNode1, e.getX(), e.getY());
                } else if (currentAnchor == curveNode2) {
                    rotationDiff = curve.rotateEndNode(curveNode2, e.getX(), e.getY());
                }
                if (rotationDiff != null) moveWidgetWorldPosBy(rotationDiff.getX(), rotationDiff.getY());
            } else if (currentAnchor.isSpecialNode()) {
                curve.moveControlNodeBy(currentAnchor, diff.getX(), diff.getY(), false);
                moveWidgetWorldPosBy(diff.getX(), diff.getY());
                curve.updateCurve();
            }
        }
        widgetManager.updateAllWidgets();
    }

    @Override
    public void moveNodesBy(MapNode controlNode, double diffX, double diffY, boolean applyScaling) {
        ArrayList<MapNode> curveList = curveManager.getCurrentCurve().getActiveControlPoints();
        for (MapNode curveNode : curveList) {
            curveNode.x = roundUpDoubleToDecimalPlaces(curveNode.x + diffX, 3);
            curveNode.z = roundUpDoubleToDecimalPlaces(curveNode.z + diffY, 3);
        }
        curveManager.getCurrentCurve().updateCurve();
        getMapPanel().repaint();
    }
}
