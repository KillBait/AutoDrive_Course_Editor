package AutoDriveEditor.GUI.Menus.ImportMenu;

import AutoDriveEditor.GUI.Menus.JMenuItemBase;

import javax.imageio.ImageIO;
import javax.swing.*;
import javax.swing.filechooser.FileFilter;
import java.awt.event.ActionEvent;
import java.awt.event.InputEvent;
import java.awt.event.KeyEvent;
import java.awt.image.BufferedImage;
import java.io.File;
import java.io.IOException;

import static AutoDriveEditor.AutoDriveEditor.editor;
import static AutoDriveEditor.Classes.UI_Components.MapInfoLabel.LabelStatus.MANUAL_LOAD;
import static AutoDriveEditor.Classes.Util_Classes.LoggerUtils.LOG;
import static AutoDriveEditor.GUI.Buttons.ActionBar.ConfigPanel.MapInfoButton.setMapImageLabel;
import static AutoDriveEditor.GUI.MapImage.setMapPanelImage;
import static AutoDriveEditor.GUI.MapPanel.forceMapImageRedraw;
import static AutoDriveEditor.Locale.LocaleManager.getLocaleString;
import static AutoDriveEditor.XMLConfig.GameXML.lastUsedLocation;

public class LoadMapImageMenu extends JMenuItemBase {

    public static JMenuItem menu_LoadMapImage;
    public LoadMapImageMenu() {
        menu_LoadMapImage = makeMenuItem("menu_import_sub_map_loadimage", KeyEvent.VK_M, InputEvent.ALT_DOWN_MASK, false);
    }

    @Override
    public void actionPerformed(ActionEvent e) {
        super.actionPerformed(e);
        JFileChooser fc = new JFileChooser(lastUsedLocation);
        fc.setDialogTitle(getLocaleString("dialog_load_image_title"));
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
                return "AutoDriveEditor MapImage (.png)";
            }
        });

        if (fc.showOpenDialog(editor) == JFileChooser.APPROVE_OPTION) {
            LOG.info("Manual loading of map image selected");
            File fileName;
            try {
                fileName = fc.getSelectedFile();
                BufferedImage mapImage = ImageIO.read(fileName);
                if (mapImage != null) {
                    if (setMapPanelImage(mapImage, true)) {
                        forceMapImageRedraw();
                        setMapImageLabel(MANUAL_LOAD);
                    }
                } else {
                    LOG.info("Failed to load map image {}", fileName);
                }
            } catch (IOException e1) {
                LOG.error(e1.getMessage(), e1);
            }
        }
    }
}
