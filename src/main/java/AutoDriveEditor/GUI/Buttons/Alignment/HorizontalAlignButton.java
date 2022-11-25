package AutoDriveEditor.GUI.Buttons.Alignment;

import AutoDriveEditor.GUI.Buttons.AlignBaseButton;
import AutoDriveEditor.RoadNetwork.MapNode;

import javax.swing.*;

import static AutoDriveEditor.AutoDriveEditor.changeManager;
import static AutoDriveEditor.Locale.LocaleManager.getLocaleString;
import static AutoDriveEditor.Managers.MultiSelectManager.multiSelectList;
import static AutoDriveEditor.Utils.GUIUtils.makeImageToggleButton;
import static AutoDriveEditor.Utils.LoggerUtils.LOG;

public class HorizontalAlignButton extends AlignBaseButton {

    public HorizontalAlignButton(JPanel panel) {
        button = makeImageToggleButton("buttons/horizontalalign","buttons/horizontalalign_selected", null,"align_horizontal_tooltip","align_horizontal_alt", panel, false, false, null, false, this);
    }

    @Override
    public String getButtonID() { return "HorizontalAlignButton"; }

    @Override
    public String getButtonAction() { return "ActionButton"; }

    @Override
    public String getButtonPanel() { return "Alignment"; }

    @Override
    public String getInfoText() { return getLocaleString("align_horizontal_tooltip"); }

    @Override
    protected void adjustNodesTo(MapNode toNode) {
        LOG.info("Horizontally Aligning {} nodes at world Z coordinate {}",multiSelectList.size(), toNode.z);
        changeManager.addChangeable( new AlignmentChanger(multiSelectList, 0, 0, toNode.z));
        for (MapNode node : multiSelectList) {
            node.z = toNode.z;
        }
    }
}
