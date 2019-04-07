/*
 * Written By : Skye You
 * Copyright (c) 2018 - 2019. All Rights Reserved.
 */

package cn.edu.shu.client.util;

import cn.edu.shu.client.ftp.FTPFile;
import cn.edu.shu.common.util.Constants;
import cn.edu.shu.common.util.Utils;

import java.text.ParseException;
import java.text.SimpleDateFormat;

public interface Helper {

    static FTPFile parseDOS(String line, FTPFile parent) {
        FTPFile file = new FTPFile(parent);
        String[] infos = line.split("\\s+");
        try {
            SimpleDateFormat format = new SimpleDateFormat("MM-dd-yyhh:mmaa");
            file.setLastChanged(format.parse(infos[0] + infos[1]));
        } catch (ParseException e) {
            e.printStackTrace();
        }
        String type = "<DIR>".equals(infos[2]) ? Constants.TYPE_DIR : Constants.TYPE_FILE;
        file.setType(type);
        if (!file.isDirectory())
            file.setSize(Long.parseLong(infos[2]));
        file.setName(infos[3]);
        file.setPath(Utils.getInstance().getPath(parent.getPath(), file.getName()));
        return file;
    }

    static FTPFile parseUNIX(String line, FTPFile parent) {
        FTPFile file = new FTPFile(parent);
        String[] infos = line.split("\\s+");
        try {
            SimpleDateFormat format = new SimpleDateFormat("EEEddyyyy");
            file.setLastChanged(format.parse(infos[5] + infos[6] + infos[7]));
        } catch (ParseException e) {
            e.printStackTrace();
        }
        String type;
        switch (infos[0].substring(0, 0)) {
            case "d":
                type = Constants.TYPE_DIR;
                break;
            case "p":
                type = Constants.TYPE_PDIR;
                break;
            case "c":
                type = Constants.TYPE_CDIR;
                break;
            default:
                type = Constants.TYPE_FILE;
        }
        file.setType(type);
        if (!file.isDirectory())
            file.setSize(Long.parseLong(infos[4]));
        file.setName(infos[8]);
        file.setPath(Utils.getInstance().getPath(parent.getPath(), file.getName()));
        return file;
    }

}
