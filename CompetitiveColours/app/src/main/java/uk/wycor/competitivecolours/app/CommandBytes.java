package uk.wycor.competitivecolours.app;

import java.nio.charset.Charset;
import java.nio.charset.StandardCharsets;

/**
 * Created by WillW on 19/04/14.
 */
public class CommandBytes {

    public static final byte COMMAND_COLOUR_CHANGE = (byte) 0xf7;
    public static final byte COMMAND_QUERY_COLOUR = (byte) 0xf6;
    public static final byte COMMAND_NOOP = (byte) 0xef; //null operation to confirm client presence
    public static final byte COMMAND_JOIN_NAME = (byte) 0x3e;

    public static byte[] commandColourChange(int colour) {
        byte[] command = new byte[3];
        command[0] = (byte) (command.length - 1);
        command[1] = COMMAND_COLOUR_CHANGE;
        command[2] = (byte) colour;
        return command;
    }

    public static byte[] commandQueryColour() {
        byte[] command = new byte[2];
        command[0] = (byte) (command.length - 1);
        command[1] = COMMAND_QUERY_COLOUR;
        return command;
    }

    public static byte[] commandNoop() {
        byte[] command = new byte[2];
        command[0] = (byte) (command.length - 1);
        command[1] = COMMAND_NOOP;
        return command;
    }

    public static byte[] commandJoinGame(String playerName) {
        //StandardCharsets.UTF_8.name()
        byte[] playerBytes = playerName.substring(0, Math.min(40, playerName.length())).getBytes(Charset.forName("UTF-8"));
        int playerBytesLength = Math.min(playerBytes.length, 254);
        byte[] command = new byte[playerBytesLength + 1];
        command[0] = COMMAND_JOIN_NAME;
        for (int i = 0; i < playerBytesLength; i++) {
            command[i+1] = playerBytes[0]; //old school array copying!
        }
        return command;
    }

}
