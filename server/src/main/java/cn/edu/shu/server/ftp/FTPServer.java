package cn.edu.shu.server.ftp;

import cn.edu.shu.common.log.MsgListener;
import cn.edu.shu.common.util.Constants;
import cn.edu.shu.server.util.ConfigUtils;
import org.apache.log4j.Logger;

import java.io.IOException;
import java.net.ServerSocket;
import java.net.Socket;
import java.net.SocketTimeoutException;
import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.LinkedBlockingQueue;
import java.util.concurrent.ThreadPoolExecutor;
import java.util.concurrent.TimeUnit;

public class FTPServer extends Thread {
    private ServerSocket serverSocket;
    private ThreadPoolExecutor executor;
    //    private Thread server;
    private MsgListener listener;
    private Logger logger;
    private List<FTPSession> sessions;
    private boolean suspend;
    private boolean stop;

    public FTPServer() {
        ConfigUtils.getInstance().initConfig();
        logger = Logger.getLogger(getClass());
        sessions = new ArrayList<>();
        int corePoolSize = Runtime.getRuntime().availableProcessors();
        executor = new ThreadPoolExecutor(corePoolSize, 10, 5, TimeUnit.SECONDS, new LinkedBlockingQueue<>());
        try {
            serverSocket = new ServerSocket(Constants.DEFAULT_PORT);
        } catch (IOException e) {
            logger.error(e.getMessage(), e);
        }
        suspend = false;
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
            while (!suspend) {
                serverSocket.setSoTimeout(1000);
                Socket socket = serverSocket.accept();
                listener.println("Connected, sending welcome message...");
                socket.setTcpNoDelay(true);  // close the buffer of socket to ensure transfer speed
                ControlConnection controlConnection = new ControlConnection(socket);
                controlConnection.setListener(listener);
                sessions.add(controlConnection.getSession());
                executor.execute(controlConnection);
            }
        } catch (SocketTimeoutException ignored) {
            listen();
        } catch (IOException e) {
            logger.error(e.getMessage(), e);
        }
    }

    public boolean isSuspend() {
        return suspend;
    }

    public void suspendServer() {
        suspend = true;
        for (FTPSession session : sessions) {
            if (session != null)
                session.close();
        }
        listener.println("Connection to server closed");
    }

    public void resumeServer() {
        suspend = false;
    }

    public void stopServer() {
        suspend = true;
        stop = true;
        try {
            Thread.sleep(2000);
        } catch (InterruptedException e) {
            e.printStackTrace();
        }
        closeSocket();
        executor.shutdownNow();
        this.interrupt();
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
                e.printStackTrace();
            }
        }
    }
/*
    class ListenThread extends Thread{
        @Override
        public void run() {
            try {
                listener.println("Waiting for connect...");
                while (listening) {
                    serverSocket.setSoTimeout(5000);
                    Socket socket = serverSocket.accept();
                    listener.println("Connected, sending welcome message...");
                    socket.setTcpNoDelay(true);  // close the buffer of socket to ensure transfer speed
                    ControlConnection controlConnection = new ControlConnection(socket);
                    controlConnection.setListener(listener);
                    sessions.add(controlConnection.getSession());
                    executor.execute(controlConnection);
                }
            } catch (SocketTimeoutException ignored){
            } catch (IOException e) {
                logger.error(e.getMessage(), e);
            }
        }
    }
    // TODO: 4/14/2019 thread pool studying*/
}
