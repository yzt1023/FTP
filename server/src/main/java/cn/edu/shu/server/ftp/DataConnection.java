/*
 * Written By : Skye You
 * Copyright (c) 2018 - 2019. All Rights Reserved.
 */

package cn.edu.shu.server.ftp;

import cn.edu.shu.common.bean.DataType;
import cn.edu.shu.common.util.Constants;
import cn.edu.shu.common.util.Utils;
import org.apache.log4j.Logger;

import javax.net.ssl.KeyManagerFactory;
import javax.net.ssl.SSLContext;
import javax.net.ssl.SSLSocket;
import javax.net.ssl.SSLSocketFactory;
import java.io.*;
import java.net.InetAddress;
import java.net.InetSocketAddress;
import java.net.ServerSocket;
import java.net.Socket;
import java.security.KeyStore;

public class DataConnection {

    private Socket dataSocket;
    private ServerSocket serverSocket;
    private boolean passiveMode;
    private boolean secureMode;
    private SSLContext sslContext;
    private InetSocketAddress socketAddress;
    private Logger logger = Logger.getLogger(getClass());
    private Utils utils = Utils.getInstance();

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
            if (secureMode) {
                Socket socket = serverSocket.accept();
                SSLSocketFactory factory = sslContext.getSocketFactory();
                SSLSocket sslSocket = (SSLSocket) factory.createSocket(socket, socket.getInetAddress().getHostName(), socket.getPort(), true);
                sslSocket.setUseClientMode(false);
                sslSocket.setNeedClientAuth(true);
                dataSocket = sslSocket;
            } else {
                dataSocket = serverSocket.accept();
            }
        } else {
            if (secureMode) {
                SSLSocketFactory socFactory = sslContext.getSocketFactory();
                SSLSocket ssoc = (SSLSocket) socFactory.createSocket();
                ssoc.setUseClientMode(false);
                dataSocket = ssoc;
            } else {
                dataSocket = new Socket();
            }
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

    public void transferToClient(FTPSession session, String line) throws IOException {
        OutputStream out = dataSocket.getOutputStream();
        OutputStreamWriter writer = new OutputStreamWriter(out, session.getEncoding());
        writer.write(line);
        writer.flush();
        writer.close();
    }

    public void transferFromClient(FTPSession session, OutputStream out) throws IOException {
        InputStream in = dataSocket.getInputStream();
        transfer(session, false, in, out);
        in.close();
    }

    private void transfer(FTPSession session, boolean isWrite, InputStream in, OutputStream out) throws IOException {
        byte[] bytes = new byte[Constants.KB];
        boolean lastWasCR = false;
        BufferedInputStream bis = new BufferedInputStream(in);
        BufferedOutputStream bos = new BufferedOutputStream(out);
        int len;
        while ((len = bis.read(bytes)) != -1) {
            if (session.getDataType() == DataType.BINARY)
                bos.write(bytes, 0, len);
            else if (isWrite) {
                for (byte b : bytes) {
                    lastWasCR = utils.toNetWrite(lastWasCR, bos, b);
                }
            } else if (utils.noConversionRequired()) {
                bos.write(bytes, 0, len);
            } else {
                for (byte b : bytes) {
                    lastWasCR = utils.fromNetWrite(lastWasCR, bos, b);
                }
            }
        }
        bos.flush();
    }

    public InetSocketAddress getSocketAddress() {
        return socketAddress;
    }

    public void setSecureMode(boolean secureMode) {
        this.secureMode = secureMode;
    }

    void setSslContext(SSLContext sslContext) {
        this.sslContext = sslContext;
    }
}
