package AutoDriveEditor;

import AutoDriveEditor.GUI.GUIBuilder;
import AutoDriveEditor.GUI.MenuBuilder;
import AutoDriveEditor.Handlers.GlobalExceptionHandler;
import AutoDriveEditor.Listeners.CurvePanelListener;
import AutoDriveEditor.Listeners.EditorListener;
import AutoDriveEditor.Locale.LocaleManager;
import AutoDriveEditor.Managers.ChangeManager;
import AutoDriveEditor.Managers.VersionManager;

import javax.swing.*;
import java.awt.*;
import java.awt.event.WindowAdapter;
import java.awt.event.WindowEvent;
import java.util.Objects;

import static AutoDriveEditor.GUI.GUIImages.loadIcons;
import static AutoDriveEditor.GUI.GUIImages.tractorImage;
import static AutoDriveEditor.Locale.LocaleManager.localeString;
import static AutoDriveEditor.MapPanel.MapPanel.*;
import static AutoDriveEditor.Utils.LoggerUtils.LOG;
import static AutoDriveEditor.XMLConfig.EditorXML.*;
import static AutoDriveEditor.XMLConfig.GameXML.saveConfigFile;
import static AutoDriveEditor.XMLConfig.GameXML.xmlConfigFile;



public class AutoDriveEditor extends JFrame {

    public static final String AUTODRIVE_INTERNAL_VERSION = "0.51.0";
    public static final String AUTODRIVE_COURSE_EDITOR_TITLE = "AutoDrive Course Editor " + AUTODRIVE_INTERNAL_VERSION + " Beta";


    public EditorListener editorListener;
    public static AutoDriveEditor editor;

    public static ChangeManager changeManager;

    public static boolean DEBUG = false;
    public static boolean EXPERIMENTAL = false;
    private String lastRunVersion;

    public AutoDriveEditor() {
        super();

        LOG.info("Starting AutoDrive Editor v{} .....", AUTODRIVE_INTERNAL_VERSION);
        LOG.info("Java Runtime Version {}", Runtime.version().feature());
        LocaleManager.setLocale();
        setTitle(createTitle());
        loadIcons();
        loadEditorXMLConfig();
        setPreferredSize(new Dimension(1024,768));
        addWindowListener(new WindowAdapter() {
            @Override
            public void windowClosing(WindowEvent e) {
                if (getMapPanel().isStale()) {
                    int response = JOptionPane.showConfirmDialog(e.getComponent(), localeString.getString("dialog_exit_unsaved"), "AutoDrive", JOptionPane.YES_NO_OPTION, JOptionPane.QUESTION_MESSAGE);
                    if (response == JOptionPane.YES_OPTION) {
                        saveConfigFile(null);
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

                saveEditorXMLConfig();
                super.windowClosing(e);
            }
        });
        setLayout(new BorderLayout());

        editorListener = new EditorListener(this);
        GUIBuilder.curvePanelListener = new CurvePanelListener();

        // init menu bar
        MenuBuilder.createMenu();
        setJMenuBar(MenuBuilder.menuBar);

        this.add(GUIBuilder.createButtonPanel(editorListener), BorderLayout.PAGE_START);
        this.add(GUIBuilder.createMapPanel(this, editorListener), BorderLayout.CENTER);
        this.add(GUIBuilder.initTextPanel(), BorderLayout.PAGE_END);

        MenuBuilder.editMenuEnabled(false);
        GUIBuilder.updateGUIButtons(false);
        setIconImage(tractorImage);
        pack();
        if (noSavedWindowPosition) {
            LOG.info("Invalid saved window Location/Size");
            setLocationRelativeTo(null);
        } else {
            setLocation( x, y);
            setSize(width, height);
        }
        this.setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);

        VersionManager.getVersionXML();
        if (lastRunVersion != null && !lastRunVersion.equals(AUTODRIVE_INTERNAL_VERSION)) {
            LOG.info("Version Updated Detected");
            // TODO display new version notes
        }

        changeManager = new ChangeManager();


        LOG.info("Setup Complete....");
    }

    public static void main(String[] args) {

        //System.setProperty("sun.java2d.opengl", "True");

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
                LOG.info("## WARNING ..... Debug mode active, editor performance may be slower then normal");
                LOG.info("##");
            }
            if (Objects.equals(arg, "-EXPERIMENTAL")) {
                EXPERIMENTAL = true;
                LOG.info("##");
                LOG.info("## WARNING ..... Experimental features are unlocked, config corruption is possible.. USE --ONLY-- ON BACKUP CONFIGS!!");
                LOG.info("##");
            }
        }

        SwingUtilities.invokeLater(() -> {
            GlobalExceptionHandler globalExceptionHandler = new GlobalExceptionHandler();
            Thread.setDefaultUncaughtExceptionHandler(globalExceptionHandler);
            new AutoDriveEditor().setVisible(true);
        });
    }

    public static String createTitle() {
        StringBuilder sb = new StringBuilder();
        sb.append(AUTODRIVE_COURSE_EDITOR_TITLE);
        if (xmlConfigFile != null) {
            sb.append(" - ").append(xmlConfigFile.getAbsolutePath()).append(getMapPanel().isStale() ? " *" : "");
        }
        return sb.toString();
    }
}
