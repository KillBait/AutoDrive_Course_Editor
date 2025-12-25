package AutoDriveEditor.Managers;

import AutoDriveEditor.Classes.Curves.ArcSpline;
import AutoDriveEditor.Classes.Curves.BezierCurve;
import AutoDriveEditor.Classes.Interfaces.CurveInterface;
import AutoDriveEditor.Classes.Interfaces.WidgetInterface;
import AutoDriveEditor.GUI.Curves.CurvePanel;
import AutoDriveEditor.RoadNetwork.Connection;
import AutoDriveEditor.RoadNetwork.MapNode;

import java.awt.*;
import java.awt.event.MouseEvent;
import java.awt.event.MouseWheelEvent;
import java.awt.geom.Point2D;
import java.lang.reflect.Constructor;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashMap;
import java.util.Map;

import static AutoDriveEditor.AutoDriveEditor.*;
import static AutoDriveEditor.Classes.Curves.ArcSpline.CURVE_TYPE_ARCSPLINE;
import static AutoDriveEditor.Classes.Curves.BezierCurve.CURVE_TYPE_BEZIER;
import static AutoDriveEditor.Classes.Util_Classes.LoggerUtils.LOG;
import static AutoDriveEditor.GUI.MapPanel.nodeSizeScaledHalf;
import static AutoDriveEditor.GUI.MapPanel.worldPosToScreenPos;
import static AutoDriveEditor.GUI.Menus.DebugMenu.Logging.LogCurveManagerInfoMenu.bDebugLogCurveManagerInfo;
import static AutoDriveEditor.GUI.Menus.DebugMenu.ShowCurveManagerInfo.curveManagerGroup;
import static AutoDriveEditor.Managers.RenderManager.PRIORITY_HIGH;


/**
 * This class allows for the creation, modification, and rendering
 * of different types of curves.
 * The curves can be registered and added to the active curves list.
 */

@SuppressWarnings({"unused", "UnusedReturnValue"})
public class CurveManager extends RenderManager.Drawable {

    // By design, the curve manager does not use any specific
    // mouse action related code, they are only passed onto
    // the active curves. This allows for more flexibility
    // in the design of the curve classes and their user
    // interactions.

    private final Map<String, Class<? extends CurveInterface>> curveTypes = new HashMap<>();
    private final ArrayList<CurveInfo> curvesList = new ArrayList<>();
    private CurveInfo currentCurve;
    private CurvePanel curvePanel;
    private boolean isDraggingWidget;

    // Initialize the curve manager and register any implemented curve types

    public CurveManager() {
        LOG.info("  Initializing CurveManager");
        setRenderPriority(PRIORITY_HIGH);
        // Initialize the default curve types
        registerCurveType(CURVE_TYPE_BEZIER, BezierCurve.class);
        registerCurveType(CURVE_TYPE_ARCSPLINE, ArcSpline.class);
        LOG.info("    Registered {} curve types", curveTypes.size());

    }

    /**
     * Register a new curve type with a name and class.
     * <br>
     * Example: registerCurveType("Bezier", BezierCurve.class);
     * @param name The name of the curve type.
     * @param curveClass The class of the curve type.
     */

    public void registerCurveType(String name, Class<? extends CurveInterface> curveClass) {
        LOG.info("    Registering curve type: {}", name);
        curveTypes.put(name, curveClass);
    }

    /**
     * Using reflection, add a new curve to the list of currently active curves
     * with parameters. The parameters will be checked against the curve class
     * to ensure there is a matching constructor available.
     * <p>
     * Example: addActiveCurve(button, "Bezier", startNode, endNode);
     * @param curveType String name of the curve type to add.
     * @param params Parameters to pass to the curve constructor.
     * @return curve reference if created successfully, null otherwise.
     * @see #registerCurveType(String, Class)
     */
    public CurveInterface addActiveCurve(String owner, String curveType, Class<? extends WidgetInterface> widgetClass, Object... params) {
        if (!isDraggingWidget) {
            Class<? extends CurveInterface> curveClass = curveTypes.get(curveType);
            if (bDebugLogCurveManagerInfo) {
                ArrayList<Object> paramList = new ArrayList<>(Arrays.asList(params));
                for (int i = 0; i < paramList.size(); i++) {
                    Object o = paramList.get(i);
                    LOG.info("## CurveManager.addActiveCurve() ## param {}: {}", i, o);
                }
            }
            if (curveClass != null) {
                try {
                    // Find the constructor with matching parameter types
                    if (bDebugLogCurveManagerInfo) LOG.info("## CurveManager.addActiveCurve() ## Looking for '{}' class constructor with {} parameters", curveClass.getSimpleName(), params.length);
                    for (Constructor<?> constructor : curveClass.getConstructors()) {
                        if (constructor.getParameterCount() == params.length) {
                            if (bDebugLogCurveManagerInfo) LOG.info("## CurveManager.addActiveCurve() ## Found constructor with {} parameters : {}", constructor.getParameterCount(), constructor.getName());
                            boolean match = true;
                            Class<?>[] paramTypes = constructor.getParameterTypes();
                            if (bDebugLogCurveManagerInfo) LOG.info("## CurveManager.addActiveCurve() ## Checking {} parameter types: {}", paramTypes.length, Arrays.toString(paramTypes));
                            for (int i = 0; i < paramTypes.length; i++) {
                                if (bDebugLogCurveManagerInfo) LOG.info("## CurveManager.addActiveCurve() ## Checking parameter type {}: '{}' against '{}'", i, paramTypes[i].getSimpleName(), params[i].getClass().getSimpleName());
                                if (!paramTypes[i].isInstance(params[i])) {
                                    match = false;
                                    break;
                                }
                            }
                            if (match) {
                                if (bDebugLogCurveManagerInfo) LOG.info("## CurveManager.addActiveCurve() ## Creating reflection based curve: '{}'", curveClass.getSimpleName());
                                CurveInterface curve = (CurveInterface) constructor.newInstance(params);
                                if (bDebugLogCurveManagerInfo) LOG.info("## CurveManager.addActiveCurve() ## Created curve: {} for owner: {}", curveType, owner);

                                createNewCurve(owner, curveType, curve, widgetClass);
                                return curve;
                            }
                        }
                    }
                    LOG.error("## CurveManager.addActiveCurve() ## No matching constructor found for curve type: {}", curveType);
                } catch (Exception e) {
                    LOG.error("## CurveManager.addActiveCurve() ## Failed to create instance of curve type: {}", curveType, e);
                }
            }
        }
        return null;
    }

    /**
     * Create a new curve and add it to the list of currently active curves.
     * @param owner The owner of the curve, usually the button ID, in String format.
     * @param curveType The type of curve to create, must be registered with {@link #registerCurveType(String, Class)}
     * @param curve The curve instance to add.
     * @param widgetClass The widget class to use for the curve.
     */
    private void createNewCurve(String owner, String curveType, CurveInterface curve, Class<? extends WidgetInterface> widgetClass) {
        WidgetInterface curveWidget = widgetManager.addWidgetClass(widgetClass, this.toString());
        if (currentCurve != null) {
            if (currentCurve.getCurveType().equals(curveType)) {
                currentCurve.setActive(false);
                if (bDebugLogCurveManagerInfo) LOG.info("## CurveManager.createNewCurve() ## Setting curve '{}' to inactive", currentCurve.getCurve().toString());
            }
            currentCurve.getWidget().setWidgetEnabled(false);
        }

        CurveInfo newCurve = new CurveInfo(owner, curveType, curve, curveWidget);
        newCurve.setActiveNode(curve.getActiveControlPoints().get(0));
        currentCurve = newCurve;
        curvesList.add(newCurve);
        if (bDebugLogCurveManagerInfo) LOG.info("## CurveManager.createNewCurve() ## Added new curve: {} to active curves list, widget {}", newCurve, curveWidget);

        curveWidget.setCurrentAnchor(currentCurve.getActiveNode());
        updateAllCurves();
        // Create the curve panel if not already done.
        if (curvePanel == null) curvePanel = new CurvePanel();
        // Update the curve panel
        curvePanel.updateCurvePanel(newCurve);
        // Show the curve panel if it is not already visible
        if (!curvePanel.isCurvePanelVisible()) curvePanel.showCurvePanel();
    }

    /**
     * Set the specified curve as the active curve.
     * @param curve The curve to set as active.
     */
    public void setActiveCurve(CurveInfo curve) {
        if (currentCurve != null) {
            if (currentCurve.getCurveType().equals(curve.getCurveType())) {
                currentCurve.setActive(false);
                if (bDebugLogCurveManagerInfo) LOG.info("## CurveManager.setActiveCurve() ## Setting current curve '{}' to inactive", currentCurve.getCurve().toString());
            }
            currentCurve.getWidget().setWidgetEnabled(false);
        }
        curve.setActive(true);
        currentCurve = curve;
        if (currentCurve.getActiveNode() != null) currentCurve.getWidget().setCurrentAnchor(currentCurve.getActiveNode());
        currentCurve.setActive(true);
        currentCurve.getWidget().setWidgetEnabled(true);
        curvePanel.updateCurvePanel(currentCurve);
        if (bDebugLogCurveManagerInfo) LOG.info("## CurveManager.createNewCurve() ## Setting current curve to: {} ( {} )", currentCurve.getCurve().toString(), currentCurve.isActive());
        updateAllCurves();
    }

    /**
     * Set the current curve based on the specified MapNode.
     * @param node The MapNode to check.
     * @return The CurveInterface if the node is part of an active curve, null otherwise.
     */
    public CurveInterface setCurrentCurveFromNode(MapNode node) {
        // Check if the curve is already active
        for (CurveInfo curveInfo : curvesList) {
            ArrayList<MapNode> cpList = curveInfo.getCurve().getActiveControlPoints();
            for (MapNode listNode : cpList) {
                if (listNode == node) {
                    currentCurve = curveInfo;
                    curvePanel.updateCurvePanel(currentCurve);
                    return curveInfo.getCurve();
                }
                if (bDebugLogCurveManagerInfo) LOG.info("## CurveManager.setCurrentCurveFromNode() ## Checking node: {} in curve: {}", node, curveInfo);
            }
        }
        return null;
    }

    /**
     * Get the curve associated with the specified MapNode.
     * @param node The MapNode to check.
     * @return The CurveInfo if the node is part of an active curve, null otherwise.
     */
    public CurveInfo getCurveForNode(MapNode node) {
        for (CurveInfo curveInfo : curvesList) {
            if (curveInfo.getCurve().getActiveControlPoints().contains(node)) {
                if (bDebugLogCurveManagerInfo) LOG.info("## CurveManager.getCurveForNode() ## Found curve: {} for node: {}", curveInfo.getCurve().toString(), node);
                return curveInfo;
            }
        }
        if (bDebugLogCurveManagerInfo) LOG.info("## CurveManager.getCurveForNode() ## No curve found for node: {}", node);
        return null;
    }

    /**
     * Get the widget of the current curve.
     * @return The widget of the current curve, or null if no current curve is set.
     */
    public WidgetInterface getCurrentWidget() {
        return currentCurve.getWidget();
    }

    /**
     * Remove a curve from the list of currently active curves.
     * @param curve The curve to remove.
     * @see #registerCurveType(String, Class)
     */
    public void removeActiveCurve(CurveInfo curve) {
        for (CurveInfo curveInfo : curvesList) {
            if (curveInfo == curve) {
                if (bDebugLogCurveManagerInfo) LOG.info("## CurveManager.removeActiveCurve() ## Removing curve: {} from active curves", curve);
                curvesList.remove(curveInfo);
                break;
            }
        }
        if (!checkIfWidgetInUse(curve.getWidget())) widgetManager.removeWidgetClass(curve.getWidget());
    }

    /**
     * Check if the specified widget is in use by any of the active curves.
     * @param widget The widget to check.
     * @return true/false if the specified widget is in use by another curve.
     */
    private boolean checkIfWidgetInUse(WidgetInterface widget) {
        for (CurveInfo curveInfo : curvesList) {
            if (curveInfo.getWidget() == widget) return true;
        }
        return false;
    }

    /**
     * Commit the current curve and remove it from the list of active curves.
     * Sets the current curve to the last active curve.
     * If no current curves remain, will hide the curvePanel.
     */
    public void commitActiveCurve() {
        currentCurve.getCurve().commitCurve();
        removeActiveCurve(currentCurve);
        CurveInfo nextCurve = getNextActiveCurve();
        if (nextCurve != null) {
            LOG.info("## CurveManager.commitActiveCurve() ## Setting new curve to {}", nextCurve.getCurve().toString());
            setActiveCurve(nextCurve);
            widgetManager.updateAllWidgets();
            curvePanel.updateCurvePanel(currentCurve);
        } else {
            LOG.info("## CurveManager.commitActiveCurve() ## No active curves available, closing curve panel");
            curvePanel.hideCurvePanel();
        }
    }

    /**
     * Cancels the current active curve and remove it from the list of active curves.
     * If any other active curves remain, set the current curve to the next active curve.
     */
    public void cancelActiveCurve() {
        if (bDebugLogCurveManagerInfo) LOG.info("## CurveManager.cancelActiveCurve() ## Canceling active curve'{}'", currentCurve);
        currentCurve.setActive(false);
        currentCurve.getWidget().setWidgetEnabled(false);
        currentCurve.getCurve().cancelCurve();
        removeActiveCurve(currentCurve);

        CurveInfo nextCurve = getNextActiveCurve();

        if (nextCurve != null) {
            if (bDebugLogCurveManagerInfo) LOG.info("## CurveManager.cancelActiveCurve() ## Setting new curve to {}", nextCurve.getCurve().toString());
            setActiveCurve(nextCurve);
            widgetManager.updateAllWidgets();
            curvePanel.updateCurvePanel(currentCurve);
        } else {
            if (bDebugLogCurveManagerInfo) LOG.info("## CurveManager.cancelActiveCurve() ## No active curves available, closing curve panel");
            curvePanel.hideCurvePanel();
        }
    }

    /**
     * Update all active curves.
     */
    public void updateAllCurves() {
        for (CurveInfo curveInfo : curvesList) {
            curveInfo.getCurve().updateCurve();
            curveInfo.getWidget().updateWidget();
        }
    }

    public void cancelAllCurves() {
        for (CurveInfo curveInfo : curvesList) {
            curveInfo.setActive(false);
            curveInfo.getWidget().setWidgetEnabled(false);
            curveInfo.getCurve().cancelCurve();
        }
        curvesList.clear();
        if (curvePanel != null) curvePanel.hideCurvePanel();
    }

    /**
     * Called when any button is selected, checks if the button that called the function
     * has any curves associated with it, and if so, sets the new curve based on its
     * active state.
     * @param button The button that was selected.
     * @see CurveInfo
     * @see CurveInterface
     */
    public void onButtonSelected(ButtonManager.ButtonInterface button) {
        ArrayList<CurveInfo> buttonCurveList = getAllCurvesForButton(button);
        if (bDebugLogCurveManagerInfo) LOG.info("## CurveManager.onButtonSelected() ## Found {} curves for button '{}'", buttonCurveList.size(), button.getButtonID());
        for (CurveInfo curveInfo : buttonCurveList) {
            if (bDebugLogCurveManagerInfo) LOG.info("## CurveManager.onButtonSelected() ## Checking curve: '{}' ( active = {} )", curveInfo.getCurve(), curveInfo.isActive());
            if (curveInfo.isActive()) {
                if (bDebugLogCurveManagerInfo) LOG.info("## CurveManager.onButtonSelected() ## Button '{}' Selected, setting Active: {}", button.getButtonID(), curveInfo.getCurve());
                if (curveInfo.activeNode != null) {
                    setActiveCurve(curveInfo);
                    curveInfo.getWidget().setCurrentAnchor(curveInfo.activeNode);
                    curveInfo.getWidget().setCurrentSelector(curveInfo.activeNode.getNodeSelector());
                    //curveInfo.getWidget().setWidgetEnabled(true);
                } else {
                    if (bDebugLogCurveManagerInfo) LOG.warn("## CurveManager.onButtonSelected() ## No active node set for curve: {}", curveInfo.getCurve());
                }
            }
        }
    }

    /**
     * Called when any button is deselected, checks if the current curve
     * is associated with the button that called the function, and if so,
     * sets the current curve's widget to inactive.
     * @param button The button that was deselected.
     * @see CurveInfo
     * @see CurveInterface
     */
    public void onButtonDeselected(ButtonManager.ButtonInterface button) {
        if (currentCurve != null/* && !currentCurve.getOwnerName().equals(button.getButtonID())*/) {
            if (bDebugLogCurveManagerInfo) LOG.info("## CurveManager.onButtonDeselected() ## Button '{}' deselected, setting {} Inactive: {}", button.getButtonID(), currentCurve.getWidget(), currentCurve.getCurve());
            currentCurve.getWidget().setWidgetEnabled(false);
            widgetManager.updateAllWidgets();
        }
    }

    /**
     * Get all curves associated with the specified button.
     * @see CurveInfo
     * @see CurveInterface
     * @param button The button to check.
     * @return An ArrayList of CurveInfo objects associated with the button.
     */
    private ArrayList<CurveInfo> getAllCurvesForButton(ButtonManager.ButtonInterface button) {
        ArrayList<CurveInfo> buttonCurves = new ArrayList<>();
        for (CurveInfo curveInfo : curvesList) {
            if (curveInfo.getOwnerName().equals(button.getButtonID())) {
                buttonCurves.add(curveInfo);
            }
        }
        return buttonCurves;
    }

    //
    // Getters
    //

    /**
     * Check if the current curve list has any entries.
     * @return true/false if the cure list is empty.
     */
    public boolean isCurveListEmpty() { return curvesList.isEmpty(); }


    /**
     * Check if a preview curve is created.
     * @return true/false if the current curve preview is created.
     */
    public boolean isCurvePreviewCreated() { return !this.curvesList.isEmpty(); }

    /**
     * Check all active curves if the supplied MapNode is a control node.
     * @param node The MapNode to check.
     * @return true/false if the supplied MapNode is a control node.
     */
    public boolean isControlNode(MapNode node) {
        for (CurveInfo curve : curvesList) {
            if (curve.getCurve().isControlNode(node)) return true;
        }
        return false;
    }

    /**
     * Check all curves if the supplied MapNode is an anchor node.
     * @param node The MapNode to check.
     * @return true/false if the supplied MapNode is an anchor node.
     */
    public boolean isAnchorNode(MapNode node) {
        for (CurveInfo curve : curvesList) {
            if (curve.getCurve().isCurveAnchorNode(node)) return true;
        }
        return false;
    }

    /**
     * Get the current curve.
     * @return The current curve, or null if no curves are active.
     */
    public CurveInterface getCurrentCurve() { return (currentCurve != null) ? currentCurve.getCurve() : null; }

    /**
     * Get the next active curve from the list of active curves.
     * If no curves are available, return null.
     * @return The next active curve, or null if no curves are available.
     */
    private CurveInfo getNextActiveCurve() {
        if (!curvesList.isEmpty()) {
            CurveInfo lastCurve = curvesList.get(curvesList.size() -1);
            if (bDebugLogCurveManagerInfo) LOG.info("## CurveManager.getNextActiveCurve() ## Last active curve: {}", lastCurve.getCurve());
            return lastCurve;
        } else {
            currentCurve = null;
            if (bDebugLogCurveManagerInfo) LOG.info("## CurveManager.getNextActiveCurve() ## No active curves available, return null");
        }
        return null;
    }

    /**
     * Returns the widget associated with a specific curve.
     * <p>Searches through the list of active curves and returns the widget
     * assigned to the specified curve.</p>
     * @param curve The Curve to find the widget for
     * @return If no curve is not found, returns null.
     * @see WidgetInterface
     * @see CurveInfo
     * @see CurveInterface
     */
    public WidgetInterface getCurrentCurveWidget(CurveInterface curve) {
        for (CurveInfo ci : curvesList) {
            if (ci.getCurve() == curve) {
                if (bDebugLogCurveManagerInfo) LOG.info("## CurveManager.getCurrentCurveWidget() ## Found curve widget: {}", ci.getWidget());
                return ci.getWidget();
            }
        }
        return null;
    }



    /**
     * Get all active control nodes from all active curves.
     * @return An ArrayList of all active control nodes.
     */
    public ArrayList<MapNode> getAllActiveControlNodes() {
        ArrayList<MapNode> allControlNodes = new ArrayList<>();
        for (CurveInfo curve : curvesList) {
            allControlNodes.addAll(curve.getCurve().getActiveControlPoints());
        }
        return allControlNodes;
    }

    /**
     * Check all curves if the supplied MapNode is an anchor.
     * @param node The MapNode to check.
     * @return The Curve that contains the anchor node, or null if not found.
     */
    public CurveInterface getAnchorNode(MapNode node) {
        for (CurveInfo curve : curvesList) {
            if (curve.getCurve().isCurveAnchorNode(node)) return curve.getCurve();
        }
        return null;
    }

    /**
     * Returns the control node at the given screen position.
     * @param pointX screen position x
     * @param pointY screen position y
     * @return The MapNode at the given screen position, or null if not found.
     */
    public MapNode getControlNodeAt(int pointX, int pointY) {
        for (CurveInfo curve : curvesList) {
            ArrayList<MapNode> controlPoints = curve.getCurve().getActiveControlPoints();
            for (MapNode node : controlPoints) {
                Point2D cpPosition = worldPosToScreenPos(node.getX(), node.getZ());
                if (pointX < cpPosition.getX() + nodeSizeScaledHalf && pointX > cpPosition.getX() - nodeSizeScaledHalf && pointY < cpPosition.getY() + nodeSizeScaledHalf && pointY > cpPosition.getY() - nodeSizeScaledHalf) {
                    return node;
                }
            }
        }
        return null;
    }

    /**
     * Get the widget associated with the current curve.
     * @return Widget associated with the current curve, or null if no curve is active.
     * @see WidgetInterface
     * @see #currentCurve
     */
    public WidgetInterface getCurrentCurveWidget() {
        return (this.currentCurve != null) ? this.currentCurve.getWidget() : null;
    }

    /**
     * Get the curve associated with a given MapNode.
     * @param node The MapNode to check.
     * @return The Curve associated with the MapNode, or null if not found.
     */
    public CurveInfo getCurveNode(MapNode node) {
        for (CurveInfo curve : curvesList) {
            if (curve.getCurve().getActiveControlPoints().contains(node)) return curve;
        }
        return null;
    }

    /**
     * Get the curve panel associated with the curve manager.
     * @return The active curve panel.
     */
    public CurvePanel getCurvePanel() {
        return curvePanel;
    }

    //
    // Setters
    //

    /**
     * Sets the current curve node priority
     * @param nodeType priority type
     * @see MapNode
     */
    public void setNodePriority(int nodeType) { if (currentCurve != null ) currentCurve.getCurve().setCurvePriority(nodeType); }

    /**
     * Sets the current curve connection type
     * @param type The connection type to set.
     * @see Connection.ConnectionType
     */
    public void setCurveType(Connection.ConnectionType type) { if (currentCurve != null ) currentCurve.getCurve().setCurveType(type); }

    /**
     * Sets the number of interpolation points for the current curve
     * @param numPoints The number of interpolation points to set.
     * @see CurvePanel
     */
    public void setNumInterpolationPoints(int numPoints) { currentCurve.getCurve().setCurveInterpolations(numPoints); }

    /**
     * Sets the number of control points for the current curve
     * @param value The number of control points to set.
     * @see CurvePanel
     */
    public void setNumControlPoints(int value) { currentCurve.getCurve().setNumCurrentControlPoints(value); }

    /**
     * Swaps the direction of the current curve
     */
    public void swapCurveDirection() { if (currentCurve != null ) currentCurve.getCurve().swapCurveDirection(); }

    //
    // Mouse event handlers
    // These methods will call the corresponding method on each active curve.
    // events are passed onto these from the MouseListener class
    //

    public void mouseClicked(MouseEvent e) {
        for (CurveInfo curve : curvesList) curve.getCurve().mouseClicked(e);
    }

    public void mousePressed(MouseEvent e) {
        for (CurveInfo curve : curvesList) curve.getCurve().mousePressed(e);
    }

    public void mouseReleased(MouseEvent e) {
        isDraggingWidget = false;
        for (CurveInfo curve : curvesList) curve.getCurve().mouseReleased(e);
    }

    public void mouseDragged(MouseEvent e) {
        isDraggingWidget = true;
        for (CurveInfo curve : curvesList) curve.getCurve().mouseDragged(e);
    }

    public void mouseMoved(MouseEvent e) {
        for (CurveInfo curve : curvesList) curve.getCurve().mouseMoved(e);
    }

    public void mouseWheelMoved(MouseWheelEvent e) {
        for (CurveInfo curve : curvesList) curve.getCurve().mouseWheelMoved(e);
    }

    /**
     * Draw all active curve previews to the screen.
     * @param g The Graphics object to draw on.
     */

    public void drawToScreen(Graphics g) {
        showDebug();
        for (CurveInfo curve : curvesList) curve.getCurve().drawToScreen(g);
    }


    //
    // Debug Info
    //

    private void showDebug() {
        curveManagerGroup.reset();
        if (curvesList.isEmpty()) {
            curveManagerGroup.addCenteredText("No Registered Curves", false);
        }
        for (CurveInfo c : curvesList) {
            curveManagerGroup.addText("Instance", c.getCurve().toString());

            curveManagerGroup.addLine();
            curveManagerGroup.addText("Owner", c.getOwnerName());
            curveManagerGroup.addText("Type", c.getCurveType());
            curveManagerGroup.addText("Active", String.valueOf(c.isActive()));
            curveManagerGroup.addText("Active Node", (c.getActiveNode() != null) ? c.getActiveNode().toString() : "null");
            if (c.curveType.equals(CURVE_TYPE_ARCSPLINE)) {
                ArrayList<MapNode> controlPoints = c.getCurve().getActiveControlPoints();
                curveManagerGroup.addText("Start Angle ", String.valueOf(c.getCurve().getRotationAngle(controlPoints.get(1))));
                curveManagerGroup.addText("End Angle ", String.valueOf(c.getCurve().getRotationAngle(controlPoints.get(2))));
            }
            curveManagerGroup.addText("Interpolations", String.valueOf(c.getCurve().getNumInterpolations()));
            curveManagerGroup.addText("Control Points", String.valueOf(c.getCurve().getNumCurrentControlPoints()));
            curveManagerGroup.addText("Widget", c.getWidget().toString());
            curveManagerGroup.addText("Selector", String.valueOf(c.getWidget().getCurrentSelector()));
            curveManagerGroup.addEmptyLine();
        }
    }

    //
    // return a shortened version of this class name for, logging purposes
    //
    @Override
    public String toString() { return "CurveManager"; }

    /**
     * This class holds information about a curve, including its owner, type, instance, and widget class.
     * It is used to manage the curves in the CurveManager.
     * <p>
     * Example usage:
     * <pre>
     * CurveInfo curveInfo = new CurveInfo("OwnerName", "CurveType", curveInstance, widgetClass);
     * </pre>
     * @see CurveInterface
     * @see WidgetInterface
     * @see CurveManager
     */
    public static class CurveInfo {
        private final String ownerName;
        private final String curveType;
        private final CurveInterface curveInstance;
        private final WidgetInterface widgetClass;
        private boolean isActive;
        private MapNode activeNode;

        public CurveInfo(String owner, String name, CurveInterface curve, WidgetInterface widgetClass) {
            this.ownerName = owner;
            this.curveType = name;
            this.curveInstance = curve;
            this.widgetClass = widgetClass;
            // Default to active when created
            this.isActive = true;
            this.activeNode = null;
        }

        public String getOwnerName() {
            return ownerName;
        }

        public String getCurveType() {
            return curveType;
        }
        public CurveInterface getCurve() {
            return curveInstance;
        }
        public WidgetInterface getWidget() {
            return widgetClass;
        }

        public boolean isActive() { return isActive; }
        public MapNode getSelectedNode() { return activeNode; }
        public MapNode getActiveNode() { return activeNode; }


        public void setActive(boolean active) {
            if (bDebugLogCurveManagerInfo) LOG.info("## CurveInfo.setActive() ## Setting curve '{}' to active: {}", curveInstance, active);
            this.isActive = active;
        }
        public void setActiveNode(MapNode node) {
            if (bDebugLogCurveManagerInfo) LOG.info("## CurveInfo.setActiveNode() ## Setting Active node to '{}'", node);
            this.activeNode = node;
            this.widgetClass.setCurrentSelector(node.getNodeSelector());
        }


    }
}
