package AutoDriveEditor.Managers;

import javax.swing.*;

import AutoDriveEditor.GUI.GUIUtils;
import AutoDriveEditor.GUI.MenuBuilder;
import AutoDriveEditor.RoadNetwork.MapNode;
import AutoDriveEditor.RoadNetwork.RoadMap;

import java.util.LinkedList;
import java.util.concurrent.ExecutionException;

import static AutoDriveEditor.AutoDriveEditor.*;
import static AutoDriveEditor.GUI.MenuBuilder.*;
import static AutoDriveEditor.MapPanel.MapPanel.*;
import static AutoDriveEditor.RoadNetwork.RoadMap.*;
import static AutoDriveEditor.Utils.LoggerUtils.*;



public class ScanManager {

    public static boolean networkScanned;
    public static double searchDistance = 0.05;

    public static void scanNetworkForOverlapNodes() {
        scanNetworkForOverlapNodes(searchDistance, false);
    }

    public static Integer scanNetworkForOverlapNodes(double distance, boolean getResult) {
        networkScanned = false;
        searchDistance = distance;
        for (MapNode node : RoadMap.mapNodes) {
            node.hasWarning = false;
            node.warningNodes.clear();
        }
        ScanNetworkWorker scanThread = new ScanNetworkWorker(searchDistance);
        scanThread.execute();
        if (getResult) {
            try {
                return scanThread.get();
            } catch (InterruptedException | ExecutionException e) {
                e.printStackTrace();
                return 0;
            }
        }
        return 0;
    }

    public static class ScanNetworkWorker extends SwingWorker<Integer, Void> {

        public double scanArea;
        public int count = 0;

        public ScanNetworkWorker(double distance) {
            this.scanArea = distance;
        }

        @Override
        protected Integer doInBackground() throws Exception {
            long timer = 0;
            LOG.info("Starting Background Scan for Overlapping Nodes");
            LOG.info(" ## Distance to search around node = {} meters ##", searchDistance);
            timer = System.currentTimeMillis();

            for (MapNode node : RoadMap.mapNodes) {
                int result = checkAreaForNodeOverlap(node);
                if (result > 0) count += 1;
            }



            String text = "Roadmap nodes = " + mapNodes.size() + " --- Found " + count + " nodes overlapping --- Time Taken " +
                    (float) (System.currentTimeMillis() - timer) / 1000 + " seconds" ;
            GUIUtils.showInTextArea(text, true);
            LOG.info(text);
            getMapPanel().repaint();
            return count;
        }

        @Override
        protected void done() {
            networkScanned = true;
            MenuBuilder.fixNodesEnabled(true);
        }
    }



    private static void scanNetwork() {

    }

    public static int checkAreaForNodeOverlap(MapNode node) {

        int result = 0;

        if (roadMap != null) {
            double searchAreaHalf = searchDistance / 2;

            double worldStartX = node.x - searchAreaHalf;
            double worldStartY = node.y - searchAreaHalf;
            double worldStartZ = node.z - searchAreaHalf;

            double areaX = (node.x + searchAreaHalf) - worldStartX;
            double areaY = (node.y + searchAreaHalf) - worldStartY;
            double areaZ = (node.z + searchAreaHalf) - worldStartZ;

            for (MapNode mapNode : RoadMap.mapNodes) {
                if (mapNode != node) {
                    if (worldStartX < mapNode.x + searchDistance && (worldStartX + areaX) > mapNode.x - searchDistance &&
                            worldStartY < mapNode.y + searchDistance && (worldStartY + areaY) > mapNode.y - searchDistance &&
                            worldStartZ < mapNode.z + searchDistance && (worldStartZ + areaZ) > mapNode.z - searchDistance) {

                        result += 1;

                        if (!mapNode.warningNodes.contains(node)) {
                            //LOG.info("Adding {} to {} warning list",node.id, mapNode.id);
                            mapNode.warningNodes.add(node);
                            mapNode.hasWarning = true;
                        }
                        if (!node.warningNodes.contains(mapNode)) {
                            //LOG.info("Adding {} to {} warning list",mapNode.id, node.id);
                            node.warningNodes.add(mapNode);
                            node.hasWarning = true;
                        }
                    }
                }
            }

        }
        return result;
    }

    public static void checkNodeOverlap(MapNode node) {
        if (checkAreaForNodeOverlap(node) == 0 ) {
            LOG.info("Node clear");

            for (MapNode mapNode : node.warningNodes) {
                //LOG.info("removing {} from {} warning list",node.id, mapNode.id);
                mapNode.warningNodes.remove(node);
                mapNode.hasWarning = mapNode.warningNodes.size() != 0;
            }

            node.hasWarning = false;
            node.warningNodes.clear();
        }
    }

    public static void  mergeOverlappingNodes() {
        if (!networkScanned) {
            LOG.info("need to run network scan first");
        } else {
            LinkedList<MapNode> mergedIncoming = new LinkedList<>();
            LinkedList<MapNode> mergedOutgoing = new LinkedList<>();
            LinkedList<MapNode> deleteNodeList = new LinkedList<>();
            LinkedList<MapNode> mergeNodeList = new LinkedList<>();

            LOG.info("Running merge nodes");
            for (MapNode mapNode : RoadMap.mapNodes) {
                if (mapNode.hasWarning && !mapNode.scheduleDelete) {

                    if (bDebugMerge) LOG.info("Merging overlapping nodes into ID {}", mapNode.id);

                    for (MapNode overlapNode : mapNode.warningNodes) {

                        mergedIncoming.clear();
                        mergedOutgoing.clear();
                        if (bDebugMerge) LOG.info("Storing incoming for {}", overlapNode.id);

                        //LOG.info("incoming size = {} , merge size before = {}", overlapNode.incoming.size(), mergedIncoming.size());

                        for (MapNode overlapNodeIncoming : overlapNode.incoming) {
                            if (overlapNodeIncoming != mapNode && !mergedIncoming.contains(overlapNodeIncoming)) {
                                if (bDebugMerge) LOG.info("adding {} to mergedIncoming", overlapNodeIncoming.id);
                                mergedIncoming.add(overlapNodeIncoming);
                            }
                            if (overlapNodeIncoming.outgoing.contains(overlapNode)) {
                                if (!overlapNodeIncoming.outgoing.contains(mapNode)) {
                                    if (bDebugMerge) LOG.info("adding {} to {}.outgoing", mapNode.id, overlapNodeIncoming.id);
                                    overlapNodeIncoming.outgoing.add(mapNode);
                                }
                            }
                        }

                        //LOG.info("merge size after = {}", mergedIncoming.size());

                        if (bDebugMerge) LOG.info("Storing outgoing for {}", overlapNode.id);

                        for (MapNode outgoingNode : overlapNode.outgoing) {
                            if (!mergedOutgoing.contains(outgoingNode)) {
                                if (bDebugMerge) LOG.info("adding {} to mergedOutgoing", outgoingNode.id);
                                if (outgoingNode != mapNode) mergedOutgoing.add(outgoingNode);
                            }
                            if (outgoingNode.incoming.contains(overlapNode)) {
                                if (bDebugMerge) LOG.info("adding {} to {}.outgoing", mapNode.id, outgoingNode.id);
                                if (!outgoingNode.incoming.contains(mapNode)) outgoingNode.incoming.add(mapNode);
                            }
                        }

                        for (MapNode reverseNode : RoadMap.mapNodes) {
                            if (reverseNode.outgoing.contains(overlapNode) && !overlapNode.incoming.contains(reverseNode)) {
                                if (bDebugMerge) LOG.info("#### reverse incoming Connection from {}", reverseNode.id);
                                //reverseNode.outgoing.remove(overlapNode);
                                if (!reverseNode.outgoing.contains(mapNode)) reverseNode.outgoing.add(mapNode);
                            }
                            if (reverseNode.incoming.contains(overlapNode)) {
                                if (bDebugMerge) LOG.info("#### reverse incoming Connection from {}", reverseNode.id);
                                //reverseNode.incoming.remove(overlapNode);
                                if (!reverseNode.incoming.contains(mapNode)) reverseNode.incoming.add(mapNode);
                            }
                        }

                        // edge case #1 - remove self references
                        for (MapNode node : mapNode.incoming) {
                            if (node == mapNode) mapNode.incoming.remove(mapNode);
                        }
                        for (MapNode node : mapNode.outgoing) {
                            if (node == mapNode) mapNode.outgoing.remove(mapNode);
                        }

                        if (bDebugMerge) LOG.info("stored Connections - Deleting node {}", overlapNode.id);
                        overlapNode.scheduleDelete = true;
                        deleteNodeList.add(overlapNode);
                        if ( mergedIncoming.size() >50 || mergedOutgoing.size() > 50 ) {
                            LOG.info("ID {} -- incoming {} , outgoing {}", overlapNode.id, mergedIncoming.size(), mergedOutgoing.size());
                        }
                    }
                    if (bDebugMerge) LOG.info("Adding all connections to mapNode {}", mapNode.id);

                    for (MapNode node : mapNode.incoming) {
                        if (!mergedIncoming.contains(node) && !node.scheduleDelete) mergedIncoming.add(node);
                    }
                    for (MapNode node : mapNode.outgoing) {
                        if (!mergedOutgoing.contains(node) && !node.scheduleDelete) mergedOutgoing.add(node);
                    }
                    mapNode.incoming.clear();
                    mapNode.outgoing.clear();
                    mapNode.incoming.addAll(mergedIncoming);
                    mapNode.outgoing.addAll(mergedOutgoing);
                    mergeNodeList.add(mapNode);
                }
            }
            String text = "Merging nodes completed - Removing " + deleteNodeList.size() + " nodes";
            GUIUtils.showInTextArea(text, true);
            LOG.info("Removing {} nodes", deleteNodeList.size());
            for (MapNode nodeToDelete : deleteNodeList) {
                removeMapNode(nodeToDelete);
            }

            for (MapNode mergedNode : mergeNodeList) {
                if (checkAreaForNodeOverlap(mergedNode) == 0) {
                    mergedNode.hasWarning = false;
                    mergedNode.warningNodes.clear();
                    getMapPanel().repaint();
                } else {
                    LOG.info("mapnode is still overlapping");
                }
            }

        }
    }
}
