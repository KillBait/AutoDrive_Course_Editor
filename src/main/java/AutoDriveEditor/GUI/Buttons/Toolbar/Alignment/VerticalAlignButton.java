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
import static AutoDriveEditor.Managers.ShortcutManager.ShortcutID.ALIGN_NODES_VERTICALLY_SHORTCUT;
import static AutoDriveEditor.Managers.ShortcutManager.getUserShortcutByID;
import static AutoDriveEditor.Managers.ShortcutManager.registerShortcut;

public class VerticalAlignButton extends AlignBaseButton {

    public VerticalAlignButton(JPanel panel) {
        ScaleAnimIcon animVerticalAlignIcon = createScaleAnimIcon(BUTTON_ALIGN_VERTICAL_ICON, false);
        button = createAnimToggleButton(animVerticalAlignIcon, panel, null, null,  false, false, this);
        direction = ALIGN_VERTICAL;

        // Setup Keyboard Shortcut
        Shortcut verticalAlignShortcut = getUserShortcutByID(ALIGN_NODES_VERTICALLY_SHORTCUT);
        if (verticalAlignShortcut != null) {
            Action verticalAlignButtonAction = new AbstractAction() {
                @Override
                public void actionPerformed(ActionEvent e) {
                    if (button.isEnabled() && !button.isSelected()) {
                        buttonManager.makeCurrent(buttonNode);
                    } else {
                        buttonManager.deSelectAll();
                    }
                }
            };
            registerShortcut(this, verticalAlignShortcut, verticalAlignButtonAction, getMapPanel());
        }
    }

    @Override
    public String getButtonID() { return "VerticalAlignButton"; }

    @Override
    public String getButtonAction() { return "ActionButton"; }

    @Override
    public String getButtonPanel() { return "Alignment"; }

    @Override
    public String getInfoText() { return getLocaleString("toolbar_align_infotext"); }

    @Override
    protected void adjustNodesTo(MapNode toNode) {
        LOG.info("Vertically Aligning {} nodes at world X coordinate {}",multiSelectList.size(), toNode.x);
        changeManager.addChangeable( new AlignmentChanger(multiSelectList, toNode.x, 0, 0));
        for (MapNode node : multiSelectList) {
            node.x = toNode.x;
            checkNodeOverlap(node);
        }
    }

    @Override
    public String buildToolTip() {
        Shortcut s = ShortcutManager.getUserShortcutByID(ALIGN_NODES_VERTICALLY_SHORTCUT);
        if (s != null) {
            return getLocaleString("toolbar_align_vertical_tooltip") + "\n\n( Shortcut: " + s.getShortcutString() + " )";
        } else {
            return getLocaleString("toolbar_align_vertical_tooltip");
        }
    }
}
