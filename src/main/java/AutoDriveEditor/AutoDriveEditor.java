package AutoDriveEditor;

import AutoDriveEditor.GUI.ActionBar;
import AutoDriveEditor.GUI.MapPanel;
import AutoDriveEditor.GUI.Menus.EditorMenu;
import AutoDriveEditor.GUI.RenderThreads.ConnectionDrawThread;
import AutoDriveEditor.GUI.RenderThreads.NodeDrawThread;
import AutoDriveEditor.GUI.TextPanel;
import AutoDriveEditor.GUI.Toolbar;
import AutoDriveEditor.Handlers.GlobalExceptionHandler;
import AutoDriveEditor.Locale.LocaleManager;
import AutoDriveEditor.Managers.*;
import AutoDriveEditor.RoadNetwork.RoadMap;
import com.formdev.flatlaf.FlatLaf;
import com.formdev.flatlaf.extras.FlatInspector;
import com.formdev.flatlaf.extras.FlatUIDefaultsInspector;
import net.miginfocom.swing.MigLayout;

import javax.swing.*;
import java.awt.*;
import java.awt.event.WindowAdapter;
import java.awt.event.WindowEvent;
import java.io.IOException;
import java.io.InputStream;
import java.time.LocalDate;
import java.time.Month;
import java.util.Objects;

import static AutoDriveEditor.Classes.Util_Classes.LoggerUtils.LOG;
import static AutoDriveEditor.GUI.MapPanel.isStale;
import static AutoDriveEditor.Locale.LocaleManager.getLocaleString;
import static AutoDriveEditor.XMLConfig.AutoSave.scheduledExecutorService;
import static AutoDriveEditor.XMLConfig.EditorXML.*;
import static AutoDriveEditor.XMLConfig.GameXML.saveGameConfig;
import static AutoDriveEditor.XMLConfig.GameXML.xmlConfigFile;

public class AutoDriveEditor extends JFrame {

    public static final String COURSE_EDITOR_VERSION = "1.1.0";
    public static final String COURSE_EDITOR_NAME = "AutoDrive Editor";
    public static final String COURSE_EDITOR_TITLE = COURSE_EDITOR_NAME + " " + COURSE_EDITOR_VERSION;
    public static final String COURSE_EDITOR_BUILD_INFO = "Java 11 SDK + IntelliJ IDEA 2025.2.3 Community Edition";

    public static AutoDriveEditor editor;
    public static JSplitPane editorSplitPane;

    public static ChangeManager changeManager;
    public static ButtonManager buttonManager;
    public static IconManager iconManager;
    public static ShortcutManager shortcutManager;
    public static ThemeManager themeManager;
    public static CurveManager curveManager;
    public static WidgetManager widgetManager;
    public static DebugDisplayManager debugDisplayManager;


    public static boolean EXPERIMENTAL = false;
    public static boolean bIsDebugEnabled = false;
    public static boolean isChristmas = false;
    public static boolean isHalloween = false;
    public AutoDriveEditor() {
        super();

        LocalDate today = LocalDate.now();
        isChristmas = today.getMonth() == Month.DECEMBER && today.getDayOfMonth() >= 24 && today.getDayOfMonth() <= 26;
        isHalloween = today.getMonth() == Month.OCTOBER && today.getDayOfMonth() == 31;


        LOG.info("Starting AutoDrive Editor v{} .....", COURSE_EDITOR_VERSION);

        if (isChristmas) {
            LOG.info("-----------------------");
            LOG.info("Happy holidays! {} 🎄", today.getYear());
            LOG.info("-----------------------");
        } else if (isHalloween) {
            LOG.info("--------------------------");
            LOG.info("Happy Halloween! {} 🎃 ", today.getYear());
            LOG.info("--------------------------");
        }

        LOG.info("Using Java Runtime Version {}", Runtime.version().feature());
        LOG.info("Launch Heap memory: Initial = {} MB, Max = {} MB",
                Runtime.getRuntime().totalMemory() / 1024 / 1024,
                Runtime.getRuntime().maxMemory() / 1024 / 1024);

        LOG.info("Detecting Locale");
        LocaleManager.setLocale();

        LOG.info("Starting Required Managers");
        debugDisplayManager = new DebugDisplayManager();
        shortcutManager = new ShortcutManager();
        iconManager = new IconManager();
        buttonManager = new ButtonManager();
        themeManager = new ThemeManager();
        changeManager = new ChangeManager();
        widgetManager = new WidgetManager();
        curveManager = new CurveManager();

        loadEditorXMLConfig();

        addWindowListener(new WindowAdapter() {
            @Override
            public void windowOpened(WindowEvent we) {
                if (!EXPERIMENTAL) {
                    VersionManager.updateCheck();
                } else {
                    LOG.info("EXPERIMENTAL active - Update check disabled");
                }
            }
            @Override
            public void windowClosing(WindowEvent e) {
                if (isStale()) {
                    int response = JOptionPane.showConfirmDialog(e.getComponent(), getLocaleString("dialog_exit_unsaved"), "AutoDrive", JOptionPane.YES_NO_OPTION, JOptionPane.QUESTION_MESSAGE);
                    if (response == JOptionPane.YES_OPTION) {
                        saveGameConfig(null, false, false);
                    }
                }
                if ( getMapPanel().connectionDrawThread != null ) {
                    ConnectionDrawThread.stop();
                    getMapPanel().connectionDrawThread.interrupt();
                }

                if ( getMapPanel().nodeDrawThread != null ) {
                    NodeDrawThread.stop();
                    getMapPanel().nodeDrawThread.interrupt();
                }

                if ( bAutoSaveEnabled && scheduledExecutorService != null ) {
                    scheduledExecutorService.shutdownNow();
                    LOG.info("AutoSave Timer Thread exiting");
                }

                saveEditorXMLConfig();
                super.windowClosing(e);
            }
        });

        setLayout(new MigLayout("insets 0 0 0 0"));
        setTitle(createWindowTitleString());
        setPreferredSize(new Dimension(1024,768));

        setJMenuBar(new EditorMenu());

        MapPanel mapPanel = new MapPanel();
        mapPanel.putClientProperty("Panel.insets", "0 0 0 0");
        TextPanel textPanel = new TextPanel();
        textPanel.setVisible(bTextPanelVisible);



        // Create the split pane
        editorSplitPane = new JSplitPane(JSplitPane.VERTICAL_SPLIT);
        // Give all extra space to the top component
        editorSplitPane.setResizeWeight(1.0);
        // Hide the divider
        editorSplitPane.setDividerSize(0);
        // Add the map panel and text panel to the split pane
        editorSplitPane.setTopComponent(mapPanel);
        editorSplitPane.setBottomComponent(textPanel);


        add(editorSplitPane, "grow, push");
        add(new ActionBar(), "north");
        //add(new ButtonToolbar(), "shrink, dock west");

        setIconImage(IconManager.getIconImage(IconManager.TRACTOR_ICON));



        pack();

        if (bWindowPositionSaved) {
            LOG.info("Stored window position/size is missing, reverting to defaults");
            setLocationRelativeTo(null);
        } else {
            LOG.info("Using saved window position/size");
            setLocation(windowX, windowY);
            setSize(windowWidth, windowHeight);
        }
        this.setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);

        new Toolbar();

        editor = this;
    }

    public static void main(String[] args) {

        // set look and feel to the system look and feel
        try {
            // register the JAR folder to look for .properties files
            FlatLaf.registerCustomDefaultsSource( "themes");
            // register the external folder to look for .properties files
            FlatLaf.registerCustomDefaultsSource("Themes");
            //UIManager.put("Button.margin", new Insets(3,3,3,3));
            ThemeManager.applySavedTheme();

            String fontName = "Dialog";

            Font font;
            if (isFontAvailable(fontName)) {
                Font debugFont = new Font(fontName, Font.PLAIN, 10);
                LOG.info("Using available font '{}' for debug", fontName);
            } else {
                // Load the font from resources
                LOG.info("Using bundled font '{}' for debug", fontName);
                Font debugFont = loadCustomFont("/fonts/" + fontName + ".ttf", 10);
            }
        } catch (Exception ex) {
            System.err.println( "Failed to initialize LaF" );
            ex.printStackTrace();
        }



        for (String arg : args) {
            if (Objects.equals(arg, "-EXPERIMENTAL")) {
                FlatInspector.install( "F5" );
                FlatUIDefaultsInspector.install("F6");
                EXPERIMENTAL = true;
                bIsDebugEnabled = true;
                LOG.info("##");
                LOG.info("## WARNING ..... Experimental features are unlocked, config corruption is possible.. USE --ONLY-- ON BACKUP CONFIGS!!");
                LOG.info("##");
            }
        }

        SwingUtilities.invokeLater(() -> {
            GlobalExceptionHandler globalExceptionHandler = new GlobalExceptionHandler();
            Thread.setDefaultUncaughtExceptionHandler(globalExceptionHandler);
            Thread.currentThread().setName("Main");
            new AutoDriveEditor().setVisible(true);
        });
    }

    public static void updateWindowTitle() {
        editor.setTitle(createWindowTitleString());
    }

    public static String createWindowTitleString() {
        StringBuilder sb = new StringBuilder();
        sb.append(COURSE_EDITOR_TITLE);
        if (xmlConfigFile != null) {
            sb.append(" - ").append(xmlConfigFile.getAbsolutePath()).append(isStale() ? " *" : "");
        }
        if (EXPERIMENTAL) {
            sb.append(" ( EXPERIMENTAL MODE )");
        } else if (bIsDebugEnabled) {
            sb.append(" ( DEBUG MODE )");
        }

        return sb.toString();
    }

    private static boolean isFontAvailable(String fontName) {
        GraphicsEnvironment ge = GraphicsEnvironment.getLocalGraphicsEnvironment();
        String[] availableFonts = ge.getAvailableFontFamilyNames();
        for (String availableFont : availableFonts) {
            if (availableFont.equalsIgnoreCase(fontName)) {
                return true;
            }
        }
        return false;
    }

    @SuppressWarnings("DataFlowIssue")
    private static Font loadCustomFont(String fontPath, float fontSize) {
        try {
            InputStream is = AutoDriveEditor.class.getResourceAsStream(fontPath);
            Font customFont = Font.createFont(Font.TRUETYPE_FONT, is).deriveFont(fontSize);
            GraphicsEnvironment ge = GraphicsEnvironment.getLocalGraphicsEnvironment();
            ge.registerFont(customFont);
            return customFont;
        } catch (FontFormatException | IOException | NullPointerException e) {
            e.printStackTrace();
            return new Font("Monospaced", Font.PLAIN, (int) fontSize); // Fallback font
        }
    }

    //
    // Getters
    //

    public static AutoDriveEditor getEditorReference() { return editor; }
    public static MapPanel getMapPanel() {
        return (MapPanel) editorSplitPane.getTopComponent();
    }
}
