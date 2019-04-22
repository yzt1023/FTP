/*
 * Written By : Skye You
 * Copyright (c) 2018 - 2019. All Rights Reserved.
 */

package cn.edu.shu.server.ui.user;

import cn.edu.shu.common.bean.User;
import cn.edu.shu.common.util.MessageUtils;

import javax.swing.*;
import javax.swing.border.BevelBorder;
import java.util.List;

class UserPane extends JPanel {
    private JList<User> userList;
    private JScrollPane scrollPane;
    private JButton btnAdd;
    private JButton btnRemove;
    private UserListModel userListModel;
    private UserSettingListener listener;

    UserPane(List<User> users, UserSettingListener listener) {
        super();
        this.listener = listener;
        initComponents();
        userListModel.setUsers(users);
        userList.setSelectedIndex(0);
    }

    private void initComponents() {
        // left pane
        userList = new JList<>();
        scrollPane = new JScrollPane(userList);
        btnAdd = new JButton("Add");
        btnRemove = new JButton("Remove");

        userListModel = new UserListModel();
        userList.setModel(userListModel);

        userList.setSelectionMode(ListSelectionModel.SINGLE_SELECTION);
        userList.addListSelectionListener(e -> {
            int index = userList.getSelectedIndex();
            if(index > -1 && index < userListModel.getSize()) {
                User user = userListModel.getUser(index);
                listener.showUserDetail(user);
            }
        });
        btnAdd.addActionListener(e -> {
            listener.cleanDetail();
            userList.clearSelection();
        });
        btnRemove.addActionListener(e -> {
            int index = userList.getSelectedIndex();
            if(index == -1)
                MessageUtils.showInfoMessage("No user is selected!");
            else{
                User user = userListModel.getUser(index);
                if(listener.RemoveUserFromDB(user)) {
                    userListModel.removeRow(index);
                    MessageUtils.showInfoMessage("Remove successfully!");
                }
            }
        });
        scrollPane.setViewportBorder(BorderFactory.createBevelBorder(BevelBorder.LOWERED));
        this.setBorder(BorderFactory.createTitledBorder("Users"));
        setGroupLayout();
    }

    private void setGroupLayout() {
        GroupLayout layout = new GroupLayout(this);
        this.setLayout(layout);


        GroupLayout.SequentialGroup btnGroup = layout.createSequentialGroup();
        btnGroup.addContainerGap();
        btnGroup.addComponent(btnAdd);
        btnGroup.addPreferredGap(LayoutStyle.ComponentPlacement.RELATED);
        btnGroup.addComponent(btnRemove);
        btnGroup.addContainerGap();

        GroupLayout.ParallelGroup horizontalGroup = layout.createParallelGroup();
        horizontalGroup.addComponent(scrollPane, GroupLayout.PREFERRED_SIZE, 150, GroupLayout.PREFERRED_SIZE);
        horizontalGroup.addGroup(btnGroup);
        layout.setHorizontalGroup(horizontalGroup);

        GroupLayout.SequentialGroup sequentialGroup = layout.createSequentialGroup();
        sequentialGroup.addContainerGap();
        sequentialGroup.addComponent(scrollPane, GroupLayout.PREFERRED_SIZE, 250, GroupLayout.PREFERRED_SIZE);
        sequentialGroup.addPreferredGap(LayoutStyle.ComponentPlacement.UNRELATED);
        sequentialGroup.addGroup(layout.createParallelGroup().addComponent(btnAdd).addComponent(btnRemove));

        layout.setVerticalGroup(sequentialGroup);
    }

    void addRow(User user) {
        userListModel.addRow(user);
        userList.setSelectedIndex(userListModel.getSize() - 1);
    }
}
