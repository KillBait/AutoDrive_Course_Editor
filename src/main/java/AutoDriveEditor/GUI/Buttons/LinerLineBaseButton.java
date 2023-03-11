package AutoDriveEditor.GUI.Buttons;

import AutoDriveEditor.Managers.ChangeManager;
import AutoDriveEditor.MapPanel.LinearLine;
import AutoDriveEditor.RoadNetwork.MapNode;
import AutoDriveEditor.RoadNetwork.RoadMap;

import java.awt.*;
import java.awt.event.MouseEvent;
import java.awt.event.MouseWheelEvent;
import java.awt.geom.Point2D;
import java.util.LinkedList;
import java.util.concurrent.locks.Lock;

import static AutoDriveEditor.GUI.GUIBuilder.mapPanel;
import static AutoDriveEditor.GUI.MenuBuilder.bDebugLogLinearlineInfo;
import static AutoDriveEditor.GUI.MenuBuilder.bDebugLogUndoRedo;
import static AutoDriveEditor.Locale.LocaleManager.getLocaleString;
import static AutoDriveEditor.MapPanel.MapPanel.*;
import static AutoDriveEditor.RoadNetwork.MapNode.NODE_FLAG_STANDARD;
import static AutoDriveEditor.RoadNetwork.MapNode.NODE_FLAG_SUBPRIO;
import static AutoDriveEditor.Utils.GUIUtils.showInTextArea;
import static AutoDriveEditor.Utils.ImageUtils.backBufferGraphics;
import static AutoDriveEditor.Utils.LoggerUtils.LOG;
import static AutoDriveEditor.XMLConfig.EditorXML.*;

public abstract class LinerLineBaseButton extends BaseButton{

    public static final int CONNECTION_STANDARD = 0;
    @SuppressWarnings("unused")
    public static final int CONNECTION_SUBPRIO = 1; // never used as subprio routes are based on a nodes .flag value
    public static final int CONNECTION_DUAL = 2;
    public static final int CONNECTION_REVERSE = 3;

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

    public void mouseClicked(MouseEvent e) {
        if (e.getButton() == MouseEvent.BUTTON3 && e.getSource() == button) {
            if (button.isEnabled() && button.isSelected()) {
                connectionState = 1 - connectionState;
                if (connectionState == NODE_FLAG_STANDARD) { // == 0
                    setNormalStateIcons();
                } else {
                    setAlternateStateIcons();
                }
            }
        }
    }

    public void mousePressed(MouseEvent e) {
        if (e.getButton() == MouseEvent.BUTTON1) {
            MapNode selected = getNodeAtScreenPosition(e.getX(), e.getY());
            if (startNode == null) {
                if (selected != null && !selected.isControlNode) {
                    startNode = selected;
                    //Point2D pointerPos = screenPosToWorldPos(e.getX(), e.getY());
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

                if (selected != null && !selected.isControlNode) {
                    if (bDebugLogLinearlineInfo) LOG.info("## LinearLine Debug ## End node selected, creating linear line");
                    createLinearLine(selected);
                    if (bContinuousConnections) {
                        if (bDebugLogLinearlineInfo) LOG.info("## LinearLine Debug ## Continuous connection enabled, starting next line");
                        startNode = selected;
                        //Point2D pointerPos = screenPosToWorldPos(e.getX(), e.getY());
                        linearLine = new LinearLine(selected, e.getX(), e.getY(), connectionState);
                    } else {
                        if (bDebugLogLinearlineInfo) LOG.info("## LinearLine Debug ## Linear line finished");
                        isDraggingLine = false;
                        startNode = null;
                    }
                } else {
                    if (selected == null && bCreateLinearLineEndNode) {
                        if (bDebugLogLinearlineInfo) LOG.info("## LinearLine Debug ## No end node selected");
                        createLinearLine(null);
                        if (bContinuousConnections) {
                            linearLine = new LinearLine(getNodeAtScreenPosition(e.getX(), e.getY()), e.getX(), e.getY(), connectionState);
                        } else {
                            isDraggingLine = false;
                            startNode = null;
                        }
                    }
                }
            }
        }
        if (e.getButton() == MouseEvent.BUTTON3) {
            cancelLinearLine();
        }
    }

    public void mouseMoved(MouseEvent e) {
        if (startNode != null && isDraggingLine) {
            if (e.getX() > mapPanel.getWidth()) getMapPanel().moveMapBy( -10, 0);
            if (e.getX() <= 0) getMapPanel().moveMapBy( 10, 0);
            if (e.getY() > mapPanel.getHeight()) getMapPanel().moveMapBy( 0, -10);
            if (e.getY() <= 0) getMapPanel().moveMapBy( 0, 10);
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

    private void createLinearLine(MapNode endNode) {
        linearLine.commit(endNode, connectionType, connectionState);
        showInTextArea(getLocaleString("infopanel_linearline_completed"), true, false);
        linearLine.clear();
        setStale(true);
    }

    private void cancelLinearLine() {
        startNode = null;
        isDraggingLine = false;
        if (linearLine != null) {
            linearLine.clear();
        }
        getMapPanel().repaint();
    }


    @Override
    public void drawToScreen(Graphics2D g, Lock lock, double scaledSizeQuarter, double scaledSizeHalf) {
        if (isDraggingLine) {
            Color colour = Color.GREEN;
            LinkedList<MapNode> lineNodeList = linearLine.getLinearLineNodeList();

            drawLock.lock();
            try {
                for (int j = 0; j < lineNodeList.size(); j++) { // skip the last node of the array
                    MapNode firstPos = lineNodeList.get(j);
                    Point2D startNodePos = worldPosToScreenPos(firstPos.x, firstPos.z);

                    // don't draw the first node image as it already exists
                    if (j > 0) {
                        // only draw the last node if bCreateLinearLineEndNode is true
                        if (j < lineNodeList.size() -1 || bCreateLinearLineEndNode) {
                            Shape oldClip = backBufferGraphics.getClip();
                            Graphics2D g2d = (Graphics2D) backBufferGraphics.create();
                            g2d.setComposite(AlphaComposite.SrcOver.derive(0.5f));
                            if (startNode.flag == NODE_FLAG_STANDARD) {
                                g2d.setColor(Color.WHITE);
                            } else {
                                g2d.setColor(colourNodeSubprio);
                            }
                            g2d.fillArc((int) (startNodePos.getX() - scaledSizeQuarter), (int) (startNodePos.getY() - scaledSizeQuarter), (int) scaledSizeHalf, (int) scaledSizeHalf, 0, 360);
                            g2d.setClip(oldClip);
                            g2d.dispose();
                        }
                    }

                    if ( j < lineNodeList.size() - 1) {
                        MapNode secondPos = lineNodeList.get(j+1);
                        Point2D endNodePos = worldPosToScreenPos(secondPos.x, secondPos.z);

                        // select the colour of line to draw
                        if ( connectionType == CONNECTION_STANDARD) {
                            if (secondPos.flag ==NODE_FLAG_SUBPRIO) colour = colourConnectSubprio;
                        } else if ( connectionType == CONNECTION_DUAL ) {
                            if (secondPos.flag == NODE_FLAG_STANDARD) {
                                colour = colourConnectDual;
                            } else {
                                colour = colourConnectDualSubprio;
                            }
                        } else if ( connectionType == CONNECTION_REVERSE ) {
                            if (secondPos.flag == NODE_FLAG_STANDARD) {
                                colour = colourConnectReverse;
                            } else {
                                colour = colourConnectReverseSubprio;
                            }
                        }

                        backBufferGraphics.setColor(colour);
                        drawArrowBetween(backBufferGraphics, startNodePos, endNodePos, connectionType == CONNECTION_DUAL);
                    }
                }
            } finally {
                drawLock.unlock();
            }
        }
    }

    //
    // Linear Line Changer
    //

    public static class LinearLineChanger implements ChangeManager.Changeable {

        private final MapNodeStore fromNode;
        private MapNodeStore toNode;
        private final LinkedList<MapNodeStore> autoGeneratedNodes;
        private final int connectionType;
        private final boolean wasEndNodeCreated;
        private final boolean isStale;

        public LinearLineChanger(MapNode fromNode, MapNode toNode, boolean endNodeCreated, LinkedList<MapNode> inbetweenNodes, int type){
            super();
            this.fromNode = new MapNodeStore(fromNode);
            if (toNode != null) {
                this.toNode = new MapNodeStore(toNode);
            }
            this.wasEndNodeCreated = endNodeCreated;
            this.autoGeneratedNodes = new LinkedList<>();
            this.connectionType=type;
            this.isStale = isStale();

            if (bDebugLogUndoRedo) LOG.info("## LinearLineChanger ## Start node ID = {} : End node ID = {} : End node created  = {}", fromNode.id, toNode != null ? toNode.id : "null", endNodeCreated);

            for (MapNode node : inbetweenNodes) {
                if (bDebugLogUndoRedo) LOG.info("## LinearLineChanger ## Adding ID {} to autoGeneratedNodes", node.id);
                autoGeneratedNodes.add(new MapNodeStore(node));
            }

            if (toNode != null && endNodeCreated) {
                if (bDebugLogUndoRedo) LOG.info("## LinearLineChanger ## Adding newly created end node ID {} to autoGeneratedNodes", toNode.id);
                autoGeneratedNodes.add(new MapNodeStore(toNode));
            }
        }

        public void undo(){
            if (this.autoGeneratedNodes.size() < 2 ) {
                if (bDebugLogUndoRedo) LOG.info("## LinearLineChanger.undo ## No autoGenerated Nodes, restoring from node connections");
                this.fromNode.restoreConnections();

                if (this.toNode != null && !this.wasEndNodeCreated) {
                    if (bDebugLogUndoRedo) LOG.info("## LinearLineChanger.undo ## restoring to node connections");
                    this.toNode.restoreConnections();
                } else if (wasEndNodeCreated) {
                    if (bDebugLogUndoRedo) LOG.info("## LinearLineChanger.undo ## wasEndNodeCreated = True : removing the created end node");
                    RoadMap.removeMapNode(this.autoGeneratedNodes.getLast().getMapNode());
                }
            } else {
                for (int i = 1; i < this.autoGeneratedNodes.size(); i++) {
                    MapNodeStore storedNode = this.autoGeneratedNodes.get(i);
                    MapNode toDelete = storedNode.getMapNode();
                    if (bDebugLogUndoRedo) LOG.info("## LinearLineChanger.undo ## undo is removing ID {} from MapNodes", toDelete.id);
                    RoadMap.removeMapNode(toDelete);
                    if (storedNode.hasChangedID()) {
                        if (bDebugLogUndoRedo) LOG.info("## LinearLineChanger.undo ## Removed node changed ID {}", storedNode.getMapNode().id);
                        storedNode.resetID();
                        if (bDebugLogUndoRedo) LOG.info("## LinearLineChanger.undo ## Reset ID to {}", storedNode.getMapNode().id);
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
            if (this.autoGeneratedNodes.size() <= 1 ) {
                if (bDebugLogUndoRedo) LOG.info("## LinearLineChanger.redo ## No autoGenerated Nodes, restoring from node connections");
                this.fromNode.restoreConnections();
                if (this.toNode != null && !this.wasEndNodeCreated) {
                    if (bDebugLogUndoRedo) LOG.info("## LinearLineChanger.undo ## restoring to node connections");
                    this.toNode.restoreConnections();
                } else if (wasEndNodeCreated) {
                    if (bDebugLogUndoRedo) LOG.info("## LinearLineChanger.undo ## wasEndNodeCreated = True : removing the created end node");
                    roadMap.insertMapNode(this.autoGeneratedNodes.getLast().getMapNode(), null,null);
                }
            }
            if (this.autoGeneratedNodes.size() >= 2 ) {
                for (int i = 1; i < this.autoGeneratedNodes.size(); i++) {
                    MapNodeStore storedNode = this.autoGeneratedNodes.get(i);
                    storedNode.clearConnections();
                    // during the undo process, removeMapNode deletes all the connections coming
                    // to/from this node but adjusts the node id's,  so we have to manually restore
                    // the id, .getMapNode() will check this for us and correct if necessary before
                    // passing us the node info.
                    MapNode newNode = storedNode.getMapNode();
                    if (bDebugLogUndoRedo) LOG.info("## LinearLineChanger.redo ## Inserting ID {} in MapNodes", newNode.id);
                    roadMap.insertMapNode(newNode, null,null);
                }
            }
            LinearLine.connectNodes(this.fromNode.getMapNode(), this.toNode.getMapNode(), getLineLinkedList(), this.connectionType);
            getMapPanel().repaint();
            setStale(true);
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
