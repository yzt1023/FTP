/*
 * Written By : Skye You
 * Copyright (c) 2018 - 2019. All Rights Reserved.
 */

package cn.edu.shu.server.ftp;

import cn.edu.shu.common.bean.DataType;
import cn.edu.shu.common.util.CommonUtils;
import cn.edu.shu.common.util.Constants;
import cn.edu.shu.server.config.SystemConfig;
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
        SystemConfig config = SystemConfig.getInstance();
        int port = utils.producePort(config.getPassiveMinPort(), config.getPassiveMaxPort());
        try {
            serverSocket = new ServerSocket(port, 1, inetAddress);
        } catch (IOException e) {
            logger.error(e.getMessage(), e);
            closeConnection();
        }

        socketAddress = new InetSocketAddress(inetAddress, serverSocket.getLocalPort());
        return socketAddress;
    }

    public void openConnection() throws Exception {
        if (passiveMode) {
            dataSocket = serverSocket.accept();
        } else {
            dataSocket = new Socket();
            dataSocket.bind(new InetSocketAddress(Constants.DEFAULT_DATA_PORT));
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

        if(session.getDataType() == DataType.ASCII){
            BufferedReader reader = new BufferedReader(new InputStreamReader(in));
            PrintWriter writer = new PrintWriter(out, true);
            String line;
            while((line = reader.readLine()) != null){
                if(isWrite && session.isSecureMode())
                    line = session.encodeResponse(line);
                else if(session.isSecureMode()){
                    line = session.decodeRequest(line);
                }

                writer.println(line);
            }
        }else{
            byte[] bytes = new byte[Constants.KB + 16];
            BufferedInputStream bis = new BufferedInputStream(in);
            BufferedOutputStream bos = new BufferedOutputStream(out);
            int len;
            while ((len = bis.read(bytes, 0, buff)) != -1) {
                if(isWrite && session.isSecureMode()){
                    bytes = session.encodeBytes(bytes, len);
                    int fill = 16 - (len % 16);
                    len = len + fill;
                }else if(session.isSecureMode()){
                    len = session.decodeBytes(bytes, len);
                }

                bos.write(bytes, 0, len);
            }
            bos.flush();
        }
    }

    public InetSocketAddress getSocketAddress() {
        return socketAddress;
    }

}
