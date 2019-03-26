/*
 * Written By : Skye You
 * Copyright (c) 2018 - 2019. All Rights Reserved.
 */

package cn.edu.shu.client.ui.task;

import javax.swing.*;
import java.awt.*;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.awt.event.MouseAdapter;
import java.awt.event.MouseEvent;

public class TaskPane extends JPanel{
    private TaskTableModel tableModel;

    public TaskPane(){
        super();
        initComponents();
    }

    private void initComponents() {
        JTable taskTable = new JTable();
        JScrollPane scrollPane = new JScrollPane(taskTable);
        scrollPane.getViewport().setBackground(Color.WHITE);

        tableModel = new TaskTableModel();
        taskTable.setModel(tableModel);
        taskTable.getColumn("Progress").setCellRenderer(new TaskTableCellRender());
        taskTable.getColumn("Direction").setMaxWidth(70);

        scrollPane.addMouseListener(new MouseAdapter() {
            @Override
            public void mouseClicked(MouseEvent e) {
                taskTable.clearSelection();
            }
        });

        JPopupMenu popupMenu = new JPopupMenu();
        JMenuItem clearItem = new JMenuItem("Clear All");
        clearItem.addActionListener(new ActionListener() {
            @Override
            public void actionPerformed(ActionEvent e) {
                tableModel.clearTasks();
            }
        });

        GroupLayout layout = new GroupLayout(this);
        this.setLayout(layout);

        layout.setVerticalGroup(layout.createParallelGroup().addComponent(scrollPane));
        layout.setHorizontalGroup(layout.createSequentialGroup().addContainerGap().addComponent(scrollPane).addContainerGap());
    }

    public TaskTableModel getTableModel(){
        return tableModel;
    }

    public void addTask(Task task){
        tableModel.addRow(task);
    }

}
