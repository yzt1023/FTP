package cn.edu.shu.client;

import cn.edu.shu.common.util.Utils;
import cn.edu.shu.client.ui.MainFrame;

import javax.swing.*;
import java.awt.*;

public class ShellApplication {
    public static void main(String[] args) {
        // change style
        UIManager.put("Label.font", new Font("宋体", Font.PLAIN, 18));
        UIManager.put("Button.font", new Font("宋体", Font.PLAIN, 18));
        UIManager.put("Menu.font", new Font("宋体", Font.PLAIN, 18));
        UIManager.put("TextField.font", new Font("宋体", Font.PLAIN, 18));
        UIManager.put("PasswordField.font", new Font("宋体", Font.PLAIN, 18));
        UIManager.put("CheckBox.font", new Font("宋体", Font.PLAIN, 18));

        Utils.setLookAndInfo();

        // display cn.edu.shu.ui
        SwingUtilities.invokeLater(new Runnable() {
            public void run() {
                MainFrame frame = new MainFrame();
                frame.setVisible(true);
            }
        });
    }
}