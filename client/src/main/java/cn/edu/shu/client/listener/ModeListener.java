/*
 * Written By : Skye You
 * Copyright (c) 2018 - 2019. All Rights Reserved.
 */

package cn.edu.shu.client.listener;

public interface ModeListener {
    void firePasvModeChanged(boolean passive);
    void fireSecureModeChanged(boolean secure);
}
