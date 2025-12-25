package AutoDriveEditor.GUI.Buttons.Toolbar.Markers;

import AutoDriveEditor.Classes.KeyBinds.Shortcut;
import AutoDriveEditor.Classes.UI_Components.ScaleAnimIcon;
import AutoDriveEditor.GUI.Buttons.MarkerBaseButton;
import AutoDriveEditor.Managers.ChangeManager;
import AutoDriveEditor.Managers.ShortcutManager;
import AutoDriveEditor.RoadNetwork.MapNode;

import javax.swing.*;
import java.awt.event.ActionEvent;
import java.awt.event.MouseEvent;

import static AutoDriveEditor.AutoDriveEditor.changeManager;
import static AutoDriveEditor.AutoDriveEditor.getMapPanel;
import static AutoDriveEditor.GUI.MapPanel.*;
import static AutoDriveEditor.Listeners.MouseListener.currentMouseX;
import static AutoDriveEditor.Listeners.MouseListener.currentMouseY;
import static AutoDriveEditor.Locale.LocaleManager.getLocaleString;
import static AutoDriveEditor.Managers.IconManager.*;
import static AutoDriveEditor.Managers.ShortcutManager.ShortcutID.DELETE_MARKER_SHORTCUT;
import static AutoDriveEditor.Managers.ShortcutManager.getUserShortcutByID;
import static AutoDriveEditor.Managers.ShortcutManager.registerShortcut;
import static AutoDriveEditor.XMLConfig.AutoSave.resumeAutoSaving;
import static AutoDriveEditor.XMLConfig.AutoSave.suspendAutoSaving;

public class DeleteMarkerButton extends MarkerBaseButton {

    public DeleteMarkerButton(JPanel panel) {
        ScaleAnimIcon animRemoveMarkerIcon = createScaleAnimIcon(BUTTON_REMOVE_MARKER_ICON, false);
        button = createAnimToggleButton(animRemoveMarkerIcon, panel, null, null,  false, false, this);

        Shortcut deleteMarkerShortcut = getUserShortcutByID(DELETE_MARKER_SHORTCUT);
        if (deleteMarkerShortcut != null) {
            Action deleteMarkerButtonAction = new AbstractAction() {
                @Override
                public void actionPerformed(ActionEvent e) {
                    MapNode selectedNode = getNodeAtScreenPosition(currentMouseX, currentMouseY);
                    if (selectedNode != null && selectedNode.hasMapMarker()) removeMarkerFromNode(selectedNode);
                }
            };
            registerShortcut(this, deleteMarkerShortcut, deleteMarkerButtonAction, getMapPanel());
        }
    }

    @Override
    public String getButtonID() { return "DeleteMarkerButton"; }

    @Override
    public String getButtonAction() { return "ActionButton"; }

    @Override
    public String getButtonPanel() { return "Markers"; }

    @Override
    public String getInfoText() { return getLocaleString("toolbar_markers_delete_infotext"); }

    @Override
    public void mouseClicked(MouseEvent e) {
        if (e.getButton() == MouseEvent.BUTTON1) {
            MapNode selectedNode = getNodeAtScreenPosition(e.getX(), e.getY());
            if (selectedNode != null) {
                if (selectedNode.hasMapMarker()) {
                    removeMarkerFromNode(selectedNode);
                }
            }
        }
    }

    public void removeMarkerFromNode(MapNode fromMapNode) {
        suspendAutoSaving();
        changeManager.addChangeable( new MarkerRemoveChanger(fromMapNode));
        fromMapNode.removeMapMarker();
        setStale(true);
        getMapPanel().repaint();
        resumeAutoSaving();
    }

    @Override
    public String buildToolTip() {
        Shortcut s = ShortcutManager.getUserShortcutByID(DELETE_MARKER_SHORTCUT);
        if (s != null) {
            return getLocaleString("toolbar_markers_delete_tooltip") + "\n\n( Shortcut: " + s.getShortcutString() + " )";
        } else {
            return getLocaleString("toolbar_markers_delete_tooltip");
        }
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

        public void undo() {
            suspendAutoSaving();
            this.markerNode.createMapMarker(this.markerName, this.markerGroup);
            setStale(this.isStale);
            resumeAutoSaving();
            getMapPanel().repaint();
        }

        public void redo() {
            suspendAutoSaving();
            this.markerNode.removeMapMarker();
            setStale(true);
            resumeAutoSaving();
            getMapPanel().repaint();
        }
    }
}
