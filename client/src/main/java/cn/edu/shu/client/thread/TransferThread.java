/*
 * Written By : Skye You
 * Copyright (c) 2018 - 2019. All Rights Reserved.
 */

package cn.edu.shu.client.thread;

import cn.edu.shu.client.config.SystemConfig;
import cn.edu.shu.client.exception.ConnectionException;
import cn.edu.shu.client.exception.FTPException;
import cn.edu.shu.client.exception.NoPermissionException;
import cn.edu.shu.client.ftp.FTPClient;
import cn.edu.shu.client.ftp.FTPFile;
import cn.edu.shu.client.listener.TransferListener;
import cn.edu.shu.client.ui.task.Task;
import cn.edu.shu.common.bean.DataType;
import cn.edu.shu.common.encryption.MD5;
import cn.edu.shu.common.util.CommonUtils;
import cn.edu.shu.common.util.Constants;
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
    private SystemConfig config;
    private boolean stop;
    private MD5 md5;
    private String serverMd5;

    public TransferThread(Queue<Task> queue, FTPClient ftpClient, TransferListener listener) {
        this.queue = queue;
        this.ftpClient = ftpClient;
        this.listener = listener;
        this.config = ftpClient.getConfig();
        this.md5 = new MD5();
    }

    @Override
    public void run() {
        while (!stop) {
            try {
                Thread.sleep(1000);
                task = queue.peek();
                if (task == null)
                    continue;

                if (Constants.STATE_PAUSE.equals(task.getState())) {
                    queue.remove();
                    queue.offer(task);
                    continue;
                }

                task.setState(Constants.STATE_PROCESSING);
                listener.beforeTransfer(task);
                boolean result = false;

                try {
                    if (task.isDownload())
                        result = download(task.getFile(), task.getFtpFile());
                    else
                        result = upload(task.getFile(), task.getFtpFile());
                } catch (Exception e) {
                    logger.error(e.getMessage(), e);
                    ftpClient.readReply();
                    task.setState(Constants.STATE_PAUSE);
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
            if (file.exists() && file.length() == ftpFile.getSize())
                return true;

            long offset = file.length();
            ftpClient.setRestartOffset(offset);
            ftpClient.setDataType(config.getFileDataType(ftpFile.getName()));
            InputStream inputStream = ftpClient.getRetrStream(ftpFile.getPath());
            if (inputStream == null)
                return false;

            RandomAccessFile raf = new RandomAccessFile(file, "rw");
            raf.seek(offset);
            OutputStream out = new FileOutputStream(raf.getFD());

            try {
                transfer(out, inputStream, true, file, offset);
            }catch (Exception e){
                inputStream.close();
                out.close();
                throw e;
            }

            ftpClient.readReply();
            if(ftpClient.isSecureMode() && DataType.BINARY == ftpClient.getDataType())
                return serverMd5.equals(md5.getFileMd5(file));

            return true;
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
        ftpFile.setSize(file.length());
        if (file.isFile()) {
            long offset = ftpClient.getFileSize(ftpFile.getPath());
            if (offset == file.length())
                return true;

            ftpClient.setDataType(config.getFileDataType(file.getName()));
            OutputStream out = ftpClient.getAppeStream(ftpFile.getPath());
            if (out == null)
                return false;

            RandomAccessFile raf = new RandomAccessFile(file, "r");
            if (offset == -1)
                offset = 0;
            raf.seek(offset);

            InputStream in = new FileInputStream(raf.getFD());
            try {
                transfer(out, in, false, file, offset);
            }catch (Exception e) {
                in.close();
                out.close();
                throw e;
            }

            String reply = ftpClient.readReply();
            return reply.charAt(0) == '2';

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

    private void transfer(OutputStream outputStream, InputStream inputStream, boolean download, File file, long read)
            throws IOException, FTPException {
        if (DataType.ASCII == ftpClient.getDataType()) {
            transferASCII(outputStream, inputStream, download, read);
            return;
        }

        if (ftpClient.isSecureMode()) {
            transferEncrypted(outputStream, inputStream, download, file, read);
            return;
        }

        BufferedInputStream in = new BufferedInputStream(inputStream);
        BufferedOutputStream out = new BufferedOutputStream(outputStream);
        byte[] bytes = new byte[Constants.KB];
        int len;
        while ((len = in.read(bytes)) != -1) {
            if (stop || Constants.STATE_PAUSE.equals(task.getState()))
                throw new FTPException();

            out.write(bytes, 0, len);
            read += len;
            notifyProgress(len);
        }
        out.flush();
        outputStream.close();
        inputStream.close();
    }

    private void transferASCII(OutputStream outputStream, InputStream inputStream, boolean download, long read) throws FTPException, IOException {
        BufferedReader reader = new BufferedReader(new InputStreamReader(inputStream));
        PrintWriter writer = new PrintWriter(outputStream, true);
        String line;
        int readLen;
        int eol = Constants.EOL.length;
        while ((line = reader.readLine()) != null) {
            if (stop || Constants.STATE_PAUSE.equals(task.getState()))
                throw new FTPException();

            if (download && ftpClient.isSecureMode()) {
                line = ftpClient.decodeMessage(line);
                readLen = line.getBytes().length + eol;
            } else if (ftpClient.isSecureMode()) {
                readLen = line.getBytes().length + eol;
                line = ftpClient.encodeMessage(line);
            } else {
                readLen = line.getBytes().length + eol;
            }

            writer.println(line);

            read += readLen;
            notifyProgress(readLen);
        }
        reader.close();
        writer.close();
    }

    private void transferEncrypted(OutputStream outputStream, InputStream inputStream, boolean download, File file, long read)
            throws IOException, FTPException {

        DataInputStream in = new DataInputStream(inputStream);
        DataOutputStream out = new DataOutputStream(outputStream);
        if (!download) {
            out.writeUTF(ftpClient.encodeMessage(md5.getFileMd5(file)));
        } else {
            serverMd5 = ftpClient.decodeMessage(in.readUTF());
        }

        int buffer = Constants.KB;
        if (download)
            buffer = buffer + 16;

        byte[] bytes = new byte[Constants.KB + 16];
        int readLen, writeLen, len;
        while ((readLen = in.read(bytes, 0, buffer)) != -1) {
            if (stop || Constants.STATE_PAUSE.equals(task.getState()))
                throw new FTPException();

            len = readLen;
            if (download) {
                writeLen = ftpClient.decodeBytes(bytes, readLen);
                len = writeLen;
            } else {
                bytes = ftpClient.encodeBytes(bytes, readLen);
                int fill = 16 - (readLen % 16);
                writeLen = readLen + fill;
            }

            out.write(bytes, 0, writeLen);

            read += len;
            notifyProgress(len);
        }
        out.flush();
        out.close();
        in.close();
    }

    private void updateState(boolean result) {
        if (Constants.STATE_PAUSE.equals(task.getState())) {
            queue.offer(task);
            return;
        }

        if (result) {
            if (task.getSize() == 0) {
                task.setMaxValue(1);
                task.setProgress(1);
                listener.notifyProgress(task);
            }
            task.setProgress(task.getSize());
            task.setState(Constants.STATE_SUCCESS);
        } else {
            task.setState(Constants.STATE_FAILURE);
            if (task.isDownload())
                task.getFile().delete();
        }
        listener.afterTransfer(task);
    }

    private void notifyProgress(long progress) {
        task.setProgress(progress + task.getProgress());
        listener.notifyProgress(task);
    }

    public void close() {
        stop = true;
    }
}
