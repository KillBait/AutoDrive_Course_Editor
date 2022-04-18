package AutoDriveEditor.XMLConfig;

import com.vdurmont.semver4j.Semver;
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
import javax.xml.xpath.XPath;
import javax.xml.xpath.XPathConstants;
import javax.xml.xpath.XPathExpressionException;
import javax.xml.xpath.XPathFactory;
import java.io.File;
import java.io.IOException;
import java.util.*;

import AutoDriveEditor.RoadNetwork.MapMarker;
import AutoDriveEditor.RoadNetwork.MapNode;
import AutoDriveEditor.RoadNetwork.RoadMap;

import static AutoDriveEditor.AutoDriveEditor.*;
import static AutoDriveEditor.GUI.MenuBuilder.saveRoutesXML;
import static AutoDriveEditor.Locale.LocaleManager.*;
import static AutoDriveEditor.MapPanel.MapImage.*;
import static AutoDriveEditor.MapPanel.MapPanel.*;
import static AutoDriveEditor.Utils.FileUtils.*;
import static AutoDriveEditor.Utils.LoggerUtils.*;
import static AutoDriveEditor.Utils.XMLUtils.*;
import static AutoDriveEditor.XMLConfig.EditorXML.*;

public class GameXML {

    public static int FS19_CONFIG = 1;
    public static int FS22_CONFIG = 2;

    public static File xmlConfigFile;
    public static String lastLoadLocation;
    private static boolean hasFlagTag = false; // indicates if the loaded XML file has the <flags> tag in the <waypoints> element
    public static boolean oldConfigFormat = false;
    public static int configVersion = 0;
    public static int saveSlot = 1;

    public static boolean loadConfigFile(File fXmlFile) {
        LOG.info("config loadFile: {}", fXmlFile.getAbsolutePath());

        try {
            RoadMap roadMap = loadXmlConfigFile(fXmlFile);
            if (roadMap != null) {
                getMapPanel().setRoadMap(roadMap);
                editor.setTitle(AUTODRIVE_COURSE_EDITOR_TITLE + " - " + fXmlFile.getAbsolutePath());
                xmlConfigFile = fXmlFile;
                loadMapImage(roadMap.roadMapName);
                forceMapImageRedraw();
                loadHeightMap(fXmlFile, false);
                saveRoutesXML.setEnabled(false);
                return true;
            } else {
                JOptionPane.showMessageDialog(editor, localeString.getString("dialog_config_unknown"), "AutoDrive", JOptionPane.ERROR_MESSAGE);
                return false;
            }
        } catch (Exception e) {
            LOG.error(e.getMessage(), e);
            JOptionPane.showMessageDialog(editor, localeString.getString("dialog_config_load_failed"), "AutoDrive", JOptionPane.ERROR_MESSAGE);
            return false;
        }
    }

    public static boolean saveConfigFile(String newName, boolean isAutoSave) {
        if (isAutoSave) {
            LOG.info("{}", localeString.getString("console_config_autosave_start"));
        } else {
            LOG.info("{}", localeString.getString("console_config_save_start"));
        }

        try
        {
            if (xmlConfigFile == null) return false;
            saveXmlConfig(xmlConfigFile, newName, isAutoSave);
            getMapPanel().setStale(false);
            if (!isAutoSave) {
                JOptionPane.showMessageDialog(editor, xmlConfigFile.getName() + " " + localeString.getString("dialog_save_success"), "AutoDrive", JOptionPane.INFORMATION_MESSAGE);
            }
            return true;
        } catch (Exception e) {
            LOG.error(e.getMessage(), e);
            JOptionPane.showMessageDialog(editor, localeString.getString("dialog_save_fail"), "AutoDrive", JOptionPane.ERROR_MESSAGE);
            return false;
        }
    }

    public static void saveMergeBackupConfigFile() {
        LOG.info("{}", localeString.getString("console_config_merge_backup"));
        String filename = removeExtension(xmlConfigFile.getAbsolutePath()) + "_mergeBackup.xml";
        saveConfigFile(filename, true);

    }

    public static void autoSaveGameConfigFile() {
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
            saveConfigFile(filename, true);
            //LOG.info("{}", filename);
            saveSlot++;
            if (saveSlot == maxAutoSaveSlots + 1 ) saveSlot = 1;
        }
        catch(IOException ioEx) {
            ioEx.printStackTrace();
        }
    }

    private static RoadMap loadXmlConfigFile(File fXmlFile) throws ParserConfigurationException, IOException, SAXException {
        DocumentBuilderFactory dbFactory = DocumentBuilderFactory.newInstance();
        DocumentBuilder dBuilder = dbFactory.newDocumentBuilder();
        Document doc = dBuilder.parse(fXmlFile);
        doc.getDocumentElement().normalize();

        if (!doc.getDocumentElement().getNodeName().equals("AutoDrive")) {
            LOG.info("Not an autodrive Config");
            return null;
        }

        LOG.info("{} :{}", localeString.getString("console_root_node"), doc.getDocumentElement().getNodeName());

        if (getTextValue(null, doc.getDocumentElement(), "markerID") != null) {
            JOptionPane.showConfirmDialog(editor, "" + localeString.getString("console_config_unsupported1") + "\n\n" + localeString.getString("console_config_unsupported2"), "AutoDrive", JOptionPane.DEFAULT_OPTION, JOptionPane.WARNING_MESSAGE);
            LOG.info("## {}",localeString.getString("console_config_unsupported1"));
            LOG.info("## {}",localeString.getString("console_config_unsupported2"));
            oldConfigFormat = true;
        } else {
            String version = getTextValue(null, doc.getDocumentElement(), "version");
            Semver configSemver = new Semver(version);
            //version = "1.1.1.6";//Semver configSemver = new Semver(version);

            if (configSemver.getMajor() == 1 ) {
                LOG.info("FS19 Config detected");
                configVersion = FS19_CONFIG;
            } else if (configSemver.getMajor() == 2) {
                LOG.info("FS22 Config detected");
                configVersion = FS22_CONFIG;
            }
            LOG.info("{} '{}'", localeString.getString("console_config_version"), version);
            oldConfigFormat = false;
        }

        NodeList markerList = doc.getElementsByTagName("mapmarker");
        LinkedList<MapMarker> mapMarkers = new LinkedList<>();

        TreeMap<Integer, MapMarker> mapMarkerTree = new TreeMap<>();
        for (int temp = 0; temp < markerList.getLength(); temp++) {
            Node markerNode = markerList.item(temp);
            if (markerNode.getNodeType() == Node.ELEMENT_NODE) {
                Element eElement = (Element) markerNode;

                NodeList idNodeList = eElement.getElementsByTagName("id");
                NodeList nameNodeList = eElement.getElementsByTagName("name");
                NodeList groupNodeList = eElement.getElementsByTagName("group");

                for (int markerIndex = 0; markerIndex<idNodeList.getLength(); markerIndex++ ) {
                    Node node = idNodeList.item(markerIndex).getChildNodes().item(0);
                    String markerNodeId = node.getNodeValue();

                    node = nameNodeList.item(markerIndex).getChildNodes().item(0);
                    String markerName = node.getNodeValue();

                    node = groupNodeList.item(markerIndex).getChildNodes().item(0);
                    String markerGroup = node.getNodeValue();

                    MapNode dummyNode = new MapNode((int)Double.parseDouble(markerNodeId), 0, 0, 0, 0, false, false);
                    MapMarker mapMarker = new MapMarker(dummyNode, markerName, markerGroup);
                    mapMarkerTree.put((int)Double.parseDouble(markerNodeId), mapMarker);
                }
            }
        }

        NodeList nList = doc.getElementsByTagName("waypoints");

        LinkedList<MapNode> nodes = new LinkedList<>();
        for (int temp = 0; temp < nList.getLength(); temp++) {

            Node nNode = nList.item(temp);

            LOG.info("Current Element :{}", nNode.getNodeName());

            if (nNode.getNodeType() == Node.ELEMENT_NODE) {
                Element eElement = (Element) nNode;

                NodeList nodeList = eElement.getElementsByTagName("id").item(0).getChildNodes();
                Node node = nodeList.item(0);
                if ( node !=null ) {
                    String idString = node.getNodeValue();
                    String[] ids = idString.split(",");

                    nodeList = eElement.getElementsByTagName("x").item(0).getChildNodes();
                    node = nodeList.item(0);
                    String xString = node.getNodeValue();
                    String[] xValues = xString.split(",");

                    nodeList = eElement.getElementsByTagName("y").item(0).getChildNodes();
                    node = nodeList.item(0);
                    String yString = node.getNodeValue();
                    String[] yValues = yString.split(",");

                    nodeList = eElement.getElementsByTagName("z").item(0).getChildNodes();
                    node = nodeList.item(0);
                    String zString = node.getNodeValue();
                    String[] zValues = zString.split(",");

                    nodeList = eElement.getElementsByTagName("out").item(0).getChildNodes();
                    node = nodeList.item(0);
                    String outString = node.getNodeValue();
                    String[] outValueArrays = outString.split(";");

                    nodeList = eElement.getElementsByTagName("incoming").item(0).getChildNodes();
                    node = nodeList.item(0);
                    String incomingString = node.getNodeValue();
                    String[] incomingValueArrays = incomingString.split(";");

                    if (eElement.getElementsByTagName("flags").item(0) != null ) {
                        nodeList = eElement.getElementsByTagName("flags").item(0).getChildNodes();
                        node = nodeList.item(0);
                        String flagsString = node.getNodeValue();
                        String[] flagsValue = flagsString.split(",");
                        hasFlagTag = true;

                        for (int i=0; i<ids.length; i++) {
                            int id = Integer.parseInt(ids[i]);
                            double x = Double.parseDouble(xValues[i]);
                            double y = Double.parseDouble(yValues[i]);
                            double z = Double.parseDouble(zValues[i]);
                            int flag = Integer.parseInt(flagsValue[i]);
                            // is this a FS22 AutoDrive config
                            if (configVersion == FS22_CONFIG) {
                                // check if a nodes flag values is equal 2 or 4, this means it was autogenerated by AutoDrive from the map splines
                                if (flag == 2 || flag == 4) {
                                    // reset the flag to 0, the editor will just see it as a CONNECTION_REGULAR in checks
                                    flag = 0;
                                }
                            }

                            MapNode mapNode = new MapNode(id, x, y, z, flag, false, false);
                            nodes.add(mapNode);
                        }
                    } else {
                        hasFlagTag = false;
                        for (int i=0; i<ids.length; i++) {
                            int id = Integer.parseInt(ids[i]);
                            double x = Double.parseDouble(xValues[i]);
                            double y = Double.parseDouble(yValues[i]);
                            double z = Double.parseDouble(zValues[i]);
                            int flag = 0;

                            MapNode mapNode = new MapNode(id, x, y, z, flag, false, false);
                            nodes.add(mapNode);
                        }
                    }

                    for (Map.Entry<Integer, MapMarker> entry : mapMarkerTree.entrySet())
                    {
                        mapMarkers.add(new MapMarker(nodes.get(entry.getKey()-1), entry.getValue().name, entry.getValue().group));
                    }

                    for (int i=0; i<ids.length; i++) {
                        MapNode mapNode = nodes.get(i);
                        String[] outNodes = outValueArrays[i].split(",");
                        for (String outNode : outNodes) {
                            if (Integer.parseInt(outNode) != -1) {
                                mapNode.outgoing.add(nodes.get(Integer.parseInt(outNode) - 1));
                            }
                        }
                    }

                    for (int i=0; i<ids.length; i++) {
                        MapNode mapNode = nodes.get(i);
                        String[] incomingNodes = incomingValueArrays[i].split(",");
                        for (String incomingNode : incomingNodes) {
                            if (Integer.parseInt(incomingNode) != -1) {
                                mapNode.incoming.add(nodes.get(Integer.parseInt(incomingNode)-1));
                            }
                        }
                    }
                }
            }
        }

        RoadMap roadMap = new RoadMap();
        RoadMap.mapNodes = nodes;
        RoadMap.mapMarkers = mapMarkers;

        // check for MapName element

        NodeList mapNameNode = doc.getElementsByTagName("MapName");
        Element mapNameElement = (Element) mapNameNode.item(0);
        if ( mapNameElement != null) {
            NodeList fstNm = mapNameElement.getChildNodes();
            String mapName = (fstNm.item(0)).getNodeValue();
            LOG.info("{} : {}", localeString.getString("console_config_load"), mapName);
            roadMap.roadMapName = mapName;
        }
        LOG.info("{}", localeString.getString("console_config_load_end"));

        return roadMap;
    }

    private static void saveXmlConfig(File file, String newName, boolean isAutoSave) throws ParserConfigurationException, IOException, SAXException, TransformerException, XPathExpressionException {

        DocumentBuilderFactory docFactory = DocumentBuilderFactory.newInstance();
        DocumentBuilder docBuilder = docFactory.newDocumentBuilder();
        Document doc = docBuilder.parse(file);
        Node AutoDrive = doc.getFirstChild();
        Node waypoints = doc.getElementsByTagName("waypoints").item(0);

        // If no <flags> tag was detected on config load, create it

        if (!hasFlagTag) {
            Element flagtag = doc.createElement("flags");
            waypoints.appendChild(flagtag);
        }



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
                        ids.append(",");
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
                        xPositions.append(",");
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
                        yPositions.append(",");
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
                        zPositions.append(",");
                    }
                }
                node.setTextContent(zPositions.toString());
            }
            if ("incoming".equals(node.getNodeName())) {
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
                        flags.append(",");
                    }
                }
                node.setTextContent(flags.toString());
            }
        }



        for (int markerIndex = 1; markerIndex < RoadMap.mapMarkers.size() + 100; markerIndex++) {
            Element element = (Element) doc.getElementsByTagName("mm" + (markerIndex)).item(0);
            if (element != null) {
                Element parent = (Element) element.getParentNode();
                while (parent.hasChildNodes())
                    parent.removeChild(parent.getFirstChild());
            }
        }


        NodeList testwaypoints = doc.getElementsByTagName("mapmarker");

        if (RoadMap.mapMarkers.size() > 0 && testwaypoints.getLength() == 0 ) {
            LOG.info("{}", localeString.getString("console_markers_new"));
            Element test = doc.createElement("mapmarker");
            AutoDrive.appendChild(test);
        }

        NodeList markerList = doc.getElementsByTagName("mapmarker");
        Node markerNode = markerList.item(0);
        int mapMarkerCount = 1;
        for (MapMarker mapMarker : RoadMap.mapMarkers) {
            Element newMapMarker = doc.createElement("mm" + mapMarkerCount);

            Element markerID = doc.createElement("id");
            markerID.appendChild(doc.createTextNode("" + mapMarker.mapNode.id));
            newMapMarker.appendChild(markerID);

            Element markerName = doc.createElement("name");
            markerName.appendChild(doc.createTextNode(mapMarker.name));
            newMapMarker.appendChild(markerName);

            Element markerGroup = doc.createElement("group");
            markerGroup.appendChild(doc.createTextNode(mapMarker.group));
            newMapMarker.appendChild(markerGroup);

            markerNode.appendChild(newMapMarker);
            mapMarkerCount += 1;
        }


        TransformerFactory transformerFactory = TransformerFactory.newInstance();
        Transformer transformer = transformerFactory.newTransformer();
        transformer.setOutputProperty(OutputKeys.ENCODING, "UTF-8");
        transformer.setOutputProperty(OutputKeys.INDENT, "yes");
        transformer.setOutputProperty("{http://xml.apache.org/xslt}indent-amount", "2");

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


    }
}
