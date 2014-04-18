package uk.wycor.competitivecolours.app;

import android.bluetooth.BluetoothSocket;
import android.os.Bundle;
import android.os.Handler;
import android.os.Message;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.OutputStream;

/**
 * Created by WillW on 18/04/14.
 */
public class ConnectedThread extends Thread {

    private static final int MESSAGE_READ = 1234; //not sure what this thing is
    private final BluetoothSocket bluetoothSocket;
    private final Handler handler;
    private final InputStream inputStream;
    private final OutputStream outputStream;

    public ConnectedThread(BluetoothSocket bts, Handler h) {
        bluetoothSocket = bts;
        handler = h;
        InputStream tmpIn = null;
        OutputStream tmpOut = null;

        // Get the input and output streams, using temp objects because
        // member streams are final
        try {
            tmpIn = bluetoothSocket.getInputStream();
            tmpOut = bluetoothSocket.getOutputStream();
        } catch (IOException e) { }

        inputStream = tmpIn;
        outputStream = tmpOut;


        Message m = handler.obtainMessage();
        Bundle b = m.getData();
        b.putString("key", "the connected thread has started. messages can begin");
        handler.sendMessage(m);
    }

    public void run() {
        byte[] buffer = new byte[1024];  // buffer store for the stream
        int bytes; // bytes returned from read()
        BufferedReader br = new BufferedReader(new InputStreamReader(inputStream));
        String line = null;
        try {
            while ((line = br.readLine()) != null) {
                Message m = handler.obtainMessage();
                Bundle b = m.getData();
                // Keep listening to the InputStream until an exception occurs
                // Read from the InputStream
                /*
                bytes = inputStream.read(buffer);
                // Send the obtained bytes to the UI activity
                handler.obtainMessage(MESSAGE_READ, bytes, -1, buffer)
                        .sendToTarget();
                String message = new String(buffer);*/
                b.putString("key", "message received!");
                String[] parts = line.split(":");
                if (parts.length == 2) {
                    int newbgcolour = Integer.parseInt(parts[1]);
                    b.putInt(MainActivity.COLOUR_CHANGE_EVENT, newbgcolour);
                }
                handler.sendMessage(m);
            }
        } catch (IOException e) {

        }
    }

    /* Call this from the main activity to send data to the remote device */
    public void write(byte[] bytes) {
        try {
            outputStream.write(bytes);
        } catch (IOException e) { }
    }

    public void writeln(String string) {
        string = String.format(string + "%n");
        write(string.getBytes());
    }

    /* Call this from the main activity to shutdown the connection */
    public void cancel() {
        try {
            bluetoothSocket.close();
        } catch (IOException e) { }
    }

}
