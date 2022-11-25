package AutoDriveEditor.GUI.Buttons.Nodes;

import AutoDriveEditor.GUI.Buttons.BaseButton;
import AutoDriveEditor.Managers.ChangeManager;
import AutoDriveEditor.RoadNetwork.MapNode;

import javax.swing.*;
import java.awt.event.MouseEvent;
import java.util.LinkedList;

import static AutoDriveEditor.AutoDriveEditor.changeManager;
import static AutoDriveEditor.Locale.LocaleManager.getLocaleString;
import static AutoDriveEditor.Managers.MultiSelectManager.*;
import static AutoDriveEditor.MapPanel.MapPanel.*;
import static AutoDriveEditor.RoadNetwork.MapNode.NODE_FLAG_CONTROL_POINT;
import static AutoDriveEditor.Utils.GUIUtils.makeImageToggleButton;

public class NodePriorityButton extends BaseButton {

    public NodePriorityButton(JPanel panel) {
        button = makeImageToggleButton("buttons/swappriority","buttons/swappriority_selected", null,"nodes_priority_tooltip","nodes_priority_alt", panel, false, false, null, false, this);
    }

    @Override
    public String getButtonID() { return "NodePriorityButton"; }

    @Override
    public String getButtonAction() { return "ActionButton"; }

    @Override
    public String getButtonPanel() { return "Nodes"; }

    @Override
    public String getInfoText() { return getLocaleString("nodes_priority_tooltip"); }

    @Override
    public void mousePressed(MouseEvent e) {
        if (e.getButton() == MouseEvent.BUTTON1) {
            MapNode changingNode = getNodeAt(e.getX(), e.getY());
            if (changingNode != null) {
                if (changingNode.flag != NODE_FLAG_CONTROL_POINT) {
                    changeNodePriority(changingNode);
                }
            }
        }
        if (e.getButton() == MouseEvent.BUTTON3) {
            startMultiSelect(e.getX(), e.getY());
        }
    }

    @Override
    public void mouseReleased(MouseEvent e) {
        if (e.getButton() == MouseEvent.BUTTON3) {
            stopMultiSelect(e.getX(), e.getY());
            getAllNodesInSelectedArea(rectangleStart, rectangleEnd);
            changeAllNodesPriInScreenArea();
        }
    }

    @Override
    public void mouseDragged(MouseEvent e) {
        if (rectangleStart != null && isMultiSelectDragging) {
            getMapPanel().repaint();
        }
    }

    public void changeNodePriority(MapNode nodeToChange) {
        nodeToChange.flag = 1 - nodeToChange.flag;
        changeManager.addChangeable( new NodePriorityChanger(nodeToChange));
        setStale(true);
        getMapPanel().repaint();
    }

    public void changeAllNodesPriInScreenArea() {
        canAutoSave = false;
        if (!multiSelectList.isEmpty()) {
            for (MapNode node : multiSelectList) {
                node.flag = 1 - node.flag;
            }
        }
        changeManager.addChangeable( new NodePriorityChanger(multiSelectList));
        setStale(true);
        clearMultiSelection();
        canAutoSave = true;
    }

    //
    // Node Priority Changer
    //

    public static class NodePriorityChanger implements ChangeManager.Changeable {

        private final LinkedList<MapNode> nodesPriorityChanged;
        private final boolean isStale;

        public NodePriorityChanger(LinkedList<MapNode> mapNodesChanged){
            super();
            this.nodesPriorityChanged = new LinkedList<>();
            for (int i = 0; i <= mapNodesChanged.size() - 1 ; i++) {
                MapNode mapNode = mapNodesChanged.get(i);
                this.nodesPriorityChanged.add(mapNode);
            }
            this.isStale = isStale();
        }

        public NodePriorityChanger(MapNode nodeToChange){
            super();
            this.nodesPriorityChanged = new LinkedList<>();
            this.nodesPriorityChanged.add(nodeToChange);
            this.isStale = isStale();
        }

        public void undo(){
            for (int i = 0; i <= this.nodesPriorityChanged.size() - 1 ; i++) {
                MapNode mapNode = this.nodesPriorityChanged.get(i);
                mapNode.flag = 1 - mapNode.flag;
            }
            getMapPanel().repaint();
            setStale(this.isStale);
        }

        public void redo(){
            for (int i = 0; i <= this.nodesPriorityChanged.size() - 1 ; i++) {
                MapNode mapNode = this.nodesPriorityChanged.get(i);
                mapNode.flag = 1 - mapNode.flag;
            }
            getMapPanel().repaint();
            setStale(true);
        }
    }
}
