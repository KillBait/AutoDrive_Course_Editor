package AutoDriveEditor.GUI.Buttons.Toolbar.Display;

import AutoDriveEditor.Classes.UI_Components.ScaleAnimIcon;
import AutoDriveEditor.GUI.Buttons.ConnectionSelectBaseButton;
import AutoDriveEditor.RoadNetwork.Connection;

import javax.swing.*;
import java.awt.event.MouseEvent;

import static AutoDriveEditor.AutoDriveEditor.getMapPanel;
import static AutoDriveEditor.Locale.LocaleManager.getLocaleString;
import static AutoDriveEditor.Managers.IconManager.*;

public class HideReverseConnectionsButton extends ConnectionSelectBaseButton {

    public HideReverseConnectionsButton(JPanel panel) {
        ScaleAnimIcon animHideReverseConnectionIcon = createScaleAnimIcon(BUTTON_VISIBILITY_CONNECT_REVERSE_ICON, false);
        button = createAnimToggleButton(animHideReverseConnectionIcon, panel, null, null,  false, false, this);
    }

    @Override
    public String getButtonID() { return "HideReverseConnectionsButton"; }

    @Override
    public String getButtonAction() { return "ActionButton"; }

    @Override
    public String getButtonPanel() { return "Display"; }

    @Override
    public String getInfoText() {
        String current = getLocaleString("toolbar_nodes_connection_type_reverse");
        return getLocaleString("toolbar_display_infotext").replace("{current}", current);

    }

    @Override
    public Integer getLineDetectionInterval() { return 20; }

    @Override
    public Boolean previewConnectionHiddenChange() { return true; }

    @Override
    public void mouseClicked(MouseEvent e) {
        if (e.getButton() == MouseEvent.BUTTON3) {
            resetHiddenStatusForConnectionType(Connection.ConnectionType.REVERSE);
            getMapPanel().repaint();
        }
    }

    @Override
    public void onButtonSelect() {
        connectionTypeFilter = Connection.ConnectionType.REVERSE;
    }

//    @Override
//    protected void filterConnections() {
//        Iterator<Connection> iterator = connectionsList.iterator();
//        while (iterator.hasNext()) {
//            Connection connection = iterator.next();
//            if (connection.getConnectionType() != Connection.ConnectionType.REVERSE) {
//                iterator.remove();
//                continue;
//            }
//            connection.getStartNode().getIgnoreDrawingConnectionsList().add(connection.getEndNode());
//        }
//    }

    @Override
    public String buildToolTip() {
        String current = getLocaleString("toolbar_nodes_connection_type_reverse");
        return getLocaleString("toolbar_display_tooltip").replace("{current}", current);
    }
}
