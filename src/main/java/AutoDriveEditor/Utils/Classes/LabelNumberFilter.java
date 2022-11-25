package AutoDriveEditor.Utils.Classes;

import javax.swing.*;
import javax.swing.text.AttributeSet;
import javax.swing.text.DocumentFilter;
import java.awt.*;

public class LabelNumberFilter extends DocumentFilter {

    private final JLabel messageLabel;
    private final double maxValue;
    private final double minValue;
    private final boolean useDecimalPlaces;

    private final boolean canBeNegative;

    public LabelNumberFilter(JLabel label, double min, double max, boolean allowDecimalPlaces, boolean canBeNegative) {
        super();
        this.messageLabel = label;
        this.minValue = min;
        this.maxValue = max;
        this.useDecimalPlaces = allowDecimalPlaces;
        this.canBeNegative = canBeNegative;

    }

    @Override
    public void insertString(FilterBypass fb, int offset,String string, AttributeSet attr) {
        try {
            if (this.useDecimalPlaces) {
                if (string.equals(".") && !fb.getDocument().getText(0, fb.getDocument().getLength()).contains(".")) {
                    super.insertString(fb, offset, string, attr);
                    return;
                }
            }
            if (this.canBeNegative) {
                if (string.equals("-") && !fb.getDocument().getText(0, fb.getDocument().getLength()).contains("-")) {
                    super.insertString(fb, offset, string, attr);
                    return;
                }
            }
            Double.parseDouble(string);
            super.insertString(fb, offset, string, attr);

        } catch (Exception e) {
            if (this.messageLabel != null) {
                this.messageLabel.setForeground(Color.RED);
                if (useDecimalPlaces) {
                    this.messageLabel.setText("* Numeric digits ( 0-9 ) and ( . ) only");
                } else {
                    this.messageLabel.setText("* Numeric digits ( 0-9 ) only");
                }
            }
        }
    }

    @Override
    public void replace(FilterBypass fb, int offset, int length, String text, AttributeSet attrs) {
        try {
            if (this.useDecimalPlaces) {
                if (text.equals(".") && !fb.getDocument().getText(0, fb.getDocument().getLength()).contains(".")) {
                    super.insertString(fb, offset, text, attrs);
                    return;
                }
            }
            if (this.canBeNegative) {
                if (text.equals("-") && !fb.getDocument().getText(0, fb.getDocument().getLength()).contains("-")) {
                    super.insertString(fb, offset, text, attrs);
                    return;
                }
            }
            Double.parseDouble(text);
            if (this.messageLabel != null) this.messageLabel.setText(" ");
            super.replace(fb, offset, length, text, attrs);
            double value = Double.parseDouble(fb.getDocument().getText(0, fb.getDocument().getLength()));
            if ( value > this.maxValue) {
                if (this.messageLabel != null) {
                    this.messageLabel.setForeground(Color.RED);
                    this.messageLabel.setText("Value " + text + " Cannot be bigger than " + maxValue);
                }
                super.replace(fb, offset, length +1, "", attrs);
            }
            if ( value < this.minValue) {
                if (this.messageLabel != null) {
                    this.messageLabel.setForeground(Color.RED);
                    this.messageLabel.setText("Value " + text + " Cannot be lower than " + minValue);
                }
                super.replace(fb, offset, length -1, "", attrs);
            }
        } catch (Exception e) {
            String message = e.getMessage();
            if (this.messageLabel != null) {
                this.messageLabel.setForeground(Color.RED);
                if (message.contains("-")) {
                    this.messageLabel.setText("* Value cannot be negative");
                } else if ( message.contains(".")) {
                    this.messageLabel.setText("* Decimal places not allowed here");
                } else {
                    this.messageLabel.setText("* Numeric digits ( 0-9 ) only");
                }
            }
        }
    }
}
