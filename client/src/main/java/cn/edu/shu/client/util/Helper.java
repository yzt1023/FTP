/*
 * Written By : Skye You
 * Copyright (c) 2018 - 2019. All Rights Reserved.
 */

package cn.edu.shu.client.util;

import cn.edu.shu.client.ftp.FTPFile;

import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.Date;

public interface Helper {
    static FTPFile parseMLSD(String line, FTPFile parent) {
        FTPFile file = new FTPFile(parent);
        SimpleDateFormat parseDate = new SimpleDateFormat(Constants.DATE_PATTERN);
        try {
            Date date = parseDate.parse(getValue(line, Constants.KEY_MODIFY));
            file.setLastChanged(date);
        } catch (ParseException e) {
            e.printStackTrace();
        }
        file.setType(getValue(line, Constants.KEY_TYPE));
        if (!file.isDirectory())
            file.setSize(Long.parseLong(getValue(line, Constants.KEY_SIZE)));
        file.setName(line.substring(line.lastIndexOf(";") + 2, line.length()));
        String path = parent.getPath();
        if (path.endsWith(Constants.SEPARATOR))
            file.setPath(path + file.getName());
        else
            file.setPath(path + Constants.SEPARATOR + file.getName());
        return file;
    }

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
        String path = parent.getPath();
        if (path.endsWith(Constants.SEPARATOR))
            file.setPath(path + file.getName());
        else
            file.setPath(path + Constants.SEPARATOR + file.getName());
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
        String path = parent.getPath();
        if (path.endsWith(Constants.SEPARATOR))
            file.setPath(path + file.getName());
        else
            file.setPath(path + Constants.SEPARATOR + file.getName());
        return file;
    }

    static String getValue(String line, String key) {
        int index = line.indexOf(key);
        return line.substring(index + key.length() + 1, line.indexOf(';', index));
    }

    static void reverse(Object[] objects, int start, int end) {
        Object temp;
        while (start < end) {
            temp = objects[start];
            objects[start] = objects[end];
            objects[end] = temp;
            start++;
            end--;
        }
    }

    static String generateSuffix() {
        SimpleDateFormat format = new SimpleDateFormat(Constants.DATE_PATTERN);
        return format.format(new Date());
    }
}
