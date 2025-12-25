package AutoDriveEditor.Managers;

import AutoDriveEditor.XMLConfig.GameXML;

import javax.imageio.ImageIO;
import javax.swing.*;
import java.awt.image.BufferedImage;
import java.io.File;
import java.io.IOException;

import static AutoDriveEditor.AutoDriveEditor.editor;
import static AutoDriveEditor.Classes.Util_Classes.FileUtils.getCurrentLocation;
import static AutoDriveEditor.Classes.Util_Classes.LoggerUtils.LOG;
import static AutoDriveEditor.Locale.LocaleManager.getLocaleString;
import static AutoDriveEditor.Managers.ImportManager.createBufferImage;

public class ExportManager {

    public static String createMapImagesFolder(String folderName) {
        File newFolder = new File(getCurrentLocation() + "mapImages" + File.separator + folderName);
        if (!newFolder.exists()) {
            if (newFolder.mkdirs()) {
                LOG.info("Created folder: {}", newFolder.getAbsolutePath());
                return newFolder.getAbsolutePath();
            } else {
                LOG.error("Failed to createSetting folder: {}", newFolder.getAbsolutePath());
                JOptionPane.showMessageDialog(editor, getLocaleString("dialog_import_zip_folder_creation_failed"), getLocaleString("dialog_import_zip_folder_creation_failed_title"), JOptionPane.ERROR_MESSAGE);
                return null;
            }
        } else {
            LOG.warn("Export Folder '{}' already exists", newFolder.getAbsolutePath());
            // Folder already exists, no need to createSetting it again
            return newFolder.getAbsolutePath();
        }
    }

    @SuppressWarnings("UnusedReturnValue")
    public static boolean exportMapImageToDisk(BufferedImage image, String filePath, GameXML.GameVersion fsVersion) {
        try {
            File outputFile = new File(filePath);
            if (outputFile.exists()) {
                if (outputFile.isDirectory())
                    throw new IOException("File '" + outputFile + "' is a directory");

                if (!outputFile.canWrite())
                    throw new IOException("File '" + outputFile + "' cannot be written");
            } else {
                File parent = outputFile.getParentFile();
                if ((parent != null) && (!parent.exists()) && (!parent.mkdirs())) {
                    throw new IOException("File '" + outputFile + "' could not be created");
                }
            }
            ImageIO.write(createBufferImage(image, fsVersion), "png", outputFile);
            return true;
        } catch (IOException e) {
            e.printStackTrace();
            return false;
        }
    }

    @SuppressWarnings("UnusedReturnValue")
    public static boolean exportHeightMapImageToDisk(BufferedImage image, String filePath) {
        try {
            File outputFile = new File(filePath);
            if (outputFile.exists()) {
                if (outputFile.isDirectory())
                    throw new IOException("File '" + outputFile + "' is a directory");

                if (!outputFile.canWrite())
                    throw new IOException("File '" + outputFile + "' cannot be written");
            } else {
                File parent = outputFile.getParentFile();
                if ((parent != null) && (!parent.exists()) && (!parent.mkdirs())) {
                    throw new IOException("File '" + outputFile + "' could not be created");
                }
            }
            ImageIO.write(image, "png", outputFile);
            return true;
        } catch (IOException e) {
            e.printStackTrace();
            return false;
        }
    }
}
