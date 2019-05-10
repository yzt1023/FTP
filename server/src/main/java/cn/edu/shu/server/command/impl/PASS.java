/*
 * Written By : Skye You
 * Copyright (c) 2018 - 2019. All Rights Reserved.
 */

package cn.edu.shu.server.command.impl;

import cn.edu.shu.common.bean.User;
import cn.edu.shu.common.ftp.FTPReplyCode;
import cn.edu.shu.common.util.Constants;
import cn.edu.shu.server.command.Command;
import cn.edu.shu.server.ftp.FTPRequest;
import cn.edu.shu.server.ftp.FTPSession;

public class PASS implements Command {

    public PASS() {
    }

    @Override
    public void execute(FTPSession session, FTPRequest request) {
        session.resetState();
        User user = session.getUser();
        if (user == null)
            session.println(FTPReplyCode.BAD_SEQUENCE + " Login with USER first");
        else if (!Constants.ANONYMOUS_USER.equals(user.getUsername()) && !user.getPassword().equals(request.getArgument()))
            session.println(FTPReplyCode.NOT_LOGGED_IN + " Authentication failed");
        else{
            String permission = "(";
            permission += user.isReadable() ? "1" : "0";
            permission += user.isWritable() ? "1" : "0";
            permission += user.canDeleted() ? "1)" : "0)";
            session.println(FTPReplyCode.LOGGED_IN.getReply() + ", and permission is" + permission);
        }
    }
}
