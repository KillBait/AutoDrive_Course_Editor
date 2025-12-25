package AutoDriveEditor.Managers;

import AutoDriveEditor.Classes.UI_Components.PopoutJPanel;
import net.miginfocom.swing.MigLayout;

import javax.swing.*;
import java.util.HashMap;

public class PopupManager {

    private static final HashMap<AbstractButton, PopoutJPanel> popupButtonMap = new HashMap<>();

    public static PopoutJPanel makePopupPanel(AbstractButton button, String headerText) {
        return makePopupPanel(button, headerText, "insets 5, gap 5", PopoutJPanel.Justification.CENTER, true);
    }

    public static PopoutJPanel makePopupPanel(AbstractButton button, String headerText, String layout) {
        return makePopupPanel(button, headerText, "insets 5, gap 5", PopoutJPanel.Justification.CENTER, true);
    }

    public static PopoutJPanel makePopupPanel(AbstractButton button, String headerText, boolean bUseSeparator) {
        return makePopupPanel(button, headerText, "insets 5, gap 5", PopoutJPanel.Justification.CENTER, bUseSeparator);
    }

    public static PopoutJPanel makePopupPanel(AbstractButton button, String headerText, String layout, PopoutJPanel.Justification justify, boolean bUseSeparator) {
        PopoutJPanel popupJPanel = new PopoutJPanel(new MigLayout(layout), headerText, justify, bUseSeparator);
        popupJPanel.setAnchorComponent(button);
        popupJPanel.setAnimationSpeed(20);
        popupJPanel.setBorderWidth(4);
        popupJPanel.setBorderRadius(12);
        popupButtonMap.put(button, popupJPanel);
        return popupJPanel;
    }

    public static void showPopupPanel(AbstractButton button) {
        for (AbstractButton b : popupButtonMap.keySet()) {
            if (b == button) {
                popupButtonMap.get(b).playOpeningAnimation();
            } else {
                if (popupButtonMap.get(b).isVisible() && !popupButtonMap.get(b).isSticky()) popupButtonMap.get(b).playClosingAnimation();
            }
        }
    }

    public static void hidePopupPanel(AbstractButton button) {
        for (AbstractButton b : popupButtonMap.keySet()) {
            if (b == button) {
                popupButtonMap.get(b).playClosingAnimation();
            }
        }
    }
}
