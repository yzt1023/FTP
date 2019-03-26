package cn.edu.shu.client.ftp;

public interface FTPCommands {
    // connect and disconnect
    String USER = "USER";   //username
    String PASS = "PASS";   // password
    String QUIT = "QUIT";   // disconnect
    // transfer type and mode
    String TYPE_I = "TYPE I";   // binary transfer type
    String TYPE_A = "TYPE A";   // ascii transfer type
    String PASV = "PASV";   // enter passive mode
    String PORT = "PORT";   // open a data port
    // current directory
    String PWD = "PWD";     // print working directory
    String CWD = "CWD";     // change working directory
    String CDUP = "CDUP";   // up to parent directory
    String LIST = "LIST";   // list files
    String MLSD = "MLSD";   // list files after parsing
    // file info
    String SIZE = "SIZE";
    String MDTM = "MDTM";   // modify time of file
    // file operation
    String MKD = "MKD";     // make directory
    String RMD = "RMD";     // remove directory
    String RNTO = "RNTO";   // rename to
    String RNFR = "RNFR";   // rename from
    String REST = "REST";   // where file transfer is to be restarted
    String APPE = "APPE";   // appends data to the end of file
    String RETR = "RETR";   // download file
    String STOR = "STOR";   // upload file
    String DELE = "DELE";   // delete file
    String NOOP = "NOOP";   // no operation

    static boolean isReady(String reply){
        int replyCode = Integer.parseInt(reply.substring(0, 3));
        return replyCode >= 100 && replyCode < 200;
    }

    static boolean isCommandOkay(String reply){
        int replyCode = Integer.parseInt(reply.substring(0, 3));
        return replyCode >= 200 && replyCode < 300;
    }

    static boolean infoRequested(String reply){
        int replyCode = Integer.parseInt(reply.substring(0, 3));
        return replyCode >= 300 && replyCode < 400;
    }

    static boolean closeConnection(String reply){
        int replyCode = Integer.parseInt(reply.substring(0, 3));
        return replyCode >= 400 && replyCode < 500;
    }

    static boolean invalidCommand(String reply){
        int replyCode = Integer.parseInt(reply);
        return replyCode >= 500 && replyCode < 600;
    }
}
