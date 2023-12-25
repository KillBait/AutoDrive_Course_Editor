package AutoDriveEditor.Managers;

import AutoDriveEditor.GUI.Buttons.Edit.PasteSelectionButton;
import AutoDriveEditor.RoadNetwork.MapNode;
import AutoDriveEditor.RoadNetwork.RoadMap;

import java.awt.*;
import java.awt.geom.Point2D;
import java.util.LinkedList;

import static AutoDriveEditor.AutoDriveEditor.changeManager;
import static AutoDriveEditor.AutoDriveEditor.getMapPanel;
import static AutoDriveEditor.GUI.Buttons.Nodes.DeleteNodeButton.*;
import static AutoDriveEditor.GUI.MapPanel.*;
import static AutoDriveEditor.GUI.Menus.DebugMenu.Logging.LogCopyPasteMenu.bDebugLogCopyPasteInfo;
import static AutoDriveEditor.GUI.Menus.EditorMenu.updateEditMenu;
import static AutoDriveEditor.Managers.MultiSelectManager.*;
import static AutoDriveEditor.RoadNetwork.RoadMap.addNodeToNetwork;
import static AutoDriveEditor.RoadNetwork.RoadMap.createMapNode;
import static AutoDriveEditor.Utils.LoggerUtils.LOG;
import static AutoDriveEditor.Utils.MathUtils.roundUpDoubleToDecimalPlaces;
import static AutoDriveEditor.XMLConfig.AutoSave.resumeAutoSaving;
import static AutoDriveEditor.XMLConfig.AutoSave.suspendAutoSaving;


public class CopyPasteManager {

    public static final int WORLD_COORDINATES = 1;
    public static final int SCREEN_COORDINATES = 2;

    private LinkedList<MapNode> nodeCache;

    // NodeTransform is used to reference the old to new map nodes when re-creating
    // the connections to the new nodes, this way we can keep the valid connections
    // when they are added to the node network

    private static class NodeTransform {
        final MapNode originalNode;
        final MapNode newNode;
        final LinkedList<MapNode> incoming;
        final LinkedList<MapNode> outgoing;

        public NodeTransform(MapNode origNode, MapNode newNode) {
            this.originalNode = origNode;
            this.newNode = newNode;
            this.incoming = new LinkedList<>();
            this.outgoing = new LinkedList<>();
        }
    }

    public CopyPasteManager() {
        this.nodeCache = new LinkedList<>();
    }

    public void CutSelection(LinkedList<MapNode> nodesToCopy) {
        deleteNodeList.clear();
        for (MapNode node : nodesToCopy) {
            addToDeleteList(node);
        }
        changeManager.addChangeable( new DeleteNodeChanger(deleteNodeList));
        CopySelection(nodesToCopy);
        if (bDebugLogCopyPasteInfo) LOG.info("## CopyPasteManager Debug ## Cutting {} nodes",nodesToCopy.size());
        removeDeleteListNodes();
        clearMultiSelection();
        updateEditMenu();
    }

    public void CopySelection(LinkedList<MapNode> nodesToCopy) {
        LinkedList<MapNode> tempCache;
        //get the centre point of the setSelected nodes
        if (nodesToCopy.size() > 0) {
            if (bDebugLogCopyPasteInfo) LOG.info("## CopyPasteManager Debug ## Copying {} nodes",nodesToCopy.size());
            // rebuild the setSelected nodes and there connections to a new arrayList
            tempCache = createNewMapNodesFromList(nodesToCopy);
            // create a cached LinkedList, so we can paste this in as many times as needed
            nodeCache = createNewMapNodesFromList(tempCache);
        }
        clearMultiSelection();
        updateEditMenu();
    }

    public void PasteSelection(boolean inOriginalLocation) {
        if (nodeCache.size() > 0 ) {
            LinkedList<MapNode> tempCache = createNewMapNodesFromList(nodeCache);
            addNodesToNetwork(tempCache, inOriginalLocation);
        } else {
            LOG.info("Cannot Paste - Buffer empty");
        }
    }

    public boolean isCopyPasteBufferEmpty() {
        return nodeCache.size() == 0;
    }


    public LinkedList<MapNode> createNewMapNodesFromList(LinkedList<MapNode> list) {

        // create a new MapNode for each node in the list

        LinkedList<NodeTransform> tempWorkBuffer = new LinkedList<>();
        LinkedList<MapNode> tempCache = new LinkedList<>();

        int n = 1;
        for (MapNode node : list) {
            if (!node.isControlNode()) {
                MapNode workBufferNode = createMapNode(n++, node.x, node.y, node.z, node.flag, true, false);
                if (node.hasMapMarker()) {
                    workBufferNode.createMapMarker(node.getMarkerName(), node.getMarkerGroup());
                }
                tempWorkBuffer.add(new NodeTransform(node, workBufferNode));
            }
        }

        // iterate through the list and remake the connections using the new nodes

        for (MapNode originalListNode : list) {
            MapNode sourceNode = null;

            for (NodeTransform workBufferNode : tempWorkBuffer) {
                if (workBufferNode.originalNode == originalListNode) {
                    sourceNode = workBufferNode.newNode;
                    break;
                }
            }

            if (sourceNode != null) {
                MapNode destNode;
                for (int in = 0; in < originalListNode.incoming.size(); in++) {
                    MapNode originalIncomingNode = originalListNode.incoming.get(in);
                    for (NodeTransform workBufferNode : tempWorkBuffer) {
                        if (workBufferNode.originalNode == originalIncomingNode) {
                            destNode = workBufferNode.newNode;
                            sourceNode.incoming.add(destNode);
                            break;
                        }
                    }
                }

                for (int out = 0; out < originalListNode.outgoing.size(); out++) {
                    MapNode originalOutgoingNode = originalListNode.outgoing.get(out);
                    for (NodeTransform workNode : tempWorkBuffer) {
                        if (workNode.originalNode == originalOutgoingNode) {
                            destNode = workNode.newNode;
                            sourceNode.outgoing.add(destNode);
                        }
                    }
                }
            }
        }

        for (NodeTransform node : tempWorkBuffer) {
            tempCache.add(node.newNode);
        }
        return tempCache;
    }

    public void addNodesToNetwork(LinkedList<MapNode> newNodes, boolean inOriginalLocation) {
        Point2D selectionCentre;
        double diffX = 0;
        double diffY = 0;

        if (roadMap != null) {

            suspendAutoSaving();

            if (!inOriginalLocation) {
                selectionCentre = screenPosToWorldPos(getMapPanel().getWidth() / 2, getMapPanel().getHeight() / 2);
                selectionAreaInfo areaInfo = getSelectionBounds(newNodes);
                diffX = areaInfo.selectionCentre.getX() - selectionCentre.getX();
                diffY = areaInfo.selectionCentre.getY() - selectionCentre.getY();
                if (bDebugLogCopyPasteInfo) {
                    LOG.info("World coordinates of viewport Centre = {}", selectionCentre);
                    LOG.info("Move distance = {},{}", diffX, diffY);
                }

            }
            clearMultiSelection();
            int startID = RoadMap.networkNodesList.size() + 1;
            for (MapNode node : newNodes) {
                node.id = startID++;
                if (!inOriginalLocation) {
                    node.x = roundUpDoubleToDecimalPlaces(node.x - diffX, 3);
                    //node.x -= diffX;
                    node.z = roundUpDoubleToDecimalPlaces(node.z - diffY, 3);
                    //node.z -= diffY;
                    double yValue = getYValueFromHeightMap(node.x, node.z);
                    if (yValue != -1) node.y = yValue;
                }
                node.setSelected(true);
                addNodeToNetwork(node);
                multiSelectList.add(node);
            }

            isMultipleSelected = true;

            changeManager.addChangeable( new PasteSelectionButton.PasteSelectionChanger(newNodes) );
            setStale(true);
            getMapPanel().repaint();
            resumeAutoSaving();
        }
    }

    @SuppressWarnings("SameParameterValue")
    public static selectionAreaInfo getSelectionBounds(LinkedList<MapNode> selectedNodes/*, int coordType*/) {
        double topLeftX = 0, topLeftY = 0;
        double bottomRightX = 0, bottomRightY = 0;
        for (int j = 0; j < selectedNodes.size(); j++) {
            MapNode node = selectedNodes.get(j);
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

        if (bDebugLogCopyPasteInfo) LOG.info("## WORLD_COORDINATES ## Rectangle start = {} , {} : end = {} , {} : size = {} , {} : Centre = {} , {}", topLeftX, topLeftY, bottomRightX, bottomRightY, rectSizeX, rectSizeY, centreX, centreY);
        return new selectionAreaInfo( new Point2D.Double(topLeftX, topLeftY) ,
                new Point2D.Double(bottomRightX, bottomRightY),
                new Point2D.Double(rectSizeX, rectSizeY),
                new Point2D.Double(centreX, centreY));
    }

    public static class selectionAreaInfo {
        private final Point2D startCoordinates;
        private final Point2D EndCoordinates;
        private final Point2D selectionSize;
        public final Point2D selectionCentre;

        public selectionAreaInfo(Point2D start, Point2D end, Point2D size, Point2D centre){
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
