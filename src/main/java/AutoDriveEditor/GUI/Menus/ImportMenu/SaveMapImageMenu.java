package AutoDriveEditor.GUI.Menus.ImportMenu;

import AutoDriveEditor.GUI.Menus.JMenuItemBase;
import AutoDriveEditor.RoadNetwork.RoadMap;

import javax.swing.*;
import javax.swing.filechooser.FileFilter;
import java.awt.event.ActionEvent;
import java.awt.event.InputEvent;
import java.awt.event.KeyEvent;
import java.io.File;
import java.io.IOException;

import static AutoDriveEditor.AutoDriveEditor.getEditorReference;
import static AutoDriveEditor.Classes.Util_Classes.FileUtils.getCurrentLocation;
import static AutoDriveEditor.Classes.Util_Classes.FileUtils.getSelectedFileWithExtension;
import static AutoDriveEditor.Classes.Util_Classes.LoggerUtils.LOG;
import static AutoDriveEditor.GUI.TextPanel.showInTextArea;
import static AutoDriveEditor.Locale.LocaleManager.getLocaleString;
import static AutoDriveEditor.XMLConfig.GameXML.canEditConfig;
import static AutoDriveEditor.XMLConfig.GameXML.lastUsedLocation;

@SuppressWarnings("CallToPrintStackTrace")
public class SaveMapImageMenu extends JMenuItemBase {

    public static JMenuItem menu_SaveMapImage;
    public SaveMapImageMenu() {
        menu_SaveMapImage = makeMenuItem("menu_import_sub_map_saveimage", KeyEvent.VK_B, InputEvent.ALT_DOWN_MASK, false);
    }
    @Override
    public void actionPerformed(ActionEvent e) {
        super.actionPerformed(e);

        String currentSavePath;

        JFileChooser fc = new JFileChooser(lastUsedLocation);

        if (canEditConfig) {
            currentSavePath = getCurrentLocation() + "mapImages/" + RoadMap.mapName + "/" + RoadMap.mapName + ".png";
        } else {
            currentSavePath = getCurrentLocation() + "mapImages/unknown.png";
        }

        LOG.info("Save Image to -- {}", currentSavePath);
        File savePath = new File(currentSavePath);
        try {
            if (savePath.exists()) {
                if (savePath.isDirectory())
                    throw new IOException("File '" + savePath + "' is a directory");

                if (!savePath.canWrite())
                    throw new IOException("File '" + savePath + "' cannot be written");
            } else {
                File saveParent = savePath.getParentFile();
                LOG.info("parent = {}", saveParent.getName());
                if (!saveParent.exists() && (!saveParent.mkdirs())) {
                    throw new IOException("'" + savePath + "' could not be created");
                }
            }
        } catch (IOException ioException) {
            ioException.printStackTrace();
        }

        fc.setDialogTitle(getLocaleString("dialog_save_mapimage"));
        fc.setFileSelectionMode(JFileChooser.FILES_ONLY);
        fc.setFileFilter(new FileFilter() {
            @Override
            public boolean accept(File f) {
                // always accept directory's
                if (f.isDirectory()) return true;
                // but only files with a specific name
                return f.getName().contains(".png");
            }

            @Override
            public String getDescription() {
                return "AutoDriveEditor Map Image (.png)";
            }
        });
        fc.setSelectedFile(savePath);
        fc.setCurrentDirectory(savePath);
        if (fc.showSaveDialog(getEditorReference()) == JFileChooser.APPROVE_OPTION) {
            File saveImageFile = getSelectedFileWithExtension(fc);
            if (saveImageFile.exists()) {
                int response = JOptionPane.showConfirmDialog(getEditorReference(), getLocaleString("dialog_overwrite"), "File already exists " + RoadMap.mapName + ".png", JOptionPane.YES_NO_OPTION, JOptionPane.QUESTION_MESSAGE);
                if (response == JOptionPane.NO_OPTION) {
                    showInTextArea("Cancelled saving of converted image", true, true);
                    return;
                }
            }
            /*boolean exportSuccess = exportMapImageToDisk(getMapPanelImage(), getSelectedFileWithExtension(fc).toString());
            if (exportSuccess) {
                setMapImageLabel(LOADED);
                showInTextArea("Export of mapImage '" + fc.getSelectedFile().getName() + "' Successful", true, true);
            } else {
                setMapImageLabel(NOT_FOUND);
                showInTextArea("Export of mapImage '" + fc.getSelectedFile().getName() + "' Failed", true, true);
            }*/
        }
    }
}
