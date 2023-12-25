package AutoDriveEditor.GUI.Buttons;

import AutoDriveEditor.Managers.ButtonManager;
import AutoDriveEditor.RoadNetwork.MapNode;

import javax.swing.*;
import java.awt.*;
import java.awt.event.*;
import java.awt.geom.Point2D;
import java.util.ArrayList;

import static AutoDriveEditor.AutoDriveEditor.buttonManager;
import static AutoDriveEditor.GUI.MapPanel.*;
import static AutoDriveEditor.GUI.Menus.DebugMenu.Logging.LogGUIInfoMenu.bDebugLogGUIInfo;
import static AutoDriveEditor.GUI.TextPanel.showInTextArea;
import static AutoDriveEditor.Managers.ButtonManager.ButtonNode;
import static AutoDriveEditor.Managers.ButtonManager.ButtonState;
import static AutoDriveEditor.Utils.LoggerUtils.LOG;
import static AutoDriveEditor.Utils.MathUtils.normalizeAngle;
import static AutoDriveEditor.XMLConfig.EditorXML.*;

public abstract class BaseButton implements ButtonState, ActionListener, MouseListener {

    public ButtonNode buttonNode;
    protected AbstractButton button;

    //
    // Base class for all buttons, all options have defaults set here.
    //
    // Override any of these functions in your button class to change
    // the default behavior.
    //


    public String getButtonID() { return "BaseButton"; }

    public String getButtonAction() { return "BaseAction"; }

    public String getButtonPanel() { return "BasePanel"; }

    public String getInfoText() {  return ""; }

    public Boolean ignoreButtonDeselect() { return false; }

    public Boolean showHoverNodeSelect() { return true; }

    public Boolean alwaysSelectHidden() { return false; }

    public Boolean useMultiSelection() { return false; }

    public Boolean previewNodeSelectionChange() { return true; }

    public Boolean previewNodeHiddenChange() { return false; }

    public Boolean previewNodeFlagChange() { return false; }

    public Boolean previewConnectionHiddenChange() { return false; }

    public Integer getLineDetectionInterval() { return 10; }

    public Boolean getShowHighlightSelected() { return false; }

    public Boolean addSelectedToMultiSelectList() { return true; }

    public Boolean ignoreDeselect() { return false; }

    @Override
    public void setNode(ButtonNode buttonNode) {
        this.buttonNode = buttonNode;
    }

    @Override
    public void setEnabled(boolean enabled) { button.setEnabled(enabled); }

    @Override
    public void setSelected(boolean selected) {
        button.setSelected(selected);
        if (selected) {
            showInTextArea(getInfoText(), true, false);
        }
    }

    @Override
    public void actionPerformed(ActionEvent e) {
        if (button.isSelected()) {
            if (buttonNode.button.ignoreButtonDeselect()) {
                if (bDebugLogGUIInfo) LOG.info("BaseButton > isSelected ignoring button");
            } else {
                buttonManager.makeCurrent(buttonNode);
                if (bDebugLogGUIInfo) LOG.info("BaseButton > setting {} as current", buttonManager.getCurrentButtonID());
            }
        } else {
            if (ButtonManager.getCurrentButton() != null) {
                if (buttonNode.button.ignoreButtonDeselect()) {
                    if (bDebugLogGUIInfo) LOG.info("BaseButton > {} ignoring deselect", buttonNode.button.getButtonID());
                } else {
                    if (bDebugLogGUIInfo) LOG.info("BaseButton > {} triggered deselect all", buttonNode.button.getButtonID());
                    buttonManager.deSelectAll();
                }
            } else {
                if (bDebugLogGUIInfo) LOG.info("CurrentButton = {}", buttonNode.button.getButtonID());
            }
        }
    }


    public void mouseClicked(MouseEvent e) {}

    public void mousePressed(MouseEvent e) {}

    public void mouseReleased(MouseEvent e) {}

    public void mouseDragged(MouseEvent e) {}

    public void mouseMoved(MouseEvent e) {}

    public void mouseWheelMoved(MouseWheelEvent e) {}

    public void mouseEntered(MouseEvent e) {}

    public void mouseExited(MouseEvent e) {}

    public void drawToScreen(Graphics g) {}

    public static class MapNodeStore {
        private final MapNode mapNode;
        private final int mapNodeIDBackup;
        private final ArrayList<MapNode> incomingBackup;
        private final ArrayList<MapNode> outgoingBackup;

        public MapNodeStore(MapNode node) {
            this.mapNode = node;
            this.mapNodeIDBackup = node.id;
            this.incomingBackup = new ArrayList<>();
            this.outgoingBackup = new ArrayList<>();
            backupConnections();
        }

        public MapNode getMapNode() {
            if (this.hasChangedID()) this.resetID();
            return this.mapNode;
        }

        public void resetID() { this.mapNode.id = this.mapNodeIDBackup; }

        public boolean hasChangedID() { return this.mapNode.id != this.mapNodeIDBackup; }

        public void clearConnections() {
            clearIncoming();
            clearOutgoing();
        }

        public void clearIncoming() { this.mapNode.incoming.clear(); }

        public void clearOutgoing() { this.mapNode.outgoing.clear(); }

        public void backupConnections() {
            copyList(this.mapNode.incoming, this.incomingBackup);
            copyList(this.mapNode.outgoing, this.outgoingBackup);
        }

        public void restoreConnections() {
            copyList(this.incomingBackup, this.mapNode.incoming);
            copyList(this.outgoingBackup, this.mapNode.outgoing);
        }

        @SuppressWarnings("unused")
        public void backupIncoming() { copyList(this.mapNode.incoming, this.incomingBackup); }

        @SuppressWarnings("unused")
        public void restoreIncoming() { copyList(this.incomingBackup, this.mapNode.incoming); }

        @SuppressWarnings("unused")
        public void backupOutgoing() { copyList(this.mapNode.outgoing, this.outgoingBackup); }

        @SuppressWarnings("unused")
        public void restoreOutgoing() { copyList(this.outgoingBackup, this.mapNode.outgoing); }

        private void copyList(ArrayList<MapNode> from, ArrayList<MapNode> to) {
            to.clear();
            // use .clone() ??
            for (int i = 0; i <= from.size() - 1 ; i++) {
                MapNode mapNode = from.get(i);
                to.add(mapNode);
            }
        }
    }

    /**
     * Draws an arrow between two points, all the node locations must be
     * screen space co-ordinates
     *
     * @param g Graphics context the line will be drawn to
     * @param startNode Start point of the connection arrow
     * @param targetNode End point of the connection arrow
     * @param dualConnection Should it be drawn as a dual connection
     */
    public static void drawArrowBetween(Graphics g, Point2D startNode, Point2D targetNode, boolean dualConnection, Color colour, boolean hidden) {

        Graphics2D gTrans = (Graphics2D) g.create();
        Polygon p = new Polygon();

        double startX = startNode.getX();
        double startY = startNode.getY();
        double targetX = targetNode.getX();
        double targetY = targetNode.getY();

        double angleRad = Math.atan2(startY - targetY, startX - targetX);

        // calculate where to start the line based around the circumference of the node

        double distCos = (nodeSizeScaledHalf) * Math.cos(angleRad);
        double distSin = (nodeSizeScaledHalf) * Math.sin(angleRad);

        double lineStartX = startX - distCos;
        double lineStartY = startY - distSin;

        // calculate where to finish the line based around the circumference of the node

        double lineEndX = targetX + distCos;
        double lineEndY = targetY + distSin;

        double maxDistance = Math.sqrt(Math.pow((targetX - startX), 2) + Math.pow((targetY - startY), 2));

        //set the transparency level
        float tr = (hidden) ? hiddenNodesTransparencyLevel:  1f;
        gTrans.setComposite(AlphaComposite.SrcOver.derive(tr));
        gTrans.setColor(colour);

        if (nodeSizeScaled >= 2.0) {

            double lineLength = maxDistance - nodeSizeScaled;
            int diff = 0;

            // arrow size adjustment code

            if (dualConnection) {
                if (lineLength <= (nodeSizeScaled * 2)) {
                    diff =(int) ((nodeSizeScaled * 2) - lineLength) / 2;
                }
            } else {
                if (lineLength <= nodeSizeScaled) {
                    diff = (int) (nodeSizeScaled - lineLength);
                }
            }
            double adjustedArrowLength = ((nodeSize * zoomLevel) * 0.7) - (diff / 1.15);

            // Calculate where the center of the edge closest to the start point is
            double targetPolygonCenterX = targetX + (Math.cos(angleRad) * (adjustedArrowLength));
            double targetPolygonCenterY = targetY + (Math.sin(angleRad) * (adjustedArrowLength));

            double arrowLeft = normalizeAngle(angleRad + Math.toRadians(-20));
            double arrowLeftX = targetX + (Math.cos(arrowLeft) * adjustedArrowLength);
            double arrowLeftY = targetY + (Math.sin(arrowLeft) * adjustedArrowLength);

            double arrowRight = normalizeAngle(angleRad + Math.toRadians(20));
            double arrowRightX = targetX + (Math.cos(arrowRight) * adjustedArrowLength);
            double arrowRightY = targetY + (Math.sin(arrowRight) * adjustedArrowLength);

            if (maxDistance >= nodeSizeScaled) {
                if (bFilledArrows) {
                    // filled arrows look better, but have a performance impact on the draw times
                    p.addPoint((int) lineEndX, (int) lineEndY);
                    p.addPoint((int) arrowLeftX, (int) arrowLeftY);
                    p.addPoint((int) arrowRightX, (int) arrowRightY);
                    gTrans.fillPolygon(p);
                    p.reset();
                } else {
                    gTrans.drawLine((int) lineStartX, (int) lineStartY, (int) lineEndX, (int) (lineEndY));
                    gTrans.drawLine((int) lineEndX, (int) lineEndY, (int) arrowLeftX, (int) arrowLeftY);
                    gTrans.drawLine((int) lineEndX, (int) lineEndY, (int) arrowRightX, (int) arrowRightY);
                }


                if (dualConnection) {
                    angleRad = normalizeAngle(angleRad+Math.PI);


                    arrowLeft = normalizeAngle(angleRad + Math.toRadians(-20));
                    arrowLeftX = startX + (Math.cos(arrowLeft) * adjustedArrowLength);
                    arrowLeftY = startY + (Math.sin(arrowLeft) * adjustedArrowLength);

                    arrowRight = normalizeAngle(angleRad + Math.toRadians(20));
                    arrowRightX = startX + (Math.cos(arrowRight) * adjustedArrowLength);
                    arrowRightY = startY + (Math.sin(arrowRight) * adjustedArrowLength);

                    if (bFilledArrows) {
                        // filled arrows look better, but have a performance impact on the draw times
                        p.addPoint((int) lineStartX, (int) lineStartY);
                        p.addPoint((int) arrowLeftX, (int) arrowLeftY);
                        p.addPoint((int) arrowRightX, (int) arrowRightY);
                        gTrans.fillPolygon(p);
                        p.reset();
                    } else {
                        gTrans.drawLine((int) lineStartX, (int) lineStartY, (int) lineEndX, (int) (lineEndY));
                        gTrans.drawLine((int) lineEndX, (int) lineEndY, (int) arrowLeftX, (int) arrowLeftY);
                        gTrans.drawLine((int) lineEndX, (int) lineEndY, (int) arrowRightX, (int) arrowRightY);
                    }
                }

                if (dualConnection) {
                    double startPolygonCenterX = startX + (Math.cos(angleRad) * adjustedArrowLength);
                    double startPolygonCenterY = startY + (Math.sin(angleRad) * adjustedArrowLength);
                    gTrans.drawLine((int) startPolygonCenterX, (int) startPolygonCenterY, (int) targetPolygonCenterX, (int) targetPolygonCenterY);
                } else {
                    gTrans.drawLine((int) lineStartX, (int) lineStartY, (int) targetPolygonCenterX, (int) targetPolygonCenterY);

                }
            }
        } else {
            // small zoomLevel's don't draw the actual Nodes, draw from the start to the end of
            // the node position, no visible gaps are seen between the node points.
            gTrans.drawLine((int) lineStartX, (int) lineStartY, (int) lineEndX, (int) (lineEndY));
        }
    }
}
