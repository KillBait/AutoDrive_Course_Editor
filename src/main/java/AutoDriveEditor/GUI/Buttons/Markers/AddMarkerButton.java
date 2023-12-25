package AutoDriveEditor.GUI.Buttons.Markers;

import AutoDriveEditor.AutoDriveEditor;
import AutoDriveEditor.GUI.Buttons.MarkerBaseButton;
import AutoDriveEditor.Managers.ChangeManager;
import AutoDriveEditor.RoadNetwork.MapNode;
import AutoDriveEditor.RoadNetwork.MarkerGroup;
import AutoDriveEditor.RoadNetwork.RoadMap;
import AutoDriveEditor.XMLConfig.RoutesXML;

import javax.swing.*;
import java.awt.event.MouseEvent;
import java.text.Collator;
import java.util.ArrayList;
import java.util.Collections;
import java.util.LinkedList;
import java.util.Objects;

import static AutoDriveEditor.AutoDriveEditor.changeManager;
import static AutoDriveEditor.AutoDriveEditor.getMapPanel;
import static AutoDriveEditor.GUI.EditorImages.getMarkerIcon;
import static AutoDriveEditor.GUI.MapPanel.*;
import static AutoDriveEditor.GUI.Menus.DebugMenu.Logging.LogMarkerInfoMenu.bDebugLogMarkerInfo;
import static AutoDriveEditor.GUI.TextPanel.showInTextArea;
import static AutoDriveEditor.Locale.LocaleManager.getLocaleString;
import static AutoDriveEditor.Locale.LocaleManager.locale;
import static AutoDriveEditor.Utils.GUIUtils.makeImageToggleButton;
import static AutoDriveEditor.Utils.LoggerUtils.LOG;
import static AutoDriveEditor.XMLConfig.RoutesXML.markerGroup;

public class AddMarkerButton extends MarkerBaseButton {

    public AddMarkerButton(JPanel panel) {
        button = makeImageToggleButton("buttons/addmarker","buttons/addmarker_selected", null,"markers_add_tooltip","markers_add_alt", panel, false, false, null, false, this);
    }

    @Override
    public String getButtonID() { return "AddMarkerButton"; }

    @Override
    public String getButtonAction() { return "ActionButton"; }

    @Override
    public String getButtonPanel() { return "Markers"; }

    @Override
    public String getInfoText() { return getLocaleString("markers_add_tooltip"); }

    @Override
    public void mouseClicked(MouseEvent e) {
        if (e.getButton() == MouseEvent.BUTTON1) {
            MapNode selectedNode = getNodeAtScreenPosition(e.getX(), e.getY());
            if (selectedNode != null) {
                if (selectedNode.hasMapMarker()) {
                    showInTextArea(getLocaleString("console_marker_add_exists"), true, true);
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

        int option = JOptionPane.showConfirmDialog(AutoDriveEditor.editor, inputFields, ""+ getLocaleString("dialog_marker_add_title") + " ( Node ID " + selectedNode.id +" )", JOptionPane.OK_CANCEL_OPTION, JOptionPane.INFORMATION_MESSAGE, getMarkerIcon());

        if (option == JOptionPane.OK_OPTION) {

            if (group[0] == null || group[0].equals("None")) group[0] = "All";
            if (destName.getText() != null && destName.getText().length() > 0) {
                // since we can't return more than 1 string, we have to package them up
                LOG.info("{} {} - Name = {} , Group = {}", getLocaleString("console_marker_add"), selectedNode.id, destName.getText(), group[0]);
                createMarkerForNode(selectedNode, destName.getText(), group[0]);
                getMapPanel().repaint();
            } else {
                LOG.info("{}", getLocaleString("console_marker_add_cancel_no_name"));
            }
            return;
        }
        LOG.info("{}", getLocaleString("console_marker_add_cancel"));
    }

    @SuppressWarnings("EqualsBetweenInconvertibleTypes")
    public void createMarkerForNode(MapNode mapNode, String newMarkerName, String newMarkerGroup) {
        if (mapNode != null && newMarkerName != null && newMarkerName.length() > 0) {
            if (newMarkerGroup == null) newMarkerGroup = "All";
            //MapMarker mapMarker = new MapMarker(newMarkerName, newMarkerGroup);
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

    public static class MarkerAddChanger implements ChangeManager.Changeable {
        //private final MapMarker markerToChange;
        private final MapNode markerNode;
        private final String markerName;
        private final String markerGroup;
        private final Boolean isStale;

        public MarkerAddChanger(MapNode mapNode, String markerName, String markerGroup){
            super();
            //this.markerToChange = mapMarker;
            this.markerNode = mapNode;
            this.markerName = markerName;
            this.markerGroup = markerGroup;
            this.isStale = isStale();
        }

        public void undo(){
            this.markerNode.removeMapMarker();
            //RoadMap.removeMapMarker(this.markerToChange);
            getMapPanel().repaint();
            setStale(this.isStale);
        }

        public void redo(){
            this.markerNode.createMapMarker(this.markerName, this.markerGroup);
            //roadMap.createMapMarker(this.markerToChange);
            getMapPanel().repaint();
            setStale(true);
        }
    }
}
