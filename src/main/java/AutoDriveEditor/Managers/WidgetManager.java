package AutoDriveEditor.Managers;

import AutoDriveEditor.Classes.Interfaces.CurveInterface;
import AutoDriveEditor.Classes.Interfaces.WidgetInterface;
import AutoDriveEditor.RoadNetwork.MapNode;

import java.awt.*;
import java.awt.event.MouseEvent;
import java.awt.event.MouseWheelEvent;
import java.math.RoundingMode;
import java.util.ArrayList;
import java.util.Map;
import java.util.TreeMap;

import static AutoDriveEditor.Classes.Util_Classes.LoggerUtils.LOG;
import static AutoDriveEditor.Classes.Util_Classes.MathUtils.limitDoubleToDecimalPlaces;
import static AutoDriveEditor.GUI.Menus.DebugMenu.Logging.LogWidgetManagerInfoMenu.bDebugLogWidgetManagerInfo;
import static AutoDriveEditor.GUI.Menus.DebugMenu.ShowWidgetManagerInfo.widgetManagerGroup;
import static AutoDriveEditor.Managers.RenderManager.PRIORITY_MAX;

@SuppressWarnings("unused")
public class WidgetManager extends RenderManager.Drawable implements MultiSelectManager.MultiSelectEventListener {

    private final TreeMap<WidgetInterface, String> widgetList;
    private WidgetInterface currentWidget = null;

    /**
     * WidgetManager constructor
     * Initializes the widget list and adds the MultiSelectEventListener
     */
    public WidgetManager() {
        LOG.info("  Initializing WidgetManager");
        this.widgetList = new TreeMap<>();
        MultiSelectManager.addMultiSelectEventListener(this);
        setRenderPriority(PRIORITY_MAX);
    }

    /**
     * Adds a widget class to the widget list, if the widget clas
     * already exists, it returns the reference tp the existing instance.
     * @param widgetClass the widget class to add
     * @param owner String containing the owner of the widget
     * @return the widget instance
     */
    public WidgetInterface addWidgetClass(Class<? extends WidgetInterface> widgetClass, String owner) {
        try {
            // Check if the widget class is already in the list, return the existing instance if it is
            if (bDebugLogWidgetManagerInfo) LOG.info("## WidgetManager.addWidgetClass() ## Check existing widgets for duplicate of '{}'", widgetClass.getSimpleName());
            for (Map.Entry<WidgetInterface, String> entry : widgetList.entrySet()) {
                WidgetInterface listWidget = entry.getKey();
                if (listWidget.getClass().equals(widgetClass)) {
                    if (bDebugLogWidgetManagerInfo) LOG.info("## WidgetManager.addWidgetClass() ## Widget '{}' already created, returning existing reference '{}'", widgetClass.getSimpleName(), currentWidget);
                    currentWidget = listWidget;
                    return currentWidget;
                }
            }
            // Create a new instance of the widget class
            WidgetInterface widget = widgetClass.getDeclaredConstructor().newInstance();
            // add the widget to the list
            widgetList.putIfAbsent(widget, owner);
            currentWidget = widget;
            if (bDebugLogWidgetManagerInfo) LOG.info("## WidgetManager.addWidgetClass() ## Created new instance of widget: {} ( {} )", widgetClass.getSimpleName(), widget);
            return widget;
        } catch (Exception e) {
            LOG.error("Error adding widget class: {} for {}", e.getMessage(), owner);
            e.printStackTrace();
            return null;
        }
    }

    /**
     * removes a widget class from the widget list
     * @param widget the widget class to remove
     */
    public void removeWidgetClass(WidgetInterface widget) {
        for (Map.Entry<WidgetInterface, String> entry : widgetList.entrySet()) {
            WidgetInterface w = entry.getKey();
            if (w.equals(widget)) {
                if (bDebugLogWidgetManagerInfo) LOG.info("## WidgetManager.removeWidgetClass() ## Removing widget class: {} ( {} )", widget.getClass().getSimpleName(), widget);
                widgetList.remove(w);
                break;
            }
        }
    }

    /**
     * clears all widgets from the widget list
     */
    public void clearWidgets() {
        if (bDebugLogWidgetManagerInfo) LOG.info("## WidgetManager.clearWidgets() ## Clearing all widgets");
        widgetList.clear();
    }

    /**
     * Get the Owner name of the widget
     * @param widget the widget to get the owner of
     * @return a String containing the owner of the widget
     */
    public String getOwner(WidgetInterface widget) {
        for (Map.Entry<WidgetInterface, String> entry : widgetList.entrySet()) {
            if (widget == entry.getKey()) {
                return entry.getValue();
            }
        }
        return "Unknown";
    }

    /**
     * Get the currently active widget
     * @return the currently active widget, or null if no widget is active
     */
    public WidgetInterface getCurrentWidget() {
        return currentWidget;
    }

    //
    // Mouse events to pass onto the widgets
    //

    public void mouseClicked(MouseEvent e) {
        widgetList.forEach((widget, owner) -> {
            if (widget.isEnabled()) widget.mouseClicked(e);
        });
    }

    public void mousePressed(MouseEvent e) {
        widgetList.forEach((widget, owner) -> {
            if (widget.isEnabled()) widget.mousePressed(e);
        });
    }

    public void mouseReleased(MouseEvent e) {
        widgetList.forEach((widget, owner) -> {
            if (widget.isEnabled()) widget.mouseReleased(e);
        });
    }

    public void mouseDragged(MouseEvent e) {
        widgetList.forEach((widget, owner) -> {
            if (widget.isEnabled()) widget.mouseDragged(e);
        });
    }

    public void mouseMoved(MouseEvent e) {
        widgetList.forEach((widget, owner) -> {
            if (widget.isEnabled()) widget.mouseMoved(e);
        });
    }

    public void mouseWheelMoved(MouseWheelEvent e) {
        widgetList.forEach((widget, owner) -> {
            if (widget.isEnabled()) widget.mouseWheelMoved(e);
        });
    }

    public void updateAllWidgets() {
        widgetList.forEach((widget, owner) -> {
            widget.updateWidget();
        });
    }


    public void drawToScreen(Graphics g) {
        showDebug();
        widgetList.forEach((widget, owner) -> {
            if (widget.isEnabled()) widget.drawToScreen(g);
        });

    }

    @Override
    public void onMultiSelectStart() {
        widgetList.forEach((widget, owner) -> {
            if (widget.isEnabled()) widget.onMultiSelectStart();
        });
    }

    @Override
    public void onMultiSelectStop() {
        widgetList.forEach((widget, owner) -> {
            if (widget.isEnabled()) widget.onMultiSelectStop();
        });

    }

    @Override
    public void onMultiSelectChange(ArrayList<MapNode> nodeList) {
        widgetList.forEach((widget, owner) -> {
            if (widget.isEnabled()) widget.onMultiSelectChange(nodeList);
        });
    }

    @Override
    public void onMultiSelectAdd(ArrayList<MapNode> addedNodes) {
        widgetList.forEach((widget, owner) -> {
            if (widget.isEnabled()) widget.onMultiSelectAdd(addedNodes);
        });
    }

    @Override
    public void onMultiSelectRemove(ArrayList<MapNode> removedNodes) {
        widgetList.forEach((widget, owner) -> {
            if (widget.isEnabled()) widget.onMultiSelectRemove(removedNodes);
        });
    }

    @Override
    public void onMultiSelectOneTime(ArrayList<MapNode> onTimeList) {
        widgetList.forEach((widget, owner) -> {
            if (widget.isEnabled()) widget.onMultiSelectRemove(onTimeList);
        });
    }

    @Override
    public void onMultiSelectCleared() {
        widgetList.forEach((widget, owner) -> widget.onMultiSelectCleared());
    }

    //
    // Debug Info
    //
    private void showDebug() {
        widgetManagerGroup.reset();
        if (widgetList.isEmpty()) {
            widgetManagerGroup.addCenteredText("No Registered Widgets", false);
        }
        for (WidgetInterface widget : widgetList.keySet()) {
            widgetManagerGroup.addEmptyLine();
            widgetManagerGroup.addLine();
            widgetManagerGroup.addText("Widget", widget.toString());
            widgetManagerGroup.addLine();
            widgetManagerGroup.addText("Owner", getOwner(widget));
            widgetManagerGroup.addText("Widget Pos", "{ " + limitDoubleToDecimalPlaces(widget.getWidgetPosWorld().getX(), 3, RoundingMode.HALF_UP) + " , " + limitDoubleToDecimalPlaces(widget.getWidgetPosWorld().getY(), 3, RoundingMode.HALF_UP) + " }");
            widgetManagerGroup.addText("Anchor", widget.getCurrentAnchor() != null ? widget.getCurrentAnchor().toString() : "null");
            if (widget.getCurrentAnchor() != null) {
                widgetManagerGroup.addText("Anchor Pos", "{ " + widget.getCurrentAnchor().x + " , " + widget.getCurrentAnchor().z + " }");
            }
            widgetManagerGroup.addText("Enabled", widget.isEnabled() ? "true" : "false");
            widgetManagerGroup.addEmptyLine();
            widgetManagerGroup.addCenteredText("Selector Info", true);
            if (widget.getCurrentSelector() != null) {
                widgetManagerGroup.addText("Current", widget.getCurrentSelector().toString());
                if (widget.getCurrentSelector().getWidgetPosWorld() != null) {
                    widgetManagerGroup.addText("Current Pos", "{ " + limitDoubleToDecimalPlaces(widget.getCurrentSelector().getWidgetPosWorld().getX(), 3, RoundingMode.HALF_UP) + " , " + limitDoubleToDecimalPlaces(widget.getCurrentSelector().getWidgetPosWorld().getY(), 3, RoundingMode.HALF_UP) + " }");
                }
                widgetManagerGroup.addText("Current Offset", "{ " + limitDoubleToDecimalPlaces(widget.getCurrentSelector().getSelectOffsetWorld().getX(), 3, RoundingMode.HALF_UP) + " , " + limitDoubleToDecimalPlaces(widget.getCurrentSelector().getSelectOffsetWorld().getY(), 3, RoundingMode.HALF_UP) + "}");
            }
        }
    }
}
