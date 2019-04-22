/*
 * Written By : Skye You
 * Copyright (c) 2018 - 2019. All Rights Reserved.
 */

package cn.edu.shu.client.ui.task;

public class ProgressArg {

    private int value;
    private int maxValue;

    public ProgressArg(int value) {
        this.value = value;
    }

    public ProgressArg(int value, int maxValue) {
        this.value = value;
        this.maxValue = maxValue;
    }

    public int getValue() {
        return value;
    }

    public void setValue(int value) {
        this.value = value;
    }

    public int getMaxValue() {
        return maxValue;
    }

    public void setMaxValue(int maxValue) {
        this.maxValue = maxValue;
    }
}
