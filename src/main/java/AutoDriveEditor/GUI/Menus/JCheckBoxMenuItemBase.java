package AutoDriveEditor.GUI.Menus;

import javax.swing.*;
import javax.swing.event.ChangeEvent;
import javax.swing.event.ChangeListener;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.awt.event.ItemEvent;
import java.awt.event.ItemListener;
import java.util.EventObject;

import static AutoDriveEditor.Classes.Util_Classes.GUIUtils.InputEvent_NONE;
import static AutoDriveEditor.Classes.Util_Classes.GUIUtils.KeyEvent_NONE;
import static AutoDriveEditor.Classes.Util_Classes.LoggerUtils.LOG;
import static AutoDriveEditor.GUI.Menus.DebugMenu.Logging.LogMenuDebugMenu.bDebugMenuState;
import static AutoDriveEditor.Locale.LocaleManager.getLocaleString;

public abstract class JCheckBoxMenuItemBase extends JCheckBoxMenuItem implements ActionListener, ItemListener, ChangeListener {

    @SuppressWarnings("SameParameterValue")
    protected JCheckBoxMenuItem makeCheckBoxMenuItem(String text, Boolean isSelected, Boolean enabled) {
        return makeCheckBoxMenuItem(text, KeyEvent_NONE, InputEvent_NONE, isSelected, enabled);
    }

    protected JCheckBoxMenuItem makeCheckBoxMenuItem(String text, int keyEvent, Boolean isSelected, Boolean enabled) {
        return makeCheckBoxMenuItem(text, keyEvent, InputEvent_NONE, isSelected, enabled);
    }

    protected JCheckBoxMenuItem makeCheckBoxMenuItem (String text, int keyEvent, int inputEvent, Boolean isSelected, Boolean enabled) {
        String localeText = getLocaleString(text);
        setText(localeText);
        if (keyEvent != KeyEvent_NONE && inputEvent != InputEvent_NONE) {
            setAccelerator(KeyStroke.getKeyStroke(keyEvent, inputEvent));
            setMnemonic(keyEvent);
        } else if (inputEvent == InputEvent_NONE && keyEvent != 0){
            setAccelerator(KeyStroke.getKeyStroke(keyEvent, 0));
        }
        setSelected(isSelected);
        getAccessibleContext().setAccessibleDescription(localeText);
        addItemListener(this);
        setEnabled(enabled);

        return this;
    }

    @Override
    public void actionPerformed(ActionEvent e) {
        if (bDebugMenuState) logMenuEventFor(e, "actionPerformed()");
    }

    @Override
    public void itemStateChanged(ItemEvent e) {
        if (bDebugMenuState) logMenuEventFor(e, "itemStateChanged()");
    }

    @Override
    public void stateChanged(ChangeEvent e) { if (bDebugMenuState) logMenuEventFor(e, "stateChanged()");
    }

    private void logMenuEventFor(EventObject e, String function) {
        String output = String.format("Menu Event: (%s) - %s", this.getText(), function);
        if (e.getClass() == ItemEvent.class) {
            output += String.format(" - isSelected: %s", ((AbstractButton) ((ItemEvent) e).getItem()).isSelected());
        }
        LOG.info(output);
    }
}
