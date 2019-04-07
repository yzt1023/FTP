/*
 * Written By : Skye You
 * Copyright (c) 2018 - 2019. All Rights Reserved.
 */

package cn.edu.shu.client.thread;

import cn.edu.shu.client.listener.TransferListener;
import cn.edu.shu.client.ui.task.Task;
import cn.edu.shu.client.ftp.FTPClient;
import cn.edu.shu.client.ftp.FTPFile;
import cn.edu.shu.common.bean.DataType;
import cn.edu.shu.common.util.Constants;
import cn.edu.shu.common.util.Utils;
import org.apache.log4j.Logger;

import java.io.*;
import java.util.Queue;

public class DownloadThread extends Thread {

    private Queue<Task> queue;
    private FTPClient ftpClient;
    private TransferListener listener;
    private Utils utils = Utils.getInstance();
    private Logger logger = Logger.getLogger(getClass());

    public DownloadThread(Queue<Task> queue, FTPClient ftpClient, TransferListener listener){
        this.queue = queue;
        this.ftpClient = ftpClient;
        this.listener = listener;
    }

    @Override
    public void run() {
        while(true) {
            try {
                Thread.sleep(5000);
                if (!queue.isEmpty()) {
                    Task task = queue.poll();
                    task.setState(Constants.STATE_PROCESSING);
                    listener.beforeTransfer(task);
                    FTPFile ftpFile = task.getFtpFile();
                    boolean result = false;
                    try {
                        if (ftpFile.isDirectory()) {
                            result = downloadDir(task);
                        } else {
                            result = downloadFile(task);
                        }
                    } catch (IOException e) {
                        e.printStackTrace();
                    } finally {
                        if (result)
                            task.setState(Constants.STATE_SUCCESS);
                        else
                            task.setState(Constants.STATE_FAILURE);
                        listener.afterTransfer(task);
                    }
                }
            }catch (Exception e){
                logger.error(e.getMessage(), e);
            }
        }
    }

    private boolean downloadDir(Task task) throws IOException {
        File file = task.getFile();
        FTPFile ftpFile = task.getFtpFile();
        if(!file.mkdir())
            return false;
        FTPFile[] ftpFiles=  ftpClient.getFiles(ftpFile);
        task.setSize(ftpFiles.length);
        for(int i = 0; i < ftpFiles.length; i++){
            boolean success;
            if(ftpFiles[i].isDirectory()){
                success = downloadDir(file.getPath(), ftpFiles[i]);
            }else{
                success = downloadFile(file.getPath(), ftpFiles[i]);
            }
            if(!success){
                return false;
            }
            task.setProgress(i + 1);
            listener.notifyProgress(task);
        }
        return true;
    }

    private boolean downloadFile(Task task) throws IOException {
        FTPFile ftpFile = task.getFtpFile();
        long size = ftpFile.getSize() / Constants.KB + 1;
        task.setSize(size);
        InputStream inputStream = ftpClient.getRetrStream(ftpFile.getPath());
        if(inputStream == null)
            return false;
        BufferedInputStream in = new BufferedInputStream(inputStream);
        FileOutputStream out = new FileOutputStream(task.getFile());
        byte[] bytes = new byte[Constants.KB];
        int len, read = 0;
        boolean lastWasCR = false;
        while((len = in.read(bytes)) != -1){
            if(ftpClient.getDataType() == DataType.BINARY || utils.noConversionRequired())
                out.write(bytes, 0, len);
            else {
                for(byte b : bytes)
                    lastWasCR = utils.fromNetWrite(lastWasCR, out, b);
            }
            task.setProgress(++read);
            listener.notifyProgress(task);
        }
        ftpClient.readReply();
        out.close();
        in.close();
        return read == size;
    }

    private boolean downloadDir(String localDir, FTPFile ftpFile)throws IOException{
        String filename = ftpFile.getName();
        File file = new File(localDir, filename);
        if(!file.mkdirs())
            return false;
        ftpClient.getFiles(ftpFile);
        for(FTPFile child : ftpFile.getChildren()){
            boolean success;
            if(child.isDirectory()){
                success = downloadDir(file.getPath(), child);
            }else{
                success = downloadFile(file.getPath(), child);
            }
            if(!success){
                return false;
            }
        }
        return true;
    }

    private boolean downloadFile(String localDir, FTPFile ftpFile)throws IOException{
        File file = new File(localDir, ftpFile.getName());
        InputStream inputStream = ftpClient.getRetrStream(ftpFile.getPath());
        if(inputStream == null)
            return false;
        BufferedInputStream in = new BufferedInputStream(inputStream);
        FileOutputStream out = new FileOutputStream(file);
        byte[] bytes = new byte[Constants.KB];
        int len;
        boolean lastWasCR = false;
        while((len = in.read(bytes)) != -1){
            if(ftpClient.getDataType() == DataType.BINARY || Utils.getInstance().noConversionRequired())
                out.write(bytes, 0, len);
            else {
                for(byte b : bytes)
                    lastWasCR = utils.fromNetWrite(lastWasCR, out, b);
            }
        }
        ftpClient.readReply();
        out.close();
        in.close();
        return true;
    }

}
