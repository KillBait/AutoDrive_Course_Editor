package AutoDriveEditor.Managers;

import AutoDriveEditor.AutoDriveEditor;
import AutoDriveEditor.RoadNetwork.MapNode;
import AutoDriveEditor.RoadNetwork.RoadMap;
import AutoDriveEditor.Utils.Classes.KDTree3D;
import AutoDriveEditor.Utils.ProfileUtil;

import javax.swing.*;
import java.util.LinkedList;
import java.util.List;

import static AutoDriveEditor.AutoDriveEditor.getMapPanel;
import static AutoDriveEditor.GUI.MapPanel.*;
import static AutoDriveEditor.GUI.Menus.DebugMenu.Logging.LogMergeFunctionMenu.bDebugLogMerge;
import static AutoDriveEditor.GUI.Menus.DebugMenu.Logging.LogScanManagerInfoMenu.bDebugLogScanManagerInfo;
import static AutoDriveEditor.GUI.TextPanel.showInTextArea;
import static AutoDriveEditor.Locale.LocaleManager.getLocaleString;
import static AutoDriveEditor.RoadNetwork.MapNode.NODE_WARNING_OVERLAP;
import static AutoDriveEditor.RoadNetwork.RoadMap.networkNodesList;
import static AutoDriveEditor.RoadNetwork.RoadMap.removeMapNode;
import static AutoDriveEditor.Utils.FileUtils.removeExtension;
import static AutoDriveEditor.Utils.LoggerUtils.LOG;
import static AutoDriveEditor.XMLConfig.AutoSave.resumeAutoSaving;
import static AutoDriveEditor.XMLConfig.AutoSave.suspendAutoSaving;
import static AutoDriveEditor.XMLConfig.GameXML.saveGameConfig;
import static AutoDriveEditor.XMLConfig.GameXML.xmlConfigFile;
import static AutoDriveEditor.XMLConfig.RoutesXML.saveRouteManagerXML;


public class ScanManager {

    public static final double searchDistance = 0.1;
    private static final ProfileUtil treeInsertTimer = new ProfileUtil();
    private static final ProfileUtil treeSearchTimer = new ProfileUtil();

    public static void  scanNetworkForOverlapNodes() {
        scanNetworkForOverlapNodes(searchDistance);
    }

    @SuppressWarnings("UnusedReturnValue")
    public static Integer scanNetworkForOverlapNodes(double distance) {

        showInTextArea("Starting scan for overlapping nodes, search distance " + distance + " meters", true, true);

        // clear all previous warnings
        for (MapNode node : RoadMap.networkNodesList) {
            node.clearWarningNodes();
            node.getWarningNodes().clear();
        }

        // start profile timers
        if (bDebugLogScanManagerInfo) treeInsertTimer.startTimer();
        treeSearchTimer.startTimer();

        // Create the KDTree and populate it with all the nodes from the network
        KDTree3D networkTree = new KDTree3D();

        for (MapNode node : RoadMap.networkNodesList) {
            networkTree.insert(node);
        }

        if (bDebugLogScanManagerInfo) {
            treeInsertTimer.stopTimer();
            LOG.info("## Scan Manager DEBUG ## Tree Creation Time = {}ms , Num Nodes Added = {}", treeInsertTimer.getTime(3), networkTree.size());
        }

        // start the network search
        List<MapNode> nodesWithinDistance;
        int numOverlaps = 0;
        StringBuilder list = new StringBuilder();
        for (MapNode mapNode : RoadMap.networkNodesList) {
            nodesWithinDistance = networkTree.withinDistance(mapNode, 0.1);
            if (nodesWithinDistance.size() > 0) {
                numOverlaps++;
                for (MapNode overlapNode : nodesWithinDistance) {
                    if (!mapNode.getWarningNodes().contains(overlapNode)) {
                        mapNode.getWarningNodes().add(overlapNode);
                        mapNode.setHasWarning(true, NODE_WARNING_OVERLAP);
                    }
                    if (bDebugLogScanManagerInfo) list.append(overlapNode.id).append(",");
                }
            }
        }

        if (bDebugLogScanManagerInfo) { LOG.info("## Scan Manager DEBUG ## KDTree matches {}", list); }

        treeSearchTimer.stopTimer();
        showInTextArea("Network scan completed, Checked " + networkNodesList.size() + " Roadmap nodes --- Found " + numOverlaps + " nodes overlapping --- Time Taken " + treeSearchTimer.getTime(2) + "ms", true, true);
        return numOverlaps;
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

            node.clearWarningNodes();

            for (MapNode mapNode : RoadMap.networkNodesList) {
                if (mapNode != node) {
                    if (worldStartX < mapNode.x + searchDistance && (worldStartX + areaX) > mapNode.x - searchDistance &&
                            worldStartY < mapNode.y + searchDistance && (worldStartY + areaY) > mapNode.y - searchDistance &&
                            worldStartZ < mapNode.z + searchDistance && (worldStartZ + areaZ) > mapNode.z - searchDistance) {

                        result += 1;

                        if (!mapNode.getWarningNodes().contains(node)) {
                            mapNode.getWarningNodes().add(node);
                            mapNode.setHasWarning(true, NODE_WARNING_OVERLAP);
                        }
                        if (!node.getWarningNodes().contains(mapNode)) {
                            node.getWarningNodes().add(mapNode);
                            node.setHasWarning(true, NODE_WARNING_OVERLAP);
                        }
                    }
                }
            }
        }
        return result;
    }

    public static void checkNodeOverlap(MapNode node) {
        if (node.hasWarning()) {
            for (MapNode warningNode : node.getWarningNodes()) {
                checkAreaForNodeOverlap(warningNode);
            }
        }
        if (checkAreaForNodeOverlap(node) == 0 ) {
            for (MapNode mapNode : node.getWarningNodes()) {
                mapNode.getWarningNodes().remove(node);
                if (mapNode.getWarningNodes().size() != 0) {
                    mapNode.setHasWarning(true, NODE_WARNING_OVERLAP);
                } else {
                    mapNode.clearWarningNodes();
                }
            }

            node.clearWarningNodes();
            node.getWarningNodes().clear();
            getMapPanel().repaint();
        }
    }

    public static void  mergeOverlappingNodes() {
        int response = JOptionPane.showConfirmDialog(AutoDriveEditor.editor, getLocaleString("dialog_merge_confirm"), "AutoDrive Editor", JOptionPane.YES_NO_OPTION, JOptionPane.QUESTION_MESSAGE);
        if (response == JOptionPane.YES_OPTION) {
            saveMergeBackupConfigFile();
            suspendAutoSaving();
            LinkedList<MapNode> mergedIncoming = new LinkedList<>();
            LinkedList<MapNode> mergedOutgoing = new LinkedList<>();
            LinkedList<MapNode> deleteNodeList = new LinkedList<>();
            LinkedList<MapNode> mergeNodeList = new LinkedList<>();

            LOG.info("Running merge nodes");
            for (MapNode mapNode : RoadMap.networkNodesList) {
                if (mapNode.hasWarning() && !mapNode.isScheduledToBeDeleted()) {

                    if (bDebugLogMerge) LOG.info("Merging overlapping nodes into ID {}", mapNode.id);

                    for (MapNode overlapNode : mapNode.getWarningNodes()) {

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

                        for (MapNode reverseNode : RoadMap.networkNodesList) {
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
                        overlapNode.setScheduledToBeDeleted(true);
                        deleteNodeList.add(overlapNode);
                    }
                    if (bDebugLogMerge) LOG.info("Adding all connections to mapNode {}", mapNode.id);

                    for (MapNode node : mapNode.incoming) {
                        if (!mergedIncoming.contains(node) && !node.isScheduledToBeDeleted()) mergedIncoming.add(node);
                    }
                    for (MapNode node : mapNode.outgoing) {
                        if (!mergedOutgoing.contains(node) && !node.isScheduledToBeDeleted()) mergedOutgoing.add(node);
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
                    mergedNode.clearWarningNodes();
                    getMapPanel().repaint();
                } else {
                    LOG.info("mapNode is still overlapping");
                }
            }

            for (MapNode node : RoadMap.networkNodesList) {
                if ( node.incoming.size() >10 || node.outgoing.size() > 10 ) {
                    LOG.info(" #### HIGH CONNECTION COUNT #### ID {} -- incoming {} , outgoing {}", node.id, node.incoming.size(), node.outgoing.size());
                }
            }
            resumeAutoSaving();
        }
    }

    public static void saveMergeBackupConfigFile() {
        LOG.info("{}", getLocaleString("console_config_merge_backup"));
        String filename = removeExtension(xmlConfigFile.getAbsolutePath()) + "_mergeBackup.xml";
        if (configType == CONFIG_SAVEGAME) {
            saveGameConfig(filename, false, true);
        } else if (configType == CONFIG_ROUTEMANAGER) {
            saveRouteManagerXML(filename, false, true);
        }


    }
}
