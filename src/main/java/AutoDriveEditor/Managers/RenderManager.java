package AutoDriveEditor.Managers;

import AutoDriveEditor.GUI.Buttons.BaseButton;

import java.awt.*;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

import static AutoDriveEditor.Classes.Util_Classes.LoggerUtils.LOG;
import static AutoDriveEditor.GUI.Menus.DebugMenu.ShowDrawOrderInfo.bDebugShowDrawOrderInfo;
import static AutoDriveEditor.GUI.Menus.DebugMenu.ShowDrawOrderInfo.drawOrderProfileGroup;

//
// Basic render manager
//

@SuppressWarnings("unused")
public class RenderManager {

    public static final int PRIORITY_NONE = 0;
    public static final int PRIORITY_LOW = 1;
    public static final int PRIORITY_MEDIUM = 64;
    public static final int PRIORITY_HIGH = 128;
    public static final int PRIORITY_MAX = 256;

    private static final List<Drawable> drawList = new ArrayList<>();
    private static int counter = 0;

    public RenderManager() { LOG.info("Initializing RenderManager"); }

    public static void addToRenderQueue(BaseButton buttonBase) {
        try {
            if (buttonBase != null) {
                if (!drawList.contains(buttonBase)) {
                    buttonBase.setRenderPriority(counter++);
                    drawList.add(buttonBase);
                }
            }
        } catch (Exception e) {
            throw new RuntimeException(e);
        }

    }

    public static void drawAll(Graphics g) {
        // Sort drawables based on priority
        Collections.sort(drawList);
        if (bDebugShowDrawOrderInfo) drawQueue();
        for (Drawable drawable : drawList) { drawable.drawToScreen(g); }
    }

    public static List<Drawable> getQueue() {
        // Sort drawables based on priority
        Collections.sort(drawList);
        return drawList;
    }

    public static void drawQueue() {
        drawOrderProfileGroup.reset();
        for (Drawable drawable : drawList) {
            drawOrderProfileGroup.addText("Priority: " + drawable.priority, drawable.getClass().getSimpleName() + " @" + Integer.toHexString(System.identityHashCode(drawable)));
        }
    }

    public abstract static class Drawable implements Comparable<Drawable> {

        protected int priority = 0;

        public Drawable() {
            this.setRenderPriority(PRIORITY_LOW);
            drawList.add(this);
        }

        // Abstract method to handle screen drawing
        public abstract void drawToScreen(Graphics g);

        // Get the render priority
        public int getRenderPriority() {
            return priority;
        }

        // Set the render priority
        public void setRenderPriority(int priority) {
            if (priority < 0 || priority > 256) {
                throw new IllegalArgumentException("Priority must be between 0 and 256");
            }
            this.priority = priority;
        }

        // Add this drawable to the render queue
        public void addToRenderQueue() { drawList.add(this); }

        // Remove this drawable from the render queue
        public void removeFromRenderQueue() { drawList.remove(this); }

        // Implementing Comparable interface to compare Drawable objects based on priority
        @Override
        public int compareTo(Drawable other) {
            return Integer.compare(this.priority, other.priority);
        }
    }
}
