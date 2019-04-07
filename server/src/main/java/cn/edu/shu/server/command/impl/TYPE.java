/*
 * Written By : Skye You
 * Copyright (c) 2018 - 2019. All Rights Reserved.
 */

package cn.edu.shu.server.command.impl;

import cn.edu.shu.common.bean.DataType;
import cn.edu.shu.common.ftp.FTPReplyCode;
import cn.edu.shu.server.command.Command;
import cn.edu.shu.server.ftp.FTPRequest;
import cn.edu.shu.server.ftp.FTPSession;

public class TYPE implements Command {

    public TYPE() {
    }

    @Override
    public void execute(FTPSession session, FTPRequest request) {
        if (request.hasArgument()){
            DataType dataType = DataType.parse(request.getArgument().charAt(0));
            if(dataType != null) {
                session.setDataType(dataType);
                session.println(FTPReplyCode.COMMAND_OK.getReply());
                return;
            }
        }
        session.println(FTPReplyCode.INVALID_PARAMETER.getReply());
    }
}
