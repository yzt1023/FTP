/*
 * Written By : Skye You
 * Copyright (c) 2018 - 2019. All Rights Reserved.
 */

package cn.edu.shu.client.ftp;

import cn.edu.shu.client.util.Helper;
import cn.edu.shu.common.log.Logger;
import cn.edu.shu.client.util.Constants;
import cn.edu.shu.common.util.MessageUtils;

import java.io.*;
import java.net.ServerSocket;
import java.net.Socket;
import java.net.SocketTimeoutException;
import java.util.ArrayList;
import java.util.List;
import java.util.StringTokenizer;


public class FTPClient {
    private String currentPath;
    private boolean passiveMode = true;
    private boolean secureMode = false;
    private long restartOffset;
    private String type;
    private String encoding;
    // server info
    private String host;
    private int port;
    private String username;
    private String password;
    // control connection
    private Socket controlSocket;
    private PrintWriter controlWriter;
    private BufferedReader controlReader;
    // data connection
    private Socket dataSocket;
    private ServerSocket serverSocket;
    private OutputStream dataWriter;
    private InputStream dataReader;
    private String response;

    private Logger logger;

    public FTPClient(Logger logger) {
        this.logger = logger;
        this.type = FTPCommands.TYPE_I;
        this.restartOffset = 0L;
        this.encoding = "ISO8859_1";
    }

    public synchronized boolean connect(String host) {
        return connect(host, Constants.DEFAULT_PORT);
    }

    public synchronized boolean connect(String host, int port) {
        return connect(host, port, Constants.ANONYMOUS_USER, "");
    }

    public synchronized boolean connect(String host, int port, String username, String password) {
        try {
            controlSocket = new Socket(host, port);
            controlSocket.setKeepAlive(true);
            controlSocket.setTcpNoDelay(true);
            controlReader = new BufferedReader(new InputStreamReader(controlSocket.getInputStream(), encoding));
            controlWriter = new PrintWriter(new OutputStreamWriter(controlSocket.getOutputStream(), encoding));
            readReplies();
            sendCommand(FTPCommands.USER + " " + username);
            if (!FTPCommands.infoRequested(readReply()))
                return false;
            sendCommand(FTPCommands.PASS + " " + password);
            if(!FTPCommands.isCommandOkay(readReply()))
                return false;
            this.host = host;
            this.port = port;
            this.username = username;
            this.password = password;
            return true;
        } catch (Exception e) {
            e.printStackTrace();
            return false;
        }
    }

    public synchronized void disconnect() {
        try {
            if(!isServerClose()) {
                sendCommand(FTPCommands.QUIT);
                readReply();
            }
            controlSocket.close();
            if (dataSocket != null)
                dataSocket.close();
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    private synchronized void sendCommand(String command) {
        if (controlSocket == null) {
            MessageUtils.showErrorMessage("cn.edu.shu.ftp is not connect!");
            return;
        }
        controlWriter.write(command + "\r\n");
        controlWriter.flush();
        if (command.startsWith(FTPCommands.PASS))
            logger.log(FTPCommands.PASS + " ******");
        else
            logger.log(command);
    }

    public synchronized String readReply(){
        try {
            response = controlReader.readLine();
            logger.log("> " + response);
        } catch (IOException e) {
            MessageUtils.showErrorMessage(Constants.RESPONSE_ERROR);
            e.printStackTrace();
        }
        return response;
    }

    private synchronized void readReplies() {
        try {
            controlSocket.setSoTimeout(Constants.TIME_OUT);
            while ((response = controlReader.readLine()) != null) {
                logger.log("> " + response);
            }
        } catch (SocketTimeoutException ignored) {
        } catch (IOException e) {
            MessageUtils.showErrorMessage(Constants.RESPONSE_ERROR);
            e.printStackTrace();
        }
    }

    public void setPassiveMode(boolean passiveMode) {
        this.passiveMode = passiveMode;
    }

    public void setSecureMode(boolean secureMode){
        this.secureMode = secureMode;
    }

    private synchronized boolean establishDataConnect() {
        openConnectionIfClose();
        if (passiveMode && pasvDataConnect())
            return true;
        else return !passiveMode && portDataConnect();
    }

    private synchronized boolean pasvDataConnect() {
        sendCommand(FTPCommands.PASV);
        readReply();
        // get server address
        String ipAndPort = response.substring(response.indexOf('(') + 1, response.indexOf(')'));
        StringTokenizer tokenizer = new StringTokenizer(ipAndPort, ",");
        String ip = tokenizer.nextToken() + '.' + tokenizer.nextToken() + '.'
                + tokenizer.nextToken() + '.' + tokenizer.nextToken();
        int port = Integer.parseInt(tokenizer.nextToken()) * 256 + Integer.parseInt(tokenizer.nextToken());
        // local computer connect the remote server
        try {
            dataSocket = new Socket(ip, port);
            dataReader = dataSocket.getInputStream();
            dataWriter = dataSocket.getOutputStream();
        } catch (IOException e) {
            MessageUtils.showErrorMessage("Open passive data connection failed!");
            e.printStackTrace();
            return false;
        }
        return true;
    }

    private synchronized boolean portDataConnect() {
        // produce a random port number
        try {
            serverSocket = new ServerSocket(0);
            Thread thread = new Thread() {
                @Override
                public void run() {
                    try {
                        dataSocket = serverSocket.accept();
                        dataReader = dataSocket.getInputStream();
                        dataWriter = dataSocket.getOutputStream();
                    } catch (IOException e) {
                        MessageUtils.showErrorMessage("Open active data connection failed!");
                        e.printStackTrace();
                    }
                }
            };
            thread.start();

            String localIp = controlSocket.getLocalAddress().getHostAddress();
            int localPort = serverSocket.getLocalPort();
            sendCommand(FTPCommands.PORT + " " + localIp.replace('.', ',')
                    + ',' + (localPort >> 8) + ',' + (localPort & 0xff));
            if (!FTPCommands.isCommandOkay(readReply()))
                return false;
        } catch (IOException e) {
            MessageUtils.showErrorMessage("Open active data connection failed!");
            e.printStackTrace();
            return false;
        }
        return true;
    }

    public synchronized void destroyDataConnect() {
        try {
            dataWriter.close();
            dataReader.close();
            if (serverSocket != null)
                serverSocket.close();
            if (dataSocket != null)
                dataSocket.close();
        } catch (IOException e) {
            MessageUtils.showErrorMessage("Destroy data connection failed");
            e.printStackTrace();
        }
    }

    public synchronized String printWorkingDir() {
//        openConnectionIfClose();
        sendCommand(FTPCommands.PWD);
        readReply();
        currentPath = response.substring(response.indexOf('\"') + 1, response.lastIndexOf('\"'));
        return currentPath;
    }

    public synchronized FTPFile[] getFiles(FTPFile parent) {
        try {
            if (!establishDataConnect())
                return new FTPFile[0];
            boolean parseMLSD = true;
            sendCommand(FTPCommands.MLSD + " " + parent.getPath());
            if(!FTPCommands.isReady(readReply())){
                parseMLSD = false;
                sendCommand(FTPCommands.LIST + " " + parent.getPath());
                readReply();
            }
            byte[] bytes = new byte[Constants.KB]; // read 1kb once
            ByteArrayOutputStream outputStream = new ByteArrayOutputStream(); // store data temporary
            int len;
            while ((len = dataReader.read(bytes)) != -1)
                outputStream.write(bytes, 0, len);
            outputStream.flush();

            ByteArrayInputStream inputStream = new ByteArrayInputStream(outputStream.toByteArray());
            BufferedReader reader = new BufferedReader(new InputStreamReader(inputStream, encoding));
            String line;
            List<FTPFile> files = new ArrayList<>();
            if(parseMLSD) {
                while ((line = reader.readLine()) != null) {
                    FTPFile child = Helper.parseMLSD(line, parent);
                    files.add(child);
                }
            }else if((line = reader.readLine()) != null){
                int length = line.split("\\s+").length;
                if(length == 4) {
                    FTPFile child = Helper.parseDOS(line, parent);
                    files.add(child);
                    while ((line = reader.readLine()) != null) {
                        child = Helper.parseDOS(line, parent);
                        files.add(child);
                    }
                }else if(length == 9){
                    FTPFile child = Helper.parseUNIX(line, parent);
                    files.add(child);
                    while ((line = reader.readLine()) != null) {
                        child = Helper.parseUNIX(line, parent);
                        files.add(child);
                    }
                }else {
                    MessageUtils.showErrorMessage("Format of list command is unknown!");
                    return new FTPFile[0];
                }

            }
            destroyDataConnect();
            readReply(); // after transfer
            FTPFile[] ftpFiles = new FTPFile[files.size()];
            files.toArray(ftpFiles);
            parent.setChildren(ftpFiles);
            return ftpFiles;
        } catch (IOException e) {
            MessageUtils.showErrorMessage("Data stream transfer failed!");
            e.printStackTrace();
            return new FTPFile[0];
        }
    }

    public synchronized boolean changeDirectory(String dir) {
        openConnectionIfClose();
        sendCommand(FTPCommands.CWD + " " + dir);
        if (!FTPCommands.isCommandOkay(readReply()))
            return false;
        currentPath = dir;
        return true;
    }

    public synchronized boolean changeUpToParent() {
        openConnectionIfClose();
        if(Constants.SEPARATOR.equals(currentPath))
            return false;
        sendCommand(FTPCommands.CDUP);
        if (!FTPCommands.isCommandOkay(readReply()))
            return false;
        currentPath = response.substring(response.indexOf('\"') + 1, response.lastIndexOf('\"'));
        return true;
    }

    public String getCurrentPath() {
        return currentPath;
    }

    /**
     * download file from remote machine
     * @param filename the name of file under the current working dir
     * @return input stream if download successfully, otherwise null
     */
    public synchronized InputStream getRetrStream(String filename) {
        if (!establishDataConnect())
            return null;
        // set transfer type to binary
        sendCommand(type);
        readReply();
        // send download command
        if (restartOffset > 0L) {
            sendCommand(FTPCommands.REST + " " + restartOffset);
            if (!FTPCommands.infoRequested(readReply()))
                return null;
        }
        sendCommand(FTPCommands.RETR + " " + filename);
        if (!FTPCommands.isReady(readReply()))
            return null;
        return dataReader;
    }

    /**
     * upload file to the remote machine
     * @param filename the name of file under the current working dir
     * @return output stream if upload successfully, otherwise null
     */
    public synchronized OutputStream getStorStream(String filename) {
        if (!establishDataConnect())
            return null;
        sendCommand(type);
        readReply();
        sendCommand(FTPCommands.STOR + " " + filename);
        if (!FTPCommands.isReady(readReply()))
            return null;
        return dataWriter;
    }

    public synchronized OutputStream getAppeStream(String filename){
        if(!establishDataConnect())
            return null;
        sendCommand(type);
        readReply();
        sendCommand(FTPCommands.APPE + " " + filename);
        if(!FTPCommands.isReady(readReply()))
            return null;
        return dataWriter;
    }

    public synchronized boolean rename(String src, String dst){
        openConnectionIfClose();
        sendCommand(FTPCommands.RNFR + " " + src);
        if(!FTPCommands.infoRequested(readReply()))
            return false;
        sendCommand(FTPCommands.RNTO + " " + dst);
        return FTPCommands.isCommandOkay(readReply());
    }

    public synchronized boolean delete(String filename) {
        openConnectionIfClose();
        sendCommand(FTPCommands.DELE + " " + filename);
        return FTPCommands.isCommandOkay(readReply());
    }

    public synchronized boolean makeDirectory(String dir) {
        openConnectionIfClose();
        sendCommand(FTPCommands.MKD + " " + dir);
        return FTPCommands.isCommandOkay(readReply());
    }

    public synchronized boolean removeDirectory(String dir) {
        openConnectionIfClose();
        sendCommand(FTPCommands.RMD + " " + dir);
        return FTPCommands.isCommandOkay(readReply());
    }

    public synchronized boolean noop(){
        sendCommand(FTPCommands.NOOP);
        return FTPCommands.isCommandOkay(readReply());
    }

    private boolean isServerClose(){
        controlWriter.write(FTPCommands.NOOP + "\r\n");
        controlWriter.flush();
        try{
            controlReader.readLine();
        }catch (IOException e) {
            return true;
        }
        return false;
    }

    private void reconnect(){
        try {
            controlReader.close();
            controlWriter.close();
            controlSocket.close();
            connect(host, port, username, password);
        }catch (IOException e){
            MessageUtils.showErrorMessage(Constants.RECONNECT_FAILED);
        }
    }

    private void openConnectionIfClose(){
        if(isServerClose())
            reconnect();
    }

    public void setRestartOffset(long restartOffset) {
        this.restartOffset = restartOffset;
    }

    public void setType(String type) {
        this.type = type;
    }

    public void setEncoding(String encoding) {
        this.encoding = encoding;
    }
}
