/*
 * Written By : Skye You
 * Copyright (c) 2018 - 2019. All Rights Reserved.
 */

package cn.edu.shu.server.ftp;

import cn.edu.shu.common.bean.DataType;
import cn.edu.shu.common.bean.User;
import cn.edu.shu.common.encryption.MD5;
import cn.edu.shu.common.util.Constants;
import cn.edu.shu.common.util.SecurityUtils;
import cn.edu.shu.server.db.UserDao;

import javax.swing.filechooser.FileSystemView;
import java.io.File;
import java.io.IOException;
import java.net.InetAddress;

public class FTPSession {
    private UserDao userDao;
    private User user;
    private String currentPath;
    private String rootPath;
    private File renamedFile;
    private long offset;
    private DataType dataType;
    private DataConnection dataConnection;
    private ControlConnection controlConnection;
    private FileSystemView fileSystemView;
    private boolean secureMode;
    private String serverKey;
    private String clientKey;
    private SecurityUtils securityUtils;

    public FTPSession(ControlConnection controlConnection) {
        this.controlConnection = controlConnection;
        this.currentPath = "/";
        this.offset = 0L;
        this.rootPath = controlConnection.getRootPath();
        this.userDao = new UserDao();
        this.fileSystemView = FileSystemView.getFileSystemView();
        this.securityUtils = SecurityUtils.getInstance();
    }

    public User getUser() {
        return user;
    }

    public void setUser(User user) {
        this.user = user;
    }

    public String getCurrentPath() {
        return currentPath;
    }

    public void setCurrentPath(String currentPath) {
        this.currentPath = currentPath;
    }

    public File getRenamedFile() {
        return renamedFile;
    }

    public void setRenamedFile(File renamedFile) {
        this.renamedFile = renamedFile;
    }

    public long getOffset() {
        return offset;
    }

    public void setOffset(long offset) {
        this.offset = offset;
    }

    public void println(String message) {
        controlConnection.println(message);
    }

    public String readRequest() throws IOException {
        return controlConnection.readRequest();
    }

    public String readLine() {
        return controlConnection.readLine();
    }

    public void sendLine(String line) {
        controlConnection.sendLine(line);
    }

    public void close() {
        clientKey = null;
        serverKey = null;
        controlConnection.shutdown();
    }

    public InetAddress getControlAddress() {
        return controlConnection.getAddress();
    }

    public DataType getDataType() {
        return dataType;
    }

    public void setDataType(DataType dataType) {
        this.dataType = dataType;
    }

    public UserDao getUserDao() {
        return userDao;
    }

    public DataConnection getDataConnection() {
        return dataConnection;
    }

    public DataConnection createDataConnection(){
        dataConnection = new DataConnection(this);
        return dataConnection;
    }

    public void closeDataConnection(){
        if(dataConnection != null)
            dataConnection.closeConnection();
        dataConnection = null;
    }

    public String getAbsolutePath(String arg) {
        String path = rootPath;
        if (arg == null)
            path += currentPath;
        else if (arg.startsWith(Constants.SEPARATOR))
            path += arg;
        else if (currentPath.endsWith(Constants.SEPARATOR))
            path += currentPath + arg;
        else
            path += currentPath + Constants.SEPARATOR + arg;
        return path;
    }

    public String getCurrentPath(String absolutePath) {
        if (absolutePath.length() <= rootPath.length())
            absolutePath = Constants.SEPARATOR;
        else
            absolutePath = absolutePath.substring(rootPath.length());
        return absolutePath;
    }

    String getEncoding() {
        return controlConnection.getEncoding();
    }

    public FileSystemView getFileSystemView() {
        return fileSystemView;
    }

    public void resetState() {
        this.renamedFile = null;
        this.offset = 0L;
    }

    public boolean isSecureMode() {
        return secureMode;
    }

    public void setSecureMode(boolean secureMode) {
        this.secureMode = secureMode;
    }

    public String getServerKey() {
        return serverKey;
    }

    public void setClientKey(String clientKey) {
        this.clientKey = clientKey;
    }

    public SecurityUtils getSecurityUtils() {
        return securityUtils;
    }

    public void generateKey() {
        serverKey = securityUtils.generateKey();
    }

    String decodeRequest(String request) {
        return securityUtils.decrypt(request, clientKey);
    }

    String encodeResponse(String response) {
        return securityUtils.encrypt(response, serverKey);
    }

    int decodeBytes(byte[] bytes, int len) {
        return securityUtils.decrypt(bytes, len, clientKey.getBytes());
    }

    byte[] encodeBytes(byte[] bytes, int len) {
        return securityUtils.encrypt(bytes, len, serverKey.getBytes());
    }

    public MD5 getMd5(){
        return securityUtils.getMd5();
    }
}
