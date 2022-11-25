package AutoDriveEditor.Utils;

import java.awt.geom.Rectangle2D;
import java.math.BigDecimal;
import java.math.RoundingMode;

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

    public static double roundUpDoubleToDecimalPlaces(double value, int places) {
        if (!Double.isNaN(value)) {
            BigDecimal bd = new BigDecimal(value);
            bd = bd.setScale(places, RoundingMode.HALF_UP);
            return bd.doubleValue();
        } else {
            return 0;
        }
    }

    public static float roundUpFloatToDecimalPlaces(float value, int places) {
        if (!Float.isNaN(value)) {
            BigDecimal bd = new BigDecimal(value);
            bd = bd.setScale(places, RoundingMode.HALF_UP);
            return (float) bd.doubleValue();
        } else {
            return 0;
        }
    }

    public static double limitDoubleToDecimalPlaces(double value, int places, RoundingMode roundingMode) {
        if (!Double.isNaN(value)) {
            BigDecimal bd = new BigDecimal(value);
            bd = bd.setScale(places, roundingMode);
            return bd.doubleValue();
        } else {
            return 0;
        }
    }

    @SuppressWarnings("unused")
    public static float limitFloatToDecimalPlaces(float value, int places, RoundingMode roundingMode) {
        if (!Float.isNaN(value)) {
            BigDecimal bd = new BigDecimal(value);
            bd = bd.setScale(places, roundingMode);
            return (float) bd.doubleValue();
        } else {
            return 0;
        }
    }
}
