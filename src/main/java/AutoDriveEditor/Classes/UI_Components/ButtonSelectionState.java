package AutoDriveEditor.Classes.UI_Components;

import com.formdev.flatlaf.extras.FlatSVGIcon;

/**
 * Represents a state with a name, icon, tooltip text, and accessible description.
 */
public class ButtonSelectionState {

    private final FlatSVGIcon icon;
    private final String name;
    private final String tooltipText;

    /**
     * Constructs a State with the specified parameters.
     *
     * @param name         the name of the state
     * @param icon         the icon for the state
     */

    public ButtonSelectionState(FlatSVGIcon icon, String name, String tooltipText) {
        this.icon = icon;
        this.name = name;
        this.tooltipText = tooltipText;
    }

    /**
     * Returns the name of the state.
     *
     * @return the name of the state
     */
    public String getName() {
        return name;
    }

    /**
     * Returns the icon of the state.
     *
     * @return FlatSVGIcon for the current state
     */
    public FlatSVGIcon getIcon() {
        return icon;
    }

    /**
     * Returns the tooltip of the state.
     *
     * @return tooltip in String format
     */
    public String getTooltip() {
        return tooltipText;
    }
}
