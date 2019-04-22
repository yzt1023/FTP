/*
 * Written By : Skye You
 * Copyright (c) 2018 - 2019. All Rights Reserved.
 */

package cn.edu.shu.client.thread;

import cn.edu.shu.client.ftp.FTPClient;
import cn.edu.shu.client.ftp.FTPFile;
import cn.edu.shu.client.listener.TransferListener;
import cn.edu.shu.client.ui.task.Task;
import cn.edu.shu.common.bean.DataType;
import cn.edu.shu.common.util.Constants;

import java.io.*;
import java.util.Date;
import java.util.Queue;

public class UploadThread extends TransferThread {

    public UploadThread(Queue<Task> queue, FTPClient ftpClient, TransferListener listener) {
        super(queue, ftpClient, listener);
    }

    @Override
    public boolean upload(File file, FTPFile ftpFile) throws IOException {
        //set ftp info
        ftpFile.setLastChanged(new Date());
        ftpFile.setType(fileSystemView.getSystemTypeDescription(file));
        ftpFile.setSize(file.length());
        ftpFile.setName(file.getName());
        if (file.isFile()) {
            OutputStream outputStream = ftpClient.getStorStream(ftpFile.getPath());
            if (outputStream == null)
                return false;
            BufferedOutputStream out = new BufferedOutputStream(outputStream);
            FileInputStream in = new FileInputStream(file);
            byte[] bytes = new byte[Constants.KB];
            int len, read = 0;
            boolean lastWasCR = false;
            while ((len = in.read(bytes)) != -1) {
                if (ftpClient.getDataType() == DataType.BINARY)
                    out.write(bytes, 0, len);
                else {
                    for (byte b : bytes) {
                        lastWasCR = utils.toNetWrite(lastWasCR, out, b);
                    }
                }
                read += len;
                notifyProgress(len);
            }
            out.flush();
            out.close();
            in.close();
            ftpClient.readReply();
            return read == file.length();
        } else {
            if (!ftpClient.makeDirectory(ftpFile.getPath()))
                return false;
            for (File f : fileSystemView.getFiles(file, false)) {
                FTPFile child = new FTPFile(ftpFile);
                String path = utils.getPath(ftpFile.getPath(), f.getName());
                child.setPath(path);
                boolean success = upload(f, child);
                if (!success)
                    return false;
            }
            return true;
        }
    }

}
