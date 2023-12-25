package AutoDriveEditor.XMLConfig;

import AutoDriveEditor.Managers.ChangeManager;
import AutoDriveEditor.RoadNetwork.MapNode;
import AutoDriveEditor.RoadNetwork.MarkerGroup;
import AutoDriveEditor.RoadNetwork.RoadMap;
import org.w3c.dom.Document;
import org.w3c.dom.Element;
import org.w3c.dom.Node;
import org.w3c.dom.NodeList;
import org.xml.sax.SAXException;

import javax.swing.*;
import javax.xml.parsers.DocumentBuilder;
import javax.xml.parsers.DocumentBuilderFactory;
import javax.xml.parsers.ParserConfigurationException;
import javax.xml.transform.OutputKeys;
import javax.xml.transform.Transformer;
import javax.xml.transform.TransformerException;
import javax.xml.transform.TransformerFactory;
import javax.xml.transform.dom.DOMSource;
import javax.xml.transform.stream.StreamResult;
import java.io.File;
import java.io.IOException;
import java.util.ArrayList;
import java.util.Iterator;
import java.util.LinkedList;
import java.util.Objects;

import static AutoDriveEditor.AutoDriveEditor.*;
import static AutoDriveEditor.Classes.MapImage.loadHeightMap;
import static AutoDriveEditor.Classes.MapImage.loadMapImage;
import static AutoDriveEditor.GUI.MapPanel.*;
import static AutoDriveEditor.GUI.Menus.DebugMenu.Logging.LogRouteManagerMenu.bDebugLogRouteManager;
import static AutoDriveEditor.GUI.Menus.EditorMenu.*;
import static AutoDriveEditor.GUI.Menus.FileMenu.RecentFilesMenu.addToRecentFiles;
import static AutoDriveEditor.Locale.LocaleManager.getLocaleString;
import static AutoDriveEditor.Managers.ImportManager.setEditorUsingImportedImage;
import static AutoDriveEditor.Managers.MultiSelectManager.clearMultiSelection;
import static AutoDriveEditor.Managers.ScanManager.scanNetworkForOverlapNodes;
import static AutoDriveEditor.RoadNetwork.RoadMap.setRoadMapNodes;
import static AutoDriveEditor.Utils.FileUtils.removeExtension;
import static AutoDriveEditor.Utils.LoggerUtils.LOG;
import static AutoDriveEditor.XMLConfig.AutoSave.canAutoSave;
import static AutoDriveEditor.XMLConfig.AutoSave.resumeAutoSaving;
import static AutoDriveEditor.XMLConfig.EditorXML.checkStoredMapInfoFor;
import static AutoDriveEditor.XMLConfig.EditorXML.maxAutoSaveSlots;
import static AutoDriveEditor.XMLConfig.GameXML.autoSaveLastUsedSlot;
import static AutoDriveEditor.XMLConfig.GameXML.xmlConfigFile;

public class RoutesXML {

    public static final LinkedList<MarkerGroup> markerGroup = new LinkedList<>();

    public static boolean loadRouteManagerXML(File fXmlFile, boolean skipRoutesCheck, String mapName) {
        LOG.info("RouteManager loadFile: {}", fXmlFile.getAbsolutePath());

        //suspendAutoSaving();

        try {
            if (bDebugLogRouteManager) LOG.info("Parent = {}", fXmlFile.getParentFile().getParent());
            // Bug fix Commit by @rheational ( https://github.com/rhaetional )
            // Replaced "//" file separator for system supported call
            // Fixes issue #70 ( https://github.com/KillBait/AutoDrive_Course_Editor/issues/70 )
            String routeFile = fXmlFile.getParentFile().getParent() + File.separator + "routes.xml";


            RoadMap roadMap = loadRouteXML(fXmlFile);
            if (roadMap != null) {
                configType = CONFIG_ROUTEMANAGER;
                getMapPanel().setRoadMap(roadMap);
                xmlConfigFile = fXmlFile;
                if (bDebugLogRouteManager) LOG.info("name = {}", fXmlFile.getName());
                if (!skipRoutesCheck) {
                    LinkedList<Route> routeList = getRoutesConfigContents(new File(routeFile));
                    if (routeList !=null) {
                        if (bDebugLogRouteManager) LOG.info("route XML filename = {}", fXmlFile.getName());
                        String fileName = fXmlFile.getName();
                        if (fileName.contains("_autosave_")) {
                            if (bDebugLogRouteManager)LOG.info("Routes XML filename contains '_autosave_' .. Removing it, filename check against routes.xml will fail otherwise");
                            fileName = fileName.substring(0, fileName.indexOf("_autosave_")) + ".xml";
                        } else if (fileName.contains("_mergeBackup")) {
                            if (bDebugLogRouteManager)LOG.info("Routes XML filename contains '_mergeBackup' .. Removing it, filename check against routes.xml will fail otherwise");
                            fileName = fileName.substring(0, fileName.indexOf("_mergeBackup")) + ".xml";
                        }
                        if (bDebugLogRouteManager)LOG.info("using '{}' to check against routes.xml", fileName);
                        for (Route route : routeList) {
                            if (Objects.equals(route.fileName, fileName)) {
                                LOG.info("setting roadMapName to {}", route.map);
                                RoadMap.mapName = route.map;
                            }
                        }
                    }
                } else {
                    RoadMap.mapName = mapName;
                }
                if (bDebugLogRouteManager) LOG.info("map = {}", RoadMap.mapName);
                loadMapImage(RoadMap.mapName);
                loadHeightMap(RoadMap.mapName);
                checkStoredMapInfoFor(RoadMap.mapName);
                LOG.info("Session UUID = {}", RoadMap.uuid);
                updateWindowTitle();
                // initialize a new changeManager so undo/redo system won't throw errors
                // when we try to undo/redo something on a config that is no longer loaded
                changeManager = new ChangeManager();
                forceMapImageRedraw();
                setEditorUsingImportedImage(false);
                saveImageEnabled(false);
                setStale(false);
                scanNetworkForOverlapNodes();
                clearMultiSelection();
                gameXMLSaveEnabled(false);
                routesXMLSaveEnabled(true);
                resumeAutoSaving();
                buttonManager.enableAllButtons();
                addToRecentFiles(xmlConfigFile.getAbsolutePath(), configType);
                return true;
            } else {
                JOptionPane.showMessageDialog(editor, getLocaleString("dialog_config_route_unknown"), "AutoDrive", JOptionPane.ERROR_MESSAGE);
                //resumeAutoSaving();
                return false;
            }
        } catch (Exception e) {
            LOG.error(e.getMessage(), e);
            JOptionPane.showMessageDialog(editor, getLocaleString("dialog_config_load_route_failed"), "AutoDrive", JOptionPane.ERROR_MESSAGE);
            //resumeAutoSaving();
            return false;
        }
    }

    public static void saveRouteManagerXML(String newName, boolean isAutoSave, boolean isBackup) {
        if (isAutoSave) {
            LOG.info(getLocaleString("console_config_autosave_start"));
        } else if (isBackup) {
            LOG.info(getLocaleString("console_config_backup_start"));
        } else {
            LOG.info(getLocaleString("console_config_save_start"));
        }

        try
        {
            if (xmlConfigFile == null) return;
            saveRouteXML(xmlConfigFile, newName, isAutoSave, isBackup);
            if (!isAutoSave || !isBackup) {
                JOptionPane.showMessageDialog(editor, xmlConfigFile.getName() + " " + getLocaleString("dialog_save_success"), "AutoDrive", JOptionPane.INFORMATION_MESSAGE);
                setStale(false);
            }
        } catch (Exception e) {
            LOG.error(e.getMessage(), e);
            JOptionPane.showMessageDialog(editor, getLocaleString("dialog_save_fail"), "AutoDrive", JOptionPane.ERROR_MESSAGE);
        }
    }

    public static void autoSaveRouteManagerXML() {
        while (!canAutoSave()) {
            try {
                LOG.info("canAutoSave = false --- Waiting 5 seconds to try again");
                //noinspection BusyWait
                Thread.sleep(5000);
            } catch (InterruptedException e) {
                e.printStackTrace();
            }
        }

        String filename = removeExtension(xmlConfigFile.getAbsolutePath()) + "_autosave_" + autoSaveLastUsedSlot + ".xml";
        File file = new File(filename);
        try {
            if (file.exists()) {
                if (file.isDirectory())
                    throw new IOException("File '" + file + "' is a directory");

                if (!file.canWrite())
                    throw new IOException("File '" + file + "' cannot be written");
            }
            saveRouteManagerXML(filename, true, false);
            autoSaveLastUsedSlot++;
            if (autoSaveLastUsedSlot >= maxAutoSaveSlots + 1 ) autoSaveLastUsedSlot = 1;
        }
        catch(IOException ioEx) {
            ioEx.printStackTrace();
        }
    }

    private static RoadMap loadRouteXML(File fXmlFile)  throws ParserConfigurationException, IOException, SAXException {
        DocumentBuilderFactory dbFactory = DocumentBuilderFactory.newInstance();
        DocumentBuilder dBuilder = dbFactory.newDocumentBuilder();
        Document doc = dBuilder.parse(fXmlFile);
        doc.getDocumentElement().normalize();

        LOG.info("----------------------------");
        LOG.info("loadRouteXML Parsing {}", fXmlFile.getAbsolutePath());

        if (!doc.getDocumentElement().getNodeName().equals("routeExport")) {
            LOG.info("Not an AutoDrive RoutesManager config");
            return null;
        }

        NodeList waypointsList = doc.getElementsByTagName("waypoints");


        ArrayList<MapNode> nodes = new ArrayList<>();

        for (int temp = 0; temp < waypointsList.getLength(); temp++) {
            LOG.info("----------------------------");
            LOG.info("{} : {}", getLocaleString("console_root_node"), doc.getDocumentElement().getNodeName());
            Node nNode = waypointsList.item(temp);
            LOG.info("Current Element :{}", nNode.getNodeName());
            if (nNode.getNodeType() == Node.ELEMENT_NODE) {
                Element eElement = (Element) nNode;
                int wayPointIDs = Integer.parseInt(eElement.getAttribute("c"));
                LOG.info("<waypoints> key = {} ID's", wayPointIDs);
                LOG.info("----------------------------");

                if (wayPointIDs > 0 ) {
                    NodeList nodeList = eElement.getElementsByTagName("x").item(0).getChildNodes();
                    Node node = nodeList.item(0);
                    String xString = node.getNodeValue();
                    String[] xValues = xString.split(";");
                    LOG.info("{} <x> Entries", xValues.length);

                    nodeList = eElement.getElementsByTagName("y").item(0).getChildNodes();
                    node = nodeList.item(0);
                    String yString = node.getNodeValue();
                    String[] yValues = yString.split(";");
                    LOG.info("{} <y> Entries", yValues.length);

                    nodeList = eElement.getElementsByTagName("z").item(0).getChildNodes();
                    node = nodeList.item(0);
                    String zString = node.getNodeValue();
                    String[] zValues = zString.split(";");
                    LOG.info("{} <z> Entries", zValues.length);

                    nodeList = eElement.getElementsByTagName("out").item(0).getChildNodes();
                    node = nodeList.item(0);
                    String outString = node.getNodeValue();
                    String[] outValueArrays = outString.split(";");
                    LOG.info("{} <out> Entries", outValueArrays.length);

                    nodeList = eElement.getElementsByTagName("in").item(0).getChildNodes();
                    node = nodeList.item(0);
                    String incomingString = node.getNodeValue();
                    String[] inValueArrays = incomingString.split(";");
                    LOG.info("{} <in> Entries", inValueArrays.length);

                    nodeList = eElement.getElementsByTagName("flags").item(0).getChildNodes();
                    node = nodeList.item(0);
                    String flagsString = node.getNodeValue();
                    String[] flagsValue = flagsString.split(";");
                    LOG.info("{} <flags> Entries", flagsValue.length);
                    LOG.info("----------------------------");

                    LOG.info("starting creation of {} map nodes", wayPointIDs);

                    for (int i=0; i<wayPointIDs; i++) {
                        int id = i+1;
                        float x = Float.parseFloat(xValues[i]);
                        float y = Float.parseFloat(yValues[i]);
                        float z = Float.parseFloat(zValues[i]);
                        int flag = Integer.parseInt(flagsValue[i]);
                        MapNode mapNode = new MapNode(id, x, y, z, flag, false, false);
                        nodes.add(mapNode);
                    }

                    for (int i=0; i<wayPointIDs; i++) {
                        MapNode mapNode = nodes.get(i);
                        String[] outNodes = outValueArrays[i].split(",");
                        for (String outNode : outNodes) {
                            if (Integer.parseInt(outNode) != -1) {
                                mapNode.outgoing.add(nodes.get(Integer.parseInt(outNode) - 1));
                            }
                        }
                    }

                    for (int i=0; i<wayPointIDs; i++) {
                        MapNode mapNode = nodes.get(i);
                        String[] incomingNodes = inValueArrays[i].split(",");
                        for (String incomingNode : incomingNodes) {
                            if (Integer.parseInt(incomingNode) != -1) {
                                mapNode.incoming.add(nodes.get(Integer.parseInt(incomingNode)-1));
                            }
                        }
                    }
                    LOG.info("Finished creating all map nodes");
                    LOG.info("----------------------------");
                }
            }
        }

        NodeList groupList = doc.getElementsByTagName("g");
        markerGroup.clear();
        if (bDebugLogRouteManager) {
            LOG.info("----------------------------");
            LOG.info("Group Index length = {}", groupList.getLength());
            LOG.info("----------------------------");
        }
        for (int temp = 0; temp < groupList.getLength(); temp++) {
            Node markerNode = groupList.item(temp);
            if (markerNode.getNodeType() == Node.ELEMENT_NODE) {
                Element eElement = (Element) markerNode;
                String groupId = eElement.getAttribute("i");
                String groupName = eElement.getAttribute("n");
                if (bDebugLogRouteManager) LOG.info("Group {} : index {} , name {}", temp+1, groupId, groupName);
                MarkerGroup group = new MarkerGroup(Integer.parseInt(groupId), groupName);
                markerGroup.add(group);
            }
        }
        if (bDebugLogRouteManager) LOG.info("markerGroup size {}", markerGroup.size());

        NodeList markerList = doc.getElementsByTagName("m");
        if (bDebugLogRouteManager) {
            LOG.info("----------------------------");
            LOG.info("Marker length = {}", markerList.getLength());
            LOG.info("----------------------------");
        }
        for (int temp = 0; temp < markerList.getLength(); temp++) {
            Node markerNode = markerList.item(temp);
            if (markerNode.getNodeType() == Node.ELEMENT_NODE) {
                Element eElement = (Element) markerNode;
                String markerNodeId = eElement.getAttribute("i");
                String markerName = eElement.getAttribute("n");
                String markerGroup = eElement.getAttribute("g");
                if (bDebugLogRouteManager) LOG.info("Marker {} : ID {} , name '{}' , group '{}'", temp+1, markerNodeId, markerName, markerGroup);
                MapNode node = nodes.get(Integer.parseInt(markerNodeId) -1);
                node.createMapMarker(markerName, markerGroup);
            }
        }
        RoadMap roadMap = new RoadMap();
        setRoadMapNodes(roadMap, new LinkedList<>(nodes));
        return roadMap;
    }

    private static void saveRouteXML(File file, String newName, boolean isAutoSave, boolean isBackup) throws ParserConfigurationException, TransformerException {

        DocumentBuilderFactory docFactory = DocumentBuilderFactory.newInstance();
        DocumentBuilder docBuilder = docFactory.newDocumentBuilder();
        Document doc = docBuilder.newDocument();

        // create the root node

        Element root = doc.createElement("routeExport");
        doc.appendChild(root);

        // create a parent node for the waypoints

        Element waypoints = doc.createElement("waypoints");
        root.appendChild(waypoints);
        waypoints.setAttribute("c", String.valueOf(RoadMap.networkNodesList.size()));

        // create a child node for all x co-ordinates

        Element xElement = doc.createElement("x");
        waypoints.appendChild(xElement);
        StringBuilder xPositions = new StringBuilder();
        for (Iterator<MapNode> xIterator = RoadMap.networkNodesList.iterator(); xIterator.hasNext();) {
            MapNode node = xIterator.next();
            String ID = String.valueOf(node.x);
            if (xIterator.hasNext()) ID += ";";
            xPositions.append(ID);
        }
        xElement.setTextContent(xPositions.toString());

        // create a child node for all y co-ordinates

        Element yElement = doc.createElement("y");
        waypoints.appendChild(yElement);
        StringBuilder yPositions = new StringBuilder();
        for (Iterator<MapNode> yIterator = RoadMap.networkNodesList.iterator(); yIterator.hasNext();) {
            MapNode node = yIterator.next();
            String ID = String.valueOf(node.y);
            if (yIterator.hasNext()) ID += ";";
            yPositions.append(ID);
        }
        yElement.setTextContent(yPositions.toString());

        // create a child node for all z co-ordinates

        Element zElement = doc.createElement("z");
        waypoints.appendChild(zElement);
        StringBuilder zPositions = new StringBuilder();
        for (Iterator<MapNode> zIterator = RoadMap.networkNodesList.iterator(); zIterator.hasNext();) {
            MapNode node = zIterator.next();
            String ID = String.valueOf(node.z);
            if (zIterator.hasNext()) ID += ";";
            zPositions.append(ID);
        }
        zElement.setTextContent(zPositions.toString());

        // create a child node for all outgoing connections

        Element outElement = doc.createElement("out");
        waypoints.appendChild(outElement);
        StringBuilder outElementString = new StringBuilder();
        for (Iterator<MapNode> roadmapIterator = RoadMap.networkNodesList.iterator(); roadmapIterator.hasNext();) {
            MapNode node = roadmapIterator.next();
            StringBuilder outString = new StringBuilder();
            for (Iterator<MapNode> outgoingIterator = node.outgoing.iterator(); outgoingIterator.hasNext();) {
                MapNode n = outgoingIterator.next();
                String ID = String.valueOf(n.id);
                if (outgoingIterator.hasNext()) ID += ",";
                outString.append(ID);
            }
            if (outString.toString().isEmpty()) {
                outString = new StringBuilder("-1");
            }
            outElementString.append(outString);
            if (roadmapIterator.hasNext()) outElementString.append(";");
        }
        outElement.setTextContent(outElementString.toString());

        // create a child node for all incoming connections

        Element inElement = doc.createElement("in");
        waypoints.appendChild(inElement);
        StringBuilder inElementString = new StringBuilder();
        for (Iterator<MapNode> roadmapIterator = RoadMap.networkNodesList.iterator(); roadmapIterator.hasNext();) {
            MapNode node = roadmapIterator.next();
            StringBuilder inString = new StringBuilder();
            for (Iterator<MapNode> incomingIterator = node.incoming.iterator(); incomingIterator.hasNext();) {
                MapNode n = incomingIterator.next();
                String ID = String.valueOf(n.id);
                if (incomingIterator.hasNext()) ID += ",";
                inString.append(ID);
            }
            if (inString.toString().isEmpty()) {
                inString = new StringBuilder("-1");
            }
            inElementString.append(inString);
            if (roadmapIterator.hasNext()) inElementString.append(";");
        }
        inElement.setTextContent(inElementString.toString());

        // create a child node for all flags

        Element flagsElement = doc.createElement("flags");
        waypoints.appendChild(flagsElement);
        StringBuilder flagElementString = new StringBuilder();
        for (Iterator<MapNode> flagIterator = RoadMap.networkNodesList.iterator(); flagIterator.hasNext();) {
            MapNode node = flagIterator.next();
            String ID = String.valueOf(node.flag);
            if (flagIterator.hasNext()) ID += ";";
            flagElementString.append(ID);
        }
        flagsElement.setTextContent(flagElementString.toString());

        // create a parent node for map markers

        Element markers = doc.createElement("markers");
        root.appendChild(markers);

        // add all map markers to the marker element
        for (MapNode mapNode : RoadMap.networkNodesList) {
            if (mapNode.hasMapMarker()) {
                Element newMapMarker = doc.createElement("m");
                markers.appendChild(newMapMarker);
                newMapMarker.setAttribute("i", String.valueOf(mapNode.id));
                newMapMarker.setAttribute("n", mapNode.getMarkerName());
                newMapMarker.setAttribute("g", mapNode.getMarkerGroup());
            }

        }

        // create a parent node for marker groups

        Element groups = doc.createElement("groups");
        root.appendChild(groups);

        LOG.info("marker groups size = {}", markerGroup.size());

        for (MarkerGroup group : markerGroup) {
            Element newMapMarker = doc.createElement("g");
            groups.appendChild(newMapMarker);
            newMapMarker.setAttribute("i", String.valueOf(group.groupIndex));
            newMapMarker.setAttribute("n", group.groupName);
        }

        TransformerFactory transformerFactory = TransformerFactory.newInstance();
        Transformer transformer = transformerFactory.newTransformer();
        transformer.setOutputProperty(OutputKeys.ENCODING, "UTF-8");
        transformer.setOutputProperty(OutputKeys.INDENT, "yes");
        transformer.setOutputProperty("{http://xml.apache.org/xslt}indent-amount", "4");

        DOMSource source = new DOMSource(doc);
        StreamResult result;

        if (newName == null) {
            result = new StreamResult(file);
        } else {
            File newFile = new File(newName);
            LOG.info("Saving config as {}",newName);
            result = new StreamResult(newFile);
            if (!isAutoSave) {
                xmlConfigFile = newFile;
                editor.setTitle(createWindowTitleString());
            }
        }
        transformer.transform(source, result);

        if (isAutoSave) {
            LOG.info(getLocaleString("console_config_autosave_end"));
        } else if (isBackup) {
            LOG.info(getLocaleString("console_config_backup_end"));
        } else {
            LOG.info(getLocaleString("console_config_save_end"));
        }
    }

    public static LinkedList<Route> getRoutesConfigContents(File routesFile) {
        try {
            if (bDebugLogRouteManager) {
                LOG.info("----------------------------");
                LOG.info("Parsing {}", routesFile.getAbsolutePath());
                LOG.info("----------------------------");
            }
            DocumentBuilderFactory dbFactory = DocumentBuilderFactory.newInstance();
            DocumentBuilder dBuilder = dbFactory.newDocumentBuilder();
            Document doc = dBuilder.parse(routesFile);
            doc.getDocumentElement().normalize();

            if (!doc.getDocumentElement().getNodeName().equals("autoDriveRoutesManager")) {
                LOG.info("Not a route manager Config");
                return null;
            }

            LOG.info("Root element : {}",doc.getDocumentElement().getNodeName());
            NodeList nList = doc.getElementsByTagName("route");
            LOG.info("XML contains info for {} routes", nList.getLength());

            LinkedList<Route> routesList = new LinkedList<>();

            for (int temp = 0; temp < nList.getLength(); temp++) {
                Node nNode = nList.item(temp);
                if (nNode.getNodeType() == Node.ELEMENT_NODE) {
                    Element eElement = (Element) nNode;
                    if (bDebugLogRouteManager) {
                        LOG.info("----------------------------");
                        LOG.info("name : {}", eElement.getAttribute("name"));
                        LOG.info("filename : {}", eElement.getAttribute("fileName"));
                        LOG.info("map : {}", eElement.getAttribute("map"));
                        LOG.info("revision : {}", eElement.getAttribute("revision"));
                        LOG.info("date : {}", eElement.getAttribute("date"));
                        LOG.info("serverId : {}", eElement.getElementsByTagName("serverId").item(0).getTextContent());
                    }
                    routesList.add(new Route(eElement.getAttribute("name"), eElement.getAttribute("fileName"),
                    eElement.getAttribute("map"), Integer.parseInt(eElement.getAttribute("revision")),
                    eElement.getAttribute("date"), eElement.getElementsByTagName("serverId").item(0).getTextContent()));
                }
            }
            return routesList;
        } catch (Exception e) {
            LOG.error("Unable to load routes.xml from - {}", routesFile.getAbsolutePath());
            return null;
        }
    }

    public static class Route {

        public final String name;
        public final String fileName;
        public final String map;
        public final int revision;
        public final String date;
        public final String serverId;

        private Route(String name, String fileName, String map, int revision, String date, String serverId) {
            this.name = name;
            this.fileName = fileName;
            this.map = map;
            this.revision = revision;
            this.date = date;
            this.serverId = serverId;
        }
    }
}
