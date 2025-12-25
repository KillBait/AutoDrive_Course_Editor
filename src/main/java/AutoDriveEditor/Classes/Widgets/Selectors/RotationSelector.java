package AutoDriveEditor.Classes.Widgets.Selectors;

import java.awt.*;
import java.awt.event.MouseEvent;
import java.awt.geom.AffineTransform;
import java.awt.geom.Arc2D;
import java.awt.geom.GeneralPath;
import java.awt.geom.Point2D;

import static AutoDriveEditor.AutoDriveEditor.curveManager;
import static AutoDriveEditor.Classes.Util_Classes.MathUtils.getPointOnCircleEdge;
import static AutoDriveEditor.GUI.MapPanel.*;
import static AutoDriveEditor.GUI.Menus.DebugMenu.ShowWidgetManagerInfo.bDebugShowWidgetManagerInfo;
import static AutoDriveEditor.XMLConfig.EditorXML.freeMoveColor;

public class RotationSelector extends SelectorBase {

    // GeneralPath for creating the selection indicators
    private final GeneralPath arrowShape;

    /** Constructor for the RotationSelector.
     * Initializes the arrowShape use for the indicator.
     */
    public RotationSelector() {
        arrowShape = new GeneralPath();
    }

    /**
     * Checks if the widget is selected based on mouse location.
     * @param e MouseEvent containing the mouse position.
     * @return The selected axis where the widget was selected, otherwise NONE.
     */
    public SELECTED_AXIS checkWidgetSelection(MouseEvent e) {
        if (ownerWidget.isEnabled()) {
            SELECTED_AXIS selectedAxis = SELECTED_AXIS.NONE;
            if (arrowShape.contains(e.getX(), e.getY())) {
                selectedAxis = SELECTED_AXIS.BOTH;
            }
            if (selectedAxis != SELECTED_AXIS.NONE) {
                Point2D widgetWorldPos = ownerWidget.getWidgetPosWorld();
                Point2D mousePosWorld = screenPosToWorldPos(e.getX(), e.getY());
                selectOffsetWorld.setLocation(widgetWorldPos.getX() - mousePosWorld.getX(), widgetWorldPos.getY() - mousePosWorld.getY());
            }
            return selectedAxis;
        }
        return SELECTED_AXIS.NONE;
    }

    public SELECTED_AXIS checkSelectedAxis(Point p) {
        if (ownerWidget.isEnabled()) {
            SELECTED_AXIS selectedAxis = SELECTED_AXIS.NONE;
            if (arrowShape.contains(p.getX(), p.getY())) {
                selectedAxis = SELECTED_AXIS.BOTH;
            }
            if (selectedAxis != SELECTED_AXIS.NONE) {
                Point2D widgetWorldPos = ownerWidget.getWidgetPosWorld();
                Point2D mousePosWorld = screenPosToWorldPos((int) p.getX(), (int) p.getY());
                selectOffsetWorld.setLocation(widgetWorldPos.getX() - mousePosWorld.getX(), widgetWorldPos.getY() - mousePosWorld.getY());
            }
            return selectedAxis;
        }
        return SELECTED_AXIS.NONE;
    }

    /**
     * Calculates the position of the rotation widget component and updates the arrowShape.
     * Creates the visual selector based on the owner widget's position and the current rotation angle
     */
    private void calcRotationSelectorPos() {
        arrowShape.reset();
        Point widgetScreenPos = worldPosToScreenPos(ownerWidget.getWidgetPosWorld());

        double innerRadius = nodeSizeScaled * 1;
        double outerRadius = nodeSizeScaled * 1.25;// Radius of the rotation circle
        double arrowSize = 30; // Size of the arrowheads
        double startAngleDeg = 155; // Start angle in degrees
        double endAngleDeg = 205; // End angle in degrees

        Arc2D innerArc = new Arc2D.Double(
                widgetScreenPos.getX() - innerRadius, widgetScreenPos.getY() - innerRadius,
                innerRadius * 2, innerRadius * 2,
                endAngleDeg, startAngleDeg - endAngleDeg,
                Arc2D.OPEN
        );

        Arc2D outerArc = new Arc2D.Double(
                widgetScreenPos.getX() - outerRadius, widgetScreenPos.getY() - outerRadius,
                outerRadius * 2, outerRadius * 2,
                startAngleDeg, endAngleDeg - startAngleDeg,
                Arc2D.OPEN
        );

        arrowShape.append(innerArc, false);

        Point2D arrow1 = getPointOnCircleEdge(widgetScreenPos, innerRadius-(innerRadius/4), -startAngleDeg);
        arrowShape.lineTo(arrow1.getX(), arrow1.getY());
        Point2D arrow2 = getPointOnCircleEdge(widgetScreenPos, innerRadius + ((outerRadius - innerRadius) / 1.2), -startAngleDeg+arrowSize);
        arrowShape.lineTo(arrow2.getX(), arrow2.getY());
        Point2D arrow3 = getPointOnCircleEdge(widgetScreenPos, outerRadius+(innerRadius/4), -startAngleDeg);
        arrowShape.lineTo(arrow3.getX(), arrow3.getY());

        arrowShape.append(outerArc, true);
        Point2D arrow4 = getPointOnCircleEdge(widgetScreenPos, outerRadius+(innerRadius/4), -endAngleDeg);
        arrowShape.lineTo(arrow4.getX(), arrow4.getY());
        Point2D arrow5 = getPointOnCircleEdge(widgetScreenPos, innerRadius + ((outerRadius - innerRadius) / 1.2), -endAngleDeg-arrowSize);
        arrowShape.lineTo(arrow5.getX(), arrow5.getY());
        Point2D arrow6 = getPointOnCircleEdge(widgetScreenPos, innerRadius-(innerRadius/4), -endAngleDeg);
        arrowShape.lineTo(arrow6.getX(), arrow6.getY());
        arrowShape.closePath();

        // Rotate the shape around the rotation node
        int rotationAngle;
        if (curveManager.getCurrentCurve() != null) {
            rotationAngle = curveManager.getCurrentCurve().getRotationAngle(ownerWidget.getCurrentAnchor());
        } else {
            rotationAngle = 0;
        }
        //int rotationAngle = curveManager.getCurrentCurve().getRotationAngle(ownerWidget.getCurrentAnchor());
        AffineTransform transform = AffineTransform.getRotateInstance(
                Math.toRadians(rotationAngle+90),
                widgetScreenPos.getX(),
                widgetScreenPos.getY()
        );
        arrowShape.transform(transform);
    }

    /**
     * Sets the selector enabled state and if enabled is true will recalculate the
     * position of the rotation widget visual component.
     * @param enabled true to enable the selector, false to disable it.
     */
    @Override
    public void setSelectorEnabled(boolean enabled) {
        super.setSelectorEnabled(enabled);
        if (enabled) {
            calcRotationSelectorPos();
        }
    }

    /**
     * Update the visual indicator of the widget if the selector is enabled and visible.
     */
    @Override
    public void updateSelector() {
        if (ownerWidget.isEnabled()) calcRotationSelectorPos();
    }

    // draw the selectors visual component to the screen
    @Override
    public void drawToScreen(Graphics g) {
        if (this.isEnabled) {
            Graphics2D g2d = (Graphics2D) g.create();

            if (bDebugShowWidgetManagerInfo) {
                g2d.setColor(Color.WHITE);
                g2d.drawString(ownerWidget.toString(), (int) arrowShape.getCurrentPoint().getX(), (int) arrowShape.getCurrentPoint().getY());
            }

            g2d.setColor(freeMoveColor);
            g2d.fill(arrowShape);
            g2d.dispose();
        }
    }
}
