package AutoDriveEditor.Utils.Classes;

import javax.swing.event.ChangeEvent;
import javax.swing.event.ChangeListener;
import javax.swing.text.AttributeSet;
import javax.swing.text.BadLocationException;
import javax.swing.text.DocumentFilter;

public class EventTriggerNumberFilter extends DocumentFilter {

    private final ChangeListener changeListener;
    private final int maxValue;
    private final int minValue;
    private final boolean useDecimalPlaces;

    private final boolean canBeNegative;

    public EventTriggerNumberFilter(int min, int max, boolean allowDecimalPlaces, boolean canBeNegative, ChangeListener changeListener) {
        super();
        this.minValue = min;
        this.maxValue = max;
        this.useDecimalPlaces = allowDecimalPlaces;
        this.canBeNegative = canBeNegative;
        this.changeListener = changeListener;
    }

    @Override
    public void remove(FilterBypass fb, int offset, int length) throws BadLocationException {
        super.remove(fb, offset, length);
        if (fb.getDocument().getLength() != 0) {
            this.changeListener.stateChanged(new ChangeEvent(Integer.parseInt(fb.getDocument().getText(0, fb.getDocument().getLength()))));
        }
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
            Integer.parseInt(string);
            super.insertString(fb, offset, string, attr);
        } catch (Exception ignored) {
            // this will only trigger if an unwanted key is pressed i.e. not a numeric 0 - 9
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
            Integer.parseInt(text);
            super.replace(fb, offset, length, text, attrs);
            int value = Integer.parseInt(fb.getDocument().getText(0, fb.getDocument().getLength()));
            if ( value > this.maxValue) {
                super.replace(fb, offset, length +1, "", attrs);
                this.changeListener.stateChanged(new ChangeEvent(fb.getDocument().getText(0, fb.getDocument().getLength())));
            } else if ( value < this.minValue) {
                super.replace(fb, offset, length -1, "", attrs);
                this.changeListener.stateChanged(new ChangeEvent(fb.getDocument().getText(0, fb.getDocument().getLength())));
            } else {
                this.changeListener.stateChanged(new ChangeEvent(value));
            }
        } catch (Exception ignored) {
            // this will only trigger if an unwanted key is pressed i.e. not a numeric 0 - 9
        }
    }
}
