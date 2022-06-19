package AutoDriveEditor.GUI;

import AutoDriveEditor.Listeners.CurvePanelListener;
import AutoDriveEditor.Listeners.EditorListener;
import AutoDriveEditor.MapPanel.MapPanel;

import javax.swing.*;
import javax.swing.border.BevelBorder;
import javax.swing.border.TitledBorder;
import javax.swing.event.AncestorEvent;
import javax.swing.event.AncestorListener;
import java.awt.*;

import static AutoDriveEditor.AutoDriveEditor.EXPERIMENTAL;
import static AutoDriveEditor.GUI.MenuBuilder.bDebugLogGUIInfo;
import static AutoDriveEditor.Locale.LocaleManager.localeString;
import static AutoDriveEditor.MapPanel.MapPanel.*;
import static AutoDriveEditor.Utils.GUIUtils.*;
import static AutoDriveEditor.Utils.LoggerUtils.LOG;
import static AutoDriveEditor.XMLConfig.EditorXML.*;
import static AutoDriveEditor.XMLConfig.GameXML.oldConfigFormat;
import static javax.swing.BoxLayout.X_AXIS;
import static javax.swing.BoxLayout.Y_AXIS;


public class GUIBuilder {

    // Editor states

    public static final int EDITORSTATE_NOOP = -1;
    public static final int EDITORSTATE_MOVING = 0;
    public static final int EDITORSTATE_CONNECTING = 1;
    public static final int EDITORSTATE_CREATE_PRIMARY_NODE = 2;
    public static final int EDITORSTATE_CHANGE_NODE_PRIORITY = 3;
    public static final int EDITORSTATE_CREATE_SUBPRIO_NODE = 4;
    public static final int EDITORSTATE_DELETE_NODES = 5;
    public static final int EDITORSTATE_CREATE_MARKER = 6;
    public static final int EDITORSTATE_EDIT_MARKER = 7;
    public static final int EDITORSTATE_DELETE_MARKER = 8;
    public static final int EDITORSTATE_ALIGN_HORIZONTAL = 9;
    public static final int EDITORSTATE_ALIGN_VERTICAL = 10;
    public static final int EDITORSTATE_ALIGN_DEPTH = 11;
    public static final int EDITORSTATE_ALIGN_EDIT_NODE = 12;
    public static final int EDITORSTATE_CNP_SELECT = 13;
    public static final int EDITORSTATE_QUADRATICBEZIER = 14;
    public static final int EDITORSTATE_CUBICBEZIER = 15;

    // Nodes Panel ActionCommands

    public static final String BUTTON_MOVE_NODES = "Move Nodes";
    public static final String BUTTON_CONNECT_NODES = "Connect Nodes";
    public static final String BUTTON_CREATE_PRIMARY_NODE = "Create Primary Node";
    public static final String BUTTON_CHANGE_NODE_PRIORITY = "Change Priority";
    public static final String BUTTON_CREATE_SUBPRIO_NODE = "Create Secondary Node";
    public static final String BUTTON_CREATE_REVERSE_CONNECTION = "Create Reverse Connection";
    public static final String BUTTON_CREATE_DUAL_CONNECTION = "Create Dual Connection";
    public static final String BUTTON_REMOVE_NODES = "Remove Nodes";

    // Curve Panel ActionCommands

    public static final String BUTTON_CREATE_QUADRATICBEZIER = "Quadratic Bezier";
    public static final String BUTTON_CREATE_CUBICBEZIER = "Cubic Bezier";
    public static final String BUTTON_COMMIT_CURVE = "Confirm Curve";
    public static final String BUTTON_CANCEL_CURVE = "Cancel Curve";
    public static final String RADIOBUTTON_PATHTYPE_REGULAR = "Regular";
    public static final String RADIOBUTTON_PATHTYPE_SUBPRIO = "SubPrio";
    public static final String RADIOBUTTON_PATHTYPE_REVERSE = "Reverse";
    public static final String RADIOBUTTON_PATHTYPE_DUAL = "Dual";

    // Markers Panel ActionCommands

    public static final String BUTTON_CREATE_DESTINATIONS = "Create Destinations";
    public static final String BUTTON_EDIT_DESTINATIONS_GROUPS = "Manage Destination Groups";
    public static final String BUTTON_DELETE_DESTINATIONS = "Remove Destinations";

    // Alignment Panel ActionCommands

    public static final String BUTTON_ALIGN_HORIZONTAL = "Horizontally Align Nodes";
    public static final String BUTTON_ALIGN_VERTICAL = "Vertically Align Nodes";
    public static final String BUTTON_ALIGN_DEPTH = "Depth Align Nodes";
    public static final String BUTTON_ALIGN_EDIT_NODE = "Edit Node Location";

    // Edit Panel ActionCommands

    public static final String BUTTON_COPYPASTE_SELECT = "CopyPaste Select";
    public static final String BUTTON_COPYPASTE_CUT = "CopyPaste Cut";
    public static final String BUTTON_COPYPASTE_COPY = "CopyPaste Copy";
    public static final String BUTTON_COPYPASTE_PASTE = "CopyPaste Paste";
    public static final String BUTTON_COPYPASTE_PASTE_ORIGINAL = "CopyPaste Original Location";

    // Option Panel ActionCommands

    public static final String BUTTON_OPTIONS_NODE_SIZE_INCREASE = "Node Size Up";
    public static final String BUTTON_OPTIONS_NODE_SIZE_DECREASE = "Node Size Down";
    public static final String BUTTON_OPTIONS_OPEN_CONFIG = "Open Config";
    public static final String BUTTON_OPTIONS_NETWORK_INFO = "Network Info";
    public static final String BUTTON_OPTIONS_CON_CONNECT = "Continuous Connections";

    // Curve Panel Button Listener
    public static CurvePanelListener curvePanelListener;


    // Main Window Reference

    public static MapPanel mapPanel;

    // default Editor State

    public static int editorState = GUIBuilder.EDITORSTATE_NOOP;


    // Node Panel Buttons

    public static JToggleButton moveNode;
    public static JToggleButton createRegularConnection;
    public static JToggleButton createPrimaryNode;
    public static JToggleButton createDualConnection;
    public static JToggleButton createSecondaryNode;
    public static JToggleButton createReverseConnection;
    public static JToggleButton changePriority;
    public static JToggleButton removeNode;

    // Curves Panel Buttons

    public static JToggleButton quadBezier;
    public static JToggleButton cubicBezier;

    // Markers Panel Buttons

    public static JToggleButton createDestination;
    public static JToggleButton editDestination;
    public static JToggleButton removeDestination;

    // Alignment Panel Buttons

    public static JToggleButton alignHorizontal;
    public static JToggleButton alignVertical;
    public static JToggleButton alignDepth;
    public static JToggleButton editNode;

    // Edit Panel Buttons

    public static JToggleButton select;
    public static JToggleButton cut;
    public static JToggleButton copy;
    public static JToggleButton paste;

    // Options Panel

    public static JToggleButton nodeSizeIncrease;
    public static JToggleButton nodeSizeDecrease;
    public static JToggleButton openConfig;
    public static JToggleButton networkInfo;
    public static JToggleButton conConnect;

    // Curve Panel

    public static JToggleButton commitCurve;
    public static JToggleButton cancelCurve;
    public static JSlider numIterationsSlider;
    public static JPanel curvePanel;
    public static JRadioButton curvePathRegular;
    public static JRadioButton curvePathSubPrio;
    public static JRadioButton curvePathReverse;
    public static JRadioButton curvePathDual;

    // Info Panel Labels

    public static JLabel imageLoadedLabel;
    public static JLabel heightMapLoadedLabel;
    public static JLabel currentMapSizeLabel;

    // Text Area Panel

    public static JTextArea textArea;

    // Toolbar Panels

    static JToolBar buttonToolbar = new JToolBar();
    static JPanel nodePanel = new JPanel();
    static JPanel curvesPanel = new JPanel();
    static JPanel markerPanel = new JPanel();
    static JPanel alignPanel = new JPanel();
    static JPanel editPanel = new JPanel();
    static JPanel optionsPanel = new JPanel();
    static JPanel testPanel = new JPanel();

    public static JPanel textPanel;



    public static MapPanel createMapPanel(EditorListener listener) {

        mapPanel = new MapPanel();
        mapPanel.setBorder(BorderFactory.createCompoundBorder(BorderFactory.createLoweredBevelBorder(), BorderFactory.createRaisedBevelBorder()));
        mapPanel.add( new AlphaContainer(initCurvePanel(listener)));
        //JRotation rot = new JRotation();
        //mapPanel.add(rot);
        return mapPanel;

    }

    public static JToolBar createButtonPanel(EditorListener editorListener, BorderLayout mainLayout, String layoutPosition, Boolean isFloating) {

        // Set the initial toolbar rotation

        boolean isToolbarHorizontal = layoutPosition.equals(BorderLayout.PAGE_START);
        if (isToolbarHorizontal) {
            buttonToolbar.setOrientation(SwingConstants.HORIZONTAL);
            buttonToolbar.setLayout(new FlowLayout());
        } else {
            buttonToolbar.setOrientation(SwingConstants.VERTICAL);
            buttonToolbar.setLayout(new BoxLayout(buttonToolbar, Y_AXIS));
        }

        buttonToolbar.addAncestorListener(new AncestorListener() {
            @Override
            public void ancestorAdded(AncestorEvent event) {
                SwingUtilities.invokeLater(() -> {
                    String pos = (String) mainLayout.getConstraints(event.getComponent()); // null if detached;
                    if (pos == null) {
                        toolbarPosition = "Floating";
                    } else if (pos.equals("First") || pos.equals("North")) {
                        toolbarPosition = "Top";
                    } else if (pos.equals("Before") || pos.equals("West")) {
                        toolbarPosition = "Left";
                    } else if (pos.equals("After") || pos.equals("East")) {
                        toolbarPosition = "Right";
                    }
                    if (bDebugLogGUIInfo) LOG.info("Toolbar Position Changed --> {} , {}", pos, toolbarPosition);
                });
            }

            @Override
            public void ancestorRemoved(AncestorEvent event) {}

            @Override
            public void ancestorMoved(AncestorEvent event) {}
        });
        buttonToolbar.addPropertyChangeListener(evt -> {
            String propertyName = evt.getPropertyName();
            if ("orientation".equals(propertyName)) {
                Integer newValue = (Integer) evt.getNewValue();
                if ( newValue == JToolBar.HORIZONTAL) {
                    switchToolbarLayoutToHorizontal();
                } else {
                    switchToolbarLayoutToVertical();
                }
            }
        });

        //
        // Create node panel
        //

        TitledBorder nodeBorder = BorderFactory.createTitledBorder(localeString.getString("panel_nodes"));
        nodeBorder.setTitleJustification(TitledBorder.CENTER);
        nodePanel.setBorder(nodeBorder);

        buttonToolbar.add(nodePanel);

        moveNode = makeImageToggleButton("buttons/movenode", "buttons/movenode_selected", BUTTON_MOVE_NODES,"nodes_move_tooltip","nodes_move_alt", nodePanel, false, null, false, editorListener);
        createRegularConnection = makeStateChangeImageToggleButton("buttons/connectregular", "buttons/connectregular_selected", BUTTON_CONNECT_NODES,"nodes_connect_tooltip","nodes_connect_alt", nodePanel, false, null, false, editorListener);
        createPrimaryNode = makeImageToggleButton("buttons/createprimary","buttons/createprimary_selected", BUTTON_CREATE_PRIMARY_NODE, "nodes_create_primary_tooltip", "nodes_create_primary_alt", nodePanel, false, null, false, editorListener);
        createDualConnection = makeStateChangeImageToggleButton("buttons/connectdual","buttons/connectdual_selected", BUTTON_CREATE_DUAL_CONNECTION, "nodes_create_dual_tooltip", "nodes_create_dual_alt", nodePanel, false, null, false, editorListener);
        createSecondaryNode = makeImageToggleButton("buttons/createsecondary","buttons/createsecondary_selected", BUTTON_CREATE_SUBPRIO_NODE, "nodes_create_secondary_tooltip", "nodes_create_secondary_alt", nodePanel, false, null, false, editorListener);
        createReverseConnection = makeStateChangeImageToggleButton("buttons/connectreverse","buttons/connectreverse_selected", BUTTON_CREATE_REVERSE_CONNECTION, "nodes_create_reverse_tooltip", "nodes_create_reverse_alt", nodePanel, false, null, false, editorListener);
        changePriority = makeImageToggleButton("buttons/swappriority","buttons/swappriority_selected", BUTTON_CHANGE_NODE_PRIORITY,"nodes_priority_tooltip","nodes_priority_alt", nodePanel, false, null, false, editorListener);
        removeNode = makeImageToggleButton("buttons/deletenodes","buttons/deletenodes_selected", BUTTON_REMOVE_NODES,"nodes_remove_tooltip","nodes_remove_alt", nodePanel, false, null, false, editorListener);

        //
        // Create Curve panel
        //

        TitledBorder curvesBorder = BorderFactory.createTitledBorder(localeString.getString("panel_curves"));
        curvesBorder.setTitleJustification(TitledBorder.CENTER);
        curvesPanel.setBorder(curvesBorder);

        buttonToolbar.add(curvesPanel);

        quadBezier = makeImageToggleButton("buttons/quadcurve","buttons/quadcurve_selected", BUTTON_CREATE_QUADRATICBEZIER, "panel_curves_quadbezier_tooltip", "panel_curves_quadbezier_alt", curvesPanel, false, null, false, editorListener);
        cubicBezier = makeImageToggleButton("buttons/cubiccurve","buttons/cubiccurve_selected", BUTTON_CREATE_CUBICBEZIER, "panel_curves_cubicbezier_tooltip", "panel_curves_cubicbezier_alt", curvesPanel, false, null,  false, editorListener);

        //
        // Create markers panel
        //

        TitledBorder markerBorder = BorderFactory.createTitledBorder(localeString.getString("panel_markers"));
        markerBorder.setTitleJustification(TitledBorder.CENTER);
        markerPanel.setBorder(markerBorder);
        //markerPanel.setBorder(BorderFactory.createTitledBorder(localeString.getString("panel_markers")));
        buttonToolbar.add(markerPanel);

        createDestination = makeImageToggleButton("buttons/addmarker","buttons/addmarker_selected", BUTTON_CREATE_DESTINATIONS,"markers_add_tooltip","markers_add_alt", markerPanel, false, null, false, editorListener);
        editDestination = makeImageToggleButton("buttons/editmarker","buttons/editmarker_selected", BUTTON_EDIT_DESTINATIONS_GROUPS,"markers_edit_tooltip","markers_edit_alt", markerPanel, false, null, false, editorListener);
        markerPanel.add(Box.createRigidArea(new Dimension(10, 1)));
        removeDestination = makeImageToggleButton("buttons/deletemarker","buttons/deletemarker_selected", BUTTON_DELETE_DESTINATIONS,"markers_delete_tooltip","markers_delete_alt", markerPanel, false, null, false, editorListener);

        //
        // Create alignment panel
        //

        TitledBorder alignmentBorder = BorderFactory.createTitledBorder(localeString.getString("panel_align"));
        alignmentBorder.setTitleJustification(TitledBorder.CENTER);
        alignPanel.setBorder(alignmentBorder);
        buttonToolbar.add(alignPanel);

        alignHorizontal = makeImageToggleButton("buttons/horizontalalign","buttons/horizontalalign_selected", BUTTON_ALIGN_HORIZONTAL,"align_horizontal_tooltip","align_horizontal_alt", alignPanel, false, null, false, editorListener);
        alignVertical = makeImageToggleButton("buttons/verticalalign","buttons/verticalalign_selected", BUTTON_ALIGN_VERTICAL,"align_vertical_tooltip","align_vertical_alt", alignPanel, false, null, false, editorListener);
        alignDepth = makeImageToggleButton("buttons/depthalign","buttons/depthalign_selected", BUTTON_ALIGN_DEPTH,"align_depth_tooltip","align_depth_alt", alignPanel, false, null, false, editorListener);
        editNode = makeImageToggleButton("buttons/editlocation","buttons/editlocation_selected", BUTTON_ALIGN_EDIT_NODE,"align_node_edit_tooltip","align_node_edit_alt", alignPanel, false, null, false, editorListener);

        //
        // copy/paste panel
        //

        TitledBorder copyBorder = BorderFactory.createTitledBorder(localeString.getString("panel_copypaste"));
        copyBorder.setTitleJustification(TitledBorder.CENTER);
        editPanel.setBorder(copyBorder);

        buttonToolbar.add(editPanel);

        select = makeImageToggleButton("buttons/select","buttons/select_selected", BUTTON_COPYPASTE_SELECT, "copypaste_select_tooltip","copypaste_select_alt", editPanel, false, null, false, editorListener);
        cut = makeImageToggleButton("buttons/cut","buttons/cut_selected", BUTTON_COPYPASTE_CUT, "copypaste_cut_tooltip","copypaste_cut_alt", editPanel, false, null, false, editorListener);
        copy = makeImageToggleButton("buttons/copy","buttons/copy_selected", BUTTON_COPYPASTE_COPY, "copypaste_copy_tooltip","copypaste_copy_alt", editPanel, false, null, false, editorListener);
        paste = makeImageToggleButton("buttons/paste","buttons/paste_selected", BUTTON_COPYPASTE_PASTE, "copypaste_paste_tooltip","copypaste_paste_alt", editPanel, false, null, false, editorListener);

        //
        // create options panel
        //

        TitledBorder optionsBorder = BorderFactory.createTitledBorder(localeString.getString("panel_options"));
        optionsBorder.setTitleJustification(TitledBorder.CENTER);
        optionsPanel.setBorder(optionsBorder);

        buttonToolbar.add(optionsPanel);

        nodeSizeDecrease = makeImageButton("buttons/nodeminus", "buttons/nodeminus_selected", BUTTON_OPTIONS_NODE_SIZE_DECREASE,"options_node_size_minus_tooltip","options_node_size_minus_alt", optionsPanel, editorListener);
        nodeSizeIncrease = makeImageButton("buttons/nodeplus", "buttons/nodeplus_selected", BUTTON_OPTIONS_NODE_SIZE_INCREASE,"options_node_size_plus_tooltip","options_node_size_plus_alt", optionsPanel, editorListener);
        openConfig = makeImageButton("buttons/config2", "buttons/config2_selected", BUTTON_OPTIONS_OPEN_CONFIG, "options_config_open_tooltip","options_config_open_alt", optionsPanel, editorListener);
        String tooltip;
        if (bContinuousConnections) {
            tooltip = "options_con_connect_enabled_tooltip";
        } else {
            tooltip = "options_con_connect_disabled_tooltip";
        }
        conConnect = makeImageToggleButton("buttons/conconnect", "buttons/conconnect_selected", BUTTON_OPTIONS_CON_CONNECT, tooltip, "options_con_connect_alt", optionsPanel, !bContinuousConnections, null, false, editorListener);

        if (EXPERIMENTAL) {
            networkInfo = makeImageButton("buttons/networkinfo", "buttons/networkinfo", BUTTON_OPTIONS_NETWORK_INFO,"options_info_network_info_tooltip","options_info_network_info_alt", optionsPanel, editorListener);
        }

        //
        // create experimental panel
        //

        testPanel.setBorder(BorderFactory.createTitledBorder(localeString.getString("panel_helper")));
        testPanel.setVisible(false);
        buttonToolbar.add(testPanel);


        testPanel.add(Box.createRigidArea(new Dimension(48, 0)));

        if (isToolbarHorizontal) {
            switchToolbarLayoutToHorizontal();
        } else {
            switchToolbarLayoutToVertical();
        }

        return buttonToolbar;
   }

    private static void switchToolbarLayoutToHorizontal() {
        buttonToolbar.setLayout(new FlowLayout());
        nodePanel.setLayout(new FlowLayout());
        curvesPanel.setLayout(new FlowLayout());
        markerPanel.setLayout(new FlowLayout());
        alignPanel.setLayout(new FlowLayout());
        editPanel.setLayout(new FlowLayout());
        optionsPanel.setLayout(new FlowLayout());
        testPanel.setLayout(new FlowLayout());
    }

    private static void switchToolbarLayoutToVertical() {
        buttonToolbar.setLayout(new BoxLayout(buttonToolbar, Y_AXIS));
        nodePanel.setLayout(new GridLayout(4,2,8,8));
        nodePanel.setMaximumSize(new Dimension(80, (int)nodePanel.getPreferredSize().getHeight()));
        curvesPanel.setLayout(new GridLayout(1,2,8,8));
        curvesPanel.setMaximumSize(new Dimension(80, (int)curvesPanel.getPreferredSize().getHeight()));
        markerPanel.setLayout(new GridLayout(2,2,8,8));
        markerPanel.setMaximumSize(new Dimension(80, (int)markerPanel.getPreferredSize().getHeight()));
        alignPanel.setLayout(new GridLayout(2,2,8,8));
        alignPanel.setMaximumSize(new Dimension(80, (int)alignPanel.getPreferredSize().getHeight()));
        editPanel.setLayout(new GridLayout(2,2,8,8));
        editPanel.setMaximumSize(new Dimension(80, (int) editPanel.getPreferredSize().getHeight()));
        if (EXPERIMENTAL) {
            optionsPanel.setLayout(new GridLayout(3,2,8,8));
        } else {
            optionsPanel.setLayout(new GridLayout(2,2,8,8));
        }
        optionsPanel.setMaximumSize(new Dimension(80, (int) optionsPanel.getPreferredSize().getHeight()));
        testPanel.setLayout(new GridLayout(2,2,8,8));
    }

    public static JPanel initCurvePanel(EditorListener editorListener) {

        // curve panel (hidden by default)

        curvePanel = new JPanel();
        curvePanel.setLayout(new BoxLayout(curvePanel, X_AXIS)); //create container ( left to right layout)
        curvePanel.setBorder(BorderFactory.createCompoundBorder(BorderFactory.createRaisedBevelBorder(), BorderFactory.createLoweredBevelBorder()));
        curvePanel.setVisible(false);
        curvePanel.setOpaque(true);
        curvePanel.setBackground(new Color(25,25,25,128));

        // create a panel for path radiobuttons using GridLayout

        JPanel curveRadioPanel = new JPanel();
        curveRadioPanel.setLayout(new GridLayout(2,2));
        curveRadioPanel.setBorder(BorderFactory.createBevelBorder(BevelBorder.RAISED, new Color(64,64,64), new Color(32,32,32)));
        curveRadioPanel.setOpaque(false);

        ButtonGroup pathNodeGroup = new ButtonGroup();

        curvePathRegular = makeRadioButton("panel_slider_radio_regular", RADIOBUTTON_PATHTYPE_REGULAR,"panel_slider_radio_regular_tooltip", Color.ORANGE,true, false, curveRadioPanel, pathNodeGroup, false, curvePanelListener);
        curvePathSubPrio = makeRadioButton("panel_slider_radio_subprio", RADIOBUTTON_PATHTYPE_SUBPRIO,"panel_slider_radio_subprio_tooltip", Color.ORANGE,false, false,curveRadioPanel, pathNodeGroup, false, curvePanelListener);
        curvePathReverse = makeRadioButton("panel_slider_radio_reverse", RADIOBUTTON_PATHTYPE_REVERSE,"panel_slider_radio_reverse_tooltip", Color.ORANGE,false, false,curveRadioPanel, null, false, curvePanelListener);
        curvePathDual = makeRadioButton("panel_slider_radio_dual", RADIOBUTTON_PATHTYPE_DUAL,"panel_slider_radio_dual_tooltip", Color.ORANGE,false, false,curveRadioPanel, null, false, curvePanelListener);

        curvePanel.add(curveRadioPanel);

        // create panel for slider using vertical layout

        JPanel interpSliderPanel = new JPanel();
        interpSliderPanel.setLayout(new BoxLayout(interpSliderPanel, Y_AXIS));
        interpSliderPanel.setBorder(BorderFactory.createEmptyBorder());
        interpSliderPanel.setOpaque(false);

        // add padding before the label to centre it

        interpSliderPanel.add(Box.createRigidArea(new Dimension(72, 5)));
        JLabel textLabel = new JLabel(localeString.getString("panel_slider_label"));
        textLabel.setForeground(Color.ORANGE);
        interpSliderPanel.add(textLabel);

        numIterationsSlider = new JSlider(JSlider.HORIZONTAL,0, curveSliderMax, curveSliderDefault);
        numIterationsSlider.setVisible(true);
        numIterationsSlider.setOpaque(false);
        numIterationsSlider.setForeground(Color.ORANGE);
        numIterationsSlider.setMajorTickSpacing(10);
        numIterationsSlider.setPaintTicks(true);
        numIterationsSlider.setPaintLabels(true);
        numIterationsSlider.addChangeListener(curvePanelListener);
        interpSliderPanel.add(numIterationsSlider);
        curvePanel.add(interpSliderPanel);

        curvePanel.add(Box.createRigidArea(new Dimension(8, 0)));
        commitCurve = makeImageToggleButton("curvepanel/confirm","curvepanel/confirm_select", BUTTON_COMMIT_CURVE,"panel_slider_confirm_curve","panel_slider_confirm_curve_alt", curvePanel, false, null, false, editorListener);
        curvePanel.add(Box.createRigidArea(new Dimension(8, 0)));
        cancelCurve = makeImageToggleButton("curvepanel/cancel","curvepanel/cancel_select", BUTTON_CANCEL_CURVE,"panel_slider_cancel_curve","panel_slider_cancel_curve_alt", curvePanel, false, null, false, editorListener);
        curvePanel.add(Box.createRigidArea(new Dimension(8, 0)));

        return curvePanel;
    }

    public static JPanel initTextPanel() {

        // Setup the text panel to show info to user
        textPanel = new JPanel(new BorderLayout());
        textArea = new JTextArea("Welcome to the AutoDrive Editor... Load a config to start editing..\n ",3,0);
        JScrollPane scrollPane = new JScrollPane(textArea);
        textArea.setEditable(false);
        Font textAreaFont = textArea.getFont();
        textArea.setFont(new Font(textArea.getFont().toString(), Font.PLAIN, textAreaFont.getSize() - 1));
        textPanel.add(scrollPane, BorderLayout.CENTER);

        // Setup the info panel to show if map images/heightmaps are loaded

        JPanel infoPanel = new JPanel(new GridLayout(3,2));
        infoPanel.setPreferredSize(new Dimension(150,50));
        infoPanel.setBorder(BorderFactory.createEtchedBorder());

        JLabel imageLabel = new JLabel(" Map Image : ");
        infoPanel.add(imageLabel);
        imageLoadedLabel = new JLabel("");
        infoPanel.add(imageLoadedLabel);

        JLabel heightMapLabel = new JLabel(" HeightMap : ");
        infoPanel.add(heightMapLabel);
        heightMapLoadedLabel = new JLabel("");
        infoPanel.add(heightMapLoadedLabel);

        JLabel mapSizeLabel = new JLabel(" Map Scale : ");
        infoPanel.add(mapSizeLabel);
        currentMapSizeLabel = new JLabel("");
        infoPanel.add(currentMapSizeLabel);

        textPanel.add(infoPanel, BorderLayout.LINE_END);

        return textPanel;
    }

    public static void updateGUIButtons(boolean enabled) {
        updateButtons();
        if (oldConfigFormat) {
            editorState = GUIBuilder.EDITORSTATE_NOOP;

            MenuBuilder.saveMenuEnabled(false);
            MenuBuilder.editMenuEnabled(false);
            enabled = false;
        }
        nodeBoxSetEnabled(enabled);
        markerBoxSetEnabled(enabled);
        alignBoxSetEnabled(enabled);
        copypasteBoxSetEnabled(enabled);
        optionsBoxSetEnabled(enabled);
    }

    private static void nodeBoxSetEnabled(boolean enabled) {
        moveNode.setEnabled(enabled);
        createRegularConnection.setEnabled(enabled);
        createPrimaryNode.setEnabled(enabled);
        changePriority.setEnabled(enabled);
        createSecondaryNode.setEnabled(enabled);
        createReverseConnection.setEnabled(enabled);
        removeNode.setEnabled(enabled);
        createDualConnection.setEnabled(enabled);
        quadBezier.setEnabled(enabled);
        cubicBezier.setEnabled(enabled);
    }
    private static void markerBoxSetEnabled(boolean enabled) {
        createDestination.setEnabled(enabled);
        editDestination.setEnabled(enabled);
        removeDestination.setEnabled(enabled);
    }

    private static void alignBoxSetEnabled(boolean enabled) {
        alignHorizontal.setEnabled(enabled);
        alignVertical.setEnabled(enabled);
        alignDepth.setEnabled(enabled);
        editNode.setEnabled(enabled);
    }

    private static void copypasteBoxSetEnabled(boolean enabled) {
        select.setEnabled(enabled);
        cut.setEnabled(enabled);
        copy.setEnabled(enabled);
        paste.setEnabled(enabled);
    }

    private static void optionsBoxSetEnabled(boolean enabled) {
        nodeSizeDecrease.setEnabled(enabled);
        nodeSizeIncrease.setEnabled(enabled);
        if (!ConfigGUI.isConfigWindowOpen) openConfig.setEnabled(true);
        conConnect.setEnabled(enabled);
        if (EXPERIMENTAL) {
            networkInfo.setEnabled(enabled);
        }
    }

    public static void  updateButtons() {
        moveNode.setSelected(false);
        createRegularConnection.setSelected(false);
        createPrimaryNode.setSelected(false);
        changePriority.setSelected(false);
        createSecondaryNode.setSelected(false);
        createReverseConnection.setSelected(false);
        createDualConnection.setSelected(false);
        removeNode.setSelected(false);
        quadBezier.setSelected(false);
        cubicBezier.setSelected(false);

        createDestination.setSelected(false);
        editDestination.setSelected(false);
        removeDestination.setSelected(false);

        alignHorizontal.setSelected(false);
        alignVertical.setSelected(false);
        alignDepth.setSelected(false);
        editNode.setSelected(false);

        select.setSelected(false);
        cut.setSelected(false);
        copy.setSelected(false);
        paste.setSelected(false);

        nodeSizeDecrease.setSelected(false);
        nodeSizeIncrease.setSelected(false);
        if (!ConfigGUI.isConfigWindowOpen) openConfig.setSelected(false);
        if (EXPERIMENTAL) {
            networkInfo.setSelected(false);
        }

        switch (editorState) {
            case EDITORSTATE_NOOP:
                isMultiSelectAllowed = false;
                rectangleStart = null;
                rectangleEnd = null;
                break;
            case EDITORSTATE_MOVING:
                moveNode.setSelected(true);
                showInTextArea("Left click ( or area select ) and drag to move", true, false);
                break;
            case EDITORSTATE_CONNECTING:
                if (connectionType == CONNECTION_STANDARD) {
                    createRegularConnection.setSelected(true);
                } else if (connectionType == CONNECTION_SUBPRIO) {
                    changePriority.setSelected(true);
                } else if (connectionType == CONNECTION_REVERSE) {
                    createReverseConnection.setSelected(true);
                } else if (connectionType == CONNECTION_DUAL) {
                    createDualConnection.setSelected(true);
                }
                showInTextArea("click on start node then on end node to create a connection", true, false);
                break;
            case EDITORSTATE_CREATE_PRIMARY_NODE:
                createPrimaryNode.setSelected(true);
                showInTextArea("click on map to create a primary node", true, false);
                break;
            case EDITORSTATE_CHANGE_NODE_PRIORITY:
                changePriority.setSelected(true);
                showInTextArea("click on a node to change it's priority, or area select to swap multiple nodes", true, false);
                break;
            case EDITORSTATE_CREATE_SUBPRIO_NODE:
                createSecondaryNode.setSelected(true);
                showInTextArea("click on map to create a secondary node", true, false);
                break;
            case EDITORSTATE_DELETE_NODES:
                removeNode.setSelected(true);
                showInTextArea("click to delete a node, or area select to delete multiple nodes", true, false);
                break;
            case EDITORSTATE_CREATE_MARKER:
                createDestination.setSelected(true);
                showInTextArea("click on a node to create a map marker", true, false);
                break;
            case EDITORSTATE_EDIT_MARKER:
                editDestination.setSelected(true);
                showInTextArea("click on a marker to edit", true, false);
                break;
            case EDITORSTATE_DELETE_MARKER:
                removeDestination.setSelected(true);
                showInTextArea("click on a node to delete it's map marker", true, false);
                break;
            case EDITORSTATE_ALIGN_HORIZONTAL:
                alignHorizontal.setSelected(true);
                showInTextArea("Hold Right click and drag to area select nodes, then click node to align too", true, false);
                break;
            case EDITORSTATE_ALIGN_VERTICAL:
                alignVertical.setSelected(true);
                showInTextArea("Hold Right click and drag to area select nodes, then click node to align too", true, false);
                break;
            case EDITORSTATE_ALIGN_DEPTH:
                alignDepth.setSelected(true);
                showInTextArea("Hold Right click and drag to area select nodes, then click node to align too", true, false);
                break;
            case EDITORSTATE_ALIGN_EDIT_NODE:
                editNode.setSelected(true);
                showInTextArea("click on a node to edit it's world location", true, false);
                break;
            case EDITORSTATE_CNP_SELECT:
                select.setSelected(true);
                showInTextArea("Hold Right click and drag to area select", true, false);
                break;
            case EDITORSTATE_QUADRATICBEZIER:
                quadBezier.setSelected(true);
                showInTextArea("click start node, then end node to create curve", true, false);
                break;
            case EDITORSTATE_CUBICBEZIER:
                cubicBezier.setSelected(true);
                showInTextArea("click start node, then end node to create curve", true, false);
                break;
        }
    }
}
