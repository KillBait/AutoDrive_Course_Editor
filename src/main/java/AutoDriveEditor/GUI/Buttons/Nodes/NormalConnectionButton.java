package AutoDriveEditor.GUI.Buttons.Nodes;

import AutoDriveEditor.GUI.Buttons.LinerLineBaseButton;

import javax.swing.*;
import java.awt.event.MouseListener;

import static AutoDriveEditor.Locale.LocaleManager.getLocaleString;
import static AutoDriveEditor.Utils.GUIUtils.makeStateChangeImageToggleButton;
import static AutoDriveEditor.Utils.ImageUtils.loadImageIcon;

public class NormalConnectionButton extends LinerLineBaseButton implements MouseListener {

    private final ImageIcon normalConnectionIcon;
    private final ImageIcon normalConnectionSelectedIcon;
    private final ImageIcon normalConnectionSubPrioIcon;
    private final ImageIcon normalConnectionSubPrioSelectedIcon;


    public NormalConnectionButton(JPanel panel) {
        normalConnectionIcon = loadImageIcon("editor/buttons/connectregular.png");
        normalConnectionSelectedIcon = loadImageIcon("editor/buttons/connectregular_selected.png");
        normalConnectionSubPrioIcon = loadImageIcon("editor/buttons/connectregular_subprio.png");
        normalConnectionSubPrioSelectedIcon = loadImageIcon("editor/buttons/connectregular_subprio_selected.png");

        button = makeStateChangeImageToggleButton("buttons/connectregular", "buttons/connectregular_selected", null,"nodes_create_regular_connection_tooltip","nodes_create_regular_connection_alt", panel, false, false, null, false, this);
        //add Mouse Listener to detect right click on button to change between normal/subprio nodes
        button.addMouseListener(this);
        connectionType = CONNECTION_STANDARD;
    }

    @Override
    public String getButtonID() { return "NormalConnectionButton"; }

    @Override
    public String getButtonAction() { return "ActionButton"; }

    @Override
    public String getButtonPanel() { return "Nodes"; }

    @Override
    public String getInfoText() { return getLocaleString("nodes_create_regular_connection_tooltip"); }

    @Override
    protected void setNormalStateIcons() {
        button.setIcon(normalConnectionIcon);
        button.setSelectedIcon(normalConnectionSelectedIcon);
    }

    @Override
    protected void setAlternateStateIcons() {
        button.setIcon(normalConnectionSubPrioIcon);
        button.setSelectedIcon(normalConnectionSubPrioSelectedIcon);
    }
}
