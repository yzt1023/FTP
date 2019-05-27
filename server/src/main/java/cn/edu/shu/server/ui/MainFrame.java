package cn.edu.shu.server.ui;

import cn.edu.shu.common.log.MsgListener;
import cn.edu.shu.common.log.MsgPane;
import cn.edu.shu.common.util.CommonUtils;
import cn.edu.shu.server.db.DBConnPool;
import cn.edu.shu.server.ftp.FTPServer;

import javax.swing.*;
import java.awt.*;
import java.awt.event.WindowAdapter;
import java.awt.event.WindowEvent;
import java.text.SimpleDateFormat;
import java.util.Calendar;

public class MainFrame extends JFrame implements MsgListener {
    private static final int DEFAULT_WIDTH = 1000;
    private static final int DEFAULT_HEIGHT = 700;
    private MsgPane msgPane;
    private FTPServer ftpServer;

    public MainFrame() {
        super("FTPServer");
        this.setDefaultCloseOperation(WindowConstants.EXIT_ON_CLOSE);
        this.setLocation(600, 200);
        this.setSize(new Dimension(DEFAULT_WIDTH, DEFAULT_HEIGHT));
        Image icon = Toolkit.getDefaultToolkit().getImage(CommonUtils.getInstance().getResourcePath(getClass(), "logo.png"));
        this.setIconImage(icon);

        this.addWindowListener(new WindowAdapter() {
            @Override
            public void windowClosing(WindowEvent e) {
                exit();
            }
        });
        initComponents();
    }

    void exit() {
        DBConnPool.getInstance().clearConns();
        if (!ftpServer.isStop())
            ftpServer.stopServer();
        System.exit(0);
    }

    private void initComponents() {
        msgPane = new MsgPane();
        this.setContentPane(msgPane);
        MenuBar menuBar = new MenuBar(this);
        this.setJMenuBar(menuBar);

        ftpServer = new FTPServer();
        ftpServer.setListener(this);
        ftpServer.start();
    }

    public void println(String message) {
        Calendar calendar = Calendar.getInstance();
        SimpleDateFormat dateFormat = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss");
        String time = dateFormat.format(calendar.getTime());
        msgPane.println(time + " > " + message);
    }
}
