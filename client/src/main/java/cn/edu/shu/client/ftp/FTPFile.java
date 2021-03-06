package cn.edu.shu.client.ftp;

import cn.edu.shu.common.util.CommonUtils;
import cn.edu.shu.common.util.Constants;

import javax.swing.*;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;

public class FTPFile {
    private String name;
    private long size;
    private Date lastChanged;
    private String type;
    private boolean isDirectory;
    private Icon icon;
    private String path;
    private FTPFile parent;
    private List<FTPFile> children;

    public FTPFile(){}

    public FTPFile(String path){
        this.path = path;
    }

    public FTPFile(FTPFile parent, String name){
        this.parent = parent;
        this.name = name;
    }

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
        isDirectory = type.equals(Constants.FILE_FOLDER) || type.equals(Constants.SYSTEM_FOLDER) || type.contains(Constants.TYPE_DIR);
    }

    public Icon getIcon() {
        if(icon == null) {
            icon = CommonUtils.getInstance().getIcon(name, isDirectory);
        }
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

    public List<FTPFile> getChildren() {
        return children;
    }

    public void setChildren(List<FTPFile> children) {
        this.children = children;
    }

    public FTPFile getChild(String name){
        for(FTPFile file : children)
            if(name.toUpperCase().equals(file.getName().toUpperCase()))
                return file;
        return null;
    }

    public void addChild(FTPFile file){
        if(children == null){
            children = new ArrayList<>();
        }
        children.add(file);
    }

    public void removeChild(FTPFile file){
        children.remove(file);
    }

}
