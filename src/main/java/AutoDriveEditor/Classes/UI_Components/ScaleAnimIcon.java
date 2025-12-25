package AutoDriveEditor.Classes.UI_Components;

import com.formdev.flatlaf.util.AnimatedIcon;

import javax.swing.*;
import java.awt.*;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;

import static AutoDriveEditor.Classes.Util_Classes.LoggerUtils.LOG;
import static AutoDriveEditor.GUI.Menus.DebugMenu.Logging.LogGUIInfoMenu.bDebugLogGUIInfo;


@SuppressWarnings("unused")
public class ScaleAnimIcon implements AnimatedIcon {

    public final int iconWidth;
    public final int iconHeight;
    private float startScale = 1.0f;
    private float endScale = 0.25f;
    private int time = 200; // animation time in milliseconds
    public Icon icon;
    public final Icon selectedIcon;
    public float progress;

    enum Direction {IDLE, UP, DOWN}
    public ScaleAnimIcon.Direction animDirection;
    public boolean isSelected;

    public ScaleAnimIcon(Icon icon, boolean isSelected, int width, int height) {
        this.icon = icon;
        this.selectedIcon = null;
        this.iconWidth = width;
        this.iconHeight = height;
        this.progress = startScale;
        this.animDirection = Direction.IDLE;
        this.isSelected = isSelected;
    }

    public ScaleAnimIcon(Icon icon, Icon selectedIcon, boolean isSelected, int width, int height) {
        this.icon = icon;
        this.selectedIcon = selectedIcon;
        this.iconWidth = width;
        this.iconHeight = height;
        this.progress = startScale;
        this.animDirection = Direction.IDLE;
        this.isSelected = isSelected;
    }

    @Override
    public int getAnimationDuration() {
        return this.time;
    }

    @Override
    public void paintIconAnimated(Component component, Graphics graphics, int x, int y, float v) {
        Graphics2D g2d = (Graphics2D) graphics.create();
        int translateX = (int) (x + (this.iconWidth - this.iconWidth * this.progress) / 2);
        int translateY = (int) ((component.getHeight() - this.iconHeight * this.progress) / 2);

        g2d.translate(translateX, translateY);
        g2d.scale(this.progress, this.progress);

        Icon iconToPaint = (this.selectedIcon == null) ? this.icon : (this.isSelected ^ this.animDirection == Direction.DOWN) ? this.selectedIcon : this.icon;
        try {
            if (iconToPaint != null) {
                iconToPaint.paintIcon(component, g2d, 0, 0);
            } else {
                LOG.error("Icon is null");
            }
        } catch (Exception e) {
            if (bDebugLogGUIInfo) LOG.error("Error painting icon: {}", e.getMessage());
        }

        g2d.dispose();
    }


    @Override
    public float getValue(Component component) {
        return ((AbstractButton) component).isSelected() ? 1 : 0;
    }

    @Override
    public int getIconWidth() {
        return this.iconWidth;
    }

    @Override
    public int getIconHeight() {
        return this.iconHeight;
    }

    public float getStartScale() {
        return startScale;
    }

    public float getEndScale() {
        return endScale;
    }

    public Icon getIcon() {
        return icon;
    }

    public Icon getSelectedIcon() {
        return selectedIcon;
    }

    public float getProgress() {
        return progress;
    }

    public boolean isSelected() { return this.isSelected; }

    //
    // Setters
    //

    public void setIcon(Icon icon) {
        this.icon = icon;
    }

    public void setSelectedIcon(Icon icon) {
        this.icon = icon;
    }

    public void setSelected(boolean isSelected) {
        this.isSelected = isSelected;
    }

    public void setStartScale(float startScale) { this.startScale = startScale; }

    public void setEndScale(float endScale) { this.endScale = endScale; }

    public void setAnimTime(int time) { this.time = time; }

    private void setProgress(float progress, Direction direction) {
        this.progress = progress;
        this.animDirection = direction;
    }

    public void startAnimation(AbstractButton button) {
        setSelected(button.isSelected());
        Timer timer = new Timer(10, new ActionListener() {
            private long startTime = -1;
            private boolean scalingDown = true;

            @Override
            public void actionPerformed(ActionEvent e) {

                if (startTime < 0) startTime = System.currentTimeMillis();
                long elapsed = System.currentTimeMillis() - startTime;
                float duration = time / 2.0f; // Half the time for scaling down, half for scaling up
                float progress = Math.min(getStartScale(), (float) elapsed / duration);

                if (scalingDown) {
                    float newScale = getStartScale() - progress;
                    setProgress(newScale, Direction.DOWN);
                    if (newScale <= getEndScale()) {
                        scalingDown = false;
                        startTime = System.currentTimeMillis(); // Reset start time for scaling up
                    }
                } else {
                    float newScale = getEndScale() + progress;
                    setProgress(newScale, Direction.UP);
                    if (getEndScale() + progress >= getStartScale()) {
                        setProgress(getStartScale(), Direction.IDLE);
                        ((Timer) e.getSource()).stop();
                    }
                }
                button.repaint();
            }
        });
        timer.start();
    }
}
