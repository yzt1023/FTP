/*
 * Written By : Skye You
 * Copyright (c) 2018 - 2019. All Rights Reserved.
 */

package cn.edu.shu.server.ftp;

import cn.edu.shu.common.bean.DataType;
import cn.edu.shu.common.bean.User;
import cn.edu.shu.common.log.MsgListener;
import cn.edu.shu.common.util.Constants;
import cn.edu.shu.server.db.UserDao;
import cn.edu.shu.server.util.ConfigUtils;

import javax.swing.filechooser.FileSystemView;
import java.io.File;
import java.io.PrintWriter;
import java.net.InetAddress;

public class FTPSession {
    private MsgListener listener;
    private boolean closed;
    private UserDao userDao;
    private User user;
    private String currentPath;
    private String rootPath;
    private File renamedFile;
    private String encoding;
    private DataType dataType;
    private InetAddress controlAddress;
    private DataConnection dataConnection;
    private FileSystemView fileSystemView;

    public FTPSession(MsgListener listener){
        this.listener = listener;
        this.currentPath = "/";
        this.rootPath = ConfigUtils.getInstance().getRootPath();
        this.closed = false;
        this.userDao = new UserDao();
        this.fileSystemView = FileSystemView.getFileSystemView();
        this.dataConnection = new DataConnection();
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

    public void println(String message){
        listener.println(message);
    }

    public boolean isClosed() {
        return closed;
    }

    public void close(){
        this.closed = true;
    }

    public InetAddress getControlAddress() {
        return controlAddress;
    }

    public void setControlAddress(InetAddress controlAddress) {
        this.controlAddress = controlAddress;
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

    public String getEncoding() {
        return encoding;
    }

    public void setEncoding(String encoding) {
        this.encoding = encoding;
    }

    public FileSystemView getFileSystemView() {
        return fileSystemView;
    }
}
