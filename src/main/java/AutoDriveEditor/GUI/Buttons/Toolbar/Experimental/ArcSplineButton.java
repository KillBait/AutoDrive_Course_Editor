package AutoDriveEditor.GUI.Buttons.Toolbar.Experimental;

import AutoDriveEditor.Classes.Interfaces.WidgetInterface;
import AutoDriveEditor.Classes.UI_Components.ScaleAnimIcon;
import AutoDriveEditor.Classes.Widgets.ArcSplineWidget;
import AutoDriveEditor.GUI.Buttons.BaseButton;
import AutoDriveEditor.RoadNetwork.MapNode;

import javax.swing.*;
import java.awt.event.MouseEvent;
import java.awt.geom.Point2D;

import static AutoDriveEditor.AutoDriveEditor.curveManager;
import static AutoDriveEditor.Classes.Curves.ArcSpline.CURVE_TYPE_ARCSPLINE;
import static AutoDriveEditor.Classes.Util_Classes.LoggerUtils.LOG;
import static AutoDriveEditor.GUI.MapPanel.getNodeAtScreenPosition;
import static AutoDriveEditor.GUI.MapPanel.screenPosToWorldPos;
import static AutoDriveEditor.GUI.Menus.DebugMenu.Logging.LogButtonManagerInfoMenu.bDebugLogButtonManagerInfo;
import static AutoDriveEditor.GUI.Menus.DebugMenu.Logging.LogCurveInfoMenu.bDebugLogCurveInfo;
import static AutoDriveEditor.Locale.LocaleManager.getLocaleString;
import static AutoDriveEditor.Managers.IconManager.*;
import static AutoDriveEditor.Managers.RenderManager.PRIORITY_HIGH;

public final class ArcSplineButton extends BaseButton {

    @Override
    public String getButtonID() { return "ArcSplineButton"; }

    @Override
    public String getButtonAction() { return "ActionButton"; }

    @Override
    public String getButtonPanel() { return "Curves"; }

    @Override
    public String getInfoText() { return getLocaleString("toolbar_nodes_curve_arc_infotext"); }

    public ArcSplineButton(JPanel panel) {
        ScaleAnimIcon animCubicCurveIcon = createScaleAnimIcon(BUTTON_ARC_SPLINE_ICON, false);
        button = createAnimToggleButton(animCubicCurveIcon, panel, null, null,  false, false, this);
        setRenderPriority(PRIORITY_HIGH);
    }

    @Override
    public void mouseReleased(MouseEvent e) {
        if (e.getButton() == MouseEvent.BUTTON1) {
            WidgetInterface widget = curveManager.getCurrentCurveWidget();
            Point2D worldPos = screenPosToWorldPos(e.getX(), e.getY());
            if (widget == null) {
                if (bDebugLogButtonManagerInfo) LOG.info("## {}.mouseReleased() ## No widget currently active, creating ArcSpline at world position X = {}, Z = {}", this.getClass().getSimpleName(), worldPos.getX(), worldPos.getY());
                createArc(worldPos);
            } else if (!widget.getCurrentSelector().isSelected()) {
                MapNode node = getNodeAtScreenPosition(e.getX(), e.getY());
                if (!curveManager.getAllActiveControlNodes().contains(node)) {
                    if (bDebugLogCurveInfo) LOG.info("## {}.mouseReleased() ## Creating Arc Spline Preview at world position X = {}, Z = {}", this.getClass().getSimpleName(), worldPos.getX(), worldPos.getY());
                    createArc(worldPos);

                }
            }
        }
    }

    private void createArc(Point2D centrePoint) {
        if (bDebugLogCurveInfo) LOG.info("Creating Arc at {}", centrePoint);
        curveManager.addActiveCurve(this.getButtonID(), CURVE_TYPE_ARCSPLINE, ArcSplineWidget.class, centrePoint);
    }

    @Override
    public String buildToolTip() {
        return getLocaleString("toolbar_nodes_curve_arc_tooltip");
    }
}
