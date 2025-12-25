package AutoDriveEditor.Classes.Widgets;

import AutoDriveEditor.Classes.Interfaces.SelectorInterface;
import AutoDriveEditor.Classes.Interfaces.WidgetInterface;
import AutoDriveEditor.Classes.Widgets.Selectors.SelectorBase;
import AutoDriveEditor.RoadNetwork.MapNode;

import java.awt.*;
import java.awt.geom.Point2D;
import java.util.ArrayList;

import static AutoDriveEditor.Classes.Util_Classes.LoggerUtils.LOG;
import static AutoDriveEditor.GUI.MapPanel.screenPosToWorldPos;
import static AutoDriveEditor.GUI.MapPanel.worldPosToScreenPos;
import static AutoDriveEditor.GUI.Menus.DebugMenu.Logging.LogMoveWidgetMenu.bDebugLogMoveWidget;
import static AutoDriveEditor.GUI.Menus.DebugMenu.Logging.LogWidgetManagerInfoMenu.bDebugLogWidgetManagerInfo;
import static AutoDriveEditor.Managers.MultiSelectManager.multiSelectList;

@SuppressWarnings("unused")
abstract class WidgetBase implements WidgetInterface, Comparable<WidgetBase> {

    protected final String instanceName = this.getClass().getSimpleName();

    SelectorInterface currentSelector;
    ArrayList<SelectorInterface> selectorList = new ArrayList<>();


    // WHat node is the widgets location based on
    protected MapNode currentAnchor;
    protected MapNode lastSelectedAnchor;

    // Widget position
    protected Point2D widgetPosWorld = new Point2D.Double();
    protected Point widgetPosScreen = new Point();


    protected boolean enabled = false;

    // Widget position
    protected boolean isDraggingWidget = false;
    protected double moveDiffX, moveDiffY;
    protected double widgetPosX, widgetPosY;


    public WidgetBase() {
        initSelectors();
    }

    public void setCurrentSelector(SelectorInterface selector) {
        if (bDebugLogWidgetManagerInfo) LOG.info("## {}.setCurrentSelector() ## Setting current selector to {}", this.instanceName, selector);
        for (SelectorInterface listSelector : selectorList) {
            if (listSelector.getClass() == selector.getClass()) {
                currentSelector = listSelector;
                currentSelector.setOwnerWidget(this);
                currentSelector.setSelectorEnabled(true);
                if (bDebugLogWidgetManagerInfo) LOG.info("## {}.setCurrentSelector() ## Enabling {} for '{}'", this.instanceName, currentSelector, this);

            } else {
                if (bDebugLogWidgetManagerInfo) LOG.info("## {}.setCurrentSelector() ## Disabling {}", this.instanceName, listSelector);
                listSelector.setSelectorEnabled(false);
            }
        }
    }


    public SelectorInterface getCurrentSelector() { return this.currentSelector; }

    public void moveWidgetWorldPosBy(double x, double y) {
        double newX = this.widgetPosWorld.getX() + x;
        double newY = this.widgetPosWorld.getY() + y;
        this.widgetPosWorld.setLocation(newX, newY);
        this.widgetPosScreen = worldPosToScreenPos(newX, newY);
        updateWidget();
    }

    public void moveWidgetScreenPosBy(int diffX, int diffY) {
        int newX, newY;
        newX = this.widgetPosScreen.x + diffX;
        newY = this.widgetPosScreen.y + diffY;
        this.widgetPosScreen.setLocation(newX, newY);
        this.widgetPosWorld = screenPosToWorldPos(newX, newY);
        updateWidget();
    }

    //
    // Getters
    //

    public boolean isEnabled() { return this.enabled; }
    public Point2D getWidgetPosWorld() { return this.widgetPosWorld; }
    public Point getWidgetPosScreen() { return worldPosToScreenPos(this.widgetPosWorld); }
    public MapNode getCurrentAnchor() { return this.currentAnchor; }

    //
    // Setters
    //


    /**
     * Sets the current anchor node for this widget.
     * If the node is not null, it will set the widget position and enable if needed.
     * If the node is null, it will clear the current anchor.
     *
     * @param mapNode The MapNode to set as the current anchor.
     */
    public void setCurrentAnchor(MapNode mapNode) {
        if (mapNode != null) {
            if (currentAnchor != null) {
                if (bDebugLogMoveWidget) LOG.info("## WidgetBase.setCurrentAnchor() ## clearing old anchor node {}", currentAnchor);
                if (!multiSelectList.contains(currentAnchor)) currentAnchor.setSelected(false);
            }
            if (bDebugLogMoveWidget) LOG.info("## WidgetBase.setCurrentAnchor() ## Setting new anchor node {}", mapNode);
            currentAnchor = mapNode;
            currentAnchor.setSelected(true);

            if (bDebugLogMoveWidget) LOG.info("## WidgetBase.setCurrentAnchor() ## Anchor node set to {}", currentAnchor);
            setWidgetWorldPosition(currentAnchor.getX(), currentAnchor.getZ());
            if (!enabled) setWidgetEnabled(true);
            if (currentSelector != null) currentSelector.setSelectorEnabled(true);
        } else {
            if (bDebugLogMoveWidget) LOG.info("## WidgetBase.setCurrentAnchor() ## setting currentAnchor to null");
            if (currentAnchor != null) {
                if (!multiSelectList.contains(currentAnchor)) currentAnchor.setSelected(false);
            }
            currentAnchor = null;
            currentSelector.setSelectorEnabled(false);
        }
    }

    public void setWidgetEnabled(boolean enabled) {
        this.enabled = enabled;
        this.currentSelector.setSelectorEnabled(enabled);
        if (currentAnchor != null) {
            if (!multiSelectList.contains(currentAnchor)) currentAnchor.setSelected(false);
            lastSelectedAnchor = currentAnchor;
        }
        updateWidget();
        if (bDebugLogMoveWidget) LOG.info("## {}.setWidgetEnabled() ## {} : widget {}", instanceName, this, (this.enabled) ? "enable" : "disable");
    }

    public void setWidgetWorldPosition(double x, double y) {
        this.widgetPosWorld.setLocation(x, y);
        this.widgetPosScreen.setLocation(worldPosToScreenPos(x, y));
        currentSelector.updateSelector();
    }

    public void updateWidget() {
        currentSelector.updateSelector();
    }

    //
    // Multiselect Interface
    //


    @Override
    public void onMultiSelectStart() {
        if (bDebugLogMoveWidget) LOG.info("## {}.onMultiSelectStart() ## Start", this.instanceName);
    }

    @Override
    public void onMultiSelectStop() {
        if (bDebugLogMoveWidget) LOG.info("## {}.onMultiSelectStop() ## Stop", this.instanceName);
    }

    @Override
    public void onMultiSelectChange(ArrayList<MapNode> nodeList) {
        if (bDebugLogMoveWidget) LOG.info("## {}.onMultiSelectChange() ## Change {} Nodes ( Total {} )", this.instanceName, nodeList.size(), multiSelectList.size());
    }

    @Override
    public void onMultiSelectAdd(ArrayList<MapNode> addedNodes) {
        if (bDebugLogMoveWidget) LOG.info("## {}.onMultiSelectAdd ## Added {} Nodes", this.instanceName, addedNodes.size());
    }

    @Override
    public void onMultiSelectRemove(ArrayList<MapNode> removedNodes) {
        if (bDebugLogMoveWidget) LOG.info("## {}.onMultiSelectRemove() ## Removed {} Nodes", this.instanceName, removedNodes.size());
    }

    @Override
    public void onMultiSelectOneTime(ArrayList<MapNode> oneTimeList) {
        if (bDebugLogMoveWidget) LOG.info("## {}.onMultiSelectOneTime() ## Removed {} Nodes", this.instanceName, oneTimeList.size());
    }

    @Override
    public void onMultiSelectCleared() {
        if (bDebugLogMoveWidget) LOG.info("## {}.onMultiSelectCleared() ## Cleared all selections", this.instanceName);
    }


    @Override
    public void drawToScreen(Graphics g) {}

    //
    // Misc Methods
    //


    @Override
    public String toString() {
        return String.format("%s@%s", this.getClass().getSimpleName(), Integer.toHexString(hashCode()));
    }

    @SuppressWarnings("ComparatorMethodParameterNotUsed")
    @Override
    public int compareTo(WidgetBase other) { return (this == other) ?  0 : 1; }

}
