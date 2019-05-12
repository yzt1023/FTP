/*
 * Written By : Skye You
 * Copyright (c) 2018 - 2019. All Rights Reserved.
 */

package cn.edu.shu.common.util;

import cn.edu.shu.common.exception.EncryptionException;
import cn.edu.shu.common.encryption.AES128;
import cn.edu.shu.common.encryption.Base64;
import cn.edu.shu.common.encryption.MD5;

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

    public byte[] encrypt(byte[] msg, byte[] key) {
        msg = fillText(msg);
        key = md5.get16Md5(key).getBytes();
        try {
            msg = aes128.encrypt(msg, key);
        } catch (EncryptionException e) {
            e.printStackTrace();
        }
        return msg;
    }

    public String encrypt(String msg, String key) {
        byte[] bytes = encrypt(msg.getBytes(), key.getBytes());
        try {
            return Base64.encode(bytes);
        } catch (EncryptionException e) {
            e.printStackTrace();
            return null;
        }
    }

    public byte[] decrypt(byte[] msg, byte[] key) {
        try {
            key = md5.get16Md5(key).getBytes();
            aes128.decrypt(msg, key);
        } catch (EncryptionException e) {
            e.printStackTrace();
        }
        return restoreTest(msg);
    }

    public String decrypt(String msg, String key) {
        byte[] bytes = new byte[0];
        try {
            bytes = Base64.decode(msg);
        } catch (EncryptionException e) {
            e.printStackTrace();
        }
        return new String(decrypt(bytes, key.getBytes()));
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

    public String generateKey(){
        Random random = new Random();
        int length = random.nextInt(25) + 8; // length : 5 - 99
        StringBuilder key = new StringBuilder();
        for(int i = 0; i < length; i++){
            int temp = random.nextInt(3) % 3;
            if(temp == 0){
                key.append((char) (random.nextInt(26) + 65));
            }else if(temp == 1){
                key.append((char) (random.nextInt(26) + 97));
            }else{
                key.append(String.valueOf(random.nextInt(10)));
            }
        }
        return key.toString();
    }

    public MD5 getMd5(){
        return this.md5;
    }

}
