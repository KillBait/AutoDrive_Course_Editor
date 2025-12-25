package AutoDriveEditor.GUI.Buttons.Toolbar.Experimental;

import AutoDriveEditor.Classes.KeyBinds.Shortcut;
import AutoDriveEditor.Classes.UI_Components.ScaleAnimIcon;
import AutoDriveEditor.GUI.Buttons.BaseButton;
import AutoDriveEditor.Managers.ShortcutManager;
import AutoDriveEditor.RoadNetwork.MapNode;

import javax.swing.*;
import java.awt.*;
import java.awt.event.ActionEvent;

import static AutoDriveEditor.AutoDriveEditor.getMapPanel;
import static AutoDriveEditor.Classes.Util_Classes.LoggerUtils.LOG;
import static AutoDriveEditor.GUI.MapImage.pdaImage;
import static AutoDriveEditor.GUI.Menus.EditMenu.CopyMenu.menu_Copy;
import static AutoDriveEditor.Locale.LocaleManager.getLocaleString;
import static AutoDriveEditor.Managers.IconManager.*;
import static AutoDriveEditor.Managers.ShortcutManager.ShortcutID.EDIT_MARKER_SHORTCUT;
import static AutoDriveEditor.RoadNetwork.RoadMap.networkNodesList;

public class TestButton extends BaseButton {

    @Override
    public String getButtonID() { return "TestButton"; }

    @Override
    public String getButtonAction() { return "ActionButton"; }

    @Override
    public String getButtonPanel() { return "Test"; }

    @Override
    public String getInfoText() { return "Test Button"; }

    @Override
    public Boolean ignoreButtonDeselect() { return true; }

    @Override
    public void setSelected(boolean selected) {}


    @Override
    public void actionPerformed(ActionEvent e) {
        super.actionPerformed(e);

        if (networkNodesList.size() > 2) {

            MapNode n1 = new MapNode(0, 1024, 0 , 1024, MapNode.NODE_FLAG_REGULAR, false);
            MapNode n2 = new MapNode(0, -1024, 0 , -1024, MapNode.NODE_FLAG_REGULAR, false);

            int[] size = {2,4,8,16,32,64,128,256};
            for (int i = 0; i < size.length; i++) {
                LOG.info("Scale : {}", size[i]);
                Point p1 = calcScreenPos(size[i], n1.getX(), n1.getZ());
                LOG.info("P1 : location {} , {}", p1.getX(), p1.getY());
                Point p2 = calcScreenPos(size[i], n2.getX(), n2.getZ());
                LOG.info("P2 : location {} , {}", p2.getX(), p2.getY());
                LOG.info("Length = {}", p1.distance(p2));
            }
        }
    }

    public TestButton(JPanel panel) {
        ScaleAnimIcon animExperimental1Icon = createScaleAnimIcon(TEMPLATE_ICON, false);
        button = createAnimToggleButton(animExperimental1Icon, panel, null, null,  false, false, this);
    }

    private Point calcScreenPos(int mapScale, double worldX, double worldZ) {

        double relativeCentreX = .5;
        double relativeCentreY = .5;
        int zoomLevel = 50;

        int centerPointOffset = 1024 * mapScale;

        worldX += centerPointOffset;
        worldZ += centerPointOffset;

        int scaledX = (int) ((worldX/ mapScale) * zoomLevel);
        int scaledY = (int) ((worldZ/ mapScale) * zoomLevel);

        double centerXScaled = (relativeCentreX * (pdaImage.getWidth()*zoomLevel));
        double centerYScaled = (relativeCentreY * (pdaImage.getHeight()*zoomLevel));

        int topLeftX = (int)centerXScaled - (getMapPanel().getWidth() / 2);
        int topLeftY = (int)centerYScaled - (getMapPanel().getHeight() / 2);

        return new Point(scaledX - topLeftX, scaledY - topLeftY);
    }


    @Override
    public String buildToolTip() {
        Shortcut s = ShortcutManager.getUserShortcutByID(EDIT_MARKER_SHORTCUT);
        if (s != null) {
            return "Test Button" + "\n\nShortcut: " + s.getShortcutString();
        } else {
            return "Test Button";
        }
    }
}
