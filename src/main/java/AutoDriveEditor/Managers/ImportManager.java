package AutoDriveEditor.Managers;

import AutoDriveEditor.Import.DDSReader;
import AutoDriveEditor.XMLConfig.GameXML;
import net.miginfocom.swing.MigLayout;

import javax.swing.*;
import java.awt.*;
import java.awt.image.BufferedImage;
import java.io.FileInputStream;
import java.io.IOException;
import java.util.concurrent.atomic.AtomicBoolean;
import java.util.concurrent.atomic.AtomicReference;

import static AutoDriveEditor.AutoDriveEditor.editor;
import static AutoDriveEditor.AutoDriveEditor.getMapPanel;
import static AutoDriveEditor.Classes.UI_Components.MapInfoLabel.LabelStatus.IMPORTED;
import static AutoDriveEditor.Classes.Util_Classes.LoggerUtils.LOG;
import static AutoDriveEditor.GUI.Buttons.ActionBar.ConfigPanel.MapInfoButton.setMapImageLabel;
import static AutoDriveEditor.GUI.MapImage.setMapPanelImage;
import static AutoDriveEditor.GUI.MapImage.setPDAImage;
import static AutoDriveEditor.GUI.MapPanel.forceMapImageRedraw;
import static AutoDriveEditor.GUI.Menus.EditorMenu.saveImageEnabled;
import static AutoDriveEditor.GUI.TextPanel.showInTextArea;
import static AutoDriveEditor.Locale.LocaleManager.getLocaleString;

public class ImportManager {

    private static boolean isUsingImportedImage = false;

    public static void importDDSForFS19(String filename) {
        createBufferImageFromDDS(filename, GameXML.GameVersion.FS19_CONFIG);
    }

    public static void importDDSForFS22(String filename) { createBufferImageFromDDS(filename, GameXML.GameVersion.FS22_CONFIG);
    }

    public static void createBufferImageFromDDS(String filename, GameXML.GameVersion gameVersion) {

        JFrame progressFrame = new JFrame("DDS Import");
        progressFrame.setDefaultCloseOperation(JFrame.DISPOSE_ON_CLOSE);
        progressFrame.setSize(275, 100);
        progressFrame.setLayout(new MigLayout("fill, insets 10"));
        progressFrame.setLocationRelativeTo(getMapPanel());
        progressFrame.setVisible(true);

        // Create the progress bar
        JProgressBar progressBar = new JProgressBar();
        progressBar.setPreferredSize(new Dimension(250, 50));
        progressBar.setStringPainted(true);
        progressBar.setString("Converting DDS Image...");
        progressFrame.add(progressBar, BorderLayout.CENTER);

        LOG.info("## createBufferImageFromDDS() ## Importing DDS File {}", filename);
        AtomicReference<BufferedImage> decodeImage = new AtomicReference<>();
        AtomicBoolean success = new AtomicBoolean(false);
        // Create a thread to decode the DDS image
        Thread decodingThread = new Thread(() -> {
            try (FileInputStream fis = new FileInputStream(filename)) {
                decodeImage.set(DDSReader.read(fis, DDSReader.ARGB, 0, progressBar));
                fis.close();
                success.set(true);
            } catch (OutOfMemoryError memoryError) {
                JOptionPane.showMessageDialog(editor, getLocaleString("dialog_ddsreader_outofmemory"), getLocaleString("dialog_ddsreader_outofmemory_title"), JOptionPane.ERROR_MESSAGE);
                memoryError.printStackTrace();
                success.set(false);
            } catch (IOException e) {
                LOG.info("## createBufferImageFromDDS() ## Unable to read '{}'", filename);
                success.set(false);
            } finally {
                SwingUtilities.invokeLater(progressFrame::dispose);
                if (success.get()) {
                    LOG.info("## createBufferImageFromDDS() ## DDS Decoding successful, creating buffer image...");
                    BufferedImage scaledImage = createBufferImage(decodeImage.get(), gameVersion);
                    // set the converted/resized image as the current map image
                    LOG.info("## createBufferImage() ## Scaled Image size {} , {}", scaledImage.getWidth(), scaledImage.getHeight());
                    setPDAImage(scaledImage);
                    setMapPanelImage(scaledImage, false);
                    forceMapImageRedraw();
                    setIsEditorUsingImportedImage(true);
                    saveImageEnabled(true);
                    setMapImageLabel(IMPORTED);
                    showInTextArea("Import of " + gameVersion + " mapImage '" + filename + "' Successful", true, true);
                } else {
                    LOG.error("## createBufferImageFromDDS() ## DDS Decoding failed or returned null image.");
                    JOptionPane.showMessageDialog(editor, getLocaleString("dialog_ddsreader_error"), getLocaleString("dialog_ddsreader_error_title"), JOptionPane.ERROR_MESSAGE);
                }
            }
        });
        decodingThread.setName("DDS Decode");
        decodingThread.start();
    }

    public static BufferedImage createBufferImage(BufferedImage decodeImage, GameXML.GameVersion fsVersion) {

        // Scale the BufferImage to a size the editor can use ( 2048 x 2048 )

        GraphicsEnvironment ge = GraphicsEnvironment.getLocalGraphicsEnvironment();
        GraphicsDevice gd = ge.getDefaultScreenDevice();
        GraphicsConfiguration gc = gd.getDefaultConfiguration();

        LOG.info("## createBufferImage() ## Creating usable image from Decoded DDS");

        BufferedImage scaledImage = gc.createCompatibleImage(2048, 2048, Transparency.OPAQUE);
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
        if (fsVersion == GameXML.GameVersion.FS19_CONFIG) {
            g.drawImage(decodeImage, 0, 0, 2048, 2048, null);
        } else if (fsVersion == GameXML.GameVersion.FS22_CONFIG || fsVersion == GameXML.GameVersion.FS25_CONFIG) {
            LOG.info("## createBufferImage() ## Scaling converted DDS Image ( {}x{} ) to correct size..", decodeImage.getWidth(), decodeImage.getHeight());
            BufferedImage crop = decodeImage.getSubimage(decodeImage.getWidth() / 4, decodeImage.getHeight() / 4, decodeImage.getWidth() / 2, decodeImage.getHeight() / 2);
            g.drawImage(crop, 0, 0, 2048, 2048, null);
        }
        g.dispose();
        return scaledImage;
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
