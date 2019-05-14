/*
 * Written By : Skye You
 * Copyright (c) 2018 - 2019. All Rights Reserved.
 */

package cn.edu.shu.common.bean;

public enum DataType {
    BINARY,
    ASCII;

    public static DataType parse(char type){
        switch (type){
            case 'a':
            case 'A':
                return ASCII;
            case 'i':
            case 'I':
                return BINARY;
            default:
                return null;
        }
    }

    @Override
    public String toString() {
        return this == ASCII ? "A" : "I";
    }
}
