package AutoDriveEditor.Managers;

import AutoDriveEditor.GUI.Buttons.CopyPaste.PasteSelectionButton;
import AutoDriveEditor.GUI.Buttons.Nodes.DeleteNodeButton.DeleteNodeChanger;
import AutoDriveEditor.GUI.MenuBuilder;
import AutoDriveEditor.RoadNetwork.MapNode;
import AutoDriveEditor.RoadNetwork.RoadMap;

import java.awt.geom.AffineTransform;
import java.awt.geom.Point2D;
import java.util.LinkedList;

import static AutoDriveEditor.AutoDriveEditor.changeManager;
import static AutoDriveEditor.GUI.MenuBuilder.bDebugLogCopyPasteInfo;
import static AutoDriveEditor.Managers.MultiSelectManager.*;
import static AutoDriveEditor.MapPanel.MapImage.mapImage;
import static AutoDriveEditor.MapPanel.MapPanel.*;
import static AutoDriveEditor.Utils.LoggerUtils.LOG;


public class CopyPasteManager {

    private static final int WORLD_COORDINATES = 1;
    private static final int SCREEN_COORDINATES = 2;

    private LinkedList<MapNode> nodeCache;

    // NodeTransform is used to reference the old to new map nodes when re-creating
    // the connections to the new nodes, this way we can keep the valid connections
    // when they are added to the node network

    private static class NodeTransform {
        MapNode originalNode;
        MapNode newNode;
        LinkedList<MapNode> incoming;
        LinkedList<MapNode> outgoing;

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
        getMapPanel().removeDeleteListNodes();
        clearMultiSelection();
    }

    public void CopySelection(LinkedList<MapNode> nodesToCopy) {
        LinkedList<MapNode> tempCache;
        //get the centre point of the setSelected nodes
        if (nodesToCopy.size() > 0) {
            // rebuild the setSelected nodes and there connections to a new arrayList
            tempCache = createNewMapNodesFromList(nodesToCopy);
            // create a cached LinkedList, so we can paste this in as many times as needed
            nodeCache = createNewMapNodesFromList(tempCache);
            MenuBuilder.rotationMenuEnabled(true);
        }
        clearMultiSelection();
    }

    public void PasteSelection(boolean inOriginalLocation) {
        if (nodeCache.size() > 0 ) {
            LinkedList<MapNode> tempCache = createNewMapNodesFromList(nodeCache);
            addNodesToNetwork(tempCache, inOriginalLocation);
        } else {
            LOG.info("Cannot Paste - Buffer empty");
        }
    }


    public LinkedList<MapNode> createNewMapNodesFromList(LinkedList<MapNode> list) {

        // create a new MapNode for each node in the list

        LinkedList<NodeTransform> tempWorkBuffer = new LinkedList<>();
        LinkedList<MapNode> tempCache = new LinkedList<>();

        int n = 1;
        for (MapNode node : list) {
            MapNode workBufferNode = new MapNode(n++, node.x, node.y, node.z, node.flag, true, false);
            if (node.hasMapMarker()) {
                workBufferNode.createMapMarker(node.getMarkerName(), node.getMarkerGroup());
            }
            tempWorkBuffer.add(new NodeTransform(node, workBufferNode));
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

        if ((roadMap == null) || (mapImage == null)) {
            return;
        }

        if (!inOriginalLocation) {
            selectionCentre = screenPosToWorldPos(getMapPanel().getWidth() / 2, getMapPanel().getHeight() / 2);
            selectionAreaInfo areaInfo = getSelectionBounds(newNodes, WORLD_COORDINATES);
            if (areaInfo != null) {
                diffX = areaInfo.selectionCentre.getX() - selectionCentre.getX();
                diffY = areaInfo.selectionCentre.getY() - selectionCentre.getY();
            }
            if (bDebugLogCopyPasteInfo) {
                LOG.info("World coordinates of viewport Centre = {}", selectionCentre);
                LOG.info("Move distance = {},{}", diffX, diffY);
            }

        }
        clearMultiSelection();

        canAutoSave = false;

        int startID = RoadMap.mapNodes.size() + 1;
        for (MapNode node : newNodes) {
            node.id = startID++;
            if ( !inOriginalLocation) {
                node.x -= diffX;
                node.z -= diffY;
                double yValue = getYValueFromHeightMap(node.x, node.z);
                if (yValue != -1) node.y = yValue;
            }
            node.isSelected = true;
            RoadMap.mapNodes.add(node);
            multiSelectList.add(node);
        }

        canAutoSave = true;

        isMultipleSelected = true;

        changeManager.addChangeable( new PasteSelectionButton.PasteSelectionChanger(newNodes) );
        setStale(true);
        getMapPanel().repaint();
    }

    public static void rotateSelected(double angle) {
        selectionAreaInfo recInfo = getSelectionBounds(multiSelectList, WORLD_COORDINATES);
        canAutoSave = false;
        for (MapNode node : multiSelectList) {
            if ( recInfo != null ) {
                rotate(node, recInfo.selectionCentre, angle);
            }
        }
        canAutoSave = true;
        getMapPanel().repaint();
        getSelectionBounds(multiSelectList, WORLD_COORDINATES);
    }

    public static void rotate(MapNode node, Point2D centre, double angle) {
        Point2D result = new Point2D.Double();
        AffineTransform rotation = new AffineTransform();
        double angleInRadians = Math.toRadians(angle);
        rotation.rotate(angleInRadians, centre.getX(), centre.getY());
        rotation.transform(new Point2D.Double(node.x, node.z), result);
        node.x = result.getX();
        node.z = result.getY();
    }

    @SuppressWarnings("SameParameterValue")
    private static selectionAreaInfo getSelectionBounds(LinkedList<MapNode> nodesToCopy, int coordType) {
        double topLeftX = 0, topLeftY = 0;
        double bottomRightX = 0, bottomRightY = 0;
        for (int j = 0; j < nodesToCopy.size(); j++) {
            MapNode node = nodesToCopy.get(j);
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

        if (coordType == WORLD_COORDINATES) {
            if (bDebugLogCopyPasteInfo) LOG.info("## WORLD_COORDINATES ## Rectangle start = {} , {} : end = {} , {} : size = {} , {} : Centre = {} , {}", topLeftX, topLeftY, bottomRightX, bottomRightY, rectSizeX, rectSizeY, centreX, centreY);
            return new selectionAreaInfo( new Point2D.Double(topLeftX, topLeftY) ,
                    new Point2D.Double(bottomRightX, bottomRightY),
                    new Point2D.Double(rectSizeX, rectSizeY),
                    new Point2D.Double(centreX, centreY));
        } else if (coordType == SCREEN_COORDINATES) {
            Point2D topLeft = worldPosToScreenPos(topLeftX, topLeftY);
            Point2D bottomRight = worldPosToScreenPos(bottomRightX, bottomRightY);
            Point2D rectSize = worldPosToScreenPos(rectSizeX, rectSizeY);
            Point2D rectCentre = worldPosToScreenPos(centreX, centreY);
            if (bDebugLogCopyPasteInfo) LOG.info("## SCREEN_COORDINATES ## Rectangle start = {} : end = {} : size = {} : Centre = {} ", topLeft, bottomRight, rectSize, rectCentre);
            return new selectionAreaInfo(topLeft, bottomRight, rectSize, rectCentre);
        } else {
            LOG.info("No return type specified for getSelectionBounds() - returning null");
            return null;
        }
    }

    public static class selectionAreaInfo {
        private final Point2D startCoordinates;
        private final Point2D EndCoordinates;
        private final Point2D selectionSize;
        private final Point2D selectionCentre;

        public selectionAreaInfo(Point2D start, Point2D end, Point2D size, Point2D centre){
            this.startCoordinates = start;
            this.EndCoordinates = end;
            this.selectionSize = size;
            this.selectionCentre = centre;
        }
        // getter setters
        @SuppressWarnings("unused")
        public Point2D getRectangleStart() {
            return this.startCoordinates;
        }

        @SuppressWarnings("unused")
        public Point2D getRectangleEnd() {
            return this.EndCoordinates;
        }

        @SuppressWarnings("unused")
        public Point2D getRectangleSize() {
            return this.selectionSize;
        }

        @SuppressWarnings("unused")
        public Point2D getRectangleCentre() {
            return this.selectionCentre;
        }

    }
}
