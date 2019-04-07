/*
 * Written By : Skye You
 * Copyright (c) 2018 - 2019. All Rights Reserved.
 */

package cn.edu.shu.server.ftp;

import cn.edu.shu.common.ftp.FTPReplyCode;
import cn.edu.shu.common.log.MsgListener;
import cn.edu.shu.server.command.Command;
import cn.edu.shu.server.command.CommandFactory;
import cn.edu.shu.server.util.ConfigUtils;
import org.apache.log4j.Logger;

import java.io.*;
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

    ControlConnection(Socket controlSocket) {
        this.controlSocket = controlSocket;
        logger = Logger.getLogger(getClass());
        encoding = "UTF-8";
        session = new FTPSession(this);
    }

    @Override
    public void run() {
        try {
            controlWriter = new PrintWriter(new OutputStreamWriter(controlSocket.getOutputStream(), encoding), true);
            controlReader = new BufferedReader(new InputStreamReader(controlSocket.getInputStream(), encoding));
            String welcomeMsg = FTPReplyCode.SERVICE_READY + " " + ConfigUtils.getInstance().getWelcomeMessage();
            println(welcomeMsg);

            session.setControlAddress(controlSocket.getLocalAddress());
            session.setEncoding(encoding);

            while (!session.isClosed() && readRequest() != null) {
                FTPRequest ftpRequest = new FTPRequest(request);
                Command command = CommandFactory.getCommand(ftpRequest.getCommand());
                command.execute(session, ftpRequest);
            }
        } catch (IOException e) {
            e.printStackTrace();
        } finally {
            closeSocket();
            listener.println("Disconnected");
        }
    }

    void setListener(MsgListener listener) {
        this.listener = listener;
    }

    @Override
    public void println(String reply) {
        controlWriter.println(reply);
        listener.println(reply);
        logger.info(reply);
    }

    private String readRequest() throws IOException{
        request = controlReader.readLine();
        listener.println(request);
        logger.info(request);
        return request;
    }

    private void closeSocket() {
        try {
            controlReader.close();
            controlWriter.close();
            if (controlSocket != null)
                controlSocket.close();
            controlSocket = null;
        } catch (IOException e) {
            logger.error(e.getMessage(), e);
            e.printStackTrace();
        }
    }
}
