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
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.RandomAccessFile;

public class STOR implements Command {

    private Logger logger = Logger.getLogger(getClass());

    public STOR() {
    }

    @Override
    public void execute(FTPSession session, FTPRequest request) {
        if (!session.getUser().isWritable()) {
            session.println(FTPReplyCode.FILE_UNAVAILABLE + " Permission denied");
            return;
        }

        if (!request.hasArgument()) {
            session.println(FTPReplyCode.INVALID_PARAMETER.getReply());
            return;
        }

        if (session.getDataConnection().getSocketAddress() == null) {
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
            long offset = session.getOffset();
            RandomAccessFile raf = new RandomAccessFile(file, "rw");
            raf.setLength(offset);
            raf.seek(offset);
            FileOutputStream outputStream = new FileOutputStream(file);

            try {
                dataConnection.openConnection();
            } catch (Exception e) {
                logger.error(e.getMessage(), e);
                session.println(FTPReplyCode.CANT_OPEN_DATA_CONNECTION.getReply());
                return;
            }

            String md5 = dataConnection.transferFromClient(session, outputStream);
            if(session.isSecureMode()) {
                FTPRequest md5Request = new FTPRequest(session.readRequest());
                String clientMd5 = md5Request.getArgument();
                if(!clientMd5.equals(md5)) {
                    session.println(FTPReplyCode.ACTION_ABORTED + " File was modified illegally");
                    file.delete();
                    return;
                }
            }
            outputStream.close();
        } catch (IOException e) {
            logger.error(e.getMessage(), e);
            session.println(FTPReplyCode.CONNECTION_CLOSED.getReply());
            return;
        } finally {
            dataConnection.closeConnection();
        }
        session.println(FTPReplyCode.CLOSING_DATA_CONNECTION.getReply().replaceFirst("\\?", current));
    }
}
