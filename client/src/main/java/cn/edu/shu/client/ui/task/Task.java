/*
 * Written By : Skye You
 * Copyright (c) 2018 - 2019. All Rights Reserved.
 */

package cn.edu.shu.client.ui.task;

import cn.edu.shu.client.ftp.FTPFile;
import cn.edu.shu.client.util.Constants;

import java.io.File;
import java.util.Date;

public class Task{
    private File file;
    private FTPFile ftpFile;
    private Date modifyTime;
    private String state;
    private boolean download;
    private long size;
    private int progress;

    public Task(File file, FTPFile ftpFile, boolean download){
        this.file = file;
        this.ftpFile = ftpFile;
        this.download = download;
        this.state = Constants.STATE_WAITING;
        this.modifyTime = new Date();
    }

    public File getFile() {
        return file;
    }

    public void setFile(File file) {
        this.file = file;
    }

    public FTPFile getFtpFile() {
        return ftpFile;
    }

    public void setFtpFile(FTPFile ftpFile) {
        this.ftpFile = ftpFile;
    }

    Date getModifyTime() {
        return modifyTime;
    }

    public void setModifyTime(Date modifyTime) {
        this.modifyTime = modifyTime;
    }

    String getState() {
        return state;
    }

    public void setState(String state) {
        this.state = state;
    }

    public boolean isDownload() {
        return download;
    }

    public void setDownload(boolean download) {
        this.download = download;
    }

    long getSize() {
        return size;
    }

    public void setSize(long size) {
        this.size = size;
    }

    public void setProgress(int value){
        this.progress = (int) (value * 100 / size);
    }

    int getProgress() {
        return progress;
    }
}
