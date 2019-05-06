/*
 * Written By : Skye You
 * Copyright (c) 2018 - 2019. All Rights Reserved.
 */

package cn.edu.shu.client.ui.task;

import cn.edu.shu.common.util.Constants;
import cn.edu.shu.common.util.MessageUtils;

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
                return task.getDisplaySize();
            case 4:
                return task.getModifyTime();
            case 5:
                return task.getState();
            case 6:
                return task.getProgressArg();
        }
        return null;
    }

    @Override
    public Class getColumnClass(int column) {
        switch (column) {
            case 4:
                return Date.class;
            case 6:
                return ProgressArg.class;
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

    Task getTask(int row) {
        return tasks.get(row);
    }

    public void updateProgress(Task task) {
        int row = getTaskRow(task);
        if (row != -1)
            fireTableCellUpdated(row, 6);
    }

    public void updateState(Task task) {
        int row = getTaskRow(task);
        if (row != -1)
            fireTableCellUpdated(row, 5);
    }

    private int getTaskRow(Task task) {
        for (int i = 0; i < tasks.size(); i++) {
            if (tasks.get(i) == task)
                return i;
        }
        return -1;
    }

    void clearTasks() {
        for (Task task : tasks)
            if (!Constants.STATE_FAILURE.equals(task.getState()) && !Constants.STATE_SUCCESS.equals(task.getState())) {
                MessageUtils.showErrorMessage("Tasks cannot be emptied because there are some outstanding tasks", "clear task");
                return;
            }

        tasks.clear();
        fireTableRowsDeleted(0, 0);
    }
}
