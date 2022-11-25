package AutoDriveEditor.GUI;

import javax.swing.*;
import java.awt.image.BufferedImage;

import static AutoDriveEditor.Utils.ImageUtils.getImageIcon;
import static AutoDriveEditor.Utils.ImageUtils.loadImage;


public class GUIImages {

    public static BufferedImage tractorImage;
    public static BufferedImage rotateRing;
    public static BufferedImage overlapWarningImage;
    public static BufferedImage negativeHeightWarningImage;

    public static ImageIcon markerIcon;
    public static ImageIcon updateIcon;

    public static void loadIcons() {
        tractorImage = loadImage("editor/tractor.png");
        markerIcon = getImageIcon("editor/marker.png");
        updateIcon = getImageIcon("editor/update.png");
        overlapWarningImage = loadImage("editor/nodes/node_warning.png");
        negativeHeightWarningImage = loadImage("editor/nodes/node_warning_y.png");
        rotateRing = loadImage("editor/rotate_ring.png");
    }

    public static ImageIcon getMarkerIcon() {
        return markerIcon;
    }
    public static ImageIcon getUpdateIcon() {return updateIcon;}
    public static BufferedImage getTractorImage() {
        return tractorImage;
    }
}
