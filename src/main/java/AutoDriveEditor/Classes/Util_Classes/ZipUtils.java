package AutoDriveEditor.Classes.Util_Classes;

import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.Enumeration;
import java.util.zip.ZipEntry;
import java.util.zip.ZipException;
import java.util.zip.ZipFile;

import static AutoDriveEditor.Classes.Util_Classes.LoggerUtils.LOG;
import static AutoDriveEditor.GUI.Menus.DebugMenu.Logging.LogZipUtilsMenu.bDebugLogZipUtils;

/**
 * Utility class for handling zip files and extracting specific files from them.
 * Provides methods to open a zip file, extract a file into memory, and find a file within the zip.
 */
public class ZipUtils {

    /**
     * Opens a zip file and returns a ZipFile object.
     * @param filePath (String) The path to the zip file.
     * @return ZipFile object if successful, null otherwise.
     */

    public static ZipFile openZipFile(String filePath) {
        try {
            // Create a new ZipFile object
            if (bDebugLogZipUtils) LOG.info("## ZipUtils.openZipFile() ## Opening .zip file: {}", filePath);
            return new ZipFile(filePath);
        } catch (ZipException e) {
            LOG.error("## ZipUtils.openZipFile() ## Error opening zip file: {}", e.getMessage());
        } catch (IllegalArgumentException e) {
            LOG.error("## ZipUtils.openZipFile() ## Illegal argument: {}", e.getMessage());
        } catch (SecurityException e) {
            LOG.error("## ZipUtils.openZipFile() ## Security Exception: {}", e.getMessage());
        } catch (IOException e) {
            LOG.error("## ZipUtils.openZipFile() ## Failed to open file: {}", e.getMessage());
        }
        return null;
    }

    /**
     * Extracts a specific file from the zip and returns its content as a byte array.
     * @param zipFile (ZipFile) The opened zip file.
     * @param fileName (String) The name of the file to extract.
     * @return byte array of the file content if found, null otherwise.
     *
     * @see #openZipFile(String file)
     */

    public static byte[] getFileFromZip(ZipFile zipFile, String fileName) {
        try {
            // Normalize the input file name
            Path normalizedPath = Paths.get(fileName).normalize();
            if (normalizedPath.isAbsolute() || normalizedPath.startsWith("..")) {
                LOG.error("## ZipUtils.getFileFromZip() ## Invalid file path: {}", fileName);
                return null; // Reject invalid paths
            }
            // Get all the entries in the zip file
            Enumeration<? extends ZipEntry> entries = zipFile.entries();
            // Iterate through the entries
            while (entries.hasMoreElements()) {
                ZipEntry entry = entries.nextElement();
                // Check if the entry name ends with the specified file name
                if (entry.getName().equals(fileName)) {
                    try (InputStream iStream = zipFile.getInputStream(entry)) {
                        // Allocate a buffer based on the entry size
                        byte[] buffer = new byte[(int) entry.getSize()];
                        int bytesRead = iStream.read(buffer);
                        if (bytesRead != entry.getSize()) {
                            LOG.error("## ZipUtils.getFileFromZip() ## File '{}' was not fully read. Expected: {}, Read: {}", fileName, entry.getSize(), bytesRead);
                        }
                        return buffer;
                    }
                }
            }
        } catch (IOException e) {
            throw new RuntimeException(e);
        }
        // Return null if the file is not found
        LOG.error("## ZipUtils.getFileFromZip() ## File '{}' not found in zip file '{}'", fileName, zipFile.getName());
        return null;
    }

    //
    // Untested - Use with caution
    //

    @SuppressWarnings("unused")
    public static InputStream getFileStreamFromZip(ZipFile zipFile, String fileName) {
        LOG.info("## ZipUtils.getFileStreamFromZip() ## fileName: {}", fileName);
        try {
            // Normalize the input file name
            Path normalizedPath = Paths.get(fileName).normalize();
            String normalizedPathString = normalizedPath.toString().replace("\\", "/");
            if (Paths.get(normalizedPathString).isAbsolute() || normalizedPathString.startsWith("..")) {
                LOG.error("## ZipUtils.getFileStreamFromZip() ## Invalid file path: {}", fileName);
                return null; // Reject invalid paths
            }
            LOG.info("Path {}", normalizedPath);
            LOG.info("## ZipUtils.getFileStreamFromZip() ## normalizedPath: {}", normalizedPathString);
            // Match the normalized path with the zip entry names, return an InputStream if found
            Enumeration<? extends ZipEntry> entries = zipFile.entries();
            while (entries.hasMoreElements()) {
                ZipEntry entry = entries.nextElement();
                LOG.info("## ZipUtils.getFileStreamFromZip() ## entry: {}", entry.getName());
                if (entry.getName().equals(normalizedPathString)) {
                    if (bDebugLogZipUtils) LOG.info("## ZipUtils.getFileStreamFromZip() ## Found '{}' in '{}'", fileName, zipFile.getName());
                    return zipFile.getInputStream(entry); // Return the InputStream directly
                }
            }
        } catch (IOException e) {
            LOG.error("## ZipUtils.getFileStreamFromZip() ## Error reading file '{}' from zip: {}", fileName, e.getMessage());
        }
        LOG.error("## ZipUtils.getFileStreamFromZip() ## File '{}' not found in zip file '{}'", fileName, zipFile.getName());
        return null;
    }

    /**
     * Extracts a specific file from the zip and writes it to a temporary file, returning the File object.
     * The caller is responsible for deleting the temporary file when done.
     * @param zipFile (ZipFile) The opened zip file.
     * @param fileName (String) The name of the file to extract.
     * @return File object pointing to the temporary file if found, null otherwise.
     *
     * @see #openZipFile(String file)
     * @see #getFileFromZip(ZipFile zipFile, String fileName)
     */
    public static File getFileInputStreamFromZip(ZipFile zipFile, String fileName) {
        try {
            // Normalize the input file name
            Path normalizedPath = Paths.get(fileName).normalize();
            String normalizedPathString = normalizedPath.toString().replace("\\", "/");
            if (Paths.get(normalizedPathString).isAbsolute() || normalizedPathString.startsWith("..")) {
                LOG.error("## ZipUtils.getFileInputStreamFromZip() ## Invalid file path: {}", fileName);
                return null; // Reject invalid paths
            }

            Enumeration<? extends ZipEntry> entries = zipFile.entries();
            while (entries.hasMoreElements()) {
                ZipEntry entry = entries.nextElement();
                if (entry.getName().equals(normalizedPathString)) {
                    if (bDebugLogZipUtils) LOG.info("## ZipUtils.getFileInputStreamFromZip() ## Found '{}' in '{}'", fileName, zipFile.getName());

                    // Write the InputStream to a temporary file
                    File tempFile = File.createTempFile("AutoDriveEditor_", ".tmp");
                    try (InputStream inputStream = zipFile.getInputStream(entry);
                        FileOutputStream outputStream = new FileOutputStream(tempFile)) {
                        byte[] buffer = new byte[1024];
                        int bytesRead;
                        while ((bytesRead = inputStream.read(buffer)) != -1) {
                            outputStream.write(buffer, 0, bytesRead);
                        }
                    }
                    LOG.info("## ZipUtils.getFileStreamFromZip() ## Extracted '{}' to temp file '{}'", normalizedPathString, tempFile.getAbsolutePath());
                    return tempFile; // Return a FileInputStream
                }
            }
        } catch (IOException e) {
            LOG.error("## ZipUtils.getFileInputStreamFromZip() ## Error reading file '{}' from zip: {}", fileName, e.getMessage());
        }
        LOG.error("## ZipUtils.getFileInputStreamFromZip() ## File '{}' not found in zip file '{}'", fileName, zipFile.getName());
        return null;
    }

    /**
     * Searches for a specific file in the zip and returns its full path if found.
     * @param zipFile (ZipFile) The opened zip file.
     * @param fileName (String) The name of the file to search for.
     * @return The full path of the file within the zip if found, null otherwise.
     */

    public static String findFileInZip(ZipFile zipFile, String fileName) {
        if (zipFile == null) return null;
        // Get all the entries in the zip file
        try {
            Enumeration<? extends ZipEntry> entries = zipFile.entries();
            // Iterate through the entries
            while (entries.hasMoreElements()) {
                ZipEntry entry = entries.nextElement();
                // Check if the entry name ends with the specified filename
                if (entry.getName().endsWith(fileName)) {
                    if (bDebugLogZipUtils) LOG.info("## ZipUtils.findFileInZip() ## Found '{}' in '{}'", entry.getName(), zipFile.getName());
                    // If the file is found, return true
                    return entry.getName();
                }
            }
            // Return false if the file is not found
            LOG.info("## ZipUtils.findFileInZip() ## File '{}' not found in {}", fileName, zipFile.getName());
            return null;
        } catch (Exception e) {
            throw new RuntimeException(e);
        }
    }
}
