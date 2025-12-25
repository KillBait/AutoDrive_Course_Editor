package AutoDriveEditor.Classes.UI_Components;

import javax.swing.*;
import java.awt.*;
import java.awt.image.BufferedImage;

import static AutoDriveEditor.Managers.IconManager.*;

@SuppressWarnings("unused")
public class ToolbarButton extends JToggleButton {

    private Icon dropdownIcon;
    private final Icon rotatedDropdownIcon;
    private Insets borderInsets;

    public ToolbarButton(JPanel panel, Icon icon) {
        super();
        this.dropdownIcon = new DropDownIcon(getSVGIcon(MENU_DROPDOWN_ICON), false, 6, 4, 1.0f, .25f, 100);
        this.rotatedDropdownIcon = new DropDownIcon(getSVGIcon(MENU_DROPDOWN_ROTATED_ICON), false, 6, 4, 1.0f, .25f, 100);
        setIcon(icon);
        setHorizontalAlignment(SwingConstants.LEFT);
        panel.add(this);
    }

    @Override
    public void setIcon(Icon icon) {
        super.setIcon(icon);
        setHorizontalAlignment(SwingConstants.LEFT);
        repaint();
    }

    @Override
    public Dimension getPreferredSize() {
        Dimension old = super.getPreferredSize();
        this.borderInsets = getInsets();
        return new Dimension( old.width + this.dropdownIcon.getIconWidth()+4, old.height);
    }

    public DropDownIcon getDropdownIcon() {
        return (DropDownIcon) this.dropdownIcon;
    }

    public void setDropdownIcon(Icon icon) {
        this.dropdownIcon = icon;
        repaint();
    }

    private Icon createRotatedIcon(Icon icon) {
        BufferedImage rotated = getSVGBufferImage(MENU_DROPDOWN_ICON, 6, 4, null);
        int w = icon.getIconWidth();
        int h = 10;
        BufferedImage image = new BufferedImage(w, h, BufferedImage.TYPE_INT_ARGB);
        Graphics2D g2d = image.createGraphics();
        g2d.rotate(Math.toRadians(180), w / 2.0, h / 2.0);
        g2d.drawImage(rotated, 0, 0, null);
        //icon.paintIcon(null, g2d, 0, 0);
        g2d.dispose();
        return new ImageIcon(image);
    }

    @Override
    protected void paintComponent(Graphics g) {
        super.paintComponent(g);
        Icon iconToPaint = isSelected() ? this.rotatedDropdownIcon : this.dropdownIcon;
        iconToPaint.paintIcon(this, g, getWidth() - this.borderInsets.left - iconToPaint.getIconWidth() - (this.borderInsets.right / 2), this.borderInsets.top);
    }
}