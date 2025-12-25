package AutoDriveEditor.Classes.Util_Classes;

import com.formdev.flatlaf.extras.FlatSVGIcon;

import javax.swing.*;
import java.awt.*;
import java.awt.event.ActionListener;
import java.awt.event.ItemListener;

import static AutoDriveEditor.Locale.LocaleManager.getLocaleString;

public class GUIUtils {

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

    public static JButton makeSVGImageButton(String svgImageName, int width, int height, String actionCommand, String toolTipText, JPanel panel, boolean enabled, ActionListener actionListener) {
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

        // createSetting the unselected state icon,
        // the colour filter will ensure it adapts to any theme changes
        FlatSVGIcon svgUnselectedIcon = new FlatSVGIcon(svgImageName, width, height);
        //svgUnselectedIcon.setColorFilter(lightDarkColorFilter);
        imageButton.setIcon(svgUnselectedIcon);

        // createSetting the selected state icon,
        // Scale the image down slightly to give the icon a slight different appearance
        // the colour filter will ensure it adapts to any theme changes
        FlatSVGIcon svgSelectedIcon = new FlatSVGIcon(svgImageName, width-2, height-2);
        //svgSelectedIcon.setColorFilter(lightDarkColorFilter);
        imageButton.setPressedIcon(svgSelectedIcon);
        panel.add(imageButton);

        return imageButton;
    }

    public static JRadioButton makeRadioButton(String text, String actionCommand, String toolTipText, Color textColour, boolean isSelected, boolean isOpaque, JPanel panel, ButtonGroup group,  boolean isGroupDefault, ItemListener itemListener) {
        return makeRadioButton(text, actionCommand, toolTipText, textColour, isSelected, isOpaque, panel, group, isGroupDefault, null, itemListener);
    }

    @SuppressWarnings("unused")
    public static JRadioButton makeRadioButton(String text, String actionCommand, String toolTipText, Color textColour, boolean isSelected, boolean isOpaque, JPanel panel, ButtonGroup group, boolean isGroupDefault, ActionListener actionListener, ItemListener itemListener) {
        JRadioButton radioButton = new JRadioButton();
        if (text != null) radioButton.setText(getLocaleString(text));
        if (actionCommand != null) radioButton.setActionCommand(actionCommand);
        radioButton.setToolTipText(getLocaleString(toolTipText));
        radioButton.setSelected(isSelected);
        radioButton.setOpaque(isOpaque);
        radioButton.setHorizontalAlignment(SwingConstants.LEADING);
        if (actionListener != null ) radioButton.addActionListener(actionListener);
        if (itemListener != null ) radioButton.addItemListener(itemListener);
        if (panel != null) panel.add(radioButton);
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
}
