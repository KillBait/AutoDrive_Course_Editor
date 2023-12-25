package AutoDriveEditor.GUI.Buttons.Nodes;

import AutoDriveEditor.GUI.Buttons.ConnectionSelectBaseButton;
import AutoDriveEditor.Managers.ChangeManager;
import AutoDriveEditor.RoadNetwork.MapNode;
import AutoDriveEditor.RoadNetwork.RoadMap;

import javax.swing.*;
import java.awt.*;
import java.awt.event.MouseEvent;
import java.awt.geom.Point2D;
import java.util.ArrayList;

import static AutoDriveEditor.AutoDriveEditor.changeManager;
import static AutoDriveEditor.AutoDriveEditor.getMapPanel;
import static AutoDriveEditor.GUI.Buttons.LinerLineBaseButton.*;
import static AutoDriveEditor.GUI.MapPanel.*;
import static AutoDriveEditor.GUI.Menus.DebugMenu.Logging.LogFlipConnectionMenu.bDebugLogFlipConnection;
import static AutoDriveEditor.Listeners.MouseListener.currentMouseX;
import static AutoDriveEditor.Listeners.MouseListener.currentMouseY;
import static AutoDriveEditor.Locale.LocaleManager.getLocaleString;
import static AutoDriveEditor.Managers.ButtonManager.getCurrentButton;
import static AutoDriveEditor.RoadNetwork.MapNode.NODE_FLAG_REGULAR;
import static AutoDriveEditor.RoadNetwork.MapNode.NODE_FLAG_SUBPRIO;
import static AutoDriveEditor.Utils.GUIUtils.makeImageToggleButton;
import static AutoDriveEditor.Utils.LoggerUtils.LOG;
import static AutoDriveEditor.XMLConfig.EditorXML.*;

public final class FlipDirectionButton extends ConnectionSelectBaseButton {

    public FlipDirectionButton(JPanel panel) {
        button = makeImageToggleButton("buttons/flip", "buttons/flip_selected", null, "nodes_flip_tooltip", "nodes_flip_alt", panel, false, false, null, false, this);
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
        return getLocaleString("nodes_flip_tooltip");
    }


    @Override
    public boolean detectDualConnections() {
        return false;
    }

    @Override
    public Integer getLineDetectionInterval() {
        return 20;
    }

    @Override
    public Boolean getShowHighlightSelected() {
        return true;
    }


    @Override
    public void mouseMoved(MouseEvent e) {
        super.mouseMoved(e);
        for (Connection connection : connectionsList) {
            connection.getStartNode().getIgnoreDrawingConnectionsList().add(connection.getEndNode());
        }
    }

    @Override
    public void mouseDragged(MouseEvent e) {
        super.mouseDragged(e);
        for (Connection connection : connectionsList) {
            connection.getStartNode().getIgnoreDrawingConnectionsList().add(connection.getEndNode());
        }
    }

    @Override
    public void getConnectionsAroundPoint(Point2D point) {
        double startX = point.getX() - padding;
        double startY = point.getY() - padding;
        double endX = point.getX() + padding;
        double endY = point.getY() + padding;

        for (MapNode mapNode : RoadMap.networkNodesList) {
            if (mapNode.x > startX && mapNode.x < endX && mapNode.z > startY && mapNode.z < endY) {
                if (mapNode.outgoing.size() > 0) {
                    for (MapNode outgoing : mapNode.outgoing) {
                        Point2D mouseWorldPos = screenPosToWorldPos(currentMouseX, currentMouseY);
                        if (pointToLineDistance(mapNode, outgoing, mouseWorldPos.getX(), mouseWorldPos.getY()) < .5) {
                            int connectionType = getConnectionTypeFor(mapNode, outgoing);
                            if (connectionType != CONNECTION_UNKNOWN) {
                                if (bSelectHidden || !mapNode.getHiddenConnectionsList().contains(outgoing)) {
                                    connectionsList.add(new Connection(mapNode, outgoing, connectionType));
                                }
                            }
                            lastDetected = mapNode;
                        }
                    }
                }
            }
        }
    }


    @Override
    public void mouseReleased(MouseEvent e) {

        // Normally it would be a simple task to remove all the connections in one go and
        // immediately restore all the flipped connections, but, this isn't possible due
        // to one special case, it is possible to have two nodes with a reverse connection
        // and a normal connection in the opposite direction.
        //
        // Due to how the logic in the createConnectionBetween() function works for reverse
        // connections, we have to follow a specific order.
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

        ArrayList<Connection> connectionList = getConnectionList();

        if (connectionList.size() > 0) {
            changeManager.addChangeable(new FlipConnectionChanger(connectionList));

            //
            // WARNING: Until createConnections has been re-factored, the connection
            // removal/restore MUST be done in this order
            //

            if (bDebugLogFlipConnection) LOG.info("Adjusting {} connections", connectionList.size());

            // remove all the selected regular connection first
            if (bDebugLogFlipConnection) LOG.info("removing regular connections");
            removeRegularConnections(connectionList);

            // remove all the selected reverse connections second
            if (bDebugLogFlipConnection) LOG.info("removing reverse connections");
            removeReverseConnections(connectionList);

            //
            // now the selected connections are removed, we can add back the flipped versions
            //

            // add back the flipped reverse connections first
            if (bDebugLogFlipConnection) LOG.info("adding reverse connections");
            addFlippedReverseConnections(connectionList);

            // add back the flipped regular connections second
            if (bDebugLogFlipConnection) LOG.info("adding regular connections");
            addFlippedRegularConnections(connectionList);
            if (bDebugLogFlipConnection) LOG.info("--------------------------");

            resetConnectionList();
            setStale(true);
            getMapPanel().repaint();
        }
    }

    private static void removeRegularConnections(ArrayList<Connection> removeList) {
        if (removeList.size() > 0) {
            for (Connection connection : removeList) {
                if (connection.getConnectionType() == CONNECTION_STANDARD || connection.getConnectionType() == CONNECTION_STANDARD_SUBPRIO) {
                    if (bDebugLogFlipConnection) {
                        if (connection.getStartNode().flag == NODE_FLAG_REGULAR) {
                            LOG.info("## Remove Regular {} --> {} isHidden {}", connection.getStartNode().id, connection.getEndNode().id, connection.isHidden());
                        } else if (connection.getStartNode().flag == NODE_FLAG_SUBPRIO) {
                            LOG.info("## Remove Subprio {} --> {} isHidden {}", connection.getStartNode().id, connection.getEndNode().id, connection.isHidden());
                        } else {
                            LOG.info("## Ignoring UNKNOWN Regular Connection ## {} --> {}", connection.getStartNode().id, connection.getEndNode().id);
                            continue;
                        }
                    }
                    if (connection.isHidden()) connection.getStartNode().getHiddenConnectionsList().remove(connection.getEndNode());
                    createConnectionBetween(connection.getStartNode(), connection.getEndNode(), CONNECTION_STANDARD);
                }
            }
        }
    }

    private static void removeReverseConnections(ArrayList<Connection> removeList) {
        if (removeList.size() > 0) {
            for (Connection connection : removeList) {
                if (connection.getConnectionType() == CONNECTION_REVERSE) {
                    if (bDebugLogFlipConnection) {
                        if (connection.getStartNode().flag == NODE_FLAG_REGULAR) {
                            LOG.info("## Remove Reverse Regular {} --> {} isHidden {}", connection.getStartNode().id, connection.getEndNode().id,connection.isHidden());
                        } else if (connection.getStartNode().flag == NODE_FLAG_SUBPRIO) {
                            LOG.info("## Remove Reverse Subprio {} --> {} isHidden {}", connection.getStartNode().id, connection.getEndNode().id, connection.isHidden());
                        } else {
                            LOG.info("## Ignoring UNKNOWN Reverse Connection ## {} --> {}", connection.getStartNode().id, connection.getEndNode().id);
                            continue;
                        }
                    }
                    createConnectionBetween(connection.getStartNode(), connection.getEndNode(), CONNECTION_REVERSE);
                }
            }
        }
    }

    private static void addFlippedRegularConnections(ArrayList<Connection> removeList) {
        if (removeList.size() > 0) {
            for (Connection connection : removeList) {
                if (connection.getConnectionType() == CONNECTION_STANDARD || connection.getConnectionType() == CONNECTION_STANDARD_SUBPRIO) {
                    if (bDebugLogFlipConnection) {
                        if (connection.getEndNode().flag == NODE_FLAG_REGULAR || connection.getConnectionType() == CONNECTION_STANDARD_SUBPRIO) {
                            LOG.info("## Add Regular {} --> {} isHidden {}", connection.getEndNode().id, connection.getStartNode().id, connection.isHidden());
                        } else if (connection.getEndNode().flag == NODE_FLAG_SUBPRIO) {
                            LOG.info("## Add Subprio {} --> {} isHidden {}", connection.getEndNode().id, connection.getStartNode().id, connection.isHidden());
                        } else {
                            LOG.info("## Ignoring UNKNOWN standard Connection ## {} --> {}", connection.getEndNode().id, connection.getStartNode().id);
                            continue;
                        }
                    }
                    createConnectionBetween(connection.getEndNode(), connection.getStartNode(), CONNECTION_STANDARD);
                    if (connection.isHidden()) connection.getEndNode().getHiddenConnectionsList().add(connection.getStartNode());
                }
            }
        }
    }

    private static void addFlippedReverseConnections(ArrayList<Connection> removeList) {
        if (removeList.size() > 0) {
            for (Connection connection : removeList) {
                if (connection.getConnectionType() == CONNECTION_REVERSE) {
                    if (bDebugLogFlipConnection) {
                        if (connection.getEndNode().flag == NODE_FLAG_REGULAR) {
                            LOG.info("## Add Reverse Regular {} --> {} isHidden {}", connection.getEndNode().id, connection.getStartNode().id, connection.isHidden());
                        } else if (connection.getEndNode().flag == NODE_FLAG_SUBPRIO) {
                            LOG.info("## Add Reverse Subprio {} --> {} isHidden {}", connection.getEndNode().id, connection.getStartNode().id, connection.isHidden());
                        } else {
                            LOG.info("## Ignoring UNKNOWN Reverse Connection ## {} --> {}", connection.getEndNode().id, connection.getStartNode().id);
                            continue;
                        }
                    }
                    createConnectionBetween(connection.getEndNode(), connection.getStartNode(), CONNECTION_REVERSE);
                    if (connection.isHidden()) connection.getEndNode().getHiddenConnectionsList().add(connection.getStartNode());
                }
            }
        }
    }

    @Override
    public void drawToScreen(Graphics g) {
        if (getCurrentButton() == this) {
            if (connectionsList.size() > 0) {
                Graphics2D g1 = (Graphics2D) g.create();
                Color colour = null;
                boolean isDual = false;
                for (Connection segment : connectionsList) {
                    // Check if the line segment intersects the rectangular region
                    Point p1 = worldPosToScreenPos(segment.getStartX(), segment.getStartZ());
                    Point p2 = worldPosToScreenPos(segment.getEndX(), segment.getEndZ());
                    switch (segment.getConnectionType()) {
                        case -1:
                            // type -1 is unknown connection, ignore
                            colour = Color.WHITE;
                            break;
                        case 0:
                        case 1:
                            if (segment.getEndNode().flag == NODE_FLAG_REGULAR) {
                                colour = colourConnectRegular;
                            } else if (segment.getEndNode().flag == NODE_FLAG_SUBPRIO) {
                                colour = colourConnectSubprio;
                            }
                            isDual = false;
                            break;
                        case 2:
                            if (segment.getEndNode().flag == NODE_FLAG_REGULAR) {
                                colour = colourConnectReverse;
                            } else if (segment.getEndNode().flag == NODE_FLAG_SUBPRIO) {
                                colour = colourConnectReverseSubprio;
                            }
                            isDual = false;
                            break;
                        case 3:
                            if (segment.getStartNode().flag == NODE_FLAG_REGULAR && segment.getEndNode().flag == NODE_FLAG_REGULAR) {
                                colour = colourConnectDual;
                            } else if (segment.getStartNode().flag == NODE_FLAG_SUBPRIO || segment.getEndNode().flag == NODE_FLAG_SUBPRIO) {
                                colour = colourConnectDualSubprio;
                            }
                            isDual = true;
                            break;
                    }
                    g1.setStroke(new BasicStroke((float) (nodeSizeScaledQuarter)));
                    drawArrowBetween(g1, p2, p1, isDual, colour, false);
                }
                g1.dispose();
            }
        }
    }

    //
    // TODO: Make the undo/redo process swap the hidden state of the connection.
    //       Temporarily remove it for the moment
    //

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
            // remove all the flipped regular connections first
            if (bDebugLogFlipConnection) LOG.info("undo() removing flipped regular connections");
            addFlippedRegularConnections(flippedList);

            // remove all the flipped reverse connections second
            if (bDebugLogFlipConnection) LOG.info("undo removing flipped reverse connections");
            addFlippedReverseConnections(flippedList);

            // add back the original reverse connections first
            if (bDebugLogFlipConnection) LOG.info("undo() restoring original reverse connections");
            removeReverseConnections(flippedList);

            // add back the original regular connections second
            if (bDebugLogFlipConnection) LOG.info("undo() restoring original regular connections");
            removeRegularConnections(flippedList);

            getMapPanel().repaint();
            setStale(this.isStale);
        }

        public void redo() {

            // remove the original regular connections first
            if (bDebugLogFlipConnection) LOG.info("undo() restoring original regular connections");
            removeRegularConnections(flippedList);

            // remove the original reverse connections second
            if (bDebugLogFlipConnection) LOG.info("undo() restoring original reverse connections");
            removeReverseConnections(flippedList);

            // add all the flipped reverse connections first
            if (bDebugLogFlipConnection) LOG.info("undo removing flipped reverse connections");
            addFlippedReverseConnections(flippedList);

            // add all the flipped regular connections second
            if (bDebugLogFlipConnection) LOG.info("undo() removing flipped regular connections");
            addFlippedRegularConnections(flippedList);

            getMapPanel().repaint();
            setStale(true);
        }
    }
}

