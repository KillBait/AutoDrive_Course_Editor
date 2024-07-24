package AutoDriveEditor;

import AutoDriveEditor.GUI.ButtonToolbar;
import AutoDriveEditor.GUI.MapPanel;
import AutoDriveEditor.GUI.Menus.EditorMenu;
import AutoDriveEditor.GUI.RenderThreads.ConnectionDrawThread;
import AutoDriveEditor.GUI.RenderThreads.NodeDrawThread;
import AutoDriveEditor.GUI.TextPanel;
import AutoDriveEditor.Handlers.GlobalExceptionHandler;
import AutoDriveEditor.Locale.LocaleManager;
import AutoDriveEditor.Managers.ButtonManager;
import AutoDriveEditor.Managers.ChangeManager;
import AutoDriveEditor.Managers.MultiSelectManager;
import AutoDriveEditor.Managers.VersionManager;
import AutoDriveEditor.RoadNetwork.RoadMap;

import javax.swing.*;
import java.awt.*;
import java.awt.event.WindowAdapter;
import java.awt.event.WindowEvent;
import java.util.Objects;

import static AutoDriveEditor.GUI.EditorImages.getTractorImage;
import static AutoDriveEditor.GUI.EditorImages.loadIcons;
import static AutoDriveEditor.GUI.MapPanel.isStale;
import static AutoDriveEditor.Locale.LocaleManager.getLocaleString;
import static AutoDriveEditor.Utils.LoggerUtils.LOG;
import static AutoDriveEditor.XMLConfig.AutoSave.scheduledExecutorService;
import static AutoDriveEditor.XMLConfig.EditorXML.*;
import static AutoDriveEditor.XMLConfig.GameXML.saveGameConfig;
import static AutoDriveEditor.XMLConfig.GameXML.xmlConfigFile;

public class AutoDriveEditor extends JFrame {

    public static final String COURSE_EDITOR_VERSION = "1.0.8";
    public static final String COURSE_EDITOR_NAME = "AutoDrive Course Editor";
    public static final String COURSE_EDITOR_TITLE = COURSE_EDITOR_NAME + " " + COURSE_EDITOR_VERSION;
    public static final String COURSE_EDITOR_BUILD_INFO = "Java 11 SDK + IntelliJ IDEA 2022.2.3 Community Edition";

    public static AutoDriveEditor editor;

    public static MapPanel mapPanel;

    public static ChangeManager changeManager;
    public static ButtonManager buttonManager;
    public static MultiSelectManager multiSelectManager;


    public static boolean EXPERIMENTAL = false;
    public static boolean bIsDebugEnabled = false;
    public AutoDriveEditor() {
        super();
        LOG.info("Starting AutoDrive Editor v{} .....", COURSE_EDITOR_VERSION);
        LOG.info("Using Java Runtime Version {}", Runtime.version().feature());
        LocaleManager.setLocale();
        setTitle(createWindowTitleString());
        loadIcons();
        loadEditorXMLConfig();
        setPreferredSize(new Dimension(1024,768));
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
        BorderLayout mainLayout = new BorderLayout();
        setLayout(mainLayout);



        String layoutPosition = BorderLayout.PAGE_START;

        switch (toolbarPosition) {
            case "Floating":
                LOG.info("Toolbar Location set to - Floating");
                break;
            case "Left":
                layoutPosition = BorderLayout.LINE_START;
                LOG.info("Toolbar Location set to - Left");
                break;
            case "Right":
                layoutPosition = BorderLayout.LINE_END;
                LOG.info("Toolbar Location set to - Right");
                break;
            default:
                layoutPosition = BorderLayout.PAGE_START;
                LOG.info("Toolbar Location set to - Top");
                break;
        }

        buttonManager = new ButtonManager();
        multiSelectManager = new MultiSelectManager();

        mapPanel = new MapPanel();

        // Init menu bar

        setJMenuBar(new EditorMenu());

        add(mapPanel, BorderLayout.CENTER);
        add(new ButtonToolbar(mainLayout, layoutPosition), layoutPosition);
        add(new TextPanel(), BorderLayout.PAGE_END);

        setIconImage(getTractorImage());
        editor = this;
        pack();

        if (bNoSavedWindowPosition) {
            LOG.info("Invalid saved window Location/Size");
            setLocationRelativeTo(null);
        } else {
            setLocation(x, y);
            setSize(width, height);
        }
        this.setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
    }

    public static void main(String[] args) {

        // set look and feel to the system look and feel
        try {
            //UIManager.setLookAndFeel("javax.swing.plaf.metal.MetalLookAndFeel");
            UIManager.setLookAndFeel(UIManager.getSystemLookAndFeelClassName());
        } catch (Exception ex) {
            LOG.error(ex.getMessage(), ex);
        }


        for (String arg : args) {
            if (Objects.equals(arg, "-EXPERIMENTAL")) {
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
        if (RoadMap.mapName != null) {
            sb.append(" ( ").append(RoadMap.mapName).append(" )");
        }
        if (EXPERIMENTAL) {
            sb.append(" ( EXPERIMENTAL MODE )");
        } else if (bIsDebugEnabled) {
            sb.append(" ( DEBUG MODE )");
        }

        return sb.toString();
    }

    //
    // Getters
    //

    public static AutoDriveEditor getEditorReference() { return editor; }
    public static MapPanel getMapPanel() {
        return mapPanel;
    }
}
