package AutoDriveEditor.Utils;

public class TimeProfiler {

    private static long time;
    private static long startTime = 0;

    public TimeProfiler() {
        time = 0;
    }

    public void startTimer() {
        startTime = System.currentTimeMillis();
    }

    public void pauseTimer() {
        time += System.currentTimeMillis() - startTime;
    }

    public void restartTimer() {
        startTimer();
    }

    public void stopTimer() {
        pauseTimer();
    }

    public void resetTimer() {
        time = 0;
    }

    public long getTime() {
        return time;
    }
}



