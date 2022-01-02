package AutoDriveEditor.GUI;

import javax.swing.*;
import java.awt.event.InputEvent;
import java.awt.event.KeyEvent;

import AutoDriveEditor.Listeners.MenuListener;

import static AutoDriveEditor.AutoDriveEditor.*;
import static AutoDriveEditor.GUI.GUIBuilder.*;
import static AutoDriveEditor.GUI.GUIUtils.*;
import static AutoDriveEditor.XMLConfig.EditorXML.*;

public class MenuBuilder {

    public static final String MENU_LOAD_CONFIG = "Load Config";
    public static final String MENU_SAVE_CONFIG = "Save Config";
    public static final String MENU_SAVE_SAVEAS = "Save As";
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
    public static final String MENU_ZOOM_1x = "1x";
    public static final String MENU_ZOOM_4x = "4x";
    public static final String MENU_ZOOM_16x = "16x";
    public static final String MENU_HEIGHTMAP_IMPORT = "HeightMap Import";
    public static final String MENU_HEIGHTMAP_FIX = "Fix Node Height";
    public static final String MENU_CHECKBOX_CONTINUECONNECT = "Continuous Connections";
    public static final String MENU_CHECKBOX_MIDDLEMOUSEMOVE = "Middle Mouse Move";
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


    public static final String MENU_DEBUG_SHOWID = "DEBUG ID";
    public static final String MENU_DEBUG_FILEIO = "DEBUG CONFIG";
    public static final String MENU_DEBUG_SELECTED_LOCATION = "DEBUG SELECTED LOCATION";
    public static final String MENU_DEBUG_PROFILE = "DEBUG PROFILE";
    public static final String MENU_DEBUG_UNDO = "DEBUG UNDO/REDO SYSTEM";
    public static final String MENU_DEBUG_ZOOMSCALE = "ZOOMSCALE";
    public static final String MENU_DEBUG_HEIGHTMAP = "DEBUG HEIGHTMAP";
    public static final String MENU_DEBUG_MERGE = "DEBUG MERGE";
    public static final String MENU_DEBUG_TEST = "TEST";

    public static int InputEvent_NONE = 0;
    public static int KeyEvent_NONE = 0;

    public static MenuListener menuListener;

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
    public static JMenuItem zoomOneX;
    public static JMenuItem zoomFourX;
    public static JMenuItem zoomSixteenX;
    public static JMenuItem importHeightmapMenuItem;
    public static JMenuItem gridSnapMenuItem;
    public static JMenuItem gridSnapSubDivisionMenuItem;

    public static JMenuItem rClockwiseMenuItem;
    public static JMenuItem r90ClockwiseMenuItem;
    public static JMenuItem rAntiClockwiseMenuItem;
    public static JMenuItem r90AntiClockwiseMenuItem;

    public static JMenuItem fixNodesHeightMenuItem;
    public static JMenuItem scanNetworkMenuItem;
    public static JMenuItem mergeNodesMenuItem;


    public static boolean bDebugShowID;
    public static boolean bDebugFileIO;
    public static boolean bDebugShowSelectedLocation;
    public static boolean bDebugProfile;
    public static boolean bDebugUndoRedo;
    public static boolean bDebugZoomScale;
    public static boolean  bDebugHeightMap;
    public static boolean bDebugMerge;
    public static boolean bDebugTest;

    public static void createMenu() {
        //JMenuItem menuItem;
        JMenu fileMenu, editMenu, mapMenu, heightmapMenu, optionsMenu, helpMenu, subMenu, gridMenu, rotationMenu, fixItMenu, debugMenu;


        menuBar = new JMenuBar();
        menuListener = new MenuListener();

        // Create the file Menu

        fileMenu = makeMenu("menu_file", KeyEvent.VK_F, "menu_file_accstring", menuBar);
        makeMenuItem("menu_file_loadconfig",  "menu_file_loadconfig_accstring", KeyEvent.VK_L, InputEvent.ALT_DOWN_MASK, fileMenu, menuListener, MENU_LOAD_CONFIG, true );
        saveConfigMenuItem = makeMenuItem("menu_file_saveconfig",  "menu_file_saveconfig_accstring", KeyEvent.VK_S, InputEvent.ALT_DOWN_MASK, fileMenu, menuListener, MENU_SAVE_CONFIG, false );
        saveConfigAsMenuItem = makeMenuItem("menu_file_saveasconfig", "menu_file_saveasconfig_accstring",  KeyEvent.VK_A, InputEvent.ALT_DOWN_MASK,fileMenu, menuListener,MENU_SAVE_SAVEAS, false );
        makeMenuItem("menu_file_exit",  "menu_file_exit_accstring", KeyEvent.VK_Q, InputEvent.ALT_DOWN_MASK, fileMenu, menuListener, MENU_EXIT, true );

        // Create the edit menu

        editMenu = makeMenu("menu_edit", KeyEvent.VK_E, "menu_edit_accstring", menuBar);

        // Create the Undo/Redo menu

        undoMenuItem = makeMenuItem("menu_edit_undo",  "menu_edit_undo_accstring", KeyEvent.VK_Z, InputEvent.CTRL_DOWN_MASK, editMenu, menuListener, MENU_EDIT_UNDO, false );
        redoMenuItem = makeMenuItem("menu_edit_redo",  "menu_edit_redo_accstring", KeyEvent.VK_Z, InputEvent.SHIFT_DOWN_MASK, editMenu, menuListener, MENU_EDIT_REDO, false );
        cutMenuItem = makeMenuItem("menu_edit_cut",  "menu_edit_cut_accstring", KeyEvent.VK_X, InputEvent.CTRL_DOWN_MASK, editMenu, menuListener, BUTTON_COPYPASTE_CUT, false );
        copyMenuItem = makeMenuItem("menu_edit_copy",  "menu_edit_copy_accstring", KeyEvent.VK_C, InputEvent.CTRL_DOWN_MASK, editMenu, menuListener, BUTTON_COPYPASTE_COPY, false );
        pasteMenuItem = makeMenuItem("menu_edit_paste",  "menu_edit_paste_accstring", KeyEvent.VK_V, InputEvent.CTRL_DOWN_MASK, editMenu, menuListener, BUTTON_COPYPASTE_PASTE, false );


        // Create the Map Menu and it's scale sub menu

        mapMenu = makeMenu("menu_map", KeyEvent.VK_M, "menu_map_accstring", menuBar);
        loadImageMenuItem = makeMenuItem("menu_map_loadimage", "menu_map_loadimage_accstring", KeyEvent.VK_M, InputEvent.ALT_DOWN_MASK,mapMenu,menuListener, MENU_LOAD_IMAGE, false );

        mapMenu.addSeparator();
        subMenu = makeSubMenu("menu_map_scale", KeyEvent.VK_M, "menu_map_scale_accstring", mapMenu);
        ButtonGroup menuZoomGroup = new ButtonGroup();
        zoomOneX = makeRadioButtonMenuItem("menu_map_scale_1x", "menu_map_scale_1x_accstring",KeyEvent.VK_1, InputEvent.ALT_DOWN_MASK, subMenu, menuListener,  MENU_ZOOM_1x,true, menuZoomGroup, true);
        zoomFourX = makeRadioButtonMenuItem("menu_map_scale_4x", "menu_map_scale_4x_accstring",KeyEvent.VK_2, InputEvent.ALT_DOWN_MASK, subMenu, menuListener,  MENU_ZOOM_4x,true, menuZoomGroup, false);
        zoomSixteenX = makeRadioButtonMenuItem("menu_map_scale_16x", "menu_map_scale_16x_accstring",KeyEvent.VK_3, InputEvent.ALT_DOWN_MASK, subMenu, menuListener, MENU_ZOOM_16x, true, menuZoomGroup, false);
        mapMenu.addSeparator();
        importFS19DDSMenuItem = makeMenuItem("menu_import_fs19_dds", "menu_import_fs19_dds_accstring", KeyEvent.VK_I, InputEvent.ALT_DOWN_MASK, mapMenu, menuListener, MENU_IMPORT_FS19_DDS, false);
        importFS22DDSMenuItem = makeMenuItem("menu_import_fs22_dds", "menu_import_fs22_dds_accstring", KeyEvent.VK_P, InputEvent.ALT_DOWN_MASK, mapMenu, menuListener, MENU_IMPORT_FS22_DDS, false);
        saveImageMenuItem = makeMenuItem("menu_map_saveimage", "menu_map_saveimage_accstring", KeyEvent.VK_B, InputEvent.ALT_DOWN_MASK, mapMenu, menuListener, MENU_SAVE_IMAGE, false);

        // create the HeightMap menu

        heightmapMenu = makeMenu("menu_heightmap", KeyEvent.VK_T, "menu_heightmap_accstring", menuBar);
        importHeightmapMenuItem = makeMenuItem("menu_heightmap_import", "menu_heightmap_import_accstring", heightmapMenu, menuListener, MENU_HEIGHTMAP_IMPORT,false);
        fixNodesHeightMenuItem = makeMenuItem("menu_heightmap_fix_nodes", "menu_heightmap_fix_nodes_accstring", heightmapMenu, menuListener, MENU_HEIGHTMAP_FIX,false);

        // create the Options menu

        optionsMenu = makeMenu("menu_options", KeyEvent.VK_O, "menu_options_accstring", menuBar);
        makeCheckBoxMenuItem("menu_conconnect", "menu_conconnect_accstring", KeyEvent.VK_4, bContinuousConnections, optionsMenu, menuListener, MENU_CHECKBOX_CONTINUECONNECT);
        makeCheckBoxMenuItem("menu_middlemousemove", "menu_middlemousemove_accstring", KeyEvent.VK_5, bMiddleMouseMove, optionsMenu, menuListener, MENU_CHECKBOX_MIDDLEMOUSEMOVE);

        // create the grid snap menu

        gridMenu = makeMenu("menu_grid", KeyEvent.VK_G, "menu_grid_accstring", menuBar);
        makeCheckBoxMenuItem("menu_grid_show", "menu_grid_show_accstring", KeyEvent.VK_G, bShowGrid, gridMenu, menuListener, MENU_GRID_SHOW);
        gridSnapMenuItem = makeCheckBoxMenuItem("menu_grid_snap", "menu_grid_snap_accstring", KeyEvent.VK_S, bGridSnap, gridMenu, menuListener, MENU_GRID_SNAP);
        gridSnapSubDivisionMenuItem = makeCheckBoxMenuItem("menu_grid_snap_subdivide", "menu_grid_snap_subdivide_accstring", KeyEvent.VK_D, bGridSnapSubs, gridMenu, menuListener, MENU_GRID_SNAP_SUBS);
        gridMenu.addSeparator();
        makeMenuItem("menu_grid_set_size", "menu_grid_set_size_accstring", KeyEvent.VK_F, InputEvent.ALT_DOWN_MASK, gridMenu, menuListener, MENU_GRID_SET, true );

        // Create the Rotation Menu

        rotationMenu = makeMenu("menu_rotate", KeyEvent.VK_R, "menu_rotate_accstring", menuBar);
        rClockwiseMenuItem = makeMenuItem("menu_rotate_clockwise", "menu_rotate_clockwise_accstring", KeyEvent.VK_T, InputEvent.CTRL_DOWN_MASK, rotationMenu, menuListener, MENU_ROTATE_CLOCKWISE, false );
        r90ClockwiseMenuItem = makeMenuItem("menu_rotate_clockwise_ninty", "menu_rotate_clockwise_ninty_accstring", KeyEvent.VK_T, InputEvent.SHIFT_DOWN_MASK, rotationMenu, menuListener, MENU_ROTATE_CLOCKWISE_NINTY, false );
        rAntiClockwiseMenuItem = makeMenuItem("menu_rotate_anticlockwise", "menu_rotate_anticlockwise_accstring", KeyEvent.VK_R, InputEvent.CTRL_DOWN_MASK, rotationMenu, menuListener, MENU_ROTATE_ANTICLOCKWISE, false );
        r90AntiClockwiseMenuItem = makeMenuItem("menu_rotate_anticlockwise_ninty", "menu_rotate_anticlockwise_ninty_accstring", KeyEvent.VK_R, InputEvent.SHIFT_DOWN_MASK, rotationMenu, menuListener, MENU_ROTATE_ANTICLOCKWISE_NINTY, false );
        rotationMenu.addSeparator();
        makeMenuItem("menu_rotate_set_step", "menu_rotate_set_step_accstring", KeyEvent.VK_Y, InputEvent.SHIFT_DOWN_MASK, rotationMenu, menuListener, MENU_ROTATE_SET, true );

        // Ctreate the FixIt menu

        fixItMenu = makeMenu("menu_scan", KeyEvent.VK_S, "menu_scan_accstring", menuBar);
        scanNetworkMenuItem = makeMenuItem("menu_scan_overlap", "menu_scan_overlap_accstring", fixItMenu, menuListener, MENU_SCAN_OVERLAP, false);
        mergeNodesMenuItem = makeMenuItem("menu_scan_merge", "menu_scan_merge_accstring", fixItMenu, menuListener, MENU_SCAN_MERGE, false);

        // Create the Help menu

        helpMenu = makeMenu("menu_help", KeyEvent.VK_H, "menu_help_accstring", menuBar);
        makeMenuItem("menu_help_about", "menu_help_about_accstring", KeyEvent.VK_H, InputEvent.ALT_DOWN_MASK, helpMenu, menuListener, MENU_ABOUT, true);


        if (DEBUG) {
            debugMenu = makeMenu("menu_debug", KeyEvent.VK_D, "menu_debug_accstring", menuBar);
            makeCheckBoxMenuItem("menu_debug_showID", "menu_debug_showID_accstring", KeyEvent.VK_6, InputEvent.ALT_DOWN_MASK, bDebugShowID, debugMenu, menuListener, MENU_DEBUG_SHOWID);
            makeCheckBoxMenuItem("menu_debug_showselectedlocation", "menu_debug_showselectedlocation_accstring", KeyEvent.VK_7, InputEvent.ALT_DOWN_MASK, bDebugShowSelectedLocation, debugMenu, menuListener, MENU_DEBUG_SELECTED_LOCATION);
            makeCheckBoxMenuItem("menu_debug_profile", "menu_debug_profile_accstring", bDebugProfile, debugMenu, menuListener, MENU_DEBUG_PROFILE);
            makeCheckBoxMenuItem("menu_debug_heightmap", "menu_debug_heightmap_accstring", bDebugHeightMap, debugMenu, menuListener, MENU_DEBUG_HEIGHTMAP);
            if (EXPERIMENTAL) {
                makeCheckBoxMenuItem("menu_debug_test", "menu_debug_test_accstring", bDebugTest, debugMenu, menuListener, MENU_DEBUG_TEST);
            }
            debugMenu.addSeparator();
            makeCheckBoxMenuItem("menu_debug_zoom", "menu_debug_zoom_accstring", bDebugZoomScale, debugMenu, menuListener, MENU_DEBUG_ZOOMSCALE);
            makeCheckBoxMenuItem("menu_debug_fileio", "menu_debug_fileio_accstring", bDebugFileIO, debugMenu, menuListener, MENU_DEBUG_FILEIO);
            makeCheckBoxMenuItem("menu_debug_undo", "menu_debug_undo_accstring", bDebugUndoRedo, debugMenu, menuListener, MENU_DEBUG_UNDO);
            makeCheckBoxMenuItem("menu_debug_merge", "menu_debug_merge_accstring", bDebugMerge, debugMenu, menuListener, MENU_DEBUG_MERGE);

        }
    }

    public static void mapMenuEnabled(boolean enabled) {
        loadImageMenuItem.setEnabled(enabled);
        importFS19DDSMenuItem.setEnabled(enabled);
        importFS22DDSMenuItem.setEnabled(enabled);
    }

    public static void saveImageEnabled(boolean enabled) {
        saveImageMenuItem.setEnabled(enabled);
    }

    public static void fixNodesEnabled(boolean enabled) {
        scanNetworkMenuItem.setEnabled(enabled);
        if (EXPERIMENTAL) {
            mergeNodesMenuItem.setEnabled(enabled);
        } else {
            mergeNodesMenuItem.setEnabled(false);
        }

    }


    public static void saveMenuEnabled(boolean enabled) {
        saveConfigMenuItem.setEnabled(enabled);
        saveConfigAsMenuItem.setEnabled(enabled);
    }

    public static void scanMenuEnabled(boolean enabled) {
        saveConfigMenuItem.setEnabled(enabled);
        saveConfigAsMenuItem.setEnabled(enabled);
    }

    public static void editMenuEnabled(boolean enabled) {
        undoMenuItem.setEnabled(enabled);
        redoMenuItem.setEnabled(enabled);
        cutMenuItem.setEnabled(enabled);
        copyMenuItem.setEnabled(enabled);
        pasteMenuItem.setEnabled(enabled);
    }

    public static void rotationMenuEnabled(boolean enabled) {
        rClockwiseMenuItem.setEnabled(enabled);
        r90ClockwiseMenuItem.setEnabled(enabled);
        rAntiClockwiseMenuItem.setEnabled(enabled);
        r90AntiClockwiseMenuItem.setEnabled(enabled);
    }
}
