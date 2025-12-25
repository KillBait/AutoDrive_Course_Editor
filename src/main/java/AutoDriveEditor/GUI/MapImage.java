package AutoDriveEditor.GUI;

import AutoDriveEditor.AutoDriveEditor;
import AutoDriveEditor.Classes.SimpleImageInfo;
import AutoDriveEditor.RoadNetwork.MapNode;
import AutoDriveEditor.RoadNetwork.RoadMap;

import javax.imageio.ImageIO;
import javax.swing.*;
import java.awt.*;
import java.awt.image.BufferedImage;
import java.awt.image.DataBufferUShort;
import java.io.File;
import java.io.IOException;
import java.net.MalformedURLException;
import java.net.URL;
import java.nio.file.Files;
import java.nio.file.Paths;
import java.util.LinkedList;
import java.util.Objects;

import static AutoDriveEditor.AutoDriveEditor.editor;
import static AutoDriveEditor.AutoDriveEditor.getMapPanel;
import static AutoDriveEditor.Classes.UI_Components.MapInfoLabel.LabelStatus.*;
import static AutoDriveEditor.Classes.Util_Classes.FileUtils.*;
import static AutoDriveEditor.Classes.Util_Classes.ImageUtils.getNewBufferImage;
import static AutoDriveEditor.Classes.Util_Classes.LoggerUtils.LOG;
import static AutoDriveEditor.GUI.Buttons.ActionBar.ConfigPanel.MapInfoButton.setHeightmapImageLabel;
import static AutoDriveEditor.GUI.Buttons.ActionBar.ConfigPanel.MapInfoButton.setMapImageLabel;
import static AutoDriveEditor.GUI.MapPanel.*;
import static AutoDriveEditor.GUI.Menus.DebugMenu.Logging.LogFileIOMenu.bDebugLogFileIO;
import static AutoDriveEditor.GUI.Menus.EditorMenu.heightmapMenuEnabled;
import static AutoDriveEditor.GUI.Menus.EditorMenu.mapImageMenuEnabled;
import static AutoDriveEditor.GUI.TextPanel.showInTextArea;
import static AutoDriveEditor.Locale.LocaleManager.getLocaleString;
import static AutoDriveEditor.XMLConfig.EditorXML.bUseOnlineMapImages;
import static AutoDriveEditor.XMLConfig.GameXML.*;
import static java.nio.file.StandardCopyOption.REPLACE_EXISTING;

@SuppressWarnings("LoggingSimilarMessage")
public class MapImage {

    public static BufferedImage pdaImage;
    public static BufferedImage mapPanelImage;

    public enum HeightmapFormat { BYTE, USHORT_GREY }
    public static HeightmapFormat heightmapType;
    public static BufferedImage heightMapImage;
    public static double heightMapScale = 1;

    // Extract the 16-bit pixel values
    public static BufferedImage heightMapImage16bit;
    public static DataBufferUShort heightMapDataBuffer;
    public static short[] heightMapPixelData;

    public static void loadMapImage(String mapName) {
        String location;

        if (mapName != null) {
            LOG.info("-----------------------------------------");
            LOG.info("Loading MapImage..... ");
            LOG.info(".....");
            LOG.info("Checking known locations for {}.png", mapName);
            location = findImageLocationFor(mapName,"");

            if (location != null) {
                BufferedImage loadedImage;
                SimpleImageInfo imageInfo;

                try {
                    imageInfo = new SimpleImageInfo(new File(location));
                    pdaImage = getNewBufferImage(imageInfo.getWidth(), imageInfo.getHeight(), Transparency.OPAQUE);
                    try {
                        loadedImage = ImageIO.read(new File(location));
                        Graphics2D g = (Graphics2D) pdaImage.getGraphics();
                        g.drawImage(loadedImage, 0, 0, loadedImage.getWidth(), loadedImage.getHeight(), null);
                        g.dispose();
                        setMapImageLabel(LOADED);
                        setMapPanelImage(pdaImage, false);
                    } catch (IOException ignored) {}
                } catch (IOException ignored) {}
            } else {
                LOG.info("could not find map image on disk or online, using default background");
                useDefaultMapImage();
                forceMapImageRedraw();
                if (configType == CONFIG_SAVEGAME) {
                    JOptionPane.showMessageDialog(editor, getLocaleString("dialog_gamexml_mapimage_not_found_message"), getLocaleString("dialog_mapimage_not_found_title"), JOptionPane.ERROR_MESSAGE);
                } else if (configType == CONFIG_ROUTEMANAGER) {
                    JOptionPane.showMessageDialog(editor, getLocaleString("dialog_routexml_mapimage_not_found_message"), getLocaleString("dialog_mapimage_not_found_title"), JOptionPane.ERROR_MESSAGE);
                }
                setMapImageLabel(NOT_FOUND);
            }
            mapImageMenuEnabled(true);
        }
    }

    public static void setPDAImage(BufferedImage image) {
        if (image != null) {
            LOG.info("Set PDA Image, required size {} x {}",image.getWidth(), image.getHeight());

            // actually draw the image and dispose of the graphics context that is no longer needed

            pdaImage = getNewBufferImage(image.getWidth(), image.getHeight(), Transparency.OPAQUE);
            Graphics2D g = (Graphics2D) pdaImage.getGraphics();

            if (image.getWidth() != 2048 || image.getHeight() != 2048 ) {
                LOG.info("Scaling PDA image to 2048 x 2048");
                Image tempImage = image.getScaledInstance(2048,2048, Image.SCALE_DEFAULT);
                //Graphics2D g = (Graphics2D) mapPanelImage.getGraphics();
                g.drawImage(tempImage, 0 , 0 , null);
                //g.dispose();
            } else {
                g.drawImage(image, 0, 0, null);
            }
            g.dispose();
        } else {
            LOG.info("## setPDAImage() ## image = null");
        }
    }

    public static void manualLoadHeightMap(File path) {
        BufferedImage loadedImage;
        try {
            loadedImage = ImageIO.read(path);
            LOG.info("HeightMap Image size {} x {}", loadedImage.getWidth(), loadedImage.getHeight());

            if (loadedImage.getType() != BufferedImage.TYPE_USHORT_GRAY) {
                    LOG.info("Image is not in 16-bit grayscale format");
                    heightMapImage = getNewBufferImage(loadedImage.getWidth(), loadedImage.getHeight(), Transparency.OPAQUE);
                    heightmapType = HeightmapFormat.BYTE;
            } else {
                    LOG.info("HeightMap Image is a 16-bit grayscale PNG");
                    heightMapImage = getNewBufferImage(loadedImage.getWidth(), loadedImage.getHeight(), Transparency.OPAQUE);
                    heightMapImage16bit = loadedImage;
                    heightMapDataBuffer = (DataBufferUShort) loadedImage.getRaster().getDataBuffer();
                    heightMapPixelData = heightMapDataBuffer.getData();
                    heightmapType = HeightmapFormat.USHORT_GREY;
            }
            Graphics2D g = (Graphics2D) heightMapImage.getGraphics();
            g.drawImage(loadedImage, 0, 0, loadedImage.getWidth(), loadedImage.getHeight(), null);
            g.dispose();
            setHeightmapImageLabel(MANUAL_LOAD);


            heightmapMenuEnabled(true);
            heightMapScale = calculateHeightMapScaling();
            LOG.info("HeightMap Scale = {}", heightMapScale);
            getMapPanel().repaint();
        } catch (IOException e) {
            setHeightmapImageLabel(NOT_FOUND);
            showInTextArea(getLocaleString("dialog_mapimage_not_found_title"), true, true);
            throw new RuntimeException(e);
        }
    }

    public static void loadHeightMap(String mapName) {
        String location;
        String configPath;

        if (mapName != null) {
            LOG.info("-----------------------------------------");
            LOG.info("Loading HeightMap..... ");
            try {
                //check if the file is in the same location as config file
                configPath = removeFilenameFromString(xmlConfigFile.toString());
                location = configPath + "terrain.heightmap.png";
                @SuppressWarnings("unused")
                SimpleImageInfo imageInfo = new SimpleImageInfo(new File(location));
                LOG.info("Found file at {}", location);
            } catch (Exception e) {
                LOG.info("Failed to find 'terrain.heightmap.png' at config location");
                LOG.info(".....");
                LOG.info("Checking known locations for {}_HeightMap.png", mapName);
                location = findImageLocationFor(mapName, "_HeightMap");
            }

            if (location != null) {
                //BufferedImage loadedImage;
                //SimpleImageInfo imageInfo;

                //imageInfo = new SimpleImageInfo(new File(location));
//                    if (EXPERIMENTAL) {
//                        heightMapImage = new BufferedImage(imageInfo.getWidth(), imageInfo.getHeight(), BufferedImage.TYPE_USHORT_565_RGB);
//                    } else {
//                        heightMapImage = getNewBufferImage(imageInfo.getWidth(), imageInfo.getHeight(), Transparency.OPAQUE);
//                    }

                try {
                    BufferedImage loadedImage = ImageIO.read(new File(location));
                    if (loadedImage.getType() != BufferedImage.TYPE_USHORT_GRAY) {
                        LOG.info("Heightmap Type = 8bit RGB ( {} )", HeightmapFormat.BYTE);
                        heightMapImage = getNewBufferImage(loadedImage.getWidth(), loadedImage.getHeight(), Transparency.OPAQUE);
                        heightmapType = HeightmapFormat.BYTE;
                    } else {
                        LOG.info("HeightMap Type = 16-bit Grayscale ( {} )", HeightmapFormat.USHORT_GREY);
                        heightMapImage = getNewBufferImage(loadedImage.getWidth(), loadedImage.getHeight(), Transparency.OPAQUE);
                        heightMapImage16bit = loadedImage;
                        heightMapDataBuffer = (DataBufferUShort) loadedImage.getRaster().getDataBuffer();
                        heightMapPixelData = heightMapDataBuffer.getData();
                        heightmapType = HeightmapFormat.USHORT_GREY;
                    }
                    Graphics2D g = (Graphics2D) heightMapImage.getGraphics();
                    g.drawImage(loadedImage, 0, 0, loadedImage.getWidth(), loadedImage.getHeight(), null);
                    g.dispose();
                    setHeightmapImageLabel(LOADED);

                    heightmapMenuEnabled(true);
                    heightMapScale = calculateHeightMapScaling();
                    LOG.info("HeightMap size = {} x {}", heightMapImage.getWidth(), heightMapImage.getHeight());
                    LOG.info("HeightMap Scale = {}", heightMapScale);
                    LOG.info("-----------------------------------------");
                } catch (IOException ignored) {}
            } else {
                LOG.info("Failed to load HeightMap");
                heightMapScale = 1;
                setHeightmapImageLabel(NOT_FOUND);
                heightmapMenuEnabled(true);
                if (configType == CONFIG_SAVEGAME) {
                    JOptionPane.showMessageDialog(editor, getLocaleString("dialog_heightmap_not_found_game"), getLocaleString("dialog_heightmap_not_found_title"), JOptionPane.ERROR_MESSAGE);
                } else if (configType == CONFIG_ROUTEMANAGER) {
                    JOptionPane.showMessageDialog(editor, getLocaleString("dialog_heightmap_not_found_route"), getLocaleString("dialog_heightmap_not_found_title"), JOptionPane.ERROR_MESSAGE);
                }
            }
        }
    }

    @SuppressWarnings({"unused", "UnusedAssignment"})
    private static String findImageLocationFor(String file, String nameExtension) {

        SimpleImageInfo imageInfo;
        String filePath = null;
        boolean bIsLegacyLocation = false;
        boolean bFileFound = false;

        String currentLocation = getCurrentLocation();
        String name = removePathFromString(file);

        try {
            //try default location
            if (currentLocation != null) {
                filePath = currentLocation + "mapImages/" + name + "/" + name + nameExtension + ".png";
            } else {
                filePath = "./mapImages/" + name + "/" + name + nameExtension + ".png";
            }
            imageInfo = new SimpleImageInfo(new File(filePath));
            LOG.info("Found file at {}", filePath);
            bFileFound = true;
        } catch (Exception e1) {
            try {
                // try the legacy location ( v0.80.0 and below )
                LOG.info("failed to find file at '{}'", filePath);
                LOG.info("Looking in legacy locations");
                if (currentLocation != null) {
                    filePath = currentLocation + "mapImages/" + name + nameExtension + ".png";
                } else {
                    filePath = "./mapImages/" + name + nameExtension + ".png";
                }
                imageInfo = new SimpleImageInfo(new File(filePath));
                LOG.info("--> Found file in legacy location {}", filePath);
                bFileFound = true;
                bIsLegacyLocation = true;
            } catch (Exception e2) {
                // try the resource location from sourcecode
                LOG.info("-->Failed to find file at {}", filePath);
                try {
                    if (currentLocation != null) {
                        filePath = currentLocation + "src/main/resources/mapImages/" + name + nameExtension + ".png";
                    } else {
                        filePath = "./src/mapImages/" + name + ".png";
                    }
                    imageInfo = new SimpleImageInfo(new File(filePath));
                    LOG.info("-->Found file at {}", filePath);
                    bFileFound = true;
                    bIsLegacyLocation = true;
                } catch (Exception e3) {
                    // try the same location as JAR file
                    LOG.info("-->failed to find file at {}", filePath);
                    try {
                        filePath = Objects.requireNonNullElse(currentLocation, "./") + name + nameExtension + ".png";
                        //pdaImage = ImageIO.read(new File(mapPath));
                        imageInfo = new SimpleImageInfo(new File(filePath));
                        LOG.info("-->Found file at {}", filePath);
                        bFileFound = true;
                        bIsLegacyLocation = true;
                    } catch (Exception e4) {
                        LOG.info("-->failed to find file at {}", filePath);
                    }
                }
            }
        }

        if (bIsLegacyLocation) {
            LOG.info("Moving Legacy file to correct location");
            if (currentLocation != null) {
                filePath = currentLocation + "mapImages/" + name + "/" + name + nameExtension + ".png";
            } else {
                filePath = "./mapImages/" + name + "/" + name + nameExtension + ".png";
            }

            File newFile = new File(filePath);
            if (!newFile.exists()) {
                File parent = newFile.getParentFile();
                if ((parent != null) && (!parent.exists())) {
                    if (parent.mkdirs()) {
                        LOG.info("Created correct directory for legacy file - {}", parent.getAbsolutePath());

                    } else {
                        LOG.info("Failed to createSetting directory {}", parent.getAbsolutePath());
                    }
                }
            }
            try {
                Files.move(Paths.get("mapImages/" + name + nameExtension + ".png"), Paths.get("mapImages/" + name + "/" + name + nameExtension + ".png"), REPLACE_EXISTING);
                LOG.info("Moved legacy file to {}", filePath);
            } catch (IOException e) {
                LOG.info("Failed to move legacy file to new location");
                e.printStackTrace();
            }

        }


        if (!bFileFound) {
            if (bUseOnlineMapImages) {
                //String fullPath;
                if (currentLocation != null) {
                    String gitPath = "https://raw.githubusercontent.com/KillBait/AutoDrive_MapImages/master/mapImages/" + name + "/" + name + nameExtension + ".png";
                    LOG.info("Checking GitHub repository for {}",gitPath);

                    //check if the URL is valid
                    URL gitUrl = null;
                    try {
                        gitUrl = new URL(gitPath);
                    } catch (MalformedURLException ex) {
                        ex.printStackTrace();
                    }

                    // set the save path for the downloaded image
                    filePath = currentLocation + "mapImages/" + name + "/" + name + nameExtension + ".png";
                    File localFile = new File(filePath);

                    // try and download the image
                    File newFile = null;
                    if (gitUrl != null) {
                        newFile = copyURLToFile(gitUrl, localFile, filePath);
                    }

                    if (newFile != null) {
                        //pdaImage = ImageIO.read(loadImage);
                        LOG.info("{}.png downloaded from GitHub repository", name + nameExtension);
                        try {
                            imageInfo = new SimpleImageInfo(new File(filePath));
                        } catch (IOException e) {
                            throw new RuntimeException(e);
                        }
                        bFileFound = true;
                    } else {
                        LOG.info("{}.png not found in GitHub repository", name + nameExtension);
                        //bMapImageFound = false;
                    }
                } else {
                    if (bDebugLogFileIO) LOG.info("getCurrentLocation returned null");
                    return null;
                }
            } else {
                LOG.info("Not checking online repository, using default image");
            }
        }
        return bFileFound ? filePath : null;
    }


    public static float  calculateHeightMapScaling(){
        double heightDiff = 0;

        LinkedList<MapNode> mapNodes = RoadMap.networkNodesList;
        if (heightMapImage != null) {
            for (MapNode node : mapNodes) {
                heightDiff += ((getYValueFromHeightMap(node.x, node.z) - node.y) / node.y);
            }
            int result = Math.round((float)heightDiff / mapNodes.size());
            if (result < 1 ) result = 1;
            return result;
        } else {
            return 1;
        }
    }

    public static void useDefaultMapImage() {
        String fullPath = "/mapImages/Blank.png";
        URL url = AutoDriveEditor.class.getResource(fullPath);
        if (url != null) {
            try {
                //SimpleImageInfo imageInfo = new SimpleImageInfo(new File(url.getFile()));
                pdaImage = getNewBufferImage(2048, 2048, Transparency.OPAQUE);
                BufferedImage tempBuffer = ImageIO.read(url);
                Graphics2D g = (Graphics2D) pdaImage.getGraphics();
                g.drawImage(tempBuffer, 0, 0, pdaImage.getWidth(), pdaImage.getHeight(), null);
                g.dispose();
                setMapPanelImage(pdaImage, false);
            } catch (IOException e) {
                e.printStackTrace();
            }
        }
    }

    @SuppressWarnings("unused")
    public static BufferedImage getMapPanelImage() {
        return mapPanelImage;
    }

    // TODO:-   eliminate mapPanelImage entirely, will need to change how the
    //          map image is loaded so pdaImage is a scaled down version of
    //          the original file.

    public static boolean setMapPanelImage(BufferedImage image, Boolean scaleToCorrectSize) {
        if (image != null) {
            LOG.info("MapPanel Image size {} x {}",image.getWidth(), image.getHeight());
            if (image.getWidth() != 2048 || image.getHeight() != 2048 ) {
                if (scaleToCorrectSize) {
                    LOG.info("Scaling image to required size 2048 x 2048");
                    Image tempImage = image.getScaledInstance(2048,2048, Image.SCALE_DEFAULT);
                    Graphics2D g = (Graphics2D) mapPanelImage.getGraphics();
                    g.drawImage(tempImage, 0 , 0 , null);
                    g.dispose();
                } else {
                    String message;
                    if (configVersion == GameVersion.FS19_CONFIG) {
                        message = getLocaleString("dialog_mapimage_incorrect_size") + "\n\n" + getLocaleString("dialog_mapimage_incorrect_size_fs19");
                    } else if (configVersion == GameVersion.FS22_CONFIG) {
                        message = getLocaleString("dialog_mapimage_incorrect_size") + "\n\n" + getLocaleString("dialog_mapimage_incorrect_size_fs22");
                    } else {
                        message = getLocaleString("dialog_mapimage_incorrect_size");
                    }
                    LOG.info(message);
                    JOptionPane.showConfirmDialog(editor, message, "AutoDriveEditor", JOptionPane.DEFAULT_OPTION, JOptionPane.ERROR_MESSAGE);
                    return false;
                }
            } else {
                // actually draw the image and dispose of the graphics context that is no longer needed
                mapPanelImage = getNewBufferImage(image.getWidth(), image.getHeight(), Transparency.OPAQUE);
                Graphics2D g2d = (Graphics2D) mapPanelImage.getGraphics();
                g2d.drawImage(image, 0, 0, null);
                g2d.dispose();
            }
            return true;
        } else {
            LOG.info("## setMapPanelImage() ## image = null");
            return false;
        }
    }

    public static class EditorImages {
        private final BufferedImage mapPanelImage;
        private BufferedImage heightMapImage;
        private BufferedImage pdaImage;
        private float heightMapScaleFactor;
        private float heightMapUnitsPerPixel;
        private BufferedImage heightMapImage16bit;
        private DataBufferUShort heightMapDataBuffer;
        private short[] heightMapPixelData;

        public EditorImages() {
            this.mapPanelImage = getNewBufferImage(2048, 2048, Transparency.OPAQUE);
            this.heightMapImage = null;
            this.pdaImage = null;
            this.heightMapScaleFactor = 1;
            this.heightMapUnitsPerPixel = 2;
        }

        public boolean setMapPanelImage(BufferedImage image, boolean scaleToCorrectSize) {
            if (image != null) {
                Graphics2D g = (Graphics2D) this.mapPanelImage.getGraphics();
                LOG.info("New Image size {} x {}", image.getWidth(), image.getHeight());
                if (image.getWidth() != 2048 || image.getHeight() != 2048) {
                    if (scaleToCorrectSize) {
                        LOG.info("Scaling image to required size 2048 x 2048");
                        //Image tempImage = image.getScaledInstance(2048, 2048, Image.SCALE_DEFAULT);
                        g.drawImage(image.getScaledInstance(2048, 2048, Image.SCALE_DEFAULT), 0, 0, null);
                    } else {
                        String message;
                        if (configVersion == GameVersion.FS19_CONFIG) {
                            message = getLocaleString("dialog_mapimage_incorrect_size") + "\n\n" + getLocaleString("dialog_mapimage_incorrect_size_fs19");
                        } else if (configVersion == GameVersion.FS22_CONFIG) {
                            message = getLocaleString("dialog_mapimage_incorrect_size") + "\n\n" + getLocaleString("dialog_mapimage_incorrect_size_fs22");
                        } else {
                            message = getLocaleString("dialog_mapimage_incorrect_size");
                        }
                        LOG.info(message);
                        JOptionPane.showConfirmDialog(editor, message, "AutoDriveEditor", JOptionPane.DEFAULT_OPTION, JOptionPane.ERROR_MESSAGE);
                        return false;
                    }
                } else {
                    g.drawImage(image, 0, 0, null);
                }
                g.dispose();
                return true;
            } else {
                LOG.info("## setMapPanelImage() ## image = null");
                return false;
            }
        }

        public BufferedImage getMapPanelImage() {
            return this.mapPanelImage;
        }
    }
}
