/*
 * Written By : Skye You
 * Copyright (c) 2018 - 2019. All Rights Reserved.
 */

package cn.edu.shu.client.ui.category;

import cn.edu.shu.client.util.Constants;
import cn.edu.shu.client.util.Helper;
import cn.edu.shu.common.util.MessageUtils;

import javax.swing.*;
import javax.swing.filechooser.FileSystemView;
import javax.swing.table.AbstractTableModel;
import java.io.File;
import java.text.DateFormat;
import java.util.Arrays;
import java.util.Date;

public class LocalTableModel extends AbstractTableModel {

    private File[] files;
    private String[] columns = {"", "Name", "Date Modified", "Type", "Size"};
    private FileSystemView fileSystemView;

    LocalTableModel(){
        fileSystemView = FileSystemView.getFileSystemView();
        this.files = new File[0];
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
        File file = files[rowIndex];
        switch(columnIndex){
            case 0 :
                return fileSystemView.getSystemIcon(file);
            case 1:
                return fileSystemView.getSystemDisplayName(file);
            case 2:
                Date date = new Date(file.lastModified());
                return DateFormat.getDateTimeInstance().format(date);
            case 3:
                return fileSystemView.getSystemTypeDescription(file);
            case 4:
                if(file.isDirectory())
                    return "";
                else
                    return file.length()/Constants.KB + 1 + " KB";
        }
        return "";
    }

    @Override
    public void setValueAt(Object aValue, int rowIndex, int columnIndex) {
        File file = files[rowIndex];
        if(columnIndex == 1) {
            String newName = (String) aValue;
            if (newName.equals("")) {
                MessageUtils.showErrorMessage( Constants.EMPTY_FILENAME, Constants.RENAME_FILE_TITLE);
            } else if (!file.getName().equals(newName)) {
                File newFile = new File(file.getParent() + Constants.SEPARATOR + newName);
                if (file.renameTo(newFile)) {
                    files[rowIndex] = newFile;
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
                return Icon.class;
        }
        return String.class;
    }

    @Override
    public String getColumnName(int column){
        return columns[column];
     }

    void setFiles(File[] files){
        this.files = files;
        OrderByName();
        fireTableDataChanged();
    }

    File getFile(int rowIndex){
        return files[rowIndex];
    }

    private void OrderByName() {
        File[] sortFiles = new File[files.length];
        int i = 0, j = files.length - 1;
        for (File file : files) {
            if (file.isDirectory())
                sortFiles[i++] = file;
            else
                sortFiles[j--] = file;
        }
        j = files.length - 1;
        Helper.reverse(sortFiles, i, j);
        files = sortFiles;
    }

    @Override
    public boolean isCellEditable(int rowIndex, int columnIndex) {
        return columnIndex == 1;
    }

    public void addRow(File file){
        File[] newFiles = Arrays.copyOf(files, files.length + 1);
        newFiles[files.length] = file;
        this.files = newFiles;
        fireTableRowsInserted(files.length - 1, files.length - 1);
    }

    public File[] getFiles(){
        return files;
    }

    void removeRows(int[] rows) {
        Arrays.sort(rows);
        File[] newFiles = new File[files.length - rows.length];
        int j = 0, k = 0;
        for(int i = 0; i < files.length; i++){
            if(j >= rows.length || i != rows[j]){
                newFiles[k++] = files[i];
            }else{
                j++;
            }
        }
        files = newFiles;
        fireTableRowsDeleted(rows[0], rows[rows.length - 1]);
    }
}
