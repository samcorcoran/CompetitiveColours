package uk.wycor.competitivecolours.app;

import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.content.res.Configuration;
import android.graphics.Color;
import android.support.v7.app.ActionBarActivity;
import android.os.Bundle;
import android.widget.Button;
import android.widget.ArrayAdapter;
import android.view.Menu;
import android.view.MenuItem;
import android.bluetooth.*;
import android.view.View;
import android.widget.Toast;
import android.widget.ToggleButton;
import android.widget.ListView;
import java.util.Set;

public class MainActivity extends ActionBarActivity {

    static final int MAKE_DISCOVERABLE_REQUEST = 1;
    static final int REQUEST_ENABLE_BT = 2;

    private BluetoothAdapter ourBluetoothAdapter;

    private ToggleButton toggle_bluetooth_enabled;
    private Button button_make_discoverable;
    private Button button_search_for_devices;

    private int currentBackground;

    static final int BACKGROUND_RED = 0x1;
    static final int BACKGROUND_GREEN = 0x2;
    static final int BACKGROUND_BLUE = 0x4;
    static final int BACKGROUND_YELLOW = 0x8;

    private ListView deviceList;
    private Set<BluetoothDevice> pairedDevices;
    private ArrayAdapter<String> BTArrayAdapter;

    protected Button button_red;
    protected Button button_green;
    protected Button button_blue;
    protected Button button_yellow;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        button_make_discoverable = (Button) findViewById(R.id.button_make_discoverable);

        button_make_discoverable.setOnClickListener(new View.OnClickListener() {
            public void onClick(View v) {
                //make a thing happen
                Intent intent_make_discoverable = new Intent(BluetoothAdapter.ACTION_REQUEST_DISCOVERABLE);
                startActivityForResult(intent_make_discoverable, MAKE_DISCOVERABLE_REQUEST);
            }
        });

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

            button_search_for_devices = (Button) findViewById(R.id.button_search_for_devices);
            button_search_for_devices.setOnClickListener(new View.OnClickListener() {
                public void onClick(View v) {
                    //make a thing happen
                    if (ourBluetoothAdapter.isDiscovering()) {
                        ourBluetoothAdapter.cancelDiscovery();
                        Toast.makeText(getApplicationContext(), "Search for devices cancelled", Toast.LENGTH_SHORT).show();
                    }
                    else {
                        Toast.makeText(getApplicationContext(), "Searching for devices...", Toast.LENGTH_SHORT).show();
                        BTArrayAdapter.clear();
                        BTArrayAdapter.notifyDataSetChanged();
                        ourBluetoothAdapter.startDiscovery();
                        registerReceiver(bluetoothReceiver, new IntentFilter(BluetoothDevice.ACTION_FOUND));
                    }
                }
            });

            // Toggle for enabling and disabling bluetooth
            toggle_bluetooth_enabled = (ToggleButton) findViewById(R.id.toggle_bluetooth_enabled);
            toggle_bluetooth_enabled.setOnClickListener(new View.OnClickListener() {
                public void onClick(View view) {
                    // Is the toggle on?
                    boolean isChecked = ((ToggleButton) view).isChecked();
                    if (isChecked) {
                        // The toggle is enabled
                        Intent turnOnIntent = new Intent(BluetoothAdapter.ACTION_REQUEST_ENABLE);
                        startActivityForResult(turnOnIntent, REQUEST_ENABLE_BT);
                    } else {
                        // The toggle is disabled
                        ourBluetoothAdapter.disable();
                        Toast.makeText(getApplicationContext(), "Bluetooth turned off", Toast.LENGTH_SHORT).show();
                    }
                }
            });
            updateBluetoothToggle();
        }

        // Register for broadcasts on BluetoothAdapter state change
        IntentFilter filter = new IntentFilter(BluetoothAdapter.ACTION_STATE_CHANGED);
        this.registerReceiver(bluetoothReceiver, filter);

        // Use array adapter and list view to hold devices found
        deviceList = (ListView)findViewById(R.id.device_list);
        BTArrayAdapter = new ArrayAdapter<String>(this, android.R.layout.simple_list_item_1);
        deviceList.setAdapter(BTArrayAdapter);
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        // Check which request we're responding to
        Context context = getApplicationContext();
        int duration = Toast.LENGTH_SHORT;

        if (requestCode == MAKE_DISCOVERABLE_REQUEST) {
            // Make sure the request was successful

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

            if (resultCode == RESULT_CANCELED) {
                Toast.makeText(context, "It won't work if you don't play along", duration).show();
            } else {
                Toast.makeText(context, "Bluetooth turned on", duration).show();
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

    @Override
    public void onResume() {
        super.onResume();
        updateBluetoothToggle();
    }


    @Override
    public void onDestroy() {
        super.onDestroy();
        this.unregisterReceiver(bluetoothReceiver);
    }

    @Override
    public void onConfigurationChanged(Configuration newConfig) {
        super.onConfigurationChanged(newConfig);
        setBackground(currentBackground);
    }

    private void updateBluetoothToggle() {
        if (ourBluetoothAdapter.isEnabled()) {
            updateBluetoothToggle(BluetoothAdapter.STATE_ON);
        } else {
            updateBluetoothToggle(BluetoothAdapter.STATE_OFF);
        }
    }

    private void updateBluetoothToggle(int state) {
        // Enable/disable bluetooth-dependent buttons
        toggle_bluetooth_enabled = (ToggleButton) findViewById(R.id.toggle_bluetooth_enabled);
        switch (state) {
            case BluetoothAdapter.STATE_OFF:
            case BluetoothAdapter.STATE_TURNING_OFF:
                toggle_bluetooth_enabled.setChecked(false);
                enableBluetoothButtons(false);
            break;
            case BluetoothAdapter.STATE_ON:
            case BluetoothAdapter.STATE_TURNING_ON:
                toggle_bluetooth_enabled.setChecked(true);
                enableBluetoothButtons(true);
                break;
        }
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

    protected void setBackgroundRed() {
        setBackground(BACKGROUND_RED);
    }

    protected void setBackgroundGreen() {
        setBackground(BACKGROUND_GREEN);
    }

    protected void setBackgroundBlue() {
        setBackground(BACKGROUND_BLUE);
    }

    protected void setBackgroundYellow() {
        setBackground(BACKGROUND_YELLOW);
    }

    public void listPairedDevices(View view){
        // get paired devices
        pairedDevices = ourBluetoothAdapter.getBondedDevices();
        // put it's one to the adapter
        for(BluetoothDevice device : pairedDevices)
            BTArrayAdapter.add(device.getName()+ "\n" + device.getAddress());
        Toast.makeText(getApplicationContext(),"Show Paired Devices",
                Toast.LENGTH_SHORT).show();
    }

    private final BroadcastReceiver bluetoothReceiver = new BroadcastReceiver() {
        @Override
        public void onReceive(Context context, Intent intent) {
            final String action = intent.getAction();

            if (action.equals(BluetoothAdapter.ACTION_STATE_CHANGED)) {
                final int state = intent.getIntExtra(BluetoothAdapter.EXTRA_STATE,
                        BluetoothAdapter.ERROR);
                updateBluetoothToggle(state);
            }
            else if (BluetoothDevice.ACTION_FOUND.equals(action)) {
                // Get the BluetoothDevice object from the Intent
                BluetoothDevice device = intent.getParcelableExtra(BluetoothDevice.EXTRA_DEVICE);
                // add the name and the MAC address of the object to the arrayAdapter
                BTArrayAdapter.add(device.getName() + "\n" + device.getAddress());
                BTArrayAdapter.notifyDataSetChanged();
            }
        }
    };

    private void enableBluetoothButtons(boolean enabled) {
        button_make_discoverable.setEnabled(enabled);
        button_search_for_devices.setEnabled(enabled);
    };
}
