package AutoDriveEditor.Utils;

import AutoDriveEditor.AutoDriveEditor;
import AutoDriveEditor.Listeners.CurvePanelListener;
import AutoDriveEditor.Listeners.EditorListener;
import AutoDriveEditor.Listeners.MenuListener;

import javax.swing.*;
import java.awt.*;
import java.awt.event.ItemListener;
import java.net.URL;

import static AutoDriveEditor.GUI.GUIBuilder.textArea;
import static AutoDriveEditor.GUI.GUIBuilder.textPanel;
import static AutoDriveEditor.GUI.MenuBuilder.InputEvent_NONE;
import static AutoDriveEditor.GUI.MenuBuilder.KeyEvent_NONE;
import static AutoDriveEditor.Locale.LocaleManager.localeString;
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
            if (toolTipText != null) button.setToolTipText(localeString.getString(toolTipText));
            if (altText != null) button.setText(localeString.getString(altText));
        } else {
            button.setToolTipText(toolTipText);
            button.setText(altText);
        }
        panel.add(button);
        button.setEnabled(enabled);

        return button;
    }
    public static JButton makeButton(String actionCommand,String toolTipText,String altText, JPanel panel, ButtonGroup group, boolean isGroupDefault, EditorListener editorListener, boolean enabled) {
        JButton button = new JButton();
        button.setActionCommand(actionCommand);
        button.setToolTipText(localeString.getString(toolTipText));
        button.addActionListener(editorListener);
        button.setText(localeString.getString(altText));
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

    public static JToggleButton makeImageButton(String imageName, String selectedImageName, String actionCommand, String toolTipText, String altText, JPanel panel, EditorListener editorListener) {

        JToggleButton imageButton = new JToggleButton();

        imageButton.setActionCommand(actionCommand);
        imageButton.setToolTipText(localeString.getString(toolTipText));
        imageButton.addActionListener(editorListener);
        imageButton.setFocusPainted(false);
        imageButton.setSelected(false);
        imageButton.setBorderPainted(false);
        imageButton.setContentAreaFilled(false);

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
                    imageButton.setSelectedIcon(new ImageIcon(selectedImageURL, altText));
                }
            }
        } else {
            //no image found
            imageButton.setText(localeString.getString(altText));
        }
        panel.add(imageButton);
        return imageButton;
    }

    public static JToggleButton makeImageToggleButton(String imageName, String actionCommand, String toolTipText, String altText, JPanel panel, boolean isSelected, ButtonGroup group, boolean isGroupDefault, EditorListener editorListener) {
        return makeImageToggleButton(imageName, null, actionCommand, toolTipText, altText, panel, isSelected, group, isGroupDefault, editorListener);
    }

    public static JToggleButton makeImageToggleButton(String imageName, String selectedImageName, String actionCommand, String toolTipText, String altText, JPanel panel, boolean isSelected, ButtonGroup group, boolean isGroupDefault, EditorListener editorListener) {

        JToggleButton toggleButton = new JToggleButton();

        toggleButton.setActionCommand(actionCommand);
        toggleButton.setToolTipText(localeString.getString(toolTipText));
        toggleButton.addActionListener(editorListener);
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
            toggleButton.setText(localeString.getString(altText));
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
    public static JToggleButton makeStateChangeImageToggleButton (String imageName, String selectedImageName, String actionCommand, String toolTipText, String altText, JPanel panel, Boolean isSelected, ButtonGroup group,  boolean isGroupDefault, EditorListener editorListener) {
        JToggleButton button = makeImageToggleButton(imageName, selectedImageName, actionCommand, toolTipText, altText, panel, isSelected, group, isGroupDefault, editorListener);
        button.addMouseListener(editorListener);

        return button;
    }

    public static JRadioButton makeRadioButton(String text, String actionCommand, String toolTipText, Color textColour, boolean isSelected, boolean isOpaque, JPanel panel, ButtonGroup group,  boolean isGroupDefault, EditorListener actionListener) {
        return makeRadioButton(text, actionCommand, toolTipText, textColour, isSelected, isOpaque, panel, group, isGroupDefault, actionListener, null);
    }

    public static JRadioButton makeRadioButton(String text, String actionCommand, String toolTipText, Color textColour, boolean isSelected, boolean isOpaque, JPanel panel, ButtonGroup group,  boolean isGroupDefault, CurvePanelListener itemListener) {
        return makeRadioButton(text, actionCommand, toolTipText, textColour, isSelected, isOpaque, panel, group, isGroupDefault, null, itemListener);
    }

    public static JRadioButton makeRadioButton(String text, String actionCommand, String toolTipText, Color textColour, boolean isSelected, boolean isOpaque, JPanel panel, ButtonGroup group,  boolean isGroupDefault, EditorListener actionListener, CurvePanelListener itemListener) {
        TransparentRadioButton radioButton = new TransparentRadioButton(null);
        if (text != null) radioButton.setText(localeString.getString(text));
        radioButton.setActionCommand(actionCommand);
        radioButton.setToolTipText(localeString.getString(toolTipText));
        radioButton.setSelected(isSelected);
        radioButton.setOpaque(isOpaque);
        radioButton.setForeground(textColour);
        radioButton.setHorizontalAlignment(SwingConstants.LEADING);
        if (actionListener != null ) radioButton.addActionListener(actionListener);
        if (itemListener !=null ) radioButton.addItemListener(itemListener);
        panel.add(radioButton);
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

    public static JMenu makeMenu(String menuName, int keyEvent, String accString, JMenuBar parentMenu) {
        JMenu newMenu = new JMenu(localeString.getString(menuName));
        newMenu.setMnemonic(keyEvent);
        newMenu.getAccessibleContext().setAccessibleDescription(localeString.getString(accString));
        parentMenu.add(newMenu);
        return newMenu;
    }

    public static JMenu makeSubMenu(String menuName, int keyEvent, String accString, JMenu parentMenu) {
        JMenu newMenu = new JMenu(localeString.getString(menuName));
        newMenu.setMnemonic(keyEvent);
        newMenu.getAccessibleContext().setAccessibleDescription(localeString.getString(accString));
        parentMenu.add(newMenu);
        return newMenu;
    }

    public static JMenuItem makeMenuItem(String menuName, String accString, JMenu menu, MenuListener listener, String actionCommand, Boolean enabled) {
        return makeMenuItem(menuName, accString, KeyEvent_NONE, InputEvent_NONE, menu, listener, actionCommand, enabled);
    }

    public static JMenuItem makeMenuItem(String menuName, String accString, int keyEvent, int inputEvent, JMenu menu, MenuListener listener, String actionCommand, Boolean enabled) {
        JMenuItem menuItem = new JMenuItem(localeString.getString(menuName));
        if (keyEvent != 0) {
            menuItem.setAccelerator(KeyStroke.getKeyStroke(keyEvent, inputEvent));
        }
        menuItem.getAccessibleContext().setAccessibleDescription(localeString.getString(accString));
        menuItem.setEnabled(enabled);
        if (actionCommand != null) menuItem.setActionCommand(actionCommand);
        menuItem.addActionListener(listener);
        menu.add(menuItem);
        return menuItem;
    }

    public static JCheckBox makeCheckBox(JLabel label, String name, ItemListener listener, boolean enabled, boolean isSelected) {

        JCheckBox checkBox = new JCheckBox(" ", isSelected);
        checkBox.setName(name);
        if (listener != null) checkBox.addItemListener(listener);
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
    public static JCheckBoxMenuItem makeCheckBoxMenuItem (String text, String accString, int keyEvent, Boolean isSelected, JMenu menu, MenuListener itemListener, String actionCommand, Boolean enabled) {
        return makeCheckBoxMenuItem(text, accString, keyEvent, InputEvent_NONE, isSelected, menu, itemListener, actionCommand, enabled);
    }

    public static JCheckBoxMenuItem makeCheckBoxMenuItem (String text, String accString, int keyEvent, int inputEvent, Boolean isSelected, JMenu menu, MenuListener itemListener, String actionCommand, Boolean enabled) {
        JCheckBoxMenuItem cbMenuItem = new JCheckBoxMenuItem(localeString.getString(text), isSelected);
        cbMenuItem.setActionCommand(actionCommand);
        if (inputEvent != 0 && keyEvent != 0) {
            cbMenuItem.setAccelerator(KeyStroke.getKeyStroke(keyEvent, inputEvent));
            cbMenuItem.setMnemonic(keyEvent);
        } else if (inputEvent == InputEvent_NONE && keyEvent != 0){
            cbMenuItem.setAccelerator(KeyStroke.getKeyStroke(keyEvent, 0));
        }
        cbMenuItem.setSelected(isSelected);
        cbMenuItem.getAccessibleContext().setAccessibleDescription(localeString.getString(accString));
        cbMenuItem.addItemListener(itemListener);
        cbMenuItem.setEnabled(enabled);
        menu.add(cbMenuItem);

        return cbMenuItem;
    }

    public static JRadioButtonMenuItem makeSimpleRadioButtonMenuItem(String text, String accString, JMenu menu,MenuListener itemListener, String actionCommand, Boolean enabled, ButtonGroup buttonGroup, boolean isGroupDefault) {
        JRadioButtonMenuItem menuItem = new JRadioButtonMenuItem(text);
        menuItem.getAccessibleContext().setAccessibleDescription(localeString.getString(accString) + " " + text);
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

    public static JRadioButtonMenuItem makeRadioButtonMenuItem(String menuName, String accString, JMenu menu, MenuListener itemListener, String actionCommand, Boolean enabled, ButtonGroup buttonGroup, boolean isGroupDefault) {
        return makeRadioButtonMenuItem(menuName, accString, 0, 0, menu, itemListener, actionCommand, enabled, buttonGroup, isGroupDefault);
    }

    // if no button group is required, set buttonGroup to null and isGroupDefault will be ignored
    public static JRadioButtonMenuItem makeRadioButtonMenuItem(String menuName, String accString, int keyEvent, int inputEvent, JMenu menu, MenuListener itemListener, String actionCommand, Boolean enabled, ButtonGroup buttonGroup, boolean isGroupDefault) {
        JRadioButtonMenuItem menuItem = new JRadioButtonMenuItem(localeString.getString(menuName));
        if (keyEvent != 0 && inputEvent != 0) {
            menuItem.setAccelerator(KeyStroke.getKeyStroke(keyEvent, inputEvent));
        }
        menuItem.getAccessibleContext().setAccessibleDescription(localeString.getString(accString));
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
