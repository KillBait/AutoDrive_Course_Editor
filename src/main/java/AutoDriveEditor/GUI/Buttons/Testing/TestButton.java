package AutoDriveEditor.GUI.Buttons.Testing;

import AutoDriveEditor.GUI.Buttons.BaseButton;

import javax.swing.*;
import java.awt.event.ActionEvent;

import static AutoDriveEditor.Locale.LocaleManager.getLocaleString;
import static AutoDriveEditor.Utils.GUIUtils.makeImageToggleButton;

public class TestButton extends BaseButton {

    public static boolean bTestButton;

    // test overrides

    public TestButton(JPanel panel) {
        button = makeImageToggleButton( "buttons/unknown", "buttons/unknown_selected", null, "nodes_test_tooltip", "options_con_connect_alt", panel, bTestButton, true, null, false, this);
    }

    @Override
    public String getButtonID() { return "TestButton"; }

    @Override
    public String getButtonAction() { return "ActionButton"; }

    @Override
    public String getButtonPanel() { return "Test"; }

    @Override
    public String getInfoText() { return getLocaleString("nodes_test_tooltip"); }

    @Override
    public Boolean ignoreButtonDeselect() { return true; }

    @Override
    public void setSelected(boolean selected) {}


    @Override
    public void actionPerformed(ActionEvent e) {
        super.actionPerformed(e);
        bTestButton = !bTestButton;
        /*if (bTestButton) {
            if (bIsDebugEnabled) LOG.info("TestButton = {}", true);
            String mainText = "<html><center><b><u><font color=#FF0000>FATAL Sequence Error detected</b></u><br><br>" + "e.getMessage()" + "<br><br>" + "See AutoDriveEditor.log for more info";
            JOptionPane.showMessageDialog(editor, mainText, "FATAL Sequence Error!!", JOptionPane.ERROR_MESSAGE);
        }*/

        /*if (multiSelectList.size() > 0) {
            for (MapNode node: multiSelectList) {
                LOG.info("ID {}", node.id);
            }
            Collections.sort(multiSelectList, (nl1, nl2) -> Integer.compare(nl1.id, nl2.id));
            for (MapNode node: multiSelectList) {
                LOG.info("ID {}", node.id);
            }

        } else {
            LOG.info("list empty");
        }*/
    }
}
