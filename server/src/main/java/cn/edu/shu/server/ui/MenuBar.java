/*
 * Written By : Skye You
 * Copyright (c) 2018 - 2019. All Rights Reserved.
 */

package cn.edu.shu.server.ui;

import cn.edu.shu.common.util.Constants;
import cn.edu.shu.common.util.MessageUtils;
import cn.edu.shu.common.util.CommonUtils;
import cn.edu.shu.server.ui.user.UserDialog;

import javax.swing.*;
import java.util.Calendar;

class MenuBar extends JMenuBar {

    private JMenu menuFile;
    private JMenu menuEdit;
    private JMenu menuHelp;
    private JMenuItem connectItem;
    private JMenuItem disconnectItem;
    private JMenuItem exitItem;
    private JMenuItem settingItem;
    private JMenuItem userItem;
    private JMenuItem aboutItem;
    private MainFrame frame;
    private CommonUtils utils = CommonUtils.getInstance();

    public MenuBar(MainFrame frame) {
        super();
        this.frame = frame;
        initComponents();
    }

    private void initComponents() {
        menuFile = new JMenu("File");
        menuFile.setMnemonic('f');
        connectItem = new JMenuItem("Connect to server");
        connectItem.setMnemonic('c');
        menuFile.add(connectItem);
        disconnectItem = new JMenuItem("Disconnect");
        disconnectItem.setMnemonic('d');
        menuFile.add(disconnectItem);
        exitItem = new JMenuItem("Exit", new ImageIcon(utils.getResourcePath(getClass(), "exit.png")));
        exitItem.setMnemonic('x');
        menuFile.add(exitItem);
        this.add(menuFile);

        menuEdit = new JMenu("Edit");
        menuEdit.setMnemonic('e');
        userItem = new JMenuItem("User...");
        userItem.setMnemonic('u');
        menuEdit.add(userItem);
        settingItem = new JMenuItem("Setting...");
        settingItem.setMnemonic('t');
        menuEdit.add(settingItem);
        this.add(menuEdit);

        menuHelp = new JMenu("Help");
        menuHelp.setMnemonic('h');
        aboutItem = new JMenuItem("About...");
        aboutItem.setMnemonic('a');
        menuHelp.add(aboutItem);
        this.add(menuHelp);

        connectItem.setEnabled(false);

        connectItem.addActionListener(e -> {
            frame.getFtpServer().resumeServer();
            connectItem.setEnabled(false);
            disconnectItem.setEnabled(true);
        });

        disconnectItem.addActionListener(e -> {
            frame.getFtpServer().suspendServer();
            connectItem.setEnabled(true);
            disconnectItem.setEnabled(false);
        });

        exitItem.addActionListener(e -> {
            int i = MessageUtils.showConfirmMessage(Constants.CONFIRM_TO_EXIT,
                    "confirm dialog");
            if (i == 0)
                System.exit(0);
        });

        userItem.addActionListener(e -> {
            UserDialog userDialog = new UserDialog(frame);
            userDialog.setVisible(true);
        });

        aboutItem.addActionListener(e -> {
            Calendar date = Calendar.getInstance();
            String year = String.valueOf(date.get(Calendar.YEAR));
            String message = "FTPServer\nCopyright(C) 2018 - " + year + " All Rights Reserved.\nWritten by: Skye You";
            ImageIcon image = new ImageIcon(utils.getResourcePath(getClass(), "logo.png"));
            MessageUtils.showMessage(message, "about FTP", image);
        });
    }
}
