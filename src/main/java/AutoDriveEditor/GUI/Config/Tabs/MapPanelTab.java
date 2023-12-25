package AutoDriveEditor.GUI.Config.Tabs;

import javax.swing.*;
import javax.swing.border.EmptyBorder;
import java.awt.*;
import java.awt.event.ItemEvent;

import static AutoDriveEditor.Locale.LocaleManager.getLocaleString;
import static AutoDriveEditor.Utils.GUIUtils.makeCheckBox;
import static AutoDriveEditor.XMLConfig.EditorXML.*;

public class MapPanelTab extends JPanel {

    public MapPanelTab() {

        setLayout(new GridLayout(6,2,0,5));

        // Use online images checkbox

        JLabel enableOnlineImagesLabel = new JLabel(getLocaleString("panel_config_tab_mappanel_online_images") + "  ", JLabel.TRAILING);
        JCheckBox cbEnableOnlineImages = makeCheckBox(enableOnlineImagesLabel, getLocaleString("panel_config_tab_mappanel_online_images"), null, true, bUseOnlineMapImages);
        cbEnableOnlineImages.addItemListener(e -> bUseOnlineMapImages = e.getStateChange() == ItemEvent.SELECTED);
        enableOnlineImagesLabel.setLabelFor(cbEnableOnlineImages);
        add(enableOnlineImagesLabel);
        add(cbEnableOnlineImages);

        // Middle mouse button move checkbox

        JLabel middleMouseMoveLabel = new JLabel(getLocaleString("panel_config_tab_mappanel_middle_mouse_move") + "  ", JLabel.TRAILING);
        JCheckBox cbMiddleMouseMove = makeCheckBox(middleMouseMoveLabel, getLocaleString("panel_config_tab_mappanel_middle_mouse_move"), null, true, bMiddleMouseMove);
        cbMiddleMouseMove.addItemListener(e -> bMiddleMouseMove = e.getStateChange() == ItemEvent.SELECTED);
        middleMouseMoveLabel.setLabelFor(cbMiddleMouseMove);
        add(middleMouseMoveLabel);
        add(cbMiddleMouseMove);

        // Lock toolbar checkbox

        JLabel lockToolbarLabel = new JLabel(getLocaleString("panel_config_tab_mappanel_lock_toolbar") + "  ", JLabel.TRAILING);
        JCheckBox cbLockToolbar = makeCheckBox(lockToolbarLabel, getLocaleString("panel_config_tab_mappanel_lock_toolbar"), null, true, bLockToolbarPosition);
        cbLockToolbar.addItemListener(e -> bLockToolbarPosition = e.getStateChange() == ItemEvent.SELECTED);
        lockToolbarLabel.setLabelFor(cbLockToolbar);
        add(lockToolbarLabel);
        add(cbLockToolbar);

        cbLockToolbar.addPropertyChangeListener(evt -> {
            if ("UNCHECKEDHOT".equals(evt.getNewValue())) {
                bLockToolbarPosition = false;
            } else if ("CHECKEDHOT".equals(evt.getNewValue())) {
                bLockToolbarPosition = true;
            }
        });

        // Zoom interpolation checkbox

        JLabel smoothZoomLabel = new JLabel(getLocaleString("panel_config_tab_mappanel_interpolate_zoom") + "  ", JLabel.TRAILING);
        JCheckBox cbSmoothZoom = makeCheckBox(middleMouseMoveLabel, getLocaleString("panel_config_tab_mappanel_interpolate_zoom"), null, true, bMiddleMouseMove);
        cbSmoothZoom.addItemListener(e -> bInterpolateZoom = e.getStateChange() == ItemEvent.SELECTED);
        smoothZoomLabel.setLabelFor(cbSmoothZoom);
        add(smoothZoomLabel);
        add(cbSmoothZoom);

        // Maximum zoom level text field and buttons

        JPanel zoomPanel = new JPanel();
        zoomPanel.setBorder(new EmptyBorder(new Insets(3,0,0,0)));
        zoomPanel.setLayout(new FlowLayout(FlowLayout.LEADING));

        JLabel maxZoomLabel = new JLabel(getLocaleString("panel_config_tab_mappanel_max_zoom") + "  ", JLabel.TRAILING);

        SpinnerModel spinnerValue = new SpinnerNumberModel(maxZoomLevel, 1, 250, 1);
        JSpinner spinner = new JSpinner(spinnerValue);
        spinner.setPreferredSize(new Dimension(50,20));

        // TODO fix changing zoomLevel to below current level locking zooming

        spinner.addChangeListener(e -> {
            JSpinner spinner1 = (JSpinner) e.getSource();
            maxZoomLevel = (int) spinner1.getValue();
        });

        add(maxZoomLabel);
        zoomPanel.add(spinner);
        add(zoomPanel);
    }
}
