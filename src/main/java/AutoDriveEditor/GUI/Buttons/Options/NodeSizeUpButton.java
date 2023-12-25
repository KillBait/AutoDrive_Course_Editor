package AutoDriveEditor.GUI.Buttons.Options;

import AutoDriveEditor.GUI.Buttons.OptionsBaseButton;

import javax.swing.*;

import static AutoDriveEditor.Utils.GUIUtils.makeImageButton;
import static AutoDriveEditor.XMLConfig.EditorXML.nodeSize;
import static AutoDriveEditor.XMLConfig.EditorXML.setNewNodeSize;

public class NodeSizeUpButton extends OptionsBaseButton {

    public NodeSizeUpButton(JPanel panel) {
        button = makeImageButton("buttons/nodeplus", "buttons/nodeplus_selected", null,"options_node_size_plus_tooltip","options_node_size_plus_alt", panel, false, this);
        Timer nodeUpButtonTimer = new Timer(75, e -> setNewNodeSize(nodeSize + 0.10f));
        nodeUpButtonTimer.setCoalesce(true);
        nodeUpButtonTimer.setRepeats(true);

        button.getModel().addChangeListener(e -> {
            if (button.getModel().isPressed()) {
                nodeUpButtonTimer.start();
            } else {
                nodeUpButtonTimer.stop();
            }
        });
    }

    @Override
    public String getButtonID() { return "NodeSizeUpButton"; }
}
