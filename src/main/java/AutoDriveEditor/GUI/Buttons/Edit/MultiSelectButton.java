package AutoDriveEditor.GUI.Buttons.Edit;

import AutoDriveEditor.GUI.Buttons.CopyPasteBaseButton;

import javax.swing.*;
import java.awt.event.MouseAdapter;
import java.awt.event.MouseEvent;

import static AutoDriveEditor.GUI.TextPanel.showInTextArea;
import static AutoDriveEditor.Locale.LocaleManager.getLocaleString;
import static AutoDriveEditor.Managers.MultiSelectManager.setUseFreeformSelection;
import static AutoDriveEditor.Managers.MultiSelectManager.setUseRectangularSelection;
import static AutoDriveEditor.Utils.GUIUtils.makeImageToggleButton;
import static AutoDriveEditor.Utils.ImageUtils.loadImageIcon;
import static AutoDriveEditor.XMLConfig.EditorXML.bUseRectangularSelection;

public class MultiSelectButton extends CopyPasteBaseButton {

    private final int SELECTION_RECTANGLE = 0;
    @SuppressWarnings("FieldCanBeLocal")
    private final int SELECTION_FREEFORM = 1;

    private final ImageIcon rectangle;
    private final ImageIcon rectangle_selected;
    private final String rectangle_tooltip;
    private final ImageIcon freeform;
    private final ImageIcon freeform_selected;
    private final String freeform_tooltip;
    private int buttonState;

    public MultiSelectButton(JPanel panel) {

        rectangle = loadImageIcon("editor/buttons/select_rectangle.png");
        rectangle_selected = loadImageIcon("editor/buttons/select_rectangle_selected.png");
        rectangle_tooltip = "copypaste_select_rectangular_tooltip";
        freeform = loadImageIcon("editor/buttons/select_freeform.png");
        freeform_selected = loadImageIcon("editor/buttons/select_freeform_selected.png");
        freeform_tooltip = "copypaste_select_freeform_tooltip";

        String startupIcon, startupIconSelected, startupTooltip, startupAltText;
        if (bUseRectangularSelection) {
            startupIcon = "buttons/select_rectangle";
            startupIconSelected = "buttons/select_rectangle_selected";
            startupTooltip = rectangle_tooltip;
            startupAltText = "copypaste_select_rectangular_alt";
            buttonState = SELECTION_RECTANGLE;
        } else {
            startupIcon = "buttons/select_freeform";
            startupIconSelected = "buttons/select_freeform_selected";
            startupTooltip = freeform_tooltip;
            startupAltText = "options_default_selection_alt";
            buttonState = SELECTION_FREEFORM;
        }

        button = makeImageToggleButton(startupIcon, startupIconSelected, null, startupTooltip, startupAltText, panel, false, false, null, false, this);
        button.addMouseListener(new MouseAdapter() {
            @Override
            public void mouseClicked(MouseEvent e) {
                super.mouseClicked(e);
                if (button.isEnabled() && SwingUtilities.isRightMouseButton(e)) {
                    buttonState = 1 - buttonState;
                    if (buttonState == SELECTION_RECTANGLE) {
                        button.setIcon(rectangle);
                        button.setSelectedIcon(rectangle_selected);
                        button.setToolTipText(getLocaleString(rectangle_tooltip));
                        setUseRectangularSelection(true);
                        showInTextArea(getLocaleString(rectangle_tooltip), true, false);
                    } else {
                        button.setIcon(freeform);
                        button.setSelectedIcon(freeform_selected);
                        button.setToolTipText(getLocaleString(freeform_tooltip));
                        setUseFreeformSelection(true);
                        showInTextArea(getLocaleString(freeform_tooltip), true, false);

                    }

                }
            }
        });
    }

    @Override
    public String getButtonID() { return "AreaSelectButton"; }

    @Override
    public String getButtonAction() { return "ActionButton"; }

    @Override
    public String getButtonPanel() { return "Edit"; }

    @Override
    public String getInfoText() { return getLocaleString("copypaste_select_rectangular_tooltip"); }

    @Override
    public Boolean useMultiSelection() { return true; }
}
