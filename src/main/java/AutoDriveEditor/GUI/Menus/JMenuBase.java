package AutoDriveEditor.GUI.Menus;

import javax.swing.*;
import javax.swing.event.ChangeEvent;
import javax.swing.event.ChangeListener;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.awt.event.ItemEvent;
import java.awt.event.ItemListener;

import static AutoDriveEditor.Classes.Util_Classes.LoggerUtils.LOG;
import static AutoDriveEditor.GUI.Menus.DebugMenu.Logging.LogMenuDebugMenu.bDebugMenuState;
import static AutoDriveEditor.Locale.LocaleManager.getLocaleString;

public abstract class JMenuBase extends JMenu implements ActionListener, ItemListener, ChangeListener {

    protected JMenu makeSubMenu(String menuName, boolean enabled) {
        String text = getLocaleString(menuName);
        setText(text);
        getAccessibleContext().setAccessibleDescription(text);
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
