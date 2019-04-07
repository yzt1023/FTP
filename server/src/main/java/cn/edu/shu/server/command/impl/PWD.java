/*
 * Written By : Skye You
 * Copyright (c) 2018 - 2019. All Rights Reserved.
 */

package cn.edu.shu.server.command.impl;

import cn.edu.shu.common.ftp.FTPReplyCode;
import cn.edu.shu.server.command.Command;
import cn.edu.shu.server.ftp.FTPRequest;
import cn.edu.shu.server.ftp.FTPSession;

public class PWD implements Command {

    public PWD() {
    }

    @Override
    public void execute(FTPSession session, FTPRequest request) {
        if (request.hasArgument())
            session.println(FTPReplyCode.INVALID_PARAMETER.getReply());
        else {
            String message = FTPReplyCode.PATHNAME_CREATED.getMessage();
            message = message.replaceFirst("\\?", session.getCurrentPath());
            session.println(FTPReplyCode.PATHNAME_CREATED + " " + message);
        }
    }
}
