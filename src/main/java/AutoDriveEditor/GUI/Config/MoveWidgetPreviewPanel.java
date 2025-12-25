package AutoDriveEditor.GUI.Config;

import javax.swing.*;
import java.awt.*;
import java.awt.event.MouseAdapter;
import java.awt.event.MouseEvent;
import java.awt.event.MouseMotionListener;
import java.awt.geom.Point2D;
import java.awt.image.BufferedImage;

import static AutoDriveEditor.AutoDriveEditor.getMapPanel;
import static AutoDriveEditor.AutoDriveEditor.widgetManager;
import static AutoDriveEditor.Classes.Util_Classes.ImageUtils.getNewBufferImage;
import static AutoDriveEditor.GUI.MapPanel.nodeSizeScaledHalf;
import static AutoDriveEditor.XMLConfig.EditorXML.*;

public class MoveWidgetPreviewPanel extends JPanel {

    private BufferedImage previewImage;
    private Graphics2D previewGraphics;

    private JPanel previewPanel;
    private int previewNodeSize = 28;
    private boolean freeMoveSelected = false;
    private Rectangle previewFreeMoveRect = new Rectangle();
    private Rectangle previewxAxisRect = new Rectangle();
    private Polygon previewxAxisArrow = new Polygon();
    private Rectangle previewyAxisRect = new Rectangle();
    private Polygon previewyAxisArrow = new Polygon();

    //private Rectangle freeMoveArea = new Rectangle();




    public MoveWidgetPreviewPanel() {
        //this.setLayout();
        setBorder(BorderFactory.createEmptyBorder());
        previewPanel = this;
        this.setPreferredSize(new Dimension(200, 200));
        this.setMinimumSize(new Dimension(200, 200));
        this.setMaximumSize(new Dimension(200,200));
        previewImage = getNewBufferImage(this.getPreferredSize().width, this.getPreferredSize().height, Transparency.BITMASK);
        previewGraphics = (Graphics2D) previewImage.getGraphics();
        this.addMouseListener(new MouseAdapter() {
            @Override
            public void mousePressed(MouseEvent e) {
                if (previewFreeMoveRect.contains(e.getPoint())) {
                    freeMoveSelected = true;
                }
            }

            @Override
            public void mouseReleased(MouseEvent e) {
                freeMoveSelected = false;
                getMapPanel().repaint();
//                getMoveWidget().updateVisibility();
                widgetManager.updateAllWidgets();
            }
        });
        this.addMouseMotionListener(new MouseMotionListener() {
            @Override
            public void mouseDragged(MouseEvent evt) {
                if (freeMoveSelected && freeMovePosition == FREEMOVE_POSITION.MANUAL) {
                    // Limit the pointer x and y to the panel size
                    int x = (int) Math.max((previewFreeMoveRect.getWidth()/2), Math.min(evt.getX(), getWidth()-(previewFreeMoveRect.getWidth()/2)));
                    int y = (int) Math.max((previewFreeMoveRect.getHeight()/2), Math.min(evt.getY(), getWidth()-(previewFreeMoveRect.getHeight()/2)));
                    freeMoveOffsetX = (int) (((double) previewPanel.getWidth() / 2) + (previewFreeMoveRect.getWidth()/2) - x);
                    freeMoveOffsetY = (int) (((double) previewPanel.getHeight() / 2) + (previewFreeMoveRect.getHeight()/2) - y);
                    previewPanel.repaint();
                }
            }

            //not used, but must override
            @Override
            public void mouseMoved(MouseEvent e) {}
        });
        //calcFreeMove();
        //calcAxis();
    }

    private void calcFreeMove() {
        if (freeMovePosition == FREEMOVE_POSITION.MANUAL) {
            previewFreeMoveRect.setLocation((this.getWidth() / 2) - freeMoveOffsetX, (this.getHeight() / 2) - freeMoveOffsetY);
        } else if (freeMovePosition == FREEMOVE_POSITION.CENTER) {
            previewFreeMoveRect.setLocation((int) (((double) this.getWidth() / 2) - (previewFreeMoveRect.getWidth() / 2)), (int) (((double) this.getWidth() / 2) - (previewFreeMoveRect.getHeight() / 2)));
        }
        switch (freeMoveSize) {
            case SMALL:
                previewFreeMoveRect.setSize( freeMoveDefaultSmall, freeMoveDefaultSmall);
                break;
            case MEDIUM:
                previewFreeMoveRect.setSize( freeMoveDefaultMedium, freeMoveDefaultMedium);
                break;
            case LARGE:
                previewFreeMoveRect.setSize( freeMoveDefaultLarge, freeMoveDefaultLarge);
                break;
        }
        previewPanel.repaint();
    }

    private void calcAxis() {
        Point2D centre = new Point(this.getWidth() / 2, this.getHeight() / 2);

        // calculate the x-axis location and length
        previewxAxisArrow.reset();
        if (xAxisDirection == X_DIRECTION.RIGHT) {

            previewxAxisArrow.addPoint((int) (centre.getX() + ((double) previewNodeSize / 2)), (int) (centre.getY() + ((double) axisWidth / 2)));
            previewxAxisArrow.addPoint((int) (centre.getX() + (((double) previewNodeSize / 2) + axisLength)), (int) (centre.getY() + ((double) axisWidth / 2)));

            Point xArrowPoint = new Point((int) ((int) centre.getX() + ((double) previewNodeSize / 2) + axisLength), (int) centre.getY());
            previewxAxisArrow.addPoint(xArrowPoint.x, xArrowPoint.y - arrowWidth);
            previewxAxisArrow.addPoint(xArrowPoint.x + arrowLength, xArrowPoint.y);
            previewxAxisArrow.addPoint(xArrowPoint.x, xArrowPoint.y + arrowWidth);

            previewxAxisArrow.addPoint((int) (centre.getX() + (((double) previewNodeSize / 2) + axisLength)), (int) (centre.getY() - ((double) axisWidth / 2)));
            previewxAxisArrow.addPoint((int) (centre.getX() + ((double) previewNodeSize / 2)), (int) (centre.getY() - ((double) axisWidth / 2)));

        } else {

            previewxAxisArrow.addPoint((int) (centre.getX() - ((double) previewNodeSize / 2)), (int) (centre.getY() - ((double) axisWidth / 2)));
            previewxAxisArrow.addPoint((int) (centre.getX() - (((double) previewNodeSize / 2) + axisLength)), (int) (centre.getY() - ((double) axisWidth / 2)));

            Point xArrowPoint = new Point((int) (centre.getX() - (((double) previewNodeSize / 2) + axisLength)), (int) centre.getY());
            previewxAxisArrow.addPoint(xArrowPoint.x, xArrowPoint.y + arrowWidth);
            previewxAxisArrow.addPoint(xArrowPoint.x - arrowLength, xArrowPoint.y);
            previewxAxisArrow.addPoint(xArrowPoint.x, xArrowPoint.y - arrowWidth);

            previewxAxisArrow.addPoint((int) (centre.getX() - (((double) previewNodeSize / 2) + axisLength)), (int) (centre.getY() + ((double) axisWidth / 2)));
            previewxAxisArrow.addPoint((int) (centre.getX() - ((double) previewNodeSize / 2)), (int) (centre.getY() + ((double) axisWidth / 2)));

        }

        // calculate the y-axis location and length
        previewyAxisArrow.reset();
        if (yAxisDirection == Y_DIRECTION.UP) {

            previewyAxisArrow.addPoint((int) (centre.getX() - ((double) axisWidth / 2)), (int) (centre.getY() - ((double) previewNodeSize / 2)));
            previewyAxisArrow.addPoint((int) (centre.getX() - ((double) axisWidth / 2)), (int) (centre.getY() - (((double) previewNodeSize / 2) + axisLength)));

            Point yArrowPoint = new Point((int) centre.getX(), (int) (centre.getY() - (((double) previewNodeSize / 2) + axisLength)));
            previewyAxisArrow.addPoint(yArrowPoint.x - arrowWidth, yArrowPoint.y);
            previewyAxisArrow.addPoint(yArrowPoint.x, yArrowPoint.y - arrowLength);
            previewyAxisArrow.addPoint(yArrowPoint.x + arrowWidth, yArrowPoint.y);

            previewyAxisArrow.addPoint((int) (centre.getX() + ((double) axisWidth / 2)), (int) (centre.getY() - (((double) previewNodeSize / 2) + axisLength)));
            previewyAxisArrow.addPoint((int) (centre.getX() + ((double) axisWidth / 2)), (int) (centre.getY() - ((double) previewNodeSize / 2)));

        } else {

            previewyAxisArrow.addPoint((int) (centre.getX() - ((double) axisWidth / 2)), (int) (centre.getY() + ((double) previewNodeSize / 2)));
            previewyAxisArrow.addPoint((int) (centre.getX() - ((double) axisWidth / 2)), (int) (centre.getY() + (((double) previewNodeSize / 2) + axisLength)));

            Point yArrowPoint = new Point((int) centre.getX(), (int) ((int) centre.getY() + ((double) previewNodeSize / 2) + axisLength));
            previewyAxisArrow.addPoint(yArrowPoint.x - arrowWidth, yArrowPoint.y);
            previewyAxisArrow.addPoint(yArrowPoint.x, yArrowPoint.y + arrowLength);
            previewyAxisArrow.addPoint(yArrowPoint.x + arrowWidth, yArrowPoint.y);

            previewyAxisArrow.addPoint((int) (centre.getX() + ((double) axisWidth / 2)), (int) (centre.getY() + (((double) previewNodeSize / 2) + axisLength)));
            previewyAxisArrow.addPoint((int) (centre.getX() + ((double) axisWidth / 2)), (int) (centre.getY() + ((double) previewNodeSize / 2)));

        }
    }

    private void setRenderHint(Graphics2D g) {
        g.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);
        g.setRenderingHint(RenderingHints.KEY_ALPHA_INTERPOLATION, RenderingHints.VALUE_ALPHA_INTERPOLATION_QUALITY);
        g.setRenderingHint(RenderingHints.KEY_COLOR_RENDERING, RenderingHints.VALUE_COLOR_RENDER_QUALITY);
        g.setRenderingHint(RenderingHints.KEY_DITHERING, RenderingHints.VALUE_DITHER_DISABLE);
        g.setRenderingHint(RenderingHints.KEY_FRACTIONALMETRICS, RenderingHints.VALUE_FRACTIONALMETRICS_OFF);
        g.setRenderingHint(RenderingHints.KEY_INTERPOLATION, RenderingHints.VALUE_INTERPOLATION_NEAREST_NEIGHBOR);
        g.setRenderingHint(RenderingHints.KEY_RENDERING, RenderingHints.VALUE_RENDER_QUALITY);
        g.setRenderingHint(RenderingHints.KEY_STROKE_CONTROL, RenderingHints.VALUE_STROKE_PURE);
        g.setRenderingHint(RenderingHints.KEY_TEXT_ANTIALIASING, RenderingHints.VALUE_TEXT_ANTIALIAS_OFF);
    }

    @Override
    protected void paintComponent(Graphics g) {

        calcAxis();

        Graphics2D g2d = (Graphics2D) g.create();
        previewGraphics.setColor(new Color(90, 115, 75));
        previewGraphics.fillRect(0, 0, this.getWidth(), this.getHeight());
        g2d.drawImage(previewImage,0, 0, null);
        drawGrid(g2d);
        setRenderHint(g2d);
        calcFreeMove();

        // draw the reference node at the centre

        g2d.setColor(colourNodeRegular);
        int pos = (int) ((double) this.getWidth() / 2 - ((double) previewNodeSize / 2));
        g2d.fillOval(pos, pos, previewNodeSize , previewNodeSize);

        //
        // Draw the x-axis
        //

        g2d.setColor(xAxisColor);
        g2d.fillPolygon(previewxAxisArrow);

        //
        // Draw the y-axis
        //

        g2d.setColor(yAxisColor);
        g2d.fillPolygon(previewyAxisArrow);

        //
        // draw the free move area
        //

        g2d.setColor(freeMoveColor);
        // If selected shape is square, draw the rectangle outline
        if (freeMoveType == FREEMOVE_TYPE.SQUARE) {
            g2d.drawRect((int) previewFreeMoveRect.getX(), (int) previewFreeMoveRect.getY(), (int) previewFreeMoveRect.getWidth(), (int) previewFreeMoveRect.getHeight());
        }

        // Modify the rounded rectangle arc depending on the selected shape
        // If square selected, do not round off the edges
        // If round selected, set the rounding to the size of the free move rectangle, appearing as circular
        int arc = (freeMoveType == FREEMOVE_TYPE.ROUND) ? (int) previewFreeMoveRect.getWidth() : 0;
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
            g2d.fillRoundRect((int) previewFreeMoveRect.getX(), (int) previewFreeMoveRect.getY(), (int) previewFreeMoveRect.getWidth(), (int) previewFreeMoveRect.getHeight(), arc, arc);
        }
        // Draw the outline of the rectangle
        g2d.setPaint(freeMoveColor);
        g2d.drawRoundRect((int) previewFreeMoveRect.getX(), (int) previewFreeMoveRect.getY(), (int) previewFreeMoveRect.getWidth(), (int) previewFreeMoveRect.getHeight(), arc, arc);
    }

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

    private void drawGrid(Graphics g) {
        Graphics2D g2d = (Graphics2D) g.create();
        g2d.setColor(new Color(0, 0, 0, 50));
        for (int i = 0; i < 200; i+=50) {
            g2d.drawLine(i, 0, i, 200);
        }
        for (int i = 0; i < 200; i+=50) {
            g2d.drawLine(0, i, 200, i);
            i++;
        }
        g2d.dispose();
    }
}
