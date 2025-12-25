package AutoDriveEditor.GUI.Buttons.Toolbar.Nodes;

import AutoDriveEditor.Classes.KeyBinds.Shortcut;
import AutoDriveEditor.Classes.UI_Components.ScaleAnimIcon;
import AutoDriveEditor.GUI.Buttons.BaseButton;
import AutoDriveEditor.Managers.ChangeManager;
import AutoDriveEditor.Managers.ShortcutManager;
import AutoDriveEditor.RoadNetwork.MapNode;

import javax.swing.*;
import java.awt.event.ActionEvent;
import java.awt.event.MouseEvent;
import java.util.ArrayList;
import java.util.LinkedList;

import static AutoDriveEditor.AutoDriveEditor.*;
import static AutoDriveEditor.GUI.MapPanel.*;
import static AutoDriveEditor.Locale.LocaleManager.getLocaleString;
import static AutoDriveEditor.Managers.IconManager.*;
import static AutoDriveEditor.Managers.ShortcutManager.ShortcutID.NODE_PRIORITY_SHORTCUT;
import static AutoDriveEditor.Managers.ShortcutManager.getUserShortcutByID;
import static AutoDriveEditor.Managers.ShortcutManager.registerShortcut;
import static AutoDriveEditor.RoadNetwork.MapNode.NODE_FLAG_CONTROL_NODE;
import static AutoDriveEditor.XMLConfig.AutoSave.resumeAutoSaving;
import static AutoDriveEditor.XMLConfig.AutoSave.suspendAutoSaving;

public class SwapNodePriorityButton extends BaseButton {




    public SwapNodePriorityButton(JPanel panel) {
//        button = makeImageToggleButton("buttons/swappriority","buttons/swappriority_selected", null,"nodes_priority_tooltip","nodes_priority_alt", panel, false, false, null, false, this);
        ScaleAnimIcon animSwapNodePriorityIcon = createScaleAnimIcon(BUTTON_SWAP_PRIORITY_ICON, false);
        button = createAnimToggleButton(animSwapNodePriorityIcon, panel, null, null,  false, false, this);

        // Setup Keyboard Shortcuts
        Shortcut moveShortcut = getUserShortcutByID(NODE_PRIORITY_SHORTCUT);
        if (moveShortcut != null) {
            Action moveButtonAction = new AbstractAction() {
                @Override
                public void actionPerformed(ActionEvent e) {
                    if (button.isEnabled() && !button.isSelected()) {
                        buttonManager.makeCurrent(buttonNode);
                    } else {
                        buttonManager.deSelectAll();
                    }
                }
            };
            registerShortcut(this, moveShortcut, moveButtonAction, getMapPanel());
        }
    }

    @Override
    public String getButtonID() { return "SwapNodePriorityButton"; }
    @Override
    public String getButtonAction() { return "ActionButton"; }
    @Override
    public String getButtonPanel() { return "Nodes"; }
    @Override
    public String getInfoText() { return getLocaleString("toolbar_nodes_priority_infotext"); }

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
            if (changingNode != null && changingNode.flag != NODE_FLAG_CONTROL_NODE) {
                if (changingNode.isSelectable()) swapNodePriority(changingNode);
            }
        }
    }

    @Override
    public void onMultiSelectOneTime(ArrayList<MapNode> nodeList) {
        if (!nodeList.isEmpty()) swapSelectionPriority(nodeList);
    }

    public void swapNodePriority(MapNode nodeToChange) {
        if (nodeToChange != null) {
            suspendAutoSaving();
            nodeToChange.flag = 1 - nodeToChange.flag;
            changeManager.addChangeable( new NodePriorityChanger(nodeToChange));
            setStale(true);
            resumeAutoSaving();
            getMapPanel().repaint();
        }
    }

    public void swapSelectionPriority(ArrayList<MapNode> nodeList) {
        if (!nodeList.isEmpty()) {
            suspendAutoSaving();
            changeManager.addChangeable( new NodePriorityChanger(nodeList));
            for (MapNode node : nodeList) {
                node.flag = 1 - node.flag;
                node.setPreviewNodeFlagChange(false);
            }
            setStale(true);
            resumeAutoSaving();
            getMapPanel().repaint();
        }
    }

    @Override
    public String buildToolTip() {
        Shortcut s = ShortcutManager.getUserShortcutByID(NODE_PRIORITY_SHORTCUT);
        if (s != null) {
            return getLocaleString("toolbar_nodes_priority_tooltip") + "\n\n( Shortcut: " + s.getShortcutString() + " )";
        } else {
            return getLocaleString("toolbar_nodes_priority_tooltip");
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
            suspendAutoSaving();
            for (int i = 0; i <= this.nodesPriorityChanged.size() - 1 ; i++) {
                MapNode mapNode = this.nodesPriorityChanged.get(i);
                mapNode.flag = 1 - mapNode.flag;
            }
            setStale(this.isStale);
            resumeAutoSaving();
            getMapPanel().repaint();
        }

        public void redo(){
            suspendAutoSaving();
            for (int i = 0; i <= this.nodesPriorityChanged.size() - 1 ; i++) {
                MapNode mapNode = this.nodesPriorityChanged.get(i);
                mapNode.flag = 1 - mapNode.flag;
            }
            setStale(true);
            resumeAutoSaving();
            getMapPanel().repaint();
        }
    }
}
