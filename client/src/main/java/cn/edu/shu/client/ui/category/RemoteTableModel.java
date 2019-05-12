/*
 * Written By : Skye You
 * Copyright (c) 2018 - 2019. All Rights Reserved.
 */

package cn.edu.shu.client.ui.category;

import cn.edu.shu.client.ftp.FTPFile;
import cn.edu.shu.client.util.TransferUtils;
import cn.edu.shu.common.util.Constants;
import cn.edu.shu.common.util.MessageUtils;
import cn.edu.shu.common.util.CommonUtils;

import javax.swing.*;
import javax.swing.table.AbstractTableModel;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

public class RemoteTableModel extends AbstractTableModel {

    private List<FTPFile> files;
    private String[] columns = {"", "Name", "Date Modified", "Type", "Size"};
    private RemoteCategoryPane categoryPane;
    private TransferUtils transferUtils;

    RemoteTableModel(RemoteCategoryPane categoryPane) {
        this.files = new ArrayList<>();
        this.categoryPane = categoryPane;
        transferUtils = TransferUtils.getInstance();
    }

    @Override
    public int getRowCount() {
        return files.size();
    }

    @Override
    public int getColumnCount() {
        return columns.length;
    }

    @Override
    public Object getValueAt(int rowIndex, int columnIndex) {
        FTPFile file = files.get(rowIndex);
        switch (columnIndex) {
            case 0:
                return file.getIcon();
            case 1:
                return file.getName();
            case 2:
                return file.getLastChanged();
            case 3:
                return file.getType();
            case 4:
                if (file.isDirectory())
                    return "";
                else
                    return transferUtils.getFormatSize(file.getSize());
        }
        return "";
    }

    @Override
    public void setValueAt(Object aValue, int rowIndex, int columnIndex) {
        FTPFile file = files.get(rowIndex);
        if (columnIndex == 1) {
            String newName = (String) aValue;
            if (newName.equals("")) {
                MessageUtils.showErrorMessage(Constants.EMPTY_FILENAME, Constants.RENAME_FILE_TITLE);
            } else if (!file.getName().equals(newName)) {
                String parentPath = categoryPane.getCurrentFile().getPath();
                String newPath = CommonUtils.getInstance().getPath(parentPath, newName);
                if (categoryPane.renameFile(file.getPath(), newPath)) {
                    file.setName(newName);
                    file.setPath(newPath);
                    file.getParent().setChildren(files);
                    fireTableCellUpdated(rowIndex, columnIndex);
                }
            }
        }
    }

    @Override
    public Class getColumnClass(int column) {
        switch (column) {
            case 0:
                return ImageIcon.class;
        }
        return String.class;
    }

    @Override
    public String getColumnName(int column) {
        return columns[column];
    }

    void setFiles(List<FTPFile> files) {
        this.files = files;
        OrderByName();
        fireTableDataChanged();
    }

    FTPFile getFile(int rowIndex) {
        return files.get(rowIndex);
    }

    private void OrderByName() {
        List<FTPFile> sortFiles = new ArrayList<>();
        for (FTPFile file : files) {
            if (file.isDirectory())
                sortFiles.add(file);
        }
        for (FTPFile file : files) {
            if (!file.isDirectory())
                sortFiles.add(file);
        }
        files = sortFiles;
    }

    @Override
    public boolean isCellEditable(int rowIndex, int columnIndex) {
        return columnIndex == 1;
    }

    public void addRow(FTPFile file) {
        int length = files.size();
        this.files.add(file);
        fireTableRowsInserted(length, length);
    }

    void removeRows(int[] rows) {
        if (rows.length == 0)
            return;
        Arrays.sort(rows);
        for (int row : rows)
            files.remove(row);
        fireTableRowsDeleted(rows[0], rows[rows.length - 1]);
    }

    void clearFiles(){
        files.clear();
    }
}
