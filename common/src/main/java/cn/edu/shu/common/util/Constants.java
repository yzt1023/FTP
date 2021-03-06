/*
 * Written By : Skye You
 * Copyright (c) 2018 - 2019. All Rights Reserved.
 */

package cn.edu.shu.common.util;

public interface Constants {
    String TEMP_DIR = System.getProperty("java.io.tmpdir");
    String LINE_SEPARATOR = System.getProperty("line.separator");
    byte[] EOL = LINE_SEPARATOR.getBytes();
    String NET_EOL = "\r\n";
    String ANONYMOUS_USER = "anonymous";
    int DEFAULT_CONTROL_PORT = 21;
    int DEFAULT_DATA_PORT = 20;
    String DATE_PATTERN = "yyyyMMddHHmm";
    String KEY_MODIFY = "modify=";
    String KEY_SIZE = "size=";
    String KEY_TYPE = "type=";
    String FILE_FOLDER = "File folder";
    String SYSTEM_FOLDER = "System Folder";
    String TYPE_DIR = "dir";
    String SEPARATOR = "/";
    int KB = 1024;
    // file operation
    String PATH_NOT_EXISTS = "The specified path does not exist!";
    String FILE_EXISTS = "This file name has already existed in the exact path!";
    String FILE_OPEN_FAILED = "Fail to open the file!";
    String FILE_DELETE_FAILED = "Fail to delete the file!";
    String FILE_RENAME_FAILED = "Rename failed!";
    String FOLDER_CREATE_FAILED = "New folder failed!";
    String INIT_NAME = "New Folder";
    String EMPTY_FILENAME = "You must type a file name!";
    String OPERATION_FAILED = "File operation failed!";
    // operation title
    String NEW_FOLDER_TITLE = "New Folder";
    String OPEN_FILE_TITLE = "Open File";
    String DELETE_FILE_TITLE = "Delete File";
    String RENAME_FILE_TITLE = "Rename File";

    String CONFIRM_TO_EXIT = "Are you sure to exit the system?";
    // connect
    String EMPTY_HOST = "Host cannot be empty!";
    String EMPTY_USER = "Username cannot be empty!";
    String INVALID_PORT = "Invalid port number!";
    String CONNECT_FAILED = "Unable to connect to the specified server!";
    String CONNECT_FIRST = "Please connect first!";
    String REGISTER_SUCCEED = "Register successfully!";
    String RECONNECT_SUCCEED = "Connection timed out and reconnect succeed!";
    String SEND_COMMAND_ERROR = "Send command error!";

    // user
    String EMPTY_PASSWORD = "Password cannot be empty!";
    String USER_EXISTS = "Username has been registered! Please use another username!";
    String PASSWORD_DIFFERENT = "The two passwords entered are different!";
    String PERMISSION_DENIED = "Permission denied!";

    // transfer task state
    String STATE_WAITING = "WAITING";
    String STATE_PROCESSING = "PROCESSING";
    String STATE_PAUSE = "PAUSE";
    String STATE_FAILURE = "FAILURE";
    String STATE_SUCCESS = "SUCCESS";
    // transfer task
    String CLEAR_FAILED = "Tasks cannot be emptied because there are some outstanding tasks!";
    // settings
    String EMPTY_INPUT = "Parameters cannot be empty! Please check your input!";
    String CHARSET_NOT_EXISTS = "The charset inputted does not exist or is not supported!";
    String NUMBER_INCORRECT = "The number format is incorrect!";
    String PORT_INCORRECT = "Incorrect port range!";
    String TIME_INCORRECT = "Incorrect timeout!";
    String SAVE_SUCCEED = "Save successfully!";
    String SAVE_FAILED = "Failed to save!";
    String NO_ITEM_SELECTED = "No item is selected!";
    String REMOVE_SUCCEED = "Remove successfully!";
    String REMOVE_FAILED = "Failed to remove!";
    String ADD_SUCCEED = "Add successfully!";
    String ADD_FAILED = "File type existed!";
    String INVALID_PATH = "Invalid path! Folder not exists or not a folder.";
}
