package AutoDriveEditor.GUI.Menus;

import AutoDriveEditor.Classes.KeyBinds.Shortcut;

import javax.swing.*;
import javax.swing.event.ChangeEvent;
import javax.swing.event.ChangeListener;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.awt.event.ItemEvent;
import java.awt.event.ItemListener;

import static AutoDriveEditor.Classes.Util_Classes.GUIUtils.InputEvent_NONE;
import static AutoDriveEditor.Classes.Util_Classes.GUIUtils.KeyEvent_NONE;
import static AutoDriveEditor.Classes.Util_Classes.LoggerUtils.LOG;
import static AutoDriveEditor.GUI.Menus.DebugMenu.Logging.LogMenuDebugMenu.bDebugMenuState;
import static AutoDriveEditor.Locale.LocaleManager.getLocaleString;

public abstract class JMenuItemBase extends JMenuItem implements ActionListener, ItemListener, ChangeListener, Shortcut.ShortcutEventInterface {

    @SuppressWarnings({"SameParameterValue", "UnusedReturnValue"})
    protected JMenuItemBase makeMenuItem(String menuName) {
        return makeMenuItem(menuName, KeyEvent_NONE, InputEvent_NONE, true);
    }

    protected JMenuItemBase makeMenuItem(String menuName, boolean enabled) {
        return makeMenuItem(menuName, KeyEvent_NONE, InputEvent_NONE, enabled);
    }

    @SuppressWarnings("MagicConstant")
    protected JMenuItemBase makeMenuItem(String menuName, int keyEvent, int inputEvent, boolean enabled) {
        String text = getLocaleString(menuName);
        setText(text);
        if (keyEvent != 0) setAccelerator(KeyStroke.getKeyStroke(keyEvent, inputEvent));
        getAccessibleContext().setAccessibleDescription(text);
        setEnabled(enabled);
        addActionListener(this);
        return this;
    }

    protected JCheckBoxMenuItem makeCheckBoxMenuItem(String text, String accString, Boolean isSelected, Boolean enabled) {
        return makeCheckBoxMenuItem(text, accString, KeyEvent_NONE, InputEvent_NONE, isSelected, enabled);
    }

    @SuppressWarnings({"SameParameterValue", "MagicConstant"})
    protected JCheckBoxMenuItem makeCheckBoxMenuItem (String text, String accString, int keyEvent, int inputEvent, Boolean isSelected, Boolean enabled) {
        JCheckBoxMenuItem cbMenuItem = new JCheckBoxMenuItem(getLocaleString(text));
        if (keyEvent != KeyEvent_NONE && inputEvent != InputEvent_NONE) {
            cbMenuItem.setAccelerator(KeyStroke.getKeyStroke(keyEvent, inputEvent));
            cbMenuItem.setMnemonic(keyEvent);
        } else if (inputEvent == InputEvent_NONE && keyEvent != 0){
            cbMenuItem.setAccelerator(KeyStroke.getKeyStroke(keyEvent, 0));
        }
        cbMenuItem.setSelected(isSelected);
        cbMenuItem.getAccessibleContext().setAccessibleDescription(getLocaleString(accString));
        cbMenuItem.addItemListener(this);
        cbMenuItem.setEnabled(enabled);

        return cbMenuItem;
    }

    @Override
    public void actionPerformed(ActionEvent e) {
        if (bDebugMenuState) logMenuEventFor("actionPerformed()");
    }

    @Override
    public void itemStateChanged(ItemEvent e) {
        if (bDebugMenuState) logMenuEventFor("itemStateChanged()");
    }

    @Override
    public void stateChanged(ChangeEvent e) { if (bDebugMenuState) logMenuEventFor("stateChanged()");
    }

    @Override
    public void onShortcutChanged() {}

    private void logMenuEventFor(String function) {
        LOG.info("Menu:- ({}) - {}",this.getText(), function);

    }
}
