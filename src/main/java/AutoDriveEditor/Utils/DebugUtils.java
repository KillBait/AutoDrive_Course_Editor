package AutoDriveEditor.Utils;

public class DebugUtils {

    public static long profileTimer = 0;
    public static long totalTime = 0;

    public static void startTimer() { profileTimer = System.currentTimeMillis(); }

    public static long  stopTimer() { return System.currentTimeMillis() - profileTimer; }
}
