package AutoDriveEditor.Managers;

import AutoDriveEditor.RoadNetwork.MapNode;
import AutoDriveEditor.RoadNetwork.RoadMap;

import java.awt.geom.Point2D;
import java.awt.geom.Rectangle2D;
import java.util.LinkedList;

import static AutoDriveEditor.AutoDriveEditor.DEBUG;
import static AutoDriveEditor.GUI.Buttons.Curves.CubicCurveButton.cubicCurve;
import static AutoDriveEditor.GUI.Buttons.Curves.CubicCurveButton.isCubicCurveCreated;
import static AutoDriveEditor.GUI.Buttons.Curves.QuadCurveButton.isQuadCurveCreated;
import static AutoDriveEditor.GUI.Buttons.Curves.QuadCurveButton.quadCurve;
import static AutoDriveEditor.GUI.MenuBuilder.updateEditMenu;
import static AutoDriveEditor.MapPanel.MapImage.mapPanelImage;
import static AutoDriveEditor.MapPanel.MapPanel.*;
import static AutoDriveEditor.Utils.LoggerUtils.LOG;
import static AutoDriveEditor.Utils.MathUtils.getNormalizedRectangleFor;
import static AutoDriveEditor.Utils.MathUtils.roundUpDoubleToDecimalPlaces;

public class MultiSelectManager {

    public static LinkedList<MapNode> multiSelectList = new LinkedList<>();
    public static Point2D rectangleStart;
    public static Point2D rectangleEnd;
    public static boolean isMultipleSelected = false;
    public static boolean isMultiSelectDragging;

    public static void startMultiSelect(int mousePosX, int mousePosY) {
        if ( mapPanelImage != null ) {
            rectangleStart = screenPosToWorldPos(mousePosX, mousePosY);
            if (DEBUG) LOG.info("Multi select started at world position x = {}, z = {}", rectangleStart.getX(), rectangleStart.getY());
            isMultiSelectDragging = true;
        }
    }

    public static void stopMultiSelect(int mousePosX, int mousePosY) {
        if (mapPanelImage != null && rectangleStart != null ) {
            rectangleEnd = screenPosToWorldPos(mousePosX, mousePosY);
            if (DEBUG) LOG.info("Multi select stopped at world position x = {}, z = {}", rectangleEnd.getX(), rectangleEnd.getY());
            if (rectangleStart.getX() != rectangleEnd.getX() && rectangleStart.getY() != rectangleEnd.getY()) {
                if (DEBUG) LOG.info("Selection size {},{}", roundUpDoubleToDecimalPlaces(rectangleEnd.getX() - rectangleStart.getX(), 3), roundUpDoubleToDecimalPlaces(rectangleEnd.getY() - rectangleStart.getY(), 3));
            }
        }
        isMultiSelectDragging = false;
        getMapPanel().repaint();
    }

    public static void clearMultiSelection() {
        if (multiSelectList != null && multiSelectList.size() > 0 ) {
            for (MapNode node : multiSelectList) {
                node.isSelected = false;
            }
            multiSelectList.clear();
        }
        isMultipleSelected = false;
        rectangleStart = null;
        rectangleEnd = null;
        LOG.info("Clearing all Selected Nodes");
        updateEditMenu();
        getMapPanel().repaint();
    }

    @SuppressWarnings("unused")
    public boolean isMultiSelectDragging() { return isMultiSelectDragging; }

    public static int getAllNodesInSelectedArea(Point2D rectangleStart, Point2D rectangleEnd) {
        if ((roadMap == null) || (mapPanelImage == null)) {
            return 0;
        }

        int count = 0;

        Rectangle2D rectangle = getNormalizedRectangleFor(rectangleStart.getX(), rectangleStart.getY(), rectangleEnd.getX() - rectangleStart.getX(), rectangleEnd.getY() - rectangleStart.getY());
        for (MapNode mapNode : RoadMap.networkNodesList) {
            if (mapNode.x > rectangle.getX() && mapNode.x < rectangle.getX() + rectangle.getWidth() && mapNode.z > rectangle.getY() && mapNode.z < rectangle.getY() + rectangle.getHeight()) {
                if (multiSelectList.contains(mapNode)) {
                    multiSelectList.remove(mapNode);
                    mapNode.setSelected(false);
                } else {
                    multiSelectList.add(mapNode);
                    mapNode.setSelected(true);
                }
                count++;
            }
        }

        if (isQuadCurveCreated) {
            MapNode controlPoint = quadCurve.getControlPoint();
            if (controlPoint.x > rectangle.getX() && controlPoint.x < rectangle.getX() + rectangle.getWidth() && controlPoint.z > rectangle.getY() && controlPoint.z < rectangle.getY() + rectangle.getHeight()) {
                if (multiSelectList.contains(controlPoint)) {
                    multiSelectList.remove(controlPoint);
                    controlPoint.isSelected = false;
                } else {
                    multiSelectList.add(controlPoint);
                    controlPoint.isSelected = true;
                }
            }
        }

        if (isCubicCurveCreated) {
            MapNode controlPoint1 = cubicCurve.getControlPoint1();
            MapNode controlPoint2 = cubicCurve.getControlPoint2();

            if (controlPoint1.x > rectangle.getX() && controlPoint1.x < rectangle.getX() + rectangle.getWidth() && controlPoint1.z > rectangle.getY() && controlPoint1.z < rectangle.getY() + rectangle.getHeight()) {
                if (multiSelectList.contains(controlPoint1)) {
                    multiSelectList.remove(controlPoint1);
                    controlPoint1.isSelected = false;
                } else {
                    multiSelectList.add(controlPoint1);
                    controlPoint1.isSelected = true;
                }
            }

            if (controlPoint2.x > rectangle.getX() && controlPoint2.x < rectangle.getX() + rectangle.getWidth() && controlPoint2.z > rectangle.getY() && controlPoint2.z < rectangle.getY() + rectangle.getHeight()) {
                if (multiSelectList.contains(controlPoint2)) {
                    multiSelectList.remove(controlPoint2);
                    controlPoint2.isSelected = false;
                } else {
                    multiSelectList.add(controlPoint2);
                    controlPoint2.isSelected = true;
                }
            }
        }

        if (multiSelectList.size() > 0 && count > 0) {
            LOG.info("Selected {} nodes", multiSelectList.size());
            isMultipleSelected = true;
        } else {
            LOG.info("No nodes selected");
            isMultipleSelected = false;
        }
        updateEditMenu();
        return multiSelectList.size();
    }
}
