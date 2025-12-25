package AutoDriveEditor.GUI.Menus.ImportMenu;

import AutoDriveEditor.GUI.Menus.JMenuItemBase;

import javax.swing.*;
import javax.swing.filechooser.FileFilter;
import java.awt.event.ActionEvent;
import java.io.File;

import static AutoDriveEditor.AutoDriveEditor.editor;
import static AutoDriveEditor.GUI.MapImage.manualLoadHeightMap;
import static AutoDriveEditor.Locale.LocaleManager.getLocaleString;
import static AutoDriveEditor.XMLConfig.GameXML.lastUsedLocation;

public class ImportHeightMapMenu extends JMenuItemBase {

    public static JMenuItem menu_ImportHeightMap;

    public ImportHeightMapMenu() {
        menu_ImportHeightMap = makeMenuItem("menu_import_sub_heightmap_import", false);
    }

    @Override
    public void actionPerformed(ActionEvent e) {
        super.actionPerformed(e);
        JFileChooser fc = new JFileChooser(lastUsedLocation);
        fc.setDialogTitle(getLocaleString("dialog_heightmap_load_title"));
        fc.setFileSelectionMode(JFileChooser.FILES_ONLY);
        fc.setFileFilter(new FileFilter() {
            @Override
            public boolean accept(File f) {
                // always accept directory's
                if (f.isDirectory()) return true;
                // but only files with a specific name
                return f.getName().equals("terrain.heightmap.png");
            }

            @Override
            public String getDescription() {
                return "FS HeightMap File (terrain.heightmap.png)";
            }
        });
        if (fc.showOpenDialog(editor) == JFileChooser.APPROVE_OPTION) {
            File fileName = fc.getSelectedFile();
            manualLoadHeightMap(fileName);
        }
    }
}
