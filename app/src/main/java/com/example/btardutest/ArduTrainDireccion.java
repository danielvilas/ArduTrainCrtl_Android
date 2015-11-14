package com.example.btardutest;

/**
 * Created by daniel on 22/8/15.
 */
public enum ArduTrainDireccion {
        Free((byte)0x00),
        StopH((byte)1),
        StopL((byte)2),
        Forward((byte)3),
        Backward((byte)4);

        public byte getValue() {
            return value;
        }

        private byte value;

        private ArduTrainDireccion(byte value){
            this.value=value;
        }

}

