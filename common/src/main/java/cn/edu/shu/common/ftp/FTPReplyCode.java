/*
 * Written By : Skye You
 * Copyright (c) 2018 - 2019. All Rights Reserved.
 */

package cn.edu.shu.common.ftp;

public enum FTPReplyCode {
    RESTART_MARKER(110, "Restart marker reply"),
    SERVICE_READY_IN(120, "Service ready in "),
    DATA_CONNECTION_ALREADY_OPEN(125, "Data connection already open; transfer starting"),
    FILE_STATUS_OK(150, "Opening data channel for directory listing of \"?\""),
    COMMAND_OK(200, "Command okay"),
    NOT_IMPLEMENTED(202, "Command not implemented, superfluous at this site"),
    SYSTEM_STATUS(211, "System status, or system help reply"),
    DIRECTORY_STATUS(212, "Directory status"),
    FILE_STATUS(213, "File status"),
    HELP_MESSAGE(214, "Help message"),
    NAME_SYSTEM_TYPE(215, "NAME system type"),
    SERVICE_READY(220, "Service ready for new user"),
    SERVICE_CLOSING(221, "Service closing control connection"),
    DATA_CONNECTION_OPEN(225, "Data connection open; no transfer in progress"),
    CLOSING_DATA_CONNECTION(226, "Successfully transferred \"?\""),
    ENTERING_PASSIVE_MODE(227, "Entering Passive Mode "),
    LOGGED_IN(230, "User logged in, proceed"),
    FILE_ACTION_OK(250, "Requested file action okay, completed"),
    PATHNAME_CREATED(257, "\"?\" is current directory"),
    NEED_PASSWORD(331, "User name okay, need password"),
    NEED_ACCOUNT(332, "Need account for login"),
    FILE_ACTION_PENDING(350, "Requested file action pending further information"),
    SERVICE_NOT_AVAILABLE(421, "Service not available, closing control connection"),
    CANT_OPEN_DATA_CONNECTION(425, "Can't open data connection"),
    CONNECTION_CLOSED(426, "Connection closed; transfer aborted"),
    FILE_ACTION_NOT_TAKEN(450, "Requested file action not taken"),
    ACTION_ABORTED(451, "Requested action aborted. Local error in processing"),
    ACTION_NOT_TAKEN(452, "Requested action not taken"),
    COMMAND_UNRECOGNIZED(500, "Syntax error, command unrecognized"),
    INVALID_PARAMETER(501, "Syntax error in parameters or arguments"),
    BAD_SEQUENCE(503, "Bad sequence of commands"),
    NOT_IMPLEMENTED_FOR_PARAMETER(504, "Command not implemented for that parameter"),
    NOT_LOGGED_IN(530, "Not logged in"),
    NEED_ACCOUNT_FOR_STORING(532, "Need account for storing files"),
    FILE_UNAVAILABLE(550, "File unavailable"),
    PAGE_TYPE_UNKNOWN(551, "Page type unknown"),
    EXCEEDED_STORAGE(552, "Exceeded storage allocation"),
    FILE_NAME_NOT_ALLOWED(553, "File name not allowed"),
    UNKNOWN_ERROR(600, "");

    private final int value;
    private final String message;

    FTPReplyCode(int value, String message){
        this.value = value;
        this.message = message;
    }

    public static FTPReplyCode find(int value) {
        FTPReplyCode[] codes = values();
        for (FTPReplyCode code : codes)
            if (code.getValue() == value)
                return code;
        return UNKNOWN_ERROR;
    }

    public int getValue(){
        return value;
    }

    public String getMessage(){
        return message;
    }

    @Override
    public String toString(){
        return String.valueOf(value);
    }

    public String getReply(){
        return value + " " + message;
    }

    public boolean isReady(){
        return value >= 100 && value < 200;
    }

    public boolean isOkay(){
        return value >= 200 && value < 300;
    }

    public boolean isInfoRequested(){
        return value >= 300 && value < 400;
    }

    public boolean isCnctClosed(){
        return value >= 400 && value < 500;
    }

    public boolean isInvalid(){
        return value >= 500 && value < 600;
    }
}
