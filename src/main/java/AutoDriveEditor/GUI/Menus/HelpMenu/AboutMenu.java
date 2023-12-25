package AutoDriveEditor.GUI.Menus.HelpMenu;

import AutoDriveEditor.GUI.Menus.JMenuItemBase;

import javax.swing.*;
import java.awt.event.ActionEvent;

import static AutoDriveEditor.AutoDriveEditor.*;
import static AutoDriveEditor.Utils.HTMLUtils.createHyperLink;

public class AboutMenu extends JMenuItemBase {

    public AboutMenu() {
        makeMenuItem("menu_help_about", "menu_help_about_accstring", true);
    }

    @Override
    public void actionPerformed(ActionEvent e) {
        super.actionPerformed(e);

        String mainText = "<html><center>Editor version : " + COURSE_EDITOR_VERSION + " by <b>KillBait!</b><br><br>" +
                          "Build info : " + COURSE_EDITOR_BUILD_INFO + "<br><br><hr><br>" +
                          "<b><u>Many thanks to the following people</b></u><br><br>" +
                          "<a href='https://github.com/rhaetional'>@rhaetional (GitHub)</a> :- Code submission for Parking Destinations/out-of-bounds code & macOS fix<br>" +
                          "Everybody for the suggestions and bug reports :-)<br><br><br>" +
                          "<u><b>AutoDrive Development Team</b></u><br><br>" +
                          "<b>Stephan (Founder/Modder and Original Editor Author)</b><br><br>" +
                          "TyKonKet (Modder)<br>Oliver (Modder)<br>Axel (Co-Modder)<br>" +
                          "Aletheist (Co-Modder)<br>Willi (Supporter & Tester)<br>" +
                          "Iwan1803 (Community Manager & Supporter)<br><br>" +
                          "<b>Many thanks goto Stephan for allowing me to take over the development of this editor";
        String linkText = "<br><br>Visit AutoDrive Editor HomePage</b>";
        JEditorPane editorLink = createHyperLink(mainText,linkText, "https://github.com/KillBait/AutoDrive_Course_Editor");
        JOptionPane.showMessageDialog(editor, editorLink, "About AutoDrive Editor", JOptionPane.PLAIN_MESSAGE);
    }
}
