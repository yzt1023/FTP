/*
 * Written By : Skye You
 * Copyright (c) 2018 - 2019. All Rights Reserved.
 */

package cn.edu.shu.client.thread;

import cn.edu.shu.client.exception.ConnectionException;
import cn.edu.shu.client.exception.FTPException;
import cn.edu.shu.client.exception.NoPermissionException;
import cn.edu.shu.client.ftp.FTPClient;
import cn.edu.shu.client.ftp.FTPFile;
import cn.edu.shu.client.listener.TransferListener;
import cn.edu.shu.client.ui.task.Task;
import cn.edu.shu.common.bean.DataType;
import cn.edu.shu.common.util.Constants;
import cn.edu.shu.common.util.CommonUtils;
import org.apache.log4j.Logger;

import javax.swing.filechooser.FileSystemView;
import java.io.*;
import java.util.Date;
import java.util.Queue;

public class TransferThread extends Thread {
    private Queue<Task> queue;
    private FTPClient ftpClient;
    private TransferListener listener;
    private FileSystemView fileSystemView = FileSystemView.getFileSystemView();
    private CommonUtils utils = CommonUtils.getInstance();
    private Logger logger = Logger.getLogger(getClass());
    private Task task;

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

                if(Constants.STATE_PAUSE.equals(task.getState())) {
                    queue.remove();
                    queue.offer(task);
                    continue;
                }

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
                    queue.remove();
                }
            } catch (Exception e) {
                logger.error(e.getMessage(), e);
            }
        }
    }

    private boolean download(File file, FTPFile ftpFile) throws IOException, ConnectionException, FTPException, NoPermissionException {
        if (!ftpFile.isDirectory()) {
            if(file.exists() && file.length() == ftpFile.getSize())
                return true;

            long offset = file.length();
            ftpClient.setRestartOffset(offset);
            InputStream inputStream = ftpClient.getRetrStream(ftpFile.getPath());
            if (inputStream == null)
                return false;

            BufferedInputStream in = new BufferedInputStream(inputStream);
            RandomAccessFile raf = new RandomAccessFile(file, "rw");
            raf.seek(offset);
            FileOutputStream out = new FileOutputStream(raf.getFD());
            int buffer = Constants.KB + 16, len;
            byte[] bytes = new byte[buffer];
            long read = offset;
            boolean lastWasCR = false;
            while ((len = in.read(bytes, 0, buffer)) != -1) {
                if(Constants.STATE_PAUSE.equals(task.getState()))
                    break;

                if(ftpClient.isSecureMode()) {
                    len = ftpClient.decodeBytes(bytes, len);
                }

                if (ftpClient.getDataType() == DataType.BINARY || utils.noConversionRequired())
                    out.write(bytes, 0, len);
                else {
                    for (byte b : bytes)
                        lastWasCR = utils.fromNetWrite(lastWasCR, out, b);
                }
                read += len;
                notifyProgress(len);
            }
            out.close();
            raf.close();
            in.close();
            ftpClient.readReply();
            return read == ftpFile.getSize();
        } else {
            if (!file.exists() && !file.mkdir())
                return false;
            for (FTPFile f : ftpFile.getChildren()) {
                File child = new File(file, f.getName());
                boolean success = download(child, f);
                if (!success)
                    return false;
            }
            return true;
        }
    }

    private boolean upload(File file, FTPFile ftpFile) throws IOException, ConnectionException, FTPException, NoPermissionException {
        //set ftp info
        ftpFile.setLastChanged(new Date());
        ftpFile.setType(fileSystemView.getSystemTypeDescription(file));
        if (file.isFile()) {
            long offset = ftpClient.getFileSize(ftpFile.getPath());
            if(offset == file.length())
                return true;

            OutputStream outputStream = ftpClient.getAppeStream(ftpFile.getPath());
            if (outputStream == null)
                return false;

            BufferedOutputStream out = new BufferedOutputStream(outputStream);
            RandomAccessFile raf = new RandomAccessFile(file, "r");
            if(offset == -1)
                offset = 0;
            raf.seek(offset);
            FileInputStream in = new FileInputStream(raf.getFD());
            byte[] bytes = new byte[Constants.KB + 16];
            int readLen, writeLen;
            long read = offset;
            boolean lastWasCR = false;
            while ((readLen = in.read(bytes, 0, Constants.KB)) != -1) {
                if(Constants.STATE_PAUSE.equals(task.getState()))
                    break;

                if(ftpClient.isSecureMode()) {
                    bytes = ftpClient.encodeBytes(bytes, readLen);
                    int fill = 16 - (readLen % 16);
                    writeLen = readLen + fill;
                }else{
                    writeLen = readLen;
                }

                if (ftpClient.getDataType() == DataType.BINARY)
                    out.write(bytes, 0, writeLen);
                else {
                    for (int i = 0; i < writeLen; i++) {
                        lastWasCR = utils.toNetWrite(lastWasCR, out, bytes[i]);
                    }
                }
                read += readLen;
                notifyProgress(readLen);
            }
            ftpFile.setSize(read);
            out.flush();
            out.close();
            in.close();
            raf.close();
            ftpClient.readReply();
            return read == file.length();
        } else {
            if (ftpClient.getFileSize(ftpFile.getPath()) == -1 && !ftpClient.makeDirectory(ftpFile.getPath()))
                return false;
            for (File f : fileSystemView.getFiles(file, false)) {
                FTPFile child = new FTPFile(ftpFile, f.getName());
                String path = utils.getPath(ftpFile.getPath(), f.getName());
                child.setPath(path);
                boolean success = upload(f, child);
                if (!success)
                    return false;
            }
            return true;
        }
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
        else{
            if(Constants.STATE_PAUSE.equals(task.getState())) {
                queue.offer(task);
                return;
            }
            task.setState(Constants.STATE_FAILURE);
        }
        listener.afterTransfer(task);
    }

    private void notifyProgress(long progress){
        task.setProgress(progress + task.getProgress());
        listener.notifyProgress(task);
    }
}
