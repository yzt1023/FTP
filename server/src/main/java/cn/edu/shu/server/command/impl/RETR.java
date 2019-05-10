/*
 * Written By : Skye You
 * Copyright (c) 2018 - 2019. All Rights Reserved.
 */

package cn.edu.shu.server.command.impl;

import cn.edu.shu.common.bean.DataType;
import cn.edu.shu.common.ftp.FTPReplyCode;
import cn.edu.shu.server.command.Command;
import cn.edu.shu.server.ftp.DataConnection;
import cn.edu.shu.server.ftp.FTPRequest;
import cn.edu.shu.server.ftp.FTPSession;
import org.apache.log4j.Logger;

import java.io.*;

public class RETR implements Command {

    private Logger logger = Logger.getLogger(getClass());

    public RETR() {
    }

    @Override
    public void execute(FTPSession session, FTPRequest request) {
        if (!session.getUser().isReadable()) {
            session.println(FTPReplyCode.FILE_UNAVAILABLE + " Permission denied");
            return;
        }

        if (!request.hasArgument()) {
            session.println(FTPReplyCode.INVALID_PARAMETER.getReply());
            return;
        }

        String path = session.getAbsolutePath(request.getArgument());
        String current = session.getCurrentPath(path);
        File file = new File(path);

        if (!file.exists() || file.isDirectory()) {
            session.println(FTPReplyCode.FILE_UNAVAILABLE + " No such file or directory");
            return;
        }

        if (session.getDataConnection().getSocketAddress() == null) {
            session.println(FTPReplyCode.BAD_SEQUENCE.getReply());
            return;
        }
        session.println(FTPReplyCode.FILE_STATUS_OK.getReply().replaceFirst("\\?", current));

        DataConnection dataConnection = session.getDataConnection();
        try {
            long len = session.getOffset();
            InputStream inputStream;

            if (session.getDataType() == DataType.ASCII) {
                long offset = 0L;
                inputStream = new BufferedInputStream(new FileInputStream(file));
                while (offset++ < len) {
                    int c;
                    if ((c = inputStream.read()) == -1) {
                        throw new IOException("Cannot skip");
                    }
                    if (c == '\n') {
                        ++offset;
                    }
                }
            } else {
                RandomAccessFile raf = new RandomAccessFile(file, "r");
                raf.seek(len);
                inputStream = new FileInputStream(raf.getFD());
            }

            try {
                dataConnection.openConnection();
            } catch (Exception e) {
                logger.error(e.getMessage(), e);
                session.println(FTPReplyCode.CANT_OPEN_DATA_CONNECTION.getReply());
                return;
            }

            dataConnection.transferToClient(session, inputStream);

            inputStream.close();
        } catch (IOException e) {
            logger.error(e.getMessage(), e);
            session.println(FTPReplyCode.CONNECTION_CLOSED.getReply());
            return;
        }finally {
            session.setOffset(0);
            dataConnection.closeConnection();
        }
        session.println(FTPReplyCode.CLOSING_DATA_CONNECTION.getReply().replaceFirst("\\?", current));
    }
}
