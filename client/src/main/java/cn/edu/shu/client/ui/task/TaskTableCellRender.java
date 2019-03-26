/*
 * Written By : Skye You
 * Copyright (c) 2018 - 2019. All Rights Reserved.
 */

package cn.edu.shu.client.ui.task;

import javax.swing.*;
import javax.swing.table.TableCellRenderer;
import java.awt.*;

public class TaskTableCellRender extends JProgressBar implements TableCellRenderer {

    TaskTableCellRender() {
        super();
        this.setOpaque(true);
    }

    @Override
    public Component getTableCellRendererComponent(JTable table, Object value, boolean isSelected, boolean hasFocus, int row, int column) {
        if (table == null) {
            return this;
        }

        // set background color and foreground color
        if (isSelected) {
            super.setBackground(table.getSelectionBackground());
        } else {
            super.setBackground(table.getBackground());
        }

        // set progress of the progress bar
        Integer progress = (Integer) value;
        setStringPainted(true);
        setValue(progress);
        return this;
    }
}
