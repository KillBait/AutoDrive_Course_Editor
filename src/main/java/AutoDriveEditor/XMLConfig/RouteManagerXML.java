package AutoDriveEditor.XMLConfig;

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
import javax.xml.xpath.XPathExpressionException;
import java.io.File;
import java.io.IOException;
import java.util.LinkedList;
import java.util.Objects;
import org.w3c.dom.Document;
import org.w3c.dom.Element;
import org.w3c.dom.Node;
import org.w3c.dom.NodeList;
import org.xml.sax.SAXException;

import AutoDriveEditor.RoadNetwork.MapMarker;
import AutoDriveEditor.RoadNetwork.MapNode;
import AutoDriveEditor.RoadNetwork.MarkerGroup;
import AutoDriveEditor.RoadNetwork.RoadMap;

import static AutoDriveEditor.AutoDriveEditor.*;
import static AutoDriveEditor.GUI.MenuBuilder.*;
import static AutoDriveEditor.Locale.LocaleManager.*;
import static AutoDriveEditor.Managers.ScanManager.scanNetworkForOverlapNodes;
import static AutoDriveEditor.MapPanel.MapImage.*;
import static AutoDriveEditor.MapPanel.MapPanel.*;
import static AutoDriveEditor.Utils.FileUtils.*;
import static AutoDriveEditor.Utils.LoggerUtils.*;
import static AutoDriveEditor.XMLConfig.EditorXML.*;
import static AutoDriveEditor.XMLConfig.GameXML.*;

public class RouteManagerXML {

    public static LinkedList<MarkerGroup> markerGroup = new LinkedList<>();

    public static boolean  loadRouteManagerXML(File fXmlFile, boolean skipRoutesCheck, String mapName) {
        LOG.info("routemanager loadFile: {}", fXmlFile.getAbsolutePath());

        try {
            LOG.info("Parent = {}", fXmlFile.getParentFile().getParent());
            String routeFile = fXmlFile.getParentFile().getParent() + "\\routes.xml";
            LinkedList<Route> routeList = getRoutesConfigContents(new File(routeFile));

            RoadMap roadMap = loadRouteXML(fXmlFile);
            if (roadMap != null) {
                getMapPanel().setRoadMap(roadMap);
                editor.setTitle(AUTODRIVE_COURSE_EDITOR_TITLE + " - " + fXmlFile.getAbsolutePath());
                xmlConfigFile = fXmlFile;
                if (bDebugRouteManager) LOG.info("name = {}", fXmlFile.getName());
                if (!skipRoutesCheck) {
                    if (routeList !=null) {
                        for (Route route : routeList) {
                            if (Objects.equals(route.fileName, fXmlFile.getName())) {
                                LOG.info("setting roadMapName to {}", route.map);
                                roadMap.roadMapName = route.map;
                            }
                        }
                    }
                } else {
                    roadMap.roadMapName = mapName;
                }
                if (bDebugRouteManager) LOG.info("map = {}", roadMap.roadMapName);
                loadMapImage(roadMap.roadMapName);
                forceMapImageRedraw();
                loadHeightMap(fXmlFile);
                saveRoutesXML.setEnabled(true);
                saveConfigMenuItem.setEnabled(false);
                saveConfigAsMenuItem.setEnabled(false);
                scanNetworkForOverlapNodes();
                return true;
            } else {
                JOptionPane.showMessageDialog(editor, localeString.getString("dialog_config_route_unknown"), "AutoDrive", JOptionPane.ERROR_MESSAGE);
                return false;
            }
        } catch (Exception e) {
            LOG.error(e.getMessage(), e);
            JOptionPane.showMessageDialog(editor, localeString.getString("dialog_config_load_route_failed"), "AutoDrive", JOptionPane.ERROR_MESSAGE);
            return false;
        }
    }

    public static void saveRouteManagerXML(String newName, boolean isAutoSave) {
        if (isAutoSave) {
            LOG.info("{}", localeString.getString("console_config_autosave_start"));
        } else {
            LOG.info("{}", localeString.getString("console_config_save_start"));
        }

        try
        {
            if (xmlConfigFile == null) return;
            saveRouteXML(xmlConfigFile, newName, isAutoSave);
            getMapPanel().setStale(false);
            if (!isAutoSave) {
                JOptionPane.showMessageDialog(editor, xmlConfigFile.getName() + " " + localeString.getString("dialog_save_success"), "AutoDrive", JOptionPane.INFORMATION_MESSAGE);
            }
        } catch (Exception e) {
            LOG.error(e.getMessage(), e);
            JOptionPane.showMessageDialog(editor, localeString.getString("dialog_save_fail"), "AutoDrive", JOptionPane.ERROR_MESSAGE);
        }
    }

    public static void autoSaveRouteManagerXML() {
        while (!canAutoSave) {
            try {
                LOG.info("canAutoSave = false --- Waiting");
                Thread.sleep(5000);
            } catch (InterruptedException e) {
                e.printStackTrace();
            }
        }

        String filename = removeExtension(xmlConfigFile.getAbsolutePath()) + "_autosave_" + saveSlot + ".xml";
        File file = new File(filename);
        try {
            if (file.exists()) {
                if (file.isDirectory())
                    throw new IOException("File '" + file + "' is a directory");

                if (!file.canWrite())
                    throw new IOException("File '" + file + "' cannot be written");
            }
            saveRouteManagerXML(filename, true);
            saveSlot++;
            if (saveSlot >= maxAutoSaveSlots + 1 ) saveSlot = 1;
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
        LOG.info("Parsing {}", fXmlFile.getAbsolutePath());

        if (!doc.getDocumentElement().getNodeName().equals("routeExport")) {
            LOG.info("Not an autodrive RoutesManager config");
            return null;
        }

        NodeList waypointsList = doc.getElementsByTagName("waypoints");

        LinkedList<MapNode> nodes = new LinkedList<>();

        for (int temp = 0; temp < waypointsList.getLength(); temp++) {
            LOG.info("----------------------------");
            LOG.info("Root element : {}",doc.getDocumentElement().getNodeName());
            Node nNode = waypointsList.item(temp);
            LOG.info("Current Element :{}", nNode.getNodeName());
            if (nNode.getNodeType() == Node.ELEMENT_NODE) {
                Element eElement = (Element) nNode;
                int waypopintIDs = Integer.parseInt(eElement.getAttribute("c"));
                LOG.info("----------------------------");
                LOG.info("total ID's is {}", waypopintIDs);

                if (waypopintIDs > 0 ) {
                    NodeList nodeList = eElement.getElementsByTagName("x").item(0).getChildNodes();
                    Node node = nodeList.item(0);
                    String xString = node.getNodeValue();
                    String[] xValues = xString.split(";");
                    //LOG.info("x length: {}", xValues.length);

                    nodeList = eElement.getElementsByTagName("y").item(0).getChildNodes();
                    node = nodeList.item(0);
                    String yString = node.getNodeValue();
                    String[] yValues = yString.split(";");
                    //LOG.info("y length: {}", yValues.length);

                    nodeList = eElement.getElementsByTagName("z").item(0).getChildNodes();
                    node = nodeList.item(0);
                    String zString = node.getNodeValue();
                    String[] zValues = zString.split(";");
                    //LOG.info("z length: {}", zValues.length);

                    nodeList = eElement.getElementsByTagName("out").item(0).getChildNodes();
                    node = nodeList.item(0);
                    String outString = node.getNodeValue();
                    String[] outValueArrays = outString.split(";");
                    //LOG.info("out length: {}", outValueArrays.length);

                    nodeList = eElement.getElementsByTagName("in").item(0).getChildNodes();
                    node = nodeList.item(0);
                    String incomingString = node.getNodeValue();
                    String[] inValueArrays = incomingString.split(";");
                    //LOG.info("in length: {}", inValueArrays.length);

                    nodeList = eElement.getElementsByTagName("flags").item(0).getChildNodes();
                    node = nodeList.item(0);
                    String flagsString = node.getNodeValue();
                    String[] flagsValue = flagsString.split(";");
                    //LOG.info("flags length: {}", flagsValue.length);

                    LOG.info("x: {} , y: {}, z: {}, in: {}, out: {}, flags: {}", xValues.length, yValues.length, zValues.length, inValueArrays.length, outValueArrays.length, flagsValue.length);

                    for (int i=0; i<waypopintIDs; i++) {
                        int id = i+1;
                        double x = Double.parseDouble(xValues[i]);
                        double y = Double.parseDouble(yValues[i]);
                        double z = Double.parseDouble(zValues[i]);
                        int flag = Integer.parseInt(flagsValue[i]);
                        MapNode mapNode = new MapNode(id, x, y, z, flag, false, false);
                        nodes.add(mapNode);
                    }

                    for (int i=0; i<waypopintIDs; i++) {
                        MapNode mapNode = nodes.get(i);
                        String[] outNodes = outValueArrays[i].split(",");
                        for (String outNode : outNodes) {
                            if (Integer.parseInt(outNode) != -1) {
                                mapNode.outgoing.add(nodes.get(Integer.parseInt(outNode) - 1));
                            }
                        }
                    }

                    for (int i=0; i<waypopintIDs; i++) {
                        MapNode mapNode = nodes.get(i);
                        String[] incomingNodes = inValueArrays[i].split(",");
                        for (String incomingNode : incomingNodes) {
                            if (Integer.parseInt(incomingNode) != -1) {
                                mapNode.incoming.add(nodes.get(Integer.parseInt(incomingNode)-1));
                            }
                        }
                    }
                }
            }
        }

        NodeList groupList = doc.getElementsByTagName("g");
        markerGroup.clear();
        if (bDebugRouteManager) {
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
                if (bDebugRouteManager) LOG.info("Group {} : index {} , name {}", temp+1, groupId, groupName);
                MarkerGroup group = new MarkerGroup(Integer.parseInt(groupId), groupName);
                markerGroup.add(group);
            }
        }
        if (bDebugRouteManager) LOG.info("markerGroup size {}", markerGroup.size());

        LinkedList<MapMarker> mapMarkers = new LinkedList<>();
        NodeList markerList = doc.getElementsByTagName("m");
        if (bDebugRouteManager) {
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
                if (bDebugRouteManager) LOG.info("Marker {} : ID {} , name {}, group {}", temp+1, markerNodeId, markerName, markerGroup);
                mapMarkers.add(new MapMarker(nodes.get(Integer.parseInt(markerNodeId) -1), markerName, markerGroup));
            }
        }

        RoadMap roadMap = new RoadMap();
        RoadMap.mapNodes = nodes;
        RoadMap.mapMarkers = mapMarkers;

        return roadMap;
    }

    /*private static void saveRouteXML(File file, String newName, boolean isAutoSave) throws ParserConfigurationException, IOException, SAXException, TransformerException, XPathExpressionException {

        DocumentBuilderFactory docFactory = DocumentBuilderFactory.newInstance();
        DocumentBuilder docBuilder = docFactory.newDocumentBuilder();
        Document doc = docBuilder.parse(file);
        Node AutoDrive = doc.getFirstChild();
        Node waypoints = doc.getElementsByTagName("waypoints").item(0);

        Element eElement = (Element) waypoints;
        eElement.setAttribute("c", String.valueOf(RoadMap.mapNodes.size()));

        // loop the staff child node
        NodeList list = waypoints.getChildNodes();

        for (int i = 0; i < list.getLength(); i++) {
            Node node = list.item(i);

            if ("id".equals(node.getNodeName())) {
                StringBuilder ids = new StringBuilder();
                for (int j = 0; j < RoadMap.mapNodes.size(); j++) {
                    MapNode mapNode = RoadMap.mapNodes.get(j);
                    ids.append(mapNode.id);
                    if (j < (RoadMap.mapNodes.size() - 1)) {
                        ids.append(";");
                    }
                }
                node.setTextContent(ids.toString());
            }
            if ("x".equals(node.getNodeName())) {
                StringBuilder xPositions = new StringBuilder();
                for (int j = 0; j < RoadMap.mapNodes.size(); j++) {
                    MapNode mapNode = RoadMap.mapNodes.get(j);
                    xPositions.append(mapNode.x);
                    if (j < (RoadMap.mapNodes.size() - 1)) {
                        xPositions.append(";");
                    }
                }
                node.setTextContent(xPositions.toString());
            }
            if ("y".equals(node.getNodeName())) {
                StringBuilder yPositions = new StringBuilder();
                for (int j = 0; j < RoadMap.mapNodes.size(); j++) {
                    MapNode mapNode = RoadMap.mapNodes.get(j);
                    yPositions.append(mapNode.y);
                    if (j < (RoadMap.mapNodes.size() - 1)) {
                        yPositions.append(";");
                    }
                }
                node.setTextContent(yPositions.toString());
            }
            if ("z".equals(node.getNodeName())) {
                StringBuilder zPositions = new StringBuilder();
                for (int j = 0; j < RoadMap.mapNodes.size(); j++) {
                    MapNode mapNode = RoadMap.mapNodes.get(j);
                    zPositions.append(mapNode.z);
                    if (j < (RoadMap.mapNodes.size() - 1)) {
                        zPositions.append(";");
                    }
                }
                node.setTextContent(zPositions.toString());
            }
            if ("in".equals(node.getNodeName())) {
                StringBuilder incomingString = new StringBuilder();
                for (int j = 0; j < RoadMap.mapNodes.size(); j++) {
                    MapNode mapNode = RoadMap.mapNodes.get(j);
                    StringBuilder incomingsPerNode = new StringBuilder();
                    for (int incomingIndex = 0; incomingIndex < mapNode.incoming.size(); incomingIndex++) {
                        MapNode incomingNode = mapNode.incoming.get(incomingIndex);
                        incomingsPerNode.append(incomingNode.id);
                        if (incomingIndex < (mapNode.incoming.size() - 1)) {
                            incomingsPerNode.append(",");
                        }
                    }
                    if (incomingsPerNode.toString().isEmpty()) {
                        incomingsPerNode = new StringBuilder("-1");
                    }
                    incomingString.append(incomingsPerNode);
                    if (j < (RoadMap.mapNodes.size() - 1)) {
                        incomingString.append(";");
                    }
                }
                node.setTextContent(incomingString.toString());
            }
            if ("out".equals(node.getNodeName())) {
                StringBuilder outgoingString = new StringBuilder();
                for (int j = 0; j < RoadMap.mapNodes.size(); j++) {
                    MapNode mapNode = RoadMap.mapNodes.get(j);
                    StringBuilder outgoingPerNode = new StringBuilder();
                    for (int outgoingIndex = 0; outgoingIndex < mapNode.outgoing.size(); outgoingIndex++) {
                        MapNode outgoingNode = mapNode.outgoing.get(outgoingIndex);
                        outgoingPerNode.append(outgoingNode.id);
                        if (outgoingIndex < (mapNode.outgoing.size() - 1)) {
                            outgoingPerNode.append(",");
                        }
                    }
                    if (outgoingPerNode.toString().isEmpty()) {
                        outgoingPerNode = new StringBuilder("-1");
                    }
                    outgoingString.append(outgoingPerNode);
                    if (j < (RoadMap.mapNodes.size() - 1)) {
                        outgoingString.append(";");
                    }
                }
                node.setTextContent(outgoingString.toString());
            }
            if ("flags".equals(node.getNodeName())) {
                StringBuilder flags = new StringBuilder();
                for (int j = 0; j < RoadMap.mapNodes.size(); j++) {
                    MapNode mapNode = RoadMap.mapNodes.get(j);
                    flags.append(mapNode.flag);
                    if (j < (RoadMap.mapNodes.size() - 1)) {
                        flags.append(";");
                    }
                }
                node.setTextContent(flags.toString());
            }
        }

        // remove all the previous map markers

        NodeList markers = doc.getElementsByTagName("markers");

        for (int markerIndex = 1; markerIndex < RoadMap.mapMarkers.size() + 100; markerIndex++) {
            Element element = (Element) doc.getElementsByTagName("m").item(0);
            if (element != null) {
                Element parent = (Element) element.getParentNode();
                while (parent.hasChildNodes())
                    parent.removeChild(parent.getFirstChild());
            }
        }

        // add current map markers

        if (RoadMap.mapMarkers.size() > 0 && markers.getLength() == 0 ) {
            LOG.info("{}", localeString.getString("console_markers_new"));
            Element test = doc.createElement("markers");
            AutoDrive.appendChild(test);
        }

        NodeList markerList = doc.getElementsByTagName("markers");
        Node markerNode = markerList.item(0);
        //int mapMarkerCount = 1;
        for (MapMarker mapMarker : RoadMap.mapMarkers) {
            Element newMapMarker = doc.createElement("m");
            markerNode.appendChild(newMapMarker);
            newMapMarker.setAttribute("i", String.valueOf(mapMarker.mapNode.id));
            newMapMarker.setAttribute("n", mapMarker.name);
            newMapMarker.setAttribute("g", mapMarker.group);
        }

        // remove all the previous marker groups

        NodeList groupElement = doc.getElementsByTagName("groups");

        if (groupElement.getLength() != 0) {
            NodeList childList = groupElement.item(0).getChildNodes();
            Element parent = (Element) childList.item(0).getParentNode();
            while (parent.hasChildNodes())
                parent.removeChild(parent.getFirstChild());
        }


        //NodeList groupList = doc.getElementsByTagName("groups");
        Node groups = groupElement.item(0);
        //int mapMarkerCount = 1;
        for (int i = 0; i < markerGroup.size(); i++) {
            MarkerGroup group = markerGroup.get(i);
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

        // Clean all the empty whitespaces from XML before save

        XPath xp = XPathFactory.newInstance().newXPath();
        NodeList nl = (NodeList) xp.evaluate("//text()[normalize-space(.)='']", doc, XPathConstants.NODESET);

        for (int i=0; i < nl.getLength(); ++i) {
            Node node = nl.item(i);
            node.getParentNode().removeChild(node);
        }

        // write the content into xml file

        StreamResult result;

        if (newName == null) {
            result = new StreamResult(xmlConfigFile);
        } else {
            File newFile = new File(newName);
            LOG.info("Saving config as {}",newName);
            result = new StreamResult(newFile);
            if (!isAutoSave) {
                xmlConfigFile = newFile;
                editor.setTitle(createTitle());
            }
        }
        transformer.transform(source, result);

        if (isAutoSave) {
            LOG.info("{}", localeString.getString("console_config_autosave_end"));
        } else {
            LOG.info("{}", localeString.getString("console_config_save_end"));
        }


    }*/

    private static void saveRouteXML(File file, String newName, boolean isAutoSave) throws ParserConfigurationException, IOException, SAXException, TransformerException, XPathExpressionException {

        DocumentBuilderFactory docFactory = DocumentBuilderFactory.newInstance();
        DocumentBuilder docBuilder = docFactory.newDocumentBuilder();
        Document doc = docBuilder.newDocument();

        // create the root node

        Element root = doc.createElement("routeExport");
        doc.appendChild(root);

        // create a parent node for the waypoints

        Element waypoints = doc.createElement("waypoints");
        root.appendChild(waypoints);
        waypoints.setAttribute("c", String.valueOf(RoadMap.mapNodes.size()));

        // create a child node for all x co-ordinates

        Element xElement = doc.createElement("x");
        waypoints.appendChild(xElement);
        StringBuilder xPositions = new StringBuilder();
        for (int j = 0; j < RoadMap.mapNodes.size(); j++) {
            MapNode mapNode = RoadMap.mapNodes.get(j);
            xPositions.append(mapNode.x);
            if (j < (RoadMap.mapNodes.size() - 1)) {
                xPositions.append(";");
            }
        }
        xElement.setTextContent(xPositions.toString());

        // create a child node for all y co-ordinates

        Element yElement = doc.createElement("y");
        waypoints.appendChild(yElement);
        StringBuilder yPositions = new StringBuilder();
        for (int j = 0; j < RoadMap.mapNodes.size(); j++) {
            MapNode mapNode = RoadMap.mapNodes.get(j);
            yPositions.append(mapNode.y);
            if (j < (RoadMap.mapNodes.size() - 1)) {
                yPositions.append(";");
            }
        }
        yElement.setTextContent(yPositions.toString());

        // create a child node for all z co-ordinates

        Element zElement = doc.createElement("z");
        waypoints.appendChild(zElement);
        StringBuilder zPositions = new StringBuilder();
        for (int j = 0; j < RoadMap.mapNodes.size(); j++) {
            MapNode mapNode = RoadMap.mapNodes.get(j);
            zPositions.append(mapNode.z);
            if (j < (RoadMap.mapNodes.size() - 1)) {
                zPositions.append(";");
            }
        }
        zElement.setTextContent(zPositions.toString());

        // create a child node for all outgoing connections

        Element outElement = doc.createElement("out");
        waypoints.appendChild(outElement);
        StringBuilder outString = new StringBuilder();
        for (int j = 0; j < RoadMap.mapNodes.size(); j++) {
            MapNode mapNode = RoadMap.mapNodes.get(j);
            StringBuilder outgoingPerNode = new StringBuilder();
            for (int outgoingIndex = 0; outgoingIndex < mapNode.outgoing.size(); outgoingIndex++) {
                MapNode outgoingNode = mapNode.outgoing.get(outgoingIndex);
                outgoingPerNode.append(outgoingNode.id);
                if (outgoingIndex < (mapNode.outgoing.size() - 1)) {
                    outgoingPerNode.append(",");
                }
            }
            if (outgoingPerNode.toString().isEmpty()) {
                outgoingPerNode = new StringBuilder("-1");
            }
            outString.append(outgoingPerNode);
            if (j < (RoadMap.mapNodes.size() - 1)) {
                outString.append(";");
            }
        }
        outElement.setTextContent(outString.toString());

        // create a child node for all incoming connections

        Element inElement = doc.createElement("in");
        waypoints.appendChild(inElement);
        StringBuilder inString = new StringBuilder();
        for (int j = 0; j < RoadMap.mapNodes.size(); j++) {
            MapNode mapNode = RoadMap.mapNodes.get(j);
            StringBuilder incomingsPerNode = new StringBuilder();
            for (int incomingIndex = 0; incomingIndex < mapNode.incoming.size(); incomingIndex++) {
                MapNode incomingNode = mapNode.incoming.get(incomingIndex);
                incomingsPerNode.append(incomingNode.id);
                if (incomingIndex < (mapNode.incoming.size() - 1)) {
                    incomingsPerNode.append(",");
                }
            }
            if (incomingsPerNode.toString().isEmpty()) {
                incomingsPerNode = new StringBuilder("-1");
            }
            inString.append(incomingsPerNode);
            if (j < (RoadMap.mapNodes.size() - 1)) {
                inString.append(";");
            }
        }
        inElement.setTextContent(inString.toString());

        // create a child node for all flags

        Element flagsElement = doc.createElement("flags");
        waypoints.appendChild(flagsElement);
        StringBuilder flags = new StringBuilder();
        for (int j = 0; j < RoadMap.mapNodes.size(); j++) {
            MapNode mapNode = RoadMap.mapNodes.get(j);
            flags.append(mapNode.flag);
            if (j < (RoadMap.mapNodes.size() - 1)) {
                flags.append(";");
            }
        }
        flagsElement.setTextContent(flags.toString());

        // create a parent node for map markers

        Element markers = doc.createElement("markers");
        root.appendChild(markers);

        // add all markers to the markers parent
        for (MapMarker mapMarker : RoadMap.mapMarkers) {
            Element newMapMarker = doc.createElement("m");
            markers.appendChild(newMapMarker);
            newMapMarker.setAttribute("i", String.valueOf(mapMarker.mapNode.id));
            newMapMarker.setAttribute("n", mapMarker.name);
            newMapMarker.setAttribute("g", mapMarker.group);
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
                editor.setTitle(createTitle());
            }
        }
        transformer.transform(source, result);

        if (isAutoSave) {
            LOG.info("{}", localeString.getString("console_config_autosave_end"));
        } else {
            LOG.info("{}", localeString.getString("console_config_save_end"));
        }


    }

    public static LinkedList<Route> getRoutesConfigContents(File routesFile) {
        try {
            if (bDebugRouteManager) {
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
            LOG.info("Total routes = {}", nList.getLength());

            LinkedList<Route> routesList = new LinkedList<>();

            for (int temp = 0; temp < nList.getLength(); temp++) {
                Node nNode = nList.item(temp);
                //LOG.info("Current Element : {}",nNode.getNodeName());

                if (nNode.getNodeType() == Node.ELEMENT_NODE) {
                    Element eElement = (Element) nNode;
                    if (bDebugRouteManager) {
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
            //e.printStackTrace();
            return null;
        }
    }

    /*public static LinkedList<Route> getRoutesXML(File routesFile) {
        try {
            LOG.info("----------------------------");
            LOG.info("Parsing {}", routesFile.getAbsolutePath());
            DocumentBuilderFactory dbFactory = DocumentBuilderFactory.newInstance();
            DocumentBuilder dBuilder = dbFactory.newDocumentBuilder();
            Document doc = dBuilder.parse(routesFile);
            doc.getDocumentElement().normalize();
            LOG.info("----------------------------");
            LOG.info("Root element : {}",doc.getDocumentElement().getNodeName());
            NodeList nList = doc.getElementsByTagName("route");


            LOG.info("Total routes = {}", nList.getLength());

            LinkedList<Route> routesList = new LinkedList<>();

            for (int temp = 0; temp < nList.getLength(); temp++) {
                Node nNode = nList.item(temp);
                //LOG.info("Current Element : {}",nNode.getNodeName());

                if (nNode.getNodeType() == Node.ELEMENT_NODE) {
                    Element eElement = (Element) nNode;
                    LOG.info("----------------------------");
                    LOG.info("name : {}", eElement.getAttribute("name"));
                    LOG.info("filename : {}", eElement.getAttribute("fileName"));
                    LOG.info("map : {}", eElement.getAttribute("map"));
                    LOG.info("revision : {}", eElement.getAttribute("revision"));
                    LOG.info("date : {}", eElement.getAttribute("date"));
                    LOG.info("serverId : {}", eElement.getElementsByTagName("serverId").item(0).getTextContent());
                    routesList.add(new Route(eElement.getAttribute("name"), eElement.getAttribute("fileName"),
                            eElement.getAttribute("map"), Integer.parseInt(eElement.getAttribute("revision")),
                            eElement.getAttribute("date"), eElement.getElementsByTagName("serverId").item(0).getTextContent()));
                }
            }
            return routesList;
        } catch (Exception e) {
            LOG.error("Unable to load routes.xml from - {}", routesFile.getAbsolutePath());
            e.printStackTrace();
            return null;
        }
    }*/

    public static class Route {

        public String name;
        public String fileName;
        public String map;
        public int revision;
        public String date;
        public String serverId;

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
