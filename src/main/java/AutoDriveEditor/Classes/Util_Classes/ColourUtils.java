package AutoDriveEditor.Classes.Util_Classes;

import java.awt.*;

@SuppressWarnings("unused")
public class ColourUtils {

    public static Color HexToColor(String hexColour) {
        int r = Integer.valueOf(hexColour.substring(1, 3), 16);
        int g = Integer.valueOf(hexColour.substring(3, 5), 16);
        int b = Integer.valueOf(hexColour.substring(5, 7), 16);
        return new Color(r,g,b);
    }

    public static Color HexToColorWithAlpha(String hexColour) {
        int r = Integer.valueOf(hexColour.substring(1, 3), 16);
        int g = Integer.valueOf(hexColour.substring(3, 5), 16);
        int b = Integer.valueOf(hexColour.substring(5, 7), 16);
        int a = hexColour.length() > 7 ? Integer.valueOf(hexColour.substring(7, 9), 16) : 255;
        return new Color(r, g, b, a);
    }

    public static String ColorToHex(Color color, boolean toUpperCase) {
        String hex = String.format("#%02x%02x%02x", color.getRed(), color.getGreen(), color.getBlue());
        if (toUpperCase) hex = hex.toUpperCase();
        return hex;
    }

    public static String ColorToHexWithAlpha(Color color, boolean toUpperCase) {
        String hex = String.format("#%02x%02x%02x%02x", color.getRed(), color.getGreen(), color.getBlue(), color.getAlpha());
        if (toUpperCase) hex = hex.toUpperCase();
        return hex;
    }

    public static Color lighten(Color color, int percentage) {
        int r = (int) (color.getRed() + (255 - color.getRed()) * percentage / 100.0);
        int g = (int) (color.getGreen() + (255 - color.getGreen()) * percentage / 100.0);
        int b = (int) (color.getBlue() + (255 - color.getBlue()) * percentage / 100.0);

        r = Math.max(0, Math.min(255, r));
        g = Math.max(0, Math.min(255, g));
        b = Math.max(0, Math.min(255, b));

        return new Color(r, g, b);
    }

    public static Color darken(Color color, int percentage) {
        int r = color.getRed() - (color.getRed() * percentage / 100);
        int g = color.getGreen() - (color.getGreen() * percentage / 100);
        int b = color.getBlue() - (color.getBlue() * percentage / 100);

        r = Math.max(0, Math.min(255, r));
        g = Math.max(0, Math.min(255, g));
        b = Math.max(0, Math.min(255, b));

        return new Color(r, g, b);
    }


}
