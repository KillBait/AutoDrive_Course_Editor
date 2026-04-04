package AutoDriveEditor.GUI.Menus.ImportMenu;

import AutoDriveEditor.Classes.Util_Classes.FSUtils;
import AutoDriveEditor.Classes.Util_Classes.XMLUtils;
import AutoDriveEditor.GUI.Menus.JMenuItemBase;
import AutoDriveEditor.Import.DDSReader;
import net.miginfocom.swing.MigLayout;
import org.w3c.dom.Node;
import org.w3c.dom.NodeList;

import javax.imageio.ImageIO;
import javax.swing.*;
import javax.swing.filechooser.FileFilter;
import java.awt.*;
import java.awt.event.*;
import java.awt.geom.RoundRectangle2D;
import java.awt.image.BufferedImage;
import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.util.HashMap;
import java.util.Locale;
import java.util.Map;
import java.util.concurrent.atomic.AtomicBoolean;

import java.util.zip.ZipFile;

import static AutoDriveEditor.AutoDriveEditor.editor;
import static AutoDriveEditor.AutoDriveEditor.getMapPanel;
import static AutoDriveEditor.Classes.Util_Classes.FileUtils.createFileChooser;
import static AutoDriveEditor.Classes.Util_Classes.FileUtils.createFileFilter;
import static AutoDriveEditor.Classes.Util_Classes.LoggerUtils.LOG;
import static AutoDriveEditor.Classes.Util_Classes.ZipUtils.*;
import static AutoDriveEditor.GUI.Menus.DebugMenu.Logging.LogZipUtilsMenu.bDebugLogZipUtils;
import static AutoDriveEditor.Locale.LocaleManager.getLocaleString;
import static AutoDriveEditor.Managers.ExportManager.*;
import static AutoDriveEditor.Managers.IconManager.getTractorImage;
import static AutoDriveEditor.XMLConfig.GameXML.lastUsedLocation;

public class ImportFromZipMenu extends JMenuItemBase {

    public static JMenuItem menu_ImportFromZip;
    public static boolean isPreviewWindowOpen = false;
    ZipFile zipFile;

    BufferedImage decodedDDSImage;
    BufferedImage decodedHeightmapImage;

    BufferedImage previewMapImage = new BufferedImage(256,256, Transparency.OPAQUE);
    BufferedImage previewHeightmapImage = new BufferedImage(256,256, Transparency.OPAQUE);

    Map<String, String> mapLocaleNames = new HashMap<>();

    /**
     * A custom JLabel that supports rounded corners for displaying images.
     * This class is used for displaying the image previews only.
     */

    public static class RoundedImageLabel extends JLabel {
        private final int cornerRadius;

        public RoundedImageLabel(int cornerRadius) {
            this.cornerRadius = cornerRadius;
        }

        @Override
        protected void paintComponent(Graphics g) {
            Graphics2D g2 = (Graphics2D) g.create();
            try {
                // Enable antialiasing for smooth edges
                g2.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);
                g2.setRenderingHint(RenderingHints.KEY_ALPHA_INTERPOLATION, RenderingHints.VALUE_ALPHA_INTERPOLATION_QUALITY);
                g2.setRenderingHint(RenderingHints.KEY_RENDERING, RenderingHints.VALUE_RENDER_QUALITY);
                // Create a rounded rectangle clipping area
                Shape clip = new RoundRectangle2D.Float(0, 0, getWidth(), getHeight(), cornerRadius, cornerRadius);
                g2.setClip(clip);
                // Paint the image or background
                super.paintComponent(g2);
            } finally {
                g2.dispose();
            }
        }

        @Override
        protected void paintBorder(Graphics g) {
            Graphics2D g2 = (Graphics2D) g.create();
            try {
                // Enable antialiasing for smooth edges
                g2.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);
                // Draw the rounded border
                g2.setColor((Color)UIManager.getLookAndFeel().getDefaults().get("Button.borderColor"));
                g2.draw(new RoundRectangle2D.Float(0, 0, getWidth()-1, getHeight()-1, cornerRadius, cornerRadius));
            } finally {
                g2.dispose();
            }
        }
    }

    public ImportFromZipMenu() {
        menu_ImportFromZip = makeMenuItem("menu_import_zip", KeyEvent.VK_Z, InputEvent.ALT_DOWN_MASK, true);
    }

    @Override
    public void actionPerformed(ActionEvent e) {
        // Prevent opening multiple preview windows
        if (isPreviewWindowOpen) return;
        // Create a file filter for zip files
        FileFilter zipFilter = createFileFilter("zip", "Zip Archive (.zip)");
        JFileChooser lfc = createFileChooser("dialog_load_config_xml_title", JFileChooser.FILES_ONLY, true, lastUsedLocation, zipFilter);
        // Open the file chooser dialogue
        if (lfc.showOpenDialog(editor) == JFileChooser.APPROVE_OPTION) {
            lastUsedLocation = lfc.getCurrentDirectory().getAbsolutePath();
            // Open the selected zip file
            zipFile = openZipFile(lfc.getSelectedFile().getAbsolutePath());
            if (zipFile != null) {
                // Create a new ZipFile instance
                LOG.info("## ImportFromZipMenu.actionPerformed() ## Zip file opened: {}", zipFile.getName());
                // create a byte array to hold the contents of the modDesc.xml file
                byte[] modDescFile = getFileFromZip(zipFile, "modDesc.xml");
                if (modDescFile != null ) {
                    // Check if the modDesc.xml file is present, create an XMLReader if true
                    XMLUtils.XMLReader modDescReader = new XMLUtils.XMLReader(modDescFile);

                    // Try to auto-detect the FS version from the modDesc.xml file
                    int descVersion = Integer.parseInt(modDescReader.getAttributeValue("modDesc", "descVersion"));
                    FSUtils.GameVersion fsVersion = FSUtils.getFSVersion(descVersion);
                    LOG.info("## ImportFromZipMenu.actionPerformed() ## Auto detected FS Version from modDesc.xml : {}", fsVersion.getDescription());
                    // Get the maps config xml file from the modDesc.xml file
                    String configFilename = modDescReader.getAttributeValue("map", "configFilename");
                    // Try and match the setupPreview title locale to the system language
                    String titleLocalString = detectUsrLocale(modDescReader);
                    if (!titleLocalString.isEmpty()) {
                        LOG.info("## ImportFromZipMenu.actionPerformed() ## Localized title: <{}> '{}'", Locale.getDefault().getLanguage(), titleLocalString);
                        if (!configFilename.isEmpty()) {
                            LOG.info("## ImportFromZipMenu.actionPerformed() ## modDesc.xml config filename = {}", configFilename);
                            byte[] configFile = getFileFromZip(zipFile, configFilename);
                            XMLUtils.XMLReader configXmlReader = new XMLUtils.XMLReader(configFile);
                            String imagePath = configXmlReader.getAttributeValue("map", "imageFilename");
                            if (!imagePath.isEmpty()) {
                                imagePath = imagePath.replace(".png", ".dds");
                                LOG.info("## ImportFromZipMenu.actionPerformed() ## imageFilename = {}", imagePath);
                                String heightmapPath = findFileInZip(zipFile, "dem.png");
                                if (!heightmapPath.isEmpty()) {
                                    LOG.info("## ImportFromZipMenu.actionPerformed() ## heightmapFilename = {}", heightmapPath);
                                    setupPreview(titleLocalString, imagePath, heightmapPath, fsVersion);
                                }
                            }
                        }
                    } else {
                        LOG.error("## ImportFromZipMenu.actionPerformed() ## No localized map title found in modDesc.xml");
                    }
                } else {
                    LOG.error("## ImportFromZipMenu.actionPerformed() ## no 'modDesc.xml' found in zip file");
                }
            }
        }
    }

    private String detectUsrLocale(XMLUtils.XMLReader xmlReader) {
        NodeList titleNodes = xmlReader.getChildNode("map", "title");
        if (titleNodes != null && titleNodes.getLength() > 0) {
            NodeList languageNodes = titleNodes.item(0).getChildNodes(); // Get the <title> node child nodes
            // add all the language nodes to a map
            mapLocaleNames.clear();
            for (int i = 0; i < languageNodes.getLength(); i++) {
                Node languageNode = languageNodes.item(i);
                if (languageNode.getNodeType() == Node.ELEMENT_NODE) {
                    if (bDebugLogZipUtils) LOG.info("## detectUsrLocale() ## Found language in modDesc.xml : <{}> {}", languageNode.getNodeName(), languageNode.getTextContent());
                    mapLocaleNames.put(languageNode.getNodeName(), languageNode.getTextContent());
                }
            }
            String systemLanguage = Locale.getDefault().getLanguage();
            if (bDebugLogZipUtils) LOG.info("## detectUsrLocale() ## System locale: {}", systemLanguage);
            // Check if the system language is in the language map, return result if true
            if (mapLocaleNames.containsKey(systemLanguage)) {
                return mapLocaleNames.get(systemLanguage);
            } else {
                LOG.info("## detectUsrLocale() ## No match to system language: {} , returning default <en>", systemLanguage);
                return mapLocaleNames.getOrDefault("en", "");
            }
        } else {
            LOG.error("## detectUsrLocale() ## No <title> node found in <setupPreview>");
            return "";
        }
    }


    private void setupPreview(String localeTitle, String imagePath, String heightmapPath, FSUtils.GameVersion fsVersion) {
        // Create a window for the decoding progress bar
        JFrame progressFrame = new JFrame("Decoding");
        progressFrame.setDefaultCloseOperation(JFrame.DISPOSE_ON_CLOSE);
        progressFrame.setSize(275, 100);
        progressFrame.setLayout(new MigLayout("insets 10"));
        progressFrame.setLocationRelativeTo(getMapPanel());
        progressFrame.setVisible(true);

        // Create the progress bar
        JProgressBar progressBar = new JProgressBar();
        progressBar.setPreferredSize(new Dimension(250,50));
        progressBar.setStringPainted(true);
        progressBar.setString("Processing DDS Image...");
        progressFrame.add(progressBar, BorderLayout.CENTER);

        AtomicBoolean success = new AtomicBoolean(false);

        try {
            File tempfile = getFileInputStreamFromZip(zipFile, imagePath);
            if (tempfile != null) {
                Thread decodingThread = new Thread(() -> {
                    try (FileInputStream fis = new FileInputStream(tempfile)) {
                        decodedDDSImage = DDSReader.read(fis, DDSReader.ARGB, 0, progressBar);
                        fis.close();
                        if (tempfile.delete()) {
                            LOG.info("Temporary DDS file deleted successfully");
                        } else {
                            LOG.error("Failed to delete temporary DDS file");
                        }
                        success.set(true);
                    } catch (OutOfMemoryError memoryError) {
                        JOptionPane.showMessageDialog(editor, getLocaleString("dialog_ddsreader_outofmemory"), getLocaleString("dialog_ddsreader_outofmemory_title"), JOptionPane.ERROR_MESSAGE);
                        memoryError.printStackTrace();
                        success.set(false);
                    } catch (IOException e) {
                        LOG.info("Unable to read '{}'", tempfile);
                        JOptionPane.showMessageDialog(editor, getLocaleString("dialog_ddsreader_file_open_error"), getLocaleString("dialog_ddsreader_file_open_error_title"), JOptionPane.ERROR_MESSAGE);
                        success.set(false);
                    } finally {
                        progressFrame.dispose();
                        if (success.get()) {
                            if (getMapImagePreview(decodedDDSImage, fsVersion)) {
                                if (getHeightMapPreview(heightmapPath)) {
                                    openPreviewWindow(localeTitle, previewMapImage, previewHeightmapImage, fsVersion);
                                }
                            }
                        }
                    }
                });
                decodingThread.setName("DDS Decoder");
                decodingThread.start();
            }
        } catch (RuntimeException e) {
            throw new RuntimeException(e);
        }
    }

    private boolean getMapImagePreview(BufferedImage image, FSUtils.GameVersion fsVersion) {
        if (image != null) {
            Graphics2D g = (Graphics2D) previewMapImage.getGraphics();
            if (fsVersion == FSUtils.GameVersion.FS19) {
                g.drawImage(image, 0, 0, 256, 256, null);
            } else {
                // For FS22 and FS25, crop the image to remove the border
                BufferedImage crop = image.getSubimage(image.getWidth() / 4, image.getHeight() / 4, image.getWidth() / 2, image.getHeight() / 2);
                g.drawImage(crop, 0, 0, 256, 256, null);
            }
            LOG.info("Created {} preview image successfully", fsVersion);
            g.dispose();
            return true;
        } else {
            LOG.error("Image is null: Failed to create preview");
        }
        return false;
    }

    private boolean getHeightMapPreview(String heightmapPath) {
        if (!heightmapPath.isEmpty()) {
            // Extract the heightmap image from the zip file
            File tempfile = getFileInputStreamFromZip(zipFile, heightmapPath);
            if (tempfile != null) {
                try {
                    decodedHeightmapImage = ImageIO.read(tempfile);
                    Graphics2D g = (Graphics2D) previewHeightmapImage.getGraphics();
                    g.drawImage(decodedHeightmapImage, 0, 0, 256, 256, null);
                    g.dispose();
                    LOG.info("Created heightmap preview successfully");
                    if (tempfile.delete()) {
                        LOG.info("Temporary heightmap file deleted successfully");
                    } else {
                        LOG.error("Failed to delete temporary heightmap file");
                    }
                    return true;
                } catch (IOException e) {
                    throw new RuntimeException(e);
                }
            }
        }
        return false;
    }

    private void openPreviewWindow(String localeTitle, BufferedImage previewMapImage, BufferedImage previewHeightmapImage, FSUtils.GameVersion fsVersion) {
        // Create a new instance of the ImportFromZipWindow
        JFrame windowFrame = new JFrame();
        SwingUtilities.invokeLater(() -> {
            windowFrame.dispatchEvent(new WindowEvent(windowFrame, WindowEvent.WINDOW_CLOSING));
            windowFrame.addWindowListener(new WindowAdapter() {
                @Override
                public void windowClosed(WindowEvent e) {
                    isPreviewWindowOpen = false;
                    super.windowClosed(e);
                }
            });
            windowFrame.setDefaultCloseOperation(JFrame.DISPOSE_ON_CLOSE);
            windowFrame.setTitle(getLocaleString("menu_import_zip_window_title"));
            windowFrame.setIconImage(getTractorImage());
            windowFrame.setPreferredSize(new Dimension(610, 480));
            windowFrame.setResizable(true);
            windowFrame.pack();
            windowFrame.setLocationRelativeTo(getMapPanel());
            windowFrame.setVisible(true);
            windowFrame.setAlwaysOnTop(false);
            windowFrame.setLayout(new MigLayout("insets 0 15 15 15"));
            isPreviewWindowOpen = true;

            // Add the map label and preview to the main panel
            JPanel mapPreviewPanel = new JPanel(new MigLayout());
            mapPreviewPanel.add(new JLabel("<html><b>" + getLocaleString("menu_import_zip_map_image")), "center, wrap");
            RoundedImageLabel mapPreview = new RoundedImageLabel(20);
            mapPreview.setIcon(new ImageIcon(previewMapImage));
            mapPreviewPanel.add(mapPreview, "center");

            windowFrame.add(mapPreviewPanel);

            // Add the heightmap label and image preview to the main panel
            JPanel heightmapPreviewPanel = new JPanel(new MigLayout());
            heightmapPreviewPanel.add(new JLabel("<html><b>" + getLocaleString("menu_import_zip_heightmap")), "center, wrap");
            RoundedImageLabel heightmapPreviewLabel = new RoundedImageLabel(20);
            heightmapPreviewLabel.setIcon(new ImageIcon(previewHeightmapImage));
            heightmapPreviewPanel.add(heightmapPreviewLabel, "center");

            windowFrame.add(heightmapPreviewPanel, "wrap");

            // Add the map name Panel to the main window frame
            JPanel versionPanel = new JPanel(new MigLayout());
            versionPanel.putClientProperty("FlatLaf.style", "border: 0,0,0,0,@disabledForeground,1,16; background: darken($Panel.background,5%)");

            // Add the auto-detected FS version to a label and display it
            JLabel gameVersionLabel = new JLabel("<html>" + getLocaleString("menu_import_zip_autodetect_version") + " : <b>'" + fsVersion.getDescription() + "'</b></html>");
            versionPanel.add(gameVersionLabel, "span, center, wrap");

            // Create a string array of all the game version descriptions
            FSUtils.GameVersion[] vals = FSUtils.GameVersion.values();
            String[] res = new String[vals.length-1];
            // skip the first value (NOT_SUPPORTED)
            for (int i = 1; i < vals.length; i++) {
                res[i-1] = vals[i].getDescription();
            }


            // Create the override game version checkbox early, so it can be enabled/disabled by the override checkbox
            JComboBox<String> overrideVersionComboBox = new JComboBox<>(res);
            overrideVersionComboBox.setEnabled(false);
            // Set the selected index to the auto-detected version
            for (int i = 0; i < res.length; i++) {
                if (res[i].equals(fsVersion.getDescription())) {
                    // Set the selected index to the index of the locale title
                    overrideVersionComboBox.setSelectedIndex(i);
                    break;
                }
            }

            JCheckBox overrideVersionCheckbox = new JCheckBox(getLocaleString("menu_import_zip_override"));
            overrideVersionCheckbox.setHorizontalTextPosition(SwingConstants.LEFT);
            overrideVersionCheckbox.setIconTextGap(10);
            overrideVersionCheckbox.addItemListener( e -> overrideVersionComboBox.setEnabled(overrideVersionCheckbox.isSelected()));
            versionPanel.add(overrideVersionCheckbox);
            versionPanel.add(overrideVersionComboBox, "span, center");

            windowFrame.add(versionPanel, "center");

            // Add the map name Panel to the main window frame
            JPanel mapNamePanel = new JPanel(new MigLayout());
            mapNamePanel.putClientProperty("FlatLaf.style", "border: 0,0,0,0,@disabledForeground,1,16; background: darken($Panel.background,5%)");

            // Add the auto-detected name to a label and display it
            JLabel mapNameLabel = new JLabel("<html>" + getLocaleString("menu_import_zip_autodetect_name") + " : <b>'" + localeTitle + "'</b></html>");
            mapNamePanel.add(mapNameLabel, "span, center, wrap");

            // Create the override name checkbox early, so it can be enabled/disabled by the override checkbox
            String[] localeNames = mapLocaleNames.values().toArray(new String[0]);
            JComboBox<String> overrideNameComboBox = new JComboBox<>(localeNames);
            overrideNameComboBox.setEnabled(false);
            for (int i = 0; i < localeNames.length; i++) {
                if (localeNames[i].equals(localeTitle)) {
                    // Set the selected index to the index of the locale title
                    overrideNameComboBox.setSelectedIndex(i);
                    break;
                }
            }

            // Add item listener for debug purposes only
            overrideNameComboBox.addItemListener(e -> {
                if (bDebugLogZipUtils) {
                    if (overrideNameComboBox.isEnabled() && e.getStateChange() == ItemEvent.SELECTED) {
                        // If a name override is selected, set the selectedName to the selected value
                        String selectedLocaleName = (String) overrideNameComboBox.getSelectedItem();
                        if (selectedLocaleName != null) {
                            LOG.info("Override Map Name Selected: {}", selectedLocaleName);
                        }
                    }
                }
            });


            // Add the override name checkbox
            JCheckBox overrideMapNameCheckbox = new JCheckBox(getLocaleString("menu_import_zip_override"));
            overrideMapNameCheckbox.setHorizontalTextPosition(SwingConstants.LEFT);
            overrideMapNameCheckbox.setIconTextGap(10);
            overrideMapNameCheckbox.addItemListener( e -> overrideNameComboBox.setEnabled(overrideMapNameCheckbox.isSelected()));
            mapNamePanel.add(overrideMapNameCheckbox);

            mapNamePanel.add(overrideNameComboBox);
            windowFrame.add(mapNamePanel, "center, wrap");

            JPanel confirmPanel = new JPanel(new MigLayout("", "[]160[]"));

            // Add the import button to the confirmPanel
            JButton importButton = new JButton(getLocaleString("menu_import_zip_import"));
            importButton.addActionListener(e1 -> {
                FSUtils.GameVersion selectedVersion = (overrideVersionCheckbox.isSelected()) ? FSUtils.GameVersion.getGameVersion(overrideVersionComboBox.getSelectedItem().toString()) : fsVersion;
                String selectedName = (overrideMapNameCheckbox.isSelected()) ? overrideNameComboBox.getSelectedItem().toString() : localeTitle;

                LOG.info("Exporting map name '{}' for {}", selectedName, selectedVersion);
                exportImages(selectedVersion, selectedName);
                windowFrame.dispose();
            });
            importButton.setPreferredSize(new Dimension(importButton.getPreferredSize().width+20, importButton.getPreferredSize().height+10));
            confirmPanel.add(importButton, "left, gapleft 40");


            // Add the cancel button to the confirmPanel
            JButton cancelButton = new JButton(getLocaleString("menu_import_zip_cancel"));
            cancelButton.addActionListener(e1 -> {
                if (bDebugLogZipUtils) LOG.info("Import cancelled by user");
                windowFrame.dispose();
            });
            cancelButton.setPreferredSize(new Dimension(cancelButton.getPreferredSize().width+20, cancelButton.getPreferredSize().height+10));

            confirmPanel.add(cancelButton, "right, gapright 40");
            windowFrame.add(confirmPanel, "gaptop 20, span, center");
        });
    }

    private void exportImages(FSUtils.GameVersion gameVersion, String name) {
        // replace any blank spaces in the folder name with underscores
        LOG.info("Exporting {} map with name: {}", gameVersion, name);
        String nameAdjust = name.replace(" ", "_").replace(".", "_");
        // Create the export folder (if needed)
        String path = createMapImagesFolder(nameAdjust);
        if (path != null) {
            LOG.info("Exporting {} images to folder: {}", gameVersion.name(), path);
            String mapImagePath = path + File.separator;
            exportMapImageToDisk(decodedDDSImage, mapImagePath + nameAdjust + ".png", gameVersion);
            exportHeightMapImageToDisk(decodedHeightmapImage, mapImagePath + nameAdjust + "_HeightMap.png");
        }
    }
}