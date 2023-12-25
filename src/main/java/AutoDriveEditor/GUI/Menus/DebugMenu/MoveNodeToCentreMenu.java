package AutoDriveEditor.GUI.Menus.DebugMenu;

import AutoDriveEditor.GUI.Menus.JMenuItemBase;
import AutoDriveEditor.RoadNetwork.MapNode;
import AutoDriveEditor.RoadNetwork.RoadMap;
import AutoDriveEditor.Utils.Classes.LabelNumberFilter;

import javax.swing.*;
import javax.swing.text.PlainDocument;
import java.awt.event.ActionEvent;
import java.awt.geom.Point2D;

import static AutoDriveEditor.AutoDriveEditor.editor;
import static AutoDriveEditor.AutoDriveEditor.mapPanel;
import static AutoDriveEditor.Classes.MapImage.pdaImage;
import static AutoDriveEditor.GUI.MapPanel.*;
import static AutoDriveEditor.Locale.LocaleManager.getLocaleString;
import static AutoDriveEditor.XMLConfig.EditorXML.maxZoomLevel;

public class MoveNodeToCentreMenu extends JMenuItemBase {
    public MoveNodeToCentreMenu() {
        makeMenuItem("menu_debug_movetonode", "menu_debug_movetonode_accstring", true);
    }

    @Override
    public void actionPerformed(ActionEvent e) {
        super.actionPerformed(e);

        if (roadMap != null && pdaImage != null ) {
            JTextField centreNode = new JTextField(String.valueOf(1));
            JLabel labelNode = new JLabel(" ");
            PlainDocument docX = (PlainDocument) centreNode.getDocument();
            docX.setDocumentFilter(new LabelNumberFilter(labelNode, 0, RoadMap.networkNodesList.size(), false, false));

            Object[] inputFields = {getLocaleString("dialog_centre_node"), centreNode, labelNode};

            int option = JOptionPane.showConfirmDialog( editor, inputFields, ""+ getLocaleString("dialog_centre_node_title"), JOptionPane.OK_CANCEL_OPTION, JOptionPane.QUESTION_MESSAGE);

            if (option == JOptionPane.OK_OPTION) {
                zoomLevel = maxZoomLevel;
                updateNodeScaling();
                MapNode node = RoadMap.networkNodesList.get(Integer.parseInt(centreNode.getText()) - 1);
                Point2D target = worldPosToScreenPos(node.x, node.z);
                double x = (mapPanel.getWidth() >> 1) - target.getX();
                double y = (mapPanel.getHeight() >> 1) - target.getY();
                moveMapBy((int)x,(int)y);
            }
        }
    }
}
