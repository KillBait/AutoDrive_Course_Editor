package AutoDriveEditor.Managers;

import java.awt.*;
import java.awt.event.ActionEvent;
import java.awt.event.MouseEvent;
import java.awt.event.MouseWheelEvent;
import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.locks.Lock;

import static AutoDriveEditor.Utils.LoggerUtils.LOG;

public class ButtonManager {

    public interface ButtonState {
        String getButtonID();
        String getButtonAction();
        String getButtonPanel();
        Boolean ignoreDeselect();
        void setNode(ButtonNode buttonNode);
        void setEnabled(boolean enabled);
        void setSelected(boolean selected);
        void actionPerformed(ActionEvent e);
        void mouseClicked(MouseEvent e);
        void mousePressed(MouseEvent e);
        void mouseReleased(MouseEvent e);
        void mouseDragged(MouseEvent e);
        void mouseMoved(MouseEvent e);
        void mouseWheelMoved(MouseWheelEvent e);
        void drawToScreen(Graphics2D g, Lock lock, double scaledSizeQuarter, double scaledSizeHalf);
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
        LOG.info("Initializing ButtonState Manager");
    }

    @SuppressWarnings("UnusedReturnValue")
    public ButtonState addButton(ButtonState button) {
        ButtonNode buttonNode = new ButtonNode(button);
        buttonNode.button.setNode(buttonNode);
        buttonList.add(buttonNode);
        return buttonNode.button;
    }

    @SuppressWarnings("unused")
    public void disableAllButtons() {
        setAllButtonsTo(false);
    }



    public void enableAllButtons() {
        setAllButtonsTo(true);
    }

    @SuppressWarnings("unused")
    public void setButton(String buttonID, boolean result) {
        for (ButtonNode buttonNode : buttonList) {
            if (buttonNode.button.getButtonID().equals(buttonID)) {
                buttonNode.button.setEnabled(result);
            }
        }
    }

    public void setAllButtonsTo(boolean enabled) {
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
        }
    }

    public void deSelectAll() {
        for (ButtonNode buttonNode : buttonList) {
            buttonNode.button.setSelected(false);
        }
        currentButton = null;
    }

    public ButtonState getCurrentButton() {
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

    @SuppressWarnings("unused")
    public String getCurrentButtonGroup() {
        if (currentButton != null && currentButton.button.getButtonAction() != null) {
            return currentButton.button.getButtonAction();
        } else {
            return "None";
        }
    }

    public Boolean isSelected(ButtonNode node) {
        return getCurrentButton() == node;
    }

    public void mouseClicked(MouseEvent e) {
        if (currentButton != null) currentButton.button.mouseClicked(e);
    }

    public void mousePressed(MouseEvent e) {
        if (currentButton != null) currentButton.button.mousePressed(e);
    }

    public void mouseReleased(MouseEvent e) {
        if (currentButton != null) currentButton.button.mouseReleased(e);
    }

    public void mouseDragged(MouseEvent e) { if (currentButton != null) currentButton.button.mouseDragged(e); }

    public void mouseMoved(MouseEvent e) {
        if (currentButton != null) currentButton.button.mouseMoved(e);
    }

    public void mouseWheelMoved(MouseWheelEvent e) { if (currentButton != null) currentButton.button.mouseWheelMoved(e);
    }

    public void draw(Graphics2D g, Lock lock, double scaledSizeQuarter, double scaledSizeHalf) {
        for (ButtonNode buttonNode : buttonList) {
            buttonNode.button.drawToScreen(g, lock, scaledSizeQuarter, scaledSizeHalf);
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
