package AutoDriveEditor.Classes.Widgets.Selectors;

import AutoDriveEditor.Classes.Interfaces.ArcSplineInterface;

import java.awt.*;
import java.awt.event.MouseEvent;
import java.awt.geom.AffineTransform;
import java.awt.geom.GeneralPath;

import static AutoDriveEditor.AutoDriveEditor.curveManager;
import static AutoDriveEditor.GUI.MapPanel.*;


/**
 * Work in progress: Not release ready
 */

public class RadiusSelector extends SelectorBase {

    private final GeneralPath radiusShape;

    public RadiusSelector() {
        radiusShape = new GeneralPath();
    }

    public MoveSelector.SELECTED_AXIS checkWidgetSelection(MouseEvent e) {
        if (ownerWidget.isEnabled()) {
            MoveSelector.SELECTED_AXIS selectedAxis = MoveSelector.SELECTED_AXIS.NONE;
            if (radiusShape.contains(e.getX(), e.getY())) {
                selectedAxis = MoveSelector.SELECTED_AXIS.BOTH;
            }
            return selectedAxis;
        }
        return MoveSelector.SELECTED_AXIS.NONE;
    }

    public MoveSelector.SELECTED_AXIS checkSelectedAxis(Point p) {
        if (ownerWidget.isEnabled()) {
            MoveSelector.SELECTED_AXIS selectedAxis = MoveSelector.SELECTED_AXIS.NONE;
            if (getNodeAtScreenPosition((int) p.getX(), (int) p.getY()) != null) {
                selectedAxis = MoveSelector.SELECTED_AXIS.BOTH;
            }
            return selectedAxis;
        }
        return MoveSelector.SELECTED_AXIS.NONE;
    }

    /**
     * Calculates the position of the rotation widget component and updates the arrowShape.
     * Creates the visual selector based on the owner widget's position and the current rotation angle
     */
    private void calcSelectorPosition() {
        radiusShape.reset();
        Point widgetPos = worldPosToScreenPos(ownerWidget.getCurrentAnchor().getWorldPosition2D());
        double length = nodeSizeScaled - (nodeSizeScaled / 5); // Example radius size, adjust as needed
        double arrowSize = nodeSizeScaled / 4;

        //radiusShape.moveTo(widgetPos.getX(),widgetPos.getY() - (arrowSize/3));
        radiusShape.moveTo(widgetPos.getX() - ((length/2) - (arrowSize/1.4)), widgetPos.getY() - (arrowSize/3));
        radiusShape.lineTo(widgetPos.getX() + ((length/2) - (arrowSize/1.4)), widgetPos.getY() - (arrowSize/3));
        radiusShape.lineTo(widgetPos.getX() + ((length/2) - (arrowSize/1.4)), widgetPos.getY() - (arrowSize/1.25));
        radiusShape.lineTo(widgetPos.getX() + (length / 2), widgetPos.getY());
        radiusShape.lineTo(widgetPos.getX() + ((length/2) - (arrowSize/1.4)), widgetPos.getY() + (arrowSize/1.25));
        radiusShape.lineTo(widgetPos.getX() + ((length/2) - (arrowSize/1.4)), widgetPos.getY() + (arrowSize/3));
        radiusShape.lineTo(widgetPos.getX() - ((length/2) - (arrowSize/1.4)),widgetPos.getY() + (arrowSize/3));
        radiusShape.lineTo(widgetPos.getX() - ((length/2) - (arrowSize/1.4)), widgetPos.getY() + (arrowSize/1.25));
        radiusShape.lineTo(widgetPos.getX() - (length / 2), widgetPos.getY());
        radiusShape.lineTo(widgetPos.getX() - ((length/2) - (arrowSize/1.4)), widgetPos.getY() - (arrowSize/1.25));
        radiusShape.closePath();

        // Rotate the shape around the rotation node
        ArcSplineInterface a = (ArcSplineInterface) curveManager.getCurrentCurve();
        //Point widgetScreenPos = worldPosToScreenPos(ownerWidget.getWidgetPosWorld());
        double rotationAngle = a.getRadiusAngle();
//        LOG.info("Rotation Angle for Radius Selector: {}", rotationAngle);
        AffineTransform transform = AffineTransform.getRotateInstance(
                rotationAngle,
                widgetPos.getX(),
                widgetPos.getY()
        );


        radiusShape.transform(transform);
    }

    /**
     * Sets the selector enabled state and if enabled is true will recalculate the
     * position of the rotation widget visual component.
     * @param enabled true to enable the selector, false to disable it.
     */

    @Override
    public void setSelectorEnabled(boolean enabled) {
        super.setSelectorEnabled(enabled);
        if (enabled) calcSelectorPosition();
    }

    /**
     * Update the visual indicator of the widget if the selector is enabled.
     */
    @Override
    public void updateSelector() {
        if (ownerWidget.isEnabled()) calcSelectorPosition();
    }



    @Override
    public void drawToScreen(Graphics g) {
        // Draw a circle around the widget position to indicate radius selection
        if (this.isEnabled) {
            Graphics2D g2d = (Graphics2D) g.create();

            Point widgetPos = worldPosToScreenPos(ownerWidget.getCurrentAnchor().getWorldPosition2D());
            int radius = (int) nodeSizeScaled / 2; // Example radius size, adjust as needed
            g2d.setColor(Color.RED);
            g2d.drawOval((int)widgetPos.getX() - radius, (int)widgetPos.getY() - radius, radius * 2, radius * 2);

            g2d.fill(radiusShape);
            g2d.dispose();
        }
    }


}
