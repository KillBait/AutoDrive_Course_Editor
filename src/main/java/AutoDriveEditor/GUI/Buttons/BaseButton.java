package AutoDriveEditor.GUI.Buttons;

import AutoDriveEditor.Classes.KeyBinds.Shortcut;
import AutoDriveEditor.Managers.RenderManager.Drawable;
import AutoDriveEditor.Managers.ShortcutManager;
import AutoDriveEditor.RoadNetwork.Connection;
import AutoDriveEditor.RoadNetwork.MapNode;

import javax.swing.*;
import java.awt.*;
import java.awt.event.*;
import java.awt.geom.Point2D;
import java.util.ArrayList;

import static AutoDriveEditor.AutoDriveEditor.buttonManager;
import static AutoDriveEditor.Classes.Util_Classes.LoggerUtils.LOG;
import static AutoDriveEditor.Classes.Util_Classes.MathUtils.normalizeAngle;
import static AutoDriveEditor.GUI.MapPanel.*;
import static AutoDriveEditor.GUI.Menus.DebugMenu.Logging.LogGUIInfoMenu.bDebugLogGUIInfo;
import static AutoDriveEditor.GUI.TextPanel.showInTextArea;
import static AutoDriveEditor.Managers.ButtonManager.*;
import static AutoDriveEditor.RoadNetwork.MapNode.NODE_FLAG_REGULAR;
import static AutoDriveEditor.RoadNetwork.MapNode.NODE_FLAG_SUBPRIO;
import static AutoDriveEditor.XMLConfig.EditorXML.*;

public abstract class BaseButton extends Drawable implements ButtonInterface, ToolTipBuilder, ActionListener, MouseListener {

    public ButtonNode buttonNode;
    protected AbstractButton button;
    private static BaseButton instance;

    //
    // Base class for all buttons, all options have defaults set here.
    //
    // Override these functions in your button class to change
    // the default behavior.
    //

    public String getButtonID() { return "BaseButton"; }

    public String getButtonAction() { return "BaseAction"; }

    public String getButtonPanel() { return "BasePanel"; }

    public String getInfoText() {  return ""; }

    public String getHelpText() { return this.getButtonID() + " HelpText"; }

    public Boolean ignoreButtonDeselect() { return false; }

    public Boolean showHoverNodeSelect() { return true; }

    public Boolean alwaysSelectHidden() { return false; }

    public Boolean useMultiSelection() { return false; }

    public Boolean usePanelEdgeScrolling() { return false; }

    public Boolean previewNodeSelectionChange() { return true; }

    public Boolean previewNodeHiddenChange() { return false; }

    public Boolean previewNodeFlagChange() { return false; }

    public Boolean previewConnectionHiddenChange() { return false; }

    public Integer getLineDetectionInterval() { return 10; }

    public Boolean getShowHighlightSelected() { return false; }

    public Boolean addSelectedToMultiSelectList() { return true; }

    public Boolean ignoreDeselect() { return false; }

    public void onButtonCreation() {};

    // triggered when a button is selected
    public void onButtonSelect() {}

    // triggered when a button is deselected
    public void onButtonDeselect() {}

    public void onConfigChange() {}

    // override this function if you want your button to draw custom graphics
    // to the MapPanel, see the RenderManager class for more information on
    // the functions to set the render priority in your button.
    public void drawToScreen(Graphics g) {}

    //
    // Handy Dandy MouseListener functions, override these in your button class
    // to save you having to manually add listeners to your buttons.
    //

    public void mouseClicked(MouseEvent e) {}

    public void mousePressed(MouseEvent e) {}

    public void mouseReleased(MouseEvent e) {}

    public void mouseDragged(MouseEvent e) {}

    public void mouseMoved(MouseEvent e) {}

    public void mouseWheelMoved(MouseWheelEvent e) {}

    public void mouseEntered(MouseEvent e) {}

    public void mouseExited(MouseEvent e) {}

    public void onMultiSelectStart() {}

    public void onMultiSelectStop() {}

    public void onMultiSelectAdd(ArrayList<MapNode> addedNodes) {}

    public void onMultiSelectRemove(ArrayList<MapNode> removedNodes) {}

    public void onMultiSelectChange(ArrayList<MapNode> nodeList) {}

    public void onMultiSelectOneTime(ArrayList<MapNode> oneTimeList) {}

    public void onMultiSelectCleared() {}




    //
    // setNode() - WARNING: Do not override this function in any class that extends BaseButton
    // Called exclusively by the ButtonManager..

    @Override
    public void setNode(ButtonNode buttonNode) {
        this.buttonNode = buttonNode;
        updateTooltip();
    }

    @Override
    public void setEnabled(boolean enabled) { if (button != null) button.setEnabled(enabled); }

    @Override
    public void setSelected(boolean selected) {
        if (!buttonNode.buttonInterface.ignoreButtonDeselect()) {
            button.setSelected(selected);
            if (selected) showInTextArea(buttonNode.buttonInterface.getInfoText(), true, false);
            if (bDebugLogGUIInfo) LOG.info("## BaseButton.setSelected() ## setSelected: button {} ( {} )",buttonNode.buttonInterface.getButtonID(), selected);
        } else {
            if (bDebugLogGUIInfo) LOG.info("## BaseButton.setSelected() ## ignoring deselect for {}", buttonNode.buttonInterface.getButtonID());
        }
    }

    @Override
    public void setVisible(boolean visible) {
        if (button != null) button.setVisible(visible);
    }

    @Override
    public void actionPerformed(ActionEvent e) {
        if (button.isSelected()) {
            if (buttonNode.buttonInterface.ignoreButtonDeselect()) {
                if (bDebugLogGUIInfo) LOG.info("## BaseButton.actionPerformed() ## isSelected ignoring button");
            } else {
                buttonManager.makeCurrent(buttonNode);
                if (bDebugLogGUIInfo) LOG.info("## BaseButton.actionPerformed() ## setting {} as current", buttonManager.getCurrentButtonID());
            }
        } else {
            if (buttonManager.getCurrentButton() != null) {
                if (buttonNode.buttonInterface.ignoreButtonDeselect()) {
                    if (bDebugLogGUIInfo) LOG.info("## BaseButton.actionPerformed() ## {} ignoring deselect", buttonNode.buttonInterface.getButtonID());
                } else {
                    if (bDebugLogGUIInfo) LOG.info("## BaseButton.actionPerformed() ## {} triggered deselect all", buttonNode.buttonInterface.getButtonID());
                    buttonManager.deSelectAll();
                }
            } else {
                if (bDebugLogGUIInfo) LOG.info("## BaseButton.actionPerformed() ## getCurrentButton() is null (actual button  = {} )", buttonNode.buttonInterface.getButtonID());
            }
        }
    }

    //
    // Tooltip helper functions, implement the ToolTipBuilder interface
    // and override the buildToolTip() function to createSetting a custom tooltip
    // for your button.
    //

    /** updateTooltip() - Automatically called by the ButtonManager when
     * a new button is added, <br><br>NOTE:<br>(1) You must implement the
     * ToolTipBuilder interface for this to work correctly.<br>
     * If you don't implement theToolTipBuilder interface, you will need
     * to manually set the tooltip text for your button.<br>
     * (2) Can be called at any time.
    **/
    public void updateTooltip() {
        String tooltip = buildToolTip();
        button.setToolTipText(tooltip);
        button.getAccessibleContext().setAccessibleDescription(tooltip);
    }

    /** setToolTip() - Manually set the tooltip, can be called anytime
     * @param tooltip - The tooltip text to set
     **/
    public void setTooltip(String tooltip) {
        button.setToolTipText(tooltip);
        button.getAccessibleContext().setAccessibleDescription(tooltip);
    }

    public void setToolTipStale() {
        updateTooltip();
    }

    public String getShortcutText(ShortcutManager.ShortcutID shortcutID, String text) {
        Shortcut s = ShortcutManager.getUserShortcutByID(shortcutID);
        if (s != null) {
            return "<html>" + text + "<br> ( Shortcut: <b>" + s.getShortcutString() + " )</b</html>";
        } else {
            return text;
        }
    }

    public AbstractButton getButton() { return this.button; }

    /** getButtonNode() - Get the button node for this button
     * @return ButtonNode - The button node for this button
     * @see ButtonNode
     */
    public ButtonNode getButtonNode() { return this.buttonNode; }

    /** getToolTip() - Get the current buttons tooltip text
     * @return String - The current tooltip text
     **/
    public String getToolTip() { return button.getToolTipText(); }

    /** forceShowToolTip() - Force the tooltip to show for any component
     * @param component - The component to show the tooltip for
     **/
    public void forceShowToolTip(JComponent component) {
        ToolTipManager.sharedInstance().mouseMoved(
                new MouseEvent(component, MouseEvent.MOUSE_MOVED, System.currentTimeMillis(), 0,
                        0, 0, 0, false));
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
    public static void drawArrowBetween(Graphics g, Point2D startNode, Point2D targetNode, boolean dualConnection, Color colour) {

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
        //float tr = (hidden) ? hiddenNodesTransparencyLevel:  1f;
        //gTrans.setComposite(AlphaComposite.SrcOver.derive(tr));
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

    public static Color getConnectionColour(MapNode startNode, MapNode endNode, Connection.ConnectionType type) {
        Color colour;
        if (type == Connection.ConnectionType.DUAL) {
            if (startNode.getFlag() == NODE_FLAG_SUBPRIO || endNode.getFlag() == NODE_FLAG_SUBPRIO) {
                colour = colourConnectDualSubprio;
            } else {
                colour = colourConnectDual;
            }
        } else if (type == Connection.ConnectionType.REVERSE || type == Connection.ConnectionType.CROSSED_REVERSE) {
            if (startNode.getFlag() == NODE_FLAG_REGULAR) {
                colour = colourConnectReverse;
            } else {
                colour = colourConnectReverseSubprio;
            }
        } else if (type == Connection.ConnectionType.REGULAR || type == Connection.ConnectionType.CROSSED_REGULAR || type == Connection.ConnectionType.SUBPRIO) {
            if (startNode.getFlag() == NODE_FLAG_REGULAR) {
                colour = colourConnectRegular;
            } else {
                colour = colourConnectSubprio;
            }
        } else {
            colour = Color.WHITE;
        }
        return colour;
    }
}
