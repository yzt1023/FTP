/*
 * Written By : Skye You
 * Copyright (c) 2018 - 2019. All Rights Reserved.
 */

package cn.edu.shu.server.ui.user;

import cn.edu.shu.common.bean.User;
import cn.edu.shu.common.util.Constants;
import cn.edu.shu.common.util.MessageUtils;

import javax.swing.*;
import javax.swing.border.TitledBorder;

public class DetailPane extends JPanel {
    private JPanel infoPane;
    private JCheckBox cbValid;
    private JLabel lblUsername;
    private JTextField txtUsername;
    private JLabel lblPassword;
    private JPasswordField txtPassword;

    private JPanel permPane;
    private JCheckBox cbRead;
    private JCheckBox cbWrite;
    private JCheckBox cbDelete;

    private JButton btnSave;
    private JButton btnCancel;
    private User user;
    private UserSettingListener listener;

    DetailPane(UserSettingListener listener) {
        super();
        this.listener = listener;
        initComponents();
    }

    private void initComponents() {
        initInfo();
        initPerm();
        btnSave = new JButton("Save");
        btnCancel = new JButton("Cancel");
        btnCancel.addActionListener(e -> listener.dispose());

        btnSave.addActionListener(e -> {
            User user = getUser();
            if (user.getUsername().isEmpty()) {
                MessageUtils.showInfoMessage(Constants.EMPTY_USER);
                return;
            }
            if (user.getPassword().isEmpty()) {
                MessageUtils.showInfoMessage(Constants.EMPTY_PASSWORD);
                return;
            }
            boolean success;
            if (txtUsername.isEditable())
                success = listener.newUserToDB(user);
            else
                success = listener.updateUser(user);

            if (success)
                MessageUtils.showInfoMessage(Constants.SAVE_SUCCEED);
            else
                MessageUtils.showInfoMessage(Constants.SAVE_FAILED);

        });

        cbValid.addChangeListener(e -> {
            if (cbValid.isSelected())
                setEnabled(true);
            else
                setEnabled(false);
        });

        TitledBorder border = BorderFactory.createTitledBorder("Details");
        border.setTitleJustification(TitledBorder.RIGHT);
        this.setBorder(border);

        setGroupLayout();
    }

    private void initInfo() {
        infoPane = new JPanel();
        infoPane.setBorder(BorderFactory.createTitledBorder("infos"));
        cbValid = new JCheckBox("Enable account");
        lblUsername = new JLabel("Username: ");
        txtUsername = new JTextField();
        lblPassword = new JLabel("Password: ");
        txtPassword = new JPasswordField();
        GroupLayout layout = new GroupLayout(infoPane);
        infoPane.setLayout(layout);

        GroupLayout.ParallelGroup lblGroup = layout.createParallelGroup();
        lblGroup.addComponent(lblUsername);
        lblGroup.addComponent(lblPassword);

        GroupLayout.ParallelGroup txtGroup = layout.createParallelGroup();
        txtGroup.addComponent(txtUsername, GroupLayout.PREFERRED_SIZE, 150, GroupLayout.PREFERRED_SIZE);
        txtGroup.addComponent(txtPassword, GroupLayout.PREFERRED_SIZE, 150, GroupLayout.PREFERRED_SIZE);

        GroupLayout.SequentialGroup horizontalGroup = layout.createSequentialGroup();
        horizontalGroup.addContainerGap();
        horizontalGroup.addGroup(lblGroup);
        horizontalGroup.addPreferredGap(LayoutStyle.ComponentPlacement.RELATED);
        horizontalGroup.addGroup(txtGroup);
        horizontalGroup.addContainerGap();

        layout.setHorizontalGroup(layout.createParallelGroup().addComponent(cbValid).addGroup(horizontalGroup));

        GroupLayout.SequentialGroup verticalGroup = layout.createSequentialGroup();
        verticalGroup.addComponent(cbValid);
        verticalGroup.addPreferredGap(LayoutStyle.ComponentPlacement.RELATED);
        verticalGroup.addGroup(layout.createParallelGroup(GroupLayout.Alignment.BASELINE).addComponent(lblUsername).addComponent(txtUsername));
        verticalGroup.addGroup(layout.createParallelGroup(GroupLayout.Alignment.BASELINE).addComponent(lblPassword).addComponent(txtPassword));
        layout.setVerticalGroup(verticalGroup);

    }

    private void initPerm() {
        permPane = new JPanel();
        permPane.setBorder(BorderFactory.createTitledBorder("permissions"));
        cbRead = new JCheckBox("Read");
        cbWrite = new JCheckBox("Write");
        cbDelete = new JCheckBox("Delete");
        GroupLayout layout = new GroupLayout(permPane);
        permPane.setLayout(layout);

        GroupLayout.ParallelGroup horizontalGroup = layout.createParallelGroup();
        horizontalGroup.addComponent(cbRead);
        horizontalGroup.addComponent(cbWrite);
        horizontalGroup.addComponent(cbDelete);
        layout.setHorizontalGroup(horizontalGroup);

        GroupLayout.SequentialGroup verticalGroup = layout.createSequentialGroup();
        verticalGroup.addComponent(cbRead);
        verticalGroup.addPreferredGap(LayoutStyle.ComponentPlacement.RELATED);
        verticalGroup.addComponent(cbWrite);
        verticalGroup.addPreferredGap(LayoutStyle.ComponentPlacement.RELATED);
        verticalGroup.addComponent(cbDelete);
        layout.setVerticalGroup(verticalGroup);
    }

    private void setGroupLayout() {
        GroupLayout layout = new GroupLayout(this);
        this.setLayout(layout);

        GroupLayout.SequentialGroup buttonGroup = layout.createSequentialGroup();
        buttonGroup.addContainerGap();
        buttonGroup.addComponent(btnSave);
        buttonGroup.addPreferredGap(LayoutStyle.ComponentPlacement.RELATED);
        buttonGroup.addComponent(btnCancel);
        buttonGroup.addContainerGap();

        GroupLayout.ParallelGroup horizontalGroup = layout.createParallelGroup();
        horizontalGroup.addComponent(infoPane, GroupLayout.PREFERRED_SIZE, 300, GroupLayout.PREFERRED_SIZE);
        horizontalGroup.addComponent(permPane, GroupLayout.PREFERRED_SIZE, 300, GroupLayout.PREFERRED_SIZE);
        horizontalGroup.addGroup(buttonGroup);
        layout.setHorizontalGroup(horizontalGroup);

        GroupLayout.SequentialGroup verticalGroup = layout.createSequentialGroup();
        verticalGroup.addContainerGap();
        verticalGroup.addComponent(infoPane);
        verticalGroup.addPreferredGap(LayoutStyle.ComponentPlacement.UNRELATED);
        verticalGroup.addComponent(permPane);
        verticalGroup.addPreferredGap(LayoutStyle.ComponentPlacement.UNRELATED);
        verticalGroup.addGroup(layout.createParallelGroup(GroupLayout.Alignment.BASELINE).addComponent(btnSave).addComponent(btnCancel));
        verticalGroup.addContainerGap();

        layout.setVerticalGroup(verticalGroup);

    }

    public User getUser() {
        if (user == null)
            user = new User();
        user.setUsername(txtUsername.getText());
        user.setPassword(new String(txtPassword.getPassword()));
        user.setReadable(cbRead.isSelected());
        user.setWritable(cbWrite.isSelected());
        user.setDeleted(cbDelete.isSelected());
        user.setValid(cbValid.isSelected());
        return user;
    }

    public void setUser(User user) {
        this.user = user;
        txtUsername.setText(user.getUsername());
        txtUsername.setEditable(false);
        txtPassword.setText(user.getPassword());
        cbRead.setSelected(user.isReadable());
        cbWrite.setSelected(user.isWritable());
        cbDelete.setSelected(user.canDeleted());
        cbValid.setSelected(user.isValid());
        if (!user.isValid())
            setEnabled(false);
    }

    void clearAll() {
        txtUsername.setText("");
        txtUsername.setEditable(true);
        txtPassword.setText("");
        cbRead.setSelected(false);
        cbWrite.setSelected(false);
        cbDelete.setSelected(false);
        cbValid.setSelected(true);
        user = null;
    }

    @Override
    public void setEnabled(boolean enabled) {
        txtUsername.setEnabled(enabled);
        txtPassword.setEnabled(enabled);
        cbRead.setEnabled(enabled);
        cbWrite.setEnabled(enabled);
        cbDelete.setEnabled(enabled);
    }
}
