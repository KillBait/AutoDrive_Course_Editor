package AutoDriveEditor.Classes.UI_Components.HeaderList;

import AutoDriveEditor.Classes.UI_Components.ColorIcon;

import javax.swing.*;
import java.awt.*;

/**
 * Custom cell renderer for the HeaderList.
 * This renderer handles different types of entries such as headers, separators, and empty spaces.
 * Created 14/11/2024 by KillBait!
 */
public class HeaderListCellRenderer extends DefaultListCellRenderer {

    /**
     * Returns the component used for drawing the cell.
     *
     * @param list the JList we're painting
     * @param value the value returned by list.getModel().getElementAt(index)
     * @param index the cell's index
     * @param isSelected true if the specified cell was selected
     * @param cellHasFocus true if the specified cell has the focus
     * @return the component used for drawing the cell
     */
    @Override
    public Component getListCellRendererComponent(JList<?> list, Object value, int index, boolean isSelected, boolean cellHasFocus) {
        if (value instanceof HeaderListEntry) {
            HeaderListEntry entry = (HeaderListEntry) value;
            if (entry.isSpacer()) {
                JPanel panel = new JPanel();
                panel.setOpaque(false);
                panel.setPreferredSize(new Dimension(1, entry.getSpacerHeight())); // Adjust the height as needed
                return panel;
            }

            int fontStyle = Font.PLAIN;
            if (entry.isBold() && entry.isItalic()) {
                fontStyle = Font.BOLD | Font.ITALIC;
            } else if (entry.isBold()) {
                fontStyle = Font.BOLD;
            } else if (entry.isItalic()) {
                fontStyle = Font.ITALIC;
            }

            if (entry.isSeparator()) {
                return new CustomHeaderLabel(entry, fontStyle);
                //return getJPanel(entry, fontStyle);
            } else if (entry.isHeader()) {
                JLabel label = new JLabel(entry.getDisplayString());
                label.setFont(label.getFont().deriveFont(fontStyle));
                label.setHorizontalAlignment(SwingConstants.CENTER);
                if (entry.getTextColor() != null) label.setForeground(entry.getTextColor());
                label.setBorder(BorderFactory.createEmptyBorder(5, 5, 5, 5));
                return label;
            } else {
                JLabel label = (JLabel) super.getListCellRendererComponent(list, entry.getDisplayString(), index, isSelected, cellHasFocus);
                label.setFont(label.getFont().deriveFont(fontStyle));
                if (entry.getIcon() != null) {
                    label.setIcon(entry.getIcon());
                    //label.setIconTextGap(10);
                } else if (entry.getIconColor() != null) {
                    label.setIcon(new ColorIcon(entry.getIconColor()));
                }
                return label;
            }
        }
        return super.getListCellRendererComponent(list, value, index, isSelected, cellHasFocus);
    }

    /**
     * Creates a JPanel for a separator entry.
     *
     * @param entry the HeaderListEntry representing the separator
     * @param fontStyle the font style to apply to the label
     * @return a JPanel containing the separator
     */
    private static JPanel getJPanel(HeaderListEntry entry, int fontStyle) {
        JPanel panel = new JPanel(new BorderLayout());
        panel.setOpaque(false);
        panel.setBorder(BorderFactory.createEmptyBorder(5, 0, 5, 0));
        if (entry.getDisplayString() != null) {
            JLabel label = new JLabel(entry.getDisplayString());
            label.setHorizontalAlignment(SwingConstants.CENTER);
            if (entry.getTextColor() != null) label.setForeground(entry.getTextColor());
            label.setFont(label.getFont().deriveFont(fontStyle));
            panel.add(label, BorderLayout.NORTH);
        }
        JSeparator separator = new JSeparator(SwingConstants.HORIZONTAL);
        separator.setForeground(entry.getSeparatorColor());
        panel.add(separator, BorderLayout.CENTER);
        return panel;
    }

    private static class CustomHeaderLabel extends JLabel {
        private final Color textColor;
        private final int fontStyle;
        private final HeaderListEntry entry;

        public CustomHeaderLabel(HeaderListEntry entry, int fontStyle) {
            super(entry.getDisplayString());
            this.entry = entry;
            this.textColor = entry.getTextColor();
            this.fontStyle = fontStyle;
            setHorizontalAlignment(SwingConstants.CENTER);
            setFont(getFont().deriveFont(fontStyle));
            if (textColor != null) {
                setForeground(textColor);
            }
        }

        @Override
        protected void paintComponent(Graphics g) {
            super.paintComponent(g);
            Graphics2D g2d = (Graphics2D) g.create();
            g2d.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);

            FontMetrics fm = g2d.getFontMetrics();
            int textWidth = fm.stringWidth(getText());
            int x = (getWidth() - textWidth) / 2;
            int y = getHeight() / 2;

            g2d.setColor(entry.getTextColor());

            g2d.drawLine(0, y, x - 5, y);
            g2d.drawLine(x + textWidth + 5, y, getWidth(), y);
            g2d.setColor(this.getForeground().brighter());
            g2d.drawLine(0, y+1, x - 5, y+1);
            g2d.drawLine(x + textWidth + 5, y+1, getWidth(), y+1);
            g2d.dispose();
        }
    }
}