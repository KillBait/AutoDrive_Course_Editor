package AutoDriveEditor.Classes.Widgets;

import AutoDriveEditor.Classes.Interfaces.CurveInterface;
import AutoDriveEditor.Classes.Interfaces.SelectorInterface;
import AutoDriveEditor.Classes.Widgets.Selectors.MoveSelector;
import AutoDriveEditor.Classes.Widgets.Selectors.SelectorBase;
import AutoDriveEditor.Managers.ChangeManager;
import AutoDriveEditor.Managers.CurveManager;
import AutoDriveEditor.RoadNetwork.MapNode;

import java.awt.*;
import java.awt.event.MouseEvent;
import java.awt.geom.Point2D;
import java.math.RoundingMode;
import java.util.ArrayList;

import static AutoDriveEditor.AutoDriveEditor.*;
import static AutoDriveEditor.Classes.Util_Classes.LoggerUtils.LOG;
import static AutoDriveEditor.Classes.Util_Classes.MathUtils.limitDoubleToDecimalPlaces;
import static AutoDriveEditor.Classes.Util_Classes.MathUtils.roundUpDoubleToDecimalPlaces;
import static AutoDriveEditor.GUI.MapPanel.*;
import static AutoDriveEditor.GUI.Menus.DebugMenu.Logging.LogMoveWidgetMenu.bDebugLogMoveWidget;
import static AutoDriveEditor.GUI.Menus.DebugMenu.Logging.LogUndoRedoMenu.bDebugLogUndoRedoInfo;
import static AutoDriveEditor.Managers.MultiSelectManager.*;
import static AutoDriveEditor.Managers.ScanManager.checkAreaForNodeOverlap;
import static AutoDriveEditor.Managers.ScanManager.checkNodeOverlap;
import static AutoDriveEditor.XMLConfig.AutoSave.resumeAutoSaving;
import static AutoDriveEditor.XMLConfig.AutoSave.suspendAutoSaving;
import static AutoDriveEditor.XMLConfig.EditorXML.*;

@SuppressWarnings("unused")
public class MoveWidget extends WidgetBase {

    SelectorInterface moveSelector;

    protected SelectorBase.SELECTED_AXIS lastUsedDirectionAxis = SelectorBase.SELECTED_AXIS.NONE;
    protected SelectorBase.SELECTED_AXIS directionAxis = SelectorBase.SELECTED_AXIS.NONE;

    public MoveWidget() {}

    @Override
    public void initSelectors() {
        if (bDebugLogMoveWidget) LOG.info("## {}.initSelectors() ## Initializing MoveWidget selectors", instanceName);
        moveSelector = new MoveSelector();
        selectorList.add(moveSelector);
        setCurrentSelector(moveSelector);
    }

    @Override
    public void mouseClicked(MouseEvent e) {}

    @Override
    public void mousePressed(MouseEvent e) {
        if (e.getButton() == MouseEvent.BUTTON1) {
            MapNode pressedNode = getNodeAtScreenPosition(e.getX(), e.getY());
            if (currentAnchor != null || pressedNode != null ) {
                directionAxis = currentSelector.checkSelectedAxis(e.getPoint());
                if (directionAxis != SelectorBase.SELECTED_AXIS.NONE ) {
                    this.isDraggingWidget = true;
                    widgetPosX = currentAnchor.getX();
                    widgetPosY = currentAnchor.getZ();
                    moveDiffX = 0;
                    moveDiffY = 0;
                    if (bDebugLogMoveWidget) LOG.info("## {}.mousePressed() ## Mouse on widget, starting {} axis drag", instanceName, directionAxis);
                }
            } else {
                if (bDebugLogMoveWidget) LOG.info("## {}.mousePressed() ## Mouse Press not on Widget", instanceName);
            }
        }
    }

    @Override
    public void mouseReleased(MouseEvent e) {
        MapNode releaseNode = getNodeAtScreenPosition(e.getX(), e.getY());
        if (e.getButton() == MouseEvent.BUTTON1) {
            if (isDraggingWidget) {
                changeManager.addChangeable(new MoveWidgetChanger(multiSelectList, moveDiffX, moveDiffY));
                setStale(true);
                isDraggingWidget = false;
            } else {
                // get the MapNode at the button press position ( if any )
                if (currentSelector.checkSelectedAxis(e.getPoint()) == MoveSelector.SELECTED_AXIS.NONE && releaseNode != null) {
                    if (multiSelectList.isEmpty()) {
                        // no nodes are selected, make the selected node the widget anchor if it is selectable
                        if (releaseNode.isSelectable() && !releaseNode.isRotationNode()) {
                            addToMultiSelectList(releaseNode);
                            //selectedNode.setSelected(true);
                            setCurrentAnchor(releaseNode);
                            if (bDebugLogMoveWidget) LOG.info("## {}.mouseReleased() ## setting initial anchor node to {}", instanceName, releaseNode.getID());
                        }
                    } else {
                        // check if the selected node is the same as the anchor selected node
                        if (multiSelectList.size() == 1) {
                            if (multiSelectList.get(0) == releaseNode) {
                                // remove the selected node from the list and reset the widgets anchor back to nothing
                                removeFromMultiSelectList(releaseNode);
                                releaseNode.setSelected(false);
                                setCurrentAnchor(null);
                                if (bDebugLogMoveWidget) LOG.info("## {}.mouseReleased() ## removing anchor node {}", instanceName, releaseNode.getID());
                            } else {
                                if (releaseNode.isSelectable()) {
                                    if (currentAnchor != null) {
                                        removeFromMultiSelectList(currentAnchor);
                                        currentAnchor.setSelected(false);
                                    }
                                    addToMultiSelectList(releaseNode);
                                    setCurrentAnchor(releaseNode);
                                    if (bDebugLogMoveWidget) LOG.info("## {}.mouseReleased() ## replacing anchor node with {}", instanceName, releaseNode.getID());
                                } else {
                                    if (bDebugLogMoveWidget) LOG.info("## {}.mouseReleased() ## Ignoring un-selectable node", instanceName);
                                }
                            }
                        } else {
                            // check if the selected node is already in the multiselect list
                            if (multiSelectList.contains(releaseNode) && releaseNode.isSelectable()) {
                                // move the anchor node of the widget to the newly selected node
                                setCurrentAnchor(releaseNode);
                                if (bDebugLogMoveWidget) LOG.info("## {}.mouseReleased() ## swapping anchor node with {}", instanceName, releaseNode.getID());
                            } else {
                                if (bDebugLogMoveWidget) LOG.info("## {}.mouseReleased() ## Selected node not in list or not selectable", instanceName);
                            }
                        }
                    }
                }
            }
        }
        if (bDebugLogMoveWidget) LOG.info("## {}.mouseReleased() ## updating widget position to {}, {}", instanceName, widgetPosWorld.getX(), widgetPosWorld.getY());
        getMapPanel().repaint();
    }

    @Override
    public void mouseDragged(MouseEvent e) {
        if (isDraggingWidget) {
            // calculate the move difference from the last stored position
            Point2D diff = calcMoveOffset(e.getPoint());
            moveDiffX += diff.getX();
            moveDiffY += diff.getY();
            moveNodesBy(diff.getX(), diff.getY(), multiSelectList);
            widgetPosX += diff.getX();
            widgetPosY += diff.getY();
            moveWidgetWorldPosBy(diff.getX(), diff.getY());
        } else {
            widgetManager.updateAllWidgets();
        }
    }

    Point2D calcMoveOffset(Point position) {
        Point2D mousePosWorld = screenPosToWorldPos((int) position.getX(), (int) position.getY());
        double anchorStepX = (mousePosWorld.getX() + currentSelector.getSelectOffsetWorld().getX()) - currentAnchor.getX();
        double anchorStepY = (mousePosWorld.getY() + currentSelector.getSelectOffsetWorld().getY()) - currentAnchor.getZ();

        // Define world bounds
        double minBoundX = -1024 * mapScale;
        double maxBoundX = 1024 * mapScale;
        double minBoundY = -1024 * mapScale;
        double maxBoundY = 1024 * mapScale;

        // Clamp anchor movement to world bounds
        double clampedAnchorX = clampBounds(currentAnchor.getX() + anchorStepX, 0, currentAnchor.getX(), minBoundX, maxBoundX);
        double clampedAnchorY = clampBounds(currentAnchor.getZ() + anchorStepY, 0, currentAnchor.getZ(), minBoundY, maxBoundY);

        // Calculate step differences
        double stepDiffX = 0;
        double stepDiffY = 0;
        if (directionAxis == MoveSelector.SELECTED_AXIS.X || directionAxis == MoveSelector.SELECTED_AXIS.BOTH) {
            double stepX = limitDoubleToDecimalPlaces(clampedAnchorX - currentAnchor.getX(), 3, RoundingMode.HALF_UP);
            if (currentAnchor.getX() + stepX < minBoundX || currentAnchor.getX() + stepX > maxBoundX
                    || mousePosWorld.getX() < minBoundX || mousePosWorld.getX() > maxBoundX) {
                // Stop movement if the selected point goes out of bounds
                stepDiffX = 0;
            } else {
                stepDiffX = stepX;
            }
        }
        if (directionAxis == MoveSelector.SELECTED_AXIS.Y || directionAxis == MoveSelector.SELECTED_AXIS.BOTH) {
            double stepY = limitDoubleToDecimalPlaces(clampedAnchorY - currentAnchor.getZ(), 5, RoundingMode.HALF_UP);
            if (currentAnchor.getY() + stepY < minBoundX || currentAnchor.getY() - stepY > maxBoundX
                    || mousePosWorld.getY() < minBoundX || mousePosWorld.getY() > maxBoundX) {
                // Stop movement if the selected point goes out of bounds
                stepDiffY = 0;
            } else {
                stepDiffY = stepY;
            }
        }
        return calcGridSnapOffset(currentAnchor.getWorldPosition2D(), stepDiffX, stepDiffY);
    }


    @SuppressWarnings("SameParameterValue")
    private double clampBounds(double value, double offset, double widgetPos, double minBound, double maxBound) {
        double max = Math.max(minBound, Math.min(maxBound - offset, value));
        if (directionAxis == MoveSelector.SELECTED_AXIS.X) {
            return xAxisDirection == X_DIRECTION.RIGHT
                    ? Math.max(minBound - offset, Math.min(maxBound - offset, value)) : max;
        }
        if (directionAxis == MoveSelector.SELECTED_AXIS.Y) {
            return yAxisDirection == Y_DIRECTION.UP
                    ? max : Math.max(minBound - offset, Math.min(maxBound, value));
        }
        if (directionAxis == MoveSelector.SELECTED_AXIS.BOTH) {
            return Math.max(minBound, Math.min(maxBound, value));
        }
        return value;
    }

    Point2D calcGridSnapOffset(Point2D position, double diffX, double diffY) {
        Point2D.Double scaledDiff = new Point2D.Double();
        if (bGridSnapEnabled) {
            //Point2D p = screenPosToWorldPos(position.x, position.y);
            Point2D offset = currentSelector.getSelectOffsetWorld();
            double gridX = bGridSnapSubs ? gridSpacingX / (gridSubDivisions + 1) : gridSpacingX;
            double gridY = bGridSnapSubs ? gridSpacingY / (gridSubDivisions + 1) : gridSpacingY;

            // Calculate the snapped position based on the grid
            double snappedX = Math.round((position.getX() + diffX) / gridX) * gridX;
            double snappedY = Math.round((position.getY() + diffY) / gridY) * gridY;

            // Calculate the difference between the snapped position and the current position
            if (directionAxis == MoveSelector.SELECTED_AXIS.X || directionAxis == MoveSelector.SELECTED_AXIS.BOTH) {
                scaledDiff.x = snappedX - position.getX();
            }
            if (directionAxis == MoveSelector.SELECTED_AXIS.Y || directionAxis == MoveSelector.SELECTED_AXIS.BOTH) {
                scaledDiff.y = snappedY - position.getY();
            }
        } else {
            // Snapping is disabled, just return the original difference
            scaledDiff.x = diffX;
            scaledDiff.y = diffY;
        }
        return scaledDiff;
    }

    @Override
    public void mouseMoved(MouseEvent e) {}

    @Override
    public void mouseWheelMoved(MouseEvent e) {
        currentSelector.updateSelector();

    }

    //@Override
    public void moveNodesBy(MapNode anchorNode, double moveDiffX, double moveDiffY, boolean bUpdateWidget) {}

    private void moveNodesBy(double diffX, double diffY, ArrayList<MapNode> list) {
        suspendAutoSaving();
        for (MapNode node : list) {
            if (!node.isControlNode() && !node.isRotationNode()) {
                node.x = roundUpDoubleToDecimalPlaces(node.x + diffX, 3);
                node.z = roundUpDoubleToDecimalPlaces(node.z + diffY, 3);
            }
            // check if a curve is currently being created
            if (curveManager.isCurvePreviewCreated()) {
                // check if the node to be moved is a curve control node
                if (curveManager.isControlNode(node)) {
                    // let the curve code move the control node
                    CurveManager.CurveInfo curve = curveManager.getCurveNode(node);
                    if (curve != null) curve.getCurve().moveControlNodeBy(node, diffX, diffY, false);
                }
                // updateWidget all curves, just in case multiple curves are using the same node
                curveManager.updateAllCurves();
            }
            //CurveInterface c = curveManager.getCurrentCurve();
            checkNodeOverlap(node);
        }
        getMapPanel().repaint();
        resumeAutoSaving();
    }

    @Override
    public void moveWidgetWorldPosBy(double x, double y) {
        super.moveWidgetWorldPosBy(x, y);
        currentSelector.updateSelector();
    }

    @Override
    public void moveWidgetScreenPosBy(int diffX, int diffY) {
        super.moveWidgetScreenPosBy(diffX, diffY);
        currentSelector.updateSelector();
    }

    //
    // Getters
    //

    public boolean isWidgetSelected() { return directionAxis != MoveSelector.SELECTED_AXIS.NONE; }

    public MapNode getCurrentAnchor() { return currentAnchor; }
    public Color getxAxisColor() { return xAxisColor; }
    public Color getyAxisColor() { return yAxisColor; }
    public Color getFreeMoveColor() { return freeMoveColor; }
    public Y_DIRECTION getAxisDirectionY() { return yAxisDirection; }
    public X_DIRECTION getAxisDirectionX() { return xAxisDirection; }
    public FREEMOVE_POSITION getFreeMovePosition() { return freeMovePosition; }

    //
    // multiSelect functions
    //

    @Override
    public void onMultiSelectChange(ArrayList<MapNode> nodeList) {
        if (bDebugLogMoveWidget) LOG.info("## MoveWidget ## MultiSelect changed:");
        if (currentAnchor == null && !multiSelectList.isEmpty()) setCurrentAnchor(multiSelectList.get(0));
        if (nodeList.contains(currentAnchor)) {
            if (multiSelectList.isEmpty()) {
                if (bDebugLogMoveWidget) LOG.info("## MoveWidget ## MultiSelect empty, hiding widget");
                setCurrentAnchor(null);
            } else {
                if (bDebugLogMoveWidget) LOG.info("## MoveWidget ## AnchorNode not in Multiselect list, setting anchor to first node in multiSelectList");
                setCurrentAnchor(multiSelectList.get(0));
            }
        }
    }

    @Override
    public void onMultiSelectAdd(ArrayList<MapNode> addedNodes) {
        if (bDebugLogMoveWidget) LOG.info("## MoveWidget.onMultiSelectAdd() ## MultiSelect added: {}", addedNodes);
    }

    @Override
    public void onMultiSelectRemove(ArrayList<MapNode> removedNodes) {
        if (bDebugLogMoveWidget) LOG.info("## MoveWidget.onMultiSelectRemove() ## MultiSelect removed: {}", removedNodes);
    }

    @Override
    public void onMultiSelectCleared() {
        if (bDebugLogMoveWidget) LOG.info("## MoveWidget.onMultiSelectCleared() ## Clearing anchor node");
        setCurrentAnchor(null);
    }

    //
    //  Move Nodes Undo
    //

    public class MoveWidgetChanger implements ChangeManager.Changeable {
        private final ArrayList<MapNode> moveNodes;
        private final double diffX;
        private final double diffY;
        private final boolean isStale;
        private final Point2D widgetPos;

        public MoveWidgetChanger(ArrayList<MapNode> mapNodesMoved, double movedX, double movedY){
            super();
            this.moveNodes = new ArrayList<>();
            this.diffX = limitDoubleToDecimalPlaces(movedX, 3, RoundingMode.HALF_UP);
            this.diffY = limitDoubleToDecimalPlaces(movedY, 3, RoundingMode.HALF_UP);
            for (MapNode node: mapNodesMoved) {
                if (node != null) this.moveNodes.add(node);
            }
            this.isStale = isStale();
            this.widgetPos = new Point2D.Double(getWidgetPosWorld().getX() - this.diffX, getWidgetPosWorld().getY() - this.diffY);
            if (bDebugLogUndoRedoInfo) {
                LOG.info("## MoveNodeChanger ## widget pre move = {}", widgetPos);
                LOG.info("## MoveNodeChanger ## widget moved {}, y = {}", this.diffX , this.diffY);
                LOG.info("## MoveNodeChanger ## widget post move = {}, {}", this.widgetPos.getX() + this.diffX, this.widgetPos.getY() + this.diffY);
            }
        }

        public void undo(){
            suspendAutoSaving();
            moveNodesBy(-this.diffX, -this.diffY, moveNodes);
            setWidgetWorldPosition(widgetPos.getX(), widgetPos.getY());
            for (MapNode node : this.moveNodes) {
                checkAreaForNodeOverlap(node);
            }
            widgetManager.updateAllWidgets();
            getMapPanel().repaint();
            setStale(this.isStale);
            resumeAutoSaving();
        }

        public void redo(){
            suspendAutoSaving();
            moveNodesBy(this.diffX, this.diffY, moveNodes);
            setWidgetWorldPosition(this.widgetPos.getX()+this.diffX, this.widgetPos.getY()+this.diffY);
            for (MapNode node : this.moveNodes) {
                checkAreaForNodeOverlap(node);
            }
            widgetManager.updateAllWidgets();
            getMapPanel().repaint();
            setStale(true);
            resumeAutoSaving();
        }
    }
}
