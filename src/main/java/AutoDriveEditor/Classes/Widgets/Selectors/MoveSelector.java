package AutoDriveEditor.Classes.Widgets.Selectors;

import java.awt.*;
import java.awt.event.MouseEvent;
import java.awt.geom.Point2D;
import java.awt.image.BufferedImage;

import static AutoDriveEditor.GUI.MapPanel.*;
import static AutoDriveEditor.GUI.MapPanel.nodeSizeScaledHalf;
import static AutoDriveEditor.GUI.Menus.DebugMenu.ShowWidgetManagerInfo.bDebugShowWidgetManagerInfo;
import static AutoDriveEditor.XMLConfig.EditorXML.*;
import static AutoDriveEditor.XMLConfig.EditorXML.freeMoveColor;

public class MoveSelector extends SelectorBase {

    // MoveWidget axis selectors
    //public enum SELECTED_AXIS {NONE, X, Y, BOTH}

    // Visual Selection indicators
    private final Polygon xAxisArrow = new Polygon();
    private final Polygon yAxisArrow = new Polygon();
    private final Rectangle freeMoveRect = new Rectangle();

    /** Constructor for the MoveSelector. */
    public MoveSelector() {
        setRenderPriority(256);
    }

    /**
     * Checks if the widget is selected based on mouse location.
     * @param e MouseEvent containing the mouse position.
     * @return The selected axis where the widget was selected, otherwise NONE.
     */
    public SELECTED_AXIS checkWidgetSelection(MouseEvent e) {
        if (ownerWidget.isEnabled()) {
            Point2D mousePosWorld = screenPosToWorldPos(e.getX(), e.getY());
            SELECTED_AXIS selectedAxis = SELECTED_AXIS.NONE;
            if (freeMoveRect.contains(e.getX(), e.getY())) {
                selectedAxis = SELECTED_AXIS.BOTH;
            } else if (xAxisArrow.contains(e.getX(), e.getY()) || xAxisArrow.contains(e.getX(), e.getY())) {
                selectedAxis = SELECTED_AXIS.X;
            } else if (yAxisArrow.contains(e.getX(), e.getY()) || yAxisArrow.contains(e.getX(), e.getY())) {
                selectedAxis = SELECTED_AXIS.Y;
            }

            if (selectedAxis != SELECTED_AXIS.NONE) {
                Point2D widgetWorldPos = ownerWidget.getWidgetPosWorld();
                selectOffsetWorld.setLocation(widgetWorldPos.getX() - mousePosWorld.getX(), widgetWorldPos.getY() - mousePosWorld.getY());
            }
            return selectedAxis;
        } else {
            return SELECTED_AXIS.NONE;
        }
    }

    public SELECTED_AXIS checkSelectedAxis(Point p) {
        if (ownerWidget.isEnabled()) {
            Point2D mousePosWorld = screenPosToWorldPos((int) p.getX(), (int) p.getY());
            SELECTED_AXIS selectedAxis = SELECTED_AXIS.NONE;
            if (freeMoveRect.contains(p.getX(), p.getY())) {
                selectedAxis = SELECTED_AXIS.BOTH;
            } else if (xAxisArrow.contains(p.getX(), p.getY()) || xAxisArrow.contains(p.getX(), p.getY())) {
                selectedAxis = SELECTED_AXIS.X;
            } else if (yAxisArrow.contains(p.getX(), p.getY()) || yAxisArrow.contains(p.getX(), p.getY())) {
                selectedAxis = SELECTED_AXIS.Y;
            }

            if (selectedAxis != SELECTED_AXIS.NONE) {
                Point2D widgetWorldPos = ownerWidget.getWidgetPosWorld();
                selectOffsetWorld.setLocation(widgetWorldPos.getX() - mousePosWorld.getX(), widgetWorldPos.getY() - mousePosWorld.getY());
            }
            return selectedAxis;
        } else {
            return SELECTED_AXIS.NONE;
        }
    }

    /**
     * Calculates the position of the move widget component and updates all the indicator shapes.
     */
    private void calcMoveSelectorPos() {
        Point widgetScreenPos = worldPosToScreenPos(ownerWidget.getWidgetPosWorld());
        if (freeMovePosition == FREEMOVE_POSITION.MANUAL) {
            freeMoveRect.setLocation((int) (widgetScreenPos.getX() - freeMoveOffsetX), (int) (widgetScreenPos.getY() - freeMoveOffsetY));
        } else if (freeMovePosition == FREEMOVE_POSITION.CENTER) {
            freeMoveRect.setLocation((int) (widgetScreenPos.getX() - (freeMoveRect.getWidth() / 2)), (int) (widgetScreenPos.getY() - (freeMoveRect.getHeight() / 2)));
        }
        switch (freeMoveSize) {
            case SMALL:
                freeMoveRect.setSize(freeMoveDefaultSmall, freeMoveDefaultSmall);
                break;
            case MEDIUM:
                freeMoveRect.setSize( freeMoveDefaultMedium, freeMoveDefaultMedium);
                break;
            case LARGE:
                freeMoveRect.setSize( freeMoveDefaultLarge, freeMoveDefaultLarge);
                break;
        }

        // calculate the x-axis location and length
        xAxisArrow.reset();
        if (xAxisDirection == X_DIRECTION.RIGHT) {
            xAxisArrow.addPoint((int) (widgetScreenPos.getX() + nodeSizeScaledHalf), (int) (widgetScreenPos.getY() + ((double) axisWidth / 2)));
            xAxisArrow.addPoint((int) (widgetScreenPos.getX() + (nodeSizeScaledHalf + axisLength)), (int) (widgetScreenPos.getY() + ((double) axisWidth / 2)));

            Point xArrowPoint = new Point((int) ((int) widgetScreenPos.getX() + nodeSizeScaledHalf + axisLength), (int) widgetScreenPos.getY());
            xAxisArrow.addPoint(xArrowPoint.x, xArrowPoint.y - arrowWidth);
            xAxisArrow.addPoint(xArrowPoint.x + arrowLength, xArrowPoint.y);
            xAxisArrow.addPoint(xArrowPoint.x, xArrowPoint.y + arrowWidth);

            xAxisArrow.addPoint((int) (widgetScreenPos.getX() + (nodeSizeScaledHalf + axisLength)), (int) (widgetScreenPos.getY() - ((double) axisWidth / 2)));
            xAxisArrow.addPoint((int) (widgetScreenPos.getX() + nodeSizeScaledHalf), (int) (widgetScreenPos.getY() - ((double) axisWidth / 2)));
        } else {
            xAxisArrow.addPoint((int) (widgetScreenPos.getX() - nodeSizeScaledHalf), (int) (widgetScreenPos.getY() - ((double) axisWidth / 2)));
            xAxisArrow.addPoint((int) (widgetScreenPos.getX() - (nodeSizeScaledHalf + axisLength)), (int) (widgetScreenPos.getY() - ((double) axisWidth / 2)));

            Point xArrowPoint = new Point((int) (widgetScreenPos.getX() - (nodeSizeScaledHalf + axisLength)), (int) widgetScreenPos.getY());
            xAxisArrow.addPoint(xArrowPoint.x, xArrowPoint.y + arrowWidth);
            xAxisArrow.addPoint(xArrowPoint.x - arrowLength, xArrowPoint.y);
            xAxisArrow.addPoint(xArrowPoint.x, xArrowPoint.y - arrowWidth);

            xAxisArrow.addPoint((int) (widgetScreenPos.getX() - (nodeSizeScaledHalf + axisLength)), (int) (widgetScreenPos.getY() + ((double) axisWidth / 2)));
            xAxisArrow.addPoint((int) (widgetScreenPos.getX() - nodeSizeScaledHalf), (int) (widgetScreenPos.getY() + ((double) axisWidth / 2)));
        }

        // calculate the y-axis location and length
        yAxisArrow.reset();
        if (yAxisDirection == Y_DIRECTION.UP) {
            yAxisArrow.addPoint((int) (widgetScreenPos.getX() - ((double) axisWidth / 2)), (int) (widgetScreenPos.getY() - nodeSizeScaledHalf));
            yAxisArrow.addPoint((int) (widgetScreenPos.getX() - ((double) axisWidth / 2)), (int) (widgetScreenPos.getY() - (nodeSizeScaledHalf + axisLength)));

            Point yArrowPoint = new Point((int) widgetScreenPos.getX(), (int) (widgetScreenPos.getY() - (nodeSizeScaledHalf + axisLength)));
            yAxisArrow.addPoint(yArrowPoint.x - arrowWidth, yArrowPoint.y);
            yAxisArrow.addPoint(yArrowPoint.x, yArrowPoint.y - arrowLength);
            yAxisArrow.addPoint(yArrowPoint.x + arrowWidth, yArrowPoint.y);

            yAxisArrow.addPoint((int) (widgetScreenPos.getX() + ((double) axisWidth / 2)), (int) (widgetScreenPos.getY() - (nodeSizeScaledHalf + axisLength)));
            yAxisArrow.addPoint((int) (widgetScreenPos.getX() + ((double) axisWidth / 2)), (int) (widgetScreenPos.getY() - nodeSizeScaledHalf));
        } else {
            yAxisArrow.addPoint((int) (widgetScreenPos.getX() - ((double) axisWidth / 2)), (int) (widgetScreenPos.getY() + nodeSizeScaledHalf));
            yAxisArrow.addPoint((int) (widgetScreenPos.getX() - ((double) axisWidth / 2)), (int) (widgetScreenPos.getY() + (nodeSizeScaledHalf + axisLength)));

            Point yArrowPoint = new Point((int) widgetScreenPos.getX(), (int) ((int) widgetScreenPos.getY() + nodeSizeScaledHalf + axisLength));
            yAxisArrow.addPoint(yArrowPoint.x - arrowWidth, yArrowPoint.y);
            yAxisArrow.addPoint(yArrowPoint.x, yArrowPoint.y + arrowLength);
            yAxisArrow.addPoint(yArrowPoint.x + arrowWidth, yArrowPoint.y);

            yAxisArrow.addPoint((int) (widgetScreenPos.getX() + ((double) axisWidth / 2)), (int) (widgetScreenPos.getY() + (nodeSizeScaledHalf + axisLength)));
            yAxisArrow.addPoint((int) (widgetScreenPos.getX() + ((double) axisWidth / 2)), (int) (widgetScreenPos.getY() + nodeSizeScaledHalf));
        }
    }

    /**
     * Creates a hatch pattern for the free move area.
     * @return BufferedImage containing the hatch pattern.
     */
    private BufferedImage createHatchPattern() {
        int size = 10;
        BufferedImage img = new BufferedImage(size, size, BufferedImage.TYPE_INT_ARGB);
        Graphics2D g2 = img.createGraphics();
        g2.setColor(freeMoveColor);
        g2.drawLine(0, 0, size, size);
        g2.drawLine(0, size, size, 0);
        g2.dispose();
        return img;
    }

    @Override
    public void setSelectorEnabled(boolean enabled) {
        super.setSelectorEnabled(enabled);
        if (enabled) {
            calcMoveSelectorPos();
        }
    }

    @Override
    public void updateSelector() {
        if (ownerWidget.isEnabled()) { calcMoveSelectorPos(); }
    }

    @Override
    public void drawToScreen(Graphics g) {
        if (ownerWidget != null && ownerWidget.getCurrentAnchor() != null && this.isEnabled) {
            Graphics2D g2d = (Graphics2D) g.create();

            if (bDebugShowWidgetManagerInfo) {
                g2d.setColor(Color.WHITE);
                g2d.drawString(ownerWidget.toString(), (int) freeMoveRect.getX(), (int) freeMoveRect.getY() - freeMoveRect.height);
            }

            // Draw the x-axis
            g2d.setColor(xAxisColor);
            g2d.fillPolygon(xAxisArrow);

            // Draw the y-axis
            g2d.setColor(yAxisColor);
            g2d.fillPolygon(yAxisArrow);

            // draw the free move area
            g2d.setColor(freeMoveColor);
            // If selected shape is square, draw the outline
            if (freeMoveType == FREEMOVE_TYPE.SQUARE) {
                g2d.drawRect((int) freeMoveRect.getX(), (int) freeMoveRect.getY(), (int) freeMoveRect.getWidth(), (int) freeMoveRect.getHeight());
            }

            // Modify the rounded rectangle arc depending on the selected shape
            // If square selected, do not round off the edges
            // If round selected, set the rounding to the size of the free move rectangle, appearing as circular
            int arc = (freeMoveType == FREEMOVE_TYPE.ROUND) ? (int) freeMoveRect.getWidth() : 0;

            // Draw the outline of the free move area
            if (freeMoveStyle == FREEMOVE_STYLE.PATTERN || freeMoveStyle == FREEMOVE_STYLE.OUTLINE) {
                g2d.setPaint(freeMoveColor);
                g2d.drawRoundRect((int) freeMoveRect.getX(), (int) freeMoveRect.getY(), (int) freeMoveRect.getWidth(), (int) freeMoveRect.getHeight(), arc, arc);
            }

            // Select the fill type of the rectangle
            switch (freeMoveStyle) {
                case SOLID:
                    g2d.setPaint(freeMoveColor);
                case OUTLINE:
                    break;
                case PATTERN:
                    g2d.setPaint(new TexturePaint(createHatchPattern(), new Rectangle(0, 0, 2, 2)));
                    break;
            }

            // Draw the filled part of the rectangle ( if needed )
            if (freeMoveStyle == FREEMOVE_STYLE.SOLID || freeMoveStyle == FREEMOVE_STYLE.PATTERN) {
                g2d.fillRoundRect((int) freeMoveRect.getX(), (int) freeMoveRect.getY(), (int) freeMoveRect.getWidth(), (int) freeMoveRect.getHeight(), arc, arc);
            }
        }
    }


}
