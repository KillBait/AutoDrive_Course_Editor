package AutoDriveEditor.Classes.UI_Components;

import javax.swing.*;
import javax.swing.border.AbstractBorder;
import java.awt.*;

/**
 * A custom JLabel with a top-rounded border and a fill color.
 * by KillBait!
 */

public class TopRoundedLabel extends JLabel {

    private final int radius; // The radius of the rounded corners
    private Color fillColor; // The fill color of the label

    /**
     * Constructs a TopRoundedLabel with the specified text, radius, and fill color.
     *
     * @param text      the text to be displayed by the label
     * @param radius    the radius of the rounded corners
     * @param fillColor the fill color of the label
     */
    public TopRoundedLabel(String text, int radius, Color fillColor) {
        super(text);
        this.radius = radius;
        if (fillColor == null) {
            // Flatlaf only - use the default background fill color of the JScrollPane
            this.fillColor = (Color) UIManager.getLookAndFeel().getDefaults().get("Label.background");
        } else {
            // use the specified fill color
            this.fillColor = fillColor;
        }

        // Add a PropertyChangeListener to listen for look and feel changes
        UIManager.addPropertyChangeListener(evt -> {
            if ("lookAndFeel".equals(evt.getPropertyName())) {
                updateFillColor();
            }
        });

        setBorder(new TopRoundedBorder(radius)); // Set the custom border
        setVerticalAlignment(SwingConstants.CENTER); // Center the text vertically
        setHorizontalAlignment(SwingConstants.CENTER); // Center the text horizontally
        setVerticalTextPosition(SwingConstants.CENTER); // Ensure the text is vertically centered
    }

    public Color getDefaultFillColor() {
        return (Color) UIManager.get("ScrollPane.background");
    }

    private void updateFillColor() {
        this.fillColor = getDefaultFillColor();
    }

    /**
     * Paints the component with the specified graphics context.
     *
     * @param g the graphics context to be used for painting
     */
    @Override
    protected void paintComponent(Graphics g) {

        // createSetting a 2D graphics context
        Graphics2D g2d = (Graphics2D) g;
        // set the rendering hint to antialiasing
        g2d.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);

        // Get the component's width and height and the arc's width and height
        int width = getWidth();
        int height = getHeight();
        int arcWidth = radius * 2;
        int arcHeight = radius * 2;

        // Set the fill color
        g2d.setColor(this.fillColor);

        // Fill the top-left arc
        g2d.fillArc(0, 0, arcWidth, arcHeight, 90, 90);

        // Fill the top-right arc
        g2d.fillArc(width - arcWidth - 1, 0, arcWidth, arcHeight, 0, 90);

        // Fill the top rectangle between the arcs
        g2d.fillRect(radius, 0, width - arcWidth, radius);

        // Fill the middle rectangle below the arcs
        g2d.fillRect(0, radius, width, height - radius);

        // Now, paint the label's text and other components on top of the background
        super.paintComponent(g);

    }

    /**
     * A custom border with top-rounded corners.
     */
    static class TopRoundedBorder extends AbstractBorder {
        private final int radius; // The radius of the rounded corners

        /**
         * Constructs a TopRoundedBorder with the specified radius.
         *
         * @param radius the radius of the rounded corners
         */
        public TopRoundedBorder(int radius) {
            // Set the radius of the border
            this.radius = radius;
        }

        /**
         * Paints the border with the specified graphics context.
         *
         * @param c      the component for which this border is being painted
         * @param g      the graphics context to be used for painting
         * @param x      the x position of the border
         * @param y      the y position of the border
         * @param width  the width of the border
         * @param height the height of the border
         */
        @Override
        public void paintBorder(Component c, Graphics g, int x, int y, int width, int height) {

            // Create a 2D graphics context
            Graphics2D g2d = (Graphics2D) g;
            // Set the rendering hint to antialiasing
            g2d.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);

            // Get the arc's width and height
            int arcWidth = radius * 2;
            int arcHeight = radius * 2;

            // Draw the border
            g2d.setColor(Color.LIGHT_GRAY);
            g2d.setStroke(new BasicStroke(1.5f));
            g2d.drawArc(x, y, arcWidth, arcHeight, 90, 90); // Top-left arc
            g2d.drawArc(x + width - arcWidth - 1, y, arcWidth, arcHeight, 0, 90); // Top-right arc
            g2d.drawLine(x + radius, y, x + width - radius - 1, y); // Top edge
            g2d.drawLine(x, y + radius, x, y + height - 1); // Left edge
            g2d.drawLine(x + width - 1, y + radius, x + width - 1, y + height - 1); // Right edge
        }

        /**
         * Returns the insets of the border.
         *
         * @param c the component for which this border insets value applies
         * @return the insets of the border
         */
        @Override
        public Insets getBorderInsets(Component c) {
            // Return the insets for the border
            return new Insets(0, 0, 0, 0);
        }

        /**
         * Reinitialize the insets parameter with this border's current insets.
         *
         * @param c the component for which this border insets value applies
         * @param insets the object to be reinitialized
         * @return the insets of the border
         */
        @Override
        public Insets getBorderInsets(Component c, Insets insets) {
            // Return the insets for the border
            insets.top = radius / 2;
            insets.left = insets.right = 10;
            insets.bottom = 3;
            return insets;
        }

        public static Color getColor() {
            return Color.LIGHT_GRAY;
        }
    }
}
