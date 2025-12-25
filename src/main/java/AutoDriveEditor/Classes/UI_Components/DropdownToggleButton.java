package AutoDriveEditor.Classes.UI_Components;

import javax.swing.*;
import java.awt.*;
import java.awt.event.MouseEvent;
import java.awt.event.MouseMotionAdapter;
import java.util.ArrayList;
import java.util.EventListener;
import java.util.EventObject;
import java.util.List;

import static AutoDriveEditor.Managers.IconManager.MENU_DROPDOWN_ICON;
import static AutoDriveEditor.Managers.IconManager.getSVGIcon;

@SuppressWarnings("unused")
public class DropdownToggleButton extends JToggleButton {

    private Icon dropdownIcon;
    private Insets borderInsets;
    private boolean hoverDropDownButton = false;
    private final List<DropdownButtonListener> listeners = new ArrayList<>();


    public DropdownToggleButton(JPanel panel, Icon icon) {
        super();
        this.setModel(new DropdownButtonModel());
        this.dropdownIcon = new DropDownIcon(getSVGIcon(MENU_DROPDOWN_ICON), false, 6, 4, 1.0f, .25f, 100);
        setIcon(icon);
        setHorizontalAlignment(SwingConstants.LEFT);
        addMouseMotionListener(new MouseMotionAdapter() {
            @Override
            public void mouseMoved(MouseEvent e) {
                hoverDropDownButton = (e.getX() > getInsets().left + getIcon().getIconWidth());
            }
        });
        panel.add(this);
    }

    public void playAnimation(Icon icon) {
        if (icon.getClass() == DropDownIcon.class) {
            ((DropDownIcon)icon).startAnimation(this);
        }
    }

    @Override
    public void setIcon(Icon icon) {
        super.setIcon(icon);
        setHorizontalAlignment(SwingConstants.LEFT);
        repaint();
    }

    @Override
    public Dimension getPreferredSize() {
        Dimension old = super.getPreferredSize();
        this.borderInsets = getInsets();
        return new Dimension( old.width + this.dropdownIcon.getIconWidth()+4, old.height);
    }

    public DropDownIcon getDropdownIcon() {
        return (DropDownIcon) this.dropdownIcon;
    }

    public void setDropdownIcon(Icon icon) {
        this.dropdownIcon = icon;
        repaint();
    }



    @Override
    protected void paintComponent(Graphics g) {
        super.paintComponent(g);
        this.dropdownIcon.paintIcon(this, g, getWidth() - this.borderInsets.left - this.dropdownIcon.getIconWidth() - (this.borderInsets.right/2), this.borderInsets.top);
    }



    //
    // Custom event for dropdown button trigger
    //

    public static class DropdownButtonEvent extends EventObject {
        public DropdownButtonEvent(Object source) {
            super(source);
        }
    }

    public interface DropdownButtonListener extends EventListener {
        void dropdownPressed(DropdownButtonEvent event);
    }

    public void addDropdownButtonListener(DropdownButtonListener listener) {
        listeners.add(listener);
    }

    public void removeDropdownButtonListener(DropdownButtonListener listener) {
        listeners.remove(listener);
    }

    private void triggerDropdownPressedEvent() {
        DropdownButtonEvent event = new DropdownButtonEvent(this);
        for (DropdownButtonListener listener : listeners) {
            listener.dropdownPressed(event);
        }
    }

    //
    // Modified ToggleButtonModel to handle dropdown button press
    //

    public class DropdownButtonModel extends ToggleButtonModel {

        @Override
        public void setSelected(boolean b) { super.setSelected(b); }

        @Override
        public void setPressed(boolean b) {
            // check if dropdown button is pressed
            if (hoverDropDownButton && b) {
                // play dropdown animation
                playAnimation(dropdownIcon);
                // fire button pressed event
                triggerDropdownPressedEvent();
                // return without changing the pressed state
                return;
            }
            // call super method, adjust the pressed state as normal
            super.setPressed(b);
        }
    }
}