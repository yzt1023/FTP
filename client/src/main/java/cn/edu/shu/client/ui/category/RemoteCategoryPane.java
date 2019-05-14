/*
 * Written By : Skye You
 * Copyright (c) 2018 - 2019. All Rights Reserved.
 */

package cn.edu.shu.client.ui.category;

import cn.edu.shu.client.exception.ConnectionException;
import cn.edu.shu.client.exception.FTPException;
import cn.edu.shu.client.exception.NoPermissionException;
import cn.edu.shu.client.ftp.FTPClient;
import cn.edu.shu.client.ftp.FTPFile;
import cn.edu.shu.client.listener.TransferListener;
import cn.edu.shu.client.util.TreeUtils;
import cn.edu.shu.common.bean.User;
import cn.edu.shu.common.util.Constants;
import cn.edu.shu.common.util.MessageUtils;

import javax.swing.*;
import javax.swing.text.Position;
import javax.swing.tree.DefaultTreeModel;
import javax.swing.tree.TreePath;
import java.io.IOException;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Date;
import java.util.List;

public class RemoteCategoryPane extends CategoryPane {

    private JMenuItem downloadItem;
    private FTPFile currentFile;
    private FTPClient ftpClient;
    private RemoteTableModel tableModel;
    private TransferListener listener;
    private FTPFile home;
    private User user;

    public RemoteCategoryPane(FTPClient client, TransferListener listener) {
        super();
        this.ftpClient = client;
        this.listener = listener;
        lblCategory.setText("Remote Category: ");
        showItems = false;
    }

    @Override
    public void initComponents() {
        super.initComponents();
        tableModel = new RemoteTableModel(this);
        ctgTable.setModel(tableModel);
        ctgTable.getColumnModel().getColumn(0).setMaxWidth(20);
        setEnabled(false); // the remote panel should be gray before connecting
    }

    @Override
    void initPopupMenu() {
        // for remote, files can be downloaded
        downloadItem = new JMenuItem("Download", new ImageIcon(utils.getResourcePath(getClass(), "download.png")));
        tableMenu.add(downloadItem);
        tableMenu.addSeparator();

        downloadItem.addActionListener(menuListener);
        super.initPopupMenu();
    }

    @Override
    void loadChildrenNode(FileTreeNode node) {
        FTPFile file = (FTPFile) node.getUserObject();
        List<FTPFile> subFiles = getFileChildren(file);
        for (FTPFile subFile : subFiles)
            if (subFile.isDirectory())
                node.add(new FileTreeNode(subFile));
    }

    @Override
    void reloadChildrenNode(FileTreeNode node) {
        node.removeAllChildren();
        FTPFile file = (FTPFile) node.getUserObject();
        List<FTPFile> subFiles = refreshChildren(file);
        for (FTPFile subFile : subFiles)
            if (subFile.isDirectory())
                node.add(new FileTreeNode(subFile));
    }

    @Override
    void showMenuItems(boolean rowSelected) {
        if (!user.isReadable()) {
            downloadItem.setEnabled(false);
            openItem.setEnabled(false);
            refreshItem.setEnabled(false);
        }else{
            downloadItem.setEnabled(rowSelected);
            openItem.setEnabled(rowSelected);
            refreshItem.setEnabled(true);
        }
        if (!user.isWritable()) {
            renameItem.setEnabled(false);
            newItem.setEnabled(false);
        }else {
            renameItem.setEnabled(rowSelected);
            newItem.setEnabled(true);
        }
        if (!user.canDeleted()) {
            deleteItem.setEnabled(false);
        }else
            deleteItem.setEnabled(rowSelected);
    }

    @Override
    void changeDirectory() {
        String path = txtCategory.getText();
        // remove the end separator except root path
        if (path.length() != 1 && path.endsWith(Constants.SEPARATOR)) {
            path = path.substring(0, path.length() - 1);
        }
        // can only input absolute path
        if (!path.startsWith(Constants.SEPARATOR)) {
            path = "/" + path;
        }
        // current path, do nothing
        if (currentFile.getPath().equals(path)) {
            txtCategory.setText(path);
            return;
        }
        try {
            if (ftpClient.changeDirectory(path)) {
                txtCategory.setText(ftpClient.printWorkingDir());
                // find the file with the specified filename
                String[] names = ftpClient.getCurrentPath().split(Constants.SEPARATOR);
                FTPFile file = home;
                if (names.length > 1) {
                    for (int i = 1; i < names.length; i++) {
                        getFileChildren(file);
                        file = file.getChild(names[i]);
                    }
                }
                currentFile = file;
                // load the table data and select corresponding row of tree
                loadTableData();
                int row = TreeUtils.getLastMatchedRow(ctgTree, currentFile.getPath());
                ctgTree.setSelectionRow(row);
            } else {
                // remote change directory failed
                MessageUtils.showInfoMessage(Constants.PATH_NOT_EXISTS);
                txtCategory.setText(currentFile.getPath());
            }
        } catch (ConnectionException e) {
            connectionExceptionHandle(e);
        }
    }

    @Override
    void changeToParentDirectory() {
        if (currentFile == home)
            return;
        currentFile = currentFile.getParent();
        loadTableData();
        int row = ctgTree.getMinSelectionRow();
        if (row == -1) {
            row = TreeUtils.getLastMatchedRow(ctgTree, currentFile.getPath());
            ctgTree.setSelectionRow(row);
        } else {
            TreePath treePath = ctgTree.getPathForRow(row).getParentPath();
            if (treePath == null)
                return;
            FTPFile file = (FTPFile) ((FileTreeNode) treePath.getLastPathComponent()).getUserObject();
            if (file.getPath().equals(currentFile.getPath()))
                ctgTree.setSelectionPath(treePath);
        }
    }

    @Override
    void downloadFile() {
        int[] rows = ctgTable.getSelectedRows();
        for (int row : rows) {
            FTPFile file = tableModel.getFile(row);
            listener.startDownload(file, false);
        }
    }

    @Override
    void enterFolder(Object object) {
        currentFile = (FTPFile) object;
        int select = ctgTree.getMinSelectionRow();
        if (select == -1) {
            select = TreeUtils.getLastMatchedRow(ctgTree, currentFile.getPath());
        } else if (ctgTree.isExpanded(select)) {
            select = TreeUtils.getNextMatch(ctgTree, currentFile.getPath(), select + 1, Position.Bias.Forward);
        }
        ctgTree.setSelectionRow(select);
        loadTableData();
    }

    @Override
    void openFile() {
        int row = ctgTable.getSelectedRow();
        ctgTable.setRowSelectionInterval(row, row);
        FTPFile file = tableModel.getFile(row);
        if (file.isDirectory())
            enterFolder(file);
        else {
            listener.startDownload(file, true);
        }
    }

    @Override
    void doubleClickPerformed(int row) {
        FTPFile file = tableModel.getFile(row);
        if (file.isDirectory()) {
            enterFolder(file);
        } else
            openFile();
    }

    boolean renameFile(String src, String des) {
        try {
            return ftpClient.rename(src, des);
        } catch (ConnectionException e) {
            connectionExceptionHandle(e);
            return false;
        } catch (NoPermissionException e) {
            noPermissionExceptionHandle(e);
            return false;
        }
    }

    @Override
    void refreshTable() {
        txtCategory.setText(currentFile.getPath());
        tableModel.setFiles(refreshChildren(currentFile));
    }

    @Override
    void deleteFile() {
        int[] rows = ctgTable.getSelectedRows();
        String message = "Are you sure to delete ";
        message = (rows.length > 1) ? message + "these files?" : message + "this file";
        int i = MessageUtils.showConfirmMessage(message, Constants.DELETE_FILE_TITLE);
        if (i == 0) {
            int j = 0;
            for (; j < rows.length; j++) {
                FTPFile file = tableModel.getFile(rows[j]);
                try {
                    if (!deleteFolder(file)) {
                        MessageUtils.showErrorMessage(Constants.FILE_DELETE_FAILED, Constants.DELETE_FILE_TITLE);
                        break;
                    } else {
                        currentFile.removeChild(file);
                    }
                } catch (ConnectionException e) {
                    connectionExceptionHandle(e);
                } catch (NoPermissionException e) {
                    noPermissionExceptionHandle(e);
                }
            }
            tableModel.removeRows(Arrays.copyOfRange(rows, 0, j));
        }
    }

    private boolean deleteFolder(FTPFile dir) throws ConnectionException, NoPermissionException {
        if (dir.isDirectory()) {
            List<FTPFile> files = refreshChildren(dir);
            for (FTPFile file : files) {
                boolean success = deleteFolder(file);
                if (!success)
                    return false;
            }
            return ftpClient.removeDirectory(dir.getPath());
        }
        return ftpClient.delete(dir.getPath());
    }

    @Override
    void newFolder() {
        String newName = Constants.INIT_NAME + utils.formatDate(new Date());
        String path = utils.getPath(currentFile.getPath(), newName);

        try {
            ftpClient.makeDirectory(path);
        } catch (NoPermissionException e) {
            MessageUtils.showErrorMessage(e.getMessage(), Constants.NEW_FOLDER_TITLE);
            return;
        } catch (ConnectionException e) {
            connectionExceptionHandle(e);
            return;
        }

        FTPFile file = new FTPFile(currentFile, newName);
        file.setPath(path);
        file.setType(Constants.FILE_FOLDER);
        file.setLastChanged(new Date());
        tableModel.addRow(file);
        currentFile.addChild(file);
        int row = tableModel.getRowCount() - 1;
        ctgTable.setRowSelectionInterval(row, row);
        ctgTable.editCellAt(row, 1);
    }

    @Override
    void setCurrentTable(Object object) {
        currentFile = (FTPFile) object;
        loadTableData();
    }

    public RemoteTableModel getTableModel() {
        return tableModel;
    }

    private void loadTableData() {
        txtCategory.setText(currentFile.getPath());
        tableModel.setFiles(getFileChildren(currentFile));
    }

    public void afterConnect() {
        setEnabled(true);
        // get remote home dir
        user = ftpClient.getUser();
        if (!user.isReadable())
            return;

        try {
            home = new FTPFile(ftpClient.printWorkingDir());
        } catch (ConnectionException e) {
            connectionExceptionHandle(e);
            return;
        }

        home.setName(home.getPath());
        home.setType(Constants.FILE_FOLDER);
        home.setParent(null);
        currentFile = home;
        root = new FileTreeNode(currentFile);
        initFileTree();
        loadTableData();
    }

    public void afterDisconnect() {
        clearData();
        showItems = false;
        setEnabled(false);
    }

    private void clearData() {
        tableModel.clearFiles();
        DefaultTreeModel treeModel = (DefaultTreeModel) ctgTree.getModel();
        treeModel.setRoot(null);
        txtCategory.setText("");
    }

    public FTPFile getCurrentFile() {
        return currentFile;
    }

    private List<FTPFile> getFileChildren(FTPFile file) {
        if (file.getChildren() == null) {
            return refreshChildren(file);
        } else
            return file.getChildren();
    }

    private void connectionExceptionHandle(ConnectionException e) {
        logger.error(e.getMessage(), e);
        try {
            ftpClient.reconnect();
        } catch (Exception ex) {
            logger.error(e.getMessage(), e);
            MessageUtils.showInfoMessage(e.getMessage());
            return;
        }
        MessageUtils.showInfoMessage(Constants.RECONNECT_SUCCEED);
    }

    private void noPermissionExceptionHandle(NoPermissionException e) {
        logger.error(e.getMessage(), e);
        MessageUtils.showInfoMessage(Constants.OPERATION_FAILED + e.getMessage());
    }

    private List<FTPFile> refreshChildren(FTPFile ftpFile) {
        try {
            return ftpClient.getFiles(ftpFile);
        } catch (ConnectionException e) {
            connectionExceptionHandle(e);
        } catch (IOException | FTPException e) {
            logger.error(e.getMessage(), e);
        } catch (NoPermissionException e) {
            noPermissionExceptionHandle(e);
        }
        return new ArrayList<>();
    }

}
