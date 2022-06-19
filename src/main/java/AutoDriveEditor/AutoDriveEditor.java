package AutoDriveEditor;

import AutoDriveEditor.GUI.GUIBuilder;
import AutoDriveEditor.GUI.MenuBuilder;
import AutoDriveEditor.Handlers.GlobalExceptionHandler;
import AutoDriveEditor.Listeners.CurvePanelListener;
import AutoDriveEditor.Listeners.EditorListener;
import AutoDriveEditor.Locale.LocaleManager;
import AutoDriveEditor.Managers.ChangeManager;
import AutoDriveEditor.Managers.VersionManager;
import AutoDriveEditor.RoadNetwork.RoadMap;

import javax.swing.*;
import java.awt.*;
import java.awt.event.WindowAdapter;
import java.awt.event.WindowEvent;
import java.util.Objects;

import static AutoDriveEditor.GUI.GUIImages.*;
import static AutoDriveEditor.Locale.LocaleManager.localeString;
import static AutoDriveEditor.MapPanel.MapPanel.*;
import static AutoDriveEditor.Utils.LoggerUtils.LOG;
import static AutoDriveEditor.XMLConfig.EditorXML.*;
import static AutoDriveEditor.XMLConfig.GameXML.saveConfigFile;
import static AutoDriveEditor.XMLConfig.GameXML.xmlConfigFile;

public class AutoDriveEditor extends JFrame {

    public static final String COURSE_EDITOR_VERSION = "1.0.2";
    public static final String COURSE_EDITOR_NAME = "AutoDrive Course Editor";
    public static final String COURSE_EDITOR_TITLE = COURSE_EDITOR_NAME + " " + COURSE_EDITOR_VERSION;
    public static final String COURSE_EDITOR_BUILD_INFO = "Java 13 SDK + IntelliJ IDEA 2022.1.2 Community Edition";


    public static EditorListener editorListener;
    public static AutoDriveEditor editor;

    public static ChangeManager changeManager;

    public static boolean DEBUG = false;
    public static boolean EXPERIMENTAL = false;

    public AutoDriveEditor() {
        super();

        LOG.info("Starting AutoDrive Editor v{} .....", COURSE_EDITOR_VERSION);
        LOG.info("Java Runtime Version {}", Runtime.version().feature());
        LocaleManager.setLocale();
        setTitle(createTitle());
        loadIcons();
        loadEditorXMLConfig();
        setPreferredSize(new Dimension(1024,768));
        addWindowListener(new WindowAdapter() {
            @Override
            public void windowOpened(WindowEvent we) {
                VersionManager.updateCheck();
            }
            @Override
            public void windowClosing(WindowEvent e) {
                if (getMapPanel().isStale()) {
                    int response = JOptionPane.showConfirmDialog(e.getComponent(), localeString.getString("dialog_exit_unsaved"), "AutoDrive", JOptionPane.YES_NO_OPTION, JOptionPane.QUESTION_MESSAGE);
                    if (response == JOptionPane.YES_OPTION) {
                        saveConfigFile(null, false, false);
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

        editorListener = new EditorListener(this);
        GUIBuilder.curvePanelListener = new CurvePanelListener();

        // init menu bar
        MenuBuilder.createMenu();
        setJMenuBar(MenuBuilder.menuBar);

        String layoutPosition = BorderLayout.PAGE_START;
        boolean isFloating = false;

        LOG.info("Toolbar Config Location {}", toolbarPosition);
        switch (toolbarPosition) {
            case "Floating":
                isFloating = true;
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


        this.add(GUIBuilder.createButtonPanel(editorListener, mainLayout, layoutPosition, isFloating), layoutPosition);
        this.add(GUIBuilder.createMapPanel(editorListener), BorderLayout.CENTER);
        this.add(GUIBuilder.initTextPanel(), BorderLayout.PAGE_END);

        MenuBuilder.editMenuEnabled(false);
        GUIBuilder.updateGUIButtons(false);
        setIconImage(getTractorImage());
        pack();
        if (noSavedWindowPosition) {
            LOG.info("Invalid saved window Location/Size");
            setLocationRelativeTo(null);
        } else {
            setLocation( x, y);
            setSize(width, height);
        }
        this.setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);

        //changeManager = new ChangeManager();
    }

    public static void main(String[] args) {

        // set look and feel to the system look and feel
        try {

            UIManager.setLookAndFeel(UIManager.getSystemLookAndFeelClassName());
        } catch (Exception ex) {
            LOG.error(ex.getMessage(), ex);
        }

        for (String arg : args) {
            if (Objects.equals(arg, "-DEBUG")) {
                DEBUG = true;
                LOG.info("##");
                LOG.info("## WARNING ..... Debug Mode is enabled, a lot of logging may occur, performance will be effected!!");
                LOG.info("##");
            }
            if (Objects.equals(arg, "-EXPERIMENTAL")) {
                EXPERIMENTAL = true;
                DEBUG = true;
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

    public static String createTitle() {
        StringBuilder sb = new StringBuilder();
        sb.append(COURSE_EDITOR_TITLE);
        if (xmlConfigFile != null) {
            sb.append(" - ").append(xmlConfigFile.getAbsolutePath()).append(getMapPanel().isStale() ? " *" : "");
        }
        if (RoadMap.mapName != null) {
            sb.append(" ( ").append(RoadMap.mapName).append(" )");
        }
        return sb.toString();
    }
}
