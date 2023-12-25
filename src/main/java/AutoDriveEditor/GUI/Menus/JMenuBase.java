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
import static AutoDriveEditor.Utils.LoggerUtils.LOG;

public abstract class JMenuBase extends JMenu implements ActionListener, ItemListener, ChangeListener {

    protected JMenu makeMenu(String menuName, int keyEvent, String accString, JMenuBar parentMenu, boolean isVisible) {
        setText(getLocaleString(menuName));
        setMnemonic(keyEvent);
        getAccessibleContext().setAccessibleDescription(getLocaleString(accString));
        setVisible(isVisible);
        parentMenu.add(this);
        return this;
    }

    protected JMenu makeSubMenu(String menuName, String accString) {
        return makeSubMenu(menuName, accString, true);
    }

    @SuppressWarnings("SameParameterValue")
    protected JMenu makeSubMenu(String menuName, String accString, boolean enabled) {
        setText(getLocaleString(menuName));
        getAccessibleContext().setAccessibleDescription(getLocaleString(accString));
        setEnabled(enabled);
        addActionListener(this);
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
