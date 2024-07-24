package AutoDriveEditor.GUI.Menus.MapImagesMenu;

import AutoDriveEditor.GUI.Menus.JMenuItemBase;

import javax.imageio.ImageIO;
import javax.swing.*;
import javax.swing.filechooser.FileFilter;
import java.awt.*;
import java.awt.event.ActionEvent;
import java.awt.event.InputEvent;
import java.awt.event.KeyEvent;
import java.awt.image.BufferedImage;
import java.io.File;
import java.io.IOException;

import static AutoDriveEditor.AutoDriveEditor.editor;
import static AutoDriveEditor.Classes.MapImage.setMapPanelImage;
import static AutoDriveEditor.GUI.MapPanel.forceMapImageRedraw;
import static AutoDriveEditor.GUI.TextPanel.setImageLoadedLabel;
import static AutoDriveEditor.Locale.LocaleManager.getLocaleString;
import static AutoDriveEditor.Utils.LoggerUtils.LOG;
import static AutoDriveEditor.XMLConfig.GameXML.lastUsedLocation;

public class LoadMapImageMenu extends JMenuItemBase {

    public static JMenuItem menu_LoadMapImage;
    public LoadMapImageMenu() {
        menu_LoadMapImage = makeMenuItem("menu_map_loadimage",  "menu_map_loadimage_accstring", KeyEvent.VK_M, InputEvent.ALT_DOWN_MASK, false);
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
            LOG.info("Manual loading of map image setSelected");
            File fileName;
            try {
                fileName = fc.getSelectedFile();
                BufferedImage mapImage = ImageIO.read(fileName);
                if (mapImage != null) {
                    setMapPanelImage(mapImage, false);
                    forceMapImageRedraw();
                    setImageLoadedLabel("Manual Load", new Color(150,100,20));
                }
            } catch (IOException e1) {
                LOG.error(e1.getMessage(), e1);
            }
        }
    }
}
