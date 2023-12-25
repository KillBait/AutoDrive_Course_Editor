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

public abstract class JCheckBoxMenuItemBase extends JCheckBoxMenuItem implements ActionListener, ItemListener, ChangeListener {

    @SuppressWarnings("SameParameterValue")
    protected JCheckBoxMenuItem makeCheckBoxMenuItem(String text, String accString, Boolean isSelected, Boolean enabled) {
        return makeCheckBoxMenuItem(text, accString, KeyEvent_NONE, InputEvent_NONE, isSelected, enabled);
    }

    protected JCheckBoxMenuItem makeCheckBoxMenuItem(String text, String accString, int keyEvent, Boolean isSelected, Boolean enabled) {
        return makeCheckBoxMenuItem(text, accString, keyEvent, InputEvent_NONE, isSelected, enabled);
    }

    protected JCheckBoxMenuItem makeCheckBoxMenuItem (String text, String accString, int keyEvent, int inputEvent, Boolean isSelected, Boolean enabled) {
        setText(getLocaleString(text));
        if (keyEvent != KeyEvent_NONE && inputEvent != InputEvent_NONE) {
            setAccelerator(KeyStroke.getKeyStroke(keyEvent, inputEvent));
            setMnemonic(keyEvent);
        } else if (inputEvent == InputEvent_NONE && keyEvent != 0){
            setAccelerator(KeyStroke.getKeyStroke(keyEvent, 0));
        }
        setSelected(isSelected);
        getAccessibleContext().setAccessibleDescription(getLocaleString(accString));
        addItemListener(this);
        setEnabled(enabled);

        return this;
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
