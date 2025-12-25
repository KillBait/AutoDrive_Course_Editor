package AutoDriveEditor.Classes.UI_Components;

import AutoDriveEditor.Classes.CircularList;
import AutoDriveEditor.Managers.IconManager;
import com.formdev.flatlaf.extras.FlatSVGIcon;

import javax.swing.*;
import java.awt.event.ActionListener;
import java.util.Iterator;

import static AutoDriveEditor.Classes.Util_Classes.LoggerUtils.LOG;
import static AutoDriveEditor.Locale.LocaleManager.getLocaleString;
import static AutoDriveEditor.Managers.IconManager.getSVGIcon;

/**
 * A custom toggle button that can cycle through a list of states, each with its own icon and tooltip.
 */
@SuppressWarnings("unused")
public class JToggleStateButton extends JToggleButton {

    private final CircularList<ButtonSelectionState> stateList = new CircularList<>();
    private final ScaleAnimIcon icon;

    /**
     * Constructs a JToggleStateButton with the specified parameters.
     *
     * @param icon         the initial icon for the button
     * @param panel        the panel to which this button will be added
     * @param toolTipText  the tooltip text for the button
     * @param altText      the accessible description for the button
     * @param isSelected   the initial selected state of the button
     * @param enabled      the initial enabled state of the button
     * @param actionListener the action listener for the button
     */
    public JToggleStateButton(ScaleAnimIcon icon, JPanel panel, String toolTipText, String altText, boolean isSelected, boolean enabled, ActionListener actionListener) {
        super();
        //
        // we need to keep a reference to the AnimatedIcon so we can trigger the animation
        // also we need the reference to later change the icon as required or else the
        // animation will not work @see swapIcon()
        //
        this.icon = icon;
        this.setIcon(icon);
        if (toolTipText != null) this.setToolTipText(getLocaleString(toolTipText));
        if (altText != null) this.getAccessibleContext().setAccessibleDescription(getLocaleString(altText));
        this.setFocusable(false);
        this.setEnabled(enabled);
        this.setSelected(isSelected);
        this.addActionListener(e -> icon.startAnimation(this));
        panel.add(this);

    }

    /**
     * Adds a new state to the button.
     *
     * @param iconName     the string identifier ( see IconManager.java ) for the state icon
     * @param stateName    the name of the state
     * @param tooltip      string description for the state
     * @see IconManager
     */
    public void addState(String iconName, String stateName, String tooltip) {
        FlatSVGIcon icon = getSVGIcon(iconName);
        stateList.add(new ButtonSelectionState(icon, stateName, tooltip));
    }

    /**
     * Adds a new state to the button.
     *
     * @param newState     the state to add to list
     */
    public void addState(ButtonSelectionState newState) {
        stateList.add(newState);
    }

    /**
     * Removes a state from the circularList by name.
     *
     * @param name the name of the state to be removed
     */
    public void removeState(String name) {
        Iterator<ButtonSelectionState> iterator = stateList.iterator();
        while (iterator.hasNext()) {
            ButtonSelectionState state = iterator.next();
            if (state.getName().equals(name)) {
                iterator.remove();
            }
        }
    }

    /**
     * Advances to the next state.
     */
    public void nextState() {
        if (!stateList.isEmpty()) {
            // we have to directly set the icon or the animation will not work
            FlatSVGIcon nextIcon = stateList.get().getIcon();
            if (nextIcon != null) {
                icon.setIcon(nextIcon);
            } else {
                LOG.info("Icon is null for state: {}", stateList.get().getName());
            }
            stateList.next();
            repaint();
        }
    }

    /**
     * Returns the name of the current state.
     *
     * @return the name of the current state
     */
    public String getCurrentStateName() {
        return (stateList.isEmpty()) ? "" : stateList.get().getName();
    }

    /**
     * Returns the current tooltip of the current state.
     *
     * @return the name of the current state
     */
    public String getCurrentStateTooltip() {
        return (stateList.isEmpty()) ? "" : stateList.get().getTooltip();
    }
}
