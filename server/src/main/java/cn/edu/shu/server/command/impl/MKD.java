/*
 * Written By : Skye You
 * Copyright (c) 2018 - 2019. All Rights Reserved.
 */

package cn.edu.shu.server.command.impl;

import cn.edu.shu.common.ftp.FTPReplyCode;
import cn.edu.shu.server.command.Command;
import cn.edu.shu.server.ftp.FTPRequest;
import cn.edu.shu.server.ftp.FTPSession;

import java.io.File;

public class MKD implements Command {

    public MKD() {
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

        File file = new File(session.getAbsolutePath(request.getArgument()));
        if (file.exists()) {
            session.println(FTPReplyCode.FILE_UNAVAILABLE + " Directory already exists");
            return;
        }

        if (file.getParentFile() == null || !file.getParentFile().exists()) {
            session.println(FTPReplyCode.FILE_UNAVAILABLE + " Not a valid path");
            return;
        }

        if (file.mkdir())
            session.println(FTPReplyCode.FILE_ACTION_OK + " Directory created");
        else
            session.println(FTPReplyCode.FILE_UNAVAILABLE + " Cannot created directory");
    }
}
