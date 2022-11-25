package AutoDriveEditor.GUI.Buttons.Alignment;

import AutoDriveEditor.GUI.Buttons.AlignBaseButton;
import AutoDriveEditor.RoadNetwork.MapNode;

import javax.swing.*;

import static AutoDriveEditor.AutoDriveEditor.changeManager;
import static AutoDriveEditor.Locale.LocaleManager.getLocaleString;
import static AutoDriveEditor.Managers.MultiSelectManager.multiSelectList;
import static AutoDriveEditor.Utils.GUIUtils.makeImageToggleButton;
import static AutoDriveEditor.Utils.LoggerUtils.LOG;

public class VerticalAlignButton extends AlignBaseButton {

    public VerticalAlignButton(JPanel panel) {
        button = makeImageToggleButton("buttons/verticalalign","buttons/verticalalign_selected", null,"align_vertical_tooltip","align_vertical_alt", panel, false, false, null, false, this);
    }

    @Override
    public String getButtonID() { return "VerticalAlignButton"; }

    @Override
    public String getButtonAction() { return "ActionButton"; }

    @Override
    public String getButtonPanel() { return "Alignment"; }

    @Override
    public String getInfoText() { return getLocaleString("align_vertical_tooltip"); }

    @Override
    protected void adjustNodesTo(MapNode toNode) {
        LOG.info("Vertically Aligning {} nodes at world X coordinate {}",multiSelectList.size(), toNode.x);
        changeManager.addChangeable( new AlignmentChanger(multiSelectList, toNode.x, 0, 0));
        for (MapNode node : multiSelectList) {
            node.x = toNode.x;
        }
    }
}
