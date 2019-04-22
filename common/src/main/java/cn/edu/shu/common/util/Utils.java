package cn.edu.shu.common.util;

import org.apache.log4j.Logger;

import javax.swing.*;
import javax.swing.filechooser.FileSystemView;
import java.awt.*;
import java.io.File;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.net.InetAddress;
import java.net.InetSocketAddress;
import java.net.URL;
import java.net.UnknownHostException;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.StringTokenizer;

public class Utils {

    private static Utils instance = new Utils();
    private final Icon FOLDER_ICON = new ImageIcon(getResourcePath(getClass(), "folder_icon.png"));
    private final Icon FILE_ICON = new ImageIcon(getResourcePath(getClass(), "file_icon.png"));
    private Logger logger;

    private Utils() {
        logger = Logger.getLogger(getClass());
    }

    public static Utils getInstance() {
        return instance;
    }

    public void reverse(Object[] objects, int start, int end) {
        Object temp;
        while (start < end) {
            temp = objects[start];
            objects[start] = objects[end];
            objects[end] = temp;
            start++;
            end--;
        }
    }

    public void setLookAndFeel() {
        Font font = new Font("Times New Roman", Font.PLAIN, 18);
        UIManager.put("Label.font", font);
        UIManager.put("Button.font", font);
        UIManager.put("MenuBar.font", font);
        UIManager.put("Menu.font", font);
        UIManager.put("TextField.font", font);
        UIManager.put("TextArea.font", font);
        UIManager.put("PasswordField.font", font);
        UIManager.put("CheckBox.font", font);
        try {
            UIManager.setLookAndFeel("com.sun.java.swing.plaf.nimbus.NimbusLookAndFeel");
            /*for (UIManager.LookAndFeelInfo info : UIManager.getInstalledLookAndFeels()) {
                if ("Nimbus".equals(info.getName())) {
                    UIManager.setLookAndFeel(info.getClassName());
                    break;
                }
            }*/
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    public String getResourcePath(Class clazz, String resourceName) {
        URL url = clazz.getClassLoader().getResource(resourceName);
        return url == null ? null : url.getPath();
    }

    public InputStream getResourceStream(Class clazz, String resourceName) {
        return clazz.getClassLoader().getResourceAsStream(resourceName);
    }

    public String formatDate(Date date) {
        SimpleDateFormat format = new SimpleDateFormat(Constants.DATE_PATTERN);
        return format.format(date);
    }

    public Date parseDate(String str) {
        SimpleDateFormat format = new SimpleDateFormat(Constants.DATE_PATTERN);
        try {
            return format.parse(str);
        } catch (ParseException e) {
            logger.error(e.getMessage(), e);
        }
        return null;
    }

    public String getPath(String parent, String name) {
        String path;
        if (parent.endsWith(Constants.SEPARATOR))
            path = parent + name;
        else
            path = parent + Constants.SEPARATOR + name;
        return path;
    }

    public String getStringByAddress(InetSocketAddress address) {
        InetAddress host = address.getAddress();
        int port = address.getPort();
        return host.getHostAddress().replace('.', ',') + ',' + (port >> 8) + ',' + (port & 255);
    }

    public InetSocketAddress getAddressByString(String str) {
        StringTokenizer st = new StringTokenizer(str, ",");
        if (st.countTokens() != 6) {
            return null;
        } else {
            String host = st.nextToken() +
                    '.' +
                    st.nextToken() +
                    '.' +
                    st.nextToken() +
                    '.' +
                    st.nextToken();
            int dataPort;
            int high = Integer.parseInt(st.nextToken());
            int low = Integer.parseInt(st.nextToken());
            dataPort = high << 8 | low;

            try {
                InetAddress address = InetAddress.getByName(host);
                return new InetSocketAddress(address, dataPort);
            } catch (UnknownHostException e) {
                logger.error(e.getMessage(), e);
                return null;
            }
        }
    }

    public boolean noConversionRequired() {
        return Constants.LINE_SEPARATOR.equals(Constants.NET_EOL);
    }

    public boolean fromNetWrite(boolean lastWasCR, OutputStream out, byte b) throws IOException {
        switch (b) {
            case '\n':
                if (lastWasCR)
                    out.write(Constants.EOL);
                else
                    out.write(b);
                lastWasCR = false;
                break;
            case '\r':
                lastWasCR = true;
                break;
            default:
                if (lastWasCR) {
                    out.write('\r');
                    lastWasCR = false;
                }
                out.write(b);
        }
        return lastWasCR;
    }

    public boolean toNetWrite(boolean lastWasCR, OutputStream out, byte b) throws IOException {
        switch (b) {
            case '\r':
                lastWasCR = true;
                out.write('\r');
                break;
            case '\n':
                if (!lastWasCR)
                    out.write('\r');
            default:
                lastWasCR = false;
                out.write(b);
        }
        return lastWasCR;
    }

    public Icon getIcon(String fileName, boolean isDirectory) {
        Icon icon;
        if (isDirectory)
            icon = FOLDER_ICON;
        else {
            try {
                File tempFile = File.createTempFile("tempfile_", fileName);
                icon = FileSystemView.getFileSystemView().getSystemIcon(tempFile);
                tempFile.delete();
            } catch (IOException e) {
                icon = FILE_ICON;
            }
        }
        return icon;
    }

}
