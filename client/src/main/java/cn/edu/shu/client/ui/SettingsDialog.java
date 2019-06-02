/*
 * Written By : Skye You
 * Copyright (c) 2018 - 2019. All Rights Reserved.
 */

package cn.edu.shu.client.ui;

import cn.edu.shu.client.config.SystemConfig;
import cn.edu.shu.common.util.Constants;
import cn.edu.shu.common.util.MessageUtils;

import javax.swing.*;
import java.awt.*;
import java.nio.charset.Charset;
import java.nio.charset.UnsupportedCharsetException;

class SettingsDialog extends JDialog {

    // encoding
    private JLabel lblEncoding;
    private JTextField txtEncoding;

    // port
    private JLabel lblPort;
    private JTextField txtMinPort;
    private JLabel lblTo;
    private JTextField txtMaxPort;
    private JLabel lblRange;

    // transfer data type
    private JLabel lblType;
    private JRadioButton rbtnAuto;
    private JRadioButton rbtnAscii;
    private JRadioButton rbtnBinary;

    private JSeparator separator;

    private JLabel lblAscii;
    private JList list;
    private SuffixListModel listModel;
    private JScrollPane scrollList;

    private JLabel lblFile;
    private JTextField txtFile;
    private JButton btnAdd;
    private JButton btnRemove;

    private JButton btnSave;
    private JButton btnCancel;

    private SystemConfig config = SystemConfig.getInstance();

    SettingsDialog(Frame frame) {
        super(frame, ModalityType.APPLICATION_MODAL);
        this.setSize(600, 500);
        this.setLocation(800, 300);
        this.setTitle("Settings Dialog");

        initComponents();
        addActions();
        setGroupLayout();
    }

    private void initComponents() {
        lblEncoding = new JLabel("Connection charset:");
        txtEncoding = new JTextField(config.getEncoding());

        lblPort = new JLabel("Passive port range:");
        txtMinPort = new JTextField(config.getActiveMinPort() + "");
        lblTo = new JLabel("-");
        txtMaxPort = new JTextField(config.getActiveMaxPort() + "");
        lblRange = new JLabel("(40000 - 65535)");

        lblType = new JLabel("Transfer data Type:");
        rbtnAuto = new JRadioButton("AUTO");
        rbtnAscii = new JRadioButton("ASCII");
        rbtnBinary = new JRadioButton("BINARY");
        ButtonGroup group = new ButtonGroup();
        group.add(rbtnAuto);
        group.add(rbtnAscii);
        group.add(rbtnBinary);

        if ("AUTO".equals(config.getDataType())) {
            rbtnAuto.setSelected(true);
        } else if ("ASCII".equals(config.getDataType())) {
            rbtnAscii.setSelected(true);
        } else {
            rbtnBinary.setSelected(true);
        }

        separator = new JSeparator();
        lblAscii = new JLabel("default extensions as ASCII type");

        listModel = new SuffixListModel(config.getSuffixs());
        list = new JList(listModel);
        list.setSelectionMode(ListSelectionModel.SINGLE_SELECTION);
        scrollList = new JScrollPane(list);

        lblFile = new JLabel("customize ASCII type");
        txtFile = new JTextField();
        btnAdd = new JButton("Add");
        btnRemove = new JButton("Remove");

        btnSave = new JButton("Save");
        btnCancel = new JButton("Cancel");
    }

    private void addActions() {
        btnCancel.addActionListener(e -> this.dispose());

        btnRemove.addActionListener(e -> {
            int index = list.getSelectedIndex();
            if (index == -1) {
                MessageUtils.showInfoMessage(Constants.NO_ITEM_SELECTED);
            } else if (listModel.removeSuffix(index)) {
                MessageUtils.showInfoMessage(Constants.REMOVE_SUCCEED);
            } else {
                MessageUtils.showInfoMessage(Constants.REMOVE_FAILED);
            }
        });

        btnAdd.addActionListener(e -> {
            String suffix = txtFile.getText();
            if (suffix.isEmpty()) {
                MessageUtils.showInfoMessage(Constants.EMPTY_INPUT);
            } else if (listModel.addSuffix(suffix)) {
                MessageUtils.showInfoMessage(Constants.ADD_SUCCEED);
                txtFile.setText("");
            } else {
                MessageUtils.showInfoMessage(Constants.ADD_FAILED);
            }
        });

        btnSave.addActionListener(e -> {
            String encoding = txtEncoding.getText();
            String minPort = txtMinPort.getText();
            String maxPort = txtMaxPort.getText();
            if (encoding.isEmpty() || minPort.isEmpty() || maxPort.isEmpty()) {
                MessageUtils.showInfoMessage(Constants.EMPTY_INPUT);
                return;
            }

            try {
                Charset.forName(encoding);
            } catch (UnsupportedCharsetException exception) {
                MessageUtils.showInfoMessage(Constants.CHARSET_NOT_EXISTS);
                return;
            }

            int min, max;
            try {
                min = Integer.parseInt(minPort);
                max = Integer.parseInt(maxPort);
                if (min < 40000 || max > 65535 || min > max) {
                    MessageUtils.showInfoMessage(Constants.PORT_INCORRECT);
                    return;
                }
            } catch (NumberFormatException exception) {
                MessageUtils.showInfoMessage(Constants.NUMBER_INCORRECT);
                return;
            }

            String dataType;
            if (rbtnAuto.isSelected())
                dataType = rbtnAuto.getText();
            else if (rbtnAscii.isSelected())
                dataType = rbtnAscii.getText();
            else
                dataType = rbtnBinary.getText();

            config.setEncoding(encoding);
            config.setActiveMinPort(min);
            config.setActiveMaxPort(max);
            config.setDataType(dataType);
            config.setSuffixs(listModel.getSuffixs());

            if (config.updateConfig()) {
                MessageUtils.showInfoMessage(Constants.SAVE_SUCCEED);
                this.dispose();
            } else {
                MessageUtils.showInfoMessage(Constants.SAVE_FAILED);
            }
        });
    }

    private void setGroupLayout() {
        JPanel panel = new JPanel();
        this.setContentPane(panel);
        GroupLayout layout = new GroupLayout(panel);
        panel.setLayout(layout);

        GroupLayout.ParallelGroup para1 = layout.createParallelGroup();
        para1.addComponent(lblEncoding);
        para1.addComponent(lblPort);
        para1.addComponent(lblType);

        GroupLayout.SequentialGroup seq1 = layout.createSequentialGroup();
        seq1.addComponent(txtMinPort, GroupLayout.PREFERRED_SIZE, 80, GroupLayout.PREFERRED_SIZE);
        seq1.addPreferredGap(LayoutStyle.ComponentPlacement.RELATED);
        seq1.addComponent(lblTo);
        seq1.addPreferredGap(LayoutStyle.ComponentPlacement.RELATED);
        seq1.addComponent(txtMaxPort, GroupLayout.PREFERRED_SIZE, 80, GroupLayout.PREFERRED_SIZE);
        seq1.addPreferredGap(LayoutStyle.ComponentPlacement.RELATED);
        seq1.addComponent(lblRange);

        GroupLayout.ParallelGroup para2 = layout.createParallelGroup();
        para2.addComponent(txtEncoding, GroupLayout.PREFERRED_SIZE, 80, GroupLayout.PREFERRED_SIZE);
        para2.addGroup(seq1);
        para2.addComponent(rbtnAuto);
        para2.addComponent(rbtnAscii);
        para2.addComponent(rbtnBinary);

        GroupLayout.SequentialGroup seq2 = layout.createSequentialGroup();
        seq2.addGap(30);
        seq2.addGroup(para1);
        seq2.addPreferredGap(LayoutStyle.ComponentPlacement.RELATED);
        seq2.addGroup(para2);

        GroupLayout.ParallelGroup para3 = layout.createParallelGroup();
        para3.addComponent(txtFile, GroupLayout.PREFERRED_SIZE, 80, GroupLayout.PREFERRED_SIZE);
        para3.addComponent(btnAdd);
        para3.addComponent(btnRemove);

        GroupLayout.SequentialGroup seq3 = layout.createSequentialGroup();
        seq3.addGap(80);
        seq3.addComponent(scrollList, GroupLayout.PREFERRED_SIZE, 100, GroupLayout.PREFERRED_SIZE);
        seq3.addGap(30);
        seq3.addGroup(para3);
        seq3.addPreferredGap(LayoutStyle.ComponentPlacement.RELATED);
        seq3.addComponent(lblFile);


        GroupLayout.ParallelGroup horizontal = layout.createParallelGroup();
        horizontal.addGroup(seq2).addGroup(seq3).addComponent(separator);
        horizontal.addGroup(layout.createSequentialGroup().addGap(30).addComponent(lblAscii));
        horizontal.addGroup(GroupLayout.Alignment.TRAILING, layout.createSequentialGroup()
                .addComponent(btnSave).addPreferredGap(LayoutStyle.ComponentPlacement.RELATED).addComponent(btnCancel).addGap(20));
        layout.setHorizontalGroup(horizontal);

        GroupLayout.SequentialGroup vertical = layout.createSequentialGroup();
        vertical.addGap(30);
        vertical.addGroup(layout.createParallelGroup(GroupLayout.Alignment.BASELINE).addComponent(lblEncoding).addComponent(txtEncoding));
        vertical.addPreferredGap(LayoutStyle.ComponentPlacement.UNRELATED);
        vertical.addGroup(layout.createParallelGroup(GroupLayout.Alignment.BASELINE).addComponent(lblPort)
                .addComponent(txtMinPort).addComponent(lblTo).addComponent(txtMaxPort).addComponent(lblRange));

        GroupLayout.SequentialGroup seq4 = layout.createSequentialGroup();
        seq4.addGap(20);
        seq4.addGroup(layout.createParallelGroup(GroupLayout.Alignment.BASELINE).addComponent(txtFile).addComponent(lblFile));
        seq4.addPreferredGap(LayoutStyle.ComponentPlacement.RELATED);
        seq4.addComponent(btnAdd);
        seq4.addPreferredGap(LayoutStyle.ComponentPlacement.RELATED);
        seq4.addComponent(btnRemove);

        vertical.addPreferredGap(LayoutStyle.ComponentPlacement.UNRELATED);
        vertical.addGroup(layout.createParallelGroup(GroupLayout.Alignment.BASELINE).addComponent(lblType).addComponent(rbtnAuto));
        vertical.addComponent(rbtnAscii);
        vertical.addComponent(rbtnBinary);
        vertical.addPreferredGap(LayoutStyle.ComponentPlacement.UNRELATED);
        vertical.addComponent(separator);
        vertical.addPreferredGap(LayoutStyle.ComponentPlacement.UNRELATED);
        vertical.addComponent(lblAscii);

        vertical.addGroup(layout.createParallelGroup(GroupLayout.Alignment.BASELINE)
                .addComponent(scrollList, GroupLayout.PREFERRED_SIZE, 150, GroupLayout.PREFERRED_SIZE).addGroup(seq4));
        vertical.addPreferredGap(LayoutStyle.ComponentPlacement.UNRELATED);
        vertical.addGroup(layout.createParallelGroup().addComponent(btnSave).addComponent(btnCancel));
        vertical.addGap(20);
        layout.setVerticalGroup(vertical);
    }

}
