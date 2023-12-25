package AutoDriveEditor.GUI.Menus.FileMenu;

import AutoDriveEditor.GUI.Menus.JMenuBase;

import javax.swing.*;
import java.awt.event.ActionListener;
import java.io.File;
import java.util.ArrayDeque;
import java.util.Deque;

import static AutoDriveEditor.GUI.EditorImages.getGameIcon;
import static AutoDriveEditor.GUI.EditorImages.getRouteIcon;
import static AutoDriveEditor.GUI.MapPanel.*;
import static AutoDriveEditor.Locale.LocaleManager.getLocaleString;
import static AutoDriveEditor.Utils.LoggerUtils.LOG;
import static AutoDriveEditor.XMLConfig.GameXML.loadGameConfig;
import static AutoDriveEditor.XMLConfig.RoutesXML.loadRouteManagerXML;

public class RecentFilesMenu extends JMenuBase {

    public static final int MAX_RECENT_FILES = 10;
    private static final int MAX_DISPLAY_LENGTH = 75;
    public static Deque<RecentEntry> recentFilesList = new ArrayDeque<>();
    private static ActionListener recentMenuItemListener;
    private static ActionListener clearListListener;
    private static JMenu recentSubMenu;

    public RecentFilesMenu() {
        recentSubMenu = makeSubMenu("menu_file_recent","menu_file_recent_accstring", true);

        // create the shared actionListener used by all the previous file menus
        recentMenuItemListener = e -> {
            RecentJMenuItem menuItem = (RecentJMenuItem) e.getSource();
            String filePath = menuItem.getConfigPath();
            LOG.info("Recent File List:- selected '{}'", menuItem.getConfigPath());
            checkIfConfigIsStaleAndConfirmSave();
            if (menuItem.getConfigType() == CONFIG_SAVEGAME) {
                loadGameConfig(new File(filePath));
            } else if (menuItem.getConfigType() == CONFIG_ROUTEMANAGER) {
                loadRouteManagerXML(new File(filePath), false, null);
            }

        };

        // create an actionListener for the clear list menu item.
        clearListListener = e -> {
            recentFilesList.clear();
            recentSubMenu.removeAll();
        };
        if (recentFilesList.size() > 0 ) recreateRecentFilesMenu();
    }

    public static void addToRecentFiles(String newPath, int configType) {

        // Check if the path is already on the recent list, if it is, return, doing nothing
        boolean found = false;
        for (RecentEntry entry : recentFilesList) {
            if (entry.path.equals(newPath)) {
                LOG.info("Config Filename is already in recent files list");
                return;
            }
        }

        String typeString;
        if (configType == CONFIG_SAVEGAME) {
            typeString = "GAME_XML";
        } else if (configType == CONFIG_ROUTEMANAGER) {
            typeString = "ROUTMANAGER_XML";
        } else {
            typeString = "UNKNOWN_TYPE";
        }
        LOG.info("Adding ({}) {} to recent files", typeString, newPath);

        // if we get this far, the newPath was not found in the previous list.
        if (recentFilesList.size() >= MAX_RECENT_FILES) {
            recentFilesList.removeLast();
        }

        // Add the new path to the top of the list
        recentFilesList.addFirst(new RecentEntry(newPath, configType));
        // remove the old menu items and re-make them to the new list order
        recreateRecentFilesMenu();
    }

    private static void recreateRecentFilesMenu() {
        recentSubMenu.removeAll();
        int i = 1;
        for (RecentEntry entry : recentFilesList) {
            String displayText = i + ") " + ((entry.path.length() > MAX_DISPLAY_LENGTH) ? "..." + entry.path.substring(entry.path.length() - MAX_DISPLAY_LENGTH) : entry.path);
            RecentJMenuItem newItem = new RecentJMenuItem(displayText);
            if (entry.configType == CONFIG_SAVEGAME) {
                newItem.setIcon(getGameIcon());
            } else if (entry.configType == CONFIG_ROUTEMANAGER) {
                newItem.setIcon(getRouteIcon());
            }
            newItem.setConfigPath(entry.path);
            newItem.setConfigType(entry.configType);
            newItem.addActionListener(recentMenuItemListener);
            recentSubMenu.add(newItem);
            i++;
        }
        recentSubMenu.addSeparator();
        if (recentFilesList.size() > 0) {
            JMenuItem removeMenuItem = new JMenuItem();
            removeMenuItem.setText(getLocaleString("menu_file_recent_clear"));
            removeMenuItem.getAccessibleContext().setAccessibleDescription(getLocaleString("menu_file_recent_clear_accstring"));
            removeMenuItem.setEnabled(true);
            removeMenuItem.addActionListener(clearListListener);
            recentSubMenu.add(removeMenuItem);
        }
    }

    public static class RecentEntry {
        private int configType;
        private String path;

        public RecentEntry(String path, int configType) {
            this.configType = configType;
            this.path = path;
        }

        // getters

        public String getConfigPath() { return this.path; }
        public int getConfigType() { return this.configType; }

        // setters

        public void setConfigPath(String path) { this.path = path; }
        public void setConfigType(int configType) { this.configType = configType; }
    }

    private static class RecentJMenuItem extends JMenuItem {
        private int configType;
        private String configPath;

        public RecentJMenuItem(String text) {
            super(text);
            this.configType = 0;
            this.configPath = "";
        }

        // getters

        public int getConfigType() { return configType; }
        public String getConfigPath() { return configPath; }

        // setters

        public void setConfigPath(String configPath) { this.configPath = configPath; }
        public void setConfigType(int configType) { this.configType = configType; }
    }
}
