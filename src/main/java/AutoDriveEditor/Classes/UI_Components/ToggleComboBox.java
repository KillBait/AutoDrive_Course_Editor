package AutoDriveEditor.Classes.UI_Components;

import javax.swing.*;
import java.awt.*;
import java.util.HashSet;
import java.util.Set;

@SuppressWarnings("unused")
public class ToggleComboBox<E> extends JComboBox<E> {
    private final Set<Integer> disabledIndices = new HashSet<>();

    public ToggleComboBox(E[] items) {
        super(items);
        setRenderer(new DisabledItemRenderer());
    }

    public void disableName(String name) {
        for (int i = 0; i < getItemCount(); i++) {
            if (getItemAt(i).equals(name)) {
                disabledIndices.add(i);
                if (getSelectedIndex() == i) {
                    setSelectedIndex(findNextEnabledIndex(i));
                }
                repaint();
                return;
            }
        }
    }

    public void enableName(int index) {
        for (int i = 0; i < getItemCount(); i++) {
            if (getItemAt(i).equals(index)) {
                disabledIndices.remove(i);
                repaint();
                return;
            }
        }
    }

    public void disableIndex(int index) {
        if (index >= 0 && index < getItemCount()) {
            disabledIndices.add(index);
            if (getSelectedIndex() == index) {
                setSelectedIndex(findNextEnabledIndex(index));
            }
            repaint();
        }
    }

    public void enableIndex(int index) {
        if (index >= 0 && index < getItemCount()) {
            disabledIndices.remove(index);
            repaint();
        }
    }

    private int findNextEnabledIndex(int startIndex) {
        for (int i = startIndex + 1; i < getItemCount(); i++) {
            if (!disabledIndices.contains(i)) {
                return i;
            }
        }
        for (int i = 0; i < startIndex; i++) {
            if (!disabledIndices.contains(i)) {
                return i;
            }
        }
        return -1; // No enabled index found
    }

    @Override
    public void setSelectedIndex(int index) {
        if (!disabledIndices.contains(index)) {
            super.setSelectedIndex(index);
        }
    }

    private class DisabledItemRenderer extends DefaultListCellRenderer {
        @Override
        public Component getListCellRendererComponent(JList<?> list, Object value, int index, boolean isSelected, boolean cellHasFocus) {
            Component c = super.getListCellRendererComponent(list, value, index, isSelected, cellHasFocus);
            if (disabledIndices.contains(index)) {
                c.setEnabled(false);
                c.setForeground(Color.GRAY);
            } else {
                c.setEnabled(true);
            }
            return c;
        }
    }
}
