/*
 * Written By : Skye You
 * Copyright (c) 2018 - 2019. All Rights Reserved.
 */

package cn.edu.shu.client.ui.category;

import cn.edu.shu.client.ftp.FTPFile;

import javax.swing.tree.DefaultMutableTreeNode;
import java.util.Enumeration;

public class FileTreeNode extends DefaultMutableTreeNode {
    private boolean firstExpand;

    FileTreeNode(Object userObject){
        super(userObject);
        firstExpand = true;
    }

    boolean isFirstExpand() {
        return firstExpand;
    }

    void setFirstExpand(boolean firstExpand) {
        this.firstExpand = firstExpand;
    }

    FileTreeNode getChild(String pathname){
        for(Enumeration e = children(); e.hasMoreElements(); ){
            FileTreeNode node = (FileTreeNode) e.nextElement();
            FTPFile file = (FTPFile)node.getUserObject();
            if(file.getPath().equals(pathname))
                return node;
        }
        return null;
    }

}
