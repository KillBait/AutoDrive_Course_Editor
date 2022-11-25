package AutoDriveEditor.GUI.Buttons.Markers;

import AutoDriveEditor.AutoDriveEditor;
import AutoDriveEditor.GUI.Buttons.MarkerBaseButton;
import AutoDriveEditor.Managers.ChangeManager;
import AutoDriveEditor.RoadNetwork.MapNode;
import AutoDriveEditor.RoadNetwork.MarkerGroup;
import AutoDriveEditor.RoadNetwork.RoadMap;

import javax.swing.*;
import java.awt.event.MouseEvent;
import java.text.Collator;
import java.util.ArrayList;
import java.util.Collections;
import java.util.LinkedList;
import java.util.Objects;

import static AutoDriveEditor.AutoDriveEditor.changeManager;
import static AutoDriveEditor.GUI.GUIImages.getMarkerIcon;
import static AutoDriveEditor.GUI.MenuBuilder.bDebugLogMarkerInfo;
import static AutoDriveEditor.Locale.LocaleManager.getLocaleString;
import static AutoDriveEditor.Locale.LocaleManager.locale;
import static AutoDriveEditor.MapPanel.MapPanel.*;
import static AutoDriveEditor.Utils.GUIUtils.makeImageToggleButton;
import static AutoDriveEditor.Utils.LoggerUtils.LOG;
import static AutoDriveEditor.XMLConfig.RouteManagerXML.markerGroup;

public class EditMarkerButton extends MarkerBaseButton {

    public EditMarkerButton(JPanel panel) {
        button = makeImageToggleButton("buttons/editmarker","buttons/editmarker_selected", null,"markers_edit_tooltip","markers_edit_alt", panel, false, false, null, false, this);
    }

    @Override
    public String getButtonID() { return "EditMarkerButton"; }

    @Override
    public String getButtonAction() { return "ActionButton"; }

    @Override
    public String getButtonPanel() { return "Markers"; }

    @Override
    public String getInfoText() { return getLocaleString("markers_edit_tooltip"); }

    @Override
    public void mouseClicked(MouseEvent e) {
        if (e.getButton() == MouseEvent.BUTTON1) {
            MapNode selectedNode = getNodeAt(e.getX(), e.getY());
            if (selectedNode != null) {
                if (selectedNode.hasMapMarker()) {
                    markerDestinationInfo info = showEditMarkerDialog(selectedNode);
                    if (info != null && info.getName() != null) {
                        if (bDebugLogMarkerInfo) LOG.info("{} {} - Name = {} --> {} , Group = {} --> {}", getLocaleString("console_marker_modify"), selectedNode.id, selectedNode.getMarkerName(), info.getName(), selectedNode.getMarkerGroup(), info.getGroup());
                        changeManager.addChangeable(new MarkerEditChanger(selectedNode, info.getName(), info.getGroup()));
                        if (configType == CONFIG_ROUTEMANAGER) {
                            boolean found = false;
                            for (MarkerGroup marker : markerGroup) {
                                if (Objects.equals(marker.groupName, info.getGroup())) {
                                    found = true;
                                    break;
                                }
                            }
                            if (!found && !info.getGroup().equals("All")) {
                                LOG.info("Adding new group {} to markerGroup", info.getGroup());
                                markerGroup.add(new MarkerGroup(markerGroup.size() + 1, info.getGroup()));
                            }
                        }
                        selectedNode.setMarkerName(info.getName());
                        selectedNode.setMarkerGroup(info.getGroup());
                        setStale(true);
                    }
                }
            }
        }
    }

    //
    // Dialog for marker edit
    //

    @SuppressWarnings({"rawtypes", "unchecked", "Java8ListSort"})
    private markerDestinationInfo showEditMarkerDialog(MapNode nodeMarkerToEdit) {

        String[] group = new String[1];
        int groupIndex = 0;

        JSeparator separator = new JSeparator(SwingConstants.HORIZONTAL);
        JTextField destName = new JTextField(nodeMarkerToEdit.getMarkerName());

        ArrayList<String> groupArray = new ArrayList<>();
        if (configType == CONFIG_SAVEGAME) {
            LinkedList<MapNode> mapNodes = RoadMap.mapNodes;
            for (MapNode mapNode : mapNodes) {
                if (mapNode.hasMapMarker() && !mapNode.getMarkerGroup().equals("All")) {
                    if (!groupArray.contains(mapNode.getMarkerGroup())) {
                        groupArray.add(mapNode.getMarkerGroup());
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
            groupString[i + 1] = groupArray.get(i);
            if (groupString[i + 1].equals(nodeMarkerToEdit.getMarkerGroup())) {
                groupIndex = i + 1;
            }

        }

        // edge case - set the output group to the setSelected one, this only
        // applies if the group isn't changed, otherwise it would return null
        group[0] = groupString[groupIndex];

        JComboBox comboBox = new JComboBox(groupString);
        comboBox.setEditable(true);
        comboBox.setSelectedIndex(groupIndex);
        comboBox.addActionListener(e -> {
            JComboBox cb = (JComboBox) e.getSource();
            group[0] = (String) cb.getSelectedItem();
        });

        int option;
        Object[] inputFields = new Object[0];

        if (configType == CONFIG_SAVEGAME) {
            inputFields = new Object[]{getLocaleString("dialog_marker_select_name"), destName, " ",
                    getLocaleString("dialog_marker_group_change"), comboBox, " ",
                    separator, "<html><center><b><u>NOTE</b></u>:</center>" + getLocaleString("dialog_marker_group_empty_warning") + " ",
                    " "};

        } else if (configType == CONFIG_ROUTEMANAGER) {
            inputFields = new Object[]{getLocaleString("dialog_marker_select_name"), destName, " ",
                    getLocaleString("dialog_marker_group_change"), comboBox};
        }

        option = JOptionPane.showConfirmDialog(AutoDriveEditor.editor, inputFields, "" + getLocaleString("dialog_marker_edit_title") + " ( Node ID " + nodeMarkerToEdit.id + " )", JOptionPane.OK_CANCEL_OPTION, JOptionPane.INFORMATION_MESSAGE, getMarkerIcon());

        if (option == JOptionPane.OK_OPTION) {

            if (group[0] == null || group[0].equals("None")) group[0] = "All";
            if (destName.getText() != null && destName.getText().length() > 0) {
                if (nodeMarkerToEdit.getMarkerName().equals(destName.getText()) && nodeMarkerToEdit.getMarkerGroup().equals(group[0])) {
                    LOG.info("{}", getLocaleString("console_marker_edit_cancel_nochange"));
                    return null;
                } else {
                    // since we can't return more than 1 string, we have to package them up
                    return new markerDestinationInfo(destName.getText(), group[0]);
                }
            }
        }
        LOG.info("{}", getLocaleString("console_marker_edit_cancel"));
        return null;
    }

    public static class MarkerEditChanger implements ChangeManager.Changeable {
        private final Boolean isStale;
        private final MapNode mapNode;
        //private final int mapNodeID;
        private final String oldName;
        private final String newName;
        private final String oldGroup;
        private final String newGroup;

        public MarkerEditChanger(MapNode mapNode, String newName, String newGroup){
            super();
            this.isStale = isStale();
            this.mapNode = mapNode;
            //this.mapNodeID = id;
            this.oldName = mapNode.getMarkerName();
            this.newName = newName;
            this.oldGroup = mapNode.getMarkerGroup();
            this.newGroup = newGroup;
        }

        public void undo() {
            this.mapNode.setMarkerName(oldName);
            this.mapNode.setMarkerGroup(oldGroup);
            getMapPanel().repaint();
            setStale(this.isStale);
        }

        public void redo() {
            this.mapNode.setMarkerName(this.newName);
            this.mapNode.setMarkerGroup(this.newGroup);
            getMapPanel().repaint();
            setStale(true);
        }
    }
}
