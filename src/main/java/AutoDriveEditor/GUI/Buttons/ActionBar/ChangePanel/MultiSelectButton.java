package AutoDriveEditor.GUI.Buttons.ActionBar.ChangePanel;

import AutoDriveEditor.Classes.UI_Components.JToggleStateButton;
import AutoDriveEditor.Classes.UI_Components.ScaleAnimIcon;
import AutoDriveEditor.GUI.Buttons.BaseButton;
import AutoDriveEditor.RoadNetwork.MapNode;

import javax.swing.*;
import java.awt.event.MouseAdapter;
import java.awt.event.MouseEvent;

import static AutoDriveEditor.AutoDriveEditor.getMapPanel;
import static AutoDriveEditor.GUI.MapPanel.getNodeAtScreenPosition;
import static AutoDriveEditor.GUI.TextPanel.showInTextArea;
import static AutoDriveEditor.Locale.LocaleManager.getLocaleString;
import static AutoDriveEditor.Managers.IconManager.*;
import static AutoDriveEditor.Managers.MultiSelectManager.*;

public class MultiSelectButton extends BaseButton {

    @Override
    public String getButtonID() { return "MultiSelectButton"; }

    @Override
    public String getButtonAction() { return "ActionButton"; }

    @Override
    public String getButtonPanel() { return "Edit"; }

    @Override
    public String getInfoText() { return getLocaleString("actionbar_copypaste_select_rectangular_infotext"); }

    @Override
    public Boolean useMultiSelection() { return true; }

    @Override
    public Boolean usePanelEdgeScrolling() { return true; }

    private final String FREEFORM = "Freeform";
    private final String RECTANGULAR = "Rectangular";

    public MultiSelectButton(JPanel panel) {

        ScaleAnimIcon animRectangleSelectIcon = createScaleAnimIcon(SELECTION_ICON, false);
        JToggleStateButton selectButton = createAnimToggleStateButton(animRectangleSelectIcon, panel, null, null, false, false, this);
        selectButton.addState(SELECTION_FREEFORM_ICON, FREEFORM, "");
        selectButton.addState(SELECTION_ICON, RECTANGULAR, "");
        button = selectButton;

        button.addMouseListener(new MouseAdapter() {
            @Override
            public void mouseClicked(MouseEvent e) {
                if (button.isEnabled() && SwingUtilities.isRightMouseButton(e)) {
                    JToggleStateButton currentButton = (JToggleStateButton) button;;
                    if (currentButton.getCurrentStateName().equals(FREEFORM)) {
                        setUseFreeformSelection(true);
                    } else {
                        setUseRectangularSelection(true);
                    }
                    showInTextArea(currentButton.getCurrentStateTooltip(), true, false);
                    currentButton.nextState();
                    updateTooltip();
                }
            }
        });
    }

    @Override
    public void mousePressed(MouseEvent e) {
        MapNode selectedNode = getNodeAtScreenPosition(e.getX(), e.getY());
        if (selectedNode != null ) {
            if (selectedNode.isSelectable()) {
                if (!multiSelectList.contains(selectedNode)) {
                    addToMultiSelectList(selectedNode);
                    selectedNode.setSelected(true);
                } else {
                    removeFromMultiSelectList(selectedNode);
                    selectedNode.setSelected(false);
                }
                getMapPanel().repaint();
            }
        }
    }

    @Override
    public String buildToolTip() {
        String snapCurrent = useRectangularSelection ? getLocaleString("actionbar_copypaste_select_rectangular") : getLocaleString("actionbar_copypaste_select_freeform");
        String snapNext = !useFreeformSelection ? getLocaleString("actionbar_copypaste_select_freeform").toLowerCase() : getLocaleString("actionbar_copypaste_select_rectangular").toLowerCase();
        String statusLine = getLocaleString("actionbar_copypaste_select_tooltip").replace("{current}", snapCurrent).replace("{next}", snapNext);
        return String.format("<html>%s</html>", statusLine);
    }
}
