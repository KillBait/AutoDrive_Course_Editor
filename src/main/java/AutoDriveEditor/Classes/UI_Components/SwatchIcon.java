package AutoDriveEditor.Classes.UI_Components;

import javax.swing.*;
import java.awt.*;

@SuppressWarnings("unused")
public class SwatchIcon implements Icon {
    private int dashSize = 1;
    private int gapSize = 3;
    private Color color;

    public SwatchIcon(Color color) {
        this.color = color;
    }

    @Override
    public void paintIcon(Component c, Graphics g, int x, int y) {
        Graphics2D g2d = (Graphics2D) g.create();

        // set the renderHints for better quality
        g2d.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);
        g2d.setRenderingHint(RenderingHints.KEY_STROKE_CONTROL, RenderingHints.VALUE_STROKE_PURE);

        int border = dashSize + gapSize;

        int rectWidth = c.getWidth() - (border * 2);
        int rectHeight = c.getHeight() - (border * 2);

        // Clear the background to ensure transparency
        c.setBackground(new Color(255, 255, 255, 0));

        // Fill the rectangle with the specified color
        g2d.setColor(color);
        g2d.fillRoundRect(border, border, rectWidth, rectHeight, 5, 5);

        // Draw the border around the rectangle
        if (c instanceof JToggleButton) {
            if (((AbstractButton) c).isSelected()) {
                g2d.setColor(new Color(128,128,128));
                g2d.setStroke(new BasicStroke(dashSize, BasicStroke.CAP_BUTT, BasicStroke.JOIN_MITER, 10.0f, new float[]{5, 4}, 0.0f));
                g2d.drawRoundRect(dashSize, dashSize, c.getWidth() - (dashSize*2), c.getHeight() - (dashSize*2), 3, 3);
            }
        }

        g2d.dispose();
    }

    @Override
    public int getIconWidth() {
        return 0;
    }

    @Override
    public int getIconHeight() {
        return 0;
    }

    public Color getColor() {
        return color;
    }

    public void setColor(Color color) {
        this.color = color;
    }

    public void setDashSize(int size) {
        this.dashSize = size;
    }

    public void setGapSize(int size) {
        this.gapSize = size;
    }
}
