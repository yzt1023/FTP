/*
 * Written By : Skye You
 * Copyright (c) 2018 - 2019. All Rights Reserved.
 */

package cn.edu.shu.server.command.impl;

import cn.edu.shu.common.ftp.FTPReplyCode;
import cn.edu.shu.server.command.Command;
import cn.edu.shu.server.ftp.FTPRequest;
import cn.edu.shu.server.ftp.FTPSession;

public class AUTH implements Command {

    public AUTH() {
    }

    @Override
    public void execute(FTPSession session, FTPRequest request) {
        session.resetState();
        if(!request.hasArgument()){
            session.println(FTPReplyCode.INVALID_PARAMETER.getReply());
            return;
        }


    }
}
