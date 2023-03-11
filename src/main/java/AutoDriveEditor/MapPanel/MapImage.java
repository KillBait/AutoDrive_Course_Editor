package AutoDriveEditor.MapPanel;

import AutoDriveEditor.AutoDriveEditor;
import AutoDriveEditor.RoadNetwork.MapNode;
import AutoDriveEditor.RoadNetwork.RoadMap;
import AutoDriveEditor.Utils.Classes.SimpleImageInfo;

import javax.imageio.ImageIO;
import javax.swing.*;
import java.awt.*;
import java.awt.image.BufferedImage;
import java.io.File;
import java.io.IOException;
import java.net.MalformedURLException;
import java.net.URL;
import java.nio.file.Files;
import java.nio.file.Paths;
import java.util.LinkedList;
import java.util.Objects;

import static AutoDriveEditor.AutoDriveEditor.EXPERIMENTAL;
import static AutoDriveEditor.AutoDriveEditor.editor;
import static AutoDriveEditor.GUI.GUIBuilder.*;
import static AutoDriveEditor.GUI.MenuBuilder.*;
import static AutoDriveEditor.Locale.LocaleManager.getLocaleString;
import static AutoDriveEditor.MapPanel.MapPanel.*;
import static AutoDriveEditor.Utils.FileUtils.*;
import static AutoDriveEditor.Utils.GUIUtils.showInTextArea;
import static AutoDriveEditor.Utils.ImageUtils.getNewBufferImage;
import static AutoDriveEditor.Utils.LoggerUtils.LOG;
import static AutoDriveEditor.Utils.MathUtils.roundUpFloatToDecimalPlaces;
import static AutoDriveEditor.XMLConfig.EditorXML.*;
import static AutoDriveEditor.XMLConfig.GameXML.*;
import static java.nio.file.StandardCopyOption.REPLACE_EXISTING;

public class MapImage {

    public static BufferedImage mapPanelImage;
    public static BufferedImage heightMapImage;
    public static BufferedImage pdaImage;

    public static double heightMapScale = 1;

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
                    if (EXPERIMENTAL) {
                        pdaImage = new BufferedImage(imageInfo.getWidth(), imageInfo.getHeight(), BufferedImage.TYPE_USHORT_565_RGB);
                    } else {
                        pdaImage = getNewBufferImage(imageInfo.getWidth(), imageInfo.getHeight(), Transparency.OPAQUE);
                    }

                    try {
                        loadedImage = ImageIO.read(new File(location));
                        Graphics2D g = (Graphics2D) pdaImage.getGraphics();
                        g.drawImage(loadedImage, 0, 0, loadedImage.getWidth(), loadedImage.getHeight(), null);
                        g.dispose();
                        imageLoadedLabel.setForeground(new Color(0, 100, 0));
                        imageLoadedLabel.setText("Loaded");
                        setImage(pdaImage, false);
                    } catch (IOException ignored) {}
                } catch (IOException ignored) {}
            } else {
                if (configType == CONFIG_SAVEGAME) {
                    JOptionPane.showMessageDialog(editor, getLocaleString("dialog_gamexml_mapimage_not_found_message"), getLocaleString("dialog_mapimage_not_found_title"), JOptionPane.ERROR_MESSAGE);
                } else if (configType == CONFIG_ROUTEMANAGER) {
                    JOptionPane.showMessageDialog(editor, getLocaleString("dialog_routexml_mapimage_not_found_message"), getLocaleString("dialog_mapimage_not_found_title"), JOptionPane.ERROR_MESSAGE);
                }
                LOG.info(getLocaleString("console_editor_no_map"));
                useDefaultMapImage();
                imageLoadedLabel.setForeground(new Color(200,0,0));
                imageLoadedLabel.setText("Not Found");
            }
            getMapPanel().repaint();
            mapMenuEnabled(true);
        }
    }

    public static void manualLoadHeightMap(File path) {
        BufferedImage loadedImage;
        try {
            loadedImage = ImageIO.read(path);
            if (EXPERIMENTAL) {
                heightMapImage = new BufferedImage(loadedImage.getWidth(), loadedImage.getHeight(), BufferedImage.TYPE_USHORT_565_RGB);
            } else {
                heightMapImage = getNewBufferImage(loadedImage.getWidth(), loadedImage.getHeight(), Transparency.OPAQUE);
            }
            Graphics2D g = (Graphics2D) heightMapImage.getGraphics();
            g.drawImage(loadedImage, 0, 0, loadedImage.getWidth(), loadedImage.getHeight(), null);
            g.dispose();
            heightMapLoadedLabel.setForeground(new Color(150,100,20));
            heightMapLoadedLabel.setText("Manual Load");

            heightmapMenuEnabled(true);
            heightMapScale = calculateHeightMapScaling();
            LOG.info("HeightMap Scale = {}", heightMapScale);
            getMapPanel().repaint();
        } catch (IOException e) {
            heightMapLoadedLabel.setForeground(new Color(200,0,0));
            heightMapLoadedLabel.setText("Not Found");
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
                BufferedImage loadedImage;
                SimpleImageInfo imageInfo;

                try {
                    imageInfo = new SimpleImageInfo(new File(location));
                    if (EXPERIMENTAL) {
                        heightMapImage = new BufferedImage(imageInfo.getWidth(), imageInfo.getHeight(), BufferedImage.TYPE_USHORT_565_RGB);
                    } else {
                        heightMapImage = getNewBufferImage(imageInfo.getWidth(), imageInfo.getHeight(), Transparency.OPAQUE);
                    }

                    try {
                        loadedImage = ImageIO.read(new File(location));
                        Graphics2D g = (Graphics2D) heightMapImage.getGraphics();
                        g.drawImage(loadedImage, 0, 0, loadedImage.getWidth(), loadedImage.getHeight(), null);
                        g.dispose();
                        heightMapLoadedLabel.setForeground(new Color(0, 100, 0));
                        heightMapLoadedLabel.setText("Loaded");

                        heightmapMenuEnabled(true);
                        heightMapScale = calculateHeightMapScaling();
                        LOG.info("HeightMap size = {} x {}", heightMapImage.getWidth(), heightMapImage.getHeight());
                        LOG.info("HeightMap Scale = {}", heightMapScale);
                        LOG.info("-----------------------------------------");
                    } catch (IOException ignored) {}
                } catch (IOException ignored) {}
            } else {
                LOG.info("Failed to load HeightMap");
                heightMapScale = 1;
                heightMapLoadedLabel.setForeground(new Color(200,0,0));
                heightMapLoadedLabel.setText("Not Found");
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
                        LOG.info("Failed to create directory {}", parent.getAbsolutePath());
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
                    showInTextArea(getLocaleString("mapimage_github_check") + " " + name + nameExtension + ".png", true, false);
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
                        showInTextArea(name + nameExtension + ".png " + getLocaleString("mapimage_github_repo_download"), true, false);
                        try {
                            imageInfo = new SimpleImageInfo(new File(filePath));
                        } catch (IOException e) {
                            throw new RuntimeException(e);
                        }
                        bFileFound = true;
                    } else {
                        showInTextArea(name + nameExtension + ".png " + getLocaleString("mapimage_github_repo_not_found"), true, false);
                        //bMapImageFound = false;
                    }
                } else {
                    if (bDebugLogFileIO) LOG.info("getCurrentLocation returned null");
                    return null;
                }
            } else {
                showInTextArea(getLocaleString("mapimage_github_bypass"), true, true);
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
                mapPanelImage = ImageIO.read(url);
                setImage(mapPanelImage, false);
            } catch (IOException e) {
                e.printStackTrace();
            }
        }
    }

    public static BufferedImage getMapPanelImage() {
        return mapPanelImage;
    }

    public static void setImage(BufferedImage image, Boolean scaleToCorrectSize) {
        if (image != null) {
            LOG.info("Selected Image size is {} x {}",image.getWidth(), image.getHeight());
            if (image.getWidth() != 2048 || image.getHeight() != 2048 ) {
                if (scaleToCorrectSize) {
                    LOG.info("Scaling image to 2048 x 2048");
                    Image tempImage = image.getScaledInstance(2048,2048, Image.SCALE_DEFAULT);
                    Graphics2D g = (Graphics2D) mapPanelImage.getGraphics();
                    g.drawImage(tempImage, 0 , 0 , null);
                    g.dispose();
                } else {
                    String message;
                    if (configVersion == FS19_CONFIG) {
                        message = getLocaleString("dialog_mapimage_incorrect_size") + "\n\n" + getLocaleString("dialog_mapimage_incorrect_size_fs19");
                    } else if (configVersion == FS22_CONFIG) {
                        message = getLocaleString("dialog_mapimage_incorrect_size") + "\n\n" + getLocaleString("dialog_mapimage_incorrect_size_fs22");
                    } else {
                        message = getLocaleString("dialog_mapimage_incorrect_size");
                    }
                    LOG.info(message);
                    JOptionPane.showConfirmDialog(editor, message, "AutoDriveEditor", JOptionPane.DEFAULT_OPTION, JOptionPane.ERROR_MESSAGE);
                }
                return;
            }

            // actually draw the image and dispose of the graphics context that is no longer needed
            mapPanelImage = getNewBufferImage(image.getWidth(), image.getHeight(), Transparency.OPAQUE);
            Graphics2D g2d = (Graphics2D) mapPanelImage.getGraphics();
            g2d.drawImage(image, 0, 0, null);
            g2d.dispose();
        }
    }

    public static void checkStoredMapInfoFor(String mapName) {
        boolean isKnown = false;
        if (mapName != null) {
            for (int i = 0; i <= knownMapList.size() - 1; i++) {
                MapInfoStore store = knownMapList.get(i);
                if (store.mapName.equals(mapName)) {
                    LOG.info("Found previously used settings for map ( {} ), applying them now", mapName);
                    updateMapScaleTo(store.zoomFactor);
                    LOG.info("  --> updating map scale to {}x", store.zoomFactor);
                    updateNodeSizeTo(store.nodeSize);
                    LOG.info("  --> updating node size to {}x", nodeSize);
                    isKnown = true;
                    break;
                }
            }
        } else {
            LOG.info("## WARNING ## - Map name is 'null', this should not happen, setting map panel to 1x map scale and 2.0 node size");
            updateSelectedMapScaleMenuTo(1);
            updateMapScaleTo(1);
            updateNodeSizeTo(2f);
        }

        if (!isKnown && mapName != null) {
            LOG.info("No previous settings found for Map ( {} ), storing new map with initial settings of 1x map scale / node size of 2.0", mapName);
            knownMapList.add(new MapInfoStore(mapName, 1, 2f));
            currentMapSizeLabel.setText("2km");
            nodeSize = 2f;
        }
    }

    public static void updateSelectedMapScaleMenuTo(int zoomFactor) {
        if (zoomFactor == 1) {
            zoom2km.setSelected(true);
        } else if (zoomFactor == 2) {
            zoom4km.setSelected(true);
        } else if (zoomFactor == 3) {
            zoom6km.setSelected(true);
        } else if (zoomFactor == 4) {
            zoom8km.setSelected(true);
        } else if (zoomFactor == 5) {
            zoom10km.setSelected(true);
        } else if (zoomFactor == 6) {
            zoom12km.setSelected(true);
        } else if (zoomFactor == 7) {
            zoom14km.setSelected(true);
        } else if (zoomFactor == 8) {
            zoom16km.setSelected(true);
        } else if (zoomFactor == 9) {
            zoom18km.setSelected(true);
        } else if (zoomFactor == 10) {
            zoom20km.setSelected(true);
        } else if (zoomFactor == 11) {
            zoom22km.setSelected(true);
        } else if (zoomFactor == 12) {
            zoom24km.setSelected(true);
        } else if (zoomFactor == 13) {
            zoom26km.setSelected(true);
        } else if (zoomFactor == 14) {
            zoom28km.setSelected(true);
        } else if (zoomFactor == 15) {
            zoom30km.setSelected(true);
        } else if (zoomFactor == 16) {
            zoom32km.setSelected(true);
        } else if (zoomFactor == 17) {
            zoom34km.setSelected(true);
        } else if (zoomFactor == 18) {
            zoom36km.setSelected(true);
        } else if (zoomFactor == 19) {
            zoom38km.setSelected(true);
        } else if (zoomFactor == 20) {
            zoom40km.setSelected(true);
        } else if (zoomFactor == 21) {
            zoom42km.setSelected(true);
        } else if (zoomFactor == 22) {
            zoom44km.setSelected(true);
        } else if (zoomFactor == 23) {
            zoom46km.setSelected(true);
        }
        currentMapSizeLabel.setText("" + zoomFactor *2 + "km");
    }

    public static void updateStoredMapScale(String mapName, int zoomFactor) {
        if (mapName != null) {
            for (int i = 0; i <= knownMapList.size() - 1; i++) {
                MapInfoStore store = knownMapList.get(i);
                if (store.mapName.equals(mapName)) {
                    store.zoomFactor = zoomFactor;
                    break;
                }
            }
        }
    }

    public static void updateStoredMapNodeSize(String mapName, float nodeSize) {
        if (mapName != null) {
            for (int i = 0; i <= knownMapList.size() - 1; i++) {
                MapInfoStore store = knownMapList.get(i);
                if (store.mapName.equals(mapName)) {
                    store.nodeSize = nodeSize;
                    break;
                }
            }
        }
    }

    public static void updateMapScaleTo(int zoomFactor) {
        if (roadMap != null) {
            getMapPanel().setMapZoomFactor(zoomFactor);
            updateSelectedMapScaleMenuTo(zoomFactor);
            updateStoredMapScale(RoadMap.mapName, zoomFactor);
            getMapPanel().repaint();
        }
    }

    public static void updateNodeSizeTo(float newNodeSize) {
        if (roadMap != null) {
            nodeSize = roundUpFloatToDecimalPlaces(newNodeSize, 1);
            updateStoredMapNodeSize(RoadMap.mapName, nodeSize);
            getMapPanel().repaint();
        }

    }
}
