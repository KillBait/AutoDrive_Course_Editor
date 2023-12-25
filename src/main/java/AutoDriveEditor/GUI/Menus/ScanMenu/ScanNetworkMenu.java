package AutoDriveEditor.GUI.Menus.ScanMenu;

import AutoDriveEditor.GUI.Menus.JMenuItemBase;
import AutoDriveEditor.Utils.Classes.LabelNumberFilter;

import javax.swing.*;
import javax.swing.text.PlainDocument;
import java.awt.event.ActionEvent;

import static AutoDriveEditor.AutoDriveEditor.editor;
import static AutoDriveEditor.GUI.MapPanel.mapScale;
import static AutoDriveEditor.Locale.LocaleManager.getLocaleString;
import static AutoDriveEditor.Managers.ScanManager.scanNetworkForOverlapNodes;
import static AutoDriveEditor.Managers.ScanManager.searchDistance;

public class ScanNetworkMenu extends JMenuItemBase {

    public static JMenuItem menu_ScanNetwork;

    public ScanNetworkMenu() {
        menu_ScanNetwork = makeMenuItem("menu_scan_overlap", "menu_scan_overlap_accstring", false);
    }

    @Override
    public void actionPerformed(ActionEvent e) {
        super.actionPerformed(e);
        JTextField mergeDistance = new JTextField(String.valueOf(searchDistance));
        JLabel labelDistance = new JLabel(" ");
        PlainDocument docX = (PlainDocument) mergeDistance.getDocument();
        docX.setDocumentFilter(new LabelNumberFilter(labelDistance, 0, 2048 * mapScale, true, false));

        Object[] inputFields = {getLocaleString("dialog_scan_area"), mergeDistance, labelDistance};

        int option = JOptionPane.showConfirmDialog(editor, inputFields, ""+ getLocaleString("dialog_scan_title"), JOptionPane.OK_CANCEL_OPTION, JOptionPane.QUESTION_MESSAGE);

        if (option == JOptionPane.OK_OPTION) {
            scanNetworkForOverlapNodes(Double.parseDouble(mergeDistance.getText()));
        }
    }
}
