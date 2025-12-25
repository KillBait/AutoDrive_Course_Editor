package AutoDriveEditor.GUI.Buttons.Toolbar.Alignment;

import AutoDriveEditor.AutoDriveEditor;
import AutoDriveEditor.Classes.UI_Components.LabelNumberFilter;
import AutoDriveEditor.Classes.UI_Components.ScaleAnimIcon;
import AutoDriveEditor.GUI.Buttons.BaseButton;
import AutoDriveEditor.RoadNetwork.MapNode;

import javax.swing.*;
import javax.swing.text.PlainDocument;
import java.awt.*;
import java.awt.event.MouseEvent;

import static AutoDriveEditor.AutoDriveEditor.getMapPanel;
import static AutoDriveEditor.Classes.Util_Classes.MathUtils.roundUpDoubleToDecimalPlaces;
import static AutoDriveEditor.GUI.MapPanel.getNodeAtScreenPosition;
import static AutoDriveEditor.GUI.MapPanel.mapScale;
import static AutoDriveEditor.Locale.LocaleManager.getLocaleString;
import static AutoDriveEditor.Managers.IconManager.*;

public class EditLocationButton extends BaseButton {

    public EditLocationButton(JPanel panel) {
        ScaleAnimIcon animEditLocationIcon = createScaleAnimIcon(BUTTON_ALIGN_EDIT_ICON, false);
        button = createAnimToggleButton(animEditLocationIcon, panel, null, null,  false, false, this);
    }

    @Override
    public String getButtonID() { return "EditLocationButton"; }

    @Override
    public String getButtonAction() { return "ActionButton"; }

    @Override
    public String getButtonPanel() { return "Alignment"; }

    @Override
    public String getInfoText() { return getLocaleString("toolbar_align_edit_infotext"); }

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
        docX.setDocumentFilter(new LabelNumberFilter(labelPosX, -1024 * mapScale, 1024 * mapScale, true, true));

        JTextField posZ = new JTextField(String.valueOf((float)node.z));
        JLabel labelPosZ = new JLabel(" ");
        PlainDocument docZ = (PlainDocument) posZ.getDocument();
        docZ.setDocumentFilter(new LabelNumberFilter(labelPosZ, -1024 * mapScale, 1024 * mapScale, true, true));

        JTextField posY = new JTextField(String.valueOf((float)node.y));
        JLabel labelPosY = new JLabel(" ");
        if (node.y < 0 ) {
            labelPosY.setForeground(Color.RED);
            labelPosY.setText("* Invalid Y location");
        }
        PlainDocument docY = (PlainDocument) posY.getDocument();
        docY.setDocumentFilter(new LabelNumberFilter(labelPosY, 0, 1024 * mapScale, true, false));


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

    @Override
    public String buildToolTip() {
        return getLocaleString("toolbar_align_edit_tooltip");
    }
}
