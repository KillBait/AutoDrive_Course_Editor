package AutoDriveEditor.Utils;

import java.math.RoundingMode;

import static AutoDriveEditor.Utils.MathUtils.limitDoubleToDecimalPlaces;

public class ProfileUtil {

    private long time;
    private long startTime = 0;

    public ProfileUtil() {
        this.time = 0;
    }

    public void startTimer() {
        this.startTime = System.nanoTime();
    }

    public void pauseTimer() {
        this.time += System.nanoTime() - this.startTime;
    }

    public void restartTimer() {
        startTimer();
    }

    public void stopTimer() {
        pauseTimer();
    }

    public void resetTimer() {
        this.time = 0;
    }

    public double getTime(int decimalPrecision) {
        return limitDoubleToDecimalPlaces((double)this.time / 1000000, decimalPrecision , RoundingMode.HALF_UP);
    }
}



