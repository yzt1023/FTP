package cn.edu.shu.common.util;

import javax.swing.*;
import java.net.URL;

public class Utils {

    public static void setLookAndInfo() {
        try {
            for (UIManager.LookAndFeelInfo info : UIManager.getInstalledLookAndFeels()) {
                if ("Nimbus".equals(info.getName())) {
                    UIManager.setLookAndFeel(info.getClassName());
                    break;
                }
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    public static String getResourcePath(Class clazz, String resourceName){
        URL url = clazz.getClassLoader().getResource(resourceName);
        return url == null? null : url.getPath();
    }
}
