package AutoDriveEditor.GUI.Menus.MapImagesMenu;

import AutoDriveEditor.GUI.Menus.JMenuItemBase;

import javax.swing.*;
import javax.swing.filechooser.FileFilter;
import java.awt.*;
import java.awt.event.ActionEvent;
import java.awt.event.InputEvent;
import java.awt.event.KeyEvent;
import java.io.File;

import static AutoDriveEditor.AutoDriveEditor.editor;
import static AutoDriveEditor.GUI.Menus.EditorMenu.saveImageEnabled;
import static AutoDriveEditor.GUI.TextPanel.setImageLoadedLabel;
import static AutoDriveEditor.GUI.TextPanel.showInTextArea;
import static AutoDriveEditor.Locale.LocaleManager.getLocaleString;
import static AutoDriveEditor.Managers.ImportManager.importFromFS22;
import static AutoDriveEditor.Managers.ImportManager.setEditorUsingImportedImage;
import static AutoDriveEditor.XMLConfig.GameXML.lastUsedLocation;

public class ImportFS22DDSMenu extends JMenuItemBase {

    public static JMenuItem menu_ImportFS22DDS;
    public ImportFS22DDSMenu() {
        menu_ImportFS22DDS = makeMenuItem("menu_import_fs22_dds", "menu_import_fs22_dds_accstring", KeyEvent.VK_P, InputEvent.ALT_DOWN_MASK,false);
    }

    @Override
    public void actionPerformed(ActionEvent e) {
        super.actionPerformed(e);
        JFileChooser fc = new JFileChooser(lastUsedLocation);
        fc.setDialogTitle(getLocaleString("dialog_import_FS22_dds_image_title"));
        fc.setFileSelectionMode(JFileChooser.FILES_ONLY);
        fc.setFileFilter(new FileFilter() {
            @Override
            public boolean accept(File f) {
                // always accept directory's
                if (f.isDirectory()) return true;
                // but only files with a specific name
                return f.getName().equals("overview.dds");
            }

            @Override
            public String getDescription() {
                return "FS22 PDA image (overview.dds)";
            }
        });
        if (fc.showOpenDialog(editor) == JFileChooser.APPROVE_OPTION) {
            if (!fc.getSelectedFile().getName().equals("overview.dds") && !fc.getSelectedFile().getName().endsWith(".dds")) {
                JOptionPane.showMessageDialog(editor, "The file " + fc.getSelectedFile() + " is not a valid dds file.", "FileType Error", JOptionPane.ERROR_MESSAGE);
                return;
            }

            boolean importResult = importFromFS22(fc.getSelectedFile().getAbsoluteFile().toString());
            if (importResult) {
                setEditorUsingImportedImage(true);
                saveImageEnabled(true);
                setImageLoadedLabel("Imported", new Color(191, 56, 14));
                showInTextArea("Import of FS22 mapImage '" + fc.getSelectedFile().getName() + "' Successful", true, true);
            }
        } else {
            showInTextArea("Cancelled import of FS22 PDA Image", true, true);
        }
    }
}
