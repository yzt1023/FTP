package cn.edu.shu.server.ftp;

import cn.edu.shu.common.log.MsgListener;
import cn.edu.shu.common.util.Constants;
import cn.edu.shu.server.config.SystemConfig;
import org.apache.log4j.Logger;

import java.io.IOException;
import java.net.ServerSocket;
import java.net.Socket;
import java.net.SocketTimeoutException;
import java.util.concurrent.LinkedBlockingQueue;
import java.util.concurrent.ThreadPoolExecutor;
import java.util.concurrent.TimeUnit;

public class FTPServer extends Thread {
    private ServerSocket serverSocket;
    private ThreadPoolExecutor executor;
    //    private Thread server;
    private MsgListener listener;
    private Logger logger;
    private boolean stop;
    private SystemConfig config;

    public FTPServer() {
        config = SystemConfig.getInstance();
        config.initConfig();
        logger = Logger.getLogger(getClass());
        int corePoolSize = Runtime.getRuntime().availableProcessors();
        executor = new ThreadPoolExecutor(corePoolSize, 10, 5, TimeUnit.SECONDS, new LinkedBlockingQueue<>());
        try {
            serverSocket = new ServerSocket(Constants.DEFAULT_CONTROL_PORT);
        } catch (Exception e) {
            logger.error(e.getMessage(), e);
        }
        stop = false;
    }

    @Override
    public void run() {
        listener.println("Waiting for connect...");
        while (!stop) {
            listen();
            try {
                Thread.sleep(1000);
            } catch (InterruptedException e) {
                logger.error(e.getMessage(), e);
            }
        }
    }

    private void listen() {
        try {
            while (!stop) {
                Socket socket = serverSocket.accept();
                socket.setSoTimeout(1000);
                listener.println("Connected, sending welcome message...");
                socket.setTcpNoDelay(true);  // close the buffer of socket to ensure transfer speed
                int timeout = config.getTimeout();
                socket.setSoTimeout(timeout * 1000);

                ControlConnection controlConnection = new ControlConnection(socket, config);
                controlConnection.setListener(listener);
                executor.execute(controlConnection);
            }
        } catch (SocketTimeoutException ignored) {
            listen();
        } catch (IOException e) {
            logger.error(e.getMessage(), e);
        }
    }

    public void stopServer() {
        stop = true;
        closeSocket();
        executor.shutdownNow();
    }

    public boolean isStop() {
        return stop;
    }

    public void setListener(MsgListener listener) {
        this.listener = listener;
    }

    private void closeSocket() {
        if (serverSocket != null) {
            try {
                serverSocket.close();
                serverSocket = null;
            } catch (IOException e) {
                logger.error(e.getMessage(), e);
            }
        }
    }
}
