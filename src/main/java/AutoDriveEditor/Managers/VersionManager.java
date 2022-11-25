package AutoDriveEditor.Managers;

import com.vdurmont.semver4j.Semver;
import org.w3c.dom.Document;
import org.w3c.dom.Element;
import org.xml.sax.SAXException;

import javax.swing.*;
import javax.swing.border.EtchedBorder;
import javax.swing.event.HyperlinkEvent;
import javax.xml.parsers.DocumentBuilder;
import javax.xml.parsers.DocumentBuilderFactory;
import javax.xml.parsers.ParserConfigurationException;
import java.awt.*;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.io.InputStream;
import java.net.HttpURLConnection;
import java.net.URISyntaxException;
import java.net.URL;

import static AutoDriveEditor.AutoDriveEditor.COURSE_EDITOR_VERSION;
import static AutoDriveEditor.AutoDriveEditor.editor;
import static AutoDriveEditor.GUI.GUIImages.getUpdateIcon;
import static AutoDriveEditor.Utils.LoggerUtils.LOG;
import static AutoDriveEditor.Utils.XMLUtils.getTextValue;
import static AutoDriveEditor.XMLConfig.EditorXML.bShowUpdateMessage;

public class VersionManager {

    public static void updateCheck() {

        InputStream in = null;
        URL url;

        LOG.info("Connecting to GitHub for update check");
        try {
            url = new URL("https://github.com/KillBait/AutoDrive_Course_Editor/raw/master/version.xml");
            HttpURLConnection urlConnection = (HttpURLConnection) url.openConnection();
            HttpURLConnection.setFollowRedirects(true);
            urlConnection.setConnectTimeout(5*1000);
            urlConnection.setRequestMethod("GET");
            urlConnection.setRequestProperty("User-Agent", "Mozilla/5.0 (Windows; U; Windows NT 6.0; en-US; rv:1.9.1.2) Gecko/20090729 Firefox/3.5.2 (.NET CLR 3.5.30729)");
            urlConnection.connect();
            in = urlConnection.getInputStream();
        } catch (FileNotFoundException e) {
            LOG.info("Update file not found");
        } catch (IOException e) {
            LOG.info("## Update check failed ##");
            e.printStackTrace();
        }

        try {
            DocumentBuilderFactory docFactory = DocumentBuilderFactory.newInstance();
            DocumentBuilder docBuilder = docFactory.newDocumentBuilder();
            Document doc = docBuilder.parse(in);
            Element e = doc.getDocumentElement();
            if (in != null) in.close();


            String remoteVersion = getTextValue(null, e, "latest_version");
            String updateHTML = getTextValue(null, e, "version_notes");
            Semver localSem = new Semver(COURSE_EDITOR_VERSION);
            if (localSem.isLowerThan(remoteVersion)) {
                if (bShowUpdateMessage) {
                    LOG.info("Update is available... remote version {} is higher than local version {}", remoteVersion, COURSE_EDITOR_VERSION);
                    JTextPane textPane = new JTextPane();
                    textPane.setContentType("text/html");
                    textPane.setText(updateHTML);
                    textPane.setEditable(false);
                    textPane.addHyperlinkListener(e1 -> {
                        if(e1.getEventType() == HyperlinkEvent.EventType.ACTIVATED) {
                            if(Desktop.isDesktopSupported()) {
                                try {
                                    Desktop.getDesktop().browse(e1.getURL().toURI());
                                } catch (IOException | URISyntaxException ex) {
                                    throw new RuntimeException(ex);
                                }
                            }
                        }
                    });

                    JScrollPane scrollPane = new JScrollPane(textPane);
                    scrollPane.setVerticalScrollBarPolicy(JScrollPane.VERTICAL_SCROLLBAR_AS_NEEDED);
                    scrollPane.setHorizontalScrollBarPolicy(JScrollPane.HORIZONTAL_SCROLLBAR_AS_NEEDED);
                    scrollPane.setBorder(BorderFactory.createEtchedBorder(EtchedBorder.LOWERED));
                    scrollPane.setPreferredSize(new Dimension(600,200));

                    // ScrollPane will default to the bottom of the text.
                    //
                    // This behaviour is triggered if you create the scrollbar at the top and set the text
                    // to display, as it displays the text, it moves the scrollbar position back to the bottom
                    // again:/

                    // A not so friendly fix is to use invokeLater() to delay setting the scrollbar position,
                    // that way it gives the JTextPane time to display the text and update the JScrollPane

                    javax.swing.SwingUtilities.invokeLater(() -> scrollPane.getVerticalScrollBar().setValue(0));


                    Object[] inputFields = new Object[]{"<html><b>A new version of AutoDrive Editor is available!</b><br><br>",
                            "<html>GitHub Version <b>v" + remoteVersion + "</b> -- you are running v" + COURSE_EDITOR_VERSION + "<br><br>",
                            "<html><b>Release Notes</b>",scrollPane, " "};

                    JOptionPane.showMessageDialog(editor, inputFields, "Version Update", JOptionPane.INFORMATION_MESSAGE, getUpdateIcon());
                }
                bShowUpdateMessage = false;
            } else if (localSem.isEqualTo(remoteVersion)){
                LOG.info("No update available... Remote version {} matches local version", remoteVersion);
                bShowUpdateMessage = true;
            } else {
                // yes.... this is a "Back To The Future" reference.. :-P
                LOG.info("Wait a minute, Doc. Are you telling me you built a time machine... local version {} is higher than remote version {}", COURSE_EDITOR_VERSION, remoteVersion);
            }
        } catch (ParserConfigurationException | IOException | SAXException e) {
            e.printStackTrace();
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
