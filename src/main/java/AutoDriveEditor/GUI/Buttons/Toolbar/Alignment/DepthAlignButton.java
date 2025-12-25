package AutoDriveEditor.GUI.Buttons.Toolbar.Alignment;

import AutoDriveEditor.Classes.UI_Components.ScaleAnimIcon;
import AutoDriveEditor.GUI.Buttons.AlignBaseButton;
import AutoDriveEditor.RoadNetwork.MapNode;

import javax.swing.*;

import static AutoDriveEditor.AutoDriveEditor.changeManager;
import static AutoDriveEditor.AutoDriveEditor.getMapPanel;
import static AutoDriveEditor.Classes.Util_Classes.LoggerUtils.LOG;
import static AutoDriveEditor.GUI.MapPanel.setStale;
import static AutoDriveEditor.Locale.LocaleManager.getLocaleString;
import static AutoDriveEditor.Managers.IconManager.*;
import static AutoDriveEditor.Managers.MultiSelectManager.multiSelectList;
import static AutoDriveEditor.XMLConfig.AutoSave.resumeAutoSaving;
import static AutoDriveEditor.XMLConfig.AutoSave.suspendAutoSaving;

public class DepthAlignButton extends AlignBaseButton {

    public DepthAlignButton(JPanel panel) {
        ScaleAnimIcon animTerrainAlignIcon = createScaleAnimIcon(BUTTON_ALIGN_TERRAIN_ICON, false);
        button = createAnimToggleButton(animTerrainAlignIcon, panel, null, null,  false, false, this);
        direction = ALIGN_NONE; // Set direction to depth alignment

    }

    @Override
    public String getButtonID() { return "DepthAlignButton"; }

    @Override
    public String getButtonAction() { return "ActionButton"; }

    @Override
    public String getButtonPanel() { return "Alignment"; }

    @Override
    public String getInfoText() { return getLocaleString("toolbar_align_infotext"); }

    @Override
    protected void adjustNodesTo(MapNode toNode) {
        LOG.info("Depth Aligning {} nodes at world y coordinate {}",multiSelectList.size(), toNode.y);
        changeManager.addChangeable( new AlignmentChanger(multiSelectList, 0, toNode.y, 0));
        suspendAutoSaving();
        for (MapNode node : multiSelectList) {
            node.y = toNode.y;
        }
        setStale(true);
        resumeAutoSaving();
        getMapPanel().repaint();
    }

    @Override
    public String buildToolTip() {
        return getLocaleString("toolbar_align_depth_tooltip");
    }
}
