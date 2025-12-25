package AutoDriveEditor.XMLConfig;


import AutoDriveEditor.Classes.KeyBinds.Shortcut;
import AutoDriveEditor.GUI.Menus.FileMenu.RecentFilesMenu;
import AutoDriveEditor.Managers.ShortcutManager;
import AutoDriveEditor.RoadNetwork.Connection;
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
import java.awt.event.InputEvent;
import java.io.FileOutputStream;
import java.io.IOException;
import java.math.RoundingMode;
import java.util.ArrayList;

import static AutoDriveEditor.AutoDriveEditor.*;
import static AutoDriveEditor.Classes.Util_Classes.LoggerUtils.LOG;
import static AutoDriveEditor.Classes.Util_Classes.MathUtils.limitFloatToDecimalPlaces;
import static AutoDriveEditor.Classes.Util_Classes.XMLUtils.*;
import static AutoDriveEditor.GUI.Buttons.ActionBar.ConfigPanel.GridDisplayButton.updateGridPanelSettings;
import static AutoDriveEditor.GUI.Buttons.ActionBar.ConfigPanel.MapInfoButton.setCurrentMapNameLabel;
import static AutoDriveEditor.GUI.Buttons.ActionBar.ConfigPanel.MapInfoButton.setCurrentMapScaleLabel;
import static AutoDriveEditor.GUI.MapPanel.*;
import static AutoDriveEditor.GUI.Menus.DebugMenu.Logging.LogShortcutInfoMenu.bDebugLogShortcutInfo;
import static AutoDriveEditor.GUI.Menus.DebugMenu.Logging.LogXMLConfigMenu.bDebugLogXMLInfo;
import static AutoDriveEditor.GUI.Menus.Display.MapScaleMenu.updateMapScaleMenu;
import static AutoDriveEditor.GUI.Menus.FileMenu.RecentFilesMenu.recentFilesList;
import static AutoDriveEditor.Managers.ShortcutManager.*;
import static AutoDriveEditor.RoadNetwork.MapNode.NODE_FLAG_REGULAR;
import static AutoDriveEditor.XMLConfig.GameXML.autoSaveLastUsedSlot;
import static AutoDriveEditor.XMLConfig.GameXML.lastUsedLocation;

public class EditorXML {

    //
    // Editor startup default values
    //

    public static boolean bShowUpdateMessage = true;
    public static boolean bWindowPositionSaved;
    public static boolean bTextPanelVisible = false;
    public static boolean bUndoRedoVisible = true;
    public static boolean bCopyPasteVisible = true;


    //public static boolean bNoSavedWindowPosition;
    public static int windowX = -99; // x + y are negative on purpose
    public static int windowY = -99;
    public static int windowWidth = 1280;
    public static int windowHeight = 955;
    public static float nodeSize = 2;
    public static final ArrayList<MapInfoStore> knownMapList = new ArrayList<>();
    public static boolean bShowSelectionBounds = false; // Experimental only

    //
    // General tab default values
    //

    public static boolean bUseOnlineMapImages = true;
    public static boolean bMiddleMouseMove = false;
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
    public static int curveIterationsDefault = 6;
    public static int controlPointMoveScaler = 3;
    public static int curveControlPointDefault = 1;
    public static int maxControlPoints = 3;
    public static int curveNodeDefaultPriority = NODE_FLAG_REGULAR;
    public static Connection.ConnectionType curveDefaultConnection = Connection.ConnectionType.REGULAR;

    //
    // Connections tab default values
    //

    public static Boolean bCreateLinearLineEndNode = false;
    public static Boolean bFilledArrows = true;
    public static int linearLineNodeDistance = 12;
    public static float hiddenNodesTransparencyLevel = 1f;

    //
    // Colours tab default values
    //

    public static Color colourNodeRegular = Color.RED;
    public static Color colourNodeSubprio = Color.ORANGE;
    public static Color colourNodeSelected = Color.WHITE;
    public static Color colourNodeControl = new Color(216,0,255);
    public static Color colourConnectRegular = Color.GREEN;
    public static Color colourConnectSubprio = Color.ORANGE;
    public static Color colourConnectDual = Color.BLUE;
    public static Color colourConnectDualSubprio = new Color(152, 104, 50 );
    public static Color colourConnectReverse = Color.CYAN;
    public static Color colourConnectReverseSubprio = new Color(0,140,140);
    public static Color colourGridLines = new Color(25, 25, 25);

    //
    // Move Widget default values
    //

    public static byte axisLength = 30;
    public static byte axisWidth = 5;
    public static byte arrowLength = 24;
    public static byte arrowWidth = 7;

    public static Color DEFAULT_X_AXIS_COLOR = new Color(255, 0, 0, 255);
    public static Color xAxisColor = DEFAULT_X_AXIS_COLOR;
    public static Color DEFAULT_Y_AXIS_COLOR = new Color(0, 0, 255, 255);
    public static Color yAxisColor = DEFAULT_Y_AXIS_COLOR;
    public static Color DEFAULT_FREE_AXIS_COLOR = new Color(0, 255, 0, 255);
    public static Color freeMoveColor = DEFAULT_FREE_AXIS_COLOR;
    public enum X_DIRECTION {LEFT, RIGHT}
    public static X_DIRECTION xAxisDirection = X_DIRECTION.RIGHT;

    public enum Y_DIRECTION {UP, DOWN}
    public static Y_DIRECTION yAxisDirection = Y_DIRECTION.UP;

    public enum FREEMOVE_POSITION {CENTER, MANUAL}
    public static FREEMOVE_POSITION freeMovePosition = FREEMOVE_POSITION.MANUAL;
    public static int freeMoveOffsetX = 0;
    public static int freeMoveOffsetY = 0;
    public static int freeMoveDefaultSmall = 12;
    public static int freeMoveDefaultMedium = 16;
    public static int freeMoveDefaultLarge = 20;
    public enum FREEMOVE_TYPE {SQUARE, ROUND}
    public static FREEMOVE_TYPE freeMoveType = FREEMOVE_TYPE.SQUARE;
    public enum FREEMOVE_STYLE {SOLID, OUTLINE, PATTERN}
    public static FREEMOVE_STYLE freeMoveStyle = FREEMOVE_STYLE.PATTERN;
    public enum FREEMOVE_SIZE {SMALL, MEDIUM, LARGE}
    public static FREEMOVE_SIZE freeMoveSize = FREEMOVE_SIZE.MEDIUM;

    //
    // Options buttons default values
    //

    public static boolean bContinuousConnections = false;
    public static boolean bUseRectangularSelection = true;
    public static boolean bSelectHidden = false;
    public static boolean bRotationSnapEnabled = true;
    public static int rotationStep = 5;

    //
    // Grid buttons default values
    //

    public static boolean bGridSnapSubs = false;
    public static boolean bGridSnapEnabled = false;
    public static boolean bShowGrid = false;
    public enum GRID_TYPE {GRID_1x1, GRID_2x2, GRID_4x4, GRID_CUSTOM}
    public static GRID_TYPE gridType = GRID_TYPE.GRID_1x1;
    public static float gridCustomX = 1;
    public static float gridCustomY = 1;
    public static int gridCustomSubDivisions = 1;

    //
    // Display menu default values
    //

    public static boolean bShowMarkerNames = true;
    public static boolean bShowMarkerIcons = true;
    public static boolean bShowParkingIcons = true;
    public static boolean bShowSelectedNodeID = false;

    //
    // Default keybinds
    //

    public static void loadEditorXMLConfig() {

        DocumentBuilderFactory docFactory = DocumentBuilderFactory.newInstance();
        try {
            DocumentBuilder docBuilder = docFactory.newDocumentBuilder();
            Document doc = docBuilder.parse("EditorConfig.xml");
            Element rootElement = doc.getDocumentElement();


            // TODO: remove default values from constructor variables and specify in get/set functions

            bShowUpdateMessage = getBooleanValue(rootElement, "ShowUpdateMessage", true);
            bAutoSaveEnabled = getBooleanValue(rootElement, "AutoSave_Enabled", true);
            autoSaveInterval = getIntegerValue(rootElement, "AutoSave_Interval", 10);
            maxAutoSaveSlots = getIntegerValue(rootElement, "AutoSave_Slots", 10);
            autoSaveLastUsedSlot = getIntegerValue(rootElement, "AutoSave_Last_Used_Slot", 1);
            if (autoSaveLastUsedSlot > maxAutoSaveSlots)  autoSaveLastUsedSlot = maxAutoSaveSlots;

            lastUsedLocation = getStringValue(rootElement, "LastUsedLocation", "");
            windowX = getIntegerValue(rootElement, "WindowX", -99);
            windowY = getIntegerValue(rootElement, "WindowY", -99);
            if ( windowX == -99 || windowY == -99) bWindowPositionSaved = true;
            windowWidth = getIntegerValue(rootElement, "WindowWidth", 1280);
            windowHeight = getIntegerValue(rootElement, "WindowHeight", 960);
            maxZoomLevel = getIntegerValue(rootElement, "MaxZoomLevel", 30);
            nodeSize = getFloatValue(rootElement, "NodeSizeScale", 2);

            bTextPanelVisible = getBooleanValue(rootElement, "TextPanelVisible", false);
            bUndoRedoVisible = getBooleanValue(rootElement, "UndoRedoVisible", true);
//            public static boolean bUndoRedoVisible = true;

            bCopyPasteVisible = getBooleanValue(rootElement, "CopyPasteVisible", true);
//            public static boolean bCopyPasteVisible = true;
            bUseOnlineMapImages = getBooleanValue(rootElement, "Check_Online_MapImages", bUseOnlineMapImages);
            bContinuousConnections = getBooleanValue(rootElement, "Continuous_Connection", bContinuousConnections);
            bMiddleMouseMove = getBooleanValue(rootElement, "MiddleMouseMove", bMiddleMouseMove);
            curveSliderMax = getIntegerValue(rootElement, "CurveSliderMaximum", curveSliderMax);
            curveIterationsDefault = getIntegerValue(rootElement, "CurveSliderDefault", curveIterationsDefault);
            if (curveIterationsDefault > curveSliderMax) curveIterationsDefault = curveSliderMax;
            controlPointMoveScaler = getIntegerValue(rootElement, "ControlPointMoveScaler", controlPointMoveScaler);
            curveControlPointDefault = getIntegerValue(rootElement, "NumControlPoints", curveControlPointDefault);
            maxControlPoints = getIntegerValue(rootElement, "MaxControlPoints", maxControlPoints);
            curveNodeDefaultPriority = getIntegerValue(rootElement, "CurveNodeDefaultPriority", curveNodeDefaultPriority);
            curveDefaultConnection = getEnumValue(Connection.ConnectionType.class, rootElement, "CurveDefaultConnection", curveDefaultConnection);
            bShowGrid = getBooleanValue(rootElement, "ShowGrid", bShowGrid);
            bGridSnapEnabled = getBooleanValue(rootElement, "GridSnapEnabled", bGridSnapEnabled);
            bGridSnapSubs = getBooleanValue(rootElement, "SnapSubDivision", bGridSnapSubs);
            rotationStep = getIntegerValue(rootElement, "RotationStep", rotationStep);
            bFilledArrows = getBooleanValue(rootElement, "FilledConnectionArrows", bFilledArrows);
            hiddenNodesTransparencyLevel = getFloatValue(rootElement, "HiddenNodeTransparency", hiddenNodesTransparencyLevel);
            bCreateLinearLineEndNode = getBooleanValue(rootElement, "CreateLinearLineEndNode", bCreateLinearLineEndNode);
            linearLineNodeDistance = getIntegerValue(rootElement, "LinearLineNodeDistance", linearLineNodeDistance);
            bUseRectangularSelection = getBooleanValue(rootElement, "UseRectangularSelection", bUseRectangularSelection);
            bRotationSnapEnabled = getBooleanValue(rootElement, "RotationSnapEnabled", bRotationSnapEnabled);
            bShowMarkerNames = getBooleanValue(rootElement, "ShowMarkerNames", bShowMarkerNames);
            bShowSelectedNodeID = getBooleanValue(rootElement, "ShowSelectedNodeID", bShowSelectedNodeID);
            bSelectHidden = getBooleanValue(rootElement, "SelectHidden", bSelectHidden);
            bShowMarkerIcons = getBooleanValue(rootElement, "ShowMarkerIcons", bShowMarkerIcons);
            bShowParkingIcons = getBooleanValue(rootElement, "ShowParkingIcons", bShowParkingIcons);
            bInterpolateZoom = getBooleanValue(rootElement, "InterpolateZoom", bInterpolateZoom);

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
            colourGridLines = getColorValue(rootElement, "Colour_Grid", new Color(25, 25, 25));


            axisLength = getByteValue(rootElement, "AxisLength", axisLength);
            axisWidth = getByteValue(rootElement, "AxisWidth", axisWidth);
            xAxisColor = getColorValue(rootElement, "X_AxisColor", xAxisColor);
            yAxisColor = getColorValue(rootElement, "Y_AxisColor", yAxisColor);
            arrowLength = getByteValue(rootElement, "ArrowLength", arrowLength);
            arrowWidth = getByteValue(rootElement, "ArrowWidth", arrowWidth);
            xAxisDirection = getEnumValue(X_DIRECTION.class, rootElement, "X_Direction", X_DIRECTION.RIGHT);
            yAxisDirection = getEnumValue(Y_DIRECTION.class, rootElement, "Y_Direction", Y_DIRECTION.UP);
            freeMovePosition = getEnumValue(FREEMOVE_POSITION.class, rootElement, "FreeMoveLocation", FREEMOVE_POSITION.MANUAL);
            freeMoveOffsetX = getIntegerValue(rootElement, "FreeMoveOffsetX", -20);
            freeMoveOffsetY = getIntegerValue(rootElement, "FreeMoveOffsetY", 30);
            freeMoveType = getEnumValue(FREEMOVE_TYPE.class, rootElement, "FreeMoveType", FREEMOVE_TYPE.SQUARE);
            freeMoveStyle = getEnumValue(FREEMOVE_STYLE.class, rootElement, "FreeMoveStyle", FREEMOVE_STYLE.PATTERN);
            freeMoveSize = getEnumValue(FREEMOVE_SIZE.class, rootElement, "FreeMoveSize", FREEMOVE_SIZE.SMALL);
            freeMoveDefaultSmall = getIntegerValue(rootElement, "FreeMoveDefaultSmall", 12);
            freeMoveDefaultMedium = getIntegerValue(rootElement, "FreeMoveDefaultMedium", 16);
            freeMoveDefaultLarge = getIntegerValue(rootElement, "FreeMoveDefaultLarge", 20);
            freeMoveColor = getColorValue(rootElement, "FreeMoveColor", freeMoveColor);

            // get the keyboard shortcuts

            LOG.info("  Checking for <Shortcuts> key in EditorConfig.xml");
            int valid = 0;
            int created = 0;
            NodeList shortcutsElement = rootElement.getElementsByTagName("Shortcuts");
            // Check if the <Shortcuts> element exists
            if (shortcutsElement.getLength() != 0) {
                // Get the child nodes of the <Shortcuts> element
                NodeList shortcutNodes = shortcutsElement.item(0).getChildNodes();

                for (Shortcut defaultShortcut : ShortcutManager.getDefaultShortcutList()) {
                    boolean found = false;
                    for (int i = 0; i < shortcutNodes.getLength(); i++) {
                        if (shortcutNodes.item(i).getNodeType() == Node.ELEMENT_NODE) {
                            Element shortcutElement = (Element) shortcutNodes.item(i);
                            // get the ID attribute of the shortcut element
                            String id = shortcutElement.getAttribute("ID");
                            // Check if the ID matches any of the default shortcuts
                            if (defaultShortcut.getId().getXmlDescriptor().equals(id)) {
                                found = true;
                                String key = shortcutElement.getAttribute("Key");
                                String modifiers = shortcutElement.getAttribute("Modifier");
                                addShortcut(defaultShortcut.getId(), getVKInteger(key), getModifiersIntFromString(modifiers));
                                valid++;
                                break;
                            }
                        }
                    }
                    if (!found) {
                        LOG.info("    Shortcut {} not found, adding using default values", defaultShortcut.getId().getXmlDescriptor());
                        Shortcut shortcut = ShortcutManager.getDefaultShortcutByName(defaultShortcut.getId().getXmlDescriptor());
                        if (shortcut != null) addMissingUserShortcut(shortcut);
                        created++;
                    }
                }
            } else {
                LOG.warn("    No shortcuts found in EditorConfig.xml, initializing all to default");
                for (Shortcut defaultShortcut : getDefaultShortcutList()) {
                    addMissingUserShortcut(defaultShortcut);
                    created++;
                }
            }
            LOG.info("    Total Shortcuts: Valid = {}, Created = {}", valid, created);

            // get the recent files list

            LOG.info("  Checking for <RecentFiles> key in EditorConfig.xml");

            NodeList recentFilesNodes = doc.getElementsByTagName("RecentFiles");
            if (recentFilesNodes.getLength() != 0) {
                NodeList recentEntries = recentFilesNodes.item(0).getChildNodes();
                // get the number of valid entries
                int recentEntriesCount = 0;
                for (int i = 0; i < recentEntries.getLength(); i++) {
                    if (recentEntries.item(i).getNodeType() == Node.ELEMENT_NODE) {
                        recentEntriesCount++;
                    }
                }
                LOG.info("    Parsing {} Entries", recentEntriesCount);
                // iterate through the entries
                for (int i = 0; i < recentEntries.getLength(); i++) {
                    if (recentEntries.item(i).getNodeType() == Node.ELEMENT_NODE) {
                        Element entryElement = (Element) recentEntries.item(i);
                        String path = entryElement.getAttribute("Path");
                        String typeStr = entryElement.getAttribute("Type");
                        if (!path.isEmpty() && !typeStr.isEmpty()) {
                            try {
                                int type = Integer.parseInt(typeStr);
                                if (type == 1 || type == 2) {
                                    if (bDebugLogXMLInfo) LOG.info("Adding {} to list", path);
                                    recentFilesList.addLast(new RecentFilesMenu.RecentEntry(path, type));
                                } else {
                                    LOG.info("      Skipping Entry {} -> <Type> is invalid", i + 1);
                                }
                            } catch (NumberFormatException e) {
                                LOG.info("      <Type> key is invalid for Entry {}", i + 1);
                            }
                        } else {
                            LOG.info("      Skipping Entry {} -> <Path> or <Type> is invalid", i + 1);
                        }
                    }
                }
            } else {
                LOG.info("----> No <RecentFiles> key found");
            }

            NodeList knownMapsList;

            LOG.info("  Checking for depreciated <mapzoomfactors> key in EditorConfig.xml");

            if (doc.getElementsByTagName("mapzoomfactor").getLength() != 0 ) {
                LOG.info("    detected old <mapzoomfactors> key, old format will be replaced with new format on next config save");
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

                            knownMapList.add(new MapInfoStore(mapName, Integer.parseInt(mapZoomInt), 2f, GRID_TYPE.GRID_CUSTOM, 2f, 2f, 1, 30));
                        }
                    }
                }
            } else if (doc.getElementsByTagName("KnownMapSettings").getLength() != 0 ) {
                LOG.info("    <mapzoomfactors> not found");
                LOG.info("  Parsing {} known maps for editor use", doc.getElementsByTagName("Map").getLength());

                NodeList mapList = doc.getElementsByTagName("KnownMapSettings");

                for (int listNum = 0; listNum < mapList.getLength(); listNum++) {
                    Node mapListNode = mapList.item(listNum);
                    if (mapListNode.getNodeType() == Node.ELEMENT_NODE) {
                        knownMapsList = doc.getElementsByTagName("Map");
                        for (int keyNum = 0; keyNum < knownMapsList.getLength(); keyNum++) {
                            Node knownMapsNode = knownMapsList.item(keyNum);
                            Element eElement = (Element) knownMapsNode;
                            String mapName = eElement.getAttribute("Name");
                            LOG.info("    Checking <{}> entry", mapName);

                            // get maps scale
                            int mapScale = getIntegerValue(eElement, "MapScale", 1);

                            // get node size
                            float nodeSize = getFloatValue(eElement, "NodeSize", 2f);

                            // get grid settings
                            GRID_TYPE mapGridType = getEnumValue(GRID_TYPE.class, eElement, "GridType", GRID_TYPE.GRID_1x1);
                            if (mapGridType != null) {
                                switch (mapGridType) {
                                    case GRID_1x1:
                                    case GRID_2x2:
                                    case GRID_4x4:
                                        gridType = mapGridType;
                                        break;
                                    case GRID_CUSTOM:
                                        gridType = GRID_TYPE.GRID_CUSTOM;
                                        float customGridX = getFloatValue(eElement, "CustomGridX");
                                        float customGridY = getFloatValue(eElement, "CustomGridY");
                                        int customGridSub = getIntegerValue(eElement, "CustomSubDivisions");
                                        if (customGridX != 0) {
                                            gridCustomX = customGridX;
                                        } else {
                                            LOG.info("      <GridX> key is missing, setting to default ( {} )", gridCustomX);
                                        }
                                        if (customGridY != 0) {
                                            gridCustomY = customGridY;
                                        } else {
                                            LOG.info("      <GridY> key is missing, setting to default ( {} )", gridCustomY);
                                        }
                                        if (customGridSub != 0) {
                                            gridCustomSubDivisions = customGridSub;
                                        } else {
                                            LOG.info("      <GridSubDivisions> key is missing, setting to default ( {} )", gridCustomSubDivisions);
                                        }
                                        break;
                                }

                            } else {
                                LOG.info("--------> <GridType> key is missing, checking other settings");
                                float mapGridX = getFloatValue(eElement, "GridX");
                                float mapGridY = getFloatValue(eElement, "GridY");
                                int mapGridSub = getIntegerValue(eElement, "GridSubDivisions");
                                if (mapGridX == 1 && mapGridY == 1 && mapGridSub ==1) {
                                    gridType = GRID_TYPE.GRID_1x1;
                                    LOG.info("--------> Stored grid settings match GRID_1x1x1, converting to new format");
                                } else if (mapGridX == 2 && mapGridY == 2 && mapGridSub ==1) {
                                    gridType = GRID_TYPE.GRID_2x2;
                                    LOG.info("--------> Stored grid settings match GRID_2x2x1, converting to new format");
                                } else if (mapGridX == 4 && mapGridY == 4 && mapGridSub ==1) {
                                    gridType = GRID_TYPE.GRID_4x4;
                                    LOG.info("--------> Stored grid settings match GRID_4x4x1, converting to new format");
                                } else {
                                    gridType = GRID_TYPE.GRID_CUSTOM;
                                    gridCustomX = (mapGridX != 0) ? mapGridX: 1;
                                    gridCustomY = (mapGridY != 0) ? mapGridY: 1;
                                    gridCustomSubDivisions = (mapGridSub != 0) ? mapGridSub: 1;
                                    LOG.info("--------> Stored grid settings do not match any default, converting to GRID_CUSTOM");
                                    LOG.info("--------> GridX = {}, GridY = {}, GridSubDivisions = {}", gridCustomX, gridCustomY, gridCustomSubDivisions);
                                }
                            }

                            // get max zoom level
                            int mapMaxZoomLevel = getIntegerValue(eElement, "MaxZoomLevel", 30);

                            knownMapList.add(new MapInfoStore(mapName, mapScale, nodeSize, gridType, gridCustomX, gridCustomY, gridCustomSubDivisions, mapMaxZoomLevel));
                        }
                    }
                }
            }
            //setGridSpacing();
        } catch (ParserConfigurationException | SAXException pce) {
            LOG.error("## Exception in loading Editor config ## SAX/Parser Exception");
            System.out.println(pce.getMessage());
        } catch (IOException ioe) {
            LOG.warn("Editor config not found, using defaults. A new config will be saved on exit");
            addAllDefaultShortcuts();
            bWindowPositionSaved = true;
        }
    }

    public static void saveEditorXMLConfig() {
        DocumentBuilderFactory docFactory = DocumentBuilderFactory.newInstance();
        try {
            DocumentBuilder docBuilder = docFactory.newDocumentBuilder();
            Document doc = docBuilder.newDocument();
            Element rootElement = doc.createElement("EditorConfig");

            setTextValue(doc, rootElement, "Version", COURSE_EDITOR_VERSION);
            setBooleanValue(doc, rootElement, "ShowUpdateMessage", bShowUpdateMessage);
            setBooleanValue(doc, rootElement, "AutoSave_Enabled", bAutoSaveEnabled);
            setIntegerValue(doc, rootElement, "AutoSave_Interval", autoSaveInterval);
            setIntegerValue(doc, rootElement, "AutoSave_Slots", maxAutoSaveSlots);
            setIntegerValue(doc, rootElement, "AutoSave_Last_Used_Slot", autoSaveLastUsedSlot);
            setTextValue(doc, rootElement, "LastUsedLocation", lastUsedLocation);
            setIntegerValue(doc, rootElement, "WindowX", editor.getBounds().x);
            setIntegerValue(doc, rootElement, "WindowY", editor.getBounds().y);
            setIntegerValue(doc, rootElement, "WindowWidth", editor.getBounds().width);
            setIntegerValue(doc, rootElement, "WindowHeight", editor.getBounds().height);
            setIntegerValue(doc, rootElement, "MaxZoomLevel", maxZoomLevel);
            setFloatValue(doc, rootElement, "NodeSizeScale", nodeSize);
            setBooleanValue(doc, rootElement, "TextPanelVisible", bTextPanelVisible);
            setBooleanValue(doc, rootElement, "UndoRedoVisible", bUndoRedoVisible);
            setBooleanValue(doc, rootElement, "CopyPasteVisible", bCopyPasteVisible);
//            bCopyPasteVisible
            setBooleanValue(doc, rootElement, "Check_Online_MapImages", bUseOnlineMapImages);
            setBooleanValue(doc, rootElement, "Continuous_Connection", bContinuousConnections);
            setBooleanValue(doc, rootElement, "MiddleMouseMove", bMiddleMouseMove);
            setIntegerValue(doc, rootElement, "CurveSliderMaximum", curveSliderMax);
            setIntegerValue(doc, rootElement, "CurveSliderDefault", curveIterationsDefault);
            if (curveIterationsDefault > curveSliderMax) curveIterationsDefault = curveSliderMax;
            setIntegerValue(doc, rootElement, "CurveNodeDefaultPriority", curveNodeDefaultPriority);
            setEnumValue(doc, rootElement, "CurveDefaultConnection", curveDefaultConnection);
            setIntegerValue(doc, rootElement, "ControlPointMoveScaler", controlPointMoveScaler);
            setIntegerValue(doc, rootElement, "NumControlPoints", curveControlPointDefault);
            setIntegerValue(doc, rootElement, "MaxControlPoints", maxControlPoints);
            setBooleanValue(doc, rootElement, "ShowGrid", bShowGrid);
            setBooleanValue(doc, rootElement, "GridSnapEnabled", bGridSnapEnabled);
            setBooleanValue(doc, rootElement, "SnapSubDivision", bGridSnapSubs);
            setIntegerValue(doc, rootElement, "RotationStep", rotationStep);
            setBooleanValue(doc, rootElement, "RotationSnapEnabled", bRotationSnapEnabled);
            setBooleanValue(doc, rootElement, "FilledConnectionArrows", bFilledArrows);
            setFloatValue(doc, rootElement, "HiddenNodeTransparency", hiddenNodesTransparencyLevel);
            setBooleanValue(doc, rootElement, "CreateLinearLineEndNode", bCreateLinearLineEndNode);
            setIntegerValue(doc, rootElement, "LinearLineNodeDistance", linearLineNodeDistance);
            setBooleanValue(doc, rootElement, "UseRectangularSelection", bUseRectangularSelection);
            setBooleanValue(doc, rootElement, "ShowMarkerNames", bShowMarkerNames);
            setBooleanValue(doc, rootElement, "ShowSelectedNodeID", bShowSelectedNodeID);
            setBooleanValue(doc, rootElement, "SelectHidden", bSelectHidden);
            setBooleanValue(doc, rootElement, "ShowMarkerIcons", bShowMarkerIcons);
            setBooleanValue(doc, rootElement, "ShowParkingIcons", bShowParkingIcons);
            setBooleanValue(doc, rootElement, "InterpolateZoom", bInterpolateZoom);
            doc.appendChild(rootElement);

            // Save all the colours

            Element colorElement = doc.createElement("EditorColours");
            setColorValue(doc, colorElement, "Colour_Node_Regular", colourNodeRegular);
            setColorValue(doc, colorElement, "Colour_Node_Subprio", colourNodeSubprio);
            setColorValue(doc, colorElement, "Colour_Node_Selected", colourNodeSelected);
            setColorValue(doc, colorElement, "Colour_Node_Control", colourNodeControl);
            setColorValue(doc, colorElement, "Colour_Connection_Regular", colourConnectRegular);
            setColorValue(doc, colorElement, "Colour_Connection_Subprio", colourConnectSubprio);
            setColorValue(doc, colorElement, "Colour_Connection_Dual", colourConnectDual);
            setColorValue(doc, colorElement, "Colour_Connection_Dual_Subprio", colourConnectDualSubprio);
            setColorValue(doc, colorElement, "Colour_Connection_Reverse", colourConnectReverse);
            setColorValue(doc, colorElement, "Colour_Connection_Reverse_Subprio", colourConnectReverseSubprio);
            setColorValue(doc, colorElement, "Colour_Grid", colourGridLines);
            rootElement.appendChild(colorElement);

            // Save all move widget options

            Element moveWidgetElement = doc.createElement("MoveWidget");
            setByteValue(doc, moveWidgetElement, "AxisLength", axisLength);
            setByteValue(doc, moveWidgetElement, "AxisWidth", axisWidth);
            setColorValue(doc, moveWidgetElement, "X_AxisColor", xAxisColor);
            setColorValue(doc, moveWidgetElement, "Y_AxisColor", yAxisColor);
            setByteValue(doc, moveWidgetElement, "ArrowLength", arrowLength);
            setByteValue(doc, moveWidgetElement, "ArrowWidth", arrowWidth);
            setEnumValue(doc, moveWidgetElement, "X_Direction", xAxisDirection);
            setEnumValue(doc, moveWidgetElement, "Y_Direction", yAxisDirection);
            setEnumValue(doc, moveWidgetElement, "FreeMoveLocation", freeMovePosition);
            setIntegerValue(doc, moveWidgetElement, "FreeMoveOffsetX", freeMoveOffsetX);
            setIntegerValue(doc, moveWidgetElement, "FreeMoveOffsetY", freeMoveOffsetY);
            setEnumValue(doc, moveWidgetElement, "FreeMoveType", freeMoveType);
            setEnumValue(doc, moveWidgetElement, "FreeMoveStyle", freeMoveStyle);
            setEnumValue(doc, moveWidgetElement, "FreeMoveSize", freeMoveSize);
            setIntegerValue(doc, moveWidgetElement, "FreeMoveDefaultSmall", freeMoveDefaultSmall);
            setIntegerValue(doc, moveWidgetElement, "FreeMoveDefaultMedium", freeMoveDefaultMedium);
            setIntegerValue(doc, moveWidgetElement, "FreeMoveDefaultLarge", freeMoveDefaultLarge);
            setColorValue(doc, moveWidgetElement, "FreeMoveColor", freeMoveColor);

            rootElement.appendChild(moveWidgetElement);

            // Save all the shortcuts

            Element shortcutsElement = doc.createElement("Shortcuts");
            for (Shortcut shortcut : getAllShortcuts()) {
                if (bDebugLogShortcutInfo) LOG.info("Saving shortcut: {}", shortcut);
                Element actionElement = doc.createElement("KeyBind");
                actionElement.setAttribute("ID", String.valueOf(shortcut.getId().getXmlDescriptor()));

                String keyString = (shortcut.getKeyCode() != 0) ? shortcut.getKeyVK() : "";
                actionElement.setAttribute("Key", keyString);

                String modifiersString = InputEvent.getModifiersExText(shortcut.getModifier());
                actionElement.setAttribute("Modifier", modifiersString);

                actionElement.setAttribute("Name", shortcut.getLocalizedString());

                shortcutsElement.appendChild(actionElement);
                if (bDebugLogShortcutInfo) LOG.info("adding Shortcut: {} -> Key: {} -> Modifiers: {}", shortcut.getId(), keyString, modifiersString);
            }
            rootElement.appendChild(shortcutsElement);

            // Store all the recent files list

            Element recentFile = doc.createElement("RecentFiles");
            for (RecentFilesMenu.RecentEntry entry : recentFilesList) {
                Element entryElement = doc.createElement("Recent");
                entryElement.setAttribute("Path", entry.getConfigPath());
                entryElement.setAttribute("Type", String.valueOf(entry.getConfigType()));
                recentFile.appendChild(entryElement);
            }
            rootElement.appendChild(recentFile);

            // store all the known map settings
            Element knownMapList = doc.createElement("KnownMapSettings");
            for (MapInfoStore mapInfo : EditorXML.knownMapList) {
                // createSetting the map element
                Element mapElement = doc.createElement("Map");
                // add the map name Attribute
                mapElement.setAttribute("Name", mapInfo.getMapName());
                // store the map scale
                setIntegerValue(doc, mapElement, "MapScale", mapInfo.getMapScale());
                // store the node size
                setFloatValue(doc, mapElement, "NodeSize", mapInfo.getNodeSize());
                // store the grid settings
                setEnumValue(doc, mapElement, "GridType", mapInfo.getGridType());
                // store the custom grid settings if needed
                if (mapInfo.getGridType() == GRID_TYPE.GRID_CUSTOM) {
                    setTextValue(doc, mapElement, "CustomGridX", String.valueOf(mapInfo.getGridX()));
                    setTextValue(doc, mapElement, "CustomGridY", String.valueOf(mapInfo.getGridY()));
                    setTextValue(doc, mapElement, "CustomSubDivisions", String.valueOf(mapInfo.getGridSubDiv()));
                }
                setTextValue(doc, mapElement, "MaxZoomLevel", String.valueOf(mapInfo.getMaxZoomLevel()));

                knownMapList.appendChild(mapElement);
            }
            rootElement.appendChild(knownMapList);


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
                    LOG.info("Completed saving Editor Config");
                } catch (IOException ioe) {
                    LOG.error("Editor config could not be saved.. check file/folder access permissions");
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
        private GRID_TYPE gridType;
        private float nodeSize;
        private float gridX;
        private float gridY;
        private int gridSubDiv;
        private int maxZoomLevel;

        public MapInfoStore(String mapName, int mapScale, float nodeSize, GRID_TYPE gridType, float gridX, float gridY, int subDiv, int maxZoomLevel) {
            this.mapName = mapName;
            this.mapScale = mapScale;
            this.nodeSize = nodeSize;
            this.gridType = gridType;
            this.gridX = gridX;
            this.gridY = gridY;
            this.gridSubDiv = subDiv;
            this.maxZoomLevel = maxZoomLevel;
        }

        // getters

        public String getMapName() { return this.mapName; }
        public int getMapScale() { return this.mapScale; }
        public float getNodeSize() { return this.nodeSize; }
        public GRID_TYPE getGridType() { return this.gridType; }
        public float getGridX() { return this.gridX; }
        public float getGridY() { return this.gridY; }
        public int getGridSubDiv() { return this.gridSubDiv; }
        public int getMaxZoomLevel() { return this.maxZoomLevel; }

        // setters

        public void setMapScale(int newMapScale) { this.mapScale = newMapScale; }
        public void setNodeSize(float newNodeSize) { this.nodeSize = newNodeSize; }
        public void setGridSettings(GRID_TYPE gridType, float newGridX, float newGridY, int newGridSubDiv) {
            this.gridType = gridType;
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
                    setCurrentMapNameLabel(store.getMapName());
                    setNewMapScale(store.getMapScale());
                    LOG.info("  --> updated map scale to {}x", mapScale);
                    setNewNodeSize(store.getNodeSize());
                    LOG.info("  --> updated node size to {}x", nodeSize);
                    setNewGridValues(store.gridType, store.getGridX(), store.getGridY(), store.getGridSubDiv());
                    LOG.info("  --> updated grid settings to Type = {} , X = {} , Y = {}, SubDiv = {}", store.getGridType(), store.getGridX(), store.getGridY(), store.getGridSubDiv());
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
            knownMapList.add(new MapInfoStore(mapName, 1, 2f, GRID_TYPE.GRID_2x2, 2f,2f,1, 30));
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

    public static void setNewGridValues(GRID_TYPE type, float newGridX, float newGridY, int newGridSubDiv) {
        if (roadMap != null) {
            switch (type) {
                case GRID_2x2:
                    gridSpacingX = 2;
                    gridSpacingY = 2;
                    gridSubDivisions = 1;
                    break;
                case GRID_4x4:
                    gridSpacingX = 4;
                    gridSpacingY = 4;
                    gridSubDivisions = 1;
                    break;
                case GRID_CUSTOM:
                    gridSpacingX = limitFloatToDecimalPlaces(newGridX, 1, RoundingMode.HALF_UP);
                    gridSpacingY = limitFloatToDecimalPlaces(newGridY, 1, RoundingMode.HALF_UP);
                    gridSubDivisions = newGridSubDiv;
                    break;
                default:
                    gridSpacingX = 1;
                    gridSpacingY = 1;
                    gridSubDivisions = 1;
                    break;
            }
            updateGridPanelSettings(type, gridSpacingX, gridSpacingY, gridSubDivisions);
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
