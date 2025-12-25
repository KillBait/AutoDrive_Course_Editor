package AutoDriveEditor.GUI;

import AutoDriveEditor.Classes.UI_Components.ScaleAnimIcon;
import AutoDriveEditor.Classes.UI_Components.ToolbarButton;
import AutoDriveEditor.GUI.Buttons.Toolbar.Alignment.DepthAlignButton;
import AutoDriveEditor.GUI.Buttons.Toolbar.Alignment.EditLocationButton;
import AutoDriveEditor.GUI.Buttons.Toolbar.Alignment.HorizontalAlignButton;
import AutoDriveEditor.GUI.Buttons.Toolbar.Alignment.VerticalAlignButton;
import AutoDriveEditor.GUI.Buttons.Toolbar.Experimental.ArcSplineButton;
import AutoDriveEditor.GUI.Buttons.Toolbar.Curves.BezierCurveButton;
import AutoDriveEditor.GUI.Buttons.Toolbar.Display.*;
import AutoDriveEditor.GUI.Buttons.Toolbar.Experimental.RoadConnection;
import AutoDriveEditor.GUI.Buttons.Toolbar.Experimental.ScaleButton;
import AutoDriveEditor.GUI.Buttons.Toolbar.Experimental.TestButton;
import AutoDriveEditor.GUI.Buttons.Toolbar.Markers.AddMarkerButton;
import AutoDriveEditor.GUI.Buttons.Toolbar.Markers.DeleteMarkerButton;
import AutoDriveEditor.GUI.Buttons.Toolbar.Markers.EditMarkerButton;
import AutoDriveEditor.GUI.Buttons.Toolbar.Nodes.*;
import net.miginfocom.swing.MigLayout;

import javax.swing.*;
import java.awt.*;

import static AutoDriveEditor.AutoDriveEditor.*;
import static AutoDriveEditor.Managers.IconManager.*;

public class Toolbar extends JPanel {

    public Toolbar() {

        // Constructor
        setLayout(new MigLayout("hidemode 3, wrap 1"));
        setOpaque(false);

        // Add the toolbar panel to the JLayeredPane
        JLayeredPane layeredPane = getMapPanel().getRootPane().getLayeredPane();
        layeredPane.add(this, JLayeredPane.POPUP_LAYER);

        //
        // Node panel
        //

        JPanel nodePanel = addButtonPanel("toolbar_nodes", NODE_PANEL_ICON);
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

        //
        // Curve panel
        //

        JPanel curvesPanel = addButtonPanel("toolbar_curves", CURVE_PANEL_ICON);
        buttonManager.addButton(new BezierCurveButton(curvesPanel));

        //
        // Marker panel
        //

        JPanel markerPanel = addButtonPanel("toolbar_markers", MARKER_PANEL_ICON);
        buttonManager.addButton(new AddMarkerButton(markerPanel));
        buttonManager.addButton(new EditMarkerButton(markerPanel));
        buttonManager.addButton(new DeleteMarkerButton(markerPanel));

        //
        // Alignment panel
        //

        JPanel alignPanel = addButtonPanel("toolbar_align", ALIGNMENT_PANEL_ICON);
        buttonManager.addButton(new HorizontalAlignButton(alignPanel));
        buttonManager.addButton(new VerticalAlignButton(alignPanel));
        buttonManager.addButton(new DepthAlignButton(alignPanel));
        buttonManager.addButton(new EditLocationButton(alignPanel));

        //
        // Node/Connection visibility panel
        //

        JPanel visibilityPanel = addButtonPanel("toolbar_display", VISIBILITY_PANEL_ICON);
        buttonManager.addButton(new HideNodeButton(visibilityPanel));
        buttonManager.addButton(new HideRegularConnectionsButton(visibilityPanel));
        buttonManager.addButton(new HideSubprioConnectionsButton(visibilityPanel));
        buttonManager.addButton(new HideDualConnectionsButton(visibilityPanel));
        buttonManager.addButton(new HideReverseConnectionsButton(visibilityPanel));

        //
        // Early testing, may not make it into release..
        //
        if (EXPERIMENTAL) {
            JPanel experimentalPanel = addButtonPanel("toolbar_experimental", EXPERIMENTAL_PANEL_ICON);
            buttonManager.addButton(new ArcSplineButton(experimentalPanel));
            buttonManager.addButton(new TestButton(experimentalPanel));
            buttonManager.addButton(new RoadConnection(experimentalPanel));
            buttonManager.addButton(new ScaleButton(experimentalPanel));
        }

        // Calculate toolbar location

        setSize(getPreferredSize());
        Point mapPanelLocation = SwingUtilities.convertPoint(getMapPanel(), getMapPanel().getLocation(), layeredPane);

        // Set the initial position of the toolbar panel on the left-hand side, centered vertically
        setLocation(mapPanelLocation);

    }

    private JPanel addButtonPanel(String title, String icon) {
        ScaleAnimIcon animIcon = createScaleAnimIcon(icon, false);
        ToolbarButton toggleButton = createAnimToolbarButton(animIcon, this, title, null, false, true, null);

        JPanel subPanel = new JPanel(new MigLayout("wrap 2"));
        subPanel.setVisible(false);
        subPanel.setOpaque(false);

        toggleButton.addActionListener(e -> {
            subPanel.setVisible(toggleButton.isSelected());
            setSize(getPreferredSize());
            repaint();
        });

        add(subPanel);
        return subPanel;
    }
}
