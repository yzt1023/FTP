/*
 * Written By : Skye You
 * Copyright (c) 2018 - 2019. All Rights Reserved.
 */

package cn.edu.shu.client.ui.task;

import cn.edu.shu.client.ftp.FTPFile;
import cn.edu.shu.common.util.Constants;

import java.io.File;
import java.util.Date;

public class Task {
    private File file;
    private FTPFile ftpFile;
    private Date modifyTime;
    private String state;
    private boolean download;
    private boolean kb;
    private String displaySize;
    private ProgressArg progressArg;

    public Task(File file, FTPFile ftpFile, boolean download) {
        this.file = file;
        this.ftpFile = ftpFile;
        this.download = download;
        this.state = Constants.STATE_WAITING;
        this.modifyTime = new Date();
        this.progressArg = new ProgressArg(0);
        this.kb = false;
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

    public Date getModifyTime() {
        return modifyTime;
    }

    public void setModifyTime(Date modifyTime) {
        this.modifyTime = modifyTime;
    }

    public String getState() {
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

    public String getDisplaySize() {
        return displaySize;
    }

    public void setDisplaySize(String displaySize) {
        this.displaySize = displaySize;
    }

    public ProgressArg getProgressArg() {
        return progressArg;
    }

    public void setMaxValue(long value) {
        if (value > Integer.MAX_VALUE) {
            value /= 1024;
            kb = true;
        }
        progressArg.setMaxValue((int) value);
    }

    public long getSize() {
        return progressArg.getMaxValue();
    }

    public long getProgress() {
        return progressArg.getValue();
    }

    public void setProgress(long value) {
        long progress;
        if (kb)
            progress = value / 1024;
        else
            progress = value;
        progressArg.setValue((int) progress);
    }
}
