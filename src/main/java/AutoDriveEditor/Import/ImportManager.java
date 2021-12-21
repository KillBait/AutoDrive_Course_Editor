package AutoDriveEditor.Import;

import AutoDriveEditor.GUI.MenuBuilder;
import AutoDriveEditor.MapPanel.MapPanel;

import javax.imageio.ImageIO;
import java.awt.*;
import java.awt.image.BufferedImage;
import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;

import static AutoDriveEditor.Locale.LocaleManager.localeString;
import static AutoDriveEditor.Utils.LoggerUtils.LOG;

public class ImportManager {

    public static Boolean importFromFS19(String filename) {

        try {
            createDDSBufferImage(filename, 0 , 0);
        } catch (IOException e) {
            e.printStackTrace();
            return false;
        }

        MapPanel.isUsingConvertedImage = true;
        MenuBuilder.saveImageEnabled(true);
        return true;
    }

    public static Boolean importFromFS22(String filename) {

        try {
            createDDSBufferImage(filename, 1024, 1024);
        } catch (IOException e) {
            e.printStackTrace();
            return false;
        }
        MapPanel.isUsingConvertedImage = true;
        MenuBuilder.saveImageEnabled(true);
        return true;
    }

    public static void createDDSBufferImage(String filename, int offsetX, int offsetY) throws IOException {
        LOG.info("Creating Bufferimage from {}", filename );

        // load the DDS file into a buffer
        FileInputStream fis = new FileInputStream(filename);
        byte [] buffer = new byte[fis.available()];
        fis.read(buffer);
        fis.close();

        // convert the DDS file in buffer to an BufferImage
        int [] pixels = DDSReader.read(buffer, DDSReader.ARGB, 0);
        int width = DDSReader.getWidth(buffer);
        int height = DDSReader.getHeight(buffer);
        BufferedImage image = new BufferedImage(width, height, BufferedImage.TYPE_INT_ARGB);
        image.setRGB(0, 0, width, height, pixels, 0, width);
        LOG.info(" {} , {}", image.getWidth(), image.getHeight());

        // Scale the BufferImage to a size the editor can use ( 2048 x 2048 )

        GraphicsEnvironment ge = GraphicsEnvironment.getLocalGraphicsEnvironment();
        GraphicsDevice gd = ge.getDefaultScreenDevice();
        GraphicsConfiguration gc = gd.getDefaultConfiguration();

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
        if (offsetX != 0 || offsetY != 0) {
            BufferedImage crop = image.getSubimage(offsetX, offsetY, 2048, 2048);
            g.drawImage( crop, 0, 0, 2048, 2048, null);
        } else {
            g.drawImage( image, 0, 0, 2048, 2048, null);
        }
        g.dispose();

        // set the converted and resized image as the map image


        MapPanel.getMapPanel().setImage(scaledImage);
        MapPanel.forceMapImageRedraw();
        MapPanel.isUsingConvertedImage = true;
    }

    public static void exportMapImage(String filePath) {
        try {
            //String location = getCurrentLocation();
            //String path = location + "mapImages/" + fileName + ".png";
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
            ImageIO.write(MapPanel.getMapPanel().getImage(), "png", outputFile);
            LOG.info("{} {}", localeString.getString("console_map_saveimage_done"), outputFile.getAbsolutePath());
        } catch (IOException e) {
            e.printStackTrace();
        }

    }
}
