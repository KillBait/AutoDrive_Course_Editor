package AutoDriveEditor.GUI;

import AutoDriveEditor.GUI.Buttons.Alignment.DepthAlignButton;
import AutoDriveEditor.GUI.Buttons.Alignment.EditLocationButton;
import AutoDriveEditor.GUI.Buttons.Alignment.HorizontalAlignButton;
import AutoDriveEditor.GUI.Buttons.Alignment.VerticalAlignButton;
import AutoDriveEditor.GUI.Buttons.Curves.CubicCurveButton;
import AutoDriveEditor.GUI.Buttons.Curves.QuadCurveButton;
import AutoDriveEditor.GUI.Buttons.Display.*;
import AutoDriveEditor.GUI.Buttons.Edit.CopySelectionButton;
import AutoDriveEditor.GUI.Buttons.Edit.CutSelectionButton;
import AutoDriveEditor.GUI.Buttons.Edit.MultiSelectButton;
import AutoDriveEditor.GUI.Buttons.Edit.PasteSelectionButton;
import AutoDriveEditor.GUI.Buttons.Markers.AddMarkerButton;
import AutoDriveEditor.GUI.Buttons.Markers.DeleteMarkerButton;
import AutoDriveEditor.GUI.Buttons.Markers.EditMarkerButton;
import AutoDriveEditor.GUI.Buttons.Nodes.*;
import AutoDriveEditor.GUI.Buttons.Options.*;
import AutoDriveEditor.GUI.Buttons.Testing.RoadConnection;
import AutoDriveEditor.GUI.Buttons.Testing.ScaleButton;
import AutoDriveEditor.GUI.Buttons.Testing.TestButton;

import javax.swing.*;
import javax.swing.border.TitledBorder;
import javax.swing.event.AncestorEvent;
import javax.swing.event.AncestorListener;
import java.awt.*;

import static AutoDriveEditor.AutoDriveEditor.EXPERIMENTAL;
import static AutoDriveEditor.AutoDriveEditor.buttonManager;
import static AutoDriveEditor.GUI.Menus.DebugMenu.Logging.LogGUIInfoMenu.bDebugLogGUIInfo;
import static AutoDriveEditor.Locale.LocaleManager.getLocaleString;
import static AutoDriveEditor.Utils.LoggerUtils.LOG;
import static AutoDriveEditor.XMLConfig.EditorXML.bLockToolbarPosition;
import static AutoDriveEditor.XMLConfig.EditorXML.toolbarPosition;
import static javax.swing.BoxLayout.Y_AXIS;

public class ButtonToolbar extends JToolBar {

    static final JPanel nodePanel = new JPanel();
    static final JPanel curvesPanel = new JPanel();
    static final JPanel markerPanel = new JPanel();
    static final JPanel alignPanel = new JPanel();
    static final JPanel editPanel = new JPanel();
    static final JPanel optionsPanel = new JPanel();
    static final JPanel experimentalPanel = new JPanel();
    static final JPanel displayPanel = new JPanel();

    public ButtonToolbar(BorderLayout mainLayout, String layoutPosition) {

        if (bLockToolbarPosition) {
            setFloatable(false);
            setBorder(BorderFactory.createCompoundBorder(BorderFactory.createLoweredBevelBorder(), BorderFactory.createRaisedBevelBorder()));
        }

        // Set the initial toolbar rotation

        boolean isToolbarHorizontal = layoutPosition.equals(BorderLayout.PAGE_START);
        if (isToolbarHorizontal) {
            setOrientation(SwingConstants.HORIZONTAL);
            setLayout(new FlowLayout());
        } else {
            setOrientation(SwingConstants.VERTICAL);
            setLayout(new BoxLayout(this, Y_AXIS));
        }

        addAncestorListener(new AncestorListener() {
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

        addPropertyChangeListener(evt -> {
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

        buttonManager.addButton(new MoveNodeButton(nodePanel));
        buttonManager.addButton(new NormalConnectionButton(nodePanel));
        buttonManager.addButton(new AddPrimaryNodeButton(nodePanel));
        buttonManager.addButton(new DualConnectionButton(nodePanel));
        buttonManager.addButton(new AddSecondaryNodeButton(nodePanel));
        buttonManager.addButton(new ReverseConnectionButton(nodePanel));
        buttonManager.addButton(new SwapNodePriorityButton(nodePanel));
        buttonManager.addButton(new FlipDirectionButton(nodePanel));
        buttonManager.addButton(new RotationButton(nodePanel));
        buttonManager.addButton(new DeleteNodeButton(nodePanel));

        add(nodePanel);
        add(Box.createRigidArea(new Dimension(10, 10)));

        //
        // Create Curve panel
        //

        TitledBorder curvesBorder = BorderFactory.createTitledBorder(getLocaleString("panel_curves"));
        curvesBorder.setTitleJustification(TitledBorder.CENTER);
        curvesPanel.setBorder(curvesBorder);

        buttonManager.addButton(new QuadCurveButton(curvesPanel));
        buttonManager.addButton(new CubicCurveButton(curvesPanel));

        add(curvesPanel);
        add(Box.createRigidArea(new Dimension(10, 10)));

        //
        // Create markers panel
        //

        TitledBorder markerBorder = BorderFactory.createTitledBorder(getLocaleString("panel_markers"));
        markerBorder.setTitleJustification(TitledBorder.CENTER);
        markerPanel.setBorder(markerBorder);

        buttonManager.addButton(new AddMarkerButton(markerPanel));
        buttonManager.addButton(new EditMarkerButton(markerPanel));
        markerPanel.add(Box.createRigidArea(new Dimension(10, 1)));
        buttonManager.addButton(new DeleteMarkerButton(markerPanel));

        add(markerPanel);
        add(Box.createRigidArea(new Dimension(10, 10)));

        //
        // Create alignment panel
        //

        TitledBorder alignmentBorder = BorderFactory.createTitledBorder(getLocaleString("panel_align"));
        alignmentBorder.setTitleJustification(TitledBorder.CENTER);
        alignPanel.setBorder(alignmentBorder);

        buttonManager.addButton(new HorizontalAlignButton(alignPanel));
        buttonManager.addButton(new VerticalAlignButton(alignPanel));
        buttonManager.addButton(new DepthAlignButton(alignPanel));
        buttonManager.addButton(new EditLocationButton(alignPanel));

        add(alignPanel);
        add(Box.createRigidArea(new Dimension(10, 10)));

        //
        // copy/paste panel
        //

        TitledBorder copyBorder = BorderFactory.createTitledBorder(getLocaleString("panel_copypaste"));
        copyBorder.setTitleJustification(TitledBorder.CENTER);
        editPanel.setBorder(copyBorder);

        buttonManager.addButton(new MultiSelectButton(editPanel));
        buttonManager.addButton(new CutSelectionButton(editPanel));
        buttonManager.addButton(new CopySelectionButton(editPanel));
        buttonManager.addButton(new PasteSelectionButton(editPanel));



        add(editPanel);
        add(Box.createRigidArea(new Dimension(10, 10)));

        //
        // Create display panel
        //

        TitledBorder displayBorder = BorderFactory.createTitledBorder(getLocaleString("panel_display"));
        displayBorder.setTitleJustification(TitledBorder.CENTER);
        displayPanel.setBorder(displayBorder);

        buttonManager.addButton(new HideNodeButton(displayPanel));
        buttonManager.addButton(new HideRegularConnectionsButton(displayPanel));
        buttonManager.addButton(new HideSubprioConnectionsButton(displayPanel));
        buttonManager.addButton(new HideDualConnectionsButton(displayPanel));
        buttonManager.addButton(new HideReverseConnectionsButton(displayPanel));

        add(displayPanel);
        add(Box.createRigidArea(new Dimension(10, 10)));


        //
        // create options panel
        //

        TitledBorder optionsBorder = BorderFactory.createTitledBorder(getLocaleString("panel_options"));
        optionsBorder.setTitleJustification(TitledBorder.CENTER);
        optionsPanel.setBorder(optionsBorder);

        buttonManager.addButton(new OpenConfigButton(optionsPanel));
        buttonManager.addButton(new ContinuousConnectionButton(optionsPanel));
        buttonManager.addButton(new NodeSizeUpButton(optionsPanel));
        buttonManager.addButton(new NodeSizeDownButton(optionsPanel));
        buttonManager.addButton(new GridDisplayButton(optionsPanel));
        buttonManager.addButton(new GridSnapButton(optionsPanel));
        buttonManager.addButton(new SelectHiddenButton(optionsPanel));

        add(optionsPanel);
        add(Box.createRigidArea(new Dimension(10, 10)));

        //
        // create experimental panel
        //

        if (EXPERIMENTAL) {
            TitledBorder experimentalBorder = BorderFactory.createTitledBorder(getLocaleString("nodes_test_alt"));
            experimentalBorder.setTitleJustification(TitledBorder.CENTER);
            experimentalPanel.setBorder(experimentalBorder);

            buttonManager.addButton(new TestButton(experimentalPanel));
            buttonManager.addButton(new RoadConnection(experimentalPanel));
            buttonManager.addButton(new ScaleButton(experimentalPanel));
            //networkInfo = makeImageButton("buttons/networkinfo", "buttons/networkinfo", BUTTON_OPTIONS_NETWORK_INFO,"options_info_network_info_tooltip","options_info_network_info_alt", optionsPanel, editorListener);

            add(experimentalPanel);

        }

        if (isToolbarHorizontal) {
            switchToolbarLayoutToHorizontal();
        } else {
            switchToolbarLayoutToVertical();
        }
    }

    private void switchToolbarLayoutToHorizontal() {
        setLayout(new FlowLayout());
        nodePanel.setLayout(new FlowLayout());
        curvesPanel.setLayout(new FlowLayout());
        markerPanel.setLayout(new FlowLayout());
        alignPanel.setLayout(new FlowLayout());
        editPanel.setLayout(new FlowLayout());
        displayPanel.setLayout(new FlowLayout());
        optionsPanel.setLayout(new FlowLayout());
        if (EXPERIMENTAL) {
            experimentalPanel.setLayout(new FlowLayout());
        }
    }

    private void switchToolbarLayoutToVertical() {

        setLayout(new BoxLayout(this, Y_AXIS));

        nodePanel.setLayout(new GridLayout(5,2,8,8));
        nodePanel.setMaximumSize(new Dimension(80, (int)nodePanel.getPreferredSize().getHeight()));
        curvesPanel.setLayout(new GridLayout(1,2,8,8));
        curvesPanel.setMaximumSize(new Dimension(80, (int)curvesPanel.getPreferredSize().getHeight()));
        markerPanel.setLayout(new GridLayout(2,2,8,8));
        markerPanel.setMaximumSize(new Dimension(80, (int)markerPanel.getPreferredSize().getHeight()));
        alignPanel.setLayout(new GridLayout(2,2,8,8));
        alignPanel.setMaximumSize(new Dimension(80, (int)alignPanel.getPreferredSize().getHeight()));
        editPanel.setLayout(new GridLayout(2,2,8,8));
        editPanel.setMaximumSize(new Dimension(80, (int) editPanel.getPreferredSize().getHeight()));
        displayPanel.setLayout(new GridLayout(3,2,8,8));
        displayPanel.setMaximumSize(new Dimension(80, (int) displayPanel.getPreferredSize().getHeight()));
        optionsPanel.setLayout(new GridLayout(4,2,8,8));
        optionsPanel.setMaximumSize(new Dimension(80, (int) optionsPanel.getPreferredSize().getHeight()));
        if (EXPERIMENTAL) {
            experimentalPanel.setLayout(new GridLayout(2,2,8,8));
            experimentalPanel.setMaximumSize(new Dimension(80, (int) experimentalPanel.getPreferredSize().getHeight()));
        }
    }

}
