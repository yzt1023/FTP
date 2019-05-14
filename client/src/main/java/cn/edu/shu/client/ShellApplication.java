package cn.edu.shu.client;

import cn.edu.shu.client.ui.MainFrame;
import cn.edu.shu.common.util.CommonUtils;

import javax.swing.*;

public class ShellApplication {
    public static void main(String[] args) {

        CommonUtils.getInstance().setLookAndFeel();
        String dir = System.getProperty("user.dir");
        System.setProperty("project", dir);

        // display cn.edu.shu.ui
        SwingUtilities.invokeLater(() -> {
            MainFrame frame = new MainFrame();
            frame.setVisible(true);
        });
    }
}