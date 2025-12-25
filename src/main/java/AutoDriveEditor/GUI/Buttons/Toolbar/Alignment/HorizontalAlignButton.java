package AutoDriveEditor.GUI.Buttons.Toolbar.Alignment;

import AutoDriveEditor.Classes.KeyBinds.Shortcut;
import AutoDriveEditor.Classes.UI_Components.ScaleAnimIcon;
import AutoDriveEditor.GUI.Buttons.AlignBaseButton;
import AutoDriveEditor.Managers.ShortcutManager;
import AutoDriveEditor.RoadNetwork.MapNode;

import javax.swing.*;
import java.awt.event.ActionEvent;

import static AutoDriveEditor.AutoDriveEditor.*;
import static AutoDriveEditor.Classes.Util_Classes.LoggerUtils.LOG;
import static AutoDriveEditor.Locale.LocaleManager.getLocaleString;
import static AutoDriveEditor.Managers.IconManager.*;
import static AutoDriveEditor.Managers.MultiSelectManager.multiSelectList;
import static AutoDriveEditor.Managers.ScanManager.checkNodeOverlap;
import static AutoDriveEditor.Managers.ShortcutManager.ShortcutID.ALIGN_NODES_HORIZONTALLY_SHORTCUT;
import static AutoDriveEditor.Managers.ShortcutManager.getUserShortcutByID;
import static AutoDriveEditor.Managers.ShortcutManager.registerShortcut;

public class HorizontalAlignButton extends AlignBaseButton {

    public HorizontalAlignButton(JPanel panel) {
        ScaleAnimIcon animHorizontalAlignIcon = createScaleAnimIcon(BUTTON_ALIGN_HORIZONTAL_ICON, false);
        button = createAnimToggleButton(animHorizontalAlignIcon, panel, null, null,  false, false, this);
        direction = ALIGN_HORIZONTAL;

        Shortcut horizontalAlignShortcut = getUserShortcutByID(ALIGN_NODES_HORIZONTALLY_SHORTCUT);
        if (horizontalAlignShortcut != null) {
            Action horizontalAlignButtonAction = new AbstractAction() {
                @Override
                public void actionPerformed(ActionEvent e) {
                    if (button.isEnabled() && !button.isSelected()) {
                        buttonManager.makeCurrent(buttonNode);
                    } else {
                        buttonManager.deSelectAll();
                    }
                }
            };
            registerShortcut(this, horizontalAlignShortcut, horizontalAlignButtonAction, getMapPanel());
        }
    }

    @Override
    public String getButtonID() { return "HorizontalAlignButton"; }

    @Override
    public String getButtonAction() { return "ActionButton"; }

    @Override
    public String getButtonPanel() { return "Alignment"; }

    @Override
    public String getInfoText() { return getLocaleString("toolbar_align_infotext"); }

    @Override
    protected void adjustNodesTo(MapNode toNode) {
        LOG.info("Horizontally Aligning {} nodes at world Z coordinate {}",multiSelectList.size(), toNode.z);
        changeManager.addChangeable( new AlignmentChanger(multiSelectList, 0, 0, toNode.z));
        for (MapNode node : multiSelectList) {
            node.z = toNode.z;
            checkNodeOverlap(node);
        }
    }

    @Override
    public String buildToolTip() {
        Shortcut s = ShortcutManager.getUserShortcutByID(ALIGN_NODES_HORIZONTALLY_SHORTCUT);
        if (s != null) {
            return getLocaleString("toolbar_align_horizontal_tooltip") + "\n\n( Shortcut: " + s.getShortcutString() + " )";
        } else {
            return getLocaleString("toolbar_align_horizontal_tooltip");
        }
    }
}
