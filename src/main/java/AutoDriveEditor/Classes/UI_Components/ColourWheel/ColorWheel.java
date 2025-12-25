package AutoDriveEditor.Classes.UI_Components.ColourWheel;

import AutoDriveEditor.Classes.UI_Components.PropertyChangeNumberFilter;
import AutoDriveEditor.Classes.UI_Components.SwatchIcon;
import com.formdev.flatlaf.extras.FlatSVGIcon;

import javax.swing.*;
import javax.swing.text.PlainDocument;
import java.awt.*;
import java.awt.event.MouseAdapter;
import java.awt.event.MouseEvent;
import java.awt.event.MouseListener;
import java.awt.event.MouseMotionListener;
import java.awt.geom.Ellipse2D;
import java.awt.image.BufferedImage;
import java.beans.PropertyChangeEvent;
import java.util.ArrayList;
import java.util.Collections;
import java.util.EventListener;
import java.util.EventObject;

import static AutoDriveEditor.Classes.Util_Classes.ImageUtils.getNewBufferImage;
import static AutoDriveEditor.Classes.Util_Classes.LoggerUtils.LOG;
import static AutoDriveEditor.Managers.IconManager.*;

/**
 * A custom JPanel that displays a color wheel for selecting colors.
 * Includes a color ring for hue selection and a triangle for saturation and brightness selection.
 */

//
// HSV functionality is currently disabled due to bugs - RGB only for now
//


@SuppressWarnings("unused")
public class ColorWheel extends JPanel {

    private final ColourRingPicker cp;
    private final java.util.List<ColorPropertyChangeListener> listeners = new ArrayList<>();
    private final ButtonGroup swatchGroup;

    private final JPanel swatchPanel;
    private final JPanel alphaPanel;
    private final JPanel inputPanel;

    private final JTextField redField;
    private final JTextField greenField;
    private final JTextField blueField;
    //private final JTextField hueField;
    //private final JTextField saturationField;
    //private final JTextField brightnessField;
    private final JSlider alphaSlider;

    private int alpha = 255;

    private boolean isTextFieldUpdating = false;

    /**
     * Constructor
     */
    public ColorWheel() {
        super();
        // Set layout to BorderLayout
        this.setLayout(new BorderLayout());
        // Create a new ColourRingPicker
        cp = new ColourRingPicker();
        // Add the ColorPropertyChangeListener so swatch buttons can get colour updates
        addColorPropertyChangeListener(evt -> {
            isTextFieldUpdating = true;
            float[] hsbValues = evt.getHSBValues();
            //Color newColor = evt.getColor();
            int rgb = Color.HSBtoRGB(hsbValues[0], hsbValues[1], hsbValues[2]);
            Color newColor = new Color((rgb >> 16) & 0xFF, (rgb >> 8) & 0xFF, rgb & 0xFF, alpha);
            updateTextFields(newColor, hsbValues);
            updateSwatchIcons(newColor);
            isTextFieldUpdating = false;
        });

        // Add the ColourRingPicker to the central panel
        this.add(cp, BorderLayout.CENTER);

        // Create the RGB/HSB input panel
        inputPanel = new JPanel(new GridBagLayout());
        GridBagConstraints gbc = new GridBagConstraints();
        gbc.gridwidth = 1;
        gbc.fill = GridBagConstraints.HORIZONTAL;
        FlatSVGIcon percentage = getSVGIcon(TEXT_PERCENT_ICON);
        redField = makeRGBPair(inputPanel, gbc, "R", 0, 255, cp.getRed(), 1);
        greenField = makeRGBPair(inputPanel, gbc, "G", 0, 255, cp.getGreen(), 2);
        blueField = makeRGBPair(inputPanel, gbc, "B", 0, 255, cp.getBlue(), 3);
        //hueField = makeHSBPair(inputPanel, gbc, "H", 0, 360, getHueAngle(cp.getHue()), 4);
        //saturationField = makeHSBPair(inputPanel, gbc, "S", 0, 100, cp.getSaturationPercentage(), 5);
        //brightnessField = makeHSBPair(inputPanel, gbc, "B", 0, 100, cp.getBrightnessPercentage(), 6);
        // Add the input panel to the east side of the main panel
        add(inputPanel, BorderLayout.EAST);

        //Create the Alpha slider panel
        alphaPanel = new JPanel();
        alphaPanel.setLayout(new BoxLayout(alphaPanel, BoxLayout.LINE_AXIS));
        alphaSlider = new JSlider(SwingConstants.HORIZONTAL);
        alphaSlider.setMinimum(0);
        alphaSlider.setMaximum(255);
        alphaSlider.setValue(255);
        alphaSlider.addChangeListener(e -> {
            JSlider source = (JSlider) e.getSource();
            if (source.getValueIsAdjusting()) {
                alpha = source.getValue();
                notifyColorPropertyChange();
            }
        });
        JLabel sliderLabel = new JLabel("Alpha:");
        sliderLabel.setLabelFor(alphaSlider);


        alphaPanel.add(sliderLabel);
        alphaPanel.add(alphaSlider);
        // Add the alpha panel to the south side of the main panel
        add(alphaPanel, BorderLayout.SOUTH);

        // Create the swatch panel
        swatchGroup = new ButtonGroup() {
            @Override
            public void setSelected(ButtonModel m, boolean b) {
                if (b && m != null && !m.isSelected()) {
                    super.setSelected(m, true);
                } else {
                    clearSelection();
                }
            }
        };

        swatchPanel = new JPanel();
        Color[] colors = {Color.RED, Color.GREEN, Color.BLUE, Color.YELLOW, Color.CYAN, Color.MAGENTA, Color.ORANGE, Color.PINK, Color.LIGHT_GRAY, Color.DARK_GRAY};
        for (int i = 0; i < 10; i++) {
            SwatchIcon icon = new SwatchIcon(colors[i]);
            JToggleButton button = new JToggleButton(icon);
            button.setOpaque(false);
            button.setBorder(BorderFactory.createEmptyBorder());
            button.setPreferredSize(new Dimension(20, 20));
            int finalI = i;
            button.addMouseListener(new MouseAdapter() {
                @Override
                public void mouseClicked(MouseEvent e) {
                    // check if right mouse clicked to reset to default colour
                    if (e.getButton() == MouseEvent.BUTTON3) {
                        icon.setColor(colors[finalI]);
                        button.repaint();
                    }
                    // notify color picker of colour reset
                    alphaSlider.setValue(icon.getColor().getAlpha());
                    cp.setRGB(icon.getColor());
                    notifyColorPropertyChange();
                }
            });
            button.addActionListener(e -> {
                alphaSlider.setValue(icon.getColor().getAlpha());
                cp.setRGB(icon.getColor());
                notifyColorPropertyChange();
            });
            swatchPanel.add(button);
            swatchGroup.add(button);
        }
        add(swatchPanel, BorderLayout.NORTH);
        this.setMinimumSize(getPreferredSize());
    }

    /**
     * Updates all the R, G, B, H, S, B textbox components with the new values.
     *
     * @param newColor the new color
     * @param hsbValues the HSB values
     */
    private void updateTextFields(Color newColor, float[] hsbValues) {
        // Update the text fields
        for (Component component : getComponents()) {
            if (component instanceof JPanel) {
                for (Component subComponent : ((JPanel) component).getComponents()) {
                    if (subComponent instanceof JTextField) {
                        redField.setText(String.valueOf(newColor.getRed()));
                        greenField.setText(String.valueOf(newColor.getGreen()));
                        blueField.setText(String.valueOf(newColor.getBlue()));

                        //TODO fix bugs in HSB values

                        //hueField.setText(String.valueOf(getHueAngle(hsbValues[0])));
                        //saturationField.setText(String.valueOf((int) (hsbValues[1]*100)));
                        //brightnessField.setText(String.valueOf((int) (hsbValues[2]*100)));
                    }
                }
            }
        }
    }

    private void updateSwatchIcons(Color newColour) {
        for (AbstractButton button : Collections.list(swatchGroup.getElements())) {
            if (button.isSelected() && button.getIcon() instanceof SwatchIcon) {
                SwatchIcon icon = (SwatchIcon) button.getIcon();
                icon.setColor(newColour);
                button.repaint();
                break;
            }
        }
    }

    /**
     * Creates a label and integer text field pair for the input panel.
     *
     * @param p the panel to add the components to
     * @param c the GridBagConstraints
     * @param labelText the label text
     * @param num the initial value
     * @param gridY the grid y position
     * @return the created JTextField
     */
    private JTextField makeRGBPair(JPanel p, GridBagConstraints c, String labelText, int minValue, int maxValue, int num, int gridY) {
        JLabel l = new JLabel(labelText);
        c.insets = new Insets(0, 10, 2, 0);
        c.ipadx = 0;
        c.gridx = 0;
        c.gridy = gridY;
        p.add(l, c);

        JTextField textField = new JTextField(String.valueOf(num));
        textField.setPreferredSize(new Dimension(40, textField.getPreferredSize().height));
        textField.setMinimumSize(new Dimension(40, textField.getPreferredSize().height));
        PropertyChangeNumberFilter filter = new PropertyChangeNumberFilter(minValue, maxValue, false, 0, false);
        filter.addPropertyChangeListener(evt -> {
            if (!isTextFieldUpdating && evt.getPropertyName().equals("valid")) {
                if (!textField.getText().isEmpty()) {
                    switch (labelText) {
                        case "R":
                            cp.setRed(Integer.parseInt(textField.getText()));
                            break;
                        case "G":
                            cp.setGreen(Integer.parseInt(textField.getText()));
                            break;
                        case "B":
                            cp.setBlue(Integer.parseInt(textField.getText()));
                            break;
                    }
                }
            } else {
                handleTextFieldError(evt, textField);
            }

        });

        PlainDocument doc = (PlainDocument) textField.getDocument();
        doc.setDocumentFilter(filter);

        c.insets = new Insets(0, 5, 5, 0);
        c.ipadx = 5;
        c.gridx = 1;
        c.gridy = gridY;
        p.add(textField, c);

        return textField;
    }

    /**
     * Creates a label and float text field pair for the input panel.
     *
     * @param p the panel to add the components to
     * @param c the GridBagConstraints
     * @param labelText the label text
     * @param num the initial value
     * @param gridY the grid y position
     * @return the created JTextField
     */
    private JTextField makeHSBPair(JPanel p, GridBagConstraints c, String labelText, int minValue, int maxValue, float num, int gridY) {
        JLabel l = new JLabel(labelText);
        c.insets = new Insets(0, 10, 2, 0);
        c.ipadx = 0;
        c.gridx = 0;
        c.gridy = gridY;
        p.add(l, c);

        JTextField textField = new JTextField(String.valueOf(num));
        textField.setPreferredSize(new Dimension(35, textField.getPreferredSize().height));
        textField.setMinimumSize(new Dimension(35, textField.getPreferredSize().height));

        PropertyChangeNumberFilter filter = new PropertyChangeNumberFilter(minValue, maxValue, false, 0, false);
        filter.addPropertyChangeListener(evt -> {
            if (!isTextFieldUpdating && evt.getPropertyName().equals("valid")) {
                if (!textField.getText().isEmpty()) {
                    switch (labelText) {
                        case "H":
                            cp.setHue(Float.parseFloat(textField.getText())/360);
                            break;
                        case "S":
                            cp.setSaturation(Float.parseFloat(textField.getText())/100);
                            break;
                        case "B":
                            cp.setBrightness(Float.parseFloat(textField.getText())/100);
                            break;
                    }
                }
                updateSwatchIcons(new Color(cp.getRed(), cp.getGreen(), cp.getBlue()));
            } else {
                handleTextFieldError(evt, textField);
            }
        });
        PlainDocument doc = (PlainDocument) textField.getDocument();
        doc.setDocumentFilter(filter);

        c.insets = new Insets(0, 5, 2, 0);
        c.ipadx = 5;
        c.gridx = 1;
        c.gridy = gridY;
        p.add(textField, c);

        return textField;
    }

    private void addListener(JTextField textField) {
        textField.addPropertyChangeListener(evt -> {

        });
    }

    /**
     * Handles text field errors by displaying an error outline and logging the error.
     *
     * @param evt the property change event
     * @param textField the text field to updateVisibility
     */
    private void handleTextFieldError(PropertyChangeEvent evt, JTextField textField) {
//        if ("errorText".equals(evt.getPropertyName())) {
            String errorText = (String) evt.getNewValue();
            if (!errorText.isEmpty()) {
                LOG.info("Error: {}", errorText);
                textField.putClientProperty("JComponent.outline", "error");
                Timer timer = new Timer(1000, e -> {
                    textField.putClientProperty("JComponent.outline", "none");
                    ((Timer) e.getSource()).stop();
                });
                timer.setRepeats(false);
                timer.start();
            } else {
                textField.putClientProperty("JComponent.outline", "none");
            }
//        }
    }


    //
    // Property Change Listeners
    //

    /**
     * Adds a ColorPropertyChangeListener to the list of listeners.
     *
     * @param listener the listener to add
     */
    public void addColorPropertyChangeListener(ColorPropertyChangeListener listener) {
        listeners.add(listener);
    }

    /**
     * Removes a ColorPropertyChangeListener from the list of listeners.
     *
     * @param listener the listener to removeOriginalNodes
     */
    public void removeColorPropertyChangeListener(ColorPropertyChangeListener listener) {
        listeners.remove(listener);
    }

    /**
     * Notifies all registered listeners about a color property change.
     */
    private void notifyColorPropertyChange() {
        float[] hsbValues = {cp.getHue(), cp.getSaturation(), cp.getBrightness()};
        int rgb = Color.HSBtoRGB(cp.getHue(), cp.getSaturation(), cp.getBrightness());
        Color newColour = new Color((rgb >> 16) & 0xFF, (rgb >> 8) & 0xFF, rgb & 0xFF, alpha);

        ColorPropertyChangeEvent event = new ColorPropertyChangeEvent(this, newColour, hsbValues);
        for (ColorPropertyChangeListener listener : listeners) {
            listener.colorPropertyChange(event);
        }
    }

    /**
     * Sets the visibility of the swatch panel.
     *
     * @param visible true to make the swatch panel visible, false to hide it
     */
    public void setSwatchVisible(boolean visible) {
        this.swatchPanel.setVisible(visible);
    }

    /**
     * Sets the visibility of the alpha slider.
     *
     * @param visible true to make the alpha slider visible, false to hide it
     */
    public void setAlphaVisible(boolean visible) {
        this.alphaPanel.setVisible(visible);
    }

    /**
     * Sets the visibility of the input panel.
     *
     * @param visible true to make the input panel visible, false to hide it
     */
    public void setInputVisible(boolean visible) {
        this.inputPanel.setVisible(visible);
    }

    public void swatchGroupClearSelection() {
        swatchGroup.clearSelection();
    }

// Wrapper functions for the ColourRingPicker

    /**
     * Gets the RGB value of the selected color.
     *
     * @return the RGB value
     */
    public int getRGB() { return cp.getRGB(); }

    /**
     * Gets the red component of the selected color.
     *
     * @return the red component
     */
    public int getRed() { return cp.getRed(); }

    /**
     * Gets the green component of the selected color.
     *
     * @return the green component
     */
    public int getGreen() { return cp.getGreen(); }

    /**
     * Gets the blue component of the selected color.
     *
     * @return the blue component
     */
    public int getBlue() { return cp.getBlue(); }

    /**
     * Gets the hue of the selected color.
     *
     * @return the hue
     */
    public float getHue() { return cp.getHue(); }

    /**
     * Gets the saturation of the selected color.
     *
     * @return the saturation
     */
    public float getSaturation() { return cp.getSaturation(); }

    /**
     * Gets the brightness of the selected color.
     *
     * @return the brightness
     */
    public float getBrightness() { return cp.getBrightness(); }

    /**
     * Gets the angle of the selected hue.
     *
     * @return the hue is degrees
     */
    public int getHueAngle(float hue) {
        int angle = (int) (hue * 360);
        return (angle == 360) ? 0 : angle;
    }

    /**
     * Sets the RGB value of the selected color.
     *
     * @param color the new color
     */
    public void setRGB(Color color) {
        swatchGroupClearSelection();
        cp.setRGB(color.getRed(), color.getGreen(), color.getBlue(), false);
        alpha = color.getAlpha();
        alphaSlider.setValue(alpha);
    }

    /**
     * Sets the RGB value of the selected color.
     *
     * @param r the red component
     * @param g the green component
     * @param b the blue component
     */
    public void setRGB(int r, int g, int b) { cp.setRGB(r, g, b, false); }

    /**
     * Sets the red component of the selected color.
     *
     * @param red the new red component
     */
    public void setRed(int red) { cp.setRed(red); }

    /**
     * Sets the green component of the selected color.
     *
     * @param green the new green component
     */
    public void setGreen(int green) { cp.setGreen(green); }

    /**
     * Sets the blue component of the selected color.
     *
     * @param blue the new blue component
     */
    public void setBlue(int blue) { cp.setBlue(blue); }

    /**
     * Sets the HSB values of the selected color.
     *
     * @param h the hue
     * @param s the saturation
     * @param b the brightness
     */
    public void setHSB(float h, float s, float b) { cp.setHSB(h, s, b); }

    /**
     * Sets the hue of the selected color.
     *
     * @param newHue the new hue
     */
    public void setHue(float newHue) { cp.setHue(newHue); }

    /**
     * Sets the saturation of the selected color.
     *
     * @param newSaturation the new saturation
     */
    public void setSaturation(float newSaturation) { cp.setSaturation(newSaturation); }

    /**
     * Sets the brightness of the selected color.
     *
     * @param newBrightness the new brightness
     */
    public void setBrightness(float newBrightness) { cp.setBrightness(newBrightness); }

    /**
     * Sets the thickness of the color ring.
     *
     * @param ringThickness the new ring thickness
     */
    public void setRingThickness(int ringThickness) { cp.setRingThickness(ringThickness); }




 @SuppressWarnings("InnerClassMayBeStatic")
class ColourRingPicker extends Component implements MouseListener, MouseMotionListener {

    // Default colour
    private float hue = 1f;
    private float saturation = 1f;
    private float brightness = 1f;

     // Colour ring
    private BufferedImage image;
    private int ringThickness = 15;
    private int outerRadius;
    private int imageStartX;
    private int imageStartY;
    private int imageCentreX;
    private int imageCentreY;

    // Hue ring position and icon position
    private FlatSVGIcon selectedHueIcon;
    private int selectedHueIconPosX = -1;
    private int selectedHueIconPosY = -1;
    private Ellipse2D selectedHueMarker;
    private boolean isRingSelection;

    // Colour Triangle
    private Polygon triangle = new Polygon();
    private FlatSVGIcon selectedTriangleIcon;
    private int selectedTrianglePosX;
    private int selectedTrianglePosY;
    private Ellipse2D selectedTriangleMarker;
    private boolean isTriangleSelection;

    /**
     * Constructs a new ColourRingPicker.
     * Initializes the component and sets up mouse listeners.
     */
    public ColourRingPicker() {
        super();
        setOpaque(false);
        addMouseListener(this);
        addMouseMotionListener(this);

        SwingUtilities.invokeLater(() -> {
            if (createRingImage(getWidth(), getHeight())) {
                createHueIcon();
                updateHueIconPosFrom(hue);

                rotateTriangleVerticesTo(hue);
                createColourRingIcon();

                updateTriangleIconPosTo(hue, saturation, brightness);
            }
        });
    }

    @Override
    public void mousePressed(MouseEvent e) {
        if (isPointInTriangle(e.getX(), e.getY()) || this.selectedTriangleMarker.contains(e.getX(), e.getY())) {
            updateSelectedTriangleColour(e.getX(), e.getY());
            isTriangleSelection = true;
        } else if (isPointInHueRing(e) || selectedHueMarker.contains(e.getX(), e.getY())) {
            hue = setHueFromRingPoint(e);
            updateHueIconPosFrom(hue);
            isRingSelection = true;
        }
        rotateTriangleVerticesTo(hue);
        updateTriangleIconPosTo(hue, saturation, brightness);
        notifyColorPropertyChange();
        repaint();
    }

    @Override
    public void mouseDragged(MouseEvent e) {
        int posX, posY;
        if (isTriangleSelection) {
            if (!isPointInTriangle(e.getX(), e.getY())) {
                // Get the nearest point on the triangle if we are outside of it
                Point p = getNearestTrianglePoint(e.getX(), e.getY());
                posX = p.x;
                posY = p.y;
            } else {
                // Use the current mouse position
                posX = e.getX();
                posY = e.getY();
            }
            updateSelectedTriangleColour(posX, posY);
        } else if (isRingSelection) {
            hue = setHueFromRingPoint(e);
            updateHueIconPosFrom(hue);
            rotateTriangleVerticesTo(hue);
        }
        updateTriangleIconPosTo(hue, saturation, brightness);
        notifyColorPropertyChange();
        repaint();
    }

    @Override
    public void mouseClicked(MouseEvent e) {}

    @Override
    public void mouseReleased(MouseEvent e) {
        this.isRingSelection = false;
        this.isTriangleSelection = false;
    }

    @Override
    public void mouseEntered(MouseEvent e) {}

    @Override
    public void mouseExited(MouseEvent e) {}

    @Override
    public void mouseMoved(MouseEvent e) {}

    @Override
    public void paint(Graphics g) {
        Graphics2D g2d = (Graphics2D) g.create();
        g2d.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);
        // Draw the color wheel image
        g2d.drawImage(this.image, this.imageStartX, this.imageStartY, null);
        // Draw the saturation and brightness triangle
        drawTriangle(g2d);
        // The drawn triangle edges are not anti-aliased, so we draw a polygon around it,
        // we set the draw colour to the background colour and we have no jagged edges
        g2d.setColor(getBackground());
        g2d.drawPolygon(triangle);
        // Draw the selection markers
        selectedHueIcon.paintIcon(this, g2d,this.selectedHueIconPosX -(this.selectedHueIcon.getIconWidth()/2), selectedHueIconPosY -(selectedHueIcon.getIconHeight()/2));
        selectedTriangleIcon.paintIcon(this, g2d,selectedTrianglePosX -(selectedTriangleIcon.getIconWidth()/2), selectedTrianglePosY -(selectedTriangleIcon.getIconHeight()/2));

        //DEBUG ONLY!!! - Draw line/marker positions
//        g2d.setColor(Color.LIGHT_GRAY);
//        g2d.drawLine(0, imageCentreY, getWidth(), imageCentreY);
//        g2d.drawLine(imageCentreX, 0, imageCentreX, getHeight());
//        g2d.setColor(new Color(200,200,200));
//        g2d.fill(selectedHueMarker);
//        g2d.setColor(new Color(200,200,200));
//        g2d.fill(selectedTriangleMarker);
        g2d.dispose();
    }

    /**
     * Updates the UI components based on the current hue, saturation, and brightness values.
     * Optionally fires a color property change event is required.
     *
     * @param fireEvent whether to fire a color property change event
     */
    @SuppressWarnings("SameParameterValue")
    private void updateUI(boolean fireEvent) {
        getTriangleCoordinatesFromHSB(hue, saturation, brightness);
        rotateTriangleVerticesTo(hue);
        updateTriangleIconPosTo(hue, saturation, brightness);
        updateHueIconPosFrom(hue);
        if (fireEvent) notifyColorPropertyChange();
        repaint();
    }

    /**
     * Creates the color ring image based on the specified width and height.
     *
     * @param width the width of the image
     * @param height the height of the image
     * @return true if the image was created successfully, false otherwise
     */
    private boolean createRingImage(int width, int height) {

            int minSize = Math.min(width, height);
            if (minSize > 0) {
                this.image = getNewBufferImage(minSize, minSize, Transparency.TRANSLUCENT);
                Graphics2D g2d = image.createGraphics();
                this.outerRadius = (minSize / 2) - (this.ringThickness / 2);
                //     one pass is not enough to completely fill in the color wheel
                for (int i=0; i < 2; i++) {
                    drawColorWheel(g2d, minSize, this.outerRadius);
                }
                g2d.dispose();
                this.imageCentreX = width / 2;
                this.imageCentreY = height / 2;
                this.imageStartX = this.imageCentreX - (minSize / 2);
                this.imageStartY = this.imageCentreY - (minSize / 2);
            } else {
                LOG.info("Failed to createSetting color wheel image - panel size is 0");
                return false;
            }
            return true;
        }

    /**
     * Draws the color wheel on the specified graphics context.
     *
     * @param g the graphics context
     * @param size the size of the color wheel
     * @param radius the radius of the color wheel
     */
    private void drawColorWheel(Graphics g, int size, int radius) {
        if (this.image == null) {
            return;
        }
        Graphics2D g2d = (Graphics2D) g.create();
        g2d.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);
        g2d.setRenderingHint(RenderingHints.KEY_ALPHA_INTERPOLATION, RenderingHints.VALUE_ALPHA_INTERPOLATION_QUALITY);
        g2d.setRenderingHint(RenderingHints.KEY_STROKE_CONTROL, RenderingHints.VALUE_STROKE_PURE);
        for (int a = 1; a <= this.ringThickness; a++) {
            for (int i = 0; i < 360; i++) {
                float hue = (float) i / 360;
                int color = Color.HSBtoRGB(hue, 1.0f, 1.0f);
                g2d.setColor(new Color(color));
                g2d.drawArc(size / 2 - radius + a, size / 2 - radius + a, (radius * 2) - (a * 2), (radius * 2) - (a * 2), i, 1);
            }
        }
        g2d.dispose();
    }

    /**
     * Creates the selection marker icon for the colour ring.
     */
    private void createColourRingIcon() {
        if (this.selectedTriangleIcon == null) {
            this.selectedTriangleIcon = getSVGIcon(COLOURWHEEL_POINT, this.ringThickness, this.ringThickness);
            FlatSVGIcon.ColorFilter customFilter = new FlatSVGIcon.ColorFilter((color) -> {
                if (color.equals(new Color(128, 128, 128))) {
                    return new Color(Color.HSBtoRGB(hue, saturation, brightness));
                }
                return color;
            });
            selectedTriangleIcon.setColorFilter(customFilter);
        }
    }

     /**
      * Checks if the specified mouse coordinates are within the hue ring.
      *
      * @param e the mouse event
      * @return true if the point is within the hue ring, false otherwise
      */
       private boolean isPointInHueRing(MouseEvent e) {
           int x = e.getX() - this.imageCentreX;
           int y = e.getY() - this.imageCentreY;
           double distance = Math.sqrt(x * x + y * y);
           return distance <= this.outerRadius && distance >= this.outerRadius - this.ringThickness;
       }

       /**
        * Sets the hue value based on the co-ordinates of the supplied mouse event.
        *
        * @param e the mouse event
        * @returns new hue value from position
        */
       private float setHueFromRingPoint(MouseEvent e) {
           Point p = getNearestRingPositionTo(e);
           float newValue = (float) (Math.atan2(p.getY(), p.getX()) / (-2 * Math.PI));
           if (newValue < 0) newValue += 1.0f;
           return newValue;
       }

       /**
        * Updates the position of the hue icon based on the specified hue value.
        *
        * @param hue the hue value
        */
       private void updateHueIconPosFrom(float hue) {
           if (this.selectedHueMarker == null) this.selectedHueMarker = new Ellipse2D.Double();
           double angle = 2 * Math.PI * hue;
           this.selectedHueIconPosX = (int) (this.imageCentreX + this.outerRadius * Math.cos(angle));
           this.selectedHueIconPosY = (int) (this.imageCentreY - this.outerRadius * Math.sin(angle));
           this.selectedHueMarker.setFrame(selectedHueIconPosX - ((double) this.selectedHueIcon.getIconWidth() /2), selectedHueIconPosY - ((double) this.selectedHueIcon.getIconHeight() /2), this.ringThickness, this.ringThickness);
       }

       /**
        * Gets the nearest position on the hue ring outer edge to the specified mouse coordinates.
        *
        * @param e the mouse event
        * @return the nearest position on the hue ring
        */
       private Point getNearestRingPositionTo(MouseEvent e) {
           int x = e.getX() - this.imageCentreX;
           int y = e.getY() - this.imageCentreY;
           double distance = Math.sqrt(x * x + y * y);
           if (distance > this.outerRadius) {
               // Clamp the position to the edge of the outer ring
               double scale = this.outerRadius / distance;
               x = (int) (x * scale);
               y = (int) (y * scale);
           } else if (distance < (this.outerRadius - this.ringThickness)) {
               // Clamp the position to the edge of the inner ring
               double scale = (this.outerRadius - this.ringThickness) / distance;
               x = (int) (x * scale);
               y = (int) (y * scale);
           }
           return new Point(x, y);
       }

        //
        // Saturation and Brightness Triangle functions
        //

       /**
        * Creates the icon for the hue selection marker.
        */
        private void createHueIcon() {
            if (this.selectedHueIcon == null) {
                this.selectedHueIcon = getSVGIcon(COLOURWHEEL_POINT, this.ringThickness, this.ringThickness);
                FlatSVGIcon.ColorFilter customFilter = new FlatSVGIcon.ColorFilter((color) -> {
                    if (color.equals(new Color(128, 128, 128))) {
                        return new Color(Color.HSBtoRGB(hue, 1.0f, 1.0f));
                    }
                    return color;
                });
                this.selectedHueIcon.setColorFilter(customFilter);
            }
        }

       /**
        * Rotates the vertices of the triangle based on the specified hue value.
        *
        * @param hue the hue value
        */
       private void rotateTriangleVerticesTo(float hue) {
           if (triangle == null) triangle = new Polygon();
           triangle.reset();
           // Calculate the rotation angle based on the hue
           double rotationAngle = 2 * Math.PI * hue;
           // Calculate the inner radius
           int innerRadius = outerRadius - (ringThickness/2) - 2;
           // createSetting the rotated triangle points
           for (int i = 0; i < 3; i++) {
               double angle = 2 * Math.PI / 3 * i - rotationAngle; // Add the rotation angle to the vertex angle
               int x = (int) (imageCentreX + (innerRadius - ((double) ringThickness / 2)) * Math.cos(angle));
               int y = (int) (imageCentreY + (innerRadius - ((double) ringThickness / 2)) * Math.sin(angle));
               triangle.addPoint(x, y);
           }
       }

       /**
        * Gets the nearest point on the triangle to the specified coordinates.
        *
        * @param x the x-coordinate
        * @param y the y-coordinate
        * @return the nearest point on the triangle
        */
       private Point getNearestTrianglePoint(int x, int y) {
           if (!isPointInTriangle(x, y)) {
               Point closestPoint = getClosestPointInTriangle(x, y);
               x = closestPoint.x;
               y = closestPoint.y;
           }
           return new Point(x, y);
       }

       /**
        * Gets the closest point on the triangle to the specified coordinates.
        *
        * @param x the x-coordinate
        * @param y the y-coordinate
        * @return the closest point on the triangle
        */
       private Point getClosestPointInTriangle(int x, int y) {
           Point p = new Point(x, y);
           Point[] vertices = new Point[] {
                   new Point(this.triangle.xpoints[0], this.triangle.ypoints[0]),
                   new Point(this.triangle.xpoints[1], this.triangle.ypoints[1]),
                   new Point(this.triangle.xpoints[2], this.triangle.ypoints[2])
           };

           Point closestPoint = getClosestPointOnLineSegment(vertices[0], vertices[1], p);
           double minDistance = p.distance(closestPoint);

           for (int i = 1; i < vertices.length; i++) {
               Point currentClosest = getClosestPointOnLineSegment(vertices[i], vertices[(i + 1) % vertices.length], p);
               double currentDistance = p.distance(currentClosest);
               if (currentDistance < minDistance) {
                   minDistance = currentDistance;
                   closestPoint = currentClosest;
               }
           }
           return closestPoint;
       }

       /**
        * Gets the closest point on the line segment between points a and b to point p.
        *
        * @param a the first point of the line segment
        * @param b the second point of the line segment
        * @param p the point to find the closest point to
        * @return the closest point on the line segment
        */
       private Point getClosestPointOnLineSegment(Point a, Point b, Point p) {
           double ax = a.getX();
           double ay = a.getY();
           double bx = b.getX();
           double by = b.getY();
           double px = p.getX();
           double py = p.getY();

           double abx = bx - ax;
           double aby = by - ay;
           double apx = px - ax;
           double apy = py - ay;

           double ab2 = abx * abx + aby * aby;
           double ap_ab = apx * abx + apy * aby;
           double t = ap_ab / ab2;

           if (t < 0) {
               t = 0;
           } else if (t > 1) {
               t = 1;
           }

           double closestX = ax + t * abx;
           double closestY = ay + t * aby;

           return new Point((int) closestX, (int) closestY);
       }

       /**
        * Draws the saturation and brightness triangle on the specified graphics context.
        *
        * @param g2d the graphics context
        */
       private void drawTriangle(Graphics2D g2d){
           // Get the vertices from the Polygon object
           int[] xPoints = this.triangle.xpoints;
           int[] yPoints = this.triangle.ypoints;

           // Calculate the colors at the vertices
           Color colorH = Color.getHSBColor(hue, 1.0f, 1.0f);
           Color colorS = Color.getHSBColor(hue, 0.0f, 1.0f);
           Color colorB = Color.getHSBColor(hue, 1.0f, 0.0f);

           // Draw the triangle by interpolating colors
           for (int y = Math.min(yPoints[0], Math.min(yPoints[1], yPoints[2])); y <= Math.max(yPoints[0], Math.max(yPoints[1], yPoints[2])); y++) {
               for (int x = Math.min(xPoints[0], Math.min(xPoints[1], xPoints[2])); x <= Math.max(xPoints[0], Math.max(xPoints[1], xPoints[2])); x++) {
                   if (isPointInTriangle(x, y)) {
                       float[] barycentric = getBarycentricCoordinatesAreaMethod(x, y, this.triangle.xpoints, this.triangle.ypoints);
                       Color interpolatedColor = interpolateColorLinear(barycentric, colorH, colorS, colorB);
                       g2d.setColor(interpolatedColor);
                       g2d.drawLine(x, y, x, y);
                   }
               }
           }
       }

        /**
         * Checks if a point is inside the triangle defined by the triangle vertices.
         *
         * @param px the x-coordinate of the point
         * @param py the y-coordinate of the point
         * @return true if the point is inside the triangle, false otherwise
         */
        private boolean isPointInTriangle(int px, int py) {
            return isPointInTriangle(px, py, this.triangle.xpoints, this.triangle.ypoints);
        }

        /**
         * Checks if the specified point is inside the triangle defined by the given vertices.
         *
         * @param px the x-coordinate of the point
         * @param py the y-coordinate of the point
         * @param xPoints the x-coordinates of the triangle vertices
         * @param yPoints the y-coordinates of the triangle vertices
         * @return true if the point is inside the triangle, false otherwise
         */
        private boolean isPointInTriangle(int px, int py, int[] xPoints, int[] yPoints) {
            int x1 = xPoints[0], y1 = yPoints[0];
            int x2 = xPoints[1], y2 = yPoints[1];
            int x3 = xPoints[2], y3 = yPoints[2];

            float denominator = (float) ((y2 - y3) * (x1 - x3) + (x3 - x2) * (y1 - y3));
            float a = (float) ((y2 - y3) * (px - x3) + (x3 - x2) * (py - y3)) / denominator;
            float b = (float) ((y3 - y1) * (px - x3) + (x1 - x3) * (py - y3)) / denominator;
            float c = 1 - a - b;

            return a >= 0 && b >= 0 && c >= 0 && a <= 1 && b <= 1 && c <= 1;
        }

        /**
         * Calculates the barycentric coordinates of a point within a triangle.
         *
         * @param x the x-coordinate of the point
         * @param y the y-coordinate of the point
         * @param xPoints the x-coordinates of the triangle vertices
         * @param yPoints the y-coordinates of the triangle vertices
         * @return an array containing the barycentric coordinates
         */
       private float[] getBarycentricCoordinatesAreaMethod(int x, int y, int[] xPoints, int[] yPoints) {
           int x1 = xPoints[0], y1 = yPoints[0];
           int x2 = xPoints[1], y2 = yPoints[1];
           int x3 = xPoints[2], y3 = yPoints[2];

           float denominator = (float) ((y2 - y3) * (x1 - x3) + (x3 - x2) * (y1 - y3));
           float a = (float) ((y2 - y3) * (x - x3) + (x3 - x2) * (y - y3)) / denominator;
           float b = (float) ((y3 - y1) * (x - x3) + (x1 - x3) * (y - y3)) / denominator;
           float c = 1 - a - b;

           return new float[]{a, b, c};
       }

       /**
        * Interpolates a color based on barycentric coordinates and the colors at the triangle vertices.
        *
        * @param barycentric the barycentric coordinates
        * @param colorH the color at the first vertex
        * @param colorS the color at the second vertex
        * @param colorB the color at the third vertex
        * @return the interpolated color
        */
       private Color interpolateColorLinear(float[] barycentric, Color colorH, Color colorS, Color colorB) {
           float a = barycentric[0];
           float b = barycentric[1];
           float c = barycentric[2];

           int red = (int) (a * colorH.getRed() + b * colorS.getRed() + c * colorB.getRed());
           int green = (int) (a * colorH.getGreen() + b * colorS.getGreen() + c * colorB.getGreen());
           int blue = (int) (a * colorH.getBlue() + b * colorS.getBlue() + c * colorB.getBlue());

           // Clamp the color values to the range 0-255
           red = Math.max(0, Math.min(255, red));
           green = Math.max(0, Math.min(255, green));
           blue = Math.max(0, Math.min(255, blue));

           return new Color(red, green, blue);
       }

       /**
        * Gets the selected color within the triangle based on the given coordinates.
        *
        * @param x the x-coordinate within the triangle
        * @param y the y-coordinate within the triangle
        */

       private void updateSelectedTriangleColour(int x, int y) {
           float[] barycentricCoordinates = getBarycentricCoordinatesAreaMethod(x, y, this.triangle.xpoints, this.triangle.ypoints);
           float[] colourHSB = baryCentricToHSB(barycentricCoordinates, hue);
           saturation = colourHSB[1];
           brightness = colourHSB[2];
       }

       /**
        * Converts barycentric coordinates to an RGB color.
        *
        * @param barycentricCoordinates the barycentric coordinates
        * @param hue the hue value
        * @return the RGB color
        */
       private Color baryCentricToRGB(float[] barycentricCoordinates, float hue) {
           return interpolateColor(barycentricCoordinates,
                   Color.getHSBColor(hue, 1.0f, 1.0f),
                   Color.getHSBColor(hue, 0.0f, 1.0f),
                   Color.getHSBColor(hue, 1.0f, 0.0f));
       }

       /**
        * Converts barycentric coordinates to HSB values.
        *
        * @param barycentricCoordinates the barycentric coordinates
        * @param hue the hue value
        * @return an array containing the HSB values
        */
       private float[] baryCentricToHSB(float[] barycentricCoordinates, float hue) {
           Color c = interpolateColor(barycentricCoordinates,
                   Color.getHSBColor(hue, 1.0f, 1.0f),
                   Color.getHSBColor(hue, 0.0f, 1.0f),
                   Color.getHSBColor(hue, 1.0f, 0.0f));
           return Color.RGBtoHSB(c.getRed(), c.getGreen(), c.getBlue(), null);
       }

       /**
        * Interpolates a color based on barycentric coordinates and the colors at the triangle vertices.
        *
        * @param barycentric the barycentric coordinates
        * @param colorH the color at the first vertex
        * @param colorS the color at the second vertex
        * @param colorB the color at the third vertex
        * @return the interpolated color
        */
       private Color interpolateColor(float[] barycentric, Color colorH, Color colorS, Color colorB) {
           float a = barycentric[0];
           float b = barycentric[1];
           float c = barycentric[2];

           int red = (int) (a * colorH.getRed() + b * colorS.getRed() + c * colorB.getRed());
           int green = (int) (a * colorH.getGreen() + b * colorS.getGreen() + c * colorB.getGreen());
           int blue = (int) (a * colorH.getBlue() + b * colorS.getBlue() + c * colorB.getBlue());

           // Clamp the color values to the range 0-255
           red = Math.max(0, Math.min(255, red));
           green = Math.max(0, Math.min(255, green));
           blue = Math.max(0, Math.min(255, blue));

           return new Color(red, green, blue);
       }

       /**
        * Updates the position of the triangle icon based on the given HSB values.
        *
        * @param hue the hue value
        * @param saturation the saturation value
        * @param brightness the brightness value
        */
       private void updateTriangleIconPosTo(float hue, float saturation, float brightness) {
           if (this.selectedTriangleMarker == null) this.selectedTriangleMarker = new Ellipse2D.Double();
           Point p = getTriangleCoordinatesFromHSB(hue, saturation, brightness);
           float[] selectedBarycentricCoordinates = getBarycentricCoordinatesAreaMethod((int) p.getX(), (int) p.getY(), this.triangle.xpoints, this.triangle.ypoints);
           this.selectedTrianglePosX = (int) (selectedBarycentricCoordinates[0] * this.triangle.xpoints[0] + selectedBarycentricCoordinates[1] * this.triangle.xpoints[1] + selectedBarycentricCoordinates[2] * this.triangle.xpoints[2]);
           this.selectedTrianglePosY = (int) (selectedBarycentricCoordinates[0] * this.triangle.ypoints[0] + selectedBarycentricCoordinates[1] * this.triangle.ypoints[1] + selectedBarycentricCoordinates[2] * this.triangle.ypoints[2]);
           selectedTriangleMarker.setFrame(selectedTrianglePosX - ((double) selectedTriangleIcon.getIconWidth() /2), selectedTrianglePosY - ((double) selectedTriangleIcon.getIconHeight() /2), ringThickness, ringThickness);
       }

       /**
        * Gets the coordinates within the triangle based on the given HSB values.
        *
        * @param hue the hue value
        * @param saturation the saturation value
        * @param brightness the brightness value
        * @return the coordinates within the triangle
        */
       private Point getTriangleCoordinatesFromHSB(float hue, float saturation, float brightness) {
           // Get the vertices from the Polygon object
           int[] xPoints = this.triangle.xpoints;
           int[] yPoints = this.triangle.ypoints;

           // Calculate the colors at the vertices
           Color colorH = Color.getHSBColor(hue, 1.0f, 1.0f);
           Color colorS = Color.getHSBColor(hue, 0.0f, 1.0f);
           Color colorB = Color.getHSBColor(hue, 1.0f, 0.0f);

           Color targetColor = Color.getHSBColor(hue, saturation, brightness);
           double minDistance = Double.MAX_VALUE;
           Point closestPoint = new Point(0, 0);

           for (int y = Math.min(yPoints[0], Math.min(yPoints[1], yPoints[2])); y <= Math.max(yPoints[0], Math.max(yPoints[1], yPoints[2])); y++) {
               for (int x = Math.min(xPoints[0], Math.min(xPoints[1], xPoints[2])); x <= Math.max(xPoints[0], Math.max(xPoints[1], xPoints[2])); x++) {
                   if (isPointInTriangle(x, y, this.triangle.xpoints, this.triangle.ypoints)) {
                       float[] barycentric = getBarycentricCoordinatesAreaMethod(x, y, this.triangle.xpoints, this.triangle.ypoints);
                       Color interpolatedColor = interpolateColor(barycentric, colorH, colorS, colorB);
                       double distance = colorDistance(interpolatedColor, targetColor);
                       if (distance < minDistance) {
                           minDistance = distance;
                           closestPoint.setLocation(x, y);
                       }
                       if (interpolatedColor.equals(targetColor)) {
                           this.selectedTrianglePosX = x;
                           this.selectedTrianglePosY = y;
                           return new Point(this.selectedTrianglePosX, this.selectedTrianglePosY);
                       }
                   }
               }
           }
           return closestPoint;
       }

       /**
        * Calculates the distance between two colors.
        *
        * @param c1 the first color
        * @param c2 the second color
        * @return the distance between the two colors
        */
       private double colorDistance(Color c1, Color c2) {
           int r1 = c1.getRed();
           int g1 = c1.getGreen();
           int b1 = c1.getBlue();
           int r2 = c2.getRed();
           int g2 = c2.getGreen();
           int b2 = c2.getBlue();
           return Math.sqrt(Math.pow(r1 - r2, 2) + Math.pow(g1 - g2, 2) + Math.pow(b1 - b2, 2));
       }


       //
       // Getters
       //

     /**
      * Gets the RGB value of the selected color.
      *
      * @return the RGB value
      */
     public int getRGB() { return Color.HSBtoRGB(hue, saturation, brightness); }

     /**
      * Gets the red component of the selected color.
      *
      * @return the red component
      */
     public int getRed() { return Color.HSBtoRGB(hue, saturation, brightness) >> 16 & 0xFF; }

     /**
      * Gets the green component of the selected color.
      *
      * @return the green component
      */
     public int getGreen() { return Color.HSBtoRGB(hue, saturation, brightness) >> 8 & 0xFF; }

     /**
      * Gets the blue component of the selected color.
      *
      * @return the blue component
      */
     public int getBlue() { return Color.HSBtoRGB(hue, saturation, brightness) & 0xFF; }

     /**
      * Gets the hue of the selected color.
      *
      * @return the hue
      */
     public float getHue() { return hue; }

     /**
      * Gets the saturation of the selected color.
      *
      * @return the saturation
      */
     public float getSaturation() { return saturation; }

     /**
      * Gets the brightness of the selected color.
      *
      * @return the brightness
      */
     public float getBrightness() { return brightness; }

     /**
      * Gets the saturation of the selected color as a percentage.
      *
      * @return the saturation
      */
     public int getSaturationPercentage() { return (int) (saturation*100); }

     /**
      * Gets the brightness of the selected color as a percentage.
      *
      * @return the brightness
      */
     public int getBrightnessPercentage() { return (int) (brightness*100); }

     //
     // Setters
     //

     /**
      * Sets the RGB value of the selected color.
      *
      * @param color the new color
      */
     public void setRGB(Color color) { setRGB(color.getRed(), color.getGreen(), color.getBlue(), color.getAlpha()); }

     /**
      * Sets the RGB value of the selected color.
      *
      * @param r the red component
      * @param g the green component
      * @param b the blue component
      */
     public void setRGB(int r, int g, int b, boolean fireEvent) {
         float[] hsb = Color.RGBtoHSB(r, g, b, null);
         this.hue = hsb[0];
         this.saturation = hsb[1];
         this.brightness = hsb[2];
         updateUI(fireEvent);
     }

     /**
      * Sets the RGB value of the selected color.
      *
      * @param r the red component
      * @param g the green component
      * @param b the blue component
      */
     public void setRGB(int r, int g, int b, int alpha) {
         float[] hsb = Color.RGBtoHSB(r, g, b, null);
         this.hue = hsb[0];
         this.saturation = hsb[1];
         this.brightness = hsb[2];
         updateUI(true);
     }

     /**
      * Sets the red component of the selected color.
      *
      * @param red the new red component
      */
     public void setRed(int red) { setRGB(red, getGreen(), getBlue() , true); }

     /**
      * Sets the green component of the selected color.
      *
      * @param green the new green component
      */
     public void setGreen(int green) { setRGB(getRed(), green, getBlue(), true); }

     /**
      * Sets the blue component of the selected color.
      *
      * @param blue the new blue component
      */
     public void setBlue(int blue) { setRGB(getRed(), getGreen(), blue, true); }

     /**
      * Sets the HSB values of the selected color.
      *
      * @param h the hue
      * @param s the saturation
      * @param b the brightness
      */
     public void setHSB(float h, float s, float b) {
         this.hue = h;
         this.saturation = s;
         this.brightness = b;
         updateUI(true);
     }

     /**
      * Sets the hue of the selected color.
      *
      * @param newHue the new hue
      */
     public void setHue(float newHue) { setHSB(newHue, saturation, brightness); }

     /**
      * Sets the saturation of the selected color.
      *
      * @param newSaturation the new saturation
      */
     public void setSaturation(float newSaturation) { setHSB(hue, newSaturation, brightness); }

     /**
      * Sets the brightness of the selected color.
      *
      * @param newBrightness the new brightness
      */
     public void setBrightness(float newBrightness) { setHSB(hue, saturation, newBrightness); }

     /**
      * Sets the thickness of the color ring.
      *
      * @param ringThickness the new ring thickness
      */
     public void setRingThickness(int ringThickness) {
         this.ringThickness = ringThickness;
         selectedHueIcon = getSVGIcon(COLOURWHEEL_POINT, ringThickness, ringThickness);
         selectedTriangleIcon = getSVGIcon(COLOURTRIANGE_POINT, ringThickness, ringThickness);
         createRingImage(this.getWidth(), this.getHeight());
     }
 }

    /**
     * Listener interface for receiving color property change events.
     */
    public interface ColorPropertyChangeListener extends EventListener {
        void colorPropertyChange(ColorPropertyChangeEvent evt);
    }

    /**
     * Event class for color property change events.
     */
    public static class ColorPropertyChangeEvent extends EventObject {
        private final Color color;
        private final float[] hsbValues;

        /**
         * Constructs a new ColorPropertyChangeEvent.
         *
         * @param source        the source of the event
         * @param oldColor      the old color
         * @param hsbValueArray the HSB values
         */
        public ColorPropertyChangeEvent(Object source, Color oldColor, float[] hsbValueArray) {
            super(source);
            this.color = oldColor;
            this.hsbValues = hsbValueArray;
        }

        /**
         * Gets the color.
         *
         * @return the color
         */
        public Color getColor() {
            return color;
        }

        /**
         * Gets the HSB values.
         *
         * @return the HSB values
         */
        public float[] getHSBValues() {
            return hsbValues;
        }
    }
}