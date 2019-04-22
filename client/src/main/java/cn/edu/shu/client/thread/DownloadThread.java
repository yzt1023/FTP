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
import java.util.Queue;

public class DownloadThread extends TransferThread {

    public DownloadThread(Queue<Task> queue, FTPClient ftpClient, TransferListener listener) {
        super(queue, ftpClient, listener);
    }

    @Override
    public boolean download(File file, FTPFile ftpFile) throws IOException {
        if (!ftpFile.isDirectory()) {
            InputStream inputStream = ftpClient.getRetrStream(ftpFile.getPath());
            if (inputStream == null)
                return false;

            BufferedInputStream in = new BufferedInputStream(inputStream);
            FileOutputStream out = new FileOutputStream(file);
            byte[] bytes = new byte[Constants.KB];
            int len;
            long read = 0;
            boolean lastWasCR = false;
            while ((len = in.read(bytes)) != -1) {
                if (ftpClient.getDataType() == DataType.BINARY || utils.noConversionRequired())
                    out.write(bytes, 0, len);
                else {
                    for (byte b : bytes)
                        lastWasCR = utils.fromNetWrite(lastWasCR, out, b);
                }
                read += len;
                notifyProgress(len);
            }
            ftpClient.readReply();
            out.close();
            in.close();
            return read == ftpFile.getSize();
        } else {
            if (!file.mkdir())
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
}
