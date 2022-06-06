package AutoDriveEditor.GUI;

import AutoDriveEditor.Listeners.MenuListener;

import javax.swing.*;
import java.awt.event.InputEvent;
import java.awt.event.KeyEvent;

import static AutoDriveEditor.AutoDriveEditor.DEBUG;
import static AutoDriveEditor.AutoDriveEditor.EXPERIMENTAL;
import static AutoDriveEditor.GUI.GUIBuilder.*;
import static AutoDriveEditor.Utils.GUIUtils.*;
import static AutoDriveEditor.XMLConfig.EditorXML.*;

public class MenuBuilder {

    public static final String MENU_LOAD_CONFIG = "Load Config";
    public static final String MENU_SAVE_CONFIG = "Save Config";
    public static final String MENU_SAVE_SAVEAS = "Save As";
    public static final String MENU_LOAD_ROUTES_MANAGER_CONFIG = "Load RoutesManager Config";
    public static final String MENU_LOAD_ROUTES_MANAGER_XML = "Load RoutesXML Config";
    public static final String MENU_SAVE_ROUTES_MANAGER_XML = "Save RoutesXML Config";
    public static final String MENU_EXIT = "Exit";
    public static final String MENU_LOAD_IMAGE = "Load Map Image";
    public static final String MENU_SAVE_IMAGE = "Save Map Image";
    public static final String MENU_IMPORT_FS19_DDS = "Import FS19 DDS";
    public static final String MENU_IMPORT_FS22_DDS = "Import FS22 DDS";
    public static final String MENU_EDIT_UNDO = "Undo";
    public static final String MENU_EDIT_REDO = "Redo";
    public static final String MENU_EDIT_CUT = "Cut";
    public static final String MENU_EDIT_COPY = "Copy";
    public static final String MENU_EDIT_PASTE = "Paste";
    public static final String MENU_ZOOM_2km = "2km";
    public static final String MENU_ZOOM_4km = "4km";
    public static final String MENU_ZOOM_6km = "6km";
    public static final String MENU_ZOOM_8km = "8km";
    public static final String MENU_ZOOM_10km = "10km";
    public static final String MENU_ZOOM_12km = "12km";
    public static final String MENU_ZOOM_14km = "14km";
    public static final String MENU_ZOOM_16km = "16km";
    public static final String MENU_ZOOM_18km = "18km";
    public static final String MENU_ZOOM_20km = "20km";
    public static final String MENU_ZOOM_22km = "22km";
    public static final String MENU_ZOOM_24km = "24km";
    public static final String MENU_ZOOM_26km = "26km";
    public static final String MENU_ZOOM_28km = "28km";
    public static final String MENU_ZOOM_30km = "30km";
    public static final String MENU_ZOOM_32km = "32km";
    public static final String MENU_ZOOM_34km = "34km";
    public static final String MENU_ZOOM_36km = "36km";
    public static final String MENU_ZOOM_38km = "38km";
    public static final String MENU_ZOOM_40km = "40km";
    public static final String MENU_ZOOM_42km = "42km";
    public static final String MENU_ZOOM_44km = "44km";
    public static final String MENU_ZOOM_46km = "45km";
    public static final String MENU_HEIGHTMAP_LOAD = "Load HeightMap";
    public static final String MENU_HEIGHTMAP_SAVE = "Save Heightmap";
    public static final String MENU_HEIGHTMAP_SHOW = "Show HeightMap";
    public static final String MENU_HEIGHTMAP_FIX = "Fix Node Height";
    public static final String MENU_GRID_SET = "Grid Set";
    public static final String MENU_GRID_SHOW = "Grid Show";
    public static final String MENU_GRID_SNAP = "Grid Snap";
    public static final String MENU_GRID_SNAP_SUBS = "Grid Snap Subs";
    public static final String MENU_ROTATE_SET = "Set Rotate Step";
    public static final String MENU_ROTATE_CLOCKWISE_NINTY ="Rotate 90 Clockwise";
    public static final String MENU_ROTATE_ANTICLOCKWISE_NINTY ="Rotate 90 Anticlockwise";
    public static final String MENU_ROTATE_CLOCKWISE="Rotate Clockwise";
    public static final String MENU_ROTATE_ANTICLOCKWISE="Rotate Anticlockwise";
    public static final String MENU_SCAN_OVERLAP="Scan Overlap";
    public static final String MENU_SCAN_MERGE="Merge Overlap";
    public static final String MENU_ABOUT = "About";
    public static final String MENU_DEBUG_ENABLE = "Enable Debug";

    public static final String MENU_DEBUG_MOVETO_NODE = "DEBUG MOVETO NODE";
    public static final String MENU_DEBUG_SHOWID = "DEBUG ID";
    public static final String MENU_DEBUG_SELECTED_LOCATION = "DEBUG SELECTED LOCATION";
    public static final String MENU_DEBUG_PROFILE = "DEBUG PROFILE";
    public static final String MENU_DEBUG_HEIGHTMAP = "DEBUG HEIGHTMAP";
    public static final String MENU_DEBUG_TEST = "TEST";

    public static final String MENU_DEBUG_LOG_FILEIO = "LOG FILEIO";
    public static final String MENU_DEBUG_LOG_UNDO = "LOG UNDO/REDO SYSTEM";
    public static final String MENU_DEBUG_LOG_ZOOMSCALE = "LOG ZOOMSCALE";
    public static final String MENU_DEBUG_LOG_MERGE = "LOG MERGE";
    public static final String MENU_DEBUG_LOG_ROUTEMANAGER = "LOG ROUTEMANAGER";
    public static final String MENU_DEBUG_LOG_HEIGHTMAP = "LOG HEIGHTMAP";
    public static final String MENU_DEBUG_LOG_CURVEINFO = "LOG CURVE";
    public static final String MENU_DEBUG_LOG_MARKERS = "LOG MARKERS";
    public static final String MENU_DEBUG_LOG_RENDER = "LOG RENDER";
    public static final String MENU_DEBUG_LOG_GUI = "LOG GUI";
    public static final String MENU_DEBUG_LOG_CONFIG = "LOG CONFIG";
    public static final String MENU_DEBUG_LOG_COPYPASTE = "LOG COPYPASTE";




    public static int InputEvent_NONE = 0;
    public static int KeyEvent_NONE = 0;

    public static MenuListener menuListener;

    public static JMenu fileMenu, editMenu, mapMenu, routesMenu, heightmapMenu,
                        helpMenu, subMenu, gridMenu, rotationMenu, fixItMenu, debugMenu;
    public static JMenuBar menuBar;
    public static JMenuItem loadImageMenuItem;
    public static JMenuItem importFS19DDSMenuItem;
    public static JMenuItem importFS22DDSMenuItem;
    public static JMenuItem saveImageMenuItem;
    public static JMenuItem saveConfigMenuItem;
    public static JMenuItem saveConfigAsMenuItem;
    public static JMenuItem undoMenuItem;
    public static JMenuItem redoMenuItem;
    public static JMenuItem cutMenuItem;
    public static JMenuItem copyMenuItem;
    public static JMenuItem pasteMenuItem;
    public static JMenuItem pasteOriginalLocationMenuItem;

    public static JMenuItem zoom2km;
    public static JMenuItem zoom4km;
    public static JMenuItem zoom6km;
    public static JMenuItem zoom8km;
    public static JMenuItem zoom10km;
    public static JMenuItem zoom12km;
    public static JMenuItem zoom14km;
    public static JMenuItem zoom16km;
    public static JMenuItem zoom18km;
    public static JMenuItem zoom20km;
    public static JMenuItem zoom22km;
    public static JMenuItem zoom24km;
    public static JMenuItem zoom26km;
    public static JMenuItem zoom28km;
    public static JMenuItem zoom30km;
    public static JMenuItem zoom32km;
    public static JMenuItem zoom34km;
    public static JMenuItem zoom36km;
    public static JMenuItem zoom38km;
    public static JMenuItem zoom40km;
    public static JMenuItem zoom42km;
    public static JMenuItem zoom44km;
    public static JMenuItem zoom46km;

    public static JMenuItem importHeightmapMenuItem;
    public static JMenuItem exportHeightMapMenuItem;
    public static JMenuItem gridSnapMenuItem;
    public static JMenuItem gridSnapSubDivisionMenuItem;

    public static JMenuItem loadRoutesConfig;
    public static JMenuItem loadRoutesXML;
    public static JMenuItem saveRoutesXML;

    public static JMenuItem rClockwiseMenuItem;
    public static JMenuItem r90ClockwiseMenuItem;
    public static JMenuItem rAntiClockwiseMenuItem;
    public static JMenuItem r90AntiClockwiseMenuItem;

    public static JMenuItem fixNodesHeightMenuItem;
    public static JMenuItem showHeightMapMenuItem;
    public static JMenuItem scanNetworkMenuItem;
    public static JMenuItem mergeNodesMenuItem;

    public static boolean bShowHeightMap;
    public static boolean bDebugEnable;
    public static boolean bDebugShowID;
    public static boolean bDebugShowSelectedLocation;
    public static boolean bDebugProfile;
    public static boolean bDebugShowHeightMapInfo;
    public static boolean bDebugTest;

    public static boolean bDebugLogZoomScale;
    public static boolean bDebugLogFileIO;
    public static boolean bDebugLogUndoRedo;
    public static boolean bDebugLogMerge;
    public static boolean bDebugLogRouteManager;
    public static boolean bDebugLogHeightMapInfo;
    public static boolean bDebugLogCurveInfo;
    public static boolean bDebugLogMarkerInfo;
    public static boolean bDebugLogRenderInfo;
    public static boolean bDebugLogGUIInfo;
    public static boolean bDebugLogCopyPasteInfo;
    public static boolean bDebugLogConfigInfo;

    public static void createMenu() {
        //JMenuItem menuItem;

        menuBar = new JMenuBar();
        menuListener = new MenuListener();

        // Create the file Menu

        fileMenu = makeMenu("menu_file", KeyEvent.VK_F, "menu_file_accstring", menuBar);
        makeMenuItem("menu_file_loadconfig",  "menu_file_loadconfig_accstring", KeyEvent.VK_L, InputEvent.ALT_DOWN_MASK, fileMenu, menuListener, MENU_LOAD_CONFIG, true );
        saveConfigMenuItem = makeMenuItem("menu_file_saveconfig",  "menu_file_saveconfig_accstring", KeyEvent.VK_S, InputEvent.ALT_DOWN_MASK, fileMenu, menuListener, MENU_SAVE_CONFIG, false );
        saveConfigAsMenuItem = makeMenuItem("menu_file_saveasconfig", "menu_file_saveasconfig_accstring",  KeyEvent.VK_A, InputEvent.ALT_DOWN_MASK,fileMenu, menuListener,MENU_SAVE_SAVEAS, false );
        makeMenuItem("menu_file_exit",  "menu_file_exit_accstring", KeyEvent.VK_Q, InputEvent.ALT_DOWN_MASK, fileMenu, menuListener, MENU_EXIT, true );

        // Create the edit menu

        routesMenu = makeMenu("menu_routes", KeyEvent.VK_M, "menu_routes_accstring", menuBar);
        loadRoutesConfig = makeMenuItem("menu_routes_load_config", "menu_routes_load_config_accstring", routesMenu, menuListener, MENU_LOAD_ROUTES_MANAGER_CONFIG, true);
        loadRoutesXML = makeMenuItem("menu_routes_load_xml", "menu_routes_load_xml_accstring", routesMenu, menuListener, MENU_LOAD_ROUTES_MANAGER_XML, true );
        saveRoutesXML = makeMenuItem("menu_routes_save_xml", "menu_routes_save_xml_accstring", routesMenu, menuListener, MENU_SAVE_ROUTES_MANAGER_XML, false );

        // Create the edit menu

        editMenu = makeMenu("menu_edit", KeyEvent.VK_E, "menu_edit_accstring", menuBar);

        // Create the Undo/Redo menu

        undoMenuItem = makeMenuItem("menu_edit_undo",  "menu_edit_undo_accstring", KeyEvent.VK_Z, InputEvent.CTRL_DOWN_MASK, editMenu, menuListener, MENU_EDIT_UNDO, false );
        redoMenuItem = makeMenuItem("menu_edit_redo",  "menu_edit_redo_accstring", KeyEvent.VK_Z, InputEvent.SHIFT_DOWN_MASK, editMenu, menuListener, MENU_EDIT_REDO, false );
        cutMenuItem = makeMenuItem("menu_edit_cut",  "menu_edit_cut_accstring", KeyEvent.VK_X, InputEvent.CTRL_DOWN_MASK, editMenu, menuListener, BUTTON_COPYPASTE_CUT, false );
        copyMenuItem = makeMenuItem("menu_edit_copy",  "menu_edit_copy_accstring", KeyEvent.VK_C, InputEvent.CTRL_DOWN_MASK, editMenu, menuListener, BUTTON_COPYPASTE_COPY, false );
        pasteMenuItem = makeMenuItem("menu_edit_paste",  "menu_edit_paste_accstring", KeyEvent.VK_V, InputEvent.CTRL_DOWN_MASK, editMenu, menuListener, BUTTON_COPYPASTE_PASTE, false );
        pasteOriginalLocationMenuItem = makeMenuItem("menu_edit_paste_original_location",  "menu_edit_paste_original_location_accstring", KeyEvent.VK_V, InputEvent.SHIFT_DOWN_MASK, editMenu, menuListener, BUTTON_COPYPASTE_PASTE_ORIGINAL, false );

        // Create the Map Menu and it's scale sub menu

        mapMenu = makeMenu("menu_map", KeyEvent.VK_M, "menu_map_accstring", menuBar);
        loadImageMenuItem = makeMenuItem("menu_map_loadimage", "menu_map_loadimage_accstring", KeyEvent.VK_M, InputEvent.ALT_DOWN_MASK,mapMenu,menuListener, MENU_LOAD_IMAGE, false );

        mapMenu.addSeparator();
        subMenu = makeSubMenu("menu_map_scale", KeyEvent.VK_M, "menu_map_scale_accstring", mapMenu);
        ButtonGroup menuZoomGroup = new ButtonGroup();
        zoom2km = makeSimpleRadioButtonMenuItem("2km ( 1x )", "menu_map_scale_simplebutton_accstring", subMenu, menuListener, MENU_ZOOM_2km,true, menuZoomGroup, true);
        zoom4km = makeSimpleRadioButtonMenuItem("4km ( 4x )", "menu_map_scale_simplebutton_accstring", subMenu, menuListener, MENU_ZOOM_4km,true, menuZoomGroup, false);
        zoom6km = makeSimpleRadioButtonMenuItem("6km ( 8x )", "menu_map_scale_simplebutton_accstring", subMenu, menuListener, MENU_ZOOM_6km, true, menuZoomGroup, false);
        zoom8km = makeSimpleRadioButtonMenuItem("8km ( 16x )", "menu_map_scale_simplebutton_accstring", subMenu, menuListener, MENU_ZOOM_8km, true, menuZoomGroup, false);
        zoom10km = makeSimpleRadioButtonMenuItem("10km ( 32x )", "menu_map_scale_simplebutton_accstring", subMenu, menuListener, MENU_ZOOM_10km, true, menuZoomGroup, false);
        zoom12km = makeSimpleRadioButtonMenuItem("12km ( 64x )", "menu_map_scale_simplebutton_accstring", subMenu, menuListener, MENU_ZOOM_12km, true, menuZoomGroup, false);
        zoom14km = makeSimpleRadioButtonMenuItem("14km ( 128x )", "menu_map_scale_simplebutton_accstring", subMenu, menuListener, MENU_ZOOM_14km, true, menuZoomGroup, false);
        zoom16km = makeSimpleRadioButtonMenuItem("16km ( 256x )", "menu_map_scale_simplebutton_accstring", subMenu, menuListener, MENU_ZOOM_16km, true, menuZoomGroup, false);
        zoom18km = makeSimpleRadioButtonMenuItem("18km ( 512x )", "menu_map_scale_simplebutton_accstring", subMenu, menuListener, MENU_ZOOM_18km, true, menuZoomGroup, false);
        zoom20km = makeSimpleRadioButtonMenuItem("20km ( 1024x )", "menu_map_scale_simplebutton_accstring", subMenu, menuListener, MENU_ZOOM_20km, true, menuZoomGroup, false);
        zoom22km = makeSimpleRadioButtonMenuItem("22km", "menu_map_scale_simplebutton_accstring", subMenu, menuListener, MENU_ZOOM_22km, true, menuZoomGroup, false);
        zoom24km = makeSimpleRadioButtonMenuItem("24km", "menu_map_scale_simplebutton_accstring", subMenu, menuListener, MENU_ZOOM_24km, true, menuZoomGroup, false);
        zoom26km = makeSimpleRadioButtonMenuItem("26km", "menu_map_scale_simplebutton_accstring", subMenu, menuListener, MENU_ZOOM_26km, true, menuZoomGroup, false);
        zoom28km = makeSimpleRadioButtonMenuItem("28km", "menu_map_scale_simplebutton_accstring", subMenu, menuListener, MENU_ZOOM_28km, true, menuZoomGroup, false);
        zoom30km = makeSimpleRadioButtonMenuItem("30km", "menu_map_scale_simplebutton_accstring", subMenu, menuListener, MENU_ZOOM_30km, true, menuZoomGroup, false);
        zoom32km = makeSimpleRadioButtonMenuItem("32km", "menu_map_scale_simplebutton_accstring", subMenu, menuListener, MENU_ZOOM_32km, true, menuZoomGroup, false);
        zoom34km = makeSimpleRadioButtonMenuItem("34km", "menu_map_scale_simplebutton_accstring", subMenu, menuListener, MENU_ZOOM_34km, true, menuZoomGroup, false);
        zoom36km = makeSimpleRadioButtonMenuItem("36km", "menu_map_scale_simplebutton_accstring", subMenu, menuListener, MENU_ZOOM_36km, true, menuZoomGroup, false);
        zoom38km = makeSimpleRadioButtonMenuItem("38km", "menu_map_scale_simplebutton_accstring", subMenu, menuListener, MENU_ZOOM_38km, true, menuZoomGroup, false);
        zoom40km = makeSimpleRadioButtonMenuItem("40km", "menu_map_scale_simplebutton_accstring", subMenu, menuListener, MENU_ZOOM_40km, true, menuZoomGroup, false);
        zoom42km = makeSimpleRadioButtonMenuItem("42km", "menu_map_scale_simplebutton_accstring", subMenu, menuListener, MENU_ZOOM_42km, true, menuZoomGroup, false);
        zoom44km = makeSimpleRadioButtonMenuItem("44km", "menu_map_scale_simplebutton_accstring", subMenu, menuListener, MENU_ZOOM_44km, true, menuZoomGroup, false);
        zoom46km = makeSimpleRadioButtonMenuItem("46km", "menu_map_scale_simplebutton_accstring", subMenu, menuListener, MENU_ZOOM_46km, true, menuZoomGroup, false);
        mapMenu.addSeparator();
        importFS19DDSMenuItem = makeMenuItem("menu_import_fs19_dds", "menu_import_fs19_dds_accstring", KeyEvent.VK_I, InputEvent.ALT_DOWN_MASK, mapMenu, menuListener, MENU_IMPORT_FS19_DDS, false);
        importFS22DDSMenuItem = makeMenuItem("menu_import_fs22_dds", "menu_import_fs22_dds_accstring", KeyEvent.VK_P, InputEvent.ALT_DOWN_MASK, mapMenu, menuListener, MENU_IMPORT_FS22_DDS, false);
        saveImageMenuItem = makeMenuItem("menu_map_saveimage", "menu_map_saveimage_accstring", KeyEvent.VK_B, InputEvent.ALT_DOWN_MASK, mapMenu, menuListener, MENU_SAVE_IMAGE, false);

        // create the HeightMap menu

        heightmapMenu = makeMenu("menu_heightmap", KeyEvent.VK_T, "menu_heightmap_accstring", menuBar);
        importHeightmapMenuItem = makeMenuItem("menu_heightmap_import", "menu_heightmap_import_accstring", heightmapMenu, menuListener, MENU_HEIGHTMAP_LOAD,false);
        exportHeightMapMenuItem = makeMenuItem("menu_heightmap_export", "menu_heightmap_export_accstring", heightmapMenu, menuListener, MENU_HEIGHTMAP_SAVE, false);
        heightmapMenu.addSeparator();
        showHeightMapMenuItem = makeCheckBoxMenuItem("menu_heightmap_show", "menu_heightmap_show", bShowHeightMap, heightmapMenu, menuListener, MENU_HEIGHTMAP_SHOW, false);
        fixNodesHeightMenuItem = makeMenuItem("menu_heightmap_fix_nodes", "menu_heightmap_fix_nodes_accstring", heightmapMenu, menuListener, MENU_HEIGHTMAP_FIX,false);

        // create the grid snap menu

        gridMenu = makeMenu("menu_grid", KeyEvent.VK_G, "menu_grid_accstring", menuBar);
        makeCheckBoxMenuItem("menu_grid_show", "menu_grid_show_accstring", KeyEvent.VK_G, bShowGrid, gridMenu, menuListener, MENU_GRID_SHOW, true);
        gridSnapMenuItem = makeCheckBoxMenuItem("menu_grid_snap", "menu_grid_snap_accstring", KeyEvent.VK_S, bGridSnap, gridMenu, menuListener, MENU_GRID_SNAP, true);
        gridSnapSubDivisionMenuItem = makeCheckBoxMenuItem("menu_grid_snap_subdivide", "menu_grid_snap_subdivide_accstring", KeyEvent.VK_D, bGridSnapSubs, gridMenu, menuListener, MENU_GRID_SNAP_SUBS, true);
        gridMenu.addSeparator();
        makeMenuItem("menu_grid_set_size", "menu_grid_set_size_accstring", KeyEvent.VK_F, InputEvent.ALT_DOWN_MASK, gridMenu, menuListener, MENU_GRID_SET, true );

        // Create the Rotation Menu

        rotationMenu = makeMenu("menu_rotate", KeyEvent.VK_R, "menu_rotate_accstring", menuBar);
        rClockwiseMenuItem = makeMenuItem("menu_rotate_clockwise", "menu_rotate_clockwise_accstring", KeyEvent.VK_T, InputEvent.CTRL_DOWN_MASK, rotationMenu, menuListener, MENU_ROTATE_CLOCKWISE, false );
        r90ClockwiseMenuItem = makeMenuItem("menu_rotate_clockwise_ninety", "menu_rotate_clockwise_ninety_accstring", KeyEvent.VK_T, InputEvent.SHIFT_DOWN_MASK, rotationMenu, menuListener, MENU_ROTATE_CLOCKWISE_NINTY, false );
        rAntiClockwiseMenuItem = makeMenuItem("menu_rotate_anticlockwise", "menu_rotate_anticlockwise_accstring", KeyEvent.VK_R, InputEvent.CTRL_DOWN_MASK, rotationMenu, menuListener, MENU_ROTATE_ANTICLOCKWISE, false );
        r90AntiClockwiseMenuItem = makeMenuItem("menu_rotate_anticlockwise_ninety", "menu_rotate_anticlockwise_ninety_accstring", KeyEvent.VK_R, InputEvent.SHIFT_DOWN_MASK, rotationMenu, menuListener, MENU_ROTATE_ANTICLOCKWISE_NINTY, false );
        rotationMenu.addSeparator();
        makeMenuItem("menu_rotate_set_step", "menu_rotate_set_step_accstring", KeyEvent.VK_Y, InputEvent.SHIFT_DOWN_MASK, rotationMenu, menuListener, MENU_ROTATE_SET, true );

        // Create the FixIt menu

        fixItMenu = makeMenu("menu_scan", KeyEvent.VK_S, "menu_scan_accstring", menuBar);
        scanNetworkMenuItem = makeMenuItem("menu_scan_overlap", "menu_scan_overlap_accstring", fixItMenu, menuListener, MENU_SCAN_OVERLAP, false);
        mergeNodesMenuItem = makeMenuItem("menu_scan_merge", "menu_scan_merge_accstring", fixItMenu, menuListener, MENU_SCAN_MERGE, false);

        // Create the Help menu

        helpMenu = makeMenu("menu_help", KeyEvent.VK_H, "menu_help_accstring", menuBar);
        makeMenuItem("menu_help_about", "menu_help_about_accstring", KeyEvent.VK_H, InputEvent.ALT_DOWN_MASK, helpMenu, menuListener, MENU_ABOUT, true);
        makeCheckBoxMenuItem("menu_help_debug", "menu_help_debug_accstring", bDebugEnable, helpMenu, menuListener, MENU_DEBUG_ENABLE, true);


        debugMenu = makeMenu("menu_debug", KeyEvent.VK_D, "menu_debug_accstring", menuBar);
        debugMenu.setVisible(DEBUG);
        makeMenuItem("menu_debug_movetonode", "menu_debug_movetonode_accstring", KeyEvent.VK_F, InputEvent_NONE, debugMenu, menuListener, MENU_DEBUG_MOVETO_NODE, true);
        debugMenu.addSeparator();
        makeCheckBoxMenuItem("menu_debug_showID", "menu_debug_showID_accstring", KeyEvent.VK_6, InputEvent.ALT_DOWN_MASK, bDebugShowID, debugMenu, menuListener, MENU_DEBUG_SHOWID, true);
        makeCheckBoxMenuItem("menu_debug_shownodelocationinfo", "menu_debug_shownodelocationinfo_accstring", KeyEvent.VK_7, InputEvent.ALT_DOWN_MASK, bDebugShowSelectedLocation, debugMenu, menuListener, MENU_DEBUG_SELECTED_LOCATION, true);
        makeCheckBoxMenuItem("menu_debug_profile", "menu_debug_profile_accstring", bDebugProfile, debugMenu, menuListener, MENU_DEBUG_PROFILE, true);
        makeCheckBoxMenuItem("menu_debug_heightmap", "menu_debug_heightmap_accstring", bDebugShowHeightMapInfo, debugMenu, menuListener, MENU_DEBUG_HEIGHTMAP, true);
        if (EXPERIMENTAL) {
            makeCheckBoxMenuItem("menu_debug_test", "menu_debug_test_accstring", bDebugTest, debugMenu, menuListener, MENU_DEBUG_TEST, true);
        }
        debugMenu.addSeparator();
        makeCheckBoxMenuItem("menu_debug_log_zoom", "menu_debug_log_zoom_accstring", bDebugLogZoomScale, debugMenu, menuListener, MENU_DEBUG_LOG_ZOOMSCALE, true);
        makeCheckBoxMenuItem("menu_debug_log_fileio", "menu_debug_log_fileio_accstring", bDebugLogFileIO, debugMenu, menuListener, MENU_DEBUG_LOG_FILEIO, true);
        makeCheckBoxMenuItem("menu_debug_log_undo", "menu_debug_log_undo_accstring", bDebugLogUndoRedo, debugMenu, menuListener, MENU_DEBUG_LOG_UNDO, true);
        makeCheckBoxMenuItem("menu_debug_log_merge", "menu_debug_log_merge_accstring", bDebugLogMerge, debugMenu, menuListener, MENU_DEBUG_LOG_MERGE, true);
        makeCheckBoxMenuItem("menu_debug_log_routemanager", "menu_debug_log_routemanager_accstring", bDebugLogRouteManager, debugMenu, menuListener, MENU_DEBUG_LOG_ROUTEMANAGER, true);
        makeCheckBoxMenuItem("menu_debug_log_config", "menu_debug_log_config_accstring", bDebugLogConfigInfo, debugMenu, menuListener, MENU_DEBUG_LOG_CONFIG, true);
        makeCheckBoxMenuItem("menu_debug_log_heightmap_info", "menu_debug_log_heightmap_info_accstring", bDebugLogHeightMapInfo, debugMenu, menuListener, MENU_DEBUG_LOG_HEIGHTMAP, true);
        makeCheckBoxMenuItem("menu_debug_log_curve_info", "menu_debug_log_curve_info_accstring", bDebugLogCurveInfo, debugMenu, menuListener, MENU_DEBUG_LOG_CURVEINFO, true);
        makeCheckBoxMenuItem("menu_debug_log_marker_info", "menu_debug_log_marker_info_accstring", bDebugLogMarkerInfo, debugMenu, menuListener, MENU_DEBUG_LOG_MARKERS, true);
        makeCheckBoxMenuItem("menu_debug_log_render_info", "menu_debug_log_render_info_accstring", bDebugLogRenderInfo, debugMenu, menuListener, MENU_DEBUG_LOG_RENDER, true);
        makeCheckBoxMenuItem("menu_debug_log_gui_info", "menu_debug_log_gui_info_accstring", bDebugLogGUIInfo, debugMenu, menuListener, MENU_DEBUG_LOG_GUI, true);
        makeCheckBoxMenuItem("menu_debug_log_copypaste_info", "menu_debug_log_copypaste_info_accstring", bDebugLogCopyPasteInfo, debugMenu, menuListener, MENU_DEBUG_LOG_COPYPASTE, true);

    }

    public static void mapMenuEnabled(boolean enabled) {
        loadImageMenuItem.setEnabled(enabled);
        importFS19DDSMenuItem.setEnabled(enabled);
        importFS22DDSMenuItem.setEnabled(enabled);
    }

    public static void saveImageEnabled(boolean enabled) {
        saveImageMenuItem.setEnabled(enabled);
    }

    public static void heightmapMenuEnabled(boolean enabled) {
        showHeightMapMenuItem.setEnabled(enabled);
        exportHeightMapMenuItem.setEnabled(enabled);
        fixNodesHeightMenuItem.setEnabled(enabled);
    }

    public static void fixNodesEnabled(boolean enabled) {
        scanNetworkMenuItem.setEnabled(enabled);
        mergeNodesMenuItem.setEnabled(enabled);
    }


    public static void saveMenuEnabled(boolean enabled) {
        saveConfigMenuItem.setEnabled(enabled);
        saveConfigAsMenuItem.setEnabled(enabled);
    }

    public static void editMenuEnabled(boolean enabled) {
        undoMenuItem.setEnabled(enabled);
        redoMenuItem.setEnabled(enabled);
        cutMenuItem.setEnabled(enabled);
        copyMenuItem.setEnabled(enabled);
        pasteMenuItem.setEnabled(enabled);
        pasteOriginalLocationMenuItem.setEnabled(enabled);
    }

    public static void rotationMenuEnabled(boolean enabled) {
        rClockwiseMenuItem.setEnabled(enabled);
        r90ClockwiseMenuItem.setEnabled(enabled);
        rAntiClockwiseMenuItem.setEnabled(enabled);
        r90AntiClockwiseMenuItem.setEnabled(enabled);
    }
}
