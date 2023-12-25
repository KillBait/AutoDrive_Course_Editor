package AutoDriveEditor.GUI.Buttons.Nodes;

import AutoDriveEditor.GUI.Buttons.AddNodeBaseButton;
import AutoDriveEditor.RoadNetwork.MapNode;

import javax.swing.*;
import java.awt.event.MouseEvent;
import java.awt.geom.Point2D;

import static AutoDriveEditor.GUI.MapPanel.screenPosToWorldPos;
import static AutoDriveEditor.Locale.LocaleManager.getLocaleString;
import static AutoDriveEditor.Managers.ScanManager.checkAreaForNodeOverlap;
import static AutoDriveEditor.RoadNetwork.MapNode.NODE_FLAG_SUBPRIO;
import static AutoDriveEditor.Utils.GUIUtils.makeImageToggleButton;

public class AddSecondaryNodeButton extends AddNodeBaseButton {

    public AddSecondaryNodeButton(JPanel panel) {
        button = makeImageToggleButton("buttons/createsecondary","buttons/createsecondary_selected", null, "nodes_create_secondary_node_tooltip", "nodes_create_secondary_node_alt", panel, false, false, null, false, this);
    }

    @Override
    public String getButtonID() { return "AddSecondaryNodeButton"; }

    @Override
    public String getButtonAction() { return "ActionButton"; }

    @Override
    public String getButtonPanel() { return "Nodes"; }

    @Override
    public String getInfoText() { return getLocaleString("nodes_create_secondary_node_tooltip"); }

    @Override
    public void mouseReleased(MouseEvent e) {
        if (e.getButton() == MouseEvent.BUTTON1) {
            Point2D worldPos = screenPosToWorldPos(e.getX(), e.getY());
            MapNode newNode = createNode(worldPos.getX(), worldPos.getY(), NODE_FLAG_SUBPRIO);
            checkAreaForNodeOverlap(newNode);
        }
    }
}
