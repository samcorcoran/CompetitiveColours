package uk.wycor.competitivecolours.app;

import android.bluetooth.BluetoothServerSocket;
import android.bluetooth.BluetoothSocket;

import java.io.IOException;

/**
 * Created by WillW on 18/04/14.
 */
public class ServerThread extends Thread {

    private final BluetoothServerSocket serverSocket;

    public ServerThread(BluetoothServerSocket bss) {
        serverSocket = bss;
    }

    public void run() {
        BluetoothSocket socket = null;
        // Keep listening until exception occurs or a socket is returned
        while (true) {
            try {
                socket = serverSocket.accept();
            } catch (IOException e) {
                break;
            }
            // If a connection was accepted
            if (socket != null) {
                // Do work to manage the connection (in a separate thread)
                forkCommunicationThread(socket);
                cancel();
                break;
            }
        }
    }

    public void cancel() {
        try {
            serverSocket.close();
        } catch (IOException e) {
            //EXPLODE
        }
    }

    private void forkCommunicationThread(BluetoothSocket bts) {
        ConnectedThread connectedThread = new ConnectedThread(bts);
        connectedThread.start();
    }
}
