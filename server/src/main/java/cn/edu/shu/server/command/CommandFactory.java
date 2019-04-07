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

    public static Command getCommand(String command){
        if(command == null || command.equals(""))
            return null;
        return commandMap.get(command.toUpperCase());
    }

    static {
/*        commandMap.put("ABOR", new ABOR());
        commandMap.put("ACCT", new ACCT());
        commandMap.put("APPE", new APPE());
        commandMap.put("AUTH", new AUTH());
        commandMap.put("EPRT", new EPRT());
        commandMap.put("EPSV", new EPSV());
        commandMap.put("FEAT", new FEAT());
        commandMap.put("HELP", new HELP());
        commandMap.put("LANG", new LANG());
        commandMap.put("LIST", new LIST());
        commandMap.put("MD5", new MD5());
        commandMap.put("MFMT", new MFMT());
        commandMap.put("MMD5", new MD5());
        commandMap.put("MDTM", new MDTM());
        commandMap.put("MLST", new MLST());
        commandMap.put("MODE", new MODE());
        commandMap.put("NLST", new NLST());
        commandMap.put("OPTS", new OPTS());
        commandMap.put("PBSZ", new PBSZ());
        commandMap.put("PROT", new PROT());
        commandMap.put("REIN", new REIN());
        commandMap.put("REST", new REST());
        commandMap.put("SITE", new SITE());
        commandMap.put("SIZE", new SIZE());
        commandMap.put("STAT", new STAT());
        commandMap.put("STOU", new STOU());
        commandMap.put("STRU", new STRU());
        commandMap.put("SYST", new SYST());*/
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
        commandMap.put(FTPCommand.STOR, new STOR());
        commandMap.put(FTPCommand.TYPE, new TYPE());
        commandMap.put(FTPCommand.USER, new USER());
    }

}
