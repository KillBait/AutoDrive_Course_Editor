package AutoDriveEditor.GUI.Buttons.Nodes;

import AutoDriveEditor.GUI.Buttons.LinerLineBaseButton;

import javax.swing.*;
import java.awt.event.MouseListener;

import static AutoDriveEditor.Locale.LocaleManager.getLocaleString;
import static AutoDriveEditor.Utils.GUIUtils.makeStateChangeImageToggleButton;
import static AutoDriveEditor.Utils.ImageUtils.loadImageIcon;

public class ReverseConnectionButton extends LinerLineBaseButton implements MouseListener {

    private final ImageIcon reverseConnectionIcon;
    private final ImageIcon reverseConnectionSelectedIcon;
    private final ImageIcon reverseConnectionSubPrioIcon;
    private final ImageIcon reverseConnectionSubPrioSelectedIcon;

    public ReverseConnectionButton(JPanel panel) {
        reverseConnectionIcon = loadImageIcon("editor/buttons/connectreverse.png");
        reverseConnectionSelectedIcon = loadImageIcon("editor/buttons/connectreverse_selected.png");
        reverseConnectionSubPrioIcon = loadImageIcon("editor/buttons/connectreverse_subprio.png");
        reverseConnectionSubPrioSelectedIcon = loadImageIcon("editor/buttons/connectreverse_subprio_selected.png");

        button = makeStateChangeImageToggleButton("buttons/connectreverse","buttons/connectreverse_selected", null, "nodes_create_reverse_connection_tooltip", "nodes_create_reverse_connection_alt", panel, false, false, null, false, this);
        button.addMouseListener(this);
        connectionType = CONNECTION_REVERSE;
    }

    @Override
    public String getButtonID() { return "ReverseConnectionButton"; }

    @Override
    public String getButtonAction() { return "ActionButton"; }

    @Override
    public String getButtonPanel() { return "Nodes"; }

    @Override
    public String getInfoText() { return getLocaleString("nodes_create_reverse_connection_tooltip"); }

    @Override
    protected void setNormalStateIcons() {
        button.setIcon(reverseConnectionIcon);
        button.setSelectedIcon(reverseConnectionSelectedIcon);
    }

    @Override
    protected void setAlternateStateIcons() {
        button.setIcon(reverseConnectionSubPrioIcon);
        button.setSelectedIcon(reverseConnectionSubPrioSelectedIcon);
    }
}
