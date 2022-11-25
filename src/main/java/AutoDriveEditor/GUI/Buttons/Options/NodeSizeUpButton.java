package AutoDriveEditor.GUI.Buttons.Options;

import AutoDriveEditor.GUI.Buttons.OptionsBaseButton;

import javax.swing.*;
import java.awt.event.ActionEvent;

import static AutoDriveEditor.MapPanel.MapImage.updateNodeSizeTo;
import static AutoDriveEditor.Utils.GUIUtils.makeImageButton;
import static AutoDriveEditor.XMLConfig.EditorXML.nodeSize;

public class NodeSizeUpButton extends OptionsBaseButton {

    public NodeSizeUpButton(JPanel panel) {
        button = makeImageButton("buttons/nodeplus", "buttons/nodeplus_selected", null,"options_node_size_plus_tooltip","options_node_size_plus_alt", panel, false, this);
    }

    @Override
    public String getButtonID() { return "NodeSizeUpButton"; }

    @Override
    public String getButtonAction() { return "OptionChange"; }

    @Override
    public String getButtonPanel() { return "Options"; }

    @Override
    public String getInfoText() { return null; }

    @Override
    public void actionPerformed(ActionEvent e) {
        super.actionPerformed(e);
        updateNodeSizeTo(nodeSize + 0.10f);
    }
}
