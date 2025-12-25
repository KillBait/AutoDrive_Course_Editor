package AutoDriveEditor.Classes.UI_Components;

import javax.swing.*;
import java.awt.*;

import static AutoDriveEditor.Classes.Util_Classes.LoggerUtils.LOG;
import static AutoDriveEditor.GUI.Menus.DebugMenu.Logging.LogGUIInfoMenu.bDebugLogGUIInfo;

@SuppressWarnings("unused")
public class DropDownIcon extends ScaleAnimIcon {

    public enum ArrowDirection {
        UP(180),
        DOWN(0),
        LEFT(90),
        RIGHT(270);

        private final int degrees;

        ArrowDirection(int degrees) {
            this.degrees = degrees;
        }

        public int getDegrees() {
            return degrees;
        }
    }
    private ArrowDirection arrowDirection = ArrowDirection.DOWN;

    public DropDownIcon(Icon icon, boolean isSelected, int width, int height) {
        super(icon, isSelected, width, height);
    }

    public DropDownIcon(Icon icon, boolean isSelected, int width, int height, float startScale, float endScale, int timeMilliseconds) {
        super(icon, isSelected, width, height);
    }

    public DropDownIcon(Icon icon, Icon selectedIcon, boolean isSelected, int width, int height) {
        super(icon, selectedIcon, isSelected, width, height);
    }

    public DropDownIcon(Icon icon, Icon selectedIcon, boolean isSelected, int width, int height, float startScale, float endScale, int timeMilliseconds) {
        super(icon, selectedIcon, isSelected, width, height);
    }

    @Override
    public void paintIconAnimated(Component component, Graphics graphics, int x, int y, float v) {
        Graphics2D g2d = (Graphics2D) graphics.create();
        int translateX = (int) (x + (iconWidth - iconWidth * progress) / 2);
        int translateY = (int) ((component.getHeight() - iconHeight * progress) / 2);

        g2d.translate(translateX + iconWidth / 2, translateY + iconHeight / 2);
        g2d.rotate(Math.toRadians(arrowDirection.getDegrees()));
        g2d.translate(-iconWidth / 2, -iconHeight / 2);
        g2d.scale(progress, progress);

        Icon iconToPaint = (selectedIcon == null) ? icon : (isSelected ^ this.animDirection == Direction.DOWN) ? selectedIcon : icon;
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

    public void setArrowDirection(ArrowDirection arrowDirection) {
        this.arrowDirection = arrowDirection;
    }
}
