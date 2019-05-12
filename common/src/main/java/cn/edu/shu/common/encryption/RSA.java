/*
 * Written By : Skye You
 * Copyright (c) 2018 - 2019. All Rights Reserved.
 */

package cn.edu.shu.common.encryption;

import java.math.BigInteger;
import java.security.SecureRandom;

public class RSA {

    BigInteger p, q;
    private BigInteger one = BigInteger.ONE;
    private SecureRandom random = new SecureRandom();

    public BigInteger producePrime() {
        return BigInteger.probablePrime(256, random);
    }

    public BigInteger producePublicKey() {
        return BigInteger.probablePrime(100, random);
    }

    public BigInteger getN(BigInteger p, BigInteger q){
        return p.multiply(q);
    }

    public BigInteger getPrivateKey(BigInteger p, BigInteger q, BigInteger publicKey) {
        BigInteger phi = (p.subtract(one)).multiply(q.subtract(one));
        return publicKey.modInverse(phi);
    }

    public BigInteger produceN() {
        p = producePrime();
        q = producePrime();
        return p.multiply(q);
    }

    // use it after produceN() method
    public BigInteger producePrivateKey(BigInteger publicKey){
        return getPrivateKey(p, q, publicKey);
    }

    public BigInteger encryptOrDecrypt(BigInteger key, BigInteger n, BigInteger message) {
        return message.modPow(key, n);
    }

    public String encrypt(BigInteger key, BigInteger n, String message){
        BigInteger msg = stringToInteger(message);
        return msg.modPow(key, n).toString();
    }

    public String decrypt(BigInteger key, BigInteger n, String message){
        BigInteger msg = new BigInteger(message);
        msg = msg.modPow(key, n);
        return integerToString(msg);
    }

    private BigInteger stringToInteger(String str){
        char[] chars = str.toCharArray();
        StringBuilder num = new StringBuilder();
        for(char c : chars){
            int n = (int)c;
            if(n / 10 == 0)
                num.append('0');
            else if(n / 100 == 0)
                num.append('0');
            num.append(n);
        }
        return new BigInteger(num.toString());
    }

    private String integerToString(BigInteger integer){
        int[] num = split(integer);
        StringBuilder str = new StringBuilder();
        for(int n : num)
            str.append((char) n);
        return str.toString();
    }

    private int[] split(BigInteger integer){
        String text = integer.toString();
        int len = text.length(), remain = len % 3;
        int arrayLen = (remain == 0 ? len / 3 : len / 3 + 1);
        int[] array = new int[arrayLen];
        int i = 0;
        if(len % 3 != 0){
            array[0] = Integer.parseInt(text.substring(0, remain));
            text = text.substring(remain);
            i = 1;
        }
        for(; i < arrayLen; i++){
            if(text.length() > 3) {
                array[i] = Integer.parseInt(text.substring(0, 3));
                text = text.substring(3);
            }else {
                array[i] = Integer.parseInt(text);
            }
        }
        return array;
    }

}
