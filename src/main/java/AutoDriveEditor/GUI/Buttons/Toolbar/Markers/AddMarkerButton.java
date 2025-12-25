package AutoDriveEditor.GUI.Buttons.Toolbar.Markers;

import AutoDriveEditor.AutoDriveEditor;
import AutoDriveEditor.Classes.KeyBinds.Shortcut;
import AutoDriveEditor.Classes.UI_Components.ScaleAnimIcon;
import AutoDriveEditor.GUI.Buttons.MarkerBaseButton;
import AutoDriveEditor.Managers.ChangeManager;
import AutoDriveEditor.Managers.ShortcutManager;
import AutoDriveEditor.RoadNetwork.MapNode;
import AutoDriveEditor.RoadNetwork.MarkerGroup;
import AutoDriveEditor.RoadNetwork.RoadMap;
import AutoDriveEditor.XMLConfig.RoutesXML;

import javax.swing.*;
import java.awt.event.ActionEvent;
import java.awt.event.MouseEvent;
import java.text.Collator;
import java.util.ArrayList;
import java.util.Collections;
import java.util.LinkedList;
import java.util.Objects;

import static AutoDriveEditor.AutoDriveEditor.changeManager;
import static AutoDriveEditor.AutoDriveEditor.getMapPanel;
import static AutoDriveEditor.Classes.Util_Classes.LoggerUtils.LOG;
import static AutoDriveEditor.GUI.MapPanel.*;
import static AutoDriveEditor.GUI.Menus.DebugMenu.Logging.LogMarkerInfoMenu.bDebugLogMarkerInfo;
import static AutoDriveEditor.GUI.TextPanel.showInTextArea;
import static AutoDriveEditor.Listeners.MouseListener.currentMouseX;
import static AutoDriveEditor.Listeners.MouseListener.currentMouseY;
import static AutoDriveEditor.Locale.LocaleManager.getLocaleString;
import static AutoDriveEditor.Locale.LocaleManager.locale;
import static AutoDriveEditor.Managers.IconManager.*;
import static AutoDriveEditor.Managers.ShortcutManager.ShortcutID.ADD_MARKER_SHORTCUT;
import static AutoDriveEditor.Managers.ShortcutManager.getUserShortcutByID;
import static AutoDriveEditor.Managers.ShortcutManager.registerShortcut;
import static AutoDriveEditor.XMLConfig.AutoSave.resumeAutoSaving;
import static AutoDriveEditor.XMLConfig.AutoSave.suspendAutoSaving;
import static AutoDriveEditor.XMLConfig.RoutesXML.markerGroup;

public class AddMarkerButton extends MarkerBaseButton {

    public AddMarkerButton(JPanel panel) {
        ScaleAnimIcon animAddMarkerIcon = createScaleAnimIcon(BUTTON_ADD_MARKER_ICON, false);
        button = createAnimToggleButton(animAddMarkerIcon, panel, null, null,  false, false, this);

        Shortcut addMarkerShortcut = getUserShortcutByID(ADD_MARKER_SHORTCUT);
        if (addMarkerShortcut != null) {
            Action addMarkerButtonAction = new AbstractAction() {
                @Override
                public void actionPerformed(ActionEvent e) {
                    MapNode selectedNode = getNodeAtScreenPosition(currentMouseX, currentMouseY);
                    if (selectedNode != null && !selectedNode.hasMapMarker()) showNewMarkerDialog(selectedNode);
                }
            };
            registerShortcut(this, addMarkerShortcut, addMarkerButtonAction, getMapPanel());
        }
    }

    @Override
    public String getButtonID() { return "AddMarkerButton"; }

    @Override
    public String getButtonAction() { return "ActionButton"; }

    @Override
    public String getButtonPanel() { return "Markers"; }

    @Override
    public String getInfoText() { return getLocaleString("toolbar_markers_add_infotext"); }

    @Override
    public void mouseClicked(MouseEvent e) {
        if (e.getButton() == MouseEvent.BUTTON1) {
            MapNode selectedNode = getNodeAtScreenPosition(e.getX(), e.getY());
            if (selectedNode != null) {
                if (selectedNode.hasMapMarker()) {
                    showInTextArea(getLocaleString("toolbar_markers_exists_infotext"), true, true);
                } else {
                    showNewMarkerDialog(selectedNode);
                }
            }
        }
    }

    //
    // Dialog for marker add
    //

    @SuppressWarnings({"rawtypes", "unchecked", "Java8ListSort"})
    private void showNewMarkerDialog(MapNode selectedNode) {
        JTextField destName = new JTextField();
        String[] group = new String[1];

        ArrayList<String> groupArray = new ArrayList<>();

        if (configType == CONFIG_SAVEGAME) {
            LinkedList<MapNode> mapNodes = RoadMap.networkNodesList;
            for (MapNode node : mapNodes) {
                if (node.hasMapMarker()) {
                    if (!node.getMarkerGroup().equals("All")) {
                        if (!groupArray.contains(node.getMarkerGroup())) {
                            groupArray.add(node.getMarkerGroup());
                        }
                    }
                }

            }
        } else if (configType == CONFIG_ROUTEMANAGER) {
            for (MarkerGroup marker : markerGroup) {
                if (!marker.groupName.equals("All")) {
                    if (!groupArray.contains(marker.groupName)) {
                        groupArray.add(marker.groupName);
                    }
                }
            }
        }

        Collator coll = Collator.getInstance(locale);
        coll.setStrength(Collator.PRIMARY);
        Collections.sort(groupArray, coll);

        String[] groupString = new String[groupArray.size() + 1];
        groupString[0] = "None";
        for (int i = 0; i < groupArray.size(); i++) {
            groupString[i+1] = groupArray.get(i);
        }

        JComboBox comboBox = new JComboBox(groupString);
        comboBox.setEditable(true);
        comboBox.setSelectedIndex(0);
        comboBox.addActionListener(e -> {
            JComboBox cb = (JComboBox)e.getSource();
            group[0] = (String)cb.getSelectedItem();
        });

        Object[] inputFields = {getLocaleString("dialog_marker_select_name"), destName,
                getLocaleString("dialog_marker_add_select_group"), comboBox};

        int option = JOptionPane.showConfirmDialog(AutoDriveEditor.editor, inputFields, getLocaleString("dialog_marker_add_title") + " ( Node ID " + selectedNode.id +" )", JOptionPane.OK_CANCEL_OPTION, JOptionPane.INFORMATION_MESSAGE, getMarkerIcon());

        if (option == JOptionPane.OK_OPTION) {

            if (group[0] == null || group[0].equals("None")) group[0] = "All";
            if (destName.getText() != null && !destName.getText().isEmpty()) {
                // since we can't return more than 1 string, we have to package them up
                LOG.info("Adding marker to node ID {} - Name = {} , Group = {}",selectedNode.id, destName.getText(), group[0]);
                createMarkerForNode(selectedNode, destName.getText(), group[0]);
                getMapPanel().repaint();
            } else {
                LOG.info("Cancelling marker creation... You must specify a name");
            }
            return;
        }
        LOG.info("Cancelling marker creation");
    }

    @SuppressWarnings("EqualsBetweenInconvertibleTypes")
    public void createMarkerForNode(MapNode mapNode, String newMarkerName, String newMarkerGroup) {
        if (mapNode != null && newMarkerName != null && !newMarkerName.isEmpty()) {
            if (newMarkerGroup == null) newMarkerGroup = "All";
            changeManager.addChangeable( new MarkerAddChanger(mapNode, newMarkerName, newMarkerGroup));
            if (configType == CONFIG_ROUTEMANAGER) {
                boolean found = false;
                for (MarkerGroup marker : RoutesXML.markerGroup) {
                    if (Objects.equals(marker.groupName, newMarkerGroup)) {
                        found = true;
                        break;
                    }
                }
                if (!found && !markerGroup.equals("All")) {
                    if (bDebugLogMarkerInfo) LOG.info("Adding new group {} to markerGroup", newMarkerGroup);
                    RoutesXML.markerGroup.add(new MarkerGroup(RoutesXML.markerGroup.size() + 1, newMarkerGroup));
                }
            }
            mapNode.createMapMarker(newMarkerName, newMarkerGroup);
            setStale(true);
        }
    }

    @Override
    public String buildToolTip() {
        Shortcut s = ShortcutManager.getUserShortcutByID(ADD_MARKER_SHORTCUT);
        if (s != null) {
            return getLocaleString("toolbar_markers_add_tooltip") + "\n\n( Shortcut: " + s.getShortcutString() + " )";
        } else {
            return getLocaleString("toolbar_markers_add_tooltip");
        }
    }

    public static class MarkerAddChanger implements ChangeManager.Changeable {
        private final MapNode markerNode;
        private final String markerName;
        private final String markerGroup;
        private final Boolean isStale;

        public MarkerAddChanger(MapNode mapNode, String markerName, String markerGroup){
            super();
            this.markerNode = mapNode;
            this.markerName = markerName;
            this.markerGroup = markerGroup;
            this.isStale = isStale();
        }

        public void undo(){
            suspendAutoSaving();
            this.markerNode.removeMapMarker();
            setStale(this.isStale);
            resumeAutoSaving();
            getMapPanel().repaint();
        }

        public void redo(){
            suspendAutoSaving();
            this.markerNode.createMapMarker(this.markerName, this.markerGroup);
            setStale(true);
            resumeAutoSaving();
            getMapPanel().repaint();
        }
    }
}
