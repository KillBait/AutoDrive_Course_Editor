package AutoDriveEditor.GUI.Buttons.Alignment;

import AutoDriveEditor.AutoDriveEditor;
import AutoDriveEditor.GUI.Buttons.BaseButton;
import AutoDriveEditor.RoadNetwork.MapNode;
import AutoDriveEditor.Utils.Classes.LabelNumberFilter;

import javax.swing.*;
import javax.swing.text.PlainDocument;
import java.awt.*;
import java.awt.event.MouseEvent;

import static AutoDriveEditor.Locale.LocaleManager.getLocaleString;
import static AutoDriveEditor.MapPanel.MapPanel.*;
import static AutoDriveEditor.Utils.GUIUtils.makeImageToggleButton;
import static AutoDriveEditor.Utils.MathUtils.roundUpDoubleToDecimalPlaces;

public class EditLocationButton extends BaseButton {

    public EditLocationButton(JPanel panel) {
        button = makeImageToggleButton("buttons/editlocation","buttons/editlocation_selected", null,"align_node_edit_tooltip","align_node_edit_alt", panel, false, false, null, false, this);
    }

    @Override
    public String getButtonID() { return "EditLocationButton"; }

    @Override
    public String getButtonAction() { return "ActionButton"; }

    @Override
    public String getButtonPanel() { return "Alignment"; }

    @Override
    public String getInfoText() { return getLocaleString("align_node_edit_tooltip"); }

    @Override
    public void mouseClicked(MouseEvent e) {
        MapNode clickedNode = getNodeAtScreenPosition(e.getX(), e.getY());
        if (clickedNode != null) showEditNodeLocationDialog(clickedNode);
    }

    //
    // Dialog for Edit Node position
    //

    public void  showEditNodeLocationDialog(MapNode node) {

        JTextField posX = new JTextField(String.valueOf((float)node.x));
        JLabel labelPosX = new JLabel(" ");
        PlainDocument docX = (PlainDocument) posX.getDocument();
        docX.setDocumentFilter(new LabelNumberFilter(labelPosX, -1024 * mapZoomFactor, 1024 * mapZoomFactor, true, true));

        JTextField posZ = new JTextField(String.valueOf((float)node.z));
        JLabel labelPosZ = new JLabel(" ");
        PlainDocument docZ = (PlainDocument) posZ.getDocument();
        docZ.setDocumentFilter(new LabelNumberFilter(labelPosZ, -1024 * mapZoomFactor, 1024 * mapZoomFactor, true, true));

        JTextField posY = new JTextField(String.valueOf((float)node.y));
        JLabel labelPosY = new JLabel(" ");
        if (node.y < 0 ) {
            labelPosY.setForeground(Color.RED);
            labelPosY.setText("* Invalid Y location");
        }
        PlainDocument docY = (PlainDocument) posY.getDocument();
        docY.setDocumentFilter(new LabelNumberFilter(labelPosY, 0, 1024 * mapZoomFactor, true, false));


        Object[] inputFields = {getLocaleString("dialog_node_position_x"), posX, labelPosX,
                getLocaleString("dialog_node_position_y"), posY, labelPosY,
                getLocaleString("dialog_node_position_z"), posZ, labelPosZ};

        int option = JOptionPane.showConfirmDialog(AutoDriveEditor.editor, inputFields, ""+ getLocaleString("dialog_node_title"), JOptionPane.OK_CANCEL_OPTION, JOptionPane.QUESTION_MESSAGE);

        if (option == JOptionPane.OK_OPTION) {
            node.x = roundUpDoubleToDecimalPlaces(Double.parseDouble(posX.getText()), 3);
            node.y = roundUpDoubleToDecimalPlaces(Double.parseDouble(posY.getText()), 3);
            node.z = roundUpDoubleToDecimalPlaces(Double.parseDouble(posZ.getText()), 3);
            getMapPanel().repaint();
        }
    }
}
