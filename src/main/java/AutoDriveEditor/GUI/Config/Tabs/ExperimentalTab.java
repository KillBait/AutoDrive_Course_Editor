package AutoDriveEditor.GUI.Config.Tabs;

import javax.swing.*;
import java.awt.*;
import java.awt.event.ItemEvent;

import static AutoDriveEditor.AutoDriveEditor.getMapPanel;
import static AutoDriveEditor.Locale.LocaleManager.getLocaleString;
import static AutoDriveEditor.Utils.GUIUtils.makeCheckBox;
import static AutoDriveEditor.XMLConfig.EditorXML.bShowSelectionBounds;

public class ExperimentalTab extends JPanel {
    public ExperimentalTab() {

        setLayout(new GridLayout(1,2,0,5));

        // Show Selection Bounds checkbox

        JLabel showSelectionBoundsLabel = new JLabel(getLocaleString("panel_config_tab_mappanel_show_selection_bounds") + "  ", JLabel.TRAILING);
        JCheckBox cbShowSelectionBounds = makeCheckBox(showSelectionBoundsLabel, "ShowSelectionBounds", null, true, bShowSelectionBounds);
        cbShowSelectionBounds.addItemListener(e -> {
            bShowSelectionBounds = e.getStateChange() == ItemEvent.SELECTED;
            getMapPanel().repaint();
        });
        showSelectionBoundsLabel.setLabelFor(cbShowSelectionBounds);
        add(showSelectionBoundsLabel);
        add(cbShowSelectionBounds);
    }

}
