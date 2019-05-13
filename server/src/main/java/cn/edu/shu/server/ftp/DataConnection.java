/*
 * Written By : Skye You
 * Copyright (c) 2018 - 2019. All Rights Reserved.
 */

package cn.edu.shu.server.ftp;

import cn.edu.shu.common.bean.DataType;
import cn.edu.shu.common.util.CommonUtils;
import cn.edu.shu.common.util.Constants;
import org.apache.log4j.Logger;

import java.io.*;
import java.net.InetAddress;
import java.net.InetSocketAddress;
import java.net.ServerSocket;
import java.net.Socket;
import java.util.List;

public class DataConnection {

    private Socket dataSocket;
    private ServerSocket serverSocket;
    private boolean passiveMode;
    private InetSocketAddress socketAddress;
    private Logger logger = Logger.getLogger(getClass());
    private CommonUtils utils = CommonUtils.getInstance();

    public void initActiveDataConnection(InetSocketAddress socketAddress) {
        closeConnection();
        passiveMode = false;
        this.socketAddress = socketAddress;
    }

    public InetSocketAddress initPassiveDataConnection(InetAddress inetAddress) {
        closeConnection();
        passiveMode = true;
        try {
            serverSocket = new ServerSocket(0, 1, inetAddress);
        } catch (IOException e) {
            logger.error(e.getMessage(), e);
            closeConnection();
            return null;
        }
        socketAddress = new InetSocketAddress(inetAddress, serverSocket.getLocalPort());
        return socketAddress;
    }

    public void openConnection() throws Exception {
        if (passiveMode) {
            dataSocket = serverSocket.accept();
        } else {
            dataSocket = new Socket();
            dataSocket.connect(socketAddress);
        }
    }

    public void closeConnection() {
        try {
            if (dataSocket != null)
                dataSocket.close();
            dataSocket = null;
            if (serverSocket != null)
                serverSocket.close();
            serverSocket = null;
        } catch (IOException e) {
            logger.error(e.getMessage(), e);
        }
    }

    public void transferToClient(FTPSession session, InputStream in) throws IOException {
        OutputStream out = dataSocket.getOutputStream();
        transfer(session, true, in, out);
        out.close();
    }

    public void transferToClient(FTPSession session, List<String> files) throws IOException {
        OutputStream out = dataSocket.getOutputStream();
        OutputStreamWriter writer = new OutputStreamWriter(out, session.getEncoding());
        for (String file : files) {
            if (session.isSecureMode())
                file = session.encodeResponse(file);
            writer.write(file + Constants.LINE_SEPARATOR);
        }
        writer.flush();
        writer.close();
    }

    public void transferFromClient(FTPSession session, OutputStream out) throws IOException {
        InputStream in = dataSocket.getInputStream();
        transfer(session, false, in, out);
        in.close();
    }

    private void transfer(FTPSession session, boolean isWrite, InputStream in, OutputStream out) throws IOException {
        int buff = Constants.KB;
        if (!isWrite)
            buff += 16;

        byte[] bytes = new byte[Constants.KB + 16];
        boolean lastWasCR = false;
        BufferedInputStream bis = new BufferedInputStream(in);
        BufferedOutputStream bos = new BufferedOutputStream(out);
        int len;
        while ((len = bis.read(bytes, 0, buff)) != -1) {
            if (isWrite) {
                if (session.isSecureMode()) {
                    bytes = session.encodeBytes(bytes, len);
                    int fill = 16 - (len % 16);
                    len = len + fill;
                }

                if (session.getDataType() == DataType.BINARY)
                    bos.write(bytes, 0, len);
                else {
                    for (int i = 0; i < len; i++) {
                        lastWasCR = utils.toNetWrite(lastWasCR, bos, bytes[i]);
                    }
                }
            } else {
                if (session.isSecureMode()) {
                    len = session.decodeBytes(bytes, len);
                }

                if (session.getDataType() == DataType.BINARY || utils.noConversionRequired())
                    bos.write(bytes, 0, len);
                else {
                    for (int i = 0; i < len; i++) {
                        lastWasCR = utils.fromNetWrite(lastWasCR, bos, bytes[i]);
                    }
                }
            }
        }
        bos.flush();
    }

    public InetSocketAddress getSocketAddress() {
        return socketAddress;
    }

}
