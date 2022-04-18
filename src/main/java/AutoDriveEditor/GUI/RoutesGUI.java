package AutoDriveEditor.GUI;

import javax.swing.*;
import javax.swing.border.TitledBorder;
import java.awt.*;
import java.awt.event.WindowEvent;
import java.io.File;
import java.util.LinkedList;

import AutoDriveEditor.XMLConfig.RouteManagerXML;

import static AutoDriveEditor.AutoDriveEditor.editor;
import static AutoDriveEditor.GUI.GUIImages.tractorImage;
import static AutoDriveEditor.MapPanel.MapPanel.getMapPanel;
import static AutoDriveEditor.Utils.GUIUtils.*;
import static AutoDriveEditor.Locale.LocaleManager.*;
import static AutoDriveEditor.Utils.LoggerUtils.*;
import static AutoDriveEditor.XMLConfig.GameXML.saveConfigFile;
import static AutoDriveEditor.XMLConfig.RouteManagerXML.*;

public class RoutesGUI extends JFrame {

    public static final String BUTTON_ROUTESGUI_LOAD = "RoutesGUI Load";
    public static final String BUTTON_ROUTESGUI_CANCEL = "RoutesGUI Cancel";

    public static RoutesGUI routesGUI;

    private static final int WIN_WIDTH = 400;
    private static final int WIN_HEIGHT = 300;

    private final JPanel labels;
    private final JPanel gui;
    private final JPanel buttons;

    private final JLabel nameLabel;
    private final JLabel filenameLabel;
    private final JLabel mapLabel;
    private final JLabel revisionLabel;
    private final JLabel dateLabel;
    private final JLabel serverIDLabel;

    private final JButton loadButton;
    private final JButton closeButton;

    private final DefaultListModel<String> model;
    private final JList<String> list;
    private final JScrollPane scrollPane;

    public static String fileName;
    public static String mapName;

    public RoutesGUI(File routesFile) {
        super();

        gui = new JPanel(new BorderLayout(5, 5));
        gui.setBorder(new TitledBorder("Routes"));

        //labels = new JPanel(new GridLayout(0, 1, 1, 1));
        labels = new JPanel();
        labels.setLayout(new BoxLayout(labels, BoxLayout.PAGE_AXIS));

        labels.setBorder(new TitledBorder("Route Details"));

        buttons = new JPanel(new GridLayout(1, 0, 1, 1));

        loadButton = makeButton(BUTTON_ROUTESGUI_LOAD, "panel_routes_gui_button_load_tooltip", "panel_routes_gui_button_load", buttons, true);
        closeButton = makeButton(BUTTON_ROUTESGUI_CANCEL, "panel_routes_gui_button_close_tooltip", "panel_routes_gui_button_close", buttons, true);

        loadButton.addActionListener(e -> {
            String routeFile = routesFile.getParentFile() + "\\routes\\" + fileName;
            //LinkedList<Route> routeList = getRoutesConfigContents(new File(routeFile));
            LOG.info("Full path = {} - {}", routeFile, mapName);
            if (getMapPanel().isStale()) {
                int response = JOptionPane.showConfirmDialog(editor, localeString.getString("dialog_exit_unsaved"), "AutoDrive", JOptionPane.YES_NO_OPTION, JOptionPane.QUESTION_MESSAGE);
                if (response == JOptionPane.YES_OPTION) {
                    saveConfigFile(null, false);
                }
            }
            loadRouteManagerXML(new File(routeFile), true, mapName);
        });

        closeButton.addActionListener(e -> routesGUI.dispatchEvent(new WindowEvent(routesGUI, WindowEvent.WINDOW_CLOSING)));

        nameLabel = new JLabel("Name: ");
        filenameLabel = new JLabel("Filename: ");
        mapLabel = new JLabel("Map Name: ");
        revisionLabel = new JLabel("Revision: ");
        dateLabel = new JLabel("Date: ");
        serverIDLabel = new JLabel("ServerID: ");

        labels.add(nameLabel);
        labels.add(Box.createRigidArea(new Dimension(0,2)));
        labels.add(filenameLabel);
        labels.add(Box.createRigidArea(new Dimension(0,2)));
        labels.add(mapLabel);
        labels.add(Box.createRigidArea(new Dimension(0,2)));
        labels.add(revisionLabel);
        labels.add(Box.createRigidArea(new Dimension(0,2)));
        labels.add(dateLabel);
        labels.add(Box.createRigidArea(new Dimension(0,2)));
        labels.add(serverIDLabel);
        labels.add(Box.createRigidArea(new Dimension(0,2)));

        model = new DefaultListModel<String>();
        list = new JList<String>(model);

        LinkedList<RouteManagerXML.Route> routeList = getRoutesConfigContents(routesFile);
        if (routeList != null) {
            for (Route route : routeList) {
                model.addElement(route.name);
            }
        }

        list.setSelectionMode(ListSelectionModel.SINGLE_SELECTION);
        list.addListSelectionListener(lse -> {
            if (routeList != null) {
                for (Route route : routeList) {
                    if (route.name.equals(list.getSelectedValue())) {
                        nameLabel.setText("Name:   " + route.name);
                        filenameLabel.setText("Filename:   " + route.fileName);
                        fileName = route.fileName;
                        mapLabel.setText("Map Name:   " + route.map);
                        mapName = route.map;
                        revisionLabel.setText("Revision: " + route.revision);
                        dateLabel.setText("Date: " + route.date);
                        serverIDLabel.setText("ServerID: " +  route.serverId);
                    }
                }

            }
        });

        int ebGap = 8;
        list.setBorder(BorderFactory.createEmptyBorder(ebGap, ebGap, ebGap, ebGap));

        scrollPane = new JScrollPane(list);

        gui.add(labels, BorderLayout.CENTER);
        gui.add(scrollPane, BorderLayout.WEST);
        gui.add(buttons, BorderLayout.SOUTH);
        add(gui);
    }

    @Override
    public Dimension getPreferredSize() {
        return new Dimension(WIN_WIDTH, WIN_HEIGHT);
    }

    public static void createRoutesGui(File routesFile, Component comp) {
        if (routesGUI != null) routesGUI.dispatchEvent(new WindowEvent(routesGUI, WindowEvent.WINDOW_CLOSING));
        routesGUI = new RoutesGUI(routesFile);
        routesGUI.setDefaultCloseOperation(JFrame.DISPOSE_ON_CLOSE);
        routesGUI.setTitle(localeString.getString("panel_routes_gui_title"));
        routesGUI.setIconImage(tractorImage);
        routesGUI.pack();
        routesGUI.setLocationRelativeTo(comp);
        routesGUI.setVisible(true);

    }
}
