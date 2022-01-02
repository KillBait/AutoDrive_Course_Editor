package AutoDriveEditor.MapPanel;

import javax.imageio.ImageIO;
import javax.swing.*;
import java.awt.*;
import java.awt.image.BufferedImage;
import java.io.File;
import java.io.IOException;
import java.net.MalformedURLException;
import java.net.URL;

import AutoDriveEditor.AutoDriveEditor;
import AutoDriveEditor.GUI.GUIBuilder;
import AutoDriveEditor.GUI.MenuBuilder;

import static AutoDriveEditor.AutoDriveEditor.*;
import static AutoDriveEditor.GUI.GUIUtils.*;
import static AutoDriveEditor.GUI.MenuBuilder.*;
import static AutoDriveEditor.Locale.LocaleManager.*;
import static AutoDriveEditor.MapPanel.MapPanel.*;
import static AutoDriveEditor.Utils.FileUtils.*;
import static AutoDriveEditor.Utils.ImageUtils.*;
import static AutoDriveEditor.Utils.LoggerUtils.*;
import static AutoDriveEditor.XMLConfig.EditorXML.*;
import static AutoDriveEditor.XMLConfig.GameXML.*;


public class MapImage {

    public static BufferedImage mapImage;
    public static BufferedImage heightMapImage;
    public static Image backBufferImage = null;
    public static Graphics2D backBufferGraphics = null;
    private static boolean bImageFound = false;
    public static BufferedImage image;
    public static BufferedImage croppedImage;

    public static void loadMapImage(String mapName) {

        URL url;

        String location = getCurrentLocation();

        if (mapName != null) {

            String mapPath = "/mapImages/" + mapName + ".png";
            url = AutoDriveEditor.class.getResource(mapPath);

            try {
                mapImage = ImageIO.read(url);
            } catch (Exception e) {
                try {
                    LOG.info("failed to load map image from JAR .. trying alternate locations");
                    if (location != null) {
                        mapPath = location + "mapImages/" + mapName + ".png";
                    } else {
                        mapPath = "./mapImages/" + mapName + ".png";
                    }
                    mapImage = ImageIO.read(new File(mapPath));
                    LOG.info("Loaded mapImage from {}", mapPath);
                    bImageFound = true;
                } catch (Exception e1) {
                    LOG.info("failed to load map image from {}", mapPath.substring(1));
                    try {
                        if (location != null) {
                            mapPath = location + "src/main/resources/mapImages/" + mapName + ".png";
                        } else {
                            mapPath = "./src/mapImages/" + mapName + ".png";
                        }
                        mapImage = ImageIO.read(new File(mapPath));
                        LOG.info("Loaded mapImage from {}", mapPath);
                        bImageFound = true;
                    } catch (Exception e2) {
                        LOG.info("failed to load map image from {}", mapPath.substring(1));
                        try {
                            if (location != null) {
                                mapPath = location + mapName + ".png";
                            } else {
                                mapPath = "./" + mapName + ".png";
                            }
                            mapImage = ImageIO.read(new File(mapPath));
                            LOG.info("Loaded mapImage from {}", mapPath);
                            bImageFound = true;
                        } catch (Exception e3) {
                            LOG.info("failed to load map image from {}", mapPath.substring(1));
                            loadImageMenuItem.setEnabled(true);
                            bImageFound = false;
                        }
                    }
                }
            }

            if (!bImageFound) {
                if (bUseOnlineMapImages) {
                    String fullPath;
                    if (location != null) {
                        String gitPath = "https://github.com/KillBait/FS19_AutoDrive_MapImages/raw/main/mapImages/" + mapName + ".png";
                        showInTextArea(localeString.getString("mapimage_github_check") + mapName + ".png", true);
                        LOG.info("Checking GitHub repository for {}",gitPath);
                        URL gitUrl = null;
                        try {
                            gitUrl = new URL(gitPath);
                        } catch (MalformedURLException ex) {
                            ex.printStackTrace();
                        }

                        fullPath = location + "mapImages/" + mapName + ".png";
                        File file = new File(fullPath);


                        File loadImage = null;
                        if (gitUrl != null) {
                            loadImage = copyURLToFile(gitUrl, file, fullPath);
                        }

                        if (loadImage != null) {
                            try {
                                mapImage = ImageIO.read(loadImage);
                                showInTextArea(mapName + ".png " + localeString.getString("mapimage_github_repo_download"), true);
                                bImageFound = true;
                            } catch (IOException ex) {
                                ex.printStackTrace();
                                showInTextArea(mapName + ".png " + localeString.getString("mapimage_github_repo_not_found"), true);
                                bImageFound = false;
                            }

                        }
                    } else {
                        if (bDebugFileIO) LOG.info("getCurrentLocation returned null");
                        bImageFound = false;
                    }
                } else {
                    showInTextArea(localeString.getString("mapimage_github_bypass"), true);
                    LOG.info("{}", localeString.getString("mapimage_github_bypass"));
                }
            }
        }

        if (bImageFound) {
            setImage(mapImage);
        } else {
            LOG.info("Using Default Image");
            JOptionPane.showMessageDialog(editor, localeString.getString("dialog_mapimage_not_found_message"), localeString.getString("dialog_mapimage_not_found_title"), JOptionPane.ERROR_MESSAGE);
            LOG.info(localeString.getString("dialog_mapimage_not_found_message"));
            useDefaultMapImage();
        }

        GUIBuilder.updateGUIButtons(true);
        getMapPanel().repaint();
        getMapZoomFactor(mapName);

        MenuBuilder.mapMenuEnabled(true);
        //editorState = GUIBuilder.EDITORSTATE_NOOP;

    }

    public static void  loadHeightMap(File path) {
        BufferedImage heightImage;
        String pathStr = path.toString();
        //pathStr.replaceAll("/", "V\");
        String launchPath = pathStr.substring(0, pathStr.lastIndexOf("\\") + 1) + "terrain.heightmap.png";
        LOG.info("HeightMap path = {}", launchPath);
        try {
            heightImage = ImageIO.read(new File(launchPath));
            heightMapImage = new BufferedImage(heightImage.getWidth(), heightImage.getHeight(), BufferedImage.TYPE_USHORT_565_RGB);
            //heightMapImage = getNewBufferImage(heightImage.getWidth(), heightImage.getHeight());
            LOG.info("type = {}", heightMapImage.toString());
            Graphics2D g = (Graphics2D) heightMapImage.getGraphics();
            g.drawImage( heightImage, 0, 0, heightImage.getWidth(), heightImage.getHeight(), null);
            g.dispose();
        } catch (IOException e) {
            JOptionPane.showMessageDialog(editor, localeString.getString("dialog_heightmap_not_found"), localeString.getString("dialog_heightmap_not_found_title"), JOptionPane.ERROR_MESSAGE);
            LOG.info("Failed to load HeightMap");
            fixNodesHeightMenuItem.setEnabled(false);
            e.printStackTrace();
        }
    }

    public static void useDefaultMapImage() {
        String fullPath = "/mapImages/Blank.png";
        URL url = AutoDriveEditor.class.getResource(fullPath);
        if (url != null) {
            try {
                mapImage = ImageIO.read(url);
                setImage(mapImage);
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
        if (DEBUG) LOG.info("Accelerated BackBufferImage = {}", gc.getImageCapabilities().isAccelerated());
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
        if (DEBUG) LOG.info("Accelerated bufferImage = {}", gc.getImageCapabilities().isAccelerated());
        bufferImage.setAccelerationPriority(1);
        return bufferImage;
    }

    public static BufferedImage getImage() {
        return image;
    }

    public static void setImage(BufferedImage loadedImage) {
        if (loadedImage != null) {
            LOG.info("Selected Image size is {} x {}",loadedImage.getWidth(), loadedImage.getHeight());
            if (loadedImage.getWidth() != 2048 || loadedImage.getHeight() != 2048 ) {
                String message;
                if (configVersion == FS19_CONFIG) {
                    message = localeString.getString("dialog_mapimage_incorrect_size") + "\n\n" + localeString.getString("dialog_mapimage_incorrect_size_fs19");
                } else if (configVersion == FS22_CONFIG) {
                    message = localeString.getString("dialog_mapimage_incorrect_size") + "\n\n" + localeString.getString("dialog_mapimage_incorrect_size_fs22");
                } else {
                    message = localeString.getString("dialog_mapimage_incorrect_size");
                }
                LOG.info(message);
                JOptionPane.showConfirmDialog(AutoDriveEditor.editor, message, "AutoDriveEditor", JOptionPane.DEFAULT_OPTION, JOptionPane.ERROR_MESSAGE);
                return;
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

    public static void getMapZoomFactor(String mapName) {
        for (int i = 0; i <= mapZoomStore.size() - 1; i++) {
            MapZoomStore store = mapZoomStore.get(i);
            if (store.mapName.equals(mapName)) {
                //return store.zoomFactor;
                LOG.info("Update zoomfactor for {}", mapName);
                updateMapZoomFactor(store.zoomFactor);
                if (store.zoomFactor == 1) {
                    zoomOneX.setSelected(true);
                } else if (store.zoomFactor == 2) {
                    zoomFourX.setSelected(true);
                } else if (store.zoomFactor == 4) {
                    zoomSixteenX.setSelected(true);
                }
            }
        }
    }

    public static void updateMapZoomStore(String mapName, int zoomFactor) {
        boolean found = false;
        for (int i = 0; i <= mapZoomStore.size() - 1; i++) {
            MapZoomStore store = mapZoomStore.get(i);
            if (store.mapName.equals(mapName)) {
                LOG.info("found {} in list of known zoomFactors, setting to {}x", store.mapName, zoomFactor);
                store.zoomFactor = zoomFactor;
                found = true;
                break;
            }
        }
        if (!found) {
            LOG.info("New map detected");
            mapZoomStore.add(new MapZoomStore(mapName, 1));
        }
    }
}
