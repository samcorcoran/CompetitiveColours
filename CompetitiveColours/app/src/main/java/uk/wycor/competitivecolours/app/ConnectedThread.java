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
        byte[] buffer; // = new byte[1024];  // buffer store for the stream
        byte[] dataLength = new byte[1];
        int bytes; // bytes returned from read()
        while (true) {
            try {
                bytes = inputStream.read(dataLength); //read 1 byte to determine length of next chunk of datas
                buffer = new byte[(int)dataLength[0]]; //craft a buffer of that length

                bytes = inputStream.read(buffer); //read aforementioned agreed-upon length
                int command = (int)buffer[0]; //get the command byte

                Message m = handler.obtainMessage();
                Bundle b = m.getData();
                b.putString("key", "message received!"); //construct message

                switch (command) { //what command did it give us?!
                    case CommandBytes.COMMAND_COLOUR_CHANGE:
                        int newBackground = (int)buffer[1];
                        b.putInt(MainActivity.COLOUR_CHANGE_EVENT, newBackground);
                        break;
                    default:
                        b.putByteArray(MainActivity.UNKNOWN_EVENT, buffer);
                }

                handler.sendMessage(m); //pass it all back up

            } catch (IOException e) {
                cancel();
            }
        }
    }

    /* Call this from the main activity to send data to the remote device */
    public void write(byte[] bytes) {
        try {
            outputStream.write(bytes);
        } catch (IOException e) { }
    }

    /* Call this from the main activity to shutdown the connection */
    public void cancel() {
        try {
            bluetoothSocket.close();
        } catch (IOException e) { }
    }

}
