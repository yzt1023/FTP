/*
 * Written By : Skye You
 * Copyright (c) 2018 - 2019. All Rights Reserved.
 */

package cn.edu.shu.client.ui.task;

import cn.edu.shu.common.util.Constants;

import javax.swing.*;
import java.awt.*;
import java.awt.event.MouseAdapter;
import java.awt.event.MouseEvent;

public class TaskPane extends JPanel {
    private TaskTableModel tableModel;

    public TaskPane() {
        super();
        initComponents();
    }

    private void initComponents() {
        // task table
        JTable taskTable = new JTable();
        taskTable.setSelectionMode(ListSelectionModel.SINGLE_SELECTION);
        JScrollPane scrollPane = new JScrollPane(taskTable);
        scrollPane.getViewport().setBackground(Color.WHITE);
        tableModel = new TaskTableModel();
        taskTable.setModel(tableModel);
        taskTable.getColumn("Progress").setCellRenderer(new TaskTableCellRender());
        taskTable.getColumn("Direction").setMaxWidth(70);

        // popup menu
        JPopupMenu popupMenu = new JPopupMenu();

        // pause
        JMenuItem pauseItem = new JMenuItem("Pause");
        pauseItem.addActionListener(e -> {

        });
        popupMenu.add(pauseItem);

        // continue
        JMenuItem continueItem = new JMenuItem("Continue");
        continueItem.addActionListener(e -> {

        });
        popupMenu.add(continueItem);

        // clear
        JMenuItem clearItem = new JMenuItem("Clear All");
        clearItem.addActionListener(e -> tableModel.clearTasks());
        popupMenu.add(clearItem);

        // action listener
        scrollPane.addMouseListener(new MouseAdapter() {
            @Override
            public void mouseClicked(MouseEvent e) {
                taskTable.clearSelection();
            }

            @Override
            public void mouseReleased(MouseEvent e) {
                if (e.isPopupTrigger()) {
                    int row = taskTable.rowAtPoint(e.getPoint());
                    Task task = tableModel.getTask(row);
                    if (Constants.STATE_PROCESSING.equals(task.getState()) || Constants.STATE_WAITING.equals(task.getState()))
                        pauseItem.setEnabled(true);
                    if (Constants.STATE_PAUSE.equals(task.getState()))
                        continueItem.setEnabled(true);
                    popupMenu.show(e.getComponent(), e.getX(), e.getY());
                }
            }
        });

        taskTable.addMouseListener(new MouseAdapter() {
            @Override
            public void mouseReleased(MouseEvent e) {
                if (e.isPopupTrigger()) {
                    pauseItem.setEnabled(false);
                    continueItem.setEnabled(false);
                    popupMenu.show(e.getComponent(), e.getX(), e.getY());
                }
            }
        });

        GroupLayout layout = new GroupLayout(this);
        this.setLayout(layout);

        layout.setVerticalGroup(layout.createParallelGroup().addComponent(scrollPane));
        layout.setHorizontalGroup(layout.createSequentialGroup().addContainerGap().addComponent(scrollPane).addContainerGap());
    }

    public TaskTableModel getTableModel() {
        return tableModel;
    }

    public void addTask(Task task) {
        tableModel.addRow(task);
    }

}
