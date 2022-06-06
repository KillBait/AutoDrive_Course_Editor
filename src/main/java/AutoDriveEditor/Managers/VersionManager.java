package AutoDriveEditor.Managers;

import com.vdurmont.semver4j.Semver;
import org.w3c.dom.Document;
import org.w3c.dom.Element;
import org.xml.sax.SAXException;

import javax.swing.*;
import javax.swing.event.HyperlinkEvent;
import javax.xml.parsers.DocumentBuilder;
import javax.xml.parsers.DocumentBuilderFactory;
import javax.xml.parsers.ParserConfigurationException;
import java.awt.*;
import java.io.BufferedInputStream;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.io.InputStream;
import java.net.URISyntaxException;
import java.net.URL;
import java.net.URLConnection;

import static AutoDriveEditor.AutoDriveEditor.*;
import static AutoDriveEditor.Utils.LoggerUtils.LOG;
import static AutoDriveEditor.Utils.XMLUtils.getTextValue;
import static AutoDriveEditor.XMLConfig.EditorXML.bShowUpdateMessage;

public class VersionManager {

    public static void updateCheck() {

        LOG.info("Connecting to GitHub for update check");

        InputStream in = null;
        URL url;
        try {
            url = new URL("https://github.com/KillBait/AutoDrive_Course_Editor/raw/master/version.xml");
            URLConnection urlConnection = url.openConnection();
            in = new BufferedInputStream(urlConnection.getInputStream());
        } catch (FileNotFoundException e) {
            LOG.info("Update file not found");
        } catch (IOException e) {
            e.printStackTrace();

        }

        if (in != null) {
            DocumentBuilderFactory docFactory = DocumentBuilderFactory.newInstance();
            try {
                DocumentBuilder docBuilder = docFactory.newDocumentBuilder();
                Document doc = docBuilder.parse(in);
                Element e = doc.getDocumentElement();

                String remoteVersion = getTextValue(null, e, "latest_version");
                Semver localSem = new Semver(COURSE_EDITOR_VERSION);
                if (localSem.isLowerThan(remoteVersion)) {
                    if (bShowUpdateMessage) {
                        LOG.info("Update is available... Current version {} is lower than remote version {}", COURSE_EDITOR_VERSION, remoteVersion);
                        String mainText = "<center>AutoDrive Editor update is available<br><br>Current Version v" + COURSE_EDITOR_VERSION + "<br><br><b>New Version v" + remoteVersion + "</b>";
                        String linkText = "<br><br>Visit AutoDrive Editor Release Page</b>";
                        JEditorPane link = createHyperLink(mainText,linkText, "https://github.com/KillBait/AutoDrive_Course_Editor/releases");
                        JOptionPane.showMessageDialog(editor, link, COURSE_EDITOR_NAME, JOptionPane.PLAIN_MESSAGE);
                    }
                    bShowUpdateMessage = false;
                } else if (localSem.isEqualTo(remoteVersion)){
                    LOG.info("No update available... Remote version {} matches current version", remoteVersion);
                    bShowUpdateMessage = true;
                } else {
                    // yes.... this is a "Back To The Future" reference.. :-P
                    LOG.info("Wait a minute, Doc. Are you telling me you built a time machine... current version {} is higher than remote version {}", COURSE_EDITOR_VERSION, remoteVersion);
                }
            } catch (ParserConfigurationException | IOException | SAXException e) {
                e.printStackTrace();
            }
        }
    }

    public static JEditorPane createHyperLink(String text, String linkText, String URL) {
        JLabel label = new JLabel();
        label.setHorizontalAlignment(SwingConstants.CENTER);
        Font font = label.getFont();

        // create some css from the label's font
        String style = "font-family:" + font.getFamily() + ";" + "font-weight:" + (font.isBold() ? "bold" : "normal") + ";" +
                "font-size:" + font.getSize() + "pt;" +
                "text-align: centre";

        // html content
        JEditorPane ep = new JEditorPane("text/html", "<html><body style=\"" + style + "\">" //
                + text + " <a href=\"" + URL + "\">" + linkText + "</a>" //
                + "</body></html>");

        // handle link events
        ep.addHyperlinkListener(e -> {
            if (e.getEventType().equals(HyperlinkEvent.EventType.ACTIVATED)) {
                try {
                    Desktop.getDesktop().browse(e.getURL().toURI()); // roll your own link launcher or use Desktop if J6+
                } catch (IOException | URISyntaxException ex) {
                    ex.printStackTrace();
                }
            }
        });
        ep.setEditable(false);
        ep.setBackground(label.getBackground());
        return ep;

    }
}
