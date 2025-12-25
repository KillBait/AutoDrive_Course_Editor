package AutoDriveEditor.GUI.Menus;

import AutoDriveEditor.GUI.Menus.DebugMenu.Logging.*;
import AutoDriveEditor.GUI.Menus.DebugMenu.*;
import AutoDriveEditor.GUI.Menus.Display.*;
import AutoDriveEditor.GUI.Menus.EditMenu.*;
import AutoDriveEditor.GUI.Menus.FileMenu.*;
import AutoDriveEditor.GUI.Menus.HelpMenu.*;
import AutoDriveEditor.GUI.Menus.ImportMenu.*;
import AutoDriveEditor.GUI.Menus.RoutesMenu.OpenRoutesConfig;
import AutoDriveEditor.GUI.Menus.RoutesMenu.OpenRoutesXML;
import AutoDriveEditor.GUI.Menus.RoutesMenu.SaveRoutesXML;
import AutoDriveEditor.GUI.Menus.ScanMenu.FixNodesHeightMenu;
import AutoDriveEditor.GUI.Menus.ScanMenu.MergeNodesMenu;
import AutoDriveEditor.GUI.Menus.ScanMenu.OutOfBoundsFixMenu;
import AutoDriveEditor.GUI.Menus.ScanMenu.ScanNetworkMenu;
import AutoDriveEditor.Managers.CopyPasteManager;

import javax.swing.*;
import java.awt.event.KeyEvent;

import static AutoDriveEditor.AutoDriveEditor.bIsDebugEnabled;
import static AutoDriveEditor.GUI.MapImage.heightMapImage;
import static AutoDriveEditor.GUI.MapPanel.*;
import static AutoDriveEditor.GUI.Menus.EditMenu.CopyMenu.menu_Copy;
import static AutoDriveEditor.GUI.Menus.EditMenu.CutMenu.menu_Cut;
import static AutoDriveEditor.GUI.Menus.EditMenu.PasteMenu.menu_Paste;
import static AutoDriveEditor.GUI.Menus.EditMenu.PasteOriginalLocationMenu.menu_PasteOriginalLocation;
import static AutoDriveEditor.GUI.Menus.FileMenu.SaveAsConfigMenu.menu_SaveConfigAs;
import static AutoDriveEditor.GUI.Menus.FileMenu.SaveConfigMenu.menu_SaveConfig;
import static AutoDriveEditor.GUI.Menus.ImportMenu.ExportHeightMapMenu.menu_ExportHeightMap;
import static AutoDriveEditor.GUI.Menus.ImportMenu.ImportFS19DDSMenu.menu_ImportFS19DDS;
import static AutoDriveEditor.GUI.Menus.ImportMenu.ImportFS22DDSMenu.menu_ImportFS22DDS;
import static AutoDriveEditor.GUI.Menus.ImportMenu.ImportHeightMapMenu.menu_ImportHeightMap;
import static AutoDriveEditor.GUI.Menus.ImportMenu.LoadMapImageMenu.menu_LoadMapImage;
import static AutoDriveEditor.GUI.Menus.ImportMenu.SaveMapImageMenu.menu_SaveMapImage;
import static AutoDriveEditor.GUI.Menus.RoutesMenu.SaveRoutesXML.menu_SaveRoutesXML;
import static AutoDriveEditor.GUI.Menus.ScanMenu.FixNodesHeightMenu.menu_FixNodesHeight;
import static AutoDriveEditor.GUI.Menus.ScanMenu.MergeNodesMenu.menu_MergeNodes;
import static AutoDriveEditor.GUI.Menus.ScanMenu.OutOfBoundsFixMenu.menu_OutOfBoundsFix;
import static AutoDriveEditor.GUI.Menus.ScanMenu.ScanNetworkMenu.menu_ScanNetwork;
import static AutoDriveEditor.Locale.LocaleManager.getLocaleString;
import static AutoDriveEditor.Managers.ImportManager.getIsEditorUsingImportedImage;
import static AutoDriveEditor.Managers.MultiSelectManager.multiSelectList;

public class EditorMenu extends JMenuBar{

    public static JMenu menu_DEBUG;

    public EditorMenu() {

        // Create File Menu

        JMenu fileMenu = makeMenu("menu_file", KeyEvent.VK_F,this, true);
        fileMenu.add(new OpenConfigMenu());
        fileMenu.add(new RecentFilesMenu());
        fileMenu.add(new SaveConfigMenu());
        fileMenu.add(new SaveAsConfigMenu());
        fileMenu.add(new ExitEditorMenu());

        // Create Routes Menu

        JMenu routesMenu = makeMenu("menu_routes", KeyEvent.VK_R, this, true);
        routesMenu.add(new OpenRoutesConfig());
        routesMenu.add(new OpenRoutesXML());
        routesMenu.add(new SaveRoutesXML());

        // Create Edit Menu

        JMenu editMenu = makeMenu("menu_edit", KeyEvent.VK_E, this, true);
        editMenu.add(new UndoMenu());
        editMenu.add(new RedoMenu());
        editMenu.add(new CutMenu());
        editMenu.add(new CopyMenu());
        editMenu.add(new PasteMenu());
        editMenu.add(new PasteOriginalLocationMenu());

        // Create the Map Menu and it's scale sub menu

        //JMenu mapImagesMenu = makeMenu("menu_map", "menu_map_accstring", KeyEvent.VK_M, this, true);
        //mapImagesMenu.add(new LoadMapImageMenu());
        //mapImagesMenu.addSeparator();
        //mapImagesMenu.add(new MapZoomMenu());
        //mapImagesMenu.addSeparator();
        //mapImagesMenu.add(new ImportFS19DDSMenu());
        //mapImagesMenu.add(new ImportFS22DDSMenu());
        //mapImagesMenu.add(new SaveMapImageMenu());

        // Create the HeightMap Menu

        //JMenu heightmapMenu = makeMenu("menu_heightmap", "menu_heightmap_accstring", KeyEvent.VK_T, this, true);
        //heightmapMenu.add(new ImportHeightMapMenu());
        //heightmapMenu.add(new ExportHeightMapMenu());


        // Create the Display Menu

        JMenu displayMenu = makeMenu("menu_display", KeyEvent.VK_D, this, true);
        displayMenu.add(new MapScaleMenu());
        displayMenu.addSeparator();
        displayMenu.add(new ShowMarkerNames());
        displayMenu.add(new ShowMarkerIcons());
        displayMenu.add(new ShowParkingIcons());
        displayMenu.add(new ShowNodeID());

        // Create the Scan Menu

        JMenu fixItMenu = makeMenu("menu_scan", KeyEvent.VK_S, this, true);
        fixItMenu.add(new ScanNetworkMenu());
        fixItMenu.addSeparator();
        fixItMenu.add(new OutOfBoundsFixMenu());
        fixItMenu.add(new FixNodesHeightMenu());
        fixItMenu.addSeparator();
        fixItMenu.add(new MergeNodesMenu());

        JMenu importMenu = makeMenu("menu_import", KeyEvent.VK_I, this, true);
        importMenu.add(new ImportFromZipMenu());
        JMenu mapImagesMenu = makeSubMenu(importMenu, "menu_import_sub_map");
        mapImagesMenu.add(new LoadMapImageMenu());
        mapImagesMenu.addSeparator();
        mapImagesMenu.add(new ImportFS19DDSMenu());
        mapImagesMenu.add(new ImportFS22DDSMenu());
        mapImagesMenu.add(new SaveMapImageMenu());
        importMenu.addSeparator();
        JMenu heightmapMenu = makeSubMenu(importMenu, "menu_import_sub_heightmap");
        heightmapMenu.add(new ImportHeightMapMenu());
        heightmapMenu.add(new ExportHeightMapMenu());



        // Create the Help Menu

        JMenu helpMenu = makeMenu("menu_help", KeyEvent.VK_H, this, true);
        helpMenu.add(new AboutMenu());
        helpMenu.add(new ShowHistoryMenu());
        helpMenu.add(new ImagesLinkMenu());
        helpMenu.addSeparator();
        helpMenu.add(new LoggingGUIMenu());
        helpMenu.add(new ShowDEBUGMenu());

        menu_DEBUG = makeMenu("menu_debug", KeyEvent.VK_D, this, true);
        menu_DEBUG.setVisible(bIsDebugEnabled);
        menu_DEBUG.add(new MoveNodeToCentreMenu());
        menu_DEBUG.addSeparator();
        menu_DEBUG.add(new ShowAllNodeIDMenu());
        menu_DEBUG.add(new ShowNodeHeightMenu());
        menu_DEBUG.add(new ShowNodeLocationInfo());
        menu_DEBUG.addSeparator();
        menu_DEBUG.add(new ShowCurveManagerInfo());
        menu_DEBUG.add(new ShowDrawOrderInfo());
        menu_DEBUG.add(new ShowFrameInfo());
        menu_DEBUG.add(new ShowHeightMapInfo());
        menu_DEBUG.add(new ShowRenderProfileInfo());
        menu_DEBUG.add(new ShowWidgetManagerInfo());
        menu_DEBUG.add(new ShowZoomLevelInfo());
        menu_DEBUG.addSeparator();
        JMenu loggingSubMenu = makeSubMenu(menu_DEBUG, "menu_debug_log_sub");

        // Manager Sub Menu

        JMenu managersSubMenu = makeSubMenu(loggingSubMenu, "menu_debug_log_sub_managers");
        managersSubMenu.add(new LogButtonManagerInfoMenu());
        managersSubMenu.add(new LogCopyPasteManagerMenu());
        managersSubMenu.add(new LogCurveManagerInfoMenu());
        managersSubMenu.add(new LogMultiSelectManagerInfoMenu());
        managersSubMenu.add(new LogRouteManagerMenu());
        managersSubMenu.add(new LogScanManagerInfoMenu());
        managersSubMenu.add(new LogThemeManagerInfoMenu());
        managersSubMenu.add(new LogWidgetManagerInfoMenu());

        // Others

        loggingSubMenu.add(new LogAutoSaveMenu());
        loggingSubMenu.add(new LogConfigGUIInfoMenu());
        loggingSubMenu.add(new LogConnectSelectionMenu());
        loggingSubMenu.add(new LogCurveInfoMenu());
        loggingSubMenu.add(new LogCurveWidgetMenu());
        loggingSubMenu.add(new LogFileIOMenu());
        loggingSubMenu.add(new LogFlipConnectionMenu());
        loggingSubMenu.add(new LogGUIInfoMenu());
        loggingSubMenu.add(new LogHeightmapInfoMenu());
        loggingSubMenu.add(new LogLinearLineInfoMenu());
        loggingSubMenu.add(new LogMarkerInfoMenu());
        loggingSubMenu.add(new LogMenuDebugMenu());
        loggingSubMenu.add(new LogMergeFunctionMenu());
        loggingSubMenu.add(new LogMoveWidgetMenu());
        loggingSubMenu.add(new LogShortcutInfoMenu());
        loggingSubMenu.add(new LogSnapShotInfoMenu());
        loggingSubMenu.add(new LogUndoRedoMenu());
        loggingSubMenu.add(new LogXMLConfigMenu());
        loggingSubMenu.add(new LogXMLReaderMenu());
        loggingSubMenu.add(new LogZipUtilsMenu());
        loggingSubMenu.add(new LogZoomScaleMenu());
    }

    public static JMenu makeMenu(String menuName, int keyEvent, JMenuBar parentMenu, boolean isVisible) {
        JMenu newMenu = new JMenu(getLocaleString(menuName));
        newMenu.setMnemonic(keyEvent);
        newMenu.getAccessibleContext().setAccessibleDescription(getLocaleString(menuName));
        newMenu.setVisible(isVisible);
        parentMenu.add(newMenu);
        return newMenu;
    }

    public static JMenu makeSubMenu(JMenu parentMenu, String menuName) {
        JMenu newMenu = new JMenu(getLocaleString(menuName));
        newMenu.getAccessibleContext().setAccessibleDescription(getLocaleString(menuName));
        parentMenu.add(newMenu);
        return newMenu;
    }

    public static void gameXMLSaveEnabled(boolean enabled) {
        menu_SaveConfig.setEnabled(isStale());
        menu_SaveConfigAs.setEnabled(enabled);
    }

    public static void routesXMLSaveEnabled(boolean ignoredEnabled) {
        if (configType == CONFIG_ROUTEMANAGER) {
            if (isStale()) menu_SaveRoutesXML.setEnabled(true);
        } else {
            menu_SaveRoutesXML.setEnabled(false);
        }
    }

    public static void updateEditMenu() {
        if (!multiSelectList.isEmpty()) {
            menu_Cut.setEnabled(true);
            menu_Copy.setEnabled(true);
        } else {
            menu_Cut.setEnabled(false);
            menu_Copy.setEnabled(false);
        }

        if (CopyPasteManager.getSnapShot()) {
            menu_Paste.setEnabled(true);
            menu_PasteOriginalLocation.setEnabled(true);
        } else {
            menu_Paste.setEnabled(false);
            menu_PasteOriginalLocation.setEnabled(false);
        }
    }

    public static void mapImageMenuEnabled(boolean enabled) {
        menu_LoadMapImage.setEnabled(enabled);
        menu_ImportFS19DDS.setEnabled(enabled);
        menu_ImportFS22DDS.setEnabled(enabled);
        if (enabled) {
            if (getIsEditorUsingImportedImage()) menu_SaveMapImage.setEnabled(true);
        } else {
            menu_SaveMapImage.setEnabled(false);
        }

    }

    public static void saveImageEnabled(boolean enabled) {
        menu_SaveMapImage.setEnabled(enabled);
    }

    public static void heightmapMenuEnabled(boolean enabled) {
        menu_ImportHeightMap.setEnabled(enabled);
        if (heightMapImage != null) menu_ExportHeightMap.setEnabled(enabled);
    }

    public static void scanMenuEnabled(boolean enabled) {
        menu_ScanNetwork.setEnabled(enabled);
        menu_MergeNodes.setEnabled(enabled);
        menu_FixNodesHeight.setEnabled(enabled);
        menu_OutOfBoundsFix.setEnabled(enabled);
    }
}
