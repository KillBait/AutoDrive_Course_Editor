package AutoDriveEditor.Classes.UI_Components;

import javax.swing.*;
import java.awt.*;

@SuppressWarnings("unused")
public class ColorIcon implements Icon {
    private Color color;
    private int width = 20;
    private int height = 12;

    public ColorIcon(Color color) {
        this.color = color;
    }

    public ColorIcon(Color color, int width, int height) {
        this.color = color;
        this.width = width;
        this.height = height;
    }

    @Override
    public void paintIcon(Component c, Graphics g, int x, int y) {
        Graphics2D g2d = (Graphics2D) g.create();

        // set the renderHints for better quality
        g2d.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);
        g2d.setRenderingHint(RenderingHints.KEY_STROKE_CONTROL, RenderingHints.VALUE_STROKE_PURE);

        // Fill the rectangle with the specified color
        g2d.setColor(color);
        g2d.fillRoundRect(x, (c.getHeight() - height) / 2, width, height, 5, 5);

        // Draw the border around the rectangle
        g2d.setColor(Color.BLACK);
        g2d.drawRoundRect(x, (c.getHeight() - height) / 2, width, height, 5, 5);
        g2d.dispose();
    }

    public void setWidth(int width) {
        this.width = width;
    }

    public void setHeight(int height) {
        this.height = height;
    }

    public void setColour(Color color) {
        this.color = color;
    }

    @Override
    public int getIconWidth() {
        return this.width;
    }

    @Override
    public int getIconHeight() { return this.height; }
}
