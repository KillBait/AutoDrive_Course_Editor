package AutoDriveEditor.GUI.Buttons.Nodes;

import AutoDriveEditor.GUI.Buttons.LinerLineBaseButton;

import javax.swing.*;
import java.awt.event.MouseEvent;
import java.awt.event.MouseListener;

import static AutoDriveEditor.Locale.LocaleManager.getLocaleString;
import static AutoDriveEditor.Utils.GUIUtils.makeStateChangeImageToggleButton;
import static AutoDriveEditor.Utils.ImageUtils.getImageIcon;

public class ReverseConnectionButton extends LinerLineBaseButton implements MouseListener {

    private final ImageIcon reverseConnectionIcon;
    private final ImageIcon reverseConnectionSelectedIcon;
    private final ImageIcon reverseConnectionSubPrioIcon;
    private final ImageIcon reverseConnectionSubPrioSelectedIcon;

    public ReverseConnectionButton(JPanel panel) {
        reverseConnectionIcon = getImageIcon("editor/buttons/connectreverse.png");
        reverseConnectionSelectedIcon = getImageIcon("editor/buttons/connectreverse_selected.png");
        reverseConnectionSubPrioIcon = getImageIcon("editor/buttons/connectreverse_subprio.png");
        reverseConnectionSubPrioSelectedIcon = getImageIcon("editor/buttons/connectreverse_subprio_selected.png");

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

    @Override
    public void mouseEntered(MouseEvent e) {}

    @Override
    public void mouseExited(MouseEvent e) {}
}
