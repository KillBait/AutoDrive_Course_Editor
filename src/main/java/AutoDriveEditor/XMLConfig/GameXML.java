package AutoDriveEditor.XMLConfig;

import AutoDriveEditor.Classes.Util_Classes.XMLUtils;
import AutoDriveEditor.RoadNetwork.MapNode;
import AutoDriveEditor.RoadNetwork.RoadMap;
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

import static AutoDriveEditor.AutoDriveEditor.*;
import static AutoDriveEditor.Classes.Util_Classes.FileUtils.removeExtension;
import static AutoDriveEditor.Classes.Util_Classes.FileUtils.removeFilenameFromString;
import static AutoDriveEditor.Classes.Util_Classes.LoggerUtils.LOG;
import static AutoDriveEditor.Classes.Util_Classes.XMLUtils.EntryTotalException;
import static AutoDriveEditor.GUI.MapImage.loadHeightMap;
import static AutoDriveEditor.GUI.MapImage.loadMapImage;
import static AutoDriveEditor.GUI.MapPanel.*;
import static AutoDriveEditor.GUI.Menus.DebugMenu.Logging.LogXMLConfigMenu.bDebugLogXMLInfo;
import static AutoDriveEditor.GUI.Menus.EditorMenu.*;
import static AutoDriveEditor.GUI.Menus.FileMenu.RecentFilesMenu.addToRecentFiles;
import static AutoDriveEditor.GUI.Menus.RoutesMenu.OpenRoutesConfig.menu_OpenRoutesConfig;
import static AutoDriveEditor.Locale.LocaleManager.getLocaleString;
import static AutoDriveEditor.Managers.ImportManager.setIsEditorUsingImportedImage;
import static AutoDriveEditor.Managers.MultiSelectManager.clearMultiSelection;
import static AutoDriveEditor.Managers.ScanManager.scanNetworkForOverlapNodes;
import static AutoDriveEditor.RoadNetwork.MapNode.NODE_FLAG_REGULAR;
import static AutoDriveEditor.RoadNetwork.MapNode.createMapNode;
import static AutoDriveEditor.RoadNetwork.RoadMap.*;
import static AutoDriveEditor.XMLConfig.AutoSave.canAutoSave;
import static AutoDriveEditor.XMLConfig.EditorXML.checkStoredMapInfoFor;
import static AutoDriveEditor.XMLConfig.EditorXML.maxAutoSaveSlots;
import static AutoDriveEditor.XMLConfig.GameXML.GameVersion.*;
import static java.lang.Thread.sleep;

public class GameXML {

    public enum GameVersion {FS19_CONFIG, FS22_CONFIG, FS25_CONFIG}

    public static File xmlConfigFile;
    public static String lastUsedLocation;
    private static boolean hasFlagTag = false; // indicates if the loaded XML file has the <flags> tag in the <waypoints> element
    public static boolean canEditConfig = false;
    public static GameVersion configVersion;
    public static int autoSaveLastUsedSlot = 1;
    //private static ModifiedProgressMonitor progressMonitor;

    public static void loadGameConfig(File fXmlFile) {
        LOG.info("config loadFile: {}", fXmlFile.getAbsolutePath());

        try {
            RoadMap roadMap = loadGameXMLFile(fXmlFile);
            if (roadMap != null) {
                configType = CONFIG_SAVEGAME;
                getMapPanel().setRoadMap(roadMap);
                xmlConfigFile = fXmlFile;
                loadMapImage(mapName);
                loadHeightMap(mapName);
                checkStoredMapInfoFor(mapName);
                LOG.info("Session UUID = {}", uuid);
                updateWindowTitle();
                // initialize a new changeManager so undo/redo system won't throw errors
                // when we try to undo/redo something on a config that is no longer loaded
                changeManager.clear();
                //changeManager = new ChangeManager();
                forceMapImageRedraw();
                setIsEditorUsingImportedImage(false);
                saveImageEnabled(false);
                setStale(false);
                scanNetworkForOverlapNodes();
                clearMultiSelection();
                gameXMLSaveEnabled(true);
                routesXMLSaveEnabled(false);
                mapImageMenuEnabled(true);
                heightmapMenuEnabled(true);
                scanMenuEnabled(true);
                menu_OpenRoutesConfig.setEnabled(true);
                buttonManager.enableAllButtons();
                buttonManager.onConfigChange();
                addToRecentFiles(xmlConfigFile.getAbsolutePath(), configType);
            } else {
                JOptionPane.showMessageDialog(editor, getLocaleString("dialog_config_unknown"), "AutoDrive", JOptionPane.ERROR_MESSAGE);
            }
        } catch (Exception e) {
            LOG.error(e.getMessage(), e);
            JOptionPane.showMessageDialog(editor, getLocaleString("dialog_config_load_failed"), "AutoDrive", JOptionPane.ERROR_MESSAGE);
        }
    }

    public static void saveGameConfig(String newName, boolean isAutoSave, boolean isBackup) {
        if (isAutoSave) {
            LOG.info("Starting AutoSave...");
        } else if (isBackup) {
            LOG.info("Starting Backup...");
        } else {
            LOG.info("Starting config save...");
        }

        try
        {
            if (xmlConfigFile == null) return;
            saveGameXMLFile(xmlConfigFile, newName, isAutoSave, isBackup);
            if (!isAutoSave && !isBackup) {
                JOptionPane.showMessageDialog(editor, xmlConfigFile.getName() + " " + getLocaleString("dialog_save_success"), "AutoDrive", JOptionPane.INFORMATION_MESSAGE);
                setStale(false);
            }
        } catch (Exception e) {
            LOG.error(e.getMessage(), e);
            JOptionPane.showMessageDialog(editor, getLocaleString("dialog_save_fail"), "AutoDrive", JOptionPane.ERROR_MESSAGE);
        }
    }

    public static void autoSaveGameConfigFile() {
        while (!canAutoSave()) {
            try {
                LOG.info("AutoSave suspended! --- Waiting 5 secs to try again.");
                //noinspection BusyWait
                sleep(5000);
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
            saveGameConfig(filename, true, false);
            autoSaveLastUsedSlot++;
            if (autoSaveLastUsedSlot == maxAutoSaveSlots + 1 ) autoSaveLastUsedSlot = 1;
        }
        catch(IOException ioEx) {
            ioEx.printStackTrace();
        }
    }

    private static RoadMap loadGameXMLFile(File fXmlFile) throws ParserConfigurationException, IOException, SAXException, XPathExpressionException {
        DocumentBuilderFactory dbFactory = DocumentBuilderFactory.newInstance();
        DocumentBuilder dBuilder = dbFactory.newDocumentBuilder();
        Document doc = dBuilder.parse(fXmlFile);
        doc.getDocumentElement().normalize();

        LOG.info("----------------------------");
        LOG.info("loadGameXMLFile Parsing {}", fXmlFile.getAbsolutePath());

        if (!doc.getDocumentElement().getNodeName().equals("AutoDrive")) {
            LOG.info("Not an AutoDrive Config");
            return null;
        }

        if (XMLUtils.getStringValue(doc.getDocumentElement(), "markerID") != null) {
            String text1 = "This Config format is no longer supported ( Pre AutoDrive v1.1.0.6 ) , reload ingame and save to updateVisibility it!";
            String text2 = "Viewing is only possible, no editing will be allowed.";
            JOptionPane.showConfirmDialog(editor, text1 + "\n\n" + text2, "AutoDrive", JOptionPane.DEFAULT_OPTION, JOptionPane.WARNING_MESSAGE);
            LOG.info("## {}", text1);
            LOG.info("## {}", text2);
            canEditConfig = false;
        } else {
            String version = XMLUtils.getStringValue(doc.getDocumentElement(), "version");
            Semver configSemver = new Semver(version);

            if (configSemver.getMajor() == 1 ) {
                LOG.info("FS19 Config detected");
                configVersion = FS19_CONFIG;
            } else if (configSemver.getMajor() == 2) {
                LOG.info("FS22 Config detected");
                configVersion = FS22_CONFIG;
            } else if (configSemver.getMajor() == 3) {
                LOG.info("FS25 Config detected");
                configVersion = FS25_CONFIG;
            }
            LOG.info("Config was saved by AutoDrive '{}'", version);
            canEditConfig = true;
        }

        // v1.05 Loading speed increase for large nodes networks ( 40,000+ nodes)
        // original line was
        //
        // LinkedList<MapNode> nodes = new LinkedList<>();
        //
        // NOTE: search the web to learn about the difference between O(n) + O(1) access
        //       and why the change has such a large effect
        //
        // Brief explanation..
        //
        // As LinkedList access is O(n), when adding large amounts of nodes and
        // incoming/outgoing connections, the access speed decreases the more
        // we add.
        //
        // An Arraylist does not suffer from this issue due to the pointers to the list
        // being in memory and fast to access, we add everything to a ArrayList and
        // when we add the list to the RoadMap network we cast it back to a LinkedList
        //
        // e.g. I have a config with 75797 nodes and roughly 170,0000 incoming/outgoing connections
        //
        //      v1.04 takes 89.2 seconds to convert the XML into a usable network.
        //
        //      Changing two lines of code to initially createSetting an ArrayList and then cast
        //      it to a LinkedList has a massive effect
        //
        //      v1.05 takes 1.6 seconds to covert the same config to a usable network!!
        //

        int lastGoodID = -1;

        try {
            // get the XML <waypoint> element
            NodeList nList = doc.getElementsByTagName("waypoints");
            // create an arrayList to store all the map nodes
            ArrayList<MapNode> nodes = new ArrayList<>();
            //
            for (int temp = 0; temp < nList.getLength(); temp++) {
                LOG.info("----------------------------");
                LOG.info("{} : {}", getLocaleString("console_root_node"), doc.getDocumentElement().getNodeName());
                Node nNode = nList.item(temp);
                LOG.info("Current Element <{}>", nNode.getNodeName());

                if (nNode.getNodeType() == Node.ELEMENT_NODE) {
                    Element eElement = (Element) nNode;

                    NodeList nodeList = eElement.getElementsByTagName("id").item(0).getChildNodes();
                    Node node = nodeList.item(0);
                    if (node != null) {
                        String idString = node.getNodeValue();
                        String[] ids = idString.split(",");
                        LOG.info("----------------------------");
                        LOG.info("Parsed {} <id> Entries", ids.length);
                        LOG.info("## Checking all <id> entries for the correct numeric sequence");
                        for (int i = 0; i <= ids.length - 1; i++) {
                            if (Integer.parseInt(ids[i]) != i + 1) {
                                LOG.info("## sequence error, Config <id> number {} does not match the expected id of {}", ids[i], i + 1);
                                lastGoodID = i - 1;
                                break;
                                // TODO: add dialog to inform user about the id error and choice to continue loading the config
                                //ids[i] = String.valueOf(i+1);
                            }
                        }
                        //if (lastGoodID == -1) lastGoodID = ids.length;
                        LOG.info("## Finished checking all ID's for the correct sequence");

                        lastGoodID = ids.length;

//                        String[] xValues = eElement.getElementsByTagName("x").item(0).getTextContent().split(",");
                        nodeList = eElement.getElementsByTagName("x").item(0).getChildNodes();
                        node = nodeList.item(0);
                        String xString = node.getNodeValue();
                        String[] xValues = xString.split(",");
                        LOG.info("Parsed {} <x> Entries", xValues.length);
                        if (xValues.length != ids.length)
                            throw new EntryTotalException("<x>", "dialog_config_load_failed_id_length");

//                        String[] yValues = eElement.getElementsByTagName("y").item(0).getTextContent().split(",");
                        nodeList = eElement.getElementsByTagName("y").item(0).getChildNodes();
                        node = nodeList.item(0);
                        String yString = node.getNodeValue();
                        String[] yValues = yString.split(",");
                        LOG.info("Parsed {} <y> Entries", yValues.length);
                        if (yValues.length != ids.length)
                            throw new EntryTotalException("<y>", "dialog_config_load_failed_id_length");


//                        String[] zValues = eElement.getElementsByTagName("z").item(0).getTextContent().split(",");
                        nodeList = eElement.getElementsByTagName("z").item(0).getChildNodes();
                        node = nodeList.item(0);
                        String zString = node.getNodeValue();
                        String[] zValues = zString.split(",");
                        LOG.info("Parsed {} <z> Entries", zValues.length);
                        if (zValues.length != ids.length)
                            throw new EntryTotalException("<z>", "dialog_config_load_failed_id_length");


//                        String[] outValueArrays = eElement.getElementsByTagName("out").item(0).getTextContent().split(";");
                        nodeList = eElement.getElementsByTagName("out").item(0).getChildNodes();
                        node = nodeList.item(0);
                        String outString = node.getNodeValue();
                        String[] outValueArrays = outString.split(";");
                        LOG.info("Parsed {} <out> Entries", outValueArrays.length);
                        if (outValueArrays.length != ids.length)
                            throw new EntryTotalException("<out>", "dialog_config_load_failed_id_length");


//                        String[] incomingValueArrays = eElement.getElementsByTagName("incoming").item(0).getTextContent().split(";");
                        nodeList = eElement.getElementsByTagName("incoming").item(0).getChildNodes();
                        node = nodeList.item(0);
                        String incomingString = node.getNodeValue();
                        String[] incomingValueArrays = incomingString.split(";");
                        LOG.info("Parsed {} <in> Entries", incomingValueArrays.length);
                        if (incomingValueArrays.length != ids.length)
                            throw new EntryTotalException("<in>", "dialog_config_load_failed_id_length");


                        String[] flagsValue;
                        if (eElement.getElementsByTagName("flags").item(0) != null) {
//                            flagsValue = eElement.getElementsByTagName("flags").item(0).getTextContent().split(",");
                            nodeList = eElement.getElementsByTagName("flags").item(0).getChildNodes();
                            node = nodeList.item(0);
                            String flagsString = node.getNodeValue();
                            flagsValue = flagsString.split(",");
                            LOG.info("Parsed {} <flags> Entries", flagsValue.length);
                            hasFlagTag = true;
                        } else {
                            LOG.info("No <flags> Entries found, setting all to normal priority connections");
                            flagsValue = new String[ids.length];
                            Arrays.fill(flagsValue, "0");
                            hasFlagTag = false;
                        }
                        LOG.info("----------------------------");

                        LOG.info("Creating {} MapNodes", ids.length);
                        for (int i = 0; i < lastGoodID; i++) {
                            //int id = Integer.parseInt(ids[i]);
                            int id = i + 1;
                            double x = Double.parseDouble(xValues[i]);
                            double y = Double.parseDouble(yValues[i]);
                            double z = Double.parseDouble(zValues[i]);
                            int flag = Integer.parseInt(flagsValue[i]);

                            // is this a FS22 AutoDrive config
                            if (configVersion == GameVersion.FS22_CONFIG || configVersion == GameVersion.FS25_CONFIG) {
                                // check if a nodes flag values is equal 2 or 4, this means it was autogenerated by AutoDrive from the map splines
                                if (flag == 2 || flag == 4) {
                                    // reset the flag to 0, the editor will just see it as a CONNECTION_REGULAR in checks
                                    flag = NODE_FLAG_REGULAR;
                                }
                            }

                            MapNode mapNode = createMapNode(id, x, y, z, flag, false);
                            nodes.add(mapNode);
                        }
                        LOG.info("Finished creating all map nodes");

                        LOG.info("Creating all incoming/outgoing connections");
                        int conCount = 0;
                        for (int i = 0; i < lastGoodID; i++) {
                            MapNode mapNode = nodes.get(i);
                            String[] outNodes = outValueArrays[i].split(",");
                            for (String outNode : outNodes) {
                                if (Integer.parseInt(outNode) != -1 && Integer.parseInt(outNode) <= lastGoodID) {
                                    mapNode.outgoing.add(nodes.get(Integer.parseInt(outNode) - 1));
                                    conCount++;
                                }
                            }
                        }
                        LOG.info("Created {} outgoing connections", conCount);
                        conCount = 0;
                        for (int i = 0; i < lastGoodID; i++) {
                            MapNode mapNode = nodes.get(i);
                            String[] incomingNodes = incomingValueArrays[i].split(",");
                            for (String incomingNode : incomingNodes) {
                                if (Integer.parseInt(incomingNode) != -1 && Integer.parseInt(incomingNode) <= lastGoodID) {
                                    mapNode.incoming.add(nodes.get(Integer.parseInt(incomingNode) - 1));
                                    conCount++;
                                }
                            }
                        }
                        LOG.info("Created {} incoming connections", conCount);
                        LOG.info("----------------------------");
                    }
                }
            }

            // EXPERIMENTAL CODE
            HashMap<Integer, List<String>> vehicleParkingMap = loadVehiclesXMLParking(fXmlFile);
            LOG.info("---------------------------------");
            // END EXPERIMENTAL CODE

            NodeList markerList = doc.getElementsByTagName("mapmarker");

            for (int temp = 0; temp < markerList.getLength(); temp++) {
                Node markerNode = markerList.item(temp);
                if (markerNode.getNodeType() == Node.ELEMENT_NODE) {
                    Element eElement = (Element) markerNode;

                    NodeList idNodeList = eElement.getElementsByTagName("id");
                    NodeList nameNodeList = eElement.getElementsByTagName("name");
                    NodeList groupNodeList = eElement.getElementsByTagName("group");

                    LOG.info("Starting Creation of {} Markers", idNodeList.getLength());

                    for (int markerIndex = 0; markerIndex<idNodeList.getLength(); markerIndex++ ) {
                        Node node = idNodeList.item(markerIndex).getChildNodes().item(0);
                        String markerNodeId = node.getNodeValue();

                        node = nameNodeList.item(markerIndex).getChildNodes().item(0);
                        String markerName = node.getNodeValue();

                        node = groupNodeList.item(markerIndex).getChildNodes().item(0);
                        String markerGroup = node.getNodeValue();

                        // AD 6.0.0.4 config fix for Node ID's being Long Format
                        float num = Float.parseFloat(markerNodeId);
                        int id = (int) num;

                        // EXPERIMENTAL CODE
                        // Add any vehicles using marker as parking destination
                        Integer markerId = markerIndex + 1;
                        List<String> markerVehiclesParked = vehicleParkingMap.get(markerId);
                        // END EXPERIMENTAL CODE

                        // add the marker info to the node
                        MapNode mapNode = nodes.get(id - 1);
                        mapNode.createMapMarker(markerName, markerGroup, markerId, markerVehiclesParked);

                        if (bDebugLogXMLInfo) LOG.info("created marker - index {} ( ID {} ) , name '{}' , group '{}' , marker id {} , Parked Vehicles {}", id - 1, id, markerName, markerGroup, markerId, markerVehiclesParked);
                        //if (bDebugLogConfigInfo) LOG.info("created marker - index {} ( ID {} ) , name {} , group {}", id-1, id, markerName, markerGroup);
                    }
                }
            }

            LOG.info("Finished creating all map markers");
            LOG.info("---------------------------------");

            RoadMap roadMap = new RoadMap();
            setRoadMapNodes(roadMap, new LinkedList<>(nodes));

            // check for MapName element

            NodeList mapNameNode = doc.getElementsByTagName("MapName");
            Element mapNameElement = (Element) mapNameNode.item(0);
            if ( mapNameElement != null) {
                NodeList fstNm = mapNameElement.getChildNodes();
                String mapName = (fstNm.item(0)).getNodeValue();
                LOG.info("Config parsing completed.");
                RoadMap.mapName = mapName;
            }
            return roadMap;
        } catch (EntryTotalException e) {
            LOG.info("## Exception during config load ## " + e.getErrorValue() + " total entries does not match the <id> total entries");
            JOptionPane.showMessageDialog(editor,getLocaleString("dialog_config_load_failed") + "\n\n" + e.getErrorValue() + " " + getLocaleString(e.getErrorMessage()), "AutoDrive Editor", JOptionPane.ERROR_MESSAGE);
            return null;
        }
    }

    private static void saveGameXMLFile(File file, String newName, boolean isAutoSave, boolean isBackup) throws ParserConfigurationException, IOException, SAXException, TransformerException, XPathExpressionException {

        DocumentBuilderFactory docFactory = DocumentBuilderFactory.newInstance();
        DocumentBuilder docBuilder = docFactory.newDocumentBuilder();
        Document doc = docBuilder.parse(file);
        Node AutoDrive = doc.getFirstChild();
        Node waypoints = doc.getElementsByTagName("waypoints").item(0);
        int totalMarkers = 0;

        // If no <flags> tag was detected on config load, createSetting it

        if (!hasFlagTag) {
            Element flagTag = doc.createElement("flags");
            waypoints.appendChild(flagTag);
        }

        // loop the staff child node

        NodeList list = waypoints.getChildNodes();

        for (int i = 0; i < list.getLength(); i++) {
            Node waypointNode = list.item(i);

            if ("id".equals(waypointNode.getNodeName())) {
                int a = 1;
                StringBuilder ids = new StringBuilder();
                for (Iterator<MapNode> idIterator = networkNodesList.iterator(); idIterator.hasNext();) {
                    MapNode node = idIterator.next();
                    String ID = String.valueOf(node.id);
                    if (idIterator.hasNext()) ID += ",";
                    ids.append(ID);
                    // check if each mapNode has a mapMarker and increment totalMarkers if it has one
                    // NOTE: TotalMarkers is used later on in creating the map markers entries.
                    if (node.hasMapMarker()) totalMarkers++;
                    if (node.id != a) LOG.info("Found");
                    a++;
                }
                waypointNode.setTextContent(ids.toString());
            }

            if ("x".equals(waypointNode.getNodeName())) {
                StringBuilder xPositions = new StringBuilder();
                for (Iterator<MapNode> xIterator = networkNodesList.iterator(); xIterator.hasNext();) {
                    MapNode node = xIterator.next();
                    String ID = String.valueOf(node.x);
                    if (xIterator.hasNext()) ID += ",";
                    xPositions.append(ID);
                }
                waypointNode.setTextContent(xPositions.toString());
            }

            if ("y".equals(waypointNode.getNodeName())) {
                StringBuilder yPositions = new StringBuilder();
                for (Iterator<MapNode> yIterator = networkNodesList.iterator(); yIterator.hasNext();) {
                    MapNode node = yIterator.next();
                    String ID = String.valueOf(node.y);
                    if (yIterator.hasNext()) ID += ",";
                    yPositions.append(ID);
                }
                waypointNode.setTextContent(yPositions.toString());
            }

            if ("z".equals(waypointNode.getNodeName())) {
                StringBuilder zPositions = new StringBuilder();
                for (Iterator<MapNode> zIterator = networkNodesList.iterator(); zIterator.hasNext();) {
                    MapNode node = zIterator.next();
                    String ID = String.valueOf(node.z);
                    if (zIterator.hasNext()) ID += ",";
                    zPositions.append(ID);
                }
                waypointNode.setTextContent(zPositions.toString());
            }

            if ("incoming".equals(waypointNode.getNodeName())) {
                StringBuilder incomingString = new StringBuilder();
                for (Iterator<MapNode> inIterator = networkNodesList.iterator(); inIterator.hasNext();) {
                    MapNode node = inIterator.next();
                    StringBuilder nodeIncomingString = new StringBuilder();
                    for (Iterator<MapNode> iter = node.incoming.iterator(); iter.hasNext();) {
                        MapNode n = iter.next();
                        String ID = String.valueOf(n.id);
                        if (iter.hasNext()) ID += ",";
                        nodeIncomingString.append(ID);
                    }
                    if (nodeIncomingString.toString().isEmpty()) {
                        nodeIncomingString = new StringBuilder("-1");
                    }
                    incomingString.append(nodeIncomingString);
                    if (inIterator.hasNext()) incomingString.append(";");
                }
                waypointNode.setTextContent(incomingString.toString());
            }

            if ("out".equals(waypointNode.getNodeName())) {
                StringBuilder outgoingString = new StringBuilder();
                for (Iterator<MapNode> outIterator = networkNodesList.iterator(); outIterator.hasNext();) {
                    MapNode node = outIterator.next();
                    StringBuilder nodeOutgoingString = new StringBuilder();
                    for (Iterator<MapNode> outgoingIterator = node.outgoing.iterator(); outgoingIterator.hasNext();) {
                        MapNode n = outgoingIterator.next();
                        String ID = String.valueOf(n.id);
                        if (outgoingIterator.hasNext()) ID += ",";
                        nodeOutgoingString.append(ID);
                    }
                    if (nodeOutgoingString.toString().isEmpty()) {
                        nodeOutgoingString = new StringBuilder("-1");
                    }
                    outgoingString.append(nodeOutgoingString);
                    if (outIterator.hasNext()) outgoingString.append(";");
                }
                waypointNode.setTextContent(outgoingString.toString());
            }

            if ("flags".equals(waypointNode.getNodeName())) {
                StringBuilder flags = new StringBuilder();
                for (Iterator<MapNode> flagIterator = networkNodesList.iterator(); flagIterator.hasNext();) {
                    MapNode node = flagIterator.next();
                    String ID = String.valueOf(node.flag);
                    if (flagIterator.hasNext()) ID += ",";
                    flags.append(ID);
                }
                waypointNode.setTextContent(flags.toString());
            }
        }

        // removeOriginalNodes all the existing map markers, so we can save an upto date list

        for (int markerIndex = 1; markerIndex < totalMarkers + 100; markerIndex++) {
            Element element = (Element) doc.getElementsByTagName("mm" + (markerIndex)).item(0);
            if (element != null) {
                Element parent = (Element) element.getParentNode();
                while (parent.hasChildNodes())
                    parent.removeChild(parent.getFirstChild());
            }
        }

        // Check if the mapmarker key exists in the XML, if it doesn't exist and totalMarkers > 1
        // we need to createSetting the <mapmarker> key or else we will get an exception thrown..

        NodeList testWaypoints = doc.getElementsByTagName("mapmarker");

        if (totalMarkers > 0 && testWaypoints.getLength() == 0 ) {
            LOG.info("New map markers to save, but no <mapmarker> tag in loaded XML.. creating tag for output file");
            Element test = doc.createElement("mapmarker");
            AutoDrive.appendChild(test);
        }

        NodeList markerList = doc.getElementsByTagName("mapmarker");
        Node markerNode = markerList.item(0);

        // EXPERIMENTAL CODE
        HashMap<String, Integer> parkDestinations = new HashMap<>();
        // END EXPERIMENTAL CODE

        int mapMarkerCount = 1;
        for (MapNode mapNode : networkNodesList) {
            if (mapNode.hasMapMarker()) {
                Element newMarkerElement = doc.createElement("mm" + mapMarkerCount);

                Element markerID = doc.createElement("id");
                markerID.appendChild(doc.createTextNode("" + mapNode.id));
                newMarkerElement.appendChild(markerID);

                Element markerName = doc.createElement("name");
                markerName.appendChild(doc.createTextNode(mapNode.getMarkerName()));
                newMarkerElement.appendChild(markerName);

                Element markerGroup = doc.createElement("group");
                markerGroup.appendChild(doc.createTextNode(mapNode.getMarkerGroup()));
                newMarkerElement.appendChild(markerGroup);

                // EXPERIMENTAL CODE
                // store maker id & vehicle ID for parkDestination in vehicles.xml
                if (mapNode.isParkDestination()) {
                    List<String> parkedVehiclesList = mapNode.getParkedVehiclesList();
                    // createSetting entry for each vehicle
                    for (String vehicleId : parkedVehiclesList) {
                        parkDestinations.put(vehicleId, mapMarkerCount);
                    }
                }
                // END EXPERIMENTAL CODE

                markerNode.appendChild(newMarkerElement);
                mapMarkerCount += 1;
            }

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
            if (!isAutoSave && !isBackup) {
                xmlConfigFile = newFile;
                editor.setTitle(createWindowTitleString());
            }
        }
        transformer.transform(source, result);

        if (isAutoSave) {
            LOG.info("AutoSave completed...");
        } else if (isBackup) {
            LOG.info("Backup completed...");
        } else {
            LOG.info("Save config completed...");
        }

        //EXPERIMENTAL CODE
        // Update vehicles.xml with current marker id ==> parkDestination
        // NOTE: We only do this when the original AD GameConfig is saved (i.e. not for backups or autosave)
        if (!isAutoSave && !isBackup) {
            saveVehiclesXMLParking(xmlConfigFile, parkDestinations);
            LOG.info("---------------------------------");
        }
        // END EXPERIMENTAL CODE
    }

    // EXPERIMENTAL CODE
    /**
     * Reads the vehicles.xml file from the same location as the configuration XML and returns a HashMap with the
     * parkDestination (corresponds to the marker ID - i.e. the number after "mm" in the marker tag) as key and
     * the vehicle ID configured to use this parking destination.
     *
     * @param gameXmlFile AutoDrive configuration XML file
     * @return HashMap with parkDestination as key and a list Vehicle IDs as value
     * @throws ParserConfigurationException XML Parser Exceptions
     * @throws IOException                  (XML) File I/O exception
     * @throws SAXException                 SAX error or warning
     * @throws XPathExpressionException     Error in XPath expression
     */
    private static HashMap<Integer, List<String>> loadVehiclesXMLParking(File gameXmlFile) throws ParserConfigurationException, IOException, SAXException, XPathExpressionException {
        String methodLogPrefix = "Load Parking Destinations - ";

        File vehicleXMLPath = new File(removeFilenameFromString(gameXmlFile.toString()) + "vehicles.xml");
        HashMap<Integer, List<String>> vehicleParkingMap = new HashMap<>();

        LOG.info("xml path: {} ( {} )", vehicleXMLPath, vehicleXMLPath.exists());

        if (!vehicleXMLPath.exists()) {
            LOG.warn(methodLogPrefix + "vehicles.xml not found in {} - exiting.", vehicleXMLPath.getAbsolutePath());
            return vehicleParkingMap;
        }

        LOG.info(methodLogPrefix + "Parsing {}", vehicleXMLPath.getAbsolutePath());

        // Build Doc from XML
        DocumentBuilderFactory dbf = DocumentBuilderFactory.newInstance();
        DocumentBuilder db = dbf.newDocumentBuilder();
        Document doc = db.parse(vehicleXMLPath);
        doc.getDocumentElement().normalize();

        // Locate vehicles with park destination using XPath
        NodeList vehicleParking;
        XPath xPath = XPathFactory.newInstance().newXPath();

        if (configVersion == FS19_CONFIG) {
            vehicleParking = (NodeList) xPath.evaluate("//vehicle[./FS19_AutoDrive/AutoDriveVehicleData[@parkDestination]]", doc, XPathConstants.NODESET);
        } else {
            vehicleParking = (NodeList) xPath.evaluate("//vehicle[./AutoDrive[@parkDestination]]", doc, XPathConstants.NODESET);
        }

        // Add vehicleId assigned for each marker
        for (int idx = 0; idx < vehicleParking.getLength(); idx++) {
            @SuppressWarnings("WrapperTypeMayBePrimitive")
            Integer parkDestinationMarkerId;
            if (configVersion == FS19_CONFIG) {
                parkDestinationMarkerId = Integer.valueOf(((Element) vehicleParking.item(idx)).getElementsByTagName("AutoDriveVehicleData").item(0).getAttributes().getNamedItem("parkDestination").getNodeValue());
            } else {
                parkDestinationMarkerId = Integer.valueOf(((Element) vehicleParking.item(idx)).getElementsByTagName("AutoDrive").item(0).getAttributes().getNamedItem("parkDestination").getNodeValue());

            }
            String vehicleId = String.valueOf(vehicleParking.item(idx).getAttributes().getNamedItem("id").getNodeValue());

            // check for existing vehicle list
            List<String> vehicleList = vehicleParkingMap.get(parkDestinationMarkerId);
            if (vehicleList == null)
                vehicleList = new ArrayList<>();

            vehicleList.add(vehicleId);
            vehicleParkingMap.put(parkDestinationMarkerId, vehicleList);

            if (bDebugLogXMLInfo) LOG.info(methodLogPrefix + "vehicle ID {} parks at marker ID {}", vehicleId, parkDestinationMarkerId);
        }
        LOG.info(methodLogPrefix + "Loaded {} Parking Destinations", vehicleParking.getLength());

        return vehicleParkingMap;
    }

    /**
     * Saves the vehicles.xml file, if found in the same location as the configuration XML
     *
     * @param gameXmlFile      AutoDrive configuration XML file
     * @param parkDestinations HashMap with Vehicle ID as key and parkDestination (sequence index of marker in gameXmlFile) as value
     * @throws ParserConfigurationException XML Parser Exceptions
     * @throws IOException                  (XML) File I/O exception
     * @throws SAXException                 SAX error or warning
     * @throws XPathExpressionException     Error in XPath expression
     * @throws TransformerException         Error creating XML tree for saving
     */
    private static void saveVehiclesXMLParking(File gameXmlFile, HashMap<String, Integer> parkDestinations) throws ParserConfigurationException, IOException, SAXException, XPathExpressionException, TransformerException {
        String methodLogPrefix = "Save Parking Destinations - ";

        File vehicleXMLFile = new File(removeFilenameFromString(gameXmlFile.toString()) + "vehicles.xml");

        // Check if saving applicable
        if (parkDestinations.isEmpty()) {
            LOG.info(methodLogPrefix + "No Parking Destinations defined.");
            return;
        } else if (!vehicleXMLFile.exists()) {
            LOG.warn(methodLogPrefix + "vehicle.xml not found in {}. Skipping.", vehicleXMLFile.getAbsolutePath());
            return;
        }

        LOG.info(methodLogPrefix + "Saving to {}", vehicleXMLFile.getAbsolutePath());

        // Build Doc from XML
        DocumentBuilderFactory dbf = DocumentBuilderFactory.newInstance();
        DocumentBuilder db = dbf.newDocumentBuilder();
        Document doc = db.parse(vehicleXMLFile);

        // Locate vehicles with park destination using XPath
        XPath xPath = XPathFactory.newInstance().newXPath();

        //Remove all parkDestinations first
        NodeList vehiclesWithParking;
        if (configVersion == FS19_CONFIG) {
            vehiclesWithParking = (NodeList) xPath.evaluate("//vehicle[./FS19_AutoDrive/AutoDriveVehicleData[@parkDestination]]", doc, XPathConstants.NODESET);
        } else {
            vehiclesWithParking = (NodeList) xPath.evaluate("//vehicle[./AutoDrive[@parkDestination]]", doc, XPathConstants.NODESET);
        }

        for (int idx = 0; idx < vehiclesWithParking.getLength(); idx++) {
            Element autoDriveElement = (Element) vehiclesWithParking.item(idx);
            autoDriveElement.removeAttribute("parkDestination");
        }

        // Add current parkDestinations
        for (Map.Entry<String, Integer> entry : parkDestinations.entrySet()) {
            String xPathToVehicle = String.format("//vehicle[@id = '%s']", entry.getKey());
            Element vehicleElement = (Element) xPath.evaluate(xPathToVehicle, doc, XPathConstants.NODE);
            Element autodriveElement;// = (Element) vehicleElement.getElementsByTagName("AutoDrive").item(0);
            if (configVersion == FS19_CONFIG) {
                autodriveElement = (Element) vehicleElement.getElementsByTagName("AutoDriveVehicleData").item(0);
            } else {
                autodriveElement = (Element) vehicleElement.getElementsByTagName("AutoDrive").item(0);
            }
            autodriveElement.setAttribute("parkDestination", String.valueOf(entry.getValue()));

            if (bDebugLogXMLInfo)
                LOG.info("{}vehicle ID {} parks at marker ID {}",methodLogPrefix,  entry.getKey(), entry.getValue());
        }

        // Clean all the empty whitespaces from XML before save
        NodeList nl = (NodeList) xPath.evaluate("//text()[normalize-space(.)='']", doc, XPathConstants.NODESET);
        for (int i = 0; i < nl.getLength(); ++i) {
            Node node = nl.item(i);
            node.getParentNode().removeChild(node);
        }

        // save vehicles.xml
        TransformerFactory transformerFactory = TransformerFactory.newInstance();
        Transformer transformer = transformerFactory.newTransformer();
        transformer.setOutputProperty(OutputKeys.ENCODING, "UTF-8");
        transformer.setOutputProperty(OutputKeys.INDENT, "yes");
        transformer.setOutputProperty("{http://xml.apache.org/xslt}indent-amount", "2");

        transformer.transform(new DOMSource(doc), new StreamResult(vehicleXMLFile));

        LOG.info("{}Updated {} Parking Destinations",methodLogPrefix, parkDestinations.size());
    }
    // END EXPERIMENTAL CODE
}
