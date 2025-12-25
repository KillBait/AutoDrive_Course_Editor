package AutoDriveEditor.GUI.Buttons.ActionBar.ConfigPanel;


import AutoDriveEditor.Classes.UI_Components.DropdownToggleButton;
import AutoDriveEditor.Classes.UI_Components.MapInfoLabel;
import AutoDriveEditor.Classes.UI_Components.PopoutJPanel;
import AutoDriveEditor.Classes.UI_Components.ScaleAnimIcon;
import AutoDriveEditor.GUI.Buttons.OptionsBaseButton;
import AutoDriveEditor.Managers.PopupManager;
import AutoDriveEditor.RoadNetwork.RoadMap;

import javax.swing.*;
import java.awt.event.ActionEvent;

import static AutoDriveEditor.AutoDriveEditor.getMapPanel;
import static AutoDriveEditor.Classes.Util_Classes.LoggerUtils.LOG;
import static AutoDriveEditor.GUI.MapImage.*;
import static AutoDriveEditor.GUI.MapPanel.forceMapImageRedraw;
import static AutoDriveEditor.GUI.MapPanel.roadMap;
import static AutoDriveEditor.Managers.IconManager.*;
import static javax.swing.JLayeredPane.POPUP_LAYER;

public class MapInfoButton extends OptionsBaseButton{

    private static PopoutJPanel popoutJPanel;
    private static MapInfoLabel mapImageLabel;
    private static MapInfoLabel heightmapImageLabel;
    private static JLabel mapScaleLabel;
    private static JLabel mapNameLabel;
    private static AbstractButton buttonRef;
    private static ScaleAnimIcon animGlobeIcon;

    @Override
    public String getButtonID() { return "MapInfoButton"; }

    @Override
    public String getButtonAction() { return "ActionBarButton"; }

    @Override
    public String getButtonPanel() { return "ActionBar"; }

    @Override
    public void actionPerformed(ActionEvent e) {
        super.actionPerformed(e);
        if (button.isSelected()) {
            if (heightMapImage != null ) {
                setMapPanelImage(heightMapImage, true);
            } else {
                LOG.info("heightMapImage = null");
            }
        } else {
            if (pdaImage != null ) {
                setMapPanelImage(pdaImage, false);
            } else {
                LOG.info("map image is NULL, setting to default mapImage");
                useDefaultMapImage();
            }
        }
        forceMapImageRedraw();
        updateTooltip();
    }

    @Override
    public void onConfigChange() {
        updateTooltip();
    }

    public MapInfoButton(JPanel panel) {
        animGlobeIcon = createToggleScalingAnimatedIcon(GLOBE_MAP_ICON, GLOBE_HEIGHTMAP_ICON, false, 20, 20, 1.0f, .25f, 100);
        button = createAnimDropdownToggleButton(animGlobeIcon, panel, null, null, false, false, this);
        buttonRef = button;
        setupPopupPanel();

        ((DropdownToggleButton) button).addDropdownButtonListener(event -> {
            if (popoutJPanel.isVisible()) {
                PopupManager.hidePopupPanel(button);
            } else {
                if (roadMap != null) {
                    PopupManager.showPopupPanel(button);
                }
            }
        });
    }

    private void setupPopupPanel() {
        // createSetting the autosave popup Panel
        popoutJPanel = PopupManager.makePopupPanel(button, "actionbar_options_mapinfo_popup_tooltip", false);
        getMapPanel().add(popoutJPanel, POPUP_LAYER);

        popoutJPanel.addPropertyChangeListener(e -> {
            if (e.getPropertyName().equals("UI") || e.getPropertyName().equals("foreground") || e.getPropertyName().equals("gridcolor")) {
                mapImageLabel.updateColours();
                heightmapImageLabel.updateColours();
                updateTooltip();
            }
        });

        mapNameLabel = new JLabel("Name : " + RoadMap.mapName);
        popoutJPanel.add(mapNameLabel, "align left, span, wrap");
        popoutJPanel.add(new JSeparator(), "align center, grow, wrap");

        mapImageLabel = new MapInfoLabel("Map Image", SwingConstants.LEFT);
        popoutJPanel.add(mapImageLabel, "align left, span, wrap");

        heightmapImageLabel = new MapInfoLabel("HeightMap");
        popoutJPanel.add(heightmapImageLabel, "align left, span, wrap");

        mapScaleLabel = new JLabel("Map Scale : ");
        popoutJPanel.add(mapScaleLabel, "align left, span, wrap");

        popoutJPanel.add(new JSeparator(), "align center, grow");
    }

    public static void setMapImageLabel(MapInfoLabel.LabelStatus status) {
        if (mapImageLabel != null && status != null)  mapImageLabel.setStatus(status);
        popoutJPanel.setSize(popoutJPanel.getPreferredSize());
        setIcon(status);
    }

    public static void setHeightmapImageLabel(MapInfoLabel.LabelStatus status) {
        if (heightmapImageLabel != null && status != null)  heightmapImageLabel.setStatus(status);
        popoutJPanel.setSize(popoutJPanel.getPreferredSize());
        setIcon(status);
    }

    public static void setCurrentMapScaleLabel(String text) {
        mapScaleLabel.setText("<html>Map Scale : <b>" + text + "</b></html>");
    }

    public static void setCurrentMapNameLabel(String mapName) {
        mapNameLabel.setText("<html>Name : <b>" + mapName + "</b></html>");
    }

    public static void setIcon(MapInfoLabel.LabelStatus status) {
        if (status != null) {
            int max = Math.max(mapImageLabel.getStatus().getWarnValue(), heightmapImageLabel.getStatus().getWarnValue());
            switch (max) {
                case 0:
                case 1:
                    animGlobeIcon.setIcon(getSVGIcon(GLOBE_MAP_ICON));
                    break;
                case 2:
                    animGlobeIcon.setIcon(getSVGIcon(GLOBE_IMPORT_ICON));
                    break;
                case 3:
                    animGlobeIcon.setIcon(getSVGIcon(GLOBE_MANUAL_ICON));
                    break;
                case 4:
                    animGlobeIcon.setIcon(getSVGIcon(GLOBE_ERROR_ICON));
                    break;
            }
            buttonRef.repaint();
        }
    }

    @Override
    public String buildToolTip() {
        String mapImageText = mapImageLabel.getTooltipText();
        String heightmapImageText = heightmapImageLabel.getTooltipText();
        String mapScaleText = mapScaleLabel.getText();
        if (RoadMap.isMapLoaded()) {
            return String.format("<html>%s<br>%s<br>%s</html>", mapImageText, heightmapImageText, mapScaleText);
        }
        return String.format("<html>%s<br>%s</html>", mapImageText, heightmapImageText);
    }
}



