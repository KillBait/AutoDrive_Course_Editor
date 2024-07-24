package AutoDriveEditor.GUI.Menus.MapImagesMenu;

import AutoDriveEditor.GUI.Menus.JMenuItemBase;
import AutoDriveEditor.RoadNetwork.RoadMap;

import javax.swing.*;
import javax.swing.filechooser.FileFilter;
import java.awt.*;
import java.awt.event.ActionEvent;
import java.awt.event.InputEvent;
import java.awt.event.KeyEvent;
import java.io.File;
import java.io.IOException;

import static AutoDriveEditor.AutoDriveEditor.*;
import static AutoDriveEditor.Classes.MapImage.getMapPanelImage;
import static AutoDriveEditor.GUI.TextPanel.*;
import static AutoDriveEditor.Locale.LocaleManager.getLocaleString;
import static AutoDriveEditor.Managers.ImportManager.exportImageToDisk;
import static AutoDriveEditor.Utils.FileUtils.getCurrentLocation;
import static AutoDriveEditor.Utils.FileUtils.getSelectedFileWithExtension;
import static AutoDriveEditor.Utils.LoggerUtils.LOG;
import static AutoDriveEditor.XMLConfig.GameXML.canEditConfig;
import static AutoDriveEditor.XMLConfig.GameXML.lastUsedLocation;

@SuppressWarnings("CallToPrintStackTrace")
public class SaveMapImageMenu extends JMenuItemBase {

    public static JMenuItem menu_SaveMapImage;
    public SaveMapImageMenu() {
        menu_SaveMapImage = makeMenuItem("menu_map_saveimage",  "menu_map_saveimage_accstring", KeyEvent.VK_B, InputEvent.ALT_DOWN_MASK, false);
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
            boolean exportSuccess = exportImageToDisk(getMapPanelImage(), getSelectedFileWithExtension(fc).toString());
            if (exportSuccess) {
                setImageLoadedLabel("Saved", new Color(0,100,0));
                //imageLoadedLabel.setForeground(new Color(0, 100, 0));
                //imageLoadedLabel.setText("Saved");
                showInTextArea("Export of mapImage '" + fc.getSelectedFile().getName() + "' Successful", true, true);
            } else {
                setImageLoadedLabel("Save Failed", new Color(150,0,0));
                //imageLoadedLabel.setForeground(new Color(200, 0, 0));
                //imageLoadedLabel.setText("Imported");
                showInTextArea("Export of mapImage '" + fc.getSelectedFile().getName() + "' Failed", true, true);
            }
        }
    }
}
