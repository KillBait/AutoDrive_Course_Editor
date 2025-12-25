package AutoDriveEditor.GUI.Buttons;

import AutoDriveEditor.Classes.CircularList;
import AutoDriveEditor.Classes.KeyBinds.Shortcut;
import AutoDriveEditor.Classes.KeyBinds.ShortcutGroup;
import AutoDriveEditor.Classes.LinearLine;
import AutoDriveEditor.Managers.ButtonManager;
import AutoDriveEditor.RoadNetwork.MapNode;

import javax.swing.*;
import java.awt.event.ActionEvent;
import java.awt.event.MouseEvent;

import static AutoDriveEditor.AutoDriveEditor.buttonManager;
import static AutoDriveEditor.AutoDriveEditor.getMapPanel;
import static AutoDriveEditor.Classes.Util_Classes.LoggerUtils.LOG;
import static AutoDriveEditor.GUI.MapPanel.getNodeAtScreenPosition;
import static AutoDriveEditor.GUI.Menus.DebugMenu.Logging.LogLinearLineInfoMenu.bDebugLogLinearlineInfo;
import static AutoDriveEditor.GUI.TextPanel.showInTextArea;
import static AutoDriveEditor.Locale.LocaleManager.getLocaleString;
import static AutoDriveEditor.Managers.ShortcutManager.*;
import static AutoDriveEditor.Managers.ShortcutManager.ShortcutID.NEW_LINEAR_LINE_SHORTCUT;
import static AutoDriveEditor.XMLConfig.EditorXML.bContinuousConnections;
import static AutoDriveEditor.XMLConfig.EditorXML.bCreateLinearLineEndNode;

public abstract class LinerLineBaseButton extends BaseButton implements ShortcutGroup.ShortcutGroups {

    protected final String NORMAL_PRIORITY_STRING = "Normal";
    protected final String SUBPRIO_PRIORITY_STRING = "Subprio";

    protected static LinearLine linearLine = new LinearLine();
    boolean isSelectingEndPoint = false;
    protected static boolean shortcutChanged;

    protected static ShortcutGroup linerLineShortcutGroup;



    @Override
    public void onButtonCreation() {
        if (linerLineShortcutGroup == null) {
            linerLineShortcutGroup = ShortcutGroup.createShortcutGroup(LINEAR_LINE_GROUP);

            Shortcut linearLineShortcut = getUserShortcutByID(NEW_LINEAR_LINE_SHORTCUT);
            if (linearLineShortcut != null) {
                Action linearLineButtonAction = new AbstractAction() {
                    @Override
                    public void actionPerformed(ActionEvent e) {
                        if (button.isEnabled()) {
                            ShortcutGroup lineGroup = ShortcutGroup.getButtonGroup(LINEAR_LINE_GROUP);
                            if (lineGroup != null) {
                                shortcutChanged = true;
                                if (buttonManager.getCurrentButton() != null && buttonManager.getCurrentButton() == lineGroup.getCurrentButton()) {
                                    ButtonManager.ButtonNode next = lineGroup.getNextButton().getButtonNode();
                                    buttonManager.makeCurrent(next);
                                } else {
                                    buttonManager.makeCurrent(lineGroup.getCurrentButton().getButtonNode());
                                }
                                shortcutChanged = false;

                            }
                        }
                    }
                };
                registerShortcut(this, linearLineShortcut, linearLineButtonAction, getMapPanel());
            }
        }
    }

    @Override
    public void onButtonDeselect() {
        if (!shortcutChanged && linearLine != null) resetLinearLine();
    }

    @Override
    public ShortcutGroup getGroup() { return linerLineShortcutGroup; }

    @Override
    public CircularList<BaseButton> getGroupMembers() { return linerLineShortcutGroup.getButtonNodeList(); }


    @Override
    public void mousePressed(MouseEvent e) {
        if (e.getButton() == MouseEvent.BUTTON1) {
            // Check if a MapNode is at the location the left mouse button was pressed
            MapNode selected = getNodeAtScreenPosition(e.getX(), e.getY());
            // Check if a line start node is selected
            if (linearLine.getStartNode() == null) {
                // No start node has been selected previously, set the start node.
                if (selected != null && (selected.isMapNode() && selected.isSelectable())) {
                    // set the Linear line start node and end location
                    linearLine.setStartNode(selected);
                    linearLine.setEndLocation(e.getX(), e.getY());
                    if (bDebugLogLinearlineInfo) LOG.info("## LinearLineBaseButton.mousePressed() ## Updated linear line start node ID to {} { x={} , y={} , z={} }", selected.id, selected.x, selected.y, selected.z);
                    isSelectingEndPoint = true;
                    if (bDebugLogLinearlineInfo) showInTextArea(getLocaleString("toolbar_nodes_connection_started_infotext"), true, false);
                }
            } else {
                // A start node has been selected, check if the location of the mouse press is over a MapNode
                if (selected != null) {
                    // User selected an MapNode as the end point, Check if the selected node is the same
                    // as the start node, cancel the linear line if they are the same
                    if (selected == linearLine.getStartNode()) {
                        if (bDebugLogLinearlineInfo) LOG.info("## LinearLine Debug ## canceling linear line");
                        showInTextArea(getLocaleString("toolbar_nodes_connection_canceled_infotext"), true, false);
                        resetLinearLine();
                    } else {
                        // Selected node was different from the starting node, check if the selected node is not control node
                        if (selected.isMapNode()) {
                            // Create the linear line ending at the selected point
                            if (bDebugLogLinearlineInfo) LOG.info("## LinearLine Debug ## End node selected, creating linear line");
                            linearLine.commit();
                            // If we have Continuous connections enabled, start a new line at the selected end point
                            // otherwise reset the required values to allow a new line creation.
                            if (bContinuousConnections) {
                                if (bDebugLogLinearlineInfo) {
                                    LOG.info("## LinearLine Debug ## Continuous connection enabled, starting next line from ID {}", selected.id);
                                }
                                linearLine.updateLineStartNode(selected);
                            } else {
                                // Linear line is finished, reset values
                                if (bDebugLogLinearlineInfo) LOG.info("## LinearLine Debug ## Linear line finished");
                                resetLinearLine();
                            }
                        }
                    }
                } else {
                    // No end node was selected, check if the config option to createSetting end nodes is enabled
                    if (bDebugLogLinearlineInfo) LOG.info("## LinearLine Debug ## No end node selected");
                    if (bCreateLinearLineEndNode) {
                        if (bDebugLogLinearlineInfo) LOG.info("## LinearLine Debug ## Create end node enabled in preferences");
                        linearLine.commit();
                        if (bContinuousConnections) {
                            if (bDebugLogLinearlineInfo) LOG.info("## LinearLine Debug ## Continuous connection is enabled, Creating new line from end node");
                            MapNode newStart = (linearLine.getAddToNetworkNodeList().isEmpty()) ? linearLine.getEndNode() : linearLine.getAddToNetworkNodeList().getLast();
                            linearLine.updateLineStartNode(newStart);
                        } else {
                            resetLinearLine();
                        }
                    }
                }
            }
        }

        if (e.getButton() == MouseEvent.BUTTON3) {
            resetLinearLine();
            showInTextArea(getLocaleString("toolbar_nodes_connection_canceled_infotext"), true, false);

        }
    }

    private void resetLinearLine() {
        //startNode = null;
        isSelectingEndPoint = false;
        if (linearLine != null) { linearLine.clear(); }
        getMapPanel().repaint();
    }
}
