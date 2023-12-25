package AutoDriveEditor.GUI.Menus.FileMenu;

import AutoDriveEditor.GUI.Menus.JMenuItemBase;

import javax.swing.*;
import javax.swing.filechooser.FileFilter;
import java.awt.event.ActionEvent;
import java.awt.event.InputEvent;
import java.awt.event.KeyEvent;
import java.io.File;

import static AutoDriveEditor.AutoDriveEditor.editor;
import static AutoDriveEditor.GUI.MapPanel.checkIfConfigIsStaleAndConfirmSave;
import static AutoDriveEditor.Utils.FileUtils.createFileChooser;
import static AutoDriveEditor.XMLConfig.AutoSave.resumeAutoSaving;
import static AutoDriveEditor.XMLConfig.AutoSave.suspendAutoSaving;
import static AutoDriveEditor.XMLConfig.GameXML.lastUsedLocation;
import static AutoDriveEditor.XMLConfig.GameXML.loadGameConfig;

public class OpenConfigMenu extends JMenuItemBase {

    public OpenConfigMenu() {
        makeMenuItem("menu_file_loadconfig",  "menu_file_loadconfig_accstring", KeyEvent.VK_L, InputEvent.ALT_DOWN_MASK, true );
    }

    @Override
    public void actionPerformed(ActionEvent e) {
        super.actionPerformed(e);
        // check if the current config has been changed a confirm save before loading a new config
        checkIfConfigIsStaleAndConfirmSave();

        JFileChooser lfc = createFileChooser("dialog_load_config_xml_title", JFileChooser.FILES_ONLY, true, lastUsedLocation, new FileFilter() {
            @Override
            public boolean accept(File f) {
                // always show folders/directory's
                if (f.isDirectory()) return true;
                // files only that end with the specific extension
                return f.getName().contains(".xml");
            }

            @Override
            public String getDescription() {
                return "AutoDrive Config (.xml)";
            }
        });

        suspendAutoSaving();

        if (lfc.showOpenDialog(editor) == JFileChooser.APPROVE_OPTION) {
            lastUsedLocation = lfc.getCurrentDirectory().getAbsolutePath();
            loadGameConfig(lfc.getSelectedFile());
        }

        resumeAutoSaving();
    }
}
