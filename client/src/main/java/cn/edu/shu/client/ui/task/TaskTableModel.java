/*
 * Written By : Skye You
 * Copyright (c) 2018 - 2019. All Rights Reserved.
 */

package cn.edu.shu.client.ui.task;

import javax.swing.table.AbstractTableModel;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;

public class TaskTableModel extends AbstractTableModel {
    private List<Task> tasks;
    private String[] columns = {"Local File", "Direction", "Remote File", "Size", "Time", "State", "Progress"};

    TaskTableModel() {
        tasks = new ArrayList<>();
    }

    @Override
    public int getRowCount() {
        return tasks.size();
    }

    @Override
    public int getColumnCount() {
        return columns.length;
    }

    @Override
    public Object getValueAt(int rowIndex, int columnIndex) {
        Task task = tasks.get(rowIndex);
        switch (columnIndex) {
            case 0:
                return task.getFile().getPath();
            case 1:
                if (task.isDownload())
                    return "<<--";
                else
                    return "-->>";
            case 2:
                return task.getFtpFile().getPath();
            case 3:
                if (task.isDownload() && task.getFtpFile().isDirectory() || !task.isDownload() && task.getFile().isDirectory())
                    return task.getSize() + "files";
                return task.getSize() + "KB";
            case 4:
                return task.getModifyTime();
            case 5:
                return task.getState();
            case 6:
                return task.getProgress();
        }
        return null;
    }

    @Override
    public Class getColumnClass(int column) {
        switch (column) {
            case 4:
                return Date.class;
            case 6:
                return Integer.class;
        }
        return String.class;
    }

    @Override
    public String getColumnName(int column) {
        return columns[column];
    }

    void addRow(Task task) {
        tasks.add(task);
        int max = tasks.size() - 1;
        fireTableRowsInserted(max, max);
    }

    private int getTaskRow(Task task) {
        for (int i = 0; i < tasks.size(); i++) {
            if (tasks.get(i) == task)
                return i;
        }
        return -1;
    }

    public void updateProgress(Task task) {
        int row = getTaskRow(task);
        fireTableCellUpdated(row, 6);
    }

    public void updateState(Task task) {
        int row = getTaskRow(task);
        fireTableCellUpdated(row, 5);
    }

    void clearTasks() {
        tasks.clear();
    }
}
