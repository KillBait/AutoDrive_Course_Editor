package AutoDriveEditor.Managers;

import com.formdev.flatlaf.FlatLaf;
import com.formdev.flatlaf.IntelliJTheme;
import com.formdev.flatlaf.json.Json;

import javax.swing.*;
import java.io.*;
import java.net.JarURLConnection;
import java.net.URL;
import java.nio.charset.StandardCharsets;
import java.util.ArrayList;
import java.util.Enumeration;
import java.util.List;
import java.util.Map;
import java.util.jar.JarEntry;
import java.util.jar.JarFile;
import java.util.prefs.Preferences;

import static AutoDriveEditor.Classes.Util_Classes.LoggerUtils.LOG;
import static AutoDriveEditor.GUI.Menus.DebugMenu.Logging.LogThemeManagerInfoMenu.bDebugLogThemeManagerInfo;

public class ThemeManager {

    private static final String THEME_PREF_KEY = "currentTheme";
    private static final Preferences prefs = Preferences.userNodeForPackage(ThemeManager.class);
    public static final List<ThemeInfo> allThemeList = new ArrayList<>();
    public static List<ThemeInfo> bundledThemeInfoList = new ArrayList<>();
    public static List<ThemeInfo> externalThemeInfoList = new ArrayList<>();


    public ThemeManager() {
        LOG.info("  Initializing Theme Manager");
        getBundledThemesInfo();
        getExternalThemes();
    }
    public static void saveCurrentTheme(String themeClassName) {
        prefs.put(THEME_PREF_KEY, themeClassName);
    }

    public static String getCurrentTheme() {
        return prefs.get(THEME_PREF_KEY, "com.formdev.flatlaf.FlatLightLaf"); // Default theme
    }

    public static void applySavedTheme() throws UnsupportedLookAndFeelException {
        String themeClassName = getCurrentTheme();
        applyTheme(themeClassName);
    }

    @SuppressWarnings("DataFlowIssue")
    public static void applyTheme(String theme) throws UnsupportedLookAndFeelException {
        try {
            UIManager.setLookAndFeel(theme);
        } catch (ClassNotFoundException | InstantiationException | IllegalAccessException ex1) {
            try {
                FlatLaf.setup(IntelliJTheme.createLaf(new FileInputStream(theme)));
            } catch (Exception ex2) {
                try {
                    InputStream is = ThemeManager.class.getResourceAsStream("/" + theme);
                    FlatLaf.setup(IntelliJTheme.createLaf(is));
                    LOG.info("Setting theme: {}", theme);
                } catch (RuntimeException | IOException ex3) {
                    LOG.info("Failed to load theme: {}", theme);
                    ex3.printStackTrace();
                }
            }
        }
    }

    private void getBundledThemesInfo() {
        try {
            Enumeration<URL> resources = this.getClass().getClassLoader().getResources("com/formdev/flatlaf/intellijthemes/themes/");
            while (resources.hasMoreElements()) {
                URL resource = resources.nextElement();
                if (bDebugLogThemeManagerInfo) LOG.info("    Resource URL: {}", resource);
                if (resource.getProtocol().equals("jar")) {
                    JarURLConnection jarConnection = (JarURLConnection) resource.openConnection();
                    try (JarFile jarFile = jarConnection.getJarFile()) {
                        Enumeration<JarEntry> entries = jarFile.entries();
                        while (entries.hasMoreElements()) {
                            JarEntry entry = entries.nextElement();
                            if (entry.getName().startsWith("com/formdev/flatlaf/intellijthemes/themes/") && entry.getName().endsWith(".json")) {
                                try (Reader reader = new InputStreamReader(jarFile.getInputStream(entry), StandardCharsets.UTF_8)) {
                                    Object json = Json.parse(reader);
                                    if (json instanceof Map) {
                                        Map<String, Object> themeMap = (Map<String, Object>) json;
                                        String name = (String) themeMap.get("name");
                                        if (name.equals("vuesion-theme")) name = "Vuesion"; // Special case for Vuesion theme
                                        boolean isMaterialTheme = entry.getName().contains("material-theme-ui-lite");
                                        ThemeInfo theme = new ThemeInfo(entry.getName(), name, (String) themeMap.get("author"), Boolean.parseBoolean((String) themeMap.get("dark")), isMaterialTheme, false);
                                        bundledThemeInfoList.add(theme);
                                        allThemeList.add(theme);
                                        if (bDebugLogThemeManagerInfo) LOG.info("## getBundledThemesInfo() Debug ## Found Bundled Theme: {}, Author: {}, Dark: {}", theme.getName(), theme.getAuthor(), theme.isDark());
                                    } else {
                                        LOG.error("      {} : Unexpected JSON structure:", entry.getName());
                                        if (bDebugLogThemeManagerInfo) LOG.error("{}",json);
                                    }
                                } catch (IOException e) {
                                    LOG.error("      Error reading themes.json", e);
                                }
                            }
                        }
                    }
                }
            }
            LOG.info("    Found {} bundled themes", bundledThemeInfoList.size());
        } catch (IOException e) {
            LOG.error("  Error scanning for bundled themes", e);
        }
    }

    @SuppressWarnings("unchecked")
    public void getExternalThemes() {
        File directory = (new File("Themes")).getAbsoluteFile();
        File[] themeDirFiles = directory.listFiles((dir, name) -> name.endsWith(".theme.json"));//        if (themeFiles != null) {

        if (themeDirFiles != null) {
            for (File f : themeDirFiles) {
                String fileName = f.getName();
                if (fileName.endsWith(".theme.json")) {
                    File themeFile = f.getAbsoluteFile();
                    try (Reader reader = new FileReader(themeFile)) {
                        Object json = Json.parse(reader);
                        if (json instanceof Map) {
                            Map<String, Object> themeMap = (Map<String, Object>) json;
                            ThemeInfo theme = new ThemeInfo(themeFile.getAbsolutePath(), (String) themeMap.get("name"), (String) themeMap.get("author"), Boolean.parseBoolean((String) themeMap.get("dark")), false, true);
                            externalThemeInfoList.add(theme);
                            allThemeList.add(theme);
                            if (bDebugLogThemeManagerInfo) LOG.info("## getBundledThemesInfo() Debug ## Found User Theme: {}, Author: {}, Dark: {}", theme.getName(), theme.getAuthor(), theme.isDark());
                        } else {
                            LOG.error("------> Unexpected JSON structure: {}", json);
                        }
                    } catch (IOException e) {
                        throw new RuntimeException(e);
                    }
                }
            }
            LOG.info("    Found {} external themes", externalThemeInfoList.size());
        } else {
            LOG.info("    No External themes found");
        }

    }

    //
    // unfinished
    //
    public void getExternalProperties() {
        LOG.info("    Scanning for external properties files");
        File directory = (new File("Themes")).getAbsoluteFile();
        File[] themeDirFiles = directory.listFiles((dir, name) -> name.endsWith(".properties"));

        if (themeDirFiles != null) {
            for (File f : themeDirFiles) {
                String fileName = f.getName();
                if (fileName.endsWith(".theme.json")) {
                    // override default theme file here
                }
            }
        }
        //String name = fileName.endsWith(".properties") ? StringUtils.removeTrailing(fileName, ".properties") : StringUtils.removeTrailing(fileName, ".theme.json");
    }

    public static class ThemeInfo {
        private final String path;
        private final String name;
        private final String author;
        private final boolean isDark;
        private final boolean isMaterialTheme;
        private final boolean isUserTheme;

        public ThemeInfo(String path, String name, String author, boolean isDark, boolean isMaterialTheme, boolean isUserTheme) {
            this.path = path;
            this.name = name;
            this.author = author;
            this.isDark = isDark;
            this.isMaterialTheme = isMaterialTheme;
            this.isUserTheme = isUserTheme;
        }

        public String getPath() {
            return path;
        }

        public String getName() {
            return name;
        }

        public String getAuthor() {
            return author;
        }

        public boolean isDark() {
            return isDark;
        }

        public boolean isMaterialTheme() {
            return isMaterialTheme;
        }

        public boolean isUserTheme() {
            return isUserTheme;
        }
    }
}