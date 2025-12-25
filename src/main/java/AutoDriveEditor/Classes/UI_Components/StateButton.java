package AutoDriveEditor.Classes.UI_Components;

import AutoDriveEditor.Classes.CircularList;

import javax.swing.*;
import java.awt.event.ActionListener;
import java.util.Iterator;

import static AutoDriveEditor.Locale.LocaleManager.getLocaleString;

/**
 * A custom toggle button that can cycle through a list of states, each with its own icon and tooltip.
 */
@SuppressWarnings("unused")
public class StateButton extends JToggleButton {

    private final CircularList<SelectionState> stateList = new CircularList<>();
    //private ScaleAnimIcon icon;

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
    public StateButton(ScaleAnimIcon icon, JPanel panel, String toolTipText, String altText, boolean isSelected, boolean enabled, ActionListener actionListener) {
        super();
        //
        // we need to keep a reference to the AnimatedIcon so we can trigger the animation
        // also we need the reference to later change the icon as required or else the
        // animation will not work @see swapIcon()
        //
        //this.icon = icon;
        this.setIcon(icon);
        if (toolTipText != null) this.setToolTipText(getLocaleString(toolTipText));
        if (altText != null) this.getAccessibleContext().setAccessibleDescription(getLocaleString(altText));
        this.setFocusable(false);
        this.setEnabled(enabled);
        this.setSelected(isSelected);
        this.addActionListener(actionListener);
        this.addActionListener(e -> {
            Icon currentIcon = this.getIcon();
            if (currentIcon instanceof ScaleAnimIcon) {
                ((ScaleAnimIcon) currentIcon).startAnimation(this);
            }});
        panel.add(this);
        addState("DEFAULT", icon, toolTipText, altText);
        stateList.setCurrentIndex(0);

    }

    /**
     * Swaps the icon and tooltip of the button to the next state in the list.
     */
    public void swapIcon(){
        if (!stateList.isEmpty()) {
            // we have to directly set the icon or the animation will not work
            SelectionState newState = stateList.next();
            newState.getIcon().setSelected(isSelected());
            this.setIcon(newState.getIcon());
            //stateList.next();
            this.repaint();
        }
    }

    /**
     * Adds a new state to the button.
     *
     * @param name         the name of the state
     * @param icon         the icon for the state
     * @param toolTipText  the tooltip text for the state
     * @param altText      the accessible description for the state
     */
    public void addState(String name, ScaleAnimIcon icon, String toolTipText, String altText) {
        stateList.add(new SelectionState(name, icon, toolTipText, altText));
        //if (stateList.getSize() == 1) stateList.next();
    }

    /**
     * Removes a state from the circularList by name.
     *
     * @param name the name of the state to be removed
     */
    public void removeState(String name) {
        Iterator<SelectionState> iterator = stateList.iterator();
        while (iterator.hasNext()) {
            SelectionState state = iterator.next();
            if (state.getName().equals(name)) {
                iterator.remove();
            }
        }
    }

    /**
     * Advances to the next state and returns its name.
     *
     * @return the name of the next state
     */
    public String nextStateName() {
        stateList.next();
        return stateList.get().getName();
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
     * Returns the tooltip text of the current state.
     *
     * @return tooltip text of current state
     */
    public String getCurrentStateToolTip() {
        return (stateList.isEmpty()) ? "" : stateList.get().getToolTipText();
    }

    /**
     * Returns the accessible description of the current state.
     *
     * @return accessible description of current state
     */
    public String getCurrentStateAltText() {
        return (stateList.isEmpty()) ? "" : stateList.get().getAltText();
    }

    private static class SelectionState {

        private final String name;
        private final ScaleAnimIcon icon;
        private final String toolTipText;
        private final String altText;


        /**
         * Constructs a State with the specified parameters.
         *
         * @param name         the name of the state
         * @param icon         the icon for the state
         * @param toolTipText  the tooltip text for the state
         * @param altText      the accessible description for the state
         */
        public SelectionState(String name, ScaleAnimIcon icon, String toolTipText, String altText) {
            this.name = name;
            this.icon = icon;
            this.toolTipText = toolTipText;
            this.altText = altText;
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
         * @return the icon of the state
         */
        public ScaleAnimIcon getIcon() {
            return icon;
        }

        /**
         * Returns the tooltip text of the state.
         *
         * @return the tooltip text of the state
         */
        public String getToolTipText() {
            return toolTipText;
        }

        /**
         * Returns the accessible description of the state.
         *
         * @return the accessible description of the state
         */
        public String getAltText() {
            return altText;
        }
    }
}
