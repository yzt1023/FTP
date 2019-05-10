package cn.edu.shu.common.ftp;

public interface FTPCommand {

    String USER = "USER";   // username
    String PASS = "PASS";   // password
    String REG = "REG";     // register
    String QUIT = "QUIT";   // disconnect

    String AUTH = "AUTH";
    String PBSZ = "PBSZ";
    String PROT = "PROT";

    String TYPE = "TYPE";
    String PASV = "PASV";   // enter passive mode
    String PORT = "PORT";   // open a data port
    String MODE = "MODE";

    String PWD = "PWD";     // println working directory
    String CWD = "CWD";     // change working directory
    String CDUP = "CDUP";   // up to parent directory
    String LIST = "LIST";   // list files
    String MLSD = "MLSD";   // list files after parsing

    String SIZE = "SIZE";
    String MDTM = "MDTM";   // modify time of file

    String MKD = "MKD";     // make directory
    String RMD = "RMD";     // remove directory
    String DELE = "DELE";   // delete file
    String RNTO = "RNTO";   // rename to
    String RNFR = "RNFR";   // rename from

    String REST = "REST";   // where file transfer is to be restarted
    String APPE = "APPE";   // appends data to the end of file
    String RETR = "RETR";   // download file
    String STOR = "STOR";   // upload file
    String STOU = "STOU";
    String ALLO = "ALLO";
    String ABOR = "ABOR";

    String SITE = "SITE";
    String SYST = "SYST";
    String STAT = "STAT";
    String SMNT = "SMNT";
    String HELP = "HELP";
    String NOOP = "NOOP";   // no operation
}
