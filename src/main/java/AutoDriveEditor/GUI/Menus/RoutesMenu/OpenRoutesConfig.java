package AutoDriveEditor.GUI.Menus.RoutesMenu;

import AutoDriveEditor.GUI.Menus.JMenuItemBase;

import javax.swing.*;
import javax.swing.filechooser.FileFilter;
import java.awt.event.ActionEvent;
import java.io.File;

import static AutoDriveEditor.AutoDriveEditor.editor;
import static AutoDriveEditor.GUI.RoutesGUI.RoutesGUI.createRoutesGUI;
import static AutoDriveEditor.Locale.LocaleManager.getLocaleString;
import static AutoDriveEditor.XMLConfig.AutoSave.resumeAutoSaving;
import static AutoDriveEditor.XMLConfig.AutoSave.suspendAutoSaving;
import static AutoDriveEditor.XMLConfig.GameXML.lastUsedLocation;

public class OpenRoutesConfig extends JMenuItemBase {

    public static JMenuItem menu_OpenRoutesConfig
            ;
    public OpenRoutesConfig() {
        menu_OpenRoutesConfig = makeMenuItem("menu_routes_load_config",  "menu_routes_load_config_accstring", true );
    }

    @Override
    public void actionPerformed(ActionEvent e) {
        super.actionPerformed(e);
        JFileChooser fc = new JFileChooser(lastUsedLocation);
        fc.setDialogTitle(getLocaleString("dialog_load_routemanager_config_title"));
        fc.setFileSelectionMode(JFileChooser.FILES_ONLY);
        fc.setFileFilter(new FileFilter() {
            @Override
            public boolean accept(File f) {
                // always accept directory's
                if (f.isDirectory()) return true;
                // but only files with a specific name
                return f.getName().equals("routes.xml");
            }

            @Override
            public String getDescription() {
                return "AutoDrive RouteManager Config (.xml)";
            }
        });

        suspendAutoSaving();
        if (fc.showOpenDialog(editor) == JFileChooser.APPROVE_OPTION) {
            lastUsedLocation = fc.getCurrentDirectory().getAbsolutePath();
            File fileName = fc.getSelectedFile();
            createRoutesGUI(fileName, editor);
        }
        resumeAutoSaving();
    }
}
