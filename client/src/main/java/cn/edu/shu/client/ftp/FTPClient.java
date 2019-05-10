/*
 * Written By : Skye You
 * Copyright (c) 2018 - 2019. All Rights Reserved.
 */

package cn.edu.shu.client.ftp;

import cn.edu.shu.client.exception.ConnectionException;
import cn.edu.shu.client.exception.FTPException;
import cn.edu.shu.client.exception.NoPermissionException;
import cn.edu.shu.common.bean.DataType;
import cn.edu.shu.common.bean.User;
import cn.edu.shu.common.ftp.FTPCommand;
import cn.edu.shu.common.ftp.FTPReplyCode;
import cn.edu.shu.common.ftp.SSLContextFactory;

import cn.edu.shu.common.log.MsgListener;
import cn.edu.shu.common.util.Constants;
import cn.edu.shu.common.util.Utils;

import javax.net.ssl.SSLContext;
import javax.net.ssl.SSLServerSocketFactory;
import javax.net.ssl.SSLSocketFactory;
import java.io.*;
import java.net.InetAddress;
import java.net.InetSocketAddress;
import java.net.ServerSocket;
import java.net.Socket;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;

public class FTPClient {
    private Utils utils = Utils.getInstance();
    private String currentPath;
    private boolean passiveMode = true;
    private boolean secureMode = true;
    private long restartOffset;
    private DataType dataType;
    private String encoding;
    private String host;
    private int port;
    private SSLContext sslContext;
    private PlainControlConnection controlConnection;
    private User user;
    /*    // control connection
        private Socket controlSocket;
        private Socket lastSocket;
        private SSLSocketFactory sslSocketFactory;
        private PrintWriter controlWriter;
        private BufferedReader controlReader;*/
    private String response;

    private MsgListener msgListener;

    public FTPClient(MsgListener msgListener) {
        this.msgListener = msgListener;
        this.dataType = DataType.BINARY;
        this.restartOffset = 0L;
        this.encoding = "UTF-8";
        this.sslContext = SSLContextFactory.createSSLContext(getClass());
    }

    private static String getValue(String line, String key) {
        int index = line.indexOf(key);
        return line.substring(index + key.length(), line.indexOf(';', index));
    }


    public synchronized void connect(String host, int port) throws ConnectionException {
        if (secureMode)
            controlConnection = new SSLControlConnection(this);
        else
            controlConnection = new PlainControlConnection(this);
        controlConnection.connect(host, port);
        this.host = host;
        this.port = port;
            /*controlSocket = new Socket(host, port);
            controlSocket.setKeepAlive(true);
            controlSocket.setTcpNoDelay(true);
            controlReader = new BufferedReader(new InputStreamReader(controlSocket.getInputStream(), encoding));
            controlWriter = new PrintWriter(new OutputStreamWriter(controlSocket.getOutputStream(), encoding), true);
        this.host = host;
        this.port = port;*/

    }

    public synchronized void login(String username, String password) throws ConnectionException, FTPException {
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
        executeCommand(FTPCommand.REG + " " + username + " " + password);
        if (!FTPReplyCode.find(getReplyCode()).isOkay())
            throw new FTPException(response);
    }

    private synchronized void executeCommand(String command) throws ConnectionException {
        try {
            response = controlConnection.executeCommand(command);
        } catch (ConnectionException e) {
            try {
                controlConnection.close();
            } catch (IOException ex) {
                throw new ConnectionException(ex.getMessage(), ex);
            }
            throw e;
        }
    }

    public synchronized void readReply() throws ConnectionException {
        response = controlConnection.readReply();
    }

    private synchronized void sendCommand(String command) throws ConnectionException {
        controlConnection.sendCommand(command);
    }
/*
    private void sendCommand(String command) {
        if (controlConnection == null) {
            return;
        }

        controlWriter.println(command);
        if (command.startsWith(FTPCommand.PASS))
            msgListener.println(FTPCommand.PASS + " ******");
        else
            msgListener.println(command);
        logger.info(command);
    }*/
/*

    public void readReply() {
        try {
            response = controlReader.readLine();
            if (response == null)
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
*/

    public void disconnect() throws ConnectionException, IOException {
        executeCommand(FTPCommand.QUIT);
        controlConnection.close();
        /*try {
            if (!isServerClose()) {
                sendCommand(FTPCommand.QUIT);
                readReply();
            }
            if (controlSocket != null)
                controlSocket.close();
            controlSocket = null;
        } catch (IOException e) {
            logger.error(e.getMessage(), e);
        }*/
    }

    public void setPassiveMode(boolean passiveMode) {
        this.passiveMode = passiveMode;
    }

    public void setSecureMode(boolean secureMode) {
        this.secureMode = secureMode;
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
        Socket socket;
        if (secureMode) {
            SSLSocketFactory factory = sslContext.getSocketFactory();
            socket = factory.createSocket(socketAddress.getAddress(), socketAddress.getPort());
        } else {
            socket = new Socket();
            socket.connect(socketAddress);
        }
        sendCommand(command);

        return socket;
    }

    private synchronized Socket establishPortDataConnection(String command)
            throws ConnectionException, IOException, FTPException {
        // produce a random port number
        InetAddress localAddress = controlConnection.getLocalAddress();
        ServerSocket serverSocket;

        if (secureMode) {
            SSLServerSocketFactory factory = sslContext.getServerSocketFactory();
            serverSocket = factory.createServerSocket(0, 0, localAddress);
        } else {
            serverSocket = new ServerSocket(0, 1, localAddress);
        }

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

    public String getCurrentPath() {
        return currentPath;
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
    public synchronized OutputStream getStorStream(String filename)
            throws ConnectionException, FTPException, IOException, NoPermissionException {
        if (!user.isWritable())
            throw new NoPermissionException();

        Socket socket = establishDataConnection(FTPCommand.TYPE + " " + dataType.toString());
        readReply();

        if (restartOffset > 0L) {
            executeCommand(FTPCommand.REST + " " + restartOffset);
            if (!FTPReplyCode.find(getReplyCode()).isInfoRequested())
                throw new FTPException(response);
        }

        executeCommand(FTPCommand.STOR + " " + filename);
        if (!FTPReplyCode.find(getReplyCode()).isReady()) {
            throw new FTPException(response);
        }

        return socket.getOutputStream();
    }

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

    /*
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
        }*/
    public synchronized void reconnect() throws IOException, ConnectionException, FTPException {
        controlConnection.close();
        msgListener.println("Trying to reconnect to server");
        connect(host, port);
        if (user != null)
            login(user.getUsername(), user.getPassword());
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

    public void setRestartOffset(long restartOffset) {
        this.restartOffset = restartOffset;
    }

    public DataType getDataType() {
        return dataType;
    }

    public void setDataType(DataType type) {
        this.dataType = type;
    }
/*

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
*/

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

    public SSLContext getSslContext() {
        return sslContext;
    }

    public User getUser() {
        return user;
    }
}
