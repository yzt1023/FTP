/*
 * Written By : Skye You
 * Copyright (c) 2018 - 2019. All Rights Reserved.
 */

package cn.edu.shu.client.ftp;

import cn.edu.shu.client.exception.ConnectionException;
import cn.edu.shu.common.encryption.RSA;
import cn.edu.shu.common.ftp.FTPCommand;
import cn.edu.shu.common.util.SecurityUtils;

import java.io.IOException;
import java.math.BigInteger;

public class SecureControlConnection extends PlainControlConnection {

    private SecurityUtils securityUtils;
    private String clientKey, serverKey;

    SecureControlConnection(FTPClient client) {
        super(client);
        securityUtils = client.getSecurityUtils();
    }

    @Override
    public void connect(String host, int port) throws ConnectionException {
        super.connect(host, port);
        sendCommand(FTPCommand.AUTH);

        try {
            // get server public key
            String key = reader.readLine();
            int index = key.indexOf(" ");
            BigInteger serverPublicKey = new BigInteger(key.substring(0, index));
            BigInteger serverN = new BigInteger(key.substring(index + 1));

            // send client public key to server
            RSA rsa = new RSA();
            BigInteger clientN = rsa.produceN();
            BigInteger clientPublicKey = rsa.producePublicKey();
            BigInteger clientPrivateKey = rsa.producePrivateKey(clientPublicKey);
            writer.println(clientPublicKey.toString() + " " + clientN.toString());

            // read aes key of server
            key = reader.readLine();
            serverKey = rsa.decrypt(clientPrivateKey, clientN, key);

            // send aes key to server
            clientKey = securityUtils.generateKey();
            String sendKey = rsa.encrypt(serverPublicKey, serverN, clientKey);
            writer.println(sendKey);

            readReply();
        } catch (IOException e) {
            throw new ConnectionException(e.getMessage(), e);
        }
    }

    @Override
    public String decodeReply(String reply) {
        if(serverKey != null)
            return securityUtils.decrypt(reply, serverKey);
        else
            return reply;
    }

    @Override
    public String encodeCommand(String command) {
        if(clientKey != null)
            return securityUtils.encrypt(command, clientKey);
        else
            return command;
    }

    @Override
    public void close() throws IOException {
        super.close();
        clientKey = null;
        serverKey = null;
    }

    public String getClientKey() {
        return clientKey;
    }

    public String getServerKey() {
        return serverKey;
    }
}
