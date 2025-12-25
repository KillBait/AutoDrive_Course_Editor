package AutoDriveEditor.Classes.Util_Classes;

import java.awt.geom.Point2D;
import java.awt.geom.Rectangle2D;
import java.math.BigDecimal;
import java.math.RoundingMode;

@SuppressWarnings("unused")
public class MathUtils {

    /**
     * Given two points that describe the start and end locations of a rectangle, will return
     * a rectangle2D with normalized co-ordinates ( i.e. start location will always be the top-Left
     * of the rectangle and the size and width will always be positive numbers.
     *
     * @param startPoint  Point2D containing the start location
     * @param endPoint    Point2D containing the end location
     * @return Rectangle2D containing the normalized X,Y,Width,Height or null if the startPoint
     *         or endPoint is invalid
     */
    public static Rectangle2D getNormalizedRectangle(Point2D startPoint, Point2D endPoint) {
        if (startPoint != null && endPoint != null) {
            double x1 = Math.min(startPoint.getX(), endPoint.getX());
            double y1 = Math.min(startPoint.getY(), endPoint.getY());
            double x2 = Math.max(startPoint.getX(), endPoint.getX());
            double y2 = Math.max(startPoint.getY(), endPoint.getY());

            // Ensure coordinates are in the correct order for the top-left and bottom-right corners
            if (x1 > x2) {
                double temp = x1;
                x1 = x2;
                x2 = temp;
            }
            if (y1 > y2) {
                double temp = y1;
                y1 = y2;
                y2 = temp;
            }

            // Ensure the width and height are non-negative
            double width = Math.max(0, x2 - x1);
            double height = Math.max(0, y2 - y1);

            return new Rectangle2D.Double(x1, y1, width, height);
        }
        return null;
    }


    /**
     * takes an input representing a (double) angle in radians and normalizes it
     * to be within the range [0, 2π) or [0, 360 degrees)
     *
     * @param input in radians
     * @return normalized angle as a (double)
     */
    public static double normalizeAngle(double input) {
        double xPI = 2 * Math.PI;
        return (input % xPI + xPI) % xPI;
    }

    /**
     * takes an input in (double) format and outputs a rounded value to the specified
     * number of decimal places using RoundingMode.HALF_UP.
     * @param value (double) value
     * @param places the number of decimal places the output will have
     * @return (double) with maximum number of decimal places
     */
    public static double roundUpDoubleToDecimalPlaces(double value, int places) {
        if (!Double.isNaN(value)) {
            BigDecimal bd = new BigDecimal(value);
            bd = bd.setScale(places, RoundingMode.HALF_UP);
            return bd.doubleValue();
        } else {
            return 0;
        }
    }

    /**
     * takes an input in (float) format and outputs a rounded value to the specified
     * number of decimal places using RoundingMode.HALF_UP.
     *
     * @param value (float) value
     * @param places the number of decimal places the output will have
     * @return (float) with maximum number of decimal places
     */
    public static float roundUpFloatToDecimalPlaces(float value, int places) {
        if (!Float.isNaN(value)) {
            BigDecimal bd = new BigDecimal(value);
            bd = bd.setScale(places, RoundingMode.HALF_UP);
            return (float) bd.doubleValue();
        } else {
            return 0;
        }
    }

    /**
     * takes an input in (double) format and outputs a rounded value to the specified
     * number of decimal places, using the specified RoundingMode.
     * @param value (double) value
     * @param places the number of decimal places the output will have
     * @param roundingMode the RoundingMode to use
     * @return (double) with maximum number of decimal places
     */
    public static double limitDoubleToDecimalPlaces(double value, int places, RoundingMode roundingMode) {
        if (!Double.isNaN(value)) {
            BigDecimal bd = new BigDecimal(value);
            bd = bd.setScale(places, roundingMode);
            return bd.doubleValue();
        } else {
            return 0;
        }
    }

    /**
     * takes an input in (float) format and outputs a rounded value to the specified
     * number of decimal places, using the specified RoundingMode.
     * @param value (float) value
     * @param places the number of decimal places the output will have
     * @param roundingMode the RoundingMode to use
     * @return (float) with maximum number of decimal places
     */
    public static float limitFloatToDecimalPlaces(float value, int places, RoundingMode roundingMode) {
        if (!Float.isNaN(value)) {
            BigDecimal bd = new BigDecimal(value);
            bd = bd.setScale(places, roundingMode);
            return (float) bd.doubleValue();
        } else {
            return 0;
        }
    }

    /**
     * takes an input in (float) format and outputs a string with the rounded value
     * to the specified number of decimal places.
     * @param value (float) value
     * @param places the number of decimal places the output will have
     * @return (String) original value with specified number of decimal places
     */
    public static String limitFloatToDecimalPlaces(float value, int places) {
        if (places < 0) throw new IllegalArgumentException("Decimal places must be non-negative");

        // Convert the specified value to string format
        String roundedValue = String.valueOf(value);

        // Trim the string to ensure it has at most two decimal places
        int decimalIndex = roundedValue.indexOf('.');
        if (decimalIndex != -1 && roundedValue.length() - decimalIndex - 1 > 2) {
            roundedValue = roundedValue.substring(0, decimalIndex + 3);
        }

        return roundedValue;
    }

    /**
     * Calculates the point on a circles edge, given a center, radius, and angle in degrees.
     * @param center The center point of the circle.
     * @param radius The radius of the circle.
     * @param angleInDegrees The angle in degrees from the center point.
     * @return A Point2D representing the point on the circle edge.
     */
    public static Point2D getPointOnCircleEdge(Point2D center, double radius, double angleInDegrees) {
        double angleInRadians = Math.toRadians(angleInDegrees);
        double x = center.getX() + (radius * Math.cos(angleInRadians));
        double y = center.getY() + (radius * Math.sin(angleInRadians));
        return new Point2D.Double(x, y);
    }

    public static double linearInterpolate(double n0, double n1, double a) {
        return (1.0 - a) * n0 + (a * n1);
    }
}
