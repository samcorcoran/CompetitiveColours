package uk.wycor.competitivecolours.app;

/**
 * Created by WillW on 19/04/14.
 */
public class CommandBytes {

    public static final byte COMMAND_COLOUR_CHANGE = (byte) 0xf7;

    public static byte[] commandColourChange(int colour) {
        byte[] command = new byte[3];
        command[0] = (byte) (command.length - 1);
        command[1] = COMMAND_COLOUR_CHANGE;
        command[2] = (byte) colour;
        return command;
    }

}
