/*
 * Written By : Skye You
 * Copyright (c) 2018 - 2019. All Rights Reserved.
 */

package cn.edu.shu.client.ftp;

import cn.edu.shu.client.exception.ConnectionException;

import java.io.IOException;
import java.net.InetAddress;

public interface ControlConnection {

    void connect(String host, int port) throws ConnectionException;

    String readReply() throws ConnectionException;

    void sendCommand(String command) throws ConnectionException;

    String executeCommand(String command) throws ConnectionException;

    InetAddress getLocalAddress();

    void close() throws IOException;
}


