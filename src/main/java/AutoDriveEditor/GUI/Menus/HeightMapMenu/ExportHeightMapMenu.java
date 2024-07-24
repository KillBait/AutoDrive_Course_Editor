package AutoDriveEditor.GUI.Menus.HeightMapMenu;

import AutoDriveEditor.GUI.Menus.JMenuItemBase;
import AutoDriveEditor.RoadNetwork.RoadMap;

import javax.swing.*;
import javax.swing.filechooser.FileFilter;
import java.awt.*;
import java.awt.event.ActionEvent;
import java.io.File;
import java.io.IOException;

import static AutoDriveEditor.AutoDriveEditor.editor;
import static AutoDriveEditor.Classes.MapImage.heightMapImage;
import static AutoDriveEditor.GUI.TextPanel.setHeightMapLoadedLabel;
import static AutoDriveEditor.GUI.TextPanel.showInTextArea;
import static AutoDriveEditor.Locale.LocaleManager.getLocaleString;
import static AutoDriveEditor.Managers.ImportManager.exportImageToDisk;
import static AutoDriveEditor.Utils.FileUtils.getCurrentLocation;
import static AutoDriveEditor.Utils.FileUtils.getSelectedFileWithExtension;
import static AutoDriveEditor.Utils.LoggerUtils.LOG;
import static AutoDriveEditor.XMLConfig.GameXML.canEditConfig;
import static AutoDriveEditor.XMLConfig.GameXML.lastUsedLocation;

public class ExportHeightMapMenu extends JMenuItemBase {

    public static JMenuItem menu_ExportHeightMap;

    public ExportHeightMapMenu() {
        menu_ExportHeightMap = makeMenuItem("menu_heightmap_export", "menu_heightmap_export_accstring", false);
    }

    @Override
    public void actionPerformed(ActionEvent e) {
        super.actionPerformed(e);

        String currentExportPath;

        JFileChooser fc = new JFileChooser(lastUsedLocation);
        if (canEditConfig) {
            currentExportPath = getCurrentLocation() + "mapImages/" + RoadMap.mapName + "/" + RoadMap.mapName + "_HeightMap.png";
        } else {
            currentExportPath = getCurrentLocation() + "mapImages/unknown_HeightMap.png";
        }

        LOG.info("Export HeightMap path = {}", currentExportPath);
        File exportPath = new File(currentExportPath);
        try {
            if (exportPath.exists()) {
                if (exportPath.isDirectory())
                    throw new IOException("File '" + exportPath + "' is a directory");

                if (!exportPath.canWrite())
                    throw new IOException("File '" + exportPath + "' cannot be written");
            } else {
                File exportParent = exportPath.getParentFile();
                LOG.info("parent = {}", exportParent.getName());
                if (!exportParent.exists() && (!exportParent.mkdirs())) {
                    throw new IOException("'" + exportPath + "' could not be created");
                }
            }
        } catch (IOException ioException) {
            ioException.printStackTrace();
        }

        fc.setDialogTitle(getLocaleString("dialog_heightmap_export_title"));
        fc.setFileSelectionMode(JFileChooser.FILES_ONLY);
        fc.setFileFilter(new FileFilter() {
            @Override
            public boolean accept(File f) {
                // always accept directory's
                if (f.isDirectory()) return true;
                // but only files with a specific name
                return f.getName().contains("_HeightMap.png");
            }

            @Override
            public String getDescription() {
                return "AutoDriveEditor Heightmap Image (_HeightMap.png)";
            }
        });
        fc.setSelectedFile(exportPath);
        fc.setCurrentDirectory(exportPath);
        if (fc.showSaveDialog(editor) == JFileChooser.APPROVE_OPTION) {
            File saveImageFile = getSelectedFileWithExtension(fc);
            if (saveImageFile.exists()) {
                int response = JOptionPane.showConfirmDialog(editor, getLocaleString("dialog_overwrite"), "File already exists " + RoadMap.mapName + ".png", JOptionPane.YES_NO_OPTION, JOptionPane.QUESTION_MESSAGE);
                if (response == JOptionPane.NO_OPTION) {
                    showInTextArea("Cancelled export of default heightmap", true, true);
                    return;
                }
            }
            LOG.info("{} {}", getLocaleString("console_heightmap_export_default"), saveImageFile);
            if (exportImageToDisk(heightMapImage, getSelectedFileWithExtension(fc).toString())) {
                setHeightMapLoadedLabel("Loaded", new Color(0,100,0));
                showInTextArea("Export to " + exportPath + " Successful", true, true);
            }
        }
    }
}
