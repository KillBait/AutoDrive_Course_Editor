package AutoDriveEditor.GUI.Buttons;

import AutoDriveEditor.RoadNetwork.MapNode;

import javax.swing.*;
import java.awt.*;
import java.awt.event.*;
import java.util.LinkedList;
import java.util.concurrent.locks.Lock;

import static AutoDriveEditor.AutoDriveEditor.buttonManager;
import static AutoDriveEditor.GUI.MenuBuilder.bDebugLogGUIInfo;
import static AutoDriveEditor.Managers.ButtonManager.ButtonNode;
import static AutoDriveEditor.Managers.ButtonManager.ButtonState;
import static AutoDriveEditor.Managers.MultiSelectManager.clearMultiSelection;
import static AutoDriveEditor.Utils.GUIUtils.showInTextArea;
import static AutoDriveEditor.Utils.LoggerUtils.LOG;

public abstract class BaseButton implements ButtonState, ActionListener, MouseListener
{

    //protected abstract String getInfoText();

    private ButtonNode buttonNode;
    protected AbstractButton button;

    public String getButtonID() { return "BaseButton"; }
    public String getButtonAction() { return null; }
    public String getInfoText() {  return button.getToolTipText(); }
    public Boolean ignoreDeselect() {
        return false;
    }

    public void actionPerformed(ActionEvent e) {
        if (button.isSelected()) {
            if (buttonNode.button.ignoreDeselect()) {
                if (bDebugLogGUIInfo) LOG.info("BaseButton > isSelected ignoring button");
            } else {
                buttonManager.makeCurrent(buttonNode);
                if (bDebugLogGUIInfo) LOG.info("BaseButton > setting {} as current", buttonManager.getCurrentButtonID());
            }
        } else {

            if (buttonManager.getCurrentButton() != null) {
                if (buttonNode.button.ignoreDeselect()) {
                    if (bDebugLogGUIInfo) LOG.info("BaseButton > {} ignoring deselect", buttonNode.button.getButtonID());
                } else {
                    if (bDebugLogGUIInfo) LOG.info("BaseButton > {} triggered deselect all", buttonNode.button.getButtonID());
                    buttonManager.deSelectAll();
                }
            } else {
                if (bDebugLogGUIInfo) LOG.info("CurrentButton = {}", buttonNode.button.getButtonID());
            }
        }
    }

    public void setNode(ButtonNode buttonNode) {
        this.buttonNode = buttonNode;
    }

    public void setEnabled(boolean enabled) { button.setEnabled(enabled); }

    public void setSelected(boolean selected) {
        button.setSelected(selected);
        if (selected) {
            showInTextArea(getInfoText(), true, false);
        }
    }

    @Override
    public void mouseClicked(MouseEvent e) {
        if (e.getButton() == MouseEvent.BUTTON3) {
            if (button.isSelected()) clearMultiSelection();
        }
    }

    @Override
    public void mousePressed(MouseEvent e) {}

    @Override
    public void mouseReleased(MouseEvent e) {}

    @Override
    public void mouseDragged(MouseEvent e) {}

    @Override
    public void mouseMoved(MouseEvent e) {}

    @Override
    public void mouseWheelMoved(MouseWheelEvent e) {}

    @Override
    public void mouseEntered(MouseEvent e) {}

    @Override
    public void mouseExited(MouseEvent e) {}

    @Override
    public void drawToScreen(Graphics2D g, Lock lock, double scaledSizeQuarter, double scaledSizeHalf) {}

    public static class MapNodeStore {
        private final MapNode mapNode;
        private final int mapNodeIDBackup;
        private final LinkedList<MapNode> incomingBackup;
        private final LinkedList<MapNode> outgoingBackup;

        public MapNodeStore(MapNode node) {
            this.mapNode = node;
            this.mapNodeIDBackup = node.id;
            this.incomingBackup = new LinkedList<>();
            this.outgoingBackup = new LinkedList<>();
            backupConnections();
        }

        public MapNode getMapNode() {
            if (this.hasChangedID()) this.resetID();
            return this.mapNode;
        }

        public void resetID() { this.mapNode.id = this.mapNodeIDBackup; }

        public boolean hasChangedID() { return this.mapNode.id != this.mapNodeIDBackup; }

        public void clearConnections() {
            clearIncoming();
            clearOutgoing();
        }

        public void clearIncoming() { this.mapNode.incoming.clear(); }

        public void clearOutgoing() { this.mapNode.outgoing.clear(); }

        public void backupConnections() {
            copyList(this.mapNode.incoming, this.incomingBackup);
            copyList(this.mapNode.outgoing, this.outgoingBackup);
        }

        public void restoreConnections() {
            copyList(this.incomingBackup, this.mapNode.incoming);
            copyList(this.outgoingBackup, this.mapNode.outgoing);
        }

        @SuppressWarnings("unused")
        public void backupIncoming() { copyList(this.mapNode.incoming, this.incomingBackup); }

        @SuppressWarnings("unused")
        public void restoreIncoming() { copyList(this.incomingBackup, this.mapNode.incoming); }

        @SuppressWarnings("unused")
        public void backupOutgoing() { copyList(this.mapNode.outgoing, this.outgoingBackup); }

        @SuppressWarnings("unused")
        public void restoreOutgoing() { copyList(this.outgoingBackup, this.mapNode.outgoing); }

        private void copyList(LinkedList<MapNode> from, LinkedList<MapNode> to) {
            to.clear();
            // use .clone() ??
            for (int i = 0; i <= from.size() - 1 ; i++) {
                MapNode mapNode = from.get(i);
                to.add(mapNode);
            }
        }
    }
}
