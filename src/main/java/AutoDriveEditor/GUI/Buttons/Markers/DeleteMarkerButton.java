package AutoDriveEditor.GUI.Buttons.Markers;

import AutoDriveEditor.GUI.Buttons.MarkerBaseButton;
import AutoDriveEditor.Managers.ChangeManager;
import AutoDriveEditor.RoadNetwork.MapNode;

import javax.swing.*;
import java.awt.event.MouseEvent;

import static AutoDriveEditor.AutoDriveEditor.changeManager;
import static AutoDriveEditor.Locale.LocaleManager.getLocaleString;
import static AutoDriveEditor.MapPanel.MapPanel.*;
import static AutoDriveEditor.Utils.GUIUtils.makeImageToggleButton;

public class DeleteMarkerButton extends MarkerBaseButton {

    public DeleteMarkerButton(JPanel panel) {
        button = makeImageToggleButton("buttons/deletemarker","buttons/deletemarker_selected", null,"markers_delete_tooltip","markers_delete_alt", panel, false, false, null, false, this);
    }

    @Override
    public String getButtonID() { return "DeleteMarkerButton"; }

    @Override
    public String getButtonAction() { return "ActionButton"; }

    @Override
    public String getButtonPanel() { return "Markers"; }

    @Override
    public String getInfoText() { return getLocaleString("markers_delete_tooltip"); }

    @Override
    public void mouseClicked(MouseEvent e) {
        if (e.getButton() == MouseEvent.BUTTON1) {
            MapNode selectedNode = getNodeAt(e.getX(), e.getY());
            if (selectedNode != null) {
                if (selectedNode.hasMapMarker()) {
                    removeMarkerFromNode(selectedNode);
                }
            }
        }
    }

    public void removeMarkerFromNode(MapNode fromMapNode) {
        canAutoSave = false;
        changeManager.addChangeable( new MarkerRemoveChanger(fromMapNode));
        fromMapNode.removeMapMarker();
        setStale(true);
        getMapPanel().repaint();
        canAutoSave = true;
    }

    public static class MarkerRemoveChanger implements ChangeManager.Changeable {
        private final MapNode markerNode;
        private final String markerName;
        private final String markerGroup;
        private final Boolean isStale;

        public MarkerRemoveChanger(MapNode mapnode){
            this.markerNode = mapnode;
            this.markerName = mapnode.getMarkerName();
            this.markerGroup = mapnode.getMarkerGroup();
            this.isStale = isStale();
        }

        public void undo(){
            this.markerNode.createMapMarker(this.markerName, this.markerGroup);
            getMapPanel().repaint();
            setStale(this.isStale);
        }

        public void redo(){
            this.markerNode.removeMapMarker();
            getMapPanel().repaint();
            setStale(true);
        }
    }
}
