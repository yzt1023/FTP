package cn.edu.shu.client.ftp;

import cn.edu.shu.common.util.Utils;
import cn.edu.shu.common.util.Constants;

import javax.swing.*;
import java.util.Date;

public class FTPFile {
    private String name;
    private long size;
    private Date lastChanged;
    private String type;
    private boolean isDirectory;
    private Icon icon;
    private String path;
    private FTPFile parent;
    private FTPFile[] children;

    public FTPFile(){}

    public FTPFile(String path){
        this.path = path;
    }

    public FTPFile(FTPFile parent){
        this.parent = parent;
    }

    // TODO: 2019/3/13 permission(read/write/execute)
    // TODO: 2019/3/13 access(group/user/world)

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public long getSize() {
        return size;
    }

    public void setSize(long size) {
        this.size = size;
    }

    public boolean isDirectory() {
        return isDirectory;
    }

    public Date getLastChanged() {
        return lastChanged;
    }

    public void setLastChanged(Date lastChanged) {
        this.lastChanged = lastChanged;
    }

    public String getType() {
        return type;
    }

    public void setType(String type) {
        this.type = type;
        if(type.equals(Constants.FILE_FOLDER) || type.equals(Constants.SYSTEM_FOLDER)) {
            isDirectory = true;
            icon = new ImageIcon(Utils.getInstance().getResourcePath(getClass(), "folder_icon.png"));
        }else{
            isDirectory = false;
            icon = new ImageIcon(Utils.getInstance().getResourcePath(getClass(), "file_icon.png"));
        }
    }

    public Icon getIcon() {
        return icon;
    }

    public String getPath() {
        return path;
    }

    public void setPath(String path) {
        this.path = path;
    }

    public FTPFile getParent() {
        return parent;
    }

    public void setParent(FTPFile parent) {
        this.parent = parent;
    }

    public FTPFile[] getChildren() {
        return children;
    }

    public void setChildren(FTPFile[] children) {
        this.children = children;
    }

    public FTPFile getChild(String name){
        for(FTPFile file : children)
            if(name.toUpperCase().equals(file.getName().toUpperCase()))
                return file;
        return null;
    }
}
