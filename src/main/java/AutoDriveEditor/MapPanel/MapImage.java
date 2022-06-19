package AutoDriveEditor.MapPanel;

import AutoDriveEditor.AutoDriveEditor;
import AutoDriveEditor.GUI.GUIBuilder;
import AutoDriveEditor.GUI.MenuBuilder;
import AutoDriveEditor.RoadNetwork.MapNode;
import AutoDriveEditor.RoadNetwork.RoadMap;

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

import static AutoDriveEditor.AutoDriveEditor.*;
import static AutoDriveEditor.GUI.GUIBuilder.*;
import static AutoDriveEditor.GUI.MenuBuilder.*;
import static AutoDriveEditor.Locale.LocaleManager.localeString;
import static AutoDriveEditor.MapPanel.MapPanel.*;
import static AutoDriveEditor.Utils.FileUtils.*;
import static AutoDriveEditor.Utils.GUIUtils.showInTextArea;
import static AutoDriveEditor.Utils.ImageUtils.getNewBufferedImage;
import static AutoDriveEditor.Utils.LoggerUtils.LOG;
import static AutoDriveEditor.XMLConfig.EditorXML.*;
import static AutoDriveEditor.XMLConfig.GameXML.*;
import static java.nio.file.StandardCopyOption.REPLACE_EXISTING;


public class MapImage {

    public static BufferedImage mapImage;
    public static BufferedImage heightMapImage;
    public static Image backBufferImage = null;
    public static Graphics2D backBufferGraphics = null;
    private static boolean bImageFound = false;
    public static BufferedImage image;
    public static double heightMapScale = 1;

    public static void loadMapImage(String mapName) {

        String mapPath = null;
        File file;
        String location = getCurrentLocation();
        boolean isLegacyLocation = false;

        if (mapName != null) {

            //
            // Try and load the mapImage from the listed locations
            //

            try {
                //try same location as config file
                String configPath = removeFilenameFromString(xmlConfigFile.toString());
                mapPath = configPath + mapName + ".png";
                mapImage = ImageIO.read(new File(mapPath));
                LOG.info("Loaded mapImage from {}", mapPath);
                bImageFound = true;
            } catch (Exception e) {
                try {
                    //try default location
                    LOG.info("failed to load map image from {}", mapPath);
                    if (location != null) {
                        mapPath = location + "mapImages/" + mapName + "/" + mapName + ".png";
                    } else {
                        mapPath = "./mapImages/" + mapName + "/" + mapName + ".png";
                    }
                    mapImage = ImageIO.read(new File(mapPath));
                    LOG.info("Loaded mapImage from {}", mapPath.substring(1));
                    bImageFound = true;
                } catch (Exception e1) {
                    try {
                        // try the legacy location ( v0.80.0 and below )
                        if (mapPath != null) LOG.info("failed to load map image from {}", mapPath.substring(1));
                        LOG.info("trying legacy locations");
                        if (location != null) {
                            mapPath = location + "mapImages/" + mapName + ".png";
                        } else {
                            mapPath = "./mapImages/" + mapName + ".png";
                        }
                        mapImage = ImageIO.read(new File(mapPath));
                        LOG.info("Loaded mapImage from legacy location {}", mapPath);
                        isLegacyLocation = true;
                        bImageFound = true;
                    } catch (Exception e2) {
                        // try the resource location from sourcecode
                        if (mapPath != null) LOG.info("failed to load map image from {}", mapPath.substring(1));
                        try {
                            if (location != null) {
                                mapPath = location + "src/main/resources/mapImages/" + mapName + ".png";
                            } else {
                                mapPath = "./src/mapImages/" + mapName + ".png";
                            }
                            mapImage = ImageIO.read(new File(mapPath));
                            LOG.info("Loaded mapImage from {}", mapPath);
                            bImageFound = true;
                        } catch (Exception e3) {
                            // try the same location as JAR file
                            LOG.info("failed to load map image from {}", mapPath.substring(1));
                            try {
                                mapPath = Objects.requireNonNullElse(location, "./") + mapName + ".png";
                                heightMapImage = ImageIO.read(new File(mapPath));
                                LOG.info("Loaded mapImage from {}", mapPath);
                                bImageFound = true;
                            } catch (Exception e4) {
                                LOG.info("failed to load map image from {}", mapPath.substring(1));
                                loadImageMenuItem.setEnabled(true);
                                bImageFound = false;
                            }
                        }
                    }
                }
            }

            if (isLegacyLocation) {
                LOG.info("Moving Legacy file to correct location");
                if (location != null) {
                    mapPath = location + "mapImages/" + mapName + "/" + mapName + ".png";
                } else {
                    mapPath = "./mapImages/" + mapName + "/" + mapName + ".png";
                }

                file = new File(mapPath);
                if (!file.exists()) {
                    File parent = file.getParentFile();
                    if ((parent != null) && (!parent.exists())) {
                        if (parent.mkdirs()) {
                            LOG.info("Created correct directory for legacy file - {}", parent.getAbsolutePath());

                        } else {
                            LOG.info("Failed to create directory {}", parent.getAbsolutePath());
                        }
                    }
                }
                try {
                    Files.move(Paths.get("mapImages/" + mapName + ".png"), Paths.get("mapImages/" + mapName + "/" + mapName + ".png"), REPLACE_EXISTING);
                    LOG.info("Moved legacy file to {}", mapPath);
                } catch (IOException e) {
                    LOG.info("Failed to move legacy file to new location");
                    e.printStackTrace();
                }

            }

            if (!bImageFound) {
                if (bUseOnlineMapImages) {
                    String fullPath;
                    if (location != null) {
                        String gitPath = "https://raw.githubusercontent.com/KillBait/AutoDrive_MapImages/master/mapImages/" + mapName + "/" + mapName + ".png";
                        showInTextArea(localeString.getString("mapimage_github_check") + " " + mapName + ".png", true, false);
                        LOG.info("Checking GitHub repository for {}",gitPath);
                        URL gitUrl = null;
                        try {
                            gitUrl = new URL(gitPath);
                        } catch (MalformedURLException ex) {
                            ex.printStackTrace();
                        }

                        fullPath = location + "mapImages/" + mapName + "/" + mapName + ".png";
                        file = new File(fullPath);


                        File loadImage = null;
                        if (gitUrl != null) {
                            loadImage = copyURLToFile(gitUrl, file, fullPath);
                        }

                        if (loadImage != null) {
                            try {
                                mapImage = ImageIO.read(loadImage);
                                showInTextArea(mapName + ".png " + localeString.getString("mapimage_github_repo_download"), true, false);
                                bImageFound = true;
                            } catch (IOException ex) {
                                ex.printStackTrace();
                                showInTextArea(mapName + ".png " + localeString.getString("mapimage_github_repo_not_found"), true, false);
                                bImageFound = false;
                            }

                        }
                    } else {
                        if (bDebugLogFileIO) LOG.info("getCurrentLocation returned null");
                        bImageFound = false;
                    }
                } else {
                    showInTextArea(localeString.getString("mapimage_github_bypass"), true, true);
                }
            }
        }

        if (bImageFound) {
            setImage(mapImage, false);
            imageLoadedLabel.setForeground(new Color(0,100,0));
            imageLoadedLabel.setText("Loaded");
        } else {
            if (configType == CONFIG_SAVEGAME) {
                JOptionPane.showMessageDialog(editor, localeString.getString("dialog_gamexml_mapimage_not_found_message"), localeString.getString("dialog_mapimage_not_found_title"), JOptionPane.ERROR_MESSAGE);
            } else if (configType == CONFIG_ROUTEMANAGER) {
                JOptionPane.showMessageDialog(editor, localeString.getString("dialog_routexml_mapimage_not_found_message"), localeString.getString("dialog_mapimage_not_found_title"), JOptionPane.ERROR_MESSAGE);
            }
            LOG.info(localeString.getString("console_editor_no_map"));
            useDefaultMapImage();
            imageLoadedLabel.setForeground(new Color(200,0,0));
            imageLoadedLabel.setText("Not Found");
        }

        GUIBuilder.updateGUIButtons(true);
        getMapsZoomFactor(mapName);
        getMapPanel().repaint();

        MenuBuilder.mapMenuEnabled(true);
    }

    public static void loadHeightMap(File path, boolean isManualLoad) {
        BufferedImage heightImage;
        String heightMapPath;
        boolean bHeightMapFound = false;

        if (path != null) {
            if (isManualLoad) {
                heightMapPath = path.toString();
            } else {
                String pathStr = path.toString();
                heightMapPath = pathStr.substring(0, pathStr.lastIndexOf("\\") + 1) + "terrain.heightmap.png";
            }
            LOG.info("Loading HeightMap...");
            heightImage = null;
            String location = getCurrentLocation();

            try {
                //try same location as config file
                heightImage = ImageIO.read(new File(heightMapPath));
                LOG.info("Loaded heightMapImage from {}", heightMapPath);
                bHeightMapFound = true;
            } catch (IOException e1) {
                //try default location
                LOG.info("failed to load heightMap from config location {}", heightMapPath);
                if (RoadMap.mapName != null) {
                    if (location != null) {
                        heightMapPath = location + "mapImages/" + RoadMap.mapName + "/" + RoadMap.mapName + "_HeightMap.png";
                    } else {
                        heightMapPath = "./mapImages/" + RoadMap.mapName + "/" + RoadMap.mapName + "_HeightMap.png";
                    }

                    try {
                        heightImage = ImageIO.read(new File(heightMapPath));
                        LOG.info("Loaded heightMap from {}", heightMapPath.substring(1));
                        bHeightMapFound = true;
                    } catch (IOException e2) {
                        LOG.info("failed to load heightMap from default location {}", heightMapPath);

                        //
                        // check gitHub for heightmap
                        //

                        if (bUseOnlineMapImages) {
                            String fullPath;
                            if (location != null) {
                                String gitPath = "https://raw.githubusercontent.com/KillBait/AutoDrive_MapImages/master/mapImages/" + RoadMap.mapName + "/" + RoadMap.mapName + "_HeightMap.png";
                                showInTextArea(localeString.getString("mapimage_github_check") + " " + RoadMap.mapName + "_HeightMap.png", true, false);
                                LOG.info("Checking GitHub repository for {}",gitPath);
                                URL gitUrl = null;
                                try {
                                    gitUrl = new URL(gitPath);
                                } catch (MalformedURLException ex) {
                                    ex.printStackTrace();
                                }

                                fullPath = location + "mapImages/" + RoadMap.mapName + "/" + RoadMap.mapName + "_HeightMap.png";
                                File file = new File(fullPath);


                                File loadImage = null;
                                if (gitUrl != null) {
                                    loadImage = copyURLToFile(gitUrl, file, fullPath);
                                }

                                if (loadImage != null) {
                                    try {
                                        heightImage = ImageIO.read(loadImage);
                                        showInTextArea(RoadMap.mapName + "_HeightMap.png " + localeString.getString("mapimage_github_repo_download"), false, false);
                                        bHeightMapFound = true;
                                    } catch (IOException ex) {
                                        ex.printStackTrace();
                                        showInTextArea(RoadMap.mapName + "_HeightMap.png " + localeString.getString("mapimage_github_repo_not_found"), false, false);
                                    }

                                }
                            } else {
                                if (bDebugLogFileIO) LOG.info("getCurrentLocation returned null");
                                bImageFound = false;
                            }
                        } else {
                            showInTextArea(localeString.getString("mapimage_github_bypass"), true, true);
                        }
                    }
                }
            }

            if (bHeightMapFound && heightImage != null) {
                LOG.info("HeightMap size = {} x {}", heightImage.getWidth(), heightImage.getHeight());
                try {
                    heightMapImage = ImageIO.read(new File(heightMapPath));
                } catch (IOException e) {
                    throw new RuntimeException(e);
                }
                if (EXPERIMENTAL) {
                    heightMapImage = new BufferedImage(heightImage.getWidth(), heightImage.getHeight(), BufferedImage.TYPE_USHORT_565_RGB);
                } else {
                    heightMapImage = getNewBufferImage(heightImage.getWidth(), heightImage.getHeight());
                }
                Graphics2D g = (Graphics2D) heightMapImage.getGraphics();
                g.drawImage(heightImage, 0, 0, heightImage.getWidth(), heightImage.getHeight(), null);
                g.dispose();
                if (isManualLoad) {
                    heightMapLoadedLabel.setForeground(new Color(150,100,20));
                    heightMapLoadedLabel.setText("Imported");
                } else {
                    heightMapLoadedLabel.setForeground(new Color(0,100,0));
                    heightMapLoadedLabel.setText("Loaded");
                }

                heightmapMenuEnabled(true);
                heightMapScale = calculateHeightMapScaling();
                LOG.info("HeightMap Scale = {}", heightMapScale);
            } else {
                LOG.info("Failed to load HeightMap");
                heightMapScale = 1;
                heightMapLoadedLabel.setForeground(new Color(200,0,0));
                heightMapLoadedLabel.setText("Not Found");
                importHeightmapMenuItem.setEnabled(true);
                exportHeightMapMenuItem.setEnabled(false);
                fixNodesHeightMenuItem.setEnabled(false);
                showHeightMapMenuItem.setEnabled(false);
                if (configType == CONFIG_SAVEGAME) {
                    JOptionPane.showMessageDialog(editor, localeString.getString("dialog_heightmap_not_found_game"), localeString.getString("dialog_heightmap_not_found_title"), JOptionPane.ERROR_MESSAGE);
                } else if (configType == CONFIG_ROUTEMANAGER) {
                    JOptionPane.showMessageDialog(editor, localeString.getString("dialog_heightmap_not_found_route"), localeString.getString("dialog_heightmap_not_found_title"), JOptionPane.ERROR_MESSAGE);
                }

            }
        }
    }

    public static float  calculateHeightMapScaling(){
        double heightDiff = 0;

        LinkedList<MapNode> mapNodes = RoadMap.mapNodes;
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
                mapImage = ImageIO.read(url);
                setImage(mapImage, false);
            } catch (IOException e) {
                e.printStackTrace();
            }
        }
    }

    public static void getNewBackBufferImage(int width, int height) {
        GraphicsEnvironment ge = GraphicsEnvironment.getLocalGraphicsEnvironment();
        GraphicsDevice gd = ge.getDefaultScreenDevice();
        GraphicsConfiguration gc = gd.getDefaultConfiguration();

        backBufferImage = gc.createCompatibleImage(width, height, Transparency.OPAQUE);
        if (bDebugLogRenderInfo) LOG.info("Accelerated BackBufferImage = {}", gc.getImageCapabilities().isAccelerated());
        backBufferImage.setAccelerationPriority(1);
        backBufferGraphics = (Graphics2D) backBufferImage.getGraphics();
        backBufferGraphics.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);
        backBufferGraphics.setRenderingHint(RenderingHints.KEY_ALPHA_INTERPOLATION, RenderingHints.VALUE_ALPHA_INTERPOLATION_QUALITY);
        backBufferGraphics.setRenderingHint(RenderingHints.KEY_COLOR_RENDERING, RenderingHints.VALUE_COLOR_RENDER_SPEED);
        backBufferGraphics.setRenderingHint(RenderingHints.KEY_DITHERING, RenderingHints.VALUE_DITHER_DISABLE);
        backBufferGraphics.setRenderingHint(RenderingHints.KEY_FRACTIONALMETRICS, RenderingHints.VALUE_FRACTIONALMETRICS_OFF);
        backBufferGraphics.setRenderingHint(RenderingHints.KEY_INTERPOLATION, RenderingHints.VALUE_INTERPOLATION_NEAREST_NEIGHBOR);
        backBufferGraphics.setRenderingHint(RenderingHints.KEY_RENDERING, RenderingHints.VALUE_RENDER_SPEED);
        backBufferGraphics.setRenderingHint(RenderingHints.KEY_STROKE_CONTROL, RenderingHints.VALUE_STROKE_PURE);
        backBufferGraphics.setRenderingHint(RenderingHints.KEY_TEXT_ANTIALIASING, RenderingHints.VALUE_TEXT_ANTIALIAS_OFF);
    }

    public static BufferedImage getNewBufferImage(int width, int height) {
        GraphicsEnvironment ge = GraphicsEnvironment.getLocalGraphicsEnvironment();
        GraphicsDevice gd = ge.getDefaultScreenDevice();
        GraphicsConfiguration gc = gd.getDefaultConfiguration();

        BufferedImage bufferImage = gc.createCompatibleImage(width, height, Transparency.OPAQUE);
        if (bDebugLogRenderInfo) LOG.info("Accelerated bufferImage = {}", gc.getImageCapabilities().isAccelerated());
        bufferImage.setAccelerationPriority(1);
        return bufferImage;
    }

    public static BufferedImage getImage() {
        return image;
    }

    public static void  setImage(BufferedImage loadedImage, Boolean ignoreSize) {
        if (loadedImage != null) {
            LOG.info("Selected Image size is {} x {}",loadedImage.getWidth(), loadedImage.getHeight());
            if (loadedImage.getWidth() != 2048 || loadedImage.getHeight() != 2048 ) {
                if (!ignoreSize) {
                    String message;
                    if (configVersion == FS19_CONFIG) {
                        message = localeString.getString("dialog_mapimage_incorrect_size") + "\n\n" + localeString.getString("dialog_mapimage_incorrect_size_fs19");
                    } else if (configVersion == FS22_CONFIG) {
                        message = localeString.getString("dialog_mapimage_incorrect_size") + "\n\n" + localeString.getString("dialog_mapimage_incorrect_size_fs22");
                    } else {
                        message = localeString.getString("dialog_mapimage_incorrect_size");
                    }
                    LOG.info(message);
                    JOptionPane.showConfirmDialog(editor, message, "AutoDriveEditor", JOptionPane.DEFAULT_OPTION, JOptionPane.ERROR_MESSAGE);
                    return;
                }
            }

            if (ignoreSize) {
                Image heightImage = loadedImage.getScaledInstance(2048,2048, Image.SCALE_DEFAULT);
                loadedImage = getNewBufferedImage(2048,2048, Transparency.OPAQUE);
                Graphics2D g = (Graphics2D) loadedImage.getGraphics();
                g.drawImage(heightImage, 0 , 0 , null);
                g.dispose();
            }
            // actually draw the image and dispose of the graphics context that is no longer needed
            image = getNewBufferedImage(loadedImage.getWidth(), loadedImage.getHeight(), Transparency.OPAQUE);
            Graphics2D g2d = (Graphics2D) image.getGraphics();
            g2d.drawImage(loadedImage, 0, 0, null);
            g2d.dispose();

            if (!oldConfigFormat) {
                GUIBuilder.updateGUIButtons(true);
                MenuBuilder.saveMenuEnabled(true);
                MenuBuilder.editMenuEnabled(true);
            }
        }
    }

    public static void getMapsZoomFactor(String mapName) {

        boolean isKnown = false;
        if (mapName != null) {
            for (int i = 0; i <= mapZoomStore.size() - 1; i++) {
                MapZoomStore store = mapZoomStore.get(i);
                if (store.mapName.equals(mapName)) {
                    LOG.info("Found map name ( {} ) in MapZoomFactor entries, setting map scale to {}x", mapName, store.zoomFactor);
                    updateMapZoomFactor(store.zoomFactor);
                    isKnown = true;
                    break;
                }
            }
        } else {
            LOG.info("Map name is null, defaulting to 1x map");
            updateDisplayedMapScale(1);
            updateMapZoomFactor(1);
        }

        if (!isKnown && mapName != null) {
            LOG.info("Map name ( {} ) not found in Editors ZoomStore, adding as new map with default 1x", mapName);
            mapZoomStore.add(new MapZoomStore(mapName, 1));
            currentMapSizeLabel.setText("2km");
        }
    }

    public static void updateDisplayedMapScale(int zoomFactor) {
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

    public static void updateMapZoomStore(String mapName, int zoomFactor) {
        if (mapName != null) {
            for (int i = 0; i <= mapZoomStore.size() - 1; i++) {
                MapZoomStore store = mapZoomStore.get(i);
                if (store.mapName.equals(mapName)) {
                    LOG.info("found {} in known zoomFactors, updating it's entry to {}x", store.mapName, zoomFactor);
                    store.zoomFactor = zoomFactor;
                    break;
                }
            }
        }
    }
}
