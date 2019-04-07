/*
 * Written By : Skye You
 * Copyright (c) 2018 - 2019. All Rights Reserved.
 */

package cn.edu.shu.client.ui.category;

import cn.edu.shu.client.ftp.FTPFile;
import cn.edu.shu.common.util.MessageUtils;
import cn.edu.shu.common.util.Constants;
import cn.edu.shu.common.util.Utils;

import javax.swing.*;
import javax.swing.table.AbstractTableModel;
import java.util.Arrays;

public class RemoteTableModel extends AbstractTableModel {

    private FTPFile[] files;
    private String[] columns = {"", "Name", "Date Modified", "Type", "Size"};
    private RemoteCategoryPane categoryPane;

    RemoteTableModel(RemoteCategoryPane categoryPane){
        this.files = new FTPFile[0];
        this.categoryPane = categoryPane;
    }

    @Override
    public int getRowCount() {
        return files.length;
    }

    @Override
    public int getColumnCount() {
        return columns.length;
    }

    @Override
    public Object getValueAt(int rowIndex, int columnIndex) {
        FTPFile file = files[rowIndex];
        switch(columnIndex){
            case 0 :
                return file.getIcon();
            case 1:
                return file.getName();
            case 2:
                return file.getLastChanged();
            case 3:
                return file.getType();
            case 4:
                if(file.isDirectory())
                    return "";
                else
                    return file.getSize()/1024 + 1 + " KB";
        }
        return "";
    }

    @Override
    public void setValueAt(Object aValue, int rowIndex, int columnIndex) {
        FTPFile file = files[rowIndex];
        if(columnIndex == 1) {
            String newName = (String) aValue;
            if (newName.equals("")) {
                MessageUtils.showErrorMessage( Constants.EMPTY_FILENAME, Constants.RENAME_FILE_TITLE);
            } else if (!file.getName().equals(newName)) {
                String parentPath = categoryPane.getCurrentFile().getPath();
                String newPath = Utils.getInstance().getPath(parentPath, newName);
                if(categoryPane.renameFile(file.getPath(), newPath)){
                    file.setName(newName);
                    file.setPath(newPath);
                    file.getParent().setChildren(files);
                    fireTableCellUpdated(rowIndex, columnIndex);
                }else{
                    MessageUtils.showErrorMessage(Constants.FILE_RENAME_FAILED, Constants.RENAME_FILE_TITLE);
                }
            }
        }
    }

    @Override
    public Class getColumnClass(int column){
        switch (column){
            case 0:
                return ImageIcon.class;
        }
        return String.class;
    }

    @Override
    public String getColumnName(int column){
        return columns[column];
    }

    void setFiles(FTPFile[] files){
        this.files = files;
        OrderByName();
        fireTableDataChanged();
    }

    public FTPFile[] getFiles(){
        return files;
    }

    FTPFile getFile(int rowIndex){
        return files[rowIndex];
    }

    private void OrderByName() {
        FTPFile[] sortFiles = new FTPFile[files.length];
        int i = 0, j = files.length - 1;
        for (FTPFile file : files) {
            if (file.isDirectory())
                sortFiles[i++] = file;
            else
                sortFiles[j--] = file;
        }
        j = files.length - 1;
        Utils.getInstance().reverse(sortFiles, i, j);
        files = sortFiles;
    }

    @Override
    public boolean isCellEditable(int rowIndex, int columnIndex) {
        return columnIndex == 1;
    }

    public void addRow(FTPFile file){
        FTPFile[] newFiles = Arrays.copyOf(files, files.length + 1);
        newFiles[files.length] = file;
        this.files = newFiles;
        file.getParent().setChildren(files);
        fireTableRowsInserted(files.length - 1, files.length - 1);
    }

    void removeRows(int[] rows){
        if(rows.length == 0)
            return;
        Arrays.sort(rows);
        FTPFile[] newFiles = new FTPFile[files.length - rows.length];
        int j = 0, k = 0;
        for(int i = 0; i < files.length; i++){
            if(j >= rows.length || i != rows[j]){
                newFiles[k++] = files[i];
            }else{
                j++;
            }
        }
        files = newFiles;
        categoryPane.getCurrentFile().setChildren(files);
        fireTableRowsDeleted(rows[0], rows[rows.length - 1]);
    }
}
