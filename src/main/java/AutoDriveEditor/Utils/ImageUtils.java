package AutoDriveEditor.Utils;

import AutoDriveEditor.AutoDriveEditor;

import javax.imageio.ImageIO;
import javax.swing.*;
import java.awt.*;
import java.awt.image.BufferedImage;
import java.io.IOException;
import java.net.URL;

import static AutoDriveEditor.Utils.LoggerUtils.LOG;

public class ImageUtils {

    public static BufferedImage getNewBufferedImage(int width, int height, int transparency) {
        GraphicsEnvironment ge = GraphicsEnvironment.getLocalGraphicsEnvironment();
        GraphicsDevice gd = ge.getDefaultScreenDevice();
        GraphicsConfiguration gc = gd.getDefaultConfiguration();
        //Graphics2D g = (Graphics2D) image.getGraphics();
        return gc.createCompatibleImage( width, height, transparency);
    }

    public static BufferedImage loadImage(String fileName) {
        try {
            URL url = AutoDriveEditor.class.getResource("/" + fileName);
            if (url != null) {

                BufferedImage file = ImageIO.read(url);
                BufferedImage image = getNewBufferedImage(file.getWidth(), file.getHeight(), Transparency.BITMASK);
                Graphics2D g = (Graphics2D) image.getGraphics();
                g.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_OFF);
                g.setRenderingHint(RenderingHints.KEY_ALPHA_INTERPOLATION, RenderingHints.VALUE_ALPHA_INTERPOLATION_SPEED);
                g.setRenderingHint(RenderingHints.KEY_COLOR_RENDERING, RenderingHints.VALUE_COLOR_RENDER_SPEED);
                g.setRenderingHint(RenderingHints.KEY_DITHERING, RenderingHints.VALUE_DITHER_DISABLE);
                g.setRenderingHint(RenderingHints.KEY_FRACTIONALMETRICS, RenderingHints.VALUE_FRACTIONALMETRICS_OFF);
                g.setRenderingHint(RenderingHints.KEY_INTERPOLATION, RenderingHints.VALUE_INTERPOLATION_NEAREST_NEIGHBOR);
                g.setRenderingHint(RenderingHints.KEY_RENDERING, RenderingHints.VALUE_RENDER_SPEED);
                g.setRenderingHint(RenderingHints.KEY_STROKE_CONTROL, RenderingHints.VALUE_STROKE_PURE);
                g.setRenderingHint(RenderingHints.KEY_TEXT_ANTIALIASING, RenderingHints.VALUE_TEXT_ANTIALIAS_OFF);
                g.drawImage(file,0,0,null);
                g.dispose();
                return image;
            }
        } catch (IOException e) {
            LOG.error(e.getMessage(), e);
        }
        return null;
    }

    public static ImageIcon getImageIcon(String name) {
        try {
            URL url = AutoDriveEditor.class.getResource("/" + name);
            if (url != null) {
                BufferedImage newImage = ImageIO.read(url);
                return new ImageIcon(newImage);
            }
        } catch (IOException e) {
            LOG.error(e.getMessage(), e);
        }
        return null;
    }
}
