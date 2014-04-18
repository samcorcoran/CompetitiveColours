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
import android.widget.CompoundButton;
import android.widget.Toast;
import android.widget.ToggleButton;

public class MainActivity extends ActionBarActivity {

    static final int MAKE_DISCOVERABLE_REQUEST = 1;
    static final int REQUEST_ENABLE_BT = 2;

    private BluetoothAdapter ourBluetoothAdapter;

    private ToggleButton toggle_bluetooth_enabled;
    private Button button_make_discoverable;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        ourBluetoothAdapter = BluetoothAdapter.getDefaultAdapter();

        if(ourBluetoothAdapter == null) {
            // Disable buttons
            toggle_bluetooth_enabled.setEnabled(false);
            // Warn user
            Toast.makeText(getApplicationContext(), R.string.warning_bluetooth_unsupported, Toast.LENGTH_LONG).show();
        } else {
            button_make_discoverable = (Button) findViewById(R.id.button_make_discoverable);
            button_make_discoverable.setOnClickListener(new View.OnClickListener() {
                public void onClick(View v) {
                    //make a thing happen
                    Intent intent_make_discoverable = new Intent(BluetoothAdapter.ACTION_REQUEST_DISCOVERABLE);
                    startActivityForResult(intent_make_discoverable, MAKE_DISCOVERABLE_REQUEST);
                }
            });

            // Toggle for enabling and disabling bluetooth
            toggle_bluetooth_enabled = (ToggleButton) findViewById(R.id.toggle_bluetooth_enabled);
            toggle_bluetooth_enabled.setChecked(ourBluetoothAdapter.isEnabled());
            toggle_bluetooth_enabled.setOnCheckedChangeListener(new CompoundButton.OnCheckedChangeListener() {
                public void onCheckedChanged(CompoundButton buttonView, boolean isChecked) {
                    if (isChecked) {
                        // The toggle is enabled
                        Intent turnOnIntent = new Intent(BluetoothAdapter.ACTION_REQUEST_ENABLE);
                        startActivityForResult(turnOnIntent, REQUEST_ENABLE_BT);
                        Toast.makeText(getApplicationContext(),"Bluetooth turned on" ,
                                Toast.LENGTH_SHORT).show();

                    } else {
                        // The toggle is disabled
                        ourBluetoothAdapter.disable();
                        Toast.makeText(getApplicationContext(),"Bluetooth turned off", Toast.LENGTH_SHORT).show();
                    }
                }
            });
        }
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        // Check which request we're responding to
        if (requestCode == MAKE_DISCOVERABLE_REQUEST) {
            // Make sure the request was successful
            Context context = getApplicationContext();
            int duration = Toast.LENGTH_SHORT;

            if (resultCode == RESULT_CANCELED) {
                // I don't know!!
                CharSequence text = "Why did you cancel this?!";

                Toast toast = Toast.makeText(context, text, duration);
                toast.show();
            } else {
                //cancelled?
                CharSequence text = "Your device is discoverable for " + String.valueOf(resultCode) + " seconds";

                Toast toast = Toast.makeText(context, text, duration);
                toast.show();
            }
        }
        else if (requestCode == REQUEST_ENABLE_BT) {
            if(ourBluetoothAdapter.isEnabled()) {

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
