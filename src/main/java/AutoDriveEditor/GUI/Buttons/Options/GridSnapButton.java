package AutoDriveEditor.GUI.Buttons.Options;

import AutoDriveEditor.GUI.Buttons.OptionsBaseButton;

import javax.swing.*;
import java.awt.event.ActionEvent;
import java.awt.event.MouseEvent;

import static AutoDriveEditor.AutoDriveEditor.getMapPanel;
import static AutoDriveEditor.Locale.LocaleManager.getLocaleString;
import static AutoDriveEditor.Utils.GUIUtils.makeImageToggleButton;
import static AutoDriveEditor.Utils.ImageUtils.loadImageIcon;
import static AutoDriveEditor.XMLConfig.EditorXML.bGridSnap;
import static AutoDriveEditor.XMLConfig.EditorXML.bGridSnapSubs;

public class GridSnapButton extends OptionsBaseButton {

    private final ImageIcon snapIconSelected;
    private final ImageIcon subSnapIconSelected;


    public GridSnapButton(JPanel panel) {
        String tooltip;
        String icon;
        boolean isSelected;

        snapIconSelected = loadImageIcon("editor/buttons/gridsnaptoggle_selected.png");
        subSnapIconSelected = loadImageIcon("editor/buttons/gridsubtoggle_selected.png");

        if (bGridSnap) {
            if (bGridSnapSubs) {
                tooltip = "options_grid_snap_sub_enabled_tooltip";
                icon = "buttons/gridsubtoggle_selected";
            } else {
                tooltip = "options_grid_snap_enabled_tooltip";
                icon = "buttons/gridsnaptoggle_selected";
            }
            isSelected = true;
        } else {
            tooltip = "options_grid_snap_disabled_tooltip";
            icon = "buttons/gridsnaptoggle_selected";
            isSelected = false;
        }

        button = makeImageToggleButton("buttons/gridsnaptoggle", icon, null, tooltip, tooltip, panel, isSelected, false, null, false, this);
        InputMap iMap = getMapPanel().getInputMap(JComponent.WHEN_IN_FOCUSED_WINDOW);
        ActionMap aMap = getMapPanel().getActionMap();

        iMap.put(KeyStroke.getKeyStroke("S"), "GridSnapToggle");
        aMap.put("GridSnapToggle", new AbstractAction() {
            @Override
            public void actionPerformed(ActionEvent e) {
                bGridSnap = !bGridSnap;
                button.setSelected(bGridSnap);
                if (bGridSnap) {
                    if (bGridSnapSubs) {
                        button.setToolTipText(getLocaleString("options_grid_snap_sub_enabled_tooltip"));
                    } else {
                        button.setToolTipText(getLocaleString("options_grid_snap_enabled_tooltip"));
                    }
                } else {
                    button.setToolTipText(getLocaleString("options_grid_snap_disabled_tooltip"));
                }
                getMapPanel().repaint();
            }
        });

        iMap.put(KeyStroke.getKeyStroke("D"), "GridSubSnapToggle");
        aMap.put("GridSubSnapToggle", new AbstractAction() {
            @Override
            public void actionPerformed(ActionEvent e) {
                if (button.isSelected()) {
                    bGridSnapSubs = !bGridSnapSubs;
                    if (bGridSnapSubs) {
                        button.setSelectedIcon(subSnapIconSelected);
                        button.setToolTipText(getLocaleString("options_grid_snap_sub_enabled_tooltip"));
                    } else {
                        button.setSelectedIcon(snapIconSelected);
                        button.setToolTipText(getLocaleString("options_grid_snap_enabled_tooltip"));
                    }
                    getMapPanel().repaint();
                }
            }
        });

        button.setSelected(isSelected);
        button.addMouseListener(this);
}

    @Override
    public String getButtonID() { return "GridSnapToggleButton"; }

    @Override
    public Boolean ignoreButtonDeselect() { return true; }

    @Override
    public void actionPerformed(ActionEvent e) {
        super.actionPerformed(e);
        bGridSnap = !bGridSnap;
        if (button.isSelected()) {
            if (bGridSnapSubs) {
                button.setSelectedIcon(subSnapIconSelected);
                button.setToolTipText(getLocaleString("options_grid_snap_sub_enabled_tooltip"));
            } else {
                button.setSelectedIcon(snapIconSelected);
                button.setToolTipText(getLocaleString("options_grid_snap_enabled_tooltip"));
            }
        } else {
            button.setToolTipText(getLocaleString("options_grid_snap_disabled_tooltip"));
            //showInTextArea(getLocaleString("options_grid_snap_disabled_tooltip"), true, false);

        }
        getMapPanel().repaint();
    }

    @Override
    public void mouseClicked(MouseEvent e) {
        if (e.getButton() == MouseEvent.BUTTON3 && e.getSource() == button) {
            if (button.isEnabled() && button.isSelected()) {
                bGridSnapSubs = !bGridSnapSubs;
                if (bGridSnapSubs) {
                    button.setSelectedIcon(subSnapIconSelected);
                    button.setToolTipText(getLocaleString("options_grid_snap_sub_enabled_tooltip"));
                } else {
                    button.setSelectedIcon(snapIconSelected);
                    button.setToolTipText(getLocaleString("options_grid_snap_enabled_tooltip"));
                }
            }
            getMapPanel().repaint();
        }
    }
}
