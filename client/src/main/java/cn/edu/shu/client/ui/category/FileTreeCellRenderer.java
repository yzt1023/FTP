/*
 * Written By : Skye You
 * Copyright (c) 2018 - 2019. All Rights Reserved.
 */

package cn.edu.shu.client.ui.category;

import cn.edu.shu.client.ftp.FTPFile;

import javax.swing.*;
import javax.swing.filechooser.FileSystemView;
import javax.swing.tree.DefaultTreeCellRenderer;
import java.awt.*;
import java.io.File;

public class FileTreeCellRenderer extends DefaultTreeCellRenderer {

    private FileSystemView fileSystemView;

    FileTreeCellRenderer() {
        super();
        fileSystemView = FileSystemView.getFileSystemView();
    }

    @Override
    public Component getTreeCellRendererComponent(JTree tree, Object value, boolean selected, boolean expanded, boolean leaf, int row, boolean hasFocus) {

        Component component = super.getTreeCellRendererComponent(tree, value, selected, expanded, leaf, row, hasFocus);

        FileTreeNode node = (FileTreeNode) value;
        Object object = node.getUserObject();
        if (object instanceof File) {
            File file = (File) node.getUserObject();
            setIcon(fileSystemView.getSystemIcon(file));
            setText(fileSystemView.getSystemDisplayName(file));
        } else if (object instanceof FTPFile) {
            FTPFile ftpFile = (FTPFile) node.getUserObject();
            setIcon(ftpFile.getIcon());
            setText(ftpFile.getName());
        }

        return component;
    }
}
