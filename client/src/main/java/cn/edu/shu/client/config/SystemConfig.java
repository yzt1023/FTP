/*
 * Written By : Skye You
 * Copyright (c) 2018 - 2019. All Rights Reserved.
 */

package cn.edu.shu.client.config;

import cn.edu.shu.common.bean.DataType;
import org.apache.log4j.Logger;
import org.dom4j.Document;
import org.dom4j.DocumentException;
import org.dom4j.DocumentHelper;
import org.dom4j.Element;
import org.dom4j.io.OutputFormat;
import org.dom4j.io.SAXReader;
import org.dom4j.io.XMLWriter;

import java.io.File;
import java.io.FileOutputStream;
import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;

public class SystemConfig {

    private static SystemConfig instance = new SystemConfig();
    private boolean defaultPassive;
    private boolean defaultSecure;
    private String encoding;
    private int activeMinPort;
    private int activeMaxPort;
    private String dataType;
    private List<String> suffixs;
    private File file;
    private Logger logger = Logger.getLogger(getClass());

    private SystemConfig() {
        file = new File("client/src/main/resources/client.xml");
    }

    public static SystemConfig getInstance() {
        return instance;
    }

    public void initConfig() {
        SAXReader reader = new SAXReader();
        Document document;
        try {
            document = reader.read(file);
            Element configTag = document.getRootElement();
            Element defaultTag = configTag.element("default");
            String temp = defaultTag.elementText("passive");
            defaultPassive = "true".equals(temp.toLowerCase());
            temp = defaultTag.elementText("secure");
            defaultSecure = "true".equals(temp.toLowerCase());

            Element settingsTag = configTag.element("settings");
            encoding = settingsTag.elementText("encoding");
            activeMinPort = Integer.parseInt(settingsTag.elementText("activeMinPort"));
            activeMaxPort = Integer.parseInt(settingsTag.elementText("activeMaxPort"));

            dataType = settingsTag.elementText("transferMode");
            Element asciiConfig = settingsTag.element("defaultAscii");
            Iterator iterator = asciiConfig.elementIterator("dataType");
            suffixs = new ArrayList<>();
            while(iterator.hasNext()){
                Element item = (Element) iterator.next();
                suffixs.add(item.getText());
            }
        } catch (DocumentException e) {
            logger.error(e.getMessage(), e);
        }
    }

    public boolean updateConfig() {
        Document document = DocumentHelper.createDocument();
        Element rootElement = document.addElement("root");
        // default
        Element defaultElement = rootElement.addElement("default");
        Element element = defaultElement.addElement("passive");
        element.setText(defaultPassive + "");
        element = defaultElement.addElement("secure");
        element.setText(defaultSecure + "");
        // settings
        Element settingsElement = rootElement.addElement("settings");
        element = settingsElement.addElement("encoding");
        element.setText(encoding);
        element = settingsElement.addElement("activeMinPort");
        element.setText(activeMinPort + "");
        element = settingsElement.addElement("activeMaxPort");
        element.setText(activeMaxPort + "");
        element = settingsElement.addElement("transferMode");
        element.setText(dataType);
        Element asciiElement = settingsElement.addElement("defaultAscii");
        for(String suffix : suffixs){
            element = asciiElement.addElement("dataType");
            element.setText(suffix);
        }

        try {
            FileOutputStream out = new FileOutputStream(file);
            OutputFormat format = OutputFormat.createPrettyPrint();
            format.setEncoding("utf-8");
            XMLWriter writer = new XMLWriter(out, format);
            writer.write(document);
            writer.close();
        } catch (Exception e) {
            logger.error(e.getMessage(), e);
            return false;
        }

        return true;
    }

    public String getEncoding() {
        return encoding;
    }

    public void setEncoding(String encoding) {
        this.encoding = encoding;
    }

    public boolean isDefaultPassive() {
        return defaultPassive;
    }

    public boolean isDefaultSecure() {
        return defaultSecure;
    }

    public int getActiveMinPort() {
        return activeMinPort;
    }

    public void setActiveMinPort(int activeMinPort) {
        this.activeMinPort = activeMinPort;
    }

    public int getActiveMaxPort() {
        return activeMaxPort;
    }

    public void setActiveMaxPort(int activeMaxPort) {
        this.activeMaxPort = activeMaxPort;
    }

    public String getDataType() {
        return dataType;
    }

    public void setDataType(String dataType) {
        this.dataType = dataType;
    }

    public List<String> getSuffixs() {
        return suffixs;
    }

    public void setSuffixs(List<String> suffixs) {
        this.suffixs = suffixs;
    }

    public DataType getFileDataType(String filename){
        if("AUTO".equals(dataType)) {
            int index = filename.lastIndexOf('.');
            if (index == -1)
                return DataType.BINARY;

            String suffix = filename.substring(index + 1);
            if (suffixs.contains(suffix))
                return DataType.ASCII;

            return DataType.BINARY;
        }else if ("ASCII".equals(dataType)){
            return DataType.ASCII;
        }else{
            return DataType.BINARY;
        }
    }
}
