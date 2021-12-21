package AutoDriveEditor.MapPanel;

import AutoDriveEditor.AutoDriveEditor;
import AutoDriveEditor.GUI.GUIBuilder;
import AutoDriveEditor.GUI.MenuBuilder;

import javax.imageio.ImageIO;
import java.awt.image.BufferedImage;
import java.io.File;
import java.io.IOException;
import java.net.MalformedURLException;
import java.net.URL;

import static AutoDriveEditor.GUI.GUIBuilder.showInTextArea;
import static AutoDriveEditor.GUI.MenuBuilder.*;
import static AutoDriveEditor.Locale.LocaleManager.localeString;
import static AutoDriveEditor.MapPanel.MapPanel.getMapPanel;
import static AutoDriveEditor.MapPanel.MapPanel.updateMapZoomFactor;
import static AutoDriveEditor.Utils.FileUtils.copyURLToFile;
import static AutoDriveEditor.Utils.FileUtils.getCurrentLocation;
import static AutoDriveEditor.Utils.LoggerUtils.LOG;
import static AutoDriveEditor.XMLConfig.EditorXML.*;

public class MapImage {

    public static BufferedImage mapImage;
    private static boolean bImageFound = false;

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
            getMapPanel().setImage(mapImage);
        } else {
            LOG.info("Using Default Image");
            useDefaultMapImage();
        }

        GUIBuilder.updateGUIButtons(true);
        getMapPanel().repaint();
        getMapZoomFactor(mapName);

        MenuBuilder.mapMenuEnabled(true);
        //editorState = GUIBuilder.EDITORSTATE_NOOP;

    }

    public static void useDefaultMapImage() {
        String fullPath = "/mapImages/Blank.png";
        URL url = AutoDriveEditor.class.getResource(fullPath);
        if (url != null) {
            try {
                mapImage = ImageIO.read(url);
                getMapPanel().setImage(mapImage);
            } catch (IOException e) {
                e.printStackTrace();
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
