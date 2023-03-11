package AutoDriveEditor.XMLConfig;

import org.w3c.dom.*;
import org.xml.sax.SAXException;

import javax.xml.parsers.DocumentBuilder;
import javax.xml.parsers.DocumentBuilderFactory;
import javax.xml.parsers.ParserConfigurationException;
import javax.xml.transform.*;
import javax.xml.transform.dom.DOMSource;
import javax.xml.transform.stream.StreamResult;
import java.awt.*;
import java.io.FileOutputStream;
import java.io.IOException;
import java.util.ArrayList;

import static AutoDriveEditor.AutoDriveEditor.COURSE_EDITOR_VERSION;
import static AutoDriveEditor.AutoDriveEditor.editor;
import static AutoDriveEditor.GUI.MenuBuilder.bDebugLogConfigInfo;
import static AutoDriveEditor.Locale.LocaleManager.getLocaleString;
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
    public static boolean bNoSavedWindowPosition;

    public static boolean bShowSelectionBounds = false;
    
    // Map panel default options

    public static int maxZoomLevel = 30;
    public static float nodeSize = 2;

    // curve panel default options
    
    public static int curveSliderMax = 50;
    public static int curveSliderDefault = 10;
    public static int controlPointMoveScaler = 3;
    
    // Linear line default options

    public static Boolean bFilledArrows = true;
    public static Boolean bCreateLinearLineEndNode = false;
    public static int linearLineNodeDistance = 12;

    
    // options menu default options

    public static boolean bContinuousConnections = false; 
    public static boolean bMiddleMouseMove = false;
    public static boolean bLockToolbarPosition = false;
    
    // Grid menu default options
    
    public static boolean bGridSnapSubs = false;
    public static boolean bGridSnap = false;
    public static boolean bShowGrid = false;
    public static float gridSpacingX = 2;
    public static float gridSpacingY = 2;
    public static int gridSubDivisions = 4;
    public static int rotationSnap = 5;

    // Autosave default options
    
    public static boolean bAutoSaveEnabled = true;
    public static int autoSaveInterval = 10;
    public static int maxAutoSaveSlots = 10;

    // Map size storage

    public static ArrayList<MapInfoStore> knownMapList = new ArrayList<>();

    // Toolbar position default

    public static String toolbarPosition = "Left";

    // Node and Connection colours

    public static final Color BROWN = new Color(152, 104, 50 );

    public static Color colourNodeRegular = Color.RED;
    public static Color colourNodeSubprio = Color.ORANGE;
    public static Color colourNodeSelected = Color.WHITE;
    public static Color colourNodeControl = Color.MAGENTA;
    public static Color colourConnectRegular = Color.GREEN;
    public static Color colourConnectSubprio = Color.ORANGE;
    public static Color colourConnectDual = Color.BLUE;
    public static Color colourConnectDualSubprio = BROWN;
    public static Color colourConnectReverse = Color.CYAN;
    public static Color colourConnectReverseSubprio = Color.CYAN;



    public static class MapInfoStore {
        public String mapName;
        public int zoomFactor;
        public float nodeSize;

        public MapInfoStore(String mapName, int zoomFactor, float nodeSize) {
            this.mapName = mapName;
            this.zoomFactor = zoomFactor;
            this.nodeSize = nodeSize;
        }
    }

    public static void loadEditorXMLConfig() {
        DocumentBuilderFactory docFactory = DocumentBuilderFactory.newInstance();
        try {
            DocumentBuilder docBuilder = docFactory.newDocumentBuilder();
            Document doc = docBuilder.parse("EditorConfig.xml");
            Element rootElement = doc.getDocumentElement();

            lastRunVersion = getTextValue(lastRunVersion, rootElement, "Version");
            bShowUpdateMessage = getBooleanValue(bShowUpdateMessage, rootElement, "ShowUpdateMessage");
            bAutoSaveEnabled = getBooleanValue(bAutoSaveEnabled, rootElement, "AutoSave_Enabled");
            autoSaveInterval = getIntegerValue(autoSaveInterval, rootElement, "AutoSave_Interval");
            maxAutoSaveSlots = getIntegerValue(maxAutoSaveSlots, rootElement, "AutoSave_Slots");
            autoSaveLastUsedSlot = getIntegerValue(autoSaveLastUsedSlot, rootElement, "AutoSave_Last_Used_Slot");
            lastLoadLocation = getTextValue(lastLoadLocation, rootElement, "LastUsedLocation");
            if (autoSaveLastUsedSlot > maxAutoSaveSlots)  autoSaveLastUsedSlot = maxAutoSaveSlots;

            x = getIntegerValue(x, rootElement, "WindowX");
            y = getIntegerValue(y, rootElement, "WindowY");
            if ( x == -99 || y == -99) bNoSavedWindowPosition = true;
            width = getIntegerValue(width, rootElement, "WindowWidth");
            height = getIntegerValue(height, rootElement, "WindowHeight");
            toolbarPosition = getTextValue(toolbarPosition, rootElement, "Toolbar_Position");
            maxZoomLevel = getIntegerValue(maxZoomLevel, rootElement, "MaxZoomLevel");
            nodeSize = getFloatValue(rootElement, "NodeSizeScale", nodeSize);

            bUseOnlineMapImages = getBooleanValue(bUseOnlineMapImages, rootElement, "Check_Online_MapImages");
            bContinuousConnections = getBooleanValue(bContinuousConnections, rootElement, "Continuous_Connection");
            bMiddleMouseMove = getBooleanValue(bMiddleMouseMove, rootElement, "MiddleMouseMove");
            bLockToolbarPosition = getBooleanValue(bLockToolbarPosition, rootElement, "LockToolbar");
            curveSliderMax = getIntegerValue(curveSliderMax, rootElement, "CurveSliderMaximum");
            curveSliderDefault = getIntegerValue(curveSliderDefault, rootElement, "CurveSliderDefault");
            if (curveSliderDefault > curveSliderMax) curveSliderDefault = curveSliderMax;
            controlPointMoveScaler = getIntegerValue(controlPointMoveScaler, rootElement, "ControlPointMoveScaler");
            bShowGrid = getBooleanValue(bShowGrid, rootElement, "ShowGrid");
            bGridSnap = getBooleanValue(bGridSnap, rootElement, "GridSnapping");
            gridSpacingX = getFloatValue(rootElement, "GridSpacingX", gridSpacingX);
            gridSpacingY = getFloatValue(rootElement, "GridSpacingY", gridSpacingY);
            bGridSnapSubs = getBooleanValue(bGridSnapSubs, rootElement, "SnapSubDivision");
            gridSubDivisions = getIntegerValue(gridSubDivisions, rootElement, "GridSubDivisions");
            rotationSnap = getIntegerValue(rotationSnap, rootElement, "RotationStep");
            bFilledArrows = getBooleanValue(bFilledArrows, rootElement, "FilledConnectionArrows");
            bCreateLinearLineEndNode = getBooleanValue(bCreateLinearLineEndNode, rootElement, "CreateLinearLineEndNode");
            linearLineNodeDistance = getIntegerValue(linearLineNodeDistance, rootElement, "LinearLineNodeDistance");

            colourNodeRegular = getColorValue(rootElement, "Colour_Node_Regular", Color.RED);
            colourNodeSubprio = getColorValue(rootElement, "Colour_Node_Subprio", Color.ORANGE);
            colourNodeSelected = getColorValue(rootElement, "Colour_Node_Selected", Color.WHITE);
            colourNodeControl = getColorValue(rootElement, "Colour_Node_Control", Color.MAGENTA);
            colourConnectRegular = getColorValue(rootElement, "Colour_Connection_Regular", Color.GREEN);
            colourConnectSubprio = getColorValue(rootElement, "Colour_Connection_Subprio", Color.ORANGE);
            colourConnectDual = getColorValue(rootElement, "Colour_Connection_Dual", Color.BLUE);
            colourConnectDualSubprio = getColorValue(rootElement, "Colour_Connection_Dual_Subprio", new Color(150,100,50)); // Color.BROWN
            colourConnectReverse = getColorValue(rootElement, "Colour_Connection_Reverse", Color.CYAN);
            colourConnectReverseSubprio = getColorValue(rootElement, "Colour_Connection_Reverse_Subprio", Color.CYAN);


            NodeList knownMapsList;

            LOG.info("Checking for depreciated <mapzoomfactors> key in EditorConfig.xml");

            if (doc.getElementsByTagName("mapzoomfactor").getLength() != 0 ) {
                LOG.info("  -> detected old <mapzoomfactors> key, old format will be replaced with new format on next config save");
                knownMapsList = doc.getElementsByTagName("mapzoomfactor");
                for (int temp = 0; temp < knownMapsList.getLength(); temp++) {
                    Node knownMapsNode = knownMapsList.item(temp);
                    if (knownMapsNode.getNodeType() == Node.ELEMENT_NODE) {

                        Element eElement = (Element) knownMapsNode;

                        NodeList mapNameElement = eElement.getElementsByTagName("name");
                        NodeList mapZoomFactorElement = eElement.getElementsByTagName("zoomfactor");

                        String mapName;
                        String mapZoomInt;
                        String mapNodeFloat;

                        for (int mapNameIndex = 0; mapNameIndex<mapNameElement.getLength(); mapNameIndex++ ) {
                            Node node = mapNameElement.item(mapNameIndex).getChildNodes().item(0);
                            mapName = node.getNodeValue();

                            node = mapZoomFactorElement.item(mapNameIndex).getChildNodes().item(0);
                            mapZoomInt = node.getNodeValue();

                            // Work around for old configs to avoid fatal NPE, if the <nodesize> key is not found then set the
                            // node size to the default ( 2.0 ), when the config is next saved all entries will have it set

                            // TODO specifying a direct float is not ideal, find a cleaner way?

                            mapNodeFloat = String.valueOf(2f);
                            knownMapList.add(new MapInfoStore(mapName, Integer.parseInt(mapZoomInt), Float.parseFloat(mapNodeFloat)));
                        }
                    }
                }
            } else if (doc.getElementsByTagName("KnownMapSettings").getLength() != 0 ) {
                LOG.info("<KnownMapSettings> key exists, EditorConfig.xml format is upto date");
                LOG.info("  --> Parsing {} known maps for editor use", doc.getElementsByTagName("Map").getLength());

                NodeList mapList = doc.getElementsByTagName("KnownMapSettings");

                for (int temp = 0; temp < mapList.getLength(); temp++) {
                    Node mapListNode = mapList.item(temp);
                    if (mapListNode.getNodeType() == Node.ELEMENT_NODE) {
                        knownMapsList = doc.getElementsByTagName("Map");
                        for (int temp2 = 0; temp2 < knownMapsList.getLength(); temp2++) {
                            Node knownMapsNode = knownMapsList.item(temp2);
                            Element eElement = (Element) knownMapsNode;
                            String mapName = eElement.getAttribute("Name");

                            // get Maps Scale

                            NodeList nodeList = eElement.getElementsByTagName("MapScale").item(0).getChildNodes();
                            Node node = nodeList.item(0);
                            String mapScale = node.getNodeValue();

                            // get Maps Scale

                            nodeList = eElement.getElementsByTagName("NodeSize").item(0).getChildNodes();
                            node = nodeList.item(0);
                            String nodeSize = node.getNodeValue();

                            if (bDebugLogConfigInfo) LOG.info("Map name = {}, Scale = {}, NodeSize = {}", mapName, mapScale, nodeSize);
                            knownMapList.add(new MapInfoStore(mapName, Integer.parseInt(mapScale), Float.parseFloat(nodeSize)));
                        }
                    }
                }
            }
        } catch (ParserConfigurationException | SAXException pce) {
            LOG.error("## Exception in loading Editor config ## SAX/Parser Exception");
            System.out.println(pce.getMessage());
        } catch (IOException ioe) {
            LOG.warn(getLocaleString("console_editor_config_load_not_found"));
            bNoSavedWindowPosition = true;
        }
    }

    public static void saveEditorXMLConfig() {
        DocumentBuilderFactory docFactory = DocumentBuilderFactory.newInstance();
        try {
            DocumentBuilder docBuilder = docFactory.newDocumentBuilder();
            Document doc = docBuilder.newDocument();
            Element rootElement = doc.createElement("EditorConfig");

            setTextValue("Version", doc, COURSE_EDITOR_VERSION, rootElement);
            setBooleanValue("ShowUpdateMessage", doc, bShowUpdateMessage, rootElement);
            setBooleanValue("AutoSave_Enabled", doc, bAutoSaveEnabled, rootElement);
            setIntegerValue("AutoSave_Interval", doc, autoSaveInterval, rootElement);
            setIntegerValue("AutoSave_Slots", doc, maxAutoSaveSlots, rootElement);
            setIntegerValue("AutoSave_Last_Used_Slot", doc, autoSaveLastUsedSlot, rootElement);
            setTextValue("LastUsedLocation", doc, lastLoadLocation, rootElement);
            setIntegerValue("WindowX", doc, editor.getBounds().x, rootElement);
            setIntegerValue("WindowY", doc, editor.getBounds().y, rootElement);
            setIntegerValue("WindowWidth", doc, editor.getBounds().width, rootElement);
            setIntegerValue("WindowHeight", doc, editor.getBounds().height, rootElement);
            setTextValue("Toolbar_Position", doc, toolbarPosition, rootElement);
            setIntegerValue( "MaxZoomLevel", doc, maxZoomLevel, rootElement);
            setFloatValue("NodeSizeScale", doc, nodeSize, rootElement);
            setBooleanValue("Check_Online_MapImages", doc, bUseOnlineMapImages, rootElement);
            setBooleanValue("Continuous_Connection", doc, bContinuousConnections, rootElement);
            setBooleanValue("MiddleMouseMove", doc, bMiddleMouseMove, rootElement);
            setBooleanValue("LockToolbar", doc, bLockToolbarPosition, rootElement);
            setIntegerValue("CurveSliderMaximum", doc, curveSliderMax, rootElement);
            setIntegerValue("CurveSliderDefault", doc, curveSliderDefault, rootElement);
            if (curveSliderDefault > curveSliderMax) curveSliderDefault = curveSliderMax;
            setIntegerValue("ControlPointMoveScaler", doc, controlPointMoveScaler, rootElement);
            setBooleanValue("ShowGrid", doc, bShowGrid, rootElement);
            setBooleanValue("GridSnapping", doc, bGridSnap, rootElement);
            setFloatValue("GridSpacingX", doc, gridSpacingX, rootElement);
            setFloatValue("GridSpacingY", doc, gridSpacingY, rootElement);
            setBooleanValue("SnapSubDivision",doc, bGridSnapSubs, rootElement);
            setIntegerValue("GridSubDivisions", doc, gridSubDivisions, rootElement);
            setIntegerValue("RotationStep", doc, rotationSnap, rootElement);
            setBooleanValue("FilledConnectionArrows", doc, bFilledArrows, rootElement);
            setBooleanValue("CreateLinearLineEndNode", doc, bCreateLinearLineEndNode, rootElement);
            setIntegerValue("LinearLineNodeDistance", doc, linearLineNodeDistance, rootElement);
            doc.appendChild(rootElement);

            Element colorElement = doc.createElement("EditorColours");
            setColorValue("Colour_Node_Regular", doc, colourNodeRegular, colorElement);
            setColorValue("Colour_Node_Subprio", doc, colourNodeSubprio, colorElement);
            setColorValue("Colour_Node_Selected", doc, colourNodeSelected, colorElement);
            setColorValue("Colour_Node_Control", doc, colourNodeControl, colorElement);
            setColorValue("Colour_Connection_Regular", doc, colourConnectRegular, colorElement);
            setColorValue("Colour_Connection_Subprio", doc, colourConnectSubprio, colorElement);
            setColorValue("Colour_Connection_Dual", doc, colourConnectDual, colorElement);
            setColorValue("Colour_Connection_Dual_Subprio", doc, colourConnectDualSubprio, colorElement);
            setColorValue("Colour_Connection_Reverse", doc, colourConnectReverse, colorElement);
            setColorValue("Colour_Connection_Reverse_Subprio", doc, colourConnectReverseSubprio, colorElement);
            rootElement.appendChild(colorElement);

            // check if depreciated <mapzoomfactors> keys exists, if so, remove them

            if (doc.getElementsByTagName("mapzoomfactor").getLength() != 0 ) {

                // remove all the previous <mapzoomfactor> entries from loaded XML

                for (int zoomStoreIndex = 1; zoomStoreIndex < knownMapList.size(); zoomStoreIndex++) {
                    Element element = (Element) doc.getElementsByTagName("mapzoomfactor" + (zoomStoreIndex)).item(0);
                    if (element != null) {
                        Element parent = (Element) element.getParentNode();
                        while (parent.hasChildNodes())
                            parent.removeChild(parent.getFirstChild());
                    }
                }
            } else if (doc.getElementsByTagName("KnownMapSettings").getLength() != 0 ) {

                // remove all the previous <KnownMapSettings> entries from loaded XML

                for (int knownMapsIndex = 1; knownMapsIndex < knownMapList.size(); knownMapsIndex++) {
                    Element element = (Element) doc.getElementsByTagName("KnownMapSettings" + (knownMapsIndex)).item(0);
                    if (element != null) {
                        Element parent = (Element) element.getParentNode();
                        while (parent.hasChildNodes())
                            parent.removeChild(parent.getFirstChild());
                    }
                }

            }

            // if <mapzoomfactor> doesn't exist, create it

            if (knownMapList.size() > 0 && doc.getElementsByTagName("KnownMapSettings").getLength() == 0 ) {
                Element test = doc.createElement("KnownMapSettings");
                rootElement.appendChild(test);
            }

            // add the stored entries, makes sure the list upto date

            NodeList knownMapList = doc.getElementsByTagName("KnownMapSettings");
            Node knownMapNode = knownMapList.item(0);

            for (MapInfoStore mapInfo : EditorXML.knownMapList) {

                Element newMapElement = doc.createElement("Map");
                newMapElement.setAttribute("Name", mapInfo.mapName);

                Element markerName = doc.createElement("MapScale");
                markerName.appendChild(doc.createTextNode(String.valueOf(mapInfo.zoomFactor)));
                newMapElement.appendChild(markerName);

                Element markerNodeSize = doc.createElement("NodeSize");
                markerNodeSize.appendChild(doc.createTextNode(String.valueOf(mapInfo.nodeSize)));
                newMapElement.appendChild(markerNodeSize);

                knownMapNode.appendChild(newMapElement);
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
                    LOG.info("{}", getLocaleString("console_editor_config_save_end"));
                } catch (IOException ioe) {
                    LOG.error( getLocaleString("console_editor_config_save_error"));
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
