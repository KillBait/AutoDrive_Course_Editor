package AutoDriveEditor.Managers;

import AutoDriveEditor.RoadNetwork.MapNode;
import AutoDriveEditor.RoadNetwork.RoadMap;

import java.awt.*;
import java.awt.event.MouseEvent;
import java.awt.event.MouseListener;
import java.awt.event.MouseMotionListener;
import java.awt.geom.Path2D;
import java.awt.geom.Point2D;
import java.awt.geom.Rectangle2D;
import java.util.ArrayList;
import java.util.LinkedList;

import static AutoDriveEditor.AutoDriveEditor.getMapPanel;
import static AutoDriveEditor.Classes.MapImage.pdaImage;
import static AutoDriveEditor.GUI.Buttons.Curves.CubicCurveButton.cubicCurve;
import static AutoDriveEditor.GUI.Buttons.Curves.CubicCurveButton.isCubicCurveCreated;
import static AutoDriveEditor.GUI.Buttons.Curves.QuadCurveButton.isQuadCurveCreated;
import static AutoDriveEditor.GUI.Buttons.Curves.QuadCurveButton.quadCurve;
//quarticbezier
import static AutoDriveEditor.GUI.Buttons.Curves.QuarticCurveButton.isQuarticCurveCreated;
import static AutoDriveEditor.GUI.Buttons.Curves.QuarticCurveButton.quarticCurve;
//
import static AutoDriveEditor.GUI.MapPanel.*;
import static AutoDriveEditor.GUI.Menus.DebugMenu.Logging.LogMultiSelectInfoMenu.bDebugLogMultiSelectInfo;
import static AutoDriveEditor.GUI.Menus.EditorMenu.updateEditMenu;
import static AutoDriveEditor.Managers.ButtonManager.getCurrentButton;
import static AutoDriveEditor.Utils.LoggerUtils.LOG;
import static AutoDriveEditor.Utils.MathUtils.getNormalizedRectangle;
import static AutoDriveEditor.Utils.MathUtils.roundUpDoubleToDecimalPlaces;

public class MultiSelectManager implements MouseListener, MouseMotionListener {

    // Start point of the selection
    private static Point2D selectStart;
    // List of all the selected nodes
    public static final LinkedList<MapNode> multiSelectList = new LinkedList<>();
    // List of nodes that were selected on the current run
    public static final ArrayList<MapNode> selectedNodes = new ArrayList<>();
    public static boolean isMultipleSelected = false;
    public static boolean isMultiSelectDragging;


    //
    // Rectangular selection
    //

    // Stores the dimensions of the selection box
    public static Rectangle2D multiSelectRect = new Rectangle2D.Double();
    public static boolean useRectangularSelection = true;

    //
    // Freeform selection
    //

    // Stores the path the freeform selection takes
    private static final Path2D freeformSelectionPath  = new Path2D.Double();
    // Where the freeform selection starts
    public static Point2D freeformSelectionStart;
    public static boolean useFreeformSelection = false;

    @Override
    public void mouseClicked(MouseEvent e) {
        if (e.getButton() == MouseEvent.BUTTON3) {
            if (getCurrentButton() != null) {
                if (getCurrentButton().useMultiSelection()) {
                    if (getCurrentButton().ignoreDeselect()) {
                        if (bDebugLogMultiSelectInfo) LOG.info("Ignoring clearMultiSelection()");
                    } else {
                        clearMultiSelection();
                        getMapPanel().repaint();
                    }
                }
            }
        }
    }

    @Override
    public void mousePressed(MouseEvent e) {
        if (getCurrentButton() != null) {
            if (getCurrentButton().useMultiSelection()) {
                if (e.getButton() == MouseEvent.BUTTON3) {
                    startMultiSelect(e.getX(), e.getY());
                }
            }
        }
    }

    @Override
    public void mouseReleased(MouseEvent e) {
        if (getCurrentButton() != null) {
            if (getCurrentButton().useMultiSelection()) {
                if (e.getButton() == MouseEvent.BUTTON3) {
                    for (MapNode node : selectedNodes) {
                        node.setPreviewNodeSelectionChange(false);
                        node.setPreviewNodeHiddenChange(false);
                    }
                    stopMultiSelect();
                    if (getCurrentButton().addSelectedToMultiSelectList()) {
                        if (useRectangularSelection) {
                            getAllNodesInSelectedRectangle(multiSelectRect, getCurrentButton().previewNodeSelectionChange());
                        } else {
                            getAllNodesInSelectedArea(freeformSelectionPath, getCurrentButton().previewNodeSelectionChange());
                        }
                    }
                }
            }
        }
    }

    @Override
    public void mouseDragged(MouseEvent e) {
        // is a button selected, ignore if not
        if (getCurrentButton() != null) {
            // does the button have multi select active
            if (getCurrentButton().useMultiSelection()) {
                /// check if a selection started
                if (selectStart != null && isMultiSelectDragging) {
                    // clear the inSelectionArea flag on the previously selected nodes, if not, the visual preview won't update properly
                    for (MapNode node : selectedNodes) {
                        node.setPreviewNodeSelectionChange(false);
                        node.setPreviewNodeHiddenChange(false);
                        node.setPreviewNodeFlagChange(false);
                    }
                    selectedNodes.clear();
                    // which selection method are we using
                    if (useRectangularSelection) {
                        // as we use a Rectangle2D to hold the selection dimensions, we have to normalize the rectangle
                        multiSelectRect = getNormalizedRectangle(selectStart, screenPosToWorldPos(e.getX(), e.getY()));
                        // search through the entire node network and check if they need adding to the selectedNodes list
                        for (MapNode mapNode : RoadMap.networkNodesList) {
                            if (mapNode.x > multiSelectRect.getX() && mapNode.x < multiSelectRect.getX() + multiSelectRect.getWidth() && mapNode.z > multiSelectRect.getY() && mapNode.z < multiSelectRect.getY() + multiSelectRect.getHeight()) {
                                // TODO Fix node visibility check
                                if (getCurrentButton().alwaysSelectHidden() || mapNode.isSelectable()) {
                                    if (!selectedNodes.contains(mapNode)) selectedNodes.add(mapNode);
                                    // check if we need to set the node to display as selected
                                    if (getCurrentButton().previewNodeSelectionChange()) mapNode.setPreviewNodeSelectionChange(true);
                                    if (getCurrentButton().previewNodeHiddenChange()) mapNode.setPreviewNodeHiddenChange(true);
                                    if (getCurrentButton().previewNodeFlagChange()) mapNode.setPreviewNodeFlagChange(true);
                                }
                            }
                        }
                    } else {
                        //update the last point of the path
                        freeformSelectionPath.lineTo(e.getX(), e.getY());
                        // search the entire node network and add only the mapNodes inside the selection area to the selectedNodes list
                        for (MapNode mapNode : RoadMap.networkNodesList) {
                            Point2D nodePosScreen = worldPosToScreenPos(mapNode.x, mapNode.z);
                            if (freeformSelectionPath.contains(nodePosScreen)) {
                                if (getCurrentButton().alwaysSelectHidden() || mapNode.isSelectable()) {
                                    selectedNodes.add(mapNode);
                                    // check if we need to set the node to display as selected
                                    if (getCurrentButton().previewNodeSelectionChange()) mapNode.setPreviewNodeSelectionChange(true);
                                    if (getCurrentButton().previewNodeHiddenChange()) mapNode.setPreviewNodeHiddenChange(true);
                                    if (getCurrentButton().previewNodeFlagChange()) mapNode.setPreviewNodeFlagChange(true);
                                }
                            }
                        }
                    }
                    // update the screen to show the changes
                    getMapPanel().repaint();
                }
            }
        }
    }

    @Override
    public void mouseMoved(MouseEvent e) {}
    @Override
    public void mouseEntered(MouseEvent e) {}
    @Override
    public void mouseExited(MouseEvent e) {}



    public static void startMultiSelect(int mousePosX, int mousePosY) {
        if ( pdaImage != null ) {
            // clear the list of previously selected nodes
            selectedNodes.clear();
            // set the selection start at world co-ordinates of mouse position
            selectStart = screenPosToWorldPos(mousePosX, mousePosY);
            if (bDebugLogMultiSelectInfo) LOG.info("Multi select started at world position x = {}, z = {}", selectStart.getX(), selectStart.getY());
            // reset/clear the previous freeform selection
            if (useFreeformSelection) {
                freeformSelectionPath.reset();
                freeformSelectionStart = new Point(mousePosX, mousePosY);
                freeformSelectionPath.moveTo(mousePosX, mousePosY);
            }
            // set selection in progress
            setIsMultiSelectDragging(true);
        }
    }

    public static void stopMultiSelect() {
        // check if the editor has a valid pdaImage and a selection has started
        if (pdaImage != null && selectStart != null ) {
            if (bDebugLogMultiSelectInfo) LOG.info("Multi select stopped at world position x = {}, z = {}", multiSelectRect.getX() + multiSelectRect.getWidth(), multiSelectRect.getY() + multiSelectRect.getHeight());
            // check what selection method we are using
            if (useRectangularSelection) {
                if (multiSelectRect.getWidth() > 0 && multiSelectRect.getHeight() > 0) {
                    if (bDebugLogMultiSelectInfo) LOG.info("Selection size {},{}", roundUpDoubleToDecimalPlaces(multiSelectRect.getWidth(), 3), roundUpDoubleToDecimalPlaces(multiSelectRect.getHeight(), 3));
                }
            } else {
                freeformSelectionPath.closePath();
            }
        }
        setIsMultiSelectDragging(false);
        getMapPanel().repaint();
    }

    public static void clearMultiSelection() {
        if (multiSelectList.size() > 0 ) {
            for (MapNode node : multiSelectList) {
                node.setSelected(false);
            }
            multiSelectList.clear();
            LOG.info("Cleared all Selected Nodes");
        }
        isMultipleSelected = false;
        updateEditMenu();
        getMapPanel().repaint();
    }

    @SuppressWarnings("unused")
    public boolean isMultiSelectDragging() { return isMultiSelectDragging; }


    @SuppressWarnings("UnusedReturnValue")
    public static int getAllNodesInSelectedArea(Path2D path, boolean setSelected) {

        int count = 0;
        if (roadMap != null) {
            for (MapNode mapNode : RoadMap.networkNodesList) {
                Point2D point = worldPosToScreenPos(mapNode.x, mapNode.z);
                if (path.contains(point)) {
                    if (mapNode.isSelectable()) {
                        if (multiSelectList.contains(mapNode)) {
                            multiSelectList.remove(mapNode);
                            mapNode.setSelected(false);
                            count--;
                        } else {
                            multiSelectList.add(mapNode);
                            if (setSelected) mapNode.setSelected(true);
                            count++;
                        }
                    }
                }
            }

            if (isQuadCurveCreated) {
                MapNode controlPoint = quadCurve.getControlPoint();
                Point cpScreenPos = worldPosToScreenPos(controlPoint.x, controlPoint.z);
                if (path.contains(cpScreenPos.getX(), cpScreenPos.getY())) {
                    if (multiSelectList.contains(controlPoint)) {
                        multiSelectList.remove(controlPoint);
                        controlPoint.setSelected(false);
                    } else {
                        multiSelectList.add(controlPoint);
                        controlPoint.setSelected(true);
                    }
                }
            }

            if (isCubicCurveCreated) {
                MapNode controlPoint1 = cubicCurve.getControlPoint1();
                Point cp1ScreenPos = worldPosToScreenPos(controlPoint1.x, controlPoint1.z);
                MapNode controlPoint2 = cubicCurve.getControlPoint2();
                Point cp2ScreenPos = worldPosToScreenPos(controlPoint2.x, controlPoint2.z);

                if (path.contains(cp1ScreenPos.getX(), cp1ScreenPos.getY())) {
                    if (multiSelectList.contains(controlPoint1)) {
                        multiSelectList.remove(controlPoint1);
                        controlPoint1.setSelected(false);
                    } else {
                        multiSelectList.add(controlPoint1);
                        controlPoint1.setSelected(true);
                    }
                }

                if (path.contains(cp2ScreenPos.getX(), cp2ScreenPos.getY())) {
                    if (multiSelectList.contains(controlPoint2)) {
                        multiSelectList.remove(controlPoint2);
                        controlPoint2.setSelected(false);
                    } else {
                        multiSelectList.add(controlPoint2);
                        controlPoint2.setSelected(true);
                    }
                }
            }
			//quarticbezier
			if (isQuarticCurveCreated) {
				MapNode controlPoint1 = quarticCurve.getControlPoint1();
				Point cp1ScreenPos = worldPosToScreenPos(controlPoint1.x, controlPoint1.z);
				MapNode controlPoint2 = quarticCurve.getControlPoint2();
				Point cp2ScreenPos = worldPosToScreenPos(controlPoint2.x, controlPoint2.z);
				MapNode controlPoint3 = quarticCurve.getControlPoint3();
				Point cp3ScreenPos = worldPosToScreenPos(controlPoint3.x, controlPoint3.z);

				if (path.contains(cp1ScreenPos.getX(), cp1ScreenPos.getY())) {
					if (multiSelectList.contains(controlPoint1)) {
						multiSelectList.remove(controlPoint1);
						controlPoint1.setSelected(false);
					} else {
						multiSelectList.add(controlPoint1);
						controlPoint1.setSelected(true);
					}
				}

				if (path.contains(cp2ScreenPos.getX(), cp2ScreenPos.getY())) {
					if (multiSelectList.contains(controlPoint2)) {
						multiSelectList.remove(controlPoint2);
						controlPoint2.setSelected(false);
					} else {
						multiSelectList.add(controlPoint2);
						controlPoint2.setSelected(true);
					}
				}

				if (path.contains(cp3ScreenPos.getX(), cp3ScreenPos.getY())) {
					if (multiSelectList.contains(controlPoint3)) {
						multiSelectList.remove(controlPoint3);
						controlPoint3.setSelected(false);
					} else {
						multiSelectList.add(controlPoint3);
						controlPoint3.setSelected(true);
					}
				}
			}

            if (count > 0) {
                LOG.info("Added {} nodes to selection",count);
            } else if (count < 0) {
                LOG.info("Removed {} nodes from selection", -count);
            }

            if (count != 0) {
                if (multiSelectList.size() > 0) {
                    LOG.info("Total {} selected nodes", multiSelectList.size());
                    isMultipleSelected = true;
                } else {
                    LOG.info("No nodes selected");
                    isMultipleSelected = false;
                }
            }
            updateEditMenu();
            return multiSelectList.size();
        }
        return 0;
    }


    @SuppressWarnings("UnusedReturnValue")
    public static int getAllNodesInSelectedRectangle(Rectangle2D rect, boolean setSelected) {

        int count = 0;

        if (roadMap != null) {
            for (MapNode mapNode : RoadMap.networkNodesList) {
                if (mapNode.x > rect.getX() && mapNode.x < rect.getX() + rect.getWidth() && mapNode.z > rect.getY() && mapNode.z < rect.getY() + rect.getHeight()) {
                    // TODO Fix node visibility check
                    if (mapNode.isSelectable()) {
                        if (multiSelectList.contains(mapNode)) {
                            multiSelectList.remove(mapNode);
                            mapNode.setSelected(false);
                            count--;
                        } else {
                            multiSelectList.add(mapNode);
                            if (setSelected) mapNode.setSelected(true);
                            count++;
                        }
                    }
                }
            }

            if (isQuadCurveCreated) {
                MapNode controlPoint = quadCurve.getControlPoint();
                if (controlPoint.x > rect.getX() && controlPoint.x < rect.getX() + rect.getWidth() && controlPoint.z > rect.getY() && controlPoint.z < rect.getY() + rect.getHeight()) {
                    if (multiSelectList.contains(controlPoint)) {
                        multiSelectList.remove(controlPoint);
                        controlPoint.setSelected(false);
                    } else {
                        multiSelectList.add(controlPoint);
                        controlPoint.setSelected(true);
                    }
                }
            }

            if (isCubicCurveCreated) {
                MapNode controlPoint1 = cubicCurve.getControlPoint1();
                MapNode controlPoint2 = cubicCurve.getControlPoint2();

                if (controlPoint1.x > rect.getX() && controlPoint1.x < rect.getX() + rect.getWidth() && controlPoint1.z > rect.getY() && controlPoint1.z < rect.getY() + rect.getHeight()) {
                    if (multiSelectList.contains(controlPoint1)) {
                        multiSelectList.remove(controlPoint1);
                        controlPoint1.setSelected(false);
                    } else {
                        multiSelectList.add(controlPoint1);
                        controlPoint1.setSelected(true);
                    }
                }

                if (controlPoint2.x > rect.getX() && controlPoint2.x < rect.getX() + rect.getWidth() && controlPoint2.z > rect.getY() && controlPoint2.z < rect.getY() + rect.getHeight()) {
                    if (multiSelectList.contains(controlPoint2)) {
                        multiSelectList.remove(controlPoint2);
                        controlPoint2.setSelected(false);
                    } else {
                        multiSelectList.add(controlPoint2);
                        controlPoint2.setSelected(true);
                    }
                }
            }

            if (count > 0) {
                LOG.info("Added {} nodes to selection",count);
            } else if (count < 0) {
                LOG.info("Removed {} nodes from selection", -count);
            }

            if (count != 0) {
                if (multiSelectList.size() > 0) {
                    LOG.info("Total {} selected nodes", multiSelectList.size());
                    isMultipleSelected = true;
                } else {
                    LOG.info("No nodes selected");
                    isMultipleSelected = false;
                }
            }
            updateEditMenu();
            return multiSelectList.size();
        }
        return count;
    }

    //
    //  Getters
    //

    public static Path2D getFreeformSelectionPath() { return freeformSelectionPath; }
    public static ArrayList<MapNode> getSelectedNodes() { return selectedNodes; }
    public static boolean getIsMultiSelectDragging() { return isMultiSelectDragging; }

    //
    // Setters
    //

    public static void setUseRectangularSelection( boolean bool) {
        useRectangularSelection = bool;
        useFreeformSelection = !bool;
        LOG.info("Rectangular Selection active");
    }
    public static void setUseFreeformSelection( boolean bool) {
        useFreeformSelection = bool;
        useRectangularSelection = !bool;
        LOG.info("Freeform Selection active");
    }
    private static void setIsMultiSelectDragging(boolean result) { isMultiSelectDragging = result; }
}
