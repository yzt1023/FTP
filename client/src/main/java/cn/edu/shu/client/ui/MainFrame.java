package cn.edu.shu.client.ui;

import cn.edu.shu.client.ftp.FTPClient;
import cn.edu.shu.client.ftp.FTPFile;
import cn.edu.shu.client.listener.ConnectListener;
import cn.edu.shu.client.listener.ModeListener;
import cn.edu.shu.client.listener.TransferListener;
import cn.edu.shu.client.thread.DownloadThread;
import cn.edu.shu.client.thread.UploadThread;
import cn.edu.shu.client.ui.category.LocalCategoryPane;
import cn.edu.shu.client.ui.category.RemoteCategoryPane;
import cn.edu.shu.client.ui.task.Task;
import cn.edu.shu.client.ui.task.TaskPane;
import cn.edu.shu.client.util.Constants;
import cn.edu.shu.common.log.LogPane;
import cn.edu.shu.common.log.Logger;
import cn.edu.shu.common.util.Utils;

import javax.swing.*;
import java.awt.*;
import java.io.File;
import java.util.LinkedList;
import java.util.Queue;

public class MainFrame extends JFrame implements TransferListener, ConnectListener, ModeListener, Logger {

    private JTabbedPane tabbedPane;
    private JPanel contentPanel;
    private ConnectPane connectPane;
    private LocalCategoryPane localCategoryPane;
    private RemoteCategoryPane remoteCategoryPane;
    private LogPane logPane;
    private TaskPane taskPane;
    private FTPMenuBar menuBar;
    private FTPClient ftpClient;
    private Queue<Task> downloadQueue;
    private Queue<Task> uploadQueue;
    private static final int DEFAULT_WIDTH = 1500;
    private static final int DEFAULT_HEIGHT = 1000;

    public MainFrame() {
        super("FTPClient");
        this.setDefaultCloseOperation(WindowConstants.EXIT_ON_CLOSE);
        this.setLocation(50, 50);
        this.setSize(new Dimension(DEFAULT_WIDTH, DEFAULT_HEIGHT));
        Image icon = Toolkit.getDefaultToolkit().getImage(Utils.getResourcePath(getClass(), "logo.png"));
        this.setIconImage(icon);

        downloadQueue = new LinkedList<>();
        uploadQueue = new LinkedList<>();

        initComponents();
        setGroupLayout();
    }

    private void initComponents() {
        // menu bar
        menuBar = new FTPMenuBar(this);
        this.setJMenuBar(menuBar);
        // connect panel
        connectPane = new ConnectPane(this);
        // tabbed panel: log / transfer
        logPane = new LogPane();
        taskPane = new TaskPane();
        tabbedPane = new JTabbedPane();
        tabbedPane.addTab("connect log", logPane);
        tabbedPane.addTab("transfer process", taskPane);
        tabbedPane.setTabLayoutPolicy(JTabbedPane.SCROLL_TAB_LAYOUT);

        ftpClient = new FTPClient(this);
        ftpClient.setEncoding("GBK");

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
    public void fireDownload(FTPFile ftpFile, boolean tempDir) {
        String localDir = localCategoryPane.getCurrentFile().getPath();
        if (tempDir)
            localDir = System.getProperty(Constants.TEMP_DIR);
        File file = new File(localDir, ftpFile.getName());
        Task task = new Task(file, ftpFile, true);
        taskPane.addTask(task);
        downloadQueue.offer(task);
    }

    @Override
    public void fireUpload(File file) {
        FTPFile parent = remoteCategoryPane.getCurrentFile();
        String path;
        if (parent.getPath().endsWith(Constants.SEPARATOR))
            path = parent.getPath() + file.getName();
        else
            path = parent.getPath() + Constants.SEPARATOR + file.getName();
        FTPFile ftpFile = new FTPFile(parent);
        ftpFile.setPath(path);
        Task task = new Task(file, ftpFile, false);
        taskPane.addTask(task);
        uploadQueue.offer(task);
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
        if (task.isDownload()) {
            localCategoryPane.getTableModel().addRow(task.getFile());
        } else {
            remoteCategoryPane.getTableModel().addRow(task.getFtpFile());
        }
    }

    @Override
    public boolean fireConnect(String host, int port) {
        return ftpClient.connect(host, port);
    }

    @Override
    public boolean fireConnect(String host, int port, String username, String password) {
        return ftpClient.connect(host, port, username, password);
    }

    @Override
    public void fireDisconnect() {
        ftpClient.disconnect();
    }

    @Override
    public void afterConnect() {
        remoteCategoryPane.afterConnect();
        DownloadThread downloadThread = new DownloadThread(downloadQueue, ftpClient, this);
        downloadThread.start();
        UploadThread uploadThread = new UploadThread(this.uploadQueue, this.ftpClient, this);
        uploadThread.start();
    }

    @Override
    public void afterDisconnect() {
        remoteCategoryPane.afterDisconnect();
    }

    @Override
    public void firePasvModeChanged(boolean passive) {
        ftpClient.setPassiveMode(passive);
    }

    @Override
    public void fireSecureModeChanged(boolean secure) {
        ftpClient.setSecureMode(secure);
    }

    @Override
    public void log(String message) {
        logPane.log(message);
    }
}
// TODO: 3/2/2019 change layout by mouse drag