/*
 * Written By : Skye You
 * Copyright (c) 2018 - 2019. All Rights Reserved.
 */

package cn.edu.shu.server.command.impl;

import cn.edu.shu.common.ftp.FTPReplyCode;
import cn.edu.shu.server.command.Command;
import cn.edu.shu.server.ftp.DataConnection;
import cn.edu.shu.server.ftp.FTPRequest;
import cn.edu.shu.server.ftp.FTPSession;
import org.apache.log4j.Logger;

import java.io.File;
import java.io.IOException;

public class APPE implements Command {

    private Logger logger = Logger.getLogger(getClass());

    public APPE() {
    }

    @Override
    public void execute(FTPSession session, FTPRequest request) {
        session.resetState();
        if (!session.getUser().isWritable()) {
            session.println(FTPReplyCode.FILE_UNAVAILABLE + " Permission denied");
            return;
        }

        if (!request.hasArgument()) {
            session.println(FTPReplyCode.INVALID_PARAMETER.getReply());
            return;
        }

        if (session.getDataConnection() == null) {
            session.println(FTPReplyCode.BAD_SEQUENCE.getReply());
            return;
        }

        String path = session.getAbsolutePath(request.getArgument());
        String current = session.getCurrentPath(path);
        File file = new File(path);

        if (!file.exists() && (file.getParentFile() == null || !file.getParentFile().exists())) {
            session.println(FTPReplyCode.FILE_UNAVAILABLE + " Invalid path");
            return;
        }
        session.println(FTPReplyCode.FILE_STATUS_OK.getReply().replaceFirst("\\?", current));
        DataConnection dataConnection = session.getDataConnection();


        try {
            dataConnection.openConnection();
        } catch (Exception e) {
            logger.error(e.getMessage(), e);
            session.println(FTPReplyCode.CANT_OPEN_DATA_CONNECTION.getReply());
            return;
        }

        try {
            dataConnection.transferFromClient(file);
        } catch (IOException e) {
            logger.error(e.getMessage(), e);
            session.println(FTPReplyCode.CONNECTION_CLOSED.getReply());
        }
    }
}
