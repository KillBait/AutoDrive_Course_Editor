package AutoDriveEditor.GUI.Menus.HelpMenu;

import AutoDriveEditor.GUI.Menus.JMenuItemBase;
import com.formdev.flatlaf.extras.FlatSVGIcon;
import net.miginfocom.swing.MigLayout;

import javax.swing.*;
import java.awt.event.ActionEvent;
import java.awt.image.BufferedImage;

import static AutoDriveEditor.AutoDriveEditor.*;
import static AutoDriveEditor.Managers.IconManager.LOGO;
import static AutoDriveEditor.Managers.IconManager.getSVGIcon;

public class AboutMenu extends JMenuItemBase {

    BufferedImage image;
    FlatSVGIcon logo;

    public AboutMenu() {
        makeMenuItem("menu_help_about", true);
        logo = getSVGIcon(LOGO);
    }

    @Override
    public void actionPerformed(ActionEvent e) { showAbout(); }

    private void showAbout() {
        // Create a new JFrame for the About window
        JFrame aboutFrame = new JFrame("About AutoDrive Editor");
        aboutFrame.setDefaultCloseOperation(JFrame.DISPOSE_ON_CLOSE);
        aboutFrame.setResizable(false);

        // create main panel
        JPanel aboutPanel = new JPanel();
        aboutPanel.setLayout(new MigLayout("wrap 1, align center", "[]", "[]0[]0[]"));
        aboutFrame.add(aboutPanel);

        // Create a panel to hold the content
        JLabel imageLabel = new JLabel(logo);
        imageLabel.setAlignmentX(JLabel.CENTER_ALIGNMENT);
        //imagePanel.add(imageLabel, "center, gapbottom " + logo.getHeight()/4 +", wrap");
        aboutPanel.add(imageLabel, "center, wrap");

        // Add text
        JPanel textPanel = new JPanel(new MigLayout());
        JLabel mainText = new JLabel("<html><center>Version : " + COURSE_EDITOR_VERSION + " by <b>KillBait!</b><br><br>" +
                "( Build info : " + COURSE_EDITOR_BUILD_INFO + " )<br><br><hr><br>" +
                "<b><u>Many thanks to the following people for Code Submissions</b></u><br><br>" +
                "<a href='https://github.com/rhaetional'>@rhaetional (GitHub)</a> :- Parking Destinations/out-of-bounds code & macOS fix<br>" +
                "<a href='https://github.com/whitevamp'>@whitevamp (GitHub)</a> :- Additional curve types/control nodes code<br>" +
                "<br>Thanks to everybody for the suggestions and bug reports :-)<br><br>" +
                "<u><b>AutoDrive Development</b></u><br><br>" +
                "Stephan (Founder/Modder and Original Editor Author)<br><br>" +
                "<b><i><u>Many thanks goto Stephan for allowing me to take over development of the editor</u></i></b></html>");
        mainText.setAlignmentX(JLabel.CENTER_ALIGNMENT);
        textPanel.add(mainText, "center, gaptop 0, wrap");
        aboutPanel.add(textPanel);

        // Add panel to the frame
        aboutFrame.pack();
        aboutFrame.setLocationRelativeTo(editor);
        aboutFrame.setVisible(true);
    }
}
