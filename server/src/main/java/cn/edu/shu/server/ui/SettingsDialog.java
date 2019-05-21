/*
 * Written By : Skye You
 * Copyright (c) 2018 - 2019. All Rights Reserved.
 */

package cn.edu.shu.server.ui;

import cn.edu.shu.common.util.Constants;
import cn.edu.shu.common.util.MessageUtils;
import cn.edu.shu.server.config.SystemConfig;

import javax.swing.*;
import java.io.File;
import java.nio.charset.Charset;
import java.nio.charset.UnsupportedCharsetException;

class SettingsDialog extends JDialog {

    private JScrollPane scrollMsg;
    private JLabel lblWelcomeMsg;
    private JTextArea txtWelcomeMsg;
    // timeout
    private JLabel lblTimeout;
    private JTextField txtTimeout;
    private JLabel lblTimeUnit;
    // port
    private JLabel lblPort;
    private JTextField txtMinPort;
    private JLabel lblTo;
    private JTextField txtMaxPort;
    private JLabel lblRange;
    // root path to share
    private JLabel lblSharedFolder;
    private JTextField txtSharedFolder;
    private JButton btnBrowse;
    private JFileChooser fileChooser;
    // encoding
    private JLabel lblEncoding;
    private JTextField txtEncoding;

    private JButton btnSave;
    private JButton btnCancel;

    private SystemConfig config = SystemConfig.getInstance();


    SettingsDialog(JFrame frame) {
        super(frame, ModalityType.APPLICATION_MODAL);
        this.setSize(600, 500);
        this.setLocation(800, 300);
        this.setTitle("Settings Dialog");

        initComponents();
        addActions();

        setGroupLayout();

    }

    private void initComponents() {

        lblWelcomeMsg = new JLabel("Welcome message");
        txtWelcomeMsg = new JTextArea(config.getWelcomeMessage());
        scrollMsg = new JScrollPane(txtWelcomeMsg);

        lblTimeout = new JLabel("Connection time out:");
        txtTimeout = new JTextField(config.getTimeout() + "");
        lblTimeUnit = new JLabel("in seconds(0 means no timeout)");

        lblPort = new JLabel("Passive port range:");
        txtMinPort = new JTextField(config.getPassiveMinPort() + "");
        lblTo = new JLabel("-");
        txtMaxPort = new JTextField(config.getPassiveMaxPort() + "");
        lblRange = new JLabel("(40000 - 65535)");

        lblSharedFolder = new JLabel("Default shared folder:");
        txtSharedFolder = new JTextField(config.getRootPath());
        btnBrowse = new JButton("Browse");
        fileChooser = new JFileChooser();
        fileChooser.setFileSelectionMode(JFileChooser.DIRECTORIES_ONLY);

        lblEncoding = new JLabel("Connection charset:");
        txtEncoding = new JTextField(config.getEncoding());

        btnSave = new JButton("Save");
        btnCancel = new JButton("Cancel");
    }

    private void addActions() {
        btnCancel.addActionListener(e -> dispose());

        btnBrowse.addActionListener(e -> {
            fileChooser.showOpenDialog(this);
            File file = fileChooser.getSelectedFile();
            if (file != null)
                txtSharedFolder.setText(file.getPath());

        });

        btnSave.addActionListener(e -> {
            String welcomeMsg = txtWelcomeMsg.getText();
            String rootDir = txtSharedFolder.getText();
            String encoding = txtEncoding.getText();
            String minPort = txtMinPort.getText();
            String maxPort = txtMaxPort.getText();
            String timeout = txtTimeout.getText();
            if (verifyInput(welcomeMsg, rootDir, encoding, minPort, maxPort, timeout)) {
                config.setWelcomeMessage(welcomeMsg);
                config.setRootPath(rootDir);
                config.setEncoding(encoding);
                config.setTimeout(Integer.parseInt(timeout));
                config.setPassiveMaxPort(Integer.parseInt(maxPort));
                config.setPassiveMinPort(Integer.parseInt(minPort));
                if (config.updateConfig()) {
                    MessageUtils.showInfoMessage(Constants.SAVE_SUCCEED);
                    this.dispose();
                }
                else
                    MessageUtils.showInfoMessage(Constants.SAVE_FAILED);
            }
        });
    }

    private boolean verifyInput(String welcomeMsg, String rootDir, String encoding, String lowPort, String highPort, String timeout) {
        if (welcomeMsg.isEmpty() || rootDir.isEmpty() || encoding.isEmpty()
                || lowPort.isEmpty() || highPort.isEmpty() || timeout.isEmpty()) {
            MessageUtils.showInfoMessage(Constants.EMPTY_INPUT);
            return false;
        }

        File file = new File(rootDir);
        if (!file.exists() || file.isFile()) {
            MessageUtils.showInfoMessage(Constants.INVALID_PATH);
            return false;
        }

        try {
            Charset.forName(encoding);
        } catch (UnsupportedCharsetException e) {
            MessageUtils.showInfoMessage(Constants.CHARSET_NOT_EXISTS);
            return false;
        }

        try {
            int low = Integer.parseInt(lowPort);
            int high = Integer.parseInt(highPort);
            int time = Integer.parseInt(timeout);
            if (low < 40000 || high > 65535 || low > high) {
                MessageUtils.showInfoMessage(Constants.PORT_INCORRECT);
                return false;
            }

            if (time < 0) {
                MessageUtils.showInfoMessage(Constants.TIME_INCORRECT);
                return false;
            }
            return true;
        } catch (NumberFormatException e) {
            MessageUtils.showInfoMessage(Constants.NUMBER_INCORRECT);
            return false;
        }

    }

    private void setGroupLayout() {
        JPanel contentPanel = new JPanel();
        this.setContentPane(contentPanel);

        GroupLayout layout = new GroupLayout(contentPanel);
        contentPanel.setLayout(layout);

        GroupLayout.ParallelGroup para1 = layout.createParallelGroup();
        para1.addComponent(lblEncoding);
        para1.addComponent(lblTimeout);
        para1.addComponent(lblPort);
        para1.addComponent(lblSharedFolder);
        para1.addComponent(lblWelcomeMsg);

        GroupLayout.SequentialGroup group1 = layout.createSequentialGroup();
        group1.addComponent(txtMinPort, GroupLayout.PREFERRED_SIZE, 80, GroupLayout.PREFERRED_SIZE);
        group1.addPreferredGap(LayoutStyle.ComponentPlacement.RELATED);
        group1.addComponent(lblTo);
        group1.addPreferredGap(LayoutStyle.ComponentPlacement.RELATED);
        group1.addComponent(txtMaxPort, GroupLayout.PREFERRED_SIZE, 80, GroupLayout.PREFERRED_SIZE);
        group1.addPreferredGap(LayoutStyle.ComponentPlacement.RELATED);
        group1.addComponent(lblRange);

        GroupLayout.SequentialGroup group2 = layout.createSequentialGroup();
        group2.addComponent(txtSharedFolder, GroupLayout.PREFERRED_SIZE, 200, GroupLayout.PREFERRED_SIZE);
        group2.addPreferredGap(LayoutStyle.ComponentPlacement.RELATED);
        group2.addComponent(btnBrowse);

        GroupLayout.SequentialGroup group3 = layout.createSequentialGroup();
        group3.addComponent(txtTimeout, GroupLayout.PREFERRED_SIZE, 80, GroupLayout.PREFERRED_SIZE);
        group3.addPreferredGap(LayoutStyle.ComponentPlacement.RELATED);
        group3.addComponent(lblTimeUnit);

        GroupLayout.SequentialGroup group5 = layout.createSequentialGroup();
        group5.addComponent(btnSave);
        group5.addPreferredGap(LayoutStyle.ComponentPlacement.RELATED);
        group5.addComponent(btnCancel);
        group5.addGap(20);

        GroupLayout.ParallelGroup para2 = layout.createParallelGroup();
        para2.addComponent(txtEncoding, GroupLayout.PREFERRED_SIZE, 80, GroupLayout.PREFERRED_SIZE);
        para2.addGroup(group3).addGroup(group1).addGroup(group2);

        GroupLayout.ParallelGroup horizontal = layout.createParallelGroup();
        horizontal.addGroup(layout.createSequentialGroup().addGap(30).addComponent(scrollMsg, GroupLayout.PREFERRED_SIZE, 500, GroupLayout.PREFERRED_SIZE));
        horizontal.addGroup(layout.createSequentialGroup().addGap(30).addGroup(para1).addGroup(para2));
        horizontal.addGroup(GroupLayout.Alignment.TRAILING, group5);

        layout.setHorizontalGroup(horizontal);

        GroupLayout.SequentialGroup vertical = layout.createSequentialGroup();
        vertical.addGap(30);
        vertical.addGroup(layout.createParallelGroup(GroupLayout.Alignment.BASELINE).addComponent(lblEncoding).addComponent(txtEncoding));
        vertical.addPreferredGap(LayoutStyle.ComponentPlacement.UNRELATED);
        vertical.addGroup(layout.createParallelGroup(GroupLayout.Alignment.BASELINE).addComponent(lblTimeout).addComponent(txtTimeout).addComponent(lblTimeUnit));
        vertical.addPreferredGap(LayoutStyle.ComponentPlacement.UNRELATED);
        vertical.addGroup(layout.createParallelGroup(GroupLayout.Alignment.BASELINE).addComponent(lblPort).addComponent(txtMinPort).addComponent(lblTo).addComponent(txtMaxPort).addComponent(lblRange));
        vertical.addPreferredGap(LayoutStyle.ComponentPlacement.UNRELATED);
        vertical.addGroup(layout.createParallelGroup(GroupLayout.Alignment.BASELINE).addComponent(lblSharedFolder).addComponent(txtSharedFolder).addComponent(btnBrowse));

        vertical.addGap(30);
        vertical.addComponent(lblWelcomeMsg);
        vertical.addPreferredGap(LayoutStyle.ComponentPlacement.RELATED);
        vertical.addComponent(scrollMsg, GroupLayout.PREFERRED_SIZE, 120, GroupLayout.PREFERRED_SIZE);
        vertical.addGap(20);
        vertical.addGroup(layout.createParallelGroup(GroupLayout.Alignment.BASELINE).addComponent(btnSave).addComponent(btnCancel));
        vertical.addGap(20);
        layout.setVerticalGroup(vertical);
    }

}
