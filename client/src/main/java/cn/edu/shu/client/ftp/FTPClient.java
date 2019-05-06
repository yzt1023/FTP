/*
 * Written By : Skye You
 * Copyright (c) 2018 - 2019. All Rights Reserved.
 */

package cn.edu.shu.client.ftp;

import cn.edu.shu.common.bean.DataType;
import cn.edu.shu.common.ftp.FTPCommand;
import cn.edu.shu.common.ftp.FTPReplyCode;
import cn.edu.shu.common.log.MsgListener;
import cn.edu.shu.common.util.Constants;
import cn.edu.shu.common.util.MessageUtils;
import cn.edu.shu.common.util.Utils;
import org.apache.log4j.Logger;

import java.io.*;
import java.net.*;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;

public class FTPClient {
    private Utils utils = Utils.getInstance();
    private String currentPath;
    private boolean passiveMode = true;
    private boolean secureMode = false;
    private long restartOffset;
    private DataType dataType;
    private String encoding;
    // server info for reconnect
    private String host;
    private int port;
    private String username;
    private String password;
    // control connection
    private Socket controlSocket;
    private PrintWriter controlWriter;
    private BufferedReader controlReader;
    private String response;

    private MsgListener msgListener;
    private Logger logger = Logger.getLogger(getClass());

    public FTPClient(MsgListener msgListener) {
        this.msgListener = msgListener;
        this.dataType = DataType.BINARY;
        this.restartOffset = 0L;
        this.encoding = "UTF-8";
//        this.encoding = "GB2312";
    }

    private static String getValue(String line, String key) {
        int index = line.indexOf(key);
        return line.substring(index + key.length(), line.indexOf(';', index));
    }

    public boolean connect(String host, int port) {
        try {
            controlSocket = new Socket(host, port);
            controlSocket.setKeepAlive(true);
            controlSocket.setTcpNoDelay(true);
            controlReader = new BufferedReader(new InputStreamReader(controlSocket.getInputStream(), encoding));
            controlWriter = new PrintWriter(new OutputStreamWriter(controlSocket.getOutputStream(), encoding), true);
            readReply();
            this.host = host;
            this.port = port;
            return true;
        } catch (Exception e) {
            logger.error(e.getMessage(), e);
            return false;
        }
    }

    public boolean login(String username, String password) {
        if (controlSocket == null)
            return false;
        sendCommand(FTPCommand.USER + " " + username);
        readReply();
        if (!FTPReplyCode.find(getReplyCode()).isInfoRequested())
            return false;
        sendCommand(FTPCommand.PASS + " " + password);
        readReply();
        if (!FTPReplyCode.find(getReplyCode()).isOkay())
            return false;
        this.username = username;
        this.password = password;
        return true;
    }

    public boolean register(String username, String password) {
        if (controlSocket == null)
            return false;
        sendCommand(FTPCommand.REG + " " + username + " " + password);
        readReply();
        return FTPReplyCode.find(getReplyCode()).isOkay();
    }

    public void disconnect() {
        try {
            if (!isServerClose()) {
                sendCommand(FTPCommand.QUIT);
                readReply();
            }
            if (controlSocket != null)
                controlSocket.close();
            controlSocket = null;
        } catch (IOException e) {
            logger.error(e.getMessage(), e);
        }
    }

    private void sendCommand(String command) {
        if (controlSocket == null) {
            MessageUtils.showInfoMessage("Ftp has not been connected!");
            return;
        }

        controlWriter.println(command);
        if (command.startsWith(FTPCommand.PASS))
            msgListener.println(FTPCommand.PASS + " ******");
        else
            msgListener.println(command);
        logger.info(command);
    }

    public void readReply() {
        try {
            response = controlReader.readLine();
            if(response == null)
                return;
            msgListener.println("> " + response);
            logger.info("> " + response);
        } catch (IOException e) {
            logger.error(e.getMessage(), e);
        }
    }

    public void readReplies() {
        try {
            controlSocket.setSoTimeout(Constants.TIME_OUT);
            while ((response = controlReader.readLine()) != null) {
                msgListener.println("> " + response);
            }
        } catch (SocketTimeoutException ignored) {
        } catch (IOException e) {
            logger.error(e.getMessage(), e);
        }
    }

    public void setPassiveMode(boolean passiveMode) {
        this.passiveMode = passiveMode;
    }

    public void setSecureMode(boolean secureMode) {
        this.secureMode = secureMode;
    }

    private Socket establishDataConnection(String command) {
        if (!openConnectionIfClose())
            return null;
        if (passiveMode)
            return establishPasvDataConnection(command);
        else
            return establishPortDataConnection(command);
    }

    private Socket establishPasvDataConnection(String command) {
        sendCommand(FTPCommand.PASV);
        readReply();
        // get server address
        InetSocketAddress socketAddress = utils.getAddressByString(response.substring(response.indexOf('(') + 1, response.indexOf(')')));
        // local computer connect the remote server
        try {
            Socket socket = new Socket();
            socket.connect(socketAddress);
            sendCommand(command);
            return socket;
        } catch (IOException e) {
            MessageUtils.showInfoMessage("Open passive data connection failed!");
            logger.error(e.getMessage(), e);
            return null;
        }
    }

    private Socket establishPortDataConnection(String command) {
        // produce a random port number
        InetAddress localAddress = controlSocket.getLocalAddress();
        ServerSocket serverSocket;
        try {
            serverSocket = new ServerSocket(0, 1, localAddress);
        } catch (IOException e) {
            logger.error(e.getMessage(), e);
            MessageUtils.showInfoMessage("Open active data connection failed!");
            return null;
        }
        InetSocketAddress socketAddress = new InetSocketAddress(localAddress, serverSocket.getLocalPort());
        String str = utils.getStringByAddress(socketAddress);
        sendCommand(FTPCommand.PORT + " " + str);
        readReply();
        if (!FTPReplyCode.find(getReplyCode()).isOkay())
            return null;
        sendCommand(command);
        try {
            Socket socket = serverSocket.accept();
            serverSocket.close();
            return socket;
        } catch (IOException e) {
            logger.error(e.getMessage(), e);
            MessageUtils.showInfoMessage("Open active data connection failed!");
            return null;
        }
    }

    public synchronized String printWorkingDir() {
        sendCommand(FTPCommand.PWD);
        readReply();
        currentPath = response.substring(response.indexOf('\"') + 1, response.lastIndexOf('\"'));
        return currentPath;
    }

    public synchronized List<FTPFile> getFiles(FTPFile parent) {
        try {
            Socket socket = establishDataConnection(FTPCommand.MLSD + " " + parent.getPath());
            if (socket == null)
                return null;

            readReply();
            if (!FTPReplyCode.find(getReplyCode()).isReady())
                return null;

            InputStream in = socket.getInputStream();
            BufferedReader reader = new BufferedReader(new InputStreamReader(in));
            String line;
            List<FTPFile> files = new ArrayList<>();
            while ((line = reader.readLine()) != null) {
                FTPFile child = parseMLSD(line, parent);
                files.add(child);
            }
            parent.setChildren(files);

            reader.close();
            socket.close();
            readReply(); // after transfer
            return files;
        } catch (IOException e) {
            MessageUtils.showInfoMessage("Data stream transfer failed!");
            logger.error(e.getMessage(), e);
            return null;
        }
    }

    public synchronized boolean changeDirectory(String dir) {
        if (!openConnectionIfClose())
            return false;
        sendCommand(FTPCommand.CWD + " " + dir);
        readReply();
        if (!FTPReplyCode.find(getReplyCode()).isOkay())
            return false;
        currentPath = dir;
        return true;
    }

    public synchronized boolean changeUpToParent() {
        if (!openConnectionIfClose())
            return false;
        if (Constants.SEPARATOR.equals(currentPath))
            return false;
        sendCommand(FTPCommand.CDUP);
        readReply();
        if (!FTPReplyCode.find(getReplyCode()).isOkay())
            return false;
        currentPath = response.substring(response.indexOf('\"') + 1, response.lastIndexOf('\"'));
        return true;
    }

    public String getCurrentPath() {
        return currentPath;
    }

    /**
     * download file from remote machine
     *
     * @param filename the name of file under the current working dir
     * @return input stream if download successfully, otherwise null
     */
    public synchronized InputStream getRetrStream(String filename) {
        Socket socket = establishDataConnection(FTPCommand.TYPE + " " + dataType.toString());
        if (socket == null)
            return null;

        readReply();
        // send download command
        if (restartOffset > 0L) {
            sendCommand(FTPCommand.REST + " " + restartOffset);
            readReply();
            if (!FTPReplyCode.find(getReplyCode()).isInfoRequested())
                return null;
        }

        sendCommand(FTPCommand.RETR + " " + filename);
        readReply();
        if (!FTPReplyCode.find(getReplyCode()).isReady()) {
            return null;
        }
        try {
            return socket.getInputStream();
        } catch (IOException e) {
            logger.error(e.getMessage(), e);
        }
        return null;
    }

    /**
     * upload file to the remote machine
     *
     * @param filename the name of file under the current working dir
     * @return output stream if upload successfully, otherwise null
     */
    public synchronized OutputStream getStorStream(String filename) {
        Socket socket = establishDataConnection(FTPCommand.TYPE + " " + dataType.toString());
        if (socket == null)
            return null;
        readReply();
        if (restartOffset > 0L) {
            sendCommand(FTPCommand.REST + " " + restartOffset);
            readReply();
            if (!FTPReplyCode.find(getReplyCode()).isInfoRequested())
                return null;
        }

        sendCommand(FTPCommand.STOR + " " + filename);
        readReply();
        if (!FTPReplyCode.find(getReplyCode()).isReady()) {
            return null;
        }
        try {
            return socket.getOutputStream();
        } catch (IOException e) {
            logger.error(e.getMessage(), e);
        }
        return null;
    }

    public synchronized OutputStream getAppeStream(String filename) {
        Socket socket = establishDataConnection(FTPCommand.TYPE + " " + dataType.toString());
        if (socket == null)
            return null;
        readReply();
        sendCommand(FTPCommand.APPE + " " + filename);
        readReply();
        if (!FTPReplyCode.find(getReplyCode()).isReady()) {
            return null;
        }
        try {
            return socket.getOutputStream();
        } catch (IOException e) {
            logger.error(e.getMessage(), e);
        }
        return null;
    }

    public synchronized boolean rename(String src, String dst) {
        if (!openConnectionIfClose())
            return false;
        sendCommand(FTPCommand.RNFR + " " + src);
        readReply();
        if (!FTPReplyCode.find(getReplyCode()).isInfoRequested())
            return false;
        sendCommand(FTPCommand.RNTO + " " + dst);
        readReply();
        return FTPReplyCode.find(getReplyCode()).isOkay();
    }

    public synchronized boolean delete(String filename) {
        if (!openConnectionIfClose())
            return false;
        sendCommand(FTPCommand.DELE + " " + filename);
        readReply();
        return FTPReplyCode.find(getReplyCode()).isOkay();
    }

    public synchronized boolean makeDirectory(String dir) {
        if (!openConnectionIfClose())
            return false;
        sendCommand(FTPCommand.MKD + " " + dir);
        readReply();
        return FTPReplyCode.find(getReplyCode()).isOkay();
    }

    public synchronized boolean removeDirectory(String dir) {
        if (!openConnectionIfClose())
            return false;
        sendCommand(FTPCommand.RMD + " " + dir);
        readReply();
        return FTPReplyCode.find(getReplyCode()).isOkay();
    }

    public synchronized long getFileSize(String filePath){
        sendCommand(FTPCommand.SIZE + " " + filePath);
        readReply();
        if(FTPReplyCode.find(getReplyCode()).isOkay()) {
            String size = response.substring(response.indexOf(" ") + 1);
            return Long.parseLong(size);
        }
        return -1;
    }

    public synchronized boolean noop() {
        sendCommand(FTPCommand.NOOP);
        readReply();
        return FTPReplyCode.find(getReplyCode()).isOkay();
    }

    private boolean isServerClose() {
        controlWriter.println(FTPCommand.NOOP);
        boolean closed;
        try {
            closed = controlReader.readLine() == null;
        } catch (IOException e) {
            closed = true;
        }
        if (closed)
            msgListener.println("Disconnected by server");
        return closed;
    }

    private boolean reconnect() {
        closeControlSocket();
        msgListener.println("Trying to reconnect to server");
        if (!connect(host, port)) {
            msgListener.println("Reconnect failed!");
            return false;
        }
        login(username, password);
        return true;
    }

    private boolean openConnectionIfClose() {
        if (isServerClose())
            return reconnect();
        return true;
    }

    private FTPFile parseMLSD(String line, FTPFile parent) {
        String name = line.substring(line.lastIndexOf(";") + 2, line.length());
        FTPFile file = new FTPFile(parent, name);
        Date date = utils.parseDate(getValue(line, Constants.KEY_MODIFY));
        file.setLastChanged(date);
        file.setType(getValue(line, Constants.KEY_TYPE));
        if (!file.isDirectory())
            file.setSize(Long.parseLong(getValue(line, Constants.KEY_SIZE)));
        file.setPath(utils.getPath(parent.getPath(), file.getName()));
        return file;
    }

    public void setRestartOffset(long restartOffset) {
        this.restartOffset = restartOffset;
    }

    public DataType getDataType() {
        return dataType;
    }

    public void setDataType(DataType type) {
        this.dataType = type;
    }

    public void setEncoding(String encoding) {
        this.encoding = encoding;
    }

    private int getReplyCode() {
        return Integer.parseInt(response.substring(0, 3));
    }

    public void closeControlSocket() {
        try {
            if (controlWriter != null)
                controlWriter.close();
            controlWriter = null;
            if (controlReader != null)
                controlReader.close();
            controlReader = null;
            if (controlSocket != null)
                controlSocket.close();
            controlSocket = null;
        } catch (IOException e) {
            logger.error(e.getMessage(), e);
        }
    }
}
