/*
 * Written By : Skye You
 * Copyright (c) 2018 - 2019. All Rights Reserved.
 */

package cn.edu.shu.client.thread;

import cn.edu.shu.client.ftp.FTPClient;
import cn.edu.shu.client.ftp.FTPFile;
import cn.edu.shu.client.listener.TransferListener;
import cn.edu.shu.client.ui.task.Task;
import cn.edu.shu.common.util.Constants;
import cn.edu.shu.common.util.Utils;
import org.apache.log4j.Logger;

import javax.swing.filechooser.FileSystemView;
import java.io.File;
import java.io.IOException;
import java.util.Queue;

public abstract class TransferThread extends Thread {
    private Queue<Task> queue;
    FTPClient ftpClient;
    private TransferListener listener;
    FileSystemView fileSystemView = FileSystemView.getFileSystemView();
    public Utils utils = Utils.getInstance();
    private Logger logger = Logger.getLogger(getClass());
    public Task task;

    public TransferThread(Queue<Task> queue, FTPClient ftpClient, TransferListener listener) {
        this.queue = queue;
        this.ftpClient = ftpClient;
        this.listener = listener;
    }

    @Override
    public void run() {
        while (true) {
            try {
                Thread.sleep(1000);
                task = queue.peek();
                if (task == null)
                    continue;

                task.setState(Constants.STATE_PROCESSING);
                listener.beforeTransfer(task);
                boolean result = false;

                try {
                    if(task.isDownload())
                        result = download(task.getFile(), task.getFtpFile());
                    else
                        result = upload(task.getFile(), task.getFtpFile());
                } catch (IOException e) {
                    logger.error(e.getMessage(), e);
                } finally {
                    updateState(result);
                    listener.afterTransfer(task);
                }
                queue.remove();
            } catch (Exception e) {
                logger.error(e.getMessage(), e);
            }
        }
    }

    public boolean download(File file, FTPFile ftpFile) throws IOException{
        return true;
    }

    public boolean upload(File file, FTPFile ftpFile) throws IOException{
        return true;
    }

    private void updateState(boolean result) {
        if (result) {
            if(task.getSize() == 0){
                task.setMaxValue(1);
                task.setProgress(1);
                listener.notifyProgress(task);
            }
            task.setState(Constants.STATE_SUCCESS);
        }
        else
            task.setState(Constants.STATE_FAILURE);
    }

    void notifyProgress(long progress){
        task.setProgress(progress + task.getProgress());
        listener.notifyProgress(task);
    }
}
