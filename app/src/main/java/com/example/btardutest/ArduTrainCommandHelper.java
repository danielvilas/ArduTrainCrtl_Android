package com.example.btardutest;

/**
 * Created by daniel on 4/7/15.
 */
public class ArduTrainCommandHelper {
    private static final int messageSize=25;
    private static final int maxPayLoad=messageSize-3; //Len, OP, CRC,

    private static byte[] generateRequest(ArduTrainCommand cmd, byte[] payload){
        if(payload.length > maxPayLoad){
            throw new RuntimeException("Message OverSize");
        }
        byte[] ret = new byte[25];
        ret[0]=(byte)payload.length;
        ret[1]=cmd.getCmd();
        System.arraycopy(payload,0,ret,2,payload.length);
        ret[payload.length+2]=generateCrc(ret, payload.length+2);
        for(int i=payload.length+3;i<messageSize;i++){
            ret[i]= (byte)payload.length;
        }
        return ret;
    }

    private static byte generateCrc(byte[] list, int size){
        byte ret=list[0];

        for(int i=1;i<size;i++){
            ret ^= list[i];
        }

        return ret;

    }

    public static byte[] generateEchoMessage(String echo){
        return generateRequest(ArduTrainCommand.Echo,echo.getBytes());
    }
}
