package AutoDriveEditor.Utils;

import AutoDriveEditor.AutoDriveEditor;
import AutoDriveEditor.Listeners.MenuListener;

import javax.swing.*;
import java.awt.*;
import java.awt.event.ActionListener;
import java.awt.event.ItemListener;
import java.net.URL;

import static AutoDriveEditor.GUI.GUIBuilder.textArea;
import static AutoDriveEditor.GUI.GUIBuilder.textPanel;
import static AutoDriveEditor.GUI.MenuBuilder.InputEvent_NONE;
import static AutoDriveEditor.GUI.MenuBuilder.KeyEvent_NONE;
import static AutoDriveEditor.Locale.LocaleManager.getLocaleString;
import static AutoDriveEditor.Utils.LoggerUtils.LOG;

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

    //
    // Button Creation functions
    //

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
        panel.add(button);
        button.setEnabled(enabled);

        return button;
    }

    public static JButton makeButton(String actionCommand,String toolTipText,String altText, JPanel panel, ButtonGroup group, boolean isGroupDefault, ActionListener actionListener, boolean enabled) {
        JButton button = new JButton();
        button.setActionCommand(actionCommand);
        button.setToolTipText(getLocaleString(toolTipText));
        button.addActionListener(actionListener);
        button.setText(getLocaleString(altText));
        button.setFocusable(false);
        panel.add(button);
        if (group != null) {
            group.add(button);
            if (isGroupDefault) {
                //ButtonModel groupDefault = menuItem.getModel();
                group.setSelected(button.getModel(), true);
            }
        }
        button.setEnabled(enabled);

        return button;
    }

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


    //
    // Menu Creation Functions
    //

    public static JMenu makeMenu(String menuName, int keyEvent, String accString, JMenuBar parentMenu) {
        JMenu newMenu = new JMenu(getLocaleString(menuName));
        newMenu.setMnemonic(keyEvent);
        newMenu.getAccessibleContext().setAccessibleDescription(getLocaleString(accString));
        parentMenu.add(newMenu);
        return newMenu;
    }

    public static JMenu makeSubMenu(String menuName, String accString, JMenu parentMenu) {
        JMenu newMenu = new JMenu(getLocaleString(menuName));
        newMenu.getAccessibleContext().setAccessibleDescription(getLocaleString(accString));
        parentMenu.add(newMenu);
        return newMenu;
    }

    public static JMenu makeSubMenu(String menuName, int keyEvent, String accString, JMenu parentMenu) {
        JMenu newMenu = new JMenu(getLocaleString(menuName));
        newMenu.setMnemonic(keyEvent);
        newMenu.getAccessibleContext().setAccessibleDescription(getLocaleString(accString));
        parentMenu.add(newMenu);
        return newMenu;
    }

    public static JMenuItem makeMenuItem(String menuName, String accString, JMenu menu, MenuListener listener, String actionCommand, Boolean enabled) {
        return makeMenuItem(menuName, accString, KeyEvent_NONE, InputEvent_NONE, menu, listener, actionCommand, enabled);
    }

    public static JMenuItem makeMenuItem(String menuName, String accString, int keyEvent, int inputEvent, JMenu menu, MenuListener listener, String actionCommand, Boolean enabled) {
        JMenuItem menuItem = new JMenuItem(getLocaleString(menuName));
        if (keyEvent != 0) {
            menuItem.setAccelerator(KeyStroke.getKeyStroke(keyEvent, inputEvent));
        }
        menuItem.getAccessibleContext().setAccessibleDescription(getLocaleString(accString));
        menuItem.setEnabled(enabled);
        if (actionCommand != null) menuItem.setActionCommand(actionCommand);
        menuItem.addActionListener(listener);
        menu.add(menuItem);
        return menuItem;
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

    public static JCheckBoxMenuItem makeCheckBoxMenuItem (String text, String accString, Boolean isSelected, JMenu menu, MenuListener itemListener, String actionCommand, Boolean enabled) {
        return makeCheckBoxMenuItem(text, accString, KeyEvent_NONE, InputEvent_NONE, isSelected, menu, itemListener, actionCommand, enabled);
    }
    @SuppressWarnings("unused")
    public static JCheckBoxMenuItem makeCheckBoxMenuItem (String text, String accString, int keyEvent, Boolean isSelected, JMenu menu, MenuListener itemListener, String actionCommand, Boolean enabled) {
        return makeCheckBoxMenuItem(text, accString, keyEvent, InputEvent_NONE, isSelected, menu, itemListener, actionCommand, enabled);
    }

    public static JCheckBoxMenuItem makeCheckBoxMenuItem (String text, String accString, int keyEvent, int inputEvent, Boolean isSelected, JMenu menu, MenuListener itemListener, String actionCommand, Boolean enabled) {
        JCheckBoxMenuItem cbMenuItem = new JCheckBoxMenuItem(getLocaleString(text), isSelected);
        cbMenuItem.setActionCommand(actionCommand);
        if (inputEvent != 0 && keyEvent != 0) {
            cbMenuItem.setAccelerator(KeyStroke.getKeyStroke(keyEvent, inputEvent));
            cbMenuItem.setMnemonic(keyEvent);
        } else if (inputEvent == InputEvent_NONE && keyEvent != 0){
            cbMenuItem.setAccelerator(KeyStroke.getKeyStroke(keyEvent, 0));
        }
        cbMenuItem.setSelected(isSelected);
        cbMenuItem.getAccessibleContext().setAccessibleDescription(getLocaleString(accString));
        cbMenuItem.addItemListener(itemListener);
        cbMenuItem.setEnabled(enabled);
        menu.add(cbMenuItem);

        return cbMenuItem;
    }

    public static JRadioButtonMenuItem makeSimpleRadioButtonMenuItem(String text, String accString, JMenu menu,MenuListener itemListener, String actionCommand, Boolean enabled, ButtonGroup buttonGroup, boolean isGroupDefault) {
        JRadioButtonMenuItem menuItem = new JRadioButtonMenuItem(text);
        menuItem.getAccessibleContext().setAccessibleDescription(getLocaleString(accString) + " " + text);
        menuItem.setEnabled(enabled);
        if (actionCommand != null) menuItem.setActionCommand(actionCommand);
        menuItem.addActionListener(itemListener);
        if (buttonGroup != null) {
            buttonGroup.add(menuItem);
            if (isGroupDefault) {
                buttonGroup.setSelected(menuItem.getModel(), true);
            }
        }
        menu.add(menuItem);
        return menuItem;
    }

    @SuppressWarnings("unused")
    public static JRadioButtonMenuItem makeRadioButtonMenuItem(String menuName, String accString, JMenu menu, MenuListener itemListener, String actionCommand, Boolean enabled, ButtonGroup buttonGroup, boolean isGroupDefault) {
        return makeRadioButtonMenuItem(menuName, accString, 0, 0, menu, itemListener, actionCommand, enabled, buttonGroup, isGroupDefault);
    }

    // if no button group is required, set buttonGroup to null and isGroupDefault will be ignored

    public static JRadioButtonMenuItem makeRadioButtonMenuItem(String menuName, String accString, int keyEvent, int inputEvent, JMenu menu, MenuListener itemListener, String actionCommand, Boolean enabled, ButtonGroup buttonGroup, boolean isGroupDefault) {
        JRadioButtonMenuItem menuItem = new JRadioButtonMenuItem(getLocaleString(menuName));
        if (keyEvent != 0 && inputEvent != 0) {
            menuItem.setAccelerator(KeyStroke.getKeyStroke(keyEvent, inputEvent));
        }
        menuItem.getAccessibleContext().setAccessibleDescription(getLocaleString(accString));
        menuItem.setEnabled(enabled);
        if (actionCommand != null) menuItem.setActionCommand(actionCommand);
        menuItem.addActionListener(itemListener);
        if (buttonGroup != null) {
            buttonGroup.add(menuItem);
            if (isGroupDefault) {
                buttonGroup.setSelected(menuItem.getModel(), true);
            }
        }
        menu.add(menuItem);
        return menuItem;
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

    public static void showInTextArea(String text, boolean clearAll, boolean outputToLogFile) {
        if (clearAll) {
            textArea.selectAll();
            textArea.replaceSelection(null);
        }
        if (outputToLogFile) LOG.info(text);
        textArea.append(text + "\n");
        textPanel.repaint();
    }
}
