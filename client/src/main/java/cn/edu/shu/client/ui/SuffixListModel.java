/*
 * Written By : Skye You
 * Copyright (c) 2018 - 2019. All Rights Reserved.
 */

package cn.edu.shu.client.ui;

import javax.swing.*;
import java.util.List;

public class SuffixListModel extends AbstractListModel {
    private List<String> suffixs;

    public SuffixListModel(List<String> suffixs) {
        this.suffixs = suffixs;
    }

    @Override
    public int getSize() {
        return suffixs.size();
    }

    @Override
    public Object getElementAt(int index) {
        return suffixs.get(index);
    }

    public boolean removeSuffix(int row) {
        if (row < 0 || row > getSize())
            return false;

        suffixs.remove(row);
        fireIntervalRemoved(this, row, row);
        return true;
    }

    public boolean addSuffix(String suffix) {
        if (suffixs.contains(suffix))
            return false;

        suffixs.add(suffix);
        fireIntervalAdded(this, suffixs.size() - 1, suffixs.size() - 1);
        return true;
    }

    public List<String> getSuffixs() {
        return suffixs;
    }

    public void setSuffixs(List<String> suffixs) {
        this.suffixs = suffixs;
    }
}
