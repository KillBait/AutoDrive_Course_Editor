package AutoDriveEditor.Managers;

public class VersionManager {



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
