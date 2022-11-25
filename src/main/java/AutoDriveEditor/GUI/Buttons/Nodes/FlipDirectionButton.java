package AutoDriveEditor.GUI.Buttons.Nodes;

import AutoDriveEditor.GUI.Buttons.BaseButton;
import AutoDriveEditor.RoadNetwork.MapNode;
import AutoDriveEditor.RoadNetwork.RoadMap;

import javax.swing.*;
import java.awt.event.MouseEvent;
import java.util.ArrayList;

import static AutoDriveEditor.Locale.LocaleManager.getLocaleString;
import static AutoDriveEditor.Managers.MultiSelectManager.*;
import static AutoDriveEditor.MapPanel.MapPanel.*;
import static AutoDriveEditor.Utils.GUIUtils.makeImageToggleButton;
import static AutoDriveEditor.Utils.LoggerUtils.LOG;

public final class FlipDirectionButton extends BaseButton {

    public FlipDirectionButton(JPanel panel) {
        button = makeImageToggleButton("buttons/flip","buttons/flip_selected", null,"nodes_flip_tooltip","nodes_flip_alt", panel, false, false,  null, false, this);
    }

    @Override
    public String getButtonID() { return "FlipDirectionButton"; }

    @Override
    public String getButtonAction() { return "ActionButton"; }

    @Override
    public String getButtonPanel() { return "Nodes"; }

    @Override
    public String getInfoText() { return getLocaleString("nodes_flip_tooltip"); }

    @Override
    public void mousePressed(MouseEvent e) {
        if (e.getButton() == MouseEvent.BUTTON3) {
            startMultiSelect(e.getX(), e.getY());
        }
    }

    @Override
    public void mouseReleased(MouseEvent e) {
        if (e.getButton() == MouseEvent.BUTTON3) {
            stopMultiSelect(e.getX(), e.getY());
            int result = getAllNodesInSelectedArea(rectangleStart, rectangleEnd);
            if (result > 0) flipNodeDirection();
        }
    }

    @Override
    public void mouseDragged(MouseEvent e) {
        getMapPanel().repaint();
    }

    private static class Connection {
        MapNode fromNode;
        MapNode toNode;
        int conType;

        private Connection(MapNode from, MapNode to, int type) {
            fromNode = from;
            toNode = to;
            conType = type;
        }
    }

    private void flipNodeDirection() {
        ArrayList<Connection> connectionList = new ArrayList<>();

        for (MapNode selectedListNode : multiSelectList) {
            for (MapNode outgoingList : selectedListNode.outgoing) {
                if (RoadMap.isDual(selectedListNode, outgoingList)) {
                    LOG.info("Detected dual Connection");
                    connectionList.add(new Connection(selectedListNode, outgoingList, CONNECTION_DUAL));
                } else if (RoadMap.isReverse(selectedListNode, outgoingList)) {
                    LOG.info("Detected reverse connection");
                    connectionList.add(new Connection(selectedListNode, outgoingList, CONNECTION_REVERSE));
                } else {
                    LOG.info("Detected Regular Connection");
                    connectionList.add(new Connection(selectedListNode, outgoingList, CONNECTION_STANDARD));
                }
            }
        }
        for (Connection connection : connectionList) {
            LOG.info("{} --> {} = {}", connection.fromNode.id, connection.toNode.id, connection.conType);
            createConnectionBetween(connection.fromNode, connection.toNode, connection.conType);
            createConnectionBetween(connection.toNode, connection.fromNode, connection.conType);
        }
        clearMultiSelection();
    }
}
