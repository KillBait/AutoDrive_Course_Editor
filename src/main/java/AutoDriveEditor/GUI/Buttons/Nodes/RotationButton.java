package AutoDriveEditor.GUI.Buttons.Nodes;

import AutoDriveEditor.Classes.Rotation;
import AutoDriveEditor.GUI.Buttons.BaseButton;
import AutoDriveEditor.Managers.ChangeManager;
import AutoDriveEditor.RoadNetwork.MapNode;
import AutoDriveEditor.Utils.Classes.LabelNumberFilter;

import javax.swing.*;
import javax.swing.text.PlainDocument;
import java.awt.*;
import java.awt.event.MouseEvent;
import java.awt.event.MouseWheelEvent;
import java.awt.geom.Point2D;
import java.util.LinkedList;

import static AutoDriveEditor.AutoDriveEditor.changeManager;
import static AutoDriveEditor.AutoDriveEditor.getMapPanel;
import static AutoDriveEditor.GUI.MapPanel.*;
import static AutoDriveEditor.GUI.Menus.DebugMenu.Logging.LogUndoRedoMenu.bDebugLogUndoRedo;
import static AutoDriveEditor.GUI.TextPanel.showInTextArea;
import static AutoDriveEditor.Locale.LocaleManager.getLocaleString;
import static AutoDriveEditor.Managers.MultiSelectManager.multiSelectList;
import static AutoDriveEditor.Utils.GUIUtils.makeImageToggleButton;
import static AutoDriveEditor.Utils.LoggerUtils.LOG;
import static AutoDriveEditor.XMLConfig.EditorXML.*;

public final class RotationButton extends BaseButton {

    public static Rotation rotation;
    boolean isControlNodeSelected = false;
    public int displayedRadius;
    private int totalAngle = 0;

    public RotationButton(JPanel panel) {
        button = makeImageToggleButton("buttons/rotatenode","buttons/rotatenode_selected", null,"copypaste_rotate_tooltip","copypaste_rotate_alt", panel, false, false,  null, false, this);
        rotation = new Rotation();
        button.addMouseListener(this);
    }

    @Override
    public String getButtonID() { return "RotateButton"; }

    @Override
    public String getButtonAction() { return "ActionButton"; }

    @Override
    public String getButtonPanel() { return "Edit"; }

    @Override
    public Boolean useMultiSelection() { return true; }

    @Override
    public void setSelected(boolean selected) {
        if (selected) {
            button.setSelected(true);
            showInTextArea(getInfoText(), true, false);
            if (multiSelectList.size() > 0) updateSelection();
            getMapPanel().repaint();
        } else {
            button.setSelected(false);
        }
    }

    // Override mouseClicked() to first check if the click happened on the button,
    // if it was, open the setting dialog and do nothing else
    // if it wasn't on the button call super()

    @Override
    public void mouseClicked(MouseEvent e) {
        if (e.getButton() == MouseEvent.BUTTON3 && e.getSource() == button) {
            if (button.isEnabled() && button.isSelected()) {
                showRotationSettingDialog();
            }
        } else {
            super.mouseClicked(e);
        }
    }


    @Override
    public void mousePressed(MouseEvent e) {
        super.mousePressed(e);
        if (e.getButton() == MouseEvent.BUTTON1) {
            if ( hoveredNode == rotation.getControlNode()) isControlNodeSelected = true;
            totalAngle = 0;
        }
    }

    @Override
    public void mouseReleased(MouseEvent e) {
        super.mouseReleased(e);
        if (e.getButton() == MouseEvent.BUTTON3 && e.getSource() == getMapPanel()) {
            updateSelection();
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
            totalAngle += rotation.rotateControlNode(e.getX(), e.getY(), 0);
            getMapPanel().repaint();
        }
    }

    @Override
    public void mouseWheelMoved(MouseWheelEvent e) { getMapPanel().repaint(); }

    private void updateSelection() {
        if (rotation != null) {
            rotation.setRotationSnapDegree(rotationSnap);
            if (multiSelectList.size() >0 ) {
                rotation.setCentrePoint(multiSelectList);
                rotation.setInitialControlNodePosition(multiSelectList);
                displayedRadius = rotation.getSelectionRadius(multiSelectList);
            }

        }
    }

    //
    // Dialog for Rotation Angle
    //

    public void showRotationSettingDialog() {
        JTextField rotText = new JTextField(String.valueOf(rotationSnap));
        JLabel rotLabel = new JLabel(" ");
        PlainDocument docX = (PlainDocument) rotText.getDocument();
        docX.setDocumentFilter(new LabelNumberFilter(rotLabel, 0, 360, false, false));

        Object[] inputFields = {getLocaleString("dialog_rotation_set"), rotText, rotLabel,};

        int option = JOptionPane.showConfirmDialog( getMapPanel(), inputFields, ""+ getLocaleString("dialog_rotation_set"), JOptionPane.OK_CANCEL_OPTION, JOptionPane.QUESTION_MESSAGE);

        if (option == JOptionPane.OK_OPTION) {
            rotationSnap = (int) Double.parseDouble(rotText.getText());
            rotation.setRotationSnapDegree(rotationSnap);
            getMapPanel().repaint();
        }
    }

    @Override
    public void drawToScreen(Graphics g) {
        if (multiSelectList.size() > 0 && this.button.isSelected()) {

            Graphics2D gTemp = (Graphics2D) g.create();
            gTemp.setColor(Color.WHITE);

            Point2D controlNodePosition = worldPosToScreenPos(rotation.getControlNode().x, rotation.getControlNode().z);
            Point2D selectionCentre = rotation.getCentrePointScreen();

            gTemp.fillArc((int) (selectionCentre.getX() - 3), (int) (selectionCentre.getY() - 3), 6, 6, 0, 360);

            Polygon p = new Polygon();

            if (hoveredNode == rotation.getControlNode()) {
                double diff = nodeSizeScaledHalf + ((nodeSizeScaled - nodeSizeScaledHalf) / 2);
                p.addPoint((int) controlNodePosition.getX(), (int) (controlNodePosition.getY() - diff));
                p.addPoint((int) (controlNodePosition.getX() + diff), (int) controlNodePosition.getY());
                p.addPoint((int) controlNodePosition.getX(), (int) (controlNodePosition.getY() + diff));
                p.addPoint((int) (controlNodePosition.getX() - diff), (int) controlNodePosition.getY());
                gTemp.setColor(colourNodeSelected);
                gTemp.fillPolygon(p);
            }

            p.reset();
            p.addPoint((int) controlNodePosition.getX(), (int) (controlNodePosition.getY() - nodeSizeScaledHalf));
            p.addPoint((int) (controlNodePosition.getX() + nodeSizeScaledHalf), (int) controlNodePosition.getY());
            p.addPoint((int) controlNodePosition.getX(), (int) (controlNodePosition.getY() + nodeSizeScaledHalf));
            p.addPoint((int) (controlNodePosition.getX() - nodeSizeScaledHalf), (int) controlNodePosition.getY());
            gTemp.setColor(colourNodeControl);
            gTemp.fillPolygon(p);



            BasicStroke bsDash = new BasicStroke(1, BasicStroke.CAP_BUTT, BasicStroke.JOIN_ROUND, 1.0f, new float[]{4f, 0f, 2f}, 2f);
            gTemp.setComposite(AlphaComposite.SrcOver.derive(0.5f));
            gTemp.setStroke(bsDash);
            gTemp.setColor(Color.WHITE);

            int radius = (int) selectionCentre.distance(controlNodePosition.getX(), controlNodePosition.getY());
            gTemp.drawArc((int) (selectionCentre.getX() - radius), (int) (selectionCentre.getY() - radius), radius * 2, radius * 2, 0, 360);

            gTemp.dispose();
        }
    }

    public static class RotateNodeChanger implements ChangeManager.Changeable {

        private final LinkedList<MapNode> storedRotateNodeList;
        private final Point2D centrePointWorld;
        private final int angle;
        private final boolean isStale;

        public RotateNodeChanger(LinkedList<MapNode> rotateNodes, Point2D centrePointWorld, int angle, boolean isStale){
            super();
            this.storedRotateNodeList = new LinkedList<>();
            this.centrePointWorld = centrePointWorld;
            this.angle = angle;
            this.isStale = isStale;

            for (int i = 0; i <= rotateNodes.size() -1 ; i++) {
                MapNode node = rotateNodes.get(i);
                if (bDebugLogUndoRedo) LOG.info("## RotateNodeChanger ## Adding ID {} to storedCurveNodeList", node.id);
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
