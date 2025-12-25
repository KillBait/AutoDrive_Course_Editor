package AutoDriveEditor.Classes.UI_Components;

import net.miginfocom.swing.MigLayout;

import javax.swing.*;
import javax.swing.border.Border;
import javax.swing.event.AncestorEvent;
import javax.swing.event.AncestorListener;
import java.awt.*;
import java.awt.event.MouseAdapter;
import java.awt.event.MouseEvent;
import java.awt.geom.Path2D;

import static AutoDriveEditor.AutoDriveEditor.getMapPanel;
import static AutoDriveEditor.Classes.Util_Classes.LoggerUtils.LOG;
import static AutoDriveEditor.Locale.LocaleManager.getLocaleString;

@SuppressWarnings("unused")
public class PopoutJPanel extends JPanel {

    private final PopoutJPanel popupPanel;
    private float animationSpeed = 3;
    private Color backgroundColor = null;
    private int borderWidth = 3;
    private Color borderColor = null;
    private int borderRadius = 12;
    private boolean borderOpaque = true;
    private final boolean borderVisible = true;
    private boolean isSticky = false;
    public enum Justification { LEFT, CENTER, RIGHT }
    private Justification justification;
    private Component anchor;

    private Timer animationTimer;
    private int animationDelay = 10;
    private int targetHeight;
    private float currentHeight = 0;
    private boolean isExpanding = true;

    private final JPanel contentPanel;

    private boolean useAbsoluteAnchorX = false;
    private boolean useAbsoluteAnchorY = false;
    private int anchorY = 0;

    public PopoutJPanel() {
        this(null, null, "", Justification.CENTER, true, true);
    }

    public PopoutJPanel(String headerText) {
        this(null, null, headerText, Justification.CENTER, true, true);
    }

    public PopoutJPanel(LayoutManager layout) {
        this(null, layout, "", Justification.CENTER, true, true);
    }

    public PopoutJPanel(Component parentComponent, String headerText) {
        this(parentComponent, null, headerText, Justification.CENTER, true, true);
    }

    public PopoutJPanel(LayoutManager layout, String headerText) {
        this(null, layout, headerText, Justification.CENTER, true, true);
    }

    public PopoutJPanel(LayoutManager layout, String headerText, Justification bHeaderJustify, boolean bUseSeparator) {
        this(null, layout, headerText, bHeaderJustify, bUseSeparator, true);
    }

    public PopoutJPanel(String headerText, Justification bHeaderJustify, boolean bUseSeparator) {
        this(null, null, headerText, bHeaderJustify, bUseSeparator, true);
    }

    public PopoutJPanel(Component parentComponent, LayoutManager layout, String headerText, Justification justify, boolean bUseSeparator, boolean doubleBuffered) {
        setBorder(new PopupBorder());
        popupPanel = this;
        this.anchor = parentComponent;
        // use the super.setLayout() method to avoid calling the overridden method
        super.setLayout(new MigLayout("insets 5, gap 5"));
        this.justification = justify;
        this.setDoubleBuffered(doubleBuffered);
        this.setOpaque(false);
        this.setVisible(false);

        if (!headerText.isEmpty()) {
            JLabel headerLabel = new JLabel(getLocaleString(headerText));
            headerLabel.setHorizontalAlignment(SwingConstants.CENTER);
            Font adjust = headerLabel.getFont().deriveFont(Font.BOLD + Font.ITALIC);
            headerLabel.setFont(adjust);
            super.add(headerLabel, "growx, wrap");
            if (bUseSeparator) super.add(new JSeparator(), "grow, wrap");
        }

        contentPanel = new JPanel(layout);
        super.add(contentPanel);

        animationTimer = new Timer(animationDelay, e -> {
            if (isExpanding) {
                this.revalidate();
                if (currentHeight < targetHeight) {
                    currentHeight += animationSpeed;
                    this.setSize(this.getPreferredSize().width, (int) currentHeight);
                } else {
                    animationTimer.stop();
                }
            } else {
                if (currentHeight > 0) {
                    currentHeight -= animationSpeed;
                    this.setSize(this.getPreferredSize().width, (int) currentHeight);
                } else {
                    animationTimer.stop();
                    this.setVisible(false);
                }
            }
        });

        UIManager.addPropertyChangeListener(evt -> {
            if ("lookAndFeel".equals(evt.getPropertyName())) {
                Timer delayTimer = new Timer(5, e1 -> {
                    Point newPoint = calculateLocation();
                    popupPanel.setSize(popupPanel.getPreferredSize().width, (int) currentHeight);
                    setLocation(newPoint);
                    popupPanel.repaint();
                });
                delayTimer.setRepeats(false);
                delayTimer.start();
            }
        });

        // Add MouseListener to detect clicks outside the panel
        addAncestorListener(new AncestorListener() {
            @Override
            public void ancestorAdded(AncestorEvent event) {
                Window window = SwingUtilities.getWindowAncestor(PopoutJPanel.this);
                if (window != null) {
                    getMapPanel().addMouseListener(new MouseAdapter() {
                        @Override
                        public void mousePressed(MouseEvent e) {
                            if (!popupPanel.contains(e.getPoint()) && !isSticky) {
                                playClosingAnimation();
                            }
                        }
                    });
                }
            }

            @Override
            public void ancestorRemoved(AncestorEvent event) {
                // No action needed
            }

            @Override
            public void ancestorMoved(AncestorEvent event) {
                // No action needed
            }
        });
    }

    public JPanel getContentPanel() {
        return contentPanel;
    }

    @Override
    public Component add(Component comp) {
        contentPanel.add(comp);
        this.setSize(this.getPreferredSize());
        return comp;
    }

    @Override
    public void add(Component comp, Object constraints) {
        contentPanel.add(comp, constraints);
        this.setSize(this.getPreferredSize());
    }

    @Override
    public Component add(Component comp, int index) {
        contentPanel.add(comp, index);
        this.setSize(this.getPreferredSize());
        return comp;
    }

    @Override
    public void add(Component comp, Object constraints, int index) {
        contentPanel.add(comp, constraints, index);
        this.setSize(this.getPreferredSize());
    }

    @Override
    public void setLayout(LayoutManager layout) {
        if (contentPanel != null) contentPanel.setLayout(layout);
    }

    public void playAnimation() {
        if (anchor != null) {
            if (isVisible()) {
                playClosingAnimation();
            } else {
                playOpeningAnimation();
            }
        } else {
            LOG.error("PopupPanel.playAnimation(): Parent component is null. Cannot play animation.");
        }
    }

    public void playOpeningAnimation() {
        if (animationTimer.isRunning()) animationTimer.stop();
        currentHeight = 0;
        this.setSize(this.getPreferredSize().width, (int) currentHeight);
        this.setVisible(true);

        Point anchorLocation = calculateLocation();
        setLocation(anchorLocation);

        targetHeight = this.getPreferredSize().height;
        isExpanding = true;
        animationTimer.start();
    }

    public void playClosingAnimation() {
        if (animationTimer.isRunning()) {
            animationTimer.stop();
        }
        isExpanding = false;
        animationTimer.start();
    }

    private Point calculateLocation() {
        Point anchorLocation = new Point(0, 0);
        int xOffset = 0;
        if (anchor != null) {
            if (anchor.getParent() != null) {
                int locationX = anchor.getParent().getLocation().x + anchor.getLocation().x - this.getParent().getLocation().x;
                int locationY = anchor.getParent().getLocation().y;
                anchorLocation.setLocation(locationX, (useAbsoluteAnchorY) ? anchorY : locationY);
            } else {
                LOG.info("PopupPanel.calculateLocation(): Parent component location not found. Using default location.");
            }

            switch (justification) {
                case LEFT:
                    break;
                case CENTER:
                    xOffset = (anchor.getWidth() / 2) - (this.getPreferredSize().width / 2);
                    break;
                case RIGHT:
                    xOffset = anchor.getWidth() - this.getPreferredSize().width;
                    break;
            }
        } else {
            LOG.error("PopupPanel.calculateLocation(): Parent component is null. Calculating location.");
            return new Point(anchorLocation.x + xOffset, anchorLocation.y);

        }
        return new Point(anchorLocation.x + xOffset, anchorLocation.y);
    }

    //
    // Getters
    //

    public Component getAnchorComponent() {
        return anchor;
    }

    public int getBorderWidth() {
        return borderWidth;
    }

    public float getAnimationSpeed() {
        return animationSpeed;
    }

    public int getAnimationDelay() {
        return animationDelay;
    }

    public Color getBorderColor() {
        return borderColor;
    }

    public Color getBackgroundColor() {
        return backgroundColor;
    }

    public int getBorderRadius() {
        return borderRadius;
    }

    public boolean isBorderOpaque() {
        return borderOpaque;
    }

    public Justification getJustification() {
        return justification;
    }

    public boolean isSticky() {
        return isSticky;
    }

    //
    // Setters
    //

    public void setAnchorComponent(Component parentComponent) {
        this.anchor = parentComponent;
    }

    public void setBorderWidth(int width) { this.borderWidth = width; }

    public void setAnimationSpeed(float speed) { this.animationSpeed = speed; }

    public void setAnimationDelay(int delay) { this.animationDelay = delay; }

    public void setBorderColor(Color color) { this.borderColor = color; }

    public void setBackgroundColor(Color color) {
        this.backgroundColor = color;
        contentPanel.setBackground(color);
    }

    public void setBorderRadius(int radius) { this.borderRadius = radius; }

    public void setBorderOpaque(boolean opaque) { this.borderOpaque = opaque; }

    public void setJustificationX(Justification justify) { this.justification = justify; }

    public void setSticky(boolean isSticky) { this.isSticky = isSticky; }

    public void setAnchorPositionX(int x) {
        useAbsoluteAnchorX = true;
    }

    public void setAnchorPositionY(int y) {
        useAbsoluteAnchorY = true;
        anchorY = y;
    }

    class PopupBorder implements Border {
        @Override
        public void paintBorder(Component c, Graphics g, int x, int y, int width, int height) {
            Graphics2D g2d = (Graphics2D) g.create();
            g2d.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);
            double halfWidth = (double) borderWidth / 2;
            Color bgFill = (backgroundColor == null) ? UIManager.getColor("Panel.background") : backgroundColor;
            Color borderFill = (borderColor == null) ? UIManager.getColor("Button.selectedBorderColor") : borderColor;

            Path2D borderPath = new Path2D.Float();
            borderPath.moveTo(x + (halfWidth + borderRadius), y + halfWidth);
            borderPath.quadTo(x + halfWidth, y + halfWidth, x + halfWidth, y + (borderRadius + halfWidth));
            borderPath.lineTo(x + halfWidth, y + (height - borderRadius));
            borderPath.quadTo(x + halfWidth, y + (height - halfWidth), x + (borderRadius - halfWidth), y + (height - halfWidth));
            borderPath.lineTo(x + (width - (borderRadius-halfWidth)), y + (height - halfWidth));
            borderPath.quadTo(x + (width-halfWidth), y + (height-halfWidth), x + (width - halfWidth), y + (height - borderRadius));
            borderPath.lineTo(x + (width - halfWidth), y + (borderRadius + halfWidth));
            borderPath.quadTo(x + (width - halfWidth), y + halfWidth, x + (width - (borderRadius - halfWidth)), y + halfWidth);
            borderPath.closePath();

            g2d.setColor(bgFill);
            g2d.fill(borderPath);

            g2d.setColor(borderFill);
            g2d.setStroke(new BasicStroke(borderWidth, BasicStroke.CAP_BUTT, BasicStroke.JOIN_BEVEL));
            g2d.setComposite(AlphaComposite.SrcOver.derive(isBorderOpaque()? 1f : .25f));
            g2d.draw(borderPath);

            g2d.dispose();
        }

        @Override
        public Insets getBorderInsets(Component c) {
            return new Insets(0, (borderRadius/8)*3, 0, (borderRadius/4));
        }

        @Override
        public boolean isBorderOpaque() {
            return borderOpaque;
        }
    }
}