/*
 * Written By : Skye You
 * Copyright (c) 2018 - 2019. All Rights Reserved.
 */

package cn.edu.shu.server.command.impl;

import cn.edu.shu.common.ftp.FTPReplyCode;
import cn.edu.shu.common.util.Utils;
import cn.edu.shu.server.command.Command;
import cn.edu.shu.server.ftp.DataConnection;
import cn.edu.shu.server.ftp.FTPRequest;
import cn.edu.shu.server.ftp.FTPSession;

import java.net.InetSocketAddress;

public class PASV implements Command {

    public PASV() {
    }

    @Override
    public void execute(FTPSession session, FTPRequest request) {
        if(request.hasArgument()) {
            session.println(FTPReplyCode.INVALID_PARAMETER.getReply());
            return;
        }

        DataConnection dataConnection = session.getDataConnection();
        InetSocketAddress socketAddress = dataConnection.initPassiveDataConnection(session.getControlAddress());

        if(socketAddress == null){
            session.println(FTPReplyCode.CANT_OPEN_DATA_CONNECTION + " Cannot open passive connection");
            return;
        }

        String addr = Utils.getInstance().getStringByAddress(socketAddress);
        session.println(FTPReplyCode.ENTERING_PASSIVE_MODE.getReply() + "(" + addr + ")");
    }
}
