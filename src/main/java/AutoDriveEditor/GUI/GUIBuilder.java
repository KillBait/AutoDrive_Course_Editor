package AutoDriveEditor.GUI;

import AutoDriveEditor.GUI.Buttons.Alignment.DepthAlignButton;
import AutoDriveEditor.GUI.Buttons.Alignment.EditLocationButton;
import AutoDriveEditor.GUI.Buttons.Alignment.HorizontalAlignButton;
import AutoDriveEditor.GUI.Buttons.Alignment.VerticalAlignButton;
import AutoDriveEditor.GUI.Buttons.Curves.CubicCurveButton;
import AutoDriveEditor.GUI.Buttons.Curves.QuadCurveButton;
import AutoDriveEditor.GUI.Buttons.Editing.*;
import AutoDriveEditor.GUI.Buttons.Markers.AddMarkerButton;
import AutoDriveEditor.GUI.Buttons.Markers.DeleteMarkerButton;
import AutoDriveEditor.GUI.Buttons.Markers.EditMarkerButton;
import AutoDriveEditor.GUI.Buttons.Nodes.*;
import AutoDriveEditor.GUI.Buttons.Options.*;
import AutoDriveEditor.GUI.Buttons.Testing.TestButton;
import AutoDriveEditor.Listeners.CurvePanelListener;
import AutoDriveEditor.MapPanel.MapPanel;

import javax.swing.*;
import javax.swing.border.BevelBorder;
import javax.swing.border.TitledBorder;
import javax.swing.event.AncestorEvent;
import javax.swing.event.AncestorListener;
import java.awt.*;

import static AutoDriveEditor.AutoDriveEditor.EXPERIMENTAL;
import static AutoDriveEditor.AutoDriveEditor.buttonManager;
import static AutoDriveEditor.GUI.MenuBuilder.bDebugLogGUIInfo;
import static AutoDriveEditor.Locale.LocaleManager.getLocaleString;
import static AutoDriveEditor.Utils.GUIUtils.*;
import static AutoDriveEditor.Utils.LoggerUtils.LOG;
import static AutoDriveEditor.XMLConfig.EditorXML.*;
import static javax.swing.BoxLayout.X_AXIS;
import static javax.swing.BoxLayout.Y_AXIS;
import static javax.swing.ScrollPaneConstants.VERTICAL_SCROLLBAR_NEVER;


public class GUIBuilder {

    // Curve Panel ActionCommands

    public static final String BUTTON_COMMIT_CURVE = "Confirm Curve";
    public static final String BUTTON_CANCEL_CURVE = "Cancel Curve";
    public static final String RADIOBUTTON_PATHTYPE_REGULAR = "Regular";
    public static final String RADIOBUTTON_PATHTYPE_SUBPRIO = "SubPrio";
    public static final String RADIOBUTTON_PATHTYPE_REVERSE = "Reverse";
    public static final String RADIOBUTTON_PATHTYPE_DUAL = "Dual";

    // Main Window Reference

    public static MapPanel mapPanel;

    // Curve Panel

    public static JButton commitCurve;
    public static JButton cancelCurve;
    public static JSlider numIterationsSlider;
    public static JPanel curveOptionsPanel;
    public static JRadioButton curvePathRegular;
    public static JRadioButton curvePathSubPrio;
    public static JRadioButton curvePathReverse;
    public static JRadioButton curvePathDual;

    // Text Area Panel

    public static JTextArea textArea;

    // Info Panel Labels

    public static JLabel imageLoadedLabel;
    public static JLabel heightMapLoadedLabel;
    public static JLabel currentMapSizeLabel;

    // Toolbar Panels

    public static JToolBar buttonToolbar = new JToolBar();
    static JPanel nodePanel = new JPanel();
    static JPanel curvesPanel = new JPanel();
    static JPanel markerPanel = new JPanel();
    static JPanel alignPanel = new JPanel();
    static JPanel editPanel = new JPanel();
    static JPanel optionsPanel = new JPanel();
    static JPanel experimentalPanel = new JPanel();

    public static JPanel textPanel;


    public static MapPanel createMapPanel() {

        mapPanel = new MapPanel();
        mapPanel.setBorder(BorderFactory.createCompoundBorder(BorderFactory.createLoweredBevelBorder(), BorderFactory.createRaisedBevelBorder()));
        mapPanel.add( new AlphaContainer(initCurvePanel(new CurvePanelListener())));
        return mapPanel;

    }

    public static JToolBar createButtonPanel(BorderLayout mainLayout, String layoutPosition) {

        if (bLockToolbarPosition) buttonToolbar.setFloatable(false);

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

        TitledBorder nodeBorder = BorderFactory.createTitledBorder(getLocaleString("panel_nodes"));
        nodeBorder.setTitleJustification(TitledBorder.CENTER);
        nodePanel.setBorder(nodeBorder);


        buttonToolbar.add(nodePanel);
        buttonToolbar.add(Box.createRigidArea(new Dimension(10, 10)));

        buttonManager.addButton(new MoveNodeButton(nodePanel));
        buttonManager.addButton(new NormalConnectionButton(nodePanel));
        buttonManager.addButton(new AddPrimaryNodeButton(nodePanel));
        buttonManager.addButton(new DualConnectionButton(nodePanel));
        buttonManager.addButton(new AddSecondaryNodeButton(nodePanel));
        buttonManager.addButton(new ReverseConnectionButton(nodePanel));
        buttonManager.addButton(new NodePriorityButton(nodePanel));
        buttonManager.addButton(new DeleteNodeButton(nodePanel));
        if (EXPERIMENTAL) {
            buttonManager.addButton(new FlipDirectionButton(nodePanel));
            buttonManager.addButton(new RoadConnection(nodePanel));
        }



        //
        // Create Curve panel
        //

        TitledBorder curvesBorder = BorderFactory.createTitledBorder(getLocaleString("panel_curves"));
        curvesBorder.setTitleJustification(TitledBorder.CENTER);
        curvesPanel.setBorder(curvesBorder);
        buttonToolbar.add(curvesPanel);
        buttonToolbar.add(Box.createRigidArea(new Dimension(10, 10)));

        buttonManager.addButton(new QuadCurveButton(curvesPanel));
        buttonManager.addButton(new CubicCurveButton(curvesPanel));

        //
        // Create markers panel
        //

        TitledBorder markerBorder = BorderFactory.createTitledBorder(getLocaleString("panel_markers"));
        markerBorder.setTitleJustification(TitledBorder.CENTER);
        markerPanel.setBorder(markerBorder);
        buttonToolbar.add(markerPanel);
        buttonToolbar.add(Box.createRigidArea(new Dimension(10, 10)));

        buttonManager.addButton(new AddMarkerButton(markerPanel));
        buttonManager.addButton(new EditMarkerButton(markerPanel));

        markerPanel.add(Box.createRigidArea(new Dimension(10, 1)));
        buttonManager.addButton(new DeleteMarkerButton(markerPanel));

        //
        // Create alignment panel
        //

        TitledBorder alignmentBorder = BorderFactory.createTitledBorder(getLocaleString("panel_align"));
        alignmentBorder.setTitleJustification(TitledBorder.CENTER);
        alignPanel.setBorder(alignmentBorder);
        buttonToolbar.add(alignPanel);
        buttonToolbar.add(Box.createRigidArea(new Dimension(10, 10)));

        buttonManager.addButton(new HorizontalAlignButton(alignPanel));
        buttonManager.addButton(new VerticalAlignButton(alignPanel));
        buttonManager.addButton(new DepthAlignButton(alignPanel));
        buttonManager.addButton(new EditLocationButton(alignPanel));

        //
        // copy/paste panel
        //

        TitledBorder copyBorder = BorderFactory.createTitledBorder(getLocaleString("panel_copypaste"));
        copyBorder.setTitleJustification(TitledBorder.CENTER);
        editPanel.setBorder(copyBorder);
        buttonToolbar.add(editPanel);
        buttonToolbar.add(Box.createRigidArea(new Dimension(10, 10)));

        buttonManager.addButton(new AreaSelectButton(editPanel));
        buttonManager.addButton(new CutSelectionButton(editPanel));
        buttonManager.addButton(new CopySelectionButton(editPanel));
        buttonManager.addButton(new PasteSelectionButton(editPanel));
        buttonManager.addButton(new RotationButton(editPanel));

        //
        // create options panel
        //

        TitledBorder optionsBorder = BorderFactory.createTitledBorder(getLocaleString("panel_options"));
        optionsBorder.setTitleJustification(TitledBorder.CENTER);
        optionsPanel.setBorder(optionsBorder);
        buttonToolbar.add(optionsPanel);
        buttonToolbar.add(Box.createRigidArea(new Dimension(10, 10)));

        buttonManager.addButton(new OpenConfigButton(optionsPanel));
        buttonManager.addButton(new ContinuousConnectionButton(optionsPanel));
        buttonManager.addButton(new NodeSizeUpButton(optionsPanel));
        buttonManager.addButton(new NodeSizeDownButton(optionsPanel));
        buttonManager.addButton(new GridDisplayButton(optionsPanel));
        buttonManager.addButton(new GridSnapButton(optionsPanel));

        //
        // create experimental panel
        //

        if (EXPERIMENTAL) {
            TitledBorder experimentalBorder = BorderFactory.createTitledBorder(getLocaleString("nodes_test_alt"));
            experimentalBorder.setTitleJustification(TitledBorder.CENTER);
            experimentalPanel.setBorder(experimentalBorder);
            buttonToolbar.add(experimentalPanel);

            buttonManager.addButton(new TestButton(experimentalPanel));
            //networkInfo = makeImageButton("buttons/networkinfo", "buttons/networkinfo", BUTTON_OPTIONS_NETWORK_INFO,"options_info_network_info_tooltip","options_info_network_info_alt", optionsPanel, editorListener);

        }

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
        if (EXPERIMENTAL) {
            experimentalPanel.setLayout(new FlowLayout());
        }
    }

    private static void switchToolbarLayoutToVertical() {
        buttonToolbar.setLayout(new BoxLayout(buttonToolbar, Y_AXIS));

        if (EXPERIMENTAL) {
            nodePanel.setLayout(new GridLayout(5,2,8,8));
        } else {
            nodePanel.setLayout(new GridLayout(4,2,8,8));
        }

        nodePanel.setMaximumSize(new Dimension(80, (int)nodePanel.getPreferredSize().getHeight()));
        curvesPanel.setLayout(new GridLayout(1,2,8,8));
        curvesPanel.setMaximumSize(new Dimension(80, (int)curvesPanel.getPreferredSize().getHeight()));
        markerPanel.setLayout(new GridLayout(2,2,8,8));
        markerPanel.setMaximumSize(new Dimension(80, (int)markerPanel.getPreferredSize().getHeight()));
        alignPanel.setLayout(new GridLayout(2,2,8,8));
        alignPanel.setMaximumSize(new Dimension(80, (int)alignPanel.getPreferredSize().getHeight()));
        editPanel.setLayout(new GridLayout(3,2,8,8));
        editPanel.setMaximumSize(new Dimension(80, (int) editPanel.getPreferredSize().getHeight()));
        optionsPanel.setLayout(new GridLayout(3,2,8,8));
        optionsPanel.setMaximumSize(new Dimension(80, (int) optionsPanel.getPreferredSize().getHeight()));
        if (EXPERIMENTAL) {
            experimentalPanel.setLayout(new GridLayout(4,2,8,8));
            experimentalPanel.setMaximumSize(new Dimension(80, (int) experimentalPanel.getPreferredSize().getHeight()));
        }
    }

    public static JPanel initCurvePanel(CurvePanelListener curvePanelListener) {

        // curve panel (hidden by default)

        curveOptionsPanel = new JPanel();
        curveOptionsPanel.setLayout(new BoxLayout(curveOptionsPanel, X_AXIS)); //create container ( left to right layout)
        curveOptionsPanel.setBorder(BorderFactory.createCompoundBorder(BorderFactory.createRaisedBevelBorder(), BorderFactory.createLoweredBevelBorder()));
        curveOptionsPanel.setVisible(false);
        curveOptionsPanel.setOpaque(true);
        curveOptionsPanel.setBackground(new Color(25,25,25,128));

        // create a panel for path radio buttons

        GridBagConstraints gbc = new GridBagConstraints();
        JPanel curveRadioPanel = new JPanel(new SpringLayout());
        curveRadioPanel.setBorder(BorderFactory.createBevelBorder(BevelBorder.RAISED, new Color(64,64,64), new Color(32,32,32)));
        curveRadioPanel.setOpaque(false);

        gbc.fill = GridBagConstraints.BOTH;
        gbc.anchor = GridBagConstraints.FIRST_LINE_END;
        gbc.weightx = 0;
        gbc.weighty = 0;
        gbc.ipady = 0;
        gbc.gridx = 2;
        gbc.gridy = 0;
        gbc.gridwidth = 1;
        gbc.gridheight = 2;
        curveOptionsPanel.add(curveRadioPanel, gbc);

        ButtonGroup pathNodeGroup = new ButtonGroup();

        curvePathRegular = makeRadioButton("panel_slider_radio_regular", RADIOBUTTON_PATHTYPE_REGULAR,"panel_slider_radio_regular_tooltip", Color.ORANGE,true, false, curveRadioPanel, pathNodeGroup, false, curvePanelListener);
        curvePathSubPrio = makeRadioButton("panel_slider_radio_subprio", RADIOBUTTON_PATHTYPE_SUBPRIO,"panel_slider_radio_subprio_tooltip", Color.ORANGE,false, false,curveRadioPanel, pathNodeGroup, false, curvePanelListener);
        curvePathReverse = makeRadioButton("panel_slider_radio_reverse", RADIOBUTTON_PATHTYPE_REVERSE,"panel_slider_radio_reverse_tooltip", Color.ORANGE,false, false,curveRadioPanel, null, false, curvePanelListener);
        curvePathDual = makeRadioButton("panel_slider_radio_dual", RADIOBUTTON_PATHTYPE_DUAL,"panel_slider_radio_dual_tooltip", Color.ORANGE,false, false,curveRadioPanel, null, false, curvePanelListener);

        curveOptionsPanel.add(curveRadioPanel);
        makeCompactGrid(curveRadioPanel, 2, 4, 0, 5, 5, 5);

        // create panel for slider using vertical layout

        JPanel interpolationSliderPanel = new JPanel();
        interpolationSliderPanel.setLayout(new BoxLayout(interpolationSliderPanel, Y_AXIS));
        interpolationSliderPanel.setBorder(BorderFactory.createEmptyBorder());
        interpolationSliderPanel.setOpaque(false);

        // add padding before the label to centre it

        interpolationSliderPanel.add(Box.createRigidArea(new Dimension(72, 5)));
        JLabel textLabel = new JLabel(getLocaleString("panel_slider_label"));
        textLabel.setForeground(Color.ORANGE);
        interpolationSliderPanel.add(textLabel);

        numIterationsSlider = new JSlider(JSlider.HORIZONTAL,0, curveSliderMax, curveSliderDefault);
        numIterationsSlider.setVisible(true);
        numIterationsSlider.setOpaque(false);
        numIterationsSlider.setForeground(Color.ORANGE);
        numIterationsSlider.setMajorTickSpacing(10);
        numIterationsSlider.setPaintTicks(true);
        numIterationsSlider.setPaintLabels(true);
        numIterationsSlider.addChangeListener(curvePanelListener);
        interpolationSliderPanel.add(numIterationsSlider);
        curveOptionsPanel.add(interpolationSliderPanel);

        curveOptionsPanel.add(Box.createRigidArea(new Dimension(8, 0)));
        commitCurve = makeImageButton("curvepanel/confirm","curvepanel/confirm_select", BUTTON_COMMIT_CURVE,"panel_slider_confirm_curve","panel_slider_confirm_curve_alt", curveOptionsPanel, true, curvePanelListener);
        curveOptionsPanel.add(Box.createRigidArea(new Dimension(8, 0)));
        cancelCurve = makeImageButton("curvepanel/cancel","curvepanel/cancel_select", BUTTON_CANCEL_CURVE,"panel_slider_cancel_curve","panel_slider_cancel_curve_alt", curveOptionsPanel, true, curvePanelListener);
        curveOptionsPanel.add(Box.createRigidArea(new Dimension(8, 0)));

        return curveOptionsPanel;
    }

    public static JPanel initTextPanel() {

        // Set up the text panel to show info to user

        textPanel = new JPanel(new BorderLayout());
        textArea = new JTextArea("Welcome to the AutoDrive Editor... Load a config to start editing..\n ",3,0);
        JScrollPane scrollPane = new JScrollPane(textArea);
        scrollPane.setVerticalScrollBarPolicy(VERTICAL_SCROLLBAR_NEVER);
        textArea.setEditable(false);
        Font textAreaFont = textArea.getFont();
        textArea.setFont(new Font(textArea.getFont().toString(), Font.PLAIN, textAreaFont.getSize() - 1));
        textPanel.add(scrollPane, BorderLayout.CENTER);

        // Set up the info panel to show if map images/heightmaps are loaded

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
}
