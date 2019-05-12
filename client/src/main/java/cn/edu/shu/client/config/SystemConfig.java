/*
 * Written By : Skye You
 * Copyright (c) 2018 - 2019. All Rights Reserved.
 */

package cn.edu.shu.client.config;

import cn.edu.shu.common.util.CommonUtils;
import org.jdom2.Document;
import org.jdom2.Element;
import org.jdom2.JDOMException;
import org.jdom2.input.SAXBuilder;

import java.io.File;
import java.io.IOException;

public class SystemConfig {

    private String encoding;
    private boolean defaultPassive;
    private boolean defaultSecure;
    private int socketTimeout;
    private static SystemConfig instance = new SystemConfig();

    private SystemConfig(){

    }

    public void initConfig(){
        File file = new File(CommonUtils.getInstance().getResourcePath(getClass(), "client.xml"));
        SAXBuilder builder = new SAXBuilder();
        Document document = null;
        try {
            document = builder.build(file);
            Element configTag = document.getRootElement();
            encoding = configTag.getChildText("encoding");
            String temp = configTag.getChildText("defaultPassive");
            defaultPassive = "true".equals(temp.toLowerCase());
            temp = configTag.getChildText("defaultSecure");
            defaultSecure = "true".equals(temp.toLowerCase());
            temp = configTag.getChildText("socketTimeout");
            socketTimeout = Integer.parseInt(temp);
        } catch (JDOMException | IOException e) {
            e.printStackTrace();
        }
    }

    public String getEncoding() {
        return encoding;
    }

    public boolean isDefaultPassive() {
        return defaultPassive;
    }

    public boolean isDefaultSecure() {
        return defaultSecure;
    }

    public int getSocketTimeout() {
        return socketTimeout;
    }

    public static SystemConfig getInstance() {
        return instance;
    }
}
