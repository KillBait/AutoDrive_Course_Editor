package AutoDriveEditor.GUI;

import javax.swing.*;
import java.awt.*;
import java.awt.event.WindowEvent;

import static AutoDriveEditor.GUI.GUIImages.tractorImage;
import static AutoDriveEditor.Locale.LocaleManager.localeString;

public class ConfigGUI extends JFrame {

    public static ConfigGUI configGUI;

    private static final int WIN_WIDTH = 400;
    private static final int WIN_HEIGHT = 300;

    public ConfigGUI() {
        super();

    }

    @Override
    public Dimension getPreferredSize() {
        return new Dimension(WIN_WIDTH, WIN_HEIGHT);
    }

    public static void createConfigGui(Component comp) {
        if (configGUI != null) configGUI.dispatchEvent(new WindowEvent(configGUI, WindowEvent.WINDOW_CLOSING));
        configGUI = new ConfigGUI();
        configGUI.setDefaultCloseOperation(JFrame.DISPOSE_ON_CLOSE);
        configGUI.setTitle(localeString.getString("panel_config_gui_title"));
        configGUI.setIconImage(tractorImage);
        configGUI.pack();
        configGUI.setLocationRelativeTo(comp);
        configGUI.setVisible(true);
    }
}
