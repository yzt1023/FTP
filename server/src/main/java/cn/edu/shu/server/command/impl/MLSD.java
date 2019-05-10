/*
 * Written By : Skye You
 * Copyright (c) 2018 - 2019. All Rights Reserved.
 */

package cn.edu.shu.server.command.impl;

import cn.edu.shu.common.ftp.FTPReplyCode;
import cn.edu.shu.common.util.Constants;
import cn.edu.shu.common.util.Utils;
import cn.edu.shu.server.command.Command;
import cn.edu.shu.server.ftp.DataConnection;
import cn.edu.shu.server.ftp.FTPRequest;
import cn.edu.shu.server.ftp.FTPSession;
import org.apache.log4j.Logger;

import javax.swing.filechooser.FileSystemView;
import java.io.File;
import java.io.IOException;
import java.util.Date;

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
        StringBuilder builder = new StringBuilder();
        if(file.isFile())
            builder.append(formatFile(file));
        else {
            for (File f : fileSystemView.getFiles(file, false))
                builder.append(formatFile(f));
        }

        try {
            dataConnection.openConnection();
        }catch (Exception e){
            logger.error(e.getMessage(), e);
            session.println(FTPReplyCode.CANT_OPEN_DATA_CONNECTION.getReply());
            return;
        }

        try {
            dataConnection.transferToClient(session, builder.toString());
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
        String modify = Utils.getInstance().formatDate(date);
        line += ";" + Constants.KEY_MODIFY + modify;
        if (!f.isDirectory())
            line += ";" + Constants.KEY_SIZE + f.length();
        line += "; " + f.getName() + Constants.LINE_SEPARATOR;
        return line;
    }
}
