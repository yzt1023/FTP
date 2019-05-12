/*
 * Written By : Skye You
 * Copyright (c) 2018 - 2019. All Rights Reserved.
 */

package cn.edu.shu.common.encryption;

import cn.edu.shu.common.exception.EncryptionException;

public class Base64 {

    private final static String code = "ABCDEFGHIJKLMNOPQRSTUVWXYZabcdefghijklmnopqrstuvwxyz0123456789+/";

    public static String encode(byte[] bytes) throws EncryptionException {
        if(bytes == null || bytes.length == 0)
            throw new EncryptionException("Data to be encrypted cannot be empty!");

        // change byte array to binary string
        StringBuilder binary = new StringBuilder();
        for (byte b : bytes) {
            String str = Integer.toBinaryString(b & 0xff);
            while (str.length() < 8) {
                str = '0' + str;
            }
            binary.append(str);
        }

        // append char '0' to the binary string
        while(binary.length() % 6 != 0){
            binary.append('0');
        }

        // substitute the binary str using the base64 coding table
        String original = binary.toString();
        StringBuilder current = new StringBuilder();
        for(int i = 0; i < binary.length(); i += 6){
            int index = Integer.parseInt(original.substring(i, i + 6), 2);
            current.append(code.charAt(index));
        }

        // fill char '='  while insufficient
        if (bytes.length % 6 == 2){
            current.append('=');
        }else if(bytes.length % 6 == 1){
            current.append("==");
        }

        return current.toString();
    }

    public static byte[] decode(String str) throws EncryptionException {
        if(str == null || str.length() == 0)
            throw new EncryptionException("Data to be encrypted cannot be empty!");

        str = str.replaceAll("=", "");
        String binary = "";

        // get binary string
        for(char c : str.toCharArray()){
            String bin = Integer.toBinaryString(code.indexOf(c));
            while(bin.length() < 6)
                bin = '0' + bin;
            binary += bin;
        }

        // remove the zero added to the tail
        int zeroCount = binary.length() % 8;
        binary = binary.substring(0, binary.length() - zeroCount);
        byte[] bytes = new byte[binary.length() / 8];
        for(int i = 0; i < binary.length(); i += 8){
            int ch = Integer.parseInt(binary.substring(i, i + 8), 2);
            bytes[i / 8] = (byte)ch;
        }
        return bytes;
    }
}
