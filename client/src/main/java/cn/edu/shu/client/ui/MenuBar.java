/*
 * Written By : Skye You
 * Copyright (c) 2018 - 2019. All Rights Reserved.
 */

package cn.edu.shu.client.ui;

import cn.edu.shu.common.util.Constants;
import cn.edu.shu.common.util.MessageUtils;
import cn.edu.shu.common.util.Utils;

import javax.swing.*;
import java.util.Calendar;

public class MenuBar extends JMenuBar {

    private JMenu menuFile;
    private JMenu menuMode;
    private JMenu menuHelp;
    private JMenuItem exitItem;
    private JCheckBoxMenuItem passiveModeItem;
    private JCheckBoxMenuItem secureModeItem;
    private JMenuItem aboutItem;
    private JMenuItem registerItem;
    private MainFrame frame;
    private Utils utils = Utils.getInstance();

    MenuBar(MainFrame frame) {
        super();
        this.frame = frame;
        initComponents();
    }

    private void initComponents() {
        // file
        menuFile = new JMenu("File");
        menuFile.setMnemonic('f');
        exitItem = new JMenuItem("Exit", new ImageIcon(utils.getResourcePath(getClass(), "exit.png")));
        exitItem.setMnemonic('x');
        menuFile.add(exitItem);
        this.add(menuFile);

        // mode
        menuMode = new JMenu("Mode");
        menuMode.setMnemonic('m');

        passiveModeItem = new JCheckBoxMenuItem("Passive Mode");
        passiveModeItem.setSelected(true);
        passiveModeItem.setMnemonic('p');
        menuMode.add(passiveModeItem);

        secureModeItem = new JCheckBoxMenuItem("Secure Mode");
        secureModeItem.setSelected(true);
        secureModeItem.setMnemonic('s');
        menuMode.add(secureModeItem);
        this.add(menuMode);

        // help
        menuHelp = new JMenu("Help");
        menuHelp.setMnemonic('h');
        aboutItem = new JMenuItem("About...");
        aboutItem.setMnemonic('a');
        menuHelp.add(aboutItem);

        registerItem = new JMenuItem("Register...");
        registerItem.setMnemonic('r');
        menuHelp.add(registerItem);
        this.add(menuHelp);

        // action
        passiveModeItem.addChangeListener(e -> frame.firePasvModeChanged(passiveModeItem.isSelected()));

        secureModeItem.addChangeListener(e -> frame.fireSecureModeChanged(secureModeItem.isSelected()));

        exitItem.addActionListener(e -> {
            int i = MessageUtils.showConfirmMessage(Constants.CONFIRM_TO_EXIT,
                    "confirm dialog");
            if (i == 0)
                System.exit(0);
        });

        aboutItem.addActionListener(e -> {
            Calendar date = Calendar.getInstance();
            String year = String.valueOf(date.get(Calendar.YEAR));
            String message = "FTPClient\nCopyright(C) 2018 - " + year + " All Rights Reserved.\nWritten by: Skye You";
            ImageIcon image = new ImageIcon(utils.getResourcePath(getClass(), "logo.png"));
            MessageUtils.showMessage(message, "about FTP", image);
        });

        registerItem.addActionListener(e -> {
            RegisterDialog dialog = new RegisterDialog(frame);
            dialog.setVisible(true);
        });
    }

    public boolean isPassiveMode() {
        return passiveModeItem.isSelected();
    }

    public boolean isSecureMode() {
        return secureModeItem.isSelected();
    }

    void afterConnect() {
        registerItem.setEnabled(false);
        secureModeItem.setEnabled(false);
    }

    void afterDisconnect(){
        registerItem.setEnabled(true);
        secureModeItem.setEnabled(true);
    }
}
