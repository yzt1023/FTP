/*
 * Written By : Skye You
 * Copyright (c) 2018 - 2019. All Rights Reserved.
 */

package cn.edu.shu.client.ui.category;

import cn.edu.shu.client.listener.TransferListener;
import cn.edu.shu.client.util.TreeUtils;
import cn.edu.shu.common.util.Constants;
import cn.edu.shu.common.util.MessageUtils;
import org.apache.log4j.Logger;

import javax.swing.*;
import javax.swing.filechooser.FileSystemView;
import javax.swing.tree.TreePath;
import java.awt.*;
import java.io.File;
import java.io.IOException;
import java.util.Arrays;
import java.util.Date;

public class LocalCategoryPane extends CategoryPane {

    private FileSystemView fileSystemView;
    private File currentFile;
    private Desktop desktop;
    private LocalTableModel tableModel;
    private JMenuItem uploadItem;
    private TransferListener listener;
    private boolean canUpload;
    private Logger logger = Logger.getLogger(getClass());


    public LocalCategoryPane(FileSystemView fileSystemView, TransferListener listener) {
        super();
        this.fileSystemView = fileSystemView;
        this.listener = listener;
        canUpload = false;
        lblCategory.setText("Local Category: ");
        showItems = true;
    }

    @Override
    public void initComponents() {
        super.initComponents();
        desktop = Desktop.getDesktop();  // used to open local files
        tableModel = new LocalTableModel();
        ctgTable.setModel(tableModel);
        ctgTable.getColumnModel().getColumn(0).setMaxWidth(20);
        // set home directory as current path
        currentFile = fileSystemView.getHomeDirectory();
        root = new FileTreeNode(currentFile);
        refreshTable();
        initFileTree();
    }

    @Override
    void initPopupMenu() {
        // for local, files can be uploaded
        uploadItem = new JMenuItem("Upload", new ImageIcon(utils.getResourcePath(getClass(), "upload.png")));
        tableMenu.add(uploadItem);
        tableMenu.addSeparator();
        uploadItem.addActionListener(menuListener);
        uploadItem.setEnabled(false);
        super.initPopupMenu();
    }

    @Override
    void loadChildrenNode(FileTreeNode root) {
        File file = (File) root.getUserObject();
        File[] subFiles = fileSystemView.getFiles(file, false);
        for (File subFile : subFiles)
            if (subFile.isDirectory())
                root.add(new FileTreeNode(subFile));
    }

    @Override
    void reloadChildrenNode(FileTreeNode node) {
        root.removeAllChildren();
        loadChildrenNode(node);
    }

    @Override
    void showMenuItems(boolean rowSelected) {
        if(!canUpload)
            uploadItem.setEnabled(false);
        else
            uploadItem.setEnabled(rowSelected);
        super.showMenuItems(rowSelected);
    }

    @Override
    void changeDirectory() {
        String path = txtCategory.getText();
        if (!path.endsWith(Constants.SEPARATOR))
            path += Constants.SEPARATOR;
        if (currentFile.getPath().equals(path)) {
            txtCategory.setText(path);
            return;
        }
        File file = new File(path);
        if (!file.exists()) {
            MessageUtils.showInfoMessage(Constants.PATH_NOT_EXISTS);
            txtCategory.setText(currentFile.getPath());
        } else if (!file.isDirectory()) {
            openFile(file);
        } else {
            ctgTree.setSelectionRow(TreeUtils.getLastMatchedRow(ctgTree, file));
            currentFile = file;
            refreshTable();
        }
    }

    /**
     * for tree selection:
     * if selection is empty, traverse the tree
     * if the ancestor(parent included) is selected, do nothing
     * if the child is selected, select the parent
     */
    @Override
    void changeToParentDirectory() {
        if (currentFile != null && fileSystemView.getParentDirectory(currentFile) != null) {
            currentFile = fileSystemView.getParentDirectory(currentFile);
            refreshTable();
            int row = ctgTree.getMinSelectionRow();
            if (row == -1) {
                ctgTree.setSelectionRow(TreeUtils.getLastMatchedRow(ctgTree, currentFile));
            } else {
                TreePath treePath = ctgTree.getPathForRow(row).getParentPath();
                // select the root
                if (treePath == null)
                    return;
                File file = (File) ((FileTreeNode) treePath.getLastPathComponent()).getUserObject();
                if (file.getPath().equals(currentFile.getPath()))
                    ctgTree.setSelectionPath(treePath);
            }
        }
    }

    @Override
    void uploadFile() {
        int[] rows = ctgTable.getSelectedRows();
        for (int row : rows) {
            File file = tableModel.getFile(row);
            listener.startUpload(file);
        }
    }

    /**
     * for tree selection:
     * traverse the tree to find the last matched row while selection is empty,
     * while the parent path is selected and expanded, find the child node among the following nodes;
     */
    @Override
    void enterFolder(Object object) {
        currentFile = (File) object;
        int select = ctgTree.getMinSelectionRow();
        if (select == -1) {
            select = TreeUtils.getLastMatchedRow(ctgTree, currentFile);
        } else if (ctgTree.isExpanded(select)) {
            select = TreeUtils.getMatchedRow(ctgTree, currentFile, select);
        }
        ctgTree.setSelectionRow(select);
        refreshTable();
    }

    @Override
    void openFile() {
        int[] rows = ctgTable.getSelectedRows();
        for (int row : rows) {
            File file = tableModel.getFile(row);
            openFile(file);
        }
    }

    public void openFile(File file) {
        try {
            desktop.open(file);
        } catch (IOException e) {
            logger.error(e.getMessage(), e);
            MessageUtils.showErrorMessage(Constants.FILE_OPEN_FAILED, Constants.OPEN_FILE_TITLE);
        }
    }

    @Override
    void doubleClickPerformed(int row) {
        File file = tableModel.getFile(row);
        if (file.isDirectory()) {
            enterFolder(file);
        } else {
            openFile();
        }
    }

    @Override
    void refreshTable() {
        txtCategory.setText(currentFile.getAbsolutePath());
        tableModel.setFiles(fileSystemView.getFiles(currentFile, true));
    }

    void deleteFile() {
        int[] rows = ctgTable.getSelectedRows();
        String message = "Are you sure to delete ";
        message = (rows.length > 1) ? message + "these files?" : message + "this file";
        int i = MessageUtils.showConfirmMessage(message, Constants.DELETE_FILE_TITLE);
        if (i == 0) {
            int j = 0;
            for (; j < rows.length; j++) {
                File file = tableModel.getFile(rows[j]);
                if (!deleteFolder(file)) {
                    MessageUtils.showErrorMessage(Constants.FILE_DELETE_FAILED, Constants.DELETE_FILE_TITLE);
                    break;
                }
            }
            tableModel.removeRows(Arrays.copyOfRange(rows, 0, j));
        }
    }

    private boolean deleteFolder(File dir) {
        if (dir.isDirectory()) {
            File[] files = fileSystemView.getFiles(dir, true);
            if (files == null)
                return dir.delete();
            for (File file : files) {
                boolean success = deleteFolder(file);
                if (!success)
                    return false;
            }
        }
        return dir.delete();
    }

    void newFolder() {
        String newName = Constants.INIT_NAME + utils.formatDate(new Date());
        String path = currentFile.getPath() + Constants.SEPARATOR + newName;
        File file = new File(path);
        if (!file.exists() && file.mkdir()) {
            tableModel.addRow(file);
            int row = tableModel.getRowCount() - 1;
            ctgTable.setRowSelectionInterval(row, row);
            ctgTable.editCellAt(row, 1);
        } else {
            MessageUtils.showErrorMessage(Constants.FOLDER_CREATE_FAILED, Constants.NEW_FOLDER_TITLE);
        }
    }

    @Override
    void setCurrentTable(Object object) {
        this.currentFile = (File) object;
        refreshTable();
    }

    public LocalTableModel getTableModel() {
        return tableModel;
    }

    public File getCurrentFile() {
        return currentFile;
    }

    public void afterConnect(boolean isWritable){
        if(isWritable)
            canUpload = true;
    }

    public void afterDisconnect(){
        canUpload = false;
    }
}
