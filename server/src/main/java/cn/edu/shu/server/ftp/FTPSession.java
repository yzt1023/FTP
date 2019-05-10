/*
 * Written By : Skye You
 * Copyright (c) 2018 - 2019. All Rights Reserved.
 */

package cn.edu.shu.server.ftp;

import cn.edu.shu.common.bean.DataType;
import cn.edu.shu.common.bean.User;
import cn.edu.shu.common.util.Constants;
import cn.edu.shu.server.db.UserDao;
import cn.edu.shu.server.util.ConfigUtils;

import javax.net.ssl.SSLContext;
import javax.swing.filechooser.FileSystemView;
import java.io.File;
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

    public FTPSession(ControlConnection controlConnection) {
        this.currentPath = "/";
        this.offset = 0L;
        this.rootPath = ConfigUtils.getInstance().getRootPath();
        this.userDao = new UserDao();
        this.fileSystemView = FileSystemView.getFileSystemView();
        this.dataConnection = new DataConnection();
        this.controlConnection = controlConnection;
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

    public void close() {
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

    void setSslContext(SSLContext sslContext) {
        dataConnection.setSslContext(sslContext);
    }
}
