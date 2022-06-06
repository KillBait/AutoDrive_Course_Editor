package AutoDriveEditor.Listeners;

import AutoDriveEditor.Managers.CopyPasteManager;
import AutoDriveEditor.MapPanel.MapPanel;

import javax.imageio.ImageIO;
import javax.swing.*;
import javax.swing.filechooser.FileFilter;
import java.awt.*;
import java.awt.event.*;
import java.awt.geom.Point2D;
import java.awt.image.BufferedImage;
import java.io.File;
import java.io.IOException;

import static AutoDriveEditor.AutoDriveEditor.*;
import static AutoDriveEditor.GUI.GUIBuilder.*;
import static AutoDriveEditor.GUI.MenuBuilder.*;
import static AutoDriveEditor.GUI.RoutesGUI.createRoutesGUI;
import static AutoDriveEditor.Locale.LocaleManager.localeString;
import static AutoDriveEditor.Managers.ImportManager.*;
import static AutoDriveEditor.Managers.ScanManager.mergeOverlappingNodes;
import static AutoDriveEditor.Managers.ScanManager.scanNetworkForOverlapNodes;
import static AutoDriveEditor.Managers.VersionManager.createHyperLink;
import static AutoDriveEditor.MapPanel.MapImage.*;
import static AutoDriveEditor.MapPanel.MapPanel.*;
import static AutoDriveEditor.Utils.FileUtils.getCurrentLocation;
import static AutoDriveEditor.Utils.FileUtils.getSelectedFileWithExtension;
import static AutoDriveEditor.Utils.GUIUtils.showInTextArea;
import static AutoDriveEditor.Utils.LoggerUtils.LOG;
import static AutoDriveEditor.XMLConfig.EditorXML.*;
import static AutoDriveEditor.XMLConfig.GameXML.*;
import static AutoDriveEditor.XMLConfig.RouteManagerXML.loadRouteManagerXML;
import static AutoDriveEditor.XMLConfig.RouteManagerXML.saveRouteManagerXML;

public class MenuListener implements ActionListener, ItemListener {

    @Override
    public void actionPerformed(ActionEvent e) {
        if (DEBUG) LOG.info("MenuListener ActionCommand: {}", e.getActionCommand());

        JFileChooser fc = new JFileChooser(lastLoadLocation);

        switch (e.getActionCommand()) {
            case MENU_LOAD_CONFIG:
                if (getMapPanel().isStale()) {
                    int response = JOptionPane.showConfirmDialog(editor, localeString.getString("dialog_exit_unsaved"), "AutoDrive", JOptionPane.YES_NO_OPTION, JOptionPane.QUESTION_MESSAGE);
                    if (response == JOptionPane.YES_OPTION) {
                        saveConfigFile(null, false, false);
                    }
                }
                fc.setDialogTitle(localeString.getString("dialog_load_config_xml_title"));
                fc.setFileSelectionMode(JFileChooser.FILES_ONLY);
                fc.setFileFilter(new FileFilter() {
                    @Override
                    public boolean accept(File f) {
                        // always accept directory's
                        if (f.isDirectory()) return true;
                        // but only files with a specific name
                        return f.getName().contains(".xml");
                    }

                    @Override
                    public String getDescription() {
                        return "AutoDrive Config (.xml)";
                    }
                });

                if (fc.showOpenDialog(editor) == JFileChooser.APPROVE_OPTION) {
                    canAutoSave = false;
                    lastLoadLocation = fc.getCurrentDirectory().getAbsolutePath();
                    getMapPanel().confirmCurve();
                    File fileName = fc.getSelectedFile();
                    if (loadConfigFile(fileName)) {
                        forceMapImageRedraw();
                        isUsingImportedImage = false;
                        saveImageEnabled(false);
                        getMapPanel().setStale(false);
                        scanNetworkForOverlapNodes();
                        bShowHeightMap = false;
                        showHeightMapMenuItem.setSelected(false);
                        canAutoSave=true;
                    }
                }
                break;
            case MENU_SAVE_CONFIG:
                saveConfigFile(null, false, false);
                break;
            case MENU_SAVE_SAVEAS:
                if (xmlConfigFile == null) break;
                fc.setDialogTitle(localeString.getString("dialog_save_destination"));
                fc.setFileSelectionMode(JFileChooser.FILES_ONLY);
                fc.setFileFilter(new FileFilter() {
                    @Override
                    public boolean accept(File f) {
                        // always accept directory's
                        if (f.isDirectory()) return true;
                        // but only files with a specific name
                        return f.getName().contains(".xml");
                    }

                    @Override
                    public String getDescription() {
                        return "AutoDrive Config (.xml)";
                    }
                });
                fc.setSelectedFile(xmlConfigFile);

                if (fc.showSaveDialog(editor) == JFileChooser.APPROVE_OPTION) {
                    lastLoadLocation = fc.getCurrentDirectory().getAbsolutePath();
                    LOG.info("{} {}", localeString.getString("console_config_save_as"), getSelectedFileWithExtension(fc));
                    saveConfigFile(getSelectedFileWithExtension(fc).toString(), false, false);
                }
                break;
            case MENU_LOAD_ROUTES_MANAGER_CONFIG:
                fc.setDialogTitle(localeString.getString("dialog_load_routemanager_config_title"));
                fc.setFileSelectionMode(JFileChooser.FILES_ONLY);
                fc.setFileFilter(new FileFilter() {
                    @Override
                    public boolean accept(File f) {
                        // always accept directory's
                        if (f.isDirectory()) return true;
                        // but only files with a specific name
                        return f.getName().equals("routes.xml");
                    }

                    @Override
                    public String getDescription() {
                        return "AutoDrive RouteManager Config (.xml)";
                    }
                });

                if (fc.showOpenDialog(editor) == JFileChooser.APPROVE_OPTION) {
                    canAutoSave = false;
                    lastLoadLocation = fc.getCurrentDirectory().getAbsolutePath();
                    //getMapPanel().confirmCurve();
                    File fileName = fc.getSelectedFile();

                    createRoutesGUI(fileName, editor);
                    configType = CONFIG_ROUTEMANAGER;
                    canAutoSave = true;
                }
                break;
            case MENU_LOAD_ROUTES_MANAGER_XML:
                if (getMapPanel().isStale()) {
                    int response = JOptionPane.showConfirmDialog(editor, localeString.getString("dialog_exit_unsaved"), "AutoDrive", JOptionPane.YES_NO_OPTION, JOptionPane.QUESTION_MESSAGE);
                    if (response == JOptionPane.YES_OPTION) {
                        saveConfigFile(null, false, false);
                    }
                }
                fc.setDialogTitle(localeString.getString("dialog_load_route_xml_title"));
                fc.setFileSelectionMode(JFileChooser.FILES_ONLY);
                fc.setFileFilter(new FileFilter() {
                    @Override
                    public boolean accept(File f) {
                        // always accept directory's
                        if (f.isDirectory()) return true;
                        // but only files with a specific name
                        return f.getName().contains(".xml") && !f.getName().equals("routes.xml");
                    }

                    @Override
                    public String getDescription() {
                        return "AutoDrive Route XML (.xml)";
                    }
                });

                if (fc.showOpenDialog(editor) == JFileChooser.APPROVE_OPTION) {
                    canAutoSave = false;
                    lastLoadLocation = fc.getCurrentDirectory().getAbsolutePath();
                    getMapPanel().confirmCurve();
                    File fileName = fc.getSelectedFile();
                    if (loadRouteManagerXML(fileName, false, null)) {
                        isUsingImportedImage = false;
                        saveImageEnabled(false);
                        getMapPanel().setStale(false);
                        canAutoSave = true;
                    }
                }
                break;
            case MENU_SAVE_ROUTES_MANAGER_XML:
                saveRouteManagerXML(null, false, false);
                break;
            case MENU_EXIT:
                editor.dispatchEvent(new WindowEvent(editor, WindowEvent.WINDOW_CLOSING));
                break;
            case MENU_EDIT_CUT:
            case MENU_EDIT_COPY:
            case MENU_EDIT_PASTE:
                break;
            case MENU_LOAD_IMAGE:
                fc.setDialogTitle(localeString.getString("dialog_load_image_title"));
                fc.setFileSelectionMode(JFileChooser.FILES_ONLY);
                fc.setFileFilter(new FileFilter() {
                    @Override
                    public boolean accept(File f) {
                        // always accept directory's
                        if (f.isDirectory()) return true;
                        // but only files with a specific name
                        return f.getName().contains(".png");
                    }

                    @Override
                    public String getDescription() {
                        return "AutoDrive MapImage (.png)";
                    }
                });

                if (fc.showOpenDialog(editor) == JFileChooser.APPROVE_OPTION) {
                    LOG.info("Manual loading of map image selected");
                    File fileName;
                    try {
                        fileName = fc.getSelectedFile();
                        BufferedImage mapImage = ImageIO.read(fileName);
                        if (mapImage != null) {
                            setImage(mapImage, false);
                            forceMapImageRedraw();
                            imageLoadedLabel.setForeground(new Color(150,100,20));
                            imageLoadedLabel.setText("Manual Load");
                        }
                    } catch (IOException e1) {
                        LOG.error(e1.getMessage(), e1);
                    }
                }
                break;
            case MENU_SAVE_IMAGE:
                String currentSavePath;
                if (!oldConfigFormat) {
                    currentSavePath = getCurrentLocation() + "mapImages/" + roadMap.mapName + "/" + roadMap.mapName + ".png";
                } else {
                    currentSavePath = getCurrentLocation() + "mapImages/unknown.png";
                }

                LOG.info("Save Image to -- {}", currentSavePath);
                File savePath = new File(currentSavePath);
                try {
                    if (savePath.exists()) {
                        if (savePath.isDirectory())
                            throw new IOException("File '" + savePath + "' is a directory");

                        if (!savePath.canWrite())
                            throw new IOException("File '" + savePath + "' cannot be written");
                    } else {
                        File saveParent = savePath.getParentFile();
                        LOG.info("parent = {}", saveParent.getName());
                        if (!saveParent.exists() && (!saveParent.mkdirs())) {
                            throw new IOException("'" + savePath + "' could not be created");
                        }
                    }
                } catch (IOException ioException) {
                    ioException.printStackTrace();
                }

                fc.setDialogTitle(localeString.getString("dialog_save_mapimage"));
                fc.setFileSelectionMode(JFileChooser.FILES_ONLY);
                fc.setFileFilter(new FileFilter() {
                    @Override
                    public boolean accept(File f) {
                        // always accept directory's
                        if (f.isDirectory()) return true;
                        // but only files with a specific name
                        return f.getName().contains(".png");
                    }

                    @Override
                    public String getDescription() {
                        return "AutoDrive Map Image (.png)";
                    }
                });
                fc.setSelectedFile(savePath);
                fc.setCurrentDirectory(savePath);
                if (fc.showSaveDialog(editor) == JFileChooser.APPROVE_OPTION) {
                    File saveImageFile = getSelectedFileWithExtension(fc);
                    if (saveImageFile.exists()) {
                        int response = JOptionPane.showConfirmDialog(editor, localeString.getString("dialog_overwrite"), "File already exists " + roadMap.mapName + ".png", JOptionPane.YES_NO_OPTION, JOptionPane.QUESTION_MESSAGE);
                        if (response == JOptionPane.NO_OPTION) {
                            showInTextArea("Cancelled saving of converted image", true, true);
                            break;
                        }
                    }
                    boolean exportSuccess = exportImageToDisk(getImage(), getSelectedFileWithExtension(fc).toString());
                    if (exportSuccess) {
                        imageLoadedLabel.setForeground(new Color(0, 100, 0));
                        imageLoadedLabel.setText("Saved");
                        showInTextArea("Export of mapImage '" + fc.getSelectedFile().getName() + "' Successful", true, true);
                    } else {
                        imageLoadedLabel.setForeground(new Color(200, 0, 0));
                        imageLoadedLabel.setText("Imported");
                        showInTextArea("Export of mapImage '" + fc.getSelectedFile().getName() + "' Failed", true, true);
                    }
                }
                break;
            case MENU_IMPORT_FS19_DDS:
                fc.setDialogTitle(localeString.getString("dialog_import_FS19_dds_image_title"));
                fc.setFileSelectionMode(JFileChooser.FILES_ONLY);
                fc.setFileFilter(new FileFilter() {
                    @Override
                    public boolean accept(File f) {
                        // always accept directory's
                        if (f.isDirectory()) return true;
                        // but only files with a specific name
                        return f.getName().equals("pda_map_H.dds");
                    }

                    @Override
                    public String getDescription() {
                        return "FS19 PDA Image (pda_map_H.dds)";
                    }
                });
                if (fc.showOpenDialog(editor) == JFileChooser.APPROVE_OPTION) {
                    if (!fc.getSelectedFile().getName().equals("pda_map_H.dds") && !fc.getSelectedFile().getName().endsWith(".dds")) {
                        JOptionPane.showMessageDialog(editor, "The file " + fc.getSelectedFile() + " is not a valid dds file.", "FileType Error", JOptionPane.ERROR_MESSAGE);
                        break;
                    }

                    boolean importResult = importFromFS19(fc.getSelectedFile().getAbsoluteFile().toString());
                    if (importResult) {
                        isUsingImportedImage = true;
                        saveImageEnabled(true);
                        imageLoadedLabel.setForeground(new Color(191, 56, 14));
                        imageLoadedLabel.setText("Imported");
                        showInTextArea("Import of FS19 mapImage '" + fc.getSelectedFile().getName() + "' Successful", true, true);
                    }
                } else {
                    showInTextArea("Cancelled import of FS19 PDA Image", true, true);
                }
                break;
            case MENU_IMPORT_FS22_DDS:
                fc.setDialogTitle(localeString.getString("dialog_import_FS22_dds_image_title"));
                fc.setFileSelectionMode(JFileChooser.FILES_ONLY);
                fc.setFileFilter(new FileFilter() {
                    @Override
                    public boolean accept(File f) {
                        // always accept directory's
                        if (f.isDirectory()) return true;
                        // but only files with a specific name
                        return f.getName().equals("overview.dds");
                    }

                    @Override
                    public String getDescription() {
                        return "FS22 PDA image (overview.dds)";
                    }
                });
                if (fc.showOpenDialog(editor) == JFileChooser.APPROVE_OPTION) {
                    if (!fc.getSelectedFile().getName().equals("overview.dds") && !fc.getSelectedFile().getName().endsWith(".dds")) {
                        JOptionPane.showMessageDialog(editor, "The file " + fc.getSelectedFile() + " is not a valid dds file.", "FileType Error", JOptionPane.ERROR_MESSAGE);
                        break;
                    }

                    boolean importResult = importFromFS22(fc.getSelectedFile().getAbsoluteFile().toString());
                    if (importResult) {
                        isUsingImportedImage = true;
                        saveImageEnabled(true);
                        imageLoadedLabel.setForeground(new Color(191, 56, 14));
                        imageLoadedLabel.setText("Imported");
                        showInTextArea("Import of FS22 mapImage '" + fc.getSelectedFile().getName() + "' Successful", true, true);
                    }
                } else {
                    showInTextArea("Cancelled import of FS22 PDA Image", true, true);
                }
                break;
            case MENU_HEIGHTMAP_LOAD:
                fc.setDialogTitle(localeString.getString("dialog_heightmap_load_title"));
                fc.setFileSelectionMode(JFileChooser.FILES_ONLY);
                fc.setFileFilter(new FileFilter() {
                    @Override
                    public boolean accept(File f) {
                        // always accept directory's
                        if (f.isDirectory()) return true;
                        // but only files with a specific name
                        return f.getName().equals("terrain.heightmap.png");
                    }

                    @Override
                    public String getDescription() {
                        return "FS HeightMap File (terrain.heightmap.png)";
                    }
                });
                if (fc.showOpenDialog(editor) == JFileChooser.APPROVE_OPTION) {
                    File fileName = fc.getSelectedFile();
                    loadHeightMap(fileName, true);
                }
                break;
            case MENU_HEIGHTMAP_SAVE:
                String currentExportPath;
                if (!oldConfigFormat) {
                    currentExportPath = getCurrentLocation() + "mapImages/" + roadMap.mapName + "/" + roadMap.mapName + "_HeightMap.png";
                } else {
                    currentExportPath = getCurrentLocation() + "mapImages/unknown_HeightMap.png";
                }

                LOG.info("Export HeightMap path = {}", currentExportPath);
                File exportPath = new File(currentExportPath);
                try {
                    if (exportPath.exists()) {
                        if (exportPath.isDirectory())
                            throw new IOException("File '" + exportPath + "' is a directory");

                        if (!exportPath.canWrite())
                            throw new IOException("File '" + exportPath + "' cannot be written");
                    } else {
                        File exportParent = exportPath.getParentFile();
                        LOG.info("parent = {}", exportParent.getName());
                        if (!exportParent.exists() && (!exportParent.mkdirs())) {
                            throw new IOException("'" + exportPath + "' could not be created");
                        }
                    }
                } catch (IOException ioException) {
                    ioException.printStackTrace();
                }

                fc.setDialogTitle(localeString.getString("dialog_heightmap_export_title"));
                fc.setFileSelectionMode(JFileChooser.FILES_ONLY);
                fc.setFileFilter(new FileFilter() {
                    @Override
                    public boolean accept(File f) {
                        // always accept directory's
                        if (f.isDirectory()) return true;
                        // but only files with a specific name
                        return f.getName().contains("_HeightMap.png");
                    }

                    @Override
                    public String getDescription() {
                        return "AutoDrive Heightmap Image (_HeightMap.png)";
                    }
                });
                fc.setSelectedFile(exportPath);
                fc.setCurrentDirectory(exportPath);
                if (fc.showSaveDialog(editor) == JFileChooser.APPROVE_OPTION) {
                    File saveImageFile = getSelectedFileWithExtension(fc);
                    if (saveImageFile.exists()) {
                        int response = JOptionPane.showConfirmDialog(editor, localeString.getString("dialog_overwrite"), "File already exists " + roadMap.mapName + ".png", JOptionPane.YES_NO_OPTION, JOptionPane.QUESTION_MESSAGE);
                        if (response == JOptionPane.NO_OPTION) {
                            showInTextArea("Cancelled export of default heightmap", true, true);
                            break;
                        }
                    }
                    LOG.info("{} {}", localeString.getString("console_heightmap_export_default"), saveImageFile);
                    if (exportImageToDisk(heightMapImage, getSelectedFileWithExtension(fc).toString())) {
                        heightMapLoadedLabel.setForeground(new Color(0,100,0));
                        heightMapLoadedLabel.setText("Loaded");
                        showInTextArea("Export to " + exportPath + " Successful", true, true);
                    }
                }
                break;
            case MENU_ZOOM_2km:  // 2km
                updateMapZoomFactor(1);
                break;
            case MENU_ZOOM_4km:  // 4km
                updateMapZoomFactor(2);
                break;
            case MENU_ZOOM_6km:  // 6km
                updateMapZoomFactor(3);
                break;
            case MENU_ZOOM_8km: // 8km
                updateMapZoomFactor(4);
                break;
            case MENU_ZOOM_10km: // 10km
                updateMapZoomFactor(5);
                break;
            case MENU_ZOOM_12km: // 12km
                updateMapZoomFactor(6);
                break;
            case MENU_ZOOM_14km: // 14km
                updateMapZoomFactor(7);
                break;
            case MENU_ZOOM_16km: // 16km
                updateMapZoomFactor(8);
                break;
            case MENU_ZOOM_18km: // 18km
                updateMapZoomFactor(9);
                break;
            case MENU_ZOOM_20km: // 20km
                updateMapZoomFactor(10);
                break;
            case MENU_ZOOM_22km: // 22km
                updateMapZoomFactor(11);
                break;
            case MENU_ZOOM_24km: // 24km
                updateMapZoomFactor(12);
                break;
            case MENU_ZOOM_26km: // 26km
                updateMapZoomFactor(13);
                break;
            case MENU_ZOOM_28km: // 28km
                updateMapZoomFactor(14);
                break;
            case MENU_ZOOM_30km: // 30km
                updateMapZoomFactor(15);
                break;
            case MENU_ZOOM_32km: // 32km
                updateMapZoomFactor(16);
                break;
            case MENU_ZOOM_34km: // 34km
                updateMapZoomFactor(17);
                break;
            case MENU_ZOOM_36km: // 36km
                updateMapZoomFactor(18);
                break;
            case MENU_ZOOM_38km: // 38km
                updateMapZoomFactor(19);
                break;
            case MENU_ZOOM_40km: // 40km
                updateMapZoomFactor(20);
                break;
            case MENU_ZOOM_42km: // 42km
                updateMapZoomFactor(21);
                break;
            case MENU_ZOOM_44km: // 44km
                updateMapZoomFactor(22);
                break;
            case MENU_ZOOM_46km: // 46km
                updateMapZoomFactor(23);
                break;
            case MENU_EDIT_UNDO:
                changeManager.undo();
                enableMultiSelect();
                break;
            case MENU_EDIT_REDO:
                changeManager.redo();
                enableMultiSelect();
                break;
            case MENU_GRID_SET:
                getMapPanel().showGridSettingDialog();
                break;
            case MENU_ROTATE_SET:
                getMapPanel().showRotationSettingDialog();
                break;
            case MENU_ROTATE_CLOCKWISE:
                CopyPasteManager.rotateSelected(rotationAngle);
                break;
            case MENU_ROTATE_ANTICLOCKWISE:
                CopyPasteManager.rotateSelected(-rotationAngle);
                break;
            case MENU_ROTATE_CLOCKWISE_NINTY:
                CopyPasteManager.rotateSelected(90);
                break;
            case MENU_ROTATE_ANTICLOCKWISE_NINTY:
                CopyPasteManager.rotateSelected(-90);
                break;
            case MENU_ABOUT:
                showAbout();
                break;
            case MENU_HEIGHTMAP_FIX:
                fixNodeHeight();
                break;
            case MENU_SCAN_OVERLAP:
                mapPanel.showScanDialog();
                break;
            case MENU_SCAN_MERGE:
                mergeOverlappingNodes();
                break;
            case BUTTON_COPYPASTE_CUT:
                cutSelected();
                break;
            case BUTTON_COPYPASTE_COPY:
                copySelected();
                break;
            case BUTTON_COPYPASTE_PASTE:
                pasteSelected();
                break;
            case BUTTON_COPYPASTE_PASTE_ORIGINAL:
                pasteSelectedInOriginalLocation();
                break;
            case MENU_DEBUG_MOVETO_NODE:
                if (hoveredNode != null) {
                    Point2D target = worldPosToScreenPos(hoveredNode.x, hoveredNode.z);
                    double x = (getMapPanel().getWidth() >> 1) - target.getX();
                    double y = (getMapPanel().getHeight() >> 1) - target.getY();
                    getMapPanel().moveMapBy((int)x,(int)y);
                } else {
                    centreNode();
                }
                break;
        }

    }

    @Override
    public void itemStateChanged(ItemEvent e) {
        AbstractButton menuItem = (AbstractButton) e.getItem();
        if (DEBUG) LOG.info("Menu ItemStateChange: {}", menuItem.getActionCommand());
        switch (menuItem.getActionCommand()) {
            case MENU_HEIGHTMAP_SHOW:
                bShowHeightMap = menuItem.isSelected();
                if (bShowHeightMap) {
                    setImage(heightMapImage, true);
                } else {
                    setImage(mapImage, false);
                }
                forceMapImageRedraw();
                //MapPanel.getMapPanel().repaint();
                break;
            case MENU_GRID_SHOW:
                bShowGrid = menuItem.isSelected();
                MapPanel.getMapPanel().repaint();
                break;
            case MENU_GRID_SNAP:
                bGridSnap = menuItem.isSelected();
                if (!menuItem.isSelected()) {
                    bGridSnapSubs = false;
                    gridSnapSubDivisionMenuItem.setSelected(false);
                }
                break;
            case MENU_GRID_SNAP_SUBS:
                bGridSnapSubs = menuItem.isSelected();
                break;
            case MENU_DEBUG_SHOWID:
                bDebugShowID = menuItem.isSelected();
                mapPanel.repaint();
                break;
            case MENU_DEBUG_SELECTED_LOCATION:
                bDebugShowSelectedLocation = menuItem.isSelected();
                break;
            case MENU_DEBUG_LOG_FILEIO:
                bDebugLogFileIO = menuItem.isSelected();
                break;
            case MENU_DEBUG_PROFILE:
                bDebugProfile = menuItem.isSelected();
                break;
            case MENU_DEBUG_LOG_UNDO:
                bDebugLogUndoRedo = menuItem.isSelected();
                break;
            case MENU_DEBUG_HEIGHTMAP:
                 bDebugShowHeightMapInfo = menuItem.isSelected();
                if (!menuItem.isSelected()) showInTextArea("", true, false);
                break;
            case MENU_DEBUG_LOG_MERGE:
                bDebugLogMerge = menuItem.isSelected();
                break;
            case MENU_DEBUG_LOG_ROUTEMANAGER:
                bDebugLogRouteManager = menuItem.isSelected();
                break;
            case MENU_DEBUG_TEST:
                bDebugTest = menuItem.isSelected();
                break;
            case MENU_DEBUG_LOG_CURVEINFO:
                bDebugLogCurveInfo = menuItem.isSelected();
                break;
            case MENU_DEBUG_LOG_MARKERS:
                bDebugLogMarkerInfo = menuItem.isSelected();
                break;
            case MENU_DEBUG_LOG_RENDER:
                bDebugLogRenderInfo = menuItem.isSelected();
                break;
            case MENU_DEBUG_LOG_GUI:
                bDebugLogGUIInfo = menuItem.isSelected();
                break;
            case MENU_DEBUG_LOG_CONFIG:
                bDebugLogConfigInfo = menuItem.isSelected();
                break;
            case MENU_DEBUG_LOG_COPYPASTE:
                bDebugLogCopyPasteInfo = menuItem.isSelected();
                break;
            case MENU_DEBUG_ENABLE:
                bDebugEnable = menuItem.isSelected();
                debugMenu.setVisible(menuItem.isSelected());
                DEBUG = menuItem.isSelected();
                break;
        }
    }

    private void showAbout() {
        String mainText = "<html><center>Editor version : " + COURSE_EDITOR_VERSION + " by <b>KillBait!</b><br><br>Build info : " + COURSE_EDITOR_BUILD_INFO + "<br><br><u>AutoDrive Development Team</u><br><br><b>Stephan (Founder/Modder and Original Editor Author)</b><br><br>TyKonKet (Modder)<br>Oliver (Modder)<br>Axel (Co-Modder)<br>Aletheist (Co-Modder)<br>Willi (Supporter & Tester)<br>Iwan1803 (Community Manager & Supporter)<br><br><b>Many thanks goto Stephan for allowing me to take over the development of this editor";
        String linkText = "<br><br>Visit AutoDrive Editor HomePage</b>";
        JEditorPane editorLink = createHyperLink(mainText,linkText, "https://github.com/KillBait/AutoDrive_Course_Editor");
        JOptionPane.showMessageDialog(editor, editorLink, "About AutoDrive Editor", JOptionPane.PLAIN_MESSAGE);
    }

    public static void enableMultiSelect() {
        switch (editorState) {
            case EDITORSTATE_MOVING:
            case EDITORSTATE_CHANGE_NODE_PRIORITY:
            case EDITORSTATE_DELETE_NODES:
            case EDITORSTATE_DELETE_MARKER:
            case EDITORSTATE_ALIGN_HORIZONTAL:
            case EDITORSTATE_ALIGN_VERTICAL:
            case EDITORSTATE_CNP_SELECT:
                isMultiSelectAllowed = true;
                return;
        }
        isMultiSelectAllowed = false;
    }
}
