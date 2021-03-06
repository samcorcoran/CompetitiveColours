package uk.wycor.competitivecolours.app;

import android.bluetooth.BluetoothDevice;
import android.bluetooth.BluetoothServerSocket;
import android.bluetooth.BluetoothSocket;
import android.os.Bundle;
import android.os.Handler;
import android.os.Message;

import java.io.IOException;
import java.util.HashMap;
import java.util.Vector;

/**
 * Created by WillW on 18/04/14.
 */
public class ServerThread extends Thread {

    private final BluetoothServerSocket serverSocket;
    private final Handler handler;

    private boolean createExtraVillagers = true;

    private Vector<Player> players;

    private HashMap<BluetoothDevice, ConnectedThread> serverThreads;

    public ServerThread(BluetoothServerSocket bss, Handler h) {
        handler = h;
        serverSocket = bss;
        serverThreads = new HashMap<BluetoothDevice, ConnectedThread>();
        players = new Vector<Player>();
    }

    public void run() {
        BluetoothSocket socket = null;
        // Keep listening until exception occurs or a socket is returned
        Message m = handler.obtainMessage();
        Bundle b = m.getData();
        b.putInt(MainActivity.CONNECTIVITY_STATUS, MainActivity.CONNECTIVITY_LISTENING);
        handler.sendMessage(m);

        while (createExtraVillagers) {
            try {
                socket = serverSocket.accept();
            } catch (IOException e) {
                m = handler.obtainMessage();
                b = m.getData();
                b.putInt(MainActivity.CONNECTIVITY_STATUS, MainActivity.CONNECTIVITY_NONE);
                handler.sendMessage(m);
            }
            // If a connection was accepted
            if (socket != null) {
                // Do work to manage the connection (in a separate thread)
                forkCommunicationThread(socket, handler);
            }
        }
    }

    public void ceaseCreatingExtraVillagers() {
        this.createExtraVillagers = false;
    }

    public boolean addPlayer(Player player) {
        if (players.size() < 4) {
            players.add(player);
        } else {
            return false;
        }
        return true;
    }

    public void cancel() {
        try {
            serverSocket.close();
        } catch (IOException e) {
            //EXPLODE
        }
        Message m = handler.obtainMessage();
        Bundle b = m.getData();
        b.putInt(MainActivity.CONNECTIVITY_STATUS, MainActivity.CONNECTIVITY_NONE);
        handler.sendMessage(m);
    }

    private void forkCommunicationThread(BluetoothSocket bts, Handler h) {
        serverThreads.put(bts.getRemoteDevice(), new ConnectedThread(bts, h, false));
        serverThreads.get(bts.getRemoteDevice()).start();
    }

    public void writeAll(byte[] message) {
        for (ConnectedThread thread : serverThreads.values()) {
            thread.write(message);
        }
    }
}
