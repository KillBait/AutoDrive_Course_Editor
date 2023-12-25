package AutoDriveEditor.GUI.Buttons.Options;

import AutoDriveEditor.GUI.Buttons.OptionsBaseButton;

import javax.swing.*;

import static AutoDriveEditor.Utils.GUIUtils.makeImageButton;
import static AutoDriveEditor.XMLConfig.EditorXML.nodeSize;
import static AutoDriveEditor.XMLConfig.EditorXML.setNewNodeSize;

public class NodeSizeDownButton extends OptionsBaseButton {

    public NodeSizeDownButton(JPanel panel) {
        button = makeImageButton("buttons/nodeminus", "buttons/nodeminus_selected", null,"options_node_size_minus_tooltip","options_node_size_minus_alt", panel, false, this);
        Timer nodeDownButtonTimer = new Timer(75, e -> {
            if (nodeSize > 0.1) setNewNodeSize(nodeSize - 0.10f);
        });
        nodeDownButtonTimer.setCoalesce(true);
        nodeDownButtonTimer.setRepeats(true);

        button.getModel().addChangeListener(e -> {
            if (button.getModel().isPressed()) {
                nodeDownButtonTimer.start();
            } else {
                nodeDownButtonTimer.stop();
            }
        });
    }

    @Override
    public String getButtonID() { return "NodeSizeDownButton"; }
}
