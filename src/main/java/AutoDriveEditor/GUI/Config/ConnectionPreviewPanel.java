package AutoDriveEditor.GUI.Config;

import AutoDriveEditor.Classes.Util_Classes.ColourUtils;
import com.formdev.flatlaf.extras.FlatSVGIcon;

import javax.swing.*;
import java.awt.*;
import java.awt.geom.Point2D;
import java.awt.image.BufferedImage;
import java.util.HashMap;
import java.util.Map;

import static AutoDriveEditor.Classes.Util_Classes.LoggerUtils.LOG;
import static AutoDriveEditor.Classes.Util_Classes.MathUtils.normalizeAngle;
import static AutoDriveEditor.Managers.IconManager.*;
import static AutoDriveEditor.XMLConfig.EditorXML.*;

public class ConnectionPreviewPanel extends JPanel {

    private static boolean isControlNodePreview;
    public static Color nodeColour = colourNodeRegular;
    public static Color connectionColour = colourConnectRegular;

    private final Polygon poly;
    private final int arcSize;
    public static boolean isNodeSelected;
    public static boolean connectionDual;

    private final FlatSVGIcon controlNodeIcon;
    private BufferedImage cnImage;
    private final FlatSVGIcon nodeIcon;
    private final FlatSVGIcon nodeSelectedIcon;
    private final Map<Color, Color> controlNodeColourMap;

    public ConnectionPreviewPanel(Dimension dimension) {
        super();
        if (this.getParent() == null) {
            this.setPreferredSize(dimension);
            this.setSize(dimension);
        }
        this.setLayout(new BorderLayout());
        this.setBackground(new Color(90,115, 75));
        poly = new Polygon();
        arcSize = (int) (this.getHeight()/1.75);
        controlNodeColourMap = new HashMap<>();
        controlNodeIcon = getSVGIcon(CONTROL_NODE_ICON);
        nodeIcon = getSVGIcon(NODE_ICON, this.getHeight() - 10, this.getHeight() - 10);
        nodeSelectedIcon = getSVGIcon(NODE_SELECTION_ICON, this.getHeight() - 10, this.getHeight() - 10);}


@Override
    protected void paintComponent(Graphics g) {
        super.paintComponent(g);

        Graphics2D g2d = (Graphics2D) g;
        g2d.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);
        g2d.setRenderingHint(RenderingHints.KEY_ALPHA_INTERPOLATION, RenderingHints.VALUE_ALPHA_INTERPOLATION_QUALITY);
        g2d.setRenderingHint(RenderingHints.KEY_COLOR_RENDERING, RenderingHints.VALUE_COLOR_RENDER_QUALITY);
        g2d.setRenderingHint(RenderingHints.KEY_DITHERING, RenderingHints.VALUE_DITHER_DISABLE);
        g2d.setRenderingHint(RenderingHints.KEY_INTERPOLATION, RenderingHints.VALUE_INTERPOLATION_NEAREST_NEIGHBOR);
        g2d.setRenderingHint(RenderingHints.KEY_RENDERING, RenderingHints.VALUE_RENDER_QUALITY);
        g2d.setRenderingHint(RenderingHints.KEY_STROKE_CONTROL, RenderingHints.VALUE_STROKE_PURE);

        if (isControlNodePreview) {
            controlNodeColourMap.clear();
            controlNodeColourMap.put(new Color(128,128,128), colourNodeControl);
            controlNodeColourMap.put(new Color(64,64,64), ColourUtils.darken(colourNodeControl, 50));
            controlNodeIcon.setColorFilter(new FlatSVGIcon.ColorFilter().addAll(controlNodeColourMap));
            cnImage = getSVGBufferImage(controlNodeIcon, this.getHeight() - 15, this.getHeight() - 15, null);
            g2d.drawImage(cnImage, (this.getWidth() / 2) - (cnImage.getWidth() / 2), (this.getHeight() / 2) - (cnImage.getHeight() / 2), cnImage.getWidth(), cnImage.getHeight(), null);
        } else {
            g2d.setColor(nodeColour);
            g2d.fillOval(arcSize/2,this.getHeight()/4, arcSize, arcSize);
            g2d.setColor(colourNodeRegular);
            g2d.fillOval(this.getWidth() - ((int)(arcSize * 1.5)),this.getHeight()/4, arcSize, arcSize);
            g2d.setColor(connectionColour);
            drawArrow(g2d, new Point2D.Float((float) arcSize+2, (float) this.getHeight() /2), new Point2D.Float(this.getWidth()-(arcSize+5), (float) this.getHeight()/2), connectionDual);

            if (isNodeSelected) {
                float borderThickness = arcSize * .1f;
                g2d.setStroke(new BasicStroke(borderThickness));
                g2d.setColor(colourNodeSelected);
                g2d.drawOval(arcSize/2,this.getHeight()/4, arcSize, arcSize);
            }
        }
        g2d.dispose();
    }

    public static void setPreviewNodeColour(Color newColor) {
//        if (!isControlNodePreview) {
            nodeColour = newColor;
            isNodeSelected = false;
            //updateNodeScaling();
            //getMapPanel().repaint();
//        }
    }
    public static void setPreviewConnectionColour(Color newColor, boolean dualConnection) {
            connectionColour = newColor;
            connectionDual = dualConnection;
            isControlNodePreview = false;
            //getMapPanel().repaint();
    }

    public static void setNodeSelected(boolean isSelected) {
        isNodeSelected = isSelected;
    }

    public static void setPreviewToControlNode(boolean result) {
        isControlNodePreview = result;
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
