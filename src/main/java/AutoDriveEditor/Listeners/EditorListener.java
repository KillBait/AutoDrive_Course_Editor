package AutoDriveEditor.Listeners;

import AutoDriveEditor.AutoDriveEditor;
import AutoDriveEditor.GUI.GUIBuilder;
import AutoDriveEditor.MapPanel.MapPanel;

import javax.swing.*;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.awt.event.MouseEvent;
import java.awt.event.MouseListener;

import static AutoDriveEditor.AutoDriveEditor.DEBUG;
import static AutoDriveEditor.AutoDriveEditor.editor;
import static AutoDriveEditor.GUI.ConfigGUI.createConfigGUI;
import static AutoDriveEditor.GUI.GUIBuilder.*;
import static AutoDriveEditor.GUI.GUIImages.*;
import static AutoDriveEditor.Locale.LocaleManager.localeString;
import static AutoDriveEditor.MapPanel.MapPanel.*;
import static AutoDriveEditor.RoadNetwork.MapNode.NODE_FLAG_STANDARD;
import static AutoDriveEditor.Utils.LoggerUtils.LOG;
import static AutoDriveEditor.XMLConfig.EditorXML.bContinuousConnections;
import static AutoDriveEditor.XMLConfig.EditorXML.nodeSize;


public class EditorListener implements ActionListener, MouseListener {

    public EditorListener(AutoDriveEditor editor) {
        AutoDriveEditor.editor = editor;
    }

    @Override
    public void actionPerformed(ActionEvent e) {

        if (DEBUG) LOG.info("Editor ActionCommand: {}", e.getActionCommand());

        switch (e.getActionCommand()) {
            case BUTTON_MOVE_NODES:
                editorState = EDITORSTATE_MOVING;
                isMultiSelectAllowed = true;
                break;
            case BUTTON_CONNECT_NODES:
                editorState = EDITORSTATE_CONNECTING;
                connectionType = CONNECTION_STANDARD;
                isMultiSelectAllowed = false;
                break;
            case BUTTON_CREATE_PRIMARY_NODE:
                editorState = EDITORSTATE_CREATE_PRIMARY_NODE;
                isMultiSelectAllowed = false;
                break;
            case BUTTON_CREATE_DUAL_CONNECTION:
                editorState = EDITORSTATE_CONNECTING;
                connectionType = CONNECTION_DUAL;
                isMultiSelectAllowed = false;
                break;
            case BUTTON_CHANGE_NODE_PRIORITY:
                editorState = EDITORSTATE_CHANGE_NODE_PRIORITY;
                isMultiSelectAllowed = true;
                break;
            case BUTTON_CREATE_SUBPRIO_NODE:
                editorState = EDITORSTATE_CREATE_SUBPRIO_NODE;
                isMultiSelectAllowed = false;
                break;
            case BUTTON_CREATE_REVERSE_CONNECTION:
                editorState = EDITORSTATE_CONNECTING;
                connectionType = CONNECTION_REVERSE;
                isMultiSelectAllowed = false;
                break;
            case BUTTON_REMOVE_NODES:
                editorState = EDITORSTATE_DELETE_NODES;
                isMultiSelectAllowed = true;
                break;
            case BUTTON_CREATE_DESTINATIONS:
                editorState = EDITORSTATE_CREATE_MARKER;
                isMultiSelectAllowed = false;
                break;
            case BUTTON_EDIT_DESTINATIONS_GROUPS:
                editorState = EDITORSTATE_EDIT_MARKER;
                isMultiSelectAllowed = false;
                break;
            case BUTTON_DELETE_DESTINATIONS:
                editorState = EDITORSTATE_DELETE_MARKER;
                isMultiSelectAllowed = false;
                break;
            case BUTTON_ALIGN_HORIZONTAL:
                editorState = EDITORSTATE_ALIGN_HORIZONTAL;
                isMultiSelectAllowed = true;
                break;
            case BUTTON_ALIGN_VERTICAL:
                editorState = EDITORSTATE_ALIGN_VERTICAL;
                isMultiSelectAllowed = true;
                break;
            case BUTTON_ALIGN_DEPTH:
                editorState = EDITORSTATE_ALIGN_DEPTH;
                isMultiSelectAllowed = true;
                break;
            case BUTTON_ALIGN_EDIT_NODE:
                editorState = EDITORSTATE_ALIGN_EDIT_NODE;
                isMultiSelectAllowed = false;
                break;
            case BUTTON_CREATE_QUADRATICBEZIER:
                editorState = EDITORSTATE_QUADRATICBEZIER;
                isMultiSelectAllowed = false;
                break;
            case BUTTON_CREATE_CUBICBEZIER:
                editorState = EDITORSTATE_CUBICBEZIER;
                isMultiSelectAllowed = false;
                break;
            case BUTTON_COMMIT_CURVE:
                MapPanel.getMapPanel().confirmCurve();
                break;
            case BUTTON_CANCEL_CURVE:
                MapPanel.getMapPanel().cancelCurve();
                break;
            case BUTTON_COPYPASTE_SELECT:
                editorState = EDITORSTATE_CNP_SELECT;
                isMultiSelectAllowed = true;
                break;
            case BUTTON_COPYPASTE_CUT:
                isMultiSelectAllowed = false;
                cutSelected();
                break;
            case BUTTON_COPYPASTE_COPY:
                isMultiSelectAllowed = false;
                copySelected();
                break;
            case BUTTON_COPYPASTE_PASTE:
                isMultiSelectAllowed = false;
                pasteSelected();
                break;
            case BUTTON_OPTIONS_OPEN_CONFIG:
                createConfigGUI(editor);
                break;
            case BUTTON_OPTIONS_NODE_SIZE_INCREASE:
                nodeSize += 0.10;
                mapPanel.repaint();
                break;
            case BUTTON_OPTIONS_NODE_SIZE_DECREASE:
                if (nodeSize >= 0.11) {
                    nodeSize -= 0.10;
                    mapPanel.repaint();
                }
                break;
            case BUTTON_OPTIONS_NETWORK_INFO:
                break;
            case BUTTON_OPTIONS_CON_CONNECT:
                bContinuousConnections = !bContinuousConnections;
                if (bContinuousConnections) {
                    conConnect.setToolTipText(localeString.getString("options_con_connect_enabled_tooltip"));
                } else {
                    conConnect.setToolTipText(localeString.getString("options_con_connect_disabled_tooltip"));
                }
                break;
        }
        updateButtons();
    }

    @Override
    public void mouseClicked(MouseEvent e) {
        if (SwingUtilities.isRightMouseButton(e) && e.getClickCount() == 1) {
            JToggleButton toggleStateButton = (JToggleButton) e.getSource();
            if (toggleStateButton.isEnabled()) {
                if (toggleStateButton == GUIBuilder.createRegularConnection) {
                    createRegularConnectionState = 1 - createRegularConnectionState;
                    if (createRegularConnectionState == NODE_FLAG_STANDARD) { // == 0
                        createRegularConnection.setIcon(regularConnectionIcon);
                        createRegularConnection.setSelectedIcon(regularConnectionSelectedIcon);
                    } else {
                        createRegularConnection.setIcon(regularConnectionSubPrioIcon);
                        createRegularConnection.setSelectedIcon(regularConnectionSubPrioSelectedIcon);
                    }
                } else if (toggleStateButton == createDualConnection) {
                    createDualConnectionState = 1 - createDualConnectionState;
                    if (createDualConnectionState == NODE_FLAG_STANDARD) { // == 0
                        createDualConnection.setIcon(dualConnectionIcon);
                        createDualConnection.setSelectedIcon(dualConnectionSelectedIcon);
                    } else {
                        createDualConnection.setIcon(dualConnectionSubPrioIcon);
                        createDualConnection.setSelectedIcon(dualConnectionSubPrioSelectedIcon);
                    }
                } else if (toggleStateButton == createReverseConnection) {
                    createReverseConnectionState = 1 - createReverseConnectionState;
                    if (createReverseConnectionState == NODE_FLAG_STANDARD) { // == 0
                        createReverseConnection.setIcon(reverseConnectionIcon);
                        createReverseConnection.setSelectedIcon(reverseConnectionSelectedIcon);
                    } else {
                        createReverseConnection.setIcon(reverseConnectionSubPrioIcon);
                        createReverseConnection.setSelectedIcon(reverseConnectionSubPrioSelectedIcon);
                    }
                }
            }
        }
    }

    // need to implement these, but are not used

    @Override
    public void mousePressed(MouseEvent e) {}

    @Override
    public void mouseReleased(MouseEvent e) {}

    @Override
    public void mouseEntered(MouseEvent e) {}

    @Override
    public void mouseExited(MouseEvent e) {}
}


