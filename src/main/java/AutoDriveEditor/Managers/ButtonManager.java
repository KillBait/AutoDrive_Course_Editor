package AutoDriveEditor.Managers;

import AutoDriveEditor.GUI.Buttons.BaseButton;
import AutoDriveEditor.RoadNetwork.MapNode;

import javax.swing.*;
import java.awt.*;
import java.awt.event.ActionEvent;
import java.awt.event.MouseEvent;
import java.awt.event.MouseWheelEvent;
import java.util.ArrayList;
import java.util.List;
import java.util.Objects;

import static AutoDriveEditor.AutoDriveEditor.curveManager;
import static AutoDriveEditor.Classes.Util_Classes.LoggerUtils.LOG;
import static AutoDriveEditor.GUI.Menus.DebugMenu.Logging.LogButtonManagerInfoMenu.bDebugLogButtonManagerInfo;
import static AutoDriveEditor.GUI.Menus.DebugMenu.Logging.LogMultiSelectManagerInfoMenu.bDebugLogMultiSelectManagerInfo;
import static AutoDriveEditor.Managers.MultiSelectManager.multiSelectList;

@SuppressWarnings("unused")
public class ButtonManager implements MultiSelectManager.MultiSelectEventListener {

    /**
     * ToolTipBuilder - Interface for creating tooltips
     * <br><br>
     * Any class implementing this interface and @Overriding the
     * buildToolTip() function can use it to easily set and updateVisibility
     * the tooltip text for any button.
     */
    public interface ToolTipBuilder {
        String buildToolTip();
    }

    /**
     * ButtonDefaults - Interface for creating buttons
     * <br><br>
     * Default values for all buttons are set in this interface,
     * you can override any of them as needed
     */
    public interface ButtonInterface {

        /**
         * sets a unique ID (name) for your button
         * @return string containing your unique name
         */
        String getButtonID();

        /**
         * sets a name for this buttons action, you can assign multiple
         * buttons the same name to effectively createSetting a group and use
         * getActionGroup("your_group_name") to get an ArrayList of all
         * buttons assigned that name.
         * @return string containing your unique name
         */
        String getButtonAction();

        /**
         * sets a name for this buttons panel, you can assign multiple
         * buttons the same name to effectively createSetting a group and use
         * getPanelGroup("your_panel_name") to get an ArrayList of all
         * buttons assigned that name.
         * @return string containing your unique name
         */

        String getButtonPanel();

        /**
         * sets a string that will be displayed in the InfoPanel when
         * the button is selected
         * @return string containing your information text
         */
        String getInfoText();

        /**
         * ignoreButtonDeselect() - if you don't want the button to de-selected
         * when any other buttons are selected, useful for making buttons that
         * are always available ( i.e. a button to open a settings window ).
         * -- Default: false
         * @return true/false
         * @
         */
        Boolean ignoreButtonDeselect();

        /**
         * useMultiSelection() - return true/false if you want the button to
         * use the multi selection feature with the right mouse/drag
         * // -- Default: false
         * @return true/false
         */
        Boolean useMultiSelection();

        /**
         * usePanelEdgeScrolling() - return true/false if you want the button to
         * enable scrolling of the mao when the mouse pointer is at the edges of
         * the MapPanel.
         * // -- Default: false
         * @return true/false
         */
        Boolean usePanelEdgeScrolling();

        /**
         * addSelectedToMultiSelectList() - Only works if useMultiSelection() is true
         * <br>
         * return true/false if you want all the nodes selected while dragging to be
         * added to the multiSelect list, if false, the selected nodes will not be
         * added to the global list, you can then use getSelectionNodes() to get an
         * ArrayList of the selected nodes only
         * // -- Default: true
         * @return true/false
         */
        Boolean addSelectedToMultiSelectList();

        /**
         * alwaysSelectHidden() - Only works if useMultiSelection() is true
         * <br>
         * When multi-selecting the default behaviour is it will ignore any hidden
         * nodes, you can toggle bSelectHidden to override this behaviour, if you want
         * to make sure everything is always selected regardless of what settings are
         * used, set this to true
         * // -- Default: false
         * @return true/false
         */
        Boolean alwaysSelectHidden();

        /**
         * ignoreDeselect() -  - Only works if useMultiSelection() is true
         * return true/false is you want to ignore the right mouse double click
         * function to clear the multiSelect list.
         *
         * @return true/false
         */
        Boolean ignoreDeselect();

        /**
         * showHoverNodeSelect() - return true/false if you want the selection
         * indicator to show when you hover over a node
         * // -- Default: true
         * @return true/false
         */
        Boolean showHoverNodeSelect();

        /**
         * previewNodeSelectionChange() - Only works if useMultiSelection() is true
         * <br>
         * return true/false if you want to preview the selected status of the nodes
         * returned while Multi-Select dragging
         * // -- Default: true
         * @return true/false
         */
        Boolean previewNodeSelectionChange();

        /**
         * previewNodeHiddenChange() - Only works if useMultiSelection() is true
         * <br>
         * return true/false if you want to preview the hidden status of nodes
         * returned while Multi Select dragging.
         * // -- Default: false
         * @return true/false
         */
        Boolean previewNodeHiddenChange();

        /**
         * previewNodeFlagChange() - Only works if useMultiSelection() is true
         * <br>
         * return true/false if you want to preview the flag (priority) change
         * of nodes returned while Multi Select dragging.
         * // -- Default: false
         * @return true/false
         */
        Boolean previewNodeFlagChange();

        /**
         * previewConnectionHiddenChange() - Only works if useMultiSelection() is true
         * <br>
         * return true/false if you want to preview the hidden status
         * of nodes returned while Multi Select dragging.
         * // -- Default: false
         * @return true/false
         */
        Boolean previewConnectionHiddenChange();

        /**
         * getLineDetectionInterval() - Used by ConnectSelectBaseButton only
         * <br><br>
         * Only used when multi selection is active, sets the number of
         * subdivisions along the distance between two nodes will be checked
         * to see if it's within the selection area.
         * <br><br>
         * NOTE:- Small values are more CPU intensive
         * Default: 10
         * @return Integer
         */
        Integer getLineDetectionInterval();

        /**
         * sets a unique reference for this button.
         * -- see ButtonManager.addButton() --
         */
        void setNode(ButtonNode buttonNode);

        /**
         * sets if this button is enabled
         */
        void setEnabled(boolean enabled);

        /**
         * sets if this button is selected
         */
        void setSelected(boolean selected);

        /**
         * sets if this button is visible
         */
        void setVisible(boolean visible);

        /**
         * override if you want to be informed when the button is interacted with
         */
        void actionPerformed(ActionEvent e);

        /**
         * override any of the below functions to be informed of mouse or activation events
         */

        void mouseClicked(MouseEvent e);
        void mousePressed(MouseEvent e);
        void mouseReleased(MouseEvent e);
        void mouseDragged(MouseEvent e);
        void mouseMoved(MouseEvent e);
        void mouseEntered(MouseEvent e);
        void mouseExited(MouseEvent e);
        void mouseWheelMoved(MouseWheelEvent e);

        /**
         * if a button specifies useMultiSelection() == true
         * override any of the below functions to be informed of multiselect events
         */
        void onMultiSelectStart();
        void onMultiSelectStop();
        void onMultiSelectChange(ArrayList<MapNode> nodeList);
        void onMultiSelectAdd(ArrayList<MapNode> addedNodes);
        void onMultiSelectRemove(ArrayList<MapNode> removedNodes);
        void onMultiSelectOneTime(ArrayList<MapNode> oneTimeList);
        void onMultiSelectCleared();

        /**
         * Override this function if you need to do anything after the button is created
         */
        void onButtonCreation();
        /**
         * Override this function if you need to do anything when the button is selected
         */
        void onButtonSelect();
        /**
         * Override this function if you need to do anything after the button is deselected
         */
        void onButtonDeselect();
        /**
         * Override this function if you need to do anything after a new game config is loaded
         */
        void onConfigChange();

        /**
         * Override this function if you need to do anything on the screen
         * @param g graphics reference for drawing
         */
        void drawToScreen(Graphics g);

        AbstractButton getButton();
    }

    public static class ButtonNode {
        public final ButtonInterface buttonInterface;

        public ButtonNode(ButtonInterface b){
            buttonInterface = b;
        }
    }

    private static final List<ButtonNode> buttonList = new ArrayList<>();
    public static ButtonNode currentButton;

    public ButtonManager() {
        LOG.info("  Initializing ButtonManager");
        MultiSelectManager.addMultiSelectEventListener(this);
    }

    /**
     * addButton() - add a button to the ButtonManager
     * @param toAdd - ButtonDefaults object
     * @return ButtonDefaults object
     */
    @SuppressWarnings("UnusedReturnValue")
    public ButtonNode addButton(ButtonInterface toAdd) {
        // Check if a button with the same ID is already in the button list
        for (ButtonNode buttonNode : buttonList) {
            if (buttonNode.buttonInterface.getButtonID().equals(toAdd.getButtonID())) {
                LOG.error("## ButtonManager.addButton() ## WARNING: Button with duplicate ID '{}' Found,", toAdd.getButtonID());
                return null;
            }
        }
        // Create a new ButtonNode and add it to the button list
        ButtonNode buttonNode = new ButtonNode(toAdd);
        buttonNode.buttonInterface.setNode(buttonNode);
        buttonList.add(buttonNode);
        if (bDebugLogButtonManagerInfo) LOG.info("## ButtonManager.addButton() ## Adding button {}",buttonNode.buttonInterface.getButtonID());
        buttonNode.buttonInterface.onButtonCreation();
        return buttonNode;

    }

    /**
     * disableAllButtons() - disable all buttons
     */
    @SuppressWarnings("unused")
    public void disableAllButtons() { setAllButtonsEnabled(false); }

    /**
     * enableAllButtons() - enable all buttons
     */
    public void enableAllButtons() {
        setAllButtonsEnabled(true);
    }

    /**
     * setButton() - enable/disable a button by it's ButtonID
     * @param buttonID - ID of the button to enable/disable
     * @param enabled - true/false to enable/disable the button
     */
    @SuppressWarnings("unused")
    public void setButton(String buttonID, boolean enabled) {
        for (ButtonNode buttonNode : buttonList) {
            if (buttonNode.buttonInterface.getButtonID().equals(buttonID)) {
                buttonNode.buttonInterface.setEnabled(enabled);
                if (bDebugLogButtonManagerInfo) LOG.info("## ButtonManager.setButton() ## Enabling button {}",buttonNode.buttonInterface.getButtonID());

            }
        }

    }

    /**
     * setAllButtonsEnabled()
     * Used by the ButtonManager to enable/disable all buttons
     * @param enabled - true/false to enable/disable all buttons
     */
    private void setAllButtonsEnabled(boolean enabled) {
        for (ButtonNode buttonNode : buttonList) {
            buttonNode.buttonInterface.setEnabled(enabled);
        }
        if (bDebugLogButtonManagerInfo) LOG.info("## ButtonManager.setAllButtonsEnabled() ## Setting all buttons enabled");

    }

    /**
     * makeCurrent() - set the specified button as the current button
     * @param currentNode - ButtonNode to set as current
     */
    public void makeCurrent(ButtonNode currentNode) {
        if (currentNode != null) {
            if (currentButton != null) {
                currentButton.buttonInterface.onButtonDeselect();
                curveManager.onButtonDeselected(currentButton.buttonInterface);
            }
            currentButton = currentNode;
            for (ButtonNode buttonNode : buttonList) {
                if (buttonNode == currentNode) {
                    if (bDebugLogButtonManagerInfo) LOG.info("## ButtonManager.makeCurrent() ##  Making button '{}' selected",buttonNode.buttonInterface.getButtonID());
                    buttonNode.buttonInterface.setSelected(true);
                    buttonNode.buttonInterface.onButtonSelect();
                    curveManager.onButtonSelected(buttonNode.buttonInterface);

                } else {
                    buttonNode.buttonInterface.setSelected(false);
                    buttonNode.buttonInterface.onButtonDeselect();
                }

            }
        }
    }

    /**
     * deSelectAll() - de-select all buttons
     * triggers onButtonDeselect() for every button the ButtonManager manages
     */
    public void deSelectAll() {
        for (ButtonNode buttonNode : buttonList) {
            buttonNode.buttonInterface.setSelected(false);
            buttonNode.buttonInterface.onButtonDeselect();
            curveManager.onButtonDeselected(buttonNode.buttonInterface);
        }
        currentButton = null;
        if (bDebugLogButtonManagerInfo) LOG.info("## ButtonManager.deSelectAll() ##  Deselecting all");

    }

    /**
     * onConfigChange() - trigger onConfigChange() for every buttons
     */
    public void onConfigChange() {
        for (ButtonNode buttonNode : buttonList) {
            buttonNode.buttonInterface.onConfigChange();
        }
    }

    /**
     * getCurrentButton() - get the currently selected button
     * @return ButtonDefaults object of the current button, null if none
     * */
    public ButtonInterface getCurrentButton() {
        if (currentButton != null) {
            return currentButton.buttonInterface;
        } else {
            return null;
        }
    }

    public static ButtonInterface getButton(BaseButton button) {
        return currentButton.buttonInterface;
    }

    public static BaseButton getButton(AbstractButton button) {
        for (ButtonNode buttonNode : ButtonManager.buttonList) {
            if (buttonNode.buttonInterface.getButton() == button) {
                return (BaseButton) buttonNode.buttonInterface;
            }
        }
        return null;
    }

    /**
     * getCurrentButtonID() - get the ID of the currently selected button
     * @return String containing the ID of the current button, "None" if none
     */
    public String getCurrentButtonID() {
        if (currentButton != null && currentButton.buttonInterface.getButtonID() != null) {
            return currentButton.buttonInterface.getButtonID();
        } else {
            return "None";
        }
    }

    /**
     * getCurrentButtonAction() - get the action of the currently selected button
     * @return String containing the action of the current button, "None" if none
     */
    public String getCurrentButtonAction() {
        if (currentButton != null && currentButton.buttonInterface.getButtonAction() != null) {
            return currentButton.buttonInterface.getButtonAction();
        } else {
            return "None";
        }
    }

    /**
     * getCurrentButtonPanel() - get the panel of the currently selected button
     * @return String containing the panel of the current button, "None" if none
     */
    public String getCurrentButtonPanel() {
        if (currentButton != null && currentButton.buttonInterface.getButtonPanel() != null) {
            return currentButton.buttonInterface.getButtonPanel();
        } else {
            return "None";
        }
    }

    /**
     * getActionGroup() - get a list of buttons with the same action name
     * @param actionName - String containing the action name to search for
     * @return ArrayList of ButtonDefaults objects with the same action name
     */
    public ArrayList<ButtonInterface> getActionGroup(String actionName) {
        ArrayList<ButtonInterface> matchList = new ArrayList<>();
        for (ButtonNode buttonNode : buttonList) {
            if (Objects.equals(buttonNode.buttonInterface.getButtonAction(), actionName)) {
                matchList.add(buttonNode.buttonInterface);
            }
        }
        return matchList;
    }

    /**
     * getPanelGroup() - get a list of buttons with the same panel name
     * @param panelName - String containing the panel name to search for
     * @return ArrayList of ButtonDefaults objects with the same panel name
     */
    public ArrayList<ButtonInterface> getPanelGroup(String panelName) {
        ArrayList<ButtonInterface> matchList = new ArrayList<>();
        for (ButtonNode buttonNode : buttonList) {
            if (Objects.equals(buttonNode.buttonInterface.getButtonPanel(), panelName)) {
                matchList.add(buttonNode.buttonInterface);
            }
        }
        return matchList;
    }

    /**
     * isSelected() - check if the specified button is selected
     * @param node ButtonNode object of the button to check
     * @return boolean true/false if the button is selected
     */
    public Boolean isSelected(BaseButton node) { return getCurrentButton() == node; }

    //
    // you can @Override any of the mouse functions in your buttons class, if you need
    // to use them.
    //

    public void mouseClicked(MouseEvent e) { if (currentButton != null) currentButton.buttonInterface.mouseClicked(e); }
    public void mousePressed(MouseEvent e) { if (currentButton != null) currentButton.buttonInterface.mousePressed(e); }
    public void mouseReleased(MouseEvent e) { if (currentButton != null) currentButton.buttonInterface.mouseReleased(e); }
    public void mouseDragged(MouseEvent e) { if (currentButton != null) currentButton.buttonInterface.mouseDragged(e); }
    public void mouseMoved(MouseEvent e) { if (currentButton != null) currentButton.buttonInterface.mouseMoved(e); }
    public void mouseEntered(MouseEvent e) { if (currentButton != null) currentButton.buttonInterface.mouseEntered(e); }
    public void mouseExited(MouseEvent e) { if (currentButton != null) currentButton.buttonInterface.mouseExited(e); }
    public void mouseWheelMoved(MouseWheelEvent e) { if (currentButton != null) currentButton.buttonInterface.mouseWheelMoved(e); }

    //
    // if your button uses useMultiSelection, you can override these if your button class
    // to get any multiselect event
    //

    @Override
    public void onMultiSelectStart() {
        for (ButtonNode buttonNode : buttonList) {
            if (buttonNode == currentButton && buttonNode.buttonInterface.useMultiSelection()) {
                buttonNode.buttonInterface.onMultiSelectStart();;
                if (bDebugLogButtonManagerInfo || bDebugLogMultiSelectManagerInfo) LOG.info("## {}.onMultiSelectStart() ## Start", currentButton.buttonInterface.getButtonID());
            }
        }
    }

    @Override
    public void onMultiSelectStop() {
        for (ButtonNode buttonNode : buttonList) {
            if (buttonNode == currentButton && buttonNode.buttonInterface.useMultiSelection()) {
                buttonNode.buttonInterface.onMultiSelectStop();
                if (bDebugLogButtonManagerInfo || bDebugLogMultiSelectManagerInfo) LOG.info("## {}.onMultiSelectStop() ## Stop", currentButton.buttonInterface.getButtonID());
            }
        }
    }

    @Override
    public void onMultiSelectChange(ArrayList<MapNode> nodeList) {
        for (ButtonNode buttonNode : buttonList) {
            if (buttonNode == currentButton && buttonNode.buttonInterface.useMultiSelection()) {
                buttonNode.buttonInterface.onMultiSelectChange(nodeList);
                if (bDebugLogButtonManagerInfo || bDebugLogMultiSelectManagerInfo) LOG.info("## {}.onMultiSelectChange() ## Change {} Nodes ( Total {} )", currentButton.buttonInterface.getButtonID(), nodeList.size(), multiSelectList.size());
            }
        }
    }

    @Override
    public void onMultiSelectAdd(ArrayList<MapNode> addedNodes) {
        for (ButtonNode buttonNode : buttonList) {
            if (buttonNode == currentButton && buttonNode.buttonInterface.useMultiSelection()) {
                buttonNode.buttonInterface.onMultiSelectAdd(addedNodes);
                if (bDebugLogButtonManagerInfo || bDebugLogMultiSelectManagerInfo) LOG.info("## {}.onMultiSelectAdd ## Added {} Nodes", currentButton.buttonInterface.getButtonID(), addedNodes.size());
            }
        }
    }

    @Override
    public void onMultiSelectRemove(ArrayList<MapNode> removedNodes) {
        for (ButtonNode buttonNode : buttonList) {
            if (buttonNode == currentButton && buttonNode.buttonInterface.useMultiSelection()) {
                buttonNode.buttonInterface.onMultiSelectRemove(removedNodes);
                if (bDebugLogButtonManagerInfo || bDebugLogMultiSelectManagerInfo) LOG.info("## {}.onMultiSelectRemove() ## Removed {} Nodes", currentButton.buttonInterface.getButtonID(), removedNodes.size());

            }
        }
    }

    @Override
    public void onMultiSelectOneTime(ArrayList<MapNode> oneTimeList) {
        for (ButtonNode buttonNode : buttonList) {
            if (buttonNode == currentButton && buttonNode.buttonInterface.useMultiSelection()) {
                buttonNode.buttonInterface.onMultiSelectOneTime(oneTimeList);
                if (bDebugLogButtonManagerInfo || bDebugLogMultiSelectManagerInfo) LOG.info("## {}.onMultiSelectOneTime ## Selected {} Nodes", currentButton.buttonInterface.getButtonID(), oneTimeList.size());
            }
        }
    }

    @Override
    public void onMultiSelectCleared() {
        for (ButtonNode buttonNode : buttonList) {
            if (buttonNode == currentButton && buttonNode.buttonInterface.useMultiSelection()) {
                buttonNode.buttonInterface.onMultiSelectCleared();
                if (bDebugLogButtonManagerInfo || bDebugLogMultiSelectManagerInfo) LOG.info("## {}.onMultiSelectCleared() ## Cleared", currentButton.buttonInterface.getButtonID());
            }
        }
    }

    //
    // you can @Override drawToScreen in your button class to use it to draw to the screen
    //

    public void drawToScreen(Graphics g) {
        for (ButtonNode buttonNode : buttonList) {
//            if (renderGraphics != null && pdaImage != null) {
                buttonNode.buttonInterface.drawToScreen(g);
//            }
        }
    }

    // Debug use only...
    @SuppressWarnings("unused")
    public void listButtons() {
        for (ButtonNode button : buttonList) {
            LOG.info("button = {}", button);
        }
    }
}
