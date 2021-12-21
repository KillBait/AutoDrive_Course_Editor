package AutoDriveEditor.Utils;

import java.awt.geom.Rectangle2D;

public class MathUtils {

    public static Rectangle2D getNormalizedRectangleFor(double x, double y, double width, double height) {
        if (width < 0) {
            x = x + width;
            width = -width;
        }
        if (height < 0) {
            y = y + height;
            height = -height;
        }
        return new Rectangle2D.Double(x,y,width,height);
    }

    public static double normalizeAngle(double input) {
        double xPI = (2*Math.PI);
        if (input > xPI) {
            input -= xPI;
        }
        else {
            if (input < -xPI ) {
                input += xPI;
            }
        }

        return input;
    }
}
