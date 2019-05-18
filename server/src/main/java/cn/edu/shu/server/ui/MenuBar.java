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

        settingItem.addActionListener(e -> {
            SettingsDialog settingsDialog = new SettingsDialog(frame);
            settingsDialog.setVisible(true);
        });

        aboutItem.addActionListener(e -> {
            String message = "FTPServer\n" + utils.getAboutMessage();
            ImageIcon image = new ImageIcon(utils.getResourcePath(getClass(), "logo.png"));
            MessageUtils.showMessage(message, "About FTP", image);
        });
    }
}
