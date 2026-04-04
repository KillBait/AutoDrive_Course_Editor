package AutoDriveEditor.Classes.Util_Classes;

public class FSUtils {

    public enum GameVersion {
        NOT_SUPPORTED("Unsupported"),
        FS19("Farming Simulator19"),
        FS22("Farming Simulator22"),
        FS25("Farming Simulator25");

        private final String description;

        GameVersion(String description){ this.description = description; }

        public String getDescription() { return description; }

        public static GameVersion getGameVersion(String versionString) {
            for (GameVersion gv : GameVersion.values()) {
                if (gv.description.equals(versionString)) {
                    return gv;
                }
            }
            return NOT_SUPPORTED;
        }

        public static boolean isSupported(int descVersion) {
            return descVersion >= 40;
        }
    }

    public static GameVersion getFSVersion(int descVersion) {
        if (descVersion >= 21 && descVersion <=25) {
            return GameVersion.NOT_SUPPORTED; // Farming Simulator15;
        } else if (descVersion >= 31 && descVersion <= 37) {
            return GameVersion.NOT_SUPPORTED; // Farming Simulator17;
        } else if (descVersion >= 40 && descVersion <= 53) {
            return GameVersion.FS19;
        } else if (descVersion >= 60 && descVersion <= 80) {
            return GameVersion.FS22;
        } else if (descVersion >=90) {
            return GameVersion.FS25;
        }
        return GameVersion.NOT_SUPPORTED;
    }
}
