package cn.edu.shu.common.util;

import javax.swing.*;

public class MessageUtils {

    public static void showErrorMessage(String message) {
        JOptionPane.showMessageDialog(null, message);
    }

    public static void showErrorMessage(String message, String title) {
        JOptionPane.showMessageDialog(null, message, title, JOptionPane.ERROR_MESSAGE);
    }

    public static int showConfirmMessage(String message, String title) {
        return JOptionPane.showConfirmDialog(null, message, title, JOptionPane.YES_NO_OPTION);
    }

    public static void showMessage(String message, String title, Icon icon) {
        JOptionPane.showMessageDialog(null, message, title, JOptionPane.ERROR_MESSAGE, icon);
    }
}
