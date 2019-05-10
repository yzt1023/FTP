/*
 * Written By : Skye You
 * Copyright (c) 2018 - 2019. All Rights Reserved.
 */

package cn.edu.shu.client.listener;

public interface ConnectListener {
    boolean startConnect(String host, int port);
    boolean startConnect(String host, int port, String username, String password);
    void startDisconnect();
    void connectCompleted();
    void disconnectCompleted();
}
