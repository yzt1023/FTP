/*
 * Written By : Skye You
 * Copyright (c) 2018 - 2019. All Rights Reserved.
 *
 */

package cn.edu.shu.client.util;

import cn.edu.shu.client.ui.category.FileTreeNode;
import cn.edu.shu.client.ftp.FTPFile;

import javax.swing.*;
import javax.swing.filechooser.FileSystemView;
import javax.swing.text.Position;
import javax.swing.tree.TreePath;
import java.io.File;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

public interface TreeUtils {
    FileSystemView FILE_SYSTEM_VIEW = FileSystemView.getFileSystemView();

    static int getLastMatchedRow(JTree tree, File file) {
        List<File> files = getFilePath(file);
        int i = 0, matchedRow = -1, start = getMatchedRow(tree, files.get(i), 0);
        while (start != -1 && i < files.size()) {
            matchedRow = start;
            i++;
            start = getMatchedRow(tree, files.get(i), matchedRow);
        }
        return matchedRow;
    }

    static List<File> getFilePath(File file) {
        ArrayList<File> files = new ArrayList<>();
        files.add(file);
        while ((file = FILE_SYSTEM_VIEW.getParentDirectory(file)) != null) {
            files.add(file);
        }
        Collections.reverse(files);
        return files;
    }

    static int getMatchedRow(JTree tree, File file, int startingRow) {
        int max = tree.getRowCount();
        int row = startingRow;
        do {
            TreePath path = tree.getPathForRow(row);
            FileTreeNode node = (FileTreeNode) path.getLastPathComponent();
            String text = ((File) node.getUserObject()).getPath();
            if (text.equals(file.getPath())) {
                return row;
            }
            row++;
        } while (row < max);
        return -1;
    }

    static int getNextMatch(JTree tree, String filePath, int startingRow, Position.Bias bias) {
        int max = tree.getRowCount();
        int increment = (bias == Position.Bias.Forward) ? 1 : -1;
        int row = startingRow;
        do {
            TreePath path = tree.getPathForRow(row);
            FileTreeNode node = (FileTreeNode) path.getLastPathComponent();
            String text = ((FTPFile) node.getUserObject()).getPath();
            if (filePath.startsWith(text)) {
                return row;
            }
            row = (row + increment + max) % max;
        } while (row != startingRow);
        return -1;
    }

    static int getLastMatchedRow(JTree tree, String filePath) {
        return getNextMatch(tree, filePath, tree.getRowCount() - 1, Position.Bias.Backward);
    }

}
