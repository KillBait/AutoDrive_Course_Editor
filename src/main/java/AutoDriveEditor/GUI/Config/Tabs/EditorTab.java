package AutoDriveEditor.GUI.Config.Tabs;

import com.formdev.flatlaf.extras.FlatSVGIcon;
import net.miginfocom.swing.MigLayout;

import javax.swing.*;
import java.awt.*;
import java.awt.event.ItemEvent;

import static AutoDriveEditor.Classes.Util_Classes.GUIUtils.makeCheckBox;
import static AutoDriveEditor.Locale.LocaleManager.getLocaleString;
import static AutoDriveEditor.Managers.IconManager.*;
import static AutoDriveEditor.XMLConfig.EditorXML.*;

public class EditorTab extends JPanel {

    public EditorTab() {

        setLayout(new MigLayout("center, insets 10 30 0 30", "[]", "[]15[]20[]20[]"));

        // Network Label

        JLabel onlineLabel = new JLabel(getLocaleString("panel_config_tab_editor_network"));
        add(onlineLabel, "center, wrap");

        // Network panel

        JPanel onlinePanel = new JPanel(new MigLayout("center"));
        onlinePanel.putClientProperty("FlatLaf.style", "border: 10,10,10,10,@disabledForeground,1,16; background: darken($Panel.background,5%)");
        FlatSVGIcon downloadIcon = getSVGIcon(DOWNLOAD_ICON);
        downloadIcon.setColorFilter(FlatSVGIcon.ColorFilter.getInstance()
                .add( new Color(0,92,255), null, new Color(68,143,255))
                .add( new Color(0,103,20), null, new Color(0,193,38)));
        JLabel networkIconLabel = new JLabel(downloadIcon);

        // Use online images checkbox

        JLabel enableOnlineImagesLabel = new JLabel(getLocaleString("panel_config_tab_editor_use_online_images"), JLabel.TRAILING);
        JCheckBox cbEnableOnlineImages = makeCheckBox(enableOnlineImagesLabel, getLocaleString("panel_config_tab_editor_use_online_images"), null, true, bUseOnlineMapImages);
        cbEnableOnlineImages.addItemListener(e -> bUseOnlineMapImages = e.getStateChange() == ItemEvent.SELECTED);
        enableOnlineImagesLabel.setLabelFor(cbEnableOnlineImages);
        onlinePanel.add(networkIconLabel, "gap 0 10 0 0");
        onlinePanel.add(enableOnlineImagesLabel);
        onlinePanel.add(cbEnableOnlineImages);

        add(onlinePanel, "center, grow, wrap");

        // Map Movement Panel

        JLabel mapLabel = new JLabel(getLocaleString("panel_config_tab_editor_map"));
        JPanel mapPanel = new JPanel(new MigLayout("center"));
        mapPanel.putClientProperty("FlatLaf.style", "border: 10,10,10,10,@disabledForeground,1,16; background: darken($Panel.background,5%)");

        // Middle mouse button move checkbox

        FlatSVGIcon monitorIcon = getSVGIcon(MONITOR_ICON);
        addDarkColourFilter(true, monitorIcon, new Color(70,70,70), new Color(150,150,150));
        JLabel monitorIconLabel = new JLabel(monitorIcon);
        JLabel middleMouseMoveLabel = new JLabel(getLocaleString("panel_config_tab_editor_middle_mouse_move"), JLabel.TRAILING);
        JCheckBox cbMiddleMouseMove = makeCheckBox(middleMouseMoveLabel, getLocaleString("panel_config_tab_editor_middle_mouse_move"), null, true, bMiddleMouseMove);
        cbMiddleMouseMove.addItemListener(e -> bMiddleMouseMove = e.getStateChange() == ItemEvent.SELECTED);
        middleMouseMoveLabel.setLabelFor(cbMiddleMouseMove);
        mapPanel.add(monitorIconLabel, "span 1 3, gap 0 20 0 0");
        mapPanel.add(middleMouseMoveLabel);
        mapPanel.add(cbMiddleMouseMove, "wrap");

        // Lock toolbar checkbox

//        JLabel lockToolbarLabel = new JLabel(getLocaleString("panel_config_tab_mappanel_lock_toolbar") + "  ", JLabel.TRAILING);
//        JCheckBox cbLockToolbar = makeCheckBox(lockToolbarLabel, getLocaleString("panel_config_tab_mappanel_lock_toolbar"), null, true, bLockToolbarPosition);
//        cbLockToolbar.addItemListener(e -> bLockToolbarPosition = e.getStateChange() == ItemEvent.SELECTED);
//        lockToolbarLabel.setLabelFor(cbLockToolbar);
//        mapPanel.add(lockToolbarLabel);
//        mapPanel.add(cbLockToolbar, "wrap");
//
//        cbLockToolbar.addPropertyChangeListener(evt -> {
//            if ("UNCHECKEDHOT".equals(evt.getNewValue())) {
//                bLockToolbarPosition = false;
//            } else if ("CHECKEDHOT".equals(evt.getNewValue())) {
//                bLockToolbarPosition = true;
//            }
//        });

        // Zoom interpolation checkbox

        JLabel smoothZoomLabel = new JLabel(getLocaleString("panel_config_tab_editor_interpolate_zoom"), JLabel.TRAILING);
        JCheckBox cbSmoothZoom = makeCheckBox(middleMouseMoveLabel, getLocaleString("panel_config_tab_editor_interpolate_zoom"), null, true, bInterpolateZoom);
        cbSmoothZoom.addItemListener(e -> bInterpolateZoom = e.getStateChange() == ItemEvent.SELECTED);
        smoothZoomLabel.setLabelFor(cbSmoothZoom);
        mapPanel.add(smoothZoomLabel);
        mapPanel.add(cbSmoothZoom, "wrap");

        // Maximum zoom level text field and buttons

//        JPanel zoomPanel = new JPanel();
//        zoomPanel.setBorder(new EmptyBorder(new Insets(3,0,0,0)));
//        zoomPanel.setLayout(new FlowLayout(FlowLayout.LEADING));

        JLabel maxZoomLabel = new JLabel(getLocaleString("panel_config_tab_editor_max_zoom"), JLabel.TRAILING);

        SpinnerModel spinnerValue = new SpinnerNumberModel(maxZoomLevel, 1, 250, 1);
        JSpinner spinner = new JSpinner(spinnerValue);
        spinner.setPreferredSize(new Dimension(50,20));

        spinner.addChangeListener(e -> {
            JSpinner spinner1 = (JSpinner) e.getSource();
            maxZoomLevel = (int) spinner1.getValue();
        });

        mapPanel.add(maxZoomLabel);
        mapPanel.add(spinner);
        add(mapLabel, "center, wrap");
        add(mapPanel, "center, grow, wrap");
    }
}
