/*
 * Written By : Skye You
 * Copyright (c) 2018 - 2019. All Rights Reserved.
 */

package cn.edu.shu.server.config;

import cn.edu.shu.common.util.CommonUtils;
import cn.edu.shu.server.db.DBConnPool;
import org.apache.log4j.Logger;

import java.util.Properties;

public class DBConfig {
    private static DBConfig instance = new DBConfig();
    private Logger logger = Logger.getLogger(getClass());
    private String driver;
    private String url;
    private String username;
    private String password;

    private DBConfig(){
        try {
            Properties properties = new Properties();
            properties.load(CommonUtils.getInstance().getResourceStream(DBConnPool.class, "db.properties"));
            driver = properties.getProperty("driver");
            url = properties.getProperty("url");
            username = properties.getProperty("username");
            password = properties.getProperty("password");
        }catch (Exception e){
            logger.error(e.getMessage(), e);

        }
    }

    public static DBConfig getInstance() {
        return instance;
    }

    public String getDriver() {
        return driver;
    }

    public String getUrl() {
        return url;
    }

    public String getUsername() {
        return username;
    }

    public String getPassword() {
        return password;
    }
}
