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

public class CWD implements Command {

    public CWD() {
    }

    @Override
    public void execute(FTPSession session, FTPRequest request) {
        String path = session.getAbsolutePath(request.getArgument());
        String current = session.getCurrentPath(path);
        File file = new File(path);
        if (!file.exists() || !file.isDirectory()) {
            session.println(FTPReplyCode.FILE_UNAVAILABLE + " No such directory");
            return ;
        }

        session.setCurrentPath(current);
        session.println(FTPReplyCode.FILE_ACTION_OK.getReply());
    }
}
