package AutoDriveEditor.GUI.Buttons.Nodes;

import AutoDriveEditor.GUI.Buttons.BaseButton;
import AutoDriveEditor.Managers.ChangeManager;
import AutoDriveEditor.MapPanel.MapPanel;
import AutoDriveEditor.RoadNetwork.MapNode;

import javax.swing.*;
import java.awt.event.MouseEvent;
import java.awt.geom.Point2D;
import java.util.LinkedList;

import static AutoDriveEditor.AutoDriveEditor.changeManager;
import static AutoDriveEditor.GUI.Buttons.Curves.CubicCurveButton.cubicCurve;
import static AutoDriveEditor.GUI.Buttons.Curves.CubicCurveButton.isCubicCurveCreated;
import static AutoDriveEditor.GUI.Buttons.Curves.QuadCurveButton.isQuadCurveCreated;
import static AutoDriveEditor.GUI.Buttons.Curves.QuadCurveButton.quadCurve;
import static AutoDriveEditor.GUI.MenuBuilder.bDebugLogUndoRedo;
import static AutoDriveEditor.Listeners.MouseListener.prevMousePosX;
import static AutoDriveEditor.Listeners.MouseListener.prevMousePosY;
import static AutoDriveEditor.Locale.LocaleManager.getLocaleString;
import static AutoDriveEditor.Managers.MultiSelectManager.*;
import static AutoDriveEditor.Managers.ScanManager.checkAreaForNodeOverlap;
import static AutoDriveEditor.Managers.ScanManager.checkNodeOverlap;
import static AutoDriveEditor.MapPanel.MapPanel.*;
import static AutoDriveEditor.Utils.GUIUtils.makeImageToggleButton;
import static AutoDriveEditor.Utils.LoggerUtils.LOG;
import static AutoDriveEditor.Utils.MathUtils.roundUpDoubleToDecimalPlaces;
import static AutoDriveEditor.XMLConfig.EditorXML.*;

public class MoveNodeButton extends BaseButton {

    MapNode selectedNode;
    boolean removeSelectedOnComplete;
    public static  boolean isDraggingNode = false;

    public MoveNodeButton(JPanel panel) {
        button = makeImageToggleButton("buttons/movenode", "buttons/movenode_selected", null,"nodes_move_tooltip","nodes_move_alt", panel, false, false,  null, false, this);
    }

    @Override
    public String getButtonID() { return "MoveNodeButton"; }

    @Override
    public String getButtonAction() { return "ActionButton"; }

    @Override
    public String getButtonPanel() { return "Nodes"; }

    @Override
    public String getInfoText() { return getLocaleString("nodes_move_tooltip"); }

    @Override
    public void mousePressed(MouseEvent e) {
        if (e.getButton() == MouseEvent.BUTTON1) {
            selectedNode = getNodeAtScreenPosition(e.getX(), e.getY());
            if (selectedNode != null) {
                if (bGridSnap || bGridSnapSubs) {
                    Point2D p = worldPosToScreenPos(selectedNode.x, selectedNode.z);
                    preSnapX = p.getX();
                    preSnapY = p.getY();
                }
                moveDiffX = 0;
                moveDiffY = 0;
                isDraggingNode = true;
                if (!multiSelectList.contains(selectedNode)) {
                    multiSelectList.add(selectedNode);
                    removeSelectedOnComplete = true;
                }
            }
        }
        if (e.getButton() == MouseEvent.BUTTON3) {
            startMultiSelect(e.getX(), e.getY());
        }
    }

    @Override
    public void mouseReleased(MouseEvent e) {
        if (e.getButton() == MouseEvent.BUTTON3) {
            stopMultiSelect(e.getX(), e.getY());
            getAllNodesInSelectedArea(rectangleStart, rectangleEnd);
        }
        if (e.getButton() == MouseEvent.BUTTON1) {
            if ( selectedNode != null ) {

                if (bGridSnap || bGridSnapSubs) {
                    Point2D p = worldPosToScreenPos(selectedNode.x, selectedNode.z);
                    moveDiffX = (int) (p.getX() - preSnapX);
                    moveDiffY = (int) (p.getY() - preSnapY);
                }

                changeManager.addChangeable( new MoveNodeChanger(multiSelectList, moveDiffX, moveDiffY));
                MapPanel.setStale(true);
                if (removeSelectedOnComplete) {
                    multiSelectList.remove(selectedNode);
                    removeSelectedOnComplete = false;
                }
                checkNodeOverlap(selectedNode);
                isDraggingNode = false;
            }
        }
    }

    @Override
    public void mouseDragged(MouseEvent e) {
        int diffX = e.getX() - prevMousePosX;
        int diffY = e.getY() - prevMousePosY;
        if (isDraggingNode) {
            moveDiffX += diffX;
            moveDiffY += diffY;
            if (bGridSnap) {
                snapMoveNodeBy(multiSelectList, diffX, diffY);
            } else {
                moveNodeBy(multiSelectList, diffX, diffY, false);
            }
        }
        getMapPanel().repaint();
    }

    private void snapMoveNodeBy(LinkedList<MapNode> nodeList, int diffX, int diffY) {
        double scaledDiffX;
        double scaledDiffY;

        canAutoSave = false;

        Point2D p = screenPosToWorldPos( prevMousePosX + diffX, prevMousePosY + diffY);
        double newX, newY;
        if (bGridSnapSubs) {
            newX = Math.round(p.getX() / (gridSpacingX / (gridSubDivisions + 1))) * (gridSpacingX / (gridSubDivisions + 1));
            newY = Math.round(p.getY() / (gridSpacingY / (gridSubDivisions + 1))) * (gridSpacingY / (gridSubDivisions + 1));
        } else {
            newX = Math.round(p.getX() / gridSpacingX) * gridSpacingX;
            newY = Math.round(p.getY() / gridSpacingY) * gridSpacingY;
        }
        scaledDiffX = newX - selectedNode.x;
        scaledDiffY = newY - selectedNode.z;

        for (MapNode node : nodeList) {
            if (!node.isControlNode) {
                if (node.x + scaledDiffX > -1024 * mapZoomFactor && node.x + scaledDiffX < 1024 * mapZoomFactor) {
                    node.x = roundUpDoubleToDecimalPlaces(node.x + scaledDiffX, 3);
                }
                if (node.z + scaledDiffY > -1024 * mapZoomFactor && node.z + scaledDiffY < 1024 * mapZoomFactor) {
                    node.z = roundUpDoubleToDecimalPlaces(node.z + scaledDiffY, 3);
                }
            }
            if (isQuadCurveCreated) {
                if (node == quadCurve.getCurveStartNode()) {
                    quadCurve.setCurveStartNode(node);
                } else if (node == quadCurve.getCurveEndNode()) {
                    quadCurve.setCurveEndNode(node);
                }
                if (node == quadCurve.getControlPoint()) {
                    quadCurve.updateControlPoint(scaledDiffX, scaledDiffY);
                }
            }
            if (isCubicCurveCreated) {
                if (node == cubicCurve.getCurveStartNode()) {
                    cubicCurve.setCurveStartNode(node);
                } else if (node == cubicCurve.getCurveEndNode()) {
                    cubicCurve.setCurveEndNode(node);
                }
                if (node == cubicCurve.getControlPoint1()) {
                    cubicCurve.updateControlPoint1(scaledDiffX, scaledDiffY);
                }
                if (node == cubicCurve.getControlPoint2()) {
                    cubicCurve.updateControlPoint2(scaledDiffX, scaledDiffY);
                }
            }
        }
        canAutoSave = true;
        getMapPanel().repaint();
    }

    private void moveNodeBy(LinkedList<MapNode> nodeList, int diffX, int diffY, boolean snapOverride) {
        double scaledDiffX;
        double scaledDiffY;

        canAutoSave = false;

        for (MapNode node : nodeList) {
            if (bGridSnap && !snapOverride) {
                Point2D p = screenPosToWorldPos( prevMousePosX + diffX, prevMousePosY + diffY);
                double newX, newY;
                if (bGridSnapSubs) {
                    newX = Math.round(p.getX() / (gridSpacingX / (gridSubDivisions + 1))) * (gridSpacingX / (gridSubDivisions + 1));
                    newY = Math.round(p.getY() / (gridSpacingY / (gridSubDivisions + 1))) * (gridSpacingY / (gridSubDivisions + 1));
                } else {
                    newX = Math.round(p.getX() / gridSpacingX) * gridSpacingX;
                    newY = Math.round(p.getY() / gridSpacingY) * gridSpacingY;
                }
                scaledDiffX = newX - node.x;
                scaledDiffY = newY - node.z;

            } else {
                scaledDiffX = roundUpDoubleToDecimalPlaces((diffX * mapZoomFactor) / zoomLevel, 3);
                scaledDiffY = roundUpDoubleToDecimalPlaces((diffY * mapZoomFactor) / zoomLevel, 3);
            }

            if (!node.isControlNode) {
                if (node.x + scaledDiffX > -1024 * mapZoomFactor && node.x + scaledDiffX < 1024 * mapZoomFactor) {
                    if (bGridSnap && !snapOverride) {
                        node.x = Math.round((node.x + scaledDiffX) * 50D) / 50D;
                    } else {
                        node.x = roundUpDoubleToDecimalPlaces(node.x + scaledDiffX, 3);
                    }
                }
                if (node.z + scaledDiffY > -1024 * mapZoomFactor && node.z + scaledDiffY < 1024 * mapZoomFactor) {
                    if (bGridSnap && !snapOverride) {
                        node.z = Math.round((node.z + scaledDiffY) * 50D) / 50D;
                    } else {
                        node.z = roundUpDoubleToDecimalPlaces(node.z + scaledDiffY, 3);
                    }
                }
            }

            if (isQuadCurveCreated) {
                if (node == quadCurve.getCurveStartNode()) {
                    quadCurve.setCurveStartNode(node);
                } else if (node == quadCurve.getCurveEndNode()) {
                    quadCurve.setCurveEndNode(node);
                }
                if (node == quadCurve.getControlPoint()) {
                    quadCurve.updateControlPoint(scaledDiffX, scaledDiffY);
                }
            }
            if (isCubicCurveCreated) {
                if (node == cubicCurve.getCurveStartNode()) {
                    cubicCurve.setCurveStartNode(node);
                } else if (node == cubicCurve.getCurveEndNode()) {
                    cubicCurve.setCurveEndNode(node);
                }
                if (node == cubicCurve.getControlPoint1()) {
                    cubicCurve.updateControlPoint1(scaledDiffX, scaledDiffY);
                }
                if (node == cubicCurve.getControlPoint2()) {
                    cubicCurve.updateControlPoint2(scaledDiffX, scaledDiffY);
                }
            }
        }
        canAutoSave = true;
        getMapPanel().repaint();
    }

    //
    //  Move Nodes Undo
    //

    public class MoveNodeChanger implements ChangeManager.Changeable {
        private final LinkedList<MapNode> moveNodes;
        private final int diffX;
        private final int diffY;
        //private final boolean wasSnapMove;
        private final boolean isStale;

        public MoveNodeChanger(LinkedList<MapNode> mapNodesMoved, int movedX, int movedY){
            super();
            this.moveNodes = new LinkedList<>();
            if (bDebugLogUndoRedo) LOG.info("node moved = {} , {}", movedX, movedY);
            this.diffX = movedX;
            this.diffY = movedY;
            //this.wasSnapMove = snapMove;
            this.moveNodes.addAll(mapNodesMoved);
            this.isStale = isStale();
        }

        public void undo(){
            moveNodeBy(this.moveNodes, -this.diffX, -this.diffY, true);
            for (MapNode node : this.moveNodes) {
                checkAreaForNodeOverlap(node);
            }
            getMapPanel().repaint();
            setStale(this.isStale);
        }

        public void redo(){
            moveNodeBy(this.moveNodes, this.diffX, this.diffY, true);
            for (MapNode node : this.moveNodes) {
                checkAreaForNodeOverlap(node);
            }
            getMapPanel().repaint();
            setStale(true);
        }
    }
}
