package AutoDriveEditor.GUI.Menus;

import javax.swing.*;
import javax.swing.event.ChangeEvent;
import javax.swing.event.ChangeListener;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.awt.event.ItemEvent;
import java.awt.event.ItemListener;

import static AutoDriveEditor.GUI.Menus.DebugMenu.Logging.LogListenerStateMenu.bDebugMenuState;
import static AutoDriveEditor.Locale.LocaleManager.getLocaleString;
import static AutoDriveEditor.Utils.GUIUtils.InputEvent_NONE;
import static AutoDriveEditor.Utils.GUIUtils.KeyEvent_NONE;
import static AutoDriveEditor.Utils.LoggerUtils.LOG;

public abstract class JMenuItemBase extends JMenuItem implements ActionListener, ItemListener, ChangeListener {

    @SuppressWarnings({"SameParameterValue", "UnusedReturnValue"})
    protected JMenuItemBase makeMenuItem(String menuName, String accString) {
        return makeMenuItem(menuName, accString, KeyEvent_NONE, InputEvent_NONE, true);
    }

    protected JMenuItemBase makeMenuItem(String menuName, String accString, boolean enabled) {
        return makeMenuItem(menuName, accString, KeyEvent_NONE, InputEvent_NONE, enabled);
    }

    protected JMenuItemBase makeMenuItem(String menuName, String accString, int keyEvent, int inputEvent, boolean enabled) {
        setText(getLocaleString(menuName));
        if (keyEvent != 0) {
            setAccelerator(KeyStroke.getKeyStroke(keyEvent, inputEvent));
        }
        getAccessibleContext().setAccessibleDescription(getLocaleString(accString));
        setEnabled(enabled);
        addActionListener(this);
        return this;
    }

    protected JCheckBoxMenuItem makeCheckBoxMenuItem(String text, String accString, Boolean isSelected, Boolean enabled) {
        return makeCheckBoxMenuItem(text, accString, KeyEvent_NONE, InputEvent_NONE, isSelected, enabled);
    }

    @SuppressWarnings("SameParameterValue")
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

    private void logMenuEventFor(String function) {
        LOG.info("Menu:- ({}) - {}",this.getText(), function);

    }
}
