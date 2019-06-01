/*
 * Written By : Skye You
 * Copyright (c) 2018 - 2019. All Rights Reserved.
 */

package cn.edu.shu.server.command;

import cn.edu.shu.common.ftp.FTPCommand;
import cn.edu.shu.server.command.impl.*;

import java.util.HashMap;
import java.util.Map;

public class CommandFactory {
    private static Map<String, Command> commandMap = new HashMap<>();

    static {
        commandMap.put(FTPCommand.APPE, new APPE());
        commandMap.put(FTPCommand.AUTH, new AUTH());
        commandMap.put(FTPCommand.CDUP, new CDUP());
        commandMap.put(FTPCommand.CWD, new CWD());
        commandMap.put(FTPCommand.DELE, new DELE());
        commandMap.put(FTPCommand.MKD, new MKD());
        commandMap.put(FTPCommand.MLSD, new MLSD());
        commandMap.put(FTPCommand.NOOP, new NOOP());
        commandMap.put(FTPCommand.PASS, new PASS());
        commandMap.put(FTPCommand.PASV, new PASV());
        commandMap.put(FTPCommand.PORT, new PORT());
        commandMap.put(FTPCommand.PWD, new PWD());
        commandMap.put(FTPCommand.QUIT, new QUIT());
        commandMap.put(FTPCommand.RETR, new RETR());
        commandMap.put(FTPCommand.RMD, new RMD());
        commandMap.put(FTPCommand.RNFR, new RNFR());
        commandMap.put(FTPCommand.RNTO, new RNTO());
        commandMap.put(FTPCommand.REG, new REG());
        commandMap.put(FTPCommand.REST, new REST());
        commandMap.put(FTPCommand.SIZE, new SIZE());
        commandMap.put(FTPCommand.TYPE, new TYPE());
        commandMap.put(FTPCommand.USER, new USER());
    }

    public static Command getCommand(String command) {
        if (command == null || command.equals(""))
            return null;
        return commandMap.get(command.toUpperCase());
    }

}
