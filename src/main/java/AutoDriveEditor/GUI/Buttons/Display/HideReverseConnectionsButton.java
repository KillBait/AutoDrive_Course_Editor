package AutoDriveEditor.GUI.Buttons.Display;

import AutoDriveEditor.GUI.Buttons.ConnectionSelectBaseButton;

import javax.swing.*;
import java.awt.event.MouseEvent;

import static AutoDriveEditor.AutoDriveEditor.getMapPanel;
import static AutoDriveEditor.GUI.Buttons.LinerLineBaseButton.CONNECTION_REVERSE;
import static AutoDriveEditor.Locale.LocaleManager.getLocaleString;
import static AutoDriveEditor.Utils.GUIUtils.makeImageToggleButton;

public class HideReverseConnectionsButton extends ConnectionSelectBaseButton {

    public HideReverseConnectionsButton(JPanel panel) {
        button = makeImageToggleButton("buttons/hide_connect_reverse","buttons/hide_connect_reverse_selected", null,"display_toggle_connect_reverse_tooltip","display_toggle_connect_reverse_alt", panel, false, false, null, false, this);
    }

    @Override
    public String getButtonID() { return "HideReverseConnectionsButton"; }

    @Override
    public String getButtonAction() { return "ActionButton"; }

    @Override
    public String getButtonPanel() { return "Display"; }

    @Override
    public String getInfoText() { return getLocaleString("display_toggle_connect_reverse_tooltip"); }

    @Override
    public boolean detectRegularConnections() { return false; }
    @Override
    public boolean detectSubprioConnections() { return false; }
    @Override
    public boolean detectDualConnections() { return false; }

    @Override
    public Integer getLineDetectionInterval() { return 20; }

    @Override
    public Boolean previewConnectionHiddenChange() { return true; }

    @Override
    public void mouseClicked(MouseEvent e) {
        if (e.getButton() == MouseEvent.BUTTON3) {
            resetHiddenStatusForAll(CONNECTION_REVERSE);
            getMapPanel().repaint();
        }
    }
}
