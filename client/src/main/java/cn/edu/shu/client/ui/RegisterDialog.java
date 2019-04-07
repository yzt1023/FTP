/*
 * Written By : Skye You
 * Copyright (c) 2018 - 2019. All Rights Reserved.
 */

package cn.edu.shu.client.ui;

import cn.edu.shu.common.util.Constants;
import cn.edu.shu.common.util.MessageUtils;

import javax.swing.*;
import java.awt.*;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.awt.event.KeyAdapter;
import java.awt.event.KeyEvent;
import java.util.Arrays;

public class RegisterDialog extends JDialog {

    private JPanel contentPane;
    private JLabel lblTitle;
    private JLabel lblHost;
    private JTextField txtHost;
    private JLabel lblPort;
    private JTextField txtPort;
    private JLabel lblUsername;
    private JTextField txtUsername;
    private JLabel lblPassword;
    private JPasswordField txtPassword;
    private JLabel lblConfirmPwd;
    private JPasswordField txtConfirmPwd;
    private JButton btnSubmit;
    private JButton btnReset;
    private JButton btnCancel;
    private MainFrame frame;

    public RegisterDialog(JFrame frame){
        super(frame, ModalityType.APPLICATION_MODAL);
        this.frame = (MainFrame) frame;
        this.setTitle("Register Dialog");
        this.setSize(500, 400);
        this.setLocation(600, 300);

        contentPane = new JPanel();
        this.setContentPane(contentPane);

        initComponents();

        setGroupLayout();
    }

    private void initComponents() {
        lblTitle = new JLabel("User Register");
        Font f = new Font("Times New Roman Italic", Font.BOLD,24);
        lblTitle.setFont(f);

        lblHost = new JLabel("Host: ");
        txtHost = new JTextField();
        lblPort = new JLabel("Port: ");
        txtPort = new JTextField();
        lblUsername = new JLabel("Username: ");
        txtUsername = new JTextField();
        lblPassword = new JLabel("Password: ");
        txtPassword = new JPasswordField();
        lblConfirmPwd = new JLabel("Confirm Password: ");
        txtConfirmPwd = new JPasswordField();
        btnSubmit = new JButton("Submit");
        btnReset = new JButton("Reset");
        btnCancel = new JButton("Cancel");

        txtPassword.addKeyListener(new KeyAdapter() {
            @Override
            public void keyPressed(KeyEvent e) {
                if (e.getKeyChar() == KeyEvent.VK_ENTER) {
                    btnSubmit.doClick();
                }
            }
        });

        btnSubmit.addActionListener(e -> {
            String host = txtHost.getText();
            String port = txtPort.getText();
            int portNum = Constants.DEFAULT_PORT;
            String user = txtUsername.getText();
            char[] pwd = txtPassword.getPassword();
            char[] confirmPwd = txtConfirmPwd.getPassword();

            if (host.isEmpty()) {
                MessageUtils.showErrorMessage(Constants.EMPTY_HOST);
                return;
            }

            if(user.isEmpty()){
                MessageUtils.showErrorMessage(Constants.EMPTY_USER);
                return;
            }

            if(!Arrays.equals(pwd, confirmPwd)){
                MessageUtils.showErrorMessage(Constants.PASSWORD_DIFFERENT);
                return;
            }

            if (!port.isEmpty()) {
                try {
                    portNum = Integer.parseInt(txtPort.getText());
                } catch (NumberFormatException exception) {
                    MessageUtils.showErrorMessage(Constants.INVALID_PORT);
                    return;
                }
            }

            if(!frame.userRegister(host, portNum, user, new String(pwd)))
                MessageUtils.showErrorMessage("Username has been registered! Please use another username!");
            else {
                MessageUtils.showErrorMessage("Register successfully!");
                clearAll();
            }
        });

        btnReset.addActionListener(e -> {
            clearAll();
        });


        btnCancel.addActionListener(e -> dispose());
    }

    private void clearAll() {
        txtHost.setText("");
        txtPort.setText("");
        txtUsername.setText("");
        txtPassword.setText("");
    }

    private void setGroupLayout() {
        GroupLayout groupLayout = new GroupLayout(contentPane);
        contentPane.setLayout(groupLayout);
        groupLayout.setAutoCreateContainerGaps(true);
        groupLayout.setAutoCreateGaps(true);

        GroupLayout.SequentialGroup hostGroup = groupLayout.createSequentialGroup();
        hostGroup.addGap(60);
        hostGroup.addComponent(lblHost);
        hostGroup.addPreferredGap(LayoutStyle.ComponentPlacement.RELATED);
        hostGroup.addComponent(txtHost, GroupLayout.PREFERRED_SIZE, 160, GroupLayout.PREFERRED_SIZE);
        hostGroup.addPreferredGap(LayoutStyle.ComponentPlacement.UNRELATED);
        hostGroup.addComponent(lblPort);
        hostGroup.addPreferredGap(LayoutStyle.ComponentPlacement.RELATED);
        hostGroup.addComponent(txtPort, GroupLayout.PREFERRED_SIZE, 85, GroupLayout.PREFERRED_SIZE);

        GroupLayout.SequentialGroup usernameGroup = groupLayout.createSequentialGroup();
        usernameGroup.addGap(60);
        usernameGroup.addComponent(lblUsername);
        usernameGroup.addPreferredGap(LayoutStyle.ComponentPlacement.RELATED);
        usernameGroup.addComponent(txtUsername, GroupLayout.PREFERRED_SIZE, 280, GroupLayout.PREFERRED_SIZE);

        GroupLayout.SequentialGroup passwordGroup = groupLayout.createSequentialGroup();
        passwordGroup.addGap(60);
        passwordGroup.addComponent(lblPassword);
        passwordGroup.addPreferredGap(LayoutStyle.ComponentPlacement.RELATED);
        passwordGroup.addComponent(txtPassword, GroupLayout.PREFERRED_SIZE, 280, GroupLayout.PREFERRED_SIZE);

        GroupLayout.SequentialGroup confirmPwdGroup = groupLayout.createSequentialGroup();
        confirmPwdGroup.addGap(60);
        confirmPwdGroup.addComponent(lblConfirmPwd);
        confirmPwdGroup.addPreferredGap(LayoutStyle.ComponentPlacement.RELATED);
        confirmPwdGroup.addComponent(txtConfirmPwd, GroupLayout.PREFERRED_SIZE, 208, GroupLayout.PREFERRED_SIZE);

        GroupLayout.SequentialGroup buttonGroup = groupLayout.createSequentialGroup();
        buttonGroup.addGap(120);
        buttonGroup.addComponent(btnSubmit);
        buttonGroup.addPreferredGap(LayoutStyle.ComponentPlacement.RELATED);
        buttonGroup.addComponent(btnReset);
        buttonGroup.addPreferredGap(LayoutStyle.ComponentPlacement.RELATED);
        buttonGroup.addComponent(btnCancel);

        GroupLayout.ParallelGroup horizontalGroup = groupLayout.createParallelGroup();
        horizontalGroup.addGroup(groupLayout.createSequentialGroup().addGap(180).addComponent(lblTitle));
        horizontalGroup.addGroup(hostGroup);
        horizontalGroup.addGroup(usernameGroup);
        horizontalGroup.addGroup(passwordGroup);
        horizontalGroup.addGroup(confirmPwdGroup);
        horizontalGroup.addGroup(buttonGroup);

        GroupLayout.ParallelGroup hostGroup1 = groupLayout.createParallelGroup(GroupLayout.Alignment.BASELINE);
        hostGroup1.addComponent(lblHost).addComponent(txtHost);
        hostGroup1.addComponent(lblPort).addComponent(txtPort);

        GroupLayout.ParallelGroup usernameGroup1 = groupLayout.createParallelGroup(GroupLayout.Alignment.BASELINE);
        usernameGroup1.addComponent(lblUsername).addComponent(txtUsername);

        GroupLayout.ParallelGroup passwordGroup1 = groupLayout.createParallelGroup(GroupLayout.Alignment.BASELINE);
        passwordGroup1.addComponent(lblPassword).addComponent(txtPassword);

        GroupLayout.ParallelGroup confirmPwdGroup1 = groupLayout.createParallelGroup(GroupLayout.Alignment.BASELINE);
        confirmPwdGroup1.addComponent(lblConfirmPwd).addComponent(txtConfirmPwd);

        GroupLayout.ParallelGroup buttonGroup1 = groupLayout.createParallelGroup(GroupLayout.Alignment.BASELINE);
        buttonGroup1.addComponent(btnSubmit).addComponent(btnReset).addComponent(btnCancel);

        GroupLayout.SequentialGroup verticalGroup = groupLayout.createSequentialGroup();
        verticalGroup.addGap(40);
        verticalGroup.addComponent(lblTitle);
        verticalGroup.addGap(30);
        verticalGroup.addGroup(hostGroup1);
        verticalGroup.addPreferredGap(LayoutStyle.ComponentPlacement.RELATED);
        verticalGroup.addGroup(usernameGroup1);
        verticalGroup.addPreferredGap(LayoutStyle.ComponentPlacement.RELATED);
        verticalGroup.addGroup(passwordGroup1);
        verticalGroup.addPreferredGap(LayoutStyle.ComponentPlacement.RELATED);
        verticalGroup.addGroup(confirmPwdGroup1);
        verticalGroup.addGap(30);
        verticalGroup.addGroup(buttonGroup1);
        verticalGroup.addContainerGap();

        groupLayout.setHorizontalGroup(horizontalGroup);
        groupLayout.setVerticalGroup(verticalGroup);
    }
}
