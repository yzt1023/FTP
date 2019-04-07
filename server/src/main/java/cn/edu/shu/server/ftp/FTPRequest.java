/*
 * Written By : Skye You
 * Copyright (c) 2018 - 2019. All Rights Reserved.
 */

package cn.edu.shu.server.ftp;

public class FTPRequest {
    private String requestLine;
    private String command;
    private String argument;

    public FTPRequest(String line){
        parse(line);
    }

    private void parse(String line){
        requestLine = line.trim();
        int index = requestLine.indexOf(" ");
        if(index != -1){
            command = requestLine.substring(0, index);
            command = command.toUpperCase();
            argument = requestLine.substring(index + 1);
        }else {
            command = requestLine.toUpperCase();
            argument = null;
        }
    }

    public String getRequestLine() {
        return requestLine;
    }

    public String getCommand() {
        return command;
    }

    public String getArgument() {
        return argument;
    }

    public boolean hasArgument(){
        return argument != null;
    }
}
