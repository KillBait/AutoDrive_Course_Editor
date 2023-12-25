package AutoDriveEditor.GUI.Menus.FileMenu;

import AutoDriveEditor.GUI.Menus.JMenuItemBase;

import javax.swing.*;
import javax.swing.filechooser.FileFilter;
import java.awt.event.ActionEvent;
import java.awt.event.InputEvent;
import java.awt.event.KeyEvent;
import java.io.File;

import static AutoDriveEditor.AutoDriveEditor.editor;
import static AutoDriveEditor.Locale.LocaleManager.getLocaleString;
import static AutoDriveEditor.Utils.FileUtils.getSelectedFileWithExtension;
import static AutoDriveEditor.Utils.LoggerUtils.LOG;
import static AutoDriveEditor.XMLConfig.GameXML.*;

public class SaveAsConfigMenu extends JMenuItemBase {

    public static JMenuItem menu_SaveConfigAs;

    public SaveAsConfigMenu() {
        menu_SaveConfigAs = makeMenuItem("menu_file_saveasconfig",  "menu_file_saveasconfig_accstring", KeyEvent.VK_A, InputEvent.ALT_DOWN_MASK, false);
    }

    @Override
    public void actionPerformed(ActionEvent e) {
        super.actionPerformed(e);

        JFileChooser fc = new JFileChooser(lastUsedLocation);

        if (xmlConfigFile == null) return;
        fc.setDialogTitle(getLocaleString("dialog_save_destination"));
        fc.setFileSelectionMode(JFileChooser.FILES_ONLY);
        fc.setFileFilter(new FileFilter() {
            @Override
            public boolean accept(File f) {
                // always accept directory's
                if (f.isDirectory()) return true;
                // but only files with a specific name
                return f.getName().contains(".xml");
            }

            @Override
            public String getDescription() {
                return "AutoDrive Config (.xml)";
            }
        });
        fc.setSelectedFile(xmlConfigFile);

        if (fc.showSaveDialog(editor) == JFileChooser.APPROVE_OPTION) {
            lastUsedLocation = fc.getCurrentDirectory().getAbsolutePath();
            LOG.info("{} {}", getLocaleString("console_config_save_as"), getSelectedFileWithExtension(fc));
            saveGameConfig(getSelectedFileWithExtension(fc).toString(), false, false);
        }
    }
}
