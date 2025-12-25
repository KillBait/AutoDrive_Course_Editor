package AutoDriveEditor.GUI.Buttons.Toolbar.Curves;

import AutoDriveEditor.Classes.Interfaces.WidgetInterface;
import AutoDriveEditor.Classes.KeyBinds.Shortcut;
import AutoDriveEditor.Classes.UI_Components.ScaleAnimIcon;
import AutoDriveEditor.Classes.Widgets.CurveWidget;
import AutoDriveEditor.GUI.Buttons.BaseButton;
import AutoDriveEditor.Managers.ShortcutManager;
import AutoDriveEditor.RoadNetwork.MapNode;

import javax.swing.*;
import java.awt.*;
import java.awt.event.ActionEvent;
import java.awt.event.MouseEvent;
import java.awt.geom.Point2D;

import static AutoDriveEditor.AutoDriveEditor.*;
import static AutoDriveEditor.Classes.Curves.ArcSpline.CURVE_TYPE_ARCSPLINE;
import static AutoDriveEditor.Classes.Curves.BezierCurve.CURVE_TYPE_BEZIER;
import static AutoDriveEditor.Classes.Util_Classes.LoggerUtils.LOG;
import static AutoDriveEditor.GUI.MapPanel.getNodeAtScreenPosition;
import static AutoDriveEditor.GUI.MapPanel.worldPosToScreenPos;
import static AutoDriveEditor.GUI.Menus.DebugMenu.Logging.LogCurveInfoMenu.bDebugLogCurveInfo;
import static AutoDriveEditor.GUI.TextPanel.showInTextArea;
import static AutoDriveEditor.Locale.LocaleManager.getLocaleString;
import static AutoDriveEditor.Managers.IconManager.*;
import static AutoDriveEditor.Managers.RenderManager.PRIORITY_HIGH;
import static AutoDriveEditor.Managers.ShortcutManager.ShortcutID.BEZIER_CURVE_SHORTCUT;
import static AutoDriveEditor.Managers.ShortcutManager.getUserShortcutByID;
import static AutoDriveEditor.Managers.ShortcutManager.registerShortcut;

public final class BezierCurveButton extends BaseButton {

    private MapNode curveStartNode;
    private final Point2D selectEnd = new Point2D.Double();
    private boolean showConnectingLine = false;

    private WidgetInterface curveWidget;

    @Override
    public String getButtonID() { return "BezierCurveButton"; }

    @Override
    public String getButtonAction() { return "ActionButton"; }

    @Override
    public String getButtonPanel() { return "Curves"; }

    @Override
    public String getInfoText() { return getLocaleString("toolbar_nodes_curve_bezier_infotext"); }

    @Override
    public void onButtonDeselect() {
        super.onButtonDeselect();
        cancelSelection();
    }

    public BezierCurveButton(JPanel panel) {
        ScaleAnimIcon animCubicCurveIcon = createScaleAnimIcon(BUTTON_BEZIER_CURVE_ICON, false);
        button = createAnimToggleButton(animCubicCurveIcon, panel, null, null,  false, false, this);
        setRenderPriority(PRIORITY_HIGH);

        // Setup Keyboard Shortcuts
        Shortcut moveShortcut = getUserShortcutByID(BEZIER_CURVE_SHORTCUT);
        if (moveShortcut != null) {
            Action moveButtonAction = new AbstractAction() {
                @Override
                public void actionPerformed(ActionEvent e) {
                    if (button.isEnabled() && !button.isSelected()) {
                        buttonManager.makeCurrent(buttonNode);
                    } else {
                        buttonManager.deSelectAll();
                    }
                }
            };
            registerShortcut(this, moveShortcut, moveButtonAction, getMapPanel());
        }
    }

    public void mouseReleased(MouseEvent e) {
        if (e.getButton() == MouseEvent.BUTTON1) {
            MapNode selected = getNodeAtScreenPosition(e.getX(), e.getY());
            if (selected != null && !selected.isControlNode()) {
                if (curveStartNode == null) {
                    curveStartNode = selected;
                    selectEnd.setLocation(selected.getScreenPosition2D());
                    showInTextArea(getLocaleString("toolbar_curves_select_end"), true, false);
                    showConnectingLine = true;
                } else if (selected == curveStartNode) {
                    curveStartNode = null;
                    showInTextArea(getLocaleString("toolbar_curves_canceled"), true, false);
                    showConnectingLine = false;
                } else {
                    showInTextArea(getLocaleString("toolbar_curves_preview"), true, false);
                    if (bDebugLogCurveInfo) LOG.info("Creating Bezier Curve Preview {} -> {}", curveStartNode, selected);
                    curveManager.addActiveCurve(this.getButtonID(), CURVE_TYPE_BEZIER, CurveWidget.class, curveStartNode, selected);
                    curveStartNode = null;
                    showConnectingLine = false;
                }
                getMapPanel().repaint();
            }
        } else if (e.getButton() == MouseEvent.BUTTON3) {
            if (curveStartNode != null) {
                showInTextArea(getLocaleString("toolbar_curves_canceled"), true, false);
                cancelSelection();
            }
        }
    }

    @Override
    public void mouseMoved(MouseEvent e) {
        if (showConnectingLine) {
            MapNode endNode = getNodeAtScreenPosition(e.getX(), e.getY());
            if (endNode != null) {
                Point2D nodePose = worldPosToScreenPos(endNode.x, endNode.z);
                selectEnd.setLocation(nodePose);
            } else {
                selectEnd.setLocation(e.getX(), e.getY());
            }
            getMapPanel().repaint();
        }
    }

    @Override
    public void mouseDragged(MouseEvent e) {
        if (showConnectingLine) {
            selectEnd.setLocation(e.getX(), e.getY());
            getMapPanel().repaint();
        }
    }

    private void cancelSelection() {
        curveStartNode = null;
        showConnectingLine = false;
        getMapPanel().repaint();
    }

    @Override
    public void drawToScreen(Graphics g) {
        // draw the initial connection arrow
        if (showConnectingLine) {
            Point2D startNodePos = worldPosToScreenPos(curveStartNode.x, curveStartNode.z);
            drawArrowBetween(g, startNodePos, selectEnd, false, Color.WHITE);
        }
    }

    @Override
    public String buildToolTip() {
        Shortcut s = ShortcutManager.getUserShortcutByID(BEZIER_CURVE_SHORTCUT);
        if (s != null) {
            return getLocaleString("toolbar_nodes_curve_bezier_tooltip") + "\n\n( Shortcut: " + s.getShortcutString() + " )";
        } else {
            return getLocaleString("toolbar_nodes_curve_bezier_tooltip");
        }
    }
}
