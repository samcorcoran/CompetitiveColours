package uk.wycor.competitivecolours.app;

import android.text.format.Time;

/**
 * Created by Sam on 19/04/14.
 */
public class CaptureAction {

    private int timestamp;
    private int colour;
    private String user;

    CaptureAction(int newTimestamp, int newColour, String newUser) {
        timestamp = newTimestamp;
        colour = newColour;
        user = newUser;
    }

    public int getTimestamp() {
        return timestamp;
    }

    public int getColour() {
        return colour;
    }

    public String getUser() {
        return user;
    }
}
