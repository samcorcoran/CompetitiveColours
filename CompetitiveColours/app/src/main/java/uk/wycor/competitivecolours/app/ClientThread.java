package uk.wycor.competitivecolours.app;

import android.bluetooth.BluetoothDevice;
import android.bluetooth.BluetoothSocket;

import java.io.IOException;

/**
 * Created by WillW on 18/04/14.
 */
public class ClientThread extends Thread {

    private final BluetoothSocket bluetoothSocket;

    public ClientThread(BluetoothSocket bts) {
        bluetoothSocket = bts;
    }

    public void run() {
        try {
            bluetoothSocket.connect();
        } catch (IOException e) {
            // Close the socket
            try {
                bluetoothSocket.close();
            } catch (IOException e2) {

            }
            // Start the service over to restart listening mode
            return;
        }

        if (bluetoothSocket.isConnected()) {
            // Do work to manage the connection (in a separate thread)
            forkCommunicationThread(bluetoothSocket);
            cancel();
        }
    }

    public void cancel() {
        try {
            bluetoothSocket.close();
        } catch (IOException e) {
            //fuck you
        }
    }

    private void forkCommunicationThread(BluetoothSocket bts) {
        ConnectedThread connectedThread = new ConnectedThread(bts);
        connectedThread.start();
    }
}
