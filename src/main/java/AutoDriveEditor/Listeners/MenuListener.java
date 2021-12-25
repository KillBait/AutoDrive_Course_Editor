package AutoDriveEditor.Listeners;

import javax.imageio.ImageIO;
import javax.swing.*;
import javax.swing.filechooser.FileFilter;
import javax.swing.filechooser.FileNameExtensionFilter;
import java.awt.event.*;
import java.awt.image.BufferedImage;
import java.io.File;
import java.io.IOException;

import AutoDriveEditor.Managers.CopyPasteManager;
import AutoDriveEditor.MapPanel.MapPanel;

import static AutoDriveEditor.AutoDriveEditor.*;
import static AutoDriveEditor.GUI.GUIBuilder.*;
import static AutoDriveEditor.GUI.GUIUtils.showInTextArea;
import static AutoDriveEditor.GUI.MenuBuilder.*;
import static AutoDriveEditor.Import.ImportManager.*;
import static AutoDriveEditor.Locale.LocaleManager.*;
import static AutoDriveEditor.MapPanel.MapImage.*;
import static AutoDriveEditor.MapPanel.MapPanel.*;
import static AutoDriveEditor.Utils.FileUtils.*;
import static AutoDriveEditor.Utils.LoggerUtils.*;
import static AutoDriveEditor.XMLConfig.EditorXML.*;
import static AutoDriveEditor.XMLConfig.GameXML.*;

public class MenuListener implements ActionListener, ItemListener {

    @Override
    public void actionPerformed(ActionEvent e) {
        LOG.info("ActionCommand: {}", e.getActionCommand());
        getMapPanel().isMultiSelectAllowed = false;

        JFileChooser fc = new JFileChooser();

        switch (e.getActionCommand()) {
            case MENU_LOAD_CONFIG:
                if (getMapPanel().isStale()) {
                    int response = JOptionPane.showConfirmDialog(editor, localeString.getString("dialog_exit_unsaved"), "AutoDrive", JOptionPane.YES_NO_OPTION, JOptionPane.QUESTION_MESSAGE);
                    if (response == JOptionPane.YES_OPTION) {
                        saveConfigFile(null);
                    }
                }

                fc.setDialogTitle(localeString.getString("dialog_load_config_title"));
                fc.setFileSelectionMode(JFileChooser.FILES_ONLY);
                fc.setAcceptAllFileFilterUsed(false);
                FileNameExtensionFilter filter = new FileNameExtensionFilter("AutoDrive config", "xml");
                fc.addChoosableFileFilter(filter);

                if (fc.showOpenDialog(editor) == JFileChooser.APPROVE_OPTION) {
                    getMapPanel().confirmCurve();
                    File fileName = fc.getSelectedFile();
                    loadConfigFile(fileName);
                    loadHeightMap(fileName);
                    forceMapImageRedraw();
                    isUsingConvertedImage = false;
                    saveImageEnabled(false);
                    getMapPanel().setStale(false);

                }

                break;
            case MENU_SAVE_CONFIG:
                saveConfigFile(null);
                break;
            case MENU_SAVE_SAVEAS:
                if (xmlConfigFile == null) break;
                fc.setDialogTitle(localeString.getString("dialog_save_destination"));
                fc.setFileSelectionMode(JFileChooser.FILES_ONLY);
                fc.setAcceptAllFileFilterUsed(false);
                FileNameExtensionFilter saveFilter = new FileNameExtensionFilter("AutoDrive config", "xml");
                fc.setSelectedFile(xmlConfigFile);
                fc.addChoosableFileFilter(saveFilter);

                if (fc.showSaveDialog(editor) == JFileChooser.APPROVE_OPTION) {
                    LOG.info("{} {}", localeString.getString("console_config_saveas"), getSelectedFileWithExtension(fc));
                    saveConfigFile(getSelectedFileWithExtension(fc).toString());
                }
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

                if (fc.showOpenDialog(editor) == JFileChooser.APPROVE_OPTION) {
                    File fileName;
                    try {
                        fileName = fc.getSelectedFile();
                        BufferedImage mapImage = ImageIO.read(fileName);
                        if (mapImage != null) {
                            setImage(mapImage);
                            forceMapImageRedraw();
                            //MapPanel.getMapPanel().moveMapBy(0,1); // hacky way to get map image to refresh
                        }
                    } catch (IOException e1) {
                        LOG.error(e1.getMessage(), e1);
                    }
                }
                break;
            case MENU_SAVE_IMAGE:
                String currentPath;
                if (!oldConfigFormat) {
                    currentPath = getCurrentLocation() + "mapImages/" + roadMap.roadMapName + ".png";
                } else {
                    currentPath = getCurrentLocation() + "mapImages/unknown.png";
                }

                LOG.info("currentpath = {}", currentPath);
                File path = new File(currentPath);
                try {
                    if (path.exists()) {
                        if (path.isDirectory())
                            throw new IOException("File '" + path + "' is a directory");

                        if (!path.canWrite())
                            throw new IOException("File '" + path + "' cannot be written");
                    } else {
                        File parent = path.getParentFile();
                        LOG.info("parent = {}", parent.getName());
                        if (!parent.exists() && (!parent.mkdirs())) {
                            throw new IOException("'" + path + "' could not be created");
                        }
                    }
                } catch (IOException ioException) {
                    ioException.printStackTrace();
                }

                LOG.info("path = {}", currentPath);
                fc.setDialogTitle(localeString.getString("dialog_save_mapimage"));
                fc.setFileSelectionMode(JFileChooser.FILES_ONLY);
                fc.setAcceptAllFileFilterUsed(false);
                FileNameExtensionFilter imageFilter = new FileNameExtensionFilter("AutoDrive Map Image", "png");
                fc.setSelectedFile(path);
                fc.setCurrentDirectory(path);
                fc.addChoosableFileFilter(imageFilter);

                if (fc.showSaveDialog(editor) == JFileChooser.APPROVE_OPTION) {
                    File saveImageFile = getSelectedFileWithExtension(fc);
                    if (saveImageFile.exists()) {
                        int response = JOptionPane.showConfirmDialog(editor, localeString.getString("dialog_mapimage_overwrite"), "File already exists " + roadMap.roadMapName + ".png", JOptionPane.YES_NO_OPTION, JOptionPane.QUESTION_MESSAGE);
                        if (response == JOptionPane.NO_OPTION) {
                            LOG.info("Cancelled saving of converted image");
                            break;
                        }
                    }
                    LOG.info("{} {}", localeString.getString("console_map_saveimage"), saveImageFile);
                    exportMapImage(getSelectedFileWithExtension(fc).toString());
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
                        return ".dds";
                    }
                });
                if (fc.showOpenDialog(editor) == JFileChooser.APPROVE_OPTION) {
                    if (!fc.getSelectedFile().getName().equals("pda_map_H.dds") && !fc.getSelectedFile().getName().endsWith(".dds")) {
                        JOptionPane.showMessageDialog(editor, "The file " + fc.getSelectedFile() + " is not a valid dds file.", "FileType Error", JOptionPane.ERROR_MESSAGE);
                        break;
                    }

                    LOG.info("Valid Filename {}", fc.getSelectedFile().getAbsoluteFile());
                    boolean result = importFromFS19(fc.getSelectedFile().getAbsoluteFile().toString());
                    if (result) {
                        isUsingConvertedImage = true;
                        saveImageEnabled(true);
                    }
                } else {
                    LOG.info("Cancelled FS19 PDA Image Import");
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
                        return ".dds";
                    }
                });
                if (fc.showOpenDialog(editor) == JFileChooser.APPROVE_OPTION) {
                    if (!fc.getSelectedFile().getName().equals("overview.dds") && !fc.getSelectedFile().getName().endsWith(".dds")) {
                        JOptionPane.showMessageDialog(editor, "The file " + fc.getSelectedFile() + " is not a valid dds file.", "FileType Error", JOptionPane.ERROR_MESSAGE);
                        break;
                    }

                    LOG.info("Valid Filename {}", fc.getSelectedFile().getAbsoluteFile());
                    boolean result = importFromFS22(fc.getSelectedFile().getAbsoluteFile().toString());
                    if (result) {
                        isUsingConvertedImage = true;
                        saveImageEnabled(true);
                    }
                } else {
                    LOG.info("Cancelled FS22 PDA Image Import");
                }
                break;
            case MENU_ZOOM_1x:
                updateMapZoomFactor(1);
                break;
            case MENU_ZOOM_4x:
                updateMapZoomFactor(2);
                break;
            case MENU_ZOOM_16x:
                updateMapZoomFactor(4);
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
            case MENU_HEIGHTMAP_IMPORT:
                break;
        }

    }

    @Override
    public void itemStateChanged(ItemEvent e) {
        AbstractButton button = (AbstractButton) e.getItem();
        switch (button.getActionCommand()) {
            case MENU_CHECKBOX_CONTINUECONNECT:
                bContinuousConnections = button.isSelected();
                break;
            case MENU_CHECKBOX_MIDDLEMOUSEMOVE:
                bMiddleMouseMove = button.isSelected();
                break;
            case MENU_GRID_SHOW:
                bShowGrid = button.isSelected();
                MapPanel.getMapPanel().repaint();
                break;
            case MENU_GRID_SNAP:
                bGridSnap = button.isSelected();
                if (!button.isSelected()) {
                    bGridSnapSubs = false;
                    gridSnapSubDivisionMenuItem.setSelected(false);
                }
                break;
            case MENU_GRID_SNAP_SUBS:
                bGridSnapSubs = button.isSelected();
                break;
            case MENU_DEBUG_SHOWID:
                bDebugShowID = button.isSelected();
                mapPanel.repaint();
                break;
            case MENU_DEBUG_SELECTED_LOCATION:
                bDebugShowSelectedLocation = button.isSelected();
                break;
            case MENU_DEBUG_FILEIO:
                bDebugFileIO = button.isSelected();
                break;
            case MENU_DEBUG_PROFILE:
                bDebugProfile = button.isSelected();
                break;
            case MENU_DEBUG_UNDO:
                bDebugUndoRedo = button.isSelected();
                break;
            case MENU_DEBUG_HEIGHTMAP:
                bDebugHeightMap = button.isSelected();
                if (!button.isSelected()) showInTextArea("", true);
                break;
            case MENU_DEBUG_TEST:
                bDebugTest = button.isSelected();
                isDraggingNode = true;
                MapPanel.getMapPanel().mouseDragged(-1, 0);
                break;
        }
    }

    private void showAbout() {
        JOptionPane.showMessageDialog(editor, "<html><center>Editor version : " + AUTODRIVE_INTERNAL_VERSION + "<br>Build info : Java 13 SDK - IntelliJ IDEA 2021.3 Community Edition<br><br><u>AutoDrive Development Team</u><br><br><b>Stephan (Founder & Modder)</b><br><br>TyKonKet (Modder)<br>Oliver (Modder)<br>Axel (Co-Modder)<br>Aletheist (Co-Modder)<br>Willi (Supporter & Tester)<br>Iwan1803 (Community Manager & Supporter)", "AutoDrive Editor", JOptionPane.PLAIN_MESSAGE);
    }

    public static void enableMultiSelect() {
        switch (editorState) {
            case EDITORSTATE_MOVING:
            case EDITORSTATE_CHANGE_NODE_PRIORITY:
            case EDITORSTATE_DELETE_NODES:
            case EDITORSTATE_DELETING_DESTINATION:
            case EDITORSTATE_ALIGN_HORIZONTAL:
            case EDITORSTATE_ALIGN_VERTICAL:
            case EDITORSTATE_CNP_SELECT:
                mapPanel.isMultiSelectAllowed = true;
                return;
        }
        mapPanel.isMultiSelectAllowed = false;
    }
}
