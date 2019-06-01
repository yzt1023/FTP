/*
 * Written By : Skye You
 * Copyright (c) 2018 - 2019. All Rights Reserved.
 */

package cn.edu.shu.server.ftp;

import cn.edu.shu.common.bean.DataType;
import cn.edu.shu.common.encryption.MD5;
import cn.edu.shu.common.ftp.FTPReplyCode;
import cn.edu.shu.common.util.CommonUtils;
import cn.edu.shu.common.util.Constants;
import cn.edu.shu.server.config.SystemConfig;
import org.apache.log4j.Logger;

import java.io.*;
import java.net.InetAddress;
import java.net.InetSocketAddress;
import java.net.ServerSocket;
import java.net.Socket;
import java.util.List;

public class DataConnection {

    private Socket dataSocket;
    private FTPSession session;
    private boolean download;
    private InputStream inputStream;
    private OutputStream outputStream;
    private File file;
    private RandomAccessFile raf;

    private ServerSocket serverSocket;
    private boolean passiveMode;
    private InetSocketAddress socketAddress;
    private Logger logger = Logger.getLogger(getClass());
    private CommonUtils utils = CommonUtils.getInstance();
    private MD5 md5 = new MD5();
    private String clientMd5;

    DataConnection(FTPSession session) {
        this.session = session;
    }

    public void initActiveDataConnection(InetSocketAddress socketAddress) {
        closeConnection();
        passiveMode = false;
        this.socketAddress = socketAddress;
    }

    public InetSocketAddress initPassiveDataConnection(InetAddress inetAddress) {
        closeConnection();
        passiveMode = true;
        SystemConfig config = SystemConfig.getInstance();
        int port = utils.producePort(config.getPassiveMinPort(), config.getPassiveMaxPort());
        try {
            serverSocket = new ServerSocket(port, 1, inetAddress);
        } catch (IOException e) {
            logger.error(e.getMessage(), e);
            closeConnection();
        }

        socketAddress = new InetSocketAddress(inetAddress, serverSocket.getLocalPort());
        return socketAddress;
    }

    public void openConnection() throws Exception {
        if (passiveMode) {
            dataSocket = serverSocket.accept();
            serverSocket.close();
            serverSocket = null;
        } else {
            dataSocket = new Socket();
            dataSocket.bind(new InetSocketAddress(Constants.DEFAULT_DATA_PORT));
            dataSocket.connect(socketAddress);
        }
    }

    public void closeConnection() {
        try {
            if (dataSocket != null)
                dataSocket.close();
            dataSocket = null;

            if (inputStream != null)
                inputStream.close();
            inputStream = null;

            if (outputStream != null)
                outputStream.close();
            outputStream = null;

            if (raf != null)
                raf.close();
            raf = null;

        } catch (IOException e) {
            logger.error(e.getMessage(), e);
        }
    }

    public void transferToClient(List<String> files) throws IOException {
        OutputStream out = dataSocket.getOutputStream();
        OutputStreamWriter writer = new OutputStreamWriter(out, session.getEncoding());
        for (String file : files) {
            if (session.isSecureMode())
                file = session.encodeResponse(file);
            writer.write(file + Constants.LINE_SEPARATOR);
        }
        writer.flush();
        writer.close();
    }

    public void transferToClient(File file) throws IOException {
        long offset = session.getOffset();
        raf = new RandomAccessFile(file, "r");
        raf.seek(offset);

        inputStream = new FileInputStream(raf.getFD());
        outputStream = dataSocket.getOutputStream();
        download = true;
        this.file = file;
        startTransfer();
    }

    public void transferFromClient(File file) throws IOException {
        long offset = 0L;
        if (file.exists())
            offset = file.length();
        raf = new RandomAccessFile(file, "rw");
        raf.seek(offset);

        outputStream = new FileOutputStream(raf.getFD());
        inputStream = dataSocket.getInputStream();
        download = false;
        this.file = file;
        startTransfer();
    }

    private void transferEncrypted() throws IOException {
        int buff = Constants.KB;
        if (!download)
            buff += 16;

        byte[] bytes = new byte[Constants.KB + 16];
        DataInputStream in = new DataInputStream(inputStream);
        DataOutputStream out = new DataOutputStream(outputStream);
        if (download) {
            out.writeUTF(session.encodeResponse(md5.getFileMd5(file)));
        } else {
            clientMd5 = session.decodeRequest(in.readUTF());
        }

        int len;
        while ((len = in.read(bytes, 0, buff)) != -1) {
            if (download) {
                bytes = session.encodeBytes(bytes, len);
                int fill = 16 - (len % 16);
                len = len + fill;
            } else {
                len = session.decodeBytes(bytes, len);
            }
            out.write(bytes, 0, len);
        }
        out.flush();

        if (!download && !clientMd5.equals(md5.getFileMd5(file))) {
            session.println(FTPReplyCode.ACTION_ABORTED + " File was modified illegally");
            raf.close();
            file.delete();
        } else {
            session.println(FTPReplyCode.CLOSING_DATA_CONNECTION.getReply());
        }
    }

    private void transfer(DataType dataType, boolean secure) throws IOException {
        if (dataType == DataType.ASCII) {
            transferASCII(secure);
            return;
        }

        if (secure) {
            transferEncrypted();
            return;
        }

        BufferedInputStream bis = new BufferedInputStream(inputStream);
        BufferedOutputStream bos = new BufferedOutputStream(outputStream);
        int len;
        byte[] bytes = new byte[Constants.KB];
        while ((len = bis.read(bytes)) != -1) {
            bos.write(bytes, 0, len);
        }
        bos.flush();
        session.println(FTPReplyCode.CLOSING_DATA_CONNECTION.getReply());
    }

    private void transferASCII(boolean secure) throws IOException {
        BufferedReader reader = new BufferedReader(new InputStreamReader(inputStream));
        PrintWriter writer = new PrintWriter(outputStream, true);
        String line;
        while ((line = reader.readLine()) != null) {
            if (download && secure)
                line = session.encodeResponse(line);
            else if (secure) {
                line = session.decodeRequest(line);
            }
            writer.println(line);
        }
        session.println(FTPReplyCode.CLOSING_DATA_CONNECTION.getReply());
    }

    private void startTransfer() {
        new Thread(() -> {
            try {
                transfer(session.getDataType(), session.isSecureMode());
            } catch (IOException e) {
                logger.error(e.getMessage(), e);
                session.println(FTPReplyCode.CONNECTION_CLOSED.getReply());
            } finally {
                closeConnection();
            }
        }).start();
    }
}
