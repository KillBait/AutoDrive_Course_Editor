package AutoDriveEditor.GUI.Buttons;

import AutoDriveEditor.Classes.CircularList;
import AutoDriveEditor.Classes.KeyBinds.Shortcut;
import AutoDriveEditor.Classes.KeyBinds.ShortcutGroup;
import AutoDriveEditor.Classes.SnapShot;
import AutoDriveEditor.Classes.Util_Classes.ExceptionUtils;
import AutoDriveEditor.Managers.ButtonManager;
import AutoDriveEditor.Managers.ChangeManager;
import AutoDriveEditor.RoadNetwork.MapNode;

import javax.swing.*;
import java.awt.*;
import java.awt.event.ActionEvent;
import java.awt.event.MouseEvent;
import java.awt.geom.Point2D;
import java.math.RoundingMode;

import static AutoDriveEditor.AutoDriveEditor.*;
import static AutoDriveEditor.Classes.Util_Classes.MathUtils.limitDoubleToDecimalPlaces;
import static AutoDriveEditor.GUI.MapPanel.*;
import static AutoDriveEditor.Listeners.MouseListener.currentMouseX;
import static AutoDriveEditor.Listeners.MouseListener.currentMouseY;
import static AutoDriveEditor.Managers.ScanManager.checkAreaForNodeOverlap;
import static AutoDriveEditor.Managers.ShortcutManager.*;
import static AutoDriveEditor.Managers.ShortcutManager.ShortcutID.NEW_NODE_SHORTCUT;
import static AutoDriveEditor.RoadNetwork.MapNode.NODE_FLAG_REGULAR;
import static AutoDriveEditor.RoadNetwork.MapNode.createNewNetworkNode;
import static AutoDriveEditor.RoadNetwork.RoadMap.showMismatchedIDError;
import static AutoDriveEditor.XMLConfig.AutoSave.resumeAutoSaving;
import static AutoDriveEditor.XMLConfig.AutoSave.suspendAutoSaving;
import static AutoDriveEditor.XMLConfig.EditorXML.bGridSnapEnabled;
import static AutoDriveEditor.XMLConfig.EditorXML.bGridSnapSubs;

public abstract class AddNodeBaseButton extends BaseButton  implements ShortcutGroup.ShortcutGroups {

    protected int nodeType;
    private boolean isActive = false;
    protected static Point2D previewWorldPos;
    protected static ShortcutGroup addNodeShortcutGroup;

    @Override
    public void onButtonCreation() {
        if (addNodeShortcutGroup == null) {
            addNodeShortcutGroup = ShortcutGroup.createShortcutGroup(ADD_NODE_GROUP);
            Shortcut addNodeShortcut = getUserShortcutByID(NEW_NODE_SHORTCUT);
            if (addNodeShortcut != null) {
                Action addNodeButtonAction = new AbstractAction() {
                    @Override
                    public void actionPerformed(ActionEvent e) {
                        if (button.isEnabled()) {
                            ShortcutGroup newNodeGroup = ShortcutGroup.getButtonGroup(ADD_NODE_GROUP);
                            if (newNodeGroup != null) {
                                if (buttonManager.getCurrentButton() != null && buttonManager.getCurrentButton() == newNodeGroup.getCurrentButton()) {
                                    ButtonManager.ButtonNode next = newNodeGroup.getNextButton().getButtonNode();
                                    buttonManager.makeCurrent(next);
                                } else {
                                    buttonManager.makeCurrent(newNodeGroup.getCurrentButton().getButtonNode());
                                }
                            }
                        }
                    }
                };
                registerShortcut(this, addNodeShortcut, addNodeButtonAction, getMapPanel());
            }
        }
    }

    @Override
    public void onButtonSelect() {
        isActive = true;
        calcPosition(currentMouseX, currentMouseY);
    }

    @Override
    public void onButtonDeselect() { isActive = false; }

    @Override
    public ShortcutGroup getGroup() { return addNodeShortcutGroup; }

    @Override
    public CircularList<BaseButton> getGroupMembers() { return addNodeShortcutGroup.getButtonNodeList(); }

    @Override
    public void mouseMoved(MouseEvent e) {
        if (isActive) calcPosition(e.getX(), e.getY());
    }

    @Override
    public void mouseReleased(MouseEvent e) {
        if (e.getButton() == MouseEvent.BUTTON1) {
            if (isActive) {
                double y = limitDoubleToDecimalPlaces(getYValueFromHeightMap(previewWorldPos.getX(), previewWorldPos.getY()), 3, RoundingMode.HALF_UP);
                MapNode newNode = createNode(previewWorldPos.getX(), y, previewWorldPos.getY(), nodeType);
                checkAreaForNodeOverlap(newNode);
            }
        }
    }

    private void calcPosition(int screenPosX, int screenPosY) {
        if (bGridSnapEnabled) {
            if (bIsShiftPressed) {
                previewWorldPos = screenPosToWorldPos(screenPosX, screenPosY);
            } else {
                previewWorldPos = calcWorldSnapPosition(screenPosX, screenPosY);
            }
        } else {
            if (bIsShiftPressed) {
                previewWorldPos = calcWorldSnapPosition(screenPosX, screenPosY);
            } else {
                previewWorldPos = screenPosToWorldPos(screenPosX, screenPosY);
            }
        }
        getMapPanel().repaint();
    }

    Point2D calcWorldSnapPosition(int screenPosX, int screenPosY) {
        Point2D.Double scaledDiff = new Point2D.Double();
        Point2D p = screenPosToWorldPos(screenPosX, screenPosY);
        double gridX = bGridSnapSubs ? gridSpacingX / (gridSubDivisions + 1) : gridSpacingX;
        double gridY = bGridSnapSubs ? gridSpacingY / (gridSubDivisions + 1) : gridSpacingY;
        scaledDiff.x = Math.round((p.getX()) / gridX) * gridX;
        scaledDiff.y = Math.round((p.getY()) / gridY) * gridY;
        return scaledDiff;
    }

    public static MapNode createNode(double worldX, double worldY, double worldZ, int flag) {
        if (roadMap != null) {
            suspendAutoSaving();
            MapNode newNode = createNewNetworkNode(worldX, worldY, worldZ, flag, false);
            changeManager.addChangeable(new AddNodeChanger(newNode));
            setStale(true);
            resumeAutoSaving();
            getMapPanel().repaint();
            return newNode;
        }
        return null;
    }

    @Override
    public void drawToScreen(Graphics g) {
        if (isActive) {
            Graphics2D g2d = (Graphics2D) g.create();
            g2d.setComposite(AlphaComposite.SrcOver.derive(0.5f));
            if (previewWorldPos != null) {
                Point2D screenPos = worldPosToScreenPos(previewWorldPos);
                if (nodeType == NODE_FLAG_REGULAR) {
                    g2d.drawImage(regularNodeImage, (int) (screenPos.getX() - (double) (regularNodeImage.getWidth()-1) / 2), (int) (screenPos.getY() - (double) (regularNodeImage.getHeight()-1) / 2), (int) nodeSizeScaled, (int) nodeSizeScaled, null);
                } else {
                    g2d.drawImage(subprioNodeImage, (int) (screenPos.getX() - (double) (regularNodeImage.getWidth()-1) / 2), (int) (screenPos.getY() - (double) (regularNodeImage.getHeight()-1) / 2), (int) nodeSizeScaled, (int) nodeSizeScaled, null);
                }
                g2d.dispose();
            }
        }
    }

    //
    // Add node changer
    //

    public static class AddNodeChanger implements ChangeManager.Changeable {
        private final SnapShot snapShot;
        private final boolean isStale;


        public AddNodeChanger(MapNode mapNode){
            super();
            this.snapShot = new SnapShot(mapNode);
            this.isStale = isStale();
        }

        public void undo(){
            suspendAutoSaving();
            this.snapShot.removeOriginalNodes();
            setStale(this.isStale);
            resumeAutoSaving();
            getMapPanel().repaint();
        }

        public void redo(){
            try {
                this.snapShot.restoreOriginalNodes();
//                for (MapNode storedNode : this.snapShot.getOriginalNodeList()) {
//                    checkNodeOverlap(storedNode);
//                }
                getMapPanel().repaint();
                setStale(true);
            } catch (ExceptionUtils.MismatchedIdException e) {
                showMismatchedIDError("AddNodeChanger redo()", e);
            }

        }
    }
}
