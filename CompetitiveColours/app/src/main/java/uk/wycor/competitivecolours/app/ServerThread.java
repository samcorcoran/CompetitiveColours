package uk.wycor.competitivecolours.app;

import android.bluetooth.BluetoothDevice;
import android.bluetooth.BluetoothServerSocket;
import android.bluetooth.BluetoothSocket;
import android.os.Bundle;
import android.os.Handler;
import android.os.Message;

import java.io.IOException;
import java.util.HashMap;

/**
 * Created by WillW on 18/04/14.
 */
public class ServerThread extends Thread {

    private final BluetoothServerSocket serverSocket;
    private final Handler handler;

    private HashMap<BluetoothDevice, ConnectedThread> serverThreads;

    public ServerThread(BluetoothServerSocket bss, Handler h) {
        handler = h;
        serverSocket = bss;
        serverThreads = new HashMap<BluetoothDevice, ConnectedThread>();
    }

    public void run() {
        BluetoothSocket socket = null;
        // Keep listening until exception occurs or a socket is returned
        Message m = handler.obtainMessage();
        Bundle b = m.getData();
        b.putString("key", "Background Task notify, server thread has started");
        handler.sendMessage(m);
        while (true) {
            try {
                socket = serverSocket.accept();
            } catch (IOException e) {
                break;
            }
            // If a connection was accepted
            if (socket != null) {
                // Do work to manage the connection (in a separate thread)
                forkCommunicationThread(socket, handler);
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

    private void forkCommunicationThread(BluetoothSocket bts, Handler h) {
        serverThreads.put(bts.getRemoteDevice(), new ConnectedThread(bts, h));
        serverThreads.get(bts.getRemoteDevice()).start();
    }

    public void writeToAllClients(String message) {
        for (ConnectedThread thread : serverThreads.values()) {
            thread.write(message.getBytes());
        }
    }
}
