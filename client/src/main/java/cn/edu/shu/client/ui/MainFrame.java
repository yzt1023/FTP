package cn.edu.shu.client.ui;

import cn.edu.shu.client.config.SystemConfig;
import cn.edu.shu.client.exception.ConnectionException;
import cn.edu.shu.client.exception.FTPException;
import cn.edu.shu.client.ftp.FTPClient;
import cn.edu.shu.client.ftp.FTPFile;
import cn.edu.shu.client.listener.ConnectListener;
import cn.edu.shu.client.listener.TransferListener;
import cn.edu.shu.client.thread.TransferThread;
import cn.edu.shu.client.ui.category.LocalCategoryPane;
import cn.edu.shu.client.ui.category.RemoteCategoryPane;
import cn.edu.shu.client.ui.task.Task;
import cn.edu.shu.client.ui.task.TaskPane;
import cn.edu.shu.client.util.TransferUtils;
import cn.edu.shu.common.log.MsgListener;
import cn.edu.shu.common.log.MsgPane;
import cn.edu.shu.common.util.CommonUtils;
import cn.edu.shu.common.util.Constants;
import cn.edu.shu.common.util.MessageUtils;
import org.apache.log4j.Logger;

import javax.swing.*;
import javax.swing.filechooser.FileSystemView;
import java.awt.*;
import java.io.File;
import java.io.IOException;
import java.util.Queue;
import java.util.LinkedList;

public class MainFrame extends JFrame implements TransferListener, ConnectListener, MsgListener {

    private static final int DEFAULT_WIDTH = 1500;
    private static final int DEFAULT_HEIGHT = 1000;
    private JTabbedPane tabbedPane;
    private JPanel contentPanel;
    private ConnectPane connectPane;
    private LocalCategoryPane localCategoryPane;
    private RemoteCategoryPane remoteCategoryPane;
    private MsgPane msgPane;
    private TaskPane taskPane;
    private MenuBar menuBar;
    private FTPClient ftpClient;
    private Queue<Task> taskQueue;
    private TransferThread transferThread;
    private CommonUtils utils;
    private TransferUtils transferUtils;
    private Logger logger = Logger.getLogger(getClass());
    private SystemConfig config;

    public MainFrame() {
        super("FTPClient");
        this.setDefaultCloseOperation(WindowConstants.EXIT_ON_CLOSE);
        this.setLocation(50, 50);
        this.setSize(new Dimension(DEFAULT_WIDTH, DEFAULT_HEIGHT));
        utils = CommonUtils.getInstance();
        transferUtils = TransferUtils.getInstance();
        config = SystemConfig.getInstance();
        config.initConfig();
        Image icon = Toolkit.getDefaultToolkit().getImage(utils.getResourcePath(getClass(), "logo.png"));
        this.setIconImage(icon);

        initComponents();
        setGroupLayout();
    }

    private void initComponents() {
        // menu bar
        menuBar = new MenuBar(this);
        this.setJMenuBar(menuBar);
        // connect panel
        connectPane = new ConnectPane(this);
        // tabbed panel: println / transfer
        msgPane = new MsgPane();
        taskPane = new TaskPane();
        tabbedPane = new JTabbedPane();
        tabbedPane.addTab("connect println", msgPane);
        tabbedPane.addTab("transfer process", taskPane);
        tabbedPane.setTabLayoutPolicy(JTabbedPane.SCROLL_TAB_LAYOUT);

        ftpClient = new FTPClient(this);
        taskQueue = new LinkedList<>();
        transferThread = new TransferThread(taskQueue, ftpClient, this);

        // category panel
        localCategoryPane = new LocalCategoryPane(this);
        remoteCategoryPane = new RemoteCategoryPane(ftpClient, this);

        contentPanel = new JPanel();
        this.setContentPane(contentPanel);

    }

    private void setGroupLayout() {
        GroupLayout layout = new GroupLayout(contentPanel);
        contentPanel.setLayout(layout);

        GroupLayout.ParallelGroup horizontalGroup = layout.createParallelGroup(GroupLayout.Alignment.LEADING);
        horizontalGroup.addComponent(connectPane);
        horizontalGroup.addComponent(tabbedPane);
        horizontalGroup.addGroup(layout.createSequentialGroup().addComponent(localCategoryPane).addComponent(remoteCategoryPane));
        layout.setHorizontalGroup(horizontalGroup);

        GroupLayout.SequentialGroup verticalGroup = layout.createSequentialGroup();
        verticalGroup.addComponent(connectPane);
        verticalGroup.addComponent(tabbedPane, GroupLayout.PREFERRED_SIZE, 300, GroupLayout.PREFERRED_SIZE);
        verticalGroup.addGroup(layout.createParallelGroup().addComponent(localCategoryPane).addComponent(remoteCategoryPane));
        layout.setVerticalGroup(verticalGroup);
    }

    @Override
    public void startDownload(FTPFile ftpFile, boolean tempDir) {
        String localDir = localCategoryPane.getCurrentFile().getPath();
        if (tempDir)
            localDir = Constants.TEMP_DIR;
        File file = new File(localDir, ftpFile.getName());
        if (file.exists()) {
            MessageUtils.showInfoMessage(Constants.FILE_EXISTS);
            return;
        }

        Task task = new Task(file, ftpFile, true);
        // scan file to obtain the file size
        long size;
        try {
            size = transferUtils.getTotalSize(ftpFile, ftpClient);
        } catch (Exception e) {
            logger.error(e.getMessage(), e);
            return;
        }
        task.setMaxValue(size);
        task.setDisplaySize(transferUtils.getFormatSize(size));
        taskPane.addTask(task);
        taskQueue.offer(task);
    }

    @Override
    public void startUpload(File file) {
        FTPFile parent = remoteCategoryPane.getCurrentFile();
        if (parent.getChild(file.getName()) != null) {
            MessageUtils.showInfoMessage(Constants.FILE_EXISTS);
            return;
        }
        FTPFile ftpFile = new FTPFile(parent, file.getName());
        String path = utils.getPath(parent.getPath(), file.getName());
        ftpFile.setPath(path);
        Task task = new Task(file, ftpFile, false);
        long size = transferUtils.getTotalSize(file);
        task.setMaxValue(size);
        task.setDisplaySize(transferUtils.getFormatSize(size));
        taskPane.addTask(task);
        taskQueue.offer(task);
    }

    @Override
    public void beforeTransfer(Task task) {
        taskPane.getTableModel().updateState(task);
    }

    @Override
    public void notifyProgress(Task task) {
        taskPane.getTableModel().updateProgress(task);
    }

    @Override
    public void afterTransfer(Task task) {
        taskPane.getTableModel().updateState(task);
        if (!Constants.STATE_SUCCESS.equals(task.getState()))
            return;

        if (task.isDownload()) {
            File file = task.getFile();
            if (file.isFile() && file.getPath().startsWith(Constants.TEMP_DIR))
                localCategoryPane.openFile(file);
            else if (file.getParent().equals(localCategoryPane.getCurrentFile().getPath()))
                localCategoryPane.getTableModel().addRow(task.getFile());
        } else {
            FTPFile file = task.getFtpFile();
            file.getParent().addChild(file);
            if (remoteCategoryPane.getCurrentFile() == file.getParent())
                remoteCategoryPane.getTableModel().addRow(task.getFtpFile());
        }
    }

    @Override
    public boolean startConnect(String host, int port) {
        return startConnect(host, port, Constants.ANONYMOUS_USER, "");
    }

    @Override
    public boolean startConnect(String host, int port, String username, String password) {
        try {
            ftpClient.connect(host, port);
            ftpClient.login(username, password);
        } catch (ConnectionException | FTPException e) {
            logger.error(e.getMessage(), e);
            return false;
        }
        return true;
    }

    @Override
    public void startDisconnect() {
        try {
            ftpClient.disconnect();
        } catch (ConnectionException | IOException e) {
            logger.error(e.getMessage(), e);
        }
    }

    @Override
    public void connectCompleted() {
        localCategoryPane.afterConnect(ftpClient.getUser().isReadable());
        remoteCategoryPane.afterConnect();
        menuBar.afterConnect();
        if(!transferThread.isAlive())
            transferThread.start();
    }

    @Override
    public void disconnectCompleted() {
        localCategoryPane.afterDisconnect();
        remoteCategoryPane.afterDisconnect();
        menuBar.afterDisconnect();
    }

    void firePasvModeChanged(boolean passive) {
        ftpClient.setPassiveMode(passive);
    }

    void fireSecureModeChanged(boolean secure) {
        ftpClient.setSecureMode(secure);
    }

    @Override
    public void println(String message) {
        msgPane.println(message);
        logger.info(message);
    }

    boolean exit(){
        if(!taskQueue.isEmpty()){
            int i = MessageUtils.showConfirmMessage("There are outstanding tasks, are you sure to stop them!", "close confirm");
            if(i != 0)
                return false;
        }

        transferThread.close();
        try {
            // wait sub thread to close
            Thread.sleep(1000);
        } catch (InterruptedException e) {
            logger.error(e.getMessage(), e);
        }

        this.startDisconnect();
        return true;
    }

    FTPClient getFtpClient() {
        return ftpClient;
    }

    SystemConfig getConfig() {
        return config;
    }
}