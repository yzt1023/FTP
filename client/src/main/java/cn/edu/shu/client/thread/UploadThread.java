/*
 * Written By : Skye You
 * Copyright (c) 2018 - 2019. All Rights Reserved.
 */

package cn.edu.shu.client.thread;

import cn.edu.shu.client.ftp.FTPClient;
import cn.edu.shu.client.listener.TransferListener;
import cn.edu.shu.client.ui.task.Task;
import cn.edu.shu.client.ftp.FTPFile;
import cn.edu.shu.common.bean.DataType;
import cn.edu.shu.common.util.Constants;
import cn.edu.shu.common.util.Utils;
import org.apache.log4j.Logger;

import javax.swing.filechooser.FileSystemView;
import java.io.*;
import java.util.Date;
import java.util.Queue;

public class UploadThread extends Thread {

    private Queue<Task> queue;
    private FTPClient ftpClient;
    private TransferListener listener;
    private FileSystemView fileSystemView = FileSystemView.getFileSystemView();
    private Utils utils = Utils.getInstance();
    private Logger logger = Logger.getLogger(getClass());

    public UploadThread(Queue<Task> queue, FTPClient ftpClient, TransferListener listener) {
        this.queue = queue;
        this.ftpClient = ftpClient;
        this.listener = listener;
    }

    @Override
    public void run() {
        while (true){
            try {
                Thread.sleep(5000);
                if (!queue.isEmpty()) {
                    Task task = queue.poll();
                    task.setState(Constants.STATE_PROCESSING);
                    File file = task.getFile();
                    listener.beforeTransfer(task);
                    boolean result = false;
                    try {
                        if (file.isDirectory())
                            result = uploadDir(task);
                        else
                            result = uploadFile(task);
                    } catch (IOException e) {
                        logger.error(e.getMessage(), e);
                    }finally {
                        if (result)
                            task.setState(Constants.STATE_SUCCESS);
                        else
                            task.setState(Constants.STATE_FAILURE);
                        listener.afterTransfer(task);
                    }
                }
            } catch (InterruptedException e) {
                logger.error(e.getMessage(), e);
            }
        }
    }

    private boolean uploadDir(Task task) throws IOException {
        File file = task.getFile();
        FTPFile ftpFile = task.getFtpFile();
        File[] files = fileSystemView.getFiles(file, false);
        task.setSize(files.length);// TODO: 4/6/2019 after setting size of folder, the task table should be refreshed
        if(!ftpClient.makeDirectory(ftpFile.getPath()))
            return false;
        ftpFile.setLastChanged(new Date());
        ftpFile.setType(fileSystemView.getSystemTypeDescription(file));
        ftpFile.setName(file.getName());
        for(int i = 0; i < files.length; i++){
            boolean success;
            if(files[i].isDirectory()){
                success = uploadDir(files[i], ftpFile.getPath());
            }else{
                success = uploadFile(files[i], ftpFile.getPath());
            }
            if(!success)
                return false;
            task.setProgress(i + 1);
            listener.notifyProgress(task);
        }
        return true;
    }

    private boolean uploadFile(Task task) throws IOException {
        File file = task.getFile();
        FTPFile ftpFile = task.getFtpFile();
        task.setSize(file.length() / Constants.KB + 1);
        OutputStream outputStream = ftpClient.getStorStream(ftpFile.getPath());
        if (outputStream == null)
            return false;
        ftpFile.setLastChanged(new Date());
        ftpFile.setType(fileSystemView.getSystemTypeDescription(file));
        ftpFile.setSize(file.length());
        ftpFile.setName(file.getName());
        BufferedOutputStream out = new BufferedOutputStream(outputStream);
        FileInputStream in = new FileInputStream(file);
        byte[] bytes = new byte[Constants.KB];
        int len, read = 0;
        boolean lastWasCR = false;
        while ((len = in.read(bytes)) != -1) {
            if(ftpClient.getDataType() == DataType.BINARY)
                out.write(bytes, 0, len);
            else {
                for(byte b : bytes){
                    lastWasCR = utils.toNetWrite(lastWasCR, out, b);
                }
            }
            task.setProgress(++read);
            listener.notifyProgress(task);
        }
        out.flush();
        out.close();
        in.close();
        ftpClient.readReply();
        return true;
    }

    private boolean uploadDir(File file, String remoteDir) throws IOException{
        String path = remoteDir + Constants.SEPARATOR + file.getName();
        if(!ftpClient.makeDirectory(path))
            return false;
        File[] files = fileSystemView.getFiles(file, false);
        for(File child : files){
            boolean success;
            if(child.isDirectory())
                success = uploadDir(file, path);
            else
                success = uploadFile(file, path);
            if(!success)
                return false;
        }
        return true;
    }

    private boolean uploadFile(File file, String remoteDir) throws IOException{
        String path = remoteDir + Constants.SEPARATOR + file.getName();
        OutputStream outputStream = ftpClient.getStorStream(path);
        if(outputStream == null)
            return false;
        BufferedOutputStream out = new BufferedOutputStream(outputStream);
        FileInputStream in = new FileInputStream(file);
        byte[] bytes = new byte[Constants.KB];
        int len;
        boolean lastWasCR = false;
        while((len = in.read(bytes)) != -1){
            if(ftpClient.getDataType() == DataType.BINARY)
                out.write(bytes, 0, len);
            else {
                for(byte b : bytes){
                    lastWasCR = utils.toNetWrite(lastWasCR, out, b);
                }
            }
        }
        out.flush();
        out.close();
        in.close();
        ftpClient.readReply();
        return true;
    }
}
