package AutoDriveEditor.Managers;

import AutoDriveEditor.AutoDriveEditor;
import com.vdurmont.semver4j.Semver;
import org.w3c.dom.Document;
import org.w3c.dom.Element;
import org.xml.sax.SAXException;

import javax.xml.parsers.DocumentBuilder;
import javax.xml.parsers.DocumentBuilderFactory;
import javax.xml.parsers.ParserConfigurationException;
import java.io.BufferedInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.net.URL;
import java.net.URLConnection;

import static AutoDriveEditor.AutoDriveEditor.AUTODRIVE_INTERNAL_VERSION;
import static AutoDriveEditor.Utils.LoggerUtils.LOG;
import static AutoDriveEditor.Utils.XMLUtils.getTextValue;

public class VersionManager {

    public static void getVersionXML() {

        InputStream in = null;
        URL url = null;
        try {
            url = new URL("https://github.com/KillBait/AutoDrive_Course_Editor/raw/refactor/version.xml");
            URLConnection urlConnection = url.openConnection();
            in = new BufferedInputStream(urlConnection.getInputStream());
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
                Semver remoteSem = new Semver(remoteVersion);
                if (remoteSem.isGreaterThan(AUTODRIVE_INTERNAL_VERSION)) {
                    LOG.info("Update is available...Remote version {} is greater than {}", remoteVersion, AUTODRIVE_INTERNAL_VERSION);
                } else {
                    LOG.info("No update available...Remote version {} is lower than {}", remoteVersion, AUTODRIVE_INTERNAL_VERSION);
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



    public static void CheckVersion(String oldVersion, String newVersion) {
        isUpdateAvailable(oldVersion.split("\\."), newVersion.split("\\."));
    }


    public static boolean isUpdateAvailable(String[] userVersionSplit, String[] latestVersionSplit) {

        try {
            int majorUserVersion = Integer.parseInt(userVersionSplit[0]);
            int minorUserVersion = Integer.parseInt(userVersionSplit[1]);
            int patchUserVersion = Integer.parseInt(userVersionSplit[2]);

            int majorLatestVersion = Integer.parseInt(latestVersionSplit[0]);
            int minorLatestVersion = Integer.parseInt(latestVersionSplit[1]);
            int patchLatestVersion = Integer.parseInt(latestVersionSplit[2]);

            if (majorUserVersion <= majorLatestVersion) {
                if (majorUserVersion < majorLatestVersion) {
                    return true;
                } else {
                    if (minorUserVersion <= minorLatestVersion) {
                        if (minorUserVersion < minorLatestVersion) {
                            return true;
                        } else {
                            return patchUserVersion < patchLatestVersion;
                        }
                    }
                }
            }
        } catch (Exception ignored) {
            // Will be throw only if the versions pattern is different from "x.x.x" format
            // Will return false at the end
        }
        return false;
    }
}
