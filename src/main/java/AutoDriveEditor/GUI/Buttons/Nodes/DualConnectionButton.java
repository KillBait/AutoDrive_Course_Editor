package AutoDriveEditor.GUI.Buttons.Nodes;

import AutoDriveEditor.GUI.Buttons.LinerLineBaseButton;

import javax.swing.*;
import java.awt.event.MouseListener;

import static AutoDriveEditor.Locale.LocaleManager.getLocaleString;
import static AutoDriveEditor.Utils.GUIUtils.makeStateChangeImageToggleButton;
import static AutoDriveEditor.Utils.ImageUtils.loadImageIcon;

public class DualConnectionButton extends LinerLineBaseButton implements MouseListener {

    private final ImageIcon dualConnectionIcon;
    private final ImageIcon dualConnectionSelectedIcon;
    private final ImageIcon dualConnectionSubPrioIcon;
    private final ImageIcon dualConnectionSubPrioSelectedIcon;


    public DualConnectionButton(JPanel panel) {
        dualConnectionIcon = loadImageIcon("editor/buttons/connectdual.png");
        dualConnectionSelectedIcon = loadImageIcon("editor/buttons/connectdual_selected.png");
        dualConnectionSubPrioIcon = loadImageIcon("editor/buttons/connectdual_subprio.png");
        dualConnectionSubPrioSelectedIcon = loadImageIcon("editor/buttons/connectdual_subprio_selected.png");

        button = makeStateChangeImageToggleButton("buttons/connectdual","buttons/connectdual_selected", null, "nodes_create_dual_connection_tooltip", "nodes_create_dual_connection_alt", panel, false, false, null, false, this);
        //add Mouse Listener to detect right click on button to change between normal/subprio nodes
        button.addMouseListener(this);
        connectionType = CONNECTION_DUAL;
    }

    @Override
    public String getButtonID() { return "DualConnectionButton"; }

    @Override
    public String getButtonAction() { return "ActionButton"; }

    @Override
    public String getButtonPanel() { return "Nodes"; }

    @Override
    public String getInfoText() { return getLocaleString("nodes_create_dual_connection_tooltip"); }

    @Override
    protected void setNormalStateIcons() {
        button.setIcon(dualConnectionIcon);
        button.setSelectedIcon(dualConnectionSelectedIcon);
    }

    @Override
    protected void setAlternateStateIcons() {
        button.setIcon(dualConnectionSubPrioIcon);
        button.setSelectedIcon(dualConnectionSubPrioSelectedIcon);
    }
}
