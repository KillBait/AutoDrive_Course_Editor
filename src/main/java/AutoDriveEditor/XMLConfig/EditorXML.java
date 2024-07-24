package AutoDriveEditor.XMLConfig;


import AutoDriveEditor.GUI.Menus.FileMenu.RecentFilesMenu;
import AutoDriveEditor.RoadNetwork.RoadMap;
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
import java.math.RoundingMode;
import java.util.ArrayList;

import static AutoDriveEditor.AutoDriveEditor.*;
import static AutoDriveEditor.GUI.MapPanel.*;
import static AutoDriveEditor.GUI.Menus.DebugMenu.Logging.LogConfigMenu.bDebugLogXMLInfo;
import static AutoDriveEditor.GUI.Menus.FileMenu.RecentFilesMenu.recentFilesList;
import static AutoDriveEditor.GUI.Menus.MapImagesMenu.MapZoomMenu.updateMapScaleMenu;
import static AutoDriveEditor.GUI.TextPanel.setCurrentMapScaleLabel;
import static AutoDriveEditor.Locale.LocaleManager.getLocaleString;
import static AutoDriveEditor.Utils.LoggerUtils.LOG;
import static AutoDriveEditor.Utils.MathUtils.limitFloatToDecimalPlaces;
import static AutoDriveEditor.Utils.XMLUtils.*;
import static AutoDriveEditor.XMLConfig.GameXML.autoSaveLastUsedSlot;
import static AutoDriveEditor.XMLConfig.GameXML.lastUsedLocation;

public class EditorXML {

    //
    // Editor startup default values
    //

    public static String lastRunVersion;
    public static boolean bShowUpdateMessage = true;
    public static boolean bNoSavedWindowPosition;
    public static int x = -99; // x + y are negative on purpose
    public static int y = -99;
    public static int width = 1280;
    public static int height = 955;
    public static float nodeSize = 2;
    public static String toolbarPosition = "Left";
    public static final ArrayList<MapInfoStore> knownMapList = new ArrayList<>();
    public static boolean bShowSelectionBounds = false; // Experimental only

    //
    // General tab default values
    //

    public static boolean bUseOnlineMapImages = true;
    public static boolean bMiddleMouseMove = false;
    public static boolean bLockToolbarPosition = false;
    public static boolean bInterpolateZoom = true;
    public static int maxZoomLevel = 30;

    //
    // Autosave tab default options
    //

    public static boolean bAutoSaveEnabled = true;
    public static int autoSaveInterval = 10;
    public static int maxAutoSaveSlots = 10;

    //
    // Curves tab default values
    //
    
    public static int curveSliderMax = 50;
    public static int curveSliderDefault = 10;
    public static int controlPointMoveScaler = 3;

    //
    // Connections tab default values
    //

    public static Boolean bCreateLinearLineEndNode = false;
    public static Boolean bFilledArrows = true;
    public static int linearLineNodeDistance = 12;
    public static float hiddenNodesTransparencyLevel = 0.3f;

    //
    // Colours tab default values
    //

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

    //
    // Options buttons default values
    //

    public static boolean bContinuousConnections = false;
    public static boolean bUseRectangularSelection = true;
    public static boolean bSelectHidden = false;

    //
    // Grid buttons default values
    //
    
    public static boolean bGridSnapSubs = false;
    public static boolean bGridSnap = false;
    public static boolean bShowGrid = false;
    public static float gridSpacingX = 2;
    public static float gridSpacingY = 2;
    public static int gridSubDivisions = 4;
    public static int rotationSnap = 5;

    //
    // Display menu default values
    //

    public static boolean bShowMarkerNames = true;
    public static boolean bShowMarkerIcons = true;
    public static boolean bShowParkingIcons = true;
    public static boolean bShowSelectedNodeID = false;


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
            lastUsedLocation = getTextValue(lastUsedLocation, rootElement, "LastUsedLocation");
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
            hiddenNodesTransparencyLevel = getFloatValue(rootElement, "HiddenNodeTransparency", hiddenNodesTransparencyLevel);
            bCreateLinearLineEndNode = getBooleanValue(bCreateLinearLineEndNode, rootElement, "CreateLinearLineEndNode");
            linearLineNodeDistance = getIntegerValue(linearLineNodeDistance, rootElement, "LinearLineNodeDistance");
            bUseRectangularSelection = getBooleanValue(bUseRectangularSelection, rootElement, "UseRectangularSelection");
            bShowMarkerNames = getBooleanValue(bShowMarkerNames, rootElement, "ShowMarkerNames");
            bShowSelectedNodeID = getBooleanValue(bShowSelectedNodeID, rootElement, "ShowSelectedNodeID");
            bSelectHidden = getBooleanValue(bSelectHidden, rootElement, "SelectHidden");
            bShowMarkerIcons = getBooleanValue(bShowMarkerIcons, rootElement, "ShowMarkerIcons");
            bShowParkingIcons = getBooleanValue(bShowParkingIcons, rootElement, "ShowParkingIcons");
            bInterpolateZoom = getBooleanValue(bInterpolateZoom, rootElement, "InterpolateZoom");


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

            LOG.info("Checking for <RecentFiles> key in EditorConfig.xml");

            NodeList recentEntryList;
            if (doc.getElementsByTagName("RecentFiles").getLength() != 0 ) {
                NodeList recentList = doc.getElementsByTagName("RecentFiles");
                LOG.info("Found <RecentFiles> tag");
                LOG.info("parsing {} <Config> tags from XML", doc.getElementsByTagName("Config").getLength());

                for (int i = 0; i < recentList.getLength(); i++) {
                    Node recentListNode = recentList.item(i);
                    if (recentListNode.getNodeType() == Node.ELEMENT_NODE) {
                        recentEntryList = doc.getElementsByTagName("Config");
                        for (int j = 0; j < recentEntryList.getLength(); j++) {
                            String path = null;
                            int type = 0;
                            Node knownEntryNode = recentEntryList.item(j);
                            Element eElement = (Element) knownEntryNode;
                            if (eElement.getElementsByTagName("Path").getLength() != 0 ) {
                                NodeList nodeList = eElement.getElementsByTagName("Path").item(0).getChildNodes();
                                Node node = nodeList.item(0);
                                // check if the <Path> key is empty
                                if (node != null) {
                                    path = node.getNodeValue();
                                } else {
                                    LOG.info("--> <Path> key is empty for Entry {}", j + 1);
                                }
                            } else {
                                // No <Path> key was found
                                LOG.info("@ --> No <Path> key found for Entry {}", j + 1);
                            }

                            if (eElement.getElementsByTagName("Type").getLength() != 0 ) {
                                NodeList nodeList = eElement.getElementsByTagName("Type").item(0).getChildNodes();
                                Node node = nodeList.item(0);
                                // Check if the <Type> tag is empty
                                if (node != null) {
                                    type = Integer.parseInt(node.getNodeValue());
                                } else {
                                    LOG.info("--> <Type> key is empty for Entry {}", j + 1);
                                }
                            } else {
                                LOG.info("@ --> No <Type> key found for Entry {}", j + 1);
                            }

                            if (path != null && type > 0) {
                                if (bDebugLogXMLInfo) LOG.info("Adding {} to list", path);
                                recentFilesList.addLast(new RecentFilesMenu.RecentEntry(path, type));
                            } else {
                                LOG.info("--> Skipping Entry {} -> <Path> or <Type> is invalid", j + 1);
                            }
                        }
                    }
                }

                //LOG.info("parsing {} recently used files for editor use", doc.getElementsByTagName("Recent").getLength());
            } else {
                LOG.info("No <RecentFiles> key found");
            }



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

                        for (int mapNameIndex = 0; mapNameIndex<mapNameElement.getLength(); mapNameIndex++ ) {
                            Node node = mapNameElement.item(mapNameIndex).getChildNodes().item(0);
                            mapName = node.getNodeValue();

                            node = mapZoomFactorElement.item(mapNameIndex).getChildNodes().item(0);
                            mapZoomInt = node.getNodeValue();

                            // Work around for old configs to avoid fatal NPE, if the <NodeSize> or <Grid...> keys are
                            // not found, set NodeSize/GridX/GridY to default (2.0) + GridSubDivisions to default (1),
                            //
                            // All the entries will be updated to the new format on the next config save.
                            // TODO:- Specifying a direct float is not ideal, find a cleaner way?

                            knownMapList.add(new MapInfoStore(mapName, Integer.parseInt(mapZoomInt), 2f, 2f, 2f, 1, 30));
                        }
                    }
                }
            } else if (doc.getElementsByTagName("KnownMapSettings").getLength() != 0 ) {
                LOG.info("<mapzoomfactors> not found");
                LOG.info("parsing {} known maps for editor use", doc.getElementsByTagName("Map").getLength());

                NodeList mapList = doc.getElementsByTagName("KnownMapSettings");

                for (int temp = 0; temp < mapList.getLength(); temp++) {
                    Node mapListNode = mapList.item(temp);
                    if (mapListNode.getNodeType() == Node.ELEMENT_NODE) {
                        knownMapsList = doc.getElementsByTagName("Map");
                        for (int temp2 = 0; temp2 < knownMapsList.getLength(); temp2++) {
                            Node knownMapsNode = knownMapsList.item(temp2);
                            Element eElement = (Element) knownMapsNode;
                            String mapName = eElement.getAttribute("Name");
                            LOG.info("  Checking <{}> entry", mapName);

                            // get Maps Scale

                            String mapScale;

                            if (eElement.getElementsByTagName("MapScale").getLength() != 0 ) {
                                NodeList nodeList = eElement.getElementsByTagName("MapScale").item(0).getChildNodes();
                                Node node = nodeList.item(0);
                                mapScale = node.getNodeValue();
                            } else {
                                mapScale = String.valueOf(1);
                                LOG.info("    --> <MapScale> key is missing, setting to default ( {} )", mapScale);
                            }

                            // get Maps Scale

                            String nodeSize;

                            if (eElement.getElementsByTagName("NodeSize").getLength() != 0 ) {
                                NodeList nodeList = eElement.getElementsByTagName("NodeSize").item(0).getChildNodes();
                                Node node = nodeList.item(0);
                                nodeSize = node.getNodeValue();
                            } else {
                                nodeSize = String.valueOf(2F);
                                LOG.info("    --> <NodeSize> key is missing, setting to default ( {} )", nodeSize);
                            }

                            // get GridX

                            String mapGridX;

                            if (eElement.getElementsByTagName("GridX").getLength() != 0 ) {
                                NodeList nodeList = eElement.getElementsByTagName("GridX").item(0).getChildNodes();
                                Node node = nodeList.item(0);
                                mapGridX = node.getNodeValue();
                            } else {
                                mapGridX = String.valueOf(2F);
                                LOG.info("    --> <GridX> key is missing, setting to default ( {} )", mapGridX);
                            }

                            // get gridY

                            String mapGridY;

                            if (eElement.getElementsByTagName("GridY").getLength() != 0 ) {
                                NodeList nodeList = eElement.getElementsByTagName("GridY").item(0).getChildNodes();
                                Node node = nodeList.item(0);
                                mapGridY = node.getNodeValue();
                            } else {
                                mapGridY = String.valueOf(2F);
                                LOG.info("    --> <GridY> key is missing, setting to default ( {} )", mapGridY);
                            }

                            // get grid subdivisions

                            String mapGridSub;

                            if (eElement.getElementsByTagName("GridSubDivisions").getLength() != 0 ) {
                                NodeList nodeList = eElement.getElementsByTagName("GridSubDivisions").item(0).getChildNodes();
                                Node node = nodeList.item(0);
                                mapGridSub = node.getNodeValue();
                            } else {
                                mapGridSub = String.valueOf(1);
                                LOG.info("    --> <GridSubDivisions> key is missing, setting to default ( {} )", mapGridSub);
                            }

                            // get max zoom level

                            String mapMaxZoomLevel;

                            if (eElement.getElementsByTagName("MaxZoomLevel").getLength() != 0 ) {
                                NodeList nodeList = eElement.getElementsByTagName("MaxZoomLevel").item(0).getChildNodes();
                                Node node = nodeList.item(0);
                                mapMaxZoomLevel = node.getNodeValue();
                            } else {
                                mapMaxZoomLevel = String.valueOf(30);
                                LOG.info("    --> <MaxZoomLevel> key is missing, setting to default ( {} )", maxZoomLevel);
                            }

                            if (bDebugLogXMLInfo) LOG.info("Map name = {}, Scale = {}, NodeSize = {}, GridX = {}, GridY = {}, GridSub = {}, maxZoom = {}", mapName, mapScale, nodeSize, mapGridX, mapGridY, mapGridSub, mapMaxZoomLevel);
                            knownMapList.add(new MapInfoStore(mapName, Integer.parseInt(mapScale), Float.parseFloat(nodeSize), Float.parseFloat(mapGridX), Float.parseFloat(mapGridY), Integer.parseInt(mapGridSub), Integer.parseInt(mapMaxZoomLevel)));
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
            setTextValue("LastUsedLocation", doc, lastUsedLocation, rootElement);
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
            setFloatValue("HiddenNodeTransparency", doc, hiddenNodesTransparencyLevel, rootElement);
            setBooleanValue("CreateLinearLineEndNode", doc, bCreateLinearLineEndNode, rootElement);
            setIntegerValue("LinearLineNodeDistance", doc, linearLineNodeDistance, rootElement);
            setBooleanValue("UseRectangularSelection", doc, bUseRectangularSelection, rootElement);
            setBooleanValue("ShowMarkerNames", doc, bShowMarkerNames, rootElement);
            setBooleanValue("ShowSelectedNodeID", doc, bShowSelectedNodeID, rootElement);
            setBooleanValue("SelectHidden", doc, bSelectHidden, rootElement);
            setBooleanValue("ShowMarkerIcons", doc, bShowMarkerIcons, rootElement);
            setBooleanValue("ShowParkingIcons", doc, bShowParkingIcons, rootElement);
            setBooleanValue("InterpolateZoom", doc, bInterpolateZoom, rootElement);

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

            // remove all the previous <RecentFiles> entries from loaded XML

            if (doc.getElementsByTagName("RecentFiles").getLength() != 0 ) {
                for (int knownRecentIndex = 1; knownRecentIndex < knownMapList.size(); knownRecentIndex++) {
                    Element element = (Element) doc.getElementsByTagName("RecentFiles" + (knownRecentIndex)).item(0);
                    if (element != null) {
                        Element parent = (Element) element.getParentNode();
                        while (parent.hasChildNodes())
                            parent.removeChild(parent.getFirstChild());
                    }
                }
            }

            // if <RecentFiles> doesn't exist, create it

            if (recentFilesList.size() > 0 && doc.getElementsByTagName("RecentFiles").getLength() == 0 ) {
                Element newElement = doc.createElement("RecentFiles");
                rootElement.appendChild(newElement);
            }

            // add the stored entries, makes sure the list is upto date

            NodeList recentList = doc.getElementsByTagName("RecentFiles");
            Node knownRecentNode = recentList.item(0);

            for (RecentFilesMenu.RecentEntry entry : recentFilesList) {
                Element newConfigElement = doc.createElement("Config");

                Element newPathElement = doc.createElement("Path");
                newPathElement.appendChild(doc.createTextNode(entry.getConfigPath()));
                newConfigElement.appendChild(newPathElement);

                Element newTypeElement = doc.createElement("Type");
                newTypeElement.appendChild(doc.createTextNode(String.valueOf(entry.getConfigType())));
                newConfigElement.appendChild(newTypeElement);

                knownRecentNode.appendChild(newConfigElement);
            }

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
                newMapElement.setAttribute("Name", mapInfo.getMapName());

                Element knownMapName = doc.createElement("MapScale");
                knownMapName.appendChild(doc.createTextNode(String.valueOf(mapInfo.getMapScale())));
                newMapElement.appendChild(knownMapName);

                Element knownMapNodeSize = doc.createElement("NodeSize");
                knownMapNodeSize.appendChild(doc.createTextNode(String.valueOf(mapInfo.getNodeSize())));
                newMapElement.appendChild(knownMapNodeSize);

                Element knownMapGridX = doc.createElement("GridX");
                knownMapGridX.appendChild(doc.createTextNode(String.valueOf(mapInfo.getGridX())));
                newMapElement.appendChild(knownMapGridX);

                Element knownMapGridY = doc.createElement("GridY");
                knownMapGridY.appendChild(doc.createTextNode(String.valueOf(mapInfo.getGridY())));
                newMapElement.appendChild(knownMapGridY);

                Element knownMapGridSubDiv = doc.createElement("GridSubDivisions");
                knownMapGridSubDiv.appendChild(doc.createTextNode(String.valueOf(mapInfo.getGridSubDiv())));
                newMapElement.appendChild(knownMapGridSubDiv);

                Element knownMapMaxZoom = doc.createElement("MaxZoomLevel");
                knownMapMaxZoom.appendChild(doc.createTextNode(String.valueOf(mapInfo.getMaxZoomLevel())));
                newMapElement.appendChild(knownMapMaxZoom);

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

    public static class MapInfoStore {
        private final String mapName;
        private int mapScale;
        private float nodeSize;
        private float gridX;
        private float gridY;
        private int gridSubDiv;
        private int maxZoomLevel;

        public MapInfoStore(String mapName, int mapScale, float nodeSize, float gridX, float gridY, int subDiv, int maxZoomLevel) {
            this.mapName = mapName;
            this.mapScale = mapScale;
            this.nodeSize = nodeSize;
            this.gridX = gridX;
            this.gridY = gridY;
            this.gridSubDiv = subDiv;
            this.maxZoomLevel = maxZoomLevel;
        }

        // getters

        public String getMapName() { return this.mapName; }
        public int getMapScale() { return this.mapScale; }
        public float getNodeSize() { return this.nodeSize; }
        public float getGridX() { return this.gridX; }
        public float getGridY() { return this.gridY; }
        public int getGridSubDiv() { return this.gridSubDiv; }
        public int getMaxZoomLevel() { return this.maxZoomLevel; }

        // setters

        public void setMapScale(int newMapScale) { this.mapScale = newMapScale; }
        public void setNodeSize(float newNodeSize) { this.nodeSize = newNodeSize; }
        public void setGridSettings(float newGridX, float newGridY, int newGridSubDiv) {
            this.gridX = limitFloatToDecimalPlaces(newGridX, 1, RoundingMode.HALF_UP);
            this.gridY = limitFloatToDecimalPlaces(newGridY, 1, RoundingMode.HALF_UP);
            this.gridSubDiv = newGridSubDiv;
        }
        public void setMaxZoomLevel(int maxZoomLevel) { this.maxZoomLevel = maxZoomLevel; }
    }

    public static void checkStoredMapInfoFor(String mapName) {
        boolean isKnown = false;
        if (mapName != null) {
            for (int i = 0; i <= knownMapList.size() - 1; i++) {
                MapInfoStore store = knownMapList.get(i);
                if (store.getMapName().equals(mapName)) {
                    LOG.info("Found previously used settings in EditorConfig.xml for map ( {} ), applying them now", mapName);
                    setNewMapScale(store.getMapScale());
                    LOG.info("  --> updated map scale to {}x", mapScale);
                    setNewNodeSize(store.getNodeSize());
                    LOG.info("  --> updated node size to {}x", nodeSize);
                    setNewGridValues(store.getGridX(), store.getGridY(), store.getGridSubDiv());
                    LOG.info("  --> updated grid settings to X = {} , Y = {}, SubDiv = {}", gridSpacingX, gridSpacingY, gridSubDivisions);
                    setNewMaxZoomLevel(store.getMaxZoomLevel());
                    LOG.info("  --> updated max zoom level to {}x", maxZoomLevel);
                    isKnown = true;
                    break;
                }
            }
        } else {
            LOG.info("## checkStoredMapInfoFor() ## - Map name is 'null', this should not happen, setting map panel to 1x map scale and 2.0 node size");
            setNewMapScale(1);
            setNewNodeSize(2f);
        }

        if (!isKnown && mapName != null) {
            LOG.info("No previous settings found for Map ( {} ), storing new map with initial settings of 1x map scale / node size of 2.0", mapName);
            knownMapList.add(new MapInfoStore(mapName, 1, 2f, 2f,2f,1, 30));
            setCurrentMapScaleLabel("2km");
            nodeSize = 2f;
        }
    }

    public static void setNewMapScale(int newMapScale) {
        if (roadMap != null) {
            getMapPanel().setMapScale(newMapScale);
            setCurrentMapScaleLabel(newMapScale * 2 + "km");
            updateMapScaleMenu(newMapScale);
            for (MapInfoStore store : knownMapList) {
                if (store.getMapName().equals(RoadMap.mapName)) {
                    store.setMapScale(newMapScale);
                    break;
                }
            }
            getMapPanel().repaint();
        }
    }

    public static void setNewNodeSize(float newNodeSize) {
        if (roadMap != null) {
            nodeSize = limitFloatToDecimalPlaces(newNodeSize, 1, RoundingMode.HALF_UP);
            updateNodeScaling();
            for (MapInfoStore store : knownMapList) {
                if (store.getMapName().equals(RoadMap.mapName)) {
                    store.setNodeSize(nodeSize);
                    break;
                }
            }
            getMapPanel().repaint();
        }

    }

    public static void setNewGridValues(float newGridX, float newGridY, int newGridSubDiv) {
        if (roadMap != null) {
            gridSpacingX = limitFloatToDecimalPlaces(newGridX, 1, RoundingMode.HALF_UP);
            gridSpacingY = limitFloatToDecimalPlaces(newGridY, 1, RoundingMode.HALF_UP);
            gridSubDivisions = newGridSubDiv;
            for (MapInfoStore store : knownMapList) {
                if (store.getMapName().equals(RoadMap.mapName)) {
                    store.setGridSettings(newGridX, newGridY, newGridSubDiv);
                    break;
                }
            }
            getMapPanel().repaint();
        }

    }

    public static void setNewMaxZoomLevel(int newMaxZoomLevel) {
        maxZoomLevel = newMaxZoomLevel;
        zoomLevel = 1;
        if (roadMap != null) {
            for (MapInfoStore store : knownMapList) {
                if (store.getMapName().equals(RoadMap.mapName)) {
                    store.setMaxZoomLevel(newMaxZoomLevel);
                    break;
                }
            }
            getMapPanel().repaint();
        }
    }
}
