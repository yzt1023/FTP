/*
 * Written By : Skye You
 * Copyright (c) 2018 - 2019. All Rights Reserved.
 */

package cn.edu.shu.common.util;

import cn.edu.shu.common.EncryptionException;
import cn.edu.shu.common.encryption.AES128;

public class AESUtils {

    private AES128 aes128;
    private AESUtils instance = new AESUtils();

    private AESUtils() {
        aes128 = new AES128();
    }

    public AESUtils getInstance(){
        return instance;
    }

    public byte[] encrypt(byte[] msg, byte[] key) {
        msg = fillText(msg);
        try {
            msg = aes128.encrypt(msg, key);
        } catch (EncryptionException e) {
            e.printStackTrace();
        }
        return msg;
    }

    public byte[] decrypt(byte[] msg, byte[] key) {
        try {
            aes128.decrypt(msg, key);
        } catch (EncryptionException e) {
            e.printStackTrace();
        }
        return restoreTest(msg);
    }

    private byte[] fillText(byte[] bytes) {
        int fillLen = 16 - bytes.length % 16;
        byte[] newByte = new byte[bytes.length + fillLen];
        System.arraycopy(bytes, 0, newByte, 0, bytes.length);
        for (int i = 0; i < fillLen; i++)
            newByte[bytes.length + i] = (byte) (fillLen & 0xff);
        return newByte;
    }

    private byte[] restoreTest(byte[] bytes) {
        int len = bytes[bytes.length - 1];
        byte[] newByte = new byte[bytes.length - len];
        System.arraycopy(bytes, 0, newByte, 0, newByte.length);
        return newByte;
    }

}
