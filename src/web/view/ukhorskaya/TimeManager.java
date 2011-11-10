package web.view.ukhorskaya;

/**
 * Created by IntelliJ IDEA.
 * User: Natalia.Ukhorskaya
 * Date: 11/9/11
 * Time: 3:32 PM
 */

public class TimeManager {
    private long startTime;
    private long savedTime = 0;

    public TimeManager() {
        startTime = System.nanoTime();
    }

    public void updateStartTime() {
        startTime = System.nanoTime();
    }

    public long getMillisecondsFromStart() {
        return (System.nanoTime() - startTime) / 1000000;
    }

    public void saveCurrentTime() {
        savedTime = System.nanoTime();
    }

    public long getMillisecondsFromSavedTime() {
        return (System.nanoTime() - savedTime) / 1000000;
    }
}
