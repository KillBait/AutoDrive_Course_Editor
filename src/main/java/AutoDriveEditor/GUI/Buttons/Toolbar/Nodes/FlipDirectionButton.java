package AutoDriveEditor.GUI.Buttons.Toolbar.Nodes;

import AutoDriveEditor.Classes.KeyBinds.Shortcut;
import AutoDriveEditor.Classes.UI_Components.ScaleAnimIcon;
import AutoDriveEditor.GUI.Buttons.ConnectionSelectBaseButton;
import AutoDriveEditor.Managers.ChangeManager;
import AutoDriveEditor.Managers.ShortcutManager;
import AutoDriveEditor.RoadNetwork.Connection;

import javax.swing.*;
import java.awt.*;
import java.awt.event.ActionEvent;
import java.awt.event.MouseEvent;
import java.util.ArrayList;
import java.util.Iterator;

import static AutoDriveEditor.AutoDriveEditor.*;
import static AutoDriveEditor.Classes.Util_Classes.LoggerUtils.LOG;
import static AutoDriveEditor.GUI.MapPanel.*;
import static AutoDriveEditor.GUI.Menus.DebugMenu.Logging.LogFlipConnectionMenu.bDebugLogFlipConnection;
import static AutoDriveEditor.Locale.LocaleManager.getLocaleString;
import static AutoDriveEditor.Managers.IconManager.*;
import static AutoDriveEditor.Managers.ShortcutManager.ShortcutID.FLIP_CONNECTION_SHORTCUT;
import static AutoDriveEditor.Managers.ShortcutManager.getUserShortcutByID;
import static AutoDriveEditor.Managers.ShortcutManager.registerShortcut;
import static AutoDriveEditor.RoadNetwork.MapNode.NODE_FLAG_REGULAR;
import static AutoDriveEditor.RoadNetwork.MapNode.NODE_FLAG_SUBPRIO;
import static AutoDriveEditor.XMLConfig.AutoSave.resumeAutoSaving;
import static AutoDriveEditor.XMLConfig.AutoSave.suspendAutoSaving;


public final class FlipDirectionButton extends ConnectionSelectBaseButton {

    public FlipDirectionButton(JPanel panel) {
        ScaleAnimIcon animSwapDirectionIcon = createScaleAnimIcon(BUTTON_FLIP_CONNECTION_ICON, false);
        button = createAnimToggleButton(animSwapDirectionIcon, panel, null, null,  false, false, this);

        // Setup Keyboard Shortcuts
        Shortcut moveShortcut = getUserShortcutByID(FLIP_CONNECTION_SHORTCUT);
        if (moveShortcut != null) {
            Action moveButtonAction = new AbstractAction() {
                @Override
                public void actionPerformed(ActionEvent e) {
                    if (button.isEnabled() && !button.isSelected()) {
                        buttonManager.makeCurrent(buttonNode);
                    } else {
                        buttonManager.deSelectAll();
                    }
                }
            };
            registerShortcut(this, moveShortcut, moveButtonAction, getMapPanel());
        }
    }

    @Override
    public String getButtonID() {
        return "FlipDirectionButton";
    }

    @Override
    public String getButtonAction() {
        return "ActionButton";
    }

    @Override
    public String getButtonPanel() {
        return "Nodes";
    }

    @Override
    public String getInfoText() {
        return getLocaleString("toolbar_nodes_flip_infotext");
    }

    @Override
    public Integer getLineDetectionInterval() {
        return 20;
    }

    @Override
    public Boolean showHoverNodeSelect() { return false; }

    @Override
    public void mouseReleased(MouseEvent e) {

        // Normally it would be a simple task to remove all the connections in one go and
        // immediately createSetting all the flipped connections, but, this isn't possible due
        // to one special case, it is possible to have two nodes with a reverse connection
        // and a normal connection in the opposite direction.
        //
        // Due to how the logic in the original createConnectionBetween() function works
        // for reverse connections, we have to follow a specific order.
        //
        // (1) remove all the regular connections first
        // (2) remove all the reverse connections
        // (3) add back the flipped reverse connections
        // (4) add back the flipped regular connections
        //
        // TODO: Modify the createConnectionBetween() logic for reverse connections
        //       so that adding a reverse connection doesn't wipe all others.
        //       This will allow the use of a single function for all the connections
        //       and use one for() loop to iterate through each entry in the array list
        //       and flip the connection immediately.

        ArrayList<Connection> crossedList = new ArrayList<>();
        for (Connection con : connectionsList) {
            if (con.isCrossed()) {
                Connection opposite = new Connection(con.getEndNode(), con.getStartNode(), (con.isCrossedRegular()) ? Connection.ConnectionType.CROSSED_REVERSE : Connection.ConnectionType.CROSSED_REGULAR);
                if (!connectionsList.contains(opposite)) {
                    if (bDebugLogFlipConnection) LOG.info("    ## FlipDirectionButton Debug ## Did NOT find opposite Connection, adding {} --> {} ( {} )", opposite.getStartNode().id, opposite.getEndNode().id, opposite.getConnectionType());
                    crossedList.add(opposite);
                }
            }
        }
        connectionsList.addAll(crossedList);

        if (!connectionsList.isEmpty()) {
            changeManager.addChangeable(new FlipConnectionChanger(connectionsList));

            //
            // WARNING: Until createConnections has been re-factored, the connection
            // removal/creation MUST be done in this order
            //

            if (bDebugLogFlipConnection) LOG.info("Adjusting {} connections", connectionsList.size());

            // removeOriginalNodes all the selected regular connection first
            if (bDebugLogFlipConnection) LOG.info("removing regular connections");
            removeRegularConnections(connectionsList);

            // removeOriginalNodes all the selected reverse connections second
            if (bDebugLogFlipConnection) LOG.info("removing reverse connections");
            removeReverseConnections(connectionsList);

            //
            // now the selected connections are removed, we can add back the flipped versions
            //

            // add back the flipped reverse connections first
            if (bDebugLogFlipConnection) LOG.info("adding reverse connections");
            addFlippedReverseConnections(connectionsList);

            // add back the flipped regular connections second
            if (bDebugLogFlipConnection) LOG.info("adding regular connections");
            addFlippedRegularConnections(connectionsList);
            if (bDebugLogFlipConnection) LOG.info("--------------------------");

            resetConnectionList();
            setStale(true);
            getMapPanel().repaint();
        }
    }

    @Override
    protected void filterConnections() {
        Iterator<Connection> iterator = connectionsList.iterator();
        while (iterator.hasNext()) {
            Connection connection = iterator.next();
            if (connection.getConnectionType() == Connection.ConnectionType.DUAL || !connection.isSelectable()) {
                iterator.remove();
                continue;
            }
            connection.getStartNode().getPreviewConnectionHiddenList().add(connection.getEndNode());
        }
    }

    private static void removeRegularConnections(ArrayList<Connection> removeList) {
        if (!removeList.isEmpty()) {
            for (Connection connection : removeList) {
                if (connection.isRegular()) {
                    if (bDebugLogFlipConnection) {
                        if (connection.getStartNode().flag == NODE_FLAG_REGULAR) {
                            LOG.info("## Remove Regular {} --> {} isEnabled {}", connection.getStartNode().id, connection.getEndNode().id, connection.isHidden());
                        } else if (connection.getStartNode().flag == NODE_FLAG_SUBPRIO) {
                            LOG.info("## Remove Subprio {} --> {} isEnabled {}", connection.getStartNode().id, connection.getEndNode().id, connection.isHidden());
                        } else {
                            LOG.info("## Ignoring UNKNOWN Regular Connection ## {} --> {}", connection.getStartNode().id, connection.getEndNode().id);
                            continue;
                        }
                    }
                    if (connection.isHidden()) connection.getStartNode().getHiddenConnectionsList().remove(connection.getEndNode());
                    createConnectionBetween(connection.getStartNode(), connection.getEndNode(), connection.getConnectionType());
                }
            }
        }
    }

    private static void removeReverseConnections(ArrayList<Connection> removeList) {
        if (!removeList.isEmpty()) {
            for (Connection connection : removeList) {
                if (connection.isReverse()) {
                    if (bDebugLogFlipConnection) {
                        if (connection.getStartNode().flag == NODE_FLAG_REGULAR) {
                            LOG.info("## Remove Reverse Regular {} --> {} isEnabled {}", connection.getStartNode().id, connection.getEndNode().id,connection.isHidden());
                        } else if (connection.getStartNode().flag == NODE_FLAG_SUBPRIO) {
                            LOG.info("## Remove Reverse Subprio {} --> {} isEnabled {}", connection.getStartNode().id, connection.getEndNode().id, connection.isHidden());
                        } else {
                            LOG.info("## Ignoring UNKNOWN Reverse Connection ## {} --> {}", connection.getStartNode().id, connection.getEndNode().id);
                            continue;
                        }
                    }
                    createConnectionBetween(connection.getStartNode(), connection.getEndNode(), connection.getConnectionType());
                }
            }
        }
    }

    private static void addFlippedRegularConnections(ArrayList<Connection> removeList) {
        if (!removeList.isEmpty()) {
            for (Connection connection : removeList) {
                if (connection.isRegular()) {
                    if (bDebugLogFlipConnection) {
                        if (connection.getEndNode().flag == NODE_FLAG_REGULAR || connection.getConnectionType() == Connection.ConnectionType.SUBPRIO) {
                            LOG.info("## Add Regular {} --> {} isEnabled {}", connection.getEndNode().id, connection.getStartNode().id, connection.isHidden());
                        } else if (connection.getEndNode().flag == NODE_FLAG_SUBPRIO) {
                            LOG.info("## Add Subprio {} --> {} isEnabled {}", connection.getEndNode().id, connection.getStartNode().id, connection.isHidden());
                        } else {
                            LOG.info("## Ignoring UNKNOWN standard Connection ## {} --> {}", connection.getEndNode().id, connection.getStartNode().id);
                            continue;
                        }
                    }
                    createConnectionBetween(connection.getEndNode(), connection.getStartNode(), connection.getConnectionType());
                    if (connection.isHidden()) connection.getEndNode().getHiddenConnectionsList().add(connection.getStartNode());
                }
            }
        }
    }

    private static void addFlippedReverseConnections(ArrayList<Connection> removeList) {
        if (!removeList.isEmpty()) {
            for (Connection connection : removeList) {
                if (connection.isReverse()) {
                    if (bDebugLogFlipConnection) {
                        if (connection.getEndNode().flag == NODE_FLAG_REGULAR) {
                            LOG.info("## Add Reverse Regular {} --> {} isEnabled {}", connection.getEndNode().id, connection.getStartNode().id, connection.isHidden());
                        } else if (connection.getEndNode().flag == NODE_FLAG_SUBPRIO) {
                            LOG.info("## Add Reverse Subprio {} --> {} isEnabled {}", connection.getEndNode().id, connection.getStartNode().id, connection.isHidden());
                        } else {
                            LOG.info("## Ignoring UNKNOWN Reverse Connection ## {} --> {}", connection.getEndNode().id, connection.getStartNode().id);
                            continue;
                        }
                    }
                    createConnectionBetween(connection.getEndNode(), connection.getStartNode(), connection.getConnectionType());
                    if (connection.isHidden()) connection.getEndNode().getHiddenConnectionsList().add(connection.getStartNode());
                }
            }
        }
    }

    @Override
    public void drawToScreen(Graphics g) {
        if (buttonManager.getCurrentButton() == this) {
            if (!connectionsList.isEmpty()) {
                Graphics2D g1 = (Graphics2D) g.create();
                Color colour = Color.GREEN;
                boolean isDual = false;
                g1.setStroke(new BasicStroke(nodeSizeScaledQuarter));
                for (Connection connection : connectionsList) {
                    Point p1 = worldPosToScreenPos(connection.getStartNode().getX(), connection.getStartNode().getZ());
                    Point p2 = worldPosToScreenPos(connection.getEndNode().getX(), connection.getEndNode().getZ());
                    drawArrowBetween(g1, p2, p1, isDual, connection.getColor());
                }
                g1.dispose();
            }
        }
    }

    @Override
    public void onButtonSelect() {
        connectionTypeFilter = Connection.ConnectionType.NONE;
    }

    @Override
    public String buildToolTip() {
        Shortcut s = ShortcutManager.getUserShortcutByID(FLIP_CONNECTION_SHORTCUT);
        if (s != null) {
            return getLocaleString("toolbar_nodes_flip_tooltip") + "\n\n( Shortcut: " + s.getShortcutString() + " )";
        } else {
            return getLocaleString("toolbar_nodes_flip_tooltip");
        }
    }

    //
    // TODO: Make the undo/redo process swap the hidden state of the connection.
    //       Temporarily removed it for the moment
    //

    @SuppressWarnings("LoggingSimilarMessage")
    public static class FlipConnectionChanger implements ChangeManager.Changeable {
        private final ArrayList<Connection> flippedList = new ArrayList<>();
        private final boolean isStale;

        public FlipConnectionChanger(ArrayList<Connection> connectionList) {
            super();
            for (Connection connection : connectionList) {
                // Remove the hidden status for the connection
                connection.getStartNode().getHiddenConnectionsList().remove(connection.getEndNode());
                flippedList.add(new Connection(connection.getStartNode(), connection.getEndNode(), connection.getConnectionType()));
            }
            this.isStale = isStale();
        }

        public void undo() {
            suspendAutoSaving();
            // removeOriginalNodes all the flipped regular connections first
            if (bDebugLogFlipConnection) LOG.info("undo() removing flipped regular connections");
            addFlippedRegularConnections(flippedList);

            // removeOriginalNodes all the flipped reverse connections second
            if (bDebugLogFlipConnection) LOG.info("undo removing flipped reverse connections");
            addFlippedReverseConnections(flippedList);

            // add back the original reverse connections first
            if (bDebugLogFlipConnection) LOG.info("undo() restoring original reverse connections");
            removeReverseConnections(flippedList);

            // add back the original regular connections second
            if (bDebugLogFlipConnection) LOG.info("undo() restoring original regular connections");
            removeRegularConnections(flippedList);

            setStale(this.isStale);
            resumeAutoSaving();
            getMapPanel().repaint();
        }

        public void redo() {
            suspendAutoSaving();
            // removeOriginalNodes the original regular connections first
            if (bDebugLogFlipConnection) LOG.info("undo() restoring original regular connections");
            removeRegularConnections(flippedList);

            // removeOriginalNodes the original reverse connections second
            if (bDebugLogFlipConnection) LOG.info("undo() restoring original reverse connections");
            removeReverseConnections(flippedList);

            // add all the flipped reverse connections first
            if (bDebugLogFlipConnection) LOG.info("undo removing flipped reverse connections");
            addFlippedReverseConnections(flippedList);

            // add all the flipped regular connections second
            if (bDebugLogFlipConnection) LOG.info("undo() removing flipped regular connections");
            addFlippedRegularConnections(flippedList);

            setStale(true);
            resumeAutoSaving();
            getMapPanel().repaint();
        }
    }
}

