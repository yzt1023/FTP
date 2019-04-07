/*
 * Written By : Skye You
 * Copyright (c) 2018 - 2019. All Rights Reserved.
 */

package cn.edu.shu.server.command.impl;

import cn.edu.shu.common.bean.User;
import cn.edu.shu.common.ftp.FTPReplyCode;
import cn.edu.shu.server.command.Command;
import cn.edu.shu.server.db.UserDao;
import cn.edu.shu.server.ftp.FTPRequest;
import cn.edu.shu.server.ftp.FTPSession;

public class REG implements Command {

    public REG() {
    }

    @Override
    public void execute(FTPSession session, FTPRequest request) {
        if(!request.hasArgument()) {
            session.println(FTPReplyCode.INVALID_PARAMETER.getReply());
            return;
        }

        String str = request.getArgument();
        int index = str.indexOf(" ");
        String username = str.substring(0, index);
        String password = str.substring(index + 1);
        UserDao userDao =session.getUserDao();
        if(userDao.isUserExists(username)) {
            session.println(FTPReplyCode.INVALID_PARAMETER + " The username has been registered!");
            return;
        }

        User user = new User(username, password, true, true, false);
        userDao.addUser(user);
        session.println(FTPReplyCode.COMMAND_OK.getReply());
    }
}
