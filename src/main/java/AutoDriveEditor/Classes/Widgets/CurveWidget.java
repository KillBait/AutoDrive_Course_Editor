package AutoDriveEditor.Classes.Widgets;

import AutoDriveEditor.Classes.Widgets.Selectors.MoveSelector;
import AutoDriveEditor.Managers.CurveManager;
import AutoDriveEditor.RoadNetwork.MapNode;

import java.awt.event.MouseEvent;
import java.awt.geom.Point2D;

import static AutoDriveEditor.AutoDriveEditor.*;
import static AutoDriveEditor.Classes.Curves.BezierCurve.CURVE_TYPE_BEZIER;
import static AutoDriveEditor.Classes.Util_Classes.LoggerUtils.LOG;
import static AutoDriveEditor.GUI.MapPanel.getNodeAtScreenPosition;
import static AutoDriveEditor.GUI.MapPanel.setStale;
import static AutoDriveEditor.GUI.Menus.DebugMenu.Logging.LogCurveWidgetMenu.bDebugLogCurveWidget;
import static AutoDriveEditor.Managers.MultiSelectManager.multiSelectList;

public class CurveWidget extends MoveWidget {

    public CurveWidget() {
        super();
    }

    @Override
    public void mouseReleased(MouseEvent e) {
        if (e.getButton() == MouseEvent.BUTTON1) {
            if (isDraggingWidget) {
                changeManager.addChangeable(new MoveWidgetChanger(multiSelectList, moveDiffX, moveDiffY));
                setStale(true);
                isDraggingWidget = false;
            } else {
                MapNode selectedNode = getNodeAtScreenPosition(e.getX(), e.getY());
                if (selectedNode != null) {
                    CurveManager.CurveInfo c = curveManager.getCurveForNode(selectedNode);
                    if (c != null && c.getCurveType().equals(CURVE_TYPE_BEZIER)) {
                        curveManager.setActiveCurve(c);
                        if (bDebugLogCurveWidget) LOG.info("## {}.mouseReleased() ## Selected Bezier Curve: {}", instanceName, c.getCurve());
                        if (selectedNode.isControlNode()) {
                            if (c.getWidget().getCurrentSelector().checkSelectedAxis(e.getPoint()) == MoveSelector.SELECTED_AXIS.NONE) {
                                if (bDebugLogCurveWidget) LOG.info("## {}.mouseReleased() ## Setting anchor to {}", instanceName, selectedNode);
                                c.getWidget().setCurrentAnchor(selectedNode);
                                c.setActiveNode(selectedNode);
                                curveManager.updateAllCurves();
                            } else {
                                setCurrentAnchor(null);
                            }
                        }
                    }
                }
                isDraggingWidget = false;
            }
        } else if (e.getButton() == MouseEvent.BUTTON3) {
            if (bDebugLogCurveWidget) LOG.info("## {}.mouseReleased() ## Right click - clearing anchor node", instanceName);
            setCurrentAnchor(null);
        }
        getMapPanel().repaint();
    }

    @Override
    public void mouseDragged(MouseEvent e) {
        if (isDraggingWidget) {
            // calculate the move difference from the last stored position
            Point2D diff = calcMoveOffset(e.getPoint());

            moveDiffX += diff.getX();
            moveDiffY += diff.getY();
            moveNodesBy(this.getCurrentAnchor(), diff.getX(), diff.getY(), false);
            widgetPosX += diff.getX();
            widgetPosY += diff.getY();
            moveWidgetWorldPosBy(diff.getX(), diff.getY());
        }
        widgetManager.updateAllWidgets();
        getMapPanel().repaint();
    }

    @Override
    public void moveNodesBy(MapNode node, double diffX, double diffY, boolean applyScaling) {
        CurveManager.CurveInfo curve = curveManager.getCurveNode(node);
        if (node.isControlNode()) {
            curve.getCurve().moveControlNodeBy(node, diffX, diffY, true);
        }
        curveManager.updateAllCurves();
    }
}
