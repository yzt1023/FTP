/*
 * Written By : Skye You
 * Copyright (c) 2018 - 2019. All Rights Reserved.
 */

package cn.edu.shu.client.listener;

import cn.edu.shu.client.ftp.FTPFile;
import cn.edu.shu.client.ui.task.Task;

import java.io.File;

public interface TransferListener {
    void fireDownload(FTPFile file, boolean tempDir);
    void fireUpload(File file);
    void beforeTransfer(Task task);
    void notifyProgress(Task task);
    void afterTransfer(Task task);
}
