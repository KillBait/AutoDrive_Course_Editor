package AutoDriveEditor.Classes.UI_Components.HeaderList;

import javax.swing.*;
import java.awt.*;

/**
 * Represents an entry in the header list, which can be a regular entry, a header, a separator, or an empty space.
 * Created 14/09/2024 by KillBait!
 */

@SuppressWarnings("unused")
public class HeaderListEntry {
    private Icon icon;
    private Color colorIcon;
    private final String displayString;
    private final String value;
    private final boolean isHeader;
    private final boolean isSeparator;
    private final boolean isSpacer;
    private final int spacerHeight;
    private final Color separatorColor;
    private final Color labelColor;
    private final boolean bold;
    private final boolean italic;

    /**
     * Construct an empty space entry, with default height of 10.
     */
    public HeaderListEntry() {
        this(null, null ,null, null, false, false, null, false, false, null, true, 10);
    }

    /**
     * Constructs an empty space entry, with specified height.
     */
    public HeaderListEntry(int height) {
        this(null, null, null, null, false, false, null, false, false, null, true, height);
    }

    /**
     * Constructs a regular list entry with a display string and a value.
     *
     * @param displayString the text to display for the entry
     * @param value the value associated with the entry
     */
    public HeaderListEntry(String displayString, String value) {
        this(null, null, displayString, value, false, false, Color.BLACK, false, false, Color.BLACK, false, 0);
    }

    /**
     * Constructs a header entry with a display string.
     *
     * @param displayString the text to display for the header
     * @param isHeader whether this entry is a header
     */
    public HeaderListEntry(String displayString, boolean isHeader) {
        this(null, null ,displayString, null, isHeader, false, Color.BLACK, false, false, Color.BLACK, false , 0);
    }

    /**
     * Constructs a header entry with a display string, style, and color.
     *
     * @param displayString the text to display for the header
     * @param isHeader whether this entry is a header
     * @param boldText whether the header text should be bold
     * @param italicText whether the header text should be italic
     * @param color the color of the header text
     */
    public HeaderListEntry(String displayString, boolean isHeader, boolean boldText, boolean italicText, Color color) {
        this(null, null ,displayString, null, isHeader, false, color, boldText, italicText, Color.BLACK, false, 0);
    }

    /**
     * Constructs a separator entry with a display string, style, and colors.
     *
     * @param displayString the text to display for the separator
     * @param stringColor the color of the separator text
     * @param bold whether the separator text should be bold
     * @param italic whether the separator text should be italic
     * @param separatorColor the color of the separator line
     */
    public HeaderListEntry(String displayString, Color stringColor, boolean bold, boolean italic, Color separatorColor) {
        this(null, null, displayString, null, false, true, stringColor, bold, italic, separatorColor, false, 0);
    }

    /**
     * Constructs a list entry with an icon display to the left of the string.
     *
     * @param icon the icon to display for the entry
     * @param displayString the text to display for the entry
     * @param value the value associated with the entry
     */
    public HeaderListEntry(Icon icon, String displayString, String value) {
        this(icon, null, displayString, value, false, false, null, false, false, null, false, 0);
    }

    /**
     * Constructs a list entry with a color icon display to the left of the string.
     *
     * @param color the color of the icon
     * @param displayString the text to display for the entry
     * @param value the value associated with the entry
     */
    public HeaderListEntry(Color color, String displayString, String value) {
        this(null, color, displayString, value, false, false, null, false, false, null, false, 0);
    }

    // full constructor, not directly usable, but can be used by other functions to createSetting a new entry
    private HeaderListEntry(Icon icon, Color colour, String displayString, String value, boolean isHeader, boolean isSeparator, Color labelColor, boolean bold, boolean italic, Color separatorColor, boolean isSpacer, int spacerHeight) {
        this.icon = icon;
        this.colorIcon = colour;
        this.displayString = displayString;
        this.value = value;
        this.isHeader = isHeader;
        this.isSeparator = isSeparator;
        this.isSpacer = isSpacer;
        this.spacerHeight = spacerHeight;
        this.separatorColor = separatorColor;
        this.labelColor = labelColor;
        this.bold = bold;
        this.italic = italic;
    }

    //
    // Getters
    //

    /**
     * Gets the display string of the entry.
     *
     * @return the display string
     */
    public String getDisplayString() {
        return displayString;
    }

    /**
     * Gets the value associated with the entry.
     *
     * @return the value, or null if there is no value
     */
    public String getValue() {
        return value;
    }

    /**
     * Gets the color of the separator line.
     *
     * @return the separator color
     */
    public Color getSeparatorColor() {
        return separatorColor;
    }

    /**
     * Gets the color of the label text.
     *
     * @return the label color
     */
    public Color getTextColor() {
        return labelColor;
    }

    /**
     * returns the specified height of the spacer.
     *
     * @return spacer height
     */
    public int getSpacerHeight() {
        return spacerHeight;
    }

    /**
     * Gets the icon of the entry.
     *
     * @return the icon
     */
    public Icon getIcon() {
        return icon;
    }

    /**
     * Gets the color of the icon.
     *
     * @return the color of the icon
     */
    public Color getIconColor() {
        return colorIcon;
    }

    /**
     * Checks if the entry has an icon.
     *
     * @return true if the entry has an icon
     */
    public boolean isIconEntry() {
        return icon != null;
    }

    /**
     * Checks if the entry has a colour icon.
     *
     * @return true if the entry has a colour icon
     */
    public boolean isColorEntry() {
        return colorIcon != null;
    }

    /**
     * Checks if the text is bold.
     *
     * @return true if the text is bold, false otherwise
     */
    public boolean isBold() {
        return bold;
    }

    /**
     * Checks if the text is italic.
     *
     * @return true if the text is italic, false otherwise
     */
    public boolean isItalic() {
        return italic;
    }

    /**
     * Checks if the entry is a spacer.
     *
     * @return true if the entry is a spacer, false otherwise
     */
    public boolean isSpacer() {
        return isSpacer;
    }

    /**
     * Checks if the entry is a header.
     *
     * @return true if the entry is a header, false otherwise
     */
    public boolean isHeader() {
        return isHeader;
    }

    /**
     * Checks if the entry is a separator.
     *
     * @return true if the entry is a separator, false otherwise
     */
    public boolean isSeparator() {
        return isSeparator;
    }

    /**
     * Checks if the entry is Valid i.e not a spacer/separator/header text.
     *
     * @return true if the entry is a separator, false otherwise
     */
    public boolean isValidEntry() {
        return !isSpacer && !isSeparator && !isHeader;
    }

    //
    // Setters
    //

    /**
     * Sets the icon of the entry.
     *
     * @param icon the icon
     */
    public void setIcon(Icon icon) {
        this.icon = icon;
    }

    /**
     * Sets the color of the icon.
     *
     * @param color the color of the icon
     */
    public void setIconColor(Color color) {
        this.colorIcon = color;
    }
}