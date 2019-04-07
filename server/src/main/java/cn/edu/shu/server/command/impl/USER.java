/*
 * Written By : Skye You
 * Copyright (c) 2018 - 2019. All Rights Reserved.
 */

package cn.edu.shu.server.command.impl;

import cn.edu.shu.common.bean.User;
import cn.edu.shu.common.ftp.FTPReplyCode;
import cn.edu.shu.server.command.Command;
import cn.edu.shu.server.ftp.FTPRequest;
import cn.edu.shu.server.ftp.FTPSession;

public class USER implements Command {

    public USER() {
    }

    @Override
    public void execute(FTPSession session, FTPRequest request) {
        if (!request.hasArgument())
            session.println(FTPReplyCode.INVALID_PARAMETER.getReply());
        else {
            User user = session.getUserDao().getUserByName(request.getArgument());
            if (user == null)
                session.println(FTPReplyCode.NOT_LOGGED_IN + " Invalid user name");
            else {
                session.setUser(user);
                session.println(FTPReplyCode.NEED_PASSWORD.getReply());
            }
        }
    }
}
