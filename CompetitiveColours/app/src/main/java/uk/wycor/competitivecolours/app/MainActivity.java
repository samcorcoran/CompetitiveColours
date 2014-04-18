package uk.wycor.competitivecolours.app;

import android.content.Context;
import android.content.Intent;
import android.support.v7.app.ActionBarActivity;
import android.os.Bundle;
import android.widget.Button;
import android.view.Menu;
import android.view.MenuItem;
import android.bluetooth.*;
import android.view.View;
import android.widget.Toast;

public class MainActivity extends ActionBarActivity {

    static final int MAKE_DISCOVERABLE_REQUEST = 1;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        final Button button_make_discoverable = (Button) findViewById(R.id.button_make_discoverable);
        button_make_discoverable.setOnClickListener(new View.OnClickListener() {
            public void onClick(View v) {
                //make a thing happen
                Intent intent_make_discoverable = new Intent(BluetoothAdapter.ACTION_REQUEST_DISCOVERABLE);
                startActivityForResult(intent_make_discoverable, MAKE_DISCOVERABLE_REQUEST);
            }
        });
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        // Check which request we're responding to
        if (requestCode == MAKE_DISCOVERABLE_REQUEST) {
            // Make sure the request was successful
            Context context = getApplicationContext();
            int duration = Toast.LENGTH_SHORT;

            if (resultCode == RESULT_OK) {
                // I don't know!!
                CharSequence text = "Your device is now discoverable.";

                Toast toast = Toast.makeText(context, text, duration);
                toast.show();
            } else {
                //cancelled?
                CharSequence text = "Why did you cancel this?!";

                Toast toast = Toast.makeText(context, text, duration);
                toast.show();
            }
        }
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        
        // Inflate the menu; this adds items to the action bar if it is present.
        getMenuInflater().inflate(R.menu.main, menu);
        return true;
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        // Handle action bar item clicks here. The action bar will
        // automatically handle clicks on the Home/Up button, so long
        // as you specify a parent activity in AndroidManifest.xml.
        int id = item.getItemId();
        if (id == R.id.action_settings) {
            return true;
        }
        return super.onOptionsItemSelected(item);
    }

}
