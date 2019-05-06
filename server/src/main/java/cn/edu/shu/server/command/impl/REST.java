/*
 * Written By : Skye You
 * Copyright (c) 2018 - 2019. All Rights Reserved.
 */

package cn.edu.shu.server.command.impl;

import cn.edu.shu.common.ftp.FTPReplyCode;
import cn.edu.shu.server.command.Command;
import cn.edu.shu.server.ftp.FTPRequest;
import cn.edu.shu.server.ftp.FTPSession;

public class REST implements Command {

    public REST() {
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

        long len = 0;
        try {
            len = Long.parseLong(request.getArgument());
            if (len < 0) {
                len = 0;
                session.println(FTPReplyCode.INVALID_PARAMETER + " Marker cannot be negative");
            } else {
                session.println(FTPReplyCode.FILE_ACTION_PENDING + " Send STORE or RETRIEVE to initiate transfer");
            }
        } catch (NumberFormatException e) {
            session.println(FTPReplyCode.INVALID_PARAMETER + " Not a valid number");
        }
        session.setOffset(len);
    }
}
