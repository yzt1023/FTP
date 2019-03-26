/*
 * Written By : Skye You
 * Copyright (c) 2018 - 2019. All Rights Reserved.
 */

package cn.edu.shu.client.listener;

public interface ConnectListener {
    boolean fireConnect(String host, int port);
    boolean fireConnect(String host, int port, String username, String password);
    void fireDisconnect();
    void afterConnect();
    void afterDisconnect();
}
