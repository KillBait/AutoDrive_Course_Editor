package AutoDriveEditor.Utils;

import static AutoDriveEditor.Utils.LoggerUtils.LOG;

public class DebugUtils {

    public static long profileTimer = 0;
    public static long totalTime = 0;

    public static void startTimer() { profileTimer = System.currentTimeMillis(); }

    public static long stopTimer() { return System.currentTimeMillis() - profileTimer; }

    public static void stopTimerAndDisplay(String text) {
        LOG.info("{} - {}", text, System.currentTimeMillis() - profileTimer);
    }
}
