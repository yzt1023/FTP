/*
 * Written By : Skye You
 * Copyright (c) 2018 - 2019. All Rights Reserved.
 */

package cn.edu.shu.common.encryption;

import cn.edu.shu.common.exception.EncryptionException;

public class AES128 {

    /**
     * r-con array used for KeyExpansion
     */
    private final static int[] RCON = {
            0x01000000, 0x02000000, 0x04000000, 0x08000000, 0x10000000,
            0x20000000, 0x40000000, 0x80000000, 0x1b000000, 0x36000000
    };

    /**
     * column matrix used for MixColumns
     */
    private final static int[][] COLMATRIX = {
            {0x02, 0x03, 0x01, 0x01},
            {0x01, 0x02, 0x03, 0x01},
            {0x01, 0x01, 0x02, 0x03},
            {0x03, 0x01, 0x01, 0x02}
    };

    /**
     * inverse column matrix used for inverse MixColumns
     */
    private final static int[][] INVCOLMATRIX = {
            {0x0e, 0x0b, 0x0d, 0x09},
            {0x09, 0x0e, 0x0b, 0x0d},
            {0x0d, 0x09, 0x0e, 0x0b},
            {0x0b, 0x0d, 0x09, 0x0e}
    };

    /**
     * s box table used for KeyExpansion and SubBytes
     */
    private final static int[][] SBOX = {
            {0x63, 0x7c, 0x77, 0x7b, 0xf2, 0x6b, 0x6f, 0xc5, 0x30, 0x01, 0x67, 0x2b, 0xfe, 0xd7, 0xab, 0x76},
            {0xca, 0x82, 0xc9, 0x7d, 0xfa, 0x59, 0x47, 0xf0, 0xad, 0xd4, 0xa2, 0xaf, 0x9c, 0xa4, 0x72, 0xc0},
            {0xb7, 0xfd, 0x93, 0x26, 0x36, 0x3f, 0xf7, 0xcc, 0x34, 0xa5, 0xe5, 0xf1, 0x71, 0xd8, 0x31, 0x15},
            {0x04, 0xc7, 0x23, 0xc3, 0x18, 0x96, 0x05, 0x9a, 0x07, 0x12, 0x80, 0xe2, 0xeb, 0x27, 0xb2, 0x75},
            {0x09, 0x83, 0x2c, 0x1a, 0x1b, 0x6e, 0x5a, 0xa0, 0x52, 0x3b, 0xd6, 0xb3, 0x29, 0xe3, 0x2f, 0x84},
            {0x53, 0xd1, 0x00, 0xed, 0x20, 0xfc, 0xb1, 0x5b, 0x6a, 0xcb, 0xbe, 0x39, 0x4a, 0x4c, 0x58, 0xcf},
            {0xd0, 0xef, 0xaa, 0xfb, 0x43, 0x4d, 0x33, 0x85, 0x45, 0xf9, 0x02, 0x7f, 0x50, 0x3c, 0x9f, 0xa8},
            {0x51, 0xa3, 0x40, 0x8f, 0x92, 0x9d, 0x38, 0xf5, 0xbc, 0xb6, 0xda, 0x21, 0x10, 0xff, 0xf3, 0xd2},
            {0xcd, 0x0c, 0x13, 0xec, 0x5f, 0x97, 0x44, 0x17, 0xc4, 0xa7, 0x7e, 0x3d, 0x64, 0x5d, 0x19, 0x73},
            {0x60, 0x81, 0x4f, 0xdc, 0x22, 0x2a, 0x90, 0x88, 0x46, 0xee, 0xb8, 0x14, 0xde, 0x5e, 0x0b, 0xdb},
            {0xe0, 0x32, 0x3a, 0x0a, 0x49, 0x06, 0x24, 0x5c, 0xc2, 0xd3, 0xac, 0x62, 0x91, 0x95, 0xe4, 0x79},
            {0xe7, 0xc8, 0x37, 0x6d, 0x8d, 0xd5, 0x4e, 0xa9, 0x6c, 0x56, 0xf4, 0xea, 0x65, 0x7a, 0xae, 0x08},
            {0xba, 0x78, 0x25, 0x2e, 0x1c, 0xa6, 0xb4, 0xc6, 0xe8, 0xdd, 0x74, 0x1f, 0x4b, 0xbd, 0x8b, 0x8a},
            {0x70, 0x3e, 0xb5, 0x66, 0x48, 0x03, 0xf6, 0x0e, 0x61, 0x35, 0x57, 0xb9, 0x86, 0xc1, 0x1d, 0x9e},
            {0xe1, 0xf8, 0x98, 0x11, 0x69, 0xd9, 0x8e, 0x94, 0x9b, 0x1e, 0x87, 0xe9, 0xce, 0x55, 0x28, 0xdf},
            {0x8c, 0xa1, 0x89, 0x0d, 0xbf, 0xe6, 0x42, 0x68, 0x41, 0x99, 0x2d, 0x0f, 0xb0, 0x54, 0xbb, 0x16}
    };

    /**
     * inverse s-box table used for inverse SubBytes
     */
    private final static int[][] INVSBOX = {
            {0x52, 0x09, 0x6a, 0xd5, 0x30, 0x36, 0xa5, 0x38, 0xbf, 0x40, 0xa3, 0x9e, 0x81, 0xf3, 0xd7, 0xfb},
            {0x7c, 0xe3, 0x39, 0x82, 0x9b, 0x2f, 0xff, 0x87, 0x34, 0x8e, 0x43, 0x44, 0xc4, 0xde, 0xe9, 0xcb},
            {0x54, 0x7b, 0x94, 0x32, 0xa6, 0xc2, 0x23, 0x3d, 0xee, 0x4c, 0x95, 0x0b, 0x42, 0xfa, 0xc3, 0x4e},
            {0x08, 0x2e, 0xa1, 0x66, 0x28, 0xd9, 0x24, 0xb2, 0x76, 0x5b, 0xa2, 0x49, 0x6d, 0x8b, 0xd1, 0x25},
            {0x72, 0xf8, 0xf6, 0x64, 0x86, 0x68, 0x98, 0x16, 0xd4, 0xa4, 0x5c, 0xcc, 0x5d, 0x65, 0xb6, 0x92},
            {0x6c, 0x70, 0x48, 0x50, 0xfd, 0xed, 0xb9, 0xda, 0x5e, 0x15, 0x46, 0x57, 0xa7, 0x8d, 0x9d, 0x84},
            {0x90, 0xd8, 0xab, 0x00, 0x8c, 0xbc, 0xd3, 0x0a, 0xf7, 0xe4, 0x58, 0x05, 0xb8, 0xb3, 0x45, 0x06},
            {0xd0, 0x2c, 0x1e, 0x8f, 0xca, 0x3f, 0x0f, 0x02, 0xc1, 0xaf, 0xbd, 0x03, 0x01, 0x13, 0x8a, 0x6b},
            {0x3a, 0x91, 0x11, 0x41, 0x4f, 0x67, 0xdc, 0xea, 0x97, 0xf2, 0xcf, 0xce, 0xf0, 0xb4, 0xe6, 0x73},
            {0x96, 0xac, 0x74, 0x22, 0xe7, 0xad, 0x35, 0x85, 0xe2, 0xf9, 0x37, 0xe8, 0x1c, 0x75, 0xdf, 0x6e},
            {0x47, 0xf1, 0x1a, 0x71, 0x1d, 0x29, 0xc5, 0x89, 0x6f, 0xb7, 0x62, 0x0e, 0xaa, 0x18, 0xbe, 0x1b},
            {0xfc, 0x56, 0x3e, 0x4b, 0xc6, 0xd2, 0x79, 0x20, 0x9a, 0xdb, 0xc0, 0xfe, 0x78, 0xcd, 0x5a, 0xf4},
            {0x1f, 0xdd, 0xa8, 0x33, 0x88, 0x07, 0xc7, 0x31, 0xb1, 0x12, 0x10, 0x59, 0x27, 0x80, 0xec, 0x5f},
            {0x60, 0x51, 0x7f, 0xa9, 0x19, 0xb5, 0x4a, 0x0d, 0x2d, 0xe5, 0x7a, 0x9f, 0x93, 0xc9, 0x9c, 0xef},
            {0xa0, 0xe0, 0x3b, 0x4d, 0xae, 0x2a, 0xf5, 0xb0, 0xc8, 0xeb, 0xbb, 0x3c, 0x83, 0x53, 0x99, 0x61},
            {0x17, 0x2b, 0x04, 0x7e, 0xba, 0x77, 0xd6, 0x26, 0xe1, 0x69, 0x14, 0x63, 0x55, 0x21, 0x0c, 0x7d}
    };
    private int[] w = new int[44];

    /**
     * Take the upper four bits and the lower four bits of a word as the row number and column number of the S box
     * Replace the original byte with the byte in the S box by the row number
     *
     * @param matrix the matrix to be byte substituted
     */
    private void subBytes(int[][] matrix) {
        for (int i = 0; i < matrix.length; i++) {
            for (int j = 0; j < matrix[0].length; j++) {
                int temp = matrix[j][i] & 0xff;
                matrix[j][i] = SBOX[temp >> 4][temp & 0x0f];
            }
        }
    }

    private void invSubBytes(int[][] matrix) {
        for (int i = 0; i < matrix.length; i++) {
            for (int j = 0; j < matrix[0].length; j++) {
                int temp = matrix[j][i] & 0xff;
                matrix[j][i] = INVSBOX[temp >> 4][temp & 0x0f];
            }
        }
    }

    /**
     * perform a left shift on each row of the matrix
     *
     * @param matrix matrix to be shifted
     */
    private void shiftRows(int[][] matrix) {
        for (int i = 0; i < matrix.length; i++)
            matrix[i] = rotate(matrix[i], i, true);
    }

    private void invShiftRows(int[][] matrix) {
        for (int i = 0; i < matrix.length; i++)
            matrix[i] = rotate(matrix[i], i, false);
    }

    private int[] rotate(int[] row, int times, boolean left) {
        if (times >= row.length)
            times = times % row.length;
        if (times == row.length || times == 0)
            return row;

        int[] temp = new int[row.length];
        for (int i = 0; i < row.length; i++) {
            if (left)
                temp[i] = row[(i + times) % row.length];
            else
                temp[(i + times) % row.length] = row[i];
        }

        return temp;
    }

    /**
     * state matrix multiplied by a fixed matrix and then added
     *
     * @param matrix matrix to be mixed
     */
    private void mixColumns(int[][] matrix) {
        int[][] temp = new int[matrix.length][matrix[0].length];
        for (int i = 0; i < matrix.length; i++)
            System.arraycopy(matrix[i], 0, temp[i], 0, temp.length);

        for (int i = 0; i < matrix.length; i++) {
            for (int j = 0; j < matrix[0].length; j++)
                matrix[i][j] = matrixMultiple(temp, COLMATRIX, i, j);
        }
    }

    private void invMixColumns(int[][] matrix) {
        int[][] temp = new int[matrix.length][matrix[0].length];
        for (int i = 0; i < matrix.length; i++)
            System.arraycopy(matrix[i], 0, temp[i], 0, temp.length);

        for (int i = 0; i < matrix.length; i++) {
            for (int j = 0; j < matrix[0].length; j++)
                matrix[i][j] = matrixMultiple(temp, INVCOLMATRIX, i, j);
        }
    }

    private int matrixMultiple(int[][] matrix, int[][] colMatrix, int i, int j) {
        int sum = 0;
        for (int k = 0; k < matrix.length; k++) {
            sum ^= gfMultiple(matrix[k][j], colMatrix[i][k]);
        }
        return sum;
    }

    private int gfMultiple(int a, int b) {
        int result = 0;
        while (b != 0) {
            if ((b & 1) == 1)
                result ^= a;
            a <<= 1;
            if ((a & 0x100) > 0)
                a ^= 0x1b;
            b >>= 1;
        }
        return result & 0xff;
    }

    /**
     * bitwise xor of the 128-byte round key with the data in the matrix
     *
     * @param matrix state matrix to operate xor
     * @param round  round of encryption
     */
    private void addRoundKey(int[][] matrix, int round) {
        int[] key;
        for (int i = 0; i < matrix.length; i++) {
            key = intToArray(w[round * 4 + i]);
            for (int j = 0; j < matrix.length; j++)
                matrix[j][i] = matrix[j][i] ^ key[j];
        }
    }

    private int[] intToArray(int n) {
        int[] k = new int[4];
        for (int i = 3; i >= 0; i--) {
            k[i] = n & 0xff;
            n >>= 8;
        }
        return k;
    }

    private int arrayToInt(int[] b) {
        return b[0] << 0x18 | b[1] << 0x10 | b[2] << 0x8 | b[3];
    }

    /**
     * extend 4-word key to 44-word
     *
     * @param key the original 4-word
     */
    private void extendKey(byte[] key) {
        int j = 0;
        for (int i = 0; i < 4; i++) {
            w[i] = (key[j++] << 0x18) | (key[j++] << 0x10) | (key[j++] << 0x08) | (key[j++]);
        }

        for (int i = 4, r = 1; i < 44; i++) {
            if (i % 4 == 0) {
                w[i] = w[i - 4] ^ t(w[i - 1], r);
                r++;
            } else {
                w[i] = w[i - 4] ^ w[i - 1];
            }
        }
    }

    private int t(int num, int r) {
        int[] key = intToArray(num);
        key = rotate(key, 1, true);

        for (int i = 0; i < key.length; i++)
            key[i] = SBOX[(key[i] & 0xf0) >> 4][key[i] & 0x0f];

        int result = arrayToInt(key);
        return result ^ RCON[r - 1];
    }

    public byte[] encrypt(byte[] message, int start, int end, byte[] key) throws EncryptionException {
        if ((end - start) % 16 != 0)
            throw new EncryptionException("The length of message must be a multiple of 16!");

        if (key.length != 16)
            throw new EncryptionException("The length of key must be 16!");

        extendKey(key);

        int[][] matrix;
        for (int i = start; i < end; i += 16) {
            matrix = byteArrayToMatrix(message, i);

            addRoundKey(matrix, 0);
            for (int j = 1; j < 10; j++) {
                subBytes(matrix);
                shiftRows(matrix);
                mixColumns(matrix);
                addRoundKey(matrix, j);
            }
            subBytes(matrix);
            shiftRows(matrix);
            addRoundKey(matrix, 10);
            matrixToByteArray(matrix, message, i);
        }
        return message;
    }

    public byte[] encrypt(byte[] message, byte[] key) throws EncryptionException {
        return encrypt(message, 0, message.length, key);
    }

    private int[][] byteArrayToMatrix(byte[] bytes, int index) {
        int[][] matrix = new int[4][4];
        for (int i = 0; i < matrix.length; i++)
            for (int j = 0; j < matrix.length; j++)
                matrix[j][i] = bytes[index++] & 0xff;
        return matrix;
    }

    private void matrixToByteArray(int[][] matrix, byte[] bytes, int index) {
        for (int i = 0; i < matrix.length; i++)
            for (int j = 0; j < matrix.length; j++)
                bytes[index++] = (byte) (matrix[j][i] & 0xff);
    }

    public byte[] decrypt(byte[] message, int start, int end, byte[] key) throws EncryptionException {
        if ((end - start) % 16 != 0)
            throw new EncryptionException("The length of message must be a multiple of 16!");

        if (key.length != 16)
            throw new EncryptionException("The length of key must be 16!");

        extendKey(key);
        int[][] matrix;
        for (int i = start; i < end; i += 16) {
            matrix = byteArrayToMatrix(message, i);

            addRoundKey(matrix, 10);
            for (int j = 9; j > 0; j--) {
                invShiftRows(matrix);
                invSubBytes(matrix);
                addRoundKey(matrix, j);
                invMixColumns(matrix);
            }
            invShiftRows(matrix);
            invSubBytes(matrix);
            addRoundKey(matrix, 0);

            matrixToByteArray(matrix, message, i);
        }
        return message;
    }

    public byte[] decrypt(byte[] message, byte[] key) throws EncryptionException {
        return decrypt(message, 0, message.length, key);
    }

    private void printMatrix(int[][] matrix) {
        for (int[] aMatrix : matrix) {
            for (int j = 0; j < 4; j++)
                System.out.print(Integer.toHexString(aMatrix[j]) + " ");
            System.out.print("\n");
        }
        System.out.print("\n");
    }

}
