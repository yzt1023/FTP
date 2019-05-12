/*
 * Written By : Skye You
 * Copyright (c) 2018 - 2019. All Rights Reserved.
 */

package cn.edu.shu.server.command.impl;

import cn.edu.shu.common.ftp.FTPReplyCode;
import cn.edu.shu.common.util.CommonUtils;
import cn.edu.shu.server.command.Command;
import cn.edu.shu.server.ftp.DataConnection;
import cn.edu.shu.server.ftp.FTPRequest;
import cn.edu.shu.server.ftp.FTPSession;

import java.net.InetSocketAddress;

public class PORT implements Command {

    public PORT() {
    }

    @Override
    public void execute(FTPSession session, FTPRequest request) {
        session.resetState();
        if(!request.hasArgument()) {
            session.println(FTPReplyCode.INVALID_PARAMETER.getReply());
            return;
        }

        String str = request.getArgument();
        InetSocketAddress socketAddress = CommonUtils.getInstance().getAddressByString(str);
        if(socketAddress == null){
            session.println(FTPReplyCode.CANT_OPEN_DATA_CONNECTION.getReply());
            return;
        }

        DataConnection dataConnection = session.getDataConnection();
        dataConnection.initActiveDataConnection(socketAddress);
        session.println(FTPReplyCode.COMMAND_OK.getReply());
    }
}
