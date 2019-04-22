package cn.edu.shu.server;

import cn.edu.shu.common.util.Utils;
import cn.edu.shu.server.ui.MainFrame;

import javax.swing.*;

public class ShellApplication {
    public static void main(String[] args) {

        Utils.getInstance().setLookAndFeel();

        // display cn.edu.shu.ui
        SwingUtilities.invokeLater(() -> {
            MainFrame mainFrame = new MainFrame();
            mainFrame.setVisible(true);
        });
    }
}
