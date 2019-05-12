/*
 * Written By : Skye You
 * Copyright (c) 2018 - 2019. All Rights Reserved.
 */

package cn.edu.shu.server.command.impl;

import cn.edu.shu.common.ftp.FTPReplyCode;
import cn.edu.shu.common.util.Constants;
import cn.edu.shu.common.util.CommonUtils;
import cn.edu.shu.server.command.Command;
import cn.edu.shu.server.ftp.DataConnection;
import cn.edu.shu.server.ftp.FTPRequest;
import cn.edu.shu.server.ftp.FTPSession;
import org.apache.log4j.Logger;

import javax.swing.filechooser.FileSystemView;
import java.io.File;
import java.io.IOException;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;

public class MLSD implements Command {

    private Logger logger = Logger.getLogger(getClass());
    private FileSystemView fileSystemView = FileSystemView.getFileSystemView();

    public MLSD() {
    }

    @Override
    public void execute(FTPSession session, FTPRequest request) {
        session.resetState();
        if (!session.getUser().isReadable()) {
            session.println(FTPReplyCode.FILE_UNAVAILABLE + " Permission denied");
            return;
        }

        if (session.getDataConnection().getSocketAddress() == null) {
            session.println(FTPReplyCode.BAD_SEQUENCE.getReply());
            return;
        }

        String path = session.getAbsolutePath(request.getArgument());
        String current = session.getCurrentPath(path);
        File file = new File(path);

        if (!file.exists()) {
            session.println(FTPReplyCode.FILE_UNAVAILABLE + " Directory not found");
            return ;
        }
        session.println(FTPReplyCode.FILE_STATUS_OK.getReply().replaceFirst("\\?", current));

        DataConnection dataConnection = session.getDataConnection();
        FileSystemView fileSystemView = session.getFileSystemView();
        List<String> files = new ArrayList<>();
        if(file.isFile())
            files.add(formatFile(file));
        else {
            for (File f : fileSystemView.getFiles(file, false))
                files.add(formatFile(f));
        }

        try {
            dataConnection.openConnection();
        }catch (Exception e){
            logger.error(e.getMessage(), e);
            session.println(FTPReplyCode.CANT_OPEN_DATA_CONNECTION.getReply());
            return;
        }

        try {
            dataConnection.transferToClient(session, files);
        }catch (IOException e){
            logger.error(e.getMessage(), e);
            session.println(FTPReplyCode.CONNECTION_CLOSED.getReply());
            return;
        }finally {
            dataConnection.closeConnection();
        }

        session.println(FTPReplyCode.CLOSING_DATA_CONNECTION.getReply().replaceFirst("\\?", current));
    }

    private String formatFile(File f) {
        String line = "";
        String type = fileSystemView.getSystemTypeDescription(f);
        line += Constants.KEY_TYPE + type;
        Date date = new Date(f.lastModified());
        String modify = CommonUtils.getInstance().formatDate(date);
        line += ";" + Constants.KEY_MODIFY + modify;
        if (!f.isDirectory())
            line += ";" + Constants.KEY_SIZE + f.length();
        line += "; " + f.getName();
        return line;
    }
}
