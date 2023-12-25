package AutoDriveEditor.GUI.Menus.HeightMapMenu;

import AutoDriveEditor.GUI.Menus.JCheckBoxMenuItemBase;

import javax.swing.*;
import java.awt.event.ItemEvent;

import static AutoDriveEditor.Classes.MapImage.*;
import static AutoDriveEditor.GUI.MapPanel.forceMapImageRedraw;
import static AutoDriveEditor.Utils.LoggerUtils.LOG;

public class ShowHeightmapMenu extends JCheckBoxMenuItemBase {

    public static JCheckBoxMenuItem menu_ShowHeightMap;
    public static boolean bShowHeightMap;
    public ShowHeightmapMenu() {
        menu_ShowHeightMap = makeCheckBoxMenuItem("menu_heightmap_show",  "menu_heightmap_show_accstring", false, true);
    }

    @Override
    public void itemStateChanged(ItemEvent e) {
        super.itemStateChanged(e);
        AbstractButton menuItem = (AbstractButton) e.getItem();
        bShowHeightMap = menuItem.isSelected();
        if (bShowHeightMap) {
            LOG.info("set heightMapImage");
            if (heightMapImage != null ) {
                setImage(heightMapImage, true);
            } else {
                LOG.info("heightMapImage = null");
            }
        } else {
            if (pdaImage != null ) {
                LOG.info("set pdaImage");
                setImage(pdaImage, false);
            } else {
                LOG.info("set default mapImage");
                useDefaultMapImage();
            }
        }
        forceMapImageRedraw();
    }
}
