package AutoDriveEditor.Managers;

import com.vdurmont.semver4j.Semver;
import org.w3c.dom.Document;
import org.w3c.dom.Element;
import org.xml.sax.SAXException;

import javax.swing.*;
import javax.swing.event.HyperlinkEvent;
import javax.swing.event.HyperlinkListener;
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
import static AutoDriveEditor.Utils.LoggerUtils.*;
import static AutoDriveEditor.Utils.XMLUtils.*;
import static AutoDriveEditor.XMLConfig.EditorXML.*;

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
                Semver localSem = new Semver(AUTODRIVE_INTERNAL_VERSION);
                if (localSem.isLowerThan(remoteVersion)) {
                    if (bShowUpdateMessage) {
                        LOG.info("Update is available... Current version {} is lower than remote version {}", AUTODRIVE_INTERNAL_VERSION, remoteVersion);
                        String mainText = "<center>AutoDrive Editor Update is available<br><br>Current Version v" + AUTODRIVE_INTERNAL_VERSION + "<br><br><b>New Version v" + remoteVersion;
                        String linkText = "<br><br>Visit AutoDrive Editor HomePage</b>";
                        JEditorPane link = createHyperLink(mainText,linkText, "https://github.com/KillBait/AutoDrive_Course_Editor");
                        JOptionPane.showMessageDialog(editor, link, "AutoDrive", JOptionPane.PLAIN_MESSAGE);
                    }
                    bShowUpdateMessage = false;
                } else if (localSem.isEqualTo(remoteVersion)){
                    LOG.info("No update available... Remote version {} matches current version", remoteVersion);
                    bShowUpdateMessage = true;
                } else {
                    // yes.... this is a "Back To The Future" reference.. :-P
                    LOG.info("Wait a minute, Doc. Are you telling me you built a time machine... current version {} is higher than remote version {}", AUTODRIVE_INTERNAL_VERSION, remoteVersion);
                }
            } catch (ParserConfigurationException e) {
                e.printStackTrace();
            } catch (IOException e) {
                e.printStackTrace();
            } catch (SAXException e) {
                e.printStackTrace();
            }
        }
    }

    private static JEditorPane createHyperLink(String text, String linkText, String URL) {
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
        ep.addHyperlinkListener(new HyperlinkListener()
        {
            @Override
            public void hyperlinkUpdate(HyperlinkEvent e)
            {
                if (e.getEventType().equals(HyperlinkEvent.EventType.ACTIVATED)) {
                    try {
                        Desktop.getDesktop().browse(e.getURL().toURI()); // roll your own link launcher or use Desktop if J6+
                    } catch (IOException | URISyntaxException ex) {
                        ex.printStackTrace();
                    }
                }
            }
        });
        ep.setEditable(false);
        ep.setBackground(label.getBackground());
        return ep;

    }
}
