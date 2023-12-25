package AutoDriveEditor.GUI.Buttons;

import AutoDriveEditor.Classes.LinearLine;
import AutoDriveEditor.Managers.ChangeManager;
import AutoDriveEditor.RoadNetwork.MapNode;
import AutoDriveEditor.RoadNetwork.RoadMap;
import AutoDriveEditor.Utils.ExceptionUtils;

import java.awt.*;
import java.awt.event.MouseEvent;
import java.awt.event.MouseWheelEvent;
import java.awt.geom.Point2D;
import java.util.LinkedList;

import static AutoDriveEditor.AutoDriveEditor.getMapPanel;
import static AutoDriveEditor.AutoDriveEditor.mapPanel;
import static AutoDriveEditor.GUI.MapPanel.*;
import static AutoDriveEditor.GUI.Menus.DebugMenu.Logging.LogLinearLineInfoMenu.bDebugLogLinearlineInfo;
import static AutoDriveEditor.GUI.Menus.DebugMenu.Logging.LogUndoRedoMenu.bDebugLogUndoRedo;
import static AutoDriveEditor.GUI.TextPanel.showInTextArea;
import static AutoDriveEditor.Locale.LocaleManager.getLocaleString;
import static AutoDriveEditor.RoadNetwork.MapNode.NODE_FLAG_REGULAR;
import static AutoDriveEditor.RoadNetwork.MapNode.NODE_FLAG_SUBPRIO;
import static AutoDriveEditor.RoadNetwork.RoadMap.showMismatchedIDError;
import static AutoDriveEditor.Utils.LoggerUtils.LOG;
import static AutoDriveEditor.XMLConfig.EditorXML.*;

public abstract class LinerLineBaseButton extends BaseButton{

    public static final int CONNECTION_UNKNOWN = -1;
    public static final int CONNECTION_STANDARD = 0;
    public static final int CONNECTION_STANDARD_SUBPRIO = 1;
    public static final int CONNECTION_REVERSE = 2;
    //public static final int CONNECTION_REVERSE_SUBPRIO = 3;
    public static final int CONNECTION_DUAL = 3;
    public static final int CONNECTION_CROSSED = 4;

    LinearLine linearLine;
    MapNode startNode;
    public int  connectionState = 0;
    public int connectionType = 0;
    boolean isDraggingLine = false;

    protected abstract void setNormalStateIcons();
    protected abstract void setAlternateStateIcons();


    @Override
    public void setSelected(boolean selected) {
        if (selected) {
            button.setSelected(true);
            showInTextArea(getInfoText(), true, false);
        } else {
            button.setSelected(false);
            cancelLinearLine();
        }
    }

    @Override
    public void mouseClicked(MouseEvent e) {
        if (e.getButton() == MouseEvent.BUTTON3 && e.getSource() == button) {
            if (button.isEnabled() && button.isSelected()) {
                connectionState = 1 - connectionState;
                if (connectionState == NODE_FLAG_REGULAR) { // == 0
                    setNormalStateIcons();
                } else {
                    setAlternateStateIcons();
                }
            }
        }
    }

    @Override
    public void mousePressed(MouseEvent e) {
        if (e.getButton() == MouseEvent.BUTTON1) {
            MapNode selected = getNodeAtScreenPosition(e.getX(), e.getY());
            if (selected != null && selected.isSelectable()) {
                if (startNode == null) {
                    if (!selected.isControlNode()) {
                        startNode = selected;
                        linearLine = new LinearLine(startNode, e.getX(), e.getY(), connectionState);
                        isDraggingLine = true;
                        if (bDebugLogLinearlineInfo) LOG.info("## LinearLine Debug ## created linear line starting at x {} y {} z {}",startNode.x, startNode.y, startNode.z);
                        showInTextArea(getLocaleString("infopanel_linearline_started"), true, false);
                    }
                } else if (selected == startNode) {
                    startNode = null;
                    isDraggingLine = false;
                    if (bDebugLogLinearlineInfo) LOG.info("## LinearLine Debug ## canceling linear line");
                    showInTextArea(getLocaleString("infopanel_linearline_canceled"), true, false);
                } else {

                    if (!selected.isControlNode()) {
                        if (bDebugLogLinearlineInfo) LOG.info("## LinearLine Debug ## End node selected, creating linear line");
                        createLinearLine(selected);
                        if (bContinuousConnections) {
                            if (bDebugLogLinearlineInfo) LOG.info("## LinearLine Debug ## Continuous connection enabled, starting next line");
                            startNode = selected;
                            linearLine = new LinearLine(selected, e.getX(), e.getY(), connectionState);
                        } else {
                            if (bDebugLogLinearlineInfo) LOG.info("## LinearLine Debug ## Linear line finished");
                            isDraggingLine = false;
                            startNode = null;
                        }
                    } else {
                        if (bCreateLinearLineEndNode) {
                            if (bDebugLogLinearlineInfo) LOG.info("## LinearLine Debug ## No end node selected");
                            MapNode lastNode = createLinearLine(null);
                            if (bContinuousConnections) {
                                linearLine = new LinearLine(lastNode, e.getX(), e.getY(), connectionState);
                                startNode = lastNode;
                            } else {
                                isDraggingLine = false;
                                startNode = null;
                            }
                        }
                    }
                }
            }
        }
        if (e.getButton() == MouseEvent.BUTTON3) {
            cancelLinearLine();
        }
    }

    @Override
    public void mouseMoved(MouseEvent e) {
        if (startNode != null && isDraggingLine) {
            if (e.getX() > mapPanel.getWidth()) moveMapBy( -10, 0);
            if (e.getX() <= 0) moveMapBy( 10, 0);
            if (e.getY() > mapPanel.getHeight()) moveMapBy( 0, -10);
            if (e.getY() <= 0) moveMapBy( 0, 10);
            Point2D pointerPos = screenPosToWorldPos(e.getX(), e.getY());
            linearLine.updateLineEndLocation(pointerPos.getX(), pointerPos.getY());
            getMapPanel().repaint();
        }
    }

    @Override
    public void mouseWheelMoved(MouseWheelEvent e) {
        Point2D pointerPos = screenPosToWorldPos(e.getX(), e.getY());
        if (linearLine != null) {
            linearLine.updateLineEndLocation(pointerPos.getX(), pointerPos.getY());
            getMapPanel().repaint();
        }
    }

    private MapNode createLinearLine(MapNode endNode) {
        MapNode lastNode = linearLine.commitLinearLineEndingAt(endNode, connectionType, connectionState);
        showInTextArea(getLocaleString("infopanel_linearline_completed"), true, false);
        linearLine.clear();
        setStale(true);
        return lastNode;
    }

    private void cancelLinearLine() {
        startNode = null;
        isDraggingLine = false;
        if (linearLine != null) { linearLine.clear(); }
        getMapPanel().repaint();
    }


    @Override
    public void drawToScreen(Graphics g) {
        if (isDraggingLine) {
            Color colour = Color.GREEN;
            LinkedList<MapNode> lineNodeList = linearLine.getLinearLineNodeList();
            Graphics2D g2d = (Graphics2D) g.create();

            for (int j = 0; j < lineNodeList.size(); j++) {
                MapNode startNode = lineNodeList.get(j);
                Point2D startNodePos = worldPosToScreenPos(startNode.x, startNode.z);
                g2d.setComposite(AlphaComposite.SrcOver.derive(0.5f));
                if (this.startNode.flag == NODE_FLAG_REGULAR) {
                    g2d.setColor(Color.WHITE);
                } else {
                    g2d.setColor(colourNodeSubprio);
                }

                if (startNode != lineNodeList.getFirst()) {
                    if (startNode != lineNodeList.getLast() || bCreateLinearLineEndNode) {
                        g2d.fillArc((int) (startNodePos.getX() - nodeSizeScaledHalf), (int) (startNodePos.getY() - nodeSizeScaledHalf), (int) nodeSizeScaled, (int) nodeSizeScaled, 0, 360);
                    }
                }

                if (startNode != lineNodeList.getLast()) {
                    MapNode firstPos = lineNodeList.get(j+1);
                    Point2D endNodePos = worldPosToScreenPos(firstPos.x, firstPos.z);

                    // select the colour of line to drawToScreen

                    if ( connectionType == CONNECTION_STANDARD) {
                        if (startNode.flag ==NODE_FLAG_SUBPRIO) colour = colourConnectSubprio;
                    } else if ( connectionType == CONNECTION_DUAL ) {
                        if (startNode.flag == NODE_FLAG_REGULAR) {
                            colour = colourConnectDual;
                        } else {
                            colour = colourConnectDualSubprio;
                        }
                    } else if ( connectionType == CONNECTION_REVERSE ) {
                        if (startNode.flag == NODE_FLAG_REGULAR) {
                            colour = colourConnectReverse;
                        } else {
                            colour = colourConnectReverseSubprio;
                        }
                    }
                    //g.setColor(colour);
                    drawArrowBetween(g, startNodePos, endNodePos, connectionType == CONNECTION_DUAL, colour, false);
                }
            }
            g2d.dispose();
        }
    }

    //
    // Linear Line Changer
    //

    public static class LinearLineChanger implements ChangeManager.Changeable {

        private final MapNodeStore fromNode;
        private final MapNodeStore toNode;
        private final LinkedList<MapNodeStore> autoGeneratedNodes;
        private final int connectionType;
        private final boolean wasEndNodeCreated;
        private final boolean isStale;

        public LinearLineChanger(LinkedList<MapNode> generatedNodes, boolean endNodeCreated, int connectionType){
            super();
            this.fromNode = new MapNodeStore(generatedNodes.getFirst());
            this.toNode = new MapNodeStore(generatedNodes.getLast());
            this.wasEndNodeCreated = endNodeCreated;
            this.autoGeneratedNodes = new LinkedList<>();
            this.connectionType = connectionType;
            this.isStale = isStale();

            if (bDebugLogUndoRedo) LOG.info("## LinearLineChanger ## Start node ID = {} : End node ID = {} : End node created  = {}", this.fromNode.getMapNode().id, this.toNode.getMapNode().id, endNodeCreated);

            for (MapNode node : generatedNodes) {
                if (bDebugLogUndoRedo) LOG.info("## LinearLineChanger ## Adding MapNode ID {} to autoGeneratedNodes", node.id);
                autoGeneratedNodes.add(new MapNodeStore(node));
            }
        }

        public void undo(){
            LOG.info("size = {}", this.autoGeneratedNodes.size());
            if (this.autoGeneratedNodes.size() <= 2 ) {
                if (bDebugLogUndoRedo) {
                    LOG.info("## LinearLineChanger.undo ## Only start + end nodes in list");
                    LOG.info("## LinearLineChanger.undo ## restoring starting node connections");
                }
                this.fromNode.restoreConnections();

                if (this.toNode != null && !this.wasEndNodeCreated) {
                    if (bDebugLogUndoRedo) LOG.info("## LinearLineChanger.undo ## restoring ending node connections");
                    this.toNode.restoreConnections();
                } else if (wasEndNodeCreated) {
                    if (bDebugLogUndoRedo) LOG.info("## LinearLineChanger.undo ## wasEndNodeCreated = True : removing the created end node");
                    RoadMap.removeMapNode(this.autoGeneratedNodes.getLast().getMapNode());
                }
            } else {
                for (int i = 1; i < this.autoGeneratedNodes.size(); i++) {
                    MapNodeStore storedNode = this.autoGeneratedNodes.get(i);
                    MapNode toDelete = storedNode.getMapNode();
                    if (toDelete != this.autoGeneratedNodes.getLast().getMapNode()) {
                        if (bDebugLogUndoRedo) LOG.info("## LinearLineChanger.undo ## removing ID {} from MapNodes", toDelete.id);
                        RoadMap.removeMapNode(toDelete);
                        if (storedNode.hasChangedID()) {
                            if (bDebugLogUndoRedo) LOG.info("## LinearLineChanger.undo ## Removed node changed ID {}", storedNode.getMapNode().id);
                            storedNode.resetID();
                            if (bDebugLogUndoRedo) LOG.info("## LinearLineChanger.undo ## Reset ID to {}", storedNode.getMapNode().id);
                        }
                    } else {
                        if (this.wasEndNodeCreated) {
                            if (bDebugLogUndoRedo) LOG.info("## LinearLineChanger.undo ## removing generated end node (ID {}) from MapNodes", toDelete.id);
                            RoadMap.removeMapNode(toDelete);
                            if (storedNode.hasChangedID()) {
                                if (bDebugLogUndoRedo) LOG.info("## LinearLineChanger.undo ## Removed end node changed ID {}", storedNode.getMapNode().id);
                                storedNode.resetID();
                                if (bDebugLogUndoRedo) LOG.info("## LinearLineChanger.undo ## Resetting end node ID to {}", storedNode.getMapNode().id);
                            }
                        } else {
                            if (bDebugLogUndoRedo) LOG.info("## LinearLineChanger.undo ## restoring connections for end node (ID {})", storedNode.getMapNode().id);
                            this.toNode.restoreConnections();
                        }
                    }
                    if (hoveredNode == toDelete) {
                        hoveredNode = null;
                    }
                }
            }
            getMapPanel().repaint();
            setStale(this.isStale);
        }

        public void redo(){
            try {
                if (this.autoGeneratedNodes.size() <= 2 ) {
                    if (bDebugLogUndoRedo) {
                        LOG.info("## LinearLineChanger.redo ## Only start + end nodes in list");
                        LOG.info("## LinearLineChanger.redo ## restoring starting node connections");
                    }
                    this.fromNode.restoreConnections();

                    if (this.wasEndNodeCreated) {
                        if (bDebugLogUndoRedo) LOG.info("## LinearLineChanger.redo ## wasEndNodeCreated = True, re-inserting the created end node");
                        roadMap.insertMapNode(this.autoGeneratedNodes.getLast().getMapNode(), null, null);
                    } else {
                        if (bDebugLogUndoRedo) LOG.info("## LinearLineChanger.redo ## restoring ending node connections");
                        this.toNode.restoreConnections();
                    }
                } else {
                    for (int i = 1; i < this.autoGeneratedNodes.size(); i++) {
                        MapNodeStore storedNode = this.autoGeneratedNodes.get(i);
                        if (storedNode != this.autoGeneratedNodes.getLast()) {
                            storedNode.clearConnections();
                            // during the undo process, removeMapNode deletes all the connections coming
                            // to/from this node but adjusts the node id's,  so we have to manually restore
                            // the id, .getMapNode() will check this for us and correct if necessary before
                            // passing us the node info.
                            MapNode newNode = storedNode.getMapNode();
                            if (bDebugLogUndoRedo) LOG.info("## LinearLineChanger.redo ## re-inserting node ( ID {} ) in MapNodes", newNode.id);
                            roadMap.insertMapNode(newNode, null, null);
                        } else {
                            if (this.wasEndNodeCreated) {
                                MapNode newNode = storedNode.getMapNode();
                                if (bDebugLogUndoRedo) LOG.info("## LinearLineChanger.redo ## re-inserting created end node (ID {} ) in MapNodes", newNode.id);
                                roadMap.insertMapNode(newNode, null, null);
                            }
                        }
                    }
                }
                if (bDebugLogUndoRedo) LOG.info("## LinearLineChanger.redo ## reconnecting all nodes");
                LinearLine.connectNodes(getLineLinkedList(), this.connectionType);
                getMapPanel().repaint();
                setStale(true);
            } catch (ExceptionUtils.MismatchedIdException e) {
                showMismatchedIDError("LinearLineChanger redo()", e);
            }

        }

        public LinkedList<MapNode> getLineLinkedList() {
            LinkedList<MapNode> list = new LinkedList<>();
            for (int i = 0; i <= this.autoGeneratedNodes.size() - 1 ; i++) {
                MapNodeStore nodeBackup = this.autoGeneratedNodes.get(i);
                list.add(nodeBackup.getMapNode());
            }
            return list;
        }
    }
}
