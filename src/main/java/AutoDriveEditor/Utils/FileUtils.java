package AutoDriveEditor.Utils;

import AutoDriveEditor.AutoDriveEditor;

import javax.swing.*;
import javax.swing.filechooser.FileNameExtensionFilter;
import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.net.URISyntaxException;
import java.net.URL;

import static AutoDriveEditor.GUI.MenuBuilder.*;
import static AutoDriveEditor.Utils.LoggerUtils.*;

public class FileUtils {

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
            if (bDebugFileIO) LOG.info("JAR Path : {}", jarPath);
            launchPath = jarPath.substring(0, jarPath.lastIndexOf("/") + 1);
            if (bDebugFileIO) LOG.info("Path : " + launchPath);
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
            // if not, append the first extension from the selected filter
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
            int n = 0;
            while (-1 != (n = input.read(buffer))) {
                output.write(buffer, 0, n);
            }

            input.close();
            output.close();

            LOG.info("File '{}' downloaded successfully!", file);
            LOG.info("Saved downloaded image to {}", fullPath);
            return file;
        }
        catch(IOException ioEx) {
            ioEx.printStackTrace();
            return null;
        }
    }

}
