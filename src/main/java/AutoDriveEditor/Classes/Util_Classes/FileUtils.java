package AutoDriveEditor.Classes.Util_Classes;

import AutoDriveEditor.AutoDriveEditor;

import javax.swing.*;
import javax.swing.filechooser.FileFilter;
import javax.swing.filechooser.FileNameExtensionFilter;
import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.net.URISyntaxException;
import java.net.URL;

import static AutoDriveEditor.Classes.Util_Classes.LoggerUtils.LOG;
import static AutoDriveEditor.GUI.Menus.DebugMenu.Logging.LogFileIOMenu.bDebugLogFileIO;
import static AutoDriveEditor.Locale.LocaleManager.getLocaleString;
import static AutoDriveEditor.XMLConfig.GameXML.lastUsedLocation;


public class FileUtils {

    public static final char EXTENSION_SEPARATOR = '.';

    public static String getCurrentLocation() {

        //
        // only works with JDK 11 and above
        //

        try {
            String launchPath;
            String jarPath = AutoDriveEditor.class
                    .getProtectionDomain()
                    .getCodeSource()
                    .getLocation()
                    .toURI()
                    .getPath();
            if (bDebugLogFileIO) LOG.info("JAR Path : {}", jarPath);
            launchPath = jarPath.substring(0, jarPath.lastIndexOf(File.separator) + 1);
            if (bDebugLogFileIO) LOG.info("Path : {}", launchPath);
            return launchPath;
        } catch (URISyntaxException uriSyntaxException) {
            uriSyntaxException.printStackTrace();
        }
        return null;
    }

    public static File getSelectedFileWithExtension(JFileChooser c) {
        File file = c.getSelectedFile();
        if (c.getFileFilter() instanceof FileNameExtensionFilter) {
            String[] extension = ((FileNameExtensionFilter)c.getFileFilter()).getExtensions();
            String nameLower = file.getName().toLowerCase();
            for (String ext : extension) { // check if it already has a valid extension
                if (nameLower.endsWith('.' + ext.toLowerCase())) {
                    return file; // if yes, return as-is
                }
            }
            // if not, append the first extension from the setSelected filter
            file = new File(file.toString() + '.' + extension[0]);
        }
        return file;
    }

    public static File copyURLToFile(URL url, File file, String fullPath) {

        try {
            InputStream input = url.openStream();
            if (file.exists()) {
                if (file.isDirectory())
                    throw new IOException("File '" + file + "' is a directory");

                if (!file.canWrite())
                    throw new IOException("File '" + file + "' cannot be written");
            } else {
                File parent = file.getParentFile();
                if ((parent != null) && (!parent.exists()) && (!parent.mkdirs())) {
                    throw new IOException("File '" + file + "' could not be created");
                }
            }

            FileOutputStream output = new FileOutputStream(file);

            byte[] buffer = new byte[4096];
            int n;
            while (-1 != (n = input.read(buffer))) {
                output.write(buffer, 0, n);
            }

            input.close();
            output.close();

            LOG.info("File '{}' downloaded successfully!", url.getFile());
            LOG.info("Saved downloaded image to {}", fullPath);
            return file;
        }
        catch(IOException ioEx) {
            LOG.info("## FileNotFoundException ## - Unable to download image");
            ioEx.printStackTrace();
            return null;
        }
    }

    public static String removeFilenameFromString(String path) {
        return path.substring(0, indexOfLastSeparator(path) + 1);
    }

    public static String removePathFromString(String path) {
        return path.substring(indexOfLastSeparator(path) +1);
    }

    @SuppressWarnings("unused")
    public static String removeFilenameFromPath(File file) {
        return file.getAbsolutePath().substring(0, file.getAbsolutePath().lastIndexOf(File.separator) + 1);
    }

    @SuppressWarnings("unused")
    public static String getParent(File file) {
        if (file.exists()) {
            return file.getParentFile().getAbsolutePath();
        } else {
            return null;
        }
    }

    public static String removeExtension(String filename) {
        if (filename == null) {
            return null;
        }
        int index = indexOfExtension(filename);
        if (index == -1) {
            return filename;
        } else {
            return filename.substring(0, index);
        }
    }

    private static int indexOfExtension(String filename) {
        if (filename == null) return -1;
        int extensionPos = filename.lastIndexOf(EXTENSION_SEPARATOR);
        int lastSeparator = indexOfLastSeparator(filename);
        if (lastSeparator > extensionPos) return -1;
        return extensionPos;
    }

    private static int indexOfLastSeparator(String filename) {
        return (filename == null) ? -1 : filename.lastIndexOf(File.separatorChar);
    }

    public static JFileChooser createFileChooser(String title, int fileSelectionMode, boolean readOnly, String path, FileFilter filter) {

        JFileChooser fc;

        if (readOnly) {
            // It's hacky to use UIManager, but don't have much choice since FileChoosers don't have an in-built
            // read only flag to set
            Boolean old = UIManager.getBoolean("FileChooser.readOnly");
            UIManager.put("FileChooser.readOnly", Boolean.TRUE);
            fc = new JFileChooser(lastUsedLocation);
            UIManager.put("FileChooser.readOnly", old);
        } else {
            fc = new JFileChooser(lastUsedLocation);
        }

        fc.setDialogTitle(getLocaleString(title));
        fc.setFileSelectionMode(fileSelectionMode);
        if (filter != null) fc.setFileFilter(filter);
        return fc;
    }

    public static FileFilter createFileFilter(String fileExtension, String fileDescription) {
        return new FileNameExtensionFilter(fileDescription, fileExtension);
    }
}
