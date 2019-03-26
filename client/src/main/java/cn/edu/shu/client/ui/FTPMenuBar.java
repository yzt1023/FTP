/*
 * Written By : Skye You
 * Copyright (c) 2018 - 2019. All Rights Reserved.
 */

package cn.edu.shu.client.ui;

import cn.edu.shu.client.listener.ModeListener;
import cn.edu.shu.common.util.Utils;
import cn.edu.shu.client.util.Constants;
import cn.edu.shu.common.util.MessageUtils;

import javax.swing.*;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.util.Calendar;

public class FTPMenuBar extends JMenuBar {

    private JMenu menuFile;
    private JMenu menuMode;
    private JMenu menuHelp;
    private JMenuItem exitItem;
    private JCheckBoxMenuItem passiveModeItem;
    private JCheckBoxMenuItem secureModeItem;
    private JMenuItem aboutItem;
    private ModeListener modelistener;

    FTPMenuBar(ModeListener modeListener) {
        super();
        this.modelistener = modeListener;
        initComponents();
    }

    private void initComponents() {
        // file
        menuFile = new JMenu("File");
        menuFile.setMnemonic('f');
        exitItem = new JMenuItem("Exit", new ImageIcon(Utils.getResourcePath(getClass(), "exit")));
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
        secureModeItem.setMnemonic('s');
        menuMode.add(secureModeItem);
        this.add(menuMode);

        // help
        menuHelp = new JMenu("Help");
        menuHelp.setMnemonic('h');
        aboutItem = new JMenuItem("About");
        aboutItem.setMnemonic('a');
        menuHelp.add(aboutItem);
        this.add(menuHelp);

        // action
        passiveModeItem.addActionListener(new ActionListener() {
            @Override
            public void actionPerformed(ActionEvent e) {
                modelistener.firePasvModeChanged(passiveModeItem.isSelected());
            }
        });


        exitItem.addActionListener(new ActionListener() {
            @Override
            public void actionPerformed(ActionEvent e) {
                int i = MessageUtils.showConfirmMessage(Constants.CONFIRM_TO_EXIT,
                        "confirm dialog");
                if (i == 0)
                    System.exit(0);
            }
        });

        aboutItem.addActionListener(new ActionListener() {
            @Override
            public void actionPerformed(ActionEvent e) {
                Calendar date = Calendar.getInstance();
                String year = String.valueOf(date.get(Calendar.YEAR));
                String message = "FTPClient\nCopyright(C) 2018 - " + year + " All Rights Reserved.\nWritten by: Skye You";
                ImageIcon image = new ImageIcon(Utils.getResourcePath(getClass(), "logo.png"));
                MessageUtils.showMessage(message, "about FTP", image);
            }
        });
    }

    public boolean isPassiveMode() {
        return passiveModeItem.isSelected();
    }

    public boolean isSecureMode() {
        return secureModeItem.isSelected();
    }
}
