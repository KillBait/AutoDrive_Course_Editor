package AutoDriveEditor.GUI.Menus;

import AutoDriveEditor.GUI.Menus.DebugMenu.Logging.*;
import AutoDriveEditor.GUI.Menus.DebugMenu.*;
import AutoDriveEditor.GUI.Menus.Display.ShowMarkerIcons;
import AutoDriveEditor.GUI.Menus.Display.ShowMarkerNames;
import AutoDriveEditor.GUI.Menus.Display.ShowNodeID;
import AutoDriveEditor.GUI.Menus.Display.ShowParkingIcons;
import AutoDriveEditor.GUI.Menus.EditMenu.*;
import AutoDriveEditor.GUI.Menus.FileMenu.*;
import AutoDriveEditor.GUI.Menus.HeightMapMenu.ExportHeightMapMenu;
import AutoDriveEditor.GUI.Menus.HeightMapMenu.ImportHeightMapMenu;
import AutoDriveEditor.GUI.Menus.HeightMapMenu.ShowHeightmapMenu;
import AutoDriveEditor.GUI.Menus.HelpMenu.AboutMenu;
import AutoDriveEditor.GUI.Menus.HelpMenu.ImagesLinkMenu;
import AutoDriveEditor.GUI.Menus.HelpMenu.ShowDEBUGMenu;
import AutoDriveEditor.GUI.Menus.HelpMenu.ShowHistoryMenu;
import AutoDriveEditor.GUI.Menus.MapImagesMenu.*;
import AutoDriveEditor.GUI.Menus.RoutesMenu.OpenRoutesConfig;
import AutoDriveEditor.GUI.Menus.RoutesMenu.OpenRoutesXML;
import AutoDriveEditor.GUI.Menus.RoutesMenu.SaveRoutesXML;
import AutoDriveEditor.GUI.Menus.ScanMenu.FixNodesHeightMenu;
import AutoDriveEditor.GUI.Menus.ScanMenu.MergeNodesMenu;
import AutoDriveEditor.GUI.Menus.ScanMenu.OutOfBoundsFixMenu;
import AutoDriveEditor.GUI.Menus.ScanMenu.ScanNetworkMenu;

import javax.swing.*;
import java.awt.event.KeyEvent;

import static AutoDriveEditor.AutoDriveEditor.bIsDebugEnabled;
import static AutoDriveEditor.Classes.MapImage.heightMapImage;
import static AutoDriveEditor.Classes.MapImage.setMapPanelImage;
import static AutoDriveEditor.GUI.MapPanel.*;
import static AutoDriveEditor.GUI.Menus.EditMenu.CopyMenu.menu_Copy;
import static AutoDriveEditor.GUI.Menus.EditMenu.CutMenu.menu_Cut;
import static AutoDriveEditor.GUI.Menus.EditMenu.PasteMenu.menu_Paste;
import static AutoDriveEditor.GUI.Menus.EditMenu.PasteOriginalLocationMenu.menu_PasteOriginalLocation;
import static AutoDriveEditor.GUI.Menus.FileMenu.SaveAsConfigMenu.menu_SaveConfigAs;
import static AutoDriveEditor.GUI.Menus.FileMenu.SaveConfigMenu.menu_SaveConfig;
import static AutoDriveEditor.GUI.Menus.HeightMapMenu.ExportHeightMapMenu.menu_ExportHeightMap;
import static AutoDriveEditor.GUI.Menus.HeightMapMenu.ImportHeightMapMenu.menu_ImportHeightMap;
import static AutoDriveEditor.GUI.Menus.HeightMapMenu.ShowHeightmapMenu.bShowHeightMap;
import static AutoDriveEditor.GUI.Menus.HeightMapMenu.ShowHeightmapMenu.menu_ShowHeightMap;
import static AutoDriveEditor.GUI.Menus.MapImagesMenu.ImportFS19DDSMenu.menu_ImportFS19DDS;
import static AutoDriveEditor.GUI.Menus.MapImagesMenu.ImportFS22DDSMenu.menu_ImportFS22DDS;
import static AutoDriveEditor.GUI.Menus.MapImagesMenu.LoadMapImageMenu.menu_LoadMapImage;
import static AutoDriveEditor.GUI.Menus.MapImagesMenu.SaveMapImageMenu.menu_SaveMapImage;
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

        JMenu fileMenu = makeMenu("menu_file", "menu_file_accstring", KeyEvent.VK_F,this, true);
        fileMenu.add(new OpenConfigMenu());
        fileMenu.add(new RecentFilesMenu());
        fileMenu.add(new SaveConfigMenu());
        fileMenu.add(new SaveAsConfigMenu());
        fileMenu.add(new ExitEditorMenu());

        // Create Routes Menu

        JMenu routesMenu = makeMenu("menu_routes", "menu_routes_accstring", KeyEvent.VK_R, this, true);
        routesMenu.add(new OpenRoutesConfig());
        routesMenu.add(new OpenRoutesXML());
        routesMenu.add(new SaveRoutesXML());

        // Create Edit Menu

        JMenu editMenu = makeMenu("menu_edit", "menu_edit_accstring", KeyEvent.VK_E, this, true);
        editMenu.add(new UndoMenu());
        editMenu.add(new RedoMenu());
        editMenu.add(new CutMenu());
        editMenu.add(new CopyMenu());
        editMenu.add(new PasteMenu());
        editMenu.add(new PasteOriginalLocationMenu());

        // Create the Map Menu and it's scale sub menu

        JMenu mapImagesMenu = makeMenu("menu_map", "menu_map_accstring", KeyEvent.VK_M, this, true);
        mapImagesMenu.add(new LoadMapImageMenu());
        mapImagesMenu.addSeparator();
        mapImagesMenu.add(new MapZoomMenu());
        mapImagesMenu.addSeparator();
        mapImagesMenu.add(new ImportFS19DDSMenu());
        mapImagesMenu.add(new ImportFS22DDSMenu());
        mapImagesMenu.add(new SaveMapImageMenu());

        // Create the HeightMap Menu

        JMenu heightmapMenu = makeMenu("menu_heightmap", "menu_heightmap_accstring", KeyEvent.VK_T, this, true);
        heightmapMenu.add(new ImportHeightMapMenu());
        heightmapMenu.add(new ExportHeightMapMenu());
        heightmapMenu.addSeparator();
        heightmapMenu.add(new ShowHeightmapMenu());

        // Create the Display Menu

        JMenu displayMenu = makeMenu("menu_display", "menu_display_accstring", KeyEvent.VK_D, this, true);
        displayMenu.add(new ShowMarkerNames());
        displayMenu.add(new ShowMarkerIcons());
        displayMenu.add(new ShowParkingIcons());
        displayMenu.add(new ShowNodeID());

        // Create the Scan Menu

        JMenu fixItMenu = makeMenu("menu_scan", "menu_scan_accstring", KeyEvent.VK_S, this, true);
        fixItMenu.add(new ScanNetworkMenu());
        fixItMenu.addSeparator();
        fixItMenu.add(new OutOfBoundsFixMenu());
        fixItMenu.add(new FixNodesHeightMenu());
        fixItMenu.addSeparator();
        fixItMenu.add(new MergeNodesMenu());

        // Create the Help Menu

        JMenu helpMenu = makeMenu("menu_help", "menu_help_accstring", KeyEvent.VK_H, this, true);
        helpMenu.add(new AboutMenu());
        helpMenu.add(new ShowHistoryMenu());
        helpMenu.add(new ImagesLinkMenu());
        helpMenu.addSeparator();
        helpMenu.add(new ShowDEBUGMenu());

        menu_DEBUG = makeMenu("menu_debug", "menu_debug_accstring", KeyEvent.VK_D, this, true);
        menu_DEBUG.setVisible(bIsDebugEnabled);
        menu_DEBUG.add(new MoveNodeToCentreMenu());
        menu_DEBUG.addSeparator();
        menu_DEBUG.add(new ShowAllNodeIDMenu());
        menu_DEBUG.add(new ShowNodeHeightMenu());
        menu_DEBUG.add(new ShowNodeLocationInfo());
        menu_DEBUG.add(new ShowProfileInfo());
        menu_DEBUG.add(new ShowZoomLevelInfo());
        menu_DEBUG.add(new ShowHeightMapInfo());
        menu_DEBUG.addSeparator();
        JMenu loggingSubMenu = makeSubMenu("menu_debug_log_sub", "menu_debug_log_sub_accstring", menu_DEBUG);
        loggingSubMenu.add(new LogScanManagerInfoMenu());
        loggingSubMenu.add(new LogMultiSelectInfoMenu());
        loggingSubMenu.add(new LogListenerStateMenu());
        loggingSubMenu.add(new LogButtonInfoMenu());
        loggingSubMenu.add(new LogZoomScaleMenu());
        loggingSubMenu.add(new LogFileIOMenu());
        loggingSubMenu.add(new LogUndoRedoMenu());
        loggingSubMenu.add(new LogMergeFunctionMenu());
        loggingSubMenu.add(new LogRouteManagerMenu());
        loggingSubMenu.add(new LogConfigMenu());
        loggingSubMenu.add(new LogHeightmapInfoMenu());
        loggingSubMenu.add(new LogCurveInfoMenu());
        loggingSubMenu.add(new LogLinearLineInfoMenu());
        loggingSubMenu.add(new LogMarkerInfoMenu());
        loggingSubMenu.add(new LogRenderInfoMenu());
        loggingSubMenu.add(new LogGUIInfoMenu());
        loggingSubMenu.add(new LogConfigGUIInfoMenu());
        loggingSubMenu.add(new LogCopyPasteMenu());
        loggingSubMenu.add(new LogConnectSelectionMenu());
        loggingSubMenu.add(new LogFlipConnectionMenu());
    }

    public static JMenu makeMenu(String menuName, String accString, int keyEvent, JMenuBar parentMenu, boolean isVisible) {
        JMenu newMenu = new JMenu(getLocaleString(menuName));
        newMenu.setMnemonic(keyEvent);
        newMenu.getAccessibleContext().setAccessibleDescription(getLocaleString(accString));
        newMenu.setVisible(isVisible);
        parentMenu.add(newMenu);
        return newMenu;
    }

    public static JMenu makeSubMenu(String menuName, String accString, JMenu parentMenu) {
        JMenu newMenu = new JMenu(getLocaleString(menuName));
        //newMenu.setMnemonic(keyEvent);
        newMenu.getAccessibleContext().setAccessibleDescription(getLocaleString(accString));
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
        if (multiSelectList.size() > 0) {
            menu_Cut.setEnabled(true);
            menu_Copy.setEnabled(true);
        } else {
            menu_Cut.setEnabled(false);
            menu_Copy.setEnabled(false);
        }

        if (!cnpManager.isCopyPasteBufferEmpty()) {
            menu_Paste.setEnabled(true);
            menu_PasteOriginalLocation.setEnabled(true);
        } else {
            menu_Paste.setEnabled(false);
            menu_PasteOriginalLocation.setEnabled(false);
        }
    }

    public static void mapMenuEnabled(boolean enabled) {
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
        if (heightMapImage != null) {
            menu_ShowHeightMap.setEnabled(enabled);
            if (bShowHeightMap) setMapPanelImage(heightMapImage, true);
        }

    }

    public static void scanMenuEnabled(boolean enabled) {
        menu_ScanNetwork.setEnabled(enabled);
        menu_MergeNodes.setEnabled(enabled);
        menu_FixNodesHeight.setEnabled(enabled);
        menu_OutOfBoundsFix.setEnabled(enabled);
    }
}
