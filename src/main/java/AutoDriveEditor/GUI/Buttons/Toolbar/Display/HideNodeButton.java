package AutoDriveEditor.GUI.Buttons.Toolbar.Display;

import AutoDriveEditor.Classes.UI_Components.ScaleAnimIcon;
import AutoDriveEditor.GUI.Buttons.BaseButton;
import AutoDriveEditor.RoadNetwork.MapNode;
import AutoDriveEditor.RoadNetwork.RoadMap;

import javax.swing.*;
import java.awt.event.MouseEvent;

import static AutoDriveEditor.AutoDriveEditor.getMapPanel;
import static AutoDriveEditor.GUI.MapPanel.getNodeAtScreenPosition;
import static AutoDriveEditor.Locale.LocaleManager.getLocaleString;
import static AutoDriveEditor.Managers.IconManager.*;
import static AutoDriveEditor.Managers.MultiSelectManager.getSelectedNodes;

public class HideNodeButton extends BaseButton {

    public HideNodeButton(JPanel panel) {
        ScaleAnimIcon animHideNodeIcon = createScaleAnimIcon(BUTTON_VISIBILITY_NODE_ICON, false);
        button = createAnimToggleButton(animHideNodeIcon, panel, null, null,  false, false, this);
    }

    @Override
    public String getButtonID() { return "NodeHideButton"; }

    @Override
    public String getButtonAction() { return "ActionButton"; }

    @Override
    public String getButtonPanel() { return "Display"; }

    @Override
    public String getInfoText() { return getLocaleString("toolbar_display_nodes_infotext"); }

    @Override
    public Boolean showHoverNodeSelect() { return false; }

    @Override
    public Boolean useMultiSelection() { return true; }

    @Override
    public Boolean addSelectedToMultiSelectList() { return false; }

    @Override
    public Boolean alwaysSelectHidden() { return true; }

    @Override
    public Boolean previewNodeSelectionChange() { return false; }

    @Override
    public Boolean previewNodeHiddenChange() { return true; }


    @Override
    public void mouseReleased(MouseEvent e) {
        if (e.getButton() == MouseEvent.BUTTON3) {
            if (getSelectedNodes().size() >0 ) {
                for (MapNode nodeList: getSelectedNodes()) {
                    nodeList.setNodeHidden(!nodeList.isNodeHidden());
                }
                getMapPanel().repaint();
            }
        } else if (e.getButton() == MouseEvent.BUTTON1) {
            MapNode selectedNode = getNodeAtScreenPosition(e.getX(), e.getY());
            if (selectedNode != null) {
                selectedNode.setNodeHidden(!selectedNode.isNodeHidden());
                getMapPanel().repaint();
            }
        }
    }

    @Override
    public void mouseClicked(MouseEvent e) {
        if (e.getButton() == MouseEvent.BUTTON3) {
            for (MapNode node: RoadMap.networkNodesList) {
                node.setNodeHidden(false);
            }
            getMapPanel().repaint();
        }
    }

    @Override
    public String buildToolTip() {
        return getLocaleString("toolbar_display_nodes_tooltip");
    }
}
