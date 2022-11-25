package AutoDriveEditor.Utils;

import java.awt.*;

public class ConversionUtils {

    public static Color HexToColor(String hexColour) {
        int r = Integer.valueOf(hexColour.substring(1, 3), 16);
        int g = Integer.valueOf(hexColour.substring(3, 5), 16);
        int b = Integer.valueOf(hexColour.substring(5, 7), 16);
        return new Color(r,g,b);
    }

    public static String ColorToHex(Color color, boolean toUpperCase) {
        String hex = String.format("#%02x%02x%02x", color.getRed(), color.getGreen(), color.getBlue());
        if (toUpperCase) hex = hex.toUpperCase();
        return hex;
    }
}
