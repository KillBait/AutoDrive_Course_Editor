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
import static AutoDriveEditor.Listeners.MouseListener.currentMouseX;
import static AutoDriveEditor.Listeners.MouseListener.currentMouseY;
import static AutoDriveEditor.Locale.LocaleManager.getLocaleString;
import static AutoDriveEditor.Locale.LocaleManager.locale;
import static AutoDriveEditor.Managers.IconManager.*;
import static AutoDriveEditor.Managers.ShortcutManager.ShortcutID.EDIT_MARKER_SHORTCUT;
import static AutoDriveEditor.Managers.ShortcutManager.getUserShortcutByID;
import static AutoDriveEditor.Managers.ShortcutManager.registerShortcut;
import static AutoDriveEditor.XMLConfig.AutoSave.resumeAutoSaving;
import static AutoDriveEditor.XMLConfig.AutoSave.suspendAutoSaving;
import static AutoDriveEditor.XMLConfig.RoutesXML.markerGroup;

public class EditMarkerButton extends MarkerBaseButton {

    public EditMarkerButton(JPanel panel) {
        ScaleAnimIcon animEditMarkerIcon = createScaleAnimIcon(BUTTON_EDIT_MARKER_ICON, false);
        button = createAnimToggleButton(animEditMarkerIcon, panel, null, null,  false, false, this);

        Shortcut editMarkerShortcut = getUserShortcutByID(EDIT_MARKER_SHORTCUT);
        if (editMarkerShortcut != null) {
            Action editMarkerButtonAction = new AbstractAction() {
                @Override
                public void actionPerformed(ActionEvent e) {
                    MapNode selectedNode = getNodeAtScreenPosition(currentMouseX, currentMouseY);
                    if (selectedNode != null && selectedNode.hasMapMarker()) showEditMarkerDialog(selectedNode);
                }
            };
            registerShortcut(this, editMarkerShortcut, editMarkerButtonAction, getMapPanel());
        }
    }

    @Override
    public String getButtonID() { return "EditMarkerButton"; }

    @Override
    public String getButtonAction() { return "ActionButton"; }

    @Override
    public String getButtonPanel() { return "Markers"; }

    @Override
    public String getInfoText() { return getLocaleString("toolbar_markers_edit_infotext"); }

    @Override
    public void mouseClicked(MouseEvent e) {
        if (e.getButton() == MouseEvent.BUTTON1) {
            MapNode selectedNode = getNodeAtScreenPosition(e.getX(), e.getY());
            if (selectedNode != null) {
                if (selectedNode.hasMapMarker()) {
                    markerDestinationInfo info = showEditMarkerDialog(selectedNode);
                    if (info != null && info.getName() != null) {
                        if (bDebugLogMarkerInfo) LOG.info("Modifying marker at node ID {} - Name = {} --> {} , Group = {} --> {}",selectedNode.id, selectedNode.getMarkerName(), info.getName(), selectedNode.getMarkerGroup(), info.getGroup());
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
            LinkedList<MapNode> mapNodes = RoadMap.networkNodesList;
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

        option = JOptionPane.showConfirmDialog(AutoDriveEditor.editor, inputFields, getLocaleString("dialog_marker_edit_title") + " ( Node ID " + nodeMarkerToEdit.id + " )", JOptionPane.OK_CANCEL_OPTION, JOptionPane.INFORMATION_MESSAGE, getMarkerIcon());

        if (option == JOptionPane.OK_OPTION) {

            if (group[0] == null || group[0].equals("None")) group[0] = "All";
            if (destName.getText() != null && !destName.getText().isEmpty()) {
                if (nodeMarkerToEdit.getMarkerName().equals(destName.getText()) && nodeMarkerToEdit.getMarkerGroup().equals(group[0])) {
                    LOG.info("Cancelling marker edit... No Changes applied");
                    return null;
                } else {
                    // since we can't return more than 1 string, we have to package them up
                    return new markerDestinationInfo(destName.getText(), group[0]);
                }
            }
        }
        LOG.info("Cancelling marker edit");
        return null;
    }

    @Override
    public String buildToolTip() {
        Shortcut s = ShortcutManager.getUserShortcutByID(EDIT_MARKER_SHORTCUT);
        if (s != null) {
            return getLocaleString("toolbar_markers_edit_tooltip") + "\n\n( Shortcut: " + s.getShortcutString() + " )";
        } else {
            return getLocaleString("toolbar_markers_edit_tooltip");
        }
    }

    public static class MarkerEditChanger implements ChangeManager.Changeable {
        private final Boolean isStale;
        private final MapNode mapNode;
        private final String oldName;
        private final String newName;
        private final String oldGroup;
        private final String newGroup;

        public MarkerEditChanger(MapNode mapNode, String newName, String newGroup){
            super();
            this.isStale = isStale();
            this.mapNode = mapNode;
            this.oldName = mapNode.getMarkerName();
            this.newName = newName;
            this.oldGroup = mapNode.getMarkerGroup();
            this.newGroup = newGroup;
        }

        public void undo() {
            suspendAutoSaving();
            this.mapNode.setMarkerName(oldName);
            this.mapNode.setMarkerGroup(oldGroup);
            setStale(this.isStale);
            resumeAutoSaving();
            getMapPanel().repaint();
        }

        public void redo() {
            suspendAutoSaving();
            this.mapNode.setMarkerName(this.newName);
            this.mapNode.setMarkerGroup(this.newGroup);
            setStale(true);
            resumeAutoSaving();
            getMapPanel().repaint();
        }
    }
}
