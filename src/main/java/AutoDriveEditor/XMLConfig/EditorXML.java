package AutoDriveEditor.XMLConfig;

import org.w3c.dom.*;
import org.xml.sax.SAXException;

import javax.xml.parsers.DocumentBuilder;
import javax.xml.parsers.DocumentBuilderFactory;
import javax.xml.parsers.ParserConfigurationException;
import javax.xml.transform.*;
import javax.xml.transform.dom.DOMSource;
import javax.xml.transform.stream.StreamResult;
import java.io.FileOutputStream;
import java.io.IOException;
import java.util.ArrayList;

import static AutoDriveEditor.AutoDriveEditor.COURSE_EDITOR_VERSION;
import static AutoDriveEditor.AutoDriveEditor.editor;
import static AutoDriveEditor.Locale.LocaleManager.localeString;
import static AutoDriveEditor.Utils.LoggerUtils.LOG;
import static AutoDriveEditor.Utils.XMLUtils.*;
import static AutoDriveEditor.XMLConfig.GameXML.autoSaveLastUsedSlot;
import static AutoDriveEditor.XMLConfig.GameXML.lastLoadLocation;

public class EditorXML {
    
    // main options

    public static String lastRunVersion;
    public static boolean bShowUpdateMessage = true;
    public static boolean bUseOnlineMapImages = true;
    public static int x = -99; // x + y are negative on purpose
    public static int y = -99;
    public static int width = 1024;
    public static int height = 768;
    public static boolean noSavedWindowPosition;
    
    // Map panel default options

    public static int maxZoomLevel = 30;
    public static double nodeSize = 2;

    // curve panel default options
    
    public static int curveSliderMax = 50;
    public static int curveSliderDefault = 10;
    public static int controlPointMoveScaler = 3;
    
    // Linear line default options

    public static int linearLineNodeDistance = 12;
    
    // options menu default options

    public static boolean bContinuousConnections = false; 
    public static boolean bMiddleMouseMove = false;
    
    // Grid menu default options
    
    public static boolean bGridSnapSubs;
    public static boolean bGridSnap;
    public static boolean bShowGrid = false;
    public static double gridSpacingX = 2;
    public static double gridSpacingY = 2;
    public static int gridSubDivisions = 4;
    public static int rotationAngle = 5;
    
    // Autosave default options
    
    public static boolean bAutoSaveEnabled = true;
    public static int autoSaveInterval = 10;
    public static int maxAutoSaveSlots = 10;

    // Map size storage

    public static ArrayList<MapZoomStore> mapZoomStore  = new ArrayList<>();

    // Toolbar position default

    public static String toolbarPosition = "Left";


    public static class MapZoomStore {
        public String mapName;
        public int zoomFactor;

        public MapZoomStore(String mapName, int zoomFactor) {
            this.mapName = mapName;
            this.zoomFactor = zoomFactor;
        }
    }

    public static void loadEditorXMLConfig() {
        DocumentBuilderFactory docFactory = DocumentBuilderFactory.newInstance();
        try {
            DocumentBuilder docBuilder = docFactory.newDocumentBuilder();
            Document doc = docBuilder.parse("EditorConfig.xml");
            Element e = doc.getDocumentElement();

            lastRunVersion = getTextValue(lastRunVersion, e, "Version");
            bShowUpdateMessage = getBooleanValue(bShowUpdateMessage, e, "ShowUpdateMessage");
            bAutoSaveEnabled = getBooleanValue(bAutoSaveEnabled, e, "AutoSave_Enabled");
            autoSaveInterval = getIntegerValue(autoSaveInterval, e, "AutoSave_Interval");
            maxAutoSaveSlots = getIntegerValue(maxAutoSaveSlots, e, "AutoSave_Slots");
            autoSaveLastUsedSlot = getIntegerValue(autoSaveLastUsedSlot, e, "AutoSave_Last_Used_Slot");
            lastLoadLocation = getTextValue(lastLoadLocation, e, "LastUsedLocation");
            if (autoSaveLastUsedSlot > maxAutoSaveSlots)  autoSaveLastUsedSlot = maxAutoSaveSlots;

            x = getIntegerValue(x, e, "WindowX");
            y = getIntegerValue(y, e, "WindowY");
            if ( x == -99 || y == -99) noSavedWindowPosition = true;
            width = getIntegerValue(width, e, "WindowWidth");
            height = getIntegerValue(height, e, "WindowHeight");
            toolbarPosition = getTextValue(toolbarPosition, e, "Toolbar_Position");
            maxZoomLevel = getIntegerValue(maxZoomLevel, e, "MaxZoomLevel");
            nodeSize = getFloatValue((float)nodeSize, e, "NodeSizeScale");

            bUseOnlineMapImages = getBooleanValue(bUseOnlineMapImages, e, "Check_Online_MapImages");
            bContinuousConnections = getBooleanValue(bContinuousConnections, e, "Continuous_Connection");
            bMiddleMouseMove = getBooleanValue(bMiddleMouseMove, e, "MiddleMouseMove");
            linearLineNodeDistance = getIntegerValue(linearLineNodeDistance, e, "LinearLineNodeDistance");
            curveSliderMax = getIntegerValue(curveSliderMax, e, "CurveSliderMaximum");
            curveSliderDefault = getIntegerValue(curveSliderDefault, e, "CurveSliderDefault");
            if (curveSliderDefault > curveSliderMax) curveSliderDefault = curveSliderMax;
            controlPointMoveScaler = getIntegerValue(controlPointMoveScaler, e, "ControlPointMoveScaler");
            bShowGrid = getBooleanValue(bShowGrid, e, "ShowGrid");
            bGridSnap = getBooleanValue(bGridSnap, e, "GridSnapping");
            gridSpacingX = getFloatValue((float)gridSpacingX, e, "GridSpacingX");
            gridSpacingY = getFloatValue((float)gridSpacingY, e, "GridSpacingY");
            bGridSnapSubs = getBooleanValue(bGridSnapSubs, e, "SnapSubDivision");
            gridSubDivisions = getIntegerValue(gridSubDivisions, e, "GridSubDivisions");
            rotationAngle = getIntegerValue( rotationAngle, e, "RotationStep");


            NodeList zoomFactorList = doc.getElementsByTagName("mapzoomfactor");

            for (int temp = 0; temp < zoomFactorList.getLength(); temp++) {
                Node zoomNode = zoomFactorList.item(temp);
                if (zoomNode.getNodeType() == Node.ELEMENT_NODE) {
                    Element eElement = (Element) zoomNode;

                    //NodeList mapID = eElement.getElementsByTagName("Map" + temp + 1);
                    NodeList mapName = eElement.getElementsByTagName("name");
                    NodeList mapZoomfactor = eElement.getElementsByTagName("zoomfactor");

                    for (int mapNameIndex = 0; mapNameIndex<mapName.getLength(); mapNameIndex++ ) {
                        Node node = mapName.item(mapNameIndex).getChildNodes().item(0);
                        String markerName = node.getNodeValue();

                        node = mapZoomfactor.item(mapNameIndex).getChildNodes().item(0);
                        String mapZoomInt = node.getNodeValue();

                        mapZoomStore.add(new MapZoomStore(markerName, Integer.parseInt(mapZoomInt)));
                    }
                }
            }

        } catch (ParserConfigurationException | SAXException pce) {
            LOG.error("## Exception in loading Editor config ## SAX/Parser Exception");
            System.out.println(pce.getMessage());
        } catch (IOException ioe) {
            LOG.warn(localeString.getString("console_editor_config_load_not_found"));
            noSavedWindowPosition = true;
        }
    }

    public static void saveEditorXMLConfig() {
        DocumentBuilderFactory docFactory = DocumentBuilderFactory.newInstance();
        try {
            DocumentBuilder docBuilder = docFactory.newDocumentBuilder();
            Document doc = docBuilder.newDocument();
            Element root = doc.createElement("EditorConfig");

            setTextValue("Version", doc, COURSE_EDITOR_VERSION, root);
            setBooleanValue("ShowUpdateMessage", doc, bShowUpdateMessage, root);
            setBooleanValue("AutoSave_Enabled", doc, bAutoSaveEnabled, root);
            setIntegerValue("AutoSave_Interval", doc, autoSaveInterval, root);
            setIntegerValue("AutoSave_Slots", doc, maxAutoSaveSlots, root);
            setIntegerValue("AutoSave_Last_Used_Slot", doc, autoSaveLastUsedSlot, root);
            setTextValue("LastUsedLocation", doc, lastLoadLocation, root);
            setIntegerValue("WindowX", doc, editor.getBounds().x, root);
            setIntegerValue("WindowY", doc, editor.getBounds().y, root);
            setIntegerValue("WindowWidth", doc, editor.getBounds().width, root);
            setIntegerValue("WindowHeight", doc, editor.getBounds().height, root);
            setTextValue("Toolbar_Position", doc, toolbarPosition, root);
            setIntegerValue( "MaxZoomLevel", doc, maxZoomLevel, root);
            setFloatValue("NodeSizeScale", doc, (float)nodeSize, root);
            setBooleanValue("Check_Online_MapImages", doc, bUseOnlineMapImages, root);
            setBooleanValue("Continuous_Connection", doc, bContinuousConnections, root);
            setBooleanValue("MiddleMouseMove", doc, bMiddleMouseMove, root);
            setIntegerValue("LinearLineNodeDistance", doc, linearLineNodeDistance, root);
            setIntegerValue("CurveSliderMaximum", doc, curveSliderMax, root);
            setIntegerValue("CurveSliderDefault", doc, curveSliderDefault, root);
            if (curveSliderDefault > curveSliderMax) curveSliderDefault = curveSliderMax;
            setIntegerValue("ControlPointMoveScaler", doc, controlPointMoveScaler, root);
            setBooleanValue("ShowGrid", doc, bShowGrid, root);
            setBooleanValue("GridSnapping", doc, bGridSnap, root);
            setFloatValue("GridSpacingX", doc, (float)gridSpacingX, root);
            setFloatValue("GridSpacingY", doc, (float)gridSpacingY, root);
            setBooleanValue("SnapSubDivision",doc, bGridSnapSubs, root);
            setIntegerValue("GridSubDivisions", doc, gridSubDivisions, root);
            setIntegerValue("RotationStep", doc, rotationAngle, root);

            doc.appendChild(root);

            // remove all the previous map zoom factor entries from loaded XML

            for (int zoomStoreIndex = 1; zoomStoreIndex < mapZoomStore.size(); zoomStoreIndex++) {
                Element element = (Element) doc.getElementsByTagName("mapzoomfactor" + (zoomStoreIndex)).item(0);
                if (element != null) {
                    Element parent = (Element) element.getParentNode();
                    while (parent.hasChildNodes())
                        parent.removeChild(parent.getFirstChild());
                }
            }

            // check if the map zoom factor key exists

            NodeList zoomList = doc.getElementsByTagName("mapzoomfactor");

            // if <mapzoomfactor> doesn't exist, create it

            if (mapZoomStore.size() > 0 && zoomList.getLength() == 0 ) {
                Element test = doc.createElement("mapzoomfactor");
                root.appendChild(test);
            }

            // add the stored entries, makes sure the list upto date

            NodeList markerList = doc.getElementsByTagName("mapzoomfactor");
            Node zoomNode = markerList.item(0);
            int zoomFactorCount = 1;
            for (MapZoomStore zoomMarker : mapZoomStore) {
                Element newMapMarker = doc.createElement("Map" + zoomFactorCount);

                Element markerID = doc.createElement("name");
                markerID.appendChild(doc.createTextNode("" + zoomMarker.mapName));
                newMapMarker.appendChild(markerID);

                Element markerName = doc.createElement("zoomfactor");
                markerName.appendChild(doc.createTextNode(String.valueOf(zoomMarker.zoomFactor)));
                newMapMarker.appendChild(markerName);

                zoomNode.appendChild(newMapMarker);
                zoomFactorCount += 1;
            }

            try {
                Transformer transformer = TransformerFactory.newInstance().newTransformer();
                transformer.setOutputProperty(OutputKeys.INDENT, "yes");
                transformer.setOutputProperty(OutputKeys.METHOD, "xml");
                transformer.setOutputProperty(OutputKeys.ENCODING, "UTF-8");
                transformer.setOutputProperty("{http://xml.apache.org/xslt}indent-amount", "4");

                DOMSource source = new DOMSource(doc);
                StreamResult result;

                try {
                    result = new StreamResult(new FileOutputStream("EditorConfig.xml"));
                    transformer.transform(source, result);
                    LOG.info("{}", localeString.getString("console_editor_config_save_end"));
                } catch (IOException ioe) {
                    LOG.error( localeString.getString("console_editor_config_save_error"));
                }
            } catch (TransformerFactoryConfigurationError | TransformerException | IllegalArgumentException transformerFactoryConfigurationError) {
                LOG.error("## Exception in saving Editor config ## Transformer Exception ##");
                transformerFactoryConfigurationError.printStackTrace();
            }
        } catch (ParserConfigurationException | DOMException e) {
            LOG.error("## Exception in saving Editor config ## SAX/Parser Exception ##");
            e.printStackTrace();
        }
    }
}
