package AutoDriveEditor.Listeners;

import javax.swing.*;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.awt.event.MouseEvent;
import java.awt.event.MouseListener;

import AutoDriveEditor.AutoDriveEditor;
import AutoDriveEditor.GUI.GUIBuilder;
import AutoDriveEditor.MapPanel.MapPanel;

import static AutoDriveEditor.GUI.GUIBuilder.*;
import static AutoDriveEditor.GUI.GUIImages.*;
import static AutoDriveEditor.MapPanel.MapPanel.*;
import static AutoDriveEditor.RoadNetwork.MapNode.*;
import static AutoDriveEditor.Utils.LoggerUtils.*;


public class EditorListener implements ActionListener, MouseListener {

    public EditorListener(AutoDriveEditor editor) {
        AutoDriveEditor.editor = editor;
    }

    @Override
    public void actionPerformed(ActionEvent e) {

        LOG.info("ActionCommand: {}", e.getActionCommand());

        JFileChooser fc = new JFileChooser();
        //MapPanel.getMapPanel().isMultiSelectAllowed = false;

        switch (e.getActionCommand()) {
            case BUTTON_MOVE_NODES:
                editorState = EDITORSTATE_MOVING;
                MapPanel.getMapPanel().isMultiSelectAllowed = true;
                break;
            case BUTTON_CONNECT_NODES:
                editorState = EDITORSTATE_CONNECTING;
                connectionType = CONNECTION_STANDARD;
                MapPanel.getMapPanel().isMultiSelectAllowed = false;
                break;
            case BUTTON_CREATE_PRIMARY_NODE:
                editorState = EDITORSTATE_CREATE_PRIMARY_NODE;
                MapPanel.getMapPanel().isMultiSelectAllowed = false;
                break;
            case BUTTON_CREATE_DUAL_CONNECTION:
                editorState = EDITORSTATE_CONNECTING;
                connectionType = CONNECTION_DUAL;
                MapPanel.getMapPanel().isMultiSelectAllowed = false;
                break;
            case BUTTON_CHANGE_NODE_PRIORITY:
                editorState = EDITORSTATE_CHANGE_NODE_PRIORITY;
                MapPanel.getMapPanel().isMultiSelectAllowed = true;
                break;
            case BUTTON_CREATE_SUBPRIO_NODE:
                editorState = EDITORSTATE_CREATE_SUBPRIO_NODE;
                MapPanel.getMapPanel().isMultiSelectAllowed = false;
                break;
            case BUTTON_CREATE_REVERSE_CONNECTION:
                editorState = EDITORSTATE_CONNECTING;
                connectionType = CONNECTION_REVERSE;
                MapPanel.getMapPanel().isMultiSelectAllowed = false;
                break;
            case BUTTON_REMOVE_NODES:
                editorState = EDITORSTATE_DELETE_NODES;
                MapPanel.getMapPanel().isMultiSelectAllowed = true;
                break;
            case BUTTON_CREATE_DESTINATIONS:
                editorState = EDITORSTATE_CREATING_DESTINATION;
                MapPanel.getMapPanel().isMultiSelectAllowed = false;
                break;
            case BUTTON_EDIT_DESTINATIONS_GROUPS:
                editorState = EDITORSTATE_EDITING_DESTINATION;
                MapPanel.getMapPanel().isMultiSelectAllowed = false;
                break;
            case BUTTON_DELETE_DESTINATIONS:
                editorState = EDITORSTATE_DELETING_DESTINATION;
                MapPanel.getMapPanel().isMultiSelectAllowed = false;
                break;
            case BUTTON_ALIGN_HORIZONTAL:
                editorState = EDITORSTATE_ALIGN_HORIZONTAL;
                MapPanel.getMapPanel().isMultiSelectAllowed = true;
                break;
            case BUTTON_ALIGN_VERTICAL:
                editorState = EDITORSTATE_ALIGN_VERTICAL;
                MapPanel.getMapPanel().isMultiSelectAllowed = true;
                break;
            case BUTTON_ALIGN_DEPTH:
                editorState = EDITORSTATE_ALIGN_DEPTH;
                MapPanel.getMapPanel().isMultiSelectAllowed = true;
                break;
            case BUTTON_ALIGN_EDIT_NODE:
                editorState = EDITORSTATE_ALIGN_EDIT_NODE;
                MapPanel.getMapPanel().isMultiSelectAllowed = false;
                break;
            case BUTTON_CREATE_QUADRATICBEZIER:
                editorState = EDITORSTATE_QUADRATICBEZIER;
                MapPanel.getMapPanel().isMultiSelectAllowed = false;
                break;
            case BUTTON_CREATE_CUBICBEZIER:
                editorState = EDITORSTATE_CUBICBEZIER;
                MapPanel.getMapPanel().isMultiSelectAllowed = false;
                break;
            case BUTTON_COMMIT_CURVE:
                MapPanel.getMapPanel().confirmCurve();
                break;
            case BUTTON_CANCEL_CURVE:
                MapPanel.getMapPanel().cancelCurve();
                break;
            case BUTTON_COPYPASTE_SELECT:
                editorState = EDITORSTATE_CNP_SELECT;
                MapPanel.getMapPanel().isMultiSelectAllowed = true;
                break;
            case BUTTON_COPYPASTE_CUT:
                MapPanel.getMapPanel().isMultiSelectAllowed = false;
                cutSelected();
                break;
            case BUTTON_COPYPASTE_COPY:
                MapPanel.getMapPanel().isMultiSelectAllowed = false;
                copySelected();
                break;
            case BUTTON_COPYPASTE_PASTE:
                MapPanel.getMapPanel().isMultiSelectAllowed = false;
                pasteSelected();
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

    @Override
    public void mousePressed(MouseEvent e) {
    }

    @Override
    public void mouseReleased(MouseEvent e) {
    }

    @Override
    public void mouseEntered(MouseEvent e) {
    }

    @Override
    public void mouseExited(MouseEvent e) {
    }
}


