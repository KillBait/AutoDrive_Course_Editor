package AutoDriveEditor.GUI.Buttons.Toolbar.Nodes;

import AutoDriveEditor.Classes.Interfaces.WidgetInterface;
import AutoDriveEditor.Classes.KeyBinds.Shortcut;
import AutoDriveEditor.Classes.UI_Components.ScaleAnimIcon;
import AutoDriveEditor.Classes.Util_Classes.RotationUtils;
import AutoDriveEditor.Classes.Widgets.MoveWidget;
import AutoDriveEditor.GUI.Buttons.BaseButton;
import AutoDriveEditor.Managers.ChangeManager;
import AutoDriveEditor.Managers.ShortcutManager;
import AutoDriveEditor.RoadNetwork.MapNode;

import javax.swing.*;
import java.awt.*;
import java.awt.event.ActionEvent;
import java.awt.event.MouseEvent;
import java.awt.event.MouseWheelEvent;
import java.awt.geom.Point2D;
import java.util.ArrayList;

import static AutoDriveEditor.AutoDriveEditor.*;
import static AutoDriveEditor.Classes.Util_Classes.LoggerUtils.LOG;
import static AutoDriveEditor.GUI.MapPanel.*;
import static AutoDriveEditor.GUI.Menus.DebugMenu.Logging.LogUndoRedoMenu.bDebugLogUndoRedoInfo;
import static AutoDriveEditor.GUI.TextPanel.showInTextArea;
import static AutoDriveEditor.Locale.LocaleManager.getLocaleString;
import static AutoDriveEditor.Managers.IconManager.*;
import static AutoDriveEditor.Managers.MultiSelectManager.multiSelectList;
import static AutoDriveEditor.Managers.ShortcutManager.ShortcutID.ROTATE_NODES_SHORTCUT;
import static AutoDriveEditor.Managers.ShortcutManager.getUserShortcutByID;
import static AutoDriveEditor.Managers.ShortcutManager.registerShortcut;
import static AutoDriveEditor.XMLConfig.EditorXML.*;

public final class RotationButton extends BaseButton {

//    private WidgetInterface rotateWidget;

    @Override
    public String getButtonID() { return "RotateButton"; }

    @Override
    public String getButtonAction() { return "ActionButton"; }

    @Override
    public String getButtonPanel() { return "Edit"; }

    @Override
    public String getInfoText() { return getLocaleString("toolbar_nodes_rotate_infotext"); }

    @Override
    public Boolean useMultiSelection() { return true; }

//    @Override
//    public void onButtonSelect() {
//        if (rotateWidget == null) rotateWidget = widgetManager.addWidgetClass(MoveWidget.class, this.getButtonID());
//        rotateWidget.setWidgetEnabled(true);
//    }

    public static RotationUtils rotation;
    boolean isControlNodeSelected = false;
    public int displayedRadius;
    private int totalAngle = 0;

    public RotationButton(JPanel panel) {
        ScaleAnimIcon animRotateNodesIcon = createScaleAnimIcon(BUTTON_ROTATE_NODE_ICON, false);
        button = createAnimToggleButton(animRotateNodesIcon, panel, null, null,  false, false, this);
        rotation = new RotationUtils();
        button.addMouseListener(this);

        // Setup Keyboard Shortcuts
        Shortcut moveShortcut = getUserShortcutByID(ROTATE_NODES_SHORTCUT);
        if (moveShortcut != null) {
            Action moveButtonAction = new AbstractAction() {
                @Override
                public void actionPerformed(ActionEvent e) {
                    if (button.isEnabled() && !button.isSelected()) {
                        buttonManager.makeCurrent(buttonNode);
                    } else {
                        buttonManager.deSelectAll();
                    }
                }
            };
            registerShortcut(this, moveShortcut, moveButtonAction, getMapPanel());
        }
    }



    @Override
    public void setSelected(boolean selected) {
        if (selected) {
            button.setSelected(true);
            showInTextArea(getInfoText(), true, false);
            if (!multiSelectList.isEmpty()) updateSelection();
            getMapPanel().repaint();
        } else {
            button.setSelected(false);
        }
    }

    @Override
    public void mousePressed(MouseEvent e) {
        super.mousePressed(e);
        if (e.getButton() == MouseEvent.BUTTON1) {
            /*if ( getMapPanel().hoveredNode == rotation.getControlNode())*/ isControlNodeSelected = true;
            totalAngle = 0;
        }
    }

    @Override
    public void mouseReleased(MouseEvent e) {
        super.mouseReleased(e);
        if (e.getButton() == MouseEvent.BUTTON3 && e.getSource() == getMapPanel()) {
            updateSelection();
            getMapPanel().repaint();
        }
        if (e.getButton() == MouseEvent.BUTTON1 && this.button.isEnabled()) {
            isControlNodeSelected = false;
            if (totalAngle > 0) changeManager.addChangeable(new RotateNodeChanger(multiSelectList, rotation.getCentrePointWorld(), totalAngle, isStale()));
        }
    }

    @Override
    public void mouseDragged(MouseEvent e) {
        super.mouseDragged(e);
        if (isControlNodeSelected) {
            totalAngle += (int) rotation.rotateControlNode(e.getX(), e.getY(), 0);
            getMapPanel().repaint();
        }
    }

    @Override
    public void mouseWheelMoved(MouseWheelEvent e) { getMapPanel().repaint(); }

    private void updateSelection() {
        if (rotation != null) {
            if (!multiSelectList.isEmpty()) {
                rotation.setCentrePoint(multiSelectList);
                rotation.setInitialControlNodePosition(multiSelectList);
                displayedRadius = rotation.getSelectionRadius(multiSelectList);
            }

        }
    }

    @Override
    public void drawToScreen(Graphics g) {
        if (!multiSelectList.isEmpty() && this.button.isSelected()) {

            Graphics2D gTemp = (Graphics2D) g.create();
            gTemp.setColor(Color.WHITE);

            Point2D controlNodePosition = worldPosToScreenPos(rotation.getControlNode().x, rotation.getControlNode().z);
            Point2D selectionCentre = rotation.getCentrePointScreen();

            gTemp.fillArc((int) (selectionCentre.getX() - 3), (int) (selectionCentre.getY() - 3), 6, 6, 0, 360);

            // Draw the circle around the selected nodes

            BasicStroke bsDash = new BasicStroke(1, BasicStroke.CAP_BUTT, BasicStroke.JOIN_ROUND, 1.0f, new float[]{4f, 0f, 2f}, 2f);
            Composite oldComposite = gTemp.getComposite();
            gTemp.setComposite(AlphaComposite.SrcOver.derive(0.5f));
            Stroke oldStroke = gTemp.getStroke();
            gTemp.setStroke(bsDash);

            int radius = (int) selectionCentre.distance(controlNodePosition.getX(), controlNodePosition.getY());
            gTemp.drawArc((int) (selectionCentre.getX() - radius), (int) (selectionCentre.getY() - radius), radius * 2, radius * 2, 0, 360);

            gTemp.setStroke(oldStroke);
            gTemp.setComposite(oldComposite);

            // Draw the rotation control Node

            Polygon p = new Polygon();

            //If the  pointer hovering over the control node, draw the selection border
            if (getMapPanel().hoveredNode == rotation.getControlNode()) {
                double diff = nodeSizeScaledHalf + ((nodeSizeScaled - nodeSizeScaledHalf) / 2);
                p.addPoint((int) controlNodePosition.getX(), (int) (controlNodePosition.getY() - diff));
                p.addPoint((int) (controlNodePosition.getX() + diff), (int) controlNodePosition.getY());
                p.addPoint((int) controlNodePosition.getX(), (int) (controlNodePosition.getY() + diff));
                p.addPoint((int) (controlNodePosition.getX() - diff), (int) controlNodePosition.getY());
                gTemp.setColor(colourNodeSelected);
                gTemp.fillPolygon(p);
            }

            // Draw the control node

            p.reset();
            p.addPoint((int) controlNodePosition.getX(), (int) (controlNodePosition.getY() - nodeSizeScaledHalf));
            p.addPoint((int) (controlNodePosition.getX() + nodeSizeScaledHalf), (int) controlNodePosition.getY());
            p.addPoint((int) controlNodePosition.getX(), (int) (controlNodePosition.getY() + nodeSizeScaledHalf));
            p.addPoint((int) (controlNodePosition.getX() - nodeSizeScaledHalf), (int) controlNodePosition.getY());
            gTemp.setColor(colourNodeControl);
            gTemp.fillPolygon(p);

            gTemp.dispose();
        }
    }

    @Override
    public String buildToolTip() {
        Shortcut s = ShortcutManager.getUserShortcutByID(ROTATE_NODES_SHORTCUT);
        if (s != null) {
            return getLocaleString("toolbar_nodes_rotate_tooltip") + "\n\n( Shortcut: " + s.getShortcutString() + " )";
        } else {
            return getLocaleString("toolbar_nodes_rotate_tooltip");
        }
    }

    public static class RotateNodeChanger implements ChangeManager.Changeable {

        private final ArrayList<MapNode> storedRotateNodeList;
        private final Point2D centrePointWorld;
        private final int angle;
        private final boolean isStale;

        public RotateNodeChanger(ArrayList<MapNode> rotateNodes, Point2D centrePointWorld, int angle, boolean isStale){
            super();
            this.storedRotateNodeList = new ArrayList<>();
            this.centrePointWorld = centrePointWorld;
            this.angle = angle;
            this.isStale = isStale;

            for (int i = 0; i <= rotateNodes.size() -1 ; i++) {
                MapNode node = rotateNodes.get(i);
                if (bDebugLogUndoRedoInfo) LOG.info("## RotateNodeChanger ## Adding ID {} to storedCurveNodeList", node.id);
                this.storedRotateNodeList.add(node);
            }
        }

        public void undo(){
            //rotation.setCentrePointWorld(this.centrePointWorld);
            rotation.rotateChanger(this.storedRotateNodeList, this.centrePointWorld, -this.angle);
            getMapPanel().repaint();
            setStale(this.isStale);
        }

        public void redo(){
            rotation.setCentrePointWorld(this.centrePointWorld);
            rotation.rotateChanger(this.storedRotateNodeList, this.centrePointWorld, this.angle);
            getMapPanel().repaint();
            setStale(true);
        }
    }
}
