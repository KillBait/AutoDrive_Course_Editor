package AutoDriveEditor.GUI.Config;

import javax.swing.*;
import java.awt.*;
import java.awt.geom.Point2D;
import java.awt.image.BufferedImage;

import static AutoDriveEditor.MapPanel.MapPanel.getMapPanel;
import static AutoDriveEditor.Utils.ImageUtils.getNewBufferImage;
import static AutoDriveEditor.Utils.MathUtils.normalizeAngle;
import static AutoDriveEditor.XMLConfig.EditorXML.*;

public class ColourPreviewPanel extends JPanel {

    private final BufferedImage previewImage;
    private final Graphics2D previewGraphics;
    private static boolean isControlNodePreview;
    public static Color nodeColour = colourNodeRegular;
    public static Color connectionColour = colourConnectRegular;

    public static boolean isSelected;
    public static boolean connectionDual;

    public ColourPreviewPanel() {
        previewImage = getNewBufferImage(196,40, Transparency.BITMASK);
        previewGraphics = (Graphics2D) previewImage.getGraphics();
        previewGraphics.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);
        previewGraphics.setRenderingHint(RenderingHints.KEY_ALPHA_INTERPOLATION, RenderingHints.VALUE_ALPHA_INTERPOLATION_QUALITY);
        previewGraphics.setRenderingHint(RenderingHints.KEY_COLOR_RENDERING, RenderingHints.VALUE_COLOR_RENDER_QUALITY);
        previewGraphics.setRenderingHint(RenderingHints.KEY_DITHERING, RenderingHints.VALUE_DITHER_DISABLE);
        previewGraphics.setRenderingHint(RenderingHints.KEY_FRACTIONALMETRICS, RenderingHints.VALUE_FRACTIONALMETRICS_OFF);
        previewGraphics.setRenderingHint(RenderingHints.KEY_INTERPOLATION, RenderingHints.VALUE_INTERPOLATION_NEAREST_NEIGHBOR);
        previewGraphics.setRenderingHint(RenderingHints.KEY_RENDERING, RenderingHints.VALUE_RENDER_QUALITY);
        previewGraphics.setRenderingHint(RenderingHints.KEY_STROKE_CONTROL, RenderingHints.VALUE_STROKE_PURE);
        previewGraphics.setRenderingHint(RenderingHints.KEY_TEXT_ANTIALIASING, RenderingHints.VALUE_TEXT_ANTIALIAS_OFF);
    }

    @Override
    protected void paintComponent(Graphics g) {
        super.paintComponent(g);

        previewGraphics.setColor(new Color(90,115, 75));
        previewGraphics.fillRect(0,0, 200, 40);

        if (isControlNodePreview) {
            Polygon p = new Polygon();
            p.addPoint(85, 10);
            p.addPoint(107, 10);
            p.addPoint(96, 32);
            previewGraphics.setColor(colourNodeControl);
            previewGraphics.fillPolygon(p);

            if (isSelected) {
                Graphics2D g2 = (Graphics2D) previewGraphics.create();
                BasicStroke bs = new BasicStroke(4);
                g2.setStroke(bs);
                Polygon p2 = new Polygon();
                p2.addPoint(85, 10);
                p2.addPoint(107, 10);
                p2.addPoint(96, 32);
                g2.setColor(colourNodeSelected);
                g2.drawPolygon(p2);
                g2.dispose();
            }


        } else {
            previewGraphics.setColor(nodeColour);
            previewGraphics.fillArc(10,10,22,22,0,360);
            previewGraphics.fillArc(165,10,22,22,0,360);

            previewGraphics.setColor(connectionColour);
            drawArrow(previewGraphics, new Point2D.Float(26, 21), new Point2D.Float(172, 21), connectionDual);

            if (isSelected) {
                Graphics2D g2 = (Graphics2D) previewGraphics.create();
                BasicStroke bs = new BasicStroke(4);
                g2.setColor(colourNodeSelected);
                g2.setStroke(bs);
                g2.drawArc(10, 10, 22, 22, 0, 360);
                g2.dispose();
            }
        }
        g.drawImage(previewImage,0, 0, null);
    }

    public static void setPreviewNodeColour(Color newColor) {
        if (!isControlNodePreview) {
            nodeColour = newColor;
            isSelected = false;
            getMapPanel().repaint();
        }
    }
    public static void setPreviewConnectionColour(Color newColor, boolean dualConnection) {
            connectionColour = newColor;
            connectionDual = dualConnection;
            isControlNodePreview = false;
            getMapPanel().repaint();
    }

    public static void setNodeSelected() {
        isSelected = true;
    }

    public static void setPreviewToControlNode(boolean result) {
        isControlNodePreview = result;
        isSelected = false;
    }

    public static void setPreviewControlNodeColour(Color newColor) {
            nodeColour = newColor;
    }

    private void drawArrow(Graphics2D g, Point2D start, Point2D target, boolean dual) {

        int zoomLevel = 1;
        int nodeSize = 30;

        double startX = start.getX();
        double startY = start.getY();
        double targetX = target.getX();
        double targetY = target.getY();


        double vecX = startX - targetX;
        double vecY = startY - targetY;

        double angleRad = Math.atan2(vecY, vecX);

        angleRad = normalizeAngle(angleRad);

        // calculate where to start the line based around the circumference of the node

        double distCos = ((nodeSize * zoomLevel) * 0.25) * Math.cos(angleRad);
        double distSin = ((nodeSize * zoomLevel) * 0.25) * Math.sin(angleRad);

        double lineStartX = startX - distCos;
        double lineStartY = startY - distSin;

        // calculate where to finish the line based around the circumference of the node

        double lineEndX = targetX + distCos;
        double lineEndY = targetY + distSin;

        g.setStroke(new BasicStroke(2));
        g.drawLine((int) lineStartX + 2, (int) lineStartY, (int) lineEndX - 2, (int) lineEndY);

        double arrowLength = (nodeSize * zoomLevel);

        double arrowLeft = normalizeAngle(angleRad + Math.toRadians(-20));
        double arrowLeftX = targetX + Math.cos(arrowLeft) * arrowLength;
        double arrowLeftY = targetY + Math.sin(arrowLeft) * arrowLength;

        double arrowRight = normalizeAngle(angleRad + Math.toRadians(20));
        double arrowRightX = targetX + Math.cos(arrowRight) * arrowLength;
        double arrowRightY = targetY + Math.sin(arrowRight) * arrowLength;

        if (bFilledArrows) {
            Polygon p = new Polygon();
            p.addPoint((int) lineEndX, (int) lineEndY);
            p.addPoint((int) arrowLeftX, (int) arrowLeftY);
            p.addPoint((int) arrowRightX, (int) arrowRightY);
            g.fillPolygon(p);
        } else {
            g.drawLine((int) lineEndX, (int) lineEndY, (int) arrowLeftX, (int) arrowLeftY);
            g.drawLine((int) lineEndX, (int) lineEndY, (int) arrowRightX, (int) arrowRightY);
        }

        if (dual) {
            angleRad = normalizeAngle(angleRad+Math.PI);

            arrowLeft = normalizeAngle(angleRad + Math.toRadians(-20));
            arrowRight = normalizeAngle(angleRad + Math.toRadians(20));

            arrowLeftX = start.getX() + Math.cos(arrowLeft) * arrowLength;
            arrowLeftY = start.getY() + Math.sin(arrowLeft) * arrowLength;
            arrowRightX = start.getX() + Math.cos(arrowRight) * arrowLength;
            arrowRightY = start.getY() + Math.sin(arrowRight) * arrowLength;

            if (bFilledArrows) {
                Polygon p = new Polygon();
                p.addPoint((int) lineStartX, (int) lineStartY);
                p.addPoint((int) arrowLeftX, (int) arrowLeftY);
                p.addPoint((int) arrowRightX, (int) arrowRightY);
                g.fillPolygon(p);
            } else {
                g.drawLine((int) lineStartX, (int) lineStartY, (int) arrowLeftX, (int) arrowLeftY);
                g.drawLine((int) lineStartX, (int) lineStartY, (int) arrowRightX, (int) arrowRightY);
            }
        }
    }
}
