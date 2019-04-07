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

    public MLSD() {
    }

    @Override
    public void execute(FTPSession session, FTPRequest request) {
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

        if (!file.exists() || !file.isDirectory()) {
            session.println(FTPReplyCode.FILE_UNAVAILABLE + " Directory not found");
            return ;
        }
        session.println(FTPReplyCode.FILE_STATUS_OK.getReply().replaceFirst("\\?", current));

        DataConnection dataConnection = session.getDataConnection();
        FileSystemView fileSystemView = session.getFileSystemView();
        StringBuilder builder = new StringBuilder();

        for (File f : fileSystemView.getFiles(file, false)) {
            String type = fileSystemView.getSystemTypeDescription(f);
            builder.append(Constants.KEY_TYPE).append(type);
            Date date = new Date(f.lastModified());
            String modify = Utils.getInstance().formatDate(date);
            builder.append(";").append(Constants.KEY_MODIFY).append(modify);
            if (!f.isDirectory())
                builder.append(";").append(Constants.KEY_SIZE).append(f.length());
            builder.append("; ").append(f.getName());
            builder.append(Constants.LINE_SEPARATOR);
        }

        try {
            dataConnection.openConnection();
        }catch (IOException e){
            logger.error(e.getMessage(), e);
            session.println(FTPReplyCode.CANT_OPEN_DATA_CONNECTION.getReply());
            return;
        }

        dataConnection.transferToClient(session, builder.toString());
        dataConnection.closeConnection();
        session.println(FTPReplyCode.CLOSING_DATA_CONNECTION.getReply().replaceFirst("\\?", current));
    }
}
