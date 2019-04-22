package cn.edu.shu.client.ui;

import cn.edu.shu.client.ftp.FTPClient;
import cn.edu.shu.client.ftp.FTPFile;
import cn.edu.shu.client.listener.ConnectListener;
import cn.edu.shu.client.listener.TransferListener;
import cn.edu.shu.client.thread.DownloadThread;
import cn.edu.shu.client.thread.UploadThread;
import cn.edu.shu.client.ui.category.LocalCategoryPane;
import cn.edu.shu.client.ui.category.RemoteCategoryPane;
import cn.edu.shu.client.ui.task.Task;
import cn.edu.shu.client.ui.task.TaskPane;
import cn.edu.shu.client.util.TransferUtils;
import cn.edu.shu.common.log.MsgListener;
import cn.edu.shu.common.log.MsgPane;
import cn.edu.shu.common.util.Constants;
import cn.edu.shu.common.util.MessageUtils;
import cn.edu.shu.common.util.Utils;

import javax.swing.*;
import java.awt.*;
import java.io.File;
import java.util.Deque;
import java.util.LinkedList;
import java.util.Queue;

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
    private Deque<Task> downloadQueue;
    private Queue<Task> uploadQueue;
    private Utils utils;
    private TransferUtils transferUtils;

    public MainFrame() {
        super("FTPClient");
        this.setDefaultCloseOperation(WindowConstants.EXIT_ON_CLOSE);
        this.setLocation(50, 50);
        this.setSize(new Dimension(DEFAULT_WIDTH, DEFAULT_HEIGHT));
        utils = Utils.getInstance();
        transferUtils = TransferUtils.getInstance();
        Image icon = Toolkit.getDefaultToolkit().getImage(utils.getResourcePath(getClass(), "logo.png"));
        this.setIconImage(icon);

        downloadQueue = new LinkedList<>();
        uploadQueue = new LinkedList<>();

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
//        ftpClient.setEncoding("GBK");

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
            localDir = Constants.TEMP_DIR;
        File file = new File(localDir, ftpFile.getName());
        if (file.exists()) {
            MessageUtils.showInfoMessage(Constants.FILE_EXISTS);
            return;
        }

        Task task = new Task(file, ftpFile, true);
        // scan file to obtain the file size
        long size = transferUtils.getTotalSize(ftpFile, ftpClient);
        task.setMaxValue(size);
        task.setDisplaySize(transferUtils.getFormatSize(size));
        taskPane.addTask(task);
        if (tempDir)
            downloadQueue.offerFirst(task);
        else
            downloadQueue.offerLast(task);
    }

    @Override
    public void fireUpload(File file) {
        FTPFile parent = remoteCategoryPane.getCurrentFile();
        FTPFile ftpFile = new FTPFile(parent);
        String path = utils.getPath(parent.getPath(), file.getName());
        ftpFile.setPath(path);
        Task task = new Task(file, ftpFile, false);
        long size = transferUtils.getTotalSize(file);
        task.setMaxValue(size);
        task.setDisplaySize(transferUtils.getFormatSize(size));
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
        if (!Constants.STATE_SUCCESS.equals(task.getState()))
            return;

        if (task.isDownload()) {
            File file = task.getFile();
            if (file.isFile() && file.getPath().startsWith(Constants.TEMP_DIR))
                localCategoryPane.openFile(file);
            else
                localCategoryPane.getTableModel().addRow(task.getFile());
        } else {
            remoteCategoryPane.getTableModel().addRow(task.getFtpFile());
        }
    }

    boolean userRegister(String host, int port, String username, String password) {
        ftpClient.connect(host, port);
        return ftpClient.register(username, password);
    }

    @Override
    public boolean fireConnect(String host, int port) {
        return fireConnect(host, port, Constants.ANONYMOUS_USER, "");
    }

    @Override
    public boolean fireConnect(String host, int port, String username, String password) {
        ftpClient.connect(host, port);
        return ftpClient.login(username, password);
    }

    @Override
    public void fireDisconnect() {
        ftpClient.disconnect();
    }

    @Override
    public void afterConnect() {
        remoteCategoryPane.afterConnect();
        menuBar.afterConnect();
        DownloadThread downloadThread = new DownloadThread(downloadQueue, ftpClient, this);
        downloadThread.start();
        UploadThread uploadThread = new UploadThread(this.uploadQueue, this.ftpClient, this);
        uploadThread.start();
    }

    @Override
    public void afterDisconnect() {
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
    }
}
// TODO: 3/2/2019 change layout by mouse drag