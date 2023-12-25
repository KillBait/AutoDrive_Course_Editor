package AutoDriveEditor.GUI.Buttons.Options;

import AutoDriveEditor.GUI.Buttons.OptionsBaseButton;
import AutoDriveEditor.Utils.Classes.LabelNumberFilter;

import javax.swing.*;
import javax.swing.text.PlainDocument;
import java.awt.event.ActionEvent;
import java.awt.event.MouseEvent;

import static AutoDriveEditor.AutoDriveEditor.getMapPanel;
import static AutoDriveEditor.GUI.MapPanel.mapScale;
import static AutoDriveEditor.GUI.TextPanel.showInTextArea;
import static AutoDriveEditor.Locale.LocaleManager.getLocaleString;
import static AutoDriveEditor.Utils.GUIUtils.makeImageToggleButton;
import static AutoDriveEditor.XMLConfig.EditorXML.*;

public class GridDisplayButton extends OptionsBaseButton {

    public GridDisplayButton(JPanel panel) {
        String tooltip;
        boolean isSelected;

        if (bShowGrid) {
            tooltip = "options_grid_draw_enabled_tooltip";
            isSelected = true;
        } else {
            tooltip = "options_grid_draw_disabled_tooltip";
            isSelected = false;
        }
        button = makeImageToggleButton("buttons/gridtoggle", "buttons/gridtoggle_selected", null, tooltip, tooltip, panel, bShowGrid, false, null, false, this);
        InputMap iMap = getMapPanel().getInputMap(JComponent.WHEN_IN_FOCUSED_WINDOW);
        ActionMap aMap = getMapPanel().getActionMap();
        iMap.put(KeyStroke.getKeyStroke("G"), "GridToggle");
        aMap.put("GridToggle", new AbstractAction() {
            @Override
            public void actionPerformed(ActionEvent e) {
                bShowGrid = !bShowGrid;
                button.setSelected(bShowGrid);
                getMapPanel().repaint();
            }
        });
        button.setSelected(isSelected);
        button.addMouseListener(this);
    }

    @Override
    public String getButtonID() { return "GridToggleButton"; }

    @Override
    public void actionPerformed(ActionEvent e) {
        super.actionPerformed(e);
        bShowGrid = !bShowGrid;
        if (bShowGrid) {
            button.setToolTipText(getLocaleString("options_grid_draw_enabled_tooltip"));
            showInTextArea(getLocaleString("options_grid_draw_enabled_tooltip"), true, false);
            getMapPanel().repaint();
        } else {
            button.setToolTipText(getLocaleString("options_grid_draw_disabled_tooltip"));
            showInTextArea(getLocaleString("options_grid_draw_disabled_tooltip"), true, false);
            getMapPanel().repaint();
        }
    }

    @Override
    public void mouseClicked(MouseEvent e) {
        if (e.getButton() == MouseEvent.BUTTON3 && e.getSource() == button) {
            if (button.isEnabled() && button.isSelected()) {
                showGridSettingDialog();
            }
        } else {
            super.mouseClicked(e);
        }
    }

    //
    // Dialog for Grid Spacing
    //

    public void showGridSettingDialog() {

        JTextField cordX = new JTextField(String.valueOf(gridSpacingX));
        JLabel labelX = new JLabel(" ");
        PlainDocument docX = (PlainDocument) cordX.getDocument();
        docX.setDocumentFilter(new LabelNumberFilter(labelX, 1, 2048 * mapScale, true, false));

        JTextField cordY = new JTextField(String.valueOf(gridSpacingY));
        JLabel labelY = new JLabel(" ");
        PlainDocument docY = (PlainDocument) cordY.getDocument();
        docY.setDocumentFilter(new LabelNumberFilter(labelY, 1, 2048 * mapScale, true, false));

        JTextField subDivisions = new JTextField(String.valueOf(gridSubDivisions));
        JLabel subLabel = new JLabel(" ");
        PlainDocument docSub = (PlainDocument) subDivisions.getDocument();
        docSub.setDocumentFilter(new LabelNumberFilter(subLabel, 1, 50, false, false));

        Object[] inputFields = {getLocaleString("dialog_grid_set_x"), cordX, labelX,
                getLocaleString("dialog_grid_set_y"), cordY, labelY,
                getLocaleString("dialog_grid_set_subdivisions"), subDivisions, subLabel};

        int option = JOptionPane.showConfirmDialog( getMapPanel(), inputFields, ""+ getLocaleString("dialog_grid_title"), JOptionPane.OK_CANCEL_OPTION, JOptionPane.QUESTION_MESSAGE);

        if (option == JOptionPane.OK_OPTION) {
            setNewGridValues(Float.parseFloat(cordX.getText()), Float.parseFloat(cordY.getText()), Integer.parseInt(subDivisions.getText()));
            getMapPanel().repaint();
        }
    }
}
