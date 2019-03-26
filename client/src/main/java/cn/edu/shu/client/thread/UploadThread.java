/*
 * Written By : Skye You
 * Copyright (c) 2018 - 2019. All Rights Reserved.
 */

package cn.edu.shu.client.thread;

import cn.edu.shu.client.ftp.FTPClient;
import cn.edu.shu.client.listener.TransferListener;
import cn.edu.shu.client.ui.task.Task;
import cn.edu.shu.client.ftp.FTPFile;
import cn.edu.shu.client.util.Constants;

import javax.swing.filechooser.FileSystemView;
import java.io.*;
import java.util.Date;
import java.util.Queue;

public class UploadThread extends Thread {

    private Queue<Task> queue;
    private FTPClient ftpClient;
    private TransferListener listener;
    private FileSystemView fileSystemView = FileSystemView.getFileSystemView();

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
                        e.printStackTrace();
                    }finally {
                        if (result)
                            task.setState(Constants.STATE_SUCCESS);
                        else
                            task.setState(Constants.STATE_FAILURE);
                        listener.afterTransfer(task);
                    }
                }
            } catch (InterruptedException e) {
                e.printStackTrace();
            }
        }
    }

    private boolean uploadDir(Task task) throws IOException {
        File file = task.getFile();
        FTPFile ftpFile = task.getFtpFile();
        File[] files = fileSystemView.getFiles(file, false);
        task.setSize(files.length);
        if(!ftpClient.makeDirectory(ftpFile.getPath()))
            return false;
        ftpFile.setLastChanged(new Date());
        ftpFile.setType(Constants.TYPE_DIR);
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
        ftpFile.setType(Constants.TYPE_FILE);
        ftpFile.setSize(file.length());
        ftpFile.setName(file.getName());
        BufferedOutputStream out = new BufferedOutputStream(outputStream);
        FileInputStream in = new FileInputStream(file);
        byte[] bytes = new byte[Constants.KB];
        int len, read = 0;
        while ((len = in.read(bytes)) != -1) {
            out.write(bytes, 0, len);
            task.setProgress(++read);
            listener.notifyProgress(task);
        }
        in.close();
        ftpClient.readReply();
        ftpClient.destroyDataConnect();
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
        while((len = in.read(bytes)) != -1){
            out.write(bytes, 0, len);
        }
        in.close();
        ftpClient.readReply();
        ftpClient.destroyDataConnect();
        return true;
    }
}
