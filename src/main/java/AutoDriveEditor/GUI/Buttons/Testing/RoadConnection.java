package AutoDriveEditor.GUI.Buttons.Testing;

import AutoDriveEditor.Classes.LinearLine;
import AutoDriveEditor.GUI.Buttons.BaseButton;
import AutoDriveEditor.RoadNetwork.MapNode;
import AutoDriveEditor.RoadNetwork.RoadMap;

import javax.swing.*;
import java.awt.*;
import java.awt.event.MouseEvent;
import java.awt.event.MouseWheelEvent;
import java.awt.geom.Point2D;
import java.util.LinkedList;

import static AutoDriveEditor.AutoDriveEditor.getMapPanel;
import static AutoDriveEditor.AutoDriveEditor.mapPanel;
import static AutoDriveEditor.GUI.Buttons.LinerLineBaseButton.*;
import static AutoDriveEditor.GUI.MapPanel.*;
import static AutoDriveEditor.GUI.Menus.DebugMenu.Logging.LogLinearLineInfoMenu.bDebugLogLinearlineInfo;
import static AutoDriveEditor.GUI.TextPanel.showInTextArea;
import static AutoDriveEditor.Locale.LocaleManager.getLocaleString;
import static AutoDriveEditor.RoadNetwork.MapNode.NODE_FLAG_REGULAR;
import static AutoDriveEditor.RoadNetwork.MapNode.NODE_FLAG_SUBPRIO;
import static AutoDriveEditor.Utils.GUIUtils.makeImageToggleButton;
import static AutoDriveEditor.Utils.LoggerUtils.LOG;
import static AutoDriveEditor.Utils.MathUtils.roundUpDoubleToDecimalPlaces;
import static AutoDriveEditor.XMLConfig.EditorXML.*;

public class RoadConnection extends BaseButton {

    //
    // Early testing:- May not make into release...
    //

    LinearLine primaryLinearLine;
    LinearLine secondaryLinearLine;
    MapNode startNode;
    MapNode secondaryEndNode;
    MapNode secondaryStartNode;
    public final int  connectionState = 0;
    public final int connectionType = 0;
    boolean isCreatingLine = false;
    double distance = 6;

    public RoadConnection(JPanel panel) {
        button = makeImageToggleButton("buttons/connectroad","buttons/connectroad_selected", null,"nodes_create_road_connection_tooltip","nodes_create_road_connection_alt", panel, false, false,  null, false, this);
    }

    @Override
    public String getButtonID() { return "RoadConnectionButton"; }

    @Override
    public String getButtonAction() { return "ActionButton"; }

    @Override
    public String getButtonPanel() { return "Nodes"; }

    @Override
    public String getInfoText() { return getLocaleString("nodes_create_road_connection_tooltip"); }

    @Override
    public void setSelected(boolean selected) {
        if (selected) {
            button.setSelected(true);
            showInTextArea(getInfoText(), true, false);
        } else {
            button.setSelected(false);
            if (isCreatingLine) {
                LOG.info("Cancelled Road Connection");
                cancelLinearLine();
            }
        }
    }

    @Override
    public void mousePressed(MouseEvent e) {
        if (e.getButton() == MouseEvent.BUTTON1) {
            MapNode selected = getNodeAtScreenPosition(e.getX(), e.getY());
            if (startNode == null) {
                if (selected != null && !selected.isControlNode()) {
                    startNode = selected;

                    // create primary Linear Line

                    primaryLinearLine = new LinearLine(startNode, e.getX(), e.getY(), connectionState);
                    isCreatingLine = true;
                    if (bDebugLogLinearlineInfo) LOG.info("## RoadConnection Debug ## created primary linear line starting at x={},y={},z={} : ending a mouse pointer x={},y={}",startNode.x, startNode.y, startNode.z, e.getX(), e.getY());
                    showInTextArea(getLocaleString("infopanel_linearline_started"), true, false);

                    // create secondary Linear Line

                    Point2D pointerWorldPos = screenPosToWorldPos(e.getX(), e.getY());
                    secondaryStartNode = RoadMap.createMapNode(-99, pointerWorldPos.getX(), pointerWorldPos.getY(), NODE_FLAG_REGULAR, false, false);

                    Point2D selectedScreenPos = worldPosToScreenPos(selected.x, selected.z);
                    secondaryLinearLine = new LinearLine(secondaryStartNode, (int) selectedScreenPos.getX(), (int) selectedScreenPos.getY(), connectionState);
                    secondaryEndNode = RoadMap.createMapNode(-99,selected.x, selected.z, NODE_FLAG_REGULAR, false, false);
                    if (bDebugLogLinearlineInfo) LOG.info("## RoadConnection Debug ## created secondary linear line starting at mouse pointer x={},y={} : ending at x={}, y={}, z={}",pointerWorldPos.getX(), pointerWorldPos.getY(), secondaryEndNode.x, secondaryEndNode.y, secondaryEndNode.z);

                    getMapPanel().repaint();

                }
            } else if (selected == startNode) {
                startNode = null;
                isCreatingLine = false;
                if (bDebugLogLinearlineInfo) LOG.info("## RoadConnection Debug ## canceling primary and secondary linear lines");
                showInTextArea(getLocaleString("infopanel_linearline_canceled"), true, false);
            } else {

                if (selected != null && !selected.isControlNode()) {
                    LOG.info("Creation Of Road Connection Is Not Yet Possible");
                    /*if (bDebugLogLinearlineInfo) LOG.info("## LinearLine Debug ## End node selected, creating linear line");
                    createLinearLine(new Point2D.Double(e.getX(), e.getY()));
                    if (bContinuousConnections) {
                        if (bDebugLogLinearlineInfo) LOG.info("## LinearLine Debug ## Continuous connection enabled, starting next line");
                        startNode = selected;
                        Point2D pointerPos = screenPosToWorldPos(e.getX(), e.getY());
                        linearLine = new LinearLine(selected, pointerPos.getX(), pointerPos.getY(), connectionState);
                    } else {
                        if (bDebugLogLinearlineInfo) LOG.info("## LinearLine Debug ## Linear line finished");
                        isDraggingLine = false;
                        startNode = null;
                    }*/
                } else {
                    LOG.info("Creation Of A Road Connection With No End Node Is Not Yet Possible");
                    /*if (selected == null && bCreateLinearLineEndNode) {
                        if (bDebugLogLinearlineInfo) LOG.info("## LinearLine Debug ## No end node selected");
                        createLinearLine(new Point2D.Double(e.getX(), e.getY()));
                        if (bContinuousConnections) {
                            linearLine = new LinearLine(getNodeAt(e.getX(), e.getY()), e.getX(), e.getY(), connectionState);
                        } else {
                            isDraggingLine = false;
                            startNode = null;
                        }
                    }*/
                }
            }
        }
        if (e.getButton() == MouseEvent.BUTTON3) {
            cancelLinearLine();
            secondaryEndNode = null;
            secondaryStartNode = null;
        }
    }

    public void mouseMoved(MouseEvent e) {

        double newX, newY;
        double scaledDiffX = 0, scaledDiffY  = 0;
        double xPos = 0,yPos = 0;

        if (startNode != null && isCreatingLine) {
            if (e.getX() > mapPanel.getWidth()) moveMapBy( -10, 0);
            if (e.getX() <= 0) moveMapBy( 10, 0);
            if (e.getY() > mapPanel.getHeight()) moveMapBy( 0, -10);
            if (e.getY() <= 0) moveMapBy( 0, 10);
            
            Point2D pointerPos = screenPosToWorldPos(e.getX(), e.getY());

            if (bGridSnap) {
                if (bGridSnapSubs) {
                    newX = Math.round(pointerPos.getX() / (gridSpacingX / (gridSubDivisions + 1))) * (gridSpacingX / (gridSubDivisions + 1));
                    newY = Math.round(pointerPos.getY() / (gridSpacingY / (gridSubDivisions + 1))) * (gridSpacingY / (gridSubDivisions + 1));
                } else {
                    newX = Math.round(pointerPos.getX() / gridSpacingX) * gridSpacingX;
                    newY = Math.round(pointerPos.getY() / gridSpacingY) * gridSpacingY;
                }
                scaledDiffX = newX - pointerPos.getX();
                scaledDiffY = newY - pointerPos.getY();

            }

            if (pointerPos.getX() + scaledDiffX > -1024 * mapScale && pointerPos.getX() + scaledDiffX < 1024 * mapScale) {
                if (bGridSnap) {
                    xPos = Math.round((pointerPos.getX() + scaledDiffX) * 50D) / 50D;
                } else {
                    xPos = roundUpDoubleToDecimalPlaces(pointerPos.getX() + scaledDiffX, 3);
                }
            }
            if (pointerPos.getY() + scaledDiffY > -1024 * mapScale && pointerPos.getY() + scaledDiffY < 1024 * mapScale) {
                if (bGridSnap) {
                    yPos = Math.round((pointerPos.getY() + scaledDiffY) * 50D) / 50D;
                } else {
                    yPos = roundUpDoubleToDecimalPlaces(pointerPos.getY() + scaledDiffY, 3);
                }
            }

            primaryLinearLine.updateLineEndLocation(xPos, yPos);

            double theta = getTheta(startNode, primaryLinearLine.getLineEndNode());
            rotateSecondaryStartPoint(theta, distance, 90);
            rotateSecondaryEndPoint(theta, distance, 90);
            getMapPanel().repaint();
        }
    }

    public void mouseWheelMoved(MouseWheelEvent e) {
        if (bIsShiftPressed) {
            distance -= e.getWheelRotation();
            double theta = getTheta(startNode, primaryLinearLine.getLineEndNode());
            rotateSecondaryStartPoint(theta, distance, 90);
            rotateSecondaryEndPoint(theta, distance, 90);
            getMapPanel().repaint();
        } else {
            Point2D pointerPos = screenPosToWorldPos(e.getX(), e.getY());
            if (primaryLinearLine != null) {
                primaryLinearLine.updateLineEndLocation(pointerPos.getX(), pointerPos.getY());
                double theta = getTheta(startNode, primaryLinearLine.getLineEndNode());
                rotateSecondaryStartPoint(theta, distance, 90);
                rotateSecondaryEndPoint(theta, distance, 90);
                getMapPanel().repaint();
            }
        }
    }

    private double getTheta(MapNode origin, MapNode point) {
        return Math.atan2(origin.z - point.z, origin.x - point.x) % (2*Math.PI);
    }

    @SuppressWarnings("SameParameterValue")
    private void rotateSecondaryStartPoint(double theta, double distance, int degreesOffset) {
        double newX = primaryLinearLine.getLineEndNode().x - (distance * Math.cos(theta - Math.toRadians(degreesOffset)));
        double newY = primaryLinearLine.getLineEndNode().z - (distance * Math.sin(theta - Math.toRadians(degreesOffset)));
        secondaryStartNode.x = newX;
        secondaryStartNode.z = newY;
        secondaryLinearLine.updateLineStartLocation(newX, newY);
    }

    @SuppressWarnings("SameParameterValue")
    private void rotateSecondaryEndPoint(double theta, double distance, int degreesOffset) {
        double newX = primaryLinearLine.getLineStartNode().x - (distance * Math.cos(theta - Math.toRadians(degreesOffset)));
        double newY = primaryLinearLine.getLineStartNode().z - (distance * Math.sin(theta - Math.toRadians(degreesOffset)));
        secondaryEndNode.x = newX;
        secondaryEndNode.z = newY;
        secondaryLinearLine.updateLineEndLocation(newX, newY);
    }

    private void cancelLinearLine() {
        startNode = null;
        isCreatingLine = false;
        primaryLinearLine = null;
        secondaryLinearLine = null;
        getMapPanel().repaint();
    }

    @Override
    public void drawToScreen(Graphics g) {
        if (isCreatingLine) {
            Color colour = Color.GREEN;
            LinkedList<MapNode> primaryLinearLineNodeList = primaryLinearLine.getLinearLineNodeList();
            LinkedList<MapNode> secondaryLinearLineNodeList = secondaryLinearLine.getLinearLineNodeList();

            for (int j = 0; j < primaryLinearLineNodeList.size(); j++) { // skip the last node of the array
                MapNode firstPrimaryNode = primaryLinearLineNodeList.get(j);
                Point2D firstPrimaryNodePos = worldPosToScreenPos(firstPrimaryNode.x, firstPrimaryNode.z);

                // don't drawToScreen the first node image as it already exists

                if (j > 0) {
                    Shape oldClip = g.getClip();
                    Graphics2D g2d = (Graphics2D) g.create();
                    g2d.setComposite(AlphaComposite.SrcOver.derive(0.5f));
                    if (startNode.flag == NODE_FLAG_REGULAR) {
                        g2d.setColor(Color.WHITE);
                    } else {
                        g2d.setColor(colourNodeSubprio);
                    }
                    g2d.fillArc((int) (firstPrimaryNodePos.getX() - nodeSizeScaledHalf), (int) (firstPrimaryNodePos.getY() - nodeSizeScaledHalf), (int) nodeSizeScaled, (int) nodeSizeScaled, 0, 360);
                    g2d.setClip(oldClip);
                    g2d.dispose();
                }

                if ( j < primaryLinearLineNodeList.size() - 1) {
                    MapNode secondPrimaryNode = primaryLinearLineNodeList.get(j+1);
                    Point2D secondaryPrimaryNodePos = worldPosToScreenPos(secondPrimaryNode.x, secondPrimaryNode.z);

                    // select the colour of line to drawToScreen

                    if (connectionType == CONNECTION_STANDARD) {
                        if (secondPrimaryNode.flag ==NODE_FLAG_SUBPRIO) colour = colourConnectSubprio;
                    } else if ( connectionType == CONNECTION_DUAL ) {
                        if (secondPrimaryNode.flag == NODE_FLAG_REGULAR) {
                            colour = colourConnectDual;
                        } else {
                            colour = colourConnectDualSubprio;
                        }
                    } else if ( connectionType == CONNECTION_REVERSE ) {
                        if (secondPrimaryNode.flag == NODE_FLAG_REGULAR) {
                            colour = colourConnectReverse;
                        } else {
                            colour = colourConnectReverseSubprio;
                        }
                    }

                    //g.setColor(colour);
                    drawArrowBetween(g, firstPrimaryNodePos, secondaryPrimaryNodePos, connectionType == CONNECTION_DUAL, colour, false);
                }
            }

            for (int j = 0; j < secondaryLinearLineNodeList.size(); j++) { // skip the last node of the array
                MapNode firstSecondaryNode = secondaryLinearLineNodeList.get(j);
                Point2D firstSecondaryNodePos = worldPosToScreenPos(firstSecondaryNode.x, firstSecondaryNode.z);

                Shape oldClip = g.getClip();
                Graphics2D g2d = (Graphics2D) g.create();
                g2d.setComposite(AlphaComposite.SrcOver.derive(0.5f));
                if (startNode.flag == NODE_FLAG_REGULAR) {
                    g2d.setColor(Color.WHITE);
                } else {
                    g2d.setColor(colourNodeSubprio);
                }
                g2d.fillArc((int) (firstSecondaryNodePos.getX() - nodeSizeScaledHalf), (int) (firstSecondaryNodePos.getY() - nodeSizeScaledHalf), (int) nodeSizeScaled, (int) nodeSizeScaled, 0, 360);
                g2d.setClip(oldClip);
                g2d.dispose();

                if ( j < secondaryLinearLineNodeList.size() - 1) {
                    MapNode secondSecondPos = secondaryLinearLineNodeList.get(j+1);
                    Point2D secondSecondNodePos = worldPosToScreenPos(secondSecondPos.x, secondSecondPos.z);

                    // select the colour of line to drawToScreen

                    if ( connectionType == CONNECTION_STANDARD) {
                        if (secondSecondPos.flag ==NODE_FLAG_SUBPRIO) colour = colourConnectSubprio;
                    } else if ( connectionType == CONNECTION_DUAL ) {
                        if (secondSecondPos.flag == NODE_FLAG_REGULAR) {
                            colour = colourConnectDual;
                        } else {
                            colour = colourConnectDualSubprio;
                        }
                    } else if ( connectionType == CONNECTION_REVERSE ) {
                        if (secondSecondPos.flag == NODE_FLAG_REGULAR) {
                            colour = colourConnectReverse;
                        } else {
                            colour = colourConnectReverseSubprio;
                        }
                    }

                    //g.setColor(colour);
                    drawArrowBetween(g, firstSecondaryNodePos, secondSecondNodePos, connectionType == CONNECTION_DUAL, colour, false);
                }
            }
        }
    }
}
