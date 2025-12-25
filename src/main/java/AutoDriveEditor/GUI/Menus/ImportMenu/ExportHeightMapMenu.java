package AutoDriveEditor.GUI.Menus.ImportMenu;

import AutoDriveEditor.GUI.Menus.JMenuItemBase;
import AutoDriveEditor.RoadNetwork.RoadMap;

import javax.swing.*;
import javax.swing.filechooser.FileFilter;
import java.awt.event.ActionEvent;
import java.io.File;
import java.io.IOException;

import static AutoDriveEditor.AutoDriveEditor.editor;
import static AutoDriveEditor.Classes.UI_Components.MapInfoLabel.LabelStatus.LOADED;
import static AutoDriveEditor.Classes.Util_Classes.FileUtils.getCurrentLocation;
import static AutoDriveEditor.Classes.Util_Classes.FileUtils.getSelectedFileWithExtension;
import static AutoDriveEditor.Classes.Util_Classes.LoggerUtils.LOG;
import static AutoDriveEditor.GUI.Buttons.ActionBar.ConfigPanel.MapInfoButton.setHeightmapImageLabel;
import static AutoDriveEditor.GUI.MapImage.heightMapImage;
import static AutoDriveEditor.GUI.TextPanel.showInTextArea;
import static AutoDriveEditor.Locale.LocaleManager.getLocaleString;
import static AutoDriveEditor.Managers.ExportManager.exportMapImageToDisk;
import static AutoDriveEditor.XMLConfig.GameXML.*;

public class ExportHeightMapMenu extends JMenuItemBase {

    public static JMenuItem menu_ExportHeightMap;

    public ExportHeightMapMenu() {
        menu_ExportHeightMap = makeMenuItem("menu_import_sub_heightmap_export", false);
    }

    @Override
    public void actionPerformed(ActionEvent e) {
        super.actionPerformed(e);

        String currentExportPath;

        JFileChooser fc = new JFileChooser(lastUsedLocation);
        if (canEditConfig) {
            currentExportPath = getCurrentLocation() + "mapImages" + File.separator + RoadMap.mapName + File.separator + RoadMap.mapName + "_HeightMap.png";
        } else {
            currentExportPath = getCurrentLocation() + "mapImages" + File.separator + "unknown_HeightMap.png";
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
            LOG.info("Export heightmap as default {}", saveImageFile);
            if (exportMapImageToDisk(heightMapImage, getSelectedFileWithExtension(fc).toString(), configVersion)) {
                setHeightmapImageLabel(LOADED);
                showInTextArea("Export to " + exportPath + " Successful", true, true);
            }
        }
    }
}
