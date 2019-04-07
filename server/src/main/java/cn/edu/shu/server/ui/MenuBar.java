/*
 * Written By : Skye You
 * Copyright (c) 2018 - 2019. All Rights Reserved.
 */

package cn.edu.shu.server.ui;

import cn.edu.shu.common.util.Constants;
import cn.edu.shu.common.util.MessageUtils;
import cn.edu.shu.common.util.Utils;

import javax.swing.*;
import java.util.Calendar;

public class MenuBar extends JMenuBar {

    private JMenu menuFile;
    private JMenu menuEdit;
    private JMenu menuHelp;
    private JMenuItem exitItem;
    private JMenuItem userItem;
    private JMenuItem aboutItem;
    private Utils utils = Utils.getInstance();

    public MenuBar() {
        super();
        initComponents();
    }

    private void initComponents() {
        menuFile = new JMenu("File");
        menuFile.setMnemonic('f');
        exitItem = new JMenuItem("Exit", new ImageIcon(utils.getResourcePath(getClass(), "exit")));
        exitItem.setMnemonic('x');
        menuFile.add(exitItem);
        this.add(menuFile);

        menuEdit = new JMenu("Edit");
        menuEdit.setMnemonic('e');
        userItem = new JMenuItem("User...");
        userItem.setMnemonic('u');
        menuEdit.add(userItem);
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
            UserDialog userDialog = new UserDialog();
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
