/*
 * Written By : Skye You
 * Copyright (c) 2018 - 2019. All Rights Reserved.
 */

package cn.edu.shu.client.ftp;

import cn.edu.shu.client.exception.ConnectionException;
import cn.edu.shu.common.ftp.FTPCommand;
import cn.edu.shu.common.log.MsgListener;
import cn.edu.shu.common.util.Constants;

import java.io.*;
import java.net.InetAddress;
import java.net.Socket;

public class PlainConnection {

    private Socket socket;
    BufferedReader reader;
    PrintWriter writer;
    private FTPClient client;
    private MsgListener listener;

    PlainConnection(FTPClient client) {
        this.client = client;
        this.listener = client.getListener();
    }

    public void connect(String host, int port) throws ConnectionException {
        try {
            socket = new Socket(host, port);
            reader = new BufferedReader(new InputStreamReader(socket.getInputStream(), client.getEncoding()));
            writer = new PrintWriter(new OutputStreamWriter(socket.getOutputStream(), client.getEncoding()), true);
        } catch (Exception e) {
            throw new ConnectionException(e.getMessage(), e);
        }

        // receive welcome message;
        readReply();
    }

    String readReply() throws ConnectionException {
        try {
            String line = reader.readLine();
            if (line == null)
                throw new ConnectionException();

            line = decodeReply(line);
            listener.println("> " + line);

            String replyCode = line.substring(0, 3);
            StringBuilder builder = new StringBuilder(line);
            if (line.charAt(3) == '-') {
                do {
                    line = reader.readLine();
                    line = decodeReply(line);

                    builder.append(Constants.LINE_SEPARATOR).append(line);
                    listener.println("> " + line);
                } while (!line.startsWith(replyCode + " "));
            }

            return builder.toString();
        } catch (IOException e) {
            throw new ConnectionException(e.getMessage(), e);
        }
    }

    public String decodeReply(String reply){
        return reply;
    }

    void sendCommand(String command) throws ConnectionException {
        if (command.startsWith(FTPCommand.PASS) || command.startsWith(FTPCommand.PASS)) {
            String temp = command.substring(0, command.lastIndexOf(" "));
            listener.println(temp + " ******");
        } else
            listener.println(command);

        command = encodeCommand(command);
        writer.println(command);
        if (writer.checkError())
            throw new ConnectionException(Constants.SEND_COMMAND_ERROR);
    }

    public String encodeCommand(String command){
        return command;
    }

    InetAddress getLocalAddress() {
        return socket.getLocalAddress();
    }

    public void close() throws IOException {
        if (writer != null)
            writer.close();
        if (reader != null)
            reader.close();
        if (socket != null)
            socket.close();
        writer = null;
        reader = null;
        socket = null;
    }
}
