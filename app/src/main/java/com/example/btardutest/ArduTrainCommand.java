package com.example.btardutest;

/**
 * Created by daniel on 4/7/15.
 */
public enum ArduTrainCommand {
    GenError((byte)0x00),
    Connect((byte)1),
    Disconnect((byte)2),
    Echo((byte)3);

    public byte getCmd() {
        return cmd;
    }

    private byte cmd;

    private ArduTrainCommand(byte cmd){
        this.cmd=cmd;
    }


}
