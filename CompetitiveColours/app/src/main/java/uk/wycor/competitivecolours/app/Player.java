package uk.wycor.competitivecolours.app;

import android.bluetooth.BluetoothDevice;

/**
 * Created by wjw on 21/04/14.
 */
public class Player {

    private BluetoothDevice bluetoothDevice;
    private String playerName;
    private int playerScore;

    public Player(BluetoothDevice btd, String pln) {
        this.bluetoothDevice = btd;
        this.playerName = pln;
        this.playerScore = 0;
    }

    public BluetoothDevice getBluetoothDevice() {
        return bluetoothDevice;
    }

    public String getPlayerName() {
        return playerName;
    }

    public int getPlayerScore() {
        return playerScore;
    }

}
