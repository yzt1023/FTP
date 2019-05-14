package cn.edu.shu.server.config;

import org.apache.log4j.Logger;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.IOException;
import java.util.Properties;

public class SystemConfig {
    private static SystemConfig instance = new SystemConfig();
    private String rootPath;
    private String encoding;
    private String welcomeMessage;
    private int timeout;
    private int passiveMinPort;
    private int passiveMaxPort;
    private File file;
    private Logger logger = Logger.getLogger(getClass());
    private Properties properties;

    private SystemConfig() {
        file = new File("server/src/main/resources/server.properties");
    }

    public static SystemConfig getInstance() {
        return instance;
    }

    public void initConfig() {
        properties = new Properties();

        try {
            properties.load(new FileInputStream(file));
        } catch (IOException e) {
            logger.error(e.getMessage(), e);
        }

        rootPath = properties.getProperty("rootDir");
        encoding = properties.getProperty("encoding");
        welcomeMessage = properties.getProperty("welcomeMessage");
        timeout = Integer.parseInt(properties.getProperty("timeout"));
        passiveMaxPort = Integer.parseInt(properties.getProperty("passiveMaxPort"));
        passiveMinPort = Integer.parseInt(properties.getProperty("passiveMinPort"));
    }

    public boolean updateConfig() {
        try {
            FileOutputStream os = new FileOutputStream(file, false);
            properties.setProperty("rootDir", rootPath);
            properties.setProperty("encoding", encoding);
            properties.setProperty("welcomeMessage", welcomeMessage);
            properties.setProperty("timeout", timeout + "");
            properties.setProperty("passiveMinPort", passiveMinPort + "");
            properties.setProperty("passiveMaxPort", passiveMaxPort + "");
            properties.store(os, "settings");
        } catch (Exception e) {
            logger.error(e.getMessage(), e);
            return false;
        }
        return true;
    }

    public String getRootPath() {
        return rootPath;
    }

    public void setRootPath(String rootPath) {
        this.rootPath = rootPath;
    }

    public String getEncoding() {
        return encoding;
    }

    public void setEncoding(String encoding) {
        this.encoding = encoding;
    }

    public int getPassiveMinPort() {
        return passiveMinPort;
    }

    public void setPassiveMinPort(int passiveMinPort) {
        this.passiveMinPort = passiveMinPort;
    }

    public int getPassiveMaxPort() {
        return passiveMaxPort;
    }

    public void setPassiveMaxPort(int passiveMaxPort) {
        this.passiveMaxPort = passiveMaxPort;
    }

    public int getTimeout() {
        return timeout;
    }

    public void setTimeout(int timeout) {
        this.timeout = timeout;
    }

    public String getWelcomeMessage() {
        return welcomeMessage;
    }

    public void setWelcomeMessage(String welcomeMessage) {
        this.welcomeMessage = welcomeMessage;
    }
}
