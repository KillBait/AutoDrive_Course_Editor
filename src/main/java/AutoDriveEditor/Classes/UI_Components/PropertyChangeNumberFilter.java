package AutoDriveEditor.Classes.UI_Components;

import javax.swing.text.AttributeSet;
import javax.swing.text.BadLocationException;
import javax.swing.text.DocumentFilter;
import java.beans.PropertyChangeListener;
import java.beans.PropertyChangeSupport;

/**
 * A custom DocumentFilter that validates and filters numeric input.
 * It supports validation for min/max values, decimal places, and negative numbers.
 */
@SuppressWarnings("unused")
public class PropertyChangeNumberFilter extends DocumentFilter {

    private final double maxValue;
    private final double minValue;
    private final boolean allowDecimalPlaces;
    private final boolean canBeNegative;
    private final int maxDecimalPlaces;
    private final PropertyChangeSupport pcs;
    private String errorText;

    /**
     * Constructs a PropertyChangeNumberFilter with the specified parameters.
     *
     * @param min               the minimum allowed value
     * @param max               the maximum allowed value
     * @param allowDecimalPlaces if decimal places are allowed
     * @param maxDecimalPlaces  the maximum number of decimal places allowed
     * @param canBeNegative     whether negative numbers are allowed
     */
    public PropertyChangeNumberFilter(double min, double max, boolean allowDecimalPlaces, int maxDecimalPlaces, boolean canBeNegative) {
        super();
        this.minValue = min;
        this.maxValue = max;
        this.allowDecimalPlaces = allowDecimalPlaces;
        this.canBeNegative = canBeNegative;
        this.maxDecimalPlaces = (allowDecimalPlaces && maxDecimalPlaces == 0) ? 1 : maxDecimalPlaces;
        this.pcs = new PropertyChangeSupport(this);
        this.errorText = "";
    }

    /**
     * Adds a PropertyChangeListener to the listener list.
     *
     * @param listener the PropertyChangeListener to be added
     */
    public void addPropertyChangeListener(PropertyChangeListener listener) {
        pcs.addPropertyChangeListener(listener);
    }

    /**
     * Removes a PropertyChangeListener from the listener list.
     *
     * @param listener the PropertyChangeListener to be removed
     */
    public void removePropertyChangeListener(PropertyChangeListener listener) {
        pcs.removePropertyChangeListener(listener);
    }

    /**
     * Returns the current error text.
     *
     * @return the current error text
     */
    public String getErrorText() {
        return errorText;
    }

    /**
     * Sets the error text and fires a property change event.
     *
     * @param errorText the error text to set
     */
    private void setErrorText(String errorText) {
        String property = (errorText.isEmpty()) ? "valid" : errorText;
        pcs.firePropertyChange(property, null, errorText);
    }

    /**
     * Validates the input text.
     *
     * @param text the input text to validate
     * @return true if the input is valid, false otherwise
     */
    private boolean isValidInput(String text) {
        try {
            if (text.isEmpty()) return true;

            double value = Double.parseDouble(text);
            if (value > this.maxValue) {
                errorText = "Value cannot be bigger than " + maxValue;
                return false;
            } else if (value < this.minValue) {
                errorText = "Value cannot be lower than " + minValue;
                return false;
            }

            if (text.equals(".")) {
                errorText = "Invalid Decimal Point";
                return false;
            }

            if (text.contains("f") || text.contains("d")) {
                errorText = "Invalid Float/Decimal Literal ( f or d )";
                return false;
            }

            if (Double.parseDouble(text) < 0 && !this.canBeNegative) {
                errorText = "Negative numbers are not allowed.";
                return false;
            }

            if (text.contains(".")) {
                if (this.allowDecimalPlaces) {
                    try {
                        String[] parts = text.split("\\.");
                        if (parts[1].length() > maxDecimalPlaces) {
                            errorText = "Number exceeds max decimal places";
                            return false;
                        } else {
                            return true;
                        }
                    } catch (IndexOutOfBoundsException e) {
                        // exception triggered only when text string contains a decimal point and no number after it,
                        // we want to allow this as the user may be in the process of typing a decimal number
                        return true;
                    }
                } else {
                    errorText = "Decimal places are not allowed.";
                    return false;
                }
            }
            return true;
        } catch (NumberFormatException e) {
            errorText = "Invalid Double Value";
            setErrorText(errorText);
            return false;
        }
    }

    /**
     * Inserts a string into the document after validating the input.
     *
     * @param fb     the FilterBypass that can be used to mutate the Document
     * @param offset the offset into the document to insert the content >= 0
     * @param string the string to insert
     * @param attr   the attributes to associate with the inserted content
     */
    @Override
    public void insertString(FilterBypass fb, int offset, String string, AttributeSet attr) {
        try {
            String currentText = fb.getDocument().getText(0, fb.getDocument().getLength());
            String newText = new StringBuilder(currentText).insert(offset, string).toString();

            if (isValidInput(newText)) {
                super.insertString(fb, offset, string, attr);
                setErrorText("");
            } else {
                setErrorText(errorText);
            }

        } catch (Exception e) {
            errorText = "insertString:- Invalid String ( " + string + " )";
            setErrorText(errorText);
        }
    }

    /**
     * Replaces a portion of the document with the given text after validating the input.
     *
     * @param fb     the FilterBypass that can be used to mutate the Document
     * @param offset the offset into the document to replace the content >= 0
     * @param length the length of the text to delete >= 0
     * @param text   the text to insert, null indicates no text to insert
     * @param attrs  the attributes to associate with the inserted content, this may be null to indicate no attributes
     */
    @Override
    public void replace(FilterBypass fb, int offset, int length, String text, AttributeSet attrs) throws BadLocationException {
        try {
            String currentText = fb.getDocument().getText(0, fb.getDocument().getLength());
            String newText;
            newText = new StringBuilder(currentText).replace(offset, offset + length, text).toString();
            if (isValidInput(newText)) {
                if (text.equals("-0.0")) text = "0.00";
                super.replace(fb, offset, length, text, attrs);
                setErrorText("");
            } else {
                setErrorText(errorText);
            }
        } catch (Exception e) {
            errorText = "replace:- Character '" + text + "' is invalid";
            setErrorText(errorText);
        }
    }

    @Override
    public void remove(FilterBypass fb, int offset, int length) throws BadLocationException {
        try {
            String currentText = fb.getDocument().getText(0, fb.getDocument().getLength());
            String newText = new StringBuilder(currentText).delete(offset, offset + length).toString();
            if (isValidInput(newText)) {
                super.remove(fb, offset, length);
                setErrorText("");
            } else {
                setErrorText(errorText);
            }
        } catch (Exception e) {
                errorText = "removeOriginalNodes:- Invalid String ( " + e.getMessage() + " )";
                setErrorText(errorText);
        }
    }
}
