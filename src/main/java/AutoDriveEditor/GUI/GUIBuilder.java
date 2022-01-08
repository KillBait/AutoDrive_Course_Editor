package AutoDriveEditor.GUI;

import javax.swing.*;
import javax.swing.border.BevelBorder;
import java.awt.*;

import AutoDriveEditor.AutoDriveEditor;
import AutoDriveEditor.Listeners.CurvePanelListener;
import AutoDriveEditor.Listeners.EditorListener;
import AutoDriveEditor.MapPanel.MapPanel;

import static AutoDriveEditor.GUI.GUIUtils.*;
import static AutoDriveEditor.Locale.LocaleManager.*;
import static AutoDriveEditor.MapPanel.MapPanel.*;
import static AutoDriveEditor.XMLConfig.EditorXML.*;
import static AutoDriveEditor.XMLConfig.GameXML.*;
import static javax.swing.BoxLayout.X_AXIS;
import static javax.swing.BoxLayout.Y_AXIS;


public class GUIBuilder {

    public static final int EDITORSTATE_NOOP = -1;
    public static final int EDITORSTATE_MOVING = 0;
    public static final int EDITORSTATE_CONNECTING = 1;
    public static final int EDITORSTATE_CREATE_PRIMARY_NODE = 2;
    public static final int EDITORSTATE_CHANGE_NODE_PRIORITY = 3;
    public static final int EDITORSTATE_CREATE_SUBPRIO_NODE = 4;
    public static final int EDITORSTATE_DELETE_NODES = 5;
    public static final int EDITORSTATE_CREATING_DESTINATION = 6;
    public static final int EDITORSTATE_EDITING_DESTINATION = 7;
    public static final int EDITORSTATE_DELETING_DESTINATION = 8;
    public static final int EDITORSTATE_ALIGN_HORIZONTAL = 9;
    public static final int EDITORSTATE_ALIGN_VERTICAL = 10;
    public static final int EDITORSTATE_ALIGN_DEPTH = 11;
    public static final int EDITORSTATE_ALIGN_EDIT_NODE = 12;
    public static final int EDITORSTATE_CNP_SELECT = 13;
    public static final int EDITORSTATE_QUADRATICBEZIER = 14;
    public static final int EDITORSTATE_CUBICBEZIER = 15;

    public static int editorState = GUIBuilder.EDITORSTATE_NOOP;

    public static final String BUTTON_MOVE_NODES = "Move Nodes";
    public static final String BUTTON_CONNECT_NODES = "Connect Nodes";
    public static final String BUTTON_CREATE_PRIMARY_NODE = "Create Primary Node";
    public static final String BUTTON_CHANGE_NODE_PRIORITY = "Change Priority";
    public static final String BUTTON_CREATE_SUBPRIO_NODE = "Create Secondary Node";
    public static final String BUTTON_CREATE_REVERSE_CONNECTION = "Create Reverse Connection";
    public static final String BUTTON_CREATE_DUAL_CONNECTION = "Create Dual Connection";
    public static final String BUTTON_REMOVE_NODES = "Remove Nodes";
    public static final String BUTTON_CREATE_DESTINATIONS = "Create Destinations";
    public static final String BUTTON_EDIT_DESTINATIONS_GROUPS = "Manage Destination Groups";
    public static final String BUTTON_DELETE_DESTINATIONS = "Remove Destinations";
    public static final String BUTTON_COPYPASTE_SELECT = "CopyPaste Select";
    public static final String BUTTON_COPYPASTE_CUT = "CopyPaste Cut";
    public static final String BUTTON_COPYPASTE_COPY = "CopyPaste Copy";
    public static final String BUTTON_COPYPASTE_PASTE = "CopyPaste Paste";
    public static final String BUTTON_COPYPASTE_PASTE_ORIGINAL = "CopyPaste Paste Original Location";

    // OCD modes

    public static final String BUTTON_ALIGN_HORIZONTAL = "Horizontally Align Nodes";
    public static final String BUTTON_ALIGN_VERTICAL = "Vertically Align Nodes";
    public static final String BUTTON_ALIGN_DEPTH = "Depth Align Nodes";
    public static final String BUTTON_ALIGN_EDIT_NODE = "Edit Node Location";
    public static final String BUTTON_CREATE_QUADRATICBEZIER = "Quadratic Bezier";
    public static final String BUTTON_CREATE_CUBICBEZIER = "Cubic Bezier";
    public static final String BUTTON_COMMIT_CURVE = "Confirm Curve";
    public static final String BUTTON_CANCEL_CURVE = "Cancel Curve";
    public static final String RADIOBUTTON_PATHTYPE_REGULAR = "Regular";
    public static final String RADIOBUTTON_PATHTYPE_SUBPRIO = "SubPrio";
    public static final String RADIOBUTTON_PATHTYPE_REVERSE = "Reverse";
    public static final String RADIOBUTTON_PATHTYPE_DUAL = "Dual";

    public static MapPanel mapPanel;
    public static CurvePanelListener curvePanelListener;

    public static JPanel nodeBox;
    public static JToggleButton removeNode;
    public static JToggleButton removeDestination;
    public static JToggleButton moveNode;
    public static JToggleButton createRegularConnection;
    public static JToggleButton createPrimaryNode;
    public static JToggleButton createDestination;
    public static JToggleButton changePriority;
    public static JToggleButton createSecondaryNode;
    public static JToggleButton createReverseConnection;
    public static JToggleButton createDualConnection;
    public static JToggleButton editDestination;
    public static JToggleButton alignHorizontal;
    public static JToggleButton alignVertical;
    public static JToggleButton alignDepth;
    public static JToggleButton editNode;
    public static JToggleButton quadBezier;
    public static JToggleButton cubicBezier;
    public static JToggleButton commitCurve;
    public static JToggleButton cancelCurve;
    public static JToggleButton select;
    public static JToggleButton cut;
    public static JToggleButton copy;
    public static JToggleButton paste;

    public static JSlider numIterationsSlider;
    public static JPanel curvePanel;
    public static JTextArea textArea;
    public static JRadioButton curvePathRegular;
    public static JRadioButton curvePathSubPrio;
    public static JRadioButton curvePathReverse;
    public static JRadioButton curvePathDual;

    public static MapPanel createMapPanel(AutoDriveEditor editor, EditorListener listener) {

        mapPanel = new MapPanel();
        mapPanel.setBorder(BorderFactory.createCompoundBorder(BorderFactory.createLoweredBevelBorder(), BorderFactory.createRaisedBevelBorder()));
        mapPanel.add( new AlphaContainer(initCurvePanel(listener)));

        //JRotation rot = new JRotation();
        //mapPanel.add(rot);

        return mapPanel;

    }

    public static JPanel createButtonPanel(EditorListener editorListener) {

        JPanel buttonPanel = new JPanel(new FlowLayout(FlowLayout.CENTER));
        //JToolBar buttonPanel = new JToolBar(BorderLayout.PAGE_START);

        //
        // Create node panel
        //

        nodeBox = new JPanel();
        nodeBox.setBorder(BorderFactory.createTitledBorder(localeString.getString("panel_nodes")));
        buttonPanel.add(nodeBox);

        moveNode = makeImageToggleButton("buttons/movenode", "buttons/movenode_selected", BUTTON_MOVE_NODES,"nodes_move_tooltip","nodes_move_alt", nodeBox, null, false, editorListener);
        createRegularConnection = makeStateChangeImageToggleButton("buttons/connectregular", "buttons/connectregular_selected", BUTTON_CONNECT_NODES,"nodes_connect_tooltip","nodes_connect_alt", nodeBox, null, false, editorListener);
        createPrimaryNode = makeImageToggleButton("buttons/createprimary","buttons/createprimary_selected", BUTTON_CREATE_PRIMARY_NODE,"nodes_createprimary_tooltip","nodes_createprimary_alt", nodeBox, null, false, editorListener);
        createDualConnection = makeStateChangeImageToggleButton("buttons/connectdual","buttons/connectdual_selected", BUTTON_CREATE_DUAL_CONNECTION,"nodes_createdual_tooltip","nodes_createdual_alt", nodeBox, null, false, editorListener);
        changePriority = makeImageToggleButton("buttons/swappriority","buttons/swappriority_selected", BUTTON_CHANGE_NODE_PRIORITY,"nodes_priority_tooltip","nodes_priority_alt", nodeBox, null, false, editorListener);
        createSecondaryNode = makeImageToggleButton("buttons/createsecondary","buttons/createsecondary_selected", BUTTON_CREATE_SUBPRIO_NODE,"nodes_createsecondary_tooltip","nodes_createsecondary_alt", nodeBox, null, false, editorListener);
        createReverseConnection = makeStateChangeImageToggleButton("buttons/connectreverse","buttons/connectreverse_selected", BUTTON_CREATE_REVERSE_CONNECTION,"nodes_createreverse_tooltip","nodes_createreverse_alt", nodeBox, null, false, editorListener);

        nodeBox.add(Box.createRigidArea(new Dimension(8, 0)));
        quadBezier = makeImageToggleButton("buttons/quadcurve","buttons/quadcurve_selected", BUTTON_CREATE_QUADRATICBEZIER,"helper_quadbezier_tooltip","helper_quadbezier_alt", nodeBox, null, false, editorListener);
        cubicBezier = makeImageToggleButton("buttons/cubiccurve","buttons/cubiccurve_selected", BUTTON_CREATE_CUBICBEZIER,"helper_cubicbezier_tooltip","helper_cubicbezier_alt", nodeBox, null,  false, editorListener);
        nodeBox.add(Box.createRigidArea(new Dimension(8, 0)));
        removeNode = makeImageToggleButton("buttons/deletenodes","buttons/deletenodes_selected", BUTTON_REMOVE_NODES,"nodes_remove_tooltip","nodes_remove_alt", nodeBox, null, false, editorListener);

        //
        // Create markers panel
        //

        JPanel markerBox = new JPanel();
        markerBox.setBorder(BorderFactory.createTitledBorder(localeString.getString("panel_markers")));
        buttonPanel.add(markerBox);

        createDestination = makeImageToggleButton("buttons/addmarker","buttons/addmarker_selected", BUTTON_CREATE_DESTINATIONS,"markers_add_tooltip","markers_add_alt", markerBox, null, false, editorListener);
        editDestination = makeImageToggleButton("buttons/editmarker","buttons/editmarker_selected", BUTTON_EDIT_DESTINATIONS_GROUPS,"markers_edit_tooltip","markers_edit_alt", markerBox, null, false, editorListener);
        markerBox.add(Box.createRigidArea(new Dimension(8, 0)));
        removeDestination = makeImageToggleButton("buttons/deletemarker","buttons/deletemarker_selected", BUTTON_DELETE_DESTINATIONS,"markers_delete_tooltip","markers_delete_alt", markerBox, null, false, editorListener);

        //
        // Create alignment panel
        //

        JPanel alignBox = new JPanel();
        alignBox.setBorder(BorderFactory.createTitledBorder(localeString.getString("panel_align")));
        buttonPanel.add(alignBox);

        alignHorizontal = makeImageToggleButton("buttons/horizontalalign","buttons/horizontalalign_selected", BUTTON_ALIGN_HORIZONTAL,"align_horizontal_tooltip","align_horizontal_alt", alignBox, null, false, editorListener);
        alignVertical = makeImageToggleButton("buttons/verticalalign","buttons/verticalalign_selected", BUTTON_ALIGN_VERTICAL,"align_vertical_tooltip","align_vertical_alt", alignBox, null, false, editorListener);
        alignDepth = makeImageToggleButton("buttons/depthalign","buttons/depthalign_selected", BUTTON_ALIGN_DEPTH,"align_depth_tooltip","align_depth_alt", alignBox, null, false, editorListener);
        alignBox.add(Box.createRigidArea(new Dimension(16, 0)));
        editNode = makeImageToggleButton("buttons/editlocation","buttons/editlocation_selected", BUTTON_ALIGN_EDIT_NODE,"align_node_edit_tooltip","align_node_edit_alt", alignBox, null, false, editorListener);

        //
        // copy/paste panel
        //

        JPanel copyBox = new JPanel();
        copyBox.setBorder(BorderFactory.createTitledBorder(localeString.getString("panel_copypaste")));
        copyBox.setVisible(true);
        buttonPanel.add(copyBox);

        select = makeImageToggleButton("buttons/select","buttons/select_selected", BUTTON_COPYPASTE_SELECT, "copypaste_select_tooltip","copypaste_select_alt", copyBox, null, false, editorListener);
        cut = makeImageToggleButton("buttons/cut","buttons/cut_selected", BUTTON_COPYPASTE_CUT, "copypaste_cut_tooltip","copypaste_cut_alt", copyBox, null, false, editorListener);
        copy = makeImageToggleButton("buttons/copy","buttons/copy_selected", BUTTON_COPYPASTE_COPY, "copypaste_copy_tooltip","copypaste_copy_alt", copyBox, null, false, editorListener);
        paste = makeImageToggleButton("buttons/paste","buttons/paste_selected", BUTTON_COPYPASTE_PASTE, "copypaste_paste_tooltip","copypaste_paste_alt", copyBox, null, false, editorListener);

        //
        // create experimental panel
        //

        JPanel testBox = new JPanel();
        testBox.setBorder(BorderFactory.createTitledBorder(localeString.getString("panel_helper")));
        testBox.setVisible(false);
        buttonPanel.add(testBox);


        testBox.add(Box.createRigidArea(new Dimension(48, 0)));

        return buttonPanel;
   }

    public static JPanel initCurvePanel(EditorListener editorListener) {

        //
        // curve panel (hidden by default)
        //

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

        numIterationsSlider = new JSlider(JSlider.HORIZONTAL,0, quadSliderMax, quadSliderDefault);
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
        commitCurve = makeImageToggleButton("curvepanel/confirm","curvepanel/confirm_select", BUTTON_COMMIT_CURVE,"panel_slider_confirm_curve","panel_slider_confirm_curve_alt", curvePanel, null, false, editorListener);
        curvePanel.add(Box.createRigidArea(new Dimension(8, 0)));
        cancelCurve = makeImageToggleButton("curvepanel/cancel","curvepanel/cancel_select", BUTTON_CANCEL_CURVE,"panel_slider_cancel_curve","panel_slider_cancel_curve_alt", curvePanel, null, false, editorListener);
        curvePanel.add(Box.createRigidArea(new Dimension(8, 0)));



        return curvePanel;
    }

    public static JPanel initTextPanel() {
        JPanel textPanel = new JPanel(new BorderLayout());
        textArea = new JTextArea("Welcome to the AutoDrive Editor... Load a config to start editing..\n ",3,0);
        JScrollPane scrollPane = new JScrollPane(textArea);
        textArea.setEditable(false);
        textPanel.add(scrollPane, BorderLayout.CENTER);
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

        switch (editorState) {
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
            case EDITORSTATE_CREATING_DESTINATION:
                createDestination.setSelected(true);
                showInTextArea("click on a node to create a map marker", true, false);
                break;
            case EDITORSTATE_EDITING_DESTINATION:
                editDestination.setSelected(true);
                showInTextArea("click on a marker to edit", true, false);
                break;
            case EDITORSTATE_DELETING_DESTINATION:
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

    //@SuppressWarnings("serial")
    /*static class JRotation extends JPanel implements MouseMotionListener {

        private double rotation = 0;
        private double angle = 0;
        private double lastAngle = 0;
        private double lastDegree = 0;
        private int lastrot = 0;
        public double getRotation() {
            return rotation;
        }

        public JRotation() {
            setPreferredSize(new Dimension(100, 100));
            addMouseMotionListener(this);
        }

        public static Point2D rotate(Graphics g, Point2D point, Point2D centre, double angle) {
            int width = getMapPanel().getWidth();
            int height = getMapPanel().getHeight();

            int sizeScaled = (int) (nodeSize * zoomLevel);
            int sizeScaledHalf = (int) (sizeScaled * 0.5);
            double currentNodeSize = nodeSize * zoomLevel * 0.5;
            Point2D result = new Point2D.Double();
            AffineTransform rotation = new AffineTransform();
            //angle = ADUtils.normalizeAngle(angle);
            double angleInRadians = Math.toRadians(angle);
            rotation.rotate(angle, centre.getX(), centre.getY());
            rotation.transform(new Point2D.Double(point.getX(), point.getY()), result);
            g.drawImage(nodeImage, (int) (result.getX() - (nodeImage.getWidth() / 4)), (int) (result.getY() - (nodeImage.getWidth() / 4)), nodeImage.getWidth() / 2, nodeImage.getHeight() / 2, null);
            //  g.drawImage(nodeImage,(int) (point.getX() - (getMapPanel().sizeScaledHalf / 2 )), (int) (point.getY() - (sizeScaledHalf / 2 )), sizeScaledHalf, sizeScaledHalf, null);
            return result;

        }

        @Override
        protected void paintComponent(Graphics g) {
            Graphics2D g2 = (Graphics2D)g.create();
            g2.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);
            //g2.setPaint(Color.white);
            //g2.fillRect(0, 0, getWidth(), getHeight());
            g2.drawImage(rotateRing, 0, 0, rotateRing.getWidth(), rotateRing.getHeight(), null);
            rotate(g2, new Point2D.Double(50, 7), new Point2D.Double(getPreferredSize().getWidth() / 2, getPreferredSize().getHeight() / 2), angle);
            //g2.rotate(-rotation);0

            //g2.setPaint(Color.black);
            //AffineTransform t = g2.getTransform();
            //g2.translate(getWidth()/2, getHeight()/2);
            //g2.rotate(Math.toDegrees(rotation));

            //g2.drawLine(0, 0, 0, -40);
            //g2.drawImage(nodeImage, -7, -50, nodeImage.getWidth() / 2, nodeImage.getHeight() / 2, null);
            //g2.setTransform(t);
        }

        @Override
        public void mouseDragged(MouseEvent e) {
            double step = 0;
            int x = e.getX();
            int y = e.getY();
            int midX = getWidth() / 2;
            int midY = getHeight() / 2;

            angle = Math.atan2(midY - y, midX - x) - PI / 2;
            if (angle < 0) // between -PI/2 and 0
                angle += 2*PI;

            step = Math.toDegrees(angle) - lastAngle;
            CopyPasteManager.rotateSelected(step);
            lastAngle = Math.toDegrees(angle);

            mapPanel.repaint();
        }

        @Override
        public void mouseMoved(MouseEvent e) {

        }

        public static float LerpDegrees(float start, float end, float amount)
        {
            float difference = Math.abs(end - start);
            if (difference > 180)
            {
                // We need to add on to one of the values.
                if (end > start)
                {
                    // We'll add it on to start...
                    start += 360;
                }
                else
                {
                    // Add it on to end.
                    end += 360;
                }
            }

            // Interpolate it.
            float value = (start + ((end - start) * amount));

            // Wrap it..
            float rangeZero = 360;

            if (value >= 0 && value <= 360)
                return value;

            return (value % rangeZero);
        }
    }*/
}
