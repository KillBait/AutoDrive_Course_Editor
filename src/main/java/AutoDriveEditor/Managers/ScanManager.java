package AutoDriveEditor.Managers;

import AutoDriveEditor.AutoDriveEditor;
import AutoDriveEditor.GUI.MenuBuilder;
import AutoDriveEditor.RoadNetwork.MapNode;
import AutoDriveEditor.RoadNetwork.RoadMap;
import AutoDriveEditor.Utils.GUIUtils;

import javax.swing.*;
import java.util.LinkedList;
import java.util.concurrent.ExecutionException;

import static AutoDriveEditor.GUI.MenuBuilder.bDebugLogMerge;
import static AutoDriveEditor.GUI.MenuBuilder.bDebugProfile;
import static AutoDriveEditor.Locale.LocaleManager.localeString;
import static AutoDriveEditor.MapPanel.MapPanel.*;
import static AutoDriveEditor.RoadNetwork.MapNode.NODE_WARNING_OVERLAP;
import static AutoDriveEditor.RoadNetwork.RoadMap.mapNodes;
import static AutoDriveEditor.RoadNetwork.RoadMap.removeMapNode;
import static AutoDriveEditor.Utils.FileUtils.removeExtension;
import static AutoDriveEditor.Utils.GUIUtils.showInTextArea;
import static AutoDriveEditor.Utils.LoggerUtils.LOG;
import static AutoDriveEditor.XMLConfig.GameXML.*;
import static AutoDriveEditor.XMLConfig.RouteManagerXML.saveRouteManagerXML;


public class ScanManager {

    public static boolean networkScanned;
    public static double searchDistance = 0.05;

    public static void  scanNetworkForOverlapNodes() {
        scanNetworkForOverlapNodes(searchDistance, false);
    }

    public static Integer scanNetworkForOverlapNodes(double distance, boolean getResult) {
        networkScanned = false;
        searchDistance = distance;
        for (MapNode node : RoadMap.mapNodes) {
            node.clearWarning();
            node.warningNodes.clear();
        }

        String backString = "Starting Background Thread to scan for Overlapping Nodes --> Search Distance " + searchDistance + " meters";
        showInTextArea(backString, true, true);
        //LOG.info("Starting Background Thread to scan for Overlapping Nodes --> Search Distance {}m", searchDistance);
        //LOG.info(" ## Distance to search around node = {} meters ##", searchDistance);

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
        public long timer;

        public ScanNetworkWorker(double distance) {
            this.scanArea = distance;
        }

        @Override
        protected Integer doInBackground() {
            int count = 0;
            Thread.currentThread().setName("Scan Network Thread");
            LOG.info("Starting Background Scan");
            timer = System.currentTimeMillis();

            for (MapNode node : RoadMap.mapNodes) {
                int result = checkAreaForNodeOverlap(node);
                if (result > 0) count += 1;
            }
            LOG.info("Finished Background Scan");
            return count;
        }

        @Override
        protected void done() {
            networkScanned = true;
            try {
                if (bDebugProfile) {
                    int count = get();
                    String text = "Checked " + mapNodes.size() + " Roadmap nodes --- Found " + count + " nodes overlapping --- Time Taken " +
                            (float) (System.currentTimeMillis() - timer) / 1000 + " seconds" ;
                    showInTextArea(text, true, true);
                }
            } catch (InterruptedException | ExecutionException e) {
                throw new RuntimeException(e);
            }

            getMapPanel().repaint();
            MenuBuilder.fixNodesEnabled(true);
        }
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
                            mapNode.warningNodes.add(node);
                            mapNode.hasWarning = true;
                            mapNode.warningType = NODE_WARNING_OVERLAP;
                        }
                        if (!node.warningNodes.contains(mapNode)) {
                            node.warningNodes.add(mapNode);
                            node.hasWarning = true;
                            node.warningType = NODE_WARNING_OVERLAP;
                        }
                    }
                }
            }

        }
        return result;
    }

    public static void checkNodeOverlap(MapNode node) {
        if (checkAreaForNodeOverlap(node) == 0 ) {
            for (MapNode mapNode : node.warningNodes) {
                mapNode.warningNodes.remove(node);
                if (mapNode.warningNodes.size() != 0) {
                    mapNode.hasWarning = true;
                    mapNode.warningType = NODE_WARNING_OVERLAP;
                } else {
                    //mapNode.hasWarning = false;
                    mapNode.clearWarning();
                }
            }

            node.clearWarning();
            node.warningNodes.clear();
        }
    }

    public static void  mergeOverlappingNodes() {
        int response = JOptionPane.showConfirmDialog(AutoDriveEditor.editor, localeString.getString("dialog_merge_confirm"), "AutoDrive Editor", JOptionPane.YES_NO_OPTION, JOptionPane.QUESTION_MESSAGE);
        if (response == JOptionPane.YES_OPTION) {
            if (!networkScanned) {
                LOG.info("need to run network scan first");
            } else {
                saveMergeBackupConfigFile();
                canAutoSave = false;
                LinkedList<MapNode> mergedIncoming = new LinkedList<>();
                LinkedList<MapNode> mergedOutgoing = new LinkedList<>();
                LinkedList<MapNode> deleteNodeList = new LinkedList<>();
                LinkedList<MapNode> mergeNodeList = new LinkedList<>();

                LOG.info("Running merge nodes");
                for (MapNode mapNode : RoadMap.mapNodes) {
                    if (mapNode.hasWarning && !mapNode.scheduledToBeDeleted) {

                        if (bDebugLogMerge) LOG.info("Merging overlapping nodes into ID {}", mapNode.id);

                        for (MapNode overlapNode : mapNode.warningNodes) {

                            if (bDebugLogMerge) LOG.info("Storing incoming for {}", overlapNode.id);

                            for (MapNode overlapNodeIncoming : overlapNode.incoming) {
                                if (overlapNodeIncoming != mapNode && !mergedIncoming.contains(overlapNodeIncoming)) {
                                    if (bDebugLogMerge) LOG.info("adding {} to mergedIncoming", overlapNodeIncoming.id);
                                    mergedIncoming.add(overlapNodeIncoming);
                                }
                                if (overlapNodeIncoming.outgoing.contains(overlapNode)) {
                                    if (!overlapNodeIncoming.outgoing.contains(mapNode)) {
                                        if (bDebugLogMerge) LOG.info("adding {} to {}.outgoing", mapNode.id, overlapNodeIncoming.id);
                                        overlapNodeIncoming.outgoing.add(mapNode);
                                    }
                                }
                            }

                            if (bDebugLogMerge) LOG.info("Storing outgoing for {}", overlapNode.id);

                            for (MapNode outgoingNode : overlapNode.outgoing) {
                                if (!mergedOutgoing.contains(outgoingNode)) {
                                    if (bDebugLogMerge) LOG.info("adding {} to mergedOutgoing", outgoingNode.id);
                                    if (outgoingNode != mapNode) mergedOutgoing.add(outgoingNode);
                                }
                                if (outgoingNode.incoming.contains(overlapNode)) {
                                    if (bDebugLogMerge) LOG.info("adding {} to {}.outgoing", mapNode.id, outgoingNode.id);
                                    if (!outgoingNode.incoming.contains(mapNode)) outgoingNode.incoming.add(mapNode);
                                }
                            }

                            for (MapNode reverseNode : RoadMap.mapNodes) {
                                if (reverseNode.outgoing.contains(overlapNode) && !overlapNode.incoming.contains(reverseNode)) {
                                    if (bDebugLogMerge) LOG.info("#### reverse incoming Connection from {}", reverseNode.id);
                                    if (!reverseNode.outgoing.contains(mapNode)) reverseNode.outgoing.add(mapNode);
                                }
                                if (reverseNode.incoming.contains(overlapNode)) {
                                    if (bDebugLogMerge) LOG.info("#### reverse incoming Connection from {}", reverseNode.id);
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

                            if (bDebugLogMerge) LOG.info("stored Connections - Deleting node {}", overlapNode.id);
                            overlapNode.scheduledToBeDeleted = true;
                            deleteNodeList.add(overlapNode);
                        }
                        if (bDebugLogMerge) LOG.info("Adding all connections to mapNode {}", mapNode.id);

                        for (MapNode node : mapNode.incoming) {
                            if (!mergedIncoming.contains(node) && !node.scheduledToBeDeleted) mergedIncoming.add(node);
                        }
                        for (MapNode node : mapNode.outgoing) {
                            if (!mergedOutgoing.contains(node) && !node.scheduledToBeDeleted) mergedOutgoing.add(node);
                        }
                        mapNode.incoming.clear();
                        mapNode.outgoing.clear();
                        mapNode.incoming.addAll(mergedIncoming);
                        mapNode.outgoing.addAll(mergedOutgoing);
                        mergeNodeList.add(mapNode);
                        mergedIncoming.clear();
                        mergedOutgoing.clear();
                    }
                }
                String text = "Merging nodes completed - Removing " + deleteNodeList.size() + " nodes";
                showInTextArea(text, true, true);
                for (MapNode nodeToDelete : deleteNodeList) {
                    removeMapNode(nodeToDelete);
                }

                for (MapNode mergedNode : mergeNodeList) {
                    if (checkAreaForNodeOverlap(mergedNode) == 0) {
                        mergedNode.hasWarning = false;
                        mergedNode.warningNodes.clear();
                        getMapPanel().repaint();
                    } else {
                        LOG.info("mapNode is still overlapping");
                    }
                }

                for (MapNode node : RoadMap.mapNodes) {
                    if ( node.incoming.size() >10 || node.outgoing.size() > 10 ) {
                        LOG.info(" #### HIGH CONNECTION COUNT #### ID {} -- incoming {} , outgoing {}", node.id, node.incoming.size(), node.outgoing.size());
                    }
                }
                canAutoSave = true;
            }
        }
    }

    public static void saveMergeBackupConfigFile() {
        LOG.info("{}", localeString.getString("console_config_merge_backup"));
        String filename = removeExtension(xmlConfigFile.getAbsolutePath()) + "_mergeBackup.xml";
        if (configType == CONFIG_SAVEGAME) {
            saveConfigFile(filename, false, true);
        } else if (configType == CONFIG_ROUTEMANAGER) {
            saveRouteManagerXML(filename, false, true);
        }


    }
}
