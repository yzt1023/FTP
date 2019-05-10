/*
 * Written By : Skye You
 * Copyright (c) 2018 - 2019. All Rights Reserved.
 */

package cn.edu.shu.common.ftp;

import cn.edu.shu.common.util.Utils;
import org.jdom2.Document;
import org.jdom2.Element;
import org.jdom2.input.SAXBuilder;

import javax.net.ssl.*;
import java.io.File;
import java.io.FileInputStream;
import java.security.KeyStore;

public class SSLContextFactory {

    public static SSLContext createSSLContext(Class clazz) {
        String path = Utils.getInstance().getResourcePath(clazz, "SSLConfig.xml");
        assert path != null;
        File file = new File(path);
        SAXBuilder builder = new SAXBuilder();
        try {
            Document document = builder.build(file);
            Element configTag = document.getRootElement();
            // root directory
            String protocol = configTag.getChildText("protocol");
            String trustCertificate = configTag.getChildText("trustCertificate");
            String trustCertificatePwd = configTag.getChildText("trustCertificatePwd");
            KeyStore keyStore = KeyStore.getInstance("JKS");
            keyStore.load(new FileInputStream(trustCertificate), trustCertificatePwd.toCharArray());
            TrustManagerFactory tmFactory = TrustManagerFactory.getInstance("SunX509");
            tmFactory.init(keyStore);
            TrustManager[] trustManagers = tmFactory.getTrustManagers();

            KeyManager[] keyManagers;
            String certificate = configTag.getChildText("certificate");
            String certificatePwd = configTag.getChildText("certificatePwd");
            String keyPwd = configTag.getChildText("keyPwd");
            keyStore = KeyStore.getInstance("JKS");
            keyStore.load(new FileInputStream(certificate), certificatePwd.toCharArray());
            KeyManagerFactory kmFactory = KeyManagerFactory.getInstance("SunX509");
            kmFactory.init(keyStore, keyPwd.toCharArray());
            keyManagers = kmFactory.getKeyManagers();

            SSLContext sslContext = SSLContext.getInstance(protocol);
            sslContext.init(keyManagers, trustManagers, null);
            return sslContext;
        } catch (Exception e) {
            e.printStackTrace();
            return null;
        }
    }
}
