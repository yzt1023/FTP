/*
 * Written By : Skye You
 * Copyright (c) 2018 - 2019. All Rights Reserved.
 */

package cn.edu.shu.client.util;

import cn.edu.shu.client.ftp.FTPClient;
import cn.edu.shu.client.ftp.FTPFile;

import javax.swing.filechooser.FileSystemView;
import java.io.File;
import java.text.DecimalFormat;

public class TransferUtils {

    private FileSystemView fileSystemView = FileSystemView.getFileSystemView();
    private static TransferUtils instance = new TransferUtils();

    public static TransferUtils getInstance() {
        return instance;
    }

    public long getTotalSize(FTPFile ftpFile, FTPClient ftpClient){
        if(!ftpFile.isDirectory())
            return ftpFile.getSize();
        int size = 0;
        for(FTPFile f : ftpClient.getFiles(ftpFile))
            size += getTotalSize(f, ftpClient);
        return size;
    }

    public long getTotalSize(File file){
        if(file.isFile())
            return file.length();
        long size = 0;
        for(File f : fileSystemView.getFiles(file, false))
            size += getTotalSize(f);
        return size;
    }

    public String getFormatSize(long size){
        long kb, mb, gb;
        String length;
        if((kb = size / 1024) < 1)
            length = size + " B";
        else if((mb = kb / 1024) < 1)
            length = kb + " KB";
        else if((gb = mb / 1024) < 1)
            length = mb + " MB";
        else
            length = gb + " GB";
        return length;
    }
}
