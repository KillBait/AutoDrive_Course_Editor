package AutoDriveEditor.GUI.Buttons.Nodes;

import AutoDriveEditor.GUI.Buttons.BaseButton;
import AutoDriveEditor.Managers.ChangeManager;
import AutoDriveEditor.RoadNetwork.MapNode;

import javax.swing.*;
import java.awt.event.MouseEvent;
import java.util.ArrayList;
import java.util.LinkedList;

import static AutoDriveEditor.AutoDriveEditor.changeManager;
import static AutoDriveEditor.AutoDriveEditor.getMapPanel;
import static AutoDriveEditor.GUI.MapPanel.*;
import static AutoDriveEditor.Locale.LocaleManager.getLocaleString;
import static AutoDriveEditor.Managers.MultiSelectManager.getSelectedNodes;
import static AutoDriveEditor.RoadNetwork.MapNode.NODE_FLAG_CONTROL_POINT;
import static AutoDriveEditor.Utils.GUIUtils.makeImageToggleButton;
import static AutoDriveEditor.XMLConfig.AutoSave.resumeAutoSaving;
import static AutoDriveEditor.XMLConfig.AutoSave.suspendAutoSaving;

public class SwapNodePriorityButton extends BaseButton {

    MapNode lastHoveredNode;



    public SwapNodePriorityButton(JPanel panel) {
        button = makeImageToggleButton("buttons/swappriority","buttons/swappriority_selected", null,"nodes_priority_tooltip","nodes_priority_alt", panel, false, false, null, false, this);
    }

    @Override
    public String getButtonID() { return "SwapNodePriorityButton"; }
    @Override
    public String getButtonAction() { return "ActionButton"; }
    @Override
    public String getButtonPanel() { return "Nodes"; }
    @Override
    public String getInfoText() { return getLocaleString("nodes_priority_tooltip"); }

    @Override
    public Boolean useMultiSelection() { return true; }
    @Override
    public Boolean previewNodeSelectionChange() { return false; }
    @Override
    public Boolean previewNodeFlagChange() { return true; }
    @Override
    public Boolean addSelectedToMultiSelectList() { return false; }
    @Override
    public Boolean ignoreDeselect() { return true; }

    @Override
    public void mousePressed(MouseEvent e) {
        super.mousePressed(e);
        if (e.getButton() == MouseEvent.BUTTON1) {
            MapNode changingNode = getNodeAtScreenPosition(e.getX(), e.getY());
            if (changingNode != null && changingNode.flag != NODE_FLAG_CONTROL_POINT) {
                if (changingNode.isSelectable()) changeNodePriority(changingNode);
            }
        }
    }

    @Override
    public void mouseReleased(MouseEvent e) {
        super.mouseReleased(e);
        if (e.getButton() == MouseEvent.BUTTON3) {
            changeAllNodesPriInSelection(getSelectedNodes());
        }
    }

    public void changeNodePriority(MapNode nodeToChange) {
        nodeToChange.flag = 1 - nodeToChange.flag;
        changeManager.addChangeable( new NodePriorityChanger(nodeToChange));
        setStale(true);
        getMapPanel().repaint();
    }

    public void changeAllNodesPriInSelection(ArrayList<MapNode> nodeList) {
        if (nodeList.size() > 0) {
            suspendAutoSaving();
            for (MapNode node : nodeList) {
                node.flag = 1 - node.flag;
                node.setPreviewNodeFlagChange(false);
            }
            changeManager.addChangeable( new NodePriorityChanger(nodeList));
            setStale(true);
            resumeAutoSaving();
            getMapPanel().repaint();
        }
    }

    //
    // Node Priority Changer
    //

    public static class NodePriorityChanger implements ChangeManager.Changeable {

        private final LinkedList<MapNode> nodesPriorityChanged;
        private final boolean isStale;

        public NodePriorityChanger(ArrayList<MapNode> mapNodesChanged){
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
