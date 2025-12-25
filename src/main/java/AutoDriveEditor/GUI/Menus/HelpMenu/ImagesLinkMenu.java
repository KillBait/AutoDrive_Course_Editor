package AutoDriveEditor.GUI.Menus.HelpMenu;

import AutoDriveEditor.GUI.Menus.JMenuItemBase;

import javax.swing.*;
import java.awt.event.ActionEvent;

import static AutoDriveEditor.AutoDriveEditor.*;
import static AutoDriveEditor.Classes.Util_Classes.HTMLUtils.createHyperLink;

public class ImagesLinkMenu extends JMenuItemBase {

    public ImagesLinkMenu() {
        makeMenuItem("menu_help_images", true);
    }

    @Override
    public void actionPerformed(ActionEvent e) {
        super.actionPerformed(e);

        String mainText = "<html><center>AutoDrive Course Editor version : " + COURSE_EDITOR_VERSION + " by <b>KillBait!</b><br><br>" +
                          "(Build info : " + COURSE_EDITOR_BUILD_INFO + ")<br><br><hr><br>" +
                          "I maintain a GitHub repository of map images that the editor<br>" +
                          "can automatically download and use when needed <a href='https://github.com/KillBait/AutoDrive_MapImages'><b>here</b></a><br><br>" +
                          "Do you want to createSetting your own images?, follow the link below<br><br>";
        String linkText = "<b>How to createSetting your own Images (GitHub Page)</b>";
        JEditorPane editorLink = createHyperLink(mainText,linkText, "https://github.com/KillBait/AutoDrive_MapImages/discussions/20");
        JOptionPane.showMessageDialog(editor, editorLink, "AutoDrive Editor Images", JOptionPane.PLAIN_MESSAGE);
    }
}
