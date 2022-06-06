package AutoDriveEditor.GUI;

import javax.swing.*;
import java.awt.image.BufferedImage;

import static AutoDriveEditor.Utils.ImageUtils.getImageIcon;
import static AutoDriveEditor.Utils.ImageUtils.loadImage;


public class GUIImages {

    public static BufferedImage tractorImage;
    public static BufferedImage nodeImage;
    public static BufferedImage nodeImageSelected;
    public static BufferedImage subPrioNodeImage;
    public static BufferedImage subPrioNodeImageSelected;
    public static BufferedImage controlPointImage;
    public static BufferedImage controlPointImageSelected;
    public static BufferedImage curveNodeImage;
    public static BufferedImage rotateRing;
    public static BufferedImage overlapWarningImage;
    public static BufferedImage negativeHeightWarningImage;

    public static ImageIcon markerIcon;
    public static ImageIcon regularConnectionIcon;
    public static ImageIcon regularConnectionSelectedIcon;
    public static ImageIcon regularConnectionSubPrioIcon;
    public static ImageIcon regularConnectionSubPrioSelectedIcon;
    public static ImageIcon dualConnectionIcon;
    public static ImageIcon dualConnectionSelectedIcon;
    public static ImageIcon dualConnectionSubPrioIcon;
    public static ImageIcon dualConnectionSubPrioSelectedIcon;
    public static ImageIcon reverseConnectionIcon;
    public static ImageIcon reverseConnectionSelectedIcon;
    public static ImageIcon reverseConnectionSubPrioIcon;
    public static ImageIcon reverseConnectionSubPrioSelectedIcon;
    public static ImageIcon conConnectIcon;
    public static ImageIcon conConnectSelectedIcon;

    public static void loadIcons() {
        //AD Tractor icon for main window
        tractorImage = loadImage("editor/tractor.png");
        // Marker Icon for Destination dialogs
        markerIcon = getImageIcon("editor/marker.png");
        // node images for MapPanel
        nodeImage = loadImage("editor/nodes/node.png");
        overlapWarningImage = loadImage("editor/nodes/node_warning.png");
        negativeHeightWarningImage = loadImage("editor/nodes/node_warning_y.png");
        nodeImageSelected = loadImage("editor/nodes/node_selected.png");
        subPrioNodeImage = loadImage("editor/nodes/subprionode.png");
        subPrioNodeImageSelected = loadImage("editor/nodes/subprionode_selected.png");
        controlPointImage = loadImage("editor/curves/controlpoint.png");
        controlPointImageSelected = loadImage("editor/curves/controlpoint_selected.png");
        curveNodeImage = loadImage("editor/curves/curvenode.png");
        rotateRing = loadImage("editor/rotate_ring.png");

        // icons for dual state buttons

        regularConnectionIcon = getImageIcon("editor/buttons/connectregular.png");
        regularConnectionSelectedIcon = getImageIcon("editor/buttons/connectregular_selected.png");
        regularConnectionSubPrioIcon = getImageIcon("editor/buttons/connectregular_subprio.png");
        regularConnectionSubPrioSelectedIcon = getImageIcon("editor/buttons/connectregular_subprio_selected.png");

        dualConnectionIcon = getImageIcon("editor/buttons/connectdual.png");
        dualConnectionSelectedIcon = getImageIcon("editor/buttons/connectdual_selected.png");
        dualConnectionSubPrioIcon = getImageIcon("editor/buttons/connectdual_subprio.png");
        dualConnectionSubPrioSelectedIcon = getImageIcon("editor/buttons/connectdual_subprio_selected.png");

        reverseConnectionIcon = getImageIcon("editor/buttons/connectreverse.png");
        reverseConnectionSelectedIcon = getImageIcon("editor/buttons/connectreverse_selected.png");
        reverseConnectionSubPrioIcon = getImageIcon("editor/buttons/connectreverse_subprio.png");
        reverseConnectionSubPrioSelectedIcon = getImageIcon("editor/buttons/connectreverse_subprio_selected.png");

        conConnectIcon = getImageIcon("editor/buttons/conconnect.png");
        conConnectSelectedIcon = getImageIcon("editor/buttons/conconnect_selected.png");
    }

    public static ImageIcon getMarkerIcon() {
        return markerIcon;
    }
}
