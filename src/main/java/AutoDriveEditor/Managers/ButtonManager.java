package AutoDriveEditor.Managers;

import java.awt.*;
import java.awt.event.ActionEvent;
import java.awt.event.MouseEvent;
import java.awt.event.MouseWheelEvent;
import java.util.ArrayList;
import java.util.List;
import java.util.Objects;

import static AutoDriveEditor.Classes.MapImage.pdaImage;
import static AutoDriveEditor.GUI.MapPanel.renderGraphics;
import static AutoDriveEditor.GUI.Menus.DebugMenu.Logging.LogButtonInfoMenu.bDebugLogButton;
import static AutoDriveEditor.Utils.LoggerUtils.LOG;

@SuppressWarnings("unused")
public class ButtonManager {

    public interface ButtonState {

        /**
         * sets a unique ID (name) for your button
         * @return string containing your unique name
         */
        String getButtonID();

        /**
         * sets a name for this buttons action, you can assign multiple
         * buttons the same name to effectively create a group and use
         * getActionGroup("your_group_name") to get an ArrayList of all
         * buttons assigned that name.
         * @return string containing your unique name
         */
        String getButtonAction();

        /**
         * sets a name for this buttons panel, you can assign multiple
         * buttons the same name to effectively create a group and use
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
         * when any other button is selected, useful for making buttons that
         * are always available ( i.e. toggle a feature on/off ).
         * -- Default: false
         * @return true/false
         * @
         */
        Boolean ignoreButtonDeselect();

        //
        //
        // MultiSelect adjusters
        //
        //

        /**
         * useMultiSelection() - return true/false if you want the button to
         * use the multi selection feature with the Right mouse/drag
         * // -- Default: false
         * @return true/false
         */
        Boolean useMultiSelection();

        /**
         * addSelectedToMultiSelectList() - Only works if useMultiSelection() is true
         *
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
         *
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
         *
         * return true/false if you want to preview the selected status of the nodes
         * returned while Multi-Select dragging
         * // -- Default: true
         * @return true/false
         */
        Boolean previewNodeSelectionChange();

        /**
         * previewNodeHiddenChange() - Only works if useMultiSelection() is true
         *
         * return true/false if you want to preview the hidden status of the nodes
         * returned while Multi Select dragging.
         * // -- Default: false
         * @return true/false
         */
        Boolean previewNodeHiddenChange();

        /**
         * previewNodeFlagChange() - Only works if useMultiSelection() is true
         *
         * return true/false if you want to preview any changes to the type of the
         * nodes returned while Multi Select dragging.
         * // -- Default: false
         * @return true/false
         */
        Boolean previewNodeFlagChange();


        Boolean previewConnectionHiddenChange();

        /**
         * getLineDetectionInterval() - Used by ConnectSelectBaseButton only
         *
         * Only used when freeform selection is used, sets the interval in
         * pixels to move along the length of a line and test each point is
         * inside the selection area.
         * NOTE:- Small values are more CPU intensive
         * Default: 10
         * @return Integer
         */
        Integer getLineDetectionInterval();





        /**
         * sets a unique reference for this button.
         * -- see addButton() --
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
         * override if you want to be informed when the button is interacted with
         */
        void actionPerformed(ActionEvent e);

        /**
         * override any of the below functions to be informed of mouse events
         */
        void mouseClicked(MouseEvent e);
        void mousePressed(MouseEvent e);
        void mouseReleased(MouseEvent e);
        void mouseDragged(MouseEvent e);
        void mouseMoved(MouseEvent e);
        void mouseWheelMoved(MouseWheelEvent e);

        /**
         * Override this function if you need to do anything on the screen
         * @param g graphics reference for your JPanel
         */
        void drawToScreen(Graphics g);
    }

    public static class ButtonNode {
        public final ButtonState button;

        public ButtonNode(ButtonState b){
            button = b;
        }
    }

    private final List<ButtonNode> buttonList = new ArrayList<>();

    public static ButtonNode currentButton;

    public ButtonManager() {
        LOG.info("Initializing ButtonManager");
    }

    @SuppressWarnings("UnusedReturnValue")
    public ButtonState addButton(ButtonState button) {
        ButtonNode buttonNode = new ButtonNode(button);
        buttonNode.button.setNode(buttonNode);
        buttonList.add(buttonNode);
        return buttonNode.button;
    }

    @SuppressWarnings("unused")
    public void disableAllButtons() { setAllButtonsEnabled(false); }

    public void enableAllButtons() {
        setAllButtonsEnabled(true);
    }

    @SuppressWarnings("unused")
    public void setButton(String buttonID, boolean result) {
        for (ButtonNode buttonNode : buttonList) {
            if (buttonNode.button.getButtonID().equals(buttonID)) {
                buttonNode.button.setEnabled(result);
            }
        }
    }

    private void setAllButtonsEnabled(boolean enabled) {
        for (ButtonNode buttonNode : buttonList) {
            buttonNode.button.setEnabled(enabled);
        }
    }

    public void makeCurrent(ButtonNode currentNode) {
        if (currentNode != null) {
            currentButton = currentNode;
            for (ButtonNode buttonNode : buttonList) {
                buttonNode.button.setSelected(buttonNode == currentButton);
            }
            if (bDebugLogButton) LOG.info("Button {}",currentButton.button.getButtonID());
        }
    }

    public void deSelectAll() {
        for (ButtonNode buttonNode : buttonList) {
            buttonNode.button.setSelected(false);
        }
        currentButton = null;
    }

    public static ButtonState getCurrentButton() {
        if (currentButton != null) {
            return currentButton.button;
        } else {
            return null;
        }
    }

    public String getCurrentButtonID() {
        if (currentButton != null && currentButton.button.getButtonID() != null) {
            return currentButton.button.getButtonID();
        } else {
            return "None";
        }
    }

    public String getCurrentButtonAction() {
        if (currentButton != null && currentButton.button.getButtonAction() != null) {
            return currentButton.button.getButtonAction();
        } else {
            return "None";
        }
    }

    public String getCurrentButtonPanel() {
        if (currentButton != null && currentButton.button.getButtonPanel() != null) {
            return currentButton.button.getButtonPanel();
        } else {
            return "None";
        }
    }

    public ArrayList<ButtonState> getActionGroup(String actionName) {
        ArrayList<ButtonState> matchList = new ArrayList<>();
        for (ButtonNode buttonNode : buttonList) {
            if (Objects.equals(buttonNode.button.getButtonAction(), actionName)) {
                matchList.add(buttonNode.button);
            }
        }
        return matchList;
    }

    public ArrayList<ButtonState> getPanelGroup(String panelName) {
        ArrayList<ButtonState> matchList = new ArrayList<>();
        for (ButtonNode buttonNode : buttonList) {
            if (Objects.equals(buttonNode.button.getButtonPanel(), panelName)) {
                matchList.add(buttonNode.button);
            }
        }
        return matchList;
    }

    public Boolean isSelected(ButtonNode node) { return getCurrentButton() == node; }

    //
    // you can @Override any of the mouse functions in your buttons class, if you need
    // to use them.
    //

    public void mouseClicked(MouseEvent e) { if (currentButton != null) currentButton.button.mouseClicked(e); }
    public void mousePressed(MouseEvent e) { if (currentButton != null) currentButton.button.mousePressed(e); }
    public void mouseReleased(MouseEvent e) { if (currentButton != null) currentButton.button.mouseReleased(e);}
    public void mouseDragged(MouseEvent e) { if (currentButton != null) currentButton.button.mouseDragged(e); }
    public void mouseMoved(MouseEvent e) { if (currentButton != null) currentButton.button.mouseMoved(e); }
    public void mouseWheelMoved(MouseWheelEvent e) { if (currentButton != null) currentButton.button.mouseWheelMoved(e); }

    //
    // you can @Override drawToScreen in your button class to use it to draw to the screen
    //

    public void drawToScreen(Graphics g) {
        for (ButtonNode buttonNode : buttonList) {
            if (renderGraphics != null && pdaImage != null) {
                buttonNode.button.drawToScreen(g);
            }
        }
    }

    // Debug use

    @SuppressWarnings("unused")
    public void listButtons() {
        for (ButtonNode button : buttonList) {
            LOG.info("button = {}", button);
        }
    }
}
