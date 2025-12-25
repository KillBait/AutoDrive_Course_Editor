package AutoDriveEditor.Locale;

import org.w3c.dom.Element;
import org.w3c.dom.Node;
import org.w3c.dom.NodeList;

import java.io.File;
import java.net.MalformedURLException;
import java.net.URL;
import java.net.URLClassLoader;
import java.nio.file.Paths;
import java.util.*;

import static AutoDriveEditor.Classes.Util_Classes.LoggerUtils.LOG;

/**
 * Manages the localization for the AutoDriveEditor application.
 */
public class LocaleManager {

    // ResourceBundle to hold the localized strings.
    public static ResourceBundle localeString;
    // Locale object to hold the current locale.
    public static Locale locale;

    /**
     * Retrieves the appropriate ResourceBundle based on the system's default locale.
     * If the bundle is not found, it attempts to load it from predefined paths.
     *
     * @return ResourceBundle for the current locale.
     */
    private static ResourceBundle getLocale() {
        LOG.info(">> System Locale: {}", Locale.getDefault());

        try {
            return ResourceBundle.getBundle("locale.AutoDriveEditor", Locale.getDefault());
        } catch (Exception e) {
            LOG.info(">> 'AutoDriveEditor_{}.properties' not found. Looking in folders", Locale.getDefault());
            String[] paths = {"./", "./locale/", "./src/locale/", "./src/main/resources/locale/"};

            for (String path : paths) {
                String localePath = path + "AutoDriveEditor_" + Locale.getDefault() + ".properties";
                if (Paths.get(localePath).toFile().exists()) {
                    LOG.info(">> Found External locale file: {}", localePath);
                    return loadExternalLocale(path);
                } else {
                    LOG.info(">> No locale found at: {}", localePath);
                }
            }

            LOG.info(">> Locale file not found. Using default locale {}", new Locale("en", "US"));
            return ResourceBundle.getBundle("locale.AutoDriveEditor", new Locale("en", "US"));
        }
    }

    public static List<String[]> getLocalizedText(Element titleElement) {
        List<String[]> localizedTexts = new ArrayList<>();

        if (titleElement != null) {
            NodeList childNodes = titleElement.getChildNodes();
            for (int i = 0; i < childNodes.getLength(); i++) {
                Node node = childNodes.item(i);
                if (node.getNodeType() == Node.ELEMENT_NODE) {
                    String language = node.getNodeName();
                    String text = node.getTextContent().trim();
                    localizedTexts.add(new String[]{language, text});
                }
            }
        }

        return localizedTexts;
    }

    /**
     * Loads an external locale file from the specified class path.
     *
     * @param classPath The path to the external locale file.
     * @return ResourceBundle loaded from the external file.
     */
    private static ResourceBundle loadExternalLocale(String classPath) {
        try {
            File file = new File(classPath);
            URL[] urls = {file.toURI().toURL()};
            ClassLoader loader = new URLClassLoader(urls);
            LOG.info(">> Loading external locale File for {}", Locale.getDefault());
            return ResourceBundle.getBundle("AutoDriveEditor", Locale.getDefault(), loader);
        } catch (MalformedURLException e) {
            e.printStackTrace();
            return null;
        }
    }

    /**
     * Sets the locale for the application by loading the appropriate ResourceBundle.
     */
    public static void setLocale() {
        localeString = getLocale();
        locale = Locale.getDefault();
    }

    /**
     * Retrieves a localized string for the given key.
     *
     * @param key The key for the desired localized string.
     * @return The localized string corresponding to the key.
     */
    public static String getLocaleString(String key) {
        try {
            return localeString.getString(key);
        } catch (NullPointerException e) {
            LOG.info("Localization error - Null pointer from getString()");
            e.printStackTrace();
            return "";
        } catch (MissingResourceException e) {
            LOG.info("Localization error - Missing key '{}'", key);
            e.printStackTrace();
            return key;
        }
    }
}