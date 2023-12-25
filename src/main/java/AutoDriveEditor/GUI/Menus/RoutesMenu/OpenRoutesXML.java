package AutoDriveEditor.GUI.Menus.RoutesMenu;

import AutoDriveEditor.GUI.Menus.JMenuItemBase;

import javax.swing.*;
import javax.swing.filechooser.FileFilter;
import java.awt.event.ActionEvent;
import java.io.File;

import static AutoDriveEditor.AutoDriveEditor.editor;
import static AutoDriveEditor.GUI.MapPanel.isStale;
import static AutoDriveEditor.GUI.Menus.EditorMenu.saveImageEnabled;
import static AutoDriveEditor.Locale.LocaleManager.getLocaleString;
import static AutoDriveEditor.Managers.ImportManager.setEditorUsingImportedImage;
import static AutoDriveEditor.XMLConfig.AutoSave.resumeAutoSaving;
import static AutoDriveEditor.XMLConfig.AutoSave.suspendAutoSaving;
import static AutoDriveEditor.XMLConfig.GameXML.lastUsedLocation;
import static AutoDriveEditor.XMLConfig.GameXML.saveGameConfig;
import static AutoDriveEditor.XMLConfig.RoutesXML.loadRouteManagerXML;

public class OpenRoutesXML extends JMenuItemBase {

    public OpenRoutesXML() { makeMenuItem("menu_routes_load_xml",  "menu_routes_load_xml_accstring", true );}

    @Override
    public void actionPerformed(ActionEvent e) {
        super.actionPerformed(e);

        //checkIfConfigIsStaleAndConfirmSave();

        JFileChooser fc = new JFileChooser(lastUsedLocation);
        if (isStale()) {
            int response = JOptionPane.showConfirmDialog(editor, getLocaleString("dialog_exit_unsaved"), "AutoDrive", JOptionPane.YES_NO_OPTION, JOptionPane.QUESTION_MESSAGE);
            if (response == JOptionPane.YES_OPTION) {
                saveGameConfig(null, false, false);
            }
        }
        fc.setDialogTitle(getLocaleString("dialog_load_route_xml_title"));
        fc.setFileSelectionMode(JFileChooser.FILES_ONLY);
        fc.setFileFilter(new FileFilter() {
            @Override
            public boolean accept(File f) {
                // always accept directory's
                if (f.isDirectory()) return true;
                // but only files with a specific name
                return f.getName().contains(".xml") && !f.getName().equals("routes.xml");
            }

            @Override
            public String getDescription() {
                return "AutoDrive Route XML (.xml)";
            }
        });

        suspendAutoSaving();

        if (fc.showOpenDialog(editor) == JFileChooser.APPROVE_OPTION) {
            lastUsedLocation = fc.getCurrentDirectory().getAbsolutePath();
            File fileName = fc.getSelectedFile();
            if (loadRouteManagerXML(fileName, false, null)) {
                setEditorUsingImportedImage(false);
                saveImageEnabled(false);
            }
        }

        resumeAutoSaving();
    }
}
