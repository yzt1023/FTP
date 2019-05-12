/*
 * Written By : Skye You
 * Copyright (c) 2018 - 2019. All Rights Reserved.
 */

package cn.edu.shu.server.ftp;

import cn.edu.shu.common.ftp.FTPCommand;
import cn.edu.shu.common.ftp.FTPReplyCode;
import cn.edu.shu.common.log.MsgListener;
import cn.edu.shu.server.command.Command;
import cn.edu.shu.server.command.CommandFactory;
import cn.edu.shu.server.config.SystemConfig;
import org.apache.log4j.Logger;

import java.io.*;
import java.net.InetAddress;
import java.net.Socket;

public class ControlConnection implements Runnable, MsgListener {

    private Socket controlSocket;
    private PrintWriter controlWriter;
    private BufferedReader controlReader;

    private MsgListener listener;
    private Logger logger;

    private String request;
    private FTPSession session;
    private String encoding;
    private boolean closed;
    private SystemConfig config;

    ControlConnection(Socket controlSocket) {
        this.controlSocket = controlSocket;
        logger = Logger.getLogger(getClass());
        config = SystemConfig.getInstance();
        encoding = config.getEncoding();
        session = new FTPSession(this);
        closed = false;
    }

    @Override
    public void run() {
        try {
            controlWriter = new PrintWriter(new OutputStreamWriter(controlSocket.getOutputStream(), encoding), true);
            controlReader = new BufferedReader(new InputStreamReader(controlSocket.getInputStream(), encoding));
            sendWelcomeMessage();

            while (!closed && readRequest() != null) {
                FTPRequest ftpRequest = new FTPRequest(request);
                Command command = CommandFactory.getCommand(ftpRequest.getCommand());
                if (command != null)
                    command.execute(session, ftpRequest);
                else
                    println(FTPReplyCode.NOT_IMPLEMENTED.getReply());
            }
        } catch (IOException e) {
            e.printStackTrace();
        } finally {
            closeSocket();
        }
    }

    private void sendWelcomeMessage() {
        String welcomeMsg = config.getWelcomeMessage();
        int index = welcomeMsg.indexOf("\n");
        while (index != -1) {
            println(FTPReplyCode.SERVICE_READY + "-" + welcomeMsg.substring(0, index));
            welcomeMsg = welcomeMsg.substring(index + 1);
            index = welcomeMsg.indexOf("\n");
        }
        println(FTPReplyCode.SERVICE_READY + " " + welcomeMsg);
    }

    @Override
    public void println(String reply) {
        log(reply);

        if (session.isSecureMode())
            reply = session.encodeResponse(reply);

        controlWriter.println(reply);
    }

    String readRequest() {
        try {
            request = controlReader.readLine();
        } catch (IOException e) {
            e.printStackTrace();
            return null;
        }

        if (request != null) {
            if (session.isSecureMode())
                request = session.decodeRequest(request);

            if (request.startsWith(FTPCommand.PASS) || request.startsWith(FTPCommand.REG)) {
                String temp = request.substring(0, request.lastIndexOf(" "));
                log(temp + " ******");
            } else {
                log(request);
            }
        }
        return request;
    }

    String readLine() {
        try {
            return controlReader.readLine();
        } catch (IOException e) {
            e.printStackTrace();
            return null;
        }
    }

    void sendLine(String line) {
        controlWriter.println(line);
    }

    private void closeSocket() {
        try {
            if (controlReader != null)
                controlReader.close();
            if (controlWriter != null)
                controlWriter.close();
            if (controlSocket != null)
                controlSocket.close();
            controlSocket = null;
        } catch (IOException e) {
            logger.error(e.getMessage(), e);
        }
    }

    public FTPSession getSession() {
        return session;
    }

    void shutdown() {
//        try {
//            controlSocket.shutdownInput();
//            controlSocket.shutdownOutput();
//        } catch (IOException e) {
//            logger.error(e.getMessage(), e);
//        }
    }

    InetAddress getAddress() {
        return controlSocket.getLocalAddress();
    }

    void setListener(MsgListener listener) {
        this.listener = listener;
    }

    String getEncoding() {
        return encoding;
    }

    private void log(String message) {
        listener.println(message);
        logger.info(message);
    }

    String getRootPath() {
        return config.getRootPath();
    }
}
