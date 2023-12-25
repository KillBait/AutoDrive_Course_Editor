package AutoDriveEditor.Utils;

import AutoDriveEditor.AutoDriveEditor;

import javax.swing.*;
import java.awt.*;
import java.awt.event.ActionListener;
import java.awt.event.ItemListener;
import java.net.URL;

import static AutoDriveEditor.Locale.LocaleManager.getLocaleString;

public class GUIUtils {

    // 1st part of fix for alpha cascading errors on radio buttons.

    public static class AlphaContainer extends JComponent
    {
        private final JComponent component;

        public AlphaContainer(JComponent component)
        {
            this.component = component;
            setLayout( new BorderLayout() );
            setOpaque( false );
            component.setOpaque( false );
            add( component );
        }

        /**
         *  Paint the background using the background Color of the
         *  contained component
         */

        @Override
        public void paintComponent(Graphics g)
        {
            g.setColor( component.getBackground() );
            g.fillRect(0, 0, getWidth(), getHeight());
        }
    }

    // 2nd part of fix for alpha cascading errors on radio buttons

    static class TransparentRadioButton extends JRadioButton {
        public TransparentRadioButton(String string) {
            super(string);
            setOpaque(false);
            setContentAreaFilled(false);
            setBorderPainted(false);
            setForeground(Color.ORANGE);
            setBackground(new Color(0,0,0,0));
        }
    }

    public static final int InputEvent_NONE = 0;
    public static final int KeyEvent_NONE = 0;

    //
    // Button Creation functions
    //

    /**
     * @param actionCommand Unique string to associate to this button
     * @param toolTipText String for tooltip
     * @param altText String for alt text
     * @param panel Which JPanel this should be added to.
     * @param enabled Is button enabled upon creation.
     * @return Reference to the button
     */
    public static JButton makeBasicButton(String actionCommand, String toolTipText, String altText, JPanel panel, boolean enabled, boolean useLocale) {
        JButton button = new JButton();
        if (actionCommand != null) button.setActionCommand(actionCommand);
        if (useLocale) {
            if (toolTipText != null) button.setToolTipText(getLocaleString(toolTipText));
            if (altText != null) button.setText(getLocaleString(altText));
        } else {
            button.setToolTipText(toolTipText);
            button.setText(altText);
        }
        if (panel != null) panel.add(button);
        button.setEnabled(enabled);

        return button;
    }

    /**
     * @param actionCommand Unique string to associate to this button
     * @param toolTipText String for tooltip
     * @param altText String for alt text
     * @param panel Which JPanel this should be added to.
     * @param buttonGroup Which ButtonGroup to add this to. (can be null)
     * @param isGroupDefault is it the default button. (ignored if buttonGroup = null)
     * @param actionListener ActionListener the button should use.
     * @param enabled Is button enabled upon creation.
     * @return Reference to the button
     */
    public static JButton makeButton(String actionCommand,String toolTipText,String altText, JPanel panel, ButtonGroup buttonGroup, boolean isGroupDefault, ActionListener actionListener, boolean enabled) {
        JButton button = new JButton();
        button.setActionCommand(actionCommand);
        button.setToolTipText(getLocaleString(toolTipText));
        button.setText(getLocaleString(altText));
        button.addActionListener(actionListener);
        button.setFocusable(false);
        panel.add(button);
        if (buttonGroup != null) {
            buttonGroup.add(button);
            if (isGroupDefault) {
                //ButtonModel groupDefault = menuItem.getModel();
                buttonGroup.setSelected(button.getModel(), true);
            }
        }
        button.setEnabled(enabled);

        return button;
    }

    /**
     * @param imageName name of the file for the regular icon
     * @param selectedImageName name of file for the selected icon
     * @param actionCommand Unique string to associate to this button
     * @param toolTipText String for tooltip
     * @param altText String for alt text
     * @param panel Which JPanel this should be added to.
     * @param enabled Is button enabled upon creation.
     * @param actionListener ActionListener the button should use.
     * <br><br>if no image can be loaded, the altText string will be used
     * for the button instead.
     * @return Reference to the button
     */

    public static JButton makeImageButton(String imageName, String selectedImageName, String actionCommand, String toolTipText, String altText, JPanel panel, boolean enabled, ActionListener actionListener) {

        JButton imageButton = new JButton();

        if (actionCommand != null) imageButton.setActionCommand(actionCommand);
        imageButton.setToolTipText(getLocaleString(toolTipText));
        if (actionListener != null) imageButton.addActionListener(actionListener);
        imageButton.setFocusPainted(false);
        imageButton.setSelected(false);
        imageButton.setEnabled(enabled);
        imageButton.setBorderPainted(false);
        imageButton.setContentAreaFilled(false);
        imageButton.setFocusable(false);

        //Load image

        String imgLocation = "/editor/" + imageName + ".png";
        URL imageURL = AutoDriveEditor.class.getResource(imgLocation);
        if (imageURL != null) {
            //image found
            imageButton.setIcon(new ImageIcon(imageURL, altText));
            imageButton.setBorder(BorderFactory.createEmptyBorder());
            if (selectedImageName !=  null) {
                String selectedImagePath = "/editor/" + selectedImageName + ".png";
                URL selectedImageURL = AutoDriveEditor.class.getResource(selectedImagePath);
                if (selectedImageURL != null) {
                    imageButton.setPressedIcon(new ImageIcon(selectedImageURL, altText));
                }
            }
        } else {
            //no image found
            imageButton.setText(getLocaleString(altText));
        }
        panel.add(imageButton);
        return imageButton;
    }

    public static JToggleButton makeImageToggleButton(String imageName, String actionCommand, String toolTipText, String altText, JPanel panel, boolean isSelected, boolean enabled, ButtonGroup group, boolean isGroupDefault, ActionListener actionListener) {
        return makeImageToggleButton(imageName, null, actionCommand, toolTipText, altText, panel, isSelected, enabled, group, isGroupDefault, actionListener);
    }

    public static JToggleButton makeImageToggleButton(String imageName, String selectedImageName, String actionCommand, String toolTipText, String altText, JPanel panel, boolean isSelected, boolean enabled, ButtonGroup group, boolean isGroupDefault, ActionListener actionListener) {

        JToggleButton toggleButton = new JToggleButton();

        toggleButton.setEnabled(enabled);
        if (actionCommand != null) toggleButton.setActionCommand(actionCommand);
        toggleButton.setToolTipText(getLocaleString(toolTipText));
        if (actionListener != null) toggleButton.addActionListener(actionListener);
        toggleButton.setFocusPainted(false);
        toggleButton.setSelected(isSelected);
        toggleButton.setBorderPainted(false);
        toggleButton.setContentAreaFilled(false);

        //Load image

        String imgLocation = "/editor/" + imageName + ".png";
        URL imageURL = AutoDriveEditor.class.getResource(imgLocation);
        if (imageURL != null) {
            //image found
            toggleButton.setIcon(new ImageIcon(imageURL, altText));
            toggleButton.setBorder(BorderFactory.createEmptyBorder());
            if (selectedImageName !=  null) {
                String selectedImagePath = "/editor/" + selectedImageName + ".png";
                URL selectedImageURL = AutoDriveEditor.class.getResource(selectedImagePath);
                if (selectedImageURL != null) {
                    toggleButton.setSelectedIcon(new ImageIcon(selectedImageURL, altText));
                }
            }
        } else {
            //no image found
            toggleButton.setText(getLocaleString(altText));
        }

        panel.add(toggleButton);
        if (group != null) {
            group.add(toggleButton);
            if (isGroupDefault) {
                //ButtonModel groupDefault = menuItem.getModel();
                group.setSelected(toggleButton.getModel(), true);
            }
        }

        return toggleButton;
    }

    //
    // special version of JToggleButton using a separate listener to change it's right click behaviour
    //

    public static JToggleButton makeStateChangeImageToggleButton (String imageName, String selectedImageName, String actionCommand, String toolTipText, String altText, JPanel panel, boolean isSelected, boolean enabled, ButtonGroup group,  boolean isGroupDefault, ActionListener actionListener) {
        //button.addMouseListener(editorListener);
        return makeImageToggleButton(imageName, selectedImageName, actionCommand, toolTipText, altText, panel, isSelected, enabled, group, isGroupDefault, actionListener);
    }

    public static JRadioButton makeRadioButton(String text, String actionCommand, String toolTipText, Color textColour, boolean isSelected, boolean isOpaque, JPanel panel, ButtonGroup group,  boolean isGroupDefault, ItemListener itemListener) {
        return makeRadioButton(text, actionCommand, toolTipText, textColour, isSelected, isOpaque, panel, group, isGroupDefault, null, itemListener);
    }

    public static JRadioButton makeRadioButton(String text, String actionCommand, String toolTipText, Color textColour, boolean isSelected, boolean isOpaque, JPanel panel, ButtonGroup group, boolean isGroupDefault, ActionListener actionListener, ItemListener itemListener) {
        TransparentRadioButton radioButton = new TransparentRadioButton(null);
        //if (text != null) radioButton.setText(getLocaleString(text));
        if (actionCommand != null) radioButton.setActionCommand(actionCommand);
        radioButton.setToolTipText(getLocaleString(toolTipText));
        radioButton.setSelected(isSelected);
        radioButton.setOpaque(isOpaque);
        radioButton.setHorizontalAlignment(SwingConstants.LEADING);
        if (actionListener != null ) radioButton.addActionListener(actionListener);
        if (itemListener != null ) radioButton.addItemListener(itemListener);

        if (panel != null) panel.add(radioButton);

        if (text != null) {
            JLabel  label = new JLabel(getLocaleString(text), JLabel.LEADING);
            label.setForeground(textColour);
            if (panel != null) {
                panel.add(label);
            }
            label.setLabelFor(radioButton);
        }

        if (group != null) {
            group.add(radioButton);
            if (isGroupDefault) {
                //ButtonModel groupDefault = menuItem.getModel();
                group.setSelected(radioButton.getModel(), true);
            }
        }
        return radioButton;
    }

    public static JCheckBox makeCheckBox(JLabel label, String name, ItemListener itemListener, boolean enabled, boolean isSelected) {

        JCheckBox checkBox = new JCheckBox(" ", isSelected);
        checkBox.setName(name);
        if (itemListener != null) checkBox.addItemListener(itemListener);
        checkBox.setEnabled(enabled);
        checkBox.setHorizontalTextPosition(SwingConstants.LEFT);
        checkBox.setIconTextGap(0);
        checkBox.setMargin(new Insets(0, 0, 0, 0));
        checkBox.setBorderPainted(false);
        checkBox.setContentAreaFilled(false);
        checkBox.setFocusPainted(false);
        label.setLabelFor(checkBox);

        return checkBox;
    }

    private static SpringLayout.Constraints getConstraintsForCell(
            int row, int col,
            Container parent,
            int cols) {
        SpringLayout layout = (SpringLayout) parent.getLayout();
        Component c = parent.getComponent(row * cols + col);
        return layout.getConstraints(c);
    }

    @SuppressWarnings("unused")
    public static void makeGrid(Container parent,
                                int rows, int cols,
                                int initialX, int initialY,
                                int xPad, int yPad) {
        SpringLayout layout;
        try {
            layout = (SpringLayout)parent.getLayout();
        } catch (ClassCastException exc) {
            System.err.println("The first argument to makeGrid must use SpringLayout.");
            return;
        }

        Spring xPadSpring = Spring.constant(xPad);
        Spring yPadSpring = Spring.constant(yPad);
        Spring initialXSpring = Spring.constant(initialX);
        Spring initialYSpring = Spring.constant(initialY);
        int max = rows * cols;

        //Calculate Springs that are the max of the width/height so that all
        //cells have the same size.

        Spring maxWidthSpring = layout.getConstraints(parent.getComponent(0)).
                getWidth();
        Spring maxHeightSpring = layout.getConstraints(parent.getComponent(0)).
                getHeight();
        for (int i = 1; i < max; i++) {
            SpringLayout.Constraints cons = layout.getConstraints(
                    parent.getComponent(i));

            maxWidthSpring = Spring.max(maxWidthSpring, cons.getWidth());
            maxHeightSpring = Spring.max(maxHeightSpring, cons.getHeight());
        }

        //Apply the new width/height Spring. This forces all the
        //components to have the same size.

        for (int i = 0; i < max; i++) {
            SpringLayout.Constraints cons = layout.getConstraints(
                    parent.getComponent(i));

            cons.setWidth(maxWidthSpring);
            cons.setHeight(maxHeightSpring);
        }

        //Then adjust the x/y constraints of all the cells so that they
        //are aligned in a grid.

        SpringLayout.Constraints lastCons = null;
        SpringLayout.Constraints lastRowCons = null;
        for (int i = 0; i < max; i++) {
            SpringLayout.Constraints cons = layout.getConstraints(
                    parent.getComponent(i));
            if (i % cols == 0) { //start of new row
                lastRowCons = lastCons;
                cons.setX(initialXSpring);
            } else { //x position depends on previous component
                cons.setX(Spring.sum(lastCons.getConstraint(SpringLayout.EAST),
                        xPadSpring));
            }

            if (i / cols == 0) { //first row
                cons.setY(initialYSpring);
            } else { //y position depends on previous row
                //noinspection ConstantConditions
                cons.setY(Spring.sum(lastRowCons.getConstraint(SpringLayout.SOUTH),
                        yPadSpring));
            }
            lastCons = cons;
        }

        //Set the parent's size.

        SpringLayout.Constraints pCons = layout.getConstraints(parent);
        //noinspection ConstantConditions
        pCons.setConstraint(SpringLayout.SOUTH,
                Spring.sum(
                        Spring.constant(yPad),
                        lastCons.getConstraint(SpringLayout.SOUTH)));
        pCons.setConstraint(SpringLayout.EAST,
                Spring.sum(
                        Spring.constant(xPad),
                        lastCons.getConstraint(SpringLayout.EAST)));
    }

    public static void makeCompactGrid(Container parent,
                                       int rows, int cols,
                                       int initialX, int initialY,
                                       int xPad, int yPad) {
        SpringLayout layout;
        try {
            layout = (SpringLayout)parent.getLayout();
        } catch (ClassCastException exc) {
            System.err.println("The first argument to makeCompactGrid must use SpringLayout.");
            return;
        }

        //Align all cells in each column and make them the same width.

        Spring x = Spring.constant(initialX);
        for (int c = 0; c < cols; c++) {
            Spring width = Spring.constant(0);
            for (int r = 0; r < rows; r++) {
                width = Spring.max(width,
                        getConstraintsForCell(r, c, parent, cols).
                                getWidth());
            }
            for (int r = 0; r < rows; r++) {
                SpringLayout.Constraints constraints =
                        getConstraintsForCell(r, c, parent, cols);
                constraints.setX(x);
                constraints.setWidth(width);
            }
            x = Spring.sum(x, Spring.sum(width, Spring.constant(xPad)));
        }

        //Align all cells in each row and make them the same height.

        Spring y = Spring.constant(initialY);
        for (int r = 0; r < rows; r++) {
            Spring height = Spring.constant(0);
            for (int c = 0; c < cols; c++) {
                height = Spring.max(height,
                        getConstraintsForCell(r, c, parent, cols).
                                getHeight());
            }
            for (int c = 0; c < cols; c++) {
                SpringLayout.Constraints constraints =
                        getConstraintsForCell(r, c, parent, cols);
                constraints.setY(y);
                constraints.setHeight(height);
            }
            y = Spring.sum(y, Spring.sum(height, Spring.constant(yPad)));
        }

        //Set the parent's size.

        SpringLayout.Constraints pCons = layout.getConstraints(parent);
        pCons.setConstraint(SpringLayout.SOUTH, y);
        pCons.setConstraint(SpringLayout.EAST, x);
    }
}
