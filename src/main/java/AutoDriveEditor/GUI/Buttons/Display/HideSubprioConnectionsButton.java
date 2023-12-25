package AutoDriveEditor.GUI.Buttons.Display;

import AutoDriveEditor.GUI.Buttons.ConnectionSelectBaseButton;

import javax.swing.*;
import java.awt.event.MouseEvent;

import static AutoDriveEditor.AutoDriveEditor.getMapPanel;
import static AutoDriveEditor.GUI.Buttons.LinerLineBaseButton.CONNECTION_STANDARD_SUBPRIO;
import static AutoDriveEditor.Locale.LocaleManager.getLocaleString;
import static AutoDriveEditor.Utils.GUIUtils.makeImageToggleButton;

public class HideSubprioConnectionsButton extends ConnectionSelectBaseButton {

    public HideSubprioConnectionsButton(JPanel panel) {
        button = makeImageToggleButton("buttons/hide_connect_subprio","buttons/hide_connect_subprio_selected", null,"display_toggle_connect_subprio_tooltip","display_toggle_connect_subprio_alt", panel, false, false, null, false, this);
    }

    @Override
    public String getButtonID() { return "HideSubprioConnectionsButton"; }

    @Override
    public String getButtonAction() { return "ActionButton"; }

    @Override
    public String getButtonPanel() { return "Display"; }

    @Override
    public String getInfoText() { return getLocaleString("display_toggle_connect_subprio_tooltip"); }


    @Override
    public boolean detectRegularConnections() { return false; }
    @Override
    public boolean detectReverseConnections() { return false; }
    @Override
    public boolean detectDualConnections() { return false; }

    @Override
    public Integer getLineDetectionInterval() { return 20; }

    @Override
    public Boolean previewConnectionHiddenChange() { return true; }

    @Override
    public void mouseClicked(MouseEvent e) {
        if (e.getButton() == MouseEvent.BUTTON3) {
            resetHiddenStatusForAll(CONNECTION_STANDARD_SUBPRIO);
            getMapPanel().repaint();
        }
    }
}
