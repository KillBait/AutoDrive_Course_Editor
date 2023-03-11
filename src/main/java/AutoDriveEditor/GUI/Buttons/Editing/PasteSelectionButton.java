package AutoDriveEditor.GUI.Buttons.Editing;

import AutoDriveEditor.GUI.Buttons.CopyPasteBaseButton;
import AutoDriveEditor.Managers.ChangeManager;
import AutoDriveEditor.RoadNetwork.MapNode;
import AutoDriveEditor.RoadNetwork.RoadMap;

import javax.swing.*;
import java.awt.event.ActionEvent;
import java.util.LinkedList;

import static AutoDriveEditor.GUI.MenuBuilder.bDebugLogCopyPasteInfo;
import static AutoDriveEditor.Managers.MultiSelectManager.clearMultiSelection;
import static AutoDriveEditor.MapPanel.MapPanel.*;
import static AutoDriveEditor.Utils.GUIUtils.makeImageButton;
import static AutoDriveEditor.Utils.LoggerUtils.LOG;

public class PasteSelectionButton extends CopyPasteBaseButton {

    public PasteSelectionButton(JPanel panel) {
        button = makeImageButton("buttons/paste", "buttons/paste_selected", null, "copypaste_paste_tooltip","copypaste_paste_alt", panel, false, this);
    }

    @Override
    public String getButtonID() { return "PasteSelectionButton"; }

    @Override
    public String getButtonAction() { return "ActionButton"; }

    @Override
    public String getButtonPanel() { return "Edit"; }

    @Override
    public void actionPerformed(ActionEvent e) {
        super.actionPerformed(e);
        if (bDebugLogCopyPasteInfo) LOG.info("PasteSelectionButton > Button Pressed");
        pasteSelected();
    }

    //
    // Paste selection changer
    //

    public static class  PasteSelectionChanger implements ChangeManager.Changeable {
        private final LinkedList<MapNode> storeNodes;
        private final boolean isStale;

        public PasteSelectionChanger(LinkedList<MapNode> nodes){
            super();
            //noinspection unchecked
            this.storeNodes = (LinkedList<MapNode>) nodes.clone();
            this.isStale = isStale();
        }

        public void undo(){
            clearMultiSelection();
            RoadMap.networkNodesList.removeAll(this.storeNodes);
            getMapPanel().repaint();
            setStale(this.isStale);
        }

        public void redo(){
            RoadMap.networkNodesList.addAll(this.storeNodes);
            getMapPanel().repaint();
            setStale(true);
        }
    }
}
