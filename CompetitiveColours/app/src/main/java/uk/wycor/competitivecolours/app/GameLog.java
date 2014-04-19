package uk.wycor.competitivecolours.app;

import android.text.format.Time;

import java.util.List;
import java.util.Vector;

/**
 * Created by Sam on 19/04/14.
 */
public class GameLog {

    private long startTime;
    private Vector<CaptureAction> captureActions;

    GameLog() {
        startTime = System.currentTimeMillis();
    }

    public void logNewAction(int colour, String user) {
        int now = (int)(System.currentTimeMillis() - startTime);
        captureActions.add(new CaptureAction(now, colour, user));
    }
}
