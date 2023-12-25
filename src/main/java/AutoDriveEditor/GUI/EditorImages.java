package AutoDriveEditor.GUI;

import javax.swing.*;
import java.awt.image.BufferedImage;

import static AutoDriveEditor.Utils.ImageUtils.loadImage;
import static AutoDriveEditor.Utils.ImageUtils.loadImageIcon;


public class EditorImages {

    private static BufferedImage tractorImage;
    private static BufferedImage overlapWarningImage;
    private static BufferedImage negativeHeightWarningImage;
    private static BufferedImage parkingImage;
    private static BufferedImage markerImage;
    private static ImageIcon updateIcon;
    private static ImageIcon markerIcon;
    private static ImageIcon gameIcon;
    private static ImageIcon routeIcon;




    public static void loadIcons() {

        updateIcon = loadImageIcon("editor/update.png");
        markerIcon = loadImageIcon("editor/marker.png");
        gameIcon = loadImageIcon("editor/gameIcon.png");
        routeIcon = loadImageIcon("editor/routeIcon.png");

        tractorImage = loadImage("editor/tractor.png");
        markerImage = loadImage("editor/nodes/node_marker_small2.png");
        parkingImage = loadImage("editor/nodes/node_parking_small.png");
        overlapWarningImage = loadImage("editor/nodes/node_warning.png");
        negativeHeightWarningImage = loadImage("editor/nodes/node_warning_y.png");
    }

    public static ImageIcon getUpdateIcon() { return updateIcon; }
    public static ImageIcon getMarkerIcon() { return markerIcon; }
    public static ImageIcon getGameIcon() { return gameIcon; }
    public static ImageIcon getRouteIcon() { return routeIcon; }

    public static BufferedImage getMarkerImage() { return markerImage; }
    public static BufferedImage getTractorImage() { return tractorImage; }
    public static BufferedImage getOverlapWarningImage() { return overlapWarningImage; }
    public static BufferedImage getNegativeHeightWarningImage() { return negativeHeightWarningImage; }
    public static BufferedImage getParkingImage() { return parkingImage; }
}
