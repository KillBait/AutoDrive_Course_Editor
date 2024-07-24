package AutoDriveEditor.Managers;

import AutoDriveEditor.Import.DDSReader;

import javax.imageio.ImageIO;
import javax.swing.*;
import java.awt.*;
import java.awt.image.BufferedImage;
import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;

import static AutoDriveEditor.AutoDriveEditor.editor;
import static AutoDriveEditor.Classes.MapImage.*;
import static AutoDriveEditor.GUI.MapPanel.forceMapImageRedraw;
import static AutoDriveEditor.GUI.Menus.EditorMenu.saveImageEnabled;
import static AutoDriveEditor.Locale.LocaleManager.getLocaleString;
import static AutoDriveEditor.Utils.LoggerUtils.LOG;

public class ImportManager {

    public static final int FS19_IMAGE = 0;
    public static final int FS22_IMAGE = 1;
    private static boolean isUsingImportedImage = false;

    public static Boolean  importFromFS19(String filename) {
        boolean success = createBufferImageFromDDS(filename, FS19_IMAGE);
        if (!success) return false;
        setIsEditorUsingImportedImage(true);
        saveImageEnabled(true);
        return true;
    }

    public static Boolean importFromFS22(String filename) {
        boolean success = createBufferImageFromDDS(filename, FS22_IMAGE);
        if (!success) return false;
        setIsEditorUsingImportedImage(true);
        saveImageEnabled(true);
        return true;
    }

    public static boolean createBufferImageFromDDS(String filename, int gameImage) {
        byte [] buffer;
        BufferedImage image;
        LOG.info("Reading DDS File {}", filename );

        // load the DDS file into a buffer

        try {
            FileInputStream fis = new FileInputStream(filename);
            buffer = new byte[fis.available()];
            //noinspection ResultOfMethodCallIgnored
            fis.read(buffer);
            fis.close();
        } catch (IOException e) {
            LOG.info("Unable to read Source File");
            return false;
        }

        // convert the DDS file in buffer to an BufferImage

        try {
            int [] pixels = DDSReader.read(buffer, DDSReader.ARGB, 0);
            int width = DDSReader.getWidth(buffer);
            int height = DDSReader.getHeight(buffer);
            image = new BufferedImage(width, height, BufferedImage.TYPE_INT_RGB);
            image.setRGB(0, 0, width, height, pixels, 0, width);
            LOG.info("DDS Image size {} , {}", image.getWidth(), image.getHeight());
        } catch (OutOfMemoryError memoryError) {
            JOptionPane.showMessageDialog(editor, getLocaleString("dialog_ddsreader_outofmemory"), getLocaleString("dialog_ddsreader_outofmemory_title"), JOptionPane.ERROR_MESSAGE);
            return false;
        }

        // Scale the BufferImage to a size the editor can use ( 2048 x 2048 )

        GraphicsEnvironment ge = GraphicsEnvironment.getLocalGraphicsEnvironment();
        GraphicsDevice gd = ge.getDefaultScreenDevice();
        GraphicsConfiguration gc = gd.getDefaultConfiguration();

        LOG.info("Creating usable image from Decoded DDS");

        BufferedImage scaledImage = gc.createCompatibleImage( 2048, 2048, Transparency.OPAQUE);
        Graphics2D g = (Graphics2D) scaledImage.getGraphics();
        g.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);
        g.setRenderingHint(RenderingHints.KEY_ALPHA_INTERPOLATION, RenderingHints.VALUE_ALPHA_INTERPOLATION_SPEED);
        g.setRenderingHint(RenderingHints.KEY_COLOR_RENDERING, RenderingHints.VALUE_COLOR_RENDER_QUALITY);
        g.setRenderingHint(RenderingHints.KEY_DITHERING, RenderingHints.VALUE_DITHER_ENABLE);
        g.setRenderingHint(RenderingHints.KEY_FRACTIONALMETRICS, RenderingHints.VALUE_FRACTIONALMETRICS_ON);
        g.setRenderingHint(RenderingHints.KEY_INTERPOLATION, RenderingHints.VALUE_INTERPOLATION_BILINEAR);
        g.setRenderingHint(RenderingHints.KEY_RENDERING, RenderingHints.VALUE_RENDER_QUALITY);
        g.setRenderingHint(RenderingHints.KEY_STROKE_CONTROL, RenderingHints.VALUE_STROKE_DEFAULT);
        g.setRenderingHint(RenderingHints.KEY_TEXT_ANTIALIASING, RenderingHints.VALUE_TEXT_ANTIALIAS_DEFAULT);
        g.setRenderingHint(RenderingHints.KEY_RESOLUTION_VARIANT, RenderingHints.VALUE_RESOLUTION_VARIANT_SIZE_FIT);
        if (gameImage == FS19_IMAGE) {
            g.drawImage( image, 0, 0, 2048, 2048, null);
        } else if (gameImage == FS22_IMAGE) {
            LOG.info("Scaling converted DDS Image to correct size..");
            BufferedImage crop = image.getSubimage(image.getWidth() / 4, image.getHeight() /4, image.getWidth() / 2, image.getHeight() / 2);
            g.drawImage( crop, 0, 0, 2048, 2048, null);
        }
        g.dispose();

        // set the converted/resized image as the current map image

        setPDAImage(scaledImage);
        setMapPanelImage(scaledImage, false);
        forceMapImageRedraw();
        setIsEditorUsingImportedImage(true);
        return true;
    }

    public static boolean exportImageToDisk(BufferedImage image, String filePath) {
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

    //
    // Getters
    //

    public static boolean getIsEditorUsingImportedImage() { return isUsingImportedImage; }


    //
    // Setters
    //

    public static void setIsEditorUsingImportedImage(boolean result) {
        isUsingImportedImage = result;
    }
}
