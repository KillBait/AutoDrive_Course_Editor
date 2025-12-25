package AutoDriveEditor.Classes.Curves;

import AutoDriveEditor.Classes.Interfaces.CurveInterface;
import AutoDriveEditor.Classes.SnapShot;
import AutoDriveEditor.Classes.Util_Classes.ExceptionUtils;
import AutoDriveEditor.GUI.Curves.CurvePanel;
import AutoDriveEditor.Managers.ChangeManager;
import AutoDriveEditor.Managers.CurveManager;
import AutoDriveEditor.RoadNetwork.Connection;
import AutoDriveEditor.RoadNetwork.MapNode;

import java.awt.*;
import java.awt.event.MouseEvent;
import java.awt.event.MouseWheelEvent;
import java.awt.geom.Point2D;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.LinkedList;
import java.util.Map;

import static AutoDriveEditor.AutoDriveEditor.curveManager;
import static AutoDriveEditor.AutoDriveEditor.getMapPanel;
import static AutoDriveEditor.Classes.Util_Classes.ColourUtils.lighten;
import static AutoDriveEditor.Classes.Util_Classes.LoggerUtils.LOG;
import static AutoDriveEditor.GUI.Buttons.BaseButton.drawArrowBetween;
import static AutoDriveEditor.GUI.MapPanel.*;
import static AutoDriveEditor.GUI.Menus.DebugMenu.Logging.LogCurveInfoMenu.bDebugLogCurveInfo;
import static AutoDriveEditor.GUI.Menus.DebugMenu.Logging.LogUndoRedoMenu.bDebugLogUndoRedoInfo;
import static AutoDriveEditor.Managers.ScanManager.checkAreaForNodeOverlap;
import static AutoDriveEditor.RoadNetwork.MapNode.NODE_FLAG_REGULAR;
import static AutoDriveEditor.RoadNetwork.RoadMap.showMismatchedIDError;
import static AutoDriveEditor.XMLConfig.AutoSave.resumeAutoSaving;
import static AutoDriveEditor.XMLConfig.AutoSave.suspendAutoSaving;
import static AutoDriveEditor.XMLConfig.EditorXML.*;

@SuppressWarnings("unused")
abstract class CurveBase implements CurveInterface {

    protected Map<Integer, MapNode> controlPointsMap;
    protected Map<MapNode, Point2D.Double> virtualControlPointMap;

    private final LinkedList<MapNode> curvePreviewNodesList;
    private final ArrayList<Connection> curvePreviewConnectionList;
    protected MapNode selectedControlPoint;
    private int curvePriority;
    private Connection.ConnectionType curveType;
    private int numInterpolations;
    private int numCurrentControlPoints = 1;

    String instanceName = this.getClass().getSimpleName();

    //
    // Every class that extends CurveBase has to implement these 3 methods
    //

    protected abstract void initControlPoints();
    protected abstract void generateInterpolationPoints();
    protected abstract void generateConnections();

    /**
     * Constructor for the CurveBase class, initializes the
     * curve and generates the initial interpolation points.
     */
    public CurveBase() {
        if (bDebugLogCurveInfo) LOG.info("## {}.constructor ## Initializing curve", instanceName);
        this.curvePreviewNodesList = new LinkedList<>();
        this.curvePreviewConnectionList = new ArrayList<>();
        this.numInterpolations = curveIterationsDefault;
        this.curveType = Connection.ConnectionType.REGULAR;
        controlPointsMap = new HashMap<>();
        virtualControlPointMap = new HashMap<>(getMaxControlPoints());
        initCurve();
    }

    @Override
    public void initCurve() {}

    // The CurveBase class does not use most of the mouse events by design, it is implemented
    // here to allow the extended classes to override them and add functionality if needed.
    //
    // NOTE: Any extended classes that uses mouseMoved() must call super() or the
    // highlight on hover for control nodes won't occur.
    @Override
    public void mousePressed(MouseEvent e) {}

    @Override
    public void mouseReleased(MouseEvent e) {}

    @Override
    public void mouseClicked(MouseEvent e) {}

    @Override
    public void mouseDragged(MouseEvent e) {}

    @Override
    public void mouseWheelMoved(MouseWheelEvent e) {}

    // if an extended class overwrites mouseHover(), they should call super.mouseMoved(e) to ensure
    // the hover selection of the control points works
     @Override
    public void mouseMoved(MouseEvent e) {
        selectedControlPoint = getControlNodeAtScreenPosition(e.getX(), e.getY());
    }

    /**
     * <p>Basic checks are performed before the interpolation points
     * for the curve are generated.</p>
     * <br>This method then calls the curve preview generation code.
     */
    private void generatePreview() {
        generateInterpolationPoints();
        generateConnections();
    }

    @Override
    public void moveControlNodeBy(MapNode node, double diffX, double diffY, boolean applyScaling) {}

    /**
     * This method is used to get the control node at the specified screen position.
     * @param e MouseEvent containing the screen position
     * @return MapNode at the specified screen position, null if none found
     */
    private MapNode getControlNodeAtScreenPosition(MouseEvent e) {
        return getControlNodeAtScreenPosition(e.getX(), e.getY());
    }

    /**
     * This method is used to get the control node at the specified screen position.
     * @param x X coordinate of the screen position
     * @param y Y coordinate of the screen position
     * @return MapNode at the specified screen position, null if none found
     */
    private MapNode getControlNodeAtScreenPosition(int x, int y) {
        MapNode selectedNode = null;
        for (Map.Entry<Integer, MapNode> entry : controlPointsMap.entrySet()) {
            MapNode controlNode = entry.getValue();
            Point2D cpScreenPosition = worldPosToScreenPos(controlNode.getX(), controlNode.getZ());
            if (x < cpScreenPosition.getX() + nodeSizeScaledHalf &&
                    x > cpScreenPosition.getX() - nodeSizeScaledHalf &&
                    y < cpScreenPosition.getY() + nodeSizeScaledHalf &&
                    y > cpScreenPosition.getY() - nodeSizeScaledHalf) {
                return controlNode;
            }
        }
        return null;
    }

    @Override
    public void commitCurve() {}

    /**
     * This method is used to cancel the curve creation process.
     * It clears all the control points and resets the curve nodes list.
     */
    @Override
    public void cancelCurve() {
        controlPointsMap.clear();
        virtualControlPointMap.clear();
        curvePreviewNodesList.clear();
        //startNode = null;
    }

    /**
     * This method is used to updateWidget the curve preview.
     */
    @Override
    public void updateCurve() {
        generatePreview();
        getMapPanel().repaint();
    }

    //
    // Getters
    //


    /**
     * This method is used to build a list of all active control points.
     * @return ArrayList of all active control points
     * @see CurveManager
     */
    @Override
    public ArrayList<MapNode> getActiveControlPoints() {
        ArrayList<MapNode> controlPoints = new ArrayList<>();
        for (Map.Entry<Integer, MapNode> entry : controlPointsMap.entrySet()) {
            if (entry.getKey() < getMaxControlPoints()) {
                controlPoints.add(entry.getValue());
            }
        }
        return controlPoints;
    }

    public Point2D rotateControlNode(MapNode rotationNode, int mouseX, int mouseY) { return null; }
    public Point2D rotateStartNode(MapNode rotationNode, int mouseX, int mouseY) { return null; }
    public Point2D rotateEndNode(MapNode rotationNode, int mouseX, int mouseY) { return null; }


    /**
     * Returns an array of all the preview nodes.
     * @return LinkedList() of all preview nodes
     */
    public LinkedList<MapNode> getCurvePreviewNodesList() { return curvePreviewNodesList; }

    /**
     * Returns an array of all the preview connections.
     * @return ArrayList() of all preview connections
     */
    public ArrayList<Connection> getCurvePreviewConnectionList() { return curvePreviewConnectionList; }

    /**
     * Returns the number of control points used by the current curve.
     * @return Integer of total control points in use.
     */
    public int getNumCurrentControlPoints() { return numCurrentControlPoints; }

    /**
     * Returns the node priority of the curve points.
     * @return Integer of either 0 (Normal) or 1 (Subprio).
     */
    public int getCurvePriority() { return curvePriority; }

    /**
     * Returns the number of interpolations used by the curve.
     * @return Integer of the number of interpolations.
     */
    @Override
    public int getNumInterpolations() { return numInterpolations; }

    /**
     * Returns the type of curve used by the curve.
     * @return Connection.ConnectionType of the curve.
     * @see Connection.ConnectionType
     */
    @Override
    public Connection.ConnectionType getCurveType() { return curveType; }

    //
    // Setters
    //

    /**
     * Sets the priority of the interpolated points.
     * @param nodeType Integer of either 0 (Normal) or 1 (Subprio).
     * @see MapNode
     */
    @Override
    public void setCurvePriority(int nodeType) {
        this.curvePriority = nodeType;
        updateCurve();
    }

    /**
     * Sets the type of connection used to generate the curve.
     * @param type ConnectionType of the curve.
     * @see Connection.ConnectionType
     */
    @Override
    public void setCurveType(Connection.ConnectionType type) {
        this.curveType = type;
        updateCurve();
    }

    /**
     * Sets the number of interpolations used by the curve.
     * @param numPoints Integer of the number of interpolations.
     */
    @Override
    public void setCurveInterpolations(int numPoints) {
        this.numInterpolations = numPoints;
        updateCurve();
    }

    /**
     * Sets the number of control points used by the curve.<br>
     * Will also trigger a preview updateWidget.
     * @param numControlPoints Integer of the number of control points.
     * @see CurveManager
     * @see CurvePanel
     */
    @Override
    public void setNumCurrentControlPoints(int numControlPoints) {
        if (numControlPoints < numCurrentControlPoints) {
            MapNode controlNode = curveManager.getCurrentCurveWidget().getCurrentAnchor();
            if (controlNode != null && controlNode == controlPointsMap.get(numControlPoints)) {
                controlNode.setSelected(false);
                curveManager.getCurrentCurveWidget().setCurrentAnchor(controlPointsMap.get(numControlPoints-1));
            }
        } else if (numControlPoints > numCurrentControlPoints) {
            if (curveManager.getCurrentCurveWidget() != null && curveManager.getCurrentCurveWidget().getCurrentAnchor() != null) {
                //curveManager.getCurrentCurveWidget().getCurrentAnchor().setSelected(false);
                curveManager.getCurrentCurveWidget().setCurrentAnchor(controlPointsMap.get(numControlPoints-1));
            }
        }
        this.numCurrentControlPoints = numControlPoints;
        updateCurve();
    }

    /**
     * Check if the supplied MapNode is a control Node.
     * @param node MapNode to check.
     * @return Boolean true if control node, false if not.
     */
    @Override
    public boolean isControlNode(MapNode node) {
        for (Map.Entry<Integer, MapNode> entry : controlPointsMap.entrySet()) {
            MapNode listNode = entry.getValue();
            if (listNode.isControlNode()) {
                return true;
            }
        }
        return false;
    }

    public ArrayList<MapNode> getNodes() { return null; }

    //
    // draw the curve preview to the screen
    //
    @Override
    public void drawToScreen(Graphics g) {
        if (!curvePreviewNodesList.isEmpty()) {
            Color colour;
            Graphics2D g2d = (Graphics2D) g.create();
            g2d.setComposite(AlphaComposite.SrcOver.derive(0.55f));
            // draw the preview nodes
            for (MapNode node : curvePreviewNodesList) {
                Point2D nodeScreenPos = worldPosToScreenPos(node.getX(), node.getZ());
                g2d.setColor((node.getFlag() == NODE_FLAG_REGULAR) ? lighten(colourNodeRegular,50) : colourNodeSubprio);
                g2d.fillArc((int) (nodeScreenPos.getX() - nodeSizeScaledHalf), (int) (nodeScreenPos.getY() - nodeSizeScaledHalf), (int) nodeSizeScaled, (int) nodeSizeScaled, 0, 360);
            }
            // draw the preview connections
            for (Connection c : curvePreviewConnectionList) {
                drawArrowBetween(g2d, c.getStartNode().getScreenPosition2D(), c.getEndNode().getScreenPosition2D(), c.isDual(), c.getColor());
            }
            // draw the control points
            if (curveManager.getCurrentCurve() == this) g2d.setComposite(AlphaComposite.SrcOver.derive(1f));
            controlPointsMap.entrySet().stream().limit(numCurrentControlPoints).forEach(entry -> {
                MapNode controlNode = entry.getValue();
                Point nodePos = worldPosToScreenPos(controlNode.x, controlNode.z);
                g2d.setColor(Color.WHITE);
                if (curveManager.getCurrentCurve() == this && controlNode.isControlNode()) {
                    int num = entry.getKey();
                    g2d.drawString("CP_" + (num + 1), (int) nodePos.getX() - 10, (int) nodePos.getY() - 15);
                }

                if (controlNode.isControlNode()) {
                    g2d.drawImage(controlNodeImage, (int) (nodePos.x - (double) (controlNodeImage.getWidth() / 2)), (int) (nodePos.y - (double) (controlNodeImage.getHeight() / 2)), (int) nodeSizeScaled, (int) nodeSizeScaled, null);
                    if (controlNode.isSelected() || /*controlNode.getPreviewNodeSelectionChange() ||*/ selectedControlPoint == controlNode ) {
                        g2d.drawImage(controlNodeSelectedImage, (int) (nodePos.x - (double) (controlNodeSelectedImage.getWidth() / 2)), (int) (nodePos.y - (double) (controlNodeSelectedImage.getHeight() / 2)), (int) nodeSizeScaled, (int) nodeSizeScaled, null);
                    }
                } else if (controlNode.isRotationNode()) {
                    if (controlNode.isSelected() || /*controlNode.getPreviewNodeSelectionChange() ||*/ selectedControlPoint == controlNode ) {
                        g2d.drawImage(rotationNodeSelectedImage, (int) (nodePos.x - (double) (controlNodeSelectedImage.getWidth() / 2)), (int) (nodePos.y - (double) (controlNodeSelectedImage.getHeight() / 2)), (int) nodeSizeScaled, (int) nodeSizeScaled, null);
                    } else {
                        g2d.drawImage(rotationNodeImage, (int) (nodePos.x - (double) (rotationNodeImage.getWidth() / 2)), (int) (nodePos.y - (double) (rotationNodeImage.getHeight() / 2)), (int) nodeSizeScaled, (int) nodeSizeScaled, null);
                    }
                }
            });
            g2d.dispose();
        }
    }

    //
    // Changeable implementation for the CurveBase class
    //
    public static class CurveChanger implements ChangeManager.Changeable {

        private final SnapShot curveSnapShot;
        private final boolean isStale;

        public CurveChanger(SnapShot snapShot) {
            this.curveSnapShot = snapShot;
            this.isStale = isStale();
        }

        public void undo() {
            suspendAutoSaving();
            if (bDebugLogUndoRedoInfo) LOG.info("## CurveBase.undo ## CurveBase.undo ## Removing snapshot");
            try {
                this.curveSnapShot.removeOriginalNodes();
            } catch (Exception e) {
                throw new RuntimeException(e);
            }
            for (MapNode node : this.curveSnapShot.getOriginalNodeList()) {
                checkAreaForNodeOverlap(node);
            }
            setStale(this.isStale);
            getMapPanel().repaint();
            resumeAutoSaving();
        }

        public void redo() {
            if (bDebugLogUndoRedoInfo) LOG.info("## CurveBase.undo ## CurveBase.redo ## Removing snapshot");
            suspendAutoSaving();
            try {
                this.curveSnapShot.restoreOriginalNodes();
            } catch (ExceptionUtils.MismatchedIdException e) {
                showMismatchedIDError("CurveChanger.redo()", e);
            }
            setStale(true);
            getMapPanel().repaint();
            resumeAutoSaving();
        }
    }

    @Override
    public String toString() {
        return String.format("%s@%s", this.getClass().getSimpleName(), Integer.toHexString(hashCode()));
    }
}
