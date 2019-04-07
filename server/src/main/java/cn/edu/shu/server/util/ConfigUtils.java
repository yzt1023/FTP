package cn.edu.shu.server.util;

import cn.edu.shu.common.util.Utils;
import org.jdom2.Document;
import org.jdom2.Element;
import org.jdom2.JDOMException;
import org.jdom2.input.SAXBuilder;

import java.io.File;
import java.io.IOException;

public class ConfigUtils {
    private static ConfigUtils instance = new ConfigUtils();
    private String rootPath;
    private int loginTimeout;
    private int cnctTimeout;
    private String welcomeMessage;

    public void initConfig() {
        String path = Utils.getInstance().getResourcePath(ConfigUtils.class, "server.xml");
        assert path != null;
        File file = new File(path);
        SAXBuilder builder = new SAXBuilder();
        try {
            Document document = builder.build(file);
            Element configTag = document.getRootElement();
            // root directory
            rootPath = configTag.getChildText("rootDir");
            /*Element usersTag = configTag.getChild("users");
            List<Element> userTags = usersTag.getChildren();
            // user
            for(Element userTag : userTags){
                users.put(userTag.getChildText("username"), userTag.getChildText("password"));
            }*/
            // settings
            Element settingTag = configTag.getChild("settings");
            loginTimeout = Integer.parseInt(settingTag.getChildText("loginTimeout"));
            cnctTimeout = Integer.parseInt(settingTag.getChildText("connectionTimeout"));
            welcomeMessage = settingTag.getChildText("welcomeMessage");
        } catch (JDOMException | IOException e) {
            e.printStackTrace();
        }
    }

    public static ConfigUtils getInstance() {
        return instance;
    }

    public String getRootPath() {
        return rootPath;
    }

    public int getLoginTimeout() {
        return loginTimeout;
    }

    public int getCnctTimeout() {
        return cnctTimeout;
    }

    public String getWelcomeMessage() {
        return welcomeMessage;
    }
}
