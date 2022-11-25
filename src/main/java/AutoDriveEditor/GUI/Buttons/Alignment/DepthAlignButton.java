package AutoDriveEditor.GUI.Buttons.Alignment;

import AutoDriveEditor.GUI.Buttons.AlignBaseButton;
import AutoDriveEditor.RoadNetwork.MapNode;

import javax.swing.*;

import static AutoDriveEditor.AutoDriveEditor.changeManager;
import static AutoDriveEditor.Locale.LocaleManager.getLocaleString;
import static AutoDriveEditor.Managers.MultiSelectManager.multiSelectList;
import static AutoDriveEditor.Utils.GUIUtils.makeImageToggleButton;
import static AutoDriveEditor.Utils.LoggerUtils.LOG;

public class DepthAlignButton extends AlignBaseButton {

    public DepthAlignButton(JPanel panel) {
        button = makeImageToggleButton("buttons/depthalign","buttons/depthalign_selected", null,"align_depth_tooltip","align_depth_alt", panel, false, false, null, false, this);
    }

    @Override
    public String getButtonID() { return "DepthAlignButton"; }

    @Override
    public String getButtonAction() { return "ActionButton"; }

    @Override
    public String getButtonPanel() { return "Alignment"; }

    @Override
    public String getInfoText() { return getLocaleString("align_depth_tooltip"); }

    @Override
    protected void adjustNodesTo(MapNode toNode) {
        LOG.info("Depth Aligning {} nodes at world y coordinate {}",multiSelectList.size(), toNode.y);
        changeManager.addChangeable( new AlignmentChanger(multiSelectList, 0, toNode.y, 0));
        for (MapNode node : multiSelectList) {
            node.y = toNode.y;
        }
    }
}
