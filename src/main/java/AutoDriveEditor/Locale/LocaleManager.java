package AutoDriveEditor.Locale;

import java.io.File;
import java.net.MalformedURLException;
import java.net.URL;
import java.net.URLClassLoader;
import java.nio.file.Paths;
import java.util.Locale;
import java.util.ResourceBundle;

import static AutoDriveEditor.Utils.LoggerUtils.LOG;

public class LocaleManager {

    public static ResourceBundle localeString;
    public static Locale locale;

    private static ResourceBundle getLocale(){

        String localePath;
        String localeName = null;
        String classPath = null;
        File newFile;
        URL url;

        try {

            ResourceBundle bundle = ResourceBundle.getBundle("locale.AutoDriveEditor", Locale.getDefault());
            LOG.info("'AutoDriveEditor_{}.properties' loaded", Locale.getDefault());
            return bundle;
        } catch (Exception e) {
            LOG.info("'AutoDriveEditor_{}.properties' not found. looking in folders", Locale.getDefault());
            localePath = "./locale/AutoDriveEditor_" + Locale.getDefault() + ".properties";
            if (Paths.get(localePath).toFile().exists()) {
                classPath = "./locale/";
            }

            localePath = "./src/locale/AutoDriveEditor_" + Locale.getDefault() + ".properties";
            if (Paths.get(localePath).toFile().exists()) {
                classPath = "./src/locale/";
            }

            localePath = "./AutoDriveEditor_" + Locale.getDefault() + ".properties";
            if (Paths.get(localePath).toFile().exists()) {
                classPath = "./";
            }

            localePath = "./src/main/resources/locale/AutoDriveEditor_" + Locale.getDefault() + ".properties";
            if (Paths.get(localePath).toFile().exists()) {
                classPath = "./src/main/resources/locale/";
            }

            if (classPath != null) {
                File file = new File(classPath);
                URL[] urls = new URL[0];
                try {
                    urls = new URL[]{file.toURI().toURL()};
                } catch (MalformedURLException ex) {
                    ex.printStackTrace();
                }
                ClassLoader loader = new URLClassLoader(urls);
                ResourceBundle bundle = ResourceBundle.getBundle("AutoDriveEditor", Locale.getDefault(), loader);
                LOG.info("loading external locale File for {}", Locale.getDefault());
                return bundle;
            } else {
                LOG.info("Locale file not found..loading default locale for {}", new Locale("en", "US"));
                return ResourceBundle.getBundle("locale.AutoDriveEditor", new Locale("en", "US"));
            }
        }
    }

    public static void setLocale() {
        localeString = getLocale();
        locale = Locale.getDefault();
    }
}
