package AutoDriveEditor.GUI.Buttons.Options;

import AutoDriveEditor.GUI.Buttons.OptionsBaseButton;

import javax.swing.*;
import java.awt.event.ActionEvent;

import static AutoDriveEditor.MapPanel.MapImage.updateNodeSizeTo;
import static AutoDriveEditor.Utils.GUIUtils.makeImageButton;
import static AutoDriveEditor.XMLConfig.EditorXML.nodeSize;

public class NodeSizeDownButton extends OptionsBaseButton {

    public NodeSizeDownButton(JPanel panel) {
        button = makeImageButton("buttons/nodeminus", "buttons/nodeminus_selected", null,"options_node_size_minus_tooltip","options_node_size_minus_alt", panel, false, this);
    }

    @Override
    public String getButtonID() { return "NodeSizeDownButton"; }

    @Override
    public void actionPerformed(ActionEvent e) {
        super.actionPerformed(e);
        if (nodeSize >= 0.11) updateNodeSizeTo(nodeSize - 0.10f);
    }
}
