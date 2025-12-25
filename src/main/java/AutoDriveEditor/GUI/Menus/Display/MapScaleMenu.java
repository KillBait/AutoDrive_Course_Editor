package AutoDriveEditor.GUI.Menus.Display;

import AutoDriveEditor.GUI.Menus.JMenuBase;

import javax.swing.*;
import java.awt.event.ActionEvent;
import java.util.Enumeration;

import static AutoDriveEditor.Classes.Util_Classes.LoggerUtils.LOG;
import static AutoDriveEditor.GUI.Menus.DebugMenu.Logging.LogMenuDebugMenu.bDebugMenuState;
import static AutoDriveEditor.XMLConfig.EditorXML.setNewMapScale;

public class MapScaleMenu extends JMenuBase {

    static final ButtonGroup zoomGroup = new ButtonGroup();
    final boolean bShowExtendedMapSizes = true;

    public MapScaleMenu() {

        String nameString;

        makeSubMenu("menu_display_scale", true);
        int maxItems = (bShowExtendedMapSizes) ? 24 : 9;

        for (int i=1; i <= maxItems; i++) {
            nameString = i*2 + "km";
            if (i <= 10) {
                nameString+= " ( " + Math.pow(2,(i)) + "x )";
            }
            makeSimpleRadioButton(nameString, true, zoomGroup, i, i == 0);
        }
    }

    @SuppressWarnings("SameParameterValue")
    private void makeSimpleRadioButton(String text, Boolean enabled, ButtonGroup buttonGroup, int mnemonic, boolean isGroupDefault) {
        JRadioButtonMenuItem menuItem = new JRadioButtonMenuItem(text);
        menuItem.getAccessibleContext().setAccessibleDescription(text);
        menuItem.setEnabled(enabled);
        menuItem.addActionListener(this);
        menuItem.setMnemonic(mnemonic);
        if (buttonGroup != null) {
            buttonGroup.add(menuItem);
            if (isGroupDefault) {
                buttonGroup.setSelected(menuItem.getModel(), true);
            }
        }
        add(menuItem);
    }

    @Override
    public void actionPerformed(ActionEvent e) {
        //super.actionPerformed(e);
        int mnemonic = zoomGroup.getSelection().getMnemonic();
        if (bDebugMenuState) LOG.info("Selected = {}", mnemonic);
        setNewMapScale(mnemonic);
    }

    public static void updateMapScaleMenu(int selection) {
        if (bDebugMenuState) LOG.info("## updateMapScaleMenu ## newMapScale = {}km", selection * 2);
        for (Enumeration<AbstractButton> e = zoomGroup.getElements(); e.hasMoreElements();) {
            AbstractButton button = e.nextElement();
            if (button.getMnemonic() == selection) {
                button.setSelected(true);
                if (bDebugMenuState) LOG.info("## updateMapScaleMenu ## setting menu {} ('{}') to True", button.getMnemonic(), button.getText());
                break;
            }
        }
    }
}
