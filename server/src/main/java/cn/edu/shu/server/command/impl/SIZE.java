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

public class SIZE implements Command {

    public SIZE() {
    }

    @Override
    public void execute(FTPSession session, FTPRequest request) {
        if(!request.hasArgument()) {
            session.println(FTPReplyCode.INVALID_PARAMETER.getReply());
            return;
        }

        File file = new File(session.getAbsolutePath(request.getArgument()));
        if(!file.exists()) {
            session.println(FTPReplyCode.FILE_UNAVAILABLE + " No such file or directory");
            return;
        }

        session.println(FTPReplyCode.FILE_STATUS + " " + file.length());
    }
}
