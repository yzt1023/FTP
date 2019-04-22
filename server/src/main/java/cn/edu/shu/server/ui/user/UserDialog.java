/*
 * Written By : Skye You
 * Copyright (c) 2018 - 2019. All Rights Reserved.
 */

package cn.edu.shu.server.ui.user;

import cn.edu.shu.common.bean.User;
import cn.edu.shu.common.util.Constants;
import cn.edu.shu.common.util.MessageUtils;
import cn.edu.shu.server.db.UserDao;

import javax.swing.*;

public class UserDialog extends JDialog implements UserSettingListener {

    private JPanel panel;
    private UserPane userPane;
    private DetailPane detailPane;
    private UserDao userDao;

    public UserDialog(JFrame frame) {
        super(frame, ModalityType.APPLICATION_MODAL);
        this.setSize(580, 420);
        this.setLocation(800, 400);
        this.setTitle("User Setting Dialog");

        initComponents();
        setGroupLayout();
    }

    private void initComponents() {
        userDao = new UserDao();
        detailPane = new DetailPane(this);
        userPane = new UserPane(userDao.getUsers(true), this);
        panel = new JPanel();
        this.setContentPane(panel);
    }

    private void setGroupLayout() {
        GroupLayout layout = new GroupLayout(panel);
        panel.setLayout(layout);
        layout.setAutoCreateContainerGaps(true);

        GroupLayout.SequentialGroup horizontalGroup = layout.createSequentialGroup();
        horizontalGroup.addComponent(userPane, GroupLayout.PREFERRED_SIZE, 180, GroupLayout.PREFERRED_SIZE);
        horizontalGroup.addPreferredGap(LayoutStyle.ComponentPlacement.RELATED);
        horizontalGroup.addComponent(detailPane, GroupLayout.PREFERRED_SIZE, 350, GroupLayout.PREFERRED_SIZE);
        layout.setHorizontalGroup(horizontalGroup);

        GroupLayout.ParallelGroup verticalGroup = layout.createParallelGroup(GroupLayout.Alignment.BASELINE);
        verticalGroup.addComponent(userPane, GroupLayout.PREFERRED_SIZE, 360, GroupLayout.PREFERRED_SIZE);
        verticalGroup.addComponent(detailPane, GroupLayout.PREFERRED_SIZE, 360, GroupLayout.PREFERRED_SIZE);
        layout.setVerticalGroup(verticalGroup);

    }

    @Override
    public boolean newUserToDB(User user) {
        if (userDao.isUserExists(user.getUsername())) {
            MessageUtils.showInfoMessage(Constants.USER_EXISTS);
            return false;
        }
        userDao.addUser(user);
        userPane.addRow(user);
        return true;
    }

    @Override
    public boolean RemoveUserFromDB(User user) {
        userDao.deleteUser(user);
        return true;
    }

    @Override
    public boolean updateUser(User user) {
        userDao.updateUser(user);
        return true;
    }

    @Override
    public void showUserDetail(User user) {
        detailPane.setUser(user);
    }

    @Override
    public void cleanDetail() {
        detailPane.clearAll();
    }

}
