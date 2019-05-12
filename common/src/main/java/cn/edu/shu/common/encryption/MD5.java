/*
 * Written By : Skye You
 * Copyright (c) 2018 - 2019. All Rights Reserved.
 */


package cn.edu.shu.common.encryption;

import static java.lang.Math.abs;
import static java.lang.Math.sin;

public class MD5 {

    private final static long A = 0x67452301L;
    private final static long B = 0xEFCDAB89L;
    private final static long C = 0x98BADCFEL;
    private final static long D = 0x10325476L;
    //four chaining variables

    private static long[] T = new long[64];

    static {
        for (int i = 0; i < 64; i++) {
            T[i] = new Double(abs(sin(i + 1)) * 4294967296L).longValue();
        }
    }

    private long rotateLeft(long a, long s) {
        return ((int) a << s) | ((int) a >>> (32 - s));
    }

    private long ff(long a, long b, long c, long d, long mj, long s, long ti) {
        return b + rotateLeft(a + ((b & c) | ((~b) & d)) + mj + ti, s);
    }

    private long gg(long a, long b, long c, long d, long mj, long s, long ti) {
        return b + rotateLeft(a + ((b & d) | (c & (~d))) + mj + ti, s);
    }

    private long hh(long a, long b, long c, long d, long mj, long s, long ti) {
        return b + rotateLeft(a + (b ^ c ^ d) + mj + ti, s);
    }

    private long ii(long a, long b, long c, long d, long mj, long s, long ti) {
        return b + rotateLeft(a + (c ^ (b | (~d))) + mj + ti, s);
    }

    public String getMD5(byte[] input) {
        long a = A, b = B, c = C, d = D;
        int msgLen = input.length;
        int blockSize = ((msgLen + 8) >>> 6) + 1;
        int totalLen = blockSize << 6;

        // ========== step 1: filling message ==========
        byte[] M = new byte[totalLen];
        // original message
        System.arraycopy(input, 0, M, 0, msgLen);
        // fill the first bit as 1, and others as 0
        if (msgLen % 64 < 56)
            M[msgLen] = (byte) (1 << 7);

        // original length
        long len = (long) msgLen << 3;
        for (int i = 0; i < 8; i++) {
            M[totalLen - 8 + i] = (byte) (len & 0xFFL);
            len >>>= 8;
        }

        // ========= step 2: compute md5 =========
        long[] X = new long[16];
        for (int i = 0; i < blockSize; i++) {
            for (int j = 0; j < 16; j++) {
                int index = i * 64 + j * 4;
                X[j] = ((long) M[index] & 0xFF) | ((long) M[index + 1] & 0xFF) << 8 | ((long) M[index + 2] & 0xFF) << 16 | ((long) M[index + 3] & 0xFF) << 24;
            }
            long lastA = a, lastB = b, lastC = c, lastD = d;
            for (int j = 0; j < 64; j++) {
                int div16 = j >>> 4;
                int index;
                switch (div16) {
                    case 0:
                        index = j;
                        if (j % 4 == 0) {
                            a = ff(a, b, c, d, X[index], 7, T[j]);
                        } else if (j % 4 == 1) {
                            d = ff(d, a, b, c, X[index], 12, T[j]);
                        } else if (j % 4 == 2) {
                            c = ff(c, d, a, b, X[index], 17, T[j]);
                        } else {
                            b = ff(b, c, d, a, X[index], 22, T[j]);
                        }
                        break;
                    case 1:
                        index = (j * 5 + 1) % 16;
                        if (j % 4 == 0) {
                            a = gg(a, b, c, d, X[index], 5, T[j]);
                        } else if (j % 4 == 1) {
                            d = gg(d, a, b, c, X[index], 9, T[j]);
                        } else if (j % 4 == 2) {
                            c = gg(c, d, a, b, X[index], 14, T[j]);
                        } else if (j % 4 == 3) {
                            b = gg(b, c, d, a, X[index], 20, T[j]);
                        }
                        break;
                    case 2:
                        index = (j * 3 + 5) % 16;
                        if (j % 4 == 0) {
                            a = hh(a, b, c, d, X[index], 4, T[j]);
                        } else if (j % 4 == 1) {
                            d = hh(d, a, b, c, X[index], 11, T[j]);
                        } else if (j % 4 == 2) {
                            c = hh(c, d, a, b, X[index], 16, T[j]);
                        } else if (j % 4 == 3) {
                            b = hh(b, c, d, a, X[index], 23, T[j]);
                        }
                        break;
                    case 3:
                        index = (j * 7) % 16;
                        if (j % 4 == 0) {
                            a = ii(a, b, c, d, X[index], 6, T[j]);
                        } else if (j % 4 == 1) {
                            d = ii(d, a, b, c, X[index], 10, T[j]);
                        } else if (j % 4 == 2) {
                            c = ii(c, d, a, b, X[index], 15, T[j]);
                        } else if (j % 4 == 3) {
                            b = ii(b, c, d, a, X[index], 21, T[j]);
                        }
                        break;
                }
            }
            a = (a + lastA) & 0xFFFFFFFFL;
            b = (b + lastB) & 0xFFFFFFFFL;
            c = (c + lastC) & 0xFFFFFFFFL;
            d = (d + lastD) & 0xFFFFFFFFL;
        }

        // ========== step 3 : connect to the result ==========
        a = encode(a);
        b = encode(b);
        c = encode(c);
        d = encode(d);

        return getString(a, b, c, d).substring(8, 24);
    }

    public String getMD5(String message){
        return getMD5(message.getBytes());
    }

    private long encode(long t) {
        return ((t >> 24) & 0xff) | ((t >> 16) & 0xff) << 8 | ((t >> 8) & 0xff) << 16 | (t & 0xff) << 24;
    }

    private String getString(long a, long b, long c, long d) {
        return toHexString(a) + toHexString(b) + toHexString(c) + toHexString(d);
    }

    private String toHexString(long value) {
        StringBuilder temp = new StringBuilder(Long.toHexString(value));
        while (temp.length() < 8)
            temp.insert(0, '0');
        return temp.toString();
    }
}