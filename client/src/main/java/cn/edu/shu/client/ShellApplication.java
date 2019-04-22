package cn.edu.shu.client;

import cn.edu.shu.client.ui.MainFrame;
import cn.edu.shu.common.util.Utils;

import javax.swing.*;

public class ShellApplication {
    public static void main(String[] args) {

        Utils.getInstance().setLookAndFeel();

        // display cn.edu.shu.ui
        SwingUtilities.invokeLater(() -> {
            MainFrame frame = new MainFrame();
            frame.setVisible(true);
        });
    }
}