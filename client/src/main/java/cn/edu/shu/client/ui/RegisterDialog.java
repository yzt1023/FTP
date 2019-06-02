/*
 * Written By : Skye You
 * Copyright (c) 2018 - 2019. All Rights Reserved.
 */

package cn.edu.shu.client.ui;

import cn.edu.shu.client.exception.ConnectionException;
import cn.edu.shu.client.exception.FTPException;
import cn.edu.shu.client.ftp.FTPClient;
import cn.edu.shu.common.util.Constants;
import cn.edu.shu.common.util.MessageUtils;
import org.apache.log4j.Logger;

import javax.swing.*;
import java.awt.*;
import java.awt.event.KeyAdapter;
import java.awt.event.KeyEvent;
import java.util.Arrays;

class RegisterDialog extends JDialog {

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
    private Logger logger = Logger.getLogger(getClass());

    RegisterDialog(JFrame frame) {
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
        Font f = new Font("Times New Roman Italic", Font.BOLD, 24);
        lblTitle.setFont(f);

        lblHost = new JLabel("Host: ");
        txtHost = new JTextField();
        lblPort = new JLabel("Port: ");
        txtPort = new JTextField();
        lblUsername = new JLabel("Username: ");
        txtUsername = new JTextField();
        lblPassword = new JLabel("Password: ");
        txtPassword = new JPasswordField();
        lblConfirmPwd = new JLabel("Confirm password: ");
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
            int portNum = Constants.DEFAULT_CONTROL_PORT;
            String user = txtUsername.getText();
            char[] pwd = txtPassword.getPassword();
            char[] confirmPwd = txtConfirmPwd.getPassword();

            if (host.isEmpty()) {
                MessageUtils.showInfoMessage(Constants.EMPTY_HOST);
                return;
            }

            if (user.isEmpty()) {
                MessageUtils.showInfoMessage(Constants.EMPTY_USER);
                return;
            }

            if (pwd.length == 0) {
                MessageUtils.showInfoMessage(Constants.EMPTY_PASSWORD);
                return;
            }

            if (!Arrays.equals(pwd, confirmPwd)) {
                MessageUtils.showInfoMessage(Constants.PASSWORD_DIFFERENT);
                return;
            }

            if (!port.isEmpty()) {
                try {
                    portNum = Integer.parseInt(txtPort.getText());
                } catch (NumberFormatException exception) {
                    MessageUtils.showInfoMessage(Constants.INVALID_PORT);
                    return;
                }
            }

            userRegister(host, portNum, user, new String(pwd));
        });

        btnReset.addActionListener(e -> clearAll());

        btnCancel.addActionListener(e -> dispose());
    }

    private void userRegister(String host, int port, String username, String password) {
        FTPClient ftpClient = frame.getFtpClient();
        try {
            ftpClient.connect(host, port);
            ftpClient.register(username, password);
        } catch (ConnectionException e) {
            logger.error(e.getMessage(), e);
            MessageUtils.showInfoMessage(Constants.CONNECT_FAILED);
            return;
        } catch (FTPException e) {
            logger.error(e.getMessage(), e);
            MessageUtils.showInfoMessage(Constants.USER_EXISTS);
            return;
        }
        MessageUtils.showInfoMessage(Constants.REGISTER_SUCCEED);
        clearAll();
    }

    private void clearAll() {
        txtHost.setText("");
        txtPort.setText("");
        txtUsername.setText("");
        txtPassword.setText("");
        txtConfirmPwd.setText("");
    }

    private void setGroupLayout() {
        GroupLayout layout = new GroupLayout(contentPane);
        contentPane.setLayout(layout);

        GroupLayout.ParallelGroup lblGroup = layout.createParallelGroup(GroupLayout.Alignment.TRAILING);
        lblGroup.addComponent(lblHost);
        lblGroup.addComponent(lblPort);
        lblGroup.addComponent(lblUsername);
        lblGroup.addComponent(lblPassword);
        lblGroup.addComponent(lblConfirmPwd);

        GroupLayout.ParallelGroup txtGroup = layout.createParallelGroup();
        txtGroup.addComponent(txtHost, GroupLayout.PREFERRED_SIZE, 200, GroupLayout.PREFERRED_SIZE);
        txtGroup.addComponent(txtPort, GroupLayout.PREFERRED_SIZE, 200, GroupLayout.PREFERRED_SIZE);
        txtGroup.addComponent(txtUsername, GroupLayout.PREFERRED_SIZE, 200, GroupLayout.PREFERRED_SIZE);
        txtGroup.addComponent(txtPassword, GroupLayout.PREFERRED_SIZE, 200, GroupLayout.PREFERRED_SIZE);
        txtGroup.addComponent(txtConfirmPwd, GroupLayout.PREFERRED_SIZE, 200, GroupLayout.PREFERRED_SIZE);

        GroupLayout.SequentialGroup btnGroup = layout.createSequentialGroup();
        btnGroup.addGap(120);
        btnGroup.addComponent(btnSubmit);
        btnGroup.addPreferredGap(LayoutStyle.ComponentPlacement.RELATED);
        btnGroup.addComponent(btnReset);
        btnGroup.addPreferredGap(LayoutStyle.ComponentPlacement.RELATED);
        btnGroup.addComponent(btnCancel);

        GroupLayout.ParallelGroup horizontal = layout.createParallelGroup();
        horizontal.addGroup(layout.createSequentialGroup().addGap(180).addComponent(lblTitle));
        horizontal.addGroup(layout.createSequentialGroup().addGap(60).addGroup(lblGroup).addGroup(txtGroup));
        horizontal.addGroup(btnGroup);
        layout.setHorizontalGroup(horizontal);

        GroupLayout.SequentialGroup vertical = layout.createSequentialGroup();
        vertical.addGap(30);
        vertical.addComponent(lblTitle);
        vertical.addGap(20);
        vertical.addGroup(layout.createParallelGroup(GroupLayout.Alignment.BASELINE).addComponent(lblHost).addComponent(txtHost));
        vertical.addPreferredGap(LayoutStyle.ComponentPlacement.RELATED);
        vertical.addGroup(layout.createParallelGroup(GroupLayout.Alignment.BASELINE).addComponent(lblPort).addComponent(txtPort));
        vertical.addPreferredGap(LayoutStyle.ComponentPlacement.RELATED);
        vertical.addGroup(layout.createParallelGroup(GroupLayout.Alignment.BASELINE).addComponent(lblUsername).addComponent(txtUsername));
        vertical.addPreferredGap(LayoutStyle.ComponentPlacement.RELATED);
        vertical.addGroup(layout.createParallelGroup(GroupLayout.Alignment.BASELINE).addComponent(lblPassword).addComponent(txtPassword));
        vertical.addPreferredGap(LayoutStyle.ComponentPlacement.RELATED);
        vertical.addGroup(layout.createParallelGroup(GroupLayout.Alignment.BASELINE).addComponent(lblConfirmPwd).addComponent(txtConfirmPwd));
        vertical.addGap(20);
        vertical.addGroup(layout.createParallelGroup().addComponent(btnSubmit).addComponent(btnReset).addComponent(btnCancel));
        layout.setVerticalGroup(vertical);
    }
}
