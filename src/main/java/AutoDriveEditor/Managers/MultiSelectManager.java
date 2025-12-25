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

import static AutoDriveEditor.AutoDriveEditor.*;
import static AutoDriveEditor.Classes.Util_Classes.LoggerUtils.LOG;
import static AutoDriveEditor.Classes.Util_Classes.MathUtils.getNormalizedRectangle;
import static AutoDriveEditor.Classes.Util_Classes.MathUtils.roundUpDoubleToDecimalPlaces;
import static AutoDriveEditor.GUI.MapImage.pdaImage;
import static AutoDriveEditor.GUI.MapPanel.*;
import static AutoDriveEditor.GUI.Menus.DebugMenu.Logging.LogMultiSelectManagerInfoMenu.bDebugLogMultiSelectManagerInfo;
import static AutoDriveEditor.GUI.Menus.EditorMenu.updateEditMenu;

@SuppressWarnings("unused")
public class MultiSelectManager implements MouseListener, MouseMotionListener {

    public static final int WORLD_COORDINATES = 1;
    public static final int SCREEN_COORDINATES = 2;
    // Start point of the selection
    private static Point2D selectStart;
    // List of all the selected nodes
    public static final ArrayList<MapNode> multiSelectList = new ArrayList<>();
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

    // List of all currently available listeners
    public static final ArrayList<MultiSelectEventListener> listeners = new ArrayList<>();

    /**
     * * The MultiSelectEventListener interface is used to notify listeners of multi-select events.
     */
    @SuppressWarnings("unused")
    public interface MultiSelectEventListener {
        void onMultiSelectStart();
        void onMultiSelectStop();
        void onMultiSelectChange(ArrayList<MapNode> nodeList);
        void onMultiSelectAdd(ArrayList<MapNode> addedNodes);
        void onMultiSelectRemove(ArrayList<MapNode> removedNodes);
        void onMultiSelectOneTime(ArrayList<MapNode> nodeList);
        void onMultiSelectCleared();
    }

    @Override
    public void mouseClicked(MouseEvent e) {
        if (e.getButton() == MouseEvent.BUTTON3) {
            if (buttonManager.getCurrentButton() != null) {
                if (buttonManager.getCurrentButton().useMultiSelection()) {
                    if (buttonManager.getCurrentButton().ignoreDeselect()) {
                        if (bDebugLogMultiSelectManagerInfo) LOG.info("## MultiSelectManager.mouseClicked() ## Ignoring clearMultiSelection()");
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
        if (buttonManager.getCurrentButton() != null) {
            if (buttonManager.getCurrentButton().useMultiSelection()) {
                if (e.getButton() == MouseEvent.BUTTON3) {
                    startMultiSelect(e.getX(), e.getY());
                }
            }
        }
    }

    @Override
    public void mouseReleased(MouseEvent e) {
        ButtonManager.ButtonInterface currentButton = buttonManager.getCurrentButton();
        if (currentButton != null && currentButton.useMultiSelection() && e.getButton() == MouseEvent.BUTTON3 && isMultiSelectDragging) {
            // reset the preview flags on all selected nodes
            for (MapNode node : selectedNodes) {
                node.setPreviewNodeSelectionChange(false);
                node.setPreviewNodeHiddenChange(false);
            }
            stopMultiSelect();

            //get the selected nodes based on the selection method
            if (!selectedNodes.isEmpty()) {
                if (currentButton.addSelectedToMultiSelectList()) {
                    // populate the added and removed nodes lists
                    ArrayList<MapNode> addedNodes = new ArrayList<>();
                    ArrayList<MapNode> removedNodes = new ArrayList<>();
                    int addCount = 0, removeCount = 0;
                    for (MapNode mapNode : selectedNodes) {
                        if (multiSelectList.contains(mapNode)) {
                            // already in multiselect list, remove it
                            removeFromMultiSelectList(mapNode);
                            mapNode.setSelected(false);
                            removedNodes.add(mapNode);
                            removeCount++;
                        } else {
                            // not in multiselect list, add it
                            addToMultiSelectList(mapNode);
                            if (currentButton.previewNodeSelectionChange()) {
                                mapNode.setSelected(true);
                            }
                            addedNodes.add(mapNode);
                            addCount++;
                        }
                    }
                    // log the number of nodes added/removed
                    String changeText = (addCount > 0 ? addCount + " Added" : "") +
                            (addCount > 0 && removeCount > 0 ? ", " : "") +
                            (removeCount > 0 ? removeCount + " Removed" : "");
                    LOG.info("MultiSelect: Node Selection change ( {}, Total {} selected )",changeText, multiSelectList.size());
                    // updateWidget isMultipleSelected and "Edit" menu selectability
                    isMultipleSelected = !multiSelectList.isEmpty();
                    updateEditMenu();
                    // notify listeners of any changes
                    if (addCount > 0) listeners.forEach(listener -> listener.onMultiSelectAdd(addedNodes));
                    if (removeCount > 0) listeners.forEach(listener -> listener.onMultiSelectRemove(removedNodes));
                    listeners.forEach(listener -> listener.onMultiSelectChange(selectedNodes));
                } else {
                    LOG.info("MultiSelect: One time selection of {} nodes", selectedNodes.size());
                    listeners.forEach(listener -> listener.onMultiSelectOneTime(selectedNodes));
                }
            }
        }
    }

    @Override
    public void mouseDragged(MouseEvent e) {
        // is a button selected, ignore if not
        if (buttonManager.getCurrentButton() != null) {
            // does the button have multi select active
            if (buttonManager.getCurrentButton().useMultiSelection()) {
                // check if a selection started
                if (selectStart != null && isMultiSelectDragging) {
                    // clear the inSelectionArea flag on the previously selected nodes, if not, the visual preview won't updateVisibility properly
                    for (MapNode node : selectedNodes) {
                        node.setPreviewNodeSelectionChange(false);
                        node.setPreviewNodeHiddenChange(false);
                        node.setPreviewNodeFlagChange(false);
                    }

                    if (curveManager.isCurvePreviewCreated()) {
                        ArrayList<MapNode> cpList = curveManager.getAllActiveControlNodes();
                        for(MapNode node : cpList) {
                            node.setPreviewNodeSelectionChange(false);
                        }
                    }

                    selectedNodes.clear();
                    // which selection method are we using
                    if (useRectangularSelection) {
                        // as we use a Rectangle2D to hold the selection dimensions, we have to normalize the rectangle
                        multiSelectRect = getNormalizedRectangle(selectStart, screenPosToWorldPos(e.getX(), e.getY()));
                        // search through the entire node network and check if they need adding to the selectedNodes list
                        for (MapNode mapNode : RoadMap.networkNodesList) {
                            //if (multiSelectRect.contains(mapNode.x, mapNode.y)) {
                            if (mapNode.x > multiSelectRect.getX() && mapNode.x < multiSelectRect.getX() + multiSelectRect.getWidth() && mapNode.z > multiSelectRect.getY() && mapNode.z < multiSelectRect.getY() + multiSelectRect.getHeight()) {
                                // TODO Fix node visibility check
                                if (buttonManager.getCurrentButton().alwaysSelectHidden() || mapNode.isSelectable()) {
                                    if (!selectedNodes.contains(mapNode)) selectedNodes.add(mapNode);
                                    // check if we need to set the node to display as selected
                                    if (buttonManager.getCurrentButton().previewNodeSelectionChange()) mapNode.setPreviewNodeSelectionChange(true);
                                    if (buttonManager.getCurrentButton().previewNodeHiddenChange()) mapNode.setPreviewNodeHiddenChange(true);
                                    if (buttonManager.getCurrentButton().previewNodeFlagChange()) mapNode.setPreviewNodeFlagChange(true);

                                }
                            }
                        }

                        if (curveManager.isCurvePreviewCreated()) {
                            ArrayList<MapNode> cpList = curveManager.getAllActiveControlNodes();
                            for(MapNode node : cpList) {
                                if (!node.isRotationNode() && multiSelectRect.contains(node.getWorldPosition2D())) {
//                                if (multiSelectRect.contains(node.getWorldPosition2D())) {
                                    if (!selectedNodes.contains(node)) {
                                        selectedNodes.add(node);
                                        node.setPreviewNodeSelectionChange(true);
                                    }
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
                                if (buttonManager.getCurrentButton().alwaysSelectHidden() || mapNode.isSelectable()) {
                                    selectedNodes.add(mapNode);
                                    // check if we need to set the node to display as selected
                                    if (buttonManager.getCurrentButton().previewNodeSelectionChange()) mapNode.setPreviewNodeSelectionChange(true);
                                    if (buttonManager.getCurrentButton().previewNodeHiddenChange()) mapNode.setPreviewNodeHiddenChange(true);
                                    if (buttonManager.getCurrentButton().previewNodeFlagChange()) mapNode.setPreviewNodeFlagChange(true);
                                }
                            }
                        }
                        if (curveManager.isCurvePreviewCreated()) {
                            ArrayList<MapNode> list = curveManager.getAllActiveControlNodes();
                            for (MapNode node : list) {
                                Point2D nodePosScreen = worldPosToScreenPos(node.x, node.z);
                                if (!node.isRotationNode() && freeformSelectionPath.contains(nodePosScreen)) {
                                    selectedNodes.add(node);
                                    // check if we need to set the node to display as selected
                                    if (buttonManager.getCurrentButton().previewNodeSelectionChange()) node.setPreviewNodeSelectionChange(true);
                                    if (buttonManager.getCurrentButton().previewNodeHiddenChange()) node.setPreviewNodeHiddenChange(true);
                                    if (buttonManager.getCurrentButton().previewNodeFlagChange()) node.setPreviewNodeFlagChange(true);
                                }
                            }
                        }


                    }
                    // updateVisibility the screen to show the changes
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


    public static void addToMultiSelectList(MapNode node) {
        if (node != null) {
            if (!multiSelectList.contains(node)) {
                multiSelectList.add(node);
                node.setSelected(true);
                isMultipleSelected = true;
            }
        }
    }

    public static void removeFromMultiSelectList(MapNode node) {
        if (node != null) {
            if (multiSelectList.contains(node)) {
                multiSelectList.remove(node);
                node.setSelected(false);
                if (multiSelectList.isEmpty()) clearMultiSelection();
            }
        }
    }

    public static void startMultiSelect(int mousePosX, int mousePosY) {
        if ( pdaImage != null ) {
            if (bDebugLogMultiSelectManagerInfo) LOG.info("## MultiSelectManager.startMultiSelect() ## Starting MultiSelect");
            // clear the list of previously selected nodes
            selectedNodes.clear();
            // set the selection start at world co-ordinates of mouse position
            selectStart = screenPosToWorldPos(mousePosX, mousePosY);
            multiSelectRect.setRect(0,0,0,0);
            if (bDebugLogMultiSelectManagerInfo) LOG.info("## MultiSelectManager.startMultiSelect() ## Multi select started at world position x = {}, z = {}", selectStart.getX(), selectStart.getY());
            // reset/clear the previous freeform selection
            if (useFreeformSelection) {
                freeformSelectionPath.reset();
                freeformSelectionStart = new Point(mousePosX, mousePosY);
                freeformSelectionPath.moveTo(mousePosX, mousePosY);
            }
            // set selection in progress
            setIsMultiSelectDragging(true);
            for (MultiSelectEventListener listener : listeners) {
                listener.onMultiSelectStart();
            }
        }
    }

    public static void stopMultiSelect() {
        // check if the editor has a valid pdaImage and a selection has started
        if (pdaImage != null && selectStart != null ) {
            if (bDebugLogMultiSelectManagerInfo) LOG.info("## MultiSelectManager.stopMultiSelect() ## Multi select stopped at world position x = {}, z = {}", multiSelectRect.getX() + multiSelectRect.getWidth(), multiSelectRect.getY() + multiSelectRect.getHeight());
            // check what selection method we are using
            if (useRectangularSelection) {
                if (multiSelectRect.getWidth() > 0 && multiSelectRect.getHeight() > 0) {
                    if (bDebugLogMultiSelectManagerInfo) LOG.info("## MultiSelectManager.stopMultiSelect() ## Selection size {},{}", roundUpDoubleToDecimalPlaces(multiSelectRect.getWidth(), 3), roundUpDoubleToDecimalPlaces(multiSelectRect.getHeight(), 3));
                }
            } else {
                freeformSelectionPath.closePath();
            }
        }
        setIsMultiSelectDragging(false);
        for (MultiSelectEventListener listener : listeners) {
            listener.onMultiSelectStop();
        }
        getMapPanel().repaint();
    }

    public static void clearMultiSelection() {
        if (!multiSelectList.isEmpty()) {
            for (MapNode node : multiSelectList) {
                node.setSelected(false);
            }
            multiSelectList.clear();
            LOG.info("Cleared all Selected Nodes");
            for (MultiSelectEventListener listener : listeners) {
                listener.onMultiSelectCleared();
            }
        }
        isMultipleSelected = false;
        updateEditMenu();
        getMapPanel().repaint();
    }

    @SuppressWarnings("unused")
    public boolean isMultiSelectDragging() { return isMultiSelectDragging; }

    private ArrayList<MapNode> getAllNodesInArea(Path2D path) {
        ArrayList<MapNode> selectedList = new ArrayList<>();
        if (roadMap != null) {
            for (MapNode mapNode : RoadMap.networkNodesList) {
                Point2D point = worldPosToScreenPos(mapNode.x, mapNode.z);
                if (path.contains(point)) {
                    if (mapNode.isSelectable()) {
                        selectedList.add(mapNode);
                    }
                }
            }

            if (curveManager.isCurvePreviewCreated()) {
                ArrayList<MapNode> list = curveManager.getAllActiveControlNodes();
                for (MapNode node : list) {
                    if (path.contains(node.getScreenPosition2D())) {
                        selectedList.add(node);
                    }
                }
            }
        }
        return selectedList;
    }

    private ArrayList<MapNode> getAllNodesInRectangle(Rectangle2D rect) {
        ArrayList<MapNode> selectList = new ArrayList<>();
        if (roadMap != null) {
            for (MapNode mapNode : RoadMap.networkNodesList) {
                if (mapNode.x > rect.getX() && mapNode.x < rect.getX() + rect.getWidth() && mapNode.z > rect.getY() && mapNode.z < rect.getY() + rect.getHeight()) {
                    if (mapNode.isSelectable()) {
                        selectList.add(mapNode);
                    }
                }
            }
        }
        if (curveManager.isCurvePreviewCreated()) {
            ArrayList<MapNode> nodeList = curveManager.getAllActiveControlNodes();
            for (MapNode mapNode : nodeList) {
                if (mapNode.x > rect.getX() && mapNode.x < rect.getX() + rect.getWidth() && mapNode.z > rect.getY() && mapNode.z < rect.getY() + rect.getHeight()) {
                    if (mapNode.isSelectable()) {
                        selectList.add(mapNode);
                    }
                }
            }
        }
        return selectList;
    }

    public static SelectionAreaInfo getSelectionBounds(ArrayList<MapNode> nodeList) {
        double topLeftX = 0, topLeftY = 0;
        double bottomRightX = 0, bottomRightY = 0;
        for (int j = 0; j < nodeList.size(); j++) {
            MapNode node = nodeList.get(j);
            if (j == 0) {
                topLeftX = node.x;
                topLeftY = node.z;
                bottomRightX = node.x;
                bottomRightY = node.z;
            } else {
                if (node.x < topLeftX ) {
                    topLeftX = node.x;
                }
                if (node.z < topLeftY ) {
                    topLeftY = node.z;
                }
                if (node.x > bottomRightX ) {
                    bottomRightX = node.x;
                }
                if (node.z > bottomRightY ) {
                    bottomRightY = node.z;
                }
            }
        }
        double rectSizeX = bottomRightX - topLeftX;
        double rectSizeY = bottomRightY - topLeftY;
        double centreX = bottomRightX - ( rectSizeX / 2 );
        double centreY = bottomRightY - ( rectSizeY / 2 );

        if (bDebugLogMultiSelectManagerInfo) LOG.info("## WORLD_COORDINATES ## Rectangle start = {} , {} : end = {} , {} : size = {} , {} : Centre = {} , {}", topLeftX, topLeftY, bottomRightX, bottomRightY, rectSizeX, rectSizeY, centreX, centreY);
        return new SelectionAreaInfo( new Point2D.Double(topLeftX, topLeftY) ,
                new Point2D.Double(bottomRightX, bottomRightY),
                new Point2D.Double(rectSizeX, rectSizeY),
                new Point2D.Double(centreX, centreY));
    }

    public static void addMultiSelectEventListener(MultiSelectEventListener listener) {
        if (!listeners.contains(listener)) {
            listeners.add(listener);
            if (bDebugLogMultiSelectManagerInfo) LOG.info("## MultiSelectManager.addMultiSelectEventListener() ## listener added: {}", listener.getClass().getSimpleName());
        }
    }

    public static void removeMultiSelectEventListener(MultiSelectEventListener listener) {
        listeners.remove(listener);
    }

    //
    //  Getters
    //

    public static Path2D getFreeformSelectionPath() { return freeformSelectionPath; }
    public static ArrayList<MapNode> getSelectedNodes() { return selectedNodes; }
    public static boolean getIsMultiSelectDragging() { return isMultiSelectDragging; }
    public static int getAdjustedCount() { return selectedNodes.size(); }

    //
    // Setters
    //

    public static void setUseRectangularSelection( boolean bool) {
        useRectangularSelection = bool;
        useFreeformSelection = !bool;
    }
    public static void setUseFreeformSelection( boolean bool) {
        useFreeformSelection = bool;
        useRectangularSelection = !bool;
    }
    private static void setIsMultiSelectDragging(boolean result) { isMultiSelectDragging = result; }

    public static class SelectionAreaInfo {
        private final Point2D startCoordinates;
        private final Point2D EndCoordinates;
        private final Point2D selectionSize;
        public final Point2D selectionCentre;

        public SelectionAreaInfo(Point2D start, Point2D end, Point2D size, Point2D centre){
            this.startCoordinates = start;
            this.EndCoordinates = end;
            this.selectionSize = size;
            this.selectionCentre = centre;
        }
        // getter setters

        public Point2D getSelectionStart(int coordType) {
            if (coordType == WORLD_COORDINATES) {
                return this.startCoordinates;
            } else {
                return worldPosToScreenPos(this.startCoordinates.getX(), this.startCoordinates.getY());
            }
        }

        public Point2D getSelectionEnd(int coordType) {
            if (coordType == WORLD_COORDINATES) {
                return this.EndCoordinates;
            } else {
                return worldPosToScreenPos(this.EndCoordinates.getX(), this.EndCoordinates.getY());
            }
        }

        @SuppressWarnings("unused")
        public Point2D getSelectionSize(int coordType) {
            if (coordType == WORLD_COORDINATES) {
                return this.selectionSize;
            } else {
                Point2D topLeft = getSelectionStart(SCREEN_COORDINATES);
                Point2D bottomRight = getSelectionEnd(SCREEN_COORDINATES);
                return new Point((int) (bottomRight.getX() - topLeft.getX()), (int) (bottomRight.getY() - topLeft.getY()));
            }
        }

        public Point2D getSelectionCentre(int coordType) {
            if (coordType == WORLD_COORDINATES) {
                return this.selectionCentre;
            } else {
                return worldPosToScreenPos(this.selectionCentre.getX(), this.selectionCentre.getY());
            }
        }
    }
}
