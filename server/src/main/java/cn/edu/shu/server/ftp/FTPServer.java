package cn.edu.shu.server.ftp;

import cn.edu.shu.common.log.MsgListener;
import cn.edu.shu.common.util.Constants;
import cn.edu.shu.server.util.ConfigUtils;
import org.apache.log4j.Logger;

import java.io.IOException;
import java.net.ServerSocket;
import java.net.Socket;
import java.util.concurrent.LinkedBlockingQueue;
import java.util.concurrent.ThreadPoolExecutor;
import java.util.concurrent.TimeUnit;

public class FTPServer {
    private ServerSocket serverSocket;
    private ThreadPoolExecutor executor;
    private static FTPServer ftpServer;
    private MsgListener listener;
    private Logger logger;

    private FTPServer(){
        ConfigUtils.getInstance().initConfig();
        int corePoolSize = Runtime.getRuntime().availableProcessors();
        executor = new ThreadPoolExecutor(corePoolSize, 10, 5, TimeUnit.SECONDS, new LinkedBlockingQueue<>());
        logger = Logger.getLogger(getClass());
        try{
            serverSocket = new ServerSocket(Constants.DEFAULT_PORT);
        }catch (IOException e){
            logger.error(e.getMessage(), e);
        }
    }

    public void listen(){
        listener.println("Waiting for connect...");
        new Thread(() -> {
            try{
                while(!serverSocket.isClosed()){
                    Socket socket = serverSocket.accept();
                    listener.println("Connected, sending welcome message...");
                    socket.setTcpNoDelay(true);  // close the buffer of socket to ensure transfer speed
                    ControlConnection controlConnection = new ControlConnection(socket);
                    controlConnection.setListener(listener);
                    executor.execute(controlConnection);
                }
            }catch (IOException e){
                logger.error(e.getMessage(), e);
            }finally {
                closeSocket();
            }
        }).start();

    }

    public static FTPServer getFTPSever() {
        if(ftpServer == null){
            synchronized (FTPServer.class){
                if(ftpServer == null){
                    ftpServer = new FTPServer();
                }
            }
        }
        return ftpServer;
    }

    public void setListener(MsgListener listener) {
        this.listener = listener;
    }

    public void closeSocket(){
        if(serverSocket != null) {
            try {
                serverSocket.close();
            } catch (IOException e) {
                e.printStackTrace();
            }
        }
        executor.shutdownNow();
    }
}
