/*
 * Written By : Skye You
 * Copyright (c) 2018 - 2019. All Rights Reserved.
 */

package cn.edu.shu.client.ftp;

import cn.edu.shu.client.exception.ConnectionException;
import cn.edu.shu.common.ftp.FTPCommand;

import javax.net.ssl.SSLSocket;
import javax.net.ssl.SSLSocketFactory;
import java.io.BufferedReader;
import java.io.InputStreamReader;
import java.io.OutputStreamWriter;
import java.io.PrintWriter;
import java.net.InetSocketAddress;
import java.net.SocketAddress;

public class SSLControlConnection extends PlainControlConnection {

    SSLControlConnection(FTPClient client) {
        super(client);
    }

    @Override
    public void connect(String host, int port) throws ConnectionException {
        try {
            SSLSocketFactory factory = client.getSslContext().getSocketFactory();
            SSLSocket sslSocket = (SSLSocket) factory.createSocket();
            String[] suites = sslSocket.getSupportedCipherSuites();
            sslSocket.setEnabledCipherSuites(suites);
            sslSocket.setUseClientMode(true);
            socket = sslSocket;
            SocketAddress address = new InetSocketAddress(host, port);
            socket.connect(address, 0);
            reader = new BufferedReader(new InputStreamReader(socket.getInputStream(), client.getEncoding()));
            writer = new PrintWriter(new OutputStreamWriter(socket.getOutputStream(), client.getEncoding()), true);
        } catch (Exception e) {
            throw new ConnectionException(e.getMessage(), e);
        }

        readReply();
        executeCommand(FTPCommand.PBSZ + " 0");
        executeCommand(FTPCommand.PROT + " P");
    }
}
