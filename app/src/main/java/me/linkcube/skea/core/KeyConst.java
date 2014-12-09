package me.linkcube.skea.core;

/**
 * Created by Ervin on 14/11/13.
 */
public class KeyConst {

    public static class Language {

        public static final String English = "English";

        public static final String Chinese = "Chinese";
    }

    public static final String AUTO_LOGIN = "AUTO_LOGIN";

    public static final String USER_ID = "USER_ID";

    public static class GameFrame {

        public static final byte[] SPEED_FRAME = {0x25, 0x2, 0x2, 0x0, 0x0,
                0x0, 0x0, 0x29};

        public static final byte[] HEART_RATE_FRAME = {0x25, 0x3, 0x0, 0x0,
                0x0, 0x0, 0x0, 0x28};

        public static final byte[] PRESS_FRAME = {0x26, 0x1, 0x0, 0x0, 0x0,
                0x0, 0x0, 0x26};

        public static final byte[] INTO_OTA_FRAME = {0x25, (byte) 0xFF,
                (byte) 0xAA, (byte) 0xAA, 0x0, 0x0, 0x0, 0x78};

        public static final byte[] SHAKE_HAND_FRAME = {0x0F};

        public static final byte[] READ_BL_FRAME = {0x0F, 0x0, 0x0, 0x0, 0x04};

        public static final byte[][] READ_MEMORY_FRAMES = {
                {0x0F, 0x01, 0x00, 0x00, 0x00, 0x00, (byte) 0x80, 0x00, (byte) 0xF9, (byte) 0xA3, 0x04},
                {0x0F, 0x01, (byte) 0x80, 0x00, 0x00, 0x00, (byte) 0x80, 0x00, (byte) 0xD9, 0x77, 0x04},
                {0x0F, 0x01, 0x00, 0x01, 0x00, 0x00, (byte) 0x80, 0x00, (byte) 0xA8, 0x09, 0x04},
                {0x0F, 0x01, (byte) 0x80, 0x01, 0x00, 0x00, (byte) 0x80, 0x00, (byte) 0x88, (byte) 0xDD, 0x04},
                {0x0F, 0x01, 0x00, 0x02, 0x00, 0x00, (byte) 0x80, 0x00, 0x7A, (byte) 0xE7, 0x04},
                {0x0F, 0x01, (byte) 0x80, 0x02, 0x00, 0x00, (byte) 0x80, 0x00, 0x5A, 0x33, 0x04},
                {0x0F, 0x01, 0x00, 0x03, 0x00, 0x00, (byte) 0x80, 0x00, 0x2B, 0x4D, 0x04},
                {0x0F, 0x01, (byte) 0x80, 0x03, 0x00, 0x00, (byte) 0x80, 0x00, 0x0B, (byte) 0x99, 0x04},
                {0x0F, 0x01, 0x00, 0x05, 0x04, 0x00, 0x00, (byte) 0x80, 0x00, (byte) 0xFF, 0x2A, 0x04},
                {0x0F, 0x01, (byte) 0x80, 0x05, 0x04, 0x00, 0x00, (byte) 0x80, 0x00, (byte) 0xDF, (byte) 0xFE, 0x04},
                {0x0F, 0x01, 0x00, 0x05, 0x05, 0x00, 0x00, (byte) 0x80, 0x00, (byte) 0xAE, (byte) 0x80, 0x04},
                {0x0F, 0x01, (byte) 0x80, 0x05, 0x05, 0x00, 0x00, (byte) 0x80, 0x00, (byte) 0x8E, 0x54, 0x04}};
    }

}
