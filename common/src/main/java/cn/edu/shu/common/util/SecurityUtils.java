/*
 * Written By : Skye You
 * Copyright (c) 2018 - 2019. All Rights Reserved.
 */

package cn.edu.shu.common.util;

import cn.edu.shu.common.encryption.AES128;
import cn.edu.shu.common.encryption.Base64;
import cn.edu.shu.common.encryption.MD5;
import cn.edu.shu.common.exception.EncryptionException;

import java.util.Arrays;
import java.util.Random;

public class SecurityUtils {

    private static SecurityUtils instance = new SecurityUtils();
    private AES128 aes128;
    private MD5 md5;

    private SecurityUtils() {
        aes128 = new AES128();
        md5 = new MD5();
    }

    public static SecurityUtils getInstance() {
        return instance;
    }

    /**
     * attention: the len of the return array is end + fillLen;
     *
     * @param msg array to encrypted
     * @param end the end of array
     * @param key key used to encrypt
     * @return the array after encrypted
     */
    public byte[] encrypt(byte[] msg, int end, byte[] key) {
        byte[] newArray;
        int fillLen = 16 - (end % 16);

        if (end < msg.length && msg.length % 16 == 0)
            newArray = msg;
        else {
            newArray = new byte[end + fillLen];
            System.arraycopy(msg, 0, newArray, 0, end);
        }
        for (int i = 0; i < fillLen; i++)
            newArray[end + i] = (byte) (fillLen & 0xff);

        key = md5.get16Md5(key).getBytes();
        try {
            aes128.encrypt(newArray, 0, end + fillLen, key);
        } catch (EncryptionException e) {
            e.printStackTrace();
        }
        return newArray;
    }

    public String encrypt(String msg, String key) {
        byte[] bytes = msg.getBytes();
        bytes = encrypt(bytes, bytes.length, key.getBytes());
        try {
            return Base64.encode(bytes);
        } catch (EncryptionException e) {
            e.printStackTrace();
            return null;
        }
    }

    public int decrypt(byte[] msg, int end, byte[] key) {
        try {
            key = md5.get16Md5(key).getBytes();
            aes128.decrypt(msg, 0, end, key);
        } catch (EncryptionException e) {
            e.printStackTrace();
        }
        int len = msg[end - 1];
        return end - len;
    }

    public String decrypt(String msg, String key) {
        try {
            byte[] bytes = Base64.decode(msg);
            int len = decrypt(bytes, bytes.length, key.getBytes());
            return new String(Arrays.copyOfRange(bytes, 0, len));
        } catch (EncryptionException e) {
            e.printStackTrace();
        }
        return null;
    }

    public String generateKey() {
        Random random = new Random();
        int length = random.nextInt(25) + 8; // length : 5 - 99
        StringBuilder key = new StringBuilder();
        for (int i = 0; i < length; i++) {
            int temp = random.nextInt(3) % 3;
            if (temp == 0) {
                key.append((char) (random.nextInt(26) + 65));
            } else if (temp == 1) {
                key.append((char) (random.nextInt(26) + 97));
            } else {
                key.append(String.valueOf(random.nextInt(10)));
            }
        }
        return key.toString();
    }

    public MD5 getMd5() {
        return this.md5;
    }

}
