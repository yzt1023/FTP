/*
 * Written By : Skye You
 * Copyright (c) 2018 - 2019. All Rights Reserved.
 */

package cn.edu.shu.server.command.impl;

import cn.edu.shu.common.encryption.RSA;
import cn.edu.shu.common.ftp.FTPReplyCode;
import cn.edu.shu.server.command.Command;
import cn.edu.shu.server.ftp.FTPRequest;
import cn.edu.shu.server.ftp.FTPSession;

import java.math.BigInteger;

public class AUTH implements Command {

    public AUTH() {
    }

    @Override
    public void execute(FTPSession session, FTPRequest request) {
        session.resetState();
        if(request.hasArgument()){
            session.println(FTPReplyCode.INVALID_PARAMETER.getReply());
            return;
        }

        // send server public key to client
        RSA rsa = new RSA();
        BigInteger serverN = rsa.produceN();
        BigInteger serverPublicKey = rsa.producePublicKey();
        BigInteger serverPrivateKey = rsa.producePrivateKey(serverPublicKey);
        session.sendLine(serverPublicKey.toString() + " " + serverN.toString());

        // get public key of client
        BigInteger clientPublicKey, clientN;
        String req = session.readLine();
        int index = req.indexOf(" ");
        clientPublicKey = new BigInteger(req.substring(0, index));
        clientN = new BigInteger(req.substring(index + 1));

        session.generateKey();
        req = rsa.encrypt(clientPublicKey, clientN, session.getServerKey());
        session.sendLine(req);

        // get aes key of client
        req = session.readLine();
        req = rsa.decrypt(serverPrivateKey, serverN, req);
        session.setClientKey(req);
        session.setSecureMode(true);

        session.println(FTPReplyCode.COMMAND_OK + " Secure channel is open");
    }
}
