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

public class RNTO implements Command {

    public RNTO() {
    }

    @Override
    public void execute(FTPSession session, FTPRequest request) {
        if (!request.hasArgument()) {
            session.println(FTPReplyCode.INVALID_PARAMETER.getReply());
            return;
        }

        File src = session.getRenamedFile();
        if (src == null) {
            session.println(FTPReplyCode.BAD_SEQUENCE + " Cannot find the file which has to be renamed");
            return;
        }

        File des = new File(session.getAbsolutePath(request.getArgument()));
        if (des.exists()) {
            session.println(FTPReplyCode.FILE_UNAVAILABLE + " File already exists");
            return;
        }

        if (src.renameTo(des)) {
            session.setRenamedFile(null);
            session.println(FTPReplyCode.FILE_ACTION_OK + " Requested file action okay, file renamed");
        } else
            session.println(FTPReplyCode.FILE_NAME_NOT_ALLOWED.getReply());
    }
}
