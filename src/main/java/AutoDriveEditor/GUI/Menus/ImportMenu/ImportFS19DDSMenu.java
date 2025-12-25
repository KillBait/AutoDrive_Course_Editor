package AutoDriveEditor.GUI.Menus.ImportMenu;

import AutoDriveEditor.GUI.Menus.JMenuItemBase;

import javax.swing.*;
import javax.swing.filechooser.FileFilter;
import java.awt.event.ActionEvent;
import java.awt.event.InputEvent;
import java.awt.event.KeyEvent;
import java.io.File;

import static AutoDriveEditor.AutoDriveEditor.editor;
import static AutoDriveEditor.GUI.TextPanel.showInTextArea;
import static AutoDriveEditor.Locale.LocaleManager.getLocaleString;
import static AutoDriveEditor.Managers.ImportManager.importDDSForFS19;
import static AutoDriveEditor.XMLConfig.GameXML.lastUsedLocation;

public class ImportFS19DDSMenu extends JMenuItemBase {

    public static JMenuItem menu_ImportFS19DDS;
    public ImportFS19DDSMenu() {
        menu_ImportFS19DDS = makeMenuItem("menu_import_sub_fs19_dds", KeyEvent.VK_I, InputEvent.ALT_DOWN_MASK,false);
    }

    @Override
    public void actionPerformed(ActionEvent e) {
        super.actionPerformed(e);

        JFileChooser fc = new JFileChooser(lastUsedLocation);
        fc.setDialogTitle(getLocaleString("dialog_import_FS19_dds_image_title"));
        fc.setFileSelectionMode(JFileChooser.FILES_ONLY);
        fc.setFileFilter(new FileFilter() {
            @Override
            public boolean accept(File f) {
                // always accept directory's
                if (f.isDirectory()) return true;
                // but only files with a specific name
                return f.getName().equals("pda_map_H.dds");
            }

            @Override
            public String getDescription() {
                return "FS19 PDA Image (pda_map_H.dds)";
            }
        });

        if (fc.showOpenDialog(editor) == JFileChooser.APPROVE_OPTION) {
            if (!fc.getSelectedFile().getName().equals("pda_map_H.dds") && !fc.getSelectedFile().getName().endsWith(".dds")) {
                JOptionPane.showMessageDialog(editor, "The file " + fc.getSelectedFile() + " is not a valid dds file.", "FileType Error", JOptionPane.ERROR_MESSAGE);
            } else {
                importDDSForFS19(fc.getSelectedFile().getAbsoluteFile().toString());
            }
        } else {
            showInTextArea("Cancelled import of FS19 PDA Image", true, true);
        }
    }
}
