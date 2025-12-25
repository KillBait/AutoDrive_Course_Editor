package AutoDriveEditor.GUI.Buttons.Toolbar.Display;

import AutoDriveEditor.Classes.UI_Components.ScaleAnimIcon;
import AutoDriveEditor.GUI.Buttons.ConnectionSelectBaseButton;
import AutoDriveEditor.RoadNetwork.Connection;

import javax.swing.*;
import java.awt.event.MouseEvent;

import static AutoDriveEditor.AutoDriveEditor.getMapPanel;
import static AutoDriveEditor.Locale.LocaleManager.getLocaleString;
import static AutoDriveEditor.Managers.IconManager.*;

public class HideRegularConnectionsButton extends ConnectionSelectBaseButton {

    public HideRegularConnectionsButton(JPanel panel) {
        ScaleAnimIcon animHideRegularConnectionIcon = createScaleAnimIcon(BUTTON_VISIBILITY_CONNECT_REGULAR_ICON, false);
        button = createAnimToggleButton(animHideRegularConnectionIcon, panel, null, null,  false, false, this);
    }

    @Override
    public String getButtonID() { return "HideRegularConnectionsButton"; }

    @Override
    public String getButtonAction() { return "ActionButton"; }

    @Override
    public String getButtonPanel() { return "Display"; }

    @Override
    public String getInfoText() {
        String current = getLocaleString("toolbar_nodes_connection_type_regular");
        return getLocaleString("toolbar_display_infotext").replace("{current}", current);
    }

    @Override
    public Boolean previewConnectionHiddenChange() { return true; }

//    @Override
//    protected void filterConnections() {
//        Iterator<Connection> iterator = connectionsList.iterator();
//        while (iterator.hasNext()) {
//            Connection connection = iterator.next();
//            if (connection.getConnectionType() != Connection.ConnectionType.REGULAR) {
//                iterator.remove();
//                continue;
//            }
//            connection.getStartNode().getPreviewConnectionHiddenList().add(connection.getEndNode());
//        }
//    }

    @Override
    public void onButtonSelect() {
        connectionTypeFilter = Connection.ConnectionType.REGULAR;
    }

    @Override
    public void mouseClicked(MouseEvent e) {
        if (e.getButton() == MouseEvent.BUTTON3) {
            resetHiddenStatusForConnectionType(Connection.ConnectionType.REGULAR);
            getMapPanel().repaint();
        }
    }

    @Override
    public String buildToolTip() {
        String current = getLocaleString("toolbar_nodes_connection_type_regular");
        return getLocaleString("toolbar_display_tooltip").replace("{current}", current);
    }
}
