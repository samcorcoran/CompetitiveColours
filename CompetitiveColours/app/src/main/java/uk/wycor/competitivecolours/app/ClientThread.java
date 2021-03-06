package uk.wycor.competitivecolours.app;

import android.bluetooth.BluetoothSocket;
import android.os.Bundle;
import android.os.Handler;
import android.os.Message;

import java.io.IOException;

/**
 * Created by WillW on 18/04/14.
 */
public class ClientThread extends Thread {

    private final BluetoothSocket bluetoothSocket;
    private final Handler handler;

    private ConnectedThread connectedThread;

    public ClientThread(BluetoothSocket bts, Handler h) {
        handler = h;
        bluetoothSocket = bts;
    }

    public void run() {
        Message m = handler.obtainMessage();
        Bundle b = m.getData();
        b.putInt(MainActivity.CONNECTIVITY_STATUS, MainActivity.CONNECTIVITY_CONNECTING);
        handler.sendMessage(m);
        try {
            bluetoothSocket.connect();
        } catch (IOException e) {
            // Close the socket
            try {
                bluetoothSocket.close();
            } catch (IOException e2) {

            }
            // Start the service over to restart listening mode

            m = handler.obtainMessage();
            b = m.getData();
            b.putInt(MainActivity.CONNECTIVITY_STATUS, MainActivity.CONNECTIVITY_NONE);
            handler.sendMessage(m);
            return;
        }

        if (bluetoothSocket.isConnected()) {
            // Do work to manage the connection (in a separate thread)
            forkCommunicationThread(bluetoothSocket, handler);
        }
    }

    public void cancel() {
        try {
            bluetoothSocket.close();
        } catch (IOException e) {
            //fuck you
        }
        Message m = handler.obtainMessage();
        Bundle b = m.getData();
        b.putInt(MainActivity.CONNECTIVITY_STATUS, MainActivity.CONNECTIVITY_NONE);
        handler.sendMessage(m);
    }

    private void forkCommunicationThread(BluetoothSocket bts, Handler h) {
        connectedThread = new ConnectedThread(bts, h, true);
        connectedThread.start();
    }

    public void write(byte[] message) {
        if (connectedThread != null) {
            connectedThread.write(message);
        }
    }
}
