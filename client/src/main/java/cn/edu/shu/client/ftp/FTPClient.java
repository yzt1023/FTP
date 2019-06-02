/*
 * Written By : Skye You
 * Copyright (c) 2018 - 2019. All Rights Reserved.
 */

package cn.edu.shu.client.ftp;

import cn.edu.shu.client.config.SystemConfig;
import cn.edu.shu.client.exception.ConnectionException;
import cn.edu.shu.client.exception.FTPException;
import cn.edu.shu.client.exception.NoPermissionException;
import cn.edu.shu.common.bean.DataType;
import cn.edu.shu.common.bean.User;
import cn.edu.shu.common.ftp.FTPCommand;
import cn.edu.shu.common.ftp.FTPReplyCode;
import cn.edu.shu.common.log.MsgListener;
import cn.edu.shu.common.util.CommonUtils;
import cn.edu.shu.common.util.Constants;
import cn.edu.shu.common.util.SecurityUtils;

import java.io.*;
import java.net.InetAddress;
import java.net.InetSocketAddress;
import java.net.ServerSocket;
import java.net.Socket;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;

public class FTPClient {
    private CommonUtils utils = CommonUtils.getInstance();
    private SecurityUtils securityUtils;
    private String currentPath;
    private boolean passiveMode;
    private boolean secureMode;
    private long restartOffset;
    private DataType dataType;
    private String encoding;
    private String host;
    private int port;
    private PlainConnection controlConnection;
    private User user;

    private String response;
    private MsgListener msgListener;
    private SystemConfig config;

    public FTPClient(MsgListener msgListener) {
        this.config = SystemConfig.getInstance();

        this.msgListener = msgListener;
        this.restartOffset = 0L;
        this.encoding = config.getEncoding();
        this.passiveMode = config.isDefaultPassive();
        this.secureMode = config.isDefaultSecure();
        securityUtils = SecurityUtils.getInstance();
    }

    private static String getValue(String line, String key) {
        int index = line.indexOf(key);
        return line.substring(index + key.length(), line.indexOf(';', index));
    }


    public synchronized void connect(String host, int port) throws ConnectionException {
        if (secureMode)
            controlConnection = new SecureConnection(this);
        else
            controlConnection = new PlainConnection(this);
        controlConnection.connect(host, port);
        this.host = host;
        this.port = port;
    }

    public synchronized void login(String username, String password) throws ConnectionException, FTPException {
        password = SecurityUtils.getMd5(password);
        reLogin(username, password);
    }

    private synchronized void reLogin(String username, String password) throws ConnectionException, FTPException {
        if (controlConnection == null)
            return;

        executeCommand(FTPCommand.USER + " " + username);
        if (!FTPReplyCode.find(getReplyCode()).isInfoRequested())
            throw new FTPException(response);

        executeCommand(FTPCommand.PASS + " " + password);
        if (!FTPReplyCode.find(getReplyCode()).isOkay())
            throw new FTPException(response);

        user = new User(username, password);
        int index = response.indexOf('(');
        if (index > 0) {
            String permission = response.substring(index + 1);
            user.setReadable(permission.charAt(0) == '1');
            user.setWritable(permission.charAt(1) == '1');
            user.setDeleted(permission.charAt(2) == '1');
        }
    }

    public synchronized void register(String username, String password) throws ConnectionException, FTPException {
        if (controlConnection == null)
            throw new FTPException(Constants.CONNECT_FIRST);

        password = SecurityUtils.getMd5(password);
        executeCommand(FTPCommand.REG + " " + username + " " + password);
        if (!FTPReplyCode.find(getReplyCode()).isOkay())
            throw new FTPException(response);
    }

    private synchronized void executeCommand(String command) throws ConnectionException {
        sendCommand(command);
        readReply();
    }

    public synchronized String readReply() throws ConnectionException {
        response = controlConnection.readReply();
        return response;
    }

    public synchronized void sendCommand(String command) throws ConnectionException {
        if (controlConnection == null)
            return;

        try {
            controlConnection.sendCommand(command);
        } catch (ConnectionException e) {
            try {
                controlConnection.close();
            } catch (IOException ex) {
                throw new ConnectionException(ex.getMessage(), ex);
            }
            throw e;
        }
    }

    public void disconnect() throws ConnectionException, IOException {
        if (controlConnection == null)
            return;

        executeCommand(FTPCommand.QUIT);
        controlConnection.close();
    }

    private synchronized Socket establishDataConnection(String command)
            throws ConnectionException, IOException, FTPException {
        if (passiveMode)
            return establishPasvDataConnection(command);
        else
            return establishPortDataConnection(command);
    }

    private synchronized Socket establishPasvDataConnection(String command)
            throws ConnectionException, IOException {
        executeCommand(FTPCommand.PASV);
        // get server address
        InetSocketAddress socketAddress = utils.getAddressByString(response.substring(response.indexOf('(') + 1, response.indexOf(')')));
        // local computer connect the remote server
        Socket socket = new Socket();
        socket.connect(socketAddress);
        sendCommand(command);

        return socket;
    }

    private synchronized Socket establishPortDataConnection(String command)
            throws ConnectionException, IOException, FTPException {
        // produce a random port number
        InetAddress localAddress = controlConnection.getLocalAddress();
        int port = utils.producePort(config.getActiveMinPort(), config.getActiveMaxPort());
        ServerSocket serverSocket = new ServerSocket(port, 1, localAddress);

        InetSocketAddress socketAddress = new InetSocketAddress(localAddress, serverSocket.getLocalPort());
        String str = utils.getStringByAddress(socketAddress);
        executeCommand(FTPCommand.PORT + " " + str);
        if (!FTPReplyCode.find(getReplyCode()).isOkay())
            throw new FTPException(response);

        sendCommand(command);
        Socket socket = serverSocket.accept();
        serverSocket.close();
        return socket;
    }

    public synchronized String printWorkingDir() throws ConnectionException {
        executeCommand(FTPCommand.PWD);
        currentPath = response.substring(response.indexOf('\"') + 1, response.lastIndexOf('\"'));
        return currentPath;
    }

    public synchronized List<FTPFile> getFiles(FTPFile parent)
            throws ConnectionException, IOException, FTPException, NoPermissionException {
        if (!user.isReadable())
            throw new NoPermissionException();

        Socket socket = establishDataConnection(FTPCommand.MLSD + " " + parent.getPath());

        readReply();
        if (!FTPReplyCode.find(getReplyCode()).isReady())
            throw new FTPException(response);

        InputStream in = socket.getInputStream();
        BufferedReader reader = new BufferedReader(new InputStreamReader(in));
        String line;
        List<FTPFile> files = new ArrayList<>();
        while ((line = reader.readLine()) != null) {
            if (isSecureMode()) {
                line = decodeMessage(line);
            }

            FTPFile child = parseMLSD(line, parent);
            files.add(child);
        }
        parent.setChildren(files);

        reader.close();
        socket.close();
        readReply(); // after transfer
        return files;
    }

    public synchronized boolean changeDirectory(String dir) throws ConnectionException {
        executeCommand(FTPCommand.CWD + " " + dir);
        if (FTPReplyCode.find(getReplyCode()).isOkay()) {
            currentPath = dir;
            return true;
        }
        return false;
    }

    public synchronized void changeUpToParent() throws ConnectionException, FTPException {
        if (Constants.SEPARATOR.equals(currentPath))
            return;
        executeCommand(FTPCommand.CDUP);
        if (!FTPReplyCode.find(getReplyCode()).isOkay())
            throw new FTPException(response);

        currentPath = response.substring(response.indexOf('\"') + 1, response.lastIndexOf('\"'));
    }

    /**
     * download file from remote machine
     *
     * @param filename the name of file under the current working dir
     * @return input stream if download successfully, otherwise null
     */
    public synchronized InputStream getRetrStream(String filename)
            throws ConnectionException, FTPException, IOException, NoPermissionException {
        if (!user.isReadable())
            throw new NoPermissionException();

        Socket socket = establishDataConnection(FTPCommand.TYPE + " " + dataType.toString());
        readReply();

        // send download command
        if (restartOffset > 0L) {
            executeCommand(FTPCommand.REST + " " + restartOffset);
            if (!FTPReplyCode.find(getReplyCode()).isInfoRequested())
                throw new FTPException(response);
        }

        executeCommand(FTPCommand.RETR + " " + filename);
        if (!FTPReplyCode.find(getReplyCode()).isReady()) {
            throw new FTPException(response);
        }

        return socket.getInputStream();
    }

    /**
     * upload file to the remote machine
     *
     * @param filename the name of file under the current working dir
     * @return output stream if upload successfully, otherwise null
     */
    public synchronized OutputStream getAppeStream(String filename)
            throws ConnectionException, FTPException, IOException, NoPermissionException {
        if (!user.isWritable())
            throw new NoPermissionException();

        Socket socket = establishDataConnection(FTPCommand.TYPE + " " + dataType.toString());
        readReply();

        sendCommand(FTPCommand.APPE + " " + filename);
        readReply();
        if (!FTPReplyCode.find(getReplyCode()).isReady()) {
            throw new FTPException(response);
        }

        return socket.getOutputStream();
    }

    public synchronized boolean rename(String src, String dst)
            throws ConnectionException, NoPermissionException {
        if (!user.isWritable())
            throw new NoPermissionException();

        executeCommand(FTPCommand.RNFR + " " + src);
        if (!FTPReplyCode.find(getReplyCode()).isInfoRequested())
            return false;

        executeCommand(FTPCommand.RNTO + " " + dst);
        return FTPReplyCode.find(getReplyCode()).isOkay();
    }

    public synchronized boolean delete(String filename) throws ConnectionException, NoPermissionException {
        if (!user.canDeleted())
            throw new NoPermissionException();

        executeCommand(FTPCommand.DELE + " " + filename);
        return FTPReplyCode.find(getReplyCode()).isOkay();
    }

    public synchronized boolean makeDirectory(String dir) throws ConnectionException, NoPermissionException {
        if (!user.isWritable())
            throw new NoPermissionException();

        executeCommand(FTPCommand.MKD + " " + dir);
        return FTPReplyCode.find(getReplyCode()).isOkay();
    }

    public synchronized boolean removeDirectory(String dir) throws ConnectionException, NoPermissionException {
        if (!user.canDeleted())
            throw new NoPermissionException();

        executeCommand(FTPCommand.RMD + " " + dir);
        return FTPReplyCode.find(getReplyCode()).isOkay();
    }

    public synchronized long getFileSize(String filePath) throws ConnectionException {
        executeCommand(FTPCommand.SIZE + " " + filePath);
        if (FTPReplyCode.find(getReplyCode()).isOkay()) {
            String size = response.substring(response.indexOf(" ") + 1);
            return Long.parseLong(size);
        }
        return -1;
    }

    public synchronized void reconnect() throws IOException, ConnectionException, FTPException {
        controlConnection.close();
        msgListener.println("Trying to reconnect to server");
        connect(host, port);
        if (user != null)
            reLogin(user.getUsername(), user.getPassword());
    }

    public synchronized void noop() throws ConnectionException {
        executeCommand(FTPCommand.NOOP);
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

    public int decodeBytes(byte[] bytes, int len) {
        if (controlConnection instanceof SecureConnection) {
            SecureConnection connection = (SecureConnection) controlConnection;
            return securityUtils.decrypt(bytes, len, connection.getServerKey().getBytes());
        }
        return len;
    }

    public byte[] encodeBytes(byte[] bytes, int len) {
        if (controlConnection instanceof SecureConnection) {
            SecureConnection connection = (SecureConnection) controlConnection;
            return securityUtils.encrypt(bytes, len, connection.getClientKey().getBytes());
        }
        return bytes;
    }

    public String encodeMessage(String message) {
        if (controlConnection instanceof SecureConnection) {
            SecureConnection connection = (SecureConnection) controlConnection;
            return securityUtils.encrypt(message, connection.getClientKey());
        }
        return message;
    }

    public String decodeMessage(String message) {
        if (controlConnection instanceof SecureConnection) {
            SecureConnection connection = (SecureConnection) controlConnection;
            return securityUtils.decrypt(message, connection.getServerKey());
        }
        return message;
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

    private int getReplyCode() {
        return Integer.parseInt(response.substring(0, 3));
    }

    String getEncoding() {
        return encoding;
    }

    public void setEncoding(String encoding) {
        this.encoding = encoding;
    }

    MsgListener getListener() {
        return msgListener;
    }

    public User getUser() {
        return user;
    }

    SecurityUtils getSecurityUtils() {
        return securityUtils;
    }

    public String getCurrentPath() {
        return currentPath;
    }

    public boolean isPassiveMode() {
        return passiveMode;
    }

    public void setPassiveMode(boolean passiveMode) {
        this.passiveMode = passiveMode;
    }

    public boolean isSecureMode() {
        return secureMode;
    }

    public void setSecureMode(boolean secureMode) {
        this.secureMode = secureMode;
    }

    public SystemConfig getConfig() {
        return config;
    }
}
