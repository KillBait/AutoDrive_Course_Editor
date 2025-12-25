package AutoDriveEditor.Classes;

import AutoDriveEditor.Classes.Util_Classes.ExceptionUtils;
import AutoDriveEditor.Managers.ChangeManager;
import AutoDriveEditor.Managers.RenderManager;
import AutoDriveEditor.RoadNetwork.Connection;
import AutoDriveEditor.RoadNetwork.MapNode;

import java.awt.*;
import java.awt.event.MouseEvent;
import java.awt.event.MouseMotionListener;
import java.awt.event.MouseWheelEvent;
import java.awt.event.MouseWheelListener;
import java.awt.geom.Point2D;
import java.util.ArrayList;
import java.util.LinkedList;

import static AutoDriveEditor.AutoDriveEditor.changeManager;
import static AutoDriveEditor.AutoDriveEditor.getMapPanel;
import static AutoDriveEditor.Classes.SnapShot.SnapShotType.*;
import static AutoDriveEditor.Classes.Util_Classes.ColourUtils.lighten;
import static AutoDriveEditor.Classes.Util_Classes.LoggerUtils.LOG;
import static AutoDriveEditor.GUI.Buttons.BaseButton.drawArrowBetween;
import static AutoDriveEditor.GUI.MapPanel.*;
import static AutoDriveEditor.GUI.Menus.DebugMenu.Logging.LogLinearLineInfoMenu.bDebugLogLinearlineInfo;
import static AutoDriveEditor.GUI.Menus.DebugMenu.Logging.LogUndoRedoMenu.bDebugLogUndoRedoInfo;
import static AutoDriveEditor.GUI.TextPanel.showInTextArea;
import static AutoDriveEditor.Listeners.MouseListener.*;
import static AutoDriveEditor.Locale.LocaleManager.getLocaleString;
import static AutoDriveEditor.Managers.RenderManager.PRIORITY_HIGH;
import static AutoDriveEditor.RoadNetwork.Connection.ConnectionType.DUAL;
import static AutoDriveEditor.RoadNetwork.MapNode.NODE_FLAG_REGULAR;
import static AutoDriveEditor.RoadNetwork.MapNode.createNewNetworkNode;
import static AutoDriveEditor.RoadNetwork.RoadMap.showMismatchedIDError;
import static AutoDriveEditor.XMLConfig.AutoSave.resumeAutoSaving;
import static AutoDriveEditor.XMLConfig.AutoSave.suspendAutoSaving;
import static AutoDriveEditor.XMLConfig.EditorXML.*;

/**
 * This Class contains the basic functions for the creation of Linear Lines
 */

public class LinearLine extends RenderManager.Drawable implements MouseMotionListener, MouseWheelListener {

    private final LinkedList<MapNode> linePreviewNodeList;
    private final ArrayList<Connection> linePreviewConnectionList;
    private final LinkedList<MapNode> addToNetworkNodeList;
    private final ArrayList<MapNode> existingNetworkNodesList;

    private MapNode startNode;
    private MapNode endNode;
    public Point2D lineEndWorld = new Point2D.Double();

    private int nodeFlagType;
    private Connection.ConnectionType connectionType;

    public LinearLine() {
        this(null, 0, 0);
    }

    /**
     * Create a linear line with a specific start node, ending at a specified location
     *
     * @param startNode      The starting node of the line.
     * @param mouseX         The x-coordinate of the mouse position.
     * @param mouseY         The y-coordinate of the mouse position.
//     * @param nodeType       The node type (i.e. Normal/Subprio)
//     * @param connectionType The type of the connection.
     */
    public LinearLine(MapNode startNode, int mouseX, int mouseY/*, int nodeType, Connection.ConnectionType connectionType*/) {
        this.linePreviewNodeList = new LinkedList<>();
        this.linePreviewConnectionList = new ArrayList<>();
        this.addToNetworkNodeList = new LinkedList<>();
        this.existingNetworkNodesList = new ArrayList<>();
        this.startNode = startNode;
        this.endNode = null;
        this.lineEndWorld.setLocation(screenPosToWorldPos(mouseX, mouseY));
        getMapPanel().addMouseMotionListener(this);
        getMapPanel().addMouseWheelListener(this);
        setRenderPriority(PRIORITY_HIGH);
    }

    @Override
    public void mouseDragged(MouseEvent e) {}

    @Override
    public void mouseMoved(MouseEvent e) {
        if (startNode != null) {
            // check if the mouse is over a node
            MapNode node = getNodeAtScreenPosition(e.getX(), e.getY());
            Point2D point = new Point2D.Double();
            // if the mouse is over a node, set the end point to the node position, else null
            if (node != null) {
                point = node.getWorldPosition2D();
                this.endNode = node;
            } else {
                this.endNode = null;
                point.setLocation(screenPosToWorldPos(e.getX(), e.getY()));
            }
            // updateVisibility the line preview
            updateLineEndLocation(point.getX(), point.getY());
            getMapPanel().repaint();
        }
    }

    @Override
    public void mouseWheelMoved(MouseWheelEvent e) {
        // adjust the line end location as the mouse wheel is moved and th map is zoomed in/out
        Point2D pointerPos = screenPosToWorldPos(e.getX(), e.getY());
        updateLineEndLocation(pointerPos.getX(), pointerPos.getY());
        getMapPanel().repaint();
    }

    /**
     * Creates the linear line preview between the start node and specified end point.
     */
    private void createLinerLinePreview() {
        if (startNode == null || connectionType == Connection.ConnectionType.UNKNOWN) {
            if (bDebugLogLinearlineInfo) LOG.info("## LinearLine.createLinerLinePreview() Debug ## No start node or connection type set, skipping preview creation");
            return;
        }
        generatePreviewInterpolationPoints();
        generatePreviewConnections();
    }

    /**
     * Calculates the interpolation points for the line between the start and end points.
     */
    private void generatePreviewInterpolationPoints() {
        // clear the previous line preview nodes
        this.linePreviewNodeList.clear();
        Point2D startPos = this.startNode.getWorldPosition2D();
        Point2D endPos = lineEndWorld;
        // calculate the number of needed interpolation points
        double totalDistance = startPos.distance(endPos);
        int numberOfSegments = (int) Math.round(totalDistance / linearLineNodeDistance);
        double diffX = endPos.getX() - startPos.getX();
        double diffY = endPos.getY() - startPos.getY();
        // calculate the interpolation points
        if (numberOfSegments > 1) {
            int id = 0;
            for (int i = 1; i <= numberOfSegments; i++) {
                double ratio = (double) i / numberOfSegments;
                double newX = startPos.getX() + ratio * diffX;
                double newY = startPos.getY() + ratio * diffY;
                if (i != numberOfSegments || endNode == null) {
                    linePreviewNodeList.add(new MapNode(id, newX, 0, newY, nodeFlagType, false));
                }
                id++;
            }
        }
    }


    /**
     * Generate the connections between all nodes in the list.
     */
    private void generatePreviewConnections() {
        linePreviewConnectionList.clear();
        if (!linePreviewNodeList.isEmpty()) {
            linePreviewConnectionList.add(new Connection(this.startNode, linePreviewNodeList.getFirst(), this.connectionType));
            for (int j = 0; j < linePreviewNodeList.size() - 1; j++) {
                MapNode startNode = linePreviewNodeList.get(j);
                MapNode endNode = linePreviewNodeList.get(j + 1);
                linePreviewConnectionList.add(new Connection(startNode, endNode, this.connectionType));
            }
        }
    }

    /**
     * Updates the start location of the line based on the specified world coordinates.
     * Triggered while moving the mouse to select start destination.
     *
     * @param worldX The x-coordinate in the world.
     * @param worldY The y-coordinate in the world.
     */
    public void updateLineStartLocation(double worldX, double worldY) {
        this.startNode.x = worldX;
        this.startNode.z = worldY;
    }

    /**
     * Updates the start node of the line.
     *
     * @param newStartNode The new start node.
     */
    public void updateLineStartNode(MapNode newStartNode) {
        if (newStartNode != null) {
            this.startNode = newStartNode;
            this.lineEndWorld.setLocation(newStartNode.getX(), newStartNode.getZ());
        }
    }

    /**
     * Updates the end location of the line based on the specified world coordinates.
     * Triggered while moving the mouse to select end destination.
     *
     * @param worldX The x-coordinate in the world.
     * @param worldY The y-coordinate in the world.
     */
    public void updateLineEndLocation(double worldX, double worldY) {
        if (this.startNode != null) {
            if (bCreateLinearLineEndNode && bGridSnapEnabled) {
                double gridSizeX = bGridSnapSubs ? gridSpacingX / (gridSubDivisions + 1) : gridSpacingX;
                double gridSizeY = bGridSnapSubs ? gridSpacingY / (gridSubDivisions + 1) : gridSpacingY;
                double newX = Math.round(worldX / gridSizeX) * gridSizeX;
                double newY = Math.round(worldY / gridSizeY) * gridSizeY;
                this.lineEndWorld.setLocation(newX, newY);
            } else {
                this.lineEndWorld.setLocation(worldX, worldY);
            }
            createLinerLinePreview();
        }
    }

    /**
     * Commits the linear line to the network, creating nodes and connections.
     */
    public void commit() {
        suspendAutoSaving();

        // Clear the previous list of nodes added to the road network
        addToNetworkNodeList.clear();
        existingNetworkNodesList.clear();

        // calculate the interpolation to be added to each MapNode position to createSetting
        // a linear transition from the start height to the end nodes height
        double endY = (endNode != null) ? endNode.getY() : getYValueFromHeightMap(getCurrentMouseWorldY(), getCurrentMouseWorldY());
        float yInterpolation = (float) ((endY - startNode.y) / (this.linePreviewNodeList.size() - 1));

        // Create new nodes.
        if (bDebugLogLinearlineInfo) {
            if (linePreviewNodeList.isEmpty()) {
                LOG.info("## LinearLine.commit() Debug ## No interpolation nodes generated");
            } else {
                LOG.info("## LinearLine.commit() Debug ## Creating {} linear line nodes", linePreviewNodeList.size());
            }
        }

        // create all the nodes needed for the curve
        if (linePreviewNodeList.isEmpty()) {
            if (endNode == null) {
                if (bDebugLogLinearlineInfo) LOG.info("## LinearLine.commit() Debug ## No end node selected, adding mapNode at mouse position");
                Point2D mousePosWorld = screenPosToWorldPos(currentMouseX, currentMouseY);
                double heightmapPosition = getYValueFromHeightMap(mousePosWorld.getX(), mousePosWorld.getY());
                endNode = createNewNetworkNode(mousePosWorld.getX(), heightmapPosition, mousePosWorld.getY(), nodeFlagType, false);
                addToNetworkNodeList.add(endNode);
            } else {
                if (bDebugLogLinearlineInfo) LOG.info("## LinearLine.commit() Debug ## End node {} selected, skipping node creation", endNode);
            }
        } else {
            for (int j = 0; j < linePreviewNodeList.size(); j++) {
                MapNode tempNode = linePreviewNodeList.get(j);
                double heightMapY = startNode.y + ( yInterpolation * j);

                MapNode nodeAtPoint = getNodeAtWorldPosition(tempNode.x, tempNode.z);
                if (nodeAtPoint == null) {
                    nodeAtPoint = createNewNetworkNode(tempNode.x, heightMapY, tempNode.z, nodeFlagType, false);
                    if (bDebugLogLinearlineInfo) LOG.info("## LinearLine.commit() Debug ## Created new MapNode {} at (x={}, y={},z={})",nodeAtPoint, tempNode.x, heightMapY, tempNode.z);
                } else {
                    if (bDebugLogLinearlineInfo) LOG.info("## LinearLine.commit() Debug ## Skipping MapNode creation, using existing node {} at (x={}, y={},z={})", nodeAtPoint, tempNode.x, heightMapY, tempNode.z);
                    existingNetworkNodesList.add(nodeAtPoint);
                }
                if (bDebugLogLinearlineInfo) LOG.info("## LinearLine.commit() Debug ## Adding node {} to networkNodeList", nodeAtPoint);
                addToNetworkNodeList.add(nodeAtPoint);
            }
        }

        // create the connections for all the nodes in the network list.
        ArrayList<Connection> connectList = connectNodes(addToNetworkNodeList);
        if (bDebugLogLinearlineInfo) LOG.info("Created {} connections", connectList.size());


        int numberOfNodes = addToNetworkNodeList.size();
        if (bDebugLogLinearlineInfo) LOG.info("## LinearLine.commit() Debug ## Total nodes in Linear line = {}",numberOfNodes);
        if (numberOfNodes > 0) {
            if (bDebugLogLinearlineInfo) LOG.info("## LinearLine.commit() Debug ## Creating Full SnapShot: Start = {}, Linear line nodes = {} , type = {}", startNode, numberOfNodes, connectionType);
            if (existingNetworkNodesList.isEmpty()) {
                changeManager.addChangeable(new LinearLineChanger(addToNetworkNodeList, SnapShot.SnapShotType.FULL));
            } else {
                if (bDebugLogLinearlineInfo) LOG.info("## LinearLine.commit() Debug ## LinearLine is using existing MapNodes, Creating Manual SnapShot: Start = {}, end = {} , type = {}", startNode, endNode, connectionType);
                // remove the existing nodes from the network node list, so the undo/redo system doesn't try to remove them
                addToNetworkNodeList.removeAll(existingNetworkNodesList);
                // create a manual SnapShot as the linear lines is using existing nodes
                changeManager.addChangeable(new LinearLineChanger(addToNetworkNodeList, connectList, MANUAL));
            }
        } else {
            if (bDebugLogLinearlineInfo) LOG.info("## LinearLine.commit() Debug ## Creating Connection only SnapShot: Start = {}, end = {} , type = {}", startNode, endNode, connectionType);
            changeManager.addChangeable(new LinearLineChanger(startNode, endNode, CONNECTION_ONLY));

        }
        showInTextArea(getLocaleString("toolbar_nodes_connection_completed_infotext"), true, false);
        setStale(true);

        resumeAutoSaving();
    }

    /**
     * This method is used to connect all the nodes in the curve.
     * @param nodesList List of nodes added to map
     */
    private ArrayList<Connection> connectNodes(LinkedList<MapNode> nodesList) {
        ArrayList<Connection> conList = new ArrayList<>();
        // loop through the remaining nodes and createSetting connections between them
        if (nodesList.isEmpty()) {
            if (bDebugLogLinearlineInfo) LOG.info("## LinearLine.connectNodes() Debug ## No interpolation nodes generated");
            if (endNode != null) {
                if (bDebugLogLinearlineInfo) LOG.info("## LinearLine.connectNodes() Debug ## Adding connection from start {} -> end {}", startNode, endNode);
                createConnectionBetween(startNode, endNode, connectionType);
                conList.add(new Connection(startNode, endNode, connectionType));
            } else {
                if (bDebugLogLinearlineInfo) LOG.info("## LinearLine.connectNodes() Debug ## Adding connection from start to mouse position");
                createConnectionBetween(startNode, startNode, connectionType);
                conList.add(new Connection(startNode, startNode, connectionType));
            }
            return conList;

        } else if (nodesList.size() == 1) {
            if (bDebugLogLinearlineInfo) LOG.info("## LinearLine.connectNodes() Debug ## Only 1 interpolation node in list");
            createConnectionBetween(startNode, nodesList.getFirst(), connectionType);
            conList.add(new Connection(startNode, nodesList.getFirst(), connectionType));
            if (bDebugLogLinearlineInfo) LOG.info("## LinearLine.connectNodes() Debug ## Added connection from start ( {} ) -> first node  ( {} )", startNode, nodesList.getFirst());
            if (endNode != null) {
                createConnectionBetween(nodesList.getLast(), endNode, connectionType);
                conList.add(new Connection(nodesList.getLast(), endNode, connectionType));
                if (bDebugLogLinearlineInfo) LOG.info("## LinearLine.connectNodes() Debug ## Adding connection from start ( {} ) -> endNode ( {} )", startNode, endNode);
            } else {
                if (bDebugLogLinearlineInfo) LOG.info("## LinearLine.connectNodes() Debug ## No end node selected, using mouse position");
                createConnectionBetween(nodesList.getLast(), nodesList.getLast(), connectionType);
                conList.add(new Connection(nodesList.getLast(), nodesList.getLast(), connectionType));
            }

        } else {
            if (bDebugLogLinearlineInfo) LOG.info("## LinearLine.connectNodes() Debug ## Creating connections for {} nodes", nodesList.size());

            createConnectionBetween(startNode, nodesList.getFirst(), connectionType);
            conList.add(new Connection(startNode, nodesList.getFirst(), connectionType));
            if (bDebugLogLinearlineInfo) LOG.info("## LinearLine.connectNodes() Debug ## Added connection from start {} -> First node {}", startNode, nodesList.getFirst());

            for (int j = 0; j < nodesList.size() - 1; j++) {
                MapNode startNode = nodesList.get(j);
                MapNode endNode = nodesList.get(j+1);
                boolean success = createConnectionBetween(startNode,endNode, connectionType);
                if (success) {
                    if (bDebugLogLinearlineInfo) LOG.info("## LinearLine.connectNodes() Debug ## Adding connection {} -> {}", startNode, endNode);
                    conList.add(new Connection(startNode, endNode, connectionType));
                }
            }
            if (endNode != null) {
                if (bDebugLogLinearlineInfo) LOG.info("## LinearLine.connectNodes() Debug ## End node selected, adding connection {} -> {}", nodesList.getLast(), endNode);
                createConnectionBetween(nodesList.getLast(), endNode, connectionType);
                conList.add(new Connection(nodesList.getLast(), endNode, connectionType));
            }
        }
        return conList;
    }


    /**
     * Clears the list of preview nodes.
     */
    public void clear() {
        this.linePreviewNodeList.clear();
        this.linePreviewConnectionList.clear();
        this.startNode = null;
        this.endNode = null;
        getMapPanel().repaint();
    }

    //
    // Getters
    //

    /**
     * Get the list of preview nodes. Used for rendering the preview
     *
     * @return The list of preview nodes.
     */
    public LinkedList<MapNode> getLinearLinePreviewList() {
        return linePreviewNodeList;
    }

    /**
     * Gets the end point of the linear line in world coordinates.
     *
     * @return The end point of the line.
     */
    public Point2D getLineEndWorld() {
        return lineEndWorld;
    }

    /**
     * Gets the end point of the linear line in screen coordinates.
     *
     * @return The end point of the line.
     */
    public Point2D getLineEndScreen() {
        return worldPosToScreenPos(lineEndWorld);
    }

    /**
     * Gets the start node of the linear line.
     *
     * @return The end node of the line.
     */
    public MapNode getStartNode() {
        return startNode;
    }

    /**
     * Gets the end node of the linear line.
     *
     * @return The end node of the line.
     */
    public MapNode getEndNode() {
        return endNode;
    }

    /**
     * Gets the list of MapNodes added to the Road network.
     *
     * @return The list of connections.
     */
    public LinkedList<MapNode> getAddToNetworkNodeList() {
        return addToNetworkNodeList;
    }

    //
    // Setters
    //

    /**
     * Sets the start node of the linear line.
     *
     * @param startNode The start node to set.
     */
    public void setStartNode(MapNode startNode) { this.startNode = startNode; }

    /**
     * Sets the end node of the linear line.
     *
     * @param selected The end node to set.
     */
    public void setEndNode(MapNode selected) { endNode = selected; }

    /**
     * Sets the end location of the linear line in world coordinates.
     *
     * @param x The x-coordinate of the end location.
     * @param y The y-coordinate of the end location.
     */
    public void setEndLocation(double x, double y) { this.lineEndWorld.setLocation(x, y); }


    /**
     * set the connection type for the linear line
     * @param connectionType The connection type to set.
     */
    public void setConnectionType(Connection.ConnectionType connectionType) {
        if (bDebugLogLinearlineInfo) LOG.info("Setting connection type to {}", connectionType);
        this.connectionType = connectionType;
        createLinerLinePreview();
        getMapPanel().repaint();
    }

    /**
     * Sets the node type for the linear line.
     *
     * @param nodeFlag The node flag type to set.
     */
    public void setNodeFlagType(int nodeFlag) {
        if (bDebugLogLinearlineInfo) LOG.info("Setting node flag type to {}", nodeFlag);
        this.nodeFlagType = nodeFlag;
        createLinerLinePreview();
        getMapPanel().repaint();
    }

    //
    // Draw to Screen
    //
    @Override
    public void drawToScreen(Graphics g) {
        if (startNode != null) {
            Graphics2D g2d = (Graphics2D) g.create();
            g2d.setComposite(AlphaComposite.SrcOver.derive(0.55f));
            Color arrowColor = (connectionType != null) ? connectionType.getColor(startNode) : Color.WHITE;

            // draw the preview nodes
            if (!linePreviewNodeList.isEmpty()) {
                // Draw the preview nodes
                for (MapNode node : linePreviewNodeList) {
                    Point2D nodeScreenPos = worldPosToScreenPos(node.getX(), node.getZ());
                    g2d.setColor((node.getFlag() == NODE_FLAG_REGULAR) ? lighten(colourNodeRegular,50) : colourNodeSubprio);
                    g2d.fillArc((int) (nodeScreenPos.getX() - nodeSizeScaledHalf), (int) (nodeScreenPos.getY() - nodeSizeScaledHalf), (int) nodeSizeScaled, (int) nodeSizeScaled, 0, 360);
                }
                // draw the preview connections
                for (Connection c : linePreviewConnectionList) {
                    drawArrowBetween(g2d, c.getStartNode().getScreenPosition2D(), c.getEndNode().getScreenPosition2D(), c.isDual(), c.getColor());
                }
                // if end node selected, draw arrow between the last preview node and the selected end node
                if (endNode != null) {
                    drawArrowBetween(g2d, linePreviewNodeList.getLast().getScreenPosition2D(), endNode.getScreenPosition2D(), connectionType == DUAL, arrowColor);
                }
            } else {
                // if end node selected, draw arrow between the start node and mouse pointer
                if (endNode != null) {
                    drawArrowBetween(g2d, startNode.getScreenPosition2D(), endNode.getScreenPosition2D(), connectionType == DUAL, arrowColor);
                } else {
                    // draw the preview node at the mouse position
                    g2d.setColor((startNode.getFlag() == NODE_FLAG_REGULAR) ? lighten(colourNodeRegular,50) : colourNodeSubprio);
                    g2d.fillArc((int) (currentMouseX - nodeSizeScaledHalf), (int) (currentMouseY - nodeSizeScaledHalf), (int) nodeSizeScaled, (int) nodeSizeScaled, 0, 360);
                    // draw the arrow between the start node and the mouse position
                    drawArrowBetween(g2d, startNode.getScreenPosition2D(), new Point2D.Double(currentMouseX, currentMouseY), connectionType == DUAL, arrowColor);
                }
            }
            // dispose of the graphics context
            g2d.dispose();
        }
    }

    //
    // LinerLine Changer
    //

    public static class LinearLineChanger implements ChangeManager.Changeable {

        private final SnapShot snapShot;
        private final SnapShot.SnapShotType snapShotType;
        private final boolean isStale;

        public LinearLineChanger(LinkedList<MapNode> generatedNodes, SnapShot.SnapShotType snapShotType) {
            super();
            this.snapShot = new SnapShot(generatedNodes);
            this.snapShotType = snapShotType;
            this.isStale = isStale();
        }

        public LinearLineChanger(LinkedList<MapNode> generatedNodes, ArrayList<Connection> generatedConnections, SnapShot.SnapShotType snapShotType) {
            super();
            this.snapShot = new SnapShot(generatedNodes, generatedConnections, MANUAL);
            this.snapShotType = snapShotType;
            this.isStale = isStale();
        }

        public LinearLineChanger(MapNode startNode, MapNode endNode, SnapShot.SnapShotType snapShotType) {
            super();
            this.snapShot = new SnapShot(startNode, endNode, snapShotType);
            this.snapShotType = snapShotType;
            this.isStale = isStale();
        }

        public void undo() {
            if (bDebugLogUndoRedoInfo) LOG.info("## LinearLineChanger.undo ## undo linear line");
            if (this.snapShotType == FULL || this.snapShotType == MANUAL) {
                if (bDebugLogUndoRedoInfo) LOG.info("## LinearLineChanger.undo ## full undo");
                this.snapShot.removeOriginalNodes();
            } else if (this.snapShotType == CONNECTION_ONLY) {
                if (bDebugLogUndoRedoInfo) LOG.info("## LinearLineChanger.undo ## connection only undo");
                this.snapShot.removeOriginalConnections();
            }
            setStale(this.isStale);
            getMapPanel().repaint();
        }

        public void redo() {
            try {
                if (bDebugLogUndoRedoInfo) LOG.info("## LinearLineChanger.undo ## re-do linear line");
                if (this.snapShotType == FULL || this.snapShotType == MANUAL) {
                    if (bDebugLogUndoRedoInfo) LOG.info("## LinearLineChanger.redo ## full redo");
                    this.snapShot.restoreOriginalNodes();
                } else if (this.snapShotType == CONNECTION_ONLY) {
                    if (bDebugLogUndoRedoInfo) LOG.info("## LinearLineChanger.redo ## connection only redo");
                    this.snapShot.restoreOriginalConnections();
                }
            } catch (ExceptionUtils.MismatchedIdException e) {
                showMismatchedIDError("LinearLineChanger redo()", e);
            }

            getMapPanel().repaint();
            setStale(true);
        }
    }
}
