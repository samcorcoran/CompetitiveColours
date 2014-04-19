package uk.wycor.competitivecolours.app;

import uk.wycor.competitivecolours.app.util.SystemUiHider;

import android.annotation.TargetApi;
import android.app.Activity;
import android.content.res.Configuration;
import android.graphics.Color;
import android.os.Build;
import android.os.Bundle;
import android.os.Handler;
import android.os.Message;
import android.util.Log;
import android.view.MotionEvent;
import android.view.View;
import android.widget.Button;

public class GameActivity extends Activity {

    private static final String TAG = "Compet~1";
    private Handler uiHandler;
    private ServerThread serverThread;
    private ClientThread clientThread;

    private int currentBackground;

    static final int BACKGROUND_RED = 0x1;
    static final int BACKGROUND_GREEN = 0x2;
    static final int BACKGROUND_BLUE = 0x4;
    static final int BACKGROUND_YELLOW = 0x8;

    protected Button button_red;
    protected Button button_green;
    protected Button button_blue;
    protected Button button_yellow;

    static final String COLOUR_CHANGE_EVENT = "whoop";
    static final String UNKNOWN_EVENT = "gah!";
    static final String QUERY_COLOUR_EVENT = "whatis?!";

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_game);

                /* wherein we instantiate a message handler */
        uiHandler = new Handler() {
            @Override
            public void handleMessage(Message msg) {
                Bundle b = msg.getData();
                Log.i(TAG, "Message: " + b.getString("key"));
                if (b.containsKey(COLOUR_CHANGE_EVENT)) {
                    setBackground(b.getInt(COLOUR_CHANGE_EVENT));
                }
                if (b.containsKey(QUERY_COLOUR_EVENT)) {
                    pushBackground();
                }
                if (serverThread != null) {
                    pushBackground();
                }
            }
        };

        button_red = (Button) findViewById(R.id.button_red);
        button_green = (Button) findViewById(R.id.button_green);
        button_blue = (Button) findViewById(R.id.button_blue);
        button_yellow = (Button) findViewById(R.id.button_yellow);

        button_red.setOnClickListener(new View.OnClickListener() {
            public void onClick(View v) {
                setBackgroundRed();
            }
        });
        button_green.setOnClickListener(new View.OnClickListener() {
            public void onClick(View v) {
                setBackgroundGreen();
            }
        });
        button_blue.setOnClickListener(new View.OnClickListener() {
            public void onClick(View v) {
                setBackgroundBlue();
            }
        });
        button_yellow.setOnClickListener(new View.OnClickListener() {
            public void onClick(View v) {
                setBackgroundYellow();
            }
        });

    }

    protected void setBackgroundRed() {
        setBackground(BACKGROUND_RED);
        pushBackground();
    }

    protected void setBackgroundGreen() {
        setBackground(BACKGROUND_GREEN);
        pushBackground();
    }

    protected void setBackgroundBlue() {
        setBackground(BACKGROUND_BLUE);
        pushBackground();
    }

    protected void setBackgroundYellow() {
        setBackground(BACKGROUND_YELLOW);
        pushBackground();
    }

    protected void setBackground(int bg) {
        currentBackground = bg;
        View main_view = findViewById(R.id.main_layout);
        switch (bg) {
            case BACKGROUND_BLUE:
                main_view.setBackgroundColor(getResources().getColor(R.color.background_blue));
                break;
            case BACKGROUND_YELLOW:
                main_view.setBackgroundColor(getResources().getColor(R.color.background_yellow));
                break;
            case BACKGROUND_RED:
                main_view.setBackgroundColor(getResources().getColor(R.color.background_red));
                break;
            case BACKGROUND_GREEN:
                main_view.setBackgroundColor(getResources().getColor(R.color.background_green));
                break;
            default:
                main_view.setBackgroundColor(Color.parseColor("#FFFAFA"));
        }
    }

    private void pushBackground() {
        if (clientThread != null) {
            clientThread.write(CommandBytes.commandColourChange(currentBackground));
        }
        if (serverThread != null) {
            serverThread.writeAll(CommandBytes.commandColourChange(currentBackground));
        }
    }

    @Override
    public void onConfigurationChanged(Configuration newConfig) {
        super.onConfigurationChanged(newConfig);
        setBackground(currentBackground);
    }
}
