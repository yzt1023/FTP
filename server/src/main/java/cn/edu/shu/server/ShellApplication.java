package cn.edu.shu.server;

import cn.edu.shu.common.util.Utils;
import cn.edu.shu.server.ui.MainFrame;

import javax.swing.*;
import java.awt.*;

public class ShellApplication {
    public static void main(String[] args) {
        // change style
        UIManager.put("TextArea.font", new Font("宋体", Font.PLAIN, 18));
        UIManager.put("Menu.font", new Font("宋体", Font.PLAIN, 18));

        Utils.getInstance().setLookAndFeel();

        // display cn.edu.shu.ui
        SwingUtilities.invokeLater(() -> {
            MainFrame mainFrame = new MainFrame();
            mainFrame.setVisible(true);
        });
    }
}
