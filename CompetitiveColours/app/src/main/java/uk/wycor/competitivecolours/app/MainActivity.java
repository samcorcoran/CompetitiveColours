package uk.wycor.competitivecolours.app;

import android.annotation.TargetApi;
import android.app.Dialog;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.IntentFilter;
import android.content.res.Configuration;
import android.graphics.Color;
import android.os.Build;
import android.os.CountDownTimer;
import android.os.Message;
import android.os.Handler;
import android.support.v7.app.ActionBarActivity;
import android.os.Bundle;
import android.util.Log;
import android.widget.AdapterView;
import android.widget.Button;
import android.widget.ArrayAdapter;
import android.view.Menu;
import android.view.MenuItem;
import android.bluetooth.*;
import android.view.View;
import android.widget.TextView;
import android.widget.Toast;
import android.widget.ToggleButton;
import android.widget.ListView;
import java.io.IOException;
import java.util.UUID;
import java.util.Vector;

public class MainActivity extends ActionBarActivity {

    public static final UUID CCUUID = UUID.fromString("30d9ff20-e01d-be4f-7183-55a0c0e01c40");

    static final int MAKE_DISCOVERABLE_REQUEST = 1;
    static final int REQUEST_ENABLE_BT = 2;
    private static final String TAG = "Compet~1";

    private Handler uiHandler;

    private BluetoothAdapter ourBluetoothAdapter;

    private int connectivityState;
    private ServerThread serverThread;
    private ClientThread clientThread;

    private ToggleButton toggle_bluetooth_enabled;
    private Button button_start_server;
    private Button button_search_for_devices;

    private CountDownTimer countdownSinceSearch;
    private boolean countdownInProgress;
    private int currentBackground;

    static final int BACKGROUND_RED = 0x1;
    static final int BACKGROUND_GREEN = 0x2;
    static final int BACKGROUND_BLUE = 0x4;
    static final int BACKGROUND_YELLOW = 0x8;

    static final int CONNECTIVITY_NONE = 0x0;
    static final int CONNECTIVITY_LISTENING = 0x1;
    static final int CONNECTIVITY_CONNECTING = 0x2;
    static final int CONNECTIVITY_CONNECTED_CLIENT = 0x4;
    static final int CONNECTIVITY_CONNECTED_SERVER = 0x8;

    static final String COLOUR_CHANGE_EVENT = "whoop";
    static final String UNKNOWN_EVENT = "gah!";
    static final String QUERY_COLOUR_EVENT = "whatis?!";
    static final String CONNECTIVITY_STATUS = "statum";

    private ListView deviceList;
    private Vector<BluetoothDevice> pairedDevices;
    private ArrayAdapter<String> BTArrayAdapter;

    protected Button button_red;
    protected Button button_green;
    protected Button button_blue;
    protected Button button_yellow;

    private Dialog optionsDialog;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        /* wherein we instantiate a message handler */
        uiHandler = new Handler() {
            @Override
            public void handleMessage(Message msg) {
                Bundle b = msg.getData();
                if (b.containsKey(COLOUR_CHANGE_EVENT)) {
                    setBackground(b.getInt(COLOUR_CHANGE_EVENT));
                }
                if (b.containsKey(QUERY_COLOUR_EVENT)) {
                    pushBackground();
                }
                if (b.containsKey(CONNECTIVITY_STATUS)) {
                    connectivityState = b.getInt(CONNECTIVITY_STATUS);
                }
                if (serverThread != null) {
                    pushBackground();
                }
            }
        };


        optionsDialog = new Dialog(this, R.style.TitlelessDialog);
        optionsDialog.setContentView(R.layout.dialog_options);
        optionsDialog.setTitle("Game setup");

        optionsDialog.setOnDismissListener(new Dialog.OnDismissListener() {
            @Override
            public void onDismiss(DialogInterface dli) {
                if (!isInGame()) {
                    displayOptions();
                }
            }
        });

        button_start_server = (Button) optionsDialog.findViewById(R.id.button_start_server);
        button_start_server.setOnClickListener(new View.OnClickListener() {
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

        countdownInProgress = false;

        // handle 'found device' event
        registerReceiver(bluetoothReceiver, new IntentFilter(BluetoothDevice.ACTION_FOUND));
        // handle 'finished searching for device' event
        registerReceiver(bluetoothReceiver, new IntentFilter(BluetoothAdapter.ACTION_DISCOVERY_FINISHED));

        if(ourBluetoothAdapter == null) {
            // Disable buttons
            toggle_bluetooth_enabled.setEnabled(false);
            // Warn user
            Toast.makeText(getApplicationContext(), R.string.warning_bluetooth_unsupported, Toast.LENGTH_LONG).show();
        } else {
            button_start_server = (Button) optionsDialog.findViewById(R.id.button_start_server);
            button_start_server.setOnClickListener(new View.OnClickListener() {
                public void onClick(View v) {
                    clearCurrentThreads();
                    Intent intent_make_discoverable = new Intent(BluetoothAdapter.ACTION_REQUEST_DISCOVERABLE);
                    startActivityForResult(intent_make_discoverable, MAKE_DISCOVERABLE_REQUEST);
                }
            });

            button_search_for_devices = (Button) optionsDialog.findViewById(R.id.button_search_for_devices);
            button_search_for_devices.setOnClickListener(new View.OnClickListener() {
                public void onClick(View v) {
                    clearCurrentThreads();
                    if (ourBluetoothAdapter.isDiscovering()) {
                        ourBluetoothAdapter.cancelDiscovery();
                        Toast.makeText(getApplicationContext(), "Search for devices cancelled", Toast.LENGTH_SHORT).show();
                    } else {
                        Toast.makeText(getApplicationContext(), "Searching for devices...", Toast.LENGTH_LONG).show();
                        BTArrayAdapter.clear();
                        pairedDevices.clear();
                        BTArrayAdapter.notifyDataSetChanged();
                        ourBluetoothAdapter.startDiscovery();
                    }
                }
            });

            // Toggle for enabling and disabling bluetooth
            toggle_bluetooth_enabled = (ToggleButton) optionsDialog.findViewById(R.id.toggle_bluetooth_enabled);
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
        pairedDevices = new Vector<BluetoothDevice>();
        deviceList = (ListView)optionsDialog.findViewById(R.id.device_list);
        BTArrayAdapter = new ArrayAdapter<String>(this, android.R.layout.simple_list_item_1);
        deviceList.setAdapter(BTArrayAdapter);
        deviceList.setOnItemClickListener(new AdapterView.OnItemClickListener() {
            @Override
            public void onItemClick(AdapterView<?> parent, View view, int position, long id) {
                // Attempt connection to selected device
                BluetoothDevice chosenDevice = pairedDevices.get(position);
                beginClientConnection(chosenDevice);
            }
        });

        displayOptions();
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
                CharSequence text = "Okay, hosting now...";

                Toast toast = Toast.makeText(context, text, duration);
                toast.show();

                connectivityState = CONNECTIVITY_LISTENING;
                beginServerListening(ourBluetoothAdapter);
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
        switch (id) {
            case R.id.action_settings:
                Toast.makeText(getApplicationContext(), "THERE ARE NO CONFIGURABLE SETTINGS", Toast.LENGTH_SHORT);
                return true;
            case R.id.action_restart:
                if (!optionsDialog.isShowing()) {
                    displayOptions();
                }
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
        toggle_bluetooth_enabled = (ToggleButton) optionsDialog.findViewById(R.id.toggle_bluetooth_enabled);
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

    private void pushBackground() {
        if (clientThread != null) {
            clientThread.write(CommandBytes.commandColourChange(currentBackground));
        }
        if (serverThread != null) {
            serverThread.writeAll(CommandBytes.commandColourChange(currentBackground));
        }
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

    private final BroadcastReceiver bluetoothReceiver = new BroadcastReceiver() {
        @TargetApi(Build.VERSION_CODES.ICE_CREAM_SANDWICH_MR1)
        @Override
        public void onReceive(Context context, Intent intent) {
            final String action = intent.getAction();

            if (action.equals(BluetoothAdapter.ACTION_STATE_CHANGED)) {
                final int state = intent.getIntExtra(BluetoothAdapter.EXTRA_STATE,
                        BluetoothAdapter.ERROR);
                updateBluetoothToggle(state);
            }
            else if (action.equals(BluetoothDevice.ACTION_FOUND)) {
                // Get the BluetoothDevice object from the Intent
                BluetoothDevice device = intent.getParcelableExtra(BluetoothDevice.EXTRA_DEVICE);
                // Add to device list
                pairedDevices.add(device);
                // add the name and the MAC address of the object to the arrayAdapter
                BTArrayAdapter.add(device.getName() + "\n" + device.getAddress());
                BTArrayAdapter.notifyDataSetChanged();
            }
            else if (action.equals(BluetoothAdapter.ACTION_DISCOVERY_FINISHED)) {
                // Finished searching for devices

                // Use countdown timer to display time elapsed since last device search ended
                final TextView timeSinceSearchMsg = (TextView) optionsDialog.findViewById(R.id.device_list_time_elapsed);
                if (countdownInProgress)
                    countdownSinceSearch.cancel();
                countdownInProgress = true;
                final long timeoutLength = 60000;
                countdownSinceSearch = new CountDownTimer(timeoutLength, 1000) {
                    public void onTick(long millisUntilFinished) {
                        long elapsed = (timeoutLength - millisUntilFinished)/1000;
                        timeSinceSearchMsg.setText("(" + elapsed + " seconds elapsed since last device search...)" );
                    }

                    public void onFinish() {
                        countdownInProgress = false;
                        timeSinceSearchMsg.setText("(>1 min since last device search...)");
                    }
                }.start();

                // Announce number of devices found
                String msg;
                int numDevices = pairedDevices.size();
                if (numDevices == 0) {
                    msg = "No devices found";
                }
                else {
                    msg = Integer.toString(numDevices) + " device";
                    if (numDevices > 1)
                        msg += "s";
                    msg += " found";
                }
                Toast.makeText(getApplicationContext(), msg, Toast.LENGTH_LONG).show();
            }
        }
    };

    private void enableBluetoothButtons(boolean enabled) {
        button_start_server.setEnabled(enabled);
        button_search_for_devices.setEnabled(enabled);
    };

    private void clearCurrentThreads() {

        if (clientThread != null) {
            clientThread.cancel();
            clientThread = null;
        }

        if (serverThread != null) {
            serverThread.cancel();
            serverThread = null;
        }
    }

    private void beginClientConnection(BluetoothDevice bluetoothDevice) {
        clearCurrentThreads();
        BluetoothSocket bluetoothSocket;
        try {
            bluetoothSocket = bluetoothDevice.createRfcommSocketToServiceRecord(CCUUID);
        } catch (IOException e) {
            // flash a big message here and don't continue
            return;
        }

        clientThread = new ClientThread(bluetoothSocket, uiHandler);
        clientThread.start();

        hideOptions();
    }

    private void beginServerListening(BluetoothAdapter bluetoothAdapter) {
        clearCurrentThreads();
        BluetoothServerSocket bluetoothServerSocket;
        try {
            bluetoothServerSocket = bluetoothAdapter.listenUsingRfcommWithServiceRecord(getResources().getString(R.string.app_name), CCUUID);
        } catch (IOException e) {
            return;
        }

        serverThread = new ServerThread(bluetoothServerSocket, uiHandler);
        serverThread.start();

        hideOptions();
    }

    private void displayOptions() {
        optionsDialog.show();
    }

    private void hideOptions() {
        if (optionsDialog.isShowing()) {
            optionsDialog.dismiss();
        }
    } 
    private boolean isInGame() {
        return (serverThread != null) || (clientThread != null);
    }
}
